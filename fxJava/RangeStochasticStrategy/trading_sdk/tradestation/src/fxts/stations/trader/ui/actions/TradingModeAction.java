/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/TradingModeAction.java#2 $
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
 * Created: Aug 31, 2007 10:18:30 AM
 *
 * $History: $
 */
package fxts.stations.trader.ui.actions;

import com.fxcm.fix.IFixDefs;
import fxts.stations.core.ATradeAction;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.trader.ui.dialogs.TradingModeDialog;
import fxts.stations.transport.ILiaisonListener;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;

import javax.swing.JButton;
import java.awt.event.ActionEvent;

/**
 */
public class TradingModeAction extends ATradeAction {
    private static final String ACTION_NAME = "TradingModeAction";
    private JButton mButton;
    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;

    public TradingModeAction(JButton aButton) {
        super(ACTION_NAME);
        mButton = aButton;
        mButton.setEnabled(mCanAct);
        setEnabled(mCanAct);
        Liaison.getInstance().addLiaisonListener(new ILiaisonListener() {
            public void onCriticalError(LiaisonException aEx) {
            }

            public void onLiaisonStatus(LiaisonStatus aStatus) {
            }

            public void onLoginCompleted() {
                canAct(true);
            }

            public void onLoginFailed(LiaisonException aEx) {
                canAct(false);
            }

            public void onLogoutCompleted() {
                canAct(false);
            }
        });
    }

    /**
     * Protected constructor with trade action name as argument.
     *
     * @param aName name of action
     */
    protected TradingModeAction(String aName) {
        super(aName);
    }

    public void actionPerformed(ActionEvent aEvent) {
        if (mCanAct) {
            MainFrame frame = TradeApp.getInst().getMainFrame();
            TradingModeDialog dialog = new TradingModeDialog(frame);
            frame.showDialog(dialog);
        }
    }

    @Override
    public void canAct(boolean aCanAct) {
        if (TradingServerSession.getInstance().getUserKind() != IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
            setEnabled(aCanAct);
            mCanAct = aCanAct;
            mButton.setEnabled(mCanAct);
        }
    }
}
