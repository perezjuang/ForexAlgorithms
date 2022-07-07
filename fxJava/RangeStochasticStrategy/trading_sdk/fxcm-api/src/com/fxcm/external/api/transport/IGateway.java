/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/fxcm-api/src/main/com/fxcm/external/api/transport/IGateway.java#4 $
 *
 * Copyright (c) 2004 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 *
 * $History: $
 * 01/03/2004   Andre Mermegas: added methods to remove listeners
 * 01/07/2004   Andre Mermegas: updated javadocs
 * 03/03/2005   Andre Mermegas: added requestclosedpositions method
 * 03/17/2005   Andre Mermegas: added isConnected method
 * 04/07/2005   Andre Mermegas: added return of massStatusReqID to bulk request methods,
 *                              added method to getTradingSessionStatus
 * 06/03/2005   Andre Mermegas  support sendMessage()
 * 08/12/2005   Miron:          overwitten request*() with aLoginId argument
 * 11/10/2005   Andre Mermegas: added request closed positions for historical snapshots and request order status
 * 06/23/2006   Andre Mermegas: added new methods to allow for trading session selection,
 *                              on logins with multiple systems attached to them
 * 11/28/2006   Andre Mermegas: added new method to support properties in opensession, PIN support
 * 12/19/2006   Andre Mermegas: added direct support for getting assets by acctid
 * 04/08/2009   Andre Mermegas: add Account to requestOrderStatus(...) signature
 * 07/29/2010   Andre Mermegas: add getUserKind
 * 02/11/2011   Andre Mermegas: add requestAccountByName(String); method
 */
package com.fxcm.external.api.transport;

import com.fxcm.external.api.transport.exception.SessionNotEstablishedException;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.external.api.util.OrdStatusRequestType;
import com.fxcm.fix.UTCDate;
import com.fxcm.fix.UTCTimeOnly;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;
import com.fxcm.messaging.TradingSessionDesc;

import java.util.Properties;

/**
 * Interface to the FXCM Platform
 *
 * @author Andre Mermegas
 *         Date: Dec 16, 2004
 *         Time: 9:26:21 AM
 */
public interface IGateway {
    /**
     * Gets last sent ISessionStatus message
     *
     * @return ISessionStatus
     *
     * @throws SessionNotEstablishedException
     */
    ISessionStatus getCurrentStatus() throws SessionNotEstablishedException;

    /**
     * @return the current active session id
     *
     * @throws SessionNotEstablishedException
     */
    String getSessionID() throws SessionNotEstablishedException;

    /**
     * Returns connected TradingSession
     *
     * @return TradingSessionDesc
     *
     * @throws SessionNotEstablishedException
     */
    TradingSessionDesc getTradingSession() throws SessionNotEstablishedException;

    /**
     * Obtains list of trading session that is available to login.
     *
     * @param aProps
     *
     * @return array of trading session
     *
     * @throws Exception
     */
    TradingSessionDesc[] getTradingSessions(FXCMLoginProperties aProps) throws Exception;

    /**
     * Gets the UserKind
     *
     * @return
     *
     * @see com.fxcm.fix.IFixDefs#FXCM_ACCT_TYPE_MANAGER
     * @see com.fxcm.fix.IFixDefs#FXCM_ACCT_TYPE_CLEARING
     * @see com.fxcm.fix.IFixDefs#FXCM_ACCT_TYPE_CONTROLLED
     */
    int getUserKind();

    /**
     * gives status of active connection to fxcm server.
     */
    boolean isConnected();

    /**
     * login to the FXCM server, exception are raised when a problem occurs
     * during login.
     *
     * @param aLoginProperties
     *
     * @throws Exception
     */
    void login(FXCMLoginProperties aLoginProperties) throws Exception;

    /**
     * logout of the FXCM server.
     */
    void logout();

    /**
     * Open a sesssion with the supplied TradingSession, only 1 session can be opened.
     *
     * @param aTradingSessionDesc
     *
     * @throws Exception
     */
    void openSession(TradingSessionDesc aTradingSessionDesc) throws Exception;

    /**
     * Open a sesssion with the supplied TradingSession, only 1 session can be opened.
     *
     * @param aTradingSessionDesc
     * @param aProperties
     *
     * @throws Exception
     */
    void openSession(TradingSessionDesc aTradingSessionDesc, Properties aProperties) throws Exception;

    /**
     * register as a aListener of generic messages: quotes,orders,excecutions
     *
     * @param aListener
     */
    void registerGenericMessageListener(IGenericMessageListener aListener);

    /**
     * register as a aListener of connection status events
     *
     * @param aListener
     */
    void registerStatusMessageListener(IStatusMessageListener aListener);

    /**
     * logout, then log back in. Exception are raised when a problem occurs during relogin
     *
     * @throws Exception
     */
    void relogin() throws Exception;

    /**
     * stops the supplied aListener from receiving future notifications.
     *
     * @param aListener
     */
    void removeGenericMessageListener(IGenericMessageListener aListener);

    /**
     * stops the supplied aListener from receiving future notifications.
     *
     * @param aListener
     */
    void removeStatusMessageListener(IStatusMessageListener aListener);

    /**
     * Get accounts by account name
     *
     * @param aAccountName
     *
     * @return requestid or null if there is a problem on request
     */
    String requestAccountByName(String aAccountName);

    /**
     * request for accounts associated with this login
     *
     * @return massStatusReqID or null if there is a problem on request
     */
    String requestAccounts();

    /**
     * request for accounts associated with supplied login id
     *
     * @param aLoginID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestAccounts(String aLoginID);

    /**
     * Get accounts by acctid
     *
     * @param aFXCMAcctID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestAccounts(long aFXCMAcctID);

    /**
     * request for the last 30 closed positions
     *
     * @return massStatusReqID  or null if there is a problem on request
     */
    String requestClosedPositions();

    /**
     * request for the last 30 closed positions with supplied login id
     *
     * @param aLoginID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestClosedPositions(String aLoginID);

    /**
     * request closed positions by acctid
     *
     * @param aFXCMAcctID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestClosedPositions(long aFXCMAcctID);

    /**
     * Request historical snapshot for closed positions in the range supplied, max allowed is 300
     *
     * @param aFXCMMaxNoResults
     * @param aFXCMStartDate
     * @param aFXCMStartTime
     * @param aFXCMEndDate
     * @param aFXCMEndTime
     *
     * @return requestid or null if there is a problem on request
     */
    String requestClosedPositions(int aFXCMMaxNoResults,
                                  UTCDate aFXCMStartDate,
                                  UTCTimeOnly aFXCMStartTime,
                                  UTCDate aFXCMEndDate,
                                  UTCTimeOnly aFXCMEndTime);

    /**
     * make a request for open orders on all accounts
     *
     * @return massStatusReqID or null if there is a problem on request
     */
    String requestOpenOrders();

    /**
     * request for open orders on all accounts with supplied login id
     *
     * @param aLoginID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestOpenOrders(String aLoginID);

    /**
     * request open orders by acctid
     *
     * @param aFXCMAcctID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestOpenOrders(long aFXCMAcctID);

    /**
     * request for open positions on all accounts
     *
     * @return requestid or null if there is a problem on request
     */
    String requestOpenPositions();

    /**
     * request for open positions on all accounts with supplied login id
     *
     * @param aLoginID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestOpenPositions(String aLoginID);

    /**
     * request open positions by acctid
     *
     * @param aFXCMAcctID
     *
     * @return requestid or null if there is a problem on request
     */
    String requestOpenPositions(long aFXCMAcctID);

    /**
     * make a request for the last order message sent for the given order id based on the request type
     *
     * @param aOrderID
     * @param aOrdStatusRequestType
     * @param aAccount
     *
     * @return requestid or null if there is a problem on request
     */
    String requestOrderStatus(String aOrderID, OrdStatusRequestType aOrdStatusRequestType, String aAccount);

    /**
     * You must call this after login as part of handshake process otherwise
     * you will not receive messages
     *
     * @return requestid or null if there is a problem on request
     */
    String requestTradingSessionStatus();

    /**
     * Send an aMessage  to the FXCM server, an exception is raised if there is a
     * problem sending the aMessage.
     *
     * @param aMessage
     *
     * @return requestid
     *
     * @throws Exception
     */
    String sendMessage(ITransportable aMessage) throws Exception;

    /**
     * Set the MSG_FLAGS for the current session
     * @param aMsgFlags msgflag
     * @throws SessionNotEstablishedException
     */
    void setMsgFlags(long aMsgFlags) throws SessionNotEstablishedException;
}
