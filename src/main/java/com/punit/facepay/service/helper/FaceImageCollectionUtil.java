// snippet-sourcedescription:[SearchFaceMatchingImageCollection.java demonstrates how to search for matching faces in an Amazon Rekognition collection.]
//snippet-keyword:[AWS SDK for Java v2]
// snippet-service:[Amazon Rekognition]

/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
 */

package com.punit.facepay.service.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
// snippet-end:[rekognition.java2.search_faces_collection.import]

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Attribute;
import software.amazon.awssdk.services.rekognition.model.DescribeCollectionRequest;
import software.amazon.awssdk.services.rekognition.model.DescribeCollectionResponse;
import software.amazon.awssdk.services.rekognition.model.Face;
import software.amazon.awssdk.services.rekognition.model.FaceMatch;
import software.amazon.awssdk.services.rekognition.model.FaceRecord;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.IndexFacesRequest;
import software.amazon.awssdk.services.rekognition.model.IndexFacesResponse;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsRequest;
import software.amazon.awssdk.services.rekognition.model.ListCollectionsResponse;
import software.amazon.awssdk.services.rekognition.model.ListFacesRequest;
import software.amazon.awssdk.services.rekognition.model.ListFacesResponse;
import software.amazon.awssdk.services.rekognition.model.QualityFilter;
import software.amazon.awssdk.services.rekognition.model.Reason;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageRequest;
import software.amazon.awssdk.services.rekognition.model.SearchFacesByImageResponse;
import software.amazon.awssdk.services.rekognition.model.UnindexedFace;

/**
 * Before running this Java V2 code example, set up your development environment, including your credentials.
 *
 * For more information, see the following documentation topic:
 *
 * https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html
 */
public class FaceImageCollectionUtil {

	public String searchFaceInCollection(RekognitionClient rekClient,String collectionId, String sourceImage) {

		try {
			InputStream sourceStream = new FileInputStream(new File(sourceImage));
			SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);
			Image souImage = Image.builder()
					.bytes(sourceBytes)
					.build();

			SearchFacesByImageRequest facesByImageRequest = SearchFacesByImageRequest.builder()
					.image(souImage)
					.maxFaces(10)
					.faceMatchThreshold(40F)
					.collectionId(collectionId)
					.build();

			SearchFacesByImageResponse imageResponse = rekClient.searchFacesByImage(facesByImageRequest) ;
			System.out.println("Faces matching in the collection");
			List<FaceMatch> faceImageMatches = imageResponse.faceMatches();
			String foundFaceName = null;
			for (FaceMatch face: faceImageMatches) {
				System.out.println("The similarity level is  "+face.similarity());
				System.out.println();
				if (face.similarity() >70) {
					foundFaceName= face.toString();
				}
			}

			return foundFaceName;

		} catch (RekognitionException | FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	public boolean addToCollection(RekognitionClient rekClient, String collectionId, Image sourceImage) {

		try {
			//			InputStream sourceStream = new FileInputStream(sourceImage);
			//			SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);
			//			Image souImage = Image.builder()
			//					.bytes(sourceBytes)
			//					.build();

			IndexFacesRequest facesRequest = IndexFacesRequest.builder()
					.collectionId(collectionId)
					.image(sourceImage)
					.maxFaces(1)
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
				return true;
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

		} catch (RekognitionException e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

	public void listAllCollections(RekognitionClient rekClient) {
		try {
			ListCollectionsRequest listCollectionsRequest = ListCollectionsRequest.builder()
					.maxResults(10)
					.build();

			ListCollectionsResponse response = rekClient.listCollections(listCollectionsRequest);
			List<String> collectionIds = response.collectionIds();
			for (String resultId : collectionIds) {
				System.out.println(resultId);
			}

		} catch (RekognitionException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	public void describeColl(RekognitionClient rekClient, String collectionName) {

		try {
			DescribeCollectionRequest describeCollectionRequest = DescribeCollectionRequest.builder()
					.collectionId(collectionName)
					.build();

			DescribeCollectionResponse describeCollectionResponse = rekClient.describeCollection(describeCollectionRequest);
			System.out.println("Collection Arn : " + describeCollectionResponse.collectionARN());
			System.out.println("Created : " + describeCollectionResponse.creationTimestamp().toString());

		} catch(RekognitionException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
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
				System.out.println("Confidence level there is a face: "+face.confidence());
				System.out.println("The face Id value is "+face.faceId());
			}

		} catch (RekognitionException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
}
