package com.punit.facepay.service;

import com.punit.facepay.service.helper.BedrockUtill;
import com.punit.facepay.service.helper.PromptGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Base64;

@Service
public class KYCRestService {

    @Autowired
    private com.punit.facepay.service.helper.s3Util s3Util;

    @Autowired
    private BedrockUtill bedrockUtil;
    final static Logger logger= LoggerFactory.getLogger(KYCRestService.class);
    @Autowired
    private Configs configs;

    public String kycScan(MultipartFile imageToSearch, String requestType, String docType, String text) throws IOException {
        logger.info("************ call claude ********");
        byte[] bytes = imageToSearch.getBytes();

        String prompt = PromptGenerator.generateLLMPrompt(requestType,docType);

        String s3filepath= Configs.S3_FOLDER_KYC ;
        String fileFinalPath=s3Util.storeAdminImageAsync(Configs.S3_BUCKET, s3filepath, bytes);
        return	bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename() , Configs.MODEL_HAIKU);
 //       return	bedrockUtil.invokeAnthropic(bytes, prompt, imageToSearch.getOriginalFilename() , Configs.MODEL_SONET);

    }


}