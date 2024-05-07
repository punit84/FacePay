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

	private String upi_id ;
	public String getUpi_id() {
		return upi_id;
	}
	public void setUpi_id(String upi_id) {
		this.upi_id = upi_id;
	}
	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	public String getPerson_s3_key() {
		return person_s3_key;
	}
	public void setPerson_s3_key(String person_s3_key) {
		this.person_s3_key = person_s3_key;
	}
	public String getModel_endpoint() {
		return model_endpoint;
	}
	public void setModel_endpoint(String model_endpoint) {
		this.model_endpoint = model_endpoint;
	}
	public String getReduction_ratio() {
		return reduction_ratio;
	}
	public void setReduction_ratio(String reduction_ratio) {
		this.reduction_ratio = reduction_ratio;
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
	public String getNum_inference_steps() {
		return num_inference_steps;
	}
	public void setNum_inference_steps(String num_inference_steps) {
		this.num_inference_steps = num_inference_steps;
	}
	public String getNum_images_per_prompt() {
		return num_images_per_prompt;
	}
	public void setNum_images_per_prompt(String num_images_per_prompt) {
		this.num_images_per_prompt = num_images_per_prompt;
	}
	public String getStrength() {
		return strength;
	}
	public void setStrength(String strength) {
		this.strength = strength;
	}
	public String getGuidance_scale() {
		return guidance_scale;
	}
	public void setGuidance_scale(String guidance_scale) {
		this.guidance_scale = guidance_scale;
	}
	public String getControlnet_1_conditioning_scale() {
		return controlnet_1_conditioning_scale;
	}
	public void setControlnet_1_conditioning_scale(String controlnet_1_conditioning_scale) {
		this.controlnet_1_conditioning_scale = controlnet_1_conditioning_scale;
	}
	public String getControlnet_1_guidance_start() {
		return controlnet_1_guidance_start;
	}
	public void setControlnet_1_guidance_start(String controlnet_1_guidance_start) {
		this.controlnet_1_guidance_start = controlnet_1_guidance_start;
	}
	public String getControlnet_1_guidance_end() {
		return controlnet_1_guidance_end;
	}
	public void setControlnet_1_guidance_end(String controlnet_1_guidance_end) {
		this.controlnet_1_guidance_end = controlnet_1_guidance_end;
	}
	public String getControlnet_2_conditioning_scale() {
		return controlnet_2_conditioning_scale;
	}
	public void setControlnet_2_conditioning_scale(String controlnet_2_conditioning_scale) {
		this.controlnet_2_conditioning_scale = controlnet_2_conditioning_scale;
	}
	public String getControlnet_2_guidance_start() {
		return controlnet_2_guidance_start;
	}
	public void setControlnet_2_guidance_start(String controlnet_2_guidance_start) {
		this.controlnet_2_guidance_start = controlnet_2_guidance_start;
	}
	public String getControlnet_2_guidance_end() {
		return controlnet_2_guidance_end;
	}
	public void setControlnet_2_guidance_end(String controlnet_2_guidance_end) {
		this.controlnet_2_guidance_end = controlnet_2_guidance_end;
	}
	private String bucket = "qart-test-bucket";
	private String person_s3_key ;
	private String model_endpoint = "qart-face-async-02-05-24-11-52-25";
	private String reduction_ratio = "0.70";
	private String theme = "0";
	private String seed = "1441837673";
	private String num_inference_steps = "50";
	private String num_images_per_prompt = "1";
	private String strength = "0.75";
	private String guidance_scale = "7.00";
	private String controlnet_1_conditioning_scale = "0.85";
	private String controlnet_1_guidance_start = "0.20";
	private String controlnet_1_guidance_end = "0.96";
	private String controlnet_2_conditioning_scale = "0.55";
	private String controlnet_2_guidance_start = "0.18";
	private String controlnet_2_guidance_end = "0.80";


	


}