package com.punit.AWSPe.service.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UPILinkUtil {

	final static Logger logger= LoggerFactory.getLogger(UPILinkUtil.class);

	
	public static void main(String[] args) {
		UPILinkUtil.getID("upi://pay?pa=nick.jat007@okicici");
	}

	public static String getUrl(String faceid) {

		logger.info("Create url for faceid " + faceid);
		
		String responseSTR = faceid;
		if (faceid.contains("://") || faceid.contains("linkedin") || faceid.contains("insta") ) {
			return faceid;
		}else {
			if (faceid.contains("@") ) {
				responseSTR = "upi://pay?pa="+faceid;
			}else {
				logger.info("given face id is mobile no");
				responseSTR = "upi://pay?pa="+faceid+"@paytm";
			}
		}

		logger.info("UPI url is " + responseSTR);

		return responseSTR;
	}

	public static String getID(String faceid) {

		logger.info("Create url for faceid " + faceid);

		if (faceid.contains("?pa=") ) {
			String[] parts = faceid.split("\\?pa=");
			String upiId = parts[1];
			logger.info("id is "+upiId );
			return upiId;
		}else {
			return faceid;
		}

	}
}
