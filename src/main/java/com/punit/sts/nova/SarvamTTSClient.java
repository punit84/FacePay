package com.punit.sts.nova;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SarvamTTSClient {
    private static final Logger log = LoggerFactory.getLogger(SarvamTTSClient.class);
    private static final String SARVAM_TTS_URL = "https://api.sarvam.ai/text-to-speech";
    private static final String OUTPUT_PATH = "output.wav";

    private final HttpClient httpClient;
    private final String apiKey = "sk_tvpazss0_rMRYAq079wYMTHCyYycqebQy" ;
    private final ObjectMapper objectMapper;

    public SarvamTTSClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public byte[] synthesize(String text, String languageCode) {
        try {
            System.out.println("TExt length is " + text.length());
            ByteArrayOutputStream finalAudio = new ByteArrayOutputStream();
            int chunkSize = 160;
            int len = text.length();

            for (int start = 0; start < len; start += chunkSize) {
                int end = Math.min(start + chunkSize, len);
                String chunk = text.substring(start, end);

                Map<String, Object> payload = Map.of(
                        "inputs", Collections.singletonList(chunk),
                        "target_language_code", languageCode != null ? languageCode : "en-IN",
                        "speaker", "amartya",
                        "pitch", 0,
                        "pace", 1.0,
                        "loudness", 1.0,
                        "speech_sample_rate", 8000,
                        "enable_preprocessing", true,
                        "model", "bulbul:v1"
                );

                String requestBody = objectMapper.writeValueAsString(payload);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(SARVAM_TTS_URL))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("api-subscription-key", apiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                log.info("Sending request to Sarvam TTS API for chunk: {}–{}", start, end);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("Sarvam API Status Code: {}", response.statusCode());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Sarvam TTS API failed: " + response.body());
                }

                Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
                if (responseBody.containsKey("audios")) {
                    String base64Audio = ((java.util.List<String>) responseBody.get("audios")).get(0);
                    byte[] audioBytes = Base64.getDecoder().decode(base64Audio);
                    finalAudio.write(audioBytes);
                } else {
                    throw new RuntimeException("No audio found in Sarvam response for chunk: " + chunk);
                }
            }

            return finalAudio.toByteArray();
        } catch (Exception e) {
            log.error("Error calling Sarvam TTS API", e);
            throw new RuntimeException("Failed to call Sarvam TTS API", e);
        }
    }
}