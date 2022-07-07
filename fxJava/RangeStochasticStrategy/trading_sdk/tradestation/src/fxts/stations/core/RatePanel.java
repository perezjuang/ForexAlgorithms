/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/RatePanel.java#3 $
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
 * $History: $
 *
 * 07/05/2006   Andre Mermegas: fix for tooltip text not showing correct number of decimal places,
 *                              show up/down cursor when not focused
 * 05/11/2007   Andre Mermegas: refactor
 * 10/30/2007   Andre Mermegas: bugfix, fraction was not updating
 * 05/14/2009   Andre Mermegas: CFD update
 */
package fxts.stations.core;

import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.NotDefinedException;
import com.fxcm.fix.TradingSecurity;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.trader.ui.dialogs.AmountsComboBox;
import fxts.stations.trader.ui.dialogs.DefaultActorImpl;
import fxts.stations.trader.ui.frames.AccountsFrame;
import fxts.stations.trader.ui.frames.AdvancedRatesFrame;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequestFactory;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.GradientLabel;
import fxts.stations.ui.ITable;
import fxts.stations.ui.ITableListener;
import fxts.stations.ui.ITableSelectionListener;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.TableManager;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ActionTypes;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.preferences.IUserPreferencesListener;
import fxts.stations.util.preferences.PreferencesManager;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 */
public class RatePanel extends JPanel implements ILocaleListener, IUserPreferencesListener, IClickModel, Observer {
    private static final Timer TIMER = new Timer("RatePanelTimer");
    private String mAccount;
    private AmountsComboBox mAmtComboBox;
    private Color mBuyFG;
    private GradientLabel mBuyFirst4Label;
    private GradientLabel mBuyFractionLabel;
    private GradientLabel mBuyLabel;
    private GradientLabel mBuyLast2Label;
    private double mBuyPrice;
    private boolean mBuyResetRunning;
    private boolean mBuyTradable = true;
    private Color mCCY1;
    private Color mCCY2;
    private Color mCCYFG;
    private Color mColorBG2Selected;
    private Color mColorBGSelected;
    private Color mColorFGDown;
    private Color mColorFGRaised;
    private String mCurrency;
    private GradientLabel mCurrencyLabel;
    private Color mHighPriceFG;
    private GradientLabel mHighPriceLabel;
    private Color mHL1;
    private Color mHL2;
    private Color mLabel1;
    private Color mLabel2;
    private Color mLowPriceFG;
    private GradientLabel mLowPriceLabel;
    private UserPreferences mPreferences;
    private Color mPrimaryColor;
    private Rate mRate;
    private Color mRateDate1;
    private Color mRateDate2;
    private Color mRateDateFG;
    private GradientLabel mRateDateLabel;
    private IRatePanelFocusListener mRatePanelFocusListener;
    private ResourceManager mRsm;
    private ITableSelectionListener mSelectionListener;
    private Color mSellFG;
    private GradientLabel mSellFirst4Label;
    private GradientLabel mSellFractionLabel;
    private GradientLabel mSellLabel;
    private GradientLabel mSellLast2Label;
    private double mSellPrice;
    private boolean mSellResetRunning;
    private boolean mSellTradable = true;
    private final SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("hh:mm:ss");
    private final DecimalFormat mSpreadFormat = new ThreadSafeNumberFormat().getInstance();
    private GradientLabel mSpreadLabel;
    private ITableListener mTableListener;
    private Cursor mTargetCursor;

    /**
     * constructor
     *
     * @param aRate rate
     * @param aRatesFrame frame
     */
    public RatePanel(Rate aRate, final AdvancedRatesFrame aRatesFrame) {
        super(new RiverLayout(0, 0), true);
        mRate = aRate;
        mRsm = aRatesFrame.getResourceManager();
        mPreferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        mBuyFG = mPreferences.getColor("AdvancedRate.buyFG");
        mLabel1 = mPreferences.getColor("AdvancedRate.label1");
        mLabel2 = mPreferences.getColor("AdvancedRate.label2");
        mSellFG = mPreferences.getColor("AdvancedRate.sellFG");
        mPrimaryColor = mPreferences.getColor("AdvancedRate.primaryColor");
        mHighPriceFG = mPreferences.getColor("AdvancedRate.highPriceFG");
        mLowPriceFG = mPreferences.getColor("AdvancedRate.lowPriceFG");
        mHL1 = mPreferences.getColor("AdvancedRate.hl1");
        mHL2 = mPreferences.getColor("AdvancedRate.hl2");
        mRateDate1 = mPreferences.getColor("AdvancedRate.rateDate1");
        mRateDate2 = mPreferences.getColor("AdvancedRate.rateDate2");
        mCCY1 = mPreferences.getColor("AdvancedRate.ccy1");
        mCCY2 = mPreferences.getColor("AdvancedRate.ccy2");
        mCCYFG = mPreferences.getColor("AdvancedRate.currencyFG");
        mRateDateFG = mPreferences.getColor("AdvancedRate.rateDateFG");
        mColorBG2Selected = mPreferences.getColor("AdvancedRate.selectedCcy2");
        mColorBGSelected = mPreferences.getColor("AdvancedRate.selectedCcy1");
        mColorFGRaised = mPreferences.getColor("AdvancedRate.foreground.raised");
        mColorFGDown = mPreferences.getColor("AdvancedRate.foreground.down");

        ClassLoader classLoader = getClass().getClassLoader();
        Toolkit tk = Toolkit.getDefaultToolkit();

        Image image = tk.getImage(classLoader.getResource("fxts/stations/trader/resources/images/target.png"));
        mTargetCursor = tk.createCustomCursor(image, new Point(0, 0), "mTargetCursor");

        MouseAdapter targetSellCursorListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent aEvent) {
                if (mRate.isSellBocked()) {
                    getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                } else {
                    getRootPane().setCursor(mTargetCursor);
                }
            }

            @Override
            public void mouseExited(MouseEvent aEvent) {
                getRootPane().setCursor(Cursor.getDefaultCursor());
            }
        };

        MouseAdapter targetBuyCursorListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent aEvent) {
                if (mRate.isBuyBlocked()) {
                    getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                } else {
                    getRootPane().setCursor(mTargetCursor);
                }
            }

            @Override
            public void mouseExited(MouseEvent aEvent) {
                getRootPane().setCursor(Cursor.getDefaultCursor());
            }
        };

        setBorder(BorderFactory.createEtchedBorder());
        addMouseListener(getStandardRFQListener(aRatesFrame));

        mCurrency = aRate.getCurrency();

        mBuyLabel = new GradientLabel.Builder()
                .text(mRsm.getString("IDS_BUY_TEXT"))
                .bg(mLabel1)
                .bg2(mLabel2)
                .showBorder(true)
                .fg(mBuyFG)
                .bottom(1)
                .right(1)
                .build();
        mBuyLabel.addMouseListener(getStandardBuyMouseListener(aRatesFrame));
        mBuyLabel.addMouseListener(targetBuyCursorListener);

        mSellLabel = new GradientLabel.Builder()
                .text(mRsm.getString("IDS_SELL_TEXT"))
                .bg(mLabel1)
                .bg2(mLabel2)
                .showBorder(true)
                .fg(mSellFG)
                .bottom(1)
                .left(1)
                .right(1)
                .build();
        mSellLabel.addMouseListener(getStandardSellMouseListener(aRatesFrame));
        mSellLabel.addMouseListener(targetSellCursorListener);

        mBuyPrice = aRate.getBuyPrice();
        mSellPrice = aRate.getSellPrice();

        mBuyFirst4Label = new GradientLabel.Builder()
                .text(getFirstHalf(mBuyPrice))
                .bg(mPrimaryColor)
                .build();
        mBuyFirst4Label.addMouseListener(targetBuyCursorListener);
        mBuyFirst4Label.setFocusable(true);
        mBuyFirst4Label.addMouseListener(getStandardBuyMouseListener(aRatesFrame));

        mSellFirst4Label = new GradientLabel.Builder()
                .text(getFirstHalf(mSellPrice))
                .bg(mPrimaryColor)
                .build();
        mSellFirst4Label.addMouseListener(targetSellCursorListener);
        mSellFirst4Label.setFocusable(true);
        mSellFirst4Label.addMouseListener(getStandardSellMouseListener(aRatesFrame));

        mBuyLast2Label = new GradientLabel.Builder()
                .text(getLastHalf(mBuyPrice))
                .bg(mPrimaryColor)
                .fontSize(25)
                .bold(true)
                .build();
        mBuyLast2Label.addMouseListener(targetBuyCursorListener);
        mBuyLast2Label.setFocusable(true);
        mBuyLast2Label.addMouseListener(getStandardBuyMouseListener(aRatesFrame));

        mBuyFractionLabel = new GradientLabel.Builder()
                .text(getFraction(mBuyPrice))
                .bg(mPrimaryColor)
                .fontSize(12)
                .build();
        mBuyFractionLabel.setVerticalAlignment(SwingConstants.TOP);
        mBuyFractionLabel.setVerticalTextPosition(SwingConstants.TOP);
        mBuyFractionLabel.addMouseListener(targetBuyCursorListener);
        mBuyFractionLabel.setFocusable(true);
        mBuyFractionLabel.addMouseListener(getStandardBuyMouseListener(aRatesFrame));
        mBuyFractionLabel.setPreferredSize(new Dimension(mBuyFractionLabel.getPreferredSize().width,
                                                         mBuyLast2Label.getPreferredSize().height));

        mSellLast2Label = new GradientLabel.Builder()
                .text(getLastHalf(mSellPrice))
                .bg(mPrimaryColor)
                .fontSize(25)
                .bold(true)
                .build();
        mSellLast2Label.addMouseListener(targetSellCursorListener);
        mSellLast2Label.setFocusable(true);
        mSellLast2Label.addMouseListener(getStandardSellMouseListener(aRatesFrame));

        mSellFractionLabel = new GradientLabel.Builder()
                .text(getFraction(mSellPrice))
                .bg(mPrimaryColor)
                .showBorder(true)
                .right(1)
                .fontSize(12)
                .build();
        mSellFractionLabel.setVerticalAlignment(SwingConstants.TOP);
        mSellFractionLabel.setVerticalTextPosition(SwingConstants.TOP);
        mSellFractionLabel.addMouseListener(targetSellCursorListener);
        mSellFractionLabel.setFocusable(true);
        mSellFractionLabel.addMouseListener(getStandardSellMouseListener(aRatesFrame));
        mSellFractionLabel.setPreferredSize(new Dimension(mSellFractionLabel.getPreferredSize().width,
                                                          mSellLast2Label.getPreferredSize().height));

        mHighPriceLabel = new GradientLabel.Builder()
                .text(mRsm.getString("IDS_RATES_HIGH") + ":" + TradeDesk.formatPrice(mCurrency, aRate.getHighPrice()))
                .bg(mHL1)
                .bg2(mHL2)
                .showBorder(true)
                .fg(mHighPriceFG)
                .top(1)
                .bottom(1)
                .build();
        mHighPriceLabel.addMouseListener(getStandardBuyMouseListener(aRatesFrame));
        mHighPriceLabel.addMouseListener(targetBuyCursorListener);

        mLowPriceLabel = new GradientLabel.Builder()
                .text(mRsm.getString("IDS_RATES_LOW") + ":" + TradeDesk.formatPrice(mCurrency, aRate.getLowPrice()))
                .bg(mHL1)
                .bg2(mHL2)
                .showBorder(true)
                .fg(mLowPriceFG)
                .top(1)
                .bottom(1)
                .build();
        mLowPriceLabel.addMouseListener(getStandardSellMouseListener(aRatesFrame));
        mLowPriceLabel.addMouseListener(targetSellCursorListener);

        String txt = aRate.getLastDate() == null ? "[NA]" : mSimpleDateFormat.format(aRate.getLastDate());
        mRateDateLabel = new GradientLabel.Builder()
                .text(txt)
                .bg(mRateDate1)
                .bg2(mRateDate2)
                .showBorder(true)
                .fg(mRateDateFG)
                .top(1)
                .right(1)
                .build();
        mRateDateLabel.setPreferredSize(new Dimension(100, 22));

        mCurrencyLabel = new GradientLabel.Builder()
                .text(mCurrency)
                .bg(mCCY1)
                .bg2(mCCY2)
                .showBorder(true)
                .fontSize(15)
                .fg(mCCYFG)
                .bold(true)
                .top(1)
                .left(1)
                .right(1)
                .build();
        mCurrencyLabel.setPreferredSize(new Dimension(100, 22));
        add("br", mCurrencyLabel);
        add("", mRateDateLabel);
        mSpreadFormat.applyPattern("#.#");
        mSpreadFormat.setMinimumFractionDigits(1);
        String spread = mSpreadFormat.format(TradeDesk.getSpread(mCurrency));
        mSpreadLabel = new GradientLabel.Builder()
                .text(spread)
                .bg(mPrimaryColor)
                .bg(mHL1)
                .bg2(mHL2)
                .showBorder(true)
                .top(1)
                .bottom(1)
                .left(1)
                .right(1)
                .build();
        mSpreadLabel.setToolTipText(spread);
        mSpreadLabel.setPreferredSize(new Dimension(40, 22));
        mHighPriceLabel.setPreferredSize(new Dimension(80, 22));
        mLowPriceLabel.setPreferredSize(new Dimension(80, 22));
        add("br", mLowPriceLabel);
        add("", mSpreadLabel);
        add("", mHighPriceLabel);

        mSellFirst4Label.setPreferredSize(new Dimension(45, 40));
        mSellLast2Label.setPreferredSize(new Dimension(35, 40));
        mSellFractionLabel.setPreferredSize(new Dimension(20, 40));
        mSellFirst4Label.setHorizontalAlignment(SwingConstants.RIGHT);
        mSellLast2Label.setVerticalAlignment(SwingConstants.TOP);
        mSellFractionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add("br", mSellFirst4Label);
        add("", mSellLast2Label);
        add("", mSellFractionLabel);

        mBuyFirst4Label.setPreferredSize(new Dimension(45, 40));
        mBuyLast2Label.setPreferredSize(new Dimension(35, 40));
        mBuyFractionLabel.setPreferredSize(new Dimension(20, 40));
        mBuyFirst4Label.setHorizontalAlignment(SwingConstants.RIGHT);
        mBuyLast2Label.setVerticalAlignment(SwingConstants.TOP);
        mBuyFractionLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add("", mBuyFirst4Label);
        add("", mBuyLast2Label);
        add("", mBuyFractionLabel);

        mSellLabel.setPreferredSize(new Dimension(60, 22));
        add("br", mSellLabel);

        mAmtComboBox = new AmountsComboBox();
        mAmtComboBox.setEditable(true);
        mAmtComboBox.setContractSize(aRate.getContractSize());
        mAmtComboBox.setSelectedItem(aRate.getContractSize());
        mAmtComboBox.init(new DefaultActorImpl());
        mAmtComboBox.setPreferredSize(new Dimension(80, 22));
        add("", mAmtComboBox);

        mSelectionListener = new ITableSelectionListener() {
            public void onTableChangeSelection(ITable aTable, int[] aRow) {
                Accounts accounts = TradeDesk.getInst().getAccounts();
                if (!accounts.isEmpty()) {
                    try {
                        Account account = (Account) accounts.get(aRow[0]);
                        mAccount = account.getAccount();
                        if (mRate.isForex()) {
                            mAmtComboBox.setContractSize((long) account.getBaseUnitSize());
                        }
                        mAmtComboBox.setSelectedIndex(0);
                    } catch (Exception e) {
                        mAccount = null;
                    } finally {
                        repaint();
                    }
                }
            }
        };
        if (TradeApp.getInst().getAccountsFrame() != null) {
            AccountsFrame frame = TradeApp.getInst().getAccountsFrame();
            int row = frame.getTable().getSelectedRow();
            Account account = (Account) TradeDesk.getInst().getAccounts().get(row == -1 ? 0 : row);
            mAccount = account.getAccount();
            if (mRate.isForex()) {
                mAmtComboBox.setContractSize((long) account.getBaseUnitSize());
                mAmtComboBox.setSelectedIndex(0);
            }
        }

        mTableListener = new ITableListener() {
            public void onAddTable(ITable aTable) {
                if (AccountsFrame.NAME.equals(aTable.getName())) {
                    Accounts accounts = TradeDesk.getInst().getAccounts();
                    if (!accounts.isEmpty()) {
                        Account account = (Account) accounts.get(0);
                        mAccount = account.getAccount();
                        if (mRate.isForex()) {
                            mAmtComboBox.setContractSize((long) account.getBaseUnitSize());
                            mAmtComboBox.setSelectedIndex(0);
                        }
                    }
                    aTable.addSelectionListener(mSelectionListener);
                }
            }

            public void onRemoveTable(ITable aTable) {
                if (AccountsFrame.NAME.equals(aTable.getName())) {
                    aTable.removeSelectionListener(mSelectionListener);
                }
            }
        };

        aRatesFrame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameOpened(InternalFrameEvent aEvent) {
                TableManager.getInst().addListener(mTableListener);
                String name = mPreferences.getUserName();
                PreferencesManager.getPreferencesManager(name).addPreferencesListener(RatePanel.this);
                TradeApp.getInst().getResourceManager().addLocaleListener(RatePanel.this);
                UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName()).addObserver(RatePanel.this);
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent aEvent) {
                TableManager.getInst().removeListener(mTableListener);
                String name = mPreferences.getUserName();
                PreferencesManager.getPreferencesManager(name).removePreferencesListener(RatePanel.this);
                TradeApp.getInst().getResourceManager().removeLocaleListener(RatePanel.this);
                UserPreferences.getUserPreferences(name).deleteObserver(RatePanel.this);
            }
        });

        String tradingMode = mPreferences.getString(TRADING_MODE);
        mAmtComboBox.setSelectedIndex(0);
        if (SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode)) {
            mAmtComboBox.setEnabled(true);
        } else {
            mAmtComboBox.setEnabled(false);
        }

        mBuyLabel.setPreferredSize(new Dimension(60, 22));
        add("", mBuyLabel);

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent aEvent) {
                if (!TradeApp.getInst().getRatesFrame().getSelectedCurrency().equals(mCurrency)) {
                    TradeApp.getInst().getRatesFrame().setSelectedCurrency(mCurrency);
                    if (mRatePanelFocusListener != null) {
                        mRatePanelFocusListener.focusGained(mCurrency);
                    }
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aEvent) {
                requestFocusInWindow();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent aEvent) {
                char aChar = aEvent.getKeyChar();
                IMainFrame frame = aRatesFrame.getMainFrame();
                switch (aChar) {
                    case 'b':
                        Action marketOrderBuyAction = frame.getAction(ActionTypes.CREATE_MARKET_ORDER, null);
                        marketOrderBuyAction.actionPerformed(new ActionEvent(mCurrency, 0, Side.BUY.getName()));
                        break;
                    case 's':
                        Action marketOrderSellAction = frame.getAction(ActionTypes.CREATE_MARKET_ORDER, null);
                        marketOrderSellAction.actionPerformed(new ActionEvent(mCurrency, 0, Side.SELL.getName()));
                        break;
                    case 'e':
                        Action entryOrder = frame.getAction(ActionTypes.CREATE_ENTRY_ORDER, null);
                        entryOrder.actionPerformed(new ActionEvent(mCurrency, 0, Side.BUY.getName()));
                        break;
                    default:
                }
            }
        });
        setBuyTradable(aRate.isBuyTradable());
        setSellTradable(aRate.isSellTradable());
    }

    @Override
    protected void paintComponent(Graphics aGraphics) {
        if (UIManager.getInst().isAAEnabled()) {
            Graphics2D g2d = (Graphics2D) aGraphics;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(aGraphics);
    }

    private MouseAdapter getStandardBuyMouseListener(final AdvancedRatesFrame aCf) {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent aEvent) {
                if (!mAmtComboBox.verifyAmount()) {
                    return;
                }
                requestFocusInWindow();
                TradeApp.getInst().getRatesFrame().setSelectedCurrency(getCurrency());
                if (TradingServerSession.getInstance().getUserKind() == IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
                    return;
                }
                if (mBuyTradable) {
                    if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                        aCf.getBuyPopupMenu().show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                    } else {
                        String tradingMode = mPreferences.getString(TRADING_MODE);
                        boolean atBest = mPreferences.getBoolean(AT_BEST);
                        int atMarket = mPreferences.getInt(AT_MARKET);
                        if ((SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode))
                            && mAccount != null) {
                            try {
                                Liaison liaison = Liaison.getInstance();
                                IRequestFactory requestFactory = liaison.getRequestFactory();
                                IRequest request;
                                if (atBest) {
                                    request = requestFactory.createTrueMarketOrder(mCurrency,
                                                                                   mAccount,
                                                                                   Side.BUY,
                                                                                   mAmtComboBox.getSelectedAmountLong(),
                                                                                   null);
                                } else {
                                    request = requestFactory.createMarketOrder(mCurrency,
                                                                               mAccount,
                                                                               Side.BUY,
                                                                               mAmtComboBox.getSelectedAmountLong(),
                                                                               null,
                                                                               atMarket);
                                }
                                if (SINGLE_CLICK.equals(tradingMode)) {
                                    if (!mRate.isBuyBlocked()) {
                                        liaison.sendRequest(request);
                                        mRate.setBuyBlocked(true);
                                        getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                        TIMER.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                mRate.setBuyBlocked(false);
                                                getRootPane().setCursor(mTargetCursor);
                                            }
                                        }, 1000);
                                    }
                                } else if (DOUBLE_CLICK.equals(tradingMode) && aEvent.getClickCount() == 2) {
                                    if (!mRate.isBuyBlocked()) {
                                        liaison.sendRequest(request);
                                        mRate.setBuyBlocked(true);
                                        getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                        TIMER.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                mRate.setBuyBlocked(false);
                                                getRootPane().setCursor(mTargetCursor);
                                            }
                                        }, 1000);
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                String title = TradeApp.getInst()
                                        .getResourceManager()
                                        .getString("IDS_MAINFRAME_SHORT_TITLE");
                                JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                              ex.getMessage(),
                                                              title,
                                                              JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            Action action = aCf.getMainFrame().getAction(ActionTypes.CREATE_MARKET_ORDER, null);
                            ActionEvent event = new ActionEvent(mCurrency, 0, Side.BUY.getName());
                            action.actionPerformed(event);
                        }
                    }
                } else {
                    showUnavailablePriceError();
                }
            }
        };
    }

    private MouseAdapter getStandardRFQListener(final AdvancedRatesFrame aCf) {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent aEvent) {
                requestFocusInWindow();
                TradeApp.getInst().getRatesFrame().setSelectedCurrency(getCurrency());
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                            aCf.getBuyPopupMenu().show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                        } else if (aEvent.getClickCount() == 2) {
                            Action rfq = aCf.getMainFrame().getAction(ActionTypes.REQUEST_FOR_QUOTE, null);
                            ActionEvent event = new ActionEvent(mCurrency, 0, "RFQ");
                            rfq.actionPerformed(event);
                        }
                    }
                });
            }
        };
    }

    private MouseAdapter getStandardSellMouseListener(final AdvancedRatesFrame aCf) {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent aEvent) {
                if (!mAmtComboBox.verifyAmount()) {
                    return;
                }
                requestFocusInWindow();
                TradeApp.getInst().getRatesFrame().setSelectedCurrency(getCurrency());
                if (TradingServerSession.getInstance().getUserKind() == IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
                    return;
                }
                if (mSellTradable) {
                    String tradingMode = mPreferences.getString(TRADING_MODE);
                    boolean atBest = mPreferences.getBoolean(AT_BEST);
                    int atMarket = mPreferences.getInt(AT_MARKET);
                    if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                        aCf.getSellPopupMenu().show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                    } else {
                        if ((SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode))
                            && mAccount != null) {
                            try {
                                Liaison liaison = Liaison.getInstance();
                                IRequestFactory requestFactory = liaison.getRequestFactory();
                                IRequest request;
                                if (atBest) {
                                    request = requestFactory.createTrueMarketOrder(mCurrency,
                                                                                   mAccount,
                                                                                   Side.SELL,
                                                                                   mAmtComboBox.getSelectedAmountLong(),
                                                                                   null);
                                } else {
                                    request = requestFactory.createMarketOrder(mCurrency,
                                                                               mAccount,
                                                                               Side.SELL,
                                                                               mAmtComboBox.getSelectedAmountLong(),
                                                                               null,
                                                                               atMarket);
                                }
                                if (SINGLE_CLICK.equals(tradingMode)) {
                                    if (!mRate.isSellBocked()) {
                                        liaison.sendRequest(request);
                                        mRate.setSellBocked(true);
                                        getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                        TIMER.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                mRate.setSellBocked(false);
                                                getRootPane().setCursor(mTargetCursor);
                                            }
                                        }, 1000);
                                    }
                                } else if (DOUBLE_CLICK.equals(tradingMode) && aEvent.getClickCount() == 2) {
                                    if (!mRate.isSellBocked()) {
                                        liaison.sendRequest(request);
                                        mRate.setSellBocked(true);
                                        getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                        TIMER.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                mRate.setSellBocked(false);
                                                getRootPane().setCursor(mTargetCursor);
                                            }
                                        }, 1000);
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                String title = TradeApp.getInst()
                                        .getResourceManager()
                                        .getString("IDS_MAINFRAME_SHORT_TITLE");
                                JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                                              ex.getMessage(),
                                                              title,
                                                              JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            Action action = aCf.getMainFrame().getAction(ActionTypes.CREATE_MARKET_ORDER, null);
                            ActionEvent event = new ActionEvent(mCurrency, 0, Side.SELL.getName());
                            action.actionPerformed(event);
                        }
                    }
                } else {
                    showUnavailablePriceError();
                }
            }
        };
    }

    private void setBuyTradable(boolean aTradable) {
        mBuyTradable = aTradable;
        mBuyLast2Label.setEnabled(aTradable);
        mBuyFirst4Label.setEnabled(aTradable);
        mBuyFractionLabel.setEnabled(aTradable);
        mBuyLabel.setEnabled(aTradable);
    }

    private void setSellTradable(boolean aTradable) {
        mSellTradable = aTradable;
        mSellLast2Label.setEnabled(aTradable);
        mSellFirst4Label.setEnabled(aTradable);
        mSellFractionLabel.setEnabled(aTradable);
        mSellLabel.setEnabled(aTradable);
    }

    /**
     * get the rate buy price
     *
     * @return buy price
     */
    public double getBuyPrice() {
        return mBuyPrice;
    }

    private void setBuyPrice(double aBuyPrice) {
        if (aBuyPrice > mBuyPrice) {
            mBuyFirst4Label.setForeground(mColorFGRaised);
            mBuyLast2Label.setForeground(mColorFGRaised);
            mBuyFractionLabel.setForeground(mColorFGRaised);
        } else {
            mBuyFirst4Label.setForeground(mColorFGDown);
            mBuyLast2Label.setForeground(mColorFGDown);
            mBuyFractionLabel.setForeground(mColorFGDown);
        }
        if (!mBuyResetRunning) {
            mBuyResetRunning = true;
            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    mBuyFirst4Label.setForeground(null);
                    mBuyLast2Label.setForeground(null);
                    mBuyFractionLabel.setForeground(null);
                    mBuyResetRunning = false;
                }
            }, 2000);
        }
        String spread = mSpreadFormat.format(TradeDesk.getSpread(mCurrency));
        mSpreadLabel.setText(spread);
        mSpreadLabel.setToolTipText(spread);
        mBuyFirst4Label.setText(getFirstHalf(aBuyPrice).trim());
        mBuyFirst4Label.setToolTipText(TradeDesk.formatPrice(mCurrency, aBuyPrice));
        mBuyLast2Label.setText(getLastHalf(aBuyPrice).trim());
        mBuyLast2Label.setToolTipText(TradeDesk.formatPrice(mCurrency, aBuyPrice));
        mBuyFractionLabel.setText(getFraction(aBuyPrice));
        mBuyFractionLabel.setToolTipText(TradeDesk.formatPrice(mCurrency, aBuyPrice));
        mBuyPrice = aBuyPrice;
    }

    /**
     * get the rate currency
     *
     * @return ccy
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * set the currency
     *
     * @param aCurrency ccy
     */
    public void setCurrency(String aCurrency) {
        mCurrency = aCurrency;
    }

    private String getFirstHalf(double aPrice) {
        int precision = 0;
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(mCurrency);
            precision = security.getFXCMSymPrecision();
        } catch (NotDefinedException e) {
            //swallow
        }
        String str = TradeDesk.formatPrice(mCurrency, aPrice);
        if (str != null && str.length() > 0 && aPrice != 0) {
            try {
                String s = str.substring(0, str.length() - 2);
                char separator = new DecimalFormatSymbols().getDecimalSeparator();
                if (precision == 3 || precision == 5 || s.endsWith(String.valueOf(separator))) {
                    s = str.substring(0, str.length() - 3);
                }
                if (s.endsWith(String.valueOf(separator))) {
                    s = s.substring(0, s.length() - 1);
                }
                return s;
            } catch (Exception e) {
                System.out.println(mCurrency + " = problem str = " + str);
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    private String getFraction(double aPrice) {
        String fraction = null;
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(mCurrency);
            if (security.getFXCMSymPrecision() == 3 || security.getFXCMSymPrecision() == 5) { //fractional price
                String str = TradeDesk.formatPrice(mCurrency, aPrice);
                fraction = str.substring(str.length() - 1);
            }
        } catch (NotDefinedException e) {
            e.printStackTrace();
        }
        return fraction;
    }

    private String getLastHalf(double aPrice) {
        String str = TradeDesk.formatPrice(mCurrency, aPrice);
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(mCurrency);
            if (security.getFXCMSymPrecision() == 3 || security.getFXCMSymPrecision() == 5) { //fractional price
                str = str.substring(0, str.length() - 1);
            }
        } catch (NotDefinedException e) {
            e.printStackTrace();
        }
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        if (str.length() > 0 && aPrice != 0) {
            try {
                String s = str.substring(str.length() - 2, str.length());
                String dsep = String.valueOf(dfs.getDecimalSeparator());
                return s.startsWith(dsep) ? str.substring(str.length() - 1, str.length()) : s;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            return "";
        }
    }

    /**
     * @return sell price for this rate
     */
    public double getSellPrice() {
        return mSellPrice;
    }

    private void setSellPrice(double aSellPrice) {
        if (aSellPrice > mSellPrice) {
            mSellFirst4Label.setForeground(mColorFGRaised);
            mSellLast2Label.setForeground(mColorFGRaised);
            mSellFractionLabel.setForeground(mColorFGRaised);
        } else {
            mSellFirst4Label.setForeground(mColorFGDown);
            mSellLast2Label.setForeground(mColorFGDown);
            mSellFractionLabel.setForeground(mColorFGDown);
        }
        if (!mSellResetRunning) {
            mSellResetRunning = true;
            TIMER.schedule(new TimerTask() {
                @Override
                public void run() {
                    mSellFirst4Label.setForeground(null);
                    mSellLast2Label.setForeground(null);
                    mSellFractionLabel.setForeground(null);
                    mSellResetRunning = false;
                }
            }, 2000);
        }
        String spread = mSpreadFormat.format(TradeDesk.getSpread(mCurrency));
        mSpreadLabel.setText(spread);
        mSpreadLabel.setToolTipText(spread);
        mSellFirst4Label.setText(getFirstHalf(aSellPrice).trim());
        mSellFirst4Label.setToolTipText(TradeDesk.formatPrice(mCurrency, aSellPrice));
        mSellLast2Label.setText(getLastHalf(aSellPrice).trim());
        mSellLast2Label.setToolTipText(TradeDesk.formatPrice(mCurrency, aSellPrice));
        mSellFractionLabel.setText(getFraction(aSellPrice));
        mSellFractionLabel.setToolTipText(TradeDesk.formatPrice(mCurrency, aSellPrice));
        mSellPrice = aSellPrice;
    }

    public void onChangeLocale(ResourceManager aMan) {
        mBuyLabel.setText(aMan.getString("IDS_BUY_TEXT"));
        mSellLabel.setText(aMan.getString("IDS_SELL_TEXT"));
        Rate rate = TradeDesk.getInst().getRates().getRate(mCurrency);
        setHighPrice(rate.getHighPrice());
        setLowPrice(rate.getLowPrice());
        repaint();
    }

    public void preferencesUpdated(Vector aChangings) {
        mBuyFG = mPreferences.getColor("AdvancedRate.buyFG");
        mLabel1 = mPreferences.getColor("AdvancedRate.label1");
        mLabel2 = mPreferences.getColor("AdvancedRate.label2");
        mSellFG = mPreferences.getColor("AdvancedRate.sellFG");
        mPrimaryColor = mPreferences.getColor("AdvancedRate.primaryColor");
        mHighPriceFG = mPreferences.getColor("AdvancedRate.highPriceFG");
        mLowPriceFG = mPreferences.getColor("AdvancedRate.lowPriceFG");
        mHL1 = mPreferences.getColor("AdvancedRate.hl1");
        mHL2 = mPreferences.getColor("AdvancedRate.hl2");
        mRateDate1 = mPreferences.getColor("AdvancedRate.rateDate1");
        mRateDate2 = mPreferences.getColor("AdvancedRate.rateDate2");
        mCCY1 = mPreferences.getColor("AdvancedRate.ccy1");
        mCCY2 = mPreferences.getColor("AdvancedRate.ccy2");
        mCCYFG = mPreferences.getColor("AdvancedRate.currencyFG");
        mRateDateFG = mPreferences.getColor("AdvancedRate.rateDateFG");
        mColorBG2Selected = mPreferences.getColor("AdvancedRate.selectedCcy2");
        mColorBGSelected = mPreferences.getColor("AdvancedRate.selectedCcy1");
        mColorFGRaised = mPreferences.getColor("AdvancedRate.foreground.raised");
        mColorFGDown = mPreferences.getColor("AdvancedRate.foreground.down");

        mRateDateLabel.setForeground(mRateDateFG);
        mRateDateLabel.setBg(mRateDate1);
        mRateDateLabel.setBg2(mRateDate2);
        mCurrencyLabel.setForeground(mCCYFG);
        mBuyLabel.setForeground(mBuyFG);
        mBuyLabel.setBg(mLabel1);
        mBuyLabel.setBg2(mLabel2);
        mSellLabel.setForeground(mSellFG);
        mSellLabel.setBg(mLabel1);
        mSellLabel.setBg2(mLabel2);
        mHighPriceLabel.setForeground(mHighPriceFG);
        mHighPriceLabel.setBg(mHL1);
        mHighPriceLabel.setBg2(mHL2);
        mLowPriceLabel.setForeground(mLowPriceFG);
        mLowPriceLabel.setBg(mHL1);
        mLowPriceLabel.setBg2(mHL2);
        mSellFirst4Label.setBackground(mPrimaryColor);
        mSellLast2Label.setBackground(mPrimaryColor);
        mSellFractionLabel.setBackground(mPrimaryColor);
        mBuyFirst4Label.setBackground(mPrimaryColor);
        mBuyLast2Label.setBackground(mPrimaryColor);
        mBuyFractionLabel.setBackground(mPrimaryColor);
        repaint();
    }

    /**
     * register a focus listener
     *
     * @param aListener listener
     */
    public void registerRatePanelFocusListener(IRatePanelFocusListener aListener) {
        mRatePanelFocusListener = aListener;
    }

    private void setHighPrice(double aHighPrice) {
        ResourceManager resourceManager = TradeApp.getInst().getResourceManager();
        mHighPriceLabel.setText(resourceManager.getString("IDS_RATES_HIGH")
                                + ":"
                                + TradeDesk.formatPrice(mCurrency, aHighPrice));
        mHighPriceLabel.setToolTipText(TradeDesk.formatPrice(mCurrency, aHighPrice));
    }

    private void setLowPrice(double aLowPrice) {
        ResourceManager resourceManager = TradeApp.getInst().getResourceManager();
        mLowPriceLabel.setText(resourceManager.getString("IDS_RATES_LOW")
                               + ":"
                               + TradeDesk.formatPrice(mCurrency, aLowPrice));
        mLowPriceLabel.setToolTipText(TradeDesk.formatPrice(mCurrency, aLowPrice));
    }

    private void setRateDate(Rate aRate) {
        mRateDateLabel.setText(mSimpleDateFormat.format(aRate.getLastDate()));
    }

    /**
     * set this panel to selected state
     */
    public void setSelected() {
        mCurrencyLabel.setBg(mColorBGSelected);
        mCurrencyLabel.setBg2(mColorBG2Selected);
        mRateDateLabel.setBg(mColorBGSelected);
        mRateDateLabel.setBg2(mColorBG2Selected);
        repaint();
    }

    @Override
    public void setToolTipText(String aText) {
        super.setToolTipText(aText);
        mBuyLabel.setToolTipText(aText);
        mSellLabel.setToolTipText(aText);
    }

    /**
     * set rate panel to unselected state
     */
    public void setUnselected() {
        mCurrencyLabel.setBg(mCCY1);
        mCurrencyLabel.setBg2(mCCY2);
        mRateDateLabel.setBg(mRateDate1);
        mRateDateLabel.setBg2(mRateDate2);
        repaint();
    }

    private void showUnavailablePriceError() {
        JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                      "There is no tradable price. (You cannot trade at this price)",
                                      mRsm.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                      JOptionPane.ERROR_MESSAGE);
    }

    public void update(Observable aObservable, Object arg) {
        if (arg.toString().endsWith(IClickModel.TRADING_MODE)) {
            String tradingMode = mPreferences.getString(TRADING_MODE);
            mAmtComboBox.setSelectedIndex(0);
            if (SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode)) {
                mAmtComboBox.setEnabled(true);
            } else {
                mAmtComboBox.setEnabled(false);
            }
        }
    }

    /**
     * update the rate panel
     *
     * @param aRate rate
     */
    public void updateRate(Rate aRate) {
        mRate = aRate;
        setBuyTradable(aRate.isBuyTradable());
        setSellTradable(aRate.isSellTradable());
        setBuyPrice(aRate.getBuyPrice());
        setSellPrice(aRate.getSellPrice());
        setHighPrice(aRate.getHighPrice());
        setLowPrice(aRate.getLowPrice());
        setRateDate(aRate);
        setToolTipText(aRate.getLastDate().toString());
    }
}
