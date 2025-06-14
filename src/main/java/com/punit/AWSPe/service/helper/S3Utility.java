package com.punit.AWSPe.service.helper;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.punit.AWSPe.service.Configs;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Component
public class S3Utility {

	final static Logger logger= LoggerFactory.getLogger(S3Utility.class);

	//DynamoDbClient dbClient = DynamoDbClient.builder()
	//		.region(Configs.REGION)
	//		.build();

	S3AsyncClient s3Client = S3AsyncClient.builder()
			.region(Configs.REGION)
			.credentialsProvider(DefaultCredentialsProvider.create())
			.build();


	
	public String storeAdminImageAsync(String path,  String filename,  byte[] imageToSearch)  {

		// Create a temporary file to store the uploaded image

		// Prepare the S3 request

		// Upload the file to Amazon S3 bucket

		 String fileNameFinal =  filename + "/" +System.currentTimeMillis()+".jpg";

		PutObjectRequest objectRequest = PutObjectRequest.builder()
				.bucket(path)
				.key(fileNameFinal)
				.build();


		logger.info("path is " + path);
		logger.info(fileNameFinal);
		// Upload the image file to S3 asynchronously

		try {

			CompletableFuture<PutObjectResponse> future = s3Client.putObject(objectRequest,AsyncRequestBody.fromBytes(imageToSearch));

			// Handle the completion of the S3 request
			future.whenComplete((response, exception) -> {
				if (exception != null) {
					// Handle the exception
					exception.printStackTrace();
					logger.info("file stored in s3 "+ exception.getMessage());

				} else {
					// Get the file URL
					String fileUrl = s3Client.utilities().getUrl(builder -> builder.bucket(path).key(fileNameFinal)).toExternalForm();

					logger.info("file stored in s3 "+ fileUrl);
				}
			});

		} catch (Exception e) {


		}finally {

		}
		
		return fileNameFinal;
		
	}


	
	private void storeImageAsync(String path, String filename,  byte[] imageToSearch)  {

		// Create a temporary file to store the uploaded image

		// Prepare the S3 request

		// Upload the file to Amazon S3 bucket

		PutObjectRequest objectRequest = PutObjectRequest.builder()
				.bucket(path)
				.key(filename)
				.build();


		logger.info("path is " + path);
		logger.info(filename);
		// Upload the image file to S3 asynchronously

		try {

			CompletableFuture<PutObjectResponse> future = s3Client.putObject(objectRequest,AsyncRequestBody.fromBytes(imageToSearch));

			// Handle the completion of the S3 request
			future.whenComplete((response, exception) -> {
				if (exception != null) {
					// Handle the exception
					exception.printStackTrace();
					logger.info("file stored in s3 "+ exception.getMessage());

				} else {
					// Get the file URL
					String fileUrl = s3Client.utilities().getUrl(builder -> builder.bucket(path).key(filename)).toExternalForm();

					logger.info("file stored in s3 "+ fileUrl);
				}
			});

		} catch (Exception e) {


		}finally {

		}




	} 

	public String storeinS3(String path, MultipartFile imageToSearch, byte[] imagebytes, String folder, String similarity) {
		String fileName = StringUtils.cleanPath(System.currentTimeMillis()+".jpg" );

		logger.info("folder is "+ folder);
		fileName = fileName.replaceAll("\\s", "");
		if(folder ==null) {

			fileName =  Configs.S3_FOLDER_SCAN+"failed/"+fileName;

		}else {

			fileName =  Configs.S3_FOLDER_SCAN+folder+"/"+similarity+"_"+fileName;
		}
		storeImageAsync( path, fileName, imagebytes);
		logger.info("url is : " + fileName);


		return fileName;
	}

}
