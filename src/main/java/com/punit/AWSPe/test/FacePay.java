package com.punit.AWSPe.test;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import com.punit.AWSPe.service.Configs;
import com.punit.AWSPe.service.helper.RekoUtil;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.AgeRange;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

public class FacePay {
	public static void main(String[] args) {

		RekoUtil rekoUtil = new RekoUtil();
		Region region = Configs.REGION;
		
		
		RekognitionClient rekClient = RekognitionClient.builder()
				.region(region)
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();

		//rekoUtil.deleteAllMyCollection(rekClient);
		
		//rekoUtil.createMyCollection(rekClient, Configs.COLLECTION_ID);

      

		System.out.println("Listing collections");
		//        String modelversion ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";

		//String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";

		rekoUtil.listAllCollections(rekClient);

		//ListFacesInCollection.listFacesCollection(rekClient, collectionId);
		//DetectLabels.detectImageLabels(rekClient, sourceImage);

		//CelebrityInfo.getCelebrityInfo(rekClient, collectionId);
		//SearchFaceMatchingIdCollection.searchFacebyId(rekClient, collectionId, sourceImage);
		//detectImageCustomLabels(rekClient, modelversion,  sourceImage);


		//		aws rekognition detect-custom-labels \
		//		  --project-version-arn "arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992" \
		//		  --image '{"S3Object": {"Bucket": "MY_BUCKET","Name": "PATH_TO_MY_IMAGE"}}' \
		//		  --region ap-south-1

	}

	public static void detectImageCustomLabels(RekognitionClient rekClient, String arn, String sourceImage )  {

		try {

			InputStream sourceStream = new FileInputStream(sourceImage);
			SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

			// Create an Image object for the source image

			Image souImage = Image.builder()
					.bytes(sourceBytes)
					.build();
			DetectCustomLabelsRequest detectCustomLabelsRequest = DetectCustomLabelsRequest.builder()
					.image(souImage)
					.projectVersionArn(arn)
					.build();

			DetectFacesRequest facesRequest = DetectFacesRequest.builder()
					.attributes(Attribute.ALL)
					.image(souImage)
					.build();

			DetectFacesResponse facesResponse = rekClient.detectFaces(facesRequest);
			List<FaceDetail> faceDetails = facesResponse.faceDetails();

			for (FaceDetail face : faceDetails) {
				System.out.println(face);
				AgeRange ageRange = face.ageRange();
				System.out.println("The detected face is estimated to be between "
						+ ageRange.low().toString() + " and " + ageRange.high().toString()
						+ " years old.");

				System.out.println("There is a smile : "+face.smile().value().toString());
			}
			//detectLable(rekClient, detectCustomLabelsRequest);
		} catch (RekognitionException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void detectLable(RekognitionClient rekClient, DetectCustomLabelsRequest detectCustomLabelsRequest) {
		DetectCustomLabelsResponse customLabelsResponse = rekClient.detectCustomLabels(detectCustomLabelsRequest);
		List<CustomLabel> customLabels = customLabelsResponse.customLabels();

		for (CustomLabel customLabel: customLabels) {
			System.out.println(customLabel.name() + ": " + customLabel.confidence().toString());
		}
		// CustomLabel customLabel1=customLabels.get(0);
		// System.out.println("Detected labels for the given photo: " +customLabel1.name());

		if(customLabels.size()==0) {
			System.out.println("no matching label found");
		}
	}

}
