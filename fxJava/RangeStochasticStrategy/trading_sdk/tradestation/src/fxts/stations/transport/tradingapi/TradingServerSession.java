/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/TradingServerSession.java#8 $
 *
 * Copyright (c) 2007 FXCM, LLC. All
 * 32 Old Slip, New York, NY 10005 USA
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
 *  1/6/2004 - created by Ushik
 * ----work in progress---
 *  12/1/2004   Andre   originally this class was originally the UserSession,
 *  changes have been made to accomodate the new fxcm messaging system
 *  ripped out old tradingapi messaging code.
 * ---------------------------
 * 05/09/2006   Andre Mermegas: get initial offers
 * 07/05/2006   Andre Mermegas: fix for processing collateral reports for new accounts added while running
 * 12/07/2006   Andre Mermegas: added colinqack processor
 * 01/18/2006   Andre Mermegas: update to pin support
 * 08/04/2008   Andre Mermegas: add JTS party to orders
 * 02/25/2009   Andre Mermegas: add USE_ORIGIN_RATE to override dealer prices with trader prices
 */
package fxts.stations.transport.tradingapi;

import com.fxcm.GenericException;
import com.fxcm.entity.ICode;
import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.GatewayFactory;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.Parties;
import com.fxcm.fix.Party;
import com.fxcm.fix.SubscriptionRequestTypeFactory;
import com.fxcm.fix.TradingSecurity;
import com.fxcm.fix.other.BusinessMessageReject;
import com.fxcm.fix.posttrade.CollateralInquiryAck;
import com.fxcm.fix.posttrade.CollateralReport;
import com.fxcm.fix.posttrade.PositionReport;
import com.fxcm.fix.posttrade.RequestForPositionsAck;
import com.fxcm.fix.pretrade.EMail;
import com.fxcm.fix.pretrade.MarketDataRequest;
import com.fxcm.fix.pretrade.MarketDataRequestReject;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.fix.pretrade.Quote;
import com.fxcm.fix.pretrade.SecurityStatus;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.fix.trade.ExecutionReport;
import com.fxcm.fix.trade.OrderCancelReject;
import com.fxcm.fix.trade.OrderCancelReplaceRequest;
import com.fxcm.fix.trade.OrderCancelRequest;
import com.fxcm.fix.trade.OrderSingle;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;
import com.fxcm.messaging.IUserSession;
import com.fxcm.messaging.TradingSessionDesc;
import com.fxcm.messaging.util.AuthenticationException;
import com.fxcm.messaging.util.IConnectionManager;
import fxts.stations.core.FXCMConnection;
import fxts.stations.core.FXCMConnectionsManager;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Order;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.trader.ui.dialogs.ChangePasswordDialog;
import fxts.stations.trader.ui.dialogs.PINDialog;
import fxts.stations.trader.ui.dialogs.TradingSessionDialog;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.processors.BusinessMessageRejectProcessor;
import fxts.stations.transport.tradingapi.processors.CollateralInquiryAckProcessor;
import fxts.stations.transport.tradingapi.processors.CollateralReportProcessor;
import fxts.stations.transport.tradingapi.processors.EMailProcessor;
import fxts.stations.transport.tradingapi.processors.ExecutionReportProcessor;
import fxts.stations.transport.tradingapi.processors.IProcessor;
import fxts.stations.transport.tradingapi.processors.MarketDataSnapshotProcessor;
import fxts.stations.transport.tradingapi.processors.OrderCancelRejectProcessor;
import fxts.stations.transport.tradingapi.processors.PositionReportProcessor;
import fxts.stations.transport.tradingapi.processors.QuoteProcessor;
import fxts.stations.transport.tradingapi.processors.RequestForPositionsAckProcessor;
import fxts.stations.transport.tradingapi.processors.SecurityStatusProcessor;
import fxts.stations.transport.tradingapi.processors.TradingSessionStatusProcessor;
import fxts.stations.transport.tradingapi.requests.ChangePasswordRequest;
import fxts.stations.transport.tradingapi.resources.OraCodeFactory;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class TradingServerSession implements IGenericMessageListener, IStatusMessageListener {
    private static final TradingServerSession SINGLETON = new TradingServerSession();
    private static final IGateway GATEWAY = GatewayFactory.createGateway();
    private final Object mMUTEX = new Object();
    private IApplication mApplication;
    private String mCfgFile;
    private String mHostUrl;
    private Log mLogger = LogFactory.getLog(TradingServerSession.class);
    private String mPassword;
    private Map<ICode, IProcessor> mProcessorMap;
    private String mRequestID;
    private ResourceManager mResMan;
    private Map<String, Order> mStopLimitOrderMap;
    private String mTerminal;
    private TradingSessionStatus mTradingSessionStatus;
    private String mUsername;
    private boolean mLogout;

    private TradingServerSession() {
        try {
            mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }
        mProcessorMap = new HashMap<ICode, IProcessor>();
        mProcessorMap.put(BusinessMessageReject.OBJ_TYPE, new BusinessMessageRejectProcessor());
        mProcessorMap.put(TradingSessionStatus.OBJ_TYPE, new TradingSessionStatusProcessor());
        mProcessorMap.put(RequestForPositionsAck.OBJ_TYPE, new RequestForPositionsAckProcessor());
        mProcessorMap.put(CollateralReport.OBJ_TYPE, new CollateralReportProcessor());
        mProcessorMap.put(PositionReport.OBJ_TYPE, new PositionReportProcessor());
        mProcessorMap.put(ExecutionReport.OBJ_TYPE, new ExecutionReportProcessor());
        mProcessorMap.put(MarketDataSnapshot.OBJ_TYPE, new MarketDataSnapshotProcessor());
        mProcessorMap.put(EMail.OBJ_TYPE, new EMailProcessor());
        mProcessorMap.put(OrderCancelReject.OBJ_TYPE, new OrderCancelRejectProcessor());
        mProcessorMap.put(Quote.OBJ_TYPE, new QuoteProcessor());
        mProcessorMap.put(CollateralInquiryAck.OBJ_TYPE, new CollateralInquiryAckProcessor());
        mProcessorMap.put(SecurityStatus.OBJ_TYPE, new SecurityStatusProcessor());
        mProcessorMap.put(MarketDataRequestReject.OBJ_TYPE, new IProcessor() {
            public void process(ITransportable aTransportable) {
                mLogger.debug("MarketDataRequestReject = " + aTransportable);
                doneProcessing();
            }
        });
        mStopLimitOrderMap = new HashMap<String, Order>();
    }

    /**
     * Updates session finished flag
     * <br>&nbsp;
     */
    public void adjustStatus() {
        Liaison liaison = Liaison.getInstance();
        synchronized (liaison) {
            LiaisonStatus status = liaison.getStatus();
            if (status == LiaisonStatus.READY) {
                liaison.setStatus(LiaisonStatus.RECEIVING);
            } else if (status == LiaisonStatus.RECEIVING) {
                liaison.setStatus(LiaisonStatus.READY);
            }
        }
    }

    private void beginProcessing() {
        synchronized (mMUTEX) {
            try {
                mMUTEX.wait();
            } catch (InterruptedException e) {
                //
            }
        }
    }

    /**
     * Clear the orders map
     */
    public void clearOrderMap() {
        mStopLimitOrderMap.clear();
    }

    public void doneProcessing() {
        synchronized (mMUTEX) {
            mMUTEX.notifyAll();
        }
    }

    private void fillJTSParty(Parties aParties) {
        Party party = new Party("fxcm.com", "C", "13");
        party.setSubParty("4", "JavaTSApp");
        party.setSubParty("4444", "JavaTS");
        aParties.addParty(party);
    }

    public IApplication getApplication() {
        return mApplication;
    }

    /**
     * set the application instance
     *
     * @param aApplication application
     */
    public void setApplication(IApplication aApplication) {
        mApplication = aApplication;
    }

    public IGateway getGateway() {
        return GATEWAY;
    }

    /**
     * @return The host url that we are connected to
     */
    public String getHostUrl() {
        return mHostUrl;
    }

    /**
     * @return instance accessor for singleton
     */
    public static TradingServerSession getInstance() {
        return SINGLETON;
    }

    public String getParameterValue(String aName) {
        return mTradingSessionStatus.getParameterValue(aName);
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String aPassword) {
        mPassword = aPassword;
    }

    public String getRequestID() {
        return mRequestID;
    }

    public void setRequestID(String aRequestID) {
        mRequestID = aRequestID;
    }

    /**
     * Returns the current active session id
     *
     * @return sessionid
     */
    public String getSessionID() {
        return GATEWAY.getSessionID();
    }


    /**
     * Gets the UserKind
     *
     * @return
     *
     * @see com.fxcm.fix.IFixDefs.FXCM_ACCT_TYPE_CUSTOMER
     * @see com.fxcm.fix.IFixDefs.FXCM_ACCT_TYPE_DEALER
     * @see com.fxcm.fix.IFixDefs.FXCM_ACCT_TYPE_TRADER
     */
    public int getUserKind() {
        return GATEWAY.getUserKind();
    }

    public Map<String, Order> getStopLimitOrderMap() {
        return mStopLimitOrderMap;
    }

    public String getTerminal() {
        return mTerminal;
    }

    public void setTerminal(String aTerminal) {
        mTerminal = aTerminal;
    }

    /**
     * @return sessionstatus
     */
    public TradingSessionStatus getTradingSessionStatus() {
        return mTradingSessionStatus;
    }

    /**
     * @param aTradingSessionStatus sessionstatus
     */
    public void setTradingSessionStatus(TradingSessionStatus aTradingSessionStatus) {
        mTradingSessionStatus = aTradingSessionStatus;
    }

    public boolean isUnlimitedCcy() {
        String unlimited = mTradingSessionStatus.getParameterValue("UNLIMITED_CCY_SUBSCRIPTION");
        return !(unlimited == null || "N".equalsIgnoreCase(unlimited));
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String aUsername) {
        mUsername = aUsername;
    }

    /**
     * Get User Objects
     *
     * @throws Exception aex
     */
    public void getUserObjects() throws Exception {
        GATEWAY.requestTradingSessionStatus();
        beginProcessing();

        String value = mTradingSessionStatus.getParameterValue("FORCE_PASSWORD_CHANGE");
        if (value != null && "Y".equalsIgnoreCase(value)) {
            TradeApp tradeApp = TradeApp.getInst();
            String msg = "For your security, you are required to change your password.";
            String title = tradeApp.getResourceManager().getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(tradeApp.getMainFrame(), msg, title, JOptionPane.INFORMATION_MESSAGE);
            ChangePasswordDialog dialog = new ChangePasswordDialog(tradeApp.getMainFrame());
            if (dialog.showModal() == JOptionPane.OK_OPTION) {
                ChangePasswordRequest cpr = new ChangePasswordRequest();
                cpr.setOldPassword(dialog.getOldPassword());
                cpr.setNewPassword(dialog.getNewPassword());
                cpr.setConfirmNewPassword(dialog.getConfirmNewPassword());
                cpr.doIt();
            } else {
                throw new GenericException("");
            }
        }

        Enumeration<TradingSecurity> securities = (Enumeration<TradingSecurity>) mTradingSessionStatus.getSecurities();
        MarketDataRequest mdr = new MarketDataRequest();
        mdr.setSubscriptionRequestType(SubscriptionRequestTypeFactory.SUBSCRIBE);
        mdr.setMDEntryTypeSet(MarketDataRequest.MDENTRYTYPESET_BIDASK);
        while (securities.hasMoreElements()) {
            TradingSecurity ts = securities.nextElement();
            if (ts.getFXCMSubscriptionStatus() == null
                || IFixDefs.FXCMSUBSCRIPTIONSTATUS_SUBSCRIBE.equals(ts.getFXCMSubscriptionStatus())) {
                mdr.addRelatedSymbol(ts);
            }
        }
        mRequestID = GATEWAY.sendMessage(mdr);
        mLogger.debug("mMarketDataRequestID = " + mRequestID);
        setWaitDialogText(mResMan.getString("IDS_GETTING_OFFERS"));
        beginProcessing();

        mRequestID = GATEWAY.requestAccounts();
        mLogger.debug("mAccountMassID = " + mRequestID);
        setWaitDialogText(mResMan.getString("IDS_GETTING_ACCOUNTS"));
        beginProcessing();

        mRequestID = GATEWAY.requestOpenPositions();
        mLogger.debug("mOpenPositionMassID = " + mRequestID);
        setWaitDialogText(mResMan.getString("IDS_GETTING_OPEN_POSITIONS"));
        beginProcessing();

        mRequestID = GATEWAY.requestOpenOrders();
        mLogger.debug("mOpenOrderMassID = " + mRequestID);
        setWaitDialogText(mResMan.getString("IDS_GETTING_OPEN_ORDERS"));
        beginProcessing();

        mRequestID = GATEWAY.requestClosedPositions();
        mLogger.debug("mClosedPositionMassID = " + mRequestID);
        setWaitDialogText(mResMan.getString("IDS_GETTING_CLOSED_POSITIONS"));
        beginProcessing();
    }

    /**
     * log into the trade server
     *
     * @param aUsername user
     * @param aPassword pass
     * @param aTerminal terminal
     * @param aUrl url
     *
     * @throws Exception aex
     */
    public void login(String aUsername, String aPassword, String aTerminal, String aUrl, String aCfgFile) throws Exception {
        mLogout = false;
        mUsername = aUsername;
        mPassword = aPassword;
        mTerminal = aTerminal;
        mHostUrl = aUrl;
        UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        GATEWAY.logout();
        GATEWAY.registerGenericMessageListener(this);
        GATEWAY.registerStatusMessageListener(this);
        if (aCfgFile == null) {
            mCfgFile = mApplication.getServerConfigFile(mUsername);
        } else {
            mCfgFile = aCfgFile;
        }
        try {
            FXCMLoginProperties props = new FXCMLoginProperties(mUsername, mPassword, mTerminal, mHostUrl, "".equals(mCfgFile) ? null : mCfgFile);
            //props.addProperty(IConnectionManager.HTTP_IMPLEMENTATION, "apache");
            //props.addProperty(IConnectionManager.MSG_FLAGS, String.valueOf(IFixDefs.CHANNEL_MARKET_DATA));
            //props.addProperty(IConnectionManager.RELOGIN_TIMEOUT, "0");
            String value = preferences.getString("Server.secure." + mTerminal);
            if (value != null) {
                props.addProperty(IConnectionManager.SECURE_PREF, value);
            }
            props.addProperty(IConnectionManager.APP_INFO, "JTS");
            if (preferences.getBoolean("Proxy.use")) {
                props.addProperty(IConnectionManager.PROXY_SERVER, preferences.getString("Proxy.host"));
                props.addProperty(IConnectionManager.PROXY_PORT, preferences.getString("Proxy.port"));
                props.addProperty(IConnectionManager.PROXY_UID, preferences.getString("Proxy.user"));
                props.addProperty(IConnectionManager.PROXY_PWD, preferences.getString("Proxy.password"));
            }
            TradingSessionDesc[] tradingSessions = GATEWAY.getTradingSessions(props);
            MainFrame mainFrame = TradeApp.getInst().getMainFrame();
            TradingSessionDesc selectedSession;
            if (tradingSessions.length > 1) {
                TradingSessionDialog tsd = new TradingSessionDialog(mainFrame, tradingSessions);
                int selection = tsd.showModal();
                selectedSession = tradingSessions[selection];
                mLogger.debug("selection = " + selectedSession);
            } else {
                selectedSession = tradingSessions[0];
            }
            String pinRequired = selectedSession.getProperty("PIN_REQUIRED");
            if (pinRequired != null && "Y".equalsIgnoreCase(pinRequired)) {
                PINDialog pinDialog = new PINDialog(mainFrame);
                pinDialog.setVisible(true);
                Properties properties = new Properties();
                properties.setProperty(IUserSession.PIN, pinDialog.getPIN());
                GATEWAY.openSession(selectedSession, properties);
            } else {
                GATEWAY.openSession(selectedSession);
            }
        } catch (Exception e) {
            //loop through all nested causes looking for authentication exception
            Throwable cause = e;
            while (cause.getCause() != null && !(cause instanceof AuthenticationException)) {
                cause = cause.getCause();
            }
            String error = OraCodeFactory.toMessage(cause.getMessage());
            if (error != null && error.length() > 0) {
                throw new LoginException(cause, error);
            } else if (e instanceof AuthenticationException || cause instanceof AuthenticationException) {
                String value = "Login failed: Incorrect user name or password.";
                throw new LoginException(cause, value);
            } else {
                String value = "Login failed. Server does not return session id.";
                throw new LoginException(cause, value);
            }
        }

        getUserObjects();

        FXCMConnection cx = FXCMConnectionsManager.getConnection(mTerminal);
        cx.setUsername(mUsername);
        FXCMConnectionsManager.updateAddConnection(mTerminal, cx);
        preferences.set("Server.last.connected.terminal", mTerminal);
    }

    /**
     * log out of the trade server
     */
    public void logout() {
        mLogout = true;
        mStopLimitOrderMap.clear();
        GATEWAY.logout();
    }

    public void messageArrived(ISessionStatus aStatus) {
        if (mApplication != null) {   // that cannot be , but just in case
            if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_ERROR) {
                mApplication.communicationError();
            } else if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTED) {
                mApplication.communicationBroken();
            } else if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTING) {
                mApplication.communicationDisconnecting();
            } else if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_LOGGEDIN) {
                mApplication.communicationEstablished();
            } else if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_READY) {
                mApplication.communicationEstablished();
            } else if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_CONNECTING) {
                mApplication.communicationConnecting();
            }
        }
    }

    public void messageArrived(ITransportable aMessage) {
        IProcessor processor = mProcessorMap.get(aMessage.getType());
        if (processor == null) {
            mLogger.debug("Unhandled Message " + aMessage.getClass() + " == " + aMessage);
        } else {
            processor.process(aMessage);
        }
    }

    public void relogin() {
        if (mLogout) {
            return;
        }
        final JDialog jd = TradeApp.getInst().getMainFrame().createWaitDialog("Session Lost...Reconnecting");
        jd.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent aEvent) {
                Thread worker = new Thread(new Runnable() {
                    public void run() {
                        try {
                            TradingServerSession.getInstance().getGateway().relogin();
                            Liaison.getInstance().refresh();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            jd.dispose();
                            jd.setVisible(false);
                        }
                    }
                });
                worker.start();
            }
        });
        jd.setVisible(true);
    }

    /**
     * sends a msg
     *
     * @param aTransportable message
     */
    public void send(ITransportable aTransportable) {
        try {
            if (aTransportable instanceof OrderSingle) {
                fillJTSParty(((OrderSingle) aTransportable).getParties());
            } else if (aTransportable instanceof OrderCancelReplaceRequest) {
                fillJTSParty(((OrderCancelReplaceRequest) aTransportable).getParties());
            } else if (aTransportable instanceof OrderCancelRequest) {
                fillJTSParty(((OrderCancelRequest) aTransportable).getParties());
            }
            GATEWAY.sendMessage(aTransportable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWaitDialogText(String aText) {
        MainFrame mainFrame = TradeApp.getInst().getMainFrame();
        JLabel aLabel = null;
        if (mainFrame.getWaitDialog() != null && mainFrame.getWaitDialog().isVisible()) {
            aLabel = (JLabel) mainFrame.getWaitDialog().getContentPane().getComponent(0);
        }
        if (aLabel != null) {
            aLabel.setText(aText);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TradingServerSession");
        sb.append("{mApplication=").append(mApplication);
        sb.append(", mCfgFile='").append(mCfgFile).append('\'');
        sb.append(", mHostUrl='").append(mHostUrl).append('\'');
        sb.append(", mProcessorMap=").append(mProcessorMap);
        sb.append(", mRequestID='").append(mRequestID).append('\'');
        sb.append(", mResMan=").append(mResMan);
        sb.append(", mStopLimitOrderMap=").append(mStopLimitOrderMap);
        sb.append(", mTradingSessionStatus=").append(mTradingSessionStatus);
        sb.append('}');
        return sb.toString();
    }
}
