package com.punit.AWSPe.service.helper;

import com.punit.AWSPe.service.Configs;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class CostCalculater {

    final static Logger logger = LoggerFactory.getLogger(CostCalculater.class);

    // Constants for Anthropic models in Asia Pacific (Mumbai) region
    private static final double CLAUDE_SONNET_COST_PER_1000_INPUT_TOKENS = 0.00300;
    private static final double CLAUDE_SONNET_COST_PER_1000_OUTPUT_TOKENS = 0.01500;
    private static final double CLAUDE_HAIKU_COST_PER_1000_INPUT_TOKENS = 0.00025;
    private static final double CLAUDE_HAIKU_COST_PER_1000_OUTPUT_TOKENS = 0.00125;
    private static final double USD_TO_INR_EXCHANGE_RATE = 83.0; // 1 USD = 83 INR
    private static final double INR_TO_PAISA_CONVERSION = 100.0; // 1 INR = 100 paisa


    public static String calculateCostInINR(String model, JSONObject usageJson) {

        int inputTokens = usageJson.getInt("input_tokens");
        int outputTokens = usageJson.getInt("output_tokens");
        String cost=  String.format("%.2f", convertUSDToINR(CostCalculater.calculateCostInUSD(model,inputTokens, outputTokens)))+" rupees" ;
        logger.info("Cost of this is " + cost);
        return cost;
    }
    public static String calculateCostInINR(String model, int inputTokens, int outputTokens) {
        return ""+convertUSDToINR(CostCalculater.calculateCostInUSD(model,inputTokens, outputTokens));
    }

    public static double calculateCostInUSD(String model, int inputTokens, int outputTokens) {
        double costPer1000InputTokens;
        double costPer1000OutputTokens;

        switch (model.toLowerCase()) {
            case Configs.MODEL_SONET:
                costPer1000InputTokens = CLAUDE_SONNET_COST_PER_1000_INPUT_TOKENS;
                costPer1000OutputTokens = CLAUDE_SONNET_COST_PER_1000_OUTPUT_TOKENS;
                break;
            case Configs.MODEL_HAIKU:
                costPer1000InputTokens = CLAUDE_HAIKU_COST_PER_1000_INPUT_TOKENS;
                costPer1000OutputTokens = CLAUDE_HAIKU_COST_PER_1000_OUTPUT_TOKENS;
                break;
            default:
                return -1;
        }

        double inputCost = (inputTokens / 1000.0) * costPer1000InputTokens;
        double outputCost = (outputTokens / 1000.0) * costPer1000OutputTokens;
        return inputCost + outputCost;
    }

    public static double convertUSDToINR(double costInUSD) {
        return costInUSD * USD_TO_INR_EXCHANGE_RATE;
    }

    public static double convertINRToPaisa(double costInINR) {
        return costInINR * INR_TO_PAISA_CONVERSION;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the model (Claude_Sonnet or Claude_Haiku): ");
        String model = scanner.nextLine();

        System.out.println("Enter the number of input tokens: ");
        int inputTokens = Integer.parseInt(scanner.nextLine());

        System.out.println("Enter the number of output tokens: ");
        int outputTokens = Integer.parseInt(scanner.nextLine());

        try {
            double costInUSD = calculateCostInUSD(model, inputTokens, outputTokens);
            double costInINR = convertUSDToINR(costInUSD);
            double costInPaisa = convertINRToPaisa(costInINR);

            System.out.println("Total cost in USD: " + costInUSD);
            System.out.println("Total cost in INR: " + costInINR);
            System.out.println("Total cost in Paisa: " + costInPaisa);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        scanner.close();
    }
}
