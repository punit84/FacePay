package com.punit.facepay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.VoiceId;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Service class for handling real-time voice and text communication support.
 * Provides functionality for speech-to-text, text-to-speech, and AI-powered responses.
 */
@Service
public class SupportService {

    private static final Logger logger = LoggerFactory.getLogger(SupportService.class);

    @Autowired
    private TranscribeStreamingAsyncClient transcribeClient;

    @Autowired
    private BedrockRuntimeClient bedrockClient;

    @Autowired
    private PollyClient pollyClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, VoiceId> languageToVoice = Map.of(
        "en-IN", VoiceId.RAVEENA,
        "hi-IN", VoiceId.ADITI,
        "ta-IN", VoiceId.ADITI,
        "te-IN", VoiceId.ADITI,
        "mr-IN", VoiceId.ADITI,
        "en-US", VoiceId.JOANNA,
        "en-GB", VoiceId.AMY
    );

    private final Set<String> supportedLanguages = Set.of(
        "en-IN", "hi-IN", "ta-IN", "te-IN", "mr-IN", "en-US", "en-GB"
    );

    /**
     * Handles incoming text messages, processes them with AI, and responds with text and audio.
     *
     * @param session WebSocket session for the client connection
     * @param message The text message to process
     * @param language The target language for speech synthesis
     */
    /**
     * Handles incoming text messages, processes them with AI, and responds with text and audio.
     * Validates input parameters and language support before processing.
     *
     * @param session WebSocket session for the client connection
     * @param message The text message to process
     * @param language The target language for speech synthesis
     * @throws IllegalArgumentException if session is null, message is empty/null, or language is unsupported
     */
    public void handleTextMessage(WebSocketSession session, String message, String language) {
        // Validate session
        if (session == null) {
            logger.error("Null WebSocket session provided");
            throw new IllegalArgumentException("WebSocket session cannot be null");
        }

        // Validate message
        if (message == null || message.trim().isEmpty()) {
            logger.error("Empty or null message received");
            try {
                sendWebSocketResponse(session, "error", "Please provide a valid message");
            } catch (IOException e) {
                logger.error("Failed to send error response for empty message", e);
            }
            return;
        }

        // Validate language
        if (!supportedLanguages.contains(language)) {
            String errorMsg = "Unsupported language code: " + language + ". Supported languages: " + String.join(", ", supportedLanguages);
            logger.error(errorMsg);
            try {
                sendWebSocketResponse(session, "error", errorMsg);
            } catch (IOException e) {
                logger.error("Failed to send language error response", e);
            }
            return;
        }

        try {
            logger.info("Processing text message in language: {}", language);

            // Get response from Bedrock
            String response = getBedrockResponse(message);
            logger.debug("Received AI response: {}", response);

            if (response == null || response.trim().isEmpty()) {
                logger.error("Empty response received from Bedrock");
                sendWebSocketResponse(session, "error", "Failed to generate a response. Please try again.");
                return;
            }

            // Convert response to speech using Polly
            byte[] audioData = synthesizeSpeech(response, language);
            if (audioData.length == 0) {
                logger.warn("Failed to synthesize speech for response");
                sendWebSocketResponse(session, "text", response);
                sendWebSocketResponse(session, "error", "Failed to generate audio response");
                return;
            }

            String base64Audio = Base64.getEncoder().encodeToString(audioData);

            // Send both text and audio responses
            sendWebSocketResponse(session, "text", response);
            sendWebSocketResponse(session, "audio", base64Audio);

        } catch (Exception e) {
            logger.error("Error processing text message: {}", e.getMessage(), e);
            try {
                String errorMessage = "Failed to process your message: " + e.getMessage();
                sendWebSocketResponse(session, "error", errorMessage);
            } catch (IOException ex) {
                logger.error("Failed to send error message to client", ex);
            }
        }
    }

    /**
     * Handles incoming audio messages, transcribes them, processes with AI, and responds with text and audio.
     *
     * @param session WebSocket session for the client connection
     * @param audioData The raw audio data to process
     * @param language The language of the audio input and desired response
     */
    /**
     * Handles incoming audio messages, transcribes them, processes with AI, and responds with text and audio.
     * Uses try-with-resources for proper resource management and includes comprehensive input validation.
     *
     * @param session WebSocket session for the client connection
     * @param audioData The raw audio data to process
     * @param language The language of the audio input and desired response
     * @throws IllegalArgumentException if session is null
     */
    public void handleAudioMessage(WebSocketSession session, byte[] audioData, String language) {
        // Validate session
        if (session == null) {
            logger.error("Null WebSocket session provided");
            throw new IllegalArgumentException("WebSocket session cannot be null");
        }

        // Validate language support
        if (!supportedLanguages.contains(language)) {
            String errorMsg = String.format("Unsupported language code: %s. Supported languages: %s",
                language, String.join(", ", supportedLanguages));
            logger.error(errorMsg);
            try {
                sendWebSocketResponse(session, "error", errorMsg);
            } catch (IOException ex) {
                logger.error("Failed to send language error message to client", ex);
            }
            return;
        }

        // Validate audio data
        if (audioData == null || audioData.length == 0) {
            logger.error("Empty or null audio data received");
            try {
                sendWebSocketResponse(session, "error", "No valid audio data received");
            } catch (IOException ex) {
                logger.error("Failed to send audio error message to client", ex);
            }
            return;
        }

        // Process audio with proper resource management
        try {
            AudioStreamPublisher publisher = new AudioStreamPublisher();
            ByteArrayInputStream audioStream = new ByteArrayInputStream(audioData);
            byte[] buffer = new byte[1024];
            int bytesRead;
//            while ((bytesRead = audioStream.read(buffer)) != -1) {
//                ByteBuffer audioBuffer = ByteBuffer.allocate(bytesRead);
//                audioBuffer.put(buffer, 0, bytesRead);
//                audioBuffer.flip();
//                publisher.addAudioChunk(audioBuffer);
//            }

            logger.info("Processing audio message in language: {}", language);

            // Set up Transcribe streaming request
            StartStreamTranscriptionRequest request = StartStreamTranscriptionRequest.builder()
                    .languageCode(language)
                    .mediaEncoding(MediaEncoding.PCM)
                    .mediaSampleRateHertz(16000)
                    .enablePartialResultsStabilization(true)
                    .build();

            // Create response handler
            StartStreamTranscriptionResponseHandler responseHandler = StartStreamTranscriptionResponseHandler
                    .builder()
                    .onResponse(response -> {
                        logger.debug("Received initial response: {}", response);
                    })
                    .onError(error -> {
                        logger.error("Error in transcription stream: {}", error.getMessage());
                        try {
                            sendWebSocketResponse(session, "error", "Transcription error: " + error.getMessage());
                        } catch (IOException e) {
                            logger.error("Failed to send error message to client", e);
                        }
                    })
                    .onComplete(() -> {
                        logger.debug("Transcription stream completed successfully");
                    })
                    .subscriber(event -> {
                        if (event instanceof TranscriptEvent) {
                            processTranscriptEvent(session, (TranscriptEvent) event, language);
                        }
                    })
                    .build();

            // Start streaming transcription and wait for completion
            CompletableFuture<Void> future = transcribeClient.startStreamTranscription(request, publisher, responseHandler);

            // Wait for transcription to complete with timeout
            try {
                future.get(); // Consider adding timeout parameter if needed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Audio processing was interrupted", e);
            }

        } catch (Exception e) {
            String errorMessage = String.format("Failed to process audio: %s", e.getMessage());
            logger.error("Error processing audio message", e);
            try {
                sendWebSocketResponse(session, "error", errorMessage);
            } catch (IOException ex) {
                logger.error("Failed to send error message to client", ex);
            }
        }
    }

    /**
     * Processes a transcript event from the streaming transcription.
     *
     * @param session WebSocket session
     * @param transcriptEvent The transcript event to process
     * @param language The target language for responses
     */
    private void processTranscriptEvent(WebSocketSession session, TranscriptEvent transcriptEvent, String language) {
        try {
            String transcript = transcriptEvent.transcript().results().get(0)
                    .alternatives().get(0).transcript();

            logger.debug("Transcribed text: {}", transcript);

            // Process the transcript with Bedrock
            String response = getBedrockResponse(transcript);
            logger.debug("AI response: {}", response);

            // Convert response to speech
            byte[] speechData = synthesizeSpeech(response, language);
            if (speechData.length == 0) {
                logger.warn("Failed to synthesize speech for response");
                return;
            }

            String base64Speech = Base64.getEncoder().encodeToString(speechData);

            // Send responses
            sendWebSocketResponse(session, "text", response);
            sendWebSocketResponse(session, "audio", base64Speech);

        } catch (Exception e) {
            logger.error("Error processing transcript event", e);
        }
    }

    /**
     * Gets an AI-generated response from Bedrock for the given input.
     *
     * @param input The text input to process
     * @return The AI-generated response
     */
    private String getBedrockResponse(String input) {
        try {
            String promptText = String.format(
                "{\"prompt\": \"Human: %s\\nAssistant: I am a helpful customer support assistant. I will help you with your query.\\nHuman: %s\\nAssistant:\", \"max_tokens_to_sample\": 500, \"temperature\": 0.7, \"top_p\": 0.9}", 
                input.replace("\"", "\\\""),
                input.replace("\"", "\\\"")
            );

            var response = bedrockClient.invokeModel(req -> req
                .modelId("anthropic.claude-v2")
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(promptText))
            );

            // Parse the response and extract the assistant's reply
            String jsonResponse = response.body().asUtf8String();
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
            return (String) responseMap.get("completion");

        } catch (Exception e) {
            logger.error("Error getting Bedrock response", e);
            return "I apologize, but I encountered an error processing your request.";
        }
    }

    /**
     * Synthesizes speech from text using Amazon Polly.
     *
     * @param text The text to convert to speech
     * @param language The target language/voice for synthesis
     * @return The synthesized audio data
     */
    private byte[] synthesizeSpeech(String text, String language) {
        if (!supportedLanguages.contains(language)) {
            logger.error("Unsupported language code for speech synthesis: {}", language);
            return new byte[0];
        }

        if (text == null || text.trim().isEmpty()) {
            logger.error("Empty text provided for speech synthesis");
            return new byte[0];
        }

        try {
            VoiceId voiceId = languageToVoice.getOrDefault(language, VoiceId.JOANNA);

            // Split long text into chunks if needed (Polly has a 3000 character limit)
            if (text.length() > 3000) {
                logger.warn("Text exceeds Polly's limit. Truncating to 3000 characters.");
                text = text.substring(0, 3000);
            }

            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(voiceId)
                    .outputFormat(OutputFormat.MP3)
                    .engine("neural") // Use neural engine for better quality
                    .build();
            var responseStream = pollyClient.synthesizeSpeech(request);
            if (responseStream == null || responseStream.response() == null) {
                logger.error("Error synthesizing speech: null response from Polly");
                return new byte[0];
            }
            return responseStream.readAllBytes();
        } catch (Exception e) {
            logger.error("Error synthesizing speech: {}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * Sends a response through the WebSocket connection.
     *
     * @param session The WebSocket session
     * @param type The type of response (text, audio, or error)
     * @param content The content of the response
     * @throws IOException if there's an error sending the message
     */
    private void sendWebSocketResponse(WebSocketSession session, String type, String content) throws IOException {
        Map<String, String> response = new HashMap<>();
        response.put("type", type);
        response.put("content", content);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * Handles a support query and returns an AI-generated response.
     *
     * @param query The user's support query
     * @param session The WebSocket session for the client connection
     * @return The AI-generated response or an error message
     */
    public String handleSupportQuery(String query, WebSocketSession session) {
        if (query == null || query.trim().isEmpty()) {
            logger.error("Empty or null query received");
            return "error: Please provide a valid query";
        }

        if (session == null) {
            logger.error("Null WebSocket session");
            return "error: Invalid session";
        }

        try {
            return getBedrockResponse(query);
        } catch (Exception e) {
            logger.error("Error processing support query", e);
            return "error: Failed to process your query. Please try again.";
        }
    }
}