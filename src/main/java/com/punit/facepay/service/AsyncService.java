package com.punit.facepay.service;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.helper.s3Util;

@Service
public class AsyncService {

	@Autowired
	private s3Util s3Util;
	final static Logger logger= LoggerFactory.getLogger(AsyncService.class);


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
