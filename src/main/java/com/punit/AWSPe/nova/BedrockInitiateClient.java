package com.punit.AWSPe.nova;

import com.punit.AWSPe.nova.websocket.WebSocketServer;

public class BedrockInitiateClient {
    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServer(8081);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
