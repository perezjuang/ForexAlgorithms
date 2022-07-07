/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/requests/CloseMultiplePositionRequest.java#1 $
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
 * 4/26/2004 Ushik fixed bug of Wrong Side passing and Account Balance is not being changed
 * 4/26/2004 Elana to fully fix bug of Wrong Side, need to use the Bank's perspective on the price.
 * So if the client sends a close order on a BUY position, need to use BUY side, SELL price.
 * On a SELL position, need to use SELL side, BUY price.
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.ISide;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.trade.OrderList;
import com.fxcm.fix.trade.OrderSingle;
import fxts.stations.datatypes.Position;
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
 * Class ClosePositionRequest.<br>
 * <br>
 * It is responsible for creating and sending to server object of class com.fxcm.fxtrade.common.datatypes.GSOrder
 * with the type of ClosePosition.<br>
 * <br>
 * Creation date (9/10/2003 11:44 AM)
 */
public class CloseMultiplePositionRequest extends BaseRequest implements IRequester {
    private OrderList mOrderList = new OrderList();

    public void addCloseOrder(String aTicketID, double aAmt, String aCustomText, int aAtMarket) {
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        Position position = tradeDesk.getOpenPosition(aTicketID);
        Rate rate = tradeDesk.getRate(position.getCurrency());
        ISide side;
        double price;
        if (position.getSide() == Side.BUY) {
            side = SideFactory.SELL;
            price = rate.getSellPrice();
        } else {
            side = SideFactory.BUY;
            price = rate.getBuyPrice();
        }
        OrderSingle delOrder = MessageGenerator.generateCloseOrder(rate.getQuoteID(),
                                                                   price,
                                                                   aTicketID,
                                                                   position.getAccount(),
                                                                   aAmt,
                                                                   side,
                                                                   rate.getCurrency(),
                                                                   aCustomText,
                                                                   aAtMarket);
        mOrderList.addOrder(delOrder);
    }

    public void addMarketOrder(String aTicketID, double aAmt, String aCustomText) {
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        Position position = tradeDesk.getOpenPosition(aTicketID);
        Rate rate = tradeDesk.getRate(position.getCurrency());
        ISide side;
        if (position.getSide() == Side.BUY) {
            side = SideFactory.SELL;
        } else {
            side = SideFactory.BUY;
        }
        OrderSingle delOrder = MessageGenerator.generateCloseMarketOrder(
                aTicketID,
                position.getAccount(),
                aAmt,
                side,
                rate.getCurrency(),
                aCustomText);
        mOrderList.addOrder(delOrder);
    }

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
            OrderSingle[] orders = mOrderList.getOrders();
            //break up into smaller chunks
            int max = 50;
            while (mOrderList.size() > max) {
                OrderList ol = new OrderList();
                for (int i = 0; i < max; i++) {
                    OrderSingle order = orders[i];
                    mOrderList.removeOrder(order);
                    ol.addOrder(order);
                }
                orders = mOrderList.getOrders();
                TradingServerSession.getInstance().send(ol);
            }
            if (mOrderList.size() > 0) {
                TradingServerSession.getInstance().send(mOrderList);
            }
            return LiaisonStatus.READY;
        } catch (Exception e) {
            e.printStackTrace();
            // it can be in case of position being closed by server in
            // asynchronous process
            return LiaisonStatus.READY;
        }
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
