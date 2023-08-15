package com.punit.facepay.service.helper;

import org.springframework.stereotype.Component;

@Component
public class UPILinkUtil {

	public static String getUrl(String faceid) {
		String responseSTR;
		if (faceid.contains("@")) {
			responseSTR = "Paytm://pay?pa="+faceid+"&pn=PaytmUser&cu=INR";
		}else {
			responseSTR = "Paytm://pay?pa="+faceid+"@paytm&pn=PaytmUser&cu=INR";
		}
		return responseSTR;
	}
}
