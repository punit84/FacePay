package com.qart;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QartGenerator {

    public static void main(String[] args) {
        try {
            QartConfig config = new QartConfig();

            // Make HTTP request
            int responseCode = makeRequest(config, "sample-UPI", "qart/p.khokhar27@okhdfcbank/person.jpg");

            // Print response code
            System.out.println("Response Code: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int makeRequest(QartConfig config, String upi, String userpicURL) throws Exception {
        // Create URL
        URL obj = new URL(config.getUrl());
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        // Set request method
        connection.setRequestMethod("GET");

        // Set headers
        connection.setRequestProperty("upi-id", upi);
        connection.setRequestProperty("bucket", config.getBucket());
        connection.setRequestProperty("person-s3-key", userpicURL);
        connection.setRequestProperty("model-endpoint", config.getModelEndpoint());
        connection.setRequestProperty("reduction-ratio", String.valueOf(config.getReductionRatio()));
        connection.setRequestProperty("theme", String.valueOf(config.getTheme()));
        connection.setRequestProperty("seed", String.valueOf(config.getSeed()));
        connection.setRequestProperty("num-inference-steps", String.valueOf(config.getNumInferenceSteps()));
        connection.setRequestProperty("num-images-per-prompt", String.valueOf(config.getNumImagesPerPrompt()));
        connection.setRequestProperty("strength", String.valueOf(config.getStrength()));
        connection.setRequestProperty("guidance-scale", String.valueOf(config.getGuidanceScale()));
        connection.setRequestProperty("controlnet-1-conditioning-scale", String.valueOf(config.getControlNet1ConditioningScale()));
        connection.setRequestProperty("controlnet-1-guidance-start", String.valueOf(config.getControlNet1GuidanceStart()));
        connection.setRequestProperty("controlnet-1-guidance-end", String.valueOf(config.getControlNet1GuidanceEnd()));
        connection.setRequestProperty("controlnet-2-conditioning-scale", String.valueOf(config.getControlNet2ConditioningScale()));
        connection.setRequestProperty("controlnet-2-guidance-start", String.valueOf(config.getControlNet2GuidanceStart()));
        connection.setRequestProperty("controlnet-2-guidance-end", String.valueOf(config.getControlNet2GuidanceEnd()));

        // Get response code
        int responseCode = connection.getResponseCode();

        // Print response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println("Response: " + response.toString());

        return responseCode;
    }
}