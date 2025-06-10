package com.punit.AWSPe.service.helper;

import org.springframework.stereotype.Component;

import com.punit.AWSPe.service.Configs;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Component
public class TextractUtil {
    private final Configs configs;
    private final TextractClient textractClient;

    public TextractUtil(Configs configs) {
        this.configs = configs;
        this.textractClient = TextractClient.builder()
                .region(Configs.REGION)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }


    public String getJobResult(String jobid) throws InterruptedException {
        GetDocumentTextDetectionResponse response = getTextDetectionResult(textractClient, jobid);
        return response.toString();
    }

    public String startTextDetection(String documentKey) {

        S3Object s3Object = S3Object.builder()
                .bucket(configs.S3_BUCKET)
                .name(documentKey)
                .build();

        DocumentLocation documentLocation = DocumentLocation.builder()
                .s3Object(s3Object)
                .build();

        StartDocumentTextDetectionRequest startRequest = StartDocumentTextDetectionRequest.builder()
                .documentLocation(documentLocation)
                .build();

        StartDocumentTextDetectionResponse startResponse = textractClient.startDocumentTextDetection(startRequest);

        return startResponse.jobId();
    }

    public GetDocumentTextDetectionResponse getTextDetectionResult(TextractClient textractClient, String jobId) throws InterruptedException {
        GetDocumentTextDetectionRequest getRequest = GetDocumentTextDetectionRequest.builder()
                .jobId(jobId)
                .build();

        GetDocumentTextDetectionResponse response;
        do {
            response = textractClient.getDocumentTextDetection(getRequest);
            Thread.sleep(1000);
        } while (response.jobStatus().toString().equals("IN_PROGRESS"));

        return response;
    }
}
