package com.punit.AWSPe;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.transcribestreaming.TranscribeStreamingAsyncClient;
import software.amazon.awssdk.services.polly.PollyClient;

import java.time.Duration;

@Configuration
public class AwsConfig {
   
    @Bean
    public BedrockRuntimeClient bedrockClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .overrideConfiguration(getDefaultClientConfig())
                .build();
    }
    
    @Bean
    public TranscribeStreamingAsyncClient transcribeStreamingClient() {
        return TranscribeStreamingAsyncClient.builder()
                .region(Region.AP_SOUTH_1)
                .overrideConfiguration(getDefaultClientConfig())
                .build();
    }
    
    @Bean
    public PollyClient pollyClient() {
        return PollyClient.builder()
                .region(Region.AP_SOUTH_1)
                .overrideConfiguration(getDefaultClientConfig())
                .build();
    }

    private ClientOverrideConfiguration getDefaultClientConfig() {
        return ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(30))
                .apiCallAttemptTimeout(Duration.ofSeconds(20))
                .retryPolicy(RetryPolicy.builder()
                        .numRetries(3)
                        .build())
                .build();
    }
}
