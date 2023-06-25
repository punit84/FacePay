package com.punit.facepay.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.punit.facepay.service.helper.ListCollections;
import com.punit.facepay.service.helper.ListFacesInCollection;
import com.punit.facepay.service.helper.RekoUtil;
import com.punit.facepay.service.helper.SearchFaceMatchingIdCollection;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;

public class FaceCollection {

	private static RekoUtil reko= new RekoUtil();

	public static void main(String[] args) {

		String collectionId = "faceCollection";
		String imageFolder = "/Users/jainpuni/accounts/genAI/images";
		

		Region region = Region.AP_SOUTH_1;
		RekognitionClient rekClient = RekognitionClient.builder()
				.region(region)
				.credentialsProvider(ProfileCredentialsProvider.create())
				.build();
		System.out.println("************CreateCollectionResponse********\n\n\n\n\n\n");


		CreateCollectionResponse collectionResponse= reko.createMyCollection(rekClient, collectionId);

		ListCollections.listAllCollections(rekClient);
		ListFacesInCollection.listFacesCollection(rekClient, collectionId);
		
		System.out.println("********************\n\n\n\n\n\n");
		System.out.println("************indexImagesInFolder********\n\n\n\n\n\n");
		
		indexImagesInFolder(imageFolder, collectionId, rekClient);
		System.out.println("********************\n\n\n\n\n\n");
		
		System.out.println("************SearchFaceMatchingIdCollection********\n\n\n\n\n\n");
		
		SearchFaceMatchingIdCollection.searchFacebyId(rekClient, collectionId, "/Users/jainpuni/pkj.jpg");

		

//		reko.addToCollection(rekClient, collectionId, sourceImage)
		//CelebrityInfo.getCelebrityInfo(rekClient, collectionId);



		//		aws rekognition detect-custom-labels \
		//		  --project-version-arn "arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992" \
		//		  --image '{"S3Object": {"Bucket": "MY_BUCKET","Name": "PATH_TO_MY_IMAGE"}}' \
		//		  --region ap-south-1





	}


	private static void indexImagesInFolder(String folderPath, String collectionId, RekognitionClient rekognitionClient) {
		try {
			Files.walk(Paths.get(folderPath))
			.filter(Files::isRegularFile)
			.filter(file -> isImageFile(file))
			.forEach(imageFile -> indexImage(imageFile, collectionId, rekognitionClient));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean isImageFile(Path file) {
		String filename = file.getFileName().toString();
		return filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png");
	}


    private static void indexImage(Path imageFile, String collectionId, RekognitionClient rekognitionClient) {
        try {
            byte[] imageData = Files.readAllBytes(imageFile);
            SdkBytes imageBytes= SdkBytes.fromByteArray(imageData);
            Image image = Image.builder().bytes(imageBytes).build();
            IndexFacesResponse indexFacesResponse =  reko.addToCollection(rekognitionClient, collectionId,image );

            System.out.println("Image indexed: " + imageFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	
}
