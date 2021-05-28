/*
 * Copyright 2006 FXCM LLC
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
 */
package fxts.stations.trader.ui.actions;

import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.dialogs.LoginDialog;
import fxts.stations.transport.ILiaisonListener;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * An action that shows Login Dialog and performs connection to server.
 */
public class LoginAction extends AbstractAction implements ILiaisonListener {
    /**
     * Is action enable or no?
     */
    private boolean mbEnabled;

    /**
     * Constructor.
     */
    public LoginAction() {
        mbEnabled = Liaison.getInstance().getStatus() == LiaisonStatus.DISCONNECTED;
        Liaison.getInstance().addLiaisonListener(this);
    }

    /**
     * Is called when acction is being executed
     */
    public void actionPerformed(ActionEvent aEvent) {
        if (mbEnabled) {
            LoginDialog loginDialog = new LoginDialog(TradeApp.getInst().getMainFrame());
            if (loginDialog.showModal() == JOptionPane.OK_OPTION) {
                Liaison.getInstance().login(loginDialog.getLoginParameters());
            }
        }
    }

    /**
     * Reports about enabling state
     */
    public boolean isEnabled() {
        return mbEnabled;
    }

    /**
     * Formal implementation of ILiaisonListener interface method.
     * Does nothing.
     */
    public void onCriticalError(LiaisonException aEx) {
    }

    /**
     * This method is called when status of liaison has changed.
     * Changes enabling state of the action.
     *
     * @param aStatus liaison status
     */
    public void onLiaisonStatus(LiaisonStatus aStatus) {
        mbEnabled = aStatus == LiaisonStatus.DISCONNECTED;
    }

    /**
     * Formal implementation of ILiaisonListener interface method.
     * Does nothing.
     */
    public void onLoginCompleted() {
    }

    /**
     * Formal implementation of ILiaisonListener interface method.
     * Does nothing.
     */
    public void onLoginFailed(LiaisonException aEx) {
    }

    /**
     * Formal implementation of ILiaisonListener interface method.
     * Does nothing.
     */
    public void onLogoutCompleted() {
    }
}