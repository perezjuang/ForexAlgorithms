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

import com.fxcm.external.api.transport.FXCMLoginProperties;

public class SSOLoginProperties extends FXCMLoginProperties {
	private String mOpenToken;
	private String mSAMLAssertion;

	public SSOLoginProperties(String aSAMLAssertion, String aTerminal, String aServer, String aConfigFile) {
		super(null, null, aTerminal, aServer, aConfigFile);
		mSAMLAssertion = aSAMLAssertion;
	}

	public SSOLoginProperties(String aSAMLAssetion, String aTerminal, String aServer) {
		super(null, null, aTerminal, aServer);
		mSAMLAssertion = aSAMLAssetion;
	}

	/**
	 * @param aOpenToken the mOpenToken to set
	 */
	public void setOpenToken(String aOpenToken) {
		this.mOpenToken = aOpenToken;
	}

	/**
	 * @return the mOpenToken
	 */
	public String getOpenToken() {
		return mOpenToken;
	}

	public String getSAMLAssertion() {
		return mSAMLAssertion;
	}

}
