package com.punit.facepay.rest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.Configs;
import com.punit.facepay.service.FaceNotFoundException;
import com.punit.facepay.service.FaceScanService;

import software.amazon.awssdk.services.ses.SesClient;

@RestController
@RequestMapping("/api")
public class FaceScanRestController {

	@Autowired(required=true)
	private static FaceScanService facepayService;

	final static Logger logger = LoggerFactory.getLogger(FaceScanRestController.class);

	public FaceScanRestController(FaceScanService awsRekognitionService) {
		this.facepayService = awsRekognitionService;
	}

	@PostMapping("/facepay")
	public Object facepay(@RequestParam MultipartFile myFile, @RequestParam String device) throws IOException {
		int type = Configs.DEVICE_ANDROID;
		logger.info("user agent received is :" + device);

	

		String respString = null;
		try {

			respString = facepayService.searchImage(myFile);

			if (respString == null) {
				return ResponseEntity.ok(Configs.FACE_NOTFOUND);

			}
		} catch (FaceNotFoundException e) {
			logger.error("no human face found" + e.getMessage());
			return ResponseEntity.ok(Configs.FACE_NOHUMAN);
		} catch (Exception e) {
			logger.error("Server error " + e.getMessage());
			return ResponseEntity.ok(Configs.SERVER_ERROR);

		}
		
		if (device.toLowerCase().contains("ios")) {
			type = Configs.DEVICE_IOS;
			respString = respString.replace("upi://", "paytm://"); //defaulting to paytm
			logger.info("reqeust received from iphone");
		} else {
			logger.info(device);
		}

		return ResponseEntity.ok(respString);
	}



	@PostMapping("/userinfo")
	public Object userinfo(@RequestParam MultipartFile myFile, @RequestParam String device) throws IOException {
		int type = Configs.DEVICE_ANDROID;
		logger.info("user agent received is :" + device);

		if (device.toLowerCase().contains("ios")) {
			type = Configs.DEVICE_IOS;
			logger.info("reqeust received from iphone");
		} else {
			logger.info(device);
		}

		String respString = null;
		try {

			respString = facepayService.searchUserDetails(myFile, type);

			logger.info(" final response is "+respString);
			System.out.println(" final response is "+respString);
			if (respString == null) {
				return ResponseEntity.ok(Configs.FACE_NOTFOUND);
			}
		} catch (FaceNotFoundException e) {
			logger.error("no human face found" + e.getMessage());
			return ResponseEntity.ok(Configs.FACE_NOHUMAN);
		} catch (Exception e) {
			logger.error("Server error " + e.getMessage());
			return ResponseEntity.ok(Configs.SERVER_ERROR);

		}

		return ResponseEntity.ok(respString);
	}
	@PostMapping("/userbyface")
	public Object userbyface( @RequestParam String faceid) throws IOException {
		String respString = null;

		respString = facepayService.searcUserDetailsByFaceID(faceid);

		logger.info(" final response is "+respString);

		return ResponseEntity.ok(respString);
	}

	@PostMapping("/registerImage")
	public Object registerImage(@RequestParam MultipartFile myFile, @RequestParam String imageID,
			@RequestParam(required = false) String imagePhone, @RequestParam(required = false) String imageEmail)
					throws IOException {
		logger.info("***********");

		logger.info("redirect url  is : " + imageID);

		String respString = facepayService.registerImage(myFile, imageID, imageEmail, imagePhone);

		if (respString == null) {
			return ResponseEntity.ok(Configs.FACE_NOHUMAN);

		}

		return ResponseEntity.ok(respString);
	}


	@PostMapping("/profile")
	public Object profile(@RequestParam MultipartFile myFile) throws IOException {
		return ResponseEntity.ok(facepayService.profile(myFile));
	}
	

}