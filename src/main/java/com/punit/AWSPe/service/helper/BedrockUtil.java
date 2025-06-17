package com.punit.AWSPe.service.helper;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.punit.AWSPe.service.Configs;

import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

@Component
public class BedrockUtil {

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private PromptGenerator promptGenerator;
    BedrockRuntimeClient client;

    final static Logger logger = LoggerFactory.getLogger(BedrockUtil.class);

    public BedrockUtil() {
        this.client = BedrockRuntimeClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(ProfileCredentialsProvider.create())
                .build();

        // Step 1: Create the Amazon Bedrock runtime client
        // The runtime client handles the communication with AI models on Amazon Bedrock
//        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .region(Region.US_EAST_1)
//                .build();
    }
//    public BedrockUtil() {
//        this.client =  BedrockRuntimeClient.builder()
//                .region(Configs.REGION)
//                .credentialsProvider(ProfileCredentialsProvider.create())
//    }


    public static String InvokeModelLama3(String userMessage) {


        // Create a Bedrock Runtime client in the AWS Region of your choice.
        var client = BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();

        // Set the model ID, e.g., Llama 3 8B Instruct.
        var modelId = "meta.llama3-70b-instruct-v1:0";

        // Embed the message in Llama 3's prompt format.
        var prompt = MessageFormat.format("""
                <|begin_of_text|>
                <|start_header_id|>user<|end_header_id|>
                {0}
                <|eot_id|>
                <|start_header_id|>assistant<|end_header_id|>
                """, userMessage);

        // Create a JSON payload using the model's native structure.
        var request = new JSONObject()
                .put("prompt", prompt)
                // Optional inference parameters:
                .put("max_gen_len", 2000)
                .put("temperature", 1F)
                .put("top_p", 0.9F);

        // Encode and send the request.
        var response = client.invokeModel(req -> req
                .body(SdkBytes.fromUtf8String(request.toString()))
                .modelId(modelId));

        // Decode the native response body.
        var nativeResponse = new JSONObject(response.body().asUtf8String());

        // Extract and print the response text.
        var responseText = nativeResponse.getString("generation");
        logger.info(responseText);
        return responseText;

    }


    public String converse(String prompt) {

        // Step 2: Specify which model to use
        // Available Amazon Nova models and their characteristics:
        // - Amazon Nova Micro: Text-only model optimized for lowest latency and cost
        // - Amazon Nova Lite:  Fast, low-cost multimodal model for image, video, and text
        // - Amazon Nova Pro:   Advanced multimodal model balancing accuracy, speed, and cost
        //
        // For the latest available models, see:
        // https://docs.aws.amazon.com/bedrock/latest/userguide/models-supported.html
        String modelId = "amazon.nova-lite-v1:0";

        // Step 3: Create the message
        // The message includes the text prompt and specifies that it comes from the user
        var message = Message.builder()
                .content(ContentBlock.fromText(prompt))
                .role(ConversationRole.USER)
                .build();

        // Step 4: Configure the request
        // Optional parameters to control the model's response:
        // - maxTokens: maximum number of tokens to generate
        // - temperature: randomness (max: 1.0, default: 0.7)
        //   OR
        // - topP: diversity of word choice (max: 1.0, default: 0.9)
        // Note: Use either temperature OR topP, but not both
        ConverseRequest request = ConverseRequest.builder()
                .modelId(modelId)
                .messages(message)
                .inferenceConfig(config -> config
                                .maxTokens(50)     // The maximum response length
                                .temperature(0.5F)  // Using temperature for randomness control
                        //.topP(0.9F)       // Alternative: use topP instead of temperature
                ).build();

        // Step 5: Send and process the request
        // - Send the request to the model
        // - Extract and return the generated text from the response
        try {
            ConverseResponse response = client.converse(request);
            return response.output().message().content().get(0).text();

        } catch (SdkClientException e) {
            System.err.printf("ERROR: Can't invoke '%s'. Reason: %s", modelId, e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public String invokeNovaPro(String prompt) {

        if (client == null){
             client = BedrockRuntimeClient.builder()
                    .region(Region.AP_SOUTH_1)
                    .build();
        }
        // Set the model ID, e.g., Llama 3 8B Instruct.
        String novaModel = "amazon.nova-pro-v1:0";


        System.out.println(prompt.toString());
        InvokeModelRequest request = InvokeModelRequest.builder()
                .modelId(novaModel) // Use amazon.nova-chat or amazon.nova-pro depending on your access
                .contentType("application/json")
                .accept("application/json")
                .body(SdkBytes.fromUtf8String(prompt))
                .build();


        System.out.println(request.toString());

        InvokeModelResponse response = client.invokeModel(request);
        System.out.println(response.body().asUtf8String());


        return response.body().asUtf8String();

    }


    /**
     * Invokes the Anthropic Claude 3 model to run an inference based on the
     * provided input.
     *
     * @param prompt The prompt for Claude to complete.
     * @return The generated response.
     */
    public static String invokeClaude(String prompt) {
        /*
         * The different model providers have individual request and response formats.
         * For the format, ranges, and default values for Anthropic Claude, refer to:
         * https://docs.aws.amazon.com/bedrock/latest/userguide/model-parameters-claude.html
         */

        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();


        logger.info("prompt is " + prompt);

        //String claudeModelId = "anthropic.claude-3-sonnet-20240229-v1:0";

        String claudeModelId = "meta.llama3-8b-instruct-v1:0";

        // Claude requires you to enclose the prompt as follows:
        String enclosedPrompt = "Human: " + prompt + "\n\nAssistant:";

        String payload = new JSONObject()
                .put("prompt", enclosedPrompt)
                .put("max_tokens_to_sample", 4096)
                .put("temperature", 0F)
                .put("stop_sequences", List.of("\n\nHuman:"))
                .toString();

        InvokeModelRequest request = InvokeModelRequest.builder()
                .body(SdkBytes.fromUtf8String(payload))
                .modelId(claudeModelId)
                .contentType("application/json")
                .accept("application/json")
                .build();

        InvokeModelResponse response = client.invokeModel(request);

        JSONObject responseBody = new JSONObject(response.body().asUtf8String());

        String generatedText = responseBody.getString("completion");

        return generatedText;
    }


    public JSONObject invokeAnthropic(byte[] fileBytes, String prompt, String fileName , String modelId) throws Exception {
        // Create a Bedrock Runtime client in the AWS Region of your choice.

        String mediaTypeMime = getMediaTypeFromExtensionMIME(getFileExtension(fileName));

        String mediaTypeString = getMediaTypeFromExtension(getFileExtension(fileName));
        logger.info("file name is " + fileName);
        logger.info("file extension is " + mediaTypeMime);

        Set<String> supportedFileTypes = new HashSet<>(Arrays.asList("doc", "docx", "pdf", "gif", "jpeg", "png"));
        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();
        String base64Image = null;
        JSONObject request = null;
        if ("image".equals(mediaTypeString)) {
            String imageBase64 = Base64.getEncoder().encodeToString(fileBytes);
            // Create the JSON payload
            request = new JSONObject()
                    .put("anthropic_version", "bedrock-2023-05-31")
                    .put("max_tokens", 4000)
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("type", mediaTypeString)
                                                    .put("source", new JSONObject()
                                                            .put("type", "base64")
                                                            .put("media_type", mediaTypeMime)
                                                            .put("data", imageBase64)))
                                            .put(new JSONObject()
                                                    .put("type", "text")
                                                    .put("text", prompt)))));
            jsonUtil.printJsonbyMasking(request.toString());
        } else {

            // Construct the document JSON object
            JSONObject documentObject = new JSONObject()
                    .put("name", fileName)
                    .put("format", "txt")
                    .put("source", new JSONObject()
                            .put("bytes", fileBytes));

            request = new JSONObject()
                    .put("anthropic_version", "bedrock-2023-05-31")
                    .put("max_tokens", 40000)
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", new JSONArray()
                                            .put(new JSONObject()
                                                    .put("type", "text")
                                                    .put("document", documentObject))
                                            .put(new JSONObject()
                                                    .put("type", "text")
                                                    .put("text", prompt)
                                            ))));

            //logger.info(request.toString());
        }

        final String requestString = request.toString();
        // Encode and send the request.
        var response = client.invokeModel(req -> req
                .body(SdkBytes.fromUtf8String(requestString))
                .modelId(modelId)
                .contentType("application/json")
                .accept("application/json"));

        logger.info(response.body().asUtf8String());
        // Decode the native response body.

        JSONObject nativeResponse = new JSONObject(response.body().asUtf8String());

        // Extract the content array
        String contentText = nativeResponse.getJSONArray("content").getJSONObject(0).getString("text");

        // logger.info("\ncontentText: " + contentText);

        // Find the start and end positions of the JSON content
        int startIndex = contentText.indexOf("{");
        int endIndex = contentText.lastIndexOf("}") + 1;
        logger.info("Start index " + startIndex + "\n end index " + endIndex);

        if (startIndex != -1 && endIndex != -1 && startIndex != 0) {
            // Extract the JSON conten
            contentText = contentText.substring(startIndex, endIndex);
            logger.info("Extracted JSON content:");
            logger.info("\n ContentText:\n " + contentText);
            logger.info(contentText);
        }


        JSONObject usageJson = nativeResponse.getJSONObject("usage");
        logger.info("\nusage: : \n" + usageJson.toString());

        JSONObject contentJson = new JSONObject(contentText);
        String cost = CostCalculater.calculateCostInINR(modelId, usageJson);

        contentJson = jsonUtil.mergeJsonObjects(contentJson, usageJson, cost);

        //String textJson = promptGenerator.processJson(contentJson.toString()).toString();
        logger.info(contentJson.toString());
        return contentJson;

    }

    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private String getMediaTypeFromExtensionMIME(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "pdf":
                return "application/pdf";
            // Add more cases for other file types as needed
            default:
                return "application/octet-stream";
        }
    }

    private String getMediaTypeFromExtension(String extension) {
        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return "image";
            case "png":
                return "image";
            case "pdf":
                return "document";
            // Add more cases for other file types as needed
            default:
                return "document";
        }
    }


}