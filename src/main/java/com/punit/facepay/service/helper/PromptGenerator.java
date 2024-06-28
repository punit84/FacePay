package com.punit.facepay.service.helper;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PromptGenerator {

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

            // Iterate through fields of rootNode
            rootNode.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode node = entry.getValue();
                if (node.isBoolean()) {
                    if (node.asBoolean()) {
                        ++trueCount[0];
                    }
                } else {
                    cleanedJson.set(fieldName, node);
                }
            });

            // Validate account number, account name, and IFSC code
            if (rootNode.has("account_number") && !rootNode.get("account_number").asText().matches("\\d+")) {
                System.out.println("Invalid account number");
                ++falseCount;
            }
            if (rootNode.has("account_name") && rootNode.get("account_name").asText().isEmpty()) {
                System.out.println("Account name is empty");
                ++falseCount;

            }
            if (rootNode.has("ifsc_code") && rootNode.get("ifsc_code").asText().isEmpty()) {
                System.out.println("IFSC code is empty");
                ++falseCount;

            }
            System.out.println("true count " +trueCount[0]);
            System.out.println("false count " +falseCount);

            // Add a node indicating the number of true values if there are more than 3
            if (trueCount[0] > 3 && falseCount ==0) {
                cleanedJson.put("Valid Document", Boolean.TRUE);
            }else{
                cleanedJson.put("Valid Document", Boolean.FALSE);

            }

            System.out.println("Number of true values: " + trueCount);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cleanedJson;
    }
    public static String generateLLMPrompt(String requestType, String docType) {
        System.out.println(" request type is  " +requestType );
        System.out.println(" docType  is  " +docType );

        if (requestType.contains(" ")){
            requestType="UpdateBankDetails";
            System.out.println(" request type is  " +requestType );

        }
        if (docType.contains(" ")) {
            docType="cheque";
            System.out.println(" docType  is  " +docType );

        }

        String criteria = "";
        String documentValidation = "";
        Map<String, String> outputFields = new HashMap<>();

        StringBuilder chequeValidation = new StringBuilder();

        // Common evaluation criteria
        chequeValidation.append("Evaluation Criteria:\n");
        chequeValidation.append("• Document Valid: The image should clearly represent a bank cheque,contains the text 'valid for 3 month' and the keyword 'bank and also having details such as bank name, account number, account name, and IFSC code '.\n");

// Additional criteria for bank cheque
        chequeValidation.append("• Valid for 3 Months: Check if the cheque contains the text 'valid for 3 months only'.\n");
        chequeValidation.append("• Keyword 'Bank': Verify if the cheque image contains the keyword 'bank'.\n");
        chequeValidation.append("• Keyword 'Payble at Par': Verify if the cheque image contains the keyword 'Payble at Par'.\n");
        chequeValidation.append("• Keyword 'Rupees': Verify if the cheque image contains the keyword 'Rupees'.\n");


        switch (requestType) {
            case "UpdateBankDetails":
                switch (docType) {
                    case "cheque":
                        criteria = chequeValidation.toString() +"Account Number (or A/C No.): Verify if the image includes an account number or abbreviation such as 'A/C No.'\\n" +
                                "• Account Name: Check for the presence of the account holder's name.\\n" +
                                "• IFSC Code: Confirm that the image displays the IFSC code.\\n";
                        documentValidation = chequeValidation.toString();
                        outputFields.put("valid document", docType);
                        outputFields.put("document_type", docType);
                        outputFields.put("account_number", "");
                        outputFields.put("account_name", "");
                        outputFields.put("ifsc_code", "");
                        break;

                    case "Passbook":
                    case "Bank Statement":
                        criteria = "• Account Number (or A/C No.): Verify if the image includes an account number or abbreviation such as 'A/C No.'\\n" +
                                "• Account Name: Check for the presence of the account holder's name.\\n" +
                                "• IFSC Code: Confirm that the image displays the IFSC code.\\n";
                        documentValidation = "• Document Type Appropriateness: Strict Check if document is of type " + docType + ".\\n response document valid document flag should be true if given document is of doctype given else it should be false";
                        outputFields.put("valid document", docType);
                        outputFields.put("document_type", docType);
                        outputFields.put("account_number", "");
                        outputFields.put("account_name", "");
                        outputFields.put("ifsc_code", "");
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid document type for UpdateBankDetails: " + docType);
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
                        criteria = "• Complete Address: The address should include house number, building, pin code, state, and city.\\n";
                        documentValidation = "• Document Type Appropriateness: The document type should be a valid " + docType + ".\\n";
                        outputFields.put("valid document", docType);
                        outputFields.put("document_type", docType);
                        outputFields.put("house_number", "");
                        outputFields.put("building", "");
                        outputFields.put("pin_code", "");
                        outputFields.put("state", "");
                        outputFields.put("city", "");
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid document type for UpdateAddress: " + docType);
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
                        criteria = "• Name: Check for the presence of the first name and last name.\\n";
                        documentValidation = "• Document Type Appropriateness: The document type should be a valid " + docType + ".\\n";
                        outputFields.put("valid document", docType);
                        outputFields.put("document_type", docType);
                        outputFields.put("first_name", "");
                        outputFields.put("last_name", "");
                        outputFields.put("sex", ""); // Assuming sex (gender) needs to be captured
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid document type for UpdateName: " + docType);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown request type: " + requestType);
        }

        String prompt = "{\n" +
                "  \"prompt\": \"Evaluation Criteria:\\n" +
                "    • Text Clarity: The text should be sharp and readable without needing zoom.\\n" +
                "    • Image Resolution: The resolution should be high enough that zooming in does not significantly pixelate the text.\\n" +
                "    • Document Completeness: All required information should be visible in the image without any parts being cut off. The document must be fully visible, with all edges (especially for cheques) intact and not cropped.\\n" +
                "    • Overall Cleanliness: The document should be clean and free from major stains, marks, or obstructions.\\n" +
                documentValidation +
                "    Result in JSON format as per given outputFields:\\n" +
                "    • Valid document: If all the above criteria are met and the document type matches the selected document type (" + docType + "), the document quality is deemed good.\\n" +
                "    • Invalid document: If any of the criteria are not met or the document type does not match the selected document type (" + docType + "), specify which criteria failed and why.\\n" +
                "    Selected Request Type: " + requestType + "\\n" +
                "    Selected Document Type: " + docType + "\\n" +
                criteria +
                "  \",\n" +
                "  \"output_format\": \"json\"\n" +"only outputField should be in reponse json each in 5-20 words"+
                "}";

        return prompt;
    }

    public static void main(String[] args) {
        String requestType = "UpdateBankDetails";
        String docType = "Checkbook";
        String llmPrompt = generateLLMPrompt(requestType, docType);
        System.out.println(llmPrompt);
    }

}
