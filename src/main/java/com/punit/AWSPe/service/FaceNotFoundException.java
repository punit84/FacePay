package com.punit.AWSPe.service;

/**
 * Custom exception class for handling cases where a face is not found in an image.
 * This exception is thrown when face detection or recognition operations fail to identify a face.
 */
public class FaceNotFoundException extends Exception {

	/**
     * Constructs a new FaceNotFoundException with no message.
     */
	public FaceNotFoundException() {
		super("Face not found in the image.");
	}

	/**
     * Constructs a new FaceNotFoundException with the specified message.
     * 
     * @param message the detail message
     */
	public FaceNotFoundException(String message) {
		super(message);
	}

	/**
     * Constructs a new FaceNotFoundException with the specified message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
	public FaceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
     * Constructs a new FaceNotFoundException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
	public FaceNotFoundException(Throwable cause) {
		super(cause);
	}
}