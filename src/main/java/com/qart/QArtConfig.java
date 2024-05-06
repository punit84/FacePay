package com.qart;

public class QArtConfig {
    private String url = "http://13.233.155.177";
    private String bucket = "qart-test-bucket";
    //private String personS3Key = "qart/p.khokhar27@okhdfcbank/person.jpg";
    private String modelEndpoint = "qart-face-async-30-04-24-18-29-06";
    private double reductionRatio = 0.70;
    private int theme = 0;
    private int seed = 1441837673;
    private int numInferenceSteps = 50;
    private int numImagesPerPrompt = 1;
    private double strength = 0.75;
    private double guidanceScale = 7.00;
    private double controlNet1ConditioningScale = 0.95;
    private double controlNet1GuidanceStart = 0.20;
    private double controlNet1GuidanceEnd = 0.96;
    private double controlNet2ConditioningScale = 0.55;
    private double controlNet2GuidanceStart = 0.18;
    private double controlNet2GuidanceEnd = 0.80;

    // Getters and Setters

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getModelEndpoint() {
        return modelEndpoint;
    }

    public void setModelEndpoint(String modelEndpoint) {
        this.modelEndpoint = modelEndpoint;
    }

    public double getReductionRatio() {
        return reductionRatio;
    }

    public void setReductionRatio(double reductionRatio) {
        this.reductionRatio = reductionRatio;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getNumInferenceSteps() {
        return numInferenceSteps;
    }

    public void setNumInferenceSteps(int numInferenceSteps) {
        this.numInferenceSteps = numInferenceSteps;
    }

    public int getNumImagesPerPrompt() {
        return numImagesPerPrompt;
    }

    public void setNumImagesPerPrompt(int numImagesPerPrompt) {
        this.numImagesPerPrompt = numImagesPerPrompt;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }

    public double getGuidanceScale() {
        return guidanceScale;
    }

    public void setGuidanceScale(double guidanceScale) {
        this.guidanceScale = guidanceScale;
    }

    public double getControlNet1ConditioningScale() {
        return controlNet1ConditioningScale;
    }

    public void setControlNet1ConditioningScale(double controlNet1ConditioningScale) {
        this.controlNet1ConditioningScale = controlNet1ConditioningScale;
    }

    public double getControlNet1GuidanceStart() {
        return controlNet1GuidanceStart;
    }

    public void setControlNet1GuidanceStart(double controlNet1GuidanceStart) {
        this.controlNet1GuidanceStart = controlNet1GuidanceStart;
    }

    public double getControlNet1GuidanceEnd() {
        return controlNet1GuidanceEnd;
    }

    public void setControlNet1GuidanceEnd(double controlNet1GuidanceEnd) {
        this.controlNet1GuidanceEnd = controlNet1GuidanceEnd;
    }

    public double getControlNet2ConditioningScale() {
        return controlNet2ConditioningScale;
    }

    public void setControlNet2ConditioningScale(double controlNet2ConditioningScale) {
        this.controlNet2ConditioningScale = controlNet2ConditioningScale;
    }

    public double getControlNet2GuidanceStart() {
        return controlNet2GuidanceStart;
    }

    public void setControlNet2GuidanceStart(double controlNet2GuidanceStart) {
        this.controlNet2GuidanceStart = controlNet2GuidanceStart;
    }

    public double getControlNet2GuidanceEnd() {
        return controlNet2GuidanceEnd;
    }

    public void setControlNet2GuidanceEnd(double controlNet2GuidanceEnd) {
        this.controlNet2GuidanceEnd = controlNet2GuidanceEnd;
    }
}
