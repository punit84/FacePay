package com.punit.AWSPe;

import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${aws.region}")
    private String awsRegion;
    
    @Value("${aws.client.api-call-timeout:30}")
    private int apiCallTimeout;
    
    @Value("${aws.client.api-call-attempt-timeout:20}")
    private int apiCallAttemptTimeout;
    
    @Value("${aws.client.retry-count:3}")
    private int retryCount;
   
    @Bean
    public BedrockRuntimeClient bedrockClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(awsRegion))
                .overrideConfiguration(getDefaultClientConfig())
                .build();
    }
    
    @Bean
    public TranscribeStreamingAsyncClient transcribeStreamingClient() {
        return TranscribeStreamingAsyncClient.builder()
                .region(Region.of(awsRegion))
                .overrideConfiguration(getDefaultClientConfig())
                .build();
    }
    
    @Bean
    public PollyClient pollyClient() {
        return PollyClient.builder()
                .region(Region.of(awsRegion))
                .overrideConfiguration(getDefaultClientConfig())
                .build();
    }

    private ClientOverrideConfiguration getDefaultClientConfig() {
        return ClientOverrideConfiguration.builder()
                .apiCallTimeout(Duration.ofSeconds(apiCallTimeout))
                .apiCallAttemptTimeout(Duration.ofSeconds(apiCallAttemptTimeout))
                .retryPolicy(RetryPolicy.builder()
                        .numRetries(retryCount)
                        .build())
                .build();
    }
}
