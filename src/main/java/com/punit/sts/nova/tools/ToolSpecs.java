package com.punit.sts.nova.tools;

import com.punit.sts.nova.event.PromptStartEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.punit.sts.nova.event.PromptStartEvent.ToolSchema;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Constants for tool specifications.
 */
public class ToolSpecs {
//    public static final Map<String,String> DEFAULT_TOOL_SPEC;
//    static {
//        Map<String, String> defaultToolSpec = new HashMap<>();
//        try {
//            defaultToolSpec.put("json",
//                    new ObjectMapper().writeValueAsString(PromptStartEvent.ToolSchema.builder()
//                            .type("object")
//                            .properties(Collections.emptyMap())
//                            .required(Collections.emptyList())
//                            .build()));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to serialize default tool schema!", e);
//        }
//        DEFAULT_TOOL_SPEC = Collections.unmodifiableMap(defaultToolSpec);
//    }
//}

    //            DEFAULT_TOOL_SPEC = Collections.unmodifiableMap(defaultToolSpec);

    public static final Map<String, String> KB_TOOL_SPEC;
    public static final Map<String, String> DEFAULT_TOOL_SPEC;
    public static final Map<String, String> STOCK_VALUE_TOOL_SPEC;
    public static final Map<String, String> PAYMENT_TRACKING_TOOL_SPEC;

    static {
        ObjectMapper mapper = new ObjectMapper();

        // knowledgeBase schema
        KB_TOOL_SPEC = new HashMap<>();
        try {
            KB_TOOL_SPEC.put("json", mapper.writeValueAsString(
                    ToolSchema.builder()
                            .type("object")
                            .properties(Map.of(
                                    "query", Map.of(
                                            "type", "string",
                                            "description", "Search the company knowledge base for past transaction history or information on key financial metrics of Paytm and Mobikwik"
                                    )
                            ))
                            .required(List.of("query"))
                            .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize knowledge base schema", e);
        }

        // getDateAndTimeTool schema
        DEFAULT_TOOL_SPEC = new HashMap<>();
        try {
            DEFAULT_TOOL_SPEC.put("json", mapper.writeValueAsString(
                    ToolSchema.builder()
                            .type("object")
                            .properties(Collections.emptyMap())
                            .required(Collections.emptyList())
                            .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize default schema", e);
        }

        // getstockvaluetool schema
        STOCK_VALUE_TOOL_SPEC = new HashMap<>();
        try {
            STOCK_VALUE_TOOL_SPEC.put("json", mapper.writeValueAsString(
                    ToolSchema.builder()
                            .type("object")
                            .properties(Map.of(
                                    "companyName", Map.of(
                                            "type", "string",
                                            "description", "The name of company to find stock value for"
                                    ),
                                    "requestNotifications", Map.of(
                                            "type", "boolean",
                                            "description", "Whether to set up notifications for this payment",
                                            "default", false
                                    )
                            ))
                            .required(List.of("companyName"))
                            .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize stock value schema", e);
        }

        // trackPaymentTool schema
        PAYMENT_TRACKING_TOOL_SPEC = new HashMap<>();
        try {
            PAYMENT_TRACKING_TOOL_SPEC.put("json", mapper.writeValueAsString(
                    ToolSchema.builder()
                            .type("object")
                            .properties(Map.of(
                                    "paymentId", Map.of(
                                            "type", "string",
                                            "description", "The UPI payment number or ID to track"
                                    ),
                                    "requestNotifications", Map.of(
                                            "type", "boolean",
                                            "description", "Whether to set up notifications for this payment",
                                            "default", false
                                    )
                            ))
                            .required(List.of("paymentId"))
                            .build()
            ));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payment tracking schema", e);
        }
    }
}
