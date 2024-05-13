package com.punit.facepay.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.facepay.service.helper.BedrockUtill;
import com.punit.facepay.service.helper.DynamoDBUtil;
import com.punit.facepay.service.helper.FaceImageCollectionUtil;
import com.punit.facepay.service.helper.QArtQueue;
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
import software.amazon.awssdk.services.rekognition.model.Face;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;

@Service
public class FaceScanService {

	//	String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";

	@Autowired
	private FaceImageCollectionUtil fiUtil;
	@Autowired
	private RekoUtil reko;

	@Autowired
	private s3Util s3Util;
	
	@Autowired
	private BedrockUtill bedrockUtil;

	@Autowired
	private QArtQueue qartQueue;

	@Autowired
	DynamoDBUtil dbUtil;

	//	@Autowired
	//	private AsyncService asyncService;


	final static Logger logger= LoggerFactory.getLogger(FaceScanService.class);


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


	public String searcUserDetailsByFaceID(String faceid) {

		return dbUtil.getFaceInfo(faceid);

	}

	public String searchUserDetails(MultipartFile imageToSearch, int type ) throws IOException, FaceNotFoundException{

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

		logger.info("************ searchFaceInCollection ********");

		//FaceMatch face = fiUtil.searchFaceInCollection(rekClient, Configs.COLLECTION_ID, souImage);
		try {

			List<FaceObject> faceObjList= fiUtil.searchFaceQART(rekClient, Configs.COLLECTION_ID, souImage);

			String  responseSTR = null;

			for (FaceObject faceObject : faceObjList) {
				if(faceObject == null) {
					logger.info("no matching User found");
					s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, responseSTR, "0%");

				}else {

					logger.info("Printing face " +  faceObject.printValue());
					return faceObject.getFaceURL();

				}
			}

			if (responseSTR == null && !detectFace(souImage)) {
				throw new FaceNotFoundException("No human face found");			 			
			}

			return responseSTR;

		}catch (Exception e) {
			s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, null, "0%");
			throw e;
		}

	}

	public String searchImage(MultipartFile imageToSearch ) throws IOException, FaceNotFoundException{

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

		logger.info("************ searchFaceInCollection ********");

		//FaceMatch face = fiUtil.searchFaceInCollection(rekClient, Configs.COLLECTION_ID, souImage);
		try {

			List<FaceObject> faceObjList= fiUtil.searchFace(rekClient, Configs.COLLECTION_ID, souImage);

			String  responseSTR = null;

			for (FaceObject faceObject : faceObjList) {
				if(faceObject == null) {
					logger.info("no matching User found");

					s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, responseSTR, "0%");

				}else {

					logger.info("Printing face " +  faceObject.printValue());

					s3Util.storeinS3(Configs.S3_BUCKET,imageToSearch, imagebytes, faceObject.getFaceid(),""+faceObject.getScore()  );
					return UPILinkUtil.getUrl(faceObject.getFaceURL());

				}
			}

			if (responseSTR == null && !detectFace(souImage)) {
				throw new FaceNotFoundException("No human face found");			 			
			}

			return responseSTR;

		}catch (Exception e) {
			s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, null, "0%");
			throw e;
		}

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

	public String registerImage(MultipartFile myFile, String upiID,String email, String phone) throws IOException {

		RekognitionClient rekClient= getRekClient();
		byte[] imagebytes = null;
		imagebytes= myFile.getBytes();

		String userID=	UPILinkUtil.getUrl(upiID);
		Image souImage = getImage(imagebytes);

		if ( !detectFace(souImage)) {
			return null;			 			
		}



		List<FaceObject> faceObjList= fiUtil.searchFace(rekClient, Configs.COLLECTION_ID, souImage);

		String  responseSTR = null;

		String  faceID =  null;
		String returnmessage =Configs.FACE_ALREADY_EXIST;

		if (faceObjList.isEmpty()) {

			logger.info("no matching User found");

			faceID = fiUtil.addToCollection(rekClient, Configs.COLLECTION_ID, souImage);
			String s3filepath= Configs.S3_FOLDER_REGISTER + upiID;

			String fileFinalPath=s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, imagebytes);
			returnmessage ="uploaded image with id: "+fileFinalPath ;
			dbUtil.putFaceIDInDB(faceID, userID, email, phone, fileFinalPath);

			if (userID.contains("upi://")) {
				logger.info("Generating QART ");
				qartQueue.sendRequest(userID, fileFinalPath);
			}
			logger.info("skipping QART generation");
		}else {

			logger.info("User Already exist");
			//			logger.info("Printing face " +  faceObject.printValue());

		}

		return returnmessage;


	}

	public boolean detectFace(Image souImage) throws IOException {

		RekognitionClient rekClient= getRekClient();

		DetectFacesRequest facesRequest = DetectFacesRequest.builder()
				.attributes(Attribute.ALL)
				.image(souImage)
				.build();

		DetectFacesResponse facesResponse = rekClient.detectFaces(facesRequest);
		List<FaceDetail> faceDetails = facesResponse.faceDetails();

		if (null == faceDetails || faceDetails.isEmpty() ) {
			return false;
		}
		for (FaceDetail face : faceDetails) {
			AgeRange ageRange = face.ageRange();
			logger.info("The detected face is estimated to be between "
					+ ageRange.low().toString() + " and " + ageRange.high().toString()
					+ " years old.");
			logger.info("There is a smile : "+face.smile().value().toString());
		}

		return true;

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
			logger.info("The detected face is estimated to be between "
					+ ageRange.low().toString() + " and " + ageRange.high().toString()
					+ " years old.");
			logger.info("There is a smile : "+face.smile().value().toString());
		}

		// Create an instance of ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			// Convert the list to JSON string
			String json = objectMapper.writeValueAsString(faceDetails.get(0));
			logger.info(json);
			return json;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		logger.info("************ detectFaceInCollection ********");

		
String userprompt = "\"Please provide a 5-10 line summary of the given face detail in the following format:\n"
		+ "\n"
		+ "Age: [Age]<br>\n"
		+ "Gender: [Gender]<br>\n"
		+ "Mood: [Mood]<br>\n"
		+ "Facial Expression: [Expression]<br>\n"
		+ "Image Quality: [Quality]<br>\n"
		+ "Please ensure each detail is provided in a new line.\"";
		
		return	bedrockUtil.InvokeModelLama3(userprompt + faceDetails.toString());
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
			logger.info(json);
			return json;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return faceDetails.toString();
	}
}