package com.punit.AWSPe.config;

import com.punit.AWSPe.nova.utility.NovaSonicBedrockInteractClient;
import com.punit.AWSPe.nova.websocket.InteractWebSocket;
import com.punit.AWSPe.websocket.NovaWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.punit.AWSPe.websocket.SupportWebSocketHandler;
import com.punit.AWSPe.websocket.VoiceChatWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private SupportWebSocketHandler supportWebSocketHandler;

    @Autowired
    private NovaWebSocketHandler novaWebSocketHandler;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        registry.addHandler(supportWebSocketHandler, "/support/chat")
//               .setAllowedOrigins("${app.websocket.allowed-origins:http://localhost:8080}");
        
        registry.addHandler(novaWebSocketHandler, "/voice/chat")
               .setAllowedOrigins("${app.websocket.allowed-origins:http://localhost:8080}");

    }
}