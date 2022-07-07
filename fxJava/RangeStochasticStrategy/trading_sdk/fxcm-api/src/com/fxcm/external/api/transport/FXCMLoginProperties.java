/*
 * Copyright (c) 2004 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 *
 * Author: Andre Mermegas
 * Date: Dec 15, 2004
 * Time: 1:33:13 PM
 *
 * 06/06/2005 Elana added  private long msgFlags and setter and getter
 * 06/13/2005 Miron symplified with usage of properties file
 * 07/07/2005   Andre Mermegas: added another constructor that doesnt require configfile
 * 08/21/2006   Andre Mermegas: use commons logger
 * 08/05/2010   Andre Mermegas: add servicename, for possible connection to priceserver as well.
 */
package com.fxcm.external.api.transport;

import com.fxcm.util.logging.Utils;
import org.apache.commons.logging.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * minimal login properties are in the consructor, additional optional properties
 * are available via setters.
 */
public class FXCMLoginProperties {
    private String mConfigFile;
    private final Log mLogger;
    private String mPassword;
    private Properties mProperties;
    private String mServer;
    private String mServiceName;
    private String mTerminal;
    private String mUserName;

    /**
     * These are the minimal requirements for a succesful connection, other
     * properties of this class are optional.
     *
     * @param aUserName like "user"
     * @param aPassword like "pass"
     * @param aTerminal like "demo"
     * @param aServer   like "server.fxcm.com:7777"
     */
    public FXCMLoginProperties(String aUserName, String aPassword, String aTerminal, String aServer) {
        mServer = aServer;
        mPassword = aPassword;
        mTerminal = aTerminal;
        mUserName = aUserName;
        mProperties = new Properties();
        mLogger = Utils.getLog(this);
    }

    /**
     * These are the minimal requirements for a succesful connection plus the path to a
     * config file with other properties.
     *
     * @param aUserName   like "user"
     * @param aPassword   like "pass"
     * @param aTerminal   like "demo"
     * @param aServer     like "server.fxcm.com:7777"
     * @param aConfigFile path to configfile cfg\test.cfg
     */
    public FXCMLoginProperties(String aUserName, String aPassword, String aTerminal, String aServer, String aConfigFile) {
        mServer = aServer;
        mPassword = aPassword;
        mTerminal = aTerminal;
        mUserName = aUserName;
        mConfigFile = aConfigFile;
        mProperties = new Properties();
        mLogger = Utils.getLog(this);
        try {
            if (mConfigFile != null) {
                addProperty("quickfix.cfg", mConfigFile);
                FileInputStream inStream = null;
                try {
                    inStream = new FileInputStream(mConfigFile);
                    mProperties.load(inStream);
                } catch (IOException e) {
                    mLogger.error(this, e);
                } finally {
                    if (inStream != null) {
                        inStream.close();
                    }
                }
            }
        } catch (IOException e) {
            mLogger.error(this, e);
        }
    }

    public void addProperty(Object aKey, Object aValue) {
        mProperties.put(aKey, aValue);
    }

    /**
     * Password associated with this properties class
     *
     * @return password
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Properties associated with this properties class
     *
     * @return properties
     *
     * @throws IOException
     */
    public Properties getProperties() throws IOException {
        return mProperties;
    }

    /**
     * Set the properties associated with this login connection
     *
     * @param aProperties
     */
    public void setProperties(Properties aProperties) {
        mProperties = aProperties;
    }

    /**
     * Server name registered in this properties class
     *
     * @return server
     */
    public String getServer() {
        return mServer;
    }

    public String getServiceName() {
        return mServiceName;
    }

    public void setServiceName(String aServiceName) {
        mServiceName = aServiceName;
    }

    /**
     * Terminal registered in this properties class
     *
     * @return terminal
     */
    public String getTerminal() {
        return mTerminal;
    }

    /**
     * Username registered in this properties class
     *
     * @return username
     */
    public String getUserName() {
        return mUserName;
    }

    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("FXCMLoginProperties");
        sb.append("{mConfigFile='").append(mConfigFile).append('\'');
        sb.append(", mPassword='").append(mPassword).append('\'');
        sb.append(", mProperties=").append(mProperties);
        sb.append(", mServer='").append(mServer).append('\'');
        sb.append(", mTerminal='").append(mTerminal).append('\'');
        sb.append(", mUserName='").append(mUserName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
