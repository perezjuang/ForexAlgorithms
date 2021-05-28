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
 * 10/10/2006   Andre Mermegas: added mousewheel support to atmarket spinner
 * 05/11/2007   Andre Mermegas: spinner alight left
 *
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.Util;
import fxts.stations.util.signals.ChangeSignal;
import fxts.stations.util.signals.RemoveSignal;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * ClosePositionDialog allows to client to close earlier open by him position.<br>
 * <br>
 * <ul>
 * <li> Herewith it can choose any one of its positions.</li>
 * <li> When closing is checked admissibility to this operations.</li>
 * </ul>
 * <br>
 * Creation date (24/09/2003 13:36 )
 */
public class ClosePositionDialog extends AmountDialog implements ISignalListener {
    private long mAmount;
    private CloseAmountsComboBox mAmountComboBox;
    private boolean mAtBest = true;
    private JSpinner mAtMarketSpinner;
    private JTextField mCustomTextTextField;
    private int mExitCode;
    private JButton mOKButton;
    private JTextField mRateTextField;
    private PositionsComboBox mTicketComboBox;
    private String mTicketID;

    /**
     * Constructor ClosePositionDialog
     * <br> Determine current trade desk, resource manager,
     * <br> create window of dialog.
     *
     * @param aParent parent window
     */
    public ClosePositionDialog(Frame aParent) {
        super(aParent);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }

            //Creates main panel
            initComponents();
            pack();
            mCustomTextTextField.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Checks all controls to allow the default action to be enabled. */
    protected boolean checkAllControls() {
        Item item = (Item) mTicketComboBox.getSelectedItem();
        return item != null && item.isEnabled();
    }

    /**
     * closeDialog.
     * <br>Unsubscribe from getting data. Switches off window.<br>
     *
     * @param aExitCode Code of terminations
     */
    @Override
    public void closeDialog(int aExitCode) {
        mExitCode = aExitCode;
        super.closeDialog(aExitCode);
    }

    /**
     * enableDialog.
     * <br> Enable or not Close position dialog from up command and
     * <br> check admissibility given operations by itself.
     *
     * @param aEnabled direct enable (if bEnabled is true) or
     * disable (if bEnabled is false) dialog
     */
    @Override
    public void enableDialog(boolean aEnabled) {
        if (aEnabled) {
            //check own status, allowing to be enable, only in this case:
            if (checkAllControls()) {
                mOKButton.setEnabled(true);
            }
        } else {
            mOKButton.setEnabled(false);
        }
        super.enableDialog(aEnabled);
    }

    /**
     * Method provided external calling
     *
     * @return ammount
     */
    public long getAmount() {
        return mAmount;
    }

    /**
     * @return at market value
     */
    public int getAtMarket() {
        return Integer.parseInt(mAtMarketSpinner.getValue().toString());
    }

    /**
     * @return custom text
     */
    public String getCustomText() {
        return mCustomTextTextField.getText();
    }

    /**
     * @return ticket id
     */
    public String getTicketID() {
        return mTicketID;
    }

    /**
     * Composition window of dialog.
     */
    @Override
    protected void initComponents() {
        UIManager uiManager = UIManager.getInst();
        JLabel ticketLabel = uiManager.createLabel(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_TICKET"));
        JLabel amountLabel = uiManager.createLabel(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_AMOUNT"));
        JLabel rateLabel = uiManager.createLabel(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_RATE"));
        JLabel customTextLabel = uiManager.createLabel(getResMan().getString("IDS_CUSTOM_TEXT"));
        mTicketComboBox = new PositionsComboBox();
        mTicketComboBox.init(new DefaultActor());
        mTicketComboBox.setDialog(this);
        mAmountComboBox = new CloseAmountsComboBox();
        mAmountComboBox.init(new DefaultActor());
        mAmountComboBox.setDialog(this);
        mOKButton = uiManager.createButton(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_OK"));
        JButton cancelButton = uiManager.createButton(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_CANCEL"));
        mRateTextField = uiManager.createTextField();
        mRateTextField.setEditable(false);
        mCustomTextTextField = uiManager.createTextField();
        mAtMarketSpinner = uiManager.createSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        mAtMarketSpinner.setEnabled(false);
        JFormattedTextField text = ((JSpinner.DefaultEditor) mAtMarketSpinner.getEditor()).getTextField();
        ((DefaultFormatter) text.getFormatter()).setAllowsInvalid(false);
        text.setHorizontalAlignment(JTextField.LEFT);
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
        setModal(true);
        setTitle(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_TITLE"));
        setBackground(Color.WHITE);

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

        mTicketComboBox.selectPosition(mTicketID);
        mTicketComboBox.addItemListener(new TicketComboBoxSelectItemListener());
        mAmountComboBox.setEditable(true);

        //assign behaviour a window
        getRootPane().setDefaultButton(mOKButton);
        // Set-select of user is a YES-analogy
        mOKButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (verify()) {
                    closeDialog(JOptionPane.YES_OPTION);
                }
            }
        });
        // Cancel-select of user is already Cancel
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        String[] options = new String[]{mResMan.getString("IDS_AT_BEST"), mResMan.getString("IDS_AT_MARKET")};
        JComboBox orderTypeBox = new JComboBox(options);
        orderTypeBox.setFocusable(true);
        orderTypeBox.addActionListener(new ActionListener() {
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

        getContentPane().setLayout(new RiverLayout());
        getContentPane().add("br left", ticketLabel);
        getContentPane().add("tab hfill", mTicketComboBox);
        getContentPane().add("br left", amountLabel);
        getContentPane().add("tab hfill", mAmountComboBox);
        getContentPane().add("br left", rateLabel);
        getContentPane().add("tab hfill", mRateTextField);
        getContentPane().add("br left", customTextLabel);
        getContentPane().add("tab hfill", mCustomTextTextField);
        getContentPane().add("br left", orderTypeBox);
        getContentPane().add("tab hfill", mAtMarketSpinner);
        getContentPane().add("br center", mOKButton);
        getContentPane().add("center", cancelButton);
        Util.setAllToBiggest(new JComponent[]{mOKButton, cancelButton});
    }

    public boolean isAtBest() {
        return mAtBest;
    }

    public void setAtBest(boolean aAtBest) {
        mAtBest = aAtBest;
    }

    /**
     * Public method onSignal. <br>
     * Used tacit by object ISignalsMediator, for processing the signals
     * to which we have subscribed.
     *
     * @param aSignal type incoming of signal.
     * @param aSrc source of signal. In this instance source can be only one.
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal.getType() == SignalType.CHANGE) {
            try {
                ChangeSignal signal = (ChangeSignal) aSignal;
                Position element = (Position) signal.getNewElement();
                if (element.getTicketID().equals(mTicketComboBox.getSelectedPosition())) {
                    mRateTextField.setText(TradeDesk.formatPrice(element.getCurrency(), element.getClosePrice()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.REMOVE) {
            try {
                RemoveSignal signal = (RemoveSignal) aSignal;
                Position element = (Position) signal.getElement();
                if (element.getTicketID().equals(mTicketComboBox.getSelectedPosition())) {
                    String msg = "The currently selected open position has been already closed."
                                 + "\nPlease, choose another open position";
                    JOptionPane.showMessageDialog(getOwner(), msg, null, JOptionPane.ERROR_MESSAGE);
                    mTicketComboBox.setSelectedIndex(-1);
                    mAmountComboBox.setSelectedIndex(-1);
                    mRateTextField.setText("");
                }
                if (mTicketComboBox.getItemCount() == 0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            closeDialog(JOptionPane.CANCEL_OPTION);
                        }
                    });
                }
                mTicketComboBox.selectPosition(mTicketComboBox.getSelectedPosition()); //xxx reselect to make sure
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.ADD) {
            mTicketComboBox.selectPosition(mTicketComboBox.getSelectedPosition()); //xxx reselect to make sure
        }
    }

    /**
     * setTicketID.
     * Positioned combo box on required position (with the equal ticket).
     * <br>Updates value of amount depending on this.
     *
     * @param aTicketID concrete ticket of position to be chosen.
     */
    public void setTicketID(String aTicketID) {
        try {
            mTicketID = aTicketID;
            mTicketComboBox.selectPosition(mTicketID);
            Position position = TradeDesk.getInst().getOpenPositions().getPosition(mTicketID);
            Account acct = (Account) TradeDesk.getInst().getAccounts().get(position.getAccount());
            mAmount = position.getAmount();
            mRateTextField.setText(TradeDesk.formatPrice(position.getCurrency(), position.getClosePrice()));
            mAmountComboBox.setMaximumValue(mAmount);
            Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
            if (rate == null || rate.isForex()) {
                mAmountComboBox.setContractSize((long) acct.getBaseUnitSize());
            } else {
                mAmountComboBox.setContractSize(rate.getContractSize());
            }
            mAmountComboBox.setSelectedIndex(mAmountComboBox.getComboBoxModel().getSize() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Public method showModal. <br>
     * Assign behaviour of window, subscribe on changing the openning positions,
     * show window and process its closing.
     *
     * @throws NumberFormatException when input rate is incorrect (unlikely).
     *                               returns Code of terminations
     */
    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.REMOVE);
        setVisible(true);
        //Store all incorporated values
        if (mExitCode == JOptionPane.OK_OPTION) {
            //Determing selected ticket and get him
            mTicketID = mTicketComboBox.getSelectedPosition();
            try {
                mAmount = mAmountComboBox.getSelectedAmountLong();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.REMOVE);
        return mExitCode;
    }

    /* Verifies user input
    * return true when all conditions is OK
    * return false when one from condition is violate.
    */
    @Override
    protected boolean verify() {
        Position position = TradeDesk.getInst().getOpenPositions().getPosition(mTicketID);
        Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
        Side side = position.getSide();
        if (side == Side.BUY && !rate.isSellTradable() || side == Side.SELL && !rate.isBuyTradable()) {
            JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                          "There is no tradable price. (You cannot trade at this price)",
                                          mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            return verifyAmount(mAmountComboBox,
                                TradeDesk.getInst().getOpenPositions().getPosition(mTicketID).getAmount(),
                                "IDS_CLOSEPOSITION_DIALOG_MAX_ERROR_MESSAGE") > 0;
        }
    }

    /**
     * DefaultActor
     * .<br>
     * <br>Used to disable default button on dialog, check status and
     * .<br> depending on this enable/disable OK buton.
     * <br>
     * Creation date (07.10.2003 17:08)
     */
    private class DefaultActor implements IDefaultActor {
        /* Enables/disbles parent dialog*/
        public void setEnabled(boolean aEnabled) {
            if (mTicketComboBox.getSelectedIndex() == -1) {
                mRateTextField.setText("");
            }
            if (aEnabled) {
                if (isDialogEnabled() && checkAllControls()) {
                    mOKButton.setEnabled(true);
                }
            } else {
                mOKButton.setEnabled(false);
            }
        }
    }

    /**
     * TicketComboBoxSelectItemListener
     * .<br>
     * <br>Intercepts change of combobox and updates its current contents.
     * .<br>
     * <br>
     * Creation date (07.10.2003 17:07)
     */
    private class TicketComboBoxSelectItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected.
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                String ticketID = mTicketComboBox.getSelectedPosition();
                Position position = TradeDesk.getInst().getOpenPositions().getPosition(ticketID);
                Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
                if (rate == null || rate.isForex()) {
                    Account acct = (Account) TradeDesk.getInst().getAccounts().get(position.getAccount());
                    mAmountComboBox.setContractSize((long) acct.getBaseUnitSize());
                } else {
                    mAmountComboBox.setContractSize(rate.getContractSize());
                }
                mAmountComboBox.setSelectedIndex(0);
                setTicketID(ticketID);
            }
        }
    }
}
