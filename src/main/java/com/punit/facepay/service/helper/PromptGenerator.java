package com.punit.facepay.service.helper;


import org.springframework.stereotype.Component;

@Component
public class PromptGenerator {

    public static String generateLLMPrompt(String requestType, String docType) {
        String criteria = "";
        if ("UpdateBankDetails".equals(requestType)) {
            criteria = "• Account Number (or A/C No.): Verify if the image includes an account number or abbreviation such as 'A/C No.'\\n" +
                    "• Account Name: Check for the presence of the account holder's name.\\n" +
                    "• IFSC Code: Confirm that the image displays the IFSC code.\\n";
        } else if ("UpdateAddress".equals(requestType)) {
            criteria = "• Complete Address: The address should include house number, building, pin code, state, and city.\\n";
        } else if ("UpdateName".equals(requestType)) {
            criteria = "• Name: Check for the presence of the first name and last name.\\n" +
                    "• Sex: Confirm the presence of sex (gender).\\n";
        }

        String prompt = "{\n" +
                "  \"prompt\": \"Evaluation Criteria:\\n\" +\n" +
                "    \"• Text Clarity: The text should be sharp and readable without needing zoom.\\n\" +\n" +
                "    \"• Image Resolution: The resolution should be high enough that zooming in does not significantly pixelate the text.\\n\" +\n" +
                "    \"• Document Completeness: All required information should be visible in the image without any parts being cut off. The document must be fully visible, with all edges (especially for cheques) intact and not cropped.\\n\" +\n" +
                "    \"• Overall Cleanliness: The document should be clean and free from major stains, marks, or obstructions.\\n\" +\n" +
                "    \"• Document Type Appropriateness: The document type should be consistent with the expected format for containing banking details.\\n\" +\n" +
                "    \"Result in JSON format:\\n\" +\n" +
                "    \"• Valid document: If all the above criteria are met and the document type matches the selected document type, the document quality is deemed good.\\n\" +\n" +
                "    \"• Invalid document: If any of the criteria are not met or the document type does not match the selected document type, specify which criteria failed and why.\\n\" +\n" +
                "    \"Selected Request Type: " + requestType + "\\n\" +\n" +
                "    \"Selected Document Type: " + docType + "\\n\" +\n" +
                "    \"" + criteria + "\" +\n" +
                "  \"output_format\": \"json\"\n" +
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
