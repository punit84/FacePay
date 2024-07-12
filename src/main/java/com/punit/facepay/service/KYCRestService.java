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

@Service
public class KYCRestService {

    @Autowired
    private com.punit.facepay.service.helper.s3Util s3Util;

    @Autowired
    private BedrockUtil bedrockUtil;

    final static Logger logger= LoggerFactory.getLogger(KYCRestService.class);

    @Autowired
    TextractUtil ocrUtil;

    @Autowired
    private RekoUtil rekoUtil;

    @Autowired
    private JsonUtil jsonUtil;


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
        JSONObject jsonFromLLM = null;
        JSONObject jsonFromReko = null;

        String prompt = PromptGenerator.generateLLMPrompt(requestType, docType);
        String s3filepath = Configs.S3_FOLDER_KYC;
        String fileFinalPath = s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, bytes);

        // Create the main JSON object
        JSONObject finalJsonReponse = new JSONObject();

        try {
            jsonFromLLM = bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename(), Configs.MODEL_HAIKU);

            // Add sub-JSON objects to the main JSON object


        } catch (Exception e) {
            logger.error("Invocation to LLM failed with error " + e.getMessage());
            jsonFromLLM.put("Next Step", "Please Contact Admin");
            jsonFromLLM.put(docType, "false");
        }

        finalJsonReponse.put("llmResponse", jsonFromLLM);

        jsonFromReko =  labelCheckFromRekognition(imageToSearch, docType);
        finalJsonReponse.put("rekoResponse", jsonFromReko);
        logger.info("Final json response: " +finalJsonReponse.toString());
        return finalJsonReponse.toString();
        //       return	bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename() , Configs.MODEL_SONET);
    }


    public JSONObject labelCheckFromRekognition(MultipartFile imageToSearch, String docType) {
        byte[] imagebytes= null;
        JSONObject jsonFromReko = new JSONObject();
        try {
            imagebytes = imageToSearch.getBytes();
            Image souImage = ImageUtil.getImage(imagebytes);
            jsonFromReko = rekoUtil.getLabelDetails(souImage, docType);

            if (jsonFromReko.length() == 0){
                jsonFromReko.put("valid Document", "false");
                jsonFromReko.put("error", "No labels found");
            }
        } catch (IOException e) {
            logger.error("Invocation to Rekognition failed with error " + e.getMessage());
            jsonFromReko.put("Next Step", "Please Contact Admin");
            jsonFromReko.put(docType, "false");
        }

            //* Label detection in rekognition */
        return jsonFromReko;
    }


}