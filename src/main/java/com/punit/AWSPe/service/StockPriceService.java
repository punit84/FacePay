package com.punit.AWSPe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.AWSPe.service.helper.BedrockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

@Service
public class StockPriceService {

    //    private static final String API_KEY = System.getenv("ALPHA_VANTAGE_KEY");
    private static final String API_KEY = "xxx";
    private static final String finhub="xxx";

 //   @Value("${finnhub.api.key}")
    private String apiKey= "d18k3m9r01qg5218i54gd18k3m9r01qg5218i550";

    @Autowired
    private BedrockUtil bedrockUtil = new BedrockUtil();
    private final RestTemplate restTemplate;
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public StockPriceService() {

        this.restTemplate = new RestTemplate();
    }

    public String handleQuery(String query) {
        try {
            String companyName = extractCompanyName(query);
            String symbol = findSymbol(companyName);
            if (symbol == null) {
                StockResponse res= new StockResponse(companyName, symbol, "Current price: NA");
                return   mapper.writeValueAsString(res);
            }
            double price = fetchPrice(symbol);
            System.out.println("price is " +price);
            StockResponse res= new StockResponse(companyName, symbol, "Current price: $" + price);
            return   mapper.writeValueAsString(res);
        } catch (Exception e) {
        }
        return query+ " : company not found";

    }

    private String extractCompanyName(String query) {
        query = query.toLowerCase(Locale.ROOT);
        query = query.replaceAll("(?i)(what|is|the|stock|price|of|show|me)", "").trim();
        return query;
    }

    private String findSymbol(String companyName) throws Exception {
        String url = "https://finnhub.io/api/v1/search?q=" + companyName + "&token=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("my response is ");

        System.out.println(response.body().toString());
        JsonNode root = mapper.readTree(response.body());
        JsonNode resultArray = root.get("result");

        if (resultArray != null && resultArray.isArray() && resultArray.size() > 0) {
            // Optional: filter only company names that match closely
            for (JsonNode item : resultArray) {
                String description = item.get("description").asText().toLowerCase();
                if (description.contains(companyName.toLowerCase())) {
                    return item.get("symbol").asText(); // return the closest match
                }
            }
        }
        return null;

    }

    private double fetchPrice(String symbol) throws Exception {
        String url = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        return root.get("c").asDouble(); // 'c' = current price
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
        System.out.println(stockService.handleQuery("amazon"));
        //System.out.println(stockService.getprice("paytm"));

    }
}