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
 * 9/4/2003 created by USHIK
 * 1/6/2004 Ushik removes method getConnectionName. It is in TradeDesk.
 */
package fxts.stations.transport;

import fxts.stations.transport.tradingapi.LoginRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Vector;

/**
 * Abstract class ALiaison.<br>
 * <br>
 * It's main trade server communicator class.
 * The class is abstract, all actual communication code should be in its subclasses.
 * Liaison should provide following functionality:
 * <ul>
 * <li>Retain trade server connection;</li>
 * <li>Send async requests to trade server;</li>
 * <li>Provide status of the liaison;</li>
 * <li>Provide general business logic information common to the system (such as account currency);</li>
 * <li>Update client business data;</li>
 * </ul>
 * Liaison features:
 * <ul>
 * <li>It's singleton class</li>
 * <li>�Interface of the liaison is general enough so it should be easy
 * customized for use with different trade servers and trading protocols
 * (RapidFX server or zDAS server);</li>
 * <li>All requests (and login/logout commands too) are async so the method
 * implementation should begin process of the request and returns immediately,
 * when request will be completed special listener method will be called,
 * note that it can be called in context of other thread that caller one;</li>
 * <li>Liaison can process the only command at one time,
 * if liaison is busy (process some command) and client
 * sends other request exception can be throw;</li>
 * <li>
 * To decrease dependency from trade server and data protocol,
 * liaison will update trade data on client itself,
 * these trade data can be accessed through ITradeDesk interface;
 * </li>
 * <li>
 * Liaison should retain connection with server,
 * when connection is lost (or other error occurred) liaison should
 * try to restore connection insensibly to user.
 * When some critical error occurred
 * (for example liaison failed to restore connection) it should send
 * notification to caller application;
 * </li>
 * </ul>
 * <br>
 * Creation date (9/4/2003 11:55 AM)
 */
public abstract class ALiaison {
    /**
     * Collection of objects implementing ILiaisonListener interface
     */
    protected final Vector<ILiaisonListener> mListeners = new Vector<ILiaisonListener>();
    protected final Log mLogger = LogFactory.getLog(ALiaison.class);
    /**
     * Status of the liaison.
     */
    protected LiaisonStatus mStatus = LiaisonStatus.DISCONNECTED;
    /**
     * Interface for trade desk object.
     * This object contains business data that can be accessed
     * through this interface.
     */
    protected ITradeDesk mTradeDesk;

    /**
     * Adds liaison listener.
     *
     * @param aListener liaison listener
     */
    public void addLiaisonListener(ILiaisonListener aListener) {
        if (mListeners != null) {
            synchronized (mListeners) {
                mListeners.add(aListener);
            }
        }
    }

    /**
     * Performs cleanup of the liaison. Should be called once before application exit.
     */
    public void cleanup() {
        if (mListeners != null) {
            synchronized (mListeners) {
                mListeners.clear();
            }
        }
    }

    /**
     * Returns account currency.
     *
     * @return account ccy
     */
    public abstract String getAccountCurrency();

    /**
     * @return factory that used to created requests
     */
    public abstract IRequestFactory getRequestFactory();

    /**
     * @return current liaison status.
     */
    public LiaisonStatus getStatus() {
        return mStatus;
    }

    /**
     * @return trade desk.
     */
    public ITradeDesk getTradeDesk() {
        return mTradeDesk;
    }

    /**
     * Sets trade desk instance. This method should be called before any
     * communication method.
     *
     * @param aTradeDesk trade desk
     */
    public void setTradeDesk(ITradeDesk aTradeDesk) {
        mTradeDesk = aTradeDesk;
    }

    /**
     * Performs login. This method is async,
     * it begins procedure and returns immediately.
     * When login will be completed onLoginCompleted or onLoginFailed method of
     * liaison listeners is called.
     *
     * @param aLoginRequest login parameters
     */
    public abstract void login(LoginRequest aLoginRequest);

    /**
     * Performs logout. The method is async, it begins procedure and returns
     * immediately. When logout will be completed onLogoutCompleted method of
     * liaison listeners is called.
     */
    public abstract void logout();

    /**
     * Refresh the Liaison
     */
    public abstract void refresh();

    /**
     * Removes liaison listener.
     *
     * @param aListener liaison listener
     */
    public void removeLiaisonListener(ILiaisonListener aListener) {
        if (mListeners != null) {
            synchronized (mListeners) {
                mListeners.remove(aListener);
            }
        }
    }

    /**
     * Sends request to the server. The method is async, it begins procedure and
     * returns immediately.
     * When request will be completed request sender are notified.
     *
     * @param aRequest request
     *
     * @throws LiaisonException if request is rejected
     */
    public abstract void sendRequest(IRequest aRequest) throws LiaisonException;
}
