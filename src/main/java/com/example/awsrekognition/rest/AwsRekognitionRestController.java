package com.example.awsrekognition.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.awsrekognition.service.AwsRekognitionService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AwsRekognitionRestController {

	private AwsRekognitionService awsRekognitionService;

	public AwsRekognitionRestController(AwsRekognitionService awsRekognitionService) {
		this.awsRekognitionService = awsRekognitionService;
	}

	@PostMapping("/facepay")
	public Object facepay(@RequestParam MultipartFile myFile) throws IOException {
		return ResponseEntity.ok(awsRekognitionService.detectLabels(myFile));
	}

	@PostMapping("/facepayImage")
	public Object facepayImage(@RequestParam String image) throws IOException {
		return ResponseEntity.ok(awsRekognitionService.detectLabelsImage(image));

	}
}