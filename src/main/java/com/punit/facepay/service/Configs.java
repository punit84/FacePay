package com.punit.facepay.service;

import org.springframework.stereotype.Component;

import software.amazon.awssdk.regions.Region;

@Component
public class Configs {
	
	
	public static final String MODEL_VERSION ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";
	public static final String COLLECTION_ID = "FacePayCollection";
	public static final Region REGION = Region.AP_SOUTH_1;
	public static final String FACE_ID = "faceid";
	public static final String S3_PATH = "awspe.com/scan";
	
	public static final String S3_PATH_ADMIN = "awspe.com/qart";

	//public static final String S3_PATH = "s3://muhpe.com/images/";
	public static int MAX_MATCHES = 1;

	public static final String FACE_TABLE = "FaceTable";
	public static final String FACE_NOTFOUND = "REGISTER-FACE-FIRST-VISIT-ADMIN-PAGE";
	public static final String FACE_NOHUMAN = "NO-HUMAN-FACE-FOUND";
	
	public static final String SERVER_ERROR = "SERVER_ERROR";


	public static final int DEVICE_IOS = 0;
	public static final int DEVICE_ANDROID = 1;
	public static final int DEVICE_WEB = 2;


}
