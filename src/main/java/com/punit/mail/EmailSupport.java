package com.punit.mail;


import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.punit.facepay.rest.FaceScanRestController;


public class EmailSupport {

	public static void main(String[] args) {
		FaceScanRestController fc= new FaceScanRestController(null);
fc.sendEmail(null);


		
		
		
	}
}