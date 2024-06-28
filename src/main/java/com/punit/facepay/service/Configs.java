package com.punit.facepay.service;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;

@Component
public class Configs {
	
	
//	public static final String MODEL_VERSION ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";
	public static final String COLLECTION_ID = "AWSPeFaceCollection";
	public static final Region REGION = Region.AP_SOUTH_1;
	public static final String FACE_ID = "faceid";
	public static final String FACE_EMAIL = "email";
	public static final String FACE_MOBILE = "mobile";
	public static final String FACE_IMAGE = "image";
	public static final String QART_MODEL = "qart-face-async-tile-canny-16-05-24-01-22-11";
	
	
//	public static final String AI_PROMPT = "\"Please provide summary of the given face in format:\n"
//			+ "\n"
//			+ "<br>Age: [Age]\n"
//			+ "<br>Gender: [Gender]\n"
//			+ "<EyeWear> : [Sunglass|eyewear]\n"
//			+ "<br>Mood: [Mood]<br>\n"
//			+ "<br>Facial Expression: [Expression]\n"
//			+ "<br>Image Quality: [Quality]\n";
//	
	
	public static final String AI_PROMPT ="Provide a crisp summary in not more than 1 lines include photo quality details, do not include confidence details:";
	public static final String IMAGE_PROMPT ="Evaluation Criteria:\n" +
			"•\tText Clarity: The text should be sharp and readable without needing zoom.\n" +
			"•\tImage Resolution: The resolution should be high enough that zooming in does not significantly pixelate the text.\n" +
			"•\tDocument Completeness: All required information should be visible in the image without any parts being cut off. The document must be fully visible, with all edges (especially for cheques) intact and not cropped.\n" +
			"•\tOverall Cleanliness: The document should be clean and free from major stains, marks, or obstructions.\n" +
			"•\tDocument Type Appropriateness: The document type should be consistent with the expected format for containing banking details. Output should be in 1-5 words\n" +
			"Result:\n" +
			"•\tValid document: If all the above criteria are met, the document quality is deemed good.\n" +
			"•\tInvalid document: If any of the criteria are not met, specify which criteria failed and why.\n" +
			"•\tDocument type: The document type should be consistent with the expected format for containing banking details. Output should be in 1-5 words\n" +
			"•\tAccount Number (or A/C No.): Verify if the image includes an account number or abbreviation such as \"A/C No.\"\n" +
			"•\tAccount Name: Check for the presence of the account holder's name.\n" +
			"•\tIFSC Code: Confirm that the image displays the IFSC code.\n";

	//	https://awspe.com/qart/punit.15884-1@okhdfcbank/0_qart.png
	public static final String IMAGE_URL_PREFIX = "https://awspe.com/";
	public static final String IMAGE_URL_QART = "/0_qart.png";

	
	
	public static final String FACE_UPI = "value";
	public static final String FACE_QART = "qart";

	public static final String S3_BUCKET = "awspe.com";
 	public static final String S3_FOLDER_SCAN = "scan/";
 	public static final String S3_FOLDER_REGISTER = "qart/";

 	
//	
//	public static final String S3_PATH_REGISTER = "/qart";
//
//	public static final String S3_PATH_ADMIN = "awspe.com";

	//public static final String S3_PATH = "s3://muhpe.com/images/";
	public static int MAX_MATCHES = 1;

	public static final String FACE_TABLE = "FaceTable";
	public static final String FACE_NOTFOUND = "REGISTER-FACE-FIRST-VISIT-ADMIN-PAGE";
	public static final String FACE_NOHUMAN = "NO-HUMAN-FACE-FOUND";
	public static final String FACE_ALREADY_EXIST = "Already Registered Contact Admin";

	
	public static final String SERVER_ERROR = "SERVER_ERROR";


	public static final int DEVICE_IOS = 0;
	public static final int DEVICE_ANDROID = 1;
	public static final int DEVICE_WEB = 2;
	
	public static final String SQS_QUEUE  = "https://sqs.ap-south-1.amazonaws.com/057641535369/qart";


}
