package com.punit.AWSPe.rest;

import java.io.IOException;
import java.util.Map;

import com.punit.AWSPe.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api")
public class AWSPeRestController {

	@Autowired(required=true)
	private static FaceScanService facepayService;

	@Autowired(required=true)
	private static KYCRestService kycService;

	@Autowired(required=true)
	private static StockPriceService stockService;

	final static Logger logger = LoggerFactory.getLogger(AWSPeRestController.class);
	private final StockPriceService stockPriceService;

	public AWSPeRestController(FaceScanService faceScanService, KYCRestService kycService, StockPriceService stockPriceService) {
		this.facepayService = faceScanService;
		this.kycService = kycService;
		this.stockPriceService = stockPriceService;
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

		String respString = null;
		try {

			respString = facepayService.searchUserDetails(myFile);

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
		
		String responsemSG=facepayService.profile(myFile);
		
        String result = responsemSG.replaceFirst(".*?:", "Image summary by Amazon Bedrock:<br>").trim(); // Replace everything up to the first colon and trim it
        System.out.println(result); // Output: Keep this text

		//responsemSG.replace("Here is a crisp summary in 2 lines, including photo quality details: ", ":");
		
		return ResponseEntity.ok(result);
	}

	@PostMapping("/kyc")
	public ResponseEntity<String> kyc(@RequestParam MultipartFile myFile, @RequestParam String requestType , @RequestParam String docType) throws IOException {
		System.out.println("Fetcing document details"); //

		String result = kycService.kycScan(myFile, requestType, docType, " Fetch text from image in json format");
		result = result.replaceAll("\\bnull\\b", "\"NotFound\"");

		System.out.println("final kyc doc details are: "+result); // Output: Keep this text
		return ResponseEntity.ok(result);
	}

	@PostMapping("/kycReko")
	public ResponseEntity<String> kycReko(@RequestParam MultipartFile myFile, @RequestParam String requestType , @RequestParam String docType) throws IOException {
		System.out.println("Fetcing document details"); //

		String result = kycService.kycScan(myFile, requestType, docType, " Fetch text from image in json format");
		result = result.replaceAll("\\bnull\\b", "\"NotFound\"");

		System.out.println("final kyc doc details are: "+result); // Output: Keep this text
		return ResponseEntity.ok(result);
	}


	@GetMapping ("/stockprice")
	public Object stockprice( @RequestParam String stock) throws IOException {
		String respString = null;
		try {

			respString = stockService.getprice(stock);

			logger.info(" final response is "+respString);
			System.out.println(" final response is "+respString);
			if (respString == null) {
				return ResponseEntity.ok(Configs.FACE_NOTFOUND);
			}
		} catch (Exception e) {
			logger.error("Server error " + e.getMessage());
			return ResponseEntity.ok(Configs.SERVER_ERROR);
		}
		return ResponseEntity.ok(respString);
	}

	@GetMapping("/doctype")
	public Map<String, Object> getDocumentTypes() {
		return kycService.getDocumentTypes();
	}
}