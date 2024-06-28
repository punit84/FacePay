package com.punit.facepay.rest;

import com.punit.facepay.service.Configs;
import com.punit.facepay.service.FaceScanService;
import com.punit.facepay.service.helper.BedrockUtill;
import com.punit.facepay.service.helper.s3Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    public Object kycScan(MultipartFile imageToSearch, String text) throws IOException {
        logger.info("************ call claude ********");

        byte[] bytes = imageToSearch.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);

        return	bedrockUtil.invokeHaiku(base64Image, Configs.IMAGE_PROMPT + text );

    }


}