package com.punit.facepay.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.helper.S3Utility;

/**
 * Service class for handling asynchronous operations in the facepay application.
 * This service provides methods for executing tasks asynchronously using Spring's @Async functionality.
 */
@Service
public class AsyncService {

    @Autowired
    private S3Utility s3Util;
    
    final static Logger logger = LoggerFactory.getLogger(AsyncService.class);

    /**
     * Performs an asynchronous task processing image data.
     *
     * @param path The path where the image needs to be processed
     * @param imageToSearch The MultipartFile containing the image to be searched
     * @param imagebytes The byte array representation of the image
     * @param responseSTR The response string to be processed
     * @return CompletableFuture<String> containing the result of the async operation
     */
    @Async
    public CompletableFuture<String> performAsyncTask(String path, MultipartFile imageToSearch, byte[] imagebytes,
            String responseSTR) {
        try {
            logger.info("Storing file in S3");
            s3Util.storeinS3(path, imageToSearch, imagebytes, responseSTR, "100%");
        } catch (Exception e) {
            logger.error("Error performing async task", e);
            return CompletableFuture.failedFuture(e);
        }

        return CompletableFuture.completedFuture("Async task completed");
    }
}