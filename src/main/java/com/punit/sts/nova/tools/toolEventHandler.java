package com.punit.sts.nova.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.punit.sts.nova.AbstractNovaS2SEventHandler;
import com.punit.sts.nova.event.PromptStartEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * S2S Event Handler that is aware of the date and time via tools.
 */
public class toolEventHandler extends AbstractNovaS2SEventHandler {
    private static final Logger log = LoggerFactory.getLogger(toolEventHandler.class);
    private static final String TIMEZONE = System.getenv().getOrDefault("TZ", "Asia/Kolkata");

    @Override
    protected void handleToolInvocation(String toolUseId, String toolName, String content, Map<String, Object> output) {
        if (toolName == null) {
            log.warn("Received null toolName");
            //            return;
        } else {
            switch (toolName) {
                case "getDateTool": {
                    handleGetDateTool(output);
                    break;

                }
                case "getTimeTool": {
                    handleGetTimeTool(output);
                    break;
                }
                case "getDateAndTimeTool": {
                    handleGetDateTimeISO(output);
                    break;
                }
                case "trackPaymentTool": {
                    handleTrackPaymentTool(output);
                    break;
                }
                case "getstockvaluetool": {
                    handleGetStockValueTool(content, output);
                    break;
                }
                case "knowledgeBase": {
                    handleKnowledgeBaseTool(content, output);
                    break;
                }
                default: {
                    log.warn("Unhandled tool: {}", toolName);
                    output.put("error", "Tool not implemented in backend");

                }
            }
        }
    }

    @Override
    public PromptStartEvent.ToolConfiguration getToolConfiguration() {
        return PromptStartEvent.ToolConfiguration.builder()
                .tools(Arrays.asList(
                        PromptStartEvent.Tool.builder()
                                .toolSpec(PromptStartEvent.ToolSpec.builder()
                                        .name("getDateTool")
                                        .description("Get information about the current date")
                                        .inputSchema(ToolSpecs.DEFAULT_TOOL_SPEC)
                                        .build())
                                .build(),

                        PromptStartEvent.Tool.builder()
                                .toolSpec(PromptStartEvent.ToolSpec.builder()
                                        .name("getTimeTool")
                                        .description("Get information about the current time")
                                        .inputSchema(ToolSpecs.DEFAULT_TOOL_SPEC)
                                        .build())
                                .build(),

                        PromptStartEvent.Tool.builder()
                                .toolSpec(PromptStartEvent.ToolSpec.builder()
                                        .name("getDateAndTimeTool")
                                        .description("Get current date and time in IST")
                                        .inputSchema(ToolSpecs.DEFAULT_TOOL_SPEC)
                                        .build())
                                .build(),

                        PromptStartEvent.Tool.builder()
                                .toolSpec(PromptStartEvent.ToolSpec.builder()
                                        .name("getstockvaluetool")
                                        .description("Fetch current stock price of a company")
                                        .inputSchema(ToolSpecs.STOCK_VALUE_TOOL_SPEC)
                                        .build())
                                .build(),

                        PromptStartEvent.Tool.builder()
                                .toolSpec(PromptStartEvent.ToolSpec.builder()
                                        .name("trackPaymentTool")
                                        .description("Track status of a UPI or digital payment by ID")
                                        .inputSchema(ToolSpecs.PAYMENT_TRACKING_TOOL_SPEC)
                                        .build())
                                .build(),

                        PromptStartEvent.Tool.builder()
                                .toolSpec(PromptStartEvent.ToolSpec.builder()
                                        .name("knowledgeBase")
                                        .description("Ask questions related to financial or business data from the knowledge base")
                                        .inputSchema(ToolSpecs.KB_TOOL_SPEC)
                                        .build())
                                .build()
                ))
                .build();
    }

    /**
     * Handles a request to get the time.
     * @param contentNode The content node to write the response to.
     */
    private static void handleGetTimeTool(Map<String, Object> contentNode) {
        ZonedDateTime localTime = ZonedDateTime.now(ZoneId.of(TIMEZONE));
        contentNode.put("timezone", TIMEZONE);
        contentNode.put("formattedTime", localTime.format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    /**
     * Handles a request to get the date.
     * @param contentNode The content node to write the response to.
     */
    private static void handleGetDateTool(Map<String, Object> contentNode) {
        LocalDate currentDate = LocalDate.now(ZoneId.of(TIMEZONE));
        contentNode.put("date", currentDate.format(DateTimeFormatter.ISO_DATE));
        contentNode.put("year", currentDate.getYear());
        contentNode.put("month", currentDate.getMonthValue());
        contentNode.put("day", currentDate.getDayOfMonth());
        contentNode.put("dayOfWeek", currentDate.getDayOfWeek().toString());
        contentNode.put("timezone", TIMEZONE);
    }

    private void handleGetDateAndTimeTool(Map<String, Object> output) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(TIMEZONE));
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
        output.put("date", formattedDate.split(" ")[0]);
        output.put("time", formattedDate.split(" ")[1]);
        output.put("timezone", formattedDate.split(" ")[2]);
    }

    private void handleGetDateTimeISO(Map<String, Object> output) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(TIMEZONE));
        String isoDateTime = now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        output.put("datetime", isoDateTime);
    }

    private void handleTrackPaymentTool(Map<String, Object> output) {
        output.put("status", "Payment ID processed successfully.");
        output.put("estimatedCompletion", LocalDate.now().plusDays(1).toString());
    }

    private void handleGetStockValueTool(String stock, Map<String, Object> output) {
        String resp= performCurl(stock);
        output.put("company_name", stock);
        output.put("stock_value", resp);
    }

    private void handleKnowledgeBaseTool(String query, Map<String, Object> output) {

        List<String> results =  KnowledgeBaseService.retrieveKB(query);
        System.out.println("knowledgebase resutls " +results);
        output.put("results ", results);

    }

    private String performCurl(String stock) {
        String companyName = "apple";
        log.info("Performing curl for stock: {}", stock);

        try {
            // Create an ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Read the JSON string into a JsonNode
            JsonNode rootNode = objectMapper.readTree(stock);

            // Get the value of the "companyName" field
            JsonNode companyNameNode = rootNode.get("companyName");

            // Check if the node exists and is not null, then get its text value
            if (companyNameNode != null) {
                companyName = companyNameNode.asText();
            }

            System.out.println("Company Name: " + companyName); // Output: Company Name: Amazon

        } catch (Exception e) {
            e.printStackTrace();
        }

       String urlprefix= "https://awspe.com/api/price?stock=" +companyName.trim();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlprefix))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            return response.body();
        } catch (Exception e) {
            log.error("Curl failed for URL: {}", urlprefix, e);
            return "{}";
        }
    }


    public static void main(String[] args) {
        toolEventHandler  handler = new toolEventHandler();
        handler.performCurl("amazon");

    }
}
