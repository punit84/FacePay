package com.punit.AWSPe.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service class for handling asynchronous operations in the facepay application.
 * This service provides methods for executing tasks asynchronously using Spring's @Async functionality.
 */
@Service
public class AsyncService {

	@Autowired
	private com.punit.AWSPe.service.helper.s3Util s3Util;
	final static Logger logger= LoggerFactory.getLogger(AsyncService.class);


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
	public CompletableFuture<String> performAsyncTask(String path, MultipartFile imageToSearch, byte[] imagebytes, String responseSTR) {
		// Perform your asynchronous task here
		// This could be a time-consuming operation, API call, or any other async logic

		// Simulating a delay of 5 seconds
		try {
			logger.info("Storing file in s3");
			s3Util.storeinS3(path, imageToSearch, imagebytes, responseSTR, "100%");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return CompletableFuture.completedFuture("Async task completed");
	}
}
