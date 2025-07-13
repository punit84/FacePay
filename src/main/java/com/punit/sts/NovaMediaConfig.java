package com.punit.sts;

import org.mjsip.ua.MediaConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NovaMediaConfig extends MediaConfig {
    @Value("${nova.voice.id:en_gb_amy}")
    private String novaVoiceId;

    @Value("${nova.prompt.default}")
    private String novaPrompt;

    @Value("${nova.model.max-tokens:1024}")
    private int novaMaxTokens;

    @Value("${nova.model.top-p:0.9}")
    private float novaTopP;

    @Value("${nova.model.temperature:0.9}")
    private float novaTemperature;

    public String getNovaVoiceId() {
        return novaVoiceId;
    }

    public void setNovaVoiceId(String novaVoiceId) {
        this.novaVoiceId = novaVoiceId;
    }

    public String getNovaPrompt() {
        return novaPrompt;
    }

    public void setNovaPrompt(String novaPrompt) {
        this.novaPrompt = novaPrompt;
    }

    public int getNovaMaxTokens() {
        return novaMaxTokens;
    }

    public void setNovaMaxTokens(int novaMaxTokens) {
        this.novaMaxTokens = novaMaxTokens;
    }

    public float getNovaTopP() {
        return novaTopP;
    }

    public void setNovaTopP(float novaTopP) {
        this.novaTopP = novaTopP;
    }

    public float getNovaTemperature() {
        return novaTemperature;
    }

    public void setNovaTemperature(float novaTemperature) {
        this.novaTemperature = novaTemperature;
    }
}