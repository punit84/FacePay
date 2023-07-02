package com.punit.facepay.rest;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.Configs;
import com.punit.facepay.service.FacePayService;

@RestController
@RequestMapping("/api")
public class FacePayRestController {

	private static FacePayService facepayService;

	public FacePayRestController(FacePayService awsRekognitionService) {
		this.facepayService = awsRekognitionService;
	}

	@PostMapping("/facepay")
	public Object facepay(@RequestParam MultipartFile myFile ) throws IOException {
		String respString= facepayService.searchImage(myFile);
		
		if (respString == null) {
			return ResponseEntity.ok(Configs.FACE_NOTFOUND);

		}
		
		return ResponseEntity.ok(respString);
	}
	
	@PostMapping("/addImage")
	public Object addImage(@RequestParam MultipartFile myFile, @RequestParam String imageID) throws IOException {
		System.out.println("***********");

		System.out.println("File name is : "+imageID);

		String respString= facepayService.addImage(myFile, imageID);
	
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