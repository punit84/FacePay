package com.punit.facepay.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.punit.facepay.service.helper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

/**
 * Service class for handling face detection, recognition, and management operations.
 * Provides functionality for searching, registering, and analyzing faces using AWS Rekognition.
 */
@Service
public class FaceScanService {

    @Autowired
    private FaceImageCollectionUtil fiUtil;
    
    @Autowired
    private RekoUtil reko;

    @Autowired
    private S3Utility s3Util;
    
    @Autowired
    private BedrockUtil bedrockUtil;

    @Autowired
    private QArtQueue qartQueue;

    @Autowired
    private DynamoDBUtil dbUtil;

    private final static Logger logger = LoggerFactory.getLogger(FaceScanService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates and configures an AWS Rekognition client.
     *
     * @return configured RekognitionClient instance
     */
    private RekognitionClient getRekClient() {
        return RekognitionClient.builder()
                .region(Configs.REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Searches for user details using a face ID.
     *
     * @param faceid the unique identifier of the face to search for
     * @return user details as a JSON string
     */
    public String searcUserDetailsByFaceID(String faceid) {
        if (!StringUtils.hasText(faceid)) {
            logger.warn("Face ID is null or empty");
            return null;
        }
        return dbUtil.getFaceInfo(faceid);
    }

    /**
     * Searches for user details using an image file.
     *
     * @param imageToSearch the image file containing the face to search for
     * @return user details as a JSON string
     * @throws IOException if there's an error processing the image
     * @throws FaceNotFoundException if no face is found in the image
     * @throws IllegalArgumentException if the input is invalid
     */
    public String searchUserDetails(MultipartFile imageToSearch) throws IOException, FaceNotFoundException {
        validateImageFile(imageToSearch);

        RekognitionClient rekClient = getRekClient();
        byte[] imagebytes = imageToSearch.getBytes();
        Image souImage = ImageUtil.getImage(imagebytes);

        try {
            List<FaceObject> faceObjList = fiUtil.searchFaceQART(rekClient, Configs.COLLECTION_ID, souImage);
            faceObjList = faceObjList != null ? faceObjList : Collections.emptyList();

            for (FaceObject faceObject : faceObjList) {
                if (faceObject != null) {
                    logger.info("Face match found: {}", faceObject.printValue());
                    return faceObject.getFaceURL();
                }
            }

            // If we get here, no valid face was found
            if (!detectFace(souImage)) {
                throw new FaceNotFoundException("No human face found in the image");
            }

            // Store the unmatched face image
            s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, null, "0%");
            return null;

        } catch (FaceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error searching user details", e);
            s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, null, "0%");
            throw new IOException("Failed to search user details", e);
        }
    }

    /**
     * Searches for a face in the collection using an image file.
     *
     * @param imageToSearch the image file to search with
     * @return search results as a JSON string
     * @throws IOException if there's an error processing the image
     * @throws FaceNotFoundException if no face is found in the image
     * @throws IllegalArgumentException if the input is invalid
     */
    public String searchImage(MultipartFile imageToSearch) throws IOException, FaceNotFoundException {
        validateImageFile(imageToSearch);

        RekognitionClient rekClient = getRekClient();
        byte[] imagebytes = imageToSearch.getBytes();
        Image souImage = ImageUtil.getImage(imagebytes);

        try {
            List<FaceObject> faceObjList = fiUtil.searchFace(rekClient, Configs.COLLECTION_ID, souImage);
            faceObjList = faceObjList != null ? faceObjList : Collections.emptyList();

            for (FaceObject faceObject : faceObjList) {
                if (faceObject != null) {
                    logger.info("Face match found: {}", faceObject.printValue());
                    s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, 
                            faceObject.getFaceid(), String.valueOf(faceObject.getScore()));
                    return UPILinkUtil.getUrl(faceObject.getFaceURL());
                }
            }

            // If we get here, no valid face was found
            if (!detectFace(souImage)) {
                throw new FaceNotFoundException("No human face found in the image");
            }

            // Store the unmatched face image
            s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, null, "0%");
            return null;

        } catch (FaceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error searching image", e);
            s3Util.storeinS3(Configs.S3_BUCKET, imageToSearch, imagebytes, null, "0%");
            throw new IOException("Failed to search image", e);
        }
    }

    /**
     * Registers a new face image with associated user details.
     *
     * @param myFile the image file containing the face to register
     * @param upiID the UPI ID associated with the user
     * @param email the email address of the user
     * @param phone the phone number of the user
     * @return registration result as a JSON string
     * @throws IOException if there's an error processing the image
     * @throws IllegalArgumentException if any required input is invalid
     */
    public String registerImage(MultipartFile myFile, String upiID, String email, String phone) throws IOException {
        validateImageFile(myFile);
        if (!StringUtils.hasText(upiID)) {
            throw new IllegalArgumentException("UPI ID is required");
        }

        RekognitionClient rekClient = getRekClient();
        byte[] imagebytes = myFile.getBytes();
        String userID = UPILinkUtil.getUrl(upiID);
        Image souImage = ImageUtil.getImage(imagebytes);

        if (!detectFace(souImage)) {
            logger.warn("No face detected in the registration image");
            return null;
        }

        List<FaceObject> faceObjList = fiUtil.searchFace(rekClient, Configs.COLLECTION_ID, souImage);
        faceObjList = faceObjList != null ? faceObjList : Collections.emptyList();

        String returnMessage = Configs.FACE_ALREADY_EXIST;
        String faceID = null;

        if (faceObjList.isEmpty()) {
            // No matching face found, register new face
            faceID = fiUtil.addToCollection(rekClient, Configs.COLLECTION_ID, souImage);
            if (faceID != null) {
                String s3filepath = Configs.S3_FOLDER_REGISTER + upiID;
                String fileFinalPath = s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, imagebytes);
                returnMessage = faceID;
                dbUtil.putFaceIDInDB(faceID, userID, email, phone, fileFinalPath);

                if (userID.contains("upi://")) {
                    logger.info("Generating QART for UPI ID");
                    qartQueue.sendRequest(userID, fileFinalPath);
                } else {
                    logger.info("Skipping QART generation - not a UPI ID");
                }
            }
        } else {
            // Update existing face registration
            for (FaceObject faceObject : faceObjList) {
                if (faceObject != null) {
                    faceID = faceObject.getFaceid();
                    logger.info("Updating existing face: {}", faceObject.printValue());
                    
                    String s3filepath = Configs.S3_FOLDER_REGISTER + upiID;
                    String fileFinalPath = s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, imagebytes);
                    returnMessage = faceID;
                    dbUtil.putFaceIDInDB(faceID, userID, email, phone, fileFinalPath);
                    logger.info("Skipping QART generation for existing face");
                    break;
                }
            }
        }

        return returnMessage;
    }

    /**
     * Detects if there is a face in the provided image.
     *
     * @param souImage the image to analyze for face detection
     * @return true if a face is detected, false otherwise
     * @throws IOException if there's an error processing the image
     */
    public boolean detectFace(Image souImage) throws IOException {
        if (souImage == null) {
            return false;
        }

        RekognitionClient rekClient = getRekClient();
        DetectFacesRequest facesRequest = DetectFacesRequest.builder()
                .attributes(Attribute.ALL)
                .image(souImage)
                .build();

        DetectFacesResponse facesResponse = rekClient.detectFaces(facesRequest);
        List<FaceDetail> faceDetails = facesResponse.faceDetails();

        if (faceDetails == null || faceDetails.isEmpty()) {
            return false;
        }

        for (FaceDetail face : faceDetails) {
            AgeRange ageRange = face.ageRange();
            if (ageRange != null) {
                logger.info("Detected face estimated age: {} to {} years old",
                        ageRange.low(), ageRange.high());
            }
            if (face.smile() != null) {
                logger.info("Smile detected: {}", face.smile().value());
            }
        }

        return true;
    }

    /**
     * Generates a profile of facial attributes from the provided image.
     *
     * @param imageToSearch the image file to analyze
     * @return profile details as a JSON string
     * @throws IOException if there's an error processing the image
     * @throws IllegalArgumentException if the input is invalid
     */
    public String profile(MultipartFile imageToSearch) throws IOException {
        validateImageFile(imageToSearch);

        RekognitionClient rekClient = getRekClient();
        byte[] imagebytes = imageToSearch.getBytes();
        Image souImage = ImageUtil.getImage(imagebytes);

        DetectFacesRequest facesRequest = DetectFacesRequest.builder()
                .attributes(Attribute.ALL)
                .image(souImage)
                .build();

        DetectFacesResponse facesResponse = rekClient.detectFaces(facesRequest);
        List<FaceDetail> faceDetails = facesResponse.faceDetails();

        if (faceDetails != null && !faceDetails.isEmpty()) {
            FaceDetail face = faceDetails.get(0);
            AgeRange ageRange = face.ageRange();
            if (ageRange != null) {
                logger.info("Detected face estimated age: {} to {} years old",
                        ageRange.low(), ageRange.high());
            }
            if (face.smile() != null) {
                logger.info("Smile detected: {}", face.smile().value());
            }
            logger.info("Generating AI profile description");
            return bedrockUtil.InvokeModelLama3(Configs.AI_PROMPT + face.toString());
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
        if (faceDetails == null || faceDetails.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(faceDetails);
        } catch (JsonProcessingException e) {
            logger.error("Error converting face details to JSON", e);
            return "[]";
        }
    }

    /**
     * Validates that an image file is present and not empty.
     *
     * @param imageFile the image file to validate
     * @throws IllegalArgumentException if the image file is invalid
     */
    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file is required and must not be empty");
        }
    }
}