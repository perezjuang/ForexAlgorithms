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
 * $History: $
 * 9/20/2003 Created by USHIK
 * 12/9/2004    Andre Mermegas  -- updated to show in sizes same as classic TS
 * 05/05/2006   Andre Mermegas: fix for NPE when clicking in totals row
 * 07/05/2006   Andre Mermegas: update to allow for totals row not be sorted
 * 05/11/2007   Andre Mermegas: default sort by first column, newest on top
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.Accounts;
import fxts.stations.core.IClickModel;
import fxts.stations.core.OpenPositions;
import fxts.stations.core.Rates;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequestFactory;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ActionTypes;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Vector;

/**
 * Frames is destined for showing of table with open positions.
 * <br>
 * Creation date (9/20/2003 6:38 PM)
 */
public class OpenPositionsFrame<E> extends ATableFrame<E> implements IClickModel {
    public static final String NAME = "OpenPositions";
    /**
     * Array of resource pairs with names of columns.
     */
    private static final String[] COLUMNS = {
            "IDS_POSITION_TICKET",
            "IDS_POSITION_ACCOUNT",
            "IDS_POSITION_CURRENCY",
            "IDS_POSITION_AMOUNT",
            "IDS_POSITION_BUYORSELL",
            "IDS_POSITION_OPEN",
            "IDS_POSITION_CLOSE",
            "IDS_POSITION_STOP",
            "IDS_STOPMOVE",
            "IDS_POSITION_LIMIT",
            "IDS_PIP_PL",
            "IDS_POSITION_GROSSPL",
            "IDS_POSITION_COMM",
            "IDS_POSITION_INTER",
            "IDS_POSITION_TIME",
            "IDS_CUSTOMTEXT"};
    /* Identifiers of columns. */
    private static final int TICKET_COLUMN = 0;
    private static final int ACCOUNT_COLUMN = 1;
    private static final int CURRENCY_COLUMN = 2;
    private static final int AMOUNT_COLUMN = 3;
    private static final int BUYORSELL_COLUMN = 4;
    private static final int OPEN_COLUMN = 5;
    private static final int CLOSE_COLUMN = 6;
    private static final int STOP_COLUMN = 7;
    private static final int STOP_MOVE_COLUMN = 8;
    private static final int LIMIT_COLUMN = 9;
    private static final int PIP_PL_COLUMN = 10;
    private static final int GROSSPL_COLUMN = 11;
    private static final int COMM_COLUMN = 12;
    private static final int INTER_COLUMN = 13;
    private static final int TIME_COLUMN = 14;
    private static final int CUSTOM_TEXT_COLUMN = 15;
    private static final String CLOSE = "CLOSE";
    private static final String STOP = "STOP";
    private static final String LIMIT = "LIMIT";
    private Color mBGClosedColor;
    private Color mBGHeaderColor;
    private Color mBGMarginColor;
    private Color mBGSelectedColor;
    private Color mEvenColor;
    private Color mFGClosedColor;
    private Color mFGColor;
    private Color mFGHeaderColor;
    private Color mFGMarginColor;
    private Color mFGNonTradeColor;
    private Color mFGSelectedColor;
    private Color mFGTradableColor;
    private Font mFontClosed;
    private Font mFontContent;
    private Font mFontMargin;
    private Font mFontNonTrade;
    private Font mFontSelected;
    private Font mHeaderFont;

    /**
     * Popup menu for limit.
     */
    private JPopupMenu mLimitPopupMenu;
    private Color mOddColor;
    /**
     * Popup menu for stop column.
     */
    private JPopupMenu mStopPopupMenu;
    private Color mSummaryBGTotalColor;
    private Color mSummaryFGTotalColor;
    private Font mTotalFont;
    private Font mTradableSelectedFont;

    /**
     * Constructor.
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    public OpenPositionsFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        setCurSortColumn(TICKET_COLUMN);

        UIManager uim = UIManager.getInst();
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.REMOVE);

        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("OpenPositions.font.content", label.getFont());
        mFGColor = uiPrefs.getColor("OpenPositions.foreground.default");
        mEvenColor = uiPrefs.getColor("OpenPositions.background.default.even");
        mOddColor = uiPrefs.getColor("OpenPositions.background.default.odd");
        mBGSelectedColor = uiPrefs.getColor("OpenPositions.background.selected");
        mFGSelectedColor = uiPrefs.getColor("OpenPositions.foreground.selected");
        mFontSelected = uiPrefs.getFont("OpenPositions.font.selected", label.getFont());
        mBGClosedColor = uiPrefs.getColor("OpenPositions.background.closed");
        mFGClosedColor = uiPrefs.getColor("OpenPositions.foreground.closed");
        mFontClosed = uiPrefs.getFont("OpenPositions.font.closed", label.getFont());
        mFGNonTradeColor = uiPrefs.getColor("OpenPositions.foreground.nontradable");
        mFontNonTrade = uiPrefs.getFont("OpenPositions.font.nontradable", label.getFont());
        mBGMarginColor = uiPrefs.getColor("OpenPositions.background.undermargincall");
        mFGMarginColor = uiPrefs.getColor("OpenPositions.foreground.undermargincall");
        mFontMargin = uiPrefs.getFont("OpenPositions.font.undermargincall", label.getFont());
        mSummaryBGTotalColor = uiPrefs.getColor("Summary.background.total");
        mSummaryFGTotalColor = uiPrefs.getColor("Summary.foreground.total");
        mTotalFont = uiPrefs.getFont("Summary.font.total", label.getFont());
        mBGHeaderColor = uiPrefs.getColor("OpenPositions.background.header");
        mFGHeaderColor = uiPrefs.getColor("OpenPositions.foreground.header");
        mHeaderFont = uiPrefs.getFont("OpenPositions.font.header", label.getFont());
        mFGTradableColor = uiPrefs.getColor("OpenPositions.foreground.nontradable.selected");
        mTradableSelectedFont = uiPrefs.getFont("OpenPositions.font.nontradable.selected", label.getFont());

        //creates renderer
        mCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable aTable,
                                                           Object aValue,
                                                           boolean aIsSelected,
                                                           boolean aHasFocus,
                                                           int aRow,
                                                           int aColumn) {
                OpenPositions openPositions = TradeDesk.getInst().getOpenPositions();
                Object valueAt = aTable.getModel().getValueAt(aRow, TICKET_COLUMN);
                if (openPositions == null || valueAt == null) {
                    return null;
                }
                Rates rates = TradeDesk.getInst().getRates();
                Position position = openPositions.getPosition(valueAt.toString());
                Accounts accounts = TradeDesk.getInst().getAccounts();

                JLabel comp = (JLabel) super.getTableCellRendererComponent(aTable,
                                                                           aValue,
                                                                           aIsSelected,
                                                                           aHasFocus,
                                                                           aRow,
                                                                           aColumn);
                comp.setFont(mFontContent);
                //sets default colors of rows
                setForeground(mFGColor);
                if (aRow % 2 == 0) {
                    setBackground(mEvenColor);
                } else {
                    setBackground(mOddColor);
                }
                if (aIsSelected) {
                    setBackground(mBGSelectedColor);
                    setForeground(mFGSelectedColor);
                    comp.setFont(mFontSelected);
                }
                if (position != null) {
                    String accountId = position.getAccount();
                    String currency = position.getCurrency();
                    //sets color of close position
                    if (position.isBeingClosed()) {
                        comp.setBackground(mBGClosedColor);
                        comp.setForeground(mFGClosedColor);
                        comp.setFont(mFontClosed);
                    }

                    //sets color of position where currency is nontradable
                    Rate rate = rates.getRate(currency);
                    if (rate != null) {
                        if (!rate.isTradable()) {
                            comp.setForeground(mFGNonTradeColor);
                            comp.setFont(mFontNonTrade);
                        }
                    }

                    //sets color of position where account is under margin call
                    Account account = accounts.getAccount(accountId);
                    if (account != null) {
                        if (account.isUnderMarginCall()) {
                            comp.setBackground(mBGMarginColor);
                            comp.setForeground(mFGMarginColor);
                            comp.setFont(mFontMargin);
                        }
                    }

                    //sets color of selected row
                    if (aTable.getSelectedRow() == aRow) {
                        setBackground(mBGSelectedColor);
                        setForeground(mFGSelectedColor);
                        comp.setFont(mFontSelected);
                        if (rate != null && !rate.isTradable()) {
                            comp.setForeground(mFGTradableColor);
                            comp.setFont(mTradableSelectedFont);
                        }
                    }
                } else {
                    if (aIsSelected) {
                        setBackground(mBGSelectedColor);
                        setForeground(mFGSelectedColor);
                        comp.setFont(mFontSelected);
                    } else {
                        setBackground(mSummaryBGTotalColor);
                        setForeground(mSummaryFGTotalColor);
                        comp.setFont(mTotalFont);
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
            //Overriding of DefaultTableCellRenderer`s method
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
                setBackground(mBGHeaderColor);
                setForeground(mFGHeaderColor);
                comp.setFont(mHeaderFont);
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

        //sets renderer to all columns
        setRenderers(mCellRenderer, mHeaderRenderer);
        JTable table = getTable();

        //creates a popup menu
        mStopPopupMenu = UIManager.getInst().createPopupMenu();

        //first menu item
        Action stopLimitOrderAction = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT, STOP);
        uim.addAction(
                stopLimitOrderAction,
                "IDS_STOP_LIMIT",
                "ID_S_L_ICON",
                null,
                "IDS_STOP_LIMIT_DESC",
                "IDS_STOP_LIMIT_DESC");
        JMenuItem menuItem = uim.createMenuItem(stopLimitOrderAction);
        menuItem.setActionCommand(STOP);
        mStopPopupMenu.add(menuItem);

        //second menu item
        Action action = getMainFrame().getAction(ActionTypes.CLOSE_POSITION, null);
        uim.addAction(action, "IDS_CLOSE", "ID_CLOSE_ICON", null, "IDS_CLOSE_DESC", "IDS_CLOSE_DESC");
        menuItem = uim.createMenuItem(action);
        menuItem.setActionCommand(CLOSE);
        mStopPopupMenu.add(menuItem);

        //creates a popup menu
        mLimitPopupMenu = UIManager.getInst().createPopupMenu();
        //first menu item
        stopLimitOrderAction = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT, LIMIT);
        uim.addAction(
                stopLimitOrderAction,
                "IDS_STOP_LIMIT",
                "ID_S_L_ICON",
                null,
                "IDS_STOP_LIMIT_DESC",
                "IDS_STOP_LIMIT_DESC");
        menuItem = uim.createMenuItem(stopLimitOrderAction);
        menuItem.setActionCommand(LIMIT);
        mLimitPopupMenu.add(menuItem);

        //second menu item
        action = getMainFrame().getAction(ActionTypes.CLOSE_POSITION, null);
        uim.addAction(action, "IDS_CLOSE", "ID_CLOSE_ICON", null, "IDS_CLOSE_DESC", "IDS_CLOSE_DESC");
        menuItem = uim.createMenuItem(action);
        mLimitPopupMenu.add(menuItem);
        table.addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent aE) {
                        if (KeyEvent.VK_DELETE == aE.getKeyCode()) {
                            Action closePositionAction = getMainFrame().getAction(ActionTypes.CLOSE_POSITION, null);
                            if (closePositionAction.isEnabled()) {
                                ActionEvent event = new ActionEvent(this, 0, "");
                                closePositionAction.actionPerformed(event);
                            }
                        }
                    }
                });
        //adds mouse listener
        table.addMouseListener(
                new MouseAdapter() {
                    private int mCurrentRow = -1;
                    private int mCurrentColumn = -1;
                    @Override
                    public void mouseReleased(MouseEvent aEvent) {
                        JTable jTable = (JTable) aEvent.getComponent();
                        mCurrentColumn = jTable.columnAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                        mCurrentRow = jTable.rowAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                        if (mCurrentRow == jTable.getRowCount() - 1) { //0 based
                            //ignore processing in totals row
                            return;
                        }
                        //sets frame to selected state (it`s only for assurance)
                        if (!isSelected()) {
                            try {
                                setSelected(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //gets index of column at model
                        TableColumn tableColumn = jTable.getColumnModel().getColumn(mCurrentColumn);
                        if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                            boolean selected = false;
                            for (int selectedRow : jTable.getSelectedRows()) {
                                if (selectedRow == mCurrentRow) {
                                    selected = true;
                                }
                            }
                            if (!selected) {
                                jTable.getSelectionModel().setSelectionInterval(mCurrentRow, mCurrentRow);
                            }
                            OpenPositions openPositions = TradeDesk.getInst().getOpenPositions();
                            if (tableColumn.getModelIndex() == LIMIT_COLUMN) {
                                //shows popup menu
                                mLimitPopupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                            } else {
                                //shows popup menu
                                JMenuItem stopLimitMenuItem = UIManager.getInst().createMenuItem();
                                JMenuItem closeMenuItem = UIManager.getInst().createMenuItem();
                                for (Component component : mStopPopupMenu.getComponents()) {
                                    if (component instanceof JMenuItem) {
                                        JMenuItem item = (JMenuItem) component;
                                        if (STOP.equals(item.getActionCommand())) {
                                            stopLimitMenuItem = item;
                                        } else if (CLOSE.equals(item.getActionCommand())) {
                                            closeMenuItem = item;
                                        }
                                    }
                                }
                                boolean showCloseMenu = true;
                                String tickedID = (String) jTable.getModel().getValueAt(mCurrentRow, TICKET_COLUMN);
                                Position position = openPositions.getPosition(tickedID);
                                Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
                                if (rate != null && !rate.isTradable()) {
                                    showCloseMenu = false;
                                }
                                if (showCloseMenu) {
                                    closeMenuItem.setEnabled(true);
                                    stopLimitMenuItem.setEnabled(true);
                                } else {
                                    closeMenuItem.setEnabled(false);
                                    stopLimitMenuItem.setEnabled(false);
                                }
                                if (jTable.getSelectedRows().length > 1) {
                                    stopLimitMenuItem.setEnabled(false);
                                }
                                mStopPopupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                            }
                        } else {
                            if (jTable.getSelectedRows().length == 1) {
                                if (tableColumn.getModelIndex() == CLOSE_COLUMN) {
                                    String tickedID = (String) jTable.getModel().getValueAt(mCurrentRow, TICKET_COLUMN);
                                    Position position = TradeDesk.getInst().getOpenPositions().getPosition(tickedID);
                                    Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
                                    String username = TradeDesk.getInst().getUserName();
                                    UserPreferences prefs = UserPreferences.getUserPreferences(username);
                                    String tradingMode = prefs.getString(TRADING_MODE);
                                    boolean atBest = prefs.getBoolean(AT_BEST);
                                    int atMarket = prefs.getInt(AT_MARKET);
                                    if (position.getSide() == Side.BUY && rate.isSellTradable()
                                        || position.getSide() == Side.SELL && rate.isBuyTradable()) {
                                        if (SINGLE_CLICK.equals(tradingMode) || DOUBLE_CLICK.equals(tradingMode)) {
                                            Liaison liaison = Liaison.getInstance();
                                            IRequestFactory requestFactory = liaison.getRequestFactory();
                                            IRequest request;
                                            if (atBest) {
                                                request = requestFactory.closeTrueMarket(position.getTicketID(),
                                                                                         position.getAmount(),
                                                                                         null);
                                            } else {
                                                request = requestFactory.closePosition(position.getTicketID(),
                                                                                       position.getAmount(),
                                                                                       null,
                                                                                       atMarket);
                                            }
                                            try {
                                                if (SINGLE_CLICK.equals(tradingMode)) {
                                                    liaison.sendRequest(request);
                                                } else if (DOUBLE_CLICK.equals(tradingMode)
                                                           && aEvent.getClickCount() == 2) {
                                                    liaison.sendRequest(request);
                                                }
                                            } catch (LiaisonException ex) {
                                                ex.printStackTrace();
                                                showUnavailablePriceError();
                                            }
                                        } else {
                                            Action a = getMainFrame().getAction(ActionTypes.CLOSE_POSITION, null);
                                            if (a.isEnabled()) {
                                                ActionEvent event = new ActionEvent(this, 0, CLOSE);
                                                a.actionPerformed(event);
                                            }
                                        }
                                    } else {
                                        showUnavailablePriceError();
                                    }
                                } else if (tableColumn.getModelIndex() == STOP_COLUMN) {
                                    Action a = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT, null);
                                    if (a.isEnabled()) {
                                        ActionEvent event = new ActionEvent(this, 0, STOP);
                                        a.actionPerformed(event);
                                    }
                                } else if (tableColumn.getModelIndex() == LIMIT_COLUMN) {
                                    Action a = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT, null);
                                    if (a.isEnabled()) {
                                        ActionEvent event = new ActionEvent(this, 0, LIMIT);
                                        a.actionPerformed(event);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent aEvent) {
                        mCurrentColumn = -1;
                        mCurrentRow = -1;
                    }

                    @Override
                    public void mouseEntered(MouseEvent aEvent) {
                        mCurrentColumn = -1;
                        mCurrentRow = -1;
                    }
                });

        //sets icon to internal frame
        URL iconUrl = getResourceManager().getResource("ID_OPENPOSITIONS_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }
        fireSorting();
    }

    private void showUnavailablePriceError() {
        JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                      "There is no tradable price. (You cannot trade at this price)",
                                      mResourceManager.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                      JOptionPane.ERROR_MESSAGE);
    }

    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Position aPosition = (Position) aObject;
        //returns value of the specified cell
        if (aColumn == TICKET_COLUMN) {
            return aPosition.getTicketID();
        } else if (aColumn == ACCOUNT_COLUMN) {
            return aPosition.getAccount();
        } else if (aColumn == CURRENCY_COLUMN) {
            return aPosition.getCurrency();
        } else if (aColumn == AMOUNT_COLUMN) {
            Rate rate = TradeDesk.getInst().getRates().getRate(aPosition.getCurrency());
            if (rate.getContractSize() / 1000 <= 0) {
                mFormat.setMinimumFractionDigits(3);
            } else {
                mFormat.setMinimumFractionDigits(0);
            }
            return mFormat.format(aPosition.getAmount() / 1000.0);
        } else if (aColumn == BUYORSELL_COLUMN) {
            return aPosition.getSide().getName();
        } else if (aColumn == OPEN_COLUMN) {
            return TradeDesk.formatPrice(aPosition.getCurrency(), aPosition.getOpenPrice());
        } else if (aColumn == CLOSE_COLUMN) {
            return TradeDesk.formatPrice(aPosition.getCurrency(), aPosition.getClosePrice());
        } else if (aColumn == STOP_COLUMN) {
            double value = aPosition.getStop();
            if (value == 0) {  //if value of stop not setted
                return null;
            } else {
                return TradeDesk.formatPrice(aPosition.getCurrency(), value);
            }
        } else if (aColumn == LIMIT_COLUMN) {
            double value = aPosition.getLimit();
            if (value == 0) {  //if value of limit not setted
                return null;
            } else {
                return TradeDesk.formatPrice(aPosition.getCurrency(), value);
            }
        } else if (aColumn == GROSSPL_COLUMN) {
            if (aPosition.getOpenPrice() != 0 && aPosition.getClosePrice() != 0) {
                return Util.format(aPosition.getGrossPnL());
            } else {
                return null;
            }
        } else if (aColumn == COMM_COLUMN) {
            return Util.format(aPosition.getCommission());
        } else if (aColumn == INTER_COLUMN) {
            return Util.format(aPosition.getInterest());
        } else if (aColumn == CUSTOM_TEXT_COLUMN) {
            return aPosition.getCustomText();
        } else if (aColumn == STOP_MOVE_COLUMN) {
            if (aPosition.getStop() == 0 || aPosition.getTrailStop() == 0) {
                return null;
            } else {
                mFormat.setMinimumFractionDigits(1);
                return mFormat.format(aPosition.getStopMove());
            }
        } else if (aColumn == TIME_COLUMN) {
            if (aPosition.getOpenTime() != null) {
                return formatDate(aPosition.getOpenTime());
            } else {
                return null;
            }
        } else if (aColumn == PIP_PL_COLUMN) {
            mFormat.setMinimumFractionDigits(1);
            return mFormat.format(aPosition.getPipPL());
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
            int positionSize = TradeDesk.getInst().getOpenPositions().size();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_POSITIONS_TITLE"));
            if (positionSize > 0) {
                titleBuffer.append(" (").append(positionSize).append(")");
            }
            if (IClickModel.SINGLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
            } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
            }
            return titleBuffer.toString();
        }
        mLogger.debug("Error: OpenPositionsFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    @Override
    protected SignalVector getSignalVector() {
        return TradeDesk.getInst().getOpenPositions();
    }

    /**
     * Returns table's model.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new OpenPostionsTableModel(aResourceMan);
        }
        return mTableModel;
    }

    /**
     * Returns table name.
     */
    @Override
    protected String getTableName() {
        return NAME;
    }

    @Override
    public boolean isSortable() {
        return true;
    }

    /**
     * Invoked on frame closing.
     *
     * @param aEvent event
     */
    @Override
    protected void onCloseFrame(InternalFrameEvent aEvent) {
        super.onCloseFrame(aEvent);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.REMOVE);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.CHANGE);
        mLimitPopupMenu.removeAll();
        mLimitPopupMenu.removeNotify();
        mLimitPopupMenu = null;

        mStopPopupMenu.removeAll();
        mStopPopupMenu.removeNotify();
        mStopPopupMenu = null;
        removeAll();
        removeNotify();
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("OpenPositions.font.content", label.getFont());
        mFGColor = uiPrefs.getColor("OpenPositions.foreground.default");
        mEvenColor = uiPrefs.getColor("OpenPositions.background.default.even");
        mOddColor = uiPrefs.getColor("OpenPositions.background.default.odd");
        mBGSelectedColor = uiPrefs.getColor("OpenPositions.background.selected");
        mFGSelectedColor = uiPrefs.getColor("OpenPositions.foreground.selected");
        mFontSelected = uiPrefs.getFont("OpenPositions.font.selected", label.getFont());
        mBGClosedColor = uiPrefs.getColor("OpenPositions.background.closed");
        mFGClosedColor = uiPrefs.getColor("OpenPositions.foreground.closed");
        mFontClosed = uiPrefs.getFont("OpenPositions.font.closed", label.getFont());
        mFGNonTradeColor = uiPrefs.getColor("OpenPositions.foreground.nontradable");
        mFontNonTrade = uiPrefs.getFont("OpenPositions.font.nontradable", label.getFont());
        mBGMarginColor = uiPrefs.getColor("OpenPositions.background.undermargincall");
        mFGMarginColor = uiPrefs.getColor("OpenPositions.foreground.undermargincall");
        mFontMargin = uiPrefs.getFont("OpenPositions.font.undermargincall", label.getFont());
        mSummaryBGTotalColor = uiPrefs.getColor("Summary.background.total");
        mSummaryFGTotalColor = uiPrefs.getColor("Summary.foreground.total");
        mTotalFont = uiPrefs.getFont("Summary.font.total", label.getFont());
        mBGHeaderColor = uiPrefs.getColor("OpenPositions.background.header");
        mFGHeaderColor = uiPrefs.getColor("OpenPositions.foreground.header");
        mHeaderFont = uiPrefs.getFont("OpenPositions.font.header", label.getFont());
        mFGTradableColor = uiPrefs.getColor("OpenPositions.foreground.nontradable.selected");
        mTradableSelectedFont = uiPrefs.getFont("OpenPositions.font.nontradable.selected", label.getFont());
    }

    /**
     * Concrete implementation of AFrameTableModel.
     * This class responds for data filling of table.
     */
    private class OpenPostionsTableModel extends AFrameTableModel {
        /**
         * Constructor.
         *
         * @param aResourceMan current resource manager
         */
        OpenPostionsTableModel(ResourceManager aResourceMan) {
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
            if (idx == TICKET_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (idx == ACCOUNT_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (idx == CURRENCY_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == AMOUNT_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == BUYORSELL_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == OPEN_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == CLOSE_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == STOP_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == LIMIT_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == GROSSPL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == COMM_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == INTER_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == TIME_COLUMN) {
                return AFrameTableModel.DATE_COLUMN;
            } else if (idx == CUSTOM_TEXT_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == STOP_MOVE_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == PIP_PL_COLUMN) {
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
            try {
                return TradeDesk.getInst().getOpenPositions().size() + 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        /**
         * Returns the value in the cell at aiCol and aiRow to aValue.
         *
         * @param aRow number of row
         * @param aCol number of column
         */
        public Object getValueAt(int aRow, int aCol) {
            OpenPositions openPositions = TradeDesk.getInst().getOpenPositions();
            if (aRow == openPositions.size()) {
                if (aCol == TICKET_COLUMN) {
                    return getResourceManager().getString("IDS_SUMMARY_TOTAL");
                } else if (aCol == AMOUNT_COLUMN) {
                    if (openPositions.getTotalAmount() / 1000 <= 0) {
                        mFormat.setMinimumFractionDigits(3);
                    } else {
                        mFormat.setMinimumFractionDigits(0);
                    }
                    return mFormat.format(openPositions.getTotalAmount() / 1000.0);
                } else if (aCol == PIP_PL_COLUMN) {
                    mFormat.setMinimumFractionDigits(1);
                    return mFormat.format(openPositions.getPipPL());
                } else if (aCol == GROSSPL_COLUMN) {
                    return Util.format(openPositions.getTotalGrossPnL());
                } else if (aCol == COMM_COLUMN) {
                    return Util.format(openPositions.getTotalCommision());
                } else if (aCol == INTER_COLUMN) {
                    return Util.format(openPositions.getTotalInterest());
                }
            } else {
                try {
                    Position position = (Position) openPositions.get(aRow);
                    return getColumnValue(aCol, position);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
            return null;
        }
    }
}
