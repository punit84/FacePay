package com.punit.AWSPe.service.helper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

	@ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
	public void handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
		//return ResponseEntity.ok("");


		//       return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
		//             .body("The requested media type is not supported by the server.");
	}

}
