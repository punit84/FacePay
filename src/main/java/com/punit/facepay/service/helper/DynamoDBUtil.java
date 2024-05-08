package com.punit.facepay.service.helper;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.facepay.service.Configs;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
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
		//		DynamoDBUtil util = new DynamoDBUtil();
		//		util.putFaceID("test", "123asfasdfasdf", );
		//		System.out.println("\n\n\n\n");
		//		util.getFaceID("test");

		DynamoDBUtil util = new DynamoDBUtil();
		util.getFaceInfo("ca5a30ac-188c-4b97-a3f1-60397b56511f");
		//util.putFaceIDInDB("punit", "jain", "punit.15884", "99110"); 
		//util.putFaceID1("punit", "jain"); 
	}

	public void putFaceIDInDB(String faceId, String value , String email, String mobile, String filePath){
		
		String upiid=UPILinkUtil.getID(value);
		String qartImageURL = Configs.IMAGE_URL_PREFIX+ Configs.IMAGE_URL_QART;
		String imageURL = Configs.IMAGE_URL_PREFIX+filePath;
		
		logger.info("qart url is "+qartImageURL);
		
		logger.info("Image url is "+imageURL);

		// Create an item to be stored in DynamoDB
		AttributeValue keyAttribute = AttributeValue.builder().s(faceId).build();
		AttributeValue valueAttribute = AttributeValue.builder().s(value.trim()).build();
		AttributeValue emailAttribute = AttributeValue.builder().s(email.trim()).build();
		AttributeValue mobileAttribute = AttributeValue.builder().s(mobile.trim()).build();

		AttributeValue qartAttribute = AttributeValue.builder().s(qartImageURL.trim()).build();
		AttributeValue imageAttribute = AttributeValue.builder().s(imageURL.trim()).build();

		// Create a PutItemRequest to store the item in DynamoDB
		PutItemRequest request = PutItemRequest.builder()
				.tableName(Configs.FACE_TABLE)
				.item(
						Map.of(Configs.FACE_ID, keyAttribute, "value", valueAttribute, "email", emailAttribute, "mobile",mobileAttribute, "qart", qartAttribute, "image",imageAttribute  )
						)
				.build();

		try {
			PutItemResponse response = client.putItem(request);
			logger.info(Configs.FACE_TABLE +" was successfully updated. The request id is "+response.responseMetadata().requestId());

		} catch (ConditionalCheckFailedException e) {
			logger.error("Error: The item with FaceID: " + faceId + " already exists.");
		} catch (ResourceNotFoundException e) {
			logger.error("Error: The Amazon DynamoDB table \"" + Configs.FACE_TABLE + "\" can't be found.");
			logger.error("Be sure that it exists and that you've typed its name correctly!");
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
		}
	}

	//
	//	public void putNewFaceIDInDB(String faceId, String value , String email, String mobile) {
	//
	//		Map<String, AttributeValue> itemKey = new HashMap<>();
	//		itemKey.put("Id", AttributeValue.builder().s(faceId).build());
	//
	//		Map<String, String> expressionAttributeNames = new HashMap<>();
	//		expressionAttributeNames.put("#id", "Id");
	//
	//		Map<String, AttributeValue> itemValues = new HashMap<>();
	//		itemValues.put(":value", AttributeValue.builder().s(value.trim()).build());
	//		itemValues.put(":email", AttributeValue.builder().s(email.trim()).build());
	//		itemValues.put(":mobile", AttributeValue.builder().s(mobile.trim()).build());
	//
	//		PutItemRequest request = PutItemRequest.builder()
	//				.tableName(Configs.FACE_TABLE)
	//				.item(itemKey) // Use itemKey instead of itemValues
	//				.conditionExpression("attribute_not_exists(#id)")
	//				.expressionAttributeValues(itemValues)
	//				.expressionAttributeNames(expressionAttributeNames)
	//				.build();
	//
	//		try {
	//			client.putItem(request);
	//			logger.info("Item with FaceID: " + faceId + " was successfully updated.");
	//		} catch (ConditionalCheckFailedException e) {
	//			logger.error("Error: The item with FaceID: " + faceId + " already exists.");
	//		} catch (ResourceNotFoundException e) {
	//			logger.error("Error: The Amazon DynamoDB table \"" + Configs.FACE_TABLE + "\" can't be found.");
	//			logger.error("Be sure that it exists and that you've typed its name correctly!");
	//		} catch (Exception e) {
	//			logger.error("Error: " + e.getMessage());
	//		}
	//	}


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
			logger.info("found face in db linked with url:" +value);

			return value;

		}

		return null;
	}

	public String getFaceInfo(String faceid) {

		GetItemRequest request = GetItemRequest.builder()
				.tableName(Configs.FACE_TABLE)
				.key(
						Map.of(Configs.FACE_ID, AttributeValue.builder().s(faceid).build())
						)
				.build();

		// Retrieve the item from DynamoDB
		GetItemResponse response = client.getItem(request);
		// Extract item attributes
		Map<String, AttributeValue> itemAttributes = response.item();
		Map<String, String> parsedAttributes = new HashMap<>();

		// Parse attributes and trim values
		for (Map.Entry<String, AttributeValue> entry : itemAttributes.entrySet()) {
			String attributeName = entry.getKey();
			AttributeValue attributeValue = entry.getValue();
			String value = attributeValue.s().trim(); // Trim the value
			parsedAttributes.put(attributeName, value);
		}

		// Convert to JSON
		ObjectMapper objectMapper = new ObjectMapper();
		String json =null;
		try {
			json = objectMapper.writeValueAsString(parsedAttributes);
			logger.info(json);

		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}


		return json;
	}
}
