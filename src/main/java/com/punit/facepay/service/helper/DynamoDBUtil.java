package com.punit.facepay.service.helper;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.punit.facepay.service.Configs;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

@Component
public class DynamoDBUtil {
	
	final static Logger logger= LoggerFactory.getLogger(DynamoDBUtil.class);

    DynamoDbClient client = DynamoDbClient.builder()
            .region(Configs.REGION)
            .build();

	public static void main(String[] args) {
		DynamoDBUtil util = new DynamoDBUtil();
		util.putFaceID("test", "123asfasdfasdf");
		System.out.println("\n\n\n\n");
		util.getFaceID("test");


	}

	public void putFaceID(String faceid, String value){
		// Create an item to be stored in DynamoDB
		AttributeValue keyAttribute = AttributeValue.builder().s(faceid).build();
		AttributeValue valueAttribute = AttributeValue.builder().s(value.trim()).build();

		// Create a PutItemRequest to store the item in DynamoDB
		PutItemRequest request = PutItemRequest.builder()
				.tableName(Configs.FACE_TABLE)
				.item(
						Map.of(Configs.FACE_ID, keyAttribute, "value", valueAttribute)
						)
				.build();

		try {
			PutItemResponse response = client.putItem(request);
			logger.info(Configs.FACE_TABLE +" was successfully updated. The request id is "+response.responseMetadata().requestId());

		} catch (ResourceNotFoundException e) {
			logger.error("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", Configs.FACE_TABLE);
			logger.error("Be sure that it exists and that you've typed its name correctly!");
		} catch (DynamoDbException e) {
			logger.error(e.getMessage());
		}
	}

	public String getFaceID(String faceid) {

		// Create a GetItemRequest to retrieve the item from DynamoDB
		GetItemRequest request = GetItemRequest.builder()
				.tableName(Configs.FACE_TABLE)
				.key(
						Map.of(Configs.FACE_ID, AttributeValue.builder().s(faceid).build())
						)
				.build();

		// Retrieve the item from DynamoDB
		GetItemResponse response = client.getItem(request);

		// Get the value from the retrieved item
		
		AttributeValue valueAttribute = response.item().get("value");
		if (valueAttribute !=null) {
			String value = valueAttribute.s().trim();
			logger.info("found face in db with url:" +value);
			
			return value;

		}

		return null;
	}
}
