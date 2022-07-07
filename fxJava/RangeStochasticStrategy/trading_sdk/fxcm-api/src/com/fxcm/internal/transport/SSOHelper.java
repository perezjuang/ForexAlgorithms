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

package com.fxcm.internal.transport;

import java.net.URLEncoder;
import java.util.Properties;
import java.util.Vector;

import com.fxcm.messaging.util.HostReader;
import com.fxcm.messaging.util.IConnectionManager;
import com.fxcm.messaging.util.web.HttpParameter;

public class SSOHelper extends HostReader {

	public SSOHelper(Properties aProperties) {
		setProxyParameters(                
				aProperties.getProperty(IConnectionManager.PROXY_SERVER),
                (int) getLongProperty(aProperties, IConnectionManager.PROXY_PORT, 80),
                aProperties.getProperty(IConnectionManager.PROXY_UID),
                aProperties.getProperty(IConnectionManager.PROXY_PWD),
                getBooleanProperty(aProperties, IConnectionManager.PROXY_HTTP11, false)
		);
	}

	//@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	public String retrieveHostsXML(String aHostDescriptorURL, String aTerminal, 
			String aSAMLAssertion, String aStationName) {
		
        Vector params = new Vector();
        params.add(new HttpParameter("ID", String.valueOf(System.currentTimeMillis())));
        params.add(new HttpParameter("MV", "4"));
        params.add(new HttpParameter("AT", "SAML"));
        params.add(new HttpParameter("DATA", URLEncoder.encode(aSAMLAssertion)));
        params.add(new HttpParameter("PN", aTerminal));
        params.add(new HttpParameter("SN", aStationName != null ? aStationName : ""));

        return getHttpContent(aHostDescriptorURL, params, true);
	}

    protected long getLongProperty(Properties aProperties, String aName, long aDefault) {
        long ret = aDefault;
        String res = aProperties.getProperty(aName);
        if (res != null && res.length() > 0) {
            try {
                ret = Long.parseLong(res);
            } catch (Exception e) {
                //swallow
            }
        }
        return ret;
    }

    protected boolean getBooleanProperty(Properties aProperties, String aName, boolean aDefault) {
        boolean ret = aDefault;
        String res = aProperties.getProperty(aName);
        if (res != null && res.length() > 0) {
            ret = "true".equals(res);
        }
        return ret;
    }
}
