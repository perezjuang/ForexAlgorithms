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
 * 12/8/2004    Andre Mermegas  -- updated format of amount to be more like classic TS aka 100 K insted of 1 lot
 * 05/10/2006   Andre Mermegas: update to show stop/limit action in all popup menus
 * 05/11/2007   Andre Mermegas: default sort by first column, newest on top
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.Accounts;
import fxts.stations.core.IClickModel;
import fxts.stations.core.Orders;
import fxts.stations.core.Rates;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ActionTypes;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.UserPreferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
 * Frames is destined for showing of table with orders.
 * <br>
 * Creation date (9/20/2003 6:38 PM)
 */
public class OrdersFrame<E> extends ATableFrame<E> {
    public static final String NAME = "Orders";
    /**
     * Array of resource pairs with names of columns.
     */
    private static final String[] COLUMNS = {
            "IDS_ORDER_ID",
            "IDS_ORDER_ACCOUNT",
            "IDS_ORDER_TYPE",
            "IDS_ORDER_STATUS",
            "IDS_ORDER_CURRENCY",
            "IDS_ORDER_AMOUNT",
            "IDS_SELL_TEXT",
            "IDS_BUY_TEXT",
            "IDS_ORDER_STOP",
            "IDS_ORDER_LIMIT",
            "IDS_ORDER_TIME",
            "IDS_CUSTOMTEXT"};
    /* Identifiers of columns. */
    private static final int ORDER_ID_COLUMN = 0;
    private static final int ACCOUNT_COLUMN = 1;
    private static final int TYPE_COLUMN = 2;
    private static final int STATUS_COLUMN = 3;
    private static final int CURRENCY_COLUMN = 4;
    private static final int AMOUNT_COLUMN = 5;
    private static final int SELL_COLUMN = 6;
    private static final int BUY_COLUMN = 7;
    private static final int STOP_COLUMN = 8;
    private static final int LIMIT_COLUMN = 9;
    private static final int TIME_COLUMN = 10;
    private static final int CUSTOM_TEXT_COLUMN = 11;
    private static final String STOP = "STOP";
    private static final String LIMIT = "LIMIT";
    private static final String UPDATE_ENTRY_ORDER = "UPDATE_ENTRY_ORDER";
    private Color mColorBGHeader;
    private Color mColorBGMargin;
    private Color mColorBGSelected;
    private Color mColorBGSelectedTradable;
    private Color mColorEven;
    private Color mColorFG;
    private Color mColorFGHeader;
    private Color mColorFGMargin;
    private Color mColorFGSelected;
    private Color mColorNontradable;
    private Color mColorOdd;
    private Font mFontContent;
    private Font mFontContentHeader;
    private Font mFontContentMargin;
    private Font mFontContentNontradable;
    private Font mFontContentSelected;
    private Font mFontContentTradableSelected;

    /**
     * Popup menu.
     */
    private JPopupMenu mPopupLimitMenu;
    /**
     * Popup menu.
     */
    private JPopupMenu mPopupStopMenu;

    /**
     * Constructor.
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    public OrdersFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        setCurSortColumn(ORDER_ID_COLUMN);

        UIManager uim = UIManager.getInst();
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.REMOVE);

        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("Orders.font.content", label.getFont());
        mColorFG = uiPrefs.getColor("Orders.foreground.default");
        mColorEven = uiPrefs.getColor("Orders.background.default.even");
        mColorOdd = uiPrefs.getColor("Orders.background.default.odd");
        mColorNontradable = uiPrefs.getColor("Orders.foreground.nontradable");
        mFontContentNontradable = uiPrefs.getFont("Orders.font.nontradable", label.getFont());
        mColorBGMargin = uiPrefs.getColor("Orders.background.undermargincall");
        mColorFGMargin = uiPrefs.getColor("Orders.foreground.undermargincall");
        mFontContentMargin = uiPrefs.getFont("Orders.font.undermargincall", label.getFont());
        mColorBGSelected = uiPrefs.getColor("Orders.background.selected");
        mColorFGSelected = uiPrefs.getColor("Orders.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Orders.font.selected", label.getFont());
        mColorBGSelectedTradable = uiPrefs.getColor("Orders.foreground.nontradable.selected");
        mFontContentTradableSelected = uiPrefs.getFont("Orders.font.nontradable.selected", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Orders.background.header");
        mColorFGHeader = uiPrefs.getColor("Orders.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Orders.font.header", label.getFont());

        //creates renderer
        mCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable aTable,
                                                           Object aValue,
                                                           boolean aIsSelected,
                                                           boolean aHasFocus,
                                                           int aRow,
                                                           int aColumn) {
                Orders orders = TradeDesk.getInst().getOrders();
                Rates rates = TradeDesk.getInst().getRates();
                Accounts accounts = TradeDesk.getInst().getAccounts();
                Object valueAt = aTable.getModel().getValueAt(aRow, ORDER_ID_COLUMN);
                if (orders == null || rates == null || accounts == null || valueAt == null) {
                    return null;
                }
                Order order = orders.getOrder(valueAt.toString());
                JLabel comp = (JLabel) super.getTableCellRendererComponent(aTable,
                                                                           aValue,
                                                                           aIsSelected,
                                                                           aHasFocus,
                                                                           aRow,
                                                                           aColumn);
                comp.setFont(mFontContent);
                //sets default colors of rows
                setForeground(mColorFG);
                if (aRow % 2 == 0) {
                    setBackground(mColorEven);
                } else {
                    setBackground(mColorOdd);
                }

                //sets color of order with nontradable currency
                String sCurrency = order.getCurrency();
                Rate rate = rates.getRate(sCurrency);
                if (rate != null) {
                    if (!rate.isTradable()) {
                        comp.setForeground(mColorNontradable);
                        comp.setFont(mFontContentNontradable);
                    }
                }

                //sets color of order where account is under margin call
                Account account = accounts.getAccount(order.getAccount());
                if (account != null) {
                    if (account.isUnderMarginCall()) {
                        comp.setBackground(mColorBGMargin);
                        comp.setForeground(mColorFGMargin);
                        comp.setFont(mFontContentMargin);
                    }
                }

                //sets color of selected row
                if (aIsSelected) {
                    setBackground(mColorBGSelected);
                    setForeground(mColorFGSelected);
                    comp.setFont(mFontContentSelected);
                    if (rate != null && !rate.isTradable()) {
                        comp.setForeground(mColorBGSelectedTradable);
                        comp.setFont(mFontContentTradableSelected);
                    }
                }

                setOpaque(true);
                //sets alignment at cell
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
                setBackground(mColorBGHeader);
                setForeground(mColorFGHeader);
                comp.setFont(mFontContentHeader);
                setOpaque(true);
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

        //sets render to all columns
        setRenderers(mCellRenderer, mHeaderRenderer);

        //creates a popup menu
        mPopupStopMenu = UIManager.getInst().createPopupMenu();
        mPopupLimitMenu = UIManager.getInst().createPopupMenu();
        Action updateEntryOrderAction = getMainFrame().getAction(ActionTypes.UPDATE_ENTRY_ORDER, null);
        uim.addAction(
                updateEntryOrderAction,
                "IDS_UPDATE_ENTRY_ORDER",
                "ID_ENTRY_ICON",
                null,
                "IDS_UPDATE_ENTRY_ORDER_DESC",
                "IDS_UPDATE_ENTRY_ORDER_DESC");
        JMenuItem menuItem = uim.createMenuItem(updateEntryOrderAction);
        menuItem.setActionCommand(UPDATE_ENTRY_ORDER);
        mPopupStopMenu.add(menuItem);
        updateEntryOrderAction = getMainFrame().getAction(ActionTypes.UPDATE_ENTRY_ORDER, null);
        uim.addAction(
                updateEntryOrderAction,
                "IDS_UPDATE_ENTRY_ORDER",
                "ID_ENTRY_ICON",
                null,
                "IDS_UPDATE_ENTRY_ORDER_DESC",
                "IDS_UPDATE_ENTRY_ORDER_DESC");
        menuItem = uim.createMenuItem(updateEntryOrderAction);
        menuItem.setActionCommand(UPDATE_ENTRY_ORDER);
        mPopupLimitMenu.add(menuItem);
        Action stopLimitOrderAction = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT_ORDER, STOP);
        uim.addAction(
                stopLimitOrderAction,
                "IDS_STOP_LIMIT",
                "ID_S_L_ICON",
                null,
                "IDS_STOP_LIMIT_DESC",
                "IDS_STOP_LIMIT_DESC");
        menuItem = uim.createMenuItem(stopLimitOrderAction);
        menuItem.setActionCommand(STOP);
        mPopupStopMenu.add(menuItem);
        stopLimitOrderAction = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT_ORDER, LIMIT);
        uim.addAction(
                stopLimitOrderAction,
                "IDS_STOP_LIMIT",
                "ID_S_L_ICON",
                null,
                "IDS_STOP_LIMIT_DESC",
                "IDS_STOP_LIMIT_DESC");
        menuItem = uim.createMenuItem(stopLimitOrderAction);
        menuItem.setActionCommand(LIMIT);
        mPopupLimitMenu.add(menuItem);
        Action removeEntryOrderAction = getMainFrame().getAction(ActionTypes.REMOVE_ENTRY_ORDER, null);
        uim.addAction(
                removeEntryOrderAction,
                "IDS_REMOVE_ORDER",
                "ID_CLOSE_ICON",
                null,
                "IDS_REMOVE_ORDER_DESC",
                "IDS_REMOVE_ORDER_DESC");
        menuItem = uim.createMenuItem(removeEntryOrderAction);
        menuItem.setActionCommand("REMOVE_ORDER");
        mPopupStopMenu.add(menuItem);
        removeEntryOrderAction = getMainFrame().getAction(ActionTypes.REMOVE_ENTRY_ORDER, null);
        uim.addAction(
                removeEntryOrderAction,
                "IDS_REMOVE_ORDER",
                "ID_CLOSE_ICON",
                null,
                "IDS_REMOVE_ORDER_DESC",
                "IDS_REMOVE_ORDER_DESC");
        menuItem = uim.createMenuItem(removeEntryOrderAction);
        menuItem.setActionCommand("REMOVE_ORDER");
        mPopupLimitMenu.add(menuItem);
        getTable().addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent aKeyEvent) {
                        if (KeyEvent.VK_DELETE == aKeyEvent.getKeyCode()) {
                            Action action = getMainFrame().getAction(ActionTypes.REMOVE_ENTRY_ORDER, null);
                            if (action.isEnabled()) {
                                ActionEvent event = new ActionEvent(this, 0, "");
                                action.actionPerformed(event);
                            }
                        }
                    }
                });

        //adds mouse listener
        getTable().addMouseListener(
                new MouseAdapter() {
                    private int mCurrentRow = -1;
                    private int mCurrentColumn = -1;

                    @Override
                    public void mouseReleased(MouseEvent aEvent) {
                        JTable table = (JTable) aEvent.getComponent();
                        mCurrentColumn = table.columnAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                        mCurrentRow = table.rowAtPoint(new Point(aEvent.getX(), aEvent.getY()));

                        //sets frame to selected state (it`s only for assurance)
                        if (!isSelected()) {
                            try {
                                setSelected(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        //gets index of column at model
                        TableColumn column = table.getColumnModel().getColumn(mCurrentColumn);
                        if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                            boolean selected = false;
                            for (int selectedRow : table.getSelectedRows()) {
                                if (selectedRow == mCurrentRow) {
                                    selected = true;
                                }
                            }
                            if (!selected) {
                                table.getSelectionModel().setSelectionInterval(mCurrentRow, mCurrentRow);
                            }

                            Orders orders = TradeDesk.getInst().getOrders();
                            if (column.getModelIndex() == LIMIT_COLUMN) {
                                //shows popup menu
                                mPopupLimitMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                            } else if (column.getModelIndex() == STOP_COLUMN) {
                                //shows popup menu
                                mPopupStopMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                            } else {
                                String orderId = (String) table.getModel().getValueAt(mCurrentRow, ORDER_ID_COLUMN);
                                Order order = orders.getOrder(orderId);
                                if (order.isChangeable()) {
                                    mPopupStopMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                                }
                            }
                        } else {
                            if (table.getSelectedRows().length == 1) {
                                if (column.getModelIndex() == STOP_COLUMN) {
                                    Action action = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT_ORDER, null);
                                    if (action.isEnabled()) {
                                        ActionEvent event = new ActionEvent(this, 0, STOP);
                                        action.actionPerformed(event);
                                    }
                                } else if (column.getModelIndex() == LIMIT_COLUMN) {
                                    Action action = getMainFrame().getAction(ActionTypes.SET_STOP_LIMIT_ORDER, null);
                                    if (action.isEnabled()) {
                                        ActionEvent event = new ActionEvent(this, 0, LIMIT);
                                        action.actionPerformed(event);
                                    }
                                } else if (column.getModelIndex() == BUY_COLUMN
                                           || column.getModelIndex() == SELL_COLUMN) {
                                    Action action = getMainFrame().getAction(ActionTypes.UPDATE_ENTRY_ORDER, null);
                                    if (action.isEnabled()) {
                                        ActionEvent event = new ActionEvent(this, 0, "UPDATE_ENTRY");
                                        action.actionPerformed(event);
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
        URL iconUrl = getResourceManager().getResource("ID_ORDERS_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }
        fireSorting();
    }

    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Order aOrder = (Order) aObject;
        if (aColumn == ORDER_ID_COLUMN) {
            return aOrder.getOrderID();
        } else if (aColumn == ACCOUNT_COLUMN) {
            return aOrder.getAccount();
        } else if (aColumn == TYPE_COLUMN) {
            return aOrder.getType();
        } else if (aColumn == STATUS_COLUMN) {
            return aOrder.getStatus();
        } else if (aColumn == CURRENCY_COLUMN) {
            return aOrder.getCurrency();
        } else if (aColumn == AMOUNT_COLUMN) {
            Rate rate = TradeDesk.getInst().getRates().getRate(aOrder.getCurrency());
            if (rate.getContractSize() / 1000 <= 0) {
                mFormat.setMinimumFractionDigits(3);
            } else {
                mFormat.setMinimumFractionDigits(0);
            }
            return mFormat.format(aOrder.getAmount() / 1000.0);
        } else if (aColumn == SELL_COLUMN) {
            return aOrder.getSide() == Side.SELL ? TradeDesk.formatPrice(aOrder.getCurrency(),
                                                                         aOrder.getOrderRate()) : "";
        } else if (aColumn == BUY_COLUMN) {
            return aOrder.getSide() == Side.BUY ? TradeDesk.formatPrice(aOrder.getCurrency(),
                                                                        aOrder.getOrderRate()) : "";
        } else if (aColumn == STOP_COLUMN) {
            double value = aOrder.getStopRate();
            if (value == 0) {  //if value of limit not setted
                return "";
            } else {
                if (getPrecision(value) == 0) {
                    return (int) value;
                } else if (getPrecision(value) == 1) {
                    return value;
                } else {
                    return TradeDesk.formatPrice(aOrder.getCurrency(), value);
                }
            }
        } else if (aColumn == LIMIT_COLUMN) {
            double value = aOrder.getLimitRate();
            if (value == 0) {  //if value of limit not setted
                return "";
            } else {
                if (getPrecision(value) == 0) {
                    return (int) value;
                } else if (getPrecision(value) == 1) {
                    return value;
                } else {
                    return TradeDesk.formatPrice(aOrder.getCurrency(), value);
                }
            }
        } else if (aColumn == TIME_COLUMN) {
            if (aOrder.getTime() != null) {
                return formatDate(aOrder.getTime());
            } else {
                return "";
            }
        } else if (aColumn == CUSTOM_TEXT_COLUMN) {
            return aOrder.getCustomText();
        } else {
            return "";
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
            int orderSize = TradeDesk.getInst().getOrders().size();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_ORDERS_TITLE"));
            if (orderSize > 0) {
                titleBuffer.append(" (").append(orderSize).append(")");
            }
            if (IClickModel.SINGLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
            } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
            }
            return titleBuffer.toString();
        }
        mLogger.debug("Error: OrdersFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    private int getPrecision(double aPrice) {
        //try to determine precision from price
        try {
            String value = String.valueOf(aPrice).split("\\.")[1];
            if ("0".equals(value)) {
                return 0;
            } else {
                return value.length();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    protected SignalVector getSignalVector() {
        return TradeDesk.getInst().getOrders();
    }

    /**
     * Returns table's model.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new OrdersTableModel(aResourceMan);
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
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.REMOVE);
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.CHANGE);

        mPopupLimitMenu.removeAll();
        mPopupLimitMenu.removeNotify();
        mPopupLimitMenu = null;

        mPopupStopMenu.removeAll();
        mPopupStopMenu.removeNotify();
        mPopupStopMenu = null;
        removeAll();
        removeNotify();
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("Orders.font.content", label.getFont());
        mColorFG = uiPrefs.getColor("Orders.foreground.default");
        mColorEven = uiPrefs.getColor("Orders.background.default.even");
        mColorOdd = uiPrefs.getColor("Orders.background.default.odd");
        mColorNontradable = uiPrefs.getColor("Orders.foreground.nontradable");
        mFontContentNontradable = uiPrefs.getFont("Orders.font.nontradable", label.getFont());
        mColorBGMargin = uiPrefs.getColor("Orders.background.undermargincall");
        mColorFGMargin = uiPrefs.getColor("Orders.foreground.undermargincall");
        mFontContentMargin = uiPrefs.getFont("Orders.font.undermargincall", label.getFont());
        mColorBGSelected = uiPrefs.getColor("Orders.background.selected");
        mColorFGSelected = uiPrefs.getColor("Orders.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Orders.font.selected", label.getFont());
        mColorBGSelectedTradable = uiPrefs.getColor("Orders.foreground.nontradable.selected");
        mFontContentTradableSelected = uiPrefs.getFont("Orders.font.nontradable.selected", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Orders.background.header");
        mColorFGHeader = uiPrefs.getColor("Orders.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Orders.font.header", label.getFont());
    }

    /**
     * Concrete implementation of AFrameTableModel.
     * This class responds for data filling of table.
     */
    private class OrdersTableModel extends AFrameTableModel {
        /**
         * Constructor.
         *
         * @param aResourceMan current resource manager
         */
        OrdersTableModel(ResourceManager aResourceMan) {
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
            switch (column.getModelIndex()) {
                case ORDER_ID_COLUMN:
                    return AFrameTableModel.INT_COLUMN;
                case ACCOUNT_COLUMN:
                    return AFrameTableModel.INT_COLUMN;
                case TYPE_COLUMN:
                    return AFrameTableModel.STRING_COLUMN;
                case STATUS_COLUMN:
                    return AFrameTableModel.STRING_COLUMN;
                case CURRENCY_COLUMN:
                    return AFrameTableModel.STRING_COLUMN;
                case AMOUNT_COLUMN:
                    return AFrameTableModel.INT_COLUMN;
                case SELL_COLUMN:
                    return AFrameTableModel.DOUBLE_COLUMN;
                case BUY_COLUMN:
                    return AFrameTableModel.DOUBLE_COLUMN;
                case STOP_COLUMN:
                    return AFrameTableModel.DOUBLE_COLUMN;
                case LIMIT_COLUMN:
                    return AFrameTableModel.DOUBLE_COLUMN;
                case TIME_COLUMN:
                    return AFrameTableModel.DATE_COLUMN;
                case CUSTOM_TEXT_COLUMN:
                    return AFrameTableModel.STRING_COLUMN;
                default:
                    return 0;
            }
        }

        /**
         * Returns the number of rows in the model.
         */
        public int getRowCount() {
            try {
                return TradeDesk.getInst().getOrders().size();
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
            try {
                Orders orders = TradeDesk.getInst().getOrders();
                if (aRow < orders.size()) {
                    Order order = (Order) orders.get(aRow);
                    return getColumnValue(aCol, order);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
