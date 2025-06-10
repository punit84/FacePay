package com.punit.AWSPe.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.AWSPe.service.SupportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Base64;
import java.util.Map;

@Component
public class SupportWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private SupportService supportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = payload.get("type");
        String content = payload.get("content");
        String language = payload.get("language");

        if ("text".equals(type)) {
            supportService.handleTextMessage(session, content, language);
        } else if ("audio".equals(type)) {
            byte[] audioData = Base64.getDecoder().decode(content);
            supportService.handleAudioMessage(session, audioData, language);
        }
    }
}