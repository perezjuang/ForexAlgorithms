/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/AccountsFrame.java#3 $
 *
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
 * 07/05/2006   Andre Mermegas: update to allow for totals row not be sorted
 * 07/18/2006   Andre Mermegas: performance update
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.Accounts;
import fxts.stations.core.IClickModel;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.ITable;
import fxts.stations.ui.ITableSelectionListener;
import fxts.stations.ui.UIManager;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
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
import java.awt.RenderingHints;
import java.net.URL;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Frames is destined for showing of table with accounts.
 * Creation date (9/20/2003 6:38 PM)
 */
public class AccountsFrame<E> extends ATableFrame<E> implements ITableSelectionListener {
    public static final String NAME = "Accounts";
    /**
     * Array of resource pairs with names of columns.
     */
    private static final String[] COLUMNS = {
            "IDS_ACCOUNT_ACCOUNT",
            "IDS_ACCOUNT_BALANCE",
            "IDS_ACCOUNT_EQUITY",
            "IDS_ACCOUNT_DAYPNL",
            "IDS_ACCOUNT_USED_MARGIN",
            "IDS_ACCOUNT_USABLE_MARGIN",
            "IDS_ACCOUNT_GROSSPL",
            "IDS_ACCOUNT_MARGIN_CALL",
            "IDS_ACCOUNT_HEDGING"
    };
    /* Identifiers of columns. */
    private static final int ACCOUNT_COLUMN = 0;
    private static final int BALANCE_COLUMN = 1;
    private static final int EQUITY_COLUMN = 2;
    private static final int DAY_PNL = 3;
    private static final int USED_MARGIN_COLUMN = 4;
    private static final int USABLE_MARGIN_COLUMN = 5;
    private static final int GROSSPL_COLUMN = 6;
    private static final int MARGIN_CALL_COLUMN = 7;
    private static final int HEDGING_COLUMN = 8;
    private Color mColorBGHeader;
    private Color mColorBGMargin;
    private Color mColorBGSelected;
    private Color mColorBGTotal;
    private Color mColorDefault;
    private Color mColorEven;
    private Color mColorFGHeader;
    private Color mColorFGMargin;
    private Color mColorFGSelected;
    private Color mColorFGTotal;
    private Color mColorOdd;
    private Font mFontContent;
    private Font mFontContentHeader;
    private Font mFontContentMargin;
    private Font mFontContentSelected;
    private Font mFontContentTotal;
    private String mHedgingColumn;
    private Account mSelectedAccount;

    /**
     * Constructor.
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    public AccountsFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        setCurSortColumn(ACCOUNT_COLUMN);

        TradeDesk.getInst().getAccounts().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getAccounts().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getAccounts().subscribe(this, SignalType.REMOVE);

        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mColorBGSelected = uiPrefs.getColor("Accounts.background.selected");
        mColorFGSelected = uiPrefs.getColor("Accounts.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Accounts.font.selected", label.getFont());
        mFontContent = uiPrefs.getFont("Accounts.font.content", label.getFont());
        mColorDefault = uiPrefs.getColor("Accounts.foreground.default");
        mColorEven = uiPrefs.getColor("Accounts.background.default.even");
        mColorOdd = uiPrefs.getColor("Accounts.background.default.odd");
        mColorBGMargin = uiPrefs.getColor("Accounts.background.undermargincall");
        mColorFGMargin = uiPrefs.getColor("Accounts.foreground.undermargincall");
        mFontContentMargin = uiPrefs.getFont("Accounts.font.undermargincall", label.getFont());
        mColorBGTotal = uiPrefs.getColor("Summary.background.total");
        mColorFGTotal = uiPrefs.getColor("Summary.foreground.total");
        mFontContentTotal = uiPrefs.getFont("Summary.font.total", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Accounts.background.header");
        mColorFGHeader = uiPrefs.getColor("Accounts.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Accounts.font.header", label.getFont());

        //creates renderer
        mCellRenderer = new DefaultTableCellRenderer() {
            private ImageIcon createImageIcon() {
                //gets instance of application
                TradeApp app = TradeApp.getInst();
                //get resouce manager
                ResourceManager resmng = app.getResourceManager();
                //sets icon to internal frame
                URL iconUrl = resmng.getResource("ID_DISABLED_ITEM_ICON");
                if (iconUrl != null) {
                    return new ImageIcon(iconUrl);
                } else {
                    return null;
                }
            }

            @Override
            public Component getTableCellRendererComponent(JTable aTable,
                                                           Object aValue,
                                                           boolean aIsSelected,
                                                           boolean aHasFocus,
                                                           int aRow,
                                                           int aColumn) {
                Accounts accounts = TradeDesk.getInst().getAccounts();
                Object valueAt = aTable.getModel().getValueAt(aRow, ACCOUNT_COLUMN);
                if (accounts == null || valueAt == null) {
                    return null;
                }
                Account account = accounts.getAccount(valueAt.toString());

                JLabel comp = (JLabel) super.getTableCellRendererComponent(aTable,
                                                                           aValue,
                                                                           aIsSelected,
                                                                           aHasFocus,
                                                                           aRow,
                                                                           aColumn);
                if (account != null) {
                    comp.setFont(mFontContent);
                    //sets default colors of rows
                    setForeground(mColorDefault);
                    if (aRow % 2 == 0) {
                        setBackground(mColorEven);
                    } else {
                        setBackground(mColorOdd);
                    }

                    //sets color of row where account is under margin call
                    if (account.isUnderMarginCall()) {
                        comp.setBackground(mColorBGMargin);
                        comp.setForeground(mColorFGMargin);
                        comp.setFont(mFontContentMargin);
                    }

                    //sets color of selected row
                    if (aTable.getSelectedRow() == aRow) {
                        setBackground(mColorBGSelected);
                        setForeground(mColorFGSelected);
                        comp.setFont(mFontContentSelected);
                    }
                    if (aColumn == ACCOUNT_COLUMN && account.isLocked()) {
                        comp.setIcon(createImageIcon());
                    } else {
                        comp.setIcon(null);
                    }
                } else {
                    if (aIsSelected) {
                        setBackground(mColorBGSelected);
                        setForeground(mColorFGSelected);
                        comp.setFont(mFontContentSelected);
                    } else {
                        setBackground(mColorBGTotal);
                        setForeground(mColorFGTotal);
                        comp.setFont(mFontContentTotal);
                    }
                }
                setOpaque(true);
                if (aColumn == 0) {
                    comp.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    comp.setHorizontalAlignment(SwingConstants.RIGHT);
                }
                int iHeight = comp.getFontMetrics(comp.getFont()).getHeight() + 2;
                if (iHeight > mInitialHeight && aTable.getRowHeight(aRow) < iHeight) {
                    EventQueue.invokeLater(new InvokerSetRowHeight(aTable, aRow, iHeight));
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

        //sets icon to internal frame
        URL iconUrl = getResourceManager().getResource("ID_ACCOUNTS_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }
        mHedgingColumn = getTable().getColumnName(HEDGING_COLUMN);
        getFrameTable().addSelectionListener(this);
        fireSorting();
    }

    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Account aAccount = (Account) aObject;
        if (aColumn == ACCOUNT_COLUMN) {
            return aAccount.getAccount();
        } else if (aColumn == BALANCE_COLUMN) {
            return Util.format(aAccount.getBalance());
        } else if (aColumn == EQUITY_COLUMN) {
            return Util.format(aAccount.getEquity());
        } else if (aColumn == USED_MARGIN_COLUMN) {
            return Util.format(aAccount.getUsedMargin());
        } else if (aColumn == USABLE_MARGIN_COLUMN) {
            return Util.format(aAccount.getUsableMargin());
        } else if (aColumn == MARGIN_CALL_COLUMN) {
            return aAccount.isUnderMarginCall() ? "Y" : "N";
        } else if (aColumn == HEDGING_COLUMN) {
            return aAccount.getHedging();
        } else if (aColumn == GROSSPL_COLUMN) {
            return Util.format(aAccount.getGrossPnL());
        } else if (aColumn == DAY_PNL) {
            return Util.format(aAccount.getBalance() - aAccount.getStartCash());
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
            int orderSize = TradeDesk.getInst().getAccounts().size();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_ACCOUNTS_TITLE"));
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
        mLogger.debug("AccountsFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    public Account getSelectedAccount() {
        if (mSelectedAccount == null) {
            return null;
        } else {
            return mSelectedAccount;
        }
    }

    @Override
    protected SignalVector getSignalVector() {
        return TradeDesk.getInst().getAccounts();
    }

    /**
     * Returns table's model.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new AccountsTableModel(aResourceMan);
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
        TradeDesk.getInst().getAccounts().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getAccounts().unsubscribe(this, SignalType.REMOVE);
        TradeDesk.getInst().getAccounts().unsubscribe(this, SignalType.CHANGE);
        getFrameTable().removeSelectionListener(this);
        mCheckBoxMap.clear();
    }

    public void onTableChangeSelection(ITable aTable, int[] aiRow) {
        try {
            Accounts accounts = TradeDesk.getInst().getAccounts();
            if (!accounts.isEmpty()) {
                mSelectedAccount = (Account) accounts.get(aiRow[0]);
                double contractSize = mSelectedAccount.getBaseUnitSize();
                mLogger.debug(mSelectedAccount.getAccount() + " unitsize = " + Util.format(contractSize));
                //pipcost changes if unitsize changes
                TradeDesk.updatePipCosts();
            }
        } catch (Exception e) {
            mSelectedAccount = null;
            //swallow
        }
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mColorBGSelected = uiPrefs.getColor("Accounts.background.selected");
        mColorFGSelected = uiPrefs.getColor("Accounts.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Accounts.font.selected", label.getFont());
        mFontContent = uiPrefs.getFont("Accounts.font.content", label.getFont());
        mColorDefault = uiPrefs.getColor("Accounts.foreground.default");
        mColorEven = uiPrefs.getColor("Accounts.background.default.even");
        mColorOdd = uiPrefs.getColor("Accounts.background.default.odd");
        mColorBGMargin = uiPrefs.getColor("Accounts.background.undermargincall");
        mColorFGMargin = uiPrefs.getColor("Accounts.foreground.undermargincall");
        mFontContentMargin = uiPrefs.getFont("Accounts.font.undermargincall", label.getFont());
        mColorBGTotal = uiPrefs.getColor("Summary.background.total");
        mColorFGTotal = uiPrefs.getColor("Summary.foreground.total");
        mFontContentTotal = uiPrefs.getFont("Summary.font.total", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Accounts.background.header");
        mColorFGHeader = uiPrefs.getColor("Accounts.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Accounts.font.header", label.getFont());
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            TimeZone tz = TimeZone.getTimeZone(TradingServerSession.getInstance().getParameterValue("BASE_TIME_ZONE"));
            if (TimeZone.getTimeZone("Japan").getDisplayName().equals(tz.getDisplayName())) {
                JCheckBoxMenuItem item = mCheckBoxMap.get(mHedgingColumn);
                item.setEnabled(false);
                item.setSelected(false);
                getTable().removeColumn(getTable().getColumn(mHedgingColumn));
                revalidate();
                repaint();
            }
        }
    }

    /**
     * Concrete implementation of AFrameTableModel.
     * This class responds for data filling of table.
     */
    private class AccountsTableModel extends AFrameTableModel {
        /**
         * Constructor.
         *
         * @param aResourceMan current resource manager
         */
        AccountsTableModel(ResourceManager aResourceMan) {
            super(aResourceMan, COLUMNS);
        }

        /**
         * Returns columns types.
         *
         * @param aColumn number of the column
         */
        @Override
        public int getColumnType(int aColumn) {
            TableColumn column = getTable().getColumnModel().getColumn(aColumn);
            int i = column.getModelIndex();
            if (i == ACCOUNT_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (i == BALANCE_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == EQUITY_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == USED_MARGIN_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == USABLE_MARGIN_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == MARGIN_CALL_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (i == HEDGING_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (i == GROSSPL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            }

            //if not correct column was specified
            return 0;
        }

        /**
         * Returns the number of rows in the model.
         */
        public int getRowCount() {
            try {
                return TradeDesk.getInst().getAccounts().size() + 1;
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
                TradeDesk tradeDesk = TradeDesk.getInst();
                //gets account with number is equal of value "iRow"
                Accounts accounts = tradeDesk.getAccounts();
                if (aRow == accounts.size()) {
                    if (aCol == ACCOUNT_COLUMN) {
                        return getResourceManager().getString("IDS_SUMMARY_TOTAL");
                    } else if (aCol == BALANCE_COLUMN) {
                        return Util.format(accounts.getTotalBalance());
                    } else if (aCol == EQUITY_COLUMN) {
                        return Util.format(accounts.getTotalEquity());
                    } else if (aCol == USED_MARGIN_COLUMN) {
                        return Util.format(accounts.getTotalUsedMargin());
                    } else if (aCol == USABLE_MARGIN_COLUMN) {
                        return Util.format(accounts.getTotalUsableMargin());
                    } else if (aCol == GROSSPL_COLUMN) {
                        return Util.format(accounts.getTotalGrossPnL());
                    }
                } else {
                    return getColumnValue(aCol, accounts.get(aRow));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
