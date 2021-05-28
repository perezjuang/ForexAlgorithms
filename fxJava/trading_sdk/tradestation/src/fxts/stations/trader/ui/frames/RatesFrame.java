/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/RatesFrame.java#5 $
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
 * 09/09/2003   KAV     Initial creation
 * 12/8/2004    Andre Mermegas  -- fixed the change % to work with new version
 * 05/10/2006   Andre Mermegas: use new timeformat instead of date format for rates
 * 07/18/2006   Andre Mermegas: performance update
 * 05/01/2007   Andre Mermegas: added MMR
 * 05/14/2009   Andre Mermegas: CFD update
 */
package fxts.stations.trader.ui.frames;

import com.fxcm.fix.IFixDefs;
import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.core.ATradeAction;
import fxts.stations.core.Accounts;
import fxts.stations.core.ActionManager;
import fxts.stations.core.IClickModel;
import fxts.stations.core.RatesComparator;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.EachRowEditor;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.trader.ui.actions.CreateEntryOrderAction;
import fxts.stations.trader.ui.actions.CreateMarketOrderAction;
import fxts.stations.trader.ui.actions.CurrencySubscriptionAction;
import fxts.stations.trader.ui.actions.RequestForQuoteAction;
import fxts.stations.trader.ui.dialogs.AmountsComboBox;
import fxts.stations.trader.ui.dialogs.DefaultActorImpl;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequestFactory;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.ITable;
import fxts.stations.ui.ITableListener;
import fxts.stations.ui.ITableSelectionListener;
import fxts.stations.ui.TableManager;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ActionTypes;
import fxts.stations.util.ISignal;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.Signaler;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;
import fxts.stations.util.signals.ChangeSignal;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Frames is destined for showing of table with Dealing rates.
 */
public class RatesFrame<E> extends ATableFrame<E> implements IClickModel, ITableSelectionListener {
    public static final String NAME = "Rates";
    /**
     * Array of resource pairs with names of columns.
     */
    private static final String[] COLUMNS = {
            "IDS_RATES_CURRENCY",
            "IDS_RATES_SELL",
            "IDS_RATES_BUY",
            "IDS_RATES_SPREAD",
            "IDS_RATES_AMT",
            "IDS_RATES_HIGH",
            "IDS_RATES_LOW",
            "IDS_INTR_S",
            "IDS_INTR_B",
            "IDS_PIP_COST",
            "IDS_RATES_TIME"};
    /* Identifiers of columns. */
    private static final int CURRENCY_COLUMN = 0;
    private static final int SELL_COLUMN = 1;
    private static final int BUY_COLUMN = 2;
    private static final int SPREAD_COLUMN = 3;
    private static final int AMT_COLUMN = 4;
    private static final int HIGH_COLUMN = 5;
    private static final int LOW_COLUMN = 6;
    private static final int SELL_INTEREST_COLUMN = 7;
    private static final int BUY_INTEREST_COLUMN = 8;
    private static final int PIP_COST_COLUMN = 9;
    private static final int TIME_COLUMN = 10;
    private String mAccount;
    private String mAmtColName;
    private TableColumn mAmtTableColumn;
    private JPopupMenu mBuyPopupMenu;
    private Color mColorBGDown;
    private Color mColorBGDownSelected;
    private Color mColorBGEven;
    private Color mColorBGHeader;
    private Color mColorBGOdd;
    private Color mColorBGRaised;
    private Color mColorBGRaisedSelected;
    private Color mColorBGSelected;
    private Color mColorFG;
    private Color mColorFGDown;
    private Color mColorFGDownSelected;
    private Color mColorFGHeader;
    private Color mColorFGRaised;
    private Color mColorFGRaisedSelected;
    private Color mColorFGSelected;
    private Color mColorFGTradable;
    private Color mColorFGTradableSelected;
    private Map<String, AmountsComboBox> mComboBoxMap = new HashMap<String, AmountsComboBox>();
    private Font mFontContent;
    private Font mFontContentDown;
    private Font mFontContentDownSelected;
    private Font mFontContentHeader;
    private Font mFontContentRaised;
    private Font mFontContentRaisedSelected;
    private Font mFontContentSelected;
    private Font mFontContentTradable;
    private Font mFontContentTradableSelected;
    private EachRowEditor mRowEditor;
    private String mSelectedCurrency = "EUR/USD";
    private JPopupMenu mSellPopupMenu;
    private DecimalFormat mSpreadFormat = new ThreadSafeNumberFormat().getInstance();
    protected SignalVector mSubscribedRates = new SignalVector();
    private ITableListener mTableListener;
    private final Timer mTimer = new Timer("RatesFrameTimer");

    /**
     * Constructor.
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    public RatesFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        mSpreadFormat.applyPattern("#.#");
        mSpreadFormat.setMinimumFractionDigits(1);
        UIManager uiManager = UIManager.getInst();
        TradeDesk.getInst().getRates().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getRates().subscribe(this, SignalType.REMOVE);
        TradeDesk.getInst().getRates().subscribe(this, SignalType.CHANGE);

        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mColorFG = uiPrefs.getColor("Rate.foreground.default");
        mColorBGEven = uiPrefs.getColor("Rate.background.default.even");
        mColorBGOdd = uiPrefs.getColor("Rate.background.default.odd");
        mFontContent = uiPrefs.getFont("Rate.font.content", label.getFont());
        mColorFGTradable = uiPrefs.getColor("Rate.foreground.nontradable");
        mFontContentTradable = uiPrefs.getFont("Rate.font.nontradable", label.getFont());
        mColorFGRaised = uiPrefs.getColor("Rate.foreground.raised");
        mColorBGRaised = uiPrefs.getColor("Rate.background.raised");
        mFontContentRaised = uiPrefs.getFont("Rate.font.raised", label.getFont());
        mColorFGDown = uiPrefs.getColor("Rate.foreground.down");
        mFontContentDown = uiPrefs.getFont("Rate.font.down", label.getFont());
        mColorBGDown = uiPrefs.getColor("Rate.background.down");
        mColorBGSelected = uiPrefs.getColor("Rate.background.selected");
        mColorFGSelected = uiPrefs.getColor("Rate.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Rate.font.selected", label.getFont());
        mColorFGRaisedSelected = uiPrefs.getColor("Rate.foreground.raised.selected");
        mColorBGDownSelected = uiPrefs.getColor("Rate.background.down.selected");
        mColorBGRaisedSelected = uiPrefs.getColor("Rate.background.raised.selected");
        mFontContentRaisedSelected = uiPrefs.getFont("Rate.font.raised.selected", label.getFont());
        mColorFGDownSelected = uiPrefs.getColor("Rate.foreground.down.selected");
        mFontContentDownSelected = uiPrefs.getFont("Rate.font.down.selected", label.getFont());
        mFontContentTradableSelected = uiPrefs.getFont("Rate.font.nontradable.selected", label.getFont());
        mColorFGTradableSelected = uiPrefs.getColor("Rate.foreground.nontradable.selected");
        mColorBGHeader = uiPrefs.getColor("Rate.background.header");
        mColorFGHeader = uiPrefs.getColor("Rate.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Rate.font.header", label.getFont());

        mSubscribedRates.setComparator(new RatesComparator());
        Enumeration enumeration = TradeDesk.getInst().getRates().elements();
        while (enumeration.hasMoreElements()) {
            Rate rate = (Rate) enumeration.nextElement();
            if (rate.isSubscribed()) {
                mSubscribedRates.add(rate);
            }
        }
        //update title again w/ rates
        setTitle(getLocalizedTitle(aMan));
        //creates renderers
        mCellRenderer = new DefaultTableCellRenderer() {
            //Overriding of DefaultTableCellRenderer`s method
            @Override
            public Component getTableCellRendererComponent(JTable aTable,
                                                           Object aValue,
                                                           boolean aIsSelected,
                                                           boolean aHasFocus,
                                                           int aRow,
                                                           int aColumn) {
                //gets index of column at model
                TableColumnModel columnModel = aTable.getColumnModel();
                TableColumn tableColumn = columnModel.getColumn(aColumn);

                if (mSubscribedRates == null) {
                    return null;
                }

                Rate rate = (Rate) mSubscribedRates.get(aRow);
                if (rate == null) {
                    return null;
                }
                JLabel comp = (JLabel) super.getTableCellRendererComponent(aTable,
                                                                           aValue,
                                                                           aIsSelected,
                                                                           aHasFocus,
                                                                           aRow,
                                                                           aColumn);
                setForeground(mColorFG);
                if (aRow % 2 == 0) {
                    setBackground(mColorBGEven);
                } else {
                    setBackground(mColorBGOdd);
                }
                //set font and color of content table
                comp.setFont(mFontContent);
                if (rate.isTradable()) {
                    if (tableColumn.getModelIndex() == SELL_COLUMN) {
                        if (rate.isSellTradable()) {
                            if (rate.getOldSellPrice() <= rate.getSellPrice()) {
                                comp.setForeground(mColorFGRaised);
                                comp.setBackground(mColorBGRaised);
                                comp.setFont(mFontContentRaised);
                            } else if (rate.getOldSellPrice() >= rate.getSellPrice()) {
                                comp.setForeground(mColorFGDown);
                                comp.setBackground(mColorBGDown);
                                comp.setFont(mFontContentDown);
                            }
                        } else {
                            comp.setForeground(mColorFGTradable);
                            comp.setFont(mFontContentTradable);
                        }
                    } else if (tableColumn.getModelIndex() == BUY_COLUMN) {
                        if (rate.isBuyTradable()) {
                            if (rate.getOldBuyPrice() <= rate.getBuyPrice()) {
                                comp.setForeground(mColorFGRaised);
                                comp.setBackground(mColorBGRaised);
                                comp.setFont(mFontContentRaised);
                            } else if (rate.getOldBuyPrice() >= rate.getBuyPrice()) {
                                comp.setForeground(mColorFGDown);
                                comp.setBackground(mColorBGDown);
                                comp.setFont(mFontContentDown);
                            }
                        } else {
                            comp.setForeground(mColorFGTradable);
                            comp.setFont(mFontContentTradable);
                        }
                    }
                } else {
                    comp.setForeground(mColorFGTradable);
                    comp.setFont(mFontContentTradable);
                }

                //sets color and fonts of selected row
                if (aTable.getSelectedRow() == aRow) {
                    setBackground(mColorBGSelected);
                    setForeground(mColorFGSelected);
                    comp.setFont(mFontContentSelected);
                    if (tableColumn.getModelIndex() == SELL_COLUMN) {
                        if (rate.isSellTradable()) {
                            if (rate.getOldSellPrice() <= rate.getSellPrice()) {
                                comp.setForeground(mColorFGRaisedSelected);
                                comp.setBackground(mColorBGRaisedSelected);
                                comp.setFont(mFontContentRaisedSelected);
                            } else if (rate.getOldSellPrice() >= rate.getSellPrice()) {
                                comp.setForeground(mColorFGDownSelected);
                                comp.setBackground(mColorBGDownSelected);
                                comp.setFont(mFontContentDownSelected);
                            }
                        } else {
                            comp.setForeground(mColorFGTradable);
                            comp.setFont(mFontContentTradable);
                        }
                    } else if (tableColumn.getModelIndex() == BUY_COLUMN) {
                        if (rate.isBuyTradable()) {
                            if (rate.getOldBuyPrice() <= rate.getBuyPrice()) {
                                comp.setForeground(mColorFGRaisedSelected);
                                comp.setBackground(mColorBGRaisedSelected);
                                comp.setFont(mFontContentRaisedSelected);
                            } else if (rate.getOldBuyPrice() >= rate.getBuyPrice()) {
                                comp.setForeground(mColorFGDownSelected);
                                comp.setBackground(mColorBGDownSelected);
                                comp.setFont(mFontContentDownSelected);
                            }
                        } else {
                            comp.setForeground(mColorFGTradable);
                            comp.setFont(mFontContentTradable);
                        }
                    }
                    if (!rate.isTradable()) {
                        comp.setFont(mFontContentTradableSelected);
                        comp.setForeground(mColorFGTradableSelected);
                    }
                }
                setOpaque(true);
                if (aColumn == 0) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
                int height = comp.getFontMetrics(comp.getFont()).getHeight() + 2;
                if (height > mInitialHeight && aTable.getRowHeight(aRow) < height) {
                    EventQueue.invokeLater(new InvokerSetRowHeight(aTable, aRow, height));
                }
                return comp;
            }

            @Override
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        mHeaderRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable aTable,
                                                           Object aValue,
                                                           boolean aIsSelected,
                                                           boolean aHasFocus,
                                                           int aRow,
                                                           int aColumn) {
                JLabel comp = (JLabel) super.getTableCellRendererComponent(aTable,
                                                                           aValue,
                                                                           aIsSelected,
                                                                           aHasFocus,
                                                                           aRow,
                                                                           aColumn);
                setBackground(mColorBGHeader);
                setForeground(mColorFGHeader);
                comp.setFont(mFontContentHeader);
                //sets opaque mode
                setOpaque(true);
                //sets border
                comp.setBorder(BorderFactory.createEtchedBorder());
                //adds icon
                if (aColumn == getCurSortColumn()) {
                    if (mDescendingMode) {
                        comp.setIcon(mUpSortIcon);
                    } else {
                        comp.setIcon(mDownSortIcon);
                    }
                } else {
                    comp.setIcon(null);
                }
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }

            @Override
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        //sets renders to all columns
        setRenderers(mCellRenderer, mHeaderRenderer);

        mRowEditor = new EachRowEditor(getTable()) {
            @Override
            protected void selectEditor(MouseEvent aEvent) {
                int row;
                if (aEvent == null) {
                    row = mTable.getSelectionModel().getAnchorSelectionIndex();
                } else {
                    row = mTable.rowAtPoint(aEvent.getPoint());
                }
                String ccy = (String) mTable.getModel().getValueAt(row, CURRENCY_COLUMN);
                int id = (int) TradeDesk.getInst().getRates().getRate(ccy).getID();
                mEditor = mEditors.get(id);
            }
        };
        TableColumn column = getTable().getColumnModel().getColumn(AMT_COLUMN);
        column.setCellEditor(mRowEditor);
        Enumeration rateEnum = TradeDesk.getInst().getRates().elements();
        while (rateEnum.hasMoreElements()) {
            Rate rate = (Rate) rateEnum.nextElement();
            AmountsComboBox amountComboBox = new AmountsComboBox();
            amountComboBox.init(new DefaultActorImpl());
            amountComboBox.setEditable(true);
            amountComboBox.setContractSize(rate.getContractSize());
            amountComboBox.setSelectedIndex(0);
            amountComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent aEvent) {
                    AmountsComboBox cb = (AmountsComboBox) aEvent.getSource();
                    if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                        cb.verifyAmount();
                    }
                }
            });
            mComboBoxMap.put(rate.getCurrency(), amountComboBox);
            mRowEditor.setEditorAt((int) rate.getID(), new DefaultCellEditor(amountComboBox));
        }

        final ITableSelectionListener selectionListener = new ITableSelectionListener() {
            public void onTableChangeSelection(ITable aTable, int[] aRow) {
                try {
                    Accounts accounts = TradeDesk.getInst().getAccounts();
                    if (!accounts.isEmpty()) {
                        Account account = (Account) accounts.get(aRow[0]);
                        mAccount = account.getAccount();
                        for (String ccy : mComboBoxMap.keySet()) {
                            Rate rate = TradeDesk.getInst().getRates().getRate(ccy);
                            AmountsComboBox amountsComboBox = mComboBoxMap.get(ccy);
                            if (rate.isForex()) {
                                amountsComboBox.setContractSize((long) account.getBaseUnitSize());
                                amountsComboBox.setSelectedIndex(0);
                            }
                        }
                    }
                } catch (Exception e) {
                    mAccount = null;
                } finally {
                    revalidate();
                    repaint();
                }
            }
        };

        if (TradeApp.getInst().getAccountsFrame() != null) {
            AccountsFrame frame = TradeApp.getInst().getAccountsFrame();
            int row = frame.getTable().getSelectedRow();
            Account acct = (Account) TradeDesk.getInst().getAccounts().get(row == -1 ? 0 : row);
            mAccount = acct.getAccount();
            for (String ccy : mComboBoxMap.keySet()) {
                Rate rate = TradeDesk.getInst().getRates().getRate(ccy);
                AmountsComboBox amountsComboBox = mComboBoxMap.get(ccy);
                if (rate.isForex()) {
                    amountsComboBox.setContractSize((long) acct.getBaseUnitSize());
                    amountsComboBox.setSelectedIndex(0);
                }
            }
        }

        mTableListener = new ITableListener() {
            public void onAddTable(ITable aTable) {
                if (AccountsFrame.NAME.equals(aTable.getName())) {
                    Accounts accounts = TradeDesk.getInst().getAccounts();
                    if (!accounts.isEmpty()) {
                        Account account = (Account) accounts.get(0);
                        for (String ccy : mComboBoxMap.keySet()) {
                            Rate rate = TradeDesk.getInst().getRates().getRate(ccy);
                            AmountsComboBox amountsComboBox = mComboBoxMap.get(ccy);
                            if (rate.isForex()) {
                                amountsComboBox.setContractSize((long) account.getBaseUnitSize());
                                amountsComboBox.setSelectedIndex(0);
                            }
                        }
                    }
                    aTable.addSelectionListener(selectionListener);
                }
            }

            public void onRemoveTable(ITable aTable) {
                if (AccountsFrame.NAME.equals(aTable.getName())) {
                    aTable.removeSelectionListener(selectionListener);
                }
            }
        };
        TableManager.getInst().addListener(mTableListener);

        //creates a popup menu for sell side
        mSellPopupMenu = uiManager.createPopupMenu();

        //first menu item
        Action entryOrderAction = getMainFrame().getAction(ActionTypes.CREATE_ENTRY_ORDER, Side.SELL.getName());
        uiManager.addAction(entryOrderAction,
                            "IDS_CREATE_ENTRY_ORDER",
                            "ID_ENTRY_ICON",
                            null,
                            "IDS_CREATE_ENTRY_ORDER_DESC",
                            "IDS_CREATE_ENTRY_ORDER_DESC");
        JMenuItem menuItem = uiManager.createMenuItem(entryOrderAction);
        menuItem.setActionCommand(Side.SELL.getName());
        mSellPopupMenu.add(menuItem);

        //second menu item
        Action marketOrderAction = getMainFrame().getAction(ActionTypes.CREATE_MARKET_ORDER, Side.SELL.getName());
        uiManager.addAction(marketOrderAction,
                            "IDS_CREATE_MARKET_ORDER",
                            "ID_MARKET_ORDER_ICON",
                            null,
                            "IDS_CREATE_MARKET_ORDER_DESC",
                            "IDS_CREATE_MARKET_ORDER_DESC");
        menuItem = uiManager.createMenuItem(marketOrderAction);
        menuItem.setActionCommand(Side.SELL.getName());
        mSellPopupMenu.add(menuItem);

        //third menu item
        Action rfq = getMainFrame().getAction(ActionTypes.REQUEST_FOR_QUOTE, null);
        uiManager.addAction(rfq,
                            "IDS_REQUEST_FOR_QUOTE",
                            "ID_MARKET_ORDER_ICON",
                            null,
                            "IDS_REQUEST_FOR_QUOTE_DESC",
                            "IDS_REQUEST_FOR_QUOTE_DESC");
        menuItem = uiManager.createMenuItem(rfq);
        menuItem.setActionCommand("RFQ");
        mSellPopupMenu.add(menuItem);
        mSellPopupMenu.add(getCCYSubsMenu());

        //creates a popup menu for buy syde
        mBuyPopupMenu = UIManager.getInst().createPopupMenu();

        //first menu item
        entryOrderAction = getMainFrame().getAction(ActionTypes.CREATE_ENTRY_ORDER, Side.BUY.getName());
        uiManager.addAction(entryOrderAction,
                            "IDS_CREATE_ENTRY_ORDER",
                            "ID_ENTRY_ICON",
                            null,
                            "IDS_CREATE_ENTRY_ORDER_DESC",
                            "IDS_CREATE_ENTRY_ORDER_DESC");
        menuItem = uiManager.createMenuItem(entryOrderAction);
        menuItem.setActionCommand(Side.BUY.getName());
        mBuyPopupMenu.add(menuItem);

        //second menu item
        marketOrderAction = getMainFrame().getAction(ActionTypes.CREATE_MARKET_ORDER, Side.BUY.getName());
        uiManager.addAction(marketOrderAction,
                            "IDS_CREATE_MARKET_ORDER",
                            "ID_MARKET_ORDER_ICON",
                            null,
                            "IDS_CREATE_MARKET_ORDER_DESC",
                            "IDS_CREATE_MARKET_ORDER_DESC");
        menuItem = uiManager.createMenuItem(marketOrderAction);
        menuItem.setActionCommand(Side.BUY.getName());
        mBuyPopupMenu.add(menuItem);

        //third menu item
        rfq = getMainFrame().getAction(ActionTypes.REQUEST_FOR_QUOTE, null);
        uiManager.addAction(rfq,
                            "IDS_REQUEST_FOR_QUOTE",
                            "ID_MARKET_ORDER_ICON",
                            null,
                            "IDS_REQUEST_FOR_QUOTE_DESC",
                            "IDS_REQUEST_FOR_QUOTE_DESC");
        menuItem = uiManager.createMenuItem(rfq);
        menuItem.setActionCommand("RFQ");
        mBuyPopupMenu.add(menuItem);
        mBuyPopupMenu.add(getCCYSubsMenu());

        //adds mouse listener
        getTable().addMouseListener(new MouseAdapter() {
            /**
             * Invoked when a mouse button has been released on a component.
             */
            @Override

            public void mouseReleased(MouseEvent aEvent) {
                JTable table = (JTable) aEvent.getComponent();
                int col = table.columnAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                int row = table.rowAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                table.setRowSelectionInterval(row, row); //selects row
                String ccy = (String) table.getModel().getValueAt(row, CURRENCY_COLUMN);
                setSelectedCurrency(ccy);
                String username = TradeDesk.getInst().getUserName();
                UserPreferences prefs = UserPreferences.getUserPreferences(username);
                String tradingMode = prefs.getString(IClickModel.TRADING_MODE);
                boolean atBest = prefs.getBoolean(IClickModel.AT_BEST);
                int atMarket = prefs.getInt(IClickModel.AT_MARKET);
                //sets frame to selected state (it`s only for assurance)
                if (!isSelected()) {
                    try {
                        setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (TradingServerSession.getInstance().getUserKind() == IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
                    return;
                }
                //gets index of column at model
                int columnIndex = table.getColumnModel().getColumn(col).getModelIndex();
                if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) { //if right button
                    if (columnIndex == SELL_COLUMN) {
                        //shows popup menu
                        mSellPopupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                    } else {
                        //shows popup menu
                        mBuyPopupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                    }
                } else { //if left button
                    if (columnIndex == CURRENCY_COLUMN) {
                        Action action = getMainFrame().getAction(ActionTypes.REQUEST_FOR_QUOTE, null);
                        ActionEvent event = new ActionEvent(this, 0, "RFQ");
                        action.actionPerformed(event);
                    } else if (columnIndex == SELL_COLUMN) {
                        final Rate rate = TradeDesk.getInst().getRates().getRate(ccy);
                        if (rate.isSellTradable()) {
                            if ((SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode))
                                && mAccount != null) {
                                try {
                                    Liaison liaison = Liaison.getInstance();
                                    IRequestFactory requestFactory = liaison.getRequestFactory();
                                    IRequest request;
                                    AmountsComboBox comboBox = mComboBoxMap.get(ccy);
                                    if (atBest) {
                                        request = requestFactory.createTrueMarketOrder(ccy,
                                                                                       mAccount,
                                                                                       Side.SELL,
                                                                                       comboBox.getSelectedAmountLong(),
                                                                                       null);
                                    } else {
                                        request = requestFactory.createMarketOrder(ccy,
                                                                                   mAccount,
                                                                                   Side.SELL,
                                                                                   comboBox.getSelectedAmountLong(),
                                                                                   null,
                                                                                   atMarket);
                                    }
                                    if (IClickModel.SINGLE_CLICK.equals(tradingMode)) {
                                        if (!rate.isSellBocked()) {
                                            liaison.sendRequest(request);
                                            rate.setSellBocked(true);
                                            mTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    rate.setSellBocked(false);
                                                }
                                            }, 1000);
                                        }
                                    } else if (IClickModel.DOUBLE_CLICK.equals(tradingMode)
                                               && aEvent.getClickCount() == 2) {
                                        if (!rate.isSellBocked()) {
                                            liaison.sendRequest(request);
                                            rate.setSellBocked(true);
                                            mTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    rate.setSellBocked(false);
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
                                Action action = getMainFrame().getAction(ActionTypes.CREATE_MARKET_ORDER, null);
                                if (action.isEnabled()) {
                                    ActionEvent event = new ActionEvent(this, 0, Side.SELL.getName());
                                    action.actionPerformed(event);
                                }
                            }
                        } else {
                            showUnavailablePriceError();
                        }
                    } else if (columnIndex == BUY_COLUMN) {
                        final Rate rate = TradeDesk.getInst().getRates().getRate(ccy);
                        if (rate.isBuyTradable()) {
                            if ((SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode))
                                && mAccount != null) {
                                try {
                                    Liaison liaison = Liaison.getInstance();
                                    IRequestFactory requestFactory = liaison.getRequestFactory();
                                    IRequest request;
                                    AmountsComboBox comboBox = mComboBoxMap.get(ccy);
                                    if (atBest) {
                                        request = requestFactory.createTrueMarketOrder(ccy,
                                                                                       mAccount,
                                                                                       Side.BUY,
                                                                                       comboBox.getSelectedAmountLong(),
                                                                                       null);
                                    } else {
                                        request = requestFactory.createMarketOrder(ccy,
                                                                                   mAccount,
                                                                                   Side.BUY,
                                                                                   comboBox.getSelectedAmountLong(),
                                                                                   null,
                                                                                   atMarket);
                                    }
                                    if (IClickModel.SINGLE_CLICK.equals(tradingMode)) {
                                        if (!rate.isBuyBlocked()) {
                                            liaison.sendRequest(request);
                                            rate.setBuyBlocked(true);
                                            mTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    rate.setBuyBlocked(false);
                                                }
                                            }, 1000);
                                        }
                                    } else if (IClickModel.DOUBLE_CLICK.equals(tradingMode)
                                               && aEvent.getClickCount() == 2) {
                                        if (!rate.isBuyBlocked()) {
                                            liaison.sendRequest(request);
                                            rate.setBuyBlocked(true);
                                            mTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    rate.setBuyBlocked(false);
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
                                Action action = getMainFrame().getAction(ActionTypes.CREATE_MARKET_ORDER, null);
                                if (action.isEnabled()) {
                                    ActionEvent event = new ActionEvent(this, 0, Side.BUY.getName());
                                    action.actionPerformed(event);
                                }
                            }
                        } else {
                            showUnavailablePriceError();
                        }
                    }
                }
            }
        });

        //sets icon to internal frame
        URL iconUrl = getResourceManager().getResource("ID_RATES_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }

        if (mSelectedCurrency == null) {
            getTable().setRowSelectionInterval(0, 0);
            String ccy = (String) getTable().getModel().getValueAt(0, CURRENCY_COLUMN);
            setSelectedCurrency(ccy);
        } else {
            int rowCount = getTable().getRowCount();
            for (int i = 0; i < rowCount; i++) {
                String ccy = (String) getTable().getModel().getValueAt(i, CURRENCY_COLUMN);
                if (mSelectedCurrency.equals(ccy)) {
                    getTable().setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        getFrameTable().addSelectionListener(this);
        mAmtColName = getTable().getColumnName(AMT_COLUMN);
        mAmtTableColumn = getTable().getColumn(mAmtColName);
    }

    /**
     * Returns popup menu for buy side.
     *
     * @return popup menu for buy side.
     */
    public JPopupMenu getBuyPopupMenu() {
        return mBuyPopupMenu;
    }

    protected JMenuItem getCCYSubsMenu() {
        JMenuItem item = UIManager.getInst().createMenuItem("IDS_CCY_SUBSCRIPTION_LIST", null, null,
                                                            "IDS_CCY_SUBSCRIPTION_LIST");
        item.addActionListener(new CurrencySubscriptionAction());
        return item;
    }

    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Rate aRate = (Rate) aObject;
        String currency = aRate.getCurrency();
        //returns value of the specified cell
        if (aColumn == CURRENCY_COLUMN) {
            return currency;
        } else if (aColumn == SELL_COLUMN) {
            return TradeDesk.formatPrice(currency, aRate.getSellPrice());
        } else if (aColumn == BUY_COLUMN) {
            return TradeDesk.formatPrice(currency, aRate.getBuyPrice());
        } else if (aColumn == SPREAD_COLUMN) {
            return mSpreadFormat.format(TradeDesk.getSpread(currency));
        } else if (aColumn == AMT_COLUMN) {
            return mComboBoxMap.get(aRate.getCurrency()).getSelectedItem().toString();
        } else if (aColumn == LOW_COLUMN) {
            return TradeDesk.formatPrice(currency, aRate.getLowPrice());
        } else if (aColumn == HIGH_COLUMN) {
            return TradeDesk.formatPrice(currency, aRate.getHighPrice());
        } else if (aColumn == SELL_INTEREST_COLUMN) {
            return Util.format(aRate.getSellInterest());
        } else if (aColumn == BUY_INTEREST_COLUMN) {
            return Util.format(aRate.getBuyInterest());
        } else if (aColumn == TIME_COLUMN) {
            if (aRate.getLastDate() != null) {
                return formatTime(aRate.getLastDate());
            } else {
                return "[NOT AVAILABLE]";
            }
        } else if (aColumn == PIP_COST_COLUMN) {
            return Util.format(aRate.getPipCost());
        } else {
            return null;
        }
    }

    /**
     * Returns localized title.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    protected String getLocalizedTitle(ResourceManager aResourceMan) {
        if (aResourceMan != null) {
            UserPreferences pref = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            String mode = pref.getString(IClickModel.TRADING_MODE);
            int ratesLength = mSubscribedRates == null ? 0 : mSubscribedRates.size();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_RATES_TITLE"));
            if (ratesLength > 0) {
                titleBuffer.append(" (").append(ratesLength).append(")");
            }
            if (IClickModel.SINGLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
            } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
            }
            return titleBuffer.toString();
        }
        mLogger.debug("Error: RatesFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    /**
     * @return Selected Currency
     */
    public String getSelectedCurrency() {
        return mSelectedCurrency;
    }

    /**
     * Returns popup menu for sell side.
     *
     * @return popup menu for sell side.
     */
    public JPopupMenu getSellPopupMenu() {
        return mSellPopupMenu;
    }

    @Override
    protected SignalVector getSignalVector() {
        return mSubscribedRates;
    }

    /**
     * Returns table's model.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new RatesTableModel(aResourceMan);
        }
        return mTableModel;
    }

    /**
     * Returns frames name.
     */
    @Override
    protected String getTableName() {
        return NAME;
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    /**
     * Method is called by Internal Frame Listener when frame has been closed.
     *
     * @param aEvent event
     */
    @Override
    protected void onCloseFrame(InternalFrameEvent aEvent) {
        super.onCloseFrame(aEvent);
        TradeDesk.getInst().getRates().unsubscribe(getTableModel(), SignalType.ADD);
        TradeDesk.getInst().getRates().unsubscribe(getTableModel(), SignalType.REMOVE);
        TradeDesk.getInst().getRates().unsubscribe(this, SignalType.CHANGE);
        mSubscribedRates.clear();
        mCheckBoxMap.clear();
        mComboBoxMap.clear();
        mRowEditor.cleanup();
        TableManager.getInst().removeListener(mTableListener);

        mBuyPopupMenu.removeAll();
        mBuyPopupMenu.removeNotify();
        mBuyPopupMenu = null;

        mSellPopupMenu.removeAll();
        mSellPopupMenu.removeNotify();
        mSellPopupMenu = null;
        removeAll();
        removeNotify();
    }

    @Override
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        super.onSignal(aSrc, aSignal);
        if (aSignal != null && aSignal instanceof ChangeSignal) {
            ChangeSignal cs = (ChangeSignal) aSignal;
            Rate rate = (Rate) cs.getNewElement();
            if (rate.isSubscribed() && mSubscribedRates.indexOf(rate) == -1) {
                mSubscribedRates.add(rate);
                refresh();
            } else if (!rate.isSubscribed() && mSubscribedRates.indexOf(rate) != -1) {
                mSubscribedRates.remove(rate);
                refresh();
            }
        }
    }

    public void onTableChangeSelection(ITable aTable, int[] aiRow) {
        TableModel model = getTable().getModel();
        String ccy = (String) model.getValueAt(aTable.getSelectedRow(), CURRENCY_COLUMN);
        setSelectedCurrency(ccy);
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mColorFG = uiPrefs.getColor("Rate.foreground.default");
        mColorBGEven = uiPrefs.getColor("Rate.background.default.even");
        mColorBGOdd = uiPrefs.getColor("Rate.background.default.odd");
        mFontContent = uiPrefs.getFont("Rate.font.content", label.getFont());
        mColorFGTradable = uiPrefs.getColor("Rate.foreground.nontradable");
        mFontContentTradable = uiPrefs.getFont("Rate.font.nontradable", label.getFont());
        mColorFGRaised = uiPrefs.getColor("Rate.foreground.raised");
        mColorBGRaised = uiPrefs.getColor("Rate.background.raised");
        mFontContentRaised = uiPrefs.getFont("Rate.font.raised", label.getFont());
        mColorFGDown = uiPrefs.getColor("Rate.foreground.down");
        mFontContentDown = uiPrefs.getFont("Rate.font.down", label.getFont());
        mColorBGDown = uiPrefs.getColor("Rate.background.down");
        mColorBGSelected = uiPrefs.getColor("Rate.background.selected");
        mColorFGSelected = uiPrefs.getColor("Rate.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Rate.font.selected", label.getFont());
        mColorFGRaisedSelected = uiPrefs.getColor("Rate.foreground.raised.selected");
        mColorBGDownSelected = uiPrefs.getColor("Rate.background.down.selected");
        mColorBGRaisedSelected = uiPrefs.getColor("Rate.background.raised.selected");
        mFontContentRaisedSelected = uiPrefs.getFont("Rate.font.raised.selected", label.getFont());
        mColorFGDownSelected = uiPrefs.getColor("Rate.foreground.down.selected");
        mFontContentDownSelected = uiPrefs.getFont("Rate.font.down.selected", label.getFont());
        mFontContentTradableSelected = uiPrefs.getFont("Rate.font.nontradable.selected", label.getFont());
        mColorFGTradableSelected = uiPrefs.getColor("Rate.foreground.nontradable.selected");
        mColorBGHeader = uiPrefs.getColor("Rate.background.header");
        mColorFGHeader = uiPrefs.getColor("Rate.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Rate.font.header", label.getFont());
    }

    protected void refresh() {
        setTitle(getLocalizedTitle(mResourceManager));
        getTable().revalidate();
        getTable().repaint();
    }

    /**
     * @param aSelectedCurrency Selected Currency
     */
    public void setSelectedCurrency(String aSelectedCurrency) {
        Rate rate = TradeDesk.getInst().getRates().getRate(aSelectedCurrency);
        Enumeration actions = ActionManager.getInst().actions();
        while (actions.hasMoreElements()) {
            ATradeAction action = (ATradeAction) actions.nextElement();
            if (rate != null) {
                if (action instanceof CreateMarketOrderAction
                    || action instanceof CreateEntryOrderAction
                    || action instanceof RequestForQuoteAction) {
                    action.canAct(rate.isTradable());
                    action.setEnabled(rate.isTradable());
                }
            }
        }
        mSelectedCurrency = aSelectedCurrency;
        int rowCount = getTable().getRowCount();
        for (int i = 0; i < rowCount; i++) {
            String ccy = (String) getTable().getModel().getValueAt(i, CURRENCY_COLUMN);
            if (mSelectedCurrency.equals(ccy)) {
                getTable().setRowSelectionInterval(i, i);
                break;
            }
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            String username = TradeDesk.getInst().getUserName();
            UserPreferences prefs = UserPreferences.getUserPreferences(username);
            String mode = prefs.getString(IClickModel.TRADING_MODE);
            if (!IClickModel.DOUBLE_CLICK.equals(mode) && !IClickModel.SINGLE_CLICK.equals(mode)) {
                JCheckBoxMenuItem item = mCheckBoxMap.get(mAmtColName);
                item.setEnabled(false);
                item.setSelected(false);
                getTable().removeColumn(mAmtTableColumn);
                revalidate();
                repaint();
            }
            if (!mSubscribedRates.isEmpty()) {
                Rate rate = (Rate) mSubscribedRates.get(0);
                if (rate != null) {
                    setSelectedCurrency(rate.getCurrency());
                }
            }
        }
    }

    private void showUnavailablePriceError() {
        JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                      "There is no tradable price. (You cannot trade at this price)",
                                      mResourceManager.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                      JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void update(Observable aObservable, Object arg) {
        super.update(aObservable, arg);
        if (arg.toString().endsWith(IClickModel.TRADING_MODE)) {
            String username = TradeDesk.getInst().getUserName();
            UserPreferences prefs = UserPreferences.getUserPreferences(username);
            String mode = prefs.getString(IClickModel.TRADING_MODE);
            if (IClickModel.DOUBLE_CLICK.equals(mode) || IClickModel.SINGLE_CLICK.equals(mode)) {
                JCheckBoxMenuItem item = mCheckBoxMap.get(mAmtColName);
                item.setEnabled(true);
                item.setSelected(true);
                getTable().addColumn(mAmtTableColumn);
                reOrderColumns();
            } else {
                JCheckBoxMenuItem item = mCheckBoxMap.get(mAmtColName);
                item.setEnabled(false);
                item.setSelected(false);
                getTable().removeColumn(mAmtTableColumn);
            }
        }
    }

    /**
     * Concrete implementation of AFrameTableModel.
     * This class responds for data filling of table.
     */
    private class RatesTableModel extends AFrameTableModel {
        /**
         * Constructor.
         *
         * @param aResourceMan current resource manager
         */
        RatesTableModel(ResourceManager aResourceMan) {
            super(aResourceMan, COLUMNS);
        }

        /**
         * Returns columns types.
         *
         * @param aCol number of the column
         */
        @Override
        public int getColumnType(int aCol) {
            TableColumn column = getTable().getColumnModel().getColumn(aCol);
            int idx = column.getModelIndex();
            if (idx == CURRENCY_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == SELL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == BUY_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == SPREAD_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == AMT_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == LOW_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == HIGH_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == TIME_COLUMN) {
                return AFrameTableModel.DATE_COLUMN;
            } else if (idx == SELL_INTEREST_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == BUY_INTEREST_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == PIP_COST_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else {
                //if not correct column was specified
                return 0;
            }
        }

        /**
         * Returns the number of rows in the model.
         */
        public int getRowCount() {
            TradeDesk tradeDesk = TradeDesk.getInst();
            if (tradeDesk == null) {
                return 0;
            }

            if (mSubscribedRates == null) {
                return 0;
            }
            return mSubscribedRates.size();
        }

        /**
         * Returns the value in the cell at aiCol and aiRow to aValue.
         *
         * @param aRow number of row
         * @param aCol number of column
         */
        public Object getValueAt(int aRow, int aCol) {
            try {
                if (mSubscribedRates == null) {
                    return null;
                }
                //gets rate with number is equal of value "row"
                Rate rate = (Rate) mSubscribedRates.get(aRow);
                return getColumnValue(aCol, rate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int aRowIndex, int aColumnIndex) {
            return aColumnIndex == AMT_COLUMN;
        }
    }
}
