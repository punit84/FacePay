package com.punit.facepay.service.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
// snippet-end:[rekognition.java2.search_faces_collection.import]

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.punit.facepay.service.FaceObject;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.Face;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.ListFacesRequest;
import software.amazon.awssdk.services.rekognition.model.ListFacesResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.Reason;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;
import software.amazon.awssdk.services.rekognition.model.UnindexedFace;

@Component
public class FaceImageCollectionUtil {
	
	final static Logger logger= LoggerFactory.getLogger(FaceImageCollectionUtil.class);
	@Autowired
	DynamoDBUtil dbUtil;
	
	public List<FaceObject> searchFace(RekognitionClient rekClient,String collectionId,Image souImage) {
		
		List<FaceObject> faceObjectList = new ArrayList<>();

		SearchFacesByImageRequest facesByImageRequest = SearchFacesByImageRequest.builder()
				.image(souImage)
				.maxFaces(1)
				.faceMatchThreshold(90F)
				.collectionId(collectionId)
				.build();

		SearchFacesByImageResponse imageResponse = rekClient.searchFacesByImage(facesByImageRequest) ;
		logger.info("Faces matching in the collection");
		List<FaceMatch> faceImageMatches = imageResponse.faceMatches();
		logger.info("No of match found are " + faceImageMatches.size());
		for (FaceMatch faceMatch: faceImageMatches) {

			logger.info("face details are  " + faceMatch.toString());

			Face face = faceMatch.face();
			logger.info("The confidence level is  "+face.confidence());
			logger.info("The similarity level is  "+faceMatch.similarity());
			
			if (face.confidence()>98) {
				String faceURL = dbUtil.getFaceID(face.faceId());
				faceObjectList.add(new FaceObject(face.faceId(), faceURL, face.confidence()));
				logger.info("face match found  is " +face.faceId() +  " and Url is " +  faceURL);
		
				
			}else {
				logger.info("Face confidence is lower than 98  " +face.toString() );
				logger.info("fileid  is " +face.faceId() );
			}
			
		}
		return faceObjectList;

	}


	public FaceMatch searchFaceInCollection(RekognitionClient rekClient,String collectionId,Image souImage) {

		SearchFacesByImageRequest facesByImageRequest = SearchFacesByImageRequest.builder()
				.image(souImage)
				.maxFaces(1)
				.faceMatchThreshold(70F)
				.collectionId(collectionId)
				.build();

		SearchFacesByImageResponse imageResponse = rekClient.searchFacesByImage(facesByImageRequest) ;
		logger.info("Faces matching in the collection");
		List<FaceMatch> faceImageMatches = imageResponse.faceMatches();
		String foundFaceName = null;
		FaceMatch matchingface = null;
		
		for (FaceMatch face: faceImageMatches) {
			matchingface = face;

			logger.info("The similarity level is  "+face.similarity());
			if (face.similarity() >98) {
				logger.info("search file details are  " +face.toString() );
				foundFaceName=matchingface.face().faceId();
				logger.info("fileid  is " +foundFaceName );
			}
		}
		return matchingface;

	}
	
	

	public FaceMatch searchFaceInCollection(RekognitionClient rekClient,String collectionId, String sourceImage) throws FileNotFoundException {

		InputStream sourceStream = new FileInputStream(new File(sourceImage));
		SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);
		Image souImage = Image.builder()
				.bytes(sourceBytes)
				.build();

		return searchFaceInCollection(rekClient, collectionId, souImage);

	}

	public String addToCollection(RekognitionClient rekClient, String collectionId, Image sourceImage) {

		try {
			
			IndexFacesRequest facesRequest = IndexFacesRequest.builder()
					.collectionId(collectionId)
					.image(sourceImage)
					.maxFaces(1)
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
				return faceRecord.face().faceId();
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

		} catch (RekognitionException e) {
			logger.error(e.getMessage());
		}
		return null;
	}



	public void listFacesCollection(RekognitionClient rekClient, String collectionId ) {
		try {
			ListFacesRequest facesRequest = ListFacesRequest.builder()
					.collectionId(collectionId)
					.maxResults(10)
					.build();

			ListFacesResponse facesResponse = rekClient.listFaces(facesRequest);
			List<Face> faces = facesResponse.faces();
			for (Face face: faces) {
				logger.info("Confidence level there is a face: "+face.confidence());
				logger.info("The face Id value is "+face.faceId());
			}

		} catch (RekognitionException e) {
            logger.error(e.getMessage());
		}
	}
	
    public void createMyCollection(RekognitionClient rekClient,String collectionId ) {

        try {
            CreateCollectionRequest collectionRequest = CreateCollectionRequest.builder()
                .collectionId(collectionId)
                .build();

            CreateCollectionResponse collectionResponse = rekClient.createCollection(collectionRequest);
            logger.info("CollectionArn: " + collectionResponse.collectionArn());
            logger.info("Status code: " + collectionResponse.statusCode().toString());

        } catch(RekognitionException e) {
            logger.error(e.getMessage());
        }
    }
}
