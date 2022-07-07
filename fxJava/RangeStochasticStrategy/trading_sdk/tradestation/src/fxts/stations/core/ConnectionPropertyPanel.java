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
 * 08/23/2006   Andre Mermegas: removed ability to set log level, configure this via commons-logging props file
 */
package fxts.stations.core;

import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.dialogs.ConnectionPropertyDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Andre Mermegas
 *         Date: Jan 16, 2006
 *         Time: 3:59:09 PM
 */
public class ConnectionPropertyPanel extends JPanel {

    /**
     * Creates new form NewJPanel
     *
     * @param aDialog         dialog
     * @param aFXCMConnection connection
     */
    public ConnectionPropertyPanel(final ConnectionPropertyDialog aDialog, final FXCMConnection aFXCMConnection) {
        final UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        ResourceManager rm = TradeApp.getInst().getResourceManager();

        JButton okButton = UIManager.getInst().createButton(rm.getString("IDS_CLOSEPOSITION_DIALOG_OK"));
        JButton cancelButton = UIManager.getInst().createButton(rm.getString("IDS_CANCEL"));

        final JTextField connectionTextField = UIManager.getInst().createTextField(aFXCMConnection.getTerminal());
        connectionTextField.setColumns(20);

        final JTextField urlTextField = UIManager.getInst().createTextField(aFXCMConnection.getUrl());
        urlTextField.setCaretPosition(0);
        urlTextField.setColumns(20);

        JLabel connectionLabel = UIManager.getInst().createLabel(rm.getString("IDS_CONNECTION_NAME"));
        JLabel urlLabel = UIManager.getInst().createLabel(rm.getString("IDS_URL"));

        final JCheckBox secureCheckBox = UIManager.getInst().createCheckBox(rm.getString("IDS_PREFER_SECURE"));
        secureCheckBox.setSelected(preferences.getBoolean("Server.secure." + connectionTextField.getText()));
        secureCheckBox.setMnemonic('s');

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                String originalTerminal = aFXCMConnection.getTerminal();
                aFXCMConnection.setTerminal(connectionTextField.getText());
                aFXCMConnection.setUrl(urlTextField.getText());
                FXCMConnectionsManager.updateAddConnection(originalTerminal, aFXCMConnection);
                preferences.set("Server.secure." + connectionTextField.getText(), secureCheckBox.isSelected());
                aDialog.closeDialog(JOptionPane.OK_OPTION);
            }
        });

        aDialog.getRootPane().setDefaultButton(okButton);

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                aDialog.closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                aDialog.closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        setLayout(new RiverLayout());
        add("left", connectionLabel);
        add("tab hfill", connectionTextField);
        add("right", okButton);

        add("br left", urlLabel);
        add("tab hfill", urlTextField);
        add("right", cancelButton);

        add("br left", secureCheckBox);

        Util.setAllToBiggest(new JComponent[]{okButton, cancelButton});
    }
}
