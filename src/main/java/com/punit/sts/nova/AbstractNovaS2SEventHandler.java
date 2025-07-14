    package com.punit.sts.nova;

    import com.punit.sts.nova.event.*;
    import com.punit.sts.nova.event.*;
    import com.punit.sts.nova.io.QueuedUlawInputStream;
    import org.springframework.beans.factory.annotation.Value;
    import com.punit.sts.nova.observer.InteractObserver;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import software.amazon.awssdk.profiles.Profile;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.polly.PollyClient;
    import software.amazon.awssdk.services.polly.model.Engine;
    import software.amazon.awssdk.services.polly.model.OutputFormat;
    import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
    import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
    import software.amazon.awssdk.core.ResponseInputStream;

    import javax.sound.sampled.AudioFormat;
    import javax.sound.sampled.AudioInputStream;
    import javax.sound.sampled.AudioSystem;
    import javax.sound.sampled.UnsupportedAudioFileException;
    import java.io.*;
    import java.util.Base64;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.UUID;

    import static com.punit.sts.constants.SonicAudioConfig.SAMPLE_RATE_STR;

    /**
     * Common NovaS2SEventHandler functionality.
     */
    public abstract class AbstractNovaS2SEventHandler implements NovaS2SEventHandler {
        private static final Logger log = LoggerFactory.getLogger(AbstractNovaS2SEventHandler.class);
        private static final String ERROR_AUDIO_FILE = "error.wav";
        private static final Base64.Decoder decoder = Base64.getDecoder();
        private final QueuedUlawInputStream audioStream = new QueuedUlawInputStream();
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final PollyClient pollyClient;
        private InteractObserver<NovaSonicEvent> outbound;
        private String promptName;
        private boolean debugAudioOutput;
        private boolean playedErrorSound = false;
        @Value("${nova.polly:false}")
        private boolean polly = false;  //Always use Polly for voice responses
        @Value("${nova.sarvam:false}")
        private boolean sarvam = true;  // Always use sarvam for voice responses
        private boolean displayAssistantText = false;
        // Polly configuration with default values
        private final String voiceId = System.getenv().getOrDefault("POLLY_VOICE_ID", "Kajal");
        private final String engineType = System.getenv().getOrDefault("POLLY_ENGINE", "neural");
        private final String languageCode = System.getenv().getOrDefault("POLLY_LANGUAGE_CODE", "hi-IN");
        private final String sampleRate = System.getenv().getOrDefault("POLLY_SAMPLE_RATE", SAMPLE_RATE_STR);
        SarvamTTSClient sarvamClient;
        public AbstractNovaS2SEventHandler() {
            this(null);
        }

        public AbstractNovaS2SEventHandler(InteractObserver<NovaSonicEvent> outbound) {
            this.outbound = outbound;
            debugAudioOutput = "true".equalsIgnoreCase(System.getenv().getOrDefault("DEBUG_AUDIO_OUTPUT", "false"));

            this.pollyClient = PollyClient.builder().region(Region.US_EAST_1).build();
            this.sarvamClient = sarvam ? new SarvamTTSClient() : null;
        }

        @Override
        public void handleCompletionStart(JsonNode node) {
            log.info("Completion started for node: {}", node);
            promptName = node.get("promptName").asText();
            log.info("Completion started with promptId: {}", promptName);
        }

        @Override
        public void handleContentStart(JsonNode node) {

            System.out.println("TextOutput-handleContentStart" +   node.get("content"));
            JsonNode contentStart = node.get("content");
            this.displayAssistantText = false;

            if (contentStart != null && contentStart.has("additionalModelFields")) {
                try {
                    String additionalFieldsStr = contentStart.get("additionalModelFields").asText();
                    System.out.println("Additional model fields: " + additionalFieldsStr);

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode additionalFields = mapper.readTree(additionalFieldsStr);

                    if (additionalFields.has("generationStage") &&
                            "SPECULATIVE".equalsIgnoreCase(additionalFields.get("generationStage").asText())) {
                        this.displayAssistantText = true;
                        System.out.println("Received speculative content"+contentStart.toPrettyString());
                    }else {
                        System.out.println("Received Final content"+contentStart.toPrettyString());

                    }

                } catch (Exception e) {
                    System.err.println("Error parsing additionalModelFields: " + e.getMessage());
                }
            }


        }

        @Override
        public void handleTextOutput(JsonNode node) {

            JsonNode contentNode = node.get("content");
            JsonNode roleNode = node.get("role");

            if (roleNode == null || contentNode == null) {
                System.err.println("handleTextOutput: Missing role or content");
            }

            String content = contentNode.asText();
            String role = roleNode.asText();

            System.out.println("TextOutput: " + content);
            System.out.println("handleTextOutput: role = " + role);


            if ("USER".equalsIgnoreCase(role) || ("ASSISTANT".equalsIgnoreCase(role) && this.displayAssistantText)) {

            if (polly) {
                System.out.println("POLLY:Running polly output " + polly);

                try {
                    // Create the speech synthesis request using Polly
                    SynthesizeSpeechRequest synthesizeSpeechRequest = SynthesizeSpeechRequest.builder()
                            .text(content)
                            .voiceId(voiceId)
                            .engine(engineType.equalsIgnoreCase("neural") ? Engine.NEURAL : Engine.STANDARD)
                            .languageCode(languageCode)
                            .outputFormat(OutputFormat.PCM)
                            .sampleRate(sampleRate)
                            .build();

                    //Call Amazon Polly to synthesize the text
                    ResponseInputStream<SynthesizeSpeechResponse> synthesisResponse = pollyClient.synthesizeSpeech(synthesizeSpeechRequest);

                    // Get the audio stream and append to our stream
                    byte[] audioData = synthesisResponse.readAllBytes();
                    audioStream.append(audioData);

                } catch (Exception e) {
                    log.error("POLLY:Failed to synthesize speech using Amazon Polly", e);
                    onError(e);
                }

            } else if (sarvam) {
                System.out.println("SARVAM:Running sarvam output " + sarvam);

                try {
                    // Call Sarvam REST API and get audio bytes
                    byte[] audio = sarvamClient.synthesize(content, "en-IN");  // You can make language configurable if needed

                    // Append audio to stream
                    if (audio.length > 0) {
                        audioStream.append(audio);
                    }

                } catch (Exception e) {
                    log.error("SARVAM: Failed to synthesize speech using Sarvam TTS", e);
                    onError(e);
                }
            }
            }
        }
        @Override
        public void handleAudioOutput(JsonNode node) {
            String content = node.get("content").asText();
            String role = node.get("role").asText();


          if (!(polly || sarvam)){

                System.out.println("Running Nova output ");

                try {
                    byte[] data=decoder.decode(content);
                    audioStream.append(data);

                } catch (Exception e) {
                    log.error("Failed to synthesize speech using Amazon Polly", e);
                    onError(e);
                }
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
