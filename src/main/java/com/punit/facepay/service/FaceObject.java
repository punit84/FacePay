package com.punit.facepay.service;

/**
 * Represents a face object with associated metadata such as face ID, URL, and confidence score.
 * This class is used to store and manage information about detected faces in the system.
 */
public class FaceObject {
	
	/**
     * Constructs a new FaceObject with the specified parameters.
     *
     * @param faceid The unique identifier for the face
     * @param faceURL The URL where the face image is stored
     * @param score The confidence score for the face detection/match
     */
	public FaceObject(String faceid, String faceURL, float score) {
		super();
		this.faceid = faceid;
		this.faceURL = faceURL;
		this.score = score;
	}

	String faceid;
	
	String faceURL;
	
	float score;
	
	/**
     * Gets the face ID.
     *
     * @return the face identifier
     */
	public String getFaceid() {
		return faceid;
	}

	/**
     * Sets the face ID.
     *
     * @param faceid the face identifier to set
     */
	public void setFaceid(String faceid) {
		this.faceid = faceid;
	}

	/**
     * Gets the URL where the face image is stored.
     *
     * @return the face image URL
     */
	public String getFaceURL() {
		return faceURL;
	}

	/**
     * Sets the URL where the face image is stored.
     *
     * @param faceURL the face image URL to set
     */
	public void setFaceURL(String faceURL) {
		this.faceURL = faceURL;
	}

	/**
     * Gets the confidence score for the face detection/match.
     *
     * @return the confidence score
     */
	public float getScore() {
		return score;
	}

	/**
     * Sets the confidence score for the face detection/match.
     *
     * @param score the confidence score to set
     */
	public void setScore(float score) {
		this.score = score;
	}

	/**
     * Returns a string representation of the face object.
     *
     * @return a string containing all the face object's properties
     */
	public String printValue() {
		return  "{"+ faceid +"," +faceURL+","+score +"}";
	}

}
