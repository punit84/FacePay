package com.punit.facepay.service.helper;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.punit.facepay.rest.FaceScanRestController;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PromptGenerator {
    final static Logger logger = LoggerFactory.getLogger(PromptGenerator.class);

    String OUTPUT_JSON_BANK = "{\n" +
            "    \"ifsc_code\": \"\",\n" +
            "    \"account_number\": \"\",\n" +
            "    \"account_name\": \"\",\n" +
            "    \"output_tokens\": \"\",\n" +
            "    \"input_tokens\": \"\",\n" +
            "    \"Valid Document\": \"\"\n" +
            "}";


    public static Map<String, Object> getDocumentTypes() {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("UpdateBankDetails", new String[]{"cheque", "Passbook", "Bank Statement"});
        documentData.put("UpdateName", new String[]{"Passport", "Driving License", "Voter's ID card", "Pan Card", "Aadhaar Card", "NRGEA Job Card"});
        documentData.put("UpdateAddress", new String[]{"Electricity Bill", "Gas Bill", "Bank Account Statement", "Landline Bill", "Life Insurance Policy", "Registered Lease/Rent Agreement"});

        return documentData;
    }

    public static JsonNode processJson(String jsonResponse) {
        ObjectMapper objectMapper = new ObjectMapper();
        int falseCount = 0;

        ObjectNode cleanedJson = objectMapper.createObjectNode();
        final int[] trueCount = {0};
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);


            logger.info("Number of true values: " + trueCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cleanedJson;
    }

    public static String generateLLMPrompt(String requestType, String docType) {
        logger.info("Request type is: " + requestType);
        logger.info("DocType is: " + docType);

        if (requestType.contains(" ")) {
            requestType = "UpdateBankDetails";
            docType = "cheque";
            logger.info("Updated request type is: " + requestType);
        }

        StringBuilder documentValidation = new StringBuilder();
        documentValidation.append("Validation Criteria:\n")
                .append("  • Text Clarity: The text should be sharp and readable without needing zoom.\n")
                .append("  • Image Resolution: The resolution should be high enough that zooming in does not significantly pixelate the text.\n")
                .append("  • Document Completeness: All required information should be visible in the image without any parts being cut off. The document must be fully visible, with all edges (especially for cheques) intact and not cropped.\n")
                .append("  • Overall Cleanliness: The document should be clean and free from major stains, marks, or obstructions.\n");

        JSONObject outputJson = new JSONObject();
        String criteria = "";

        outputJson.put("valid_document", "true/false");
        outputJson.put("document type", "");
        outputJson.put("invalid document reason", "");

        switch (requestType) {
            case "UpdateBankDetails":
                outputJson.put("account_number", "");
                outputJson.put("account_name", "");
                outputJson.put("ifsc_code", "");
                outputJson.put("is Cheque", "true/false");

                switch (docType) {
                    case "cheque":
                        documentValidation.append("• Document Valid: The image should clearly represent a bank cheque and contain details such as bank name, account number, account name, and IFSC code.\n")
                                .append("• Valid Cheque contains the texts 'valid for 3 months only', 'Bank', 'Payable at Par', 'RUPEES', and 'Please Sign'.\n");
                        criteria = documentValidation.toString() +
                                "• Account Number (or A/C No.): Verify if the image includes an account number or abbreviation such as 'A/C No.'\n" +
                                "• Account Name: Check for the presence of the account holder's name.\n" +
                                "• IFSC Code: Confirm that the image displays the IFSC code.\n";
                        break;
                    case "Passbook":
                    case "Bank Statement":
                        criteria = documentValidation.toString() +
                                "• Account Number (or A/C No.): Verify if the image includes an account number or abbreviation such as 'A/C No.'\n" +
                                "• Account Name: Check for the presence of the account holder's name.\n" +
                                "• IFSC Code: Confirm that the image displays the IFSC code.\n";
                        break;
                    default:
                        requestType = "UpdateBankDetails";
                        docType = "Passbook";
                        break;
                }
                break;
            case "UpdateAddress":
                switch (docType) {
                    case "Electricity Bill":
                    case "Gas Bill":
                    case "Bank Account Statement":
                    case "Landline Bill":
                    case "Life Insurance Policy":
                    case "Registered Lease/Rent Agreement":
                        criteria = documentValidation.toString() +
                                "• Complete Address: The address should include house number, building, pin code, state, and city.\n";
                        outputJson.put("document_type", "");
                        outputJson.put("house_number", "");
                        outputJson.put("building", "");
                        outputJson.put("pin_code", "");
                        outputJson.put("state", "");
                        outputJson.put("city", "");
                        break;
                    default:
                        requestType = "UpdateAddress";
                        docType = "Electricity Bill";
                        break;
                }
                break;
            case "UpdateName":
                switch (docType) {
                    case "Passport":
                    case "Driving License":
                    case "Voter's ID card":
                    case "Pan Card":
                    case "Aadhaar Card":
                    case "NRGEA Job Card":
                        criteria = documentValidation.toString() +
                                "• Name: Check for the presence of the first name and last name.\n";
                        outputJson.put("document_type", "");
                        outputJson.put("first_name", "");
                        outputJson.put("last_name", "");
                        outputJson.put("sex", ""); // Assuming sex (gender) needs to be captured
                        break;
                    default:
                        requestType = "UpdateName";
                        docType = "Passport";
                        break;
                }
                break;
            default:
                requestType = "UpdateBankDetails";
                docType = "Passport";
                break;
        }

        String prompt = "You are an image classification and OCR expert. " +
                "I am providing an Image or a PDF file. Please analyse or classify the image to determine if this is a valid document of type: " + docType + ". " +
                "Please refer to the following validation criteria to decide if it is a valid document or not:\n" + criteria
                + "Finally  \"output_format\": \"json\"\n" +" Result in JSON format as per given outputJsonFormat in 5-20 words, keep NA for blank or null value.  outputJsonFormat: "+ outputJson.toString(4);

        return prompt;
    }


    public static void main(String[] args) {
        String requestType = "Example Request Type";
        String docType = "Example Document Type";
        String prompt = generateLLMPrompt(requestType, docType);
        System.out.println(prompt);
    }


}
