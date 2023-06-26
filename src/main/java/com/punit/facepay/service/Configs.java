package com.punit.facepay.service;

import software.amazon.awssdk.regions.Region;

public class Configs {
	
	
	public static final String MODEL_VERSION ="arn:aws:rekognition:ap-south-1:057641535369:project/logos_1/version/logos_1.2023-06-15T13.21.51/1686815511992";
	public static final String COLLECTION_ID = "FacePayCollection";
	public static final Region REGION = Region.AP_SOUTH_1;
	public static final String FACE_ID = "faceid";
	public static int MAX_MATCHES = 1;

	public static final String FACE_TABLE = "FaceTable";


}
