package com.punit.facepay.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.facepay.service.helper.DynamoDBUtil;
import com.punit.facepay.service.helper.FaceImageCollectionUtil;
import com.punit.facepay.service.helper.RekoUtil;
import com.punit.facepay.service.helper.UPILinkUtil;
import com.punit.facepay.service.helper.s3Util;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.AgeRange;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.Beard;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.CustomLabel;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectCustomLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

@Service
public class FacePayService {

	//	String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";

	@Autowired
	private FaceImageCollectionUtil fiUtil;
	@Autowired
	private RekoUtil reko;

	@Autowired
	private s3Util s3Util;

	@Autowired
	DynamoDBUtil dbUtil;

	@Autowired
	private AsyncService asyncService;


	//private static HashMap< String, String> faceStore = new HashMap<>();

	public String detectLabels(MultipartFile imageToCheck) throws IOException {

		Image souImage = getImage(imageToCheck.getBytes());
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

	private Image getImage(byte[] imageToCheck) throws IOException {

		Image souImage = Image.builder()
				.bytes(SdkBytes.fromByteArray(imageToCheck))
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

	public String searchImage(MultipartFile imageToSearch ) {

		RekognitionClient rekClient= getRekClient();

		byte[] imagebytes = null;
		Image souImage = null;
		try {
			imagebytes = imageToSearch.getBytes();
			souImage = getImage(imagebytes);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("************ searchFaceInCollection ********");



		FaceMatch face = fiUtil.searchFaceInCollection(rekClient, Configs.COLLECTION_ID, souImage);

		String  responseSTR = null;
		if(face ==null) {
			System.out.println("no matching label found");

			s3Util.storeinS3(imageToSearch, imagebytes, responseSTR);

		}else {
			responseSTR = face.face().faceId();
			String faceid = dbUtil.getFaceID(responseSTR);
			System.out.println("face id in DB is "+faceid);
			s3Util.storeinS3(imageToSearch, imagebytes, responseSTR+"_"+face.similarity());

			responseSTR = UPILinkUtil.getUrl(faceid);


		}

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

		Image souImage = getImage(myFile.getBytes());



		String  faceID = fiUtil.addToCollection(rekClient, Configs.COLLECTION_ID, souImage);

		dbUtil.putFaceID(faceID,imageID);

		return "uploaded image with id: "+imageID;


	}

	public String profile(MultipartFile imageToSearch) throws IOException {

		RekognitionClient rekClient= getRekClient();

		byte[] imagebytes= imageToSearch.getBytes();
		Image souImage = getImage(imagebytes);

		DetectFacesRequest facesRequest = DetectFacesRequest.builder()
				.attributes(Attribute.ALL)
				.image(souImage)
				.build();

		DetectFacesResponse facesResponse = rekClient.detectFaces(facesRequest);
		List<FaceDetail> faceDetails = facesResponse.faceDetails();
		for (FaceDetail face : faceDetails) {
			AgeRange ageRange = face.ageRange();
			System.out.println("The detected face is estimated to be between "
					+ ageRange.low().toString() + " and " + ageRange.high().toString()
					+ " years old.");
			System.out.println("There is a smile : "+face.smile().value().toString());
		}

		// Create an instance of ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			// Convert the list to JSON string
			String json = objectMapper.writeValueAsString(faceDetails);
			System.out.println(json);
			return json;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		System.out.println("************ detectFaceInCollection ********");


		return faceDetails.toString();
	}

	private String facejson(List<FaceDetail> faceDetails) {
		// Create a sample DetectFacesResponse with all available parameters
		DetectFacesResponse response = DetectFacesResponse.builder()
				.faceDetails(
						FaceDetail.builder()
						.ageRange(AgeRange.builder().low(20).high(30).build())
						.beard(Beard.builder().value(true).confidence(0.95f).build())
						.boundingBox(BoundingBox.builder().width(0.3f).height(0.4f).left(0.2f).top(0.1f).build())
						// Add more parameters as needed
						.build(),
						FaceDetail.builder()
						.ageRange(AgeRange.builder().low(25).high(35).build())
						.beard(Beard.builder().value(false).confidence(0.85f).build())
						.boundingBox(BoundingBox.builder().width(0.2f).height(0.5f).left(0.1f).top(0.3f).build())
						// Add more parameters as needed
						.build()
						)
				.build();

		faceDetails.addAll(response.faceDetails());

		// Create an instance of ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			// Convert the List<FaceDetail> to JSON string
			String json = objectMapper.writeValueAsString(faceDetails);
			System.out.println(json);
			return json;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return faceDetails.toString();
	}
}