/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/requests/CreateEntryOrderRequest.java#2 $
 *
 * Copyright (c) 2008 FXCM, LLC.
 * 32 Old Slip, New York NY, 10005 USA
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
 * 9/10/2003 created by Ushik
 * 12/8/2004    Andre Mermegas  -- updated to handle entry order requests in the FCXM Msg format
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.IOrdType;
import com.fxcm.fix.ISide;
import com.fxcm.fix.OrdTypeFactory;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.trade.OrderSingle;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.transport.BaseRequest;
import fxts.stations.transport.IReqCollection;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequester;
import fxts.stations.transport.ITradeDesk;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingAPIException;
import fxts.stations.transport.tradingapi.TradingServerSession;

/**
 * Class CreateEntryOrderRequest.<br>
 * <br>
 * It is responsible for creating and sending to server object of class
 * com.fxcm.fxtrade.common.datatypes.EntryNewOrder.<br>
 * <br>
 * Creation date (9/10/2003 2:27 PM)
 */
public class CreateEntryOrderRequest extends BaseRequest implements IRequester {
    private String mAccount;
    private long mAmount;
    private String mCurrency;
    private String mCustomText;
    private double mRate;
    private Side mSide;

    /**
     * Executes the request
     *
     * @return Status Ready if successful null else
     */
    public LiaisonStatus doIt() throws LiaisonException {
        Liaison liaison = Liaison.getInstance();
        if (liaison.getSessionID() == null) {
            throw new TradingAPIException(null, "IDS_SESSION_ISNOT_LOGGED");
        }
        try {
            ITradeDesk tradeDesk = liaison.getTradeDesk();
            Rate rate = tradeDesk.getRate(mCurrency);
            TradingServerSession ts = TradingServerSession.getInstance();
            Account account = TradeDesk.getInst().getAccounts().getAccount(mAccount);
            IOrdType entryType = null;
            ISide side = null;
            if (mSide == Side.BUY) {
                side = SideFactory.BUY;
                if (mRate - rate.getBuyPrice() >= 0) {
                    entryType = OrdTypeFactory.STOP;
                } else {
                    entryType = OrdTypeFactory.LIMIT;
                }
            } else if (mSide == Side.SELL) {
                side = SideFactory.SELL;
                if (mRate - rate.getSellPrice() >= 0) {
                    entryType = OrdTypeFactory.LIMIT;
                } else {
                    entryType = OrdTypeFactory.STOP;
                }
            }
            OrderSingle entryOrder = MessageGenerator.generateStopLimitEntry(mRate,
                                                                             entryType,
                                                                             account.getAccount(),
                                                                             mAmount,
                                                                             side,
                                                                             rate.getCurrency(),
                                                                             mCustomText);
            ts.send(entryOrder);
            return LiaisonStatus.READY;
        } catch (Exception e) {
            e.printStackTrace();
            // it can be in case of position being closed by server in
            // asynchronous process
            throw new TradingAPIException(e, "IDS_INVALID_REQUEST_FIELD");
        }
    }

    /**
     * Returns account.
     */
    public String getAccount() {
        return mAccount;
    }

    /**
     * Sets account.
     */
    public void setAccount(String aAccount) {
        mAccount = aAccount;
    }

    /**
     * Returns contract size.
     */
    public long getAmount() {
        return mAmount;
    }

    /**
     * Sets contract size.
     */
    public void setAmount(long aAmount) {
        mAmount = aAmount;
    }

    /**
     * Returns currency pair.
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * Sets currency pair.
     */
    public void setCurrency(String aCurrency) {
        mCurrency = aCurrency;
    }

    /**
     * Returns entry order rate.
     */
    public double getRate() {
        return mRate;
    }

    /**
     * Sets entry order rate.
     */
    public void setRate(double aRate) {
        mRate = aRate;
    }

    /**
     * Returns parent batch request
     */
    public IRequest getRequest() {
        return this;
    }

    /**
     * Returns next request of batch request
     */
    public IRequester getSibling() {
        return null;
    }

    /**
     * Returns contract side (buy or sell).
     */
    public Side getSide() {
        return mSide;
    }

    /**
     * Sets contract side (buy or sell).
     */
    public void setSide(Side aSide) {
        mSide = aSide;
    }

    public void setCustomText(String aCustomText) {
        mCustomText = aCustomText;
    }

    /**
     * Adds itself or other objects implementing IRequester interface to
     * IReqCollection implementation passed as parameter.
     */
    public void toQueue(IReqCollection aQueue) {
        aQueue.add(this);
    }
}
