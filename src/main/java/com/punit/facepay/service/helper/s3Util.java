package com.punit.facepay.service.helper;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;

import com.punit.facepay.service.Configs;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Component
public class s3Util {


	//DynamoDbClient dbClient = DynamoDbClient.builder()
	//		.region(Configs.REGION)
	//		.build();

	S3AsyncClient s3Client = S3AsyncClient.builder()
			.region(Configs.REGION)
			.credentialsProvider(DefaultCredentialsProvider.create())
			.build();



	
	private void storeImageAsync( String filename,  byte[] imageToSearch)  {

		// Create a temporary file to store the uploaded image

		// Prepare the S3 request

		// Upload the file to Amazon S3 bucket

		PutObjectRequest objectRequest = PutObjectRequest.builder()
				.bucket(Configs.S3_PATH)
				.key(filename)
				.build();


		System.out.println(Configs.S3_PATH);
		System.out.println(filename);
		// Upload the image file to S3 asynchronously

		try {

			CompletableFuture<PutObjectResponse> future = s3Client.putObject(objectRequest,AsyncRequestBody.fromBytes(imageToSearch));

			// Handle the completion of the S3 request
			future.whenComplete((response, exception) -> {
				if (exception != null) {
					// Handle the exception
					exception.printStackTrace();
					System.out.println("file stored in s3 "+ exception.getMessage());

				} else {
					// Get the file URL
					String fileUrl = s3Client.utilities().getUrl(builder -> builder.bucket(Configs.S3_PATH).key(filename)).toExternalForm();

					System.out.println("file stored in s3 "+ fileUrl);
				}
			});

		} catch (Exception e) {


		}finally {

		}




	} 

	public String storeinS3(MultipartFile imageToSearch, byte[] imagebytes, String responseSTR, String similarity) {
		String fileName = StringUtils.cleanPath(System.currentTimeMillis()+"_"+imageToSearch.getOriginalFilename()  );

		fileName = fileName.replaceAll("\\W", "");
		if(responseSTR ==null) {

			fileName =  "images/failed/"+fileName;

		}else {

			fileName =  "images/"+similarity+"_"+responseSTR+"/"+fileName;
		}
		storeImageAsync( fileName, imagebytes);
		System.out.println("url is : " + responseSTR);


		return responseSTR;
	}

}
