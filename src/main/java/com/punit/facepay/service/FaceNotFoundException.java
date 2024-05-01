package com.punit.facepay.service;

public class FaceNotFoundException extends Exception {

	public FaceNotFoundException() {
		super("Face not found in the image.");
	}

	public FaceNotFoundException(String message) {
		super(message);
	}

	public FaceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public FaceNotFoundException(Throwable cause) {
		super(cause);
	}
}