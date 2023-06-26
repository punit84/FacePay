package com.punit.facepay.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.helper.DynamoDBUtil;
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

	private FaceImageCollectionUtil fiUtil= new FaceImageCollectionUtil();
	private RekoUtil reko= new RekoUtil();
	DynamoDBUtil dbUtil = new DynamoDBUtil();


	//private static HashMap< String, String> faceStore = new HashMap<>();

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

		RekognitionClient client = RekognitionClient.builder()
				.region(Configs.REGION)
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
				.projectVersionArn(Configs.MODEL_VERSION)
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

	public String searchImage(MultipartFile imageToSearch ) throws IOException {

		RekognitionClient rekClient= getRekClient();
		Image souImage = getImage(imageToSearch);


		System.out.println("************ searchFaceInCollection ********");

		String  responseSTR = fiUtil.searchFaceInCollection(rekClient, Configs.COLLECTION_ID, souImage);


		if(responseSTR ==null) {

			responseSTR= "No Match found";
			System.out.println("no matching label found");

		}else {
			
			String faceid = dbUtil.getFaceID(responseSTR);
			System.out.println("face id in DB is "+faceid);
			
			if (faceid.contains("@")) {
				responseSTR = "upi://pay?pa="+faceid+"&pn=PaytmUser&cu=INR";
			}else {
				responseSTR = "upi://pay?pa="+faceid+"@paytm&pn=PaytmUser&cu=INR";
			}
			
			System.out.println("url is : " + responseSTR);

		}

		//		reko.addToCollection(rekClient, collectionId, sourceImage)
		//CelebrityInfo.getCelebrityInfo(rekClient, collectionId);



		//		aws rekognition detect-custom-labels \
		//		  --project-version-arn "arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992" \
		//		  --image '{"S3Object": {"Bucket": "MY_BUCKET","Name": "PATH_TO_MY_IMAGE"}}' \
		//		  --region ap-south-1



		return responseSTR;
	}



//	private String addImage(Image souImage) {
//
//		RekognitionClient rekClient= getRekClient();		
//
//		//CreateCollectionResponse collectionResponse= reko.createMyCollection(rekClient, collectionId);
//
//		fiUtil.listAllCollections(rekClient);
//		fiUtil.listFacesCollection(rekClient, Configs.COLLECTION_ID);
//		//indexImagesInFolder(imageFolder, collectionId, rekClient);
//
//		return null;
//	}

	public String addImage(MultipartFile myFile, String imageID) throws IOException {

		RekognitionClient rekClient= getRekClient();	
		
		Image souImage = getImage(myFile);

		String  faceID = fiUtil.addToCollection(rekClient, Configs.COLLECTION_ID, souImage);
		
		dbUtil.putFaceID(faceID,imageID);

		return "uploaded image with id: "+imageID;


	}
}