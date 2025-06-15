package com.punit.AWSPe.handler;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class VoiceWebSocketHandler extends BinaryWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(VoiceWebSocketHandler.class);
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("New WebSocket connection established: " + session.getId());
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        // Handle incoming voice data
        byte[] voiceData = message.getPayload().array();
        
        // Broadcasting voice data to all other connected clients
        for (WebSocketSession recipient : sessions.values()) {
            if (!recipient.getId().equals(session.getId()) && recipient.isOpen()) {
                recipient.sendMessage(new BinaryMessage(voiceData));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        logger.info("WebSocket connection closed: " + session.getId());
        sessions.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Error in WebSocket transport", exception);
        if (session.isOpen()) {
            session.close();
        }
        sessions.remove(session.getId());
    }
}