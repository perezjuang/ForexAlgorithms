/*
 * $Header:$
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
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Rate;
import fxts.stations.trader.TradeApp;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

/**
 * This class allows to user to enter parameters for create new market order.<br>
 * <br>
 * Creation date (9/24/2003 1:36 PM)
 */
public class RequestForQuoteDialog extends AmountDialog {
    private String mAccount;
    private AccountsComboBox mAccountComboBox;
    private long mAmount;
    private AmountsComboBox mAmountComboBox;
    private String mCurrency;
    private CurrenciesComboBox mCurrencyComboBox;
    private int mExitCode;
    private JButton mOKButton;

    /**
     * Constructor.
     *
     * @param aParent parent window
     */
    public RequestForQuoteDialog(Frame aParent) {
        super(aParent);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
            //creates main panel
            initComponents();
            pack();
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
            //ALWAYS!!!
            mOKButton.setEnabled(false);
        }
        //ALWAYS:
        super.enableDialog(aEnabled);
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
     * @return initial or changed currency pair
     */
    public String getCurrency() {
        return mCurrency;
    }

    /* returns selected currency */
    private String getSelectedCurrency() {
        return mCurrencyComboBox == null ? null : mCurrencyComboBox.getSelectedCurrency();
    }

    /**
     * This method is called from the constructor to
     * initialize the form.
     */
    @Override
    protected void initComponents() {
        getContentPane().setLayout(new RiverLayout());
        setModal(true);
        setTitle(getResMan().getString("IDS_REQUEST_FOR_QUOTE_TITLE"));
        setBackground(Color.WHITE);
        JPanel buttonPanel = new JPanel();
        mOKButton = UIManager.getInst().createButton(getResMan().getString("IDS_MARKET_DIALOG_OK"));
        JButton cancelButton = UIManager.getInst().createButton(getResMan().getString("IDS_MARKET_DIALOG_CANCEL"));
        mAccountComboBox = new AccountsComboBox();
        mAccountComboBox.setDialog(this);
        mAccountComboBox.init(new DefaultActor());
        mCurrencyComboBox = new CurrenciesComboBox();
        mCurrencyComboBox.setDialog(this);
        mCurrencyComboBox.init(new DefaultActor());
        mAmountComboBox = new AmountsComboBox();
        mAmountComboBox.setDialog(this);
        mAmountComboBox.init(new DefaultActor());
        mAmountComboBox.setEditable(true);

        buttonPanel.setLayout(new RiverLayout());
        getRootPane().setDefaultButton(mOKButton);
        mOKButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (verify()) {
                    closeDialog(JOptionPane.OK_OPTION);
                }
            }
        });
        buttonPanel.add(mOKButton);
        buttonPanel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exiting by escape
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        JLabel accountLabel = UIManager
                .getInst().createLabel(getResMan().getString("IDS_MARKET_DIALOG_ACCOUNT"));
        JLabel currencyLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_MARKET_DIALOG_CURRENCY"));
        JLabel amountLabel = UIManager.getInst().createLabel(getResMan().getString("IDS_MARKET_DIALOG_AMOUNT"));
        mAccountComboBox.selectAccount(mAccount);
        mCurrencyComboBox.selectCurrency(mCurrency);
        mCurrencyComboBox.addItemListener(new CurrencyComboBoxSelectItemListener());
        Util.setAllToBiggest(new JComponent[]{mOKButton, cancelButton});
        getContentPane().add("left", accountLabel);
        getContentPane().add("tab hfill", mAccountComboBox);

        getContentPane().add("br left", currencyLabel);
        getContentPane().add("tab hfill", mCurrencyComboBox);

        getContentPane().add("br left", amountLabel);
        getContentPane().add("tab hfill", mAmountComboBox);

        getContentPane().add("br center", buttonPanel);
    }

    /**
     * @param aAccount initial Account ID
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
     * @param aCurrency initial currency pair. Changes combo boxes states
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
            mAmountComboBox.setSelectedIndex(0);
        }
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
        setVisible(true);
        if (mExitCode == JOptionPane.OK_OPTION) {
            try {
                mCurrency = getSelectedCurrency();
                mAccount = mAccountComboBox.getSelectedAccount();
                mAmount = mAmountComboBox.getSelectedAmountLong();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mExitCode;
    }

    /* verifies user input */
    @Override
    protected boolean verify() {
        return verifyAmount(mAmountComboBox) > 0;
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
                int iSelected = mCurrencyComboBox.getSelectedIndex();
                if (iSelected < 0) {
                    return;
                }
                mAmountComboBox.setSelectedIndex(0);
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
}
