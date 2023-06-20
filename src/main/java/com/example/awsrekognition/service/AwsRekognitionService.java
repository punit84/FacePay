package com.example.awsrekognition.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

@Service
public class AwsRekognitionService {

//	String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";

  String modelversion ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";

	public String detectLabels(MultipartFile imageToCheck) throws IOException {

		Image souImage = getImage(imageToCheck);
		getRekClient();		
		
		return detectLabels(souImage);
	}

	public String detectLabelsImage(String sourceImage) throws IOException {

		InputStream sourceStream = new FileInputStream(sourceImage);
		SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

		// Create an Image object for the source image

		Image souImage = Image.builder()
				.bytes(sourceBytes)
				.build();

		return detectLabels(souImage);
	}


	private RekognitionClient getRekClient() {
		Region region = Region.AP_SOUTH_1;

		RekognitionClient client = RekognitionClient.builder()
				.region(region)
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();
		return client;
	}

	private Image getImage(MultipartFile imageToCheck) throws IOException {
		Image souImage = Image.builder()
				.bytes(SdkBytes.fromByteArray(imageToCheck.getBytes()))
				.build();
		return souImage;
	}

	public String detectLabels(String imageToCheck) throws IOException {
		//DetectModerationLabelsRequest request = new DetectModerationLabelsRequest()
		//		.withImage(new Image().withBytes(ByteBuffer.wrap(imageToCheck.getBytes())));

		// Create an Image object from the loaded image bytes
	
		Image souImage = Image.builder()
				.bytes(SdkBytes.fromByteArray(imageToCheck.getBytes()))
				.build();
		return detectLabels(souImage);
	}

	public String detectImageCustomLabels( String sourceImage )  {

		Image souImage =null;
		try {
			InputStream sourceStream = new FileInputStream(sourceImage);
			SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

			// Create an Image object for the source image

			souImage = Image.builder()
					.bytes(sourceBytes)
					.build();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return detectLabels(souImage);

	}

	public String detectLabels( Image souImage) {
		String customLable = "punit@paytm";

		DetectCustomLabelsRequest detectCustomLabelsRequest = DetectCustomLabelsRequest.builder()
				.image(souImage)
				.projectVersionArn(modelversion)
				.build();
		try {

			RekognitionClient client= getRekClient();		

			DetectCustomLabelsResponse customLabelsResponse = client.detectCustomLabels(detectCustomLabelsRequest);
			List<CustomLabel> customLabels = customLabelsResponse.customLabels();

			for (CustomLabel customLabel: customLabels) {
				System.out.println(customLabel.name() + ": " + customLabel.confidence().toString());
			}

			if(customLabels.size()==0) {
				System.out.println("no matching label found");
			}else {
				CustomLabel customLabel1=customLabels.get(0);
				customLable = customLabel1.name();
				System.out.println("Detected labels for the given photo: " +customLable);

				if (!customLable.contains("paytm")) {
					customLable = customLabel1 + "@paytm";
					System.out.println("Detected labels for the given photo: " +customLable);

				}
			}
		} catch (RekognitionException e) {
			System.out.println(e.getMessage());
			System.exit(1);


		}
		return customLable;
	}


}