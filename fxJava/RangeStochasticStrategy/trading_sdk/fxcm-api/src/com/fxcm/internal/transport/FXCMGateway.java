/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/fxcm-api/src/main/com/fxcm/internal/transport/FXCMGateway.java#16 $
 *
 * Copyright (c) 2011 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 *
 * $History: $
 * 01/03/2005   Andre Mermegas: added methods to remove listeners
 * 01/04/2005   Andre Mermegas: minor tweaks, throw exception if user tries to send unknown message abstracted synchronization into the messagerouter
 * 01/07/2005   Andre Mermegas: added ability to process open/close entry orders and open positions, also setting the stop/limit on open positions
 * 01/10/2005   Andre Mermegas: added handling for deleting/updating stop/limit on entry and market orders
 * 02/11/2005   Andre Mermegas: numerous changes, moved some functionality in from outside code for locking and sending message
 * 02/28/2005   Andre Mermegas: changse to object model, use new fix classes
 * 03/03/2005   Andre Mermegas: added getClosedPositions
 * 03/17/2005   Andre Mermegas: added isConnected method, refactored the IMessageStrategy method names
 * 04/13/2005   Andre Mermegas: added a getTradingSessionStatus method, and updates for changes in fxmsg the parsing is moved up
 * 05/24/2005   Andre Mermegas: changed from getTradingSessionSTatus to RequestTradingSessionStatus
 *                              updates to use fix objects for batch requests etc.., clear listener maps on logout
 * 06/03/2005   Andre Mermegas: FXCMGateway supports sendMessage(), some objects moved to different package
 * 06/03/2005   Miron:          OrderMassStatusRequest moved to trade package
 * 06/06/2005   Elana:          added in loginProc: ConnectionManagerEx.setMsgFlags (props.getMsgFlags());
 * 06/10/2005   Andre Mermegas: fix to relogin(), dont call logout because that clears listeners.
 * 06/13/2005   Miron:          symplified to follow up FXCMLoginProperties changes
 * 06/17/2005   Andre Mermegas: station should be "TS", also fill in requestID
 * 06/20/2005   Miron:          sendMessage(), requestTradingSessionStatus() redone
 * 06/30/2005   Andre Mermegas: use RequestforPositions for open and closed
 * 08/01/2005   Miron:          sendMessage() takes care about TradingSessionID/SubID to fill
 * 08/12/2005   Miron:           added request*() with aLoginId
 * 11/10/2005   Andre Mermegas: added request closed positions for historical snapshots and request order status
 * 02/20/2006   Andre Mermegas: minor cleanup
 * 03/08/2006   Andre Mermegas: Use JAVA-API station name
 * 03/16/2006   Andre Meremgas: Set the appInfo property
 * 03/29/2006   Andre Mermegas: added extra logging for logout method
 * 05/09/2006   Andre Mermegas: added support for MarketDataRequest
 * 06/23/2006   Andre Mermegas: added new methods to allow for trading session selection, on logins with multiple systems attached to them
 * 07/21/2006   Andre Mermegas: removed debug log for logout lock - resolved
 * 08/14/2006   Andre Mermegas: update, not use Iterator, just use for loop for concurrency issues
 * 08/21/2006   Andre Mermegas: use commons logger
 * 11/28/2006   Andre Mermegas: added new method to support properties in opensession, PIN support
 * 12/19/2006   Andre Mermegas: added direct support for getting assets by acctid
 * 03/30/2007   Andre Mermegas: add some sort of app_info, based on calling class name for identification, as per request of Darren Merwitz
 * 07/03/2007   Andre Mermegas: added support of userrequest,orderlist,fxcmrequest
 * 07/27/2007   Andre Mermegas: listid support
 * 04/23/2008   Andre Mermegas: fill clordid on list suborders
 * 04/08/2009   Andre Mermegas: add Account to requestOrderStatus(...) signature
 * 03/30/2009   AP:             external price server support
 * 05/07/2009   AP:             setMsgFlags usage added
 * 11/17/2009   Andre Mermegas: don't logout(), just close session.
 * 03/30/2010   Andre Mermegas: use IUserTransportableListener instead of IUserMessageListener
 * 07/29/2010   Andre Mermegas: add getUserKind
 * 08/19/2010   Andre Mermegas: cleanup session handling
 * 02/11/2011   Andre Mermegas: add requestAccountByName(String); method
 * 08/02/2011   Andre Mermegas: don't processExternalPriceServer if native connection
 * 04/19/2012   Andre Mermegas: use util helper
 * 05/16/2013   vstelnikov:     SSOLoginProperties support
 */
package com.fxcm.internal.transport;

import com.fxcm.GenericException;
import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.sso.SSOLoginProperties;
import com.fxcm.external.api.transport.exception.SessionNotEstablishedException;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.external.api.util.OrdStatusRequestType;
import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.IFixValueDefs;
import com.fxcm.fix.PosReqTypeFactory;
import com.fxcm.fix.SubscriptionRequestTypeFactory;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;
import com.fxcm.fix.custom.BatchRequest;
import com.fxcm.fix.custom.FXCMRequest;
import com.fxcm.fix.other.UserRequest;
import com.fxcm.fix.posttrade.CollateralInquiry;
import com.fxcm.fix.posttrade.RequestForPositions;
import com.fxcm.fix.pretrade.MarketDataRequest;
import com.fxcm.fix.pretrade.QuoteRequest;
import com.fxcm.fix.pretrade.QuoteResponse;
import com.fxcm.fix.pretrade.SecurityListRequest;
import com.fxcm.fix.pretrade.SecurityStatusRequest;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.fix.pretrade.TradingSessionStatusRequest;
import com.fxcm.fix.trade.OrderCancelReplaceRequest;
import com.fxcm.fix.trade.OrderCancelRequest;
import com.fxcm.fix.trade.OrderList;
import com.fxcm.fix.trade.OrderMassStatusRequest;
import com.fxcm.fix.trade.OrderSingle;
import com.fxcm.fix.trade.OrderStatusRequest;
import com.fxcm.messaging.ConnectionManagerEx;
import com.fxcm.messaging.IMessage;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;
import com.fxcm.messaging.IUserSession;
import com.fxcm.messaging.IUserSessionStatusListener;
import com.fxcm.messaging.IUserTransportableListener;
import com.fxcm.messaging.TradingSessionDesc;
import com.fxcm.messaging.util.IConnectionManager;
import com.fxcm.messaging.util.fix.FIXUserSession;
import com.fxcm.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class FXCMGateway implements IUserTransportableListener, IUserSessionStatusListener, IGateway {
    private static final int WAIT_RESPONSE_TO = 180000; // 3 min
    private AtomicBoolean mConnectingPrices;
    private List mGenericMessageListeners;
    private boolean mLoggedIn;
    private final Log mLogger;
    private IUserSession mPriceSession;
    private FXCMLoginProperties mProps;
    private List mStatusMessageListeners;
    private boolean mTradingSessionRetrieved;
    private final TransportMutex mTransportMutex;
    private IUserSession mUserSession;

    public FXCMGateway() {
        mLogger = LogFactory.getLog(FXCMGateway.class);
        mGenericMessageListeners = new ArrayList();
        mStatusMessageListeners = new ArrayList();
        mTransportMutex = new TransportMutex();
        mConnectingPrices = new AtomicBoolean(false);
    }

    /**
     * Closes the opened user session and removes all references to it
     */
    private void close(IUserSession aUserSession) {
        if (aUserSession != null) {
            try {
                aUserSession.close();
            } catch (GenericException e) {
                mLogger.error(e.getMessage(), e);
            }
            if (mUserSession == aUserSession) {
                mUserSession = null;
            } else if (mPriceSession == aUserSession) {
                mPriceSession = null;
            }
            aUserSession.removeMessageListener(this);
            aUserSession.removeSessionStatusListener(this);
            ConnectionManagerEx.cleanup(aUserSession);
        }
    }

    private void fillExtraFields(ITransportable aTransportable) throws GenericException {
        String nextRequestID = aTransportable.getRequestID();
        if (nextRequestID == null) {
            nextRequestID = mUserSession.getNextRequestID();
        }
        if (aTransportable instanceof QuoteRequest) {
            ((QuoteRequest) aTransportable).setQuoteReqID(nextRequestID);
            ((QuoteRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((QuoteRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof OrderCancelReplaceRequest) {
            ((OrderCancelReplaceRequest) aTransportable).setClOrdID(nextRequestID);
            ((OrderCancelReplaceRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((OrderCancelReplaceRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof OrderCancelRequest) {
            ((OrderCancelRequest) aTransportable).setClOrdID(nextRequestID);
            ((OrderCancelRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((OrderCancelRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof OrderSingle) {
            ((OrderSingle) aTransportable).setClOrdID(nextRequestID);
            ((OrderSingle) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((OrderSingle) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof OrderMassStatusRequest) {
            ((OrderMassStatusRequest) aTransportable).setMassStatusReqID(nextRequestID);
            ((OrderMassStatusRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((OrderMassStatusRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof RequestForPositions) {
            ((RequestForPositions) aTransportable).setPosReqID(nextRequestID);
            ((RequestForPositions) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((RequestForPositions) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof CollateralInquiry) {
            ((CollateralInquiry) aTransportable).setCollInquiryID(nextRequestID);
            ((CollateralInquiry) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((CollateralInquiry) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof TradingSessionStatusRequest) {
            ((TradingSessionStatusRequest) aTransportable).setTradSesReqID(nextRequestID);
            ((TradingSessionStatusRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((TradingSessionStatusRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof OrderStatusRequest) {
            ((OrderStatusRequest) aTransportable).setOrderStatusReqID(nextRequestID);
            ((OrderStatusRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((OrderStatusRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof QuoteResponse) {
            ((QuoteResponse) aTransportable).setQuoteRespID(nextRequestID);
            ((QuoteResponse) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((QuoteResponse) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof SecurityStatusRequest) {
            ((SecurityStatusRequest) aTransportable).setSecurityStatusReqID(nextRequestID);
            ((SecurityStatusRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((SecurityStatusRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof SecurityListRequest) {
            ((SecurityListRequest) aTransportable).setSecurityReqID(nextRequestID);
            ((SecurityListRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((SecurityListRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof MarketDataRequest) {
            ((MarketDataRequest) aTransportable).setMDReqID(nextRequestID);
            ((MarketDataRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((MarketDataRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof OrderList) {
            OrderList orderList = (OrderList) aTransportable;
            orderList.setTradingSessionID(mUserSession.getTradingSession().getID());
            orderList.setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
            orderList.setListID(nextRequestID);
            OrderSingle[] orders = orderList.getOrders();
            for (int i = 0; i < orders.length; i++) {
                OrderSingle order = orders[i];
                order.setClOrdID(mUserSession.getNextRequestID());
                order.setTradingSessionID(mUserSession.getTradingSession().getID());
                order.setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
            }
        } else if (aTransportable instanceof UserRequest) {
            ((UserRequest) aTransportable).setUserRequestID(nextRequestID);
            ((UserRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((UserRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof FXCMRequest) {
            ((FXCMRequest) aTransportable).setTestReqID(nextRequestID);
            ((FXCMRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((FXCMRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else if (aTransportable instanceof BatchRequest) {
            ((BatchRequest) aTransportable).setMDReqID(nextRequestID);
            ((BatchRequest) aTransportable).setTradingSessionID(mUserSession.getTradingSession().getID());
            ((BatchRequest) aTransportable).setTradingSessionSubID(mUserSession.getTradingSession().getSubID());
        } else {
            mLogger.error("UNKNOWN TYPE: CAN'T FILL EXTRA INFO: " + aTransportable);
        }
    }

    public ISessionStatus getCurrentStatus() {
        try {
            return mUserSession.getCurrentStatus();
        } catch (Exception e) {
            throw new SessionNotEstablishedException("UserSession not established. Need to Login", e.getCause());
        }
    }

    public String getSessionID() {
        try {
            return mUserSession.getSessionID();
        } catch (Exception e) {
            throw new SessionNotEstablishedException("UserSession not established. Need to Login", e.getCause());
        }
    }

    public TradingSessionDesc getTradingSession() {
        try {
            return mUserSession.getTradingSession();
        } catch (Exception e) {
            throw new SessionNotEstablishedException("UserSession not established. Need to Login", e.getCause());
        }
    }

    private TradingSessionDesc[] getTradingSessionDescs() throws Exception {
        TradingSessionDesc[] tsds = new TradingSessionDesc[0];
        try {
            tsds = mUserSession.retrieveTradingSessions();
        } catch (GenericException aException) {
            close(mUserSession);
            throw aException;
        }
        if (tsds == null || tsds.length == 0) {
            close(mUserSession);
            throw new Exception("Problem retrieving trading session");
        }
        return tsds;
    }

    public TradingSessionDesc[] getTradingSessions(FXCMLoginProperties aProps) throws Exception {
        mProps = aProps;
        if (mProps != null) {
            ConnectionManagerEx.init(mProps.getProperties());
            String appInfo = mProps.getProperties().getProperty(IConnectionManager.APP_INFO);
            try {
                if (appInfo == null) {
                    appInfo = new Exception().getStackTrace()[1].getClassName();
                }
            } catch (Exception e) {
                //swallow
            }

            if (aProps instanceof SSOLoginProperties) {
            	SSOLoginProperties ssoProps = (SSOLoginProperties)aProps;

                SSOHelper ssoHelper = new SSOHelper(mProps.getProperties());

                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("retrieve HostsXML from " + ssoProps.getServer());
                }

                String hostsXML = ssoHelper.retrieveHostsXML(ssoProps.getServer(),
                                                             ssoProps.getTerminal(),
                                                             ssoProps.getSAMLAssertion(),
                                                             appInfo);
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("Open token = " + ssoProps.getOpenToken());
                }

                mUserSession = ConnectionManagerEx.createUserSessionOpenToken(null,
                                                                              hostsXML,
                                                                              ssoProps.getTerminal(),
                                                                              ssoProps.getServiceName(),
                                                                              ssoProps.getOpenToken(),
                                                                              appInfo);
            } else {
                mUserSession = ConnectionManagerEx.createUserSession(mProps.getServer(),
                                                                     mProps.getTerminal(),
                                                                     mProps.getServiceName(),
                                                                     mProps.getUserName(),
                                                                     mProps.getPassword(),
                                                                     appInfo);
            }

            try {
                if (!mUserSession.loadStationDescriptor()) {
                    close(mUserSession);
                    throw new Exception("Problem getting station descriptor");
                }
                return getTradingSessionDescs();
            } catch (Exception aException) {
                close(mUserSession);
                throw aException;
            }

        } else {
            throw new Exception("Received null FXCMLoginProperties");
        }
    }

    public int getUserKind() {
        try {
            return mUserSession.getUserKind();
        } catch (Exception e) {
            throw new SessionNotEstablishedException("UserSession not established. Need to Login", e.getCause());
        }
    }

    public boolean isConnected() {
        return mUserSession != null && !mUserSession.isClosed() && mUserSession.isValid();
    }

    public void login(FXCMLoginProperties aProps) throws Exception {
        mProps = aProps;
        if (mProps != null) {
            ConnectionManagerEx.init(mProps.getProperties());
            String appInfo = mProps.getProperties().getProperty(IConnectionManager.APP_INFO);
            try {
                if (appInfo == null) {
                    appInfo = new Exception().getStackTrace()[1].getClassName();
                }
            } catch (Exception e) {
                //swallow
            }
            mUserSession = ConnectionManagerEx.createUserSession(mProps.getServer(),
                                                                 mProps.getTerminal(),
                                                                 mProps.getServiceName(),
                                                                 mProps.getUserName(),
                                                                 mProps.getPassword(),
                                                                 appInfo);
            mUserSession.setMessageListener(this);
            mUserSession.setSessionStatusListener(this);
            try {
                if (!mUserSession.loadStationDescriptor()) {
                    close(mUserSession);
                    throw new Exception("Problem getting station descriptor");
                }

                TradingSessionDesc tsds = getTradingSessionDescs()[0];
                mUserSession.setTradingSession(tsds);
                String extraParams = Util.parseParams(aProps.getProperties());
                if (!mUserSession.open(extraParams)) {
                    close(mUserSession);
                    throw new Exception("Not connected, can't send out initial queries. Could not establish UserSession.");
                }
                String priceTerminal = tsds.getProperty("PRICE_TERMINAL");
                processExternalPriceServer(priceTerminal);
                mLoggedIn = true;
            } catch (Exception aException) {
                close(mUserSession);
                throw aException;
            }
        } else {
            throw new Exception("Received null FXCMLoginProperties");
        }
    }

    public void logout() {
        mTransportMutex.interruptWaiting();
        close(mPriceSession);
        close(mUserSession);
        mGenericMessageListeners.clear();
        mStatusMessageListeners.clear();
        mLoggedIn = false;
    }

    public void openSession(TradingSessionDesc aTradingSessionDesc) throws Exception {
        openSession(aTradingSessionDesc, null);
    }

    public void openSession(TradingSessionDesc aTradingSessionDesc, Properties aProperties) throws Exception {
        if (mLoggedIn) {
            throw new Exception("Can not open more than one session at a time.");
        } else {
            String extraParams = Util.parseParams(aProperties);
            mUserSession.setTradingSession(aTradingSessionDesc);
            mUserSession.setMessageListener(this);
            mUserSession.setSessionStatusListener(this);
            try {
                if (!mUserSession.open(extraParams)) {
                    close(mUserSession);
                    throw new Exception("Not connected, can't send out initial queries. Could not establish UserSession.");
                }
                String priceTerminal = aTradingSessionDesc.getProperty("PRICE_TERMINAL");
                processExternalPriceServer(priceTerminal);
                mLoggedIn = true;
            } catch (Exception aException) {
                close(mUserSession);
                throw aException;
            }
        }
    }

    private void processExternalPriceServer(String priceTerminal) throws Exception {
        if (mUserSession instanceof FIXUserSession && ((FIXUserSession) mUserSession).isNative()) {
            return;
        }
        // prevent concurrent connecting from different threads on relogin
        if (!mConnectingPrices.compareAndSet(false, true)) {
            return;
        }

        String appInfo = mProps.getProperties().getProperty(IConnectionManager.APP_INFO);
        try {
            if (appInfo == null) {
                appInfo = new Exception().getStackTrace()[1].getClassName();
            }
        } catch (Exception e) {
            //swallow
        }

        try {
            // this request is used to get system rules where we can find a property
            // which defines should we use external price server
            TradingSessionStatusRequest tssReq = new TradingSessionStatusRequest();
            tssReq.setTradSesReqID(mUserSession.getNextRequestID());
            tssReq.setSubscriptionRequestType(SubscriptionRequestTypeFactory.SNAPSHOT);
            ITransportable resp = sendRequestAndWaitResponse(tssReq, tssReq.getRequestID());
            if (resp == null) {
                // this request is implemented by FIX framerwork so results should be always
                // even if external price server is not available
                throw new Exception("Failed to get Trading Session Status");
            }
            TradingSessionStatus tss = (TradingSessionStatus) resp;

            // this property defines should we use external price server or not
            String extPriceTerminal = tss.getParameterValue("ext_price_terminal");
            if (extPriceTerminal == null) {
                extPriceTerminal = tss.getParameterValue("EXT_PRICE_TERMINAL");
            }

            if (!Util.isEmpty(extPriceTerminal)) {
                priceTerminal = extPriceTerminal;
            }

            // use external price server?
            if (!Util.isEmpty(priceTerminal)) {
                // create price server session
                mPriceSession = ConnectionManagerEx.createUserSession(mProps.getServer(),
                                                                      priceTerminal,
                                                                      null,
                                                                      null,
                                                                      appInfo);
                if (!mPriceSession.loadStationDescriptor()) {
                    throw new Exception("Problem getting station descriptor for price session");
                }

                // set trading session, make copy of the trading one
                mPriceSession.setTradingSession(mUserSession.getTradingSession());

                // set message flags for both sessions
                long priceMsgFlags = IFixDefs.CHANNEL_TRADING_DATA;
                long userMsgFlags = IFixDefs.CHANNEL_MARKET_DATA;
                String value = mProps.getProperties().getProperty(IConnectionManager.MSG_FLAGS);
                try {
                    if (value != null && !"0".equals(value)) {
                        long lFlag = Long.parseLong(value);
                        priceMsgFlags = lFlag | IFixDefs.CHANNEL_TRADING_DATA;
                        userMsgFlags = lFlag | IFixDefs.CHANNEL_MARKET_DATA;
                    }
                } catch (Exception e) {
                    mLogger.error("problem parsing msg flags:" + value, e);
                }
                mPriceSession.setMsgFlags(priceMsgFlags);
                mUserSession.setMsgFlags(userMsgFlags);

                mPriceSession.attach(mUserSession.getSessionID(), null);
                mPriceSession.setMessageListener(this);
                mPriceSession.setSessionStatusListener(this);
                // send attach request
                UserRequest ur = new UserRequest();
                ur.setUserRequestType(IFixValueDefs.USERREQUESTTYPE_ATTACHSESSION);
                ur.setTradingSessionID(mPriceSession.getTradingSession().getID());
                ur.setTradingSessionSubID(mPriceSession.getTradingSession().getSubID());
                mPriceSession.send(ur.toMessage(mPriceSession.getSessionID(), mPriceSession.getMessageFactory()));
            }
        } catch (Exception e) {
            mLogger.error(e);
            mTransportMutex.interruptWaiting();
            close(mPriceSession);
            throw e;
        } finally {
            mConnectingPrices.set(false);
        }
    }

    public void registerGenericMessageListener(IGenericMessageListener aListener) {
        mGenericMessageListeners.add(aListener);
    }

    public void registerStatusMessageListener(IStatusMessageListener aListener) {
        mStatusMessageListeners.add(aListener);
    }

    public void relogin() throws Exception {
        if (mProps == null || !mLoggedIn) {
            throw new Exception("Not logged in or login properties not available from original login, cannot relogin without logging in first.");
        } else {
            close(mPriceSession);
            close(mUserSession);
            mLoggedIn = false;
            login(mProps);
        }
    }

    public void removeGenericMessageListener(IGenericMessageListener aListener) {
        mGenericMessageListeners.remove(aListener);
    }

    public void removeStatusMessageListener(IStatusMessageListener aListener) {
        mStatusMessageListeners.remove(aListener);
    }

    public String requestAccountByName(String aAccountName) {
        String reqID = null;
        try {
            CollateralInquiry ci = new CollateralInquiry();
            ci.getParties().setFXCMAcctID(1); //xxx indicator that we should perform equity query, but by name not id
            ci.setAccount(aAccountName);
            reqID = sendMessage(ci);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestAccounts() {
        String reqID = null;
        try {
            reqID = sendMessage(new CollateralInquiry());
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestAccounts(String aLoginID) {
        String reqID = null;
        try {
            CollateralInquiry ci = new CollateralInquiry();
            if (aLoginID != null) {
                ci.getParties().setFXCMTraderLoginId(aLoginID);
            }
            reqID = sendMessage(ci);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestAccounts(long aFXCMAcctID) {
        String reqID = null;
        try {
            CollateralInquiry ci = new CollateralInquiry();
            ci.getParties().setFXCMAcctID(aFXCMAcctID);
            reqID = sendMessage(ci);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestClosedPositions() {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setPosReqType(PosReqTypeFactory.TRADES);
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestClosedPositions(String aLoginID) {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setPosReqType(PosReqTypeFactory.TRADES);
            if (aLoginID != null) {
                message.getParties().setFXCMTraderLoginId(aLoginID);
            }
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestClosedPositions(long aFXCMAcctID) {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setPosReqType(PosReqTypeFactory.TRADES);
            message.getParties().setFXCMAcctID(aFXCMAcctID);
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestClosedPositions(int aFXCMMaxNoResults,
                                         UTCDate aFXCMStartDate,
                                         UTCTimeOnly aFXCMStartTime,
                                         UTCDate aFXCMEndDate,
                                         UTCTimeOnly aFXCMEndTime) {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setFXCMMaxNoResults(aFXCMMaxNoResults);
            message.setFXCMStartDate(aFXCMStartDate);
            message.setFXCMStartTime(aFXCMStartTime);
            message.setFXCMEndDate(aFXCMEndDate);
            message.setFXCMEndTime(aFXCMEndTime);
            message.setPosReqType(PosReqTypeFactory.TRADES);
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOpenOrders() {
        String reqID = null;
        try {
            reqID = sendMessage(new OrderMassStatusRequest());
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOpenOrders(String aLoginID) {
        String reqID = null;
        try {
            OrderMassStatusRequest omsr = new OrderMassStatusRequest();
            if (aLoginID != null) {
                omsr.getParties().setFXCMTraderLoginId(aLoginID);
            }
            reqID = sendMessage(omsr);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOpenOrders(long aFXCMAcctID) {
        String reqID = null;
        try {
            OrderMassStatusRequest omsr = new OrderMassStatusRequest();
            omsr.getParties().setFXCMAcctID(aFXCMAcctID);
            reqID = sendMessage(omsr);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOpenPositions() {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setPosReqType(PosReqTypeFactory.POSITIONS);
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOpenPositions(String aLoginID) {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setPosReqType(PosReqTypeFactory.POSITIONS);
            if (aLoginID != null) {
                message.getParties().setFXCMTraderLoginId(aLoginID);
            }
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOpenPositions(long aFXCMAcctID) {
        String reqID = null;
        try {
            RequestForPositions message = new RequestForPositions();
            message.setPosReqType(PosReqTypeFactory.POSITIONS);
            message.getParties().setFXCMAcctID(aFXCMAcctID);
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestOrderStatus(String aId, OrdStatusRequestType aOrdStatusRequestType, String aAccount) {
        String reqID = null;
        try {
            OrderStatusRequest message = new OrderStatusRequest();
            if (aOrdStatusRequestType == OrdStatusRequestType.ORDERID) {
                message.setOrderID(aId);
            } else if (aOrdStatusRequestType == OrdStatusRequestType.CLORDID) {
                message.setClOrdID(aId);
            } else if (aOrdStatusRequestType == OrdStatusRequestType.SECONDARYCLORDID) {
                message.setSecondaryClOrdID(aId);
            }
            message.setAccount(aAccount);
            reqID = sendMessage(message);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String requestTradingSessionStatus() {
        String reqID = null;
        try {
            mTradingSessionRetrieved = true;
            TradingSessionStatusRequest tsr = new TradingSessionStatusRequest();
            reqID = sendMessage(tsr);
        } catch (Exception e) {
            mLogger.error(e.getMessage(), e);
            reqID = null;
        }
        return reqID;
    }

    public String sendMessage(ITransportable aTransportable) throws Exception {
        fillExtraFields(aTransportable);
        IMessage msg = aTransportable.toMessage(mUserSession.getSessionID(), mUserSession.getMessageFactory());
        mUserSession.send(msg);
        return aTransportable.getRequestID();
    }

    /**
     * Sends a request and waits for a single response.
     *
     * @param aRequest a request object
     * @param aReqID an id of the request object
     *
     * @return a response object or null if the request cannot be sent or failed to get
     *         a response
     *
     * @throws Exception if transport related error occurred
     */
    private ITransportable sendRequestAndWaitResponse(ITransportable aRequest, String aReqID) throws Exception {
        synchronized (mTransportMutex) {
            try {
                // may be we are already waiting
                if (mTransportMutex.getRequestID() != null) {
                    return null;
                }

                mTransportMutex.setRequestID(aReqID);
                mTransportMutex.setResponse(null);
                mTransportMutex.setWaitingThread(Thread.currentThread());

                // send the request and wait for response some time
                sendMessage(aRequest);
                mTransportMutex.wait(WAIT_RESPONSE_TO);

                return mTransportMutex.getResponse();
            } finally {
                mTransportMutex.setRequestID(null);
                mTransportMutex.setWaitingThread(null);
            }
        }
    }

    public void setMsgFlags(long aMsgFlags) throws SessionNotEstablishedException {
        if (mUserSession == null || !mLoggedIn) {
            throw new SessionNotEstablishedException("UserSession not established. Need to Login");
        }
        mUserSession.setMsgFlags(aMsgFlags);

        if (mPriceSession != null) {
            mPriceSession.setMsgFlags(aMsgFlags);
        }
    }

    public void update(IUserSession aSession, ISessionStatus aNewStatus) {
        if (aSession == mUserSession) {
            // *** STATUSCODE_CONNECTING ***
            if (aNewStatus.getStatusCode() == ISessionStatus.STATUSCODE_CONNECTING) {
                // we are here if connection with a trading server has been lost and we try
                // to restore it -> close price session, it will be reopened later
                close(mPriceSession);
                // *** STATUSCODE_DISCONNECTED ***
            } else if (aNewStatus.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTED) {
                // interrupt a waiting request if any and close a price session if it's opened
                mTransportMutex.interruptWaiting();
                close(mPriceSession);
                // *** STATUSCODE_LOGGEDIN ***
            } else if (aNewStatus.getStatusCode() == ISessionStatus.STATUSCODE_LOGGEDIN) {
                if (mLoggedIn) {
                    // we are here if the user session was relogged, now it has
                    // another session id -> reopen the price session
                    new Thread() {
                        public void run() {
                            close(mPriceSession);

                            // just for case... if after relogin of user session external
                            // price server is disabled we should get prices from the
                            // user session. processExternalPriceServer below will override
                            // it if necessary
                            mUserSession.setMsgFlags(IFixDefs.CHANNEL_SETTING_DEFAULT);

                            // reopen it
                            try {
                                processExternalPriceServer(null);
                            } catch (Exception e) {
                                // failed to reopen the price session -> close the user session
                                mTransportMutex.interruptWaiting();
                                close(mPriceSession);
                            }
                        }
                    }.start();
                }
            }

            // notify listeners but only for user session
            for (int i = 0; i < mStatusMessageListeners.size(); i++) {
                IStatusMessageListener listener = (IStatusMessageListener) mStatusMessageListeners.get(i);
                listener.messageArrived(aNewStatus);
            }
        } else if (aSession == mPriceSession) {
            // *** STATUSCODE_DISCONNECTED ***
            if (aNewStatus.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTED) {
                // interrupt a waiting request if any and close a price session if it's opened
                mTransportMutex.interruptWaiting();
                close(mPriceSession);
            }
        }
    }

    public void update(IUserSession aSession, ITransportable aTransportable) {
        // may be a response that we are waiting for is received
        synchronized (mTransportMutex) {
            String sRequestID = mTransportMutex.getRequestID();
            if (sRequestID != null) {
                if (sRequestID.equals(aTransportable.getRequestID())) {
                    mTransportMutex.setResponse(aTransportable);
                    mTransportMutex.notifyAll();
                }
            }
        }

        if (mLoggedIn && mTradingSessionRetrieved) {
            for (int i = 0; i < mGenericMessageListeners.size(); i++) {
                IGenericMessageListener listener = (IGenericMessageListener) mGenericMessageListeners.get(i);
                listener.messageArrived(aTransportable);
            }
        }
    }

    protected static class TransportMutex {
        protected String mRequestID;
        protected ITransportable mResponse;
        protected Thread mWaitingThread;

        TransportMutex() {
        }

        String getRequestID() {
            return mRequestID;
        }

        ITransportable getResponse() {
            return mResponse;
        }

        void setResponse(ITransportable aResponse) {
            mResponse = aResponse;
        }

        void interruptWaiting() {
            if (mWaitingThread != null) {
                mWaitingThread.interrupt();
            }
        }

        void setRequestID(String aRequestID) {
            mRequestID = aRequestID;
            mResponse = null;
        }

        void setWaitingThread(Thread aThread) {
            mWaitingThread = aThread;
        }
    }
}
