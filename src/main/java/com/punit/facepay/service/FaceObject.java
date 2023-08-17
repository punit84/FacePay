package com.punit.facepay.service;

public class FaceObject {
	
	public FaceObject(String faceid, String faceURL, float score) {
		super();
		this.faceid = faceid;
		this.faceURL = faceURL;
		this.score = score;
	}

	String faceid;
	
	String faceURL;
	
	float score;
	
	public String getFaceid() {
		return faceid;
	}

	public void setFaceid(String faceid) {
		this.faceid = faceid;
	}

	public String getFaceURL() {
		return faceURL;
	}

	public void setFaceURL(String faceURL) {
		this.faceURL = faceURL;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public String printValue() {
		return  "{"+ faceid +"," +faceURL+","+score +"}";
	}

}
