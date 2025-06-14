package com.punit.AWSPe.websocket;

import com.punit.AWSPe.nova.utility.NovaSonicBedrockInteractClient;
import com.punit.AWSPe.nova.utility.InteractObserver;
import com.punit.AWSPe.nova.utility.OutputEventsInteractObserver;
import com.punit.AWSPe.nova.websocket.InteractWebSocket;
import com.punit.AWSPe.service.VoiceChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class NovaWebSocketHandler extends BinaryWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(NovaWebSocketHandler.class);
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, NovaSonicBedrockInteractClient> novaClients = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, InteractObserver<String>> inputObservers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> initialRequestFlags = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New voice chat session established: {}", session.getId());
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        initialRequestFlags.put(sessionId, new AtomicBoolean(true));

        // Initialize Bedrock client
        BedrockRuntimeAsyncClient client = BedrockRuntimeAsyncClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create("bedrock-test"))
                .build();

        // Create and store Nova client for this session
        NovaSonicBedrockInteractClient novaClient = new NovaSonicBedrockInteractClient(client);
        novaClients.put(sessionId, novaClient);

        logger.info("Nova client initialized for session: {}", sessionId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        String sessionId = session.getId();
        ByteBuffer audioData = message.getPayload();
        
        try {
            NovaSonicBedrockInteractClient novaClient = novaClients.get(sessionId);
            if (novaClient == null) {
                logger.error("No Nova client found for session: {}", sessionId);
                return;
            }

            // Check if this is the initial request for the session
            AtomicBoolean isInitial = initialRequestFlags.get(sessionId);
            if (isInitial != null && isInitial.get()) {
                // Handle initial request
                handleInitialRequest(session, audioData);
                isInitial.set(false);
            } else {
                // Handle subsequent audio data
                handleAudioData(session, audioData);
            }
        } catch (Exception e) {
            logger.error("Error processing audio message for session {}: {}", sessionId, e.getMessage());
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ex) {
                logger.error("Error closing session: {}", ex.getMessage());
            }
        }
    }

    private void handleInitialRequest(WebSocketSession session, ByteBuffer audioData) {
        String sessionId = session.getId();
        NovaSonicBedrockInteractClient novaClient = novaClients.get(sessionId);
        
        try {
            // Create output observer for the session using the adapter
            OutputEventsInteractObserver outputObserver = new OutputEventsInteractObserver(session);
            
            // Initialize the interaction with Nova
            String initialRequest = "{\"inputAudio\": \"" + java.util.Base64.getEncoder().encodeToString(audioData.array()) + "\"}";
            InteractObserver<String> inputObserver = novaClient.interactMultimodal(initialRequest, outputObserver);
            
            // Store the input observer for future use
            inputObservers.put(sessionId, inputObserver);
            outputObserver.setInputObserver(inputObserver);
            
            logger.info("Initial request processed for session: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error handling initial request for session {}: {}", sessionId, e.getMessage());
            throw e;
        }
    }

    private void handleAudioData(WebSocketSession session, ByteBuffer audioData) {
        String sessionId = session.getId();
        InteractObserver<String> inputObserver = inputObservers.get(sessionId);
        
        if (inputObserver == null) {
            logger.error("No input observer found for session: {}", sessionId);
            return;
        }

        try {
            // Convert audio data to the format expected by Nova
            String audioMessage = "{\"inputAudio\": \"" + java.util.Base64.getEncoder().encodeToString(audioData.array()) + "\"}";
            inputObserver.onNext(audioMessage);
        } catch (Exception e) {
            logger.error("Error processing audio data for session {}: {}", sessionId, e.getMessage());
            inputObserver.onError(e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        logger.info("Voice chat session closed: {} with status: {}", sessionId, status);
        
        // Clean up all resources for this session
        InteractObserver<String> inputObserver = inputObservers.remove(sessionId);
        if (inputObserver != null) {
            inputObserver.onComplete();
        }
        
        novaClients.remove(sessionId);
        sessions.remove(sessionId);
        initialRequestFlags.remove(sessionId);
        
        logger.info("Cleanup completed for session: {}", sessionId);
    }
}