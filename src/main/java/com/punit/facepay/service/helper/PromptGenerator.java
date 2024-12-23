package com.punit.facepay.service.helper;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PromptGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PromptGenerator.class);

    public static String generateLLMPrompt(String requestType, String docType) {
        logger.info("Generating LLM prompt for requestType: {} and docType: {}", requestType, docType);

        // Normalize request type and document type
        RequestConfig config = normalizeRequestConfig(requestType, docType);
        requestType = config.requestType;
        docType = config.docType;

        // Build validation criteria
        StringBuilder criteria = buildBaseCriteria(docType);
        JSONObject outputSchema = buildBaseOutputSchema(docType);

        // Add specific validation criteria and output schema based on request type
        switch (requestType) {
            case "UpdateBankDetails":
                addBankDetailsValidation(criteria, outputSchema, docType);
                break;
            case "UpdateAddress":
                addAddressValidation(criteria, outputSchema, docType);
                break;
            case "UpdateName":
                addNameValidation(criteria, outputSchema, docType);
                break;
            default:
                throw new IllegalArgumentException("Unsupported request type: " + requestType);
        }

        // Build enhanced prompt
        return buildEnhancedPrompt(docType, criteria.toString(), outputSchema.toString());
    }

    private static RequestConfig normalizeRequestConfig(String requestType, String docType) {
        if (requestType.contains(" ")) {
            logger.warn("Request type contains spaces, normalizing to default values");
            return new RequestConfig("UpdateBankDetails", "cheque");
        }
        return new RequestConfig(requestType, docType);
    }

    private static StringBuilder buildBaseCriteria(String docType) {
        StringBuilder criteria = new StringBuilder();
        criteria.append("Valid ").append(docType).append(" Document criteria:\n")
               .append("1. Image Quality Requirements:\n")
               .append("   • Text Clarity: Text must be sharp, clear, and readable without zooming\n")
               .append("   • Resolution: Minimum 300 DPI recommended, no significant pixelation when zoomed\n")
               .append("   • Lighting: Even lighting without glare or shadows\n")
               .append("   • Focus: Document must be in sharp focus throughout\n\n")
               .append("2. Document Orientation and Layout:\n")
               .append("   • Orientation: Document may be rotated; analyze text orientation and process accordingly\n")
               .append("   • Layout Recognition: Identify and extract information regardless of rotation angle\n")
               .append("   • Text Direction: Account for text flow direction when extracting information\n\n")
               .append("3. Document Integrity:\n")
               .append("   • Completeness: All required information must be fully visible\n")
               .append("   • No Obstruction: Free from fingers, watermarks, or other objects covering text\n")
               .append("   • Physical Condition: No tears, major creases, or damage affecting readability\n")
               .append("   • Cleanliness: Free from significant stains, marks, or smudges\n\n")
               .append("4. Authentication Elements:\n")
               .append("   • Official Markings: Required stamps, seals, or watermarks must be visible\n")
               .append("   • Document Format: Must match standard government/institutional format\n\n");
        return criteria;
    }

    private static JSONObject buildBaseOutputSchema(String docType) {
        JSONObject schema = new JSONObject();
        schema.put(docType.toLowerCase(), "true/false")
              .put("valid_document", "true/false")
              .put("document_type", "")
              .put("invalid_document_reason", "")
              .put("confidence_score", "0.0 to 1.0")
              .put("validation_timestamp", "ISO-8601 timestamp");
        return schema;
    }

    private static void addBankDetailsValidation(StringBuilder criteria, JSONObject schema, String docType) {
        criteria.append("4. Banking Document Specific Requirements:\n")
               .append("   • Bank Identifiers: Clear bank name, branch details, and logo\n")
               .append("   • Account Details: Complete and legible account number and IFSC code\n")
               .append("   • Security Features: Visible security patterns, microprint (if applicable)\n")
               .append("   • Document Currency: Must be dated within last 3 months\n");

        schema.put("account_number", "")
              .put("account_name", "")
              .put("ifsc_code", "")
              .put("bank_name", "")
              .put("branch_name", "")
              .put("document_date", "");
    }

    private static void addAddressValidation(StringBuilder criteria, JSONObject schema, String docType) {
        criteria.append("4. Address Document Specific Requirements:\n")
               .append("   • Address Completeness: Must include all address components\n")
               .append("   • Proof Currency: Document must be dated within last 3 months\n")
               .append("   • Issuer Details: Clear identification of issuing authority\n");

        schema.put("address_components", new JSONObject()
                .put("house_number", "")
                .put("building", "")
                .put("street", "")
                .put("locality", "")
                .put("city", "")
                .put("state", "")
                .put("pin_code", ""))
              .put("issuer_details", "")
              .put("issue_date", "");
    }

    private static void addNameValidation(StringBuilder criteria, JSONObject schema, String docType) {
        criteria.append("4. Identity Document Specific Requirements:\n")
               .append("   • Name Format: Full name in standard format\n")
               .append("   • Photo Quality: Clear, recent photograph (if applicable)\n")
               .append("   • ID Numbers: All identification numbers clearly visible\n");

        schema.put("personal_details", new JSONObject()
                .put("first_name", "")
                .put("middle_name", "")
                .put("last_name", "")
                .put("date_of_birth", "")
                .put("gender", ""))
              .put("id_number", "")
              .put("issue_date", "")
              .put("expiry_date", "");
    }

    private static String buildEnhancedPrompt(String docType, String criteria, String outputSchema) {
        return String.format(
            "You are an advanced document analysis and verification expert specialized in OCR and image classification. " +
            "Task: Analyze the provided image/PDF to verify if it is a valid %s and extract required information.\n\n" +
            "Validation Criteria:\n%s\n" +
            "Instructions:\n" +
            "1. First determine the document orientation and rotate mentally if needed\n" +
            "2. Analyze the document thoroughly against all provided criteria\n" +
            "3. Extract all relevant information as per the output schema, accounting for any rotation\n" +
            "4. Provide confidence scores for key extracted fields\n" +
            "5. Include detailed reasoning for any validation failures\n" +
            "6. If document is rotated, mention the approximate rotation angle in the response\n\n" +
            "Required Output Schema:\n%s\n" +
            "Note: Ensure all responses are in valid JSON format. Include 'valid_document' as false if ANY criteria fails.",
            docType, criteria, outputSchema);
    }

    private static class RequestConfig {
        final String requestType;
        final String docType;

        RequestConfig(String requestType, String docType) {
            this.requestType = requestType;
            this.docType = docType;
        }
    }
}