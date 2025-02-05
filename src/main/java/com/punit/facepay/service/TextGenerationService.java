package com.punit.facepay.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class TextGenerationService {

    private final RestTemplate restTemplate;
    private final String sagemakerEndpoint = "https://runtime.sagemaker.us-west-2.amazonaws.com/endpoints/jumpstart-dft-meta-textgenerationne-20250205-113035/invocations";

    public TextGenerationService() {
        this.restTemplate = new RestTemplate();
    }

    public String generateText(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode requestBody = mapper.createObjectNode();
            requestBody.put("prompt", prompt);
            requestBody.put("max_new_tokens", 100);
            requestBody.put("temperature", 0.7);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);
            
            String response = restTemplate.postForObject(sagemakerEndpoint, request, String.class);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error generating text: " + e.getMessage(), e);
        }
    }
}