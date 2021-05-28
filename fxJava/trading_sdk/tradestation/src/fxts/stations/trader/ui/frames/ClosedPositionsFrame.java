/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/ClosedPositionsFrame.java#1 $
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
 * 07/05/2006   Andre Mermegas: update to allow for totals row not be sorted
 * 06/1/2007   Andre Mermegas: default sort by first column, newest on top
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.ClosedPositions;
import fxts.stations.core.IClickModel;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.ui.UIManager;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import java.util.Vector;

/**
 * Frames is destined for showing of table with open positions.
 * <br>
 * Creation date (9/20/2003 6:38 PM)
 */
public class ClosedPositionsFrame<E> extends ATableFrame<E> {
    public static final String NAME = "ClosedPositions";
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
            "IDS_POSITION_GROSSPL",
            "IDS_POSITION_COMM",
            "IDS_POSITION_INTER",
            "IDS_POSITION_NETPL",
            "IDS_POSITION_TIME",
            "IDS_POSITION_CLOSE_TIME",
            "IDS_CUSTOMTEXT",
            "IDS_CUSTOMTEXT2"};
    /* Identifiers of columns. */
    private static final int TICKET_COLUMN = 0;
    private static final int ACCOUNT_COLUMN = 1;
    private static final int CURRENCY_COLUMN = 2;
    private static final int AMOUNT_COLUMN = 3;
    private static final int BUYORSELL_COLUMN = 4;
    private static final int OPEN_COLUMN = 5;
    private static final int CLOSE_COLUMN = 6;
    private static final int GROSSPL_COLUMN = 7;
    private static final int COMMISION_COLUMN = 8;
    private static final int INTEREST_COLUMN = 9;
    private static final int NETPL_COLUMN = 10;
    private static final int TIME_COLUMN = 11;
    private static final int POSITION_CLOSE_TIME_COLUMN = 12;
    private static final int CUSTOM_TEXT_COLUMN = 13;
    private static final int CUSTOM_TEXT2_COLUMN = 14;
    private Color mColorBGClosed;
    private Color mColorBGHeader;
    private Color mColorBGSelected;
    private Color mColorBGTotal;
    private Color mColorDefault;
    private Color mColorEven;
    private Color mColorFGClosed;
    private Color mColorFGHeader;
    private Color mColorFGSelected;
    private Color mColorFGTotal;
    private Color mColorOdd;
    private Font mFontContent;
    private Font mFontContentClosed;
    private Font mFontContentHeader;
    private Font mFontContentSelected;
    private Font mFontContentTotal;

    /**
     * Constructor.
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    public ClosedPositionsFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        setCurSortColumn(POSITION_CLOSE_TIME_COLUMN);
        TradeDesk.getInst().getClosedPositions().subscribe(this, SignalType.ADD);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("ClosedPositions.font.content", label.getFont());
        mColorDefault = uiPrefs.getColor("ClosedPositions.foreground.default");
        mColorEven = uiPrefs.getColor("ClosedPositions.background.default.even");
        mColorOdd = uiPrefs.getColor("ClosedPositions.background.default.odd");
        mColorBGSelected = uiPrefs.getColor("ClosedPositions.background.selected");
        mColorFGSelected = uiPrefs.getColor("ClosedPositions.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("ClosedPositions.font.selected", label.getFont());
        mColorBGClosed = uiPrefs.getColor("ClosedPositions.background.closed");
        mColorFGClosed = uiPrefs.getColor("ClosedPositions.foreground.closed");
        mFontContentClosed = uiPrefs.getFont("ClosedPositions.font.closed", label.getFont());
        mColorBGTotal = uiPrefs.getColor("Summary.background.total");
        mColorFGTotal = uiPrefs.getColor("Summary.foreground.total");
        mFontContentTotal = uiPrefs.getFont("Summary.font.total", label.getFont());
        mColorBGHeader = uiPrefs.getColor("ClosedPositions.background.header");
        mColorFGHeader = uiPrefs.getColor("ClosedPositions.foreground.header");
        mFontContentHeader = uiPrefs.getFont("ClosedPositions.font.header", label.getFont());

        //creates renderer
        mCellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable aTable,
                                                           Object aValue,
                                                           boolean aIsSelected,
                                                           boolean aHasFocus,
                                                           int aRow,
                                                           int aColumn) {
                ClosedPositions closedPositions = TradeDesk.getInst().getClosedPositions();
                Object valueAt = aTable.getModel().getValueAt(aRow, ClosedPositionsFrame.TICKET_COLUMN);
                if (closedPositions == null || valueAt == null) {
                    return null;
                }
                Position position = closedPositions.getPosition(valueAt.toString());
                JLabel comp = (JLabel) super.getTableCellRendererComponent(aTable,
                                                                           aValue,
                                                                           aIsSelected,
                                                                           aHasFocus,
                                                                           aRow,
                                                                           aColumn);
                comp.setFont(mFontContent);
                //sets default colors of rows
                setForeground(mColorDefault);
                if (aRow % 2 == 0) {
                    setBackground(mColorEven);
                } else {
                    setBackground(mColorOdd);
                }
                if (aIsSelected) {
                    setBackground(mColorBGSelected);
                    setForeground(mColorFGSelected);
                    comp.setFont(mFontContentSelected);
                }
                if (position != null) {
                    //sets color of close position
                    if (position.isBeingClosed()) {
                        comp.setBackground(mColorBGClosed);
                        comp.setForeground(mColorFGClosed);
                        comp.setFont(mFontContentClosed);
                    }

                    //sets color of selected row
                    if (aTable.getSelectedRow() == aRow) {
                        setBackground(mColorBGSelected);
                        setForeground(mColorFGSelected);
                        comp.setFont(mFontContentSelected);
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
        URL iconUrl = getResourceManager().getResource("ID_OPENPOSITIONS_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }

        fireSorting();
    }

    /**
     * Returns columns types.
     *
     * @param aColumn number of the column
     */
    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Position aPosition = (Position) aObject;
        if (aColumn == ClosedPositionsFrame.TICKET_COLUMN) {
            return aPosition.getTicketID();
        } else if (aColumn == ClosedPositionsFrame.ACCOUNT_COLUMN) {
            return aPosition.getAccount();
        } else if (aColumn == ClosedPositionsFrame.CURRENCY_COLUMN) {
            return aPosition.getCurrency();
        } else if (aColumn == ClosedPositionsFrame.AMOUNT_COLUMN) {
            Rate rate = TradeDesk.getInst().getRates().getRate(aPosition.getCurrency());
            if (rate.getContractSize() / 1000 <= 0) {
                mFormat.setMinimumFractionDigits(3);
            } else {
                mFormat.setMinimumFractionDigits(0);
            }
            return String.valueOf(aPosition.getAmount() / 1000.0);
        } else if (aColumn == ClosedPositionsFrame.BUYORSELL_COLUMN) {
            return aPosition.getSide().getName();
        } else if (aColumn == ClosedPositionsFrame.OPEN_COLUMN) {
            return TradeDesk.formatPrice(aPosition.getCurrency(), aPosition.getOpenPrice());
        } else if (aColumn == ClosedPositionsFrame.CLOSE_COLUMN) {
            return TradeDesk.formatPrice(aPosition.getCurrency(), aPosition.getClosePrice());
        } else if (aColumn == ClosedPositionsFrame.GROSSPL_COLUMN) {
            return Util.format(aPosition.getGrossPnL());
        } else if (aColumn == ClosedPositionsFrame.NETPL_COLUMN) {
            return Util.format(aPosition.getGrossPnL() - aPosition.getCommission() + aPosition.getInterest());
        } else if (aColumn == ClosedPositionsFrame.CUSTOM_TEXT_COLUMN) {
            return aPosition.getCustomText();
        } else if (aColumn == ClosedPositionsFrame.CUSTOM_TEXT2_COLUMN) {
            return aPosition.getCustomText2();
        } else if (aColumn == ClosedPositionsFrame.POSITION_CLOSE_TIME_COLUMN) {
            return formatDate(aPosition.getCloseTime());
        } else if (aColumn == ClosedPositionsFrame.INTEREST_COLUMN) {
            return Util.format(aPosition.getInterest());
        } else if (aColumn == ClosedPositionsFrame.COMMISION_COLUMN) {
            return Util.format(aPosition.getCommission());
        } else if (aColumn == ClosedPositionsFrame.TIME_COLUMN) {
            if (aPosition.getOpenTime() != null) {
                return formatDate(aPosition.getOpenTime());
            } else {
                return null;
            }
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
            int positionSize = TradeDesk.getInst().getClosedPositions().size();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_CLOSED_POSITIONS_TITLE"));
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
        mLogger.debug("ClosedPositionsFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    @Override
    protected SignalVector getSignalVector() {
        return TradeDesk.getInst().getClosedPositions();
    }

    /**
     * Returns table's model.
     *
     * @param aResourceMan current resource manager
     */
    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new ClosedPostionsTableModel(aResourceMan);
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
        TradeDesk.getInst().getClosedPositions().unsubscribe(this, SignalType.ADD);
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("ClosedPositions.font.content", label.getFont());
        mColorDefault = uiPrefs.getColor("ClosedPositions.foreground.default");
        mColorEven = uiPrefs.getColor("ClosedPositions.background.default.even");
        mColorOdd = uiPrefs.getColor("ClosedPositions.background.default.odd");
        mColorBGSelected = uiPrefs.getColor("ClosedPositions.background.selected");
        mColorFGSelected = uiPrefs.getColor("ClosedPositions.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("ClosedPositions.font.selected", label.getFont());
        mColorBGClosed = uiPrefs.getColor("ClosedPositions.background.closed");
        mColorFGClosed = uiPrefs.getColor("ClosedPositions.foreground.closed");
        mFontContentClosed = uiPrefs.getFont("ClosedPositions.font.closed", label.getFont());
        mColorBGTotal = uiPrefs.getColor("Summary.background.total");
        mColorFGTotal = uiPrefs.getColor("Summary.foreground.total");
        mFontContentTotal = uiPrefs.getFont("Summary.font.total", label.getFont());
        mColorBGHeader = uiPrefs.getColor("ClosedPositions.background.header");
        mColorFGHeader = uiPrefs.getColor("ClosedPositions.foreground.header");
        mFontContentHeader = uiPrefs.getFont("ClosedPositions.font.header", label.getFont());
    }

    /**
     * Concrete implementation of AFrameTableModel.
     * This class responds for data filling of table.
     */
    private class ClosedPostionsTableModel extends AFrameTableModel {
        /**
         * Constructor.
         *
         * @param aResourceMan current resource manager
         */
        ClosedPostionsTableModel(ResourceManager aResourceMan) {
            super(aResourceMan, ClosedPositionsFrame.COLUMNS);
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
            if (idx == ClosedPositionsFrame.TICKET_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (idx == ClosedPositionsFrame.ACCOUNT_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == ClosedPositionsFrame.CURRENCY_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == ClosedPositionsFrame.AMOUNT_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (idx == ClosedPositionsFrame.BUYORSELL_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == ClosedPositionsFrame.OPEN_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == ClosedPositionsFrame.CLOSE_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == ClosedPositionsFrame.GROSSPL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == ClosedPositionsFrame.TIME_COLUMN) {
                return AFrameTableModel.DATE_COLUMN;
            } else if (idx == ClosedPositionsFrame.CUSTOM_TEXT_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == ClosedPositionsFrame.CUSTOM_TEXT2_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (idx == ClosedPositionsFrame.POSITION_CLOSE_TIME_COLUMN) {
                return AFrameTableModel.DATE_COLUMN;
            } else if (idx == ClosedPositionsFrame.INTEREST_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == ClosedPositionsFrame.COMMISION_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (idx == ClosedPositionsFrame.NETPL_COLUMN) {
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
                return TradeDesk.getInst().getClosedPositions().size() + 1;
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
            //gets position with number is equal of value "iRow"
            ClosedPositions closedPositions = TradeDesk.getInst().getClosedPositions();
            if (aRow == closedPositions.size()) {
                if (aCol == TICKET_COLUMN) {
                    return getResourceManager().getString("IDS_SUMMARY_TOTAL");
                } else if (aCol == AMOUNT_COLUMN) {
                    if (closedPositions.getTotalAmount() / 1000 <= 0) {
                        mFormat.setMinimumFractionDigits(3);
                    } else {
                        mFormat.setMinimumFractionDigits(0);
                    }
                    return String.valueOf(closedPositions.getTotalAmount() / 1000.0);
                } else if (aCol == GROSSPL_COLUMN) {
                    return Util.format(closedPositions.getTotalGrossPnL());
                } else if (aCol == NETPL_COLUMN) {
                    return Util.format(closedPositions.getTotalNetPnL());
                } else if (aCol == COMMISION_COLUMN) {
                    return Util.format(closedPositions.getTotalCommision());
                } else if (aCol == INTEREST_COLUMN) {
                    return Util.format(closedPositions.getTotalInterest());
                }
            } else {
                try {
                    Position position = (Position) closedPositions.get(aRow);
                    return getColumnValue(aCol, position);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
