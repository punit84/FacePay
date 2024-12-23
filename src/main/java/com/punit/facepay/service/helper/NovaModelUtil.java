package com.punit.facepay.service.helper;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.nio.charset.StandardCharsets;

@Component
public class NovaModelUtil {
    
    @Autowired
    private BedrockRuntimeClient bedrockClient;

    public String chatWithNova(String userMessage) {
        try {
            String modelId = "anthropic.claude-3-sonnet-20240229-v1:0";
            
            // Format the prompt as required by Claude
            String formattedPrompt = "Human: " + userMessage + "\n\nAssistant:";
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("prompt", formattedPrompt);
            requestBody.put("max_tokens_to_sample", 4096);
            requestBody.put("temperature", 0.7);
            requestBody.put("stop_sequences", new String[]{"\n\nHuman:"});
            
            // Create the request
            InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(requestBody.toString()))
                .build();

            // Invoke the model
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            // Parse and return the completion
            JSONObject responseJson = new JSONObject(responseBody);
            return responseJson.getString("completion");
            
        } catch (Exception e) {
            throw new RuntimeException("Error chatting with Nova model: " + e.getMessage(), e);
        }
    }
}