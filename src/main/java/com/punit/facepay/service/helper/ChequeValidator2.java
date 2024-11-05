package com.punit.facepay.service.helper;

import java.util.HashMap;
import java.util.Map;

public class ChequeValidator2 {

    public static BankDocument checkStringAndCalculateConfidence(String input, int lenght) {
        // Define the keywords and their respective weights
        Map<String, Double> keywordWeights = new HashMap<>();
        keywordWeights.put("BANK", 1.0);
        keywordWeights.put("INDIA", 0.8);
        keywordWeights.put("CHEQUE", 1.2);
        keywordWeights.put("PAYABLE", 1.0);
        keywordWeights.put("VALID FOR 3 MONTHS ONLY", 1.5);
        keywordWeights.put("CTS", 0.5);
        keywordWeights.put("IFSC", 1.0);
        keywordWeights.put("RUPEES", 1.0);
        keywordWeights.put("SIGN", 0.7);
        // Initialize variables to calculate total weight and matched weight
        double totalWeight = 0.0;
        double matchedWeight = 0.0;

        if (lenght>200) {
            matchedWeight = 0;
            totalWeight = 1;
        }

        // Convert input to uppercase for case-insensitive matching
        String upperInput = input.toUpperCase();

        BankDocument bankDocument = new BankDocument(lenght);



        // Check each keyword in the input string, update weights, and print the result
        for (Map.Entry<String, Double> entry : keywordWeights.entrySet()) {
            String keyword = entry.getKey();
            double weight = entry.getValue();
            totalWeight += weight;

            boolean isFound = upperInput.contains(keyword);
            bankDocument.addResult(keyword,isFound);
            if (isFound) {
                matchedWeight += weight;
            }

            // Print if the keyword is found or not, along with its weight
            System.out.println(keyword + " : " + isFound + " (Weight: " + weight + ")");
        }

        // Calculate weighted confidence score as a percentage
        bankDocument.confidence = (matchedWeight / totalWeight) * 100;
        System.out.println("confidence"  + " : " + bankDocument.confidence);

        return bankDocument;
    }
}
