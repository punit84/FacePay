package com.punit.sts.nova;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SarvamTTSClient {
    private static final Logger log = LoggerFactory.getLogger(SarvamTTSClient.class);

    private final WebSocket webSocket;
    private final ByteBuffer audioBuffer = ByteBuffer.allocate(10_000_000);  // 10MB buffer

    public SarvamTTSClient(String apiKey) {
        try {
            this.webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .header("Authorization", "Bearer " + apiKey)
                    .buildAsync(URI.create("wss://api.sarvam.ai/v1/tts/stream"), new SarvamListener(audioBuffer))
                    .join();

            sendConfig();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Sarvam TTS Client", e);
        }
    }


    private void sendConfig() {
        String configJson = """
            {
              "type": "config",
              "data": {
                "model": "bulbul:v2",
                "target_language_code": "en-IN",
                "speaker": "anushka",
                "output_audio_codec": "mp3",
                "min_buffer_size": 50,
                "max_chunk_length": 300,
                "speech_sample_rate": 8000
              }
            }
            """;
        webSocket.sendText(configJson, true);
    }

    public void sendTextChunk(String text) {
        String payload = String.format("""
            {
              "type": "text",
              "data": {
                "text": "%s"
              }
            }
            """, escapeJson(text));
        webSocket.sendText(payload, true);
    }

    public void flush() {
        webSocket.sendText("{\"type\": \"flush\"}", true);
    }

    public void close() {
        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "complete").join();
        log.info("Sarvam WebSocket closed");
    }

    public byte[] getAudioBytesAndReset() {
        audioBuffer.flip();
        byte[] audio = new byte[audioBuffer.remaining()];
        audioBuffer.get(audio);
        audioBuffer.clear();
        return audio;
    }

    private static String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "");
    }

    // make the listener class static and pass audioBuffer explicitly
    private static class SarvamListener implements WebSocket.Listener {
        private final ByteBuffer audioBuffer;

        public SarvamListener(ByteBuffer audioBuffer) {
            this.audioBuffer = audioBuffer;
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket ws, ByteBuffer data, boolean last) {
            byte[] chunk = new byte[data.remaining()];
            data.get(chunk);
            if (audioBuffer.remaining() >= chunk.length) {
                audioBuffer.put(chunk);
            } else {
                LoggerFactory.getLogger(SarvamListener.class).warn("Buffer overflow, dropping audio chunk.");
            }
            return Listener.super.onBinary(ws, data, last);
        }

        @Override public void onOpen(WebSocket ws) {
            LoggerFactory.getLogger(SarvamListener.class).info("WebSocket connection opened.");
        }

        @Override public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
            LoggerFactory.getLogger(SarvamListener.class).info("WebSocket closed: {} - {}", statusCode, reason);
            return Listener.super.onClose(ws, statusCode, reason);
        }

        @Override public void onError(WebSocket ws, Throwable error) {
            LoggerFactory.getLogger(SarvamListener.class).error("WebSocket error", error);
        }

        @Override public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
            LoggerFactory.getLogger(SarvamListener.class).debug("Received text: {}", data);
            return Listener.super.onText(ws, data, last);
        }
    }

}