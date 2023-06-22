package com.example.awsrekognition.service;



import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

public class FacePay {
	public static void main(String[] args) {

        String collectionId = "facepay";
        String sourceImage = "/Users/jainpuni/pkj.jpg";
        
        Region region = Region.AP_SOUTH_1;
        RekognitionClient rekClient = RekognitionClient.builder()
            .region(region)
            .credentialsProvider(ProfileCredentialsProvider.create())
            .build();

        System.out.println("Listing collections");
//        String modelversion ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";

       String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";
        
        //ListCollections.listAllCollections(rekClient);
        //ListFacesInCollection.listFacesCollection(rekClient, collectionId);
		//DetectLabels.detectImageLabels(rekClient, sourceImage);
	
		//CelebrityInfo.getCelebrityInfo(rekClient, collectionId);
		//SearchFaceMatchingIdCollection.searchFacebyId(rekClient, collectionId, sourceImage);
		detectImageCustomLabels(rekClient, modelversion,  sourceImage);
		

		
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
        } catch (RekognitionException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
