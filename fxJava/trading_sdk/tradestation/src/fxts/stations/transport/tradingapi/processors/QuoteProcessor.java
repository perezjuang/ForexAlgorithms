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
 * Created: Nov 13, 2006 11:16:05 AM
 *
 * $History: $
 * 03/30/2007   Andre Mermegas: add instrument to QuoteResponse
 */
package fxts.stations.transport.tradingapi.processors;

import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.pretrade.Quote;
import com.fxcm.fix.pretrade.QuoteResponse;
import com.fxcm.fix.trade.OrderSingle;
import com.fxcm.messaging.ITransportable;
import com.fxcm.messaging.util.fix.FXCMCommandType;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.trader.ui.dialogs.ShowQuoteDialog;
import fxts.stations.transport.tradingapi.TradingServerSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.SwingUtilities;
import java.util.Date;

/**
 */
public class QuoteProcessor implements IProcessor {
    private final Log mLogger = LogFactory.getLog(QuoteProcessor.class);

    public void process(ITransportable aTransportable) {
        final TradingServerSession aTradingServerSession = TradingServerSession.getInstance();
        final Quote aQuote = (Quote) aTransportable;
        mLogger.debug("aQuote = " + aQuote);
        final MainFrame mainFrame = TradeApp.getInst().getMainFrame();
        if (aQuote.getQuoteID().startsWith(FXCMCommandType.REQUOTE_PREFIX)) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        ShowQuoteDialog mDlg = new ShowQuoteDialog(mainFrame);
                        mDlg.setExpirationDate(aQuote.getValidUntilTime().toDate());
                        mDlg.setCurrency(aQuote.getInstrument().getSymbol());
                        mDlg.setBuyPrice(aQuote.getBidPx());
                        mDlg.setSellPrice(aQuote.getOfferPx());
                        int result = mainFrame.showDialog(mDlg);
                        ITransportable amsg;
                        if (result == ShowQuoteDialog.BUY || result == ShowQuoteDialog.SELL) {
                            amsg = MessageGenerator.generateAcceptOrder(aQuote.getQuoteID(), "accept requote");
                        } else {
                            amsg = MessageGenerator.generatePassResponse(aQuote.getQuoteID());
                        }
                        aTradingServerSession.send(amsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            // RFQ
            SwingUtilities.invokeLater(new Runnable() {
                private ShowQuoteDialog mDlg = new ShowQuoteDialog(mainFrame);

                public void run() {
                    try {
                        if (aQuote.getValidUntilTime().toDate().after(new Date())) {
                            mDlg.setExpirationDate(aQuote.getValidUntilTime().toDate());
                            mDlg.setCurrency(aQuote.getInstrument().getSymbol());
                            mDlg.setBuyPrice(aQuote.getOfferPx());
                            mDlg.setSellPrice(aQuote.getBidPx());
                            int res = mainFrame.showDialog(mDlg);
                            if (res == ShowQuoteDialog.BUY) {
                                OrderSingle orderSingle =
                                        MessageGenerator.generateOpenOrder(aQuote.getQuoteID(),
                                                                           aQuote.getOfferPx(),
                                                                           aQuote.getAccount(),
                                                                           aQuote.getOrderQty(),
                                                                           SideFactory.BUY,
                                                                           aQuote.getInstrument().getSymbol(),
                                                                           null);
                                aTradingServerSession.send(orderSingle);
                            } else if (res == ShowQuoteDialog.SELL) {
                                OrderSingle orderSingle =
                                        MessageGenerator.generateOpenOrder(aQuote.getQuoteID(),
                                                                           aQuote.getBidPx(),
                                                                           aQuote.getAccount(),
                                                                           aQuote.getOrderQty(),
                                                                           SideFactory.SELL,
                                                                           aQuote.getInstrument().getSymbol(),
                                                                           null);
                                aTradingServerSession.send(orderSingle);
                            }
                        }
                        QuoteResponse qr = MessageGenerator.generatePassResponse(aQuote.getQuoteRespID());
                        qr.setInstrument(aQuote.getInstrument());
                        mLogger.debug("Removing the Quote");
                        aTradingServerSession.send(qr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
