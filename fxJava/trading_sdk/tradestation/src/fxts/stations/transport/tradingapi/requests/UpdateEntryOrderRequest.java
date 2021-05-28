/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/requests/UpdateEntryOrderRequest.java#2 $
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
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.ISide;
import com.fxcm.fix.Instrument;
import com.fxcm.fix.OrdTypeFactory;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.trade.OrderCancelReplaceRequest;
import fxts.stations.datatypes.Order;
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
 * Class UpdateEntryOrderRequest.<br>
 * <br>
 * It is responsible for creating and sending to server object of class
 * com.fxcm.fxtrade.common.datatypes.EntryResetOrder:<br>
 * <br>
 * Creation date (9/10/2003 3:35 PM)
 */
public class UpdateEntryOrderRequest extends BaseRequest implements IRequester {
    /**
     * Contract size. If less or equals to 0 than it's not updated.
     */
    private long mAmount;
    private String mCustomText;
    /**
     * Order id.
     */
    private String mOrderID;
    /**
     * Order rate. If less or equals to 0.0 than it's not updated.
     */
    private double mRate;

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
            Order order = tradeDesk.getOrder(mOrderID);
            ISide side;
            if (order.getSide() == Side.BUY) {
                side = SideFactory.BUY;
            } else {
                side = SideFactory.SELL;
            }
            TradingServerSession ts = TradingServerSession.getInstance();
            OrderCancelReplaceRequest os =
                    MessageGenerator.generateOrderReplaceRequest(mCustomText,
                                                                 mOrderID,
                                                                 side,
                                                                 OrdTypeFactory.toCode(order.getOrdType()),
                                                                 mRate,
                                                                 0,
                                                                 order.getAccount());
            os.setInstrument(new Instrument(order.getCurrency()));
            os.setLeavesQty(order.getAmount());
            ts.send(os);
            return LiaisonStatus.READY;
        } catch (Exception e) {
            e.printStackTrace();
            // it can be in case of position being closed by server in
            // asynchronous process
            throw new TradingAPIException(e, "IDS_INVALID_REQUEST_FIELD");
        }
    }

    /**
     * Returns contract size. if less or equals to 0 than it's not updated.
     */
    public long getAmount() {
        return mAmount;
    }

    /**
     * Sets contract size. If less or equals to 0 than it's not updated.
     */
    public void setAmount(long aAmount) {
        mAmount = aAmount;
    }

    /**
     * Returns order id.
     */
    public String getOrderID() {
        return mOrderID;
    }

    /**
     * Sets order id.
     */
    public void setOrderID(String aOrderID) {
        mOrderID = aOrderID;
    }

    /**
     * Returns order rate. if less or equals to 0.0 than it's not updated.
     */
    public double getRate() {
        return mRate;
    }

    /**
     * Sets order rate. If less or equals to 0.0 than it's not updated.
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
