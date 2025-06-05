package com.punit.facepay.service;

import com.punit.facepay.service.helper.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class for handling Know Your Customer (KYC) related operations.
 * Provides functionality for document scanning, OCR, and document verification using AWS services.
 */
@Service
public class KYCRestService {

    @Autowired
    private S3Utility s3Util;

    @Autowired
    private BedrockUtil bedrockUtil;

    final static Logger logger = LoggerFactory.getLogger(KYCRestService.class);

    @Autowired
    TextractUtil ocrUtil;

    @Autowired
    private RekoUtil rekoUtil;

    @Autowired
    private JsonUtil jsonUtil;

    /**
     * Retrieves a mapping of supported document types and their configurations.
     *
     * @return Map containing document type configurations
     */
    public static Map<String, Object> getDocumentTypes() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("UpdateBankDetails", new String[]{"cheque", "Passbook", "Bank Statement"});
        documentData.put("UpdateName", new String[]{"Passport", "Driving License", "Voter's ID card", "Pan Card", "Aadhaar Card", "NRGEA Job Card"});
        documentData.put("UpdateAddress", new String[]{"Electricity Bill", "Gas Bill", "Bank Account Statement", "Landline Bill", "Life Insurance Policy", "Registered Lease/Rent Agreement"});

        return documentData;
    }

    /**
     * Performs OCR scanning on a document image.
     *
     * @param imageToSearch the document image to scan
     * @param requestType the type of OCR request
     * @param docType the type of document being scanned
     * @param text additional text parameters for the scan
     * @return OCR scan results as a string
     * @throws IOException if there's an error processing the image
     * @throws InterruptedException if the scanning process is interrupted
     */
    public String ocrScan(MultipartFile imageToSearch, String requestType, String docType, String text) throws IOException, InterruptedException {
        logger.info("Starting OCR scan for document type: {}", docType);
        byte[] bytes = imageToSearch.getBytes();
        String s3filepath = Configs.S3_FOLDER_OCR;
        String fileFinalPath = s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, bytes);

        String jobId = ocrUtil.startTextDetection(fileFinalPath);
        return ocrUtil.getJobResult(jobId);
    }

    /**
     * Performs KYC verification scan on a document.
     *
     * @param imageToSearch the document image to verify
     * @param requestType the type of KYC verification request
     * @param docType the type of document being verified
     * @param text additional text parameters for verification
     * @return KYC verification results as a string
     * @throws IOException if there's an error processing the image
     */
    public String kycScan(MultipartFile imageToSearch, String requestType, String docType, String text) throws IOException {
        logger.info("Starting KYC scan for document type: {}", docType);
        byte[] bytes = imageToSearch.getBytes();
        JSONObject jsonFromLLM = new JSONObject();
        JSONObject jsonFromReko;

        String prompt = PromptGenerator.generateLLMPrompt(requestType, docType);
        String s3filepath = Configs.S3_FOLDER_KYC;
        String fileFinalPath = s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, bytes);

        // Create the main JSON object
        JSONObject finalJsonResponse = new JSONObject();

        try {
            jsonFromLLM = bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename(), Configs.MODEL_HAIKU);
        } catch (Exception e) {
            logger.error("LLM invocation failed", e);
            jsonFromLLM.put("status", "error");
            jsonFromLLM.put("message", "LLM processing failed");
            jsonFromLLM.put("nextStep", "Please contact admin");
            jsonFromLLM.put(docType, "false");
        }

        finalJsonResponse.put("llmResponse", jsonFromLLM);

        jsonFromReko = labelCheckFromRekognition(imageToSearch, docType);
        finalJsonResponse.put("rekoResponse", jsonFromReko);
        
        logger.info("KYC scan completed for document type: {}", docType);
        return finalJsonResponse.toString();
    }

    /**
     * Checks image labels using AWS Rekognition to verify document type.
     *
     * @param imageToSearch the document image to analyze
     * @param docType the expected document type to verify
     * @return JSONObject containing the label verification results
     */
    public JSONObject labelCheckFromRekognition(MultipartFile imageToSearch, String docType) {
        JSONObject jsonFromReko = new JSONObject();
        
        try {
            byte[] imagebytes = imageToSearch.getBytes();
            Image sourceImage = ImageUtil.getImage(imagebytes);
            jsonFromReko = rekoUtil.getLabelDetails(sourceImage, docType);

            if (jsonFromReko.length() == 0) {
                jsonFromReko.put("valid", false);
                jsonFromReko.put("error", "No labels found");
                jsonFromReko.put("nextStep", "Please try again with a clearer image");
            }
        } catch (IOException e) {
            logger.error("Rekognition processing failed", e);
            jsonFromReko.put("status", "error");
            jsonFromReko.put("message", "Image processing failed");
            jsonFromReko.put("nextStep", "Please contact admin");
            jsonFromReko.put(docType, "false");
        }

        return jsonFromReko;
    }
}