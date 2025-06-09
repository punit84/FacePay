package com.punit.facepay.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.punit.facepay.service.VoiceChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoiceChatWebSocketHandler extends BinaryWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(VoiceChatWebSocketHandler.class);
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Autowired
    private VoiceChatService voiceChatService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        logger.info("New voice chat session established: {}", session.getId());
        sessions.put(session.getId(), session);
        voiceChatService.initializeSession(session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        ByteBuffer audioData = message.getPayload();
        String sessionId = session.getId();
        
        try {
            // Process the audio chunk and get AI response
            voiceChatService.processAudioChunk(sessionId, audioData)
                .thenAccept(response -> {
                    try {
                        // Send the synthesized speech back to the client
                        if (session.isOpen()) {
                            session.sendMessage(new BinaryMessage(response));
                        }
                    } catch (IOException e) {
                        logger.error("Error sending response to client: {}", e.getMessage());
                    }
                })
                .exceptionally(throwable -> {
                    logger.error("Error processing audio: {}", throwable.getMessage());
                    return null;
                });
        } catch (Exception e) {
            logger.error("Error in voice chat processing: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        logger.info("Voice chat session closed: {}", session.getId());
        sessions.remove(session.getId());
        voiceChatService.cleanupSession(session.getId());
    }
}