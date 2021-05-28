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
 * 12/8/2004    Andre Mermegas  -- updated to be in units of 100K like classic TS not lots
 * 12/9/2004    Andre Mermegas  -- updated to show in sizes same as classic TS
 * 07/05/2006   Andre Mermegas: update to allow for totals row not be sorted, also to show values for avg buy/avg sell even if 0.0
 * 03/30/2007   Andre Mermeags: update frame title to use price formatter
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.IClickModel;
import fxts.stations.core.Summaries;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Summary;
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
 * This frame shows summary info for all open positions of the user (for all accounts).<br>
 * The name of the frame and table associated with the frame is Summary.<br>
 * The table shows contents of the Summaries table.<br>
 * Content pane of the frame consists of the JTable with following  columns.<br>
 * <br>
 * Creation date (9/20/2003 6:38 PM)
 */
public class SummaryFrame<E> extends ATableFrame<E> {
    public static final String NAME = "Summary";
    /**
     * Array of resource pairs with names of columns.
     */
    private static final String[] COLUMNS = {
            "IDS_SUMMARY_CURRENCY",
            "IDS_SUMMARY_PNL_SELL",
            "IDS_SUMMARY_AMOUNT_SELL",
            "IDS_SUMMARY_AVG_SELL",
            "IDS_SUMMARY_PNL_BUY",
            "IDS_SUMMARY_AMOUNT_BUY",
            "IDS_SUMMARY_AVG_BUY",
            "IDS_POSITION_SIDE_SELL",
            "IDS_POSITION_SIDE_BUY",
            "IDS_SUMMARY_POSITIONS",
            "IDS_SUMMARY_AMOUNT",
            "IDS_POSITION_GROSSPL",
            "IDS_POSITION_NETPL"};
    /**
     * Identifiers of columns.
     */
    private static final int CURRENCY_COLUMN = 0;
    private static final int PNL_SELL_COLUMN = 1;
    private static final int AMOUNT_SELL_COLUMN = 2;
    private static final int AVG_SELL_COLUMN = 3;
    private static final int PNL_BUY_COLUMN = 4;
    private static final int AMOUNT_BUY_COLUMN = 5;
    private static final int AVG_BUY_COLUMN = 6;
    private static final int SELL_RATE_COLUMN = 7;
    private static final int BUY_RATE_COLUMN = 8;
    private static final int POSITIONS_COLUMN = 9;
    private static final int AMOUNT_COLUMN = 10;
    private static final int GROSS_PNL_COLUMN = 11;
    private static final int NET_PNL_COLUMN = 12;
    private Color mColorBGEven;
    private Color mColorBGHeader;
    private Color mColorBGOdd;
    private Color mColorBGSelected;
    private Color mColorBGTotal;
    private Color mColorFG;
    private Color mColorFGHeader;
    private Color mColorFGSelected;
    private Color mColorFGTotal;
    private Font mFontContent;
    private Font mFontContentHeader;
    private Font mFontContentSelected;
    private Font mFontContentTotal;

    /**
     * Constructor.
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    public SummaryFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);

        TradeDesk.getInst().getSummaries().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getSummaries().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getSummaries().subscribe(this, SignalType.REMOVE);

        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("Summary.font.content", label.getFont());
        mColorBGSelected = uiPrefs.getColor("Summary.background.selected");
        mColorFGSelected = uiPrefs.getColor("Summary.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Summary.font.selected", label.getFont());
        mColorFG = uiPrefs.getColor("Summary.foreground.default");
        mColorBGEven = uiPrefs.getColor("Summary.background.default.even");
        mColorBGOdd = uiPrefs.getColor("Summary.background.default.odd");
        mColorBGTotal = uiPrefs.getColor("Summary.background.total");
        mColorFGTotal = uiPrefs.getColor("Summary.foreground.total");
        mFontContentTotal = uiPrefs.getFont("Summary.font.total", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Summary.background.header");
        mColorFGHeader = uiPrefs.getColor("Summary.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Summary.font.header", label.getFont());

        mCellRenderer = new DefaultTableCellRenderer() {
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
                Summaries summaries = TradeDesk.getInst().getSummaries();
                Object valueAt = aTable.getModel().getValueAt(aRow, CURRENCY_COLUMN);
                if (summaries == null || valueAt == null) {
                    return null;
                }

                comp.setFont(mFontContent);
                Summary summary = summaries.getSummary(valueAt.toString());
                if (summary != null) {
                    //sets default colors of rows
                    setForeground(mColorFG);
                    if (aRow % 2 == 0) {
                        setBackground(mColorBGEven);
                    } else {
                        setBackground(mColorBGOdd);
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
                //sets opaque mode
                setOpaque(true);
                //sets alignment at cell
                if (aColumn == 0) {
                    comp.setHorizontalAlignment(SwingConstants.LEFT);
                } else {
                    comp.setHorizontalAlignment(SwingConstants.RIGHT);
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

        //sets render to all columns
        setRenderers(mCellRenderer, mHeaderRenderer);

        //sets icon to internal frame
        URL iconUrl = getResourceManager().getResource("ID_SUMMARY_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }
    }

    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Summary aSummary = (Summary) aObject;
        if (aColumn == CURRENCY_COLUMN) {
            return aSummary.getCurrency();
        } else if (aColumn == PNL_SELL_COLUMN) {
            if (aSummary.getAmountSell() == 0) {
                return "";
            } else {
                return Util.format(aSummary.getSellPnL());
            }
        } else if (aColumn == AMOUNT_SELL_COLUMN) {
            if (aSummary.getAmountSell() == 0) {
                return "";
            } else {
                Rate rate = TradeDesk.getInst().getRates().getRate(aSummary.getCurrency());
                if (rate.getContractSize() / 1000 <= 0) {
                    mFormat.setMinimumFractionDigits(3);
                } else {
                    mFormat.setMinimumFractionDigits(0);
                }
                return mFormat.format(aSummary.getAmountSell() / 1000.0);
            }
        } else if (aColumn == AVG_SELL_COLUMN) {
            if (aSummary.getAvgSellRate() >= TradeDesk.getFractionalPipsPrice(aSummary.getCurrency()) / 2) {
                return TradeDesk.formatPrice(aSummary.getCurrency(), aSummary.getAvgSellRate());
            } else {
                return "";
            }
        } else if (aColumn == PNL_BUY_COLUMN) {
            if (aSummary.getAmountBuy() == 0) {
                return "";
            } else {
                return Util.format(aSummary.getBuyPnL());
            }
        } else if (aColumn == AMOUNT_BUY_COLUMN) {
            if (aSummary.getAmountBuy() == 0) {
                return "";
            } else {
                Rate rate = TradeDesk.getInst().getRates().getRate(aSummary.getCurrency());
                if (rate.getContractSize() / 1000 <= 0) {
                    mFormat.setMinimumFractionDigits(3);
                } else {
                    mFormat.setMinimumFractionDigits(0);
                }
                return mFormat.format(aSummary.getAmountBuy() / 1000.0);
            }
        } else if (aColumn == AVG_BUY_COLUMN) {
            if (aSummary.getAvgBuyRate() >= TradeDesk.getFractionalPipsPrice(aSummary.getCurrency()) / 2) {
                return TradeDesk.formatPrice(aSummary.getCurrency(), aSummary.getAvgBuyRate());
            } else {
                return "";
            }
        } else if (aColumn == POSITIONS_COLUMN) {
            return String.valueOf(aSummary.getPositionsCount());
        } else if (aColumn == AMOUNT_COLUMN) {
            double sell = aSummary.getAmountSell() / 1000.0;
            double buy = aSummary.getAmountBuy() / 1000.0;
            if (sell == 0 && buy == 0) {
                return "";
            } else {
                Rate rate = TradeDesk.getInst().getRates().getRate(aSummary.getCurrency());
                if (rate.getContractSize() / 1000 <= 0) {
                    mFormat.setMinimumFractionDigits(3);
                } else {
                    mFormat.setMinimumFractionDigits(0);
                }
                return mFormat.format(buy - sell);
            }
        } else if (aColumn == GROSS_PNL_COLUMN) {
            return Util.format(aSummary.getGrossTotalPnL());
        } else if (aColumn == NET_PNL_COLUMN) {
            return Util.format(aSummary.getNetTotalPnL());
        } else if (aColumn == SELL_RATE_COLUMN) {
            if (aSummary.getAmountBuy() == 0) {
                return "";
            } else {
                double sell = TradeDesk.getInst().getRates().getRate(aSummary.getCurrency()).getSellPrice();
                return TradeDesk.formatPrice(aSummary.getCurrency(), sell);
            }
        } else if (aColumn == BUY_RATE_COLUMN) {
            if (aSummary.getAmountSell() == 0) {
                return "";
            } else {
                double buy = TradeDesk.getInst().getRates().getRate(aSummary.getCurrency()).getBuyPrice();
                return TradeDesk.formatPrice(aSummary.getCurrency(), buy);
            }
        } else {
            return "";
        }
    }

    /**
     * Gets Localized Title of Frame.
     * An implementation of abstract method of fxts.stations.trader.ui.frames.ATableFrame class
     *
     * @param aResourceMan Current Resource manager that is used for localistion
     *
     * @return localized Title
     */
    @Override
    protected String getLocalizedTitle(ResourceManager aResourceMan) {
        if (aResourceMan != null) {
            UserPreferences pref = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            String mode = pref.getString(IClickModel.TRADING_MODE);
            Summaries summaries = TradeDesk.getInst().getSummaries();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_SUMMARYS_TITLE"));
            if (!summaries.isEmpty()) {
                titleBuffer.append(" (").append(Util.format(summaries.getNetTotalPnL())).append(")");
            }
            if (IClickModel.SINGLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
            } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
            }
            return titleBuffer.toString();
        }
        mLogger.debug("Error: SummaryFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    @Override
    protected SignalVector getSignalVector() {
        return TradeDesk.getInst().getSummaries();
    }

    /**
     * Returns table's model.
     *
     * @param aResourceMan Current Resource manager that is used for localistion
     *
     * @return table model
     */
    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new SummariesTableModel(aResourceMan);
        }
        return mTableModel;
    }

    /**
     * Gets name of table .
     * An implementation of abstract method of fxts.stations.trader.ui.frames.ATableFrame class
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
     * Invoked when user attempts to close window
     */
    @Override
    protected void onCloseFrame(InternalFrameEvent aEvent) {
        super.onCloseFrame(aEvent);
        TradeDesk.getInst().getSummaries().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getSummaries().unsubscribe(this, SignalType.REMOVE);
        TradeDesk.getInst().getSummaries().unsubscribe(this, SignalType.CHANGE);
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mFontContent = uiPrefs.getFont("Summary.font.content", label.getFont());
        mColorBGSelected = uiPrefs.getColor("Summary.background.selected");
        mColorFGSelected = uiPrefs.getColor("Summary.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Summary.font.selected", label.getFont());
        mColorFG = uiPrefs.getColor("Summary.foreground.default");
        mColorBGEven = uiPrefs.getColor("Summary.background.default.even");
        mColorBGOdd = uiPrefs.getColor("Summary.background.default.odd");
        mColorBGTotal = uiPrefs.getColor("Summary.background.total");
        mColorFGTotal = uiPrefs.getColor("Summary.foreground.total");
        mFontContentTotal = uiPrefs.getFont("Summary.font.total", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Summary.background.header");
        mColorFGHeader = uiPrefs.getColor("Summary.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Summary.font.header", label.getFont());
    }

    /**
     * Concrete implementation of AFrameTableModel.
     * This class responds for data filling of table.
     */
    private class SummariesTableModel extends AFrameTableModel {
        /**
         * Constructor.
         *
         * @param aResourceMan current resource manager
         */
        SummariesTableModel(ResourceManager aResourceMan) {
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
            int i = column.getModelIndex();
            if (i == CURRENCY_COLUMN) {
                return AFrameTableModel.STRING_COLUMN;
            } else if (i == PNL_SELL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == AMOUNT_SELL_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (i == AVG_SELL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == PNL_BUY_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == AMOUNT_BUY_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (i == AVG_BUY_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == POSITIONS_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (i == AMOUNT_COLUMN) {
                return AFrameTableModel.INT_COLUMN;
            } else if (i == GROSS_PNL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == NET_PNL_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == SELL_RATE_COLUMN) {
                return AFrameTableModel.DOUBLE_COLUMN;
            } else if (i == BUY_RATE_COLUMN) {
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
                return TradeDesk.getInst().getSummaries().size() + 1;
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
                Summaries summaries = tradeDesk.getSummaries();
                if (aRow == summaries.size()) {
                    if (summaries.getTotalAmount() / 1000 <= 0) {
                        mFormat.setMinimumFractionDigits(3);
                    } else {
                        mFormat.setMinimumFractionDigits(0);
                    }
                    if (aCol == CURRENCY_COLUMN) {
                        return getResourceManager().getString("IDS_SUMMARY_TOTAL");
                    } else if (aCol == PNL_SELL_COLUMN) {
                        if (summaries.getTotalAmountSell() == 0) {
                            return "";
                        } else {
                            return Util.format(summaries.getTotalSellPnL());
                        }
                    } else if (aCol == AMOUNT_SELL_COLUMN) {
                        if (summaries.getTotalAmountSell() == 0) {
                            return "";
                        } else {
                            return mFormat.format(summaries.getTotalAmountSell() / 1000.0);
                        }
                    } else if (aCol == AVG_SELL_COLUMN) {
                        return "";
                    } else if (aCol == PNL_BUY_COLUMN) {
                        if (summaries.getTotalAmountBuy() == 0) {
                            return "";
                        } else {
                            return Util.format(summaries.getTotalBuyPnL());
                        }
                    } else if (aCol == AMOUNT_BUY_COLUMN) {
                        if (summaries.getTotalAmountBuy() == 0) {
                            return "";
                        } else {
                            return mFormat.format(summaries.getTotalAmountBuy() / 1000.0);
                        }
                    } else if (aCol == AVG_BUY_COLUMN) {
                        return "";
                    } else if (aCol == POSITIONS_COLUMN) {
                        return String.valueOf(summaries.getTotalPositionCount());
                    } else if (aCol == AMOUNT_COLUMN) {
                        double sell = summaries.getTotalAmountSell() / 1000.0;
                        double buy = summaries.getTotalAmountBuy() / 1000.0;
                        if (sell == 0 && buy == 0) {
                            return "";
                        } else {
                            return mFormat.format(buy - sell);
                        }
                    } else if (aCol == GROSS_PNL_COLUMN) {
                        return Util.format(summaries.getGrossTotalPnL());
                    } else if (aCol == SELL_RATE_COLUMN) {
                        return "";
                    } else if (aCol == BUY_RATE_COLUMN) {
                        return "";
                    } else if (aCol == NET_PNL_COLUMN) {
                        return Util.format(summaries.getNetTotalPnL());
                    }
                } else {
                    Summary summary = (Summary) summaries.get(aRow);
                    return getColumnValue(aCol, summary);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
