/*
 * $Header:$
 *
 * Copyright (c) 2009 FXCM, LLC.
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
 * 05/22/2006   Andre Mermegas: fix to use JSpinner instead of hacked scrollbars
 * 10/04/2006   Andre Mermegas: scrollwheel support in spinner
 * 05/11/2007   Andre Mermegas: spinner alight left
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.RateSpinner;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
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
 * This dialog allows to user to enter parameters for create new entry order
 */
public class CreateEntryOrderDialog extends AmountDialog {
    private String mAccount;
    private AccountsComboBox mAccountComboBox;
    private long mAmount;
    private AmountsComboBox mAmountComboBox;
    private JLabel mConditionLabel1;
    private JLabel mConditionLabel2;
    private String mCurrency;
    private CurrenciesComboBox mCurrencyComboBox;
    private JTextField mCustomTextTextField;
    private int mExitCode;
    private JButton mOKButton;
    private RateSpinner mRateSpinner;
    private Side mSide;
    private KeySensitiveComboBox mSideComboBox;

    /**
     * Creates new form CreateEntryOrderDialog.
     *
     * @param aParent parent window
     */
    public CreateEntryOrderDialog(Frame aParent) {
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

    /**
     * Checks all controls to allow the default action to be enable.
     *
     * @return status
     */
    protected boolean checkAllControls() {
        try {
            Item item = (Item) mCurrencyComboBox.getSelectedItem();
            if (item == null) {
                return false;
            }
            if (!item.isEnabled()) {
                return false;
            }
            item = (Item) mAccountComboBox.getSelectedItem();
            return item != null && item.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the dialog
     */
    @Override
    public void closeDialog(int aExitCode) {
        mExitCode = aExitCode;
        super.closeDialog(aExitCode);
    }

    /**
     * Sets ability of the dialog.
     */
    @Override
    public void enableDialog(boolean aEnabled) {
        if (aEnabled) {
            //check own status, allowing to be enable
            if (checkAllControls()) {
                mOKButton.setEnabled(true);
            }
        } else {
            mOKButton.setEnabled(false);
        }
        super.enableDialog(aEnabled);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form field Rate.
     */
    private void fillRateTextField() {
        double selectedPrice = getSelectedPrice();
        if (selectedPrice > 0) {
            Side side = getSelectedSide();
            double entryPipDistance = getSelectedRate().getFXCMCondDistEntryStop();
            if (entryPipDistance == 0) {
                entryPipDistance = TradeDesk.getConditionalEntryDistance();
            }
            entryPipDistance += 1;
            double pipsPrice = TradeDesk.getPipsPrice(getSelectedCurrency());
            double buyPrice = selectedPrice + pipsPrice * entryPipDistance;
            double sellPrice = selectedPrice - pipsPrice * entryPipDistance;
            mRateSpinner.setCurrency(getSelectedCurrency());
            mRateSpinner.setValue(side == Side.BUY ? buyPrice : sellPrice);
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form field Side.
     */
    private void fillSideComboBox() {
        mSideComboBox.addItem(Side.BUY.getName());
        mSideComboBox.addItem(Side.SELL.getName());
        int iDefault = -1;
        if (mSide == Side.BUY) {
            iDefault = 0;
        } else if (mSide == Side.SELL) {
            iDefault = 1;
        }
        mSideComboBox.setSelectedIndex(iDefault);
    }

    /**
     * @return account id
     */
    public String getAccount() {
        return mAccount;
    }

    /**
     * @return amount
     */
    public long getAmount() {
        return mAmount;
    }

    /**
     * @return currency.
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * @return custom text
     */
    public String getCustomText() {
        return mCustomTextTextField.getText();
    }

    /**
     * @return rate.
     */
    public double getRate() {
        double value;
        try {
            value = Util.parseDouble(mRateSpinner.getValue().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
        try {
            value = Util.parseDouble(TradeDesk.formatPrice(getSelectedCurrency(), value));
        } catch (Exception e) {
            e.printStackTrace();
            value = 0;
        }
        return value;
    }

    /* Returns selected currency. */
    private String getSelectedCurrency() {
        return mCurrencyComboBox == null ? null : mCurrencyComboBox.getSelectedCurrency();
    }

    /**
     * @return price of selected currency.
     */
    private double getSelectedPrice() {
        Rate rate = getSelectedRate();
        Side side = getSelectedSide();
        if (rate == null || side == null) {
            return 0;
        }
        return side == Side.BUY ? rate.getBuyPrice() : rate.getSellPrice();
    }

    /**
     * @return selected rate.
     */
    private Rate getSelectedRate() {
        String ccy = getSelectedCurrency();
        return ccy == null ? null : TradeDesk.getInst().getRates().getRate(ccy);
    }

    /* Returns selected side. */
    private Side getSelectedSide() {
        int index = mSideComboBox.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        return index == 0 ? Side.BUY : Side.SELL;
    }

    /**
     * @return side.
     */
    public Side getSide() {
        return mSide;
    }

    /**
     * Initailzation of components.
     */
    @Override
    protected void initComponents() {
        //creating of components
        mOKButton = UIManager.getInst().createButton();
        JButton cancelButton = UIManager.getInst().createButton();
        JLabel accountLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_ACCOUNT"));
        JLabel currencyLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_CURRENCY"));
        JLabel sideLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_BUY_SELL"));
        JLabel rateLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_RATE"));
        JLabel amountLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_AMOUNT"));
        mAccountComboBox = new AccountsComboBox();
        mAccountComboBox.setDialog(this);
        mAccountComboBox.init(new DefaultActor());
        mAmountComboBox = new AmountsComboBox();
        mAmountComboBox.init(new DefaultActor());
        mAmountComboBox.setDialog(this);
        mAmountComboBox.setEditable(true);
        mConditionLabel1 = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_RATE_MORE"));
        mConditionLabel2 = UIManager.getInst().createLabel(getResMan().getString("IDS_ENTRY_DIALOG_RATE_LESS"));
        mCurrencyComboBox = new CurrenciesComboBox();
        mCurrencyComboBox.init(new DefaultActor());
        mCurrencyComboBox.setDialog(this);
        mSideComboBox = new KeySensitiveComboBox();
        mSideComboBox.setDialog(this);
        JLabel customTextLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_CUSTOM_TEXT"));
        mCustomTextTextField = UIManager.getInst().createTextField();

        mRateSpinner = UIManager.getInst().createRateSpinner();
        JFormattedTextField text = ((JSpinner.DefaultEditor) mRateSpinner.getEditor()).getTextField();
        text.setHorizontalAlignment(JTextField.LEFT);
        text.setFont(customTextLabel.getFont());
        text.setPreferredSize(new Dimension(100, 20));
        text.setEditable(true);
        text.setMargin(new Insets(0, 2, 0, 2));

        //sets dialog
        setModal(true);
        setTitle(getResMan().getString("IDS_ENTRY_DIALOG_TITLE"));
        setBackground(Color.WHITE);

        //sets main panel
        getContentPane().setLayout(new RiverLayout());

        mOKButton.setText(getResMan().getString("IDS_ENTRY_DIALOG_OK"));
        getRootPane().setDefaultButton(mOKButton);
        mOKButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvt) {
                if (verify()) {
                    closeDialog(JOptionPane.OK_OPTION);
                }
            }
        }
        );
        cancelButton.setText(getResMan().getString("IDS_ENTRY_DIALOG_CANCEL"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvt) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
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

        //Accounts comboBox
        mAccountComboBox.selectAccount(mAccount);
        mAccountComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent aEvent) {
                if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                    setAccount(aEvent.getItem().toString());
                    if (mAccountComboBox.isShowing()) {
                        mAmountComboBox.setSelectedIndex(0);
                    }
                }
            }
        });

        //Currency comboBox
        mCurrencyComboBox.selectCurrency(mCurrency);
        mAmountComboBox.setSelectedIndex(0);
        mCurrencyComboBox.addItemListener(new CurrencyComboBoxSelectItemListener());

        //SideComboBox
        fillSideComboBox();
        mSideComboBox.addItemListener(new SideComboBoxSelectItemListener());

        fillRateTextField();

        getContentPane().add("left", accountLabel);
        getContentPane().add("tab hfill", mAccountComboBox);

        getContentPane().add("br left", currencyLabel);
        getContentPane().add("tab hfill", mCurrencyComboBox);

        getContentPane().add("br left", sideLabel);
        getContentPane().add("tab hfill", mSideComboBox);

        getContentPane().add("br left", amountLabel);
        getContentPane().add("tab hfill", mAmountComboBox);

        getContentPane().add("br left", rateLabel);
        getContentPane().add("tab hfill", mRateSpinner);

        getContentPane().add("br left", customTextLabel);
        getContentPane().add("tab hfill", mCustomTextTextField);

        //sets texts for mLimitTextField and mStopTextField

        Util.setAllToBiggest(new JComponent[]{mOKButton, cancelButton});
        getContentPane().add("br center", mOKButton);
        getContentPane().add("", cancelButton);
    }

    /**
     * @param aAccount account id.
     */
    public void setAccount(String aAccount) {
        mAccount = aAccount;
        Account acct = (Account) TradeDesk.getInst().getAccounts().get(aAccount);
        Rate rate = TradeDesk.getInst().getRates().getRate(mCurrency);
        if (rate == null || rate.isForex()) {
            mAmountComboBox.setContractSize((long) acct.getBaseUnitSize());
        }
        if (mAccountComboBox != null) {
            mAccountComboBox.selectAccount(aAccount);
        }
    }

    /**
     * @param aCurrency the value of currency
     */
    public void setCurrency(String aCurrency) {
        mCurrency = aCurrency;
        if (mCurrencyComboBox != null) {
            mCurrencyComboBox.selectCurrency(mCurrency);
        }
        if (mAmountComboBox != null) {
            Rate rate = TradeDesk.getInst().getRates().getRate(aCurrency);
            if (rate == null || rate.isForex()) {
                Account acct = (Account) TradeDesk.getInst().getAccounts().get(mAccount);
                mAmountComboBox.setContractSize((long) acct.getBaseUnitSize());
            } else {
                mAmountComboBox.setContractSize(rate.getContractSize());
            }
        }
        fillRateTextField();
        mAmountComboBox.setSelectedIndex(0);
    }

    /**
     * @param aSide side.
     */
    public void setSide(Side aSide) {
        mSide = aSide;
        if (aSide != null) {
            mSideComboBox.setSelectedIndex(aSide == Side.BUY ? 0 : 1);
            if (aSide == Side.BUY) {
                mConditionLabel1.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_MORE"));
                mConditionLabel2.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_LESS"));
            } else {
                mConditionLabel1.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_LESS"));
                mConditionLabel2.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_MORE"));
            }
        } else {
            mSideComboBox.setSelectedIndex(-1);
        }
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
        setVisible(true);
        if (mExitCode == JOptionPane.OK_OPTION) {
            try {
                mCurrency = getSelectedCurrency();
                mAccount = mAccountComboBox.getSelectedAccount();
                mSide = getSelectedSide();
                mAmount = mAmountComboBox.getSelectedAmountLong();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mExitCode;
    }

    /**
     * Verifies of amount`s value.
     *
     * @return status
     */
    @Override
    protected boolean verify() {
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        //checking amount
        try {
            boolean rc = verifyAmount(mAmountComboBox) > 0;
            if (!rc) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        //checking rate for numberic value
        double value;
        try {
            value = Util.parseDouble(mRateSpinner.getValue().toString());
        } catch (NumberFormatException e) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(this,
                                          resmng.getString("IDS_ENTRY_DIALOG_RATE_NUMBER_ERROR"),
                                          resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (value <= 0) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(this,
                                          resmng.getString("IDS_ENTRY_DIALOG_RATE_LESS_ZERO"),
                                          resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }
        double entryStop = getSelectedRate().getFXCMCondDistEntryStop();
        if (entryStop == 0) {
            entryStop = TradeDesk.getConditionalEntryDistance();
        }
        entryStop++;

        double entryLimit = getSelectedRate().getFXCMCondDistEntryLimit();
        if (entryLimit == 0) {
            entryLimit = TradeDesk.getConditionalEntryDistance();
        }
        entryLimit++;
        if (entryStop > 0) {
            double pip = TradeDesk.getPipsPrice(getSelectedCurrency());
            double price = getSelectedPrice();
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
            if (mSide == Side.BUY) {
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
     * Currency combo box listener.
     */
    private class CurrencyComboBoxSelectItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected.
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                if (mCurrencyComboBox.getSelectedIndex() < 0) {
                    return;
                }
                //sets values of rate, stop and limit fields
                fillRateTextField();
                mAmountComboBox.setSelectedIndex(0);
            }
        }
    }

    /**
     * Default actor.
     */
    private class DefaultActor implements IDefaultActor {
        /**
         * Sets dialog`s ability.
         */
        public void setEnabled(boolean aEnabled) {
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
     * Side combo box listener.
     */
    private class SideComboBoxSelectItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected.
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                if (mSideComboBox.getSelectedIndex() < 0) {
                    return;
                }
                try {
                    if (mSideComboBox.getSelectedIndex() == 0) {
                        mSide = Side.BUY;
                        mConditionLabel1.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_MORE"));
                        mConditionLabel2.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_LESS"));
                    } else {
                        mSide = Side.SELL;
                        mConditionLabel1.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_LESS"));
                        mConditionLabel2.setText(getResMan().getString("IDS_ENTRY_DIALOG_RATE_MORE"));
                    }
                    fillRateTextField();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
