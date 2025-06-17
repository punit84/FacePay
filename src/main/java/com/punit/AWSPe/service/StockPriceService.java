package com.punit.AWSPe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.AWSPe.service.helper.BedrockUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockPriceService {

    //    private static final String API_KEY = System.getenv("ALPHA_VANTAGE_KEY");
    private static final String API_KEY = "xxx";
    private static final String finhub="xxx";

    @Autowired
    private BedrockUtil bedrockUtil = new BedrockUtil();
    private final RestTemplate restTemplate;

    public StockPriceService() {

        this.restTemplate = new RestTemplate();
    }

    public String getprice(String userQuery) {
        try {
            // Step 1: Fetch stock price using RestTemplate
            String url = String.format("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s", "AAPL", API_KEY);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String stockData = response.getBody();
            String extractedPrice = extractPriceFromJson(stockData);

            // Step 2: Construct prompt
            String prompt = "UReal-time stock price of :" + userQuery + "\n"
                    + "is: " + extractedPrice  ;
            System.out.println("Sending Payload: " + prompt);
//
//            JSONObject contentObject = new JSONObject()
//                    .put("text", prompt);
//
//            JSONObject messageObject = new JSONObject()
//                    .put("role", "user")
//                    .put("content", new JSONArray().put(contentObject));
//
//            JSONObject payload = new JSONObject()
//                    .put("messages", new JSONArray().put(messageObject));
//            // Add inference parameters at the top level of the payload
//            // System.out.println("Sending Payload: " + payload.toString(2));

            return bedrockUtil.converse(prompt.toString());

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String extractPriceFromJson(String json) {
        try {
            int index = json.indexOf("\"05. price\":");
            if (index != -1) {
                int start = json.indexOf("\"", index + 13) + 1;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }
        } catch (Exception ignored) {
        }
        return "Unavailable";
    }

    public static void main(String[] args) {

        StockPriceService stockService = new StockPriceService();
        System.out.println(stockService.getprice("AAPL"));
        //System.out.println(stockService.getprice("paytm"));

    }
}