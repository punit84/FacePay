package com.punit.facepay.service.helper;

import org.springframework.stereotype.Component;

@Component
public class QArtMessage {

	
/*{"upi_id": "upi://pay?pa=nick.jat007@okicici",
	"bucket": "qart-test-bucket",
	"person_s3_key": "qart/nick.jat007@okicici/person.jpg", 
	"model_endpoint": "qart-face-async-30-04-24-18-29-06",
	"reduction_ratio": "0.70", 
	"theme": "0","seed": "1441837673", 
	"num_inference_steps": "50", 
	"num_images_per_prompt": "1", 
	"strength": "0.75", 
	"guidance_scale": "7.00", 
	"controlnet_1_conditioning_scale": "0.85", "controlnet_1_guidance_start": "0.20", "controlnet_1_guidance_end": "0.96", "controlnet_2_conditioning_scale": "0.55", "controlnet_2_guidance_start": "0.18", "controlnet_2_guidance_end": "0.80"}

*/
	
	private String upiId;
	private String personS3Key;

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

	private double controlNet1GuidanceStart = 0.20;
	private double controlNet1GuidanceEnd = 0.96;
	private double controlNet2ConditioningScale = 0.55;
	private double controlNet2GuidanceStart = 0.18;
	private double controlNet2GuidanceEnd = 0.80;

	// Getters and setters
	public String getUpiId() {
		return upiId;
	}

	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getPersonS3Key() {
		return personS3Key;
	}

	public void setPersonS3Key(String personS3Key) {
		this.personS3Key = personS3Key;
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

	public double getControlnet2GuidanceStart() {
		return controlNet2GuidanceStart;
	}

	public void setControlnet2GuidanceStart(double controlnet2GuidanceStart) {
		this.controlNet2GuidanceStart = controlnet2GuidanceStart;
	}

	public double getControlnet2GuidanceEnd() {
		return controlNet2GuidanceEnd;
	}

	public void setControlnet2GuidanceEnd(double controlnet2GuidanceEnd) {
		this.controlNet2GuidanceEnd = controlnet2GuidanceEnd;
	}
}