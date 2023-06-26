package com.punit.facepay.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.helper.FaceImageCollectionUtil;
import com.punit.facepay.service.helper.RekoUtil;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

@Service
public class FacePayService {

	//	String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";

	String modelversion ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";
	private FaceImageCollectionUtil fiUtil= new FaceImageCollectionUtil();
	private RekoUtil reko= new RekoUtil();
	private String collectionId = "Punit-faceCollection";

	private static HashMap< String, String> faceStore = new HashMap<>();

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
				.credentialsProvider(DefaultCredentialsProvider.create())
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
		String customLable = null;


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

				if (customLabel1.confidence() <80) {

					System.out.println("confidence score is low " +customLabel1.confidence());
					return "low confidence <kindly take another image> : " + customLabel1.confidence();
				}

				customLable = customLabel1.name();
				System.out.println("Detected labels for the given photo: " +customLable);

				if (!customLable.contains("paytm")) {
					customLable = customLabel1 + "@paytm";
					System.out.println("Detected labels for the given photo: " +customLable);

				}
				customLable = "upi://pay?pa="+customLable+"&pn=PaytmUser&mc=0000&mode=02&purpose=00&orgid=159761";

			}
		} catch (RekognitionException e) {
			System.out.println(e.getMessage());
			System.exit(1);


		}
		return customLable;
	}

	public String searchImage(MultipartFile imageToSearch) throws IOException {

		RekognitionClient rekClient= getRekClient();
		Image souImage = getImage(imageToSearch);


		System.out.println("************CreateCollectionResponse********\n\n\n\n\n\n");


		//CreateCollectionResponse collectionResponse= reko.createMyCollection(rekClient, collectionId);

		fiUtil.listAllCollections(rekClient);
		fiUtil.listFacesCollection(rekClient, collectionId);

		System.out.println("********************\n\n\n\n\n\n");
		System.out.println("************indexImagesInFolder********\n\n\n\n\n\n");

		//indexImagesInFolder(imageFolder, collectionId, rekClient);
		System.out.println("********************\n\n\n\n\n\n");

		System.out.println("************searchFaceInCollection********\n\n\n\n\n\n");

		String  responseSTR = fiUtil.searchFaceInCollection(rekClient, collectionId, souImage);


		if(responseSTR ==null) {

			responseSTR= "No Match found";
			System.out.println("no matching label found");

		}else {
			String faceid = faceStore.get(responseSTR);
			System.out.println("face id in map is "+faceid);
			System.out.println("face id in map is "+faceStore.toString());
			
			responseSTR = "upi://pay?pa="+faceid+"&pn=PaytmUser&mc=0000&mode=02&purpose=00&orgid=159761";

		}

		//		reko.addToCollection(rekClient, collectionId, sourceImage)
		//CelebrityInfo.getCelebrityInfo(rekClient, collectionId);



		//		aws rekognition detect-custom-labels \
		//		  --project-version-arn "arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992" \
		//		  --image '{"S3Object": {"Bucket": "MY_BUCKET","Name": "PATH_TO_MY_IMAGE"}}' \
		//		  --region ap-south-1



		return responseSTR;
	}



	private String addImage(Image souImage) {

		RekognitionClient rekClient= getRekClient();		

		//CreateCollectionResponse collectionResponse= reko.createMyCollection(rekClient, collectionId);

		fiUtil.listAllCollections(rekClient);
		fiUtil.listFacesCollection(rekClient, collectionId);
		//indexImagesInFolder(imageFolder, collectionId, rekClient);

		return null;
	}

	public String addImage(MultipartFile myFile, String imageID) throws IOException {

		RekognitionClient rekClient= getRekClient();	
		Image souImage = getImage(myFile);

		String  faceID = fiUtil.addToCollection(rekClient, collectionId, souImage);
		if (faceStore.containsKey(faceID)) {
			faceStore.put(imageID, faceID);
			return "face already exist with id"+imageID+", replacing same";
		}
		faceStore.put(faceID, imageID);

		return "uploaded image with id: "+imageID;


	}
}