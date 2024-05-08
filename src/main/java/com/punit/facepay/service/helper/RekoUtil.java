package com.punit.facepay.service.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.punit.facepay.service.Configs;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.DeleteCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.DescribeCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.DescribeCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsRequest;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.Reason;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.UnindexedFace;

@Component
public class RekoUtil {
	final static Logger logger= LoggerFactory.getLogger(RekoUtil.class);


	
	public static void main(String[] args) {
		RekoUtil reko= new RekoUtil();
		RekognitionClient client = RekognitionClient.builder()
				.region(Configs.REGION)
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
		
		
		reko.createMyCollection(client, Configs.COLLECTION_ID);
		
		System.out.println("New collection created succesfully");
		
		
	}
	public CreateCollectionResponse createMyCollection(RekognitionClient rekClient,String collectionId ) {

		CreateCollectionResponse collectionResponse = null;
		try {
			CreateCollectionRequest collectionRequest = CreateCollectionRequest.builder()
					.collectionId(collectionId)
					.build();

			collectionResponse = rekClient.createCollection(collectionRequest);
			logger.info("CollectionArn: " + collectionResponse.collectionArn());
			logger.info("Status code: " + collectionResponse.statusCode().toString());

			DynamoDBUtil dbUtil = new DynamoDBUtil();
			dbUtil.putFaceIDInDB("FaceCollectionArn", collectionResponse.collectionArn(), "","");

		} catch(RekognitionException e) {
			logger.info(e.getMessage());
		}
		return collectionResponse;
	}

	public void deleteMyCollection(RekognitionClient rekClient,String collectionId ) {

		try {
			DeleteCollectionRequest deleteCollectionRequest = DeleteCollectionRequest.builder()
					.collectionId(collectionId)
					.build();

			DeleteCollectionResponse deleteCollectionResponse = rekClient.deleteCollection(deleteCollectionRequest);
			logger.info(collectionId + ": " + deleteCollectionResponse.statusCode().toString());

		} catch(RekognitionException e) {
			logger.info(e.getMessage());
			System.exit(1);
		}
	}

	public void deleteAllMyCollection(RekognitionClient rekClient ) {

		try {

			List<String> collectionIds = getAllCollections(rekClient);
			for (String collectionId : collectionIds) {
				logger.info(collectionId);
				DeleteCollectionRequest deleteCollectionRequest = DeleteCollectionRequest.builder()
						.collectionId(collectionId)
						.build();

				DeleteCollectionResponse deleteCollectionResponse = rekClient.deleteCollection(deleteCollectionRequest);
				logger.info(collectionId + ": " + deleteCollectionResponse.statusCode().toString());

			}

		} catch(RekognitionException e) {
			logger.info(e.getMessage());
		}
	}


	public IndexFacesResponse addToCollection(RekognitionClient rekClient, String collectionId, String sourceImage) {
		InputStream sourceStream;
		try {
			sourceStream = new FileInputStream(sourceImage);
			SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);
			Image souImage = Image.builder()
					.bytes(sourceBytes)
					.build();

			return addToCollection(rekClient, collectionId, souImage);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}


	public IndexFacesResponse addToCollection(RekognitionClient rekClient, String collectionId, Image souImage) {

		IndexFacesRequest facesRequest = IndexFacesRequest.builder()
				.collectionId(collectionId)
				.image(souImage)
				.maxFaces(Configs.MAX_MATCHES)
				.qualityFilter(QualityFilter.AUTO)
				.detectionAttributes(Attribute.DEFAULT)
				.build();

		IndexFacesResponse facesResponse = rekClient.indexFaces(facesRequest);
		logger.info("Results for the image");
		logger.info("\n Faces indexed:");
		List<FaceRecord> faceRecords = facesResponse.faceRecords();
		for (FaceRecord faceRecord : faceRecords) {
			logger.info("  Face ID: " + faceRecord.face().faceId());
			logger.info("  Location:" + faceRecord.faceDetail().boundingBox().toString());
		}

		List<UnindexedFace> unindexedFaces = facesResponse.unindexedFaces();
		logger.info("Faces not indexed:");
		for (UnindexedFace unindexedFace : unindexedFaces) {
			logger.info("  Location:" + unindexedFace.faceDetail().boundingBox().toString());
			logger.info("  Reasons:");
			for (Reason reason : unindexedFace.reasons()) {
				logger.info("Reason:  " + reason);
			}
		}
		return facesResponse;

	}
	public void listAllCollections(RekognitionClient rekClient) {
		try {
			List<String> collectionIds = getAllCollections(rekClient);
			for (String resultId : collectionIds) {
				logger.info(resultId);
			}

		} catch (RekognitionException e) {
			logger.info(e.getMessage());
		}
	}

	public List<String> getAllCollections(RekognitionClient rekClient) {
		ListCollectionsRequest listCollectionsRequest = ListCollectionsRequest.builder()
				.maxResults(10)
				.build();

		ListCollectionsResponse response = rekClient.listCollections(listCollectionsRequest);
		List<String> collectionIds = response.collectionIds();
		return collectionIds;
	}

	public void describeColl(RekognitionClient rekClient, String collectionName) {

		try {
			DescribeCollectionRequest describeCollectionRequest = DescribeCollectionRequest.builder()
					.collectionId(collectionName)
					.build();

			DescribeCollectionResponse describeCollectionResponse = rekClient.describeCollection(describeCollectionRequest);
			logger.info("Collection Arn : " + describeCollectionResponse.collectionARN());
			logger.info("Created : " + describeCollectionResponse.creationTimestamp().toString());

		} catch(RekognitionException e) {
			logger.info(e.getMessage());
		}
	}

}
