package com.punit.facepay.rest;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.FacePayService;

@RestController
@RequestMapping("/api")
public class FacePayRestController {

	private FacePayService facepayService;

	public FacePayRestController(FacePayService awsRekognitionService) {
		this.facepayService = awsRekognitionService;
	}

	@PostMapping("/facepay")
	public Object facepay(@RequestParam MultipartFile myFile) throws IOException {
		String respString= facepayService.searchImage(myFile);
	
		return ResponseEntity.ok(respString);
	}
	
	@PostMapping("/addImage")
	public Object addImage(@RequestParam MultipartFile myFile) throws IOException {
		String respString= facepayService.addImage(myFile);
	
		return ResponseEntity.ok(respString);
	}
	
	@PostMapping("/detectLabel")
	public Object detectLabel(@RequestParam MultipartFile myFile) throws IOException {
		String respString= facepayService.detectLabels(myFile);
	
		return ResponseEntity.ok(respString);
	}


	@PostMapping("/facepayImage")
	public Object facepayImage(@RequestParam String image) throws IOException {
		return ResponseEntity.ok(facepayService.detectLabelsImage(image));

	}
}