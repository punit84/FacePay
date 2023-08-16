package com.punit.facepay.rest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.Configs;
import com.punit.facepay.service.FacePayService;
import com.punit.facepay.service.helper.UPILinkUtil.DEVICE_TYPE;

@RestController
@RequestMapping("/api")
public class FacePayRestController {

	private static FacePayService facepayService;

	final static Logger logger= LoggerFactory.getLogger(FacePayRestController.class);

	public FacePayRestController(FacePayService awsRekognitionService) {
		this.facepayService = awsRekognitionService;
	}

	@PostMapping("/facepay" )
	public Object facepay(@RequestParam MultipartFile myFile, @RequestHeader(value = "User-Agent") String userAgent ) throws IOException {
		DEVICE_TYPE type= DEVICE_TYPE.ANDROID;
		logger.info("user agent received is :" +userAgent);

		if (userAgent.toLowerCase().contains("apple")) {
			type =DEVICE_TYPE.IPHONE;
			logger.info("reqeust received from iphone");
			logger.info(userAgent);
        } else {
        	
			logger.info(userAgent);
        }
		
		String respString= facepayService.searchImage(myFile, type);

		
		if (respString == null) {
			return ResponseEntity.ok(Configs.FACE_NOTFOUND);

		}
		

		
		return ResponseEntity.ok( respString );
	}
	
	@PostMapping("/addImage")
	public Object addImage(@RequestParam MultipartFile myFile, @RequestParam String imageID) throws IOException {
		logger.info("***********");

		logger.info("File name is : "+imageID);

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
	
	@PostMapping("/profile")
	public Object profile(@RequestParam MultipartFile myFile) throws IOException {
		return ResponseEntity.ok(facepayService.profile(myFile));
	}

}