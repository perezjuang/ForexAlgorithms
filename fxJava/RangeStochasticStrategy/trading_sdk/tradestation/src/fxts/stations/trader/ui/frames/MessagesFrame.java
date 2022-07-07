/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/MessagesFrame.java#1 $
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
 * Author: Andre Mermegas
 * Created: Jul 6, 2007 11:52:43 AM
 *
 * $History: $
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.core.IClickModel;
import fxts.stations.core.Messages;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Message;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.trader.ui.dialogs.MessageDialog;
import fxts.stations.ui.UIManager;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.UserPreferences;

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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Vector;

/**
 */
public class MessagesFrame<E> extends ATableFrame<E> {
    public static final String NAME = "Messages";
    private static final int TIME_COLUMN = 0;
    private static final int FROM_COLUMN = 1;
    private static final int TEXT_COLUMN = 2;
    private static final String[] COLUMNS = {"IDS_MESSAGES_TIME", "IDS_MESSAGES_FROM", "IDS_MESSAGES_TEXT"};
    private Color mColorBGHeader;
    private Color mColorBGSelected;
    private Color mColorEven;
    private Color mColorFGDefault;
    private Color mColorFGHeader;
    private Color mColorFGSelected;
    private Color mColorOdd;
    private Font mFontContent;
    private Font mFontContentHeader;
    private Font mFontContentSelected;

    public MessagesFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(aMan, aMainFrame);
        setCurSortColumn(TIME_COLUMN);

        TradeDesk.getInst().getMessages().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getMessages().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getMessages().subscribe(this, SignalType.REMOVE);

        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mColorFGDefault = uiPrefs.getColor("Messages.foreground.default");
        mColorEven = uiPrefs.getColor("Messages.background.default.even");
        mColorOdd = uiPrefs.getColor("Messages.background.default.odd");
        mColorBGSelected = uiPrefs.getColor("Messages.background.selected");
        mColorFGSelected = uiPrefs.getColor("Messages.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Messages.font.selected", label.getFont());
        mFontContent = uiPrefs.getFont("Messages.font.content", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Messages.background.header");
        mColorFGHeader = uiPrefs.getColor("Messages.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Messages.font.header", label.getFont());

        getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aEvent) {
                doSelect(aEvent);
                if (aEvent.getClickCount() == 2) {
                    JTable table = (JTable) aEvent.getComponent();
                    int row = table.rowAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                    String key = (String) table.getModel().getValueAt(row, TIME_COLUMN);
                    Message m = (Message) TradeDesk.getInst().getMessages().get(key);
                    if (m != null) {
                        MessageDialog md = new MessageDialog(TradeApp.getInst().getMainFrame());
                        md.setMessage(m);
                        md.showModal();
                    }
                }
            }

            private void doSelect(MouseEvent aEvent) {
                JTable table = (JTable) aEvent.getComponent();
                int currentRow = table.rowAtPoint(new Point(aEvent.getX(), aEvent.getY()));
                if (!isSelected()) {
                    try {
                        setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                    if (currentRow != table.getSelectedRow()) {
                        table.getSelectionModel().setSelectionInterval(currentRow, currentRow);
                    }
                }
            }
        });

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
                comp.setFont(mFontContent);
                Message msg = (Message) TradeDesk.getInst().getMessages().get(aRow);
                if (msg != null) {
                    //sets default colors of rows
                    setForeground(mColorFGDefault);
                    if (aRow % 2 == 0) {
                        setBackground(mColorEven);
                    } else {
                        setBackground(mColorOdd);
                    }
                    //sets color of selected row
                    if (aTable.getSelectedRow() == aRow) {
                        setBackground(mColorBGSelected);
                        setForeground(mColorFGSelected);
                        comp.setFont(mFontContentSelected);
                    }
                }
                //sets opaque mode
                setOpaque(true);
                //sets alignment at cell
                comp.setHorizontalAlignment(SwingConstants.LEFT);
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

        setRenderers(mCellRenderer, mHeaderRenderer);

        //sets icon to internal frame
        URL iconUrl = getResourceManager().getResource("ID_MESSAGES_FRAME_ICON");
        if (iconUrl != null) {
            ImageIcon icon = new ImageIcon(iconUrl);
            setFrameIcon(icon);
        }

        fireSorting();
    }

    @Override
    protected Object getColumnValue(int aColumn, Object aObject) {
        Message aMessage = (Message) aObject;
        if (aColumn == TIME_COLUMN) {
            return formatDate(aMessage.getDate());
        } else if (aColumn == FROM_COLUMN) {
            return aMessage.getFrom();
        } else if (aColumn == TEXT_COLUMN) {
            return aMessage.getText();
        } else {
            return "";
        }
    }

    @Override
    protected String getLocalizedTitle(ResourceManager aResourceMan) {
        if (aResourceMan != null) {
            UserPreferences pref = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            String mode = pref.getString(IClickModel.TRADING_MODE);
            int messageSize = TradeDesk.getInst().getMessages().size();
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_MESSAGES_TITLE"));
            if (messageSize > 0) {
                titleBuffer.append(" (").append(messageSize).append(")");
            }
            if (IClickModel.SINGLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
            } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
                titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
            }
            return titleBuffer.toString();
        }
        mLogger.debug("Error: MessagesFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    @Override
    protected SignalVector getSignalVector() {
        return TradeDesk.getInst().getMessages();
    }

    @Override
    public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
        if (mTableModel == null) {
            mTableModel = new MessagesModel(aResourceMan);
        }
        return mTableModel;
    }

    @Override
    protected String getTableName() {
        return NAME;
    }

    @Override
    public boolean isSortable() {
        return true;
    }

    @Override
    protected void onCloseFrame(InternalFrameEvent aEvent) {
        super.onCloseFrame(aEvent);
        TradeDesk.getInst().getMessages().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getMessages().unsubscribe(this, SignalType.REMOVE);
        TradeDesk.getInst().getMessages().unsubscribe(this, SignalType.CHANGE);
    }

    @Override
    public void preferencesUpdated(Vector aChangings) {
        super.preferencesUpdated(aChangings);
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        JLabel label = new JLabel();
        mColorFGDefault = uiPrefs.getColor("Messages.foreground.default");
        mColorEven = uiPrefs.getColor("Messages.background.default.even");
        mColorOdd = uiPrefs.getColor("Messages.background.default.odd");
        mColorBGSelected = uiPrefs.getColor("Messages.background.selected");
        mColorFGSelected = uiPrefs.getColor("Messages.foreground.selected");
        mFontContentSelected = uiPrefs.getFont("Messages.font.selected", label.getFont());
        mFontContent = uiPrefs.getFont("Messages.font.content", label.getFont());
        mColorBGHeader = uiPrefs.getColor("Messages.background.header");
        mColorFGHeader = uiPrefs.getColor("Messages.foreground.header");
        mFontContentHeader = uiPrefs.getFont("Messages.font.header", label.getFont());
    }

    private class MessagesModel extends AFrameTableModel {
        private MessagesModel(ResourceManager aMan) {
            super(aMan, COLUMNS);
        }

        @Override
        public int getColumnType(int aCol) {
            TableColumn column = getTable().getColumnModel().getColumn(aCol);
            switch (column.getModelIndex()) {
                case TIME_COLUMN:
                    return AFrameTableModel.DATE_COLUMN;
                case FROM_COLUMN:
                    return AFrameTableModel.STRING_COLUMN;
                case TEXT_COLUMN:
                    return AFrameTableModel.STRING_COLUMN;
            }
            return 0;
        }

        public int getRowCount() {
            return TradeDesk.getInst().getMessages().size();
        }

        public Object getValueAt(int aRow, int aCol) {
            try {
                Messages messages = TradeDesk.getInst().getMessages();
                if (aRow < messages.size()) {
                    Message m = (Message) messages.get(aRow);
                    return getColumnValue(aCol, m);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
