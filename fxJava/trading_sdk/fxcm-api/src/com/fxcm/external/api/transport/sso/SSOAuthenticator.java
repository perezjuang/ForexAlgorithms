/*
 * Copyright (c) 2013 FXCM, LLC.
 * 55 Water Street, 50th Floor
 * New York, New York 10041
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 *
 * Author: vstrelnikov
 * Date: May 16, 2013
 * Time: 1:33:13 PM
 *
 * $History: $
 *  05/16/2013   vstrelnikov: initially created
 */

package com.fxcm.external.api.transport.sso;

import java.io.InputStream;

import java.util.Formatter;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.auth.UsernamePasswordCredentials;

import com.fxcm.external.api.transport.sso.SSOException;

public class SSOAuthenticator {
	
	public static final String WEB_SERVER_USER = "pf_sts_user";
	
	public static final String WEB_SERVER_PASSWORD = "-6VhwfsxyBx}2uy!";
	
	public static final String requestTemplate = 
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
			"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
				"<soap:Header>" + 
					"<wsa:Action xmlns:wsa=\"http://www.w3.org/2005/08/addressing\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"ie7kY43lgFs35j7jsP20vdKKfyuk\">" +
						"http://www.w3.org/2005/08/addressing" +
					"</wsa:Action>" +
					"<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" soap:mustUnderstand=\"1\">" +
					"</wsse:Security>" +
				"</soap:Header>" +
				"<soap:Body xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"iLT9G2Ta0k7w2gOzgnHTWz47GUWo\">" +
					"<wst:RequestSecurityToken xmlns:wst=\"http://docs.oasis-open.org/ws-sx/ws-trust/200512/\">" +
						"<wst:RequestType>http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue</wst:RequestType>" +
						"<wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\">" +
							"<wsa:EndpointReference xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">" +
								"<wsa:Address>${ENDPOINT}</wsa:Address>" +
							"</wsa:EndpointReference>" +
						"</wsp:AppliesTo>" +
						"<wst:OnBehalfOf>" +
							"<wsse:UsernameToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
								"<wsse:Username>${LOGIN}</wsse:Username>" +
								"<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">${PASSWORD}</wsse:Password>" +
							"</wsse:UsernameToken>" +
						"</wst:OnBehalfOf>" +
					"</wst:RequestSecurityToken>" +
				"</soap:Body>" +
		"</soap:Envelope>";
	
	public static String authenticate(String service, String endPoint, String login, String password) throws Exception {
        String requestData = requestTemplate.replace("${ENDPOINT}", endPoint);
        requestData = requestData.replace("${LOGIN}", login);
        requestData = requestData.replace("${PASSWORD}", password);
		
		HttpPost httpPost = new HttpPost(service);
		
        httpPost.setHeader("Content-Type", "text/xml");
        httpPost.setHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(WEB_SERVER_USER, WEB_SERVER_PASSWORD), "US-ASCII", false));
        httpPost.setEntity(new StringEntity(requestData));
        
        HttpClient httpClient = new DefaultHttpClient();
        //httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, new HttpHost("127.0.0.1", 8888));
        HttpResponse response = httpClient.execute(httpPost);
        
        if (response.getStatusLine().getStatusCode() != 200 && response.getStatusLine().getStatusCode() != 500) {
        	throw new SSOException(response.getStatusLine().getReasonPhrase());
        }
        
        InputStream is = response.getEntity().getContent();
        
		String responseData = convertStreamToString(is);
		
		return getEncryptedAssertion(responseData);
	}
	
	public static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}

	private static String getEncryptedAssertion(String msg) {
		final String ea = "saml:EncryptedAssertion";
		final String openEa = "<" + ea;
		final String closeEa = "</" + ea + ">";
		
		int startEa = msg.indexOf(openEa);
		if (startEa > 0) {
			int endEa = msg.indexOf(closeEa);
			if (endEa > 0) {
				return msg.substring(startEa, endEa + closeEa.length());
			}
		}
		
		return null;
	}
}
