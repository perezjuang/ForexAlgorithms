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
 * $History: $
 * 04/17/2004  SVV  + Added FrontEnd support
 * 09/05/2003 Created by USHK
 * 06/07/2005   Andre Mermegas: added listeners to selectall in textfield on focus
 */
package fxts.stations.trader.ui;

import fxts.stations.core.FXCMConnection;
import fxts.stations.core.FXCMConnectionsManager;
import fxts.stations.core.IConnectionManagerListener;
import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.dialogs.ConnectionManagerDialog;
import fxts.stations.trader.ui.dialogs.LoginDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.LoginRequest;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Class LoginDialogPane.<br>
 * <br>
 * It is responsible for:
 * <ul>
 * <li> creating login dialog; </li>
 * <li> getting login parameters. </li>
 * </ul>
 * <br>
 *
 * @Creation date (9/5/2003 11:01 AM)
 */
public class LoginDialogPane extends JComponent implements IConnectionManagerListener {
    private ResourceManager mResourceManager;
    private JButton mCancelButton;
    private JLabel mCapslockLabel;
    private JComboBox mConnectionComboBox;
    private JLabel mConnectionNameLabel;
    private LoginDialog mDialog;
    private JButton mLoginButton;
    private JLabel mPasswordLabel;
    private JPasswordField mPasswordTextField;
    private JButton mSettingsButton;
    private JLabel mUserNameLabel;
    private JTextField mUserNameTextField;

    /**
     * @param aLoginDialog dialog
     */
    public LoginDialogPane(LoginDialog aLoginDialog) {
        mDialog = aLoginDialog;
        try {
            mResourceManager = ResourceManager.getManager("fxts.stations.transport.tradingapi.resources.Resources");
        } catch (Exception e) {
            throw new RuntimeException("can't get resource manager" + e.getMessage());
        }
        mUserNameLabel = UIManager.getInst().createLabel(mResourceManager.getString("IDS_USER_NAME_LABEL"));
        mPasswordLabel = UIManager.getInst().createLabel(mResourceManager.getString("IDS_USER_PWD_LABEL"));
        mSettingsButton = UIManager.getInst().createButton(mResourceManager.getString("IDS_SETTINGS"));
        mCapslockLabel = UIManager.getInst().createLabel(">>> caps lock is on <<<");
        mCapslockLabel.setForeground(Color.RED);
        mSettingsButton.setMnemonic('s');
        mSettingsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                ConnectionManagerDialog mcd = new ConnectionManagerDialog(TradeApp.getInst().getMainFrame());
                mcd.showModal();
            }
        });
        mCancelButton = UIManager.getInst().createButton(mResourceManager.getString("IDS_BUTTON_CANCEL_TITLE"));
        mCancelButton.setMnemonic('c');
        mLoginButton = UIManager.getInst().createButton(mResourceManager.getString("IDS_LOGIN_OK_TITLE"));
        mLoginButton.setMnemonic('l');
        mLoginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (mDialog != null) {
                    if (getPasswordTextField().getPassword().length == 0) {
                        JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                      "Error: no password supplied",
                                                      "Error: no password supplied",
                                                      JOptionPane.ERROR_MESSAGE);
                    } else {
                        closeDialog(JOptionPane.OK_OPTION);
                    }
                }
            }
        });

        mDialog.getRootPane().setDefaultButton(mLoginButton);
        mCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exting by escape
        mCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                         "Exit");
        mCancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        Font font = new Font(mResourceManager.getString("IDS_LOGIN_DIALOG_FONT"), Font.PLAIN, 12);
        mUserNameLabel.setFont(font);
        mPasswordLabel.setFont(font);
        mLoginButton.setFont(font);
        mSettingsButton.setFont(font);
        mCancelButton.setFont(font);
        mCapslockLabel.setFont(font);
        mUserNameTextField = UIManager.getInst().createTextField();
        mPasswordTextField = UIManager.getInst().createPasswordField();
        mUserNameTextField.setFont(font);
        mPasswordTextField.setFont(font);
        mUserNameTextField.setColumns(10);
        mPasswordTextField.setColumns(10);
        String sUserName = null;
        try {
            sUserName = Liaison.getInstance().getTradeDesk().getUserName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sUserName != null) {
            mUserNameTextField.setText(sUserName);
        }
        setDialogLayout();
    }

    public void closeDialog(int aExitCode) {
        mDialog.closeDialog(aExitCode);
    }

    /**
     * @return login parameters
     */
    public LoginRequest getLoginParameters() {
        String name = mUserNameTextField.getText();
        String connectionName = mConnectionComboBox.getSelectedItem().toString();
        FXCMConnection cx = FXCMConnectionsManager.getConnection(connectionName);
        return new LoginRequest(name, String.valueOf(mPasswordTextField.getPassword()), connectionName, cx.getUrl(), null);
    }

    /**
     * @return password text field
     */
    public JPasswordField getPasswordTextField() {
        return mPasswordTextField;
    }

    /**
     * @return username text field
     */
    public JTextField getUserNameTextField() {
        return mUserNameTextField;
    }

    private void initConnectionControls() {
        String connectionNameLabelText;
        String fontName = mResourceManager.getString("IDS_LOGIN_DIALOG_FONT", "Default");
        Font font = new Font(fontName, Font.PLAIN, 12);
        try {
            String descriptor = "fxts.stations.transport.tradingapi.resources.Resources";
            ResourceManager manager = ResourceManager.getManager(descriptor);
            connectionNameLabelText = manager.getString("IDS_CONNECTION_NAME_LABEL", "Connection name:");
        } catch (Exception e) {
            throw new RuntimeException("Localization is not available");
        }
        mConnectionNameLabel = UIManager.getInst().createLabel(connectionNameLabelText);
        mConnectionNameLabel.setFont(font);
        mConnectionComboBox = new JComboBox();
        mConnectionComboBox.setFont(font);
        mUserNameTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent aEvent) {
                mUserNameTextField.selectAll();
            }
        });
        mPasswordTextField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent aEvent) {
                mPasswordTextField.selectAll();
            }
        });
        mPasswordTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent aEvent) {
                if (Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK)) {
                    if (!mCapslockLabel.isShowing()) {
                        add("br center", mCapslockLabel);
                    }
                } else {
                    if (mCapslockLabel.isShowing()) {
                        remove(mCapslockLabel);
                    }
                }
                mDialog.pack();
            }
        });
        mConnectionComboBox.setModel(new DefaultComboBoxModel(FXCMConnectionsManager.getTerminals()));
        mConnectionComboBox.setFocusable(true);
        mConnectionComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent aEvent) {
                String cx = (String) mConnectionComboBox.getSelectedItem();
                FXCMConnection fxcmcx = FXCMConnectionsManager.getConnection(cx);
                if (fxcmcx != null) {
                    mUserNameTextField.setText(fxcmcx.getUsername());
                }
            }
        });
        mConnectionComboBox.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent aEvent) {
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent aEvent) {
                mPasswordTextField.setText("");
                mPasswordTextField.requestFocus();
            }

            public void popupMenuCanceled(PopupMenuEvent aEvent) {
            }
        });
        UserPreferences userPreferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        String terminal = userPreferences.getString("Server.last.connected.terminal");
        if (terminal != null) {
            mConnectionComboBox.setSelectedItem(terminal);
        } else if (mConnectionComboBox.getItemCount() > 0) {
            mConnectionComboBox.setSelectedIndex(0);
        }
    }

    protected void setDialogLayout() {
        initConnectionControls();
        setLayout(new RiverLayout());

        add("left", mUserNameLabel);
        add("tab hfill", mUserNameTextField);

        add("br left", mPasswordLabel);
        add("tab hfill", mPasswordTextField);

        add("br left", mConnectionNameLabel);
        add("tab hfill", mConnectionComboBox);

        add("br center", mLoginButton);
        add("center", mSettingsButton);
        add("center", mCancelButton);
        Util.setAllToBiggest(new JComponent[]{mLoginButton, mSettingsButton, mCancelButton});
    }

    public void updated() {
        try {
            mConnectionComboBox.setModel(new DefaultComboBoxModel(FXCMConnectionsManager.getTerminals()));
            UserPreferences userPreferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            String terminal = userPreferences.getString("Server.last.connected.terminal");
            if (terminal == null || FXCMConnectionsManager.getConnection(terminal) == null) {
                if (mConnectionComboBox.getItemCount() > 0) {
                    mConnectionComboBox.setSelectedIndex(0);
                }
            } else {
                mConnectionComboBox.setSelectedItem(terminal);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
