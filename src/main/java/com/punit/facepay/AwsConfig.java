package com.punit.facepay;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

@Configuration
public class AwsConfig {
   
    
    public BedrockRuntimeClient bedrockClient() {
      return   BedrockRuntimeClient.builder()
                .region(Region.AP_SOUTH_1)
                .build();

    	
    }
    
}
