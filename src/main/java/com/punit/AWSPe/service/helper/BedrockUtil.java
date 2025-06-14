package com.punit.AWSPe.service.helper;

import java.text.MessageFormat;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.punit.AWSPe.service.Configs;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Component
public class BedrockUtil {

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private PromptGenerator promptGenerator;

    final static Logger logger = LoggerFactory.getLogger(BedrockUtil.class);

    public static void main(String[] args) {

        BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                .region(Configs.REGION)
                .credentialsProvider(ProfileCredentialsProvider.create())

                .build();
        //		BedrockUtill.invokeClaude("provide 4-5 line summary of  given face detail and predict humand details \n"
        //				+ "[FaceDetail(BoundingBox=BoundingBox(Width=0.25544488, Height=0.3909185, Left=0.35042447, Top=0.19347797), AgeRange=AgeRange(Low=31, High=39), Smile=Smile(Value=false, Confidence=88.38633), Eyeglasses=Eyeglasses(Value=false, Confidence=99.916435), Sunglasses=Sunglasses(Value=false, Confidence=99.99473), Gender=Gender(Value=Male, Confidence=99.22574), Beard=Beard(Value=true, Confidence=98.61274), Mustache=Mustache(Value=true, Confidence=70.31538), EyesOpen=EyeOpen(Value=true, Confidence=98.19997), MouthOpen=MouthOpen(Value=false, Confidence=99.52749), Emotions=[Emotion(Type=CALM, Confidence=100.0), Emotion(Type=HAPPY, Confidence=0.0060796738), Emotion(Type=FEAR, Confidence=2.9206276E-4), Emotion(Type=DISGUSTED, Confidence=2.3245811E-4), Emotion(Type=SAD, Confidence=1.7881393E-4), Emotion(Type=SURPRISED, Confidence=4.4703484E-5), Emotion(Type=ANGRY, Confidence=1.1920929E-5), Emotion(Type=CONFUSED, Confidence=1.1920929E-5)], Landmarks=[Landmark(Type=eyeLeft, X=0.43098167, Y=0.33953437), Landmark(Type=eyeRight, X=0.5416262, Y=0.3586149), Landmark(Type=mouthLeft, X=0.4224637, Y=0.46836543), Landmark(Type=mouthRight, X=0.5148277, Y=0.4842749), Landmark(Type=nose, X=0.48030612, Y=0.41992703), Landmark(Type=leftEyeBrowLeft, X=0.39201882, Y=0.30190095), Landmark(Type=leftEyeBrowRight, X=0.46026522, Y=0.30661464), Landmark(Type=leftEyeBrowUp, X=0.42843854, Y=0.293875), Landmark(Type=rightEyeBrowLeft, X=0.5235607, Y=0.3175142), Landmark(Type=rightEyeBrowRight, X=0.58431613, Y=0.33481872), Landmark(Type=rightEyeBrowUp, X=0.55592674, Y=0.31576887), Landmark(Type=leftEyeLeft, X=0.41069523, Y=0.33515522), Landmark(Type=leftEyeRight, X=0.45245424, Y=0.34440225), Landmark(Type=leftEyeUp, X=0.43160564, Y=0.3331868), Landmark(Type=leftEyeDown, X=0.43048057, Y=0.34521276), Landmark(Type=rightEyeLeft, X=0.51924324, Y=0.35591522), Landmark(Type=rightEyeRight, X=0.56063044, Y=0.36091897), Landmark(Type=rightEyeUp, X=0.5426872, Y=0.35231575), Landmark(Type=rightEyeDown, X=0.5400754, Y=0.36405468), Landmark(Type=noseLeft, X=0.45483372, Y=0.42841393), Landmark(Type=noseRight, X=0.4957919, Y=0.43539867), Landmark(Type=mouthUp, X=0.47147122, Y=0.46236923), Landmark(Type=mouthDown, X=0.46552765, Y=0.5001743), Landmark(Type=leftPupil, X=0.43098167, Y=0.33953437), Landmark(Type=rightPupil, X=0.5416262, Y=0.3586149), Landmark(Type=upperJawlineLeft, X=0.35867757, Y=0.32551366), Landmark(Type=midJawlineLeft, X=0.36294374, Y=0.46749434), Landmark(Type=chinBottom, X=0.45471728, Y=0.5645863), Landmark(Type=midJawlineRight, X=0.5587331, Y=0.50070554), Landmark(Type=upperJawlineRight, X=0.5998669, Y=0.36667028)], Pose=Pose(Roll=7.7445073, Yaw=0.9672852, Pitch=1.5708907), Quality=ImageQuality(Brightness=87.34692, Sharpness=92.22801), Confidence=99.99985, FaceOccluded=FaceOccluded(Value=false, Confidence=99.93733), EyeDirection=EyeDirection(Yaw=-4.984548, Pitch=-11.698688, Confidence=99.988754))]");
        //

    }


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