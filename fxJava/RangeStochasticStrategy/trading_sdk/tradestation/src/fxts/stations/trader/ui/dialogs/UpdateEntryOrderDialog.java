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
 * 05/22/2006   Andre Mermegas: fix to use JSpinner instead of hacked scrollbars
 * 10/04/2006   Andre Mermegas: scrollwheel support in spinner
 * 05/11/2007   Andre Mermegas: spinner alight left
 *
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.Orders;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.RateSpinner;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.Util;
import fxts.stations.util.signals.RemoveSignal;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

/**
 * This dialog allows to user to enter parameters for update entry order
 * and for process it.
 */
public class UpdateEntryOrderDialog extends AmountDialog implements ISignalListener {
    private JTextField mCustomTextTextField;
    private int mExitCode;
    private JButton mOKButton;
    private Order mOrder;
    private OrdersComboBox mOrdersComboBox;
    private RateSpinner mRateSpinner;

    /**
     * Creates new form UpdateEntryOrderDialog.
     *
     * @param aParent parent frame
     */
    public UpdateEntryOrderDialog(Frame aParent) {
        super(aParent);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
            initComponents();
            pack();
            mCustomTextTextField.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes the dialog.
     *
     * @param aExitCode code of exiting
     */
    @Override
    public void closeDialog(int aExitCode) {
        super.closeDialog(aExitCode);
        mExitCode = aExitCode;
    }

    /**
     * Sets ability of the dialog.
     *
     * @param aEnabled true value corresponds to enabling of dialog
     */
    @Override
    public void enableDialog(boolean aEnabled) {
        if (aEnabled) {
            mOKButton.setEnabled(true);
        } else {
            mOKButton.setEnabled(false);
        }
        super.enableDialog(aEnabled);
    }

    /**
     * Initializes the form field Rate.
     */
    private void fillRateTextField() {
        if (mOrder != null) {
            String currency = mOrder.getCurrency();
            mRateSpinner.setCurrency(currency);
            mRateSpinner.setValue(mOrder.getOrderRate());
        }
    }

    /**
     * @return Custom Text
     */
    public String getCustomText() {
        return mCustomTextTextField.getText();
    }

    /**
     * @return order id.
     */
    public String getOrderID() {
        return mOrder.getOrderID();
    }

    /**
     * @return orders rate. (For using only after exiting from dialog.)
     */
    public double getOrderRate() {
        if (mOrder == null) {
            return 0;
        }
        double value;
        try {
            value = Util.parseDouble(mRateSpinner.getValue().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
        try {
            value = Util.parseDouble(TradeDesk.formatPrice(mOrder.getCurrency(), value));
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }

    /**
     * Initialization of components.
     */
    @Override
    protected void initComponents() {
        //creating of components
        JPanel buttonPanel = new JPanel();
        UIManager uiManager = UIManager.getInst();
        mOKButton = uiManager.createButton(getResMan().getString("IDS_ENTRY_DIALOG_OK"));
        JButton cancelButton = uiManager.createButton(getResMan().getString("IDS_ENTRY_DIALOG_CANCEL"));
        JLabel orderLabel = uiManager.createLabel(getResMan().getString("IDS_CHANGE_ENTRY_DIALOG_ORDER_ID"));
        JLabel rateLabel = uiManager.createLabel(getResMan().getString("IDS_ENTRY_DIALOG_RATE"));
        JPanel rateSpinnerPanel = new JPanel();
        mOrdersComboBox = new OrdersComboBox();
        mOrdersComboBox.setDialog(this);
        mOrdersComboBox.init(new DefaultActor());
        JLabel customTextLabel = uiManager.createLabel(mResMan.getString("IDS_CUSTOM_TEXT"));
        mCustomTextTextField = uiManager.createTextField();

        mRateSpinner = uiManager.createRateSpinner();
        JFormattedTextField text = ((JSpinner.DefaultEditor) mRateSpinner.getEditor()).getTextField();
        text.setHorizontalAlignment(JTextField.LEFT);
        text.setFont(customTextLabel.getFont());
        text.setPreferredSize(new Dimension(100, 20));
        text.setEditable(true);
        text.setMargin(new Insets(0, 2, 0, 2));

        //sets dialog
        getContentPane().setLayout(new RiverLayout());
        setModal(true);
        setTitle(getResMan().getString("IDS_CHANGE_ENTRY_DIALOG_TITLE"));
        setBackground(Color.WHITE);

        mOrdersComboBox.addItemListener(new OrdersComboBoxSelectItemListener());

        rateSpinnerPanel.setLayout(new BorderLayout());
        rateSpinnerPanel.add(mRateSpinner, BorderLayout.CENTER);

        //buttons panel
        getRootPane().setDefaultButton(mOKButton);
        mOKButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvt) {
                        if (verify()) {
                            closeDialog(JOptionPane.OK_OPTION);
                        }
                    }
                });
        buttonPanel.add(mOKButton);
        buttonPanel.add(cancelButton);
        cancelButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvt) {
                        closeDialog(JOptionPane.CANCEL_OPTION);
                    }
                });
        //sets for exiting by escape
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                        "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        Util.setAllToBiggest(new JComponent[]{mOKButton, cancelButton});

        getContentPane().add("left", orderLabel);
        getContentPane().add("tab hfill", mOrdersComboBox);

        getContentPane().add("br left", rateLabel);
        getContentPane().add("tab hfill", rateSpinnerPanel);

        getContentPane().add("br left", customTextLabel);
        getContentPane().add("tab hfill", mCustomTextTextField);
        getContentPane().add("br center", buttonPanel);
    }

    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal.getType() == SignalType.REMOVE) {
            try {
                RemoveSignal signal = (RemoveSignal) aSignal;
                Order element = (Order) signal.getElement();
                if (element.getOrderID().equals(mOrdersComboBox.getSelectedOrder())) {
                    String msg = "The currently selected open order has been already closed."
                                 + "\nPlease, choose another open order";
                    JOptionPane.showMessageDialog(getOwner(), msg, null, JOptionPane.ERROR_MESSAGE);
                    mOrdersComboBox.setSelectedIndex(-1);
                } else if (mOrdersComboBox.getItemCount() == 0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            closeDialog(JOptionPane.CANCEL_OPTION);
                        }
                    });
                }
                mOrdersComboBox.selectOrder(mOrdersComboBox.getSelectedOrder()); //xxx reselect to make sure
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.ADD) {
            mOrdersComboBox.selectOrder(mOrdersComboBox.getSelectedOrder()); //xxx reselect to make sure
        }
    }

    /**
     * Sets current order.
     *
     * @param aOrderID identifier of order
     */
    public void setOrderID(String aOrderID) {
        if (mOrdersComboBox != null) {
            mOrdersComboBox.selectOrder(aOrderID);
        }
        //sets values of rate, stop and limit fields
        fillRateTextField();
    }

    /**
     * Forces dialog to modal.
     * Will assign sizes and position of dialog on application window.
     */
    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.REMOVE);
        setVisible(true);
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.REMOVE);
        return mExitCode;
    }

    /**
     * @return of amount`s value.
     */
    @Override
    protected boolean verify() {
        if (mOrder == null) {
            return true;
        }
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        double value;
        try {
            value = Util.parseDouble(mRateSpinner.getValue().toString());
        } catch (NumberFormatException e) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(
                    this,
                    resmng.getString("IDS_ENTRY_DIALOG_RATE_NUMBER_ERROR"),
                    resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (value < mOrder.getOfferRate() && value > mOrder.getOfferRate()) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(
                    this,
                    resmng.getString("IDS_ENTRY_DIALOG_RATE_VALUE_ERROR"),
                    resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }
        if (value <= 0) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(
                    this,
                    resmng.getString("IDS_ENTRY_DIALOG_RATE_LESS_ZERO"),
                    resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }
        Rate rate = TradeDesk.getInst().getRates().getRate(mOrder.getCurrency());
        double entryStop = rate.getFXCMCondDistEntryStop();
        if (entryStop == 0) {
            entryStop = TradeDesk.getConditionalEntryDistance();
        }
        entryStop++;

        double entryLimit = rate.getFXCMCondDistEntryLimit();
        if (entryLimit == 0) {
            entryLimit = TradeDesk.getConditionalEntryDistance();
        }
        entryLimit++;
        if (entryStop > 0) {
            double pip = TradeDesk.getPipsPrice(mOrder.getCurrency());
            double price = mOrder.getOfferRate();
            double maxPipDist = 5000 * pip;
            double maxPrice = price + maxPipDist;
            double minPrice = price - maxPipDist;
            System.out.println("entryStop = " + entryStop);
            System.out.println("entryLimit = " + entryLimit);
            System.out.println("maxPipDist = " + maxPipDist);
            System.out.println("value = " + value);
            System.out.println("price = " + price);
            System.out.println("maxPrice = " + maxPrice);
            System.out.println("minPrice = " + minPrice);
            if (value == price) {
                JOptionPane.showMessageDialog(this,
                                              resmng.getString("IDS_ENTRY_DIALOG_RATE_VALUE_ERROR"),
                                              resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                              JOptionPane.ERROR_MESSAGE);
                mRateSpinner.requestFocus();
                return false;
            }
            double adjustedPosPrice;
            double adjustedNegPrice;
            if (mOrder.getSide() == Side.BUY) {
                adjustedPosPrice = price + pip * entryStop;
                adjustedNegPrice = price - pip * entryLimit;
            } else {
                adjustedNegPrice = price - pip * entryStop;
                adjustedPosPrice = price + pip * entryLimit;
            }
            System.out.println("adjustedBuyPrice = " + adjustedNegPrice);
            System.out.println("adjustedSellPrice = " + adjustedPosPrice);
            if (value > price && value < adjustedPosPrice || value < minPrice) {
                JOptionPane.showMessageDialog(this,
                                              resmng.getString("IDS_ENTRY_DIALOG_RATE_LESS_ALLOWABLE"),
                                              resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                              JOptionPane.ERROR_MESSAGE);
                mRateSpinner.requestFocus();
                return false;
            } else if (value < price && value > adjustedNegPrice || value > maxPrice) {
                JOptionPane.showMessageDialog(this,
                                              resmng.getString("IDS_ENTRY_DIALOG_RATE_MORE_ALLOWABLE"),
                                              resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                              JOptionPane.ERROR_MESSAGE);
                mRateSpinner.requestFocus();
                return false;
            }
        }
        return true;
    }

    /**
     * Default actor.
     * This class respond for enabling of <code>ok</code> and
     * <code>cancel</code> buttons.
     */
    private class DefaultActor implements IDefaultActor {
        public void setEnabled(boolean aEnabled) {
            //checking: was selected order deleted or not
            if (mOrdersComboBox.getSelectedIndex() == -1) {
                mOrder = null;
            }
            if (aEnabled) {
                if (isDialogEnabled()) {
                    mOKButton.setEnabled(true);
                }
            } else {
                mOKButton.setEnabled(false);
            }
        }
    }

    /**
     * Orders combo box listener.
     */
    private class OrdersComboBoxSelectItemListener implements ItemListener {
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                try {
                    TradeDesk tradeDesk = TradeDesk.getInst();
                    Orders orders = tradeDesk.getOrders();
                    mOrder = orders.getOrder(mOrdersComboBox.getSelectedOrder());
                    mCustomTextTextField.setText(mOrder.getCustomText());
                    fillRateTextField();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
