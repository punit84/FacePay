package com.punit.facepay.service.helper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ChequeValidater {

    public static BankDocument checkStringAndCalculateConfidence(String input, int  lenght) {
        // Define the list of keywords to search for

        HashMap resultset = new HashMap<String, String>();
        BankDocument bankDocument = new BankDocument(lenght);
        List<String> keywords = Arrays.asList("BANK", "INDIA", "CHEQUE", "PAYABLE",
                "VALID FOR 3 MONTHS ONLY", "CTS-2010",
                "IFSC", "RUPEES", "SIGN", "ACCOUNT" ) ;

        // Initialize variables to count matches and store total keywords
        int totalKeywords = keywords.size();

        // Convert input to uppercase for case-insensitive matching
        String upperInput = input.toUpperCase();

        // Check each keyword in the input string and print the result
        for (String keyword : keywords) {
            boolean isFound = upperInput.contains(keyword);
            resultset.put(keyword, isFound);

            if (isFound) {
                bankDocument.increaseMatchCount();
            }
            // Print if the keyword is found or not
            //System.out.println(keyword + " : " + isFound);
        }

        // Calculate confidence score as a percentage
        bankDocument.calculateConfidence(totalKeywords);
        return bankDocument;
    }
}
