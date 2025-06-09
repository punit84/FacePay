package com.punit.AWSPe.nova;

import com.punit.AWSPe.nova.event.*;
import com.punit.AWSPe.nova.io.QueuedUlawInputStream;
import com.punit.AWSPe.nova.observer.InteractObserver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Common NovaS2SEventHandler functionality.
 */
public abstract class AbstractNovaS2SEventHandler implements NovaS2SEventHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractNovaS2SEventHandler.class);
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static final String ERROR_AUDIO_FILE = "error.wav";
    private final QueuedUlawInputStream audioStream = new QueuedUlawInputStream();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private InteractObserver<NovaSonicEvent> outbound;
    private String promptName;
    private boolean debugAudioOutput;
    private boolean playedErrorSound = false;

    public AbstractNovaS2SEventHandler() {
        this(null);
    }

    public AbstractNovaS2SEventHandler(InteractObserver<NovaSonicEvent> outbound) {
        this.outbound = outbound;
        debugAudioOutput = "true".equalsIgnoreCase(System.getenv().getOrDefault("DEBUG_AUDIO_OUTPUT", "false"));
    }

    @Override
    public void handleCompletionStart(JsonNode node) {
        log.info("Completion started for node: {}", node);
        promptName = node.get("promptName").asText();
        log.info("Completion started with promptId: {}", promptName);
    }

    @Override
    public void handleContentStart(JsonNode node) {

    }

    @Override
    public void handleTextOutput(JsonNode node) {

    }

    @Override
    public void handleAudioOutput(JsonNode node) {
        String content = node.get("content").asText();
        String role = node.get("role").asText();
        if (debugAudioOutput) {
            log.info("Received audio output {} from {}", content, role);
        }
        byte[] data = decoder.decode(content);
        try {
            audioStream.append(data);
        } catch (InterruptedException e) {
            log.error("Failed to append audio data to queued input stream", e);
        }
    }

    @Override
    public void handleContentEnd(JsonNode node) {
        log.info("Content end for node: {}", node);
        String contentId = node.get("contentId").asText();
        String stopReason = node.has("stopReason") ? node.get("stopReason").asText() : "";
        log.info("Content ended: {} with reason: {}", contentId, stopReason);
    }

    @Override
    public void handleCompletionEnd(JsonNode node) {
        log.info("Completion end for node: {}", node);
        String stopReason = node.has("stopReason") ? node.get("stopReason").asText() : "";
        log.info("Completion ended with reason: {}", stopReason);
    }

    @Override
    public void onStart() {
        log.info("Session started, playing greeting.");
        String greetingFilename = System.getenv().getOrDefault("GREETING_FILENAME","hello-how.wav");
        try { playAudioFile(greetingFilename); }
        catch (FileNotFoundException e) {
            log.info("{} not found, no greeting will be sent", greetingFilename);
        }
    }

    @Override
    public void onError(Exception e) {
        if (!playedErrorSound) {
            try {
                playAudioFile(ERROR_AUDIO_FILE);
                playedErrorSound = true;
            } catch (FileNotFoundException ex) {
                log.error("Failed to play error audio file", ex);
            }
        }
    }

    @Override
    public void onComplete() {
        log.info("Stream complete");
    }

    @Override
    public InputStream getAudioInputStream() {
        return audioStream;
    }

    @Override
    public void setOutbound(InteractObserver<NovaSonicEvent> outbound) {
        this.outbound = outbound;
    }

    /**
     * Handles the actual invocation of a tool.
     * @param toolUseId The tool use id.
     * @param toolName The tool name.
     * @param content Content provided as a parameter to the invocation.
     * @param output The output node.
     */
    protected abstract void handleToolInvocation(String toolUseId, String toolName, String content, Map<String,Object> output);

    @Override
    public void handleToolUse(JsonNode node, String toolUseId, String toolName, String content) {
        log.info("Tool {} invoked with id={}, content={}", toolName, toolUseId, content);
        String contentName = UUID.randomUUID().toString();
        try {
            Map<String, Object> contentNode = new HashMap<>();
            handleToolInvocation(toolUseId, toolName, content, contentNode);

            ToolResultEvent toolResultEvent = new ToolResultEvent();
            Map<String,Object> toolResult = toolResultEvent.getToolResult().getProperties();
            toolResult.put("promptName", promptName);
            toolResult.put("contentName", contentName);
            toolResult.put("role", "TOOL");
            toolResult.put("content", objectMapper.writeValueAsString(contentNode)); // Ensure proper escaping

            sendToolContentStart(toolUseId, contentName);
            outbound.onNext(toolResultEvent);
            outbound.onNext(ContentEndEvent.create(promptName, contentName));
        } catch (Exception e) {
            throw new RuntimeException("Error creating JSON payload for toolResult", e);
        }
    }

    /**
     * Plays an audio file, either relative to the working directory or from the classpath.
     * @param filename The file name of the file to play.
     */
    protected void playAudioFile(String filename) throws FileNotFoundException {
        InputStream is = null;
        File file = new File(filename);
        if (file.exists()) {
            try { is = new FileInputStream(file); }
            catch (FileNotFoundException e) {
                // we already checked if it exists ... this should never happen
            }
        } else {
            is = getClass().getClassLoader().getResourceAsStream(filename);
        }
        if (is != null) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(is));
                AudioInputStream transcodedStream = AudioSystem.getAudioInputStream(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 2, 8000, false), audioInputStream);
                audioStream.append(transcodedStream.readAllBytes());
                log.debug("Wrote audio from {} to output stream ...", filename);
            } catch (RuntimeException e) {
                log.error("Runtime exception while playing audio from {}", filename, e);
            } catch (InterruptedException e) {
                log.error("Interrupted while appending audio to queued input stream", e);
            } catch (IOException | UnsupportedAudioFileException e) {
                log.error("Failed to load {}", filename, e);
            }
        } else {
            throw new FileNotFoundException("Could not find "+filename);
        }
    }

    private void sendToolContentStart(String toolUseId, String contentName) {
        Map<String,Object> toolResultInputConfig=new HashMap<>();
        toolResultInputConfig.put("toolUseId", toolUseId);
        toolResultInputConfig.put("type", "TEXT");
        toolResultInputConfig.put("textInputConfiguration", MediaConfiguration.builder().mediaType("text/plain").build());

        outbound.onNext(ContentStartEvent.builder()
                .contentStart(ContentStartEvent.ContentStart.builder()
                        .promptName(promptName)
                        .contentName(contentName)
                        .interactive(false)
                        .type("TOOL")
                        .property("toolResultInputConfiguration", toolResultInputConfig)
                        .property("role", "TOOL")
                        .build())
                .build());
    }
}
