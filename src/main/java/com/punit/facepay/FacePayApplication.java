package com.punit.facepay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.punit.facepay.rest.FacePayRestController;

@SpringBootApplication
public class FacePayApplication {
	
	final static Logger logger= LoggerFactory.getLogger(FacePayApplication.class);


    public static void main(String[] args) {
    	logger.info("Starting application FacePayApplication ********** ");
        SpringApplication.run(FacePayApplication.class, args);
    }
}
