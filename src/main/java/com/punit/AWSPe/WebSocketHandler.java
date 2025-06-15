package com.punit.AWSPe;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages here
        String payload = message.getPayload();
        // Echo the message back to the client
        session.sendMessage(new TextMessage("Server received: " + payload));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Called when a new WebSocket connection is established
        session.sendMessage(new TextMessage("Connected to WebSocket server"));
    }
}