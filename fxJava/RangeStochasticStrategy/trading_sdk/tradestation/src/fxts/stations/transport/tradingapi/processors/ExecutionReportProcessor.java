/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/processors/ExecutionReportProcessor.java#7 $
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
 * Author: Andre Mermegas
 * Created: Nov 13, 2006 10:41:51 AM
 *
 * $History: $
 */
package fxts.stations.transport.tradingapi.processors;

import com.fxcm.external.api.util.MessageAnalyzer;
import com.fxcm.fix.FXCMOrdStatusFactory;
import com.fxcm.fix.FXCMOrdTypeFactory;
import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.NotDefinedException;
import com.fxcm.fix.OrdStatusFactory;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.trade.ExecutionReport;
import com.fxcm.messaging.ITransportable;
import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.datatypes.Message;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.dialogs.MessageDialog;
import fxts.stations.transport.ITradeDesk;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.transport.tradingapi.resources.OraCodeFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public class ExecutionReportProcessor implements IProcessor {
    private static final DecimalFormat FORMAT = new ThreadSafeNumberFormat().getInstance();
    private final Log mLogger;

    public ExecutionReportProcessor() {
        mLogger = LogFactory.getLog(ExecutionReportProcessor.class);
    }

    private void doDelete(ExecutionReport aExeRpt) {
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        tradeDesk.syncServerTime(aExeRpt.getTransactTime().toDate());
        Map<String, Order> orderMap = TradingServerSession.getInstance().getStopLimitOrderMap();
        Order order = orderMap.get(aExeRpt.getOrderID());
        if (order == null) {
            tradeDesk.removeOrder(aExeRpt.getOrderID());
        } else {
            Position openPosition = tradeDesk.getOpenPosition(order.getTradeId());
            Order openOrder = tradeDesk.getOrderByTradeId(order.getTradeId());
            if (openPosition != null) {
                Position position = tradeDesk.getOpenPosition(order.getTradeId());
                if (order.isLimit()) {
                    position.setLimit(0);
                } else if (order.isStop() || order.isTrailingStop()) {
                    position.setStop(0);
                    position.setTrailStop(0);
                }
                tradeDesk.updateOpenPosition(position);
            } else if (openOrder != null) {
                openOrder.setCustomText(order.getCustomText());
                if (order.isLimit()) {
                    openOrder.setLimitRate(0);
                } else if (order.isStop() || order.isTrailingStop()) {
                    openOrder.setStopRate(0);
                    openOrder.setTrailStop(0);
                }
                tradeDesk.updateOrder(openOrder);
            }
        }
    }

    private void doNewOrder(ExecutionReport aExeRpt) {
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        Map<String, Order> orderMap = TradingServerSession.getInstance().getStopLimitOrderMap();
        if (aExeRpt.getTotNumReports() > 0 || aExeRpt.getMassStatusReqID() == null) {
            Order newOrder = fillOrder(aExeRpt);
            // go through order map to see if a stop / limit came before the entry order in
            // processing stream and set it.
            for (Order order : orderMap.values()) {
                if (order.getTradeId() != null
                    && order.getTradeId().equals(aExeRpt.getFXCMPosID())) {
                    if (order.isLimit()) {
                        newOrder.setLimitRate(order.getOrderRate());
                        newOrder.setLimitOrderID(order.getOrderID());
                    } else if (order.isStop()) {
                        newOrder.setStopRate(order.getOrderRate());
                        newOrder.setStopOrderID(order.getOrderID());
                    }
                }
            }
            tradeDesk.addOrder(newOrder);
        }
    }

    private boolean doReject(ExecutionReport aExeRpt) {
        if (isDisconnect(aExeRpt)) {
            JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                          aExeRpt.getFXCMErrorDetails(),
                                          "Problem with your order..please reconnect.",
                                          JOptionPane.ERROR_MESSAGE);
            TradingServerSession.getInstance().logout();
        } else {
            if (aExeRpt.getFXCMOrdStatus() != FXCMOrdStatusFactory.REQUOTED) {
                final String fxcmErrorDetails = aExeRpt.getFXCMErrorDetails();
                mLogger.debug("fxcmErrorDetails = " + fxcmErrorDetails);
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        Message m = new Message(new Date(),
                                                "FXCM",
                                                "Problem with your order..",
                                                OraCodeFactory.toMessage(fxcmErrorDetails));
                        MessageDialog dialog = new MessageDialog(TradeApp.getInst().getMainFrame());
                        Liaison.getInstance().getTradeDesk().addMessage(m);
                        dialog.setMessage(m);
                        dialog.showModal();
                    }
                });
                Liaison.getInstance().getTradeDesk().removeOrder(aExeRpt.getOrderID());
            }
        }
        return false;
    }

    private void doStopLimit(ExecutionReport aExeRpt) {
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        Map<String, Order> orderMap = TradingServerSession.getInstance().getStopLimitOrderMap();
        Position openPosition = tradeDesk.getOpenPosition(String.valueOf(aExeRpt.getFXCMPosID()));
        Order openOrder = tradeDesk.getOrderByTradeId(String.valueOf(aExeRpt.getFXCMPosID()));
        orderMap.put(aExeRpt.getOrderID(), fillOrder(aExeRpt));
        if (openPosition != null) {
            //we got a stop/limit order
            if (MessageAnalyzer.isLimitOrder(aExeRpt)) {
                openPosition.setLimit(aExeRpt.getPrice());
                openPosition.setLimitOrderID(aExeRpt.getOrderID());
            } else if (MessageAnalyzer.isStopOrder(aExeRpt)) {
                openPosition.setStop(aExeRpt.getStopPx());
                openPosition.setStopOrderID(aExeRpt.getOrderID());
                openPosition.setTrailStop(0);
                openPosition.setStopMove(0);
            } else if (MessageAnalyzer.isTrailingStopCloseOrder(aExeRpt)) {
                if (aExeRpt.getFXCMOrdType() == FXCMOrdTypeFactory.TRAILING_STOP) {
                    openPosition.setTrailingRate(aExeRpt.getLastPx());
                    if (aExeRpt.getPegInstructions().getPegOffsetValue() > 0) {
                        openPosition.setStop(aExeRpt.getPegInstructions().getPegOffsetValue());
                    } else {
                        //handle case where it doesnt transition from trail stop back to regular stop
                        openPosition.setStop(aExeRpt.getLastPx());
                    }
                    openPosition.setTrailStop(aExeRpt.getPegInstructions().getFXCMPegFluctuatePts());
                    openPosition.setStopMove(aExeRpt.getPegInstructions().getFXCMPegFluctuatePts());
                    // initialize the stopmove to static trailing stop number
                    openPosition.setStopOrderID(aExeRpt.getOrderID());
                } else if (aExeRpt.getFXCMOrdType() == FXCMOrdTypeFactory.TRAILING_LIMIT) {
                    openPosition.setLimit(aExeRpt.getPrice());
                    openPosition.setLimitOrderID(aExeRpt.getOrderID());
                }
            }
            tradeDesk.updateOpenPosition(openPosition);
        } else if (openOrder != null) {
            if (MessageAnalyzer.isLimitOrder(aExeRpt)) {
                if (aExeRpt.getPrice() == 0) {
                    openOrder.setLimitRate(aExeRpt.getPegInstructions().getPegOffsetValue());
                } else {
                    openOrder.setLimitRate(aExeRpt.getPrice());
                }
                openOrder.setLimitOrderID(aExeRpt.getOrderID());
            } else if (MessageAnalyzer.isStopOrder(aExeRpt)) {
                if (aExeRpt.getPrice() == 0) {
                    openOrder.setStopRate(aExeRpt.getPegInstructions().getPegOffsetValue());
                } else {
                    openOrder.setStopRate(aExeRpt.getPrice());
                }
                openOrder.setStopOrderID(aExeRpt.getOrderID());
            } else if (MessageAnalyzer.isTrailingStopCloseOrder(aExeRpt)) {
                openOrder.setOfferRate(aExeRpt.getLastPx());
                if (aExeRpt.getPegInstructions().getPegOffsetValue() > 0) {
                    openOrder.setStopRate(aExeRpt.getPegInstructions().getPegOffsetValue());
                } else {
                    //handle case where it doesnt transition from trail stop back to regular stop
                    openOrder.setStopRate(aExeRpt.getPrice());
                }
                openOrder.setTrailStop(aExeRpt.getPegInstructions().getFXCMPegFluctuatePts());
                openOrder.setStopOrderID(aExeRpt.getOrderID());
            }
            tradeDesk.updateOrder(openOrder);
        }
    }

    private void doUpdateOrder(ExecutionReport aExeRpt) {
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        Order openOrder = tradeDesk.getOrderByTradeId(String.valueOf(aExeRpt.getFXCMPosID()));
        if (openOrder == null) {
            openOrder = tradeDesk.getOrder(aExeRpt.getOrderID());
        }
        if (openOrder != null) {
            openOrder.setCustomText(aExeRpt.getSecondaryClOrdID());
            openOrder.setOrderRate(aExeRpt.getPrice());
            openOrder.setStatus(aExeRpt.getFXCMOrdStatus().getLabel());
            openOrder.setType(aExeRpt.getFXCMOrdType().getCode());
            openOrder.setAmount((long) aExeRpt.getOrderQty());
            openOrder.setTime(aExeRpt.getTransactTime().toDate());
            tradeDesk.updateOrder(openOrder);
        }
    }

    private Order fillOrder(ExecutionReport aExeRpt) {
        Order order = new Order();
        order.setCustomText(aExeRpt.getSecondaryClOrdID());
        order.setAccountID(String.valueOf(aExeRpt.getParties().getFXCMAcctID()));
        order.setAccount(aExeRpt.getAccount());
        order.setAmount((long) aExeRpt.getOrderQty());
        try {
            order.setCurrency(aExeRpt.getInstrument().getSymbol());
        } catch (NotDefinedException e) {
            e.printStackTrace();
        }
        order.setCurrencyTradable(true);
        order.setOrderID(aExeRpt.getOrderID());
        order.setOrdType(aExeRpt.getOrdType().getCode());
        order.setStop(MessageAnalyzer.isStopOrder(aExeRpt));
        order.setLimit(MessageAnalyzer.isLimitOrder(aExeRpt));
        order.setTrailingStop(MessageAnalyzer.isTrailingStopCloseOrder(aExeRpt));
        order.setTrailStop(aExeRpt.getPegInstructions().getFXCMPegFluctuatePts());
        if (aExeRpt.getSide() == SideFactory.BUY) {
            order.setSide(Side.BUY);
        } else {
            order.setSide(Side.SELL);
        }
        order.setOrderRate(aExeRpt.getPrice());
        order.setStatus(aExeRpt.getFXCMOrdStatus().getLabel());
        order.setType(aExeRpt.getFXCMOrdType().getCode());
        order.setStage('O');
        order.setTime(aExeRpt.getTransactTime().toDate());
        order.setTradeId(String.valueOf(aExeRpt.getFXCMPosID()));
        return order;
    }

    private boolean isAnyStopLimit(ExecutionReport aExeRpt) {
        return MessageAnalyzer.isStopLimitCloseOrder(aExeRpt) || MessageAnalyzer.isTrailingStopCloseOrder(aExeRpt);
    }

    private boolean isDelete(ExecutionReport aExeRpt) {
        return aExeRpt.getFXCMOrdStatus() != null && aExeRpt.getFXCMOrdStatus().isDeleted();
    }

    private boolean isDisconnect(ExecutionReport aExeRpt) {
        return aExeRpt.getFXCMErrorDetails() != null
               && aExeRpt.getFXCMErrorDetails().indexOf("Connection lost, please close this window and login") > 0;
    }

    private boolean isNewOrder(ExecutionReport aExeRpt) {
        return Liaison.getInstance().getTradeDesk().getOrder(aExeRpt.getOrderID()) == null;
    }

    private boolean isReject(ExecutionReport aExeRpt) {
        return aExeRpt.getOrdStatus() == OrdStatusFactory.REJECTED
               && IFixDefs.FXCMREQUESTREJECTREASON_DATA_NOT_FOUND != aExeRpt.getFXCMRequestRejectReason();
    }

    private boolean isUpdateOrder(ExecutionReport aExeRpt) {
        return Liaison.getInstance().getTradeDesk().getOrder(aExeRpt.getOrderID()) != null;
    }

    public void process(ITransportable aTransportable) {
        ExecutionReport exe = (ExecutionReport) aTransportable;
        mLogger.debug("client: inc execution report = " + exe);
        mLogger.debug("********************************************");
        mLogger.debug(exe.getRequestID() + " :: " + exe.getOrderID());
        mLogger.debug("OrderID                = " + exe.getOrderID());
        mLogger.debug("ClOrdID                = " + exe.getClOrdID());
        mLogger.debug("OrigClOrdID            = " + exe.getOrigClOrdID());
        mLogger.debug("ListID                 = " + exe.getListID());
        mLogger.debug("ExpireTime             = " + exe.getExpireTime());
        mLogger.debug("EffectiveTime          = " + exe.getEffectiveTime());
        mLogger.debug("OrdType                = " + exe.getOrdType());
        mLogger.debug("Side                   = " + exe.getSide());
        mLogger.debug("TimeInforce            = " + exe.getTimeInForce());
        mLogger.debug("ExpireTime             = " + exe.getExpireTime());
        mLogger.debug("FXCMOrdType            = " + exe.getFXCMOrdType());
        mLogger.debug("ContingencyID          = " + exe.getFXCMContingencyID());
        mLogger.debug("SecondaryClOrdID       = " + exe.getSecondaryClOrdID());
        mLogger.debug("FXCMPosID              = " + exe.getFXCMPosID());
        mLogger.debug("Price                  = " + exe.getPrice());
        mLogger.debug("StopPx                 = " + exe.getStopPx());
        mLogger.debug("TransactTime           = " + exe.getTransactTime());
        mLogger.debug("ExecType               = " + exe.getExecType());
        mLogger.debug("FXCMOrdStatus          = " + exe.getFXCMOrdStatus());
        mLogger.debug("OrdStatus              = " + exe.getOrdStatus());
        mLogger.debug("OrderQty               = " + FORMAT.format(exe.getOrderQty()));
        mLogger.debug("CumQty                 = " + FORMAT.format(exe.getCumQty()));
        mLogger.debug("LastQty                = " + FORMAT.format(exe.getLastQty()));
        mLogger.debug("LeavesQty              = " + FORMAT.format(exe.getLeavesQty()));
        mLogger.debug("********************************************");
        if (isReject(exe)) {
            doReject(exe);
        } else if (isDelete(exe)) {
            doDelete(exe);
        } else if (isAnyStopLimit(exe)) {
            doStopLimit(exe);
        } else if (isUpdateOrder(exe)) {
            doUpdateOrder(exe);
        } else if (isNewOrder(exe)) {
            doNewOrder(exe);
        }
        if (requiresNotification(exe)) {
            TradingServerSession.getInstance().doneProcessing();
        }
    }

    private boolean requiresNotification(ExecutionReport aExeRpt) {
        return aExeRpt.isLastRptRequested()
               && TradingServerSession.getInstance().getRequestID().equals(aExeRpt.getMassStatusReqID());
    }
}
