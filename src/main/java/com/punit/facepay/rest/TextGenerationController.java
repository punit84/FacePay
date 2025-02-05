package com.punit.facepay.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.punit.facepay.service.TextGenerationService;
import java.util.Map;
import java.util.HashMap;

@RestController
public class TextGenerationController {

    @Autowired
    private TextGenerationService textGenerationService;

    @PostMapping("/api/generate-text")
    public ResponseEntity<Map<String, String>> generateText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String generatedText = textGenerationService.generateText(prompt);
        
        Map<String, String> response = new HashMap<>();
        response.put("generatedText", generatedText);
        
        return ResponseEntity.ok(response);
    }
}