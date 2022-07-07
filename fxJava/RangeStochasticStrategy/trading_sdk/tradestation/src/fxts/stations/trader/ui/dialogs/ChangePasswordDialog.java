/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/ChangePasswordDialog.java#1 $
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
 * Created: Oct 17, 2008 10:54:47 AM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 */
public class ChangePasswordDialog extends ABaseDialog {
    private JPasswordField mConfirmNewPassword;
    private int mExitCode;
    private JPasswordField mNewPassword;
    private JPasswordField mOldPassword;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public ChangePasswordDialog(Frame aOwner) {
        super(aOwner);
        initComponents();
        pack();
    }

    /**
     * Closes the dialog.
     *
     * @param aExitCode code of exiting
     */
    @Override
    public void closeDialog(int aExitCode) {
        mExitCode = aExitCode;
        super.closeDialog(aExitCode);
    }

    public String getConfirmNewPassword() {
        return String.valueOf(mConfirmNewPassword.getPassword());
    }

    public String getNewPassword() {
        return String.valueOf(mNewPassword.getPassword());
    }

    public String getOldPassword() {
        return String.valueOf(mOldPassword.getPassword());
    }

    private void initComponents() {
        setLayout(new RiverLayout());
        setModal(true);
        setTitle("Change Password");

        mOldPassword = UIManager.getInst().createPasswordField();
        mOldPassword.setColumns(10);

        mNewPassword = UIManager.getInst().createPasswordField();
        mNewPassword.setColumns(10);

        mConfirmNewPassword = UIManager.getInst().createPasswordField();
        mConfirmNewPassword.setColumns(10);

        add("left", UIManager.getInst().createLabel("Old Password:"));
        add("tab hfill", mOldPassword);

        add("br left", UIManager.getInst().createLabel("New Password:"));
        add("tab hfill", mNewPassword);

        add("br left", UIManager.getInst().createLabel("Confirm New Password:"));
        add("tab hfill", mConfirmNewPassword);

        JButton okButton = UIManager.getInst().createButton("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (getOldPassword() == null || getOldPassword().length() == 0) {
                    JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                  "Error: Old password incorrect.",
                                                  "Error: Old password incorrect.",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!getOldPassword().equals(TradingServerSession.getInstance().getPassword())) {
                    JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                  "Error: Old password incorrect.",
                                                  "Error: Old password incorrect.",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (getNewPassword() == null || getNewPassword().length() == 0) {
                    JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                  "Error: New password invalid.",
                                                  "Error: New password invalid.",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (getConfirmNewPassword() == null && getConfirmNewPassword().length() == 0) {
                    JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                  "Error: New password invalid.",
                                                  "Error: New password invalid.",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!getNewPassword().equals(getConfirmNewPassword())) {
                    JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                  "Error: New password does not match.",
                                                  "Error: New password does not match.",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (getOldPassword().equals(getNewPassword())) {
                    JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                  "Error: New password same as old password.",
                                                  "Error: New password same as old password.",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }

                closeDialog(JOptionPane.OK_OPTION);
            }
        });

        JButton cancelButton = UIManager.getInst().createButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        add("br center", okButton);
        add("center", cancelButton);

        getRootPane().setDefaultButton(okButton);
        Util.setAllToBiggest(new JComponent[]{okButton, cancelButton});
    }

    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        mOldPassword.requestFocus();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return mExitCode;
    }
}
