package com.punit.AWSPe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AWSPeApplication {
	
	final static Logger logger= LoggerFactory.getLogger(AWSPeApplication.class);


    public static void main(String[] args) {
    	logger.info("Starting application FacePayApplication ********** ");
        SpringApplication.run(AWSPeApplication.class, args);
    }
}
