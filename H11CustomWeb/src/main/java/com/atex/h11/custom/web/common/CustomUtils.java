package com.atex.h11.custom.web.common;

import java.io.UnsupportedEncodingException;

public class CustomUtils {

	public static String getHexBytes(String s, String encoding) throws UnsupportedEncodingException {
		byte[] bytes = s.getBytes(encoding);
		String bytesStr = "";
	    for (int i = 0; i < bytes.length; i++) {
	    	if (i > 0) { bytesStr += " "; }
	    	bytesStr += Integer.toHexString(bytes[i]);
	    }			
	    return bytesStr;
	}
	
}
