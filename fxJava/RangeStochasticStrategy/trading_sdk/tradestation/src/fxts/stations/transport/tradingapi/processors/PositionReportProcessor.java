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
 * Author: Andre Mermegas
 * Created: Nov 13, 2006 11:09:40 AM
 *
 * $History: $
 */
package fxts.stations.transport.tradingapi.processors;

import com.fxcm.fix.NotDefinedException;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.posttrade.ClosedPositionReport;
import com.fxcm.fix.posttrade.PositionReport;
import com.fxcm.messaging.ITransportable;
import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Side;
import fxts.stations.transport.ITradeDesk;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;

public class PositionReportProcessor implements IProcessor {
    private final Log mLogger = LogFactory.getLog(PositionReportProcessor.class);
    private static final DecimalFormat FORMAT = new ThreadSafeNumberFormat().getInstance();

    public void process(ITransportable aTransportable) {
        if (aTransportable instanceof ClosedPositionReport) {
            processClose(aTransportable);
        } else {
            processOpen(aTransportable);
        }
    }

    public void processOpen(ITransportable aTransportable) {
        TradingServerSession aTradingServerSession = TradingServerSession.getInstance();
        PositionReport aPr = (PositionReport) aTransportable;
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        tradeDesk.syncServerTime(aPr.getTransactTime().toDate());
        mLogger.debug("client: inc position report = " + aPr);
        mLogger.debug("********************************************");
        mLogger.debug("Text                = " + aPr.getText());
        mLogger.debug("FXCMPosID           = " + aPr.getFXCMPosID());
        mLogger.debug("RefFXCMPosID        = " + aPr.getFXCMPosIDRef());
        mLogger.debug("PositionQty         = " + FORMAT.format(aPr.getPositionQty().getQty()));
        mLogger.debug("OrderID             = " + aPr.getOrderID());
        mLogger.debug("SecondaryClOrdID    = " + aPr.getSecondaryClOrdID());
        mLogger.debug("SettlePrice         = " + aPr.getSettlPrice());
        mLogger.debug("FXCMPosOpenTime     = " + aPr.getFXCMPosOpenTime().toString());
        mLogger.debug("TransactTime        = " + aPr.getTransactTime().toString());
        mLogger.debug("********************************************");
        if (aPr.getTotalNumPosReports() > 0 || aPr.getPosReqID() == null) {
            if (aPr.getAccount() == null) {
                mLogger.debug("updating interest = " + aPr.getFXCMPosInterest());
                Position position = tradeDesk.getPositions().getPosition(aPr.getFXCMPosID());
                mLogger.debug("old position.getInterest() = " + position.getInterest());
                position.setInterest(aPr.getFXCMPosInterest());
                mLogger.debug("new position.getInterest() = " + position.getInterest());
                return;
            }
            Position position = tradeDesk.getOpenPosition(aPr.getFXCMPosID());
            if (position == null) {
                position = new Position();
            }
            position.setAccountID(String.valueOf(aPr.getParties().getFXCMAcctID()));
            position.setAccount(aPr.getAccount());
            position.setAmount((long) aPr.getPositionQty().getQty());
            try {
                position.setCurrency(aPr.getInstrument().getSymbol());
            } catch (Exception e) {
                e.printStackTrace();
            }
            position.setCurrencyTradable(true);
            position.setOpenPrice(aPr.getSettlPrice());
            position.setTicketID(String.valueOf(aPr.getFXCMPosID()));
            position.setOpenTime(aPr.getFXCMPosOpenTime().toDate());
            position.setCustomText(aPr.getSecondaryClOrdID());
            position.setInterest(aPr.getFXCMPosInterest());
            position.setCommission(aPr.getFXCMPosCommission());
            position.setUsedMargin(aPr.getFXCMUsedMargin());
            position.setBatch(aPr.getTotalNumPosReports() > 0);
            position.setLast(aPr.isLastRptRequested());
            if (aPr.getPositionQty().getSide() == SideFactory.BUY) {
                position.setSide(Side.BUY);
            } else {
                position.setSide(Side.SELL);
            }
            if (tradeDesk.getOpenPosition(String.valueOf(aPr.getFXCMPosID())) != null) {
                tradeDesk.updateOpenPosition(position);
            } else {
                tradeDesk.addOpenPosition(position);
            }
        }
        if (aPr.isLastRptRequested() && aTradingServerSession.getRequestID().equals(aPr.getPosReqID())) {
            aTradingServerSession.doneProcessing();
        }
    }

    public void processClose(ITransportable aTransportable) {
        TradingServerSession aTradingServerSession = TradingServerSession.getInstance();
        ClosedPositionReport aCpr = (ClosedPositionReport) aTransportable;
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        mLogger.debug("client: inc closed position report = " + aCpr);
        mLogger.debug("********************************************");
        mLogger.debug("Text                         = " + aCpr.getText());
        mLogger.debug("PositionQty                  = " + FORMAT.format(aCpr.getPositionQty().getQty()));
        mLogger.debug("FXCMPosID                    = " + aCpr.getFXCMPosID());
        mLogger.debug("FXCMPosIDRef                 = " + aCpr.getFXCMPosIDRef());
        mLogger.debug("OrderID                      = " + aCpr.getOrderID());
        mLogger.debug("FXCMCloseOrderID             = " + aCpr.getFXCMCloseOrderID());
        mLogger.debug("FXCMCloseClOrdID             = " + aCpr.getFXCMCloseClOrdID());
        mLogger.debug("SecondaryClOrdID             = " + aCpr.getSecondaryClOrdID());
        mLogger.debug("FXCMCloseSecondaryClOrdID    = " + aCpr.getFXCMCloseSecondaryClOrdID());
        mLogger.debug("SettlPrice                   = " + aCpr.getSettlPrice());
        mLogger.debug("FXCMCloseSettlPrice          = " + aCpr.getFXCMCloseSettlPrice());
        mLogger.debug("TransactTime                 = " + aCpr.getTransactTime().toString());
        mLogger.debug("FXCMPosOpenTime              = " + aCpr.getFXCMPosOpenTime().toString());
        mLogger.debug("FXCMPosCloseTime             = " + aCpr.getFXCMPosCloseTime().toString());
        mLogger.debug("********************************************");
        tradeDesk.removeOpenPosition(String.valueOf(aCpr.getFXCMPosID()));
        if (aCpr.getTotalNumPosReports() > 0 || aCpr.getPosReqID() == null) {
            Position position = new Position();
            position.setAccountID(String.valueOf(aCpr.getParties().getFXCMAcctID()));
            position.setAccount(aCpr.getAccount());
            position.setAmount((long) aCpr.getPositionQty().getQty());
            try {
                position.setCurrency(aCpr.getInstrument().getSymbol());
            } catch (NotDefinedException e) {
                e.printStackTrace();
            }
            position.setCurrencyTradable(true);
            position.setOpenPrice(aCpr.getSettlPrice());
            position.setClosePrice(aCpr.getFXCMCloseSettlPrice());
            position.setGrossPnL(aCpr.getFXCMPosClosePNL());
            position.setNetPnL(aCpr.getFXCMPosClosePNL() + aCpr.getFXCMPosCommission() + aCpr.getFXCMPosInterest());
            position.setTicketID(String.valueOf(aCpr.getFXCMPosID()));
            position.setOpenTime(aCpr.getFXCMPosOpenTime().toDate());
            position.setCloseTime(aCpr.getFXCMPosCloseTime().toDate());
            position.setInterest(aCpr.getFXCMPosInterest());
            position.setUsedMargin(aCpr.getFXCMUsedMargin());
            position.setCommission(aCpr.getFXCMPosCommission());
            position.setCustomText(aCpr.getSecondaryClOrdID());
            position.setCustomText2(aCpr.getFXCMCloseSecondaryClOrdID());
            if (aCpr.getPositionQty().getSide() == SideFactory.BUY) {
                position.setSide(Side.BUY);
            } else {
                position.setSide(Side.SELL);
            }
            tradeDesk.addClosedPosition(position);
        }
        if (aCpr.isLastRptRequested() && aTradingServerSession.getRequestID().equals(aCpr.getPosReqID())) {
            aTradingServerSession.doneProcessing();
        }
    }
}
