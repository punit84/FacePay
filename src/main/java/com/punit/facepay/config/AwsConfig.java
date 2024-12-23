package com.punit.facepay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class AwsConfig {
    @Bean
    public BedrockRuntimeClient bedrockClient() {
      return   BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();
    }
    
}
