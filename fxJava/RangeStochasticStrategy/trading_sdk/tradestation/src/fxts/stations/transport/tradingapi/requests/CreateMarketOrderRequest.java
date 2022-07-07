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
 * 12/1/2004 Andre  ---work in progress---
 * updated to create a market order request in the new system
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.ISide;
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
 * Class CreateMarketOrderRequest.<br>
 * <br>
 * The class is responsible for creating and sending to server object of class
 * com.fxcm.fxtrade.common.datatypes.GSOrder.<br>
 * <br>
 * Creation date (9/5/2003 10:03 AM)
 */
public class CreateMarketOrderRequest extends BaseRequest implements IRequester {
    private String mAccount;
    private long mAmount;
    private int mAtMarket;
    private String mCurrency;
    private String mCustomText;
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
            TradingServerSession ts = TradingServerSession.getInstance();
            Account account = TradeDesk.getInst().getAccounts().getAccount(mAccount);
            Rate rate = tradeDesk.getRate(mCurrency);
            ISide side;
            double price;
            if (mSide == Side.BUY) {
                side = SideFactory.BUY;
                price = rate.getBuyPrice();
            } else {
                side = SideFactory.SELL;
                price = rate.getSellPrice();
            }
            OrderSingle orderSingle = MessageGenerator.generateOpenOrder(rate.getQuoteID(),
                                                                         price,
                                                                         account.getAccount(),
                                                                         mAmount,
                                                                         side,
                                                                         rate.getCurrency(),
                                                                         mCustomText,
                                                                         mAtMarket);
            ts.send(orderSingle);
            return LiaisonStatus.READY;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TradingAPIException(e, "IDS_INVALID_REQUEST_FIELD");
        }
    }

    /**
     * Return account.
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
     * Returns market order currency pair.
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * Sets market order currency pair.
     */
    public void setCurrency(String aCurrency) {
        mCurrency = aCurrency;
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
     * Returns side (buy or sell)
     */
    public Side getSide() {
        return mSide;
    }

    /**
     * Sets side (buy or sell)
     */
    public void setSide(Side aSide) {
        mSide = aSide;
    }

    public void setAtMarket(int aAtMarketPoints) {
        mAtMarket = aAtMarketPoints;
    }

    public void setCustomText(String aCustomText) {
        if (aCustomText != null && !"".equals(aCustomText.trim())) {
            mCustomText = aCustomText;
        }
    }

    /**
     * Adds itself or other objects implementing IRequester interface to
     * IReqCollection implementation passed as parameter.
     */
    public void toQueue(IReqCollection aQueue) {
        aQueue.add(this);
    }
}