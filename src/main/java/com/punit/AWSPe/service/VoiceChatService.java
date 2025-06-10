package com.punit.AWSPe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.transcribestreaming.model.*;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VoiceChatService {
    private static final Logger logger = LoggerFactory.getLogger(VoiceChatService.class);
    private final ConcurrentHashMap<String, TranscriptionSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    private TranscribeStreamingAsyncClient transcribeClient;

    @Autowired
    private BedrockRuntimeClient bedrockClient;

    @Autowired
    private PollyClient pollyClient;

    private static class TranscriptionSession {
        AudioStreamPublisher audioPublisher;
        StringBuilder transcriptionBuffer;
        CompletableFuture<Void> transcriptionFuture;
        
        TranscriptionSession() {
            this.audioPublisher = new AudioStreamPublisher();
            this.transcriptionBuffer = new StringBuilder();
            this.transcriptionFuture = new CompletableFuture<>();
        }
    }

    public void initializeSession(String sessionId) {
        sessions.put(sessionId, new TranscriptionSession());
        startTranscription(sessionId);
    }

    public void cleanupSession(String sessionId) {
        TranscriptionSession session = sessions.remove(sessionId);
        if (session != null && session.audioPublisher != null) {
            session.audioPublisher.close();
        }
    }

    public CompletableFuture<ByteBuffer> processAudioChunk(String sessionId, ByteBuffer audioData) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (audioData == null) {
            throw new IllegalArgumentException("Audio data cannot be null");
        }

        TranscriptionSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalStateException("Session not found: " + sessionId);
        }
        if (session.audioPublisher == null) {
            throw new IllegalStateException("Audio publisher not initialized for session: " + sessionId);
        }

        try {
            // Add audio data to the publisher
            session.audioPublisher.addAudioChunk(audioData);

            // If we have accumulated enough transcribed text, process it
            String transcribedText;
            synchronized (session.transcriptionBuffer) {
                if (session.transcriptionBuffer.length() > 0) {
                    transcribedText = session.transcriptionBuffer.toString();
                    session.transcriptionBuffer.setLength(0); // Clear the buffer
                } else {
                    return CompletableFuture.completedFuture(ByteBuffer.allocate(0));
                }
            }
            
            return processTranscribedText(transcribedText)
                .thenCompose(response -> {
                    if (response == null || response.trim().isEmpty()) {
                        return CompletableFuture.completedFuture(ByteBuffer.allocate(0));
                    }
                    return convertTextToSpeech(response);
                })
                .exceptionally(throwable -> {
                    logger.error("Error processing audio chunk: {}", throwable.getMessage());
                    return ByteBuffer.allocate(0);
                });
        } catch (Exception e) {
            logger.error("Error processing audio chunk: {}", e.getMessage());
            return CompletableFuture.completedFuture(ByteBuffer.allocate(0));
        }
    }

    private void startTranscription(String sessionId) {
        TranscriptionSession session = sessions.get(sessionId);
        
        StartStreamTranscriptionRequest request = StartStreamTranscriptionRequest.builder()
            .languageCode(LanguageCode.EN_US)
            .mediaEncoding(MediaEncoding.PCM)
            .mediaSampleRateHertz(16000)
            .build();

        StartStreamTranscriptionResponseHandler responseHandler = StartStreamTranscriptionResponseHandler
            .builder()
            .onResponse(r -> logger.debug("Received initial response: {}", r))
            .onError(e -> {
                logger.error("Error during transcription: {}", e);
                session.transcriptionFuture.completeExceptionally(e);
            })
            .onComplete(() -> {
                logger.info("Transcription completed");
                session.transcriptionFuture.complete(null);
            })
            .subscriber(event -> {
                if (event instanceof TranscriptEvent) {
                    TranscriptEvent transcriptEvent = (TranscriptEvent) event;
                    transcriptEvent.transcript().results().forEach(result -> {
                        if (!result.isPartial()) {
                            result.alternatives().forEach(alt -> {
                                synchronized (session.transcriptionBuffer) {
                                    session.transcriptionBuffer.append(alt.transcript());
                                }
                            });
                        }
                    });
                }
            })
            .build();

        //transcribeClient.startStreamTranscription(request, responseHandler, session.audioPublisher);
    }

    private CompletableFuture<String> processTranscribedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return CompletableFuture.completedFuture("");
        }

        // Create Bedrock request for Claude
        JSONObject requestBody = new JSONObject()
            .put("prompt", "\n\nHuman: " + text + "\n\nAssistant: ")
            .put("max_tokens_to_sample", 300)
            .put("temperature", 0.7)
            .put("top_p", 1)
            .put("anthropic_version", "bedrock-2023-05-31");

        return CompletableFuture.supplyAsync(() -> {
            try {
                var response = bedrockClient.invokeModel(req -> req
                    .modelId("anthropic.claude-v2")
                    .contentType("application/json")
                    .body(SdkBytes.fromUtf8String(requestBody.toString())));

                if (response == null || response.body() == null) {
                    throw new RuntimeException("Null response from Bedrock");
                }

                String responseBody = response.body().asUtf8String();
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                // Handle both possible response formats
                String completion;
                if (jsonResponse.has("completion")) {
                    completion = jsonResponse.getString("completion");
                } else if (jsonResponse.has("content")) {
                    completion = jsonResponse.getString("content");
                } else {
                    throw new RuntimeException("Unexpected response format from Bedrock");
                }
                
                return completion != null ? completion.trim() : "";
            } catch (Exception e) {
                logger.error("Error processing text with Bedrock: {}", e.getMessage());
                throw new RuntimeException("Failed to process text with AI", e);
            }
        });
    }

    private CompletableFuture<ByteBuffer> convertTextToSpeech(String text) {
        if (text == null || text.trim().isEmpty()) {
            return CompletableFuture.completedFuture(ByteBuffer.allocate(0));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId("Joanna")
                    .engine("neural")
                    .build();

                var responseStream = pollyClient.synthesizeSpeech(request);
                if (responseStream == null || responseStream.response() == null) {
                    logger.error("Null response from Polly service");
                    return ByteBuffer.allocate(0);
                }
                
                // Read the audio stream into a byte array
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[4096];
                    int read;
//                    while ((read = responseStream.readAllBytes()) > -1) {
//                        baos.write(buffer, 0, read);
//                    }
                    return ByteBuffer.wrap(baos.toByteArray());
                }
            } catch (Exception e) {
                logger.error("Error converting text to speech: {}", e.getMessage());
                throw new RuntimeException("Failed to convert text to speech", e);
            }
        });
    }
}