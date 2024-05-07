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
	private String reductionRatio = "0.70";
	private String theme = "0";
	private String seed = "1441837673";
	private String numInferenceSteps = "50";
	private String numImagesPerPrompt = "1";
	private String strength = "0.75";
	private String guidanceScale = "7.00";
	private String controlNet1ConditioningScale = "0.95";
	
	private String controlNet1GuidanceStart = "0.20";
	private String controlNet1GuidanceEnd = "0.96";
	private String controlNet2ConditioningScale = "0.55";
	private String controlNet2GuidanceStart = "0.18";
	private String controlNet2GuidanceEnd = "0.80";
	public String getUpiId() {
		return upiId;
	}
	public void setUpiId(String upiId) {
		this.upiId = upiId;
	}
	public String getPersonS3Key() {
		return personS3Key;
	}
	public void setPersonS3Key(String personS3Key) {
		this.personS3Key = personS3Key;
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
	public String getReductionRatio() {
		return reductionRatio;
	}
	public void setReductionRatio(String reductionRatio) {
		this.reductionRatio = reductionRatio;
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public String getSeed() {
		return seed;
	}
	public void setSeed(String seed) {
		this.seed = seed;
	}
	public String getNumInferenceSteps() {
		return numInferenceSteps;
	}
	public void setNumInferenceSteps(String numInferenceSteps) {
		this.numInferenceSteps = numInferenceSteps;
	}
	public String getNumImagesPerPrompt() {
		return numImagesPerPrompt;
	}
	public void setNumImagesPerPrompt(String numImagesPerPrompt) {
		this.numImagesPerPrompt = numImagesPerPrompt;
	}
	public String getStrength() {
		return strength;
	}
	public void setStrength(String strength) {
		this.strength = strength;
	}
	public String getGuidanceScale() {
		return guidanceScale;
	}
	public void setGuidanceScale(String guidanceScale) {
		this.guidanceScale = guidanceScale;
	}
	public String getControlNet1ConditioningScale() {
		return controlNet1ConditioningScale;
	}
	public void setControlNet1ConditioningScale(String controlNet1ConditioningScale) {
		this.controlNet1ConditioningScale = controlNet1ConditioningScale;
	}
	public String getControlNet1GuidanceStart() {
		return controlNet1GuidanceStart;
	}
	public void setControlNet1GuidanceStart(String controlNet1GuidanceStart) {
		this.controlNet1GuidanceStart = controlNet1GuidanceStart;
	}
	public String getControlNet1GuidanceEnd() {
		return controlNet1GuidanceEnd;
	}
	public void setControlNet1GuidanceEnd(String controlNet1GuidanceEnd) {
		this.controlNet1GuidanceEnd = controlNet1GuidanceEnd;
	}
	public String getControlNet2ConditioningScale() {
		return controlNet2ConditioningScale;
	}
	public void setControlNet2ConditioningScale(String controlNet2ConditioningScale) {
		this.controlNet2ConditioningScale = controlNet2ConditioningScale;
	}
	public String getControlNet2GuidanceStart() {
		return controlNet2GuidanceStart;
	}
	public void setControlNet2GuidanceStart(String controlNet2GuidanceStart) {
		this.controlNet2GuidanceStart = controlNet2GuidanceStart;
	}
	public String getControlNet2GuidanceEnd() {
		return controlNet2GuidanceEnd;
	}
	public void setControlNet2GuidanceEnd(String controlNet2GuidanceEnd) {
		this.controlNet2GuidanceEnd = controlNet2GuidanceEnd;
	}
	
	

	
}