/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/requests/RemoveEntryOrderRequest.java#1 $
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
 * 12/10/2004   Andre Mermegas  -- updated to use new msg system
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.fix.Instrument;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.trade.OrderCancelRequest;
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
 * Class RemoveEntryOrderRequest.<br>
 * <br>
 * It is responsible for creating and sending to server object of class
 * com.fxcm.fxtrade.common.datatypes.EntryRemoveOrder:<br>
 * <br>
 * Creation date (9/10/2003 3:38 PM)
 */
public class RemoveEntryOrderRequest extends BaseRequest implements IRequester {
    /**
     * Order id.
     */
    private String mOrderID;

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
            TradingServerSession ts = TradingServerSession.getInstance();
            OrderCancelRequest ocr = new OrderCancelRequest();
            mLogger.debug("client: deleting the entry order");
            ocr.setOrderID(order.getOrderID());
            ocr.setOrderQty(order.getAmount());
            ocr.setSide(order.getSide() == Side.BUY ? SideFactory.BUY : SideFactory.SELL);
            ocr.setAccount(order.getAccount());
            ocr.setInstrument(new Instrument(order.getCurrency()));
            ts.send(ocr);
            return LiaisonStatus.READY;
        } catch (Exception e) {
            e.printStackTrace();
            // it can be in case of order being removed by server in
            // asynchronous process
            throw new TradingAPIException(e, "IDS_INVALID_REQUEST_FIELD");
        }
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
     * Adds itself or other objects implementing IRequester interface to
     * IReqCollection implementation passed as parameter.
     */
    public void toQueue(IReqCollection aQueue) {
        aQueue.add(this);
    }
}
