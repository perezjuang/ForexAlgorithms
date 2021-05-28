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
 * Created: Nov 13, 2006 11:14:59 AM
 *
 * $History: $
 */
package fxts.stations.transport.tradingapi.processors;

import com.fxcm.fix.trade.OrderCancelReject;
import com.fxcm.messaging.ITransportable;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.transport.tradingapi.resources.OraCodeFactory;

import javax.swing.JOptionPane;

/**
 */
public class OrderCancelRejectProcessor implements IProcessor {
    private boolean isDisconnect(OrderCancelReject aOrderCancelReject) {
        String details = aOrderCancelReject.getFXCMErrorDetails();
        return details != null && details.indexOf("Connection lost, please close this window and login") > 0;
    }

    public void process(ITransportable aTransportable) {
        OrderCancelReject aOcr = (OrderCancelReject) aTransportable;
        if (isDisconnect(aOcr)) {
            JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                          OraCodeFactory.toMessage(aOcr.getFXCMErrorDetails()),
                                          "Problem with your order..Reconnecting",
                                          JOptionPane.ERROR_MESSAGE);
            TradingServerSession.getInstance().logout();
        } else {
            MainFrame mainFrame = TradeApp.getInst().getMainFrame();
            JOptionPane.showMessageDialog(mainFrame,
                                          OraCodeFactory.toMessage(aOcr.getFXCMErrorDetails()),
                                          "Problem with your order..",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
}
