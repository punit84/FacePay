package com.punit.facepay.service;

import com.punit.facepay.service.helper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KYCRestService {

    @Autowired
    private com.punit.facepay.service.helper.s3Util s3Util;

    @Autowired
    private BedrockUtill bedrockUtil;

    final static Logger logger= LoggerFactory.getLogger(KYCRestService.class);

    @Autowired
    TextractUtil ocrUtil;

    @Autowired
    private RekoUtil reko;

    private RekognitionClient getRekClient() {

        RekognitionClient client = RekognitionClient.builder()
                .region(Configs.REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
        return client;
    }

    public static Map<String, Object> getDocumentTypes() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("UpdateBankDetails", new String[]{"cheque", "Passbook", "Bank Statement"});
        documentData.put("UpdateName", new String[]{"Passport", "Driving License", "Voter's ID card", "Pan Card", "Aadhaar Card", "NRGEA Job Card"});
        documentData.put("UpdateAddress", new String[]{"Electricity Bill", "Gas Bill", "Bank Account Statement", "Landline Bill", "Life Insurance Policy", "Registered Lease/Rent Agreement"});

        return documentData;
    }

    public String ocrScan(MultipartFile imageToSearch, String requestType, String docType, String text) throws IOException, InterruptedException {
        logger.info("************ call claude ********");
        byte[] bytes = imageToSearch.getBytes();
        String s3filepath= Configs.S3_FOLDER_OCR ;
        String fileFinalPath=s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, bytes);

        String jobId = ocrUtil.startTextDetection(fileFinalPath);
        return	ocrUtil.getJobResult(jobId);
        //return	bedrockUtil.invokeHaiku(bytes, prompt, imageToSearch.getOriginalFilename() , Configs.MODEL_SONET);

    }

    public String kycScan(MultipartFile imageToSearch, String requestType, String docType, String text) throws IOException {
        logger.info("************ call claude ********");
        byte[] bytes = imageToSearch.getBytes();
        String prompt = PromptGenerator.generateLLMPrompt(requestType,docType);
        String s3filepath= Configs.S3_FOLDER_KYC ;
        String fileFinalPath=s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, bytes);
        return	bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename() , Configs.MODEL_HAIKU);
        //       return	bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename() , Configs.MODEL_SONET);

    }

    public String kycReko(MultipartFile imageToSearch) throws IOException {

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


}