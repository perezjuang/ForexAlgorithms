/*
 * $Header:$
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
 * 12/8/2004    Andre Mermegas  -- updated to send out FXCMRequest DasMessages
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.ISide;
import com.fxcm.fix.OrdTypeFactory;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.trade.OrderCancelReplaceRequest;
import com.fxcm.fix.trade.OrderSingle;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Side;
import fxts.stations.transport.IReqCollection;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequester;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingAPIException;
import fxts.stations.transport.tradingapi.TradingServerSession;

/**
 * Class SetStopLimitRequest.<br>
 * <br>
 * It is responsible for creating and sending to server instances of one or
 * both extension of the class com.fxcm.fxtrade.common.datatypes.StopLimitOrder:
 * StopOrder or/and LimitOrder:.<br>
 * <br>
 * Creation date (9/10/2003 12:36 PM)
 */
public class SetStopLimitOrderRequest extends SetStopLimitRequest {
    private class SetRequest implements IRequester {
        private SetRequest mNext;

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
                Order order = Liaison.getInstance().getTradeDesk().getOrder(getOrderID());
                TradeDesk tradeDesk = TradeDesk.getInst();
                TradingServerSession ts = TradingServerSession.getInstance();
                Account account = tradeDesk.getAccounts().getAccount(order.getAccount());
                ISide buySide;
                if (order.getSide() == Side.BUY) {
                    buySide = SideFactory.BUY;
                } else {
                    buySide = SideFactory.SELL;
                }
                if (getLimit() != -1.0 && order.getLimitRate() == 0) {
                    OrderSingle stopLimit = MessageGenerator.generateStopLimitClose(getLimit(),
                                                                                    order.getTradeId(),
                                                                                    OrdTypeFactory.LIMIT,
                                                                                    account.getAccount(),
                                                                                    order.getAmount(),
                                                                                    buySide,
                                                                                    order.getCurrency(),
                                                                                    null,
                                                                                    0);
                    ts.send(stopLimit);
                    return LiaisonStatus.READY;
                } else if (getStop() != -1.0 && order.getStopRate() == 0) {
                    OrderSingle stopLimit =
                            MessageGenerator.generateStopLimitClose(getStop(),
                                                                    order.getTradeId(),
                                                                    OrdTypeFactory.STOP,
                                                                    account.getAccount(),
                                                                    order.getAmount(),
                                                                    buySide,
                                                                    order.getCurrency(),
                                                                    null,
                                                                    getTrailStop());
                    ts.send(stopLimit);
                    return LiaisonStatus.READY;
                } else if (getLimit() != -1.0 && order.getLimitRate() > 0) {
                    String limitOrderID = order.getLimitOrderID();
                    OrderCancelReplaceRequest stopLimit =
                            MessageGenerator.generateOrderReplaceRequest(null,
                                                                         limitOrderID,
                                                                         buySide,
                                                                         OrdTypeFactory.LIMIT,
                                                                         getLimit(),
                                                                         0,
                                                                         account.getAccount());
                    ts.send(stopLimit);
                    return LiaisonStatus.READY;
                } else if (getStop() != -1.0 && order.getStopRate() > 0) {
                    String stopOrderID = order.getStopOrderID();
                    OrderCancelReplaceRequest stopLimit =
                            MessageGenerator.generateOrderReplaceRequest(null,
                                                                         stopOrderID,
                                                                         buySide,
                                                                         OrdTypeFactory.STOP,
                                                                         getStop(),
                                                                         getTrailStop(),
                                                                         account.getAccount());
                    ts.send(stopLimit);
                    return LiaisonStatus.READY;
                } else {
                    return LiaisonStatus.READY;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // it can be in case of position being closed by server in
                // asynchronous process
                throw new TradingAPIException(e, "IDS_INVALID_REQUEST_FIELD");
            }
        }

        /**
         * Returns parent batch request
         */
        public IRequest getRequest() {
            return SetStopLimitOrderRequest.this;
        }

        /**
         * Returns next request of batch request
         */
        public IRequester getSibling() {
            return mNext;
        }

        public void setLink(SetRequest aNext) {
            mNext = aNext;
        }

        /**
         * does nothing because is called never.
         */
        public void toQueue(IReqCollection aQueue) {
        }
    }

    private String mOrderID;

    public String getOrderID() {
        return mOrderID;
    }

    public void setOrderID(String aOrderID) {
        mOrderID = aOrderID;
    }

    /**
     * Adds itself or other objects implementing IRequester interface to
     * IReqCollection implementation passed as parameter.
     */
    @Override
    public void toQueue(IReqCollection aQueue) {
        aQueue.add(new SetRequest());
    }
}
