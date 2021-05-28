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
 * 1/6/2004 USHIK Remove setting property msConnectionName.
 *           Set this property in TradeDesk after login instead
 */
package fxts.stations.transport.tradingapi;

import com.fxcm.fix.ITradSesStatus;
import com.fxcm.fix.TradSesStatusFactory;
import fxts.stations.transport.ALiaisonImpl;
import fxts.stations.transport.IRequestFactory;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;

import java.net.InetAddress;

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
public abstract class BaseLiaison extends ALiaisonImpl {
    /**
     * Flag that disconnect is requeried
     */
    protected boolean mDisconnectRequeried;
    /**
     * Reference to disconnect thread
     */
    protected DisconnectedThread mDisconnectedThread;
    /**
     * Reference to login parameters for reconnection
     */
    protected LoginRequest mLoginRequest;
    /**
     * Reference to singleton of RequestFactory
     */
    protected IRequestFactory mRequestFactory;
    private String mSessionID;

    /**
     * Cleans up all allocated resources
     */
    public void cleanup() {
        mRequestFactory = null;
        if (getStatus() != LiaisonStatus.DISCONNECTED) {
            if (getStatus() == LiaisonStatus.DISCONNECTING && mDisconnectedThread != null) {
                try {
                    mDisconnectedThread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                setStatus(LiaisonStatus.DISCONNECTING);
                disconnect();
            }
        }
        mRequestFactory = null;
        super.cleanup();
    }

    /**
     * Does disconnection
     */
    public void disconnect() {
        mDisconnectRequeried = true;
        stop();
        dispatchLogoutCompleted();
        if (mSessionID != null) {
            mSessionID = null;
        }

        mDisconnectRequeried = false;
        setStatus(LiaisonStatus.DISCONNECTED);
        mDisconnectedThread = null;
    }

    /**
     * Returns account currency.
     */
    public String getAccountCurrency() {
        return TradingServerSession.getInstance().getParameterValue("BASE_CRNCY");
    }

    protected InetAddress getLocalHost() throws LiaisonException {
        try {
            return InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
            throw new TradingAPIException(e, "IDS_UNKNOWN_HOST_LOCALHOST");
        }
    }

    /**
     * Returns mLoginParameters value
     */
    public LoginRequest getLoginRequest() {
        return mLoginRequest;
    }

    /**
     * Initializes mRequestFactory by newly created instance of RequestFactory
     * at once, returns mRequestFactory always.
     */
    public IRequestFactory getRequestFactory() {
        if (mRequestFactory == null) {
            mRequestFactory = new RequestFactory();
        }
        return mRequestFactory;
    }

    /**
     * Returns mSessionObject value
     */
    public String getSessionID() {
        return mSessionID;
    }

    /**
     * Returns flag that the market is closed
     */
    public synchronized boolean isMarketClosed() {
        ITradSesStatus tradSesStatus = TradingServerSession.getInstance().getTradingSessionStatus().getTradSesStatus();
        return tradSesStatus == TradSesStatusFactory.CLOSED;
    }

    /**
     * Sets mSessionObject value.
     *
     * @param aSessionID
     *
     * @throws TradingAPIException if FXTradeSession is invalid
     */
    public void setSessionID(String aSessionID) {
        mSessionID = aSessionID;
    }

    /**
     * Disconnection thread.<br>
     * <br>
     * .<br>
     * <br>
     */
    protected class DisconnectedThread extends Thread {
        public void run() {
            disconnect();
            mSessionID = null;
            mLoginRequest = null;
        }
    }
}