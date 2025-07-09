    package com.punit.sts.nova.tools;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.databind.node.ArrayNode;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
    import software.amazon.awssdk.services.bedrockagentruntime.model.*;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
    import software.amazon.awssdk.regions.Region;
    import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
    import software.amazon.awssdk.services.bedrockagentruntime.model.BedrockAgentRuntimeException;
       import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveAndGenerateResponse;
    import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveRequest;
    import software.amazon.awssdk.services.bedrockagentruntime.model.RetrieveResponse;

    public class KnowledgeBaseService {

        private static final String KB_ID = "DKT5KZUBWK";
        private static final String MODEL_ARN = "anthropic.claude-3-haiku-20240307-v1:0"; // Replace with correct ARN if needed
        private static final Region REGION = Region.US_EAST_1;

        private static final BedrockAgentRuntimeClient bedrockClient = BedrockAgentRuntimeClient.builder()
                .region(REGION)
                .build();

        public static List<String> retrieveKB(String query) {
            if (query == null || query.isBlank()) {
                throw new IllegalArgumentException("Query cannot be null or empty");
            }
            ObjectMapper requestMapper = new ObjectMapper();

            List<String> resultsKB = new ArrayList<>();
            try {

                // Initialize the knowledgebase configuration
                KnowledgeBaseVectorSearchConfiguration knowledgeBaseVectorSearchConfiguration = KnowledgeBaseVectorSearchConfiguration.builder()
                        .numberOfResults(1)
                        .build();
                KnowledgeBaseRetrievalConfiguration knowledgeBaseRetrievalConfiguration = KnowledgeBaseRetrievalConfiguration.builder()
                        .vectorSearchConfiguration(knowledgeBaseVectorSearchConfiguration)
                        .build();

                // Initializing the bedrock
                try (BedrockAgentRuntimeClient bedrockAgentRuntimeClient = BedrockAgentRuntimeClient.builder()
                        .region(REGION)
                        .build()) {
                    // Form the request for bedrock knowledgebase
                    KnowledgeBaseQuery knowledgeBaseQuery = KnowledgeBaseQuery.builder()
                            .text(query)
                            .build();
                    RetrieveRequest retrieveRequest = RetrieveRequest.builder()
                            .knowledgeBaseId(KB_ID)
                            .retrievalQuery(knowledgeBaseQuery)
                            .retrievalConfiguration(knowledgeBaseRetrievalConfiguration)
                            .build();

                    // Invoke the bedrock knowledgebase
                    RetrieveResponse retrieveResponse = bedrockAgentRuntimeClient.retrieve(retrieveRequest);

                    List<String> results = new ArrayList<>();
                    // Extract the bedrock results and return the results
                    try {
                        if (retrieveResponse.hasRetrievalResults()) {
                            ArrayNode responseNode = requestMapper.createArrayNode();
                            for (KnowledgeBaseRetrievalResult result : retrieveResponse.retrievalResults()) {
                                String text = result.content().text();
                                if (text != null && !text.isBlank()) {
                                    resultsKB.add(text);
                                }
                            }
                        }
                    } catch (Exception e) {

                    }
                }

            } catch (BedrockAgentRuntimeException e) {
                throw new RuntimeException("Error while retrieving from knowledge base: " + e.getMessage(), e);
            }

            return resultsKB;
        }


    }
