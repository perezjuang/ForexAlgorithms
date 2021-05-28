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
 *
 * 05/18/2006   Andre Mermegas: extends ABaseDialog
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.FXCMConnectionsManager;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.trader.ui.LoginDialogPane;
import fxts.stations.transport.tradingapi.LoginRequest;
import fxts.stations.util.ResourceManager;

import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.Frame;

/**
 * Class LoginDialog.<br>
 * <br>
 * It is responsible for input name and password of client
 * <ul>
 * <li> creating login dialog; </li>
 * <li> getting login parameters. </li>
 * </ul>
 * <br>
 * Creation date (9/5/2003 11:01 AM)
 */
public class LoginDialog extends ABaseDialog {
    //Login dialog exit code. It's set from closeDialog.
    private int mExitCode;
    /**
     * Main panel.
     */
    private LoginDialogPane mLoginDialogPane;

    /**
     * Constructor.
     *
     * @param aOwner frame owner
     */
    public LoginDialog(Frame aOwner) {
        super(aOwner);
        mLoginDialogPane = new LoginDialogPane(this);
        getContentPane().add(mLoginDialogPane);
    }

    /**
     * Closes the dialog.
     *
     * @param aExitCode code of exiting
     */
    public void closeDialog(int aExitCode) {
        mExitCode = aExitCode;
        super.closeDialog(aExitCode);
    }

    /**
     * @return exit code
     */
    public int getExitCode() {
        return mExitCode;
    }

    /**
     * @return login parameters
     */
    public LoginRequest getLoginParameters() {
        return mLoginDialogPane.getLoginParameters();
    }

    /**
     * showModal. <br>
     * Assign behaviour of window, subscribe on changing the openning positions,
     * show window and process its closing.
     *
     * @return Code of terminations
     *
     * @throws NumberFormatException when input rate is incorrect (unlikely).
     */
    public int showModal() {
        FXCMConnectionsManager.register(mLoginDialogPane);
        mExitCode = JOptionPane.CLOSED_OPTION;
        ResourceManager resMan = TradeApp.getInst().getResourceManager();
        if (resMan != null) {
            setTitle(resMan.getString("IDS_LOGIN_DIALOG_TITLE"));
        }
        setModal(true);
        pack();
        // if we have read and entered a previous username, put the focus into the password field
        String userName = mLoginDialogPane.getUserNameTextField().getText();
        if (userName != null && userName.trim().length() != 0) {
            mLoginDialogPane.getPasswordTextField().requestFocus();
        }

        //sets minimal sizes for components
        Dimension dim = mLoginDialogPane.getSize();
        mLoginDialogPane.setMinimumSize(dim);

        //setResizable(false);
        setLocationRelativeTo(getOwner());
        setVisible(true);
        FXCMConnectionsManager.unregister(mLoginDialogPane);
        return mExitCode;
    }
}