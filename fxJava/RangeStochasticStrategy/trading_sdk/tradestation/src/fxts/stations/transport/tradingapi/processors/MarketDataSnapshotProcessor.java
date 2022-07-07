/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/processors/MarketDataSnapshotProcessor.java#5 $
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
 * Created: Nov 13, 2006 11:11:00 AM
 *
 * $History: $
 * 05/01/2007   Andre Mermegas: added MMR
 */
package fxts.stations.transport.tradingapi.processors;

import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.NotDefinedException;
import com.fxcm.fix.TradingSecurity;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.messaging.ITransportable;
import fxts.stations.datatypes.Rate;
import fxts.stations.transport.ITradeDesk;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;

/**
 *
 */
public class MarketDataSnapshotProcessor implements IProcessor {
    public void process(ITransportable aTransportable) {
        TradingServerSession session = TradingServerSession.getInstance();
        MarketDataSnapshot aMds = (MarketDataSnapshot) aTransportable;
        session.adjustStatus();
        if (aMds.getRequestID() != null && aMds.getRequestID().equals(session.getRequestID())) {
            if (aMds.getFXCMContinuousFlag() == IFixDefs.FXCMCONTINUOUS_END) {
                session.doneProcessing();
            }
        }
        ITradeDesk tradeDesk = Liaison.getInstance().getTradeDesk();
        tradeDesk.syncServerTime(aMds.getOpenTimestamp().toDate());
        String symbol = null;
        try {
            symbol = aMds.getInstrument().getSymbol();
        } catch (NotDefinedException e) {
            e.printStackTrace();
        }
        Rate rate = tradeDesk.getRate(symbol);
        if (rate != null && symbol != null) {
            rate.setBuyPrice(aMds.getAskClose()); //1
            rate.setSellPrice(aMds.getBidClose()); //0
            if (rate.getOpenAsk() == 0) {
                rate.setOpenAsk(aMds.getAskClose());
            }
            if (rate.getOpenBid() == 0) {
                rate.setOpenBid(aMds.getBidClose());
            }
            rate.setLastDate(aMds.getOpenTimestamp().toDate());
            rate.setTradable(true);
            rate.setHighPrice(aMds.getHigh());
            rate.setLowPrice(aMds.getLow());
            rate.setBuyTradable("A".equals(aMds.getAskQuoteCondition()) && aMds.getAskQuoteType() == 1);
            rate.setSellTradable("A".equals(aMds.getBidQuoteCondition()) && aMds.getBidQuoteType() == 1);
            rate.setID(aMds.getInstrument().getFXCMSymID());
            rate.setQuoteID(aMds.getQuoteID());
            TradingSecurity security = session.getTradingSessionStatus().getSecurity(symbol);
            rate.setBuyInterest(security.getFXCMSymInterestBuy());
            rate.setSellInterest(security.getFXCMSymInterestSell());
            tradeDesk.updateRate(rate);
        }
        session.adjustStatus();
    }
}
