package com.punit.AWSPe.service;

import java.io.IOException;
import java.util.List;

import com.punit.AWSPe.service.helper.*;
import com.punit.AWSPe.service.helper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.AgeRange;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.Beard;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.DetectFacesRequest;
import software.amazon.awssdk.services.rekognition.model.DetectFacesResponse;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;
import software.amazon.awssdk.services.rekognition.model.Image;

/**
 * Service class for handling face detection, recognition, and management operations.
 * Provides functionality for searching, registering, and analyzing faces using AWS Rekognition.
 */
@Service
public class FaceScanService {

	//	String modelversion = "arn:aws:rekognition:ap-south-1:057641535369:project/logos_2/version/logos_2.2023-06-19T23.41.34/1687198294871";

	@Autowired
	private FaceImageCollectionUtil fiUtil;
	@Autowired
	private RekoUtil reko;

	@Autowired
	private com.punit.AWSPe.service.helper.s3Util s3Util;
	
	@Autowired
	private BedrockUtil bedrockUtil;

	@Autowired
	private QArtQueue qartQueue;

	@Autowired
    DynamoDBUtil dbUtil;


	//	@Autowired
	//	private AsyncService asyncService;


	final static Logger logger= LoggerFactory.getLogger(FaceScanService.class);


	/**
     * Creates and configures an AWS Rekognition client.
     *
     * @return configured RekognitionClient instance
     */
	private RekognitionClient getRekClient() {

		RekognitionClient client = RekognitionClient.builder()
				.region(Configs.REGION)
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
		return client;
	}

	/**
     * Searches for user details using a face ID.
     *
     * @param faceid the unique identifier of the face to search for
     * @return user details as a JSON string
     */
	public String searcUserDetailsByFaceID(String faceid) {

		return dbUtil.getFaceInfo(faceid);

	}

	/**
     * Searches for user details using an image file.
     *
     * @param imageToSearch the image file containing the face to search for
     * @return user details as a JSON string
     * @throws IOException if there's an error processing the image
     * @throws FaceNotFoundException if no face is found in the image
     */
	public String searchUserDetails(MultipartFile imageToSearch ) throws IOException, FaceNotFoundException{

		RekognitionClient rekClient= getRekClient();

		byte[] imagebytes = null;
		Image souImage = null;
		try {
			imagebytes = imageToSearch.getBytes();
			souImage = ImageUtil.getImage(imagebytes);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		logger.info("************ searchFaceInCollection ********");

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

	/**
     * Searches for a face in the collection using an image file.
     *
     * @param imageToSearch the image file to search with
     * @return search results as a JSON string
     * @throws IOException if there's an error processing the image
     * @throws FaceNotFoundException if no face is found in the image
     */
	public String searchImage(MultipartFile imageToSearch ) throws IOException, FaceNotFoundException{

		RekognitionClient rekClient= getRekClient();

		byte[] imagebytes = null;
		Image souImage = null;
		try {
			imagebytes = imageToSearch.getBytes();
			souImage = ImageUtil.getImage(imagebytes);

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

	/**
     * Registers a new face image with associated user details.
     *
     * @param myFile the image file containing the face to register
     * @param upiID the UPI ID associated with the user
     * @param email the email address of the user
     * @param phone the phone number of the user
     * @return registration result as a JSON string
     * @throws IOException if there's an error processing the image
     */
	public String registerImage(MultipartFile myFile, String upiID,String email, String phone) throws IOException {

		RekognitionClient rekClient= getRekClient();
		byte[] imagebytes = null;
		imagebytes= myFile.getBytes();

		String userID=	UPILinkUtil.getUrl(upiID);
		Image souImage = ImageUtil.getImage(imagebytes);

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
			returnmessage =  faceID;
			dbUtil.putFaceIDInDB(faceID, userID, email, phone, fileFinalPath);

			if (userID.contains("upi://")) {
				logger.info("Generating QART ");
				qartQueue.sendRequest(userID, fileFinalPath);
			}else {
				logger.info("skipping QART generation");
			}
		}else {

			//find face id

			for (FaceObject faceObject : faceObjList) {
				if(faceObject == null) {
					logger.info("no matching User found");
				}else {
					faceID = faceObject.getFaceid();
					logger.info("Printing face " +  faceObject.printValue());
					
					String s3filepath= Configs.S3_FOLDER_REGISTER + upiID;
					String fileFinalPath=s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, imagebytes);
					returnmessage =faceID ;
					dbUtil.putFaceIDInDB(faceID, userID, email, phone, fileFinalPath);

//					if (userID.contains("upi://")) {
//						logger.info("Generating QART ");
//						qartQueue.sendRequest(userID, fileFinalPath);
//					}
					logger.info("skipping QART generation");
					
					return returnmessage;
				}
			}
					
		}

		return returnmessage;


	}

	/**
     * Detects if there is a face in the provided image.
     *
     * @param souImage the image to analyze for face detection
     * @return true if a face is detected, false otherwise
     * @throws IOException if there's an error processing the image
     */
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

	/**
     * Generates a profile of facial attributes from the provided image.
     *
     * @param imageToSearch the image file to analyze
     * @return profile details as a JSON string
     * @throws IOException if there's an error processing the image
     */
	public String profile(MultipartFile imageToSearch) throws IOException {

		RekognitionClient rekClient= getRekClient();

		byte[] imagebytes= imageToSearch.getBytes();
		Image souImage = ImageUtil.getImage(imagebytes);

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
			logger.info("************ detectFaceInCollection ********");
			return	bedrockUtil.InvokeModelLama3(Configs.AI_PROMPT + face.toString());

		}
		return "";

	}

	/**
     * Converts face details to a JSON string representation.
     *
     * @param faceDetails list of face details from Rekognition
     * @return JSON string containing face details
     */
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