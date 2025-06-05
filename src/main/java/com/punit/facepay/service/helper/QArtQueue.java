package com.punit.facepay.service.helper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.facepay.service.Configs;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Component
public class QArtQueue {

    final static Logger logger = LoggerFactory.getLogger(QArtQueue.class);
    private static final SqsClient sqsClient = SqsClient.builder().build();

    public static void main(String[] args) throws Exception {
        QArtQueue q = new QArtQueue();
        String messageid = q.sendRequest("upi://pay?pa=nick.jat007@okicici", "qart/nick.jat007@okicici/person.jpg");
        System.out.println(messageid);
    }

    public String sendRequest(String upi, String userpicURL) {
        final String queueUrl = Configs.SQS_QUEUE;

        logger.info("sending sms to SQS for qart generation");
        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Create message object
        QArtMessage message = new QArtMessage();
        message.setUpi_id(upi);
        message.setBucket(Configs.S3_BUCKET);
        message.setPerson_s3_key(userpicURL);

        // Convert message object to JSON string
        String jsonMessage = null;
        SendMessageResponse response = null;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
            logger.info(jsonMessage);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(jsonMessage)
                .build();

            response = sqsClient.sendMessage(sendMsgRequest);
        } catch (IOException e) {
            logger.error("sending sqs failed " + e.getMessage());
            e.printStackTrace();
        }
        logger.info("Message sent successfully!");
        return response != null ? response.messageId() : null;
    }
}