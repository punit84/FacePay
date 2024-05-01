package com.punit.facepay.service.helper;

import org.springframework.stereotype.Component;

import com.punit.facepay.service.Configs;

@Component
public class UPILinkUtil {


	public static String getUrl(String faceid, int type) {
		String prefix="upi";
		switch (type) {
		case Configs.DEVICE_ANDROID: {
			prefix="upi";
			break;
		}
		case Configs.DEVICE_IOS: {

			prefix="Paytm";
			break;

		}

		default:
			prefix="upi";
			break;
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
