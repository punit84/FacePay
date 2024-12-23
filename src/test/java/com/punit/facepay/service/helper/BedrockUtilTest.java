package com.punit.facepay.service.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClientBuilder;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.core.SdkBytes;


@ExtendWith(MockitoExtension.class)
public class BedrockUtilTest {

    @Mock
    private BedrockRuntimeClient bedrockRuntimeClient;

    @InjectMocks
    private BedrockUtil bedrockUtil;

    private final BedrockUtil bedrockUtil_2 = new BedrockUtil();

    @Mock
    private JsonUtil jsonUtil;

    @Mock
    private BedrockRuntimeClient mockClient;

    @Test
    public void testGetFileExtensionWithDotOnly() {
        /**
         * Tests the behavior of getFileExtension when given a filename that ends with a dot.
         * Expected: An empty string should be returned.
         */
        String result = bedrockUtil_2.getFileExtension("filename.");
        assertEquals("", result, "getFileExtension should return an empty string for a filename ending with a dot");
    }

    @Test
    public void testGetFileExtensionWithEmptyInput() {
        /**
         * Tests the behavior of getFileExtension when given an empty string input.
         * Expected: An empty string should be returned.
         */
        String result = bedrockUtil_2.getFileExtension("");
        assertEquals("", result, "getFileExtension should return an empty string for empty input");
    }

    @Test
    public void testGetFileExtensionWithHiddenFile() {
        /**
         * Tests the behavior of getFileExtension when given a hidden file (starting with a dot).
         * Expected: The correct extension should be returned.
         */
        String result = bedrockUtil_2.getFileExtension(".hidden.txt");
        assertEquals("txt", result, "getFileExtension should return the correct extension for a hidden file");
    }

    /**
     * Test case for getFileExtension method when the file name has multiple dots
     */
    @Test
    public void testGetFileExtensionWithMultipleDots() {
        BedrockUtil bedrockUtil = new BedrockUtil();
        String fileName = "example.config.json";
        String result = bedrockUtil.getFileExtension(fileName);
        assertEquals("json", result, "File extension should be 'json'");
    }

    @Test
    public void testGetFileExtensionWithMultipleDots_2() {
        /**
         * Tests the behavior of getFileExtension when given a filename with multiple dots.
         * Expected: Only the last extension should be returned.
         */
        String result = bedrockUtil_2.getFileExtension("file.name.with.dots.txt");
        assertEquals("txt", result, "getFileExtension should return only the last extension for a filename with multiple dots");
    }

    @Test
    public void testGetFileExtensionWithNoExtension() {
        /**
         * Tests the behavior of getFileExtension when given a filename without an extension.
         * Expected: An empty string should be returned.
         */
        String result = bedrockUtil_2.getFileExtension("filename");
        assertEquals("", result, "getFileExtension should return an empty string for a filename without extension");
    }

    @Test
    public void testGetFileExtensionWithNullInput() {
        /**
         * Tests the behavior of getFileExtension when given a null input.
         * Expected: An empty string should be returned.
         */
        String result = bedrockUtil_2.getFileExtension(null);
        assertEquals("", result, "getFileExtension should return an empty string for null input");
    }

    /**
     * Test getFileExtension method with null or empty filename
     */
    @Test
    public void testGetFileExtensionWithNullOrEmptyFilename() {
        BedrockUtil bedrockUtil = new BedrockUtil();

        // Test with null filename
        assertEquals("", bedrockUtil.getFileExtension(null), "Should return empty string for null filename");

        // Test with empty filename
        assertEquals("", bedrockUtil.getFileExtension(""), "Should return empty string for empty filename");
    }

    /**
     * Test case for getFileExtension method when the file name contains an extension
     */
    @Test
    public void testGetFileExtensionWithValidExtension() {
        BedrockUtil bedrockUtil = new BedrockUtil();
        String fileName = "example.txt";
        String result = bedrockUtil.getFileExtension(fileName);
        assertEquals("txt", result, "File extension should be 'txt'");
    }

    /**
     * Test case for getFileExtension method when the file name doesn't contain an extension
     */
    @Test
    public void testGetFileExtensionWithoutExtension() {
        BedrockUtil bedrockUtil = new BedrockUtil();
        String fileName = "examplefile";
        String result = bedrockUtil.getFileExtension(fileName);
        assertEquals("", result, "File extension should be an empty string");
    }

    /**
     * Test case for invokeAnthropic method with a non-image file
     * Verifies that the method processes a document correctly and returns the expected JSON response
     */


    /**
     * Test invokeAnthropic method for image processing scenario
     */


    /**
     * Test case for invokeAnthropic method with an image file
     * Verifies the method's behavior when processing an image file and receiving a response without JSON content
     */


    @Test
    public void testInvokeAnthropic_EmptyInput() {
        byte[] emptyFileBytes = new byte[0];
        String emptyPrompt = "";
        String emptyFileName = "";
        String modelId = "test-model";

        Exception exception = assertThrows(Exception.class, () -> {
            bedrockUtil.invokeAnthropic(emptyFileBytes, emptyPrompt, emptyFileName, modelId);
        });

//        assertTrue(exception.getMessage().contains("Invalid input"));
    }

    //@Test
    public void testInvokeAnthropic_InvalidModelId() {
        byte[] fileBytes = "test content".getBytes();
        String prompt = "test prompt";
        String fileName = "test.txt";
        String invalidModelId = "";

        Exception exception = assertThrows(Exception.class, () -> {
            bedrockUtil.invokeAnthropic(fileBytes, prompt, fileName, invalidModelId);
        });

        assertTrue(exception.getMessage().contains("Invalid model ID"));
    }


  //  @Test
    public void testInvokeAnthropic_UnsupportedFileType() {
        byte[] fileBytes = "test content".getBytes();
        String prompt = "test prompt";
        String fileName = "test.unsupported";
        String modelId = "test-model";

        Exception exception = assertThrows(Exception.class, () -> {
            bedrockUtil.invokeAnthropic(fileBytes, prompt, fileName, modelId);
        });

        assertTrue(exception.getMessage().contains("Unsupported file type"));
    }

    /**
     * Test that invokeClaude method successfully generates a response from the Claude model
     */


    //@Test
    public void testInvokeClaude_LongInput() {
        String longInput = "a".repeat(1000001); // More than 1 million characters
        assertThrows(IllegalArgumentException.class, () -> BedrockUtil.invokeClaude(longInput),
                "Should throw IllegalArgumentException for input longer than 1 million characters");
    }

    //@Test
    public void testInvokeClaude_NullInput() {
        assertThrows(NullPointerException.class, () -> BedrockUtil.invokeClaude(null),
                "Should throw NullPointerException for null input");
    }


    //@Test
    public void testInvokeModelLama3_NullInput() {
        assertThrows(NullPointerException.class, () -> BedrockUtil.InvokeModelLama3(null),
                "Null input should throw NullPointerException");
    }

    /**
     * Test case for InvokeModelLama3 method
     * Verifies that the method correctly processes the user message and returns the expected response
     */


    //@Test
    public void testMainWithCredentialsProviderException() {
        try (MockedStatic<ProfileCredentialsProvider> mockedProvider = Mockito.mockStatic(ProfileCredentialsProvider.class)) {
            mockedProvider.when(ProfileCredentialsProvider::create).thenThrow(new RuntimeException("Credentials error"));

            assertThrows(RuntimeException.class, () -> BedrockUtil.main(new String[]{}));
        }
    }

    @Test
    public void testMainWithEmptyArgs() {
        String[] args = {};
        assertDoesNotThrow(() -> BedrockUtil.main(args));
    }


    @Test
    public void testMainWithNoOutput() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        BedrockUtil.main(new String[]{});

        assertEquals("", outContent.toString().trim());
        System.setOut(System.out);
    }


    /**
     * Test invokeAnthropic method for image processing scenario
     */
     public void testInvokeAnthropicWithImage() throws Exception {
        // Arrange
        byte[] fileBytes = "test image content".getBytes();
        String prompt = "Describe this image";
        String fileName = "test.jpg";
        String modelId = "anthropic.claude-3-sonnet-20240229-v1:0";

        // Mock BedrockRuntimeClient response
        JSONObject mockResponseBody = new JSONObject()
                .put("content", new JSONArray()
                        .put(new JSONObject()
                                .put("text", "{\"description\": \"A test image\"}")))
                .put("usage", new JSONObject()
                        .put("input_tokens", 100)
                        .put("output_tokens", 50));

        InvokeModelResponse mockResponse = InvokeModelResponse.builder()
                .body(SdkBytes.fromUtf8String(mockResponseBody.toString()))
                .build();

        when(bedrockRuntimeClient.invokeModel((InvokeModelRequest) any())).thenReturn(mockResponse);

        // Mock JsonUtil
        when(jsonUtil.mergeJsonObjects(any(), any(), any())).thenReturn(new JSONObject()
                .put("description", "A test image")
                .put("input_tokens", 100)
                .put("output_tokens", 50)
                .put("Cost", "0.10 rupees"));

        // Act
        JSONObject result = bedrockUtil.invokeAnthropic(fileBytes, prompt, fileName, modelId);

        // Assert
        assertNotNull(result);
        assertEquals("A test image", result.getString("description"));
        assertEquals(100, result.getInt("input_tokens"));
        assertEquals(50, result.getInt("output_tokens"));
        assertEquals("0.10 rupees", result.getString("Cost"));
    }

    /**
     * Test case for invokeAnthropic method with an image file
     * Verifies the method's behavior when processing an image file and receiving a response without JSON content
     */
    @Test
    public void testInvokeAnthropicWithImage_2() throws Exception {
        // Arrange
        byte[] fileBytes = "test image content".getBytes();
        String prompt = "Describe this image";
        String fileName = "test.jpg";
        String modelId = "anthropic.claude-3-sonnet-20240229-v1:0";

        JSONObject mockResponse = new JSONObject()
                .put("content", new JSONArray()
                        .put(new JSONObject()
                                .put("text", "This is a description of the image.")))
                .put("usage", new JSONObject()
                        .put("input_tokens", 100)
                        .put("output_tokens", 50));

        InvokeModelResponse mockInvokeModelResponse = InvokeModelResponse.builder()
                .body(SdkBytes.fromUtf8String(mockResponse.toString()))
                .build();

    }

}