package com.net.business.util;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpUtils {
	
	public static String executeGet(String url) throws Exception {  
		StringBuffer res = new StringBuffer();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		
		client.executeMethod(method);
		if (method.getStatusCode() == HttpStatus.SC_OK) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),"UTF-8"));
			String line;
			while ((line = reader.readLine()) != null) {
				res.append(line);
			}
			reader.close();
		}
		return res.toString();
    }  
	
}
