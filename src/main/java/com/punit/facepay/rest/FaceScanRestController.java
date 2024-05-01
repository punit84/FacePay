package com.punit.facepay.rest;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.Configs;
import com.punit.facepay.service.FaceNotFoundException;
import com.punit.facepay.service.FaceScanService;

@RestController
@RequestMapping("/api")
public class FaceScanRestController {

	private static FaceScanService facepayService;

	final static Logger logger= LoggerFactory.getLogger(FaceScanRestController.class);

	public FaceScanRestController(FaceScanService awsRekognitionService) {
		this.facepayService = awsRekognitionService;
	}

	@PostMapping("/facepay" )
	public Object facepay(@RequestParam MultipartFile myFile, @RequestParam String device ) throws IOException {
		int type= Configs.DEVICE_ANDROID;
		logger.info("user agent received is :" + device);

		if (device.toLowerCase().contains("ios") ) {
			type =Configs.DEVICE_IOS;
			logger.info("reqeust received from iphone");
		} else {
			logger.info(device);
		}

		String respString = null;
		try {

			 respString= facepayService.searchImage(myFile, type);

			if (respString == null) {
				return ResponseEntity.ok(Configs.FACE_NOTFOUND);

			}
		} catch (FaceNotFoundException e) {
			return ResponseEntity.ok(Configs.FACE_NOHUMAN);
		}		
		catch (Exception e) {
			logger.error(e.getMessage());
			return ResponseEntity.ok(Configs.SERVER_ERROR );
			
		}		


		return ResponseEntity.ok( respString );
	}

	@PostMapping("/addImage")
	public Object addImage(@RequestParam MultipartFile myFile, @RequestParam String imageID) throws IOException {
		logger.info("***********");

		logger.info("File name is : "+imageID);

		String respString= facepayService.addImage(myFile, imageID);
		
		if (respString == null) {
			return ResponseEntity.ok(Configs.FACE_NOHUMAN);

		}

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