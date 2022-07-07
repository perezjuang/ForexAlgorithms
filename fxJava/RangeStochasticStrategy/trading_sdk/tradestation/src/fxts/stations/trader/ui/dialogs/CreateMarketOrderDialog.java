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
import javax.swing.text.DefaultFormatter;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * This class allows to user to enter parameters for create new market order.<br>
 * <br>
 * Creation date (9/24/2003 1:36 PM)
 */
public class CreateMarketOrderDialog extends AmountDialog implements ISignalListener {
    private String mAccount;
    private AccountsComboBox mAccountComboBox;
    private long mAmount;
    private AmountsComboBox mAmountComboBox;
    private boolean mAtBest = true;
    private JSpinner mAtMarketSpinner;
    private String mCurrency;
    private CurrenciesComboBox mCurrencyComboBox;
    private JTextField mCustomTextTextField;
    private int mExitCode;
    private JButton mOKButton;
    private JTextField mRateTextField;
    private Side mSide;
    private KeySensitiveComboBox mSideComboBox;

    /**
     * Constructor.
     *
     * @param aParent parent window
     */
    public CreateMarketOrderDialog(Frame aParent) {
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

    /* checks all controls to allow the default action to be enabler */
    protected boolean checkAllControls() {
        Item item = (Item) mCurrencyComboBox.getSelectedItem();
        if (item == null) {
            return false;
        }
        if (!item.isEnabled()) {
            return false;
        }
        item = (Item) mAccountComboBox.getSelectedItem();
        return item != null && item.isEnabled();
    }

    /**
     * Closes the dialog with specified returned code.
     *
     * @param aIExitCode code of exiting
     */
    @Override
    public void closeDialog(int aIExitCode) {
        mExitCode = aIExitCode;
        super.closeDialog(aIExitCode);
    }

    /**
     * Method is called by App Window to notify dialog about situation when
     * its default action cannot be performed.
     * Sets/reset default button to enable state consider to internal state
     * If paramter is true, sets the dialog enable if
     * the logic of checking internal conditions to be enabled allowes that.
     * If paramtere is false, sets dialog disabled in all cases
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

    /* This method is called from the constructor to
     * initialize the form field Rate.
     */
    private void fillRateTextField() {
        mRateTextField.setText(getSelectedPriceString());
    }

    /* This method is called from the constructor to
     * initialize the form field Side.
     */
    private void fillSideComboBox() {
        mSideComboBox.addItem(Side.BUY.getName());
        mSideComboBox.addItem(Side.SELL.getName());
        int idx = -1;
        if (mSide == Side.BUY) {
            idx = 0;
        } else if (mSide == Side.SELL) {
            idx = 1;
        }
        mSideComboBox.setSelectedIndex(idx);
    }

    /**
     * @return initial or changed Account ID
     */
    public String getAccount() {
        return mAccount;
    }

    /**
     * @return initial or changed amount of operation.
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
     * @return initial or changed currency pair
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
     * @return current rate valur.
     */
    public double getRate() {
        return getSelectedPrice();
    }

    /* returns selected currency */
    private String getSelectedCurrency() {
        return mCurrencyComboBox == null ? null : mCurrencyComboBox.getSelectedCurrency();
    }

    /* returns current price of selected currency consider current side of operation*/
    private double getSelectedPrice() {
        Rate rate = getSelectedRate();
        Side side = getSelectedSide();
        return rate == null || side == null ? 0.0 : side == Side.BUY ? rate.getBuyPrice() : rate.getSellPrice();
    }

    /* returns String presentation of current price*/
    private String getSelectedPriceString() {
        String sCurrency = getSelectedCurrency();
        if (sCurrency == null) {
            return "";
        }
        Rate rate = TradeDesk.getInst().getRates().getRate(sCurrency);
        Side side = getSelectedSide();
        if (rate == null || side == null) {
            return "";
        }
        return TradeDesk.formatPrice(sCurrency, side == Side.BUY ? rate.getBuyPrice() : rate.getSellPrice());
    }

    /* returns selected Rate object*/
    private Rate getSelectedRate() {
        String sCurrency = getSelectedCurrency();
        return sCurrency == null ? null : TradeDesk.getInst().getRates().getRate(sCurrency);
    }

    /* returns selected amount of operation*/
    private Side getSelectedSide() {
        int index = mSideComboBox.getSelectedIndex();
        if (index < 0) {
            return null;
        }
        return index == 0 ? Side.BUY : Side.SELL;
    }

    /**
     * @return initial or changed Side of operation
     */
    public Side getSide() {
        return mSide;
    }

    /**
     * This method is called from the constructor to
     * initialize the form.
     */
    @Override
    protected void initComponents() {
        mOKButton = UIManager.getInst().createButton();
        JButton cancelButton = UIManager.getInst().createButton();
        JLabel accountLabel = UIManager.getInst().createLabel();
        JLabel currencyLabel = UIManager.getInst().createLabel();
        JLabel buySellLabel = UIManager.getInst().createLabel();
        JLabel rateLabel = UIManager.getInst().createLabel();
        JLabel amountLabel = UIManager.getInst().createLabel();
        mAccountComboBox = new AccountsComboBox();
        mAccountComboBox.setDialog(this);
        mAccountComboBox.init(new DefaultActor());
        mCurrencyComboBox = new CurrenciesComboBox();
        mCurrencyComboBox.setDialog(this);
        mCurrencyComboBox.init(new DefaultActor());
        mSideComboBox = new KeySensitiveComboBox();
        mSideComboBox.setDialog(this);
        mAmountComboBox = new AmountsComboBox();
        mAmountComboBox.setDialog(this);
        mAmountComboBox.init(new DefaultActor());
        mAmountComboBox.setEditable(true);
        mRateTextField = UIManager.getInst().createTextField();
        JLabel customTextLabel = UIManager.getInst().createLabel();
        mCustomTextTextField = UIManager.getInst().createTextField();
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
        setModal(true);
        setTitle(getResMan().getString("IDS_MARKET_DIALOG_TITLE"));
        setBackground(Color.WHITE);

        //sets main panel
        mOKButton.setText(getResMan().getString("IDS_MARKET_DIALOG_OK"));
        getRootPane().setDefaultButton(mOKButton);
        mOKButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (verify()) {
                    closeDialog(JOptionPane.OK_OPTION);
                }
            }
        }
        );
        cancelButton.setText(getResMan().getString("IDS_MARKET_DIALOG_CANCEL"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        }
        );
        //sets for exiting by escape
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                        "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        accountLabel.setText(getResMan().getString("IDS_MARKET_DIALOG_ACCOUNT"));
        currencyLabel.setText(getResMan().getString("IDS_MARKET_DIALOG_CURRENCY"));
        buySellLabel.setText(getResMan().getString("IDS_MARKET_DIALOG_BUY_SELL"));
        amountLabel.setText(getResMan().getString("IDS_MARKET_DIALOG_AMOUNT"));
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
        mCurrencyComboBox.selectCurrency(mCurrency);
        fillSideComboBox();
        mSideComboBox.addItemListener(new SideComboBoxSelectItemListener());
        mRateTextField.setEditable(false);
        mRateTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent aEvent) {
                mOKButton.requestFocus();
            }
        });
        fillRateTextField();
        rateLabel.setText(getResMan().getString("IDS_MARKET_DIALOG_RATE"));
        customTextLabel.setText(mResMan.getString("IDS_CUSTOM_TEXT"));
        mCurrencyComboBox.addItemListener(new CurrencyComboBoxSelectItemListener());
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

        getContentPane().add("left", accountLabel);
        getContentPane().add("tab hfill", mAccountComboBox);

        getContentPane().add("br left", currencyLabel);
        getContentPane().add("tab hfill", mCurrencyComboBox);

        getContentPane().add("br left", buySellLabel);
        getContentPane().add("tab hfill", mSideComboBox);

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
     * This method is called when signal is fired by Rate table.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal.getType() == SignalType.CHANGE) {
            ChangeSignal changeSignal = (ChangeSignal) aSignal;
            int rate = changeSignal.getIndex();
            try {
                if (mCurrencyComboBox != null) {
                    if (rate == mCurrencyComboBox.getSelectedIndex()) {
                        mRateTextField.setText(getSelectedPriceString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param aAccount initial Account ID
     */
    public void setAccount(String aAccount) {
        Account acct = (Account) TradeDesk.getInst().getAccounts().get(aAccount);
        Rate rate = TradeDesk.getInst().getRates().getRate(mCurrency);
        if (rate == null || rate.isForex()) {
            mAmountComboBox.setContractSize((long) acct.getBaseUnitSize());
        }
        mAccount = aAccount;
        if (mAccountComboBox != null) {
            mAccountComboBox.selectAccount(aAccount);
        }
    }

    /**
     * @param aCurrency initial currency pair. Changes combo boxes states
     */
    public void setCurrency(String aCurrency) {
        mCurrency = aCurrency;
        if (mCurrencyComboBox != null) {
            mCurrencyComboBox.selectCurrency(mCurrency);
        }
        if (mAmountComboBox != null) {
            Rate rate = TradeDesk.getInst().getRates().getRate(aCurrency);
            if (rate != null && !rate.isForex()) {
                mAmountComboBox.setContractSize(rate.getContractSize());
            }
        }
        fillRateTextField();
        mAmountComboBox.setSelectedIndex(0);
    }

    /**
     * @param aSide initial Side of operation. Changes combo boxes states
     */
    public void setSide(Side aSide) {
        mSide = aSide;
        if (aSide != null) {
            mSideComboBox.setSelectedIndex(aSide == Side.BUY ? 0 : 1);
        } else {
            mSideComboBox.setSelectedIndex(-1);
        }
        fillRateTextField();
    }

    /**
     * this method shows login dialog to modal
     */
    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        //subscribe on add type of the signals
        TradeDesk.getInst().getRates().subscribe(this, SignalType.CHANGE);
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
        TradeDesk.getInst().getRates().unsubscribe(this, SignalType.CHANGE);
        return mExitCode;
    }

    @Override
    protected boolean verify() {
        Rate rate = TradeDesk.getInst().getRates().getRate(mCurrency);
        Side side = Side.getSide(mSideComboBox.getSelectedItem().toString());
        if (side == Side.BUY && !rate.isBuyTradable() || side == Side.SELL && !rate.isSellTradable()) {
            JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                          "There is no tradable price. (You cannot trade at this price)",
                                          mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            return verifyAmount(mAmountComboBox) > 0;
        }
    }

    /**
     * Class listens currency to change price
     */
    private class CurrencyComboBoxSelectItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected.
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                int selected = mCurrencyComboBox.getSelectedIndex();
                if (selected < 0) {
                    mRateTextField.setText("");
                    return;
                }
                try {
                    Rate rate = TradeDesk.getInst().getRates().getRate(mCurrencyComboBox.getSelectedCurrency());
                    if (rate != null && !rate.isForex()) {
                        mAmountComboBox.setContractSize(rate.getContractSize());
                        mAmountComboBox.setSelectedIndex(0);
                    }
                    mRateTextField.setText(getSelectedPriceString());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * Instance of this class is passed to combo boxes to notify about changing ability
     * of default action to be performed
     */
    private class DefaultActor implements IDefaultActor {
        /**
         * Sets ability of ok button.
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
     * Class listens side of operation changing to change price
     */
    private class SideComboBoxSelectItemListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected.
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                int selected = mSideComboBox.getSelectedIndex();
                if (selected < 0) {
                    mRateTextField.setText("");
                    return;
                }
                try {
                    mRateTextField.setText(getSelectedPriceString());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
