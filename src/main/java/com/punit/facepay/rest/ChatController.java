package com.punit.facepay.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.punit.facepay.service.helper.NovaModelUtil;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private NovaModelUtil novaModelUtil;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String response = novaModelUtil.chatWithNova(request.getQuestion());
        return new ChatResponse(response);
    }
}

class ChatRequest {
    private String question;

    // Default constructor
    public ChatRequest() {}

    public ChatRequest(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}

class ChatResponse {
    private String response;

    public ChatResponse(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}