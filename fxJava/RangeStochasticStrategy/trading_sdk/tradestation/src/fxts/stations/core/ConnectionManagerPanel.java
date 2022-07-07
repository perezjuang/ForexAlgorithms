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
 * 05/05/2006   Andre Mermegas: added confirmation before deleting connection
 * 07/05/2006   Andre Mermegas: added fix to use proxy if set in connection test
 */
package fxts.stations.core;

import com.fxcm.GenericException;
import com.fxcm.messaging.util.HostReader;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.trader.ui.dialogs.ConnectionManagerDialog;
import fxts.stations.trader.ui.dialogs.ConnectionPropertyDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Andre Mermegas
 *         Date: Jan 16, 2006
 *         Time: 1:23:21 PM
 */
public class ConnectionManagerPanel extends JPanel implements IConnectionManagerListener {
    private JList mConnectionList;
    private MainFrame mMainFrame = TradeApp.getInst().getMainFrame();
    private FXCMConnection mSelectedConnection;
    private ConnectionManagerDialog mDialog;

    /**
     * Creates new form ConnectionManagerPanel
     *
     * @param aConnectionManagerDialog dialog
     */
    public ConnectionManagerPanel(ConnectionManagerDialog aConnectionManagerDialog) {
        mDialog = aConnectionManagerDialog;
        final ResourceManager rm = TradeApp.getInst().getResourceManager();
        final UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        FXCMConnectionsManager.register(this);

        JButton newButton = UIManager.getInst().createButton(rm.getString("IDS_NEW_BUTTON"));
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                mSelectedConnection = new FXCMConnection(null, null, null);
                ConnectionPropertyDialog cpd = new ConnectionPropertyDialog(mSelectedConnection, mMainFrame);
                cpd.showModal();
            }
        });

        JButton testButton = UIManager.getInst().createButton(rm.getString("IDS_TEST_BUTTON"));
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (!mConnectionList.isSelectionEmpty()) {
                    HostReader hr = new HostReader();
                    if (preferences.getBoolean("Proxy.use")) {
                        String host = preferences.getString("Proxy.host");
                        int port = Integer.parseInt(preferences.getString("Proxy.port"));
                        String user = preferences.getString("Proxy.user");
                        String pass = preferences.getString("Proxy.password");
                        hr.setProxyParameters(host, port, user, pass, false);
                    }
                    String selectedValue = (String) mConnectionList.getSelectedValue();
                    FXCMConnection connection = FXCMConnectionsManager.getConnection(selectedValue);
                    try {
                        hr.read(connection.getUrl(), connection.getTerminal(), null, null);
                        JOptionPane.showMessageDialog(mMainFrame, rm.getString("IDS_GOOD_CONNECTION"));
                    } catch (GenericException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(mMainFrame,
                                                      e.getMessage(),
                                                      rm.getString("IDS_BAD_CONNECTION"),
                                                      JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        JButton deleteButton = UIManager.getInst().createButton(rm.getString("IDS_DELETE_BUTTON"));
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        String cx = (String) mConnectionList.getSelectedValue();
                        int i = JOptionPane.showConfirmDialog(TradeApp.getInst().getMainFrame(),
                                                              rm.getString("IDS_DELETE_CONNECTION") + cx + " ?",
                                                              rm.getString("IDS_DELETE_CONNECTION"),
                                                              JOptionPane.INFORMATION_MESSAGE);
                        if (i == JOptionPane.YES_OPTION) {
                            FXCMConnectionsManager.remove(cx);
                        }
                    }
                });
            }
        });

        JButton editButton = UIManager.getInst().createButton(rm.getString("IDS_EDIT_BUTTON"));
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                String o = (String) mConnectionList.getSelectedValue();
                if (o != null) {
                    showConnectionDialog(o);
                }
            }
        });
        aConnectionManagerDialog.getRootPane().setDefaultButton(editButton);

        JButton closeButton = UIManager.getInst().createButton(rm.getString("IDS_CLOSE_BUTTON"));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CLOSED_OPTION);
            }
        });
        closeButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                       "Exit");
        closeButton.getActionMap().put("Exit", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CLOSED_OPTION);
            }
        });

        mConnectionList = UIManager.getInst().createList();
        mConnectionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent aEvent) {
                if (aEvent.getClickCount() == 2) {
                    JList list = (JList) aEvent.getSource();
                    String o = (String) list.getSelectedValue();
                    showConnectionDialog(o);
                }
            }
        });
        mConnectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mConnectionList.setModel(new AbstractListModel() {
            private String[] mStrings = FXCMConnectionsManager.getTerminals();

            public int getSize() {
                return mStrings.length;
            }

            public Object getElementAt(int aIndex) {
                return mStrings[aIndex];
            }
        });
        //String terminal = preferences.getString("Server.last.connected.terminal");
        //mConnectionList.setSelectedValue(terminal,true);
        mConnectionList.setSelectedIndex(0);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(mConnectionList);
        scrollPane.setPreferredSize(new Dimension(200, 200));

        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new RiverLayout());
        JPanel rightPanel = new JPanel(new RiverLayout());

        leftPanel.add("left", UIManager.getInst().createLabel(rm.getString("IDS_CONNECTION_NAMES")));
        leftPanel.add("br vfill hfill", scrollPane);
        add(leftPanel, BorderLayout.CENTER);

        rightPanel.add("p left", newButton);
        rightPanel.add("br left", editButton);
        rightPanel.add("br left", deleteButton);
        rightPanel.add("br left", closeButton);
        rightPanel.add("br left", testButton);
        add(rightPanel, BorderLayout.EAST);

        Util.setAllToBiggest(new JComponent[]{newButton, editButton, deleteButton, closeButton, testButton});
    }

    private void showConnectionDialog(String aTerminal) {
        mSelectedConnection = FXCMConnectionsManager.getConnection(aTerminal);
        ConnectionPropertyDialog cpd = new ConnectionPropertyDialog(mSelectedConnection, mMainFrame);
        cpd.showModal();
    }

    public void closeDialog(int aExitCode) {
        FXCMConnectionsManager.unregister(this);
        mDialog.closeDialog(aExitCode);
    }

    public void updated() {
        mConnectionList.setModel(new AbstractListModel() {
            private String[] mStrings = FXCMConnectionsManager.getTerminals();

            public int getSize() {
                return mStrings.length;
            }

            public Object getElementAt(int aIndex) {
                return mStrings[aIndex];
            }
        });
        if (mConnectionList.getModel().getSize() > 0) {
            if (mSelectedConnection == null) {
                mConnectionList.setSelectedIndex(0);
            } else {
                mConnectionList.setSelectedValue(mSelectedConnection.getTerminal(), true);
            }
        }
        mSelectedConnection = null;
    }
}
