/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/TradingModeDialog.java#1 $
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
 * Created: Aug 24, 2007 4:52:36 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.IClickModel;
import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatter;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 */
public class TradingModeDialog extends ABaseDialog implements IClickModel {
    private boolean mAtBest;
    private JSpinner mAtMarketSpinner;
    private JComboBox mOrderTypeBox;
    private ResourceManager mResMan;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public TradingModeDialog(Frame aOwner) {
        super(aOwner);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
            setTitle("Choose Mode");
            initComponents();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        final ResourceManager resourceManager = TradeApp.getInst().getResourceManager();
        UIManager ui = UIManager.getInst();
        final JLabel description = ui.createLabel();
        JPanel buttonPanel = new JPanel(new RiverLayout());
        String user = TradeDesk.getInst().getUserName();
        String tradingMode = UserPreferences.getUserPreferences(user).getString(TRADING_MODE);
        boolean atBest = UserPreferences.getUserPreferences(user).getBoolean(AT_BEST);
        int atMarket = UserPreferences.getUserPreferences(user).getInt(AT_MARKET);

        final JRadioButton defaultButton = ui.createRadioButton("Default (Click and Confirm");
        defaultButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent aEvent) {
                if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                    description.setText(resourceManager.getString("IDS_DEFAULT_CLICK_DESCRIPTION"));
                    mOrderTypeBox.setSelectedIndex(0);
                    mOrderTypeBox.setEnabled(false);
                    pack();
                }
            }
        });
        final JRadioButton singleButton = ui.createRadioButton("Single-Click");
        singleButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent aEvent) {
                if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                    description.setText(resourceManager.getString("IDS_SINGLE_CLICK_DESCRIPTION"));
                    mOrderTypeBox.setEnabled(true);
                    pack();
                }
            }
        });
        final JRadioButton doubleButton = ui.createRadioButton("Double-Click");
        doubleButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent aEvent) {
                if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                    description.setText(resourceManager.getString("IDS_DOUBLE_CLICK_DESCRIPTION"));
                    mOrderTypeBox.setEnabled(true);
                    pack();
                }
            }
        });
        ButtonGroup group = new ButtonGroup();
        group.add(defaultButton);
        group.add(singleButton);
        group.add(doubleButton);
        buttonPanel.add("br hfill", defaultButton);
        buttonPanel.add("br hfill", singleButton);
        buttonPanel.add("br hfill", doubleButton);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Mode"));

        JPanel descPanel = new JPanel(new RiverLayout());
        descPanel.setBorder(BorderFactory.createTitledBorder("Description"));
        descPanel.add("br hfill", description);

        JPanel paramPanel = new JPanel(new RiverLayout());
        mAtMarketSpinner = UIManager.getInst().createSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        mAtMarketSpinner.setEnabled(false);
        mAtMarketSpinner.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent aMouseWheelEvent) {
                if (mAtMarketSpinner.isEnabled()) {
                    if (aMouseWheelEvent.getWheelRotation() <= 0) {
                        mAtMarketSpinner.setValue(mAtMarketSpinner.getNextValue());
                    } else {
                        mAtMarketSpinner.setValue(mAtMarketSpinner.getPreviousValue());
                    }
                }
            }
        });
        JFormattedTextField text = ((JSpinner.DefaultEditor) mAtMarketSpinner.getEditor()).getTextField();
        ((DefaultFormatter) text.getFormatter()).setAllowsInvalid(false);
        text.setHorizontalAlignment(JTextField.LEFT);
        String[] options = new String[]{mResMan.getString("IDS_AT_BEST"), mResMan.getString("IDS_AT_MARKET")};
        mOrderTypeBox = new JComboBox(options);
        mOrderTypeBox.setFocusable(true);
        mOrderTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                JComboBox cb = (JComboBox) aEvent.getSource();
                if (cb.getSelectedIndex() == 0) {
                    mAtMarketSpinner.setEnabled(false);
                    mAtBest = true;
                } else {
                    mAtMarketSpinner.setEnabled(true);
                    mAtBest = false;
                }
            }
        });
        if (atBest) {
            mAtBest = true;
            mOrderTypeBox.setSelectedIndex(0);
            mAtMarketSpinner.setEnabled(false);
        } else {
            mAtBest = false;
            mOrderTypeBox.setSelectedIndex(1);
            mAtMarketSpinner.setEnabled(true);
            mAtMarketSpinner.setValue(atMarket);
        }
        paramPanel.add("br left", mOrderTypeBox);
        paramPanel.add("tab hfill", mAtMarketSpinner);

        JButton okButton = UIManager.getInst().createButton();
        okButton.setText(mResMan.getString("IDS_MARKET_DIALOG_OK"));
        getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                MainFrame frame = TradeApp.getInst().getMainFrame();
                UserPreferences prefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
                if (defaultButton.isSelected()) {
                    prefs.set(TRADING_MODE, DEFAULT_CLICK);
                    prefs.set(AT_BEST, true);
                    prefs.set(AT_MARKET, 0);
                } else if (singleButton.isSelected()) {
                    DisclaimerDialog dd = new DisclaimerDialog(frame);
                    if (frame.showDialog(dd) == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                    prefs.set(TRADING_MODE, SINGLE_CLICK);
                } else if (doubleButton.isSelected()) {
                    DisclaimerDialog dd = new DisclaimerDialog(frame);
                    if (frame.showDialog(dd) == JOptionPane.CANCEL_OPTION) {
                        return;
                    }
                    prefs.set(TRADING_MODE, DOUBLE_CLICK);
                }
                if (mAtBest) {
                    prefs.set(AT_BEST, true);
                    prefs.set(AT_MARKET, 0);
                } else {
                    prefs.set(AT_BEST, false);
                    prefs.set(AT_MARKET, mAtMarketSpinner.getValue().toString());
                }
                closeDialog(JOptionPane.OK_OPTION);
            }
        });
        JButton cancelButton = UIManager.getInst().createButton();
        cancelButton.setText(mResMan.getString("IDS_MARKET_DIALOG_CANCEL"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exiting by escape
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                        "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        if (tradingMode == null || DEFAULT_CLICK.equals(tradingMode)) {
            defaultButton.setSelected(true);
            description.setText(resourceManager.getString("IDS_DEFAULT_CLICK_DESCRIPTION"));
        } else if (SINGLE_CLICK.equals(tradingMode)) {
            singleButton.setSelected(true);
            description.setText(resourceManager.getString("IDS_SINGLE_CLICK_DESCRIPTION"));
        } else if (DOUBLE_CLICK.equals(tradingMode)) {
            doubleButton.setSelected(true);
            description.setText(resourceManager.getString("IDS_DOUBLE_CLICK_DESCRIPTION"));
        }

        getContentPane().setLayout(new RiverLayout());
        getContentPane().add("br hfill", buttonPanel);
        getContentPane().add("br hfill", descPanel);
        getContentPane().add("br hfill", paramPanel);
        getContentPane().add("br center", okButton);
        getContentPane().add("center", cancelButton);
        Util.setAllToBiggest(new JComponent[]{okButton, cancelButton});
    }

    /**
     * shows dialog as modal
     */
    @Override
    public int showModal() {
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return JOptionPane.CLOSED_OPTION;
    }
}
