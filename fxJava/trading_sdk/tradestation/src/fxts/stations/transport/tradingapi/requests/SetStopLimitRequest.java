/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/requests/SetStopLimitRequest.java#1 $
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
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Side;
import fxts.stations.transport.BaseRequest;
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
public class SetStopLimitRequest extends BaseRequest implements IRequester {
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
                Position position = Liaison.getInstance().getTradeDesk().getOpenPosition(getTicketID());
                TradeDesk tradeDesk = TradeDesk.getInst();
                TradingServerSession ts = TradingServerSession.getInstance();
                Account account = tradeDesk.getAccounts().getAccount(position.getAccount());
                ISide buySide;
                if (position.getSide() == Side.BUY) {
                    buySide = SideFactory.BUY;
                } else {
                    buySide = SideFactory.SELL;
                }
                if (getLimit() != -1.0 && position.getLimit() == 0) {
                    OrderSingle stopLimit =
                            MessageGenerator.generateStopLimitClose(getLimit(),
                                                                    position.getTicketID(),
                                                                    OrdTypeFactory.LIMIT,
                                                                    account.getAccount(),
                                                                    position.getAmount(),
                                                                    buySide,
                                                                    position.getCurrency(),
                                                                    null,
                                                                    0);
                    ts.send(stopLimit);
                    return LiaisonStatus.READY;
                } else if (getStop() != -1.0 && position.getStop() == 0) {
                    OrderSingle stopLimit =
                            MessageGenerator.generateStopLimitClose(getStop(),
                                                                    position.getTicketID(),
                                                                    OrdTypeFactory.STOP,
                                                                    account.getAccount(),
                                                                    position.getAmount(),
                                                                    buySide,
                                                                    position.getCurrency(),
                                                                    null,
                                                                    getTrailStop());
                    ts.send(stopLimit);
                    return LiaisonStatus.READY;
                } else if (getLimit() != -1.0 && position.getLimit() > 0) {
                    String limitOrderID = position.getLimitOrderID();
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
                } else if (getStop() != -1.0 && position.getStop() > 0) {
                    String stopOrderID = position.getStopOrderID();
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
            return SetStopLimitRequest.this;
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

    private int mTrailStop;
    /**
     * Limit value. If less than 0.0 than it's not used.
     */
    private double mLimit;
    /**
     * Stop-loss value. If less than 0.0 than it's not used.
     */
    private double mStop;
    /**
     * Ticket id.
     */
    private String mTicketID;

    /**
     * Does nothing, because it's not stored in queue.
     */
    public LiaisonStatus doIt() throws LiaisonException {
        mLogger.debug("doIt of this may not called");
        return LiaisonStatus.READY;
    }

    /**
     * Returns limit value. if less than 0.0 than it's not used.
     *
     * @return limit value
     */
    public double getLimit() {
        return mLimit;
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
     * Returns stop-loss value. if less than 0.0 than it's not used.
     *
     * @return stop value
     */
    public double getStop() {
        return mStop;
    }

    /**
     * Returns ticket id.
     *
     * @return ticket id
     */
    public String getTicketID() {
        return mTicketID;
    }

    public int getTrailStop() {
        return mTrailStop;
    }

    public void setTrailStop(int aTrailStop) {
        mTrailStop = aTrailStop;
    }

    /**
     * Sets limit value. If less than 0.0 than it's not used.
     *
     * @param aLimit limit
     */
    public void setLimit(double aLimit) {
        mLimit = aLimit;
    }

    /**
     * Sets stop-loss value. If less than 0.0 than it's not used.
     *
     * @param aStop stop
     */
    public void setStop(double aStop) {
        mStop = aStop;
    }

    /**
     * Sets ticket id.
     *
     * @param aTicketID ticketid
     */
    public void setTicketID(String aTicketID) {
        mTicketID = aTicketID;
    }

    /**
     * Adds itself or other objects implementing IRequester interface to
     * IReqCollection implementation passed as parameter.
     */
    public void toQueue(IReqCollection aQueue) {
        aQueue.add(new SetRequest());
    }
}
