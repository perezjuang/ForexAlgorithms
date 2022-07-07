/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/ChangePasswordAction.java#1 $
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
 * Created: Oct 17, 2008 11:06:23 AM
 *
 * $History: $
 */
package fxts.stations.trader.ui.actions;

import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MessageBoxRunnable;
import fxts.stations.trader.ui.dialogs.ChangePasswordDialog;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequestFactory;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.tradingapi.Liaison;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

/**
 */
public class ChangePasswordAction extends AbstractAction {
    public void actionPerformed(ActionEvent aEvent) {
        ChangePasswordDialog d = new ChangePasswordDialog(TradeApp.getInst().getMainFrame());
        if (d.showModal() == JOptionPane.OK_OPTION) {
            Liaison liaison = Liaison.getInstance();
            IRequestFactory requestFactory = liaison.getRequestFactory();
            IRequest request = requestFactory.changePassword(d.getOldPassword(),
                                                             d.getNewPassword(),
                                                             d.getConfirmNewPassword());
            try {
                liaison.sendRequest(request);
            } catch (LiaisonException aEx) {
                EventQueue.invokeLater(new MessageBoxRunnable(aEx));
            }
        }
    }
}
