/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/CloseMultiplePositionsDialog.java#1 $
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
 * Created: Aug 2, 2007 2:43:01 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.EachRowEditor;
import fxts.stations.ui.AAJTable;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;
import fxts.stations.util.signals.RemoveSignal;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultFormatter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloseMultiplePositionsDialog extends AmountDialog implements ISignalListener  {
    private Map<String, CloseAmountsComboBox> mAmountMap;
    private boolean mAtBest = true;
    private JSpinner mAtMarketSpinner;
    private JTextField mCustomTextTextField;
    private int mExitCode;
    private EachRowEditor mRowEditor;
    private List<String> mTickets;
    private DefaultTableModel mModel;

    public CloseMultiplePositionsDialog(Frame aParent, List<String> aTickets) {
        super(aParent);
        mTickets = new ArrayList<String>(aTickets);
        mAmountMap = new HashMap<String, CloseAmountsComboBox>();
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }

            //Creates main panel
            initComponents();
            pack();
            mCustomTextTextField.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closeDialog(int aExitCode) {
        super.closeDialog(aExitCode);
        mExitCode = aExitCode;
    }

    public Map<String, CloseAmountsComboBox> getAmountMap() {
        return mAmountMap;
    }

    /**
     * @return at market value
     */
    public int getAtMarket() {
        return Integer.parseInt(mAtMarketSpinner.getValue().toString());
    }

    public String getCustomText() {
        return mCustomTextTextField.getText();
    }

    public List<String> getTickets() {
        return Collections.unmodifiableList(mTickets);
    }

    @Override
    protected void initComponents() {
        getContentPane().setLayout(new RiverLayout());
        UIManager uim = UIManager.getInst();
        JLabel customTextLabel = uim.createLabel(getResMan().getString("IDS_CUSTOM_TEXT"));
        mModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int aRow, int aColumn) {
                return aColumn != 0;
            }
        };
        JTable table = new AAJTable(mModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
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
            @Override
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

            @Override
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        mModel.setColumnIdentifiers(new Object[]{
                getResMan().getString("IDS_CLOSEPOSITION_DIALOG_TICKET"),
                getResMan().getString("IDS_CLOSEPOSITION_DIALOG_AMOUNT")});
        mRowEditor = new EachRowEditor(table);
        table.getColumn(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_AMOUNT")).setCellEditor(mRowEditor);
        table.getColumnModel().getColumn(0).setHeaderRenderer(headerRenderer);
        table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
        table.getColumnModel().getColumn(1).setHeaderRenderer(headerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
        for (int i = 0; i < mTickets.size(); i++) {
            String ticketid = mTickets.get(i);
            CloseAmountsComboBox amountComboBox = new CloseAmountsComboBox();
            mAmountMap.put(ticketid, amountComboBox);
            amountComboBox.init(new DefaultActorImpl());
            amountComboBox.setDialog(this);
            amountComboBox.setEditable(true);
            TradeDesk desk = TradeDesk.getInst();
            Position position = desk.getOpenPositions().getPosition(ticketid);
            long amount = position.getAmount();
            amountComboBox.setMaximumValue(amount);
            Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
            if (rate == null || rate.isForex()) {
                Account acct = (Account) TradeDesk.getInst().getAccounts().get(position.getAccount());
                amountComboBox.setContractSize((long) acct.getBaseUnitSize());
            } else {
                amountComboBox.setContractSize(rate.getContractSize());
            }
            int index = amountComboBox.getComboBoxModel().getSize() - 1;
            amountComboBox.setSelectedIndex(index);
            mRowEditor.setEditorAt(i, new DefaultCellEditor(amountComboBox));
            mModel.addRow(new Object[]{ticketid, amountComboBox.getSelectedAmountString()});
        }
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add("br hfill vfill", scrollPane);
        JButton okButton = uim.createButton(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_OK"));
        JButton cancelButton = uim.createButton(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_CANCEL"));
        mCustomTextTextField = uim.createTextField();
        mAtMarketSpinner = uim.createSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        mAtMarketSpinner.setEnabled(false);
        JFormattedTextField text = ((JSpinner.DefaultEditor) mAtMarketSpinner.getEditor()).getTextField();
        ((DefaultFormatter) text.getFormatter()).setAllowsInvalid(false);
        text.setHorizontalAlignment(JTextField.LEFT);
        mAtMarketSpinner.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent aMouseWheelEvent) {
                if (mAtMarketSpinner.isEnabled()) {
                    if (aMouseWheelEvent.getWheelRotation() <= 0) {
                        mAtMarketSpinner.setValue(mAtMarketSpinner.getNextValue());
                    } else {
                        mAtMarketSpinner.setValue(mAtMarketSpinner.getPreviousValue());
                    }
                }
            }
        });
        setModal(true);
        setTitle(getResMan().getString("IDS_CLOSEPOSITION_DIALOG_TITLE"));
        setBackground(Color.WHITE);

        //sets for exiting by escape
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                        "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        getRootPane().setDefaultButton(okButton);
        // Set-select of user is a YES-analogy
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (verify()) {
                    closeDialog(JOptionPane.YES_OPTION);
                }
            }
        });
        // Cancel-select of user is already Cancel
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        String[] options = new String[]{mResMan.getString("IDS_AT_BEST"), mResMan.getString("IDS_AT_MARKET")};
        JComboBox orderTypeBox = new JComboBox(options);
        orderTypeBox.setFocusable(true);
        orderTypeBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                JComboBox cb = (JComboBox) aEvent.getSource();
                if (cb.getSelectedIndex() == 0) {
                    mAtMarketSpinner.setEnabled(false);
                    mAtBest = true;
                } else {
                    mAtMarketSpinner.setEnabled(true);
                    mAtBest = false;
                }
            }
        });
        getContentPane().add("br left", customTextLabel);
        getContentPane().add("tab hfill", mCustomTextTextField);
        getContentPane().add("br left", orderTypeBox);
        getContentPane().add("tab hfill", mAtMarketSpinner);
        getContentPane().add("br center", okButton);
        getContentPane().add("center", cancelButton);
        Util.setAllToBiggest(new JComponent[]{okButton, cancelButton});
    }

    public boolean isAtBest() {
        return mAtBest;
    }

    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.REMOVE);
        setVisible(true);
        mRowEditor.cleanup();
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.REMOVE);
        return mExitCode;
    }

    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal.getType() == SignalType.REMOVE) {
            try {
                RemoveSignal signal = (RemoveSignal) aSignal;
                Position element = (Position) signal.getElement();
                mTickets.remove(element.getTicketID());
                for (int i = 0; i < mModel.getRowCount(); i++) {
                    Object at = mModel.getValueAt(i, 0);
                    if (element.getTicketID().equals(at)) {
                        mModel.removeRow(i);
                    }
                }
                if (mModel.getRowCount() == 0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            closeDialog(JOptionPane.CANCEL_OPTION);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean verify() {
        for (String ticketID : mTickets) {
            Position position = TradeDesk.getInst().getOpenPositions().getPosition(ticketID);
            Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
            Side side = position.getSide();
            if (side == Side.BUY && !rate.isSellTradable() || side == Side.SELL && !rate.isBuyTradable()) {
                JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                              "There is no tradable price. (You cannot trade at this price)",
                                              mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                              JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                if (verifyAmount(mAmountMap.get(ticketID),
                                 TradeDesk.getInst().getOpenPositions().getPosition(ticketID).getAmount(),
                                 "IDS_CLOSEPOSITION_DIALOG_MAX_ERROR_MESSAGE") < 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
