package com.punit.facepay.rest;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.Configs;
import com.punit.facepay.service.FaceNotFoundException;
import com.punit.facepay.service.FaceScanService;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@RestController
@RequestMapping("/api")
public class FaceScanRestController {

	private static FaceScanService facepayService;

	@Autowired
	private SesClient sesClient;

	@Autowired(required=true)
	private CognitoIdentityProviderClient cognitoClient;

	final static Logger logger = LoggerFactory.getLogger(FaceScanRestController.class);

	public FaceScanRestController(FaceScanService awsRekognitionService) {
		this.facepayService = awsRekognitionService;
	}

	@GetMapping("/login")
	public String login(@RequestParam("username") String username, @RequestParam("password") String password) {

		AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder().authFlow("ADMIN_NO_SRP_AUTH")
				.authParameters(Map.of("USERNAME", username, "PASSWORD", password)).clientId("YOUR_CLIENT_ID")
				.userPoolId("YOUR_USER_POOL_ID").build();

		AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
		AuthenticationResultType authResult = authResponse.authenticationResult();

		return authResult.accessToken();
	}

	@PostMapping("/facepay")
	public Object facepay(@RequestParam MultipartFile myFile, @RequestParam String device) throws IOException {
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

			respString = facepayService.searchImage(myFile, type);

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
	@PostMapping("/sendEmail")
	public String sendEmail(@RequestBody Map<String, String> formData) {
		String name = "test";
		String email = "jainpuni@amazon.com";
		String message = "how are you";


		//		String name = formData.get("name");
		//		String email = formData.get("email");
		//		String message = formData.get("message");
		String senderEmail = "jainpuni@amazon.com";
		String recipientEmail = "jainpuni@amazon.com";


		SesClient sesClien1t = SesClient.builder()
				.region(Region.AP_SOUTH_1) // Replace YOUR_REGION with your region, e.g., Region.US_EAST_1
				.build();

		SendEmailRequest emailRequest = SendEmailRequest.builder()
				.source(senderEmail)
				.destination(destination -> destination.toAddresses(recipientEmail))
				.message(Message.builder()
						.subject(Content.builder().data("New Contact Us Message").build())
						.body(Body.builder()
								.text(Content.builder().data("Name: " + name + "\nEmail: " + email + "\n\n" + message).build())
								.build())
						.build())
				.build();

		sesClien1t.sendEmail(emailRequest);

		return "Email sent successfully!";
	}

}