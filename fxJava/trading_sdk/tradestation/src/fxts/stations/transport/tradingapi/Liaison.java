/*
 * Copyright 2006 FXCM LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $History: $
 * 9/5/2003 created by USHIK
 * 4/27/2004 USHIK changed getReportURL method considering flexibility url template from User Preference
  * 12/1/2004   Andre fixed a bug where the tradedesk was holding onto its old entries after logging out,
  * now on logout we ask the tradedesk to clear its tables, also replaced the string concat with a stringbuffer
  * when generating the reportURL
 */
package fxts.stations.transport.tradingapi;

import fxts.stations.trader.TradeApp;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.util.UserPreferences;

import javax.swing.SwingUtilities;
import java.util.StringTokenizer;

/**
 * Class Liaison.<br>
 * <br>
 * The class Liaison extends ALiaisonImpl class and refines its methods.
 * The only singleton of Liaison class might be created.
 * The singleton of Liaison class can be accessed by static method getInstance().
 * It is responsible for:
 * <ul>
 * <li>creating login dialog;</li>
 * <li>doing the connection and storing the its parameters;</li>
 * <li>sending requests and receiving responses;</li>
 * <li>doing disconnection.</li>
 * </ul>
 * <br>
 * Creation date (9/5/2003 10:15 AM)
 */
public class Liaison extends BaseLiaison implements IApplication {
    /**
     * A singleton of this class
     */
    private static final Liaison INST = new Liaison();

    /**
     * Private constructor. Calls the constructor of superclass
     */
    private Liaison() {
        TradingServerSession.getInstance().setApplication(this);
    }

    public void communicationBroken() {
        TradingServerSession.getInstance().relogin();
    }

    public void communicationConnecting() {
        if (getSessionID() != null) {
            setStatus(LiaisonStatus.RECONNECTING);
        }
    }

    public void communicationDisconnecting() {
    }

    public void communicationError() {
    }

    public void communicationEstablished() {
        if (getSessionID() != null) {
            setStatus(LiaisonStatus.READY);
        }
    }

    /**
     * Does disconnection
     */
    @Override
    public void disconnect() {
        mDisconnectRequeried = true;
        stop();
        dispatchLogoutCompleted();
        if (getSessionID() != null) {
            setSessionID(null);
        }
        TradingServerSession.getInstance().logout();
        mDisconnectRequeried = false;
        setStatus(LiaisonStatus.DISCONNECTED);
        mDisconnectedThread = null;
    }

    /**
     * @return client IP address
     */
    public String getClientIP() {
        try {
            return getLocalHost().getHostAddress();
        } catch (LiaisonException ex) {
            onCriticalError(ex);
        }
        return null;
    }

    public String getHostUrl(String aUserID) throws LiaisonException {
        String host = System.getProperty("Server.url");
        if (host == null) {
            host = UserPreferences.getUserPreferences(aUserID).getString("Server.url");
        }
        if (host == null) {
            throw new LiaisonException(null, "IDS_SERVER_ISNT_SPECIFIED");
        }
        return host.trim();
    }

    /**
     * @return Initializes cInst property at once, returns cInst always
     */
    public static Liaison getInstance() {
        return INST;
    }

    private String getMacroValue(String aMacroName, String aAccountID, String aFrom, String aTo) {
        String sRC = aMacroName;
        if (aMacroName.startsWith("IDS_")) {
            try {
                sRC = TradeApp.getInst().getResourceManager().getString(aMacroName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("serverURL".equalsIgnoreCase(aMacroName)) {
            sRC = TradingServerSession.getInstance().getParameterValue("REPORTS_URL");
        } else if ("accID".equalsIgnoreCase(aMacroName)) {
            sRC = aAccountID;
        } else if ("fromDate".equalsIgnoreCase(aMacroName)) {
            sRC = aFrom;
        } else if ("toDate".equalsIgnoreCase(aMacroName)) {
            sRC = aTo;
        } else if ("SESSIONID".equalsIgnoreCase(aMacroName)) {
            sRC = getSessionID();
        } else if ("CONNECTIONNAME".equalsIgnoreCase(aMacroName)) {
            sRC = getTradeDesk().getDatabaseName();
        } else if ("language".equalsIgnoreCase(aMacroName)) {
            try {
                sRC = TradeApp.getInst().getResourceManager().getLocale().getLanguage();
            } catch (Exception e) {
                e.printStackTrace();
                sRC = "en";
            }
        } else {
            UserPreferences up = UserPreferences.getUserPreferences(getTradeDesk().getUserName());
            String value = up.getString(aMacroName);
            if (value != null) {
                sRC = value;
            }
        }
        return sRC;
    }

    public String getProxyHost(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getString("Proxy.host");
    }

    public int getProxyPort(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getInt("Proxy.port");
    }

    public String getProxyPwd(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getString("Proxy.password");
    }

    public String getProxyUser(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getString("Proxy.user");
    }

    public String getReportURL(String aAccountID, String aFrom, String aTo) {
        UserPreferences up = UserPreferences.getUserPreferences(getTradeDesk().getUserName());
        String sReportFormat = up.getString("report.format");
        StringTokenizer st = new StringTokenizer(sReportFormat, "%", false);
        int i = sReportFormat.startsWith("%") ? 1 : 0;
        boolean bEndWithMacro = sReportFormat.endsWith("%");
        StringBuffer reportBuffer = new StringBuffer();
        for (; st.hasMoreTokens(); i++) {
            String token = st.nextToken();
            if (i % 2 == 0) {
                reportBuffer.append(token);
            } else {
                if (bEndWithMacro || st.hasMoreTokens()) {
                    reportBuffer.append(getMacroValue(token, aAccountID, aFrom, aTo));
                } else {
                    reportBuffer.append("%").append(token);
                }
            }
        }
        mLogger.debug("report url = " + reportBuffer);
        return reportBuffer.toString();
    }

    public String getServerConfigFile(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getString("Server.cfg").trim();
    }

    public int getServerTcpTimeout(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getInt("Server.tcp-timeout");
    }

    public boolean isProxyUsed(String aUserID) {
        return UserPreferences.getUserPreferences(aUserID).getBoolean("Proxy.use");
    }

    /**
     * Starts Reader, Ping and Sender threads
     */
    @Override
    public void login(LoginRequest aLoginRequest) {
        if (mLoginRequest == null) {
            mDisconnectRequeried = false;
            setStatus(LiaisonStatus.CONNECTING);
            mLoginRequest = aLoginRequest;
            new Thread() {
                @Override
                public void run() {
                    try {
                        mLoginRequest.doIt();
                        Liaison.this.getTradeDesk().setUserName(mLoginRequest.getUID());
                        Liaison.this.getTradeDesk().setConnectionName(mLoginRequest.getConnectionName());
                        Liaison.this.setSessionID(mLoginRequest.getSessionID());
                        Liaison.this.dispatchLoginCompleted();
                        Liaison.this.start();
                        Liaison.this.setStatus(LiaisonStatus.READY);
                    } catch (LiaisonException ex) {
                        setStatus(LiaisonStatus.DISCONNECTED);
                        mLoginRequest = null;
                        onCriticalError(ex);
                    }
                }
            }.start();
        }
    }

    /**
     * Starts Disconnect thread
     */
    @Override
    public void logout() {
        //clean up tradesk tables on logout.
        getTradeDesk().getRates().clear();
        getTradeDesk().clear();
        mDisconnectRequeried = true;
        if (getSessionID() != null) {
            setSessionID(null);
        }
        if (getStatus() == LiaisonStatus.DISCONNECTED || mDisconnectedThread != null) {
            return;
        }
        mDisconnectedThread = new DisconnectedThread();
        setStatus(LiaisonStatus.DISCONNECTING);
        mDisconnectedThread.start();
    }

    @Override
    public void refresh() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                getTradeDesk().clear();
                TradingServerSession.getInstance().clearOrderMap();
                try {
                    TradingServerSession.getInstance().getUserObjects();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
