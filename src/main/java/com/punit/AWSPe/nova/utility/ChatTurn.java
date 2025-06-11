package com.punit.AWSPe.nova.utility;

import lombok.Getter;

@Getter
public class ChatTurn {
    private final String role;
    private final String content;

    public ChatTurn(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

}