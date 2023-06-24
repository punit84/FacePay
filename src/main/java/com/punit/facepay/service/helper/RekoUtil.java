package com.punit.facepay.service.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.CreateCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.Reason;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.UnindexedFace;

public class RekoUtil {
	private static int MAX_MATCHES = 1;

	public CreateCollectionResponse createMyCollection(RekognitionClient rekClient,String collectionId ) {

		CreateCollectionResponse collectionResponse = null;
		try {
			CreateCollectionRequest collectionRequest = CreateCollectionRequest.builder()
					.collectionId(collectionId)
					.build();

			collectionResponse = rekClient.createCollection(collectionRequest);
			System.out.println("CollectionArn: " + collectionResponse.collectionArn());
			System.out.println("Status code: " + collectionResponse.statusCode().toString());

		} catch(RekognitionException e) {
			System.out.println(e.getMessage());
		}
		return collectionResponse;
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
					.maxFaces(MAX_MATCHES)
					.qualityFilter(QualityFilter.AUTO)
					.detectionAttributes(Attribute.DEFAULT)
					.build();

			IndexFacesResponse facesResponse = rekClient.indexFaces(facesRequest);
			System.out.println("Results for the image");
			System.out.println("\n Faces indexed:");
			List<FaceRecord> faceRecords = facesResponse.faceRecords();
			for (FaceRecord faceRecord : faceRecords) {
				System.out.println("  Face ID: " + faceRecord.face().faceId());
				System.out.println("  Location:" + faceRecord.faceDetail().boundingBox().toString());
			}

			List<UnindexedFace> unindexedFaces = facesResponse.unindexedFaces();
			System.out.println("Faces not indexed:");
			for (UnindexedFace unindexedFace : unindexedFaces) {
				System.out.println("  Location:" + unindexedFace.faceDetail().boundingBox().toString());
				System.out.println("  Reasons:");
				for (Reason reason : unindexedFace.reasons()) {
					System.out.println("Reason:  " + reason);
				}
			}
			return facesResponse;

	}

}
