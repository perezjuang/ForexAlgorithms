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
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
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
import java.awt.event.KeyEvent;
import java.util.Date;

/**
 * @author Andre Mermegas
 *         Date: Apr 4, 2006
 *         Time: 11:19:32 AM
 */
public class ShowQuoteDialog extends ABaseDialog {
    public static final int BUY = 3;
    public static final int SELL = 4;
    private JPanel mButtonPanel;
    private JButton mBuyButton;
    private JButton mCancelButton;
    private JButton mSellButton;
    private JLabel mBuyPriceLabel;
    private JLabel mCurrencyLabel;
    private String mCurrency;
    private JLabel mExpirationDateLabel;
    private JLabel mSellPriceLabel;
    private Date mExpirationDate;
    private ResourceManager mResMan;
    private double mSellPrice;
    private double mBuyPrice;
    private int mExitCode;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public ShowQuoteDialog(Frame aOwner) {
        super(aOwner);
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
     *
     * @return buy price
     */
    public double getBuyPrice() {
        return mBuyPrice;
    }

    /**
     *
     * @return currency
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     *
     * @return expiration date
     */
    public Date getExpirationDate() {
        return mExpirationDate;
    }

    /**
     *
     * @return sell price
     */
    public double getSellPrice() {
        return mSellPrice;
    }

    protected void initComponents() {
        mButtonPanel = new JPanel();
        mBuyButton = UIManager.getInst().createButton(mResMan.getString("IDS_BUY_TEXT"));
        mSellButton = UIManager.getInst().createButton(mResMan.getString("IDS_SELL_TEXT"));
        mCancelButton = UIManager.getInst().createButton(mResMan.getString("IDS_MARKET_DIALOG_CANCEL"));
        getContentPane().setLayout(new RiverLayout());
        setModal(true);
        setTitle(mResMan.getString("IDS_REQUEST_FOR_QUOTE_TITLE"));
        setBackground(Color.WHITE);
        //sets main panel
        mButtonPanel.setLayout(new RiverLayout());
        getRootPane().setDefaultButton(mBuyButton);
        mBuyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(BUY);
            }
        });
        mSellButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(SELL);
            }
        });
        mButtonPanel.add(mBuyButton);
        mButtonPanel.add(mSellButton);
        mButtonPanel.add(mCancelButton);
        Util.setAllToBiggest(new JComponent[]{mBuyButton, mSellButton, mCancelButton});
        mCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exiting by escape
        mCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                         "Exit");
        mCancelButton.getActionMap().put("Exit", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        mCurrencyLabel = UIManager.getInst().createLabel();
        mBuyPriceLabel = UIManager.getInst().createLabel();
        mSellPriceLabel = UIManager.getInst().createLabel();
        mExpirationDateLabel = UIManager.getInst().createLabel();
        getContentPane().add("left", mCurrencyLabel);
        getContentPane().add("br left", mBuyPriceLabel);
        getContentPane().add("br left", mSellPriceLabel);
        getContentPane().add("br left", mExpirationDateLabel);
        getContentPane().add("br center", mButtonPanel);
    }

    /**
     *
     * @param aBuyPrice buy price
     */
    public void setBuyPrice(double aBuyPrice) {
        mBuyPrice = aBuyPrice;
        if (mBuyPrice > 0) {
            mBuyPriceLabel.setText("Buy Price: " + TradeDesk.formatPrice(mCurrency, mBuyPrice));
        } else {
            mButtonPanel.remove(mBuyButton);
        }
    }

    /**
     *
     * @param aCurrency currency
     */
    public void setCurrency(String aCurrency) {
        mCurrency = aCurrency;
        mCurrencyLabel.setText("Currency: " + getCurrency());
    }

    /**
     *
     * @param aExpirationDate expiration date
     */
    public void setExpirationDate(Date aExpirationDate) {
        mExpirationDate = aExpirationDate;
        mExpirationDateLabel.setText("Expiration Time: " + getExpirationDate());
    }

    /**
     *
     * @param aSellPrice sell price
     */
    public void setSellPrice(double aSellPrice) {
        mSellPrice = aSellPrice;
        if (mSellPrice > 0) {
            mSellPriceLabel.setText("Sell Price: " + TradeDesk.formatPrice(mCurrency, mSellPrice));
        } else {
            mButtonPanel.remove(mSellButton);
        }
    }

    @Override
    public int showModal() {
        new Thread(new Runnable() {
            public void run() {
                Date expires = getExpirationDate();
                Date now = new Date();
                long diffMillis = now.getTime() - expires.getTime();
                int diffSecs = (int) (diffMillis / 1000); //difference in seconds
                for (int i = diffSecs; i < 0; i++) {
                    try {
                        mSellButton.setText(mResMan.getString("IDS_SELL_TEXT") + " " + Math.abs(i));
                        mBuyButton.setText(mResMan.getString("IDS_BUY_TEXT") + " " + Math.abs(i));
                        Util.setAllToBiggest(new JComponent[]{mBuyButton, mSellButton, mCancelButton});
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isShowing()) { // catch if timed out, cancel the order
                    closeDialog(JOptionPane.CANCEL_OPTION);
                }
            }
        }).start();
        mExitCode = JOptionPane.CANCEL_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return mExitCode;
    }
}
