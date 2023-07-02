package com.punit.facepay.service.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.punit.facepay.service.Configs;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
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


	public void storeImageAsync( String folder, String filename,  byte[] imageToSearch)  {

		// Create a temporary file to store the uploaded image
		File tempFile = null;

		try {


            // Define the local file path where you want to store the uploaded file
            String filePath = "./temp/TEMP/" ;

			tempFile = new File(filePath+ filename);
			
			Files.write(tempFile.toPath(), imageToSearch);
			// Transfer the contents of the uploaded file to the temporary file
			//imageToSearch.transferTo(tempFile);

			// Prepare the S3 request

			PutObjectRequest objectRequest = PutObjectRequest.builder()
					.bucket(Configs.S3_PATH)
					.key(filename)
					.build();


			System.out.println(Configs.S3_PATH);
			System.out.println(filename);
			// Upload the image file to S3 asynchronously

			CompletableFuture<PutObjectResponse> future = s3Client.putObject(objectRequest,tempFile.toPath());

			// Handle the completion of the S3 request
			future.whenComplete((response, exception) -> {
				if (exception != null) {
					// Handle the exception
					exception.printStackTrace();

				} else {

					System.out.println("file stored in s3 "+ response);
				}
			});
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		finally {
			// Delete the temporary file after successful upload
			try {
				Files.deleteIfExists(tempFile.toPath());
				System.out.println("temp file deleted " + tempFile);

			} catch (Exception e) {
				e.printStackTrace();
			}


		}


	} 
}
