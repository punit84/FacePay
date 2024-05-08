package com.punit.facepay.service.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UPILinkUtil {

	final static Logger logger= LoggerFactory.getLogger(UPILinkUtil.class);

	
	public static String getUrl(String faceid) {
		
		logger.info("Create url for faceid " + faceid);
		String responseSTR = faceid;
		if (faceid.contains("://") ) {
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
}
