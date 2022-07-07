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
 */
package fxts.stations.trader.ui.dialogs;

import com.fxcm.messaging.TradingSessionDesc;
import fxts.stations.core.TradeDesk;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.AAJTable;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

/**
 * @author Andre Mermegas
 *         Date: Jun 16, 2006
 *         Time: 10:01:31 AM
 */
public class TradingSessionDialog extends ABaseDialog {
    private ResourceManager mResourceManager;
    private int mSelectedRow;

    /**
     * Constructor.
     *
     * @param aOwner frame owner
     * @param aTradingSessionDesc trading session description
     */
    public TradingSessionDialog(Frame aOwner, TradingSessionDesc[] aTradingSessionDesc) {
        super(aOwner);
        try {
            mResourceManager = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setModal(true);
        setTitle(mResourceManager.getString("IDS_CHOOSE_TRADING_SESSION"));
        initComponents(aTradingSessionDesc);
        pack();
    }

    private void initComponents(TradingSessionDesc[] aTradingSessionDesc) {
        String name = mResourceManager.getString("IDS_NAME");
        String id = mResourceManager.getString("IDS_ID");
        String description = mResourceManager.getString("IDS_DESCRIPTION");

        DefaultTableModel dataModel =
                new DefaultTableModel(new Object[][]{}, new String[]{name, id, description}) {
                    public boolean isCellEditable(int aRow, int aColumn) {
                        return false;
                    }
                };

        for (int i = 0; i < aTradingSessionDesc.length; i++) {
            TradingSessionDesc tradingSessionDesc = aTradingSessionDesc[i];
            dataModel.insertRow(i,
                                new Object[]{
                                        tradingSessionDesc.getID(),
                                        tradingSessionDesc.getSubID(),
                                        tradingSessionDesc.getDesc()});
        }

        final JTable table = new AAJTable(dataModel);
        table.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent aEvent) {
                if (aEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    mSelectedRow = table.getSelectedRow();
                    closeDialog(JOptionPane.OK_OPTION);
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent aEvent) {
                if (aEvent.getClickCount() == 2) {
                    mSelectedRow = table.getSelectedRow();
                    closeDialog(JOptionPane.OK_OPTION);
                }
            }
        });
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            //Overriding of DefaultTableCellRenderer`s method
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
                //gets user preferences
                UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
                if (uiPrefs == null) {
                    LOG.debug("UserPreferencies not initialized!");
                    return null;
                }
                setBackground(uiPrefs.getColor("Table.background.header"));
                setForeground(uiPrefs.getColor("Table.foreground.header"));
                comp.setFont(uiPrefs.getFont("Table.font.header", comp.getFont()));
                //sets opaque mode
                setOpaque(true);
                //sets border
                comp.setBorder(BorderFactory.createEtchedBorder());
                comp.setHorizontalAlignment(SwingConstants.CENTER);
                return comp;
            }
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            //Overriding of DefaultTableCellRenderer`s method
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
                //gets user preferences
                UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
                if (uiPrefs == null) {
                    LOG.debug("UserPreferencies not initialized!");
                    return null;
                }
                comp.setFont(uiPrefs.getFont("Table.font.content", comp.getFont()));
                //sets default colors of rows
                setForeground(uiPrefs.getColor("Table.foreground.default"));
                if (aRow % 2 == 0) {
                    setBackground(uiPrefs.getColor("Table.background.default.even"));
                } else {
                    setBackground(uiPrefs.getColor("Table.background.default.odd"));
                }
                //sets color of selected row
                if (aTable.getSelectedRow() == aRow) {
                    setBackground(uiPrefs.getColor("Table.background.selected"));
                    setForeground(uiPrefs.getColor("Table.foreground.selected"));
                    comp.setFont(uiPrefs.getFont("Table.font.selected", comp.getFont()));
                }
                //sets opaque mode
                setOpaque(true);
                //sets alignment at cell
                comp.setHorizontalAlignment(SwingConstants.LEFT);
                return comp;
            }
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        setRenderers(table, cellRenderer, headerRenderer);
        JScrollPane scrollPane = new JScrollPane(table);
        String title = mResourceManager.getString("IDS_AVAILABLE_TRADING_SESSIONS");
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredSize().width + 50, 100));
        table.setRowSelectionInterval(0, 0);

        JButton okButton = UIManager.getInst().createButton(mResourceManager.getString("IDS_CLOSEPOSITION_DIALOG_OK"));
        InputMap inputMap = okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ExitAction");
        okButton.getActionMap().put("ExitAction", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                mSelectedRow = table.getSelectedRow();
                closeDialog(JOptionPane.OK_OPTION);
            }
        });
        okButton.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        mSelectedRow = table.getSelectedRow();
                        closeDialog(JOptionPane.OK_OPTION);
                    }
                });

        UIManager.getInst().packColumns(table, 2);
        getRootPane().setDefaultButton(okButton);

        getContentPane().setLayout(new RiverLayout());
        getContentPane().add("vfill hfill", scrollPane);
        getContentPane().add("br center", okButton);
    }

    /**
     * Sets renderers to all columns.
     */
    protected void setRenderers(JTable aTable,
                                DefaultTableCellRenderer aCellRender,
                                DefaultTableCellRenderer aHeaderRender) {
        TableColumnModel columnModel = aTable.getColumnModel();
        for (Enumeration enumeration = columnModel.getColumns(); enumeration.hasMoreElements();) {
            TableColumn column = (TableColumn) enumeration.nextElement();
            if (aCellRender != null) {
                column.setCellRenderer(aCellRender);
            }
            if (aHeaderRender != null) {
                column.setHeaderRenderer(aHeaderRender);
            }
        }
    }

    public int showModal() {
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return mSelectedRow;
    }
}
