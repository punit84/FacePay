package com.punit.AWSPe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.punit.AWSPe.handler.VoiceWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceWebSocketHandler(), "/voice")
               .setAllowedOrigins("*"); // In production, specify exact origins
    }

    @Bean
    public WebSocketHandler voiceWebSocketHandler() {
        return new VoiceWebSocketHandler();
    }
}