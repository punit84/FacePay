package com.punit.facepay.service.helper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.facepay.service.Configs;

@Component
public class QArtQueue {

	final static Logger logger= LoggerFactory.getLogger(QArtQueue.class);


	public static void main(String[] args) throws Exception  {
		QArtQueue q= new QArtQueue();

		String messageid= q.sendRequest("upi://pay?pa=nick.jat007@okicici", "qart/nick.jat007@okicici/person.jpg");
		System.out.println(messageid);

	}

	public static String sendRequest(String upi, String userpicURL)   {
		final String queueUrl = Configs.SQS_QUEUE; // Replace 'YOUR_QUEUE_URL' with the actual URL of your SQS queue

		logger.info("sending sms to SQS for qart generation");
		// Create ObjectMapper instance
		ObjectMapper objectMapper = new ObjectMapper();

		// Create message object
		QArtMessage message = new QArtMessage();
		message.setUpi_id(upi);
		message.setBucket(Configs.S3_BUCKET); //awspe.com/qart
		message.setPerson_s3_key(userpicURL); //    /nick.jat007@okicici/person.jpg


		// Convert message object to JSON string
		String jsonMessage = null;
		SendMessageResult mr = null;
		try {
			jsonMessage = objectMapper.writeValueAsString(message);
			logger.info(jsonMessage.toString());



			final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

			SendMessageRequest send_msg_request = new SendMessageRequest()
					.withQueueUrl(queueUrl)
					.withMessageBody(jsonMessage);
			//.withMessageGroupId("1"); // Add MessageGroupId
			//.withMessageDeduplicationId(UUID.randomUUID().toString()); // Add MessageDeduplicationId

			mr=sqs.sendMessage(send_msg_request);
		} catch (IOException e) {
			logger.error("sending sqs failed " + e.getMessage());

			e.printStackTrace();
		}
		logger.info("Message sent successfully!");
		return mr.getMessageId();
	}


}
