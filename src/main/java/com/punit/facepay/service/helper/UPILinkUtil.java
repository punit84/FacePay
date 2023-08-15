package com.punit.facepay.service.helper;

import org.springframework.stereotype.Component;

@Component
public class UPILinkUtil {


	public static enum DEVICE_TYPE{ANDROID,IPHONE, WEB};


	public static String getUrl(String faceid, DEVICE_TYPE type) {
		String prefix="upi";
		switch (type) {
		case ANDROID: {

			prefix="upi";
		}

		case IPHONE: {

			prefix="Paytm";
		}

		default:
			prefix="upi";

		}

		String responseSTR;
		if (faceid.contains("@")) {

			responseSTR =prefix+ "://pay?pa="+faceid+"&pn=PaytmUser&cu=INR";
		}else {
			responseSTR = prefix+"://pay?pa="+faceid+"@paytm&pn=PaytmUser&cu=INR";
		}
		return responseSTR;
	}
}
