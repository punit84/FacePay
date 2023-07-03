package com.punit.facepay.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.helper.s3Util;

@Service
public class AsyncService {

	@Autowired
	private s3Util s3Util;

	@Async
	public CompletableFuture<String> performAsyncTask(MultipartFile imageToSearch, byte[] imagebytes, String responseSTR) {
		// Perform your asynchronous task here
		// This could be a time-consuming operation, API call, or any other async logic

		// Simulating a delay of 5 seconds
		try {
			s3Util.storeinS3(imageToSearch, imagebytes, responseSTR, "100%");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return CompletableFuture.completedFuture("Async task completed");
	}
}
