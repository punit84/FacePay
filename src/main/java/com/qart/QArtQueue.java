package com.qart;

import java.io.IOException;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.facepay.service.Configs;


    public class QArtQueue {

        public static void main(String[] args) throws Exception  {
        	QArtQueue q= new QArtQueue();
        	
        	String messageid= q.sendRequest("upi://pay?pa=nick.jat007@okicici", "qart/nick.jat007@okicici/person.jpg");
        	System.out.println(messageid);
        	
        }

        public static String sendRequest(String upi, String userpicURL) throws Exception  {
            final String queueUrl = Configs.SQS_QUEUE; // Replace 'YOUR_QUEUE_URL' with the actual URL of your SQS queue

            // Create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Create message object
            QArtMessage message = new QArtMessage();
            message.setUpiId(upi);
            message.setBucket(Configs.S3_PATH_ADMIN); //awspe.com/qart
            message.setPersonS3Key(userpicURL); //    /nick.jat007@okicici/person.jpg
            
          
            // Convert message object to JSON string
            String jsonMessage = null;
            try {
                jsonMessage = objectMapper.writeValueAsString(message);
                System.out.println(jsonMessage.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }

            final AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();

            SendMessageRequest send_msg_request = new SendMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMessageBody(jsonMessage);
                    //.withMessageGroupId("1"); // Add MessageGroupId
                    //.withMessageDeduplicationId(UUID.randomUUID().toString()); // Add MessageDeduplicationId

            SendMessageResult mr=sqs.sendMessage(send_msg_request);
            System.out.println("Message sent successfully!");
            return mr.getMessageId();
        }
        
        
}
