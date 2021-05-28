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
 * 05/22/2006   Andre Mermegas: fix to use JSpinner instead of hacked scrollbars
 * 10/04/2006   Andre Mermegas: scrollwheel support in spinner
 * 10/10/2006   Andre Mermegas: added mousewheel support to trailstop spinner
 * 05/11/2007   Andre Mermegas: spinner align left, trail stop min 10
 *
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.trader.ui.RateSpinner;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.Util;
import fxts.stations.util.signals.ChangeSignal;
import fxts.stations.util.signals.RemoveSignal;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;

/**
 * SetStopLimitDialog allows to client set (or remove made earlier) limits<br>
 * on profit or lose in selected open position.<br>
 * <ul>
 * <li> Herewith he can choose any one of its positions, set or stop, and rate for its.</li>
 * <li> When updated is checked admissibility to this operations.</li>
 * </ul>
 * <br>
 * Creation date (9/24/2003 1:36 PM)
 */
public class SetStopLimitOrderDialog extends ABaseDialog implements ISignalListener {
    public static final int STOP = 0;
    public static final int LIMIT = 1;

    private DefaultActor mDefaultActor;
    private int mExitCode;
    private JLabel mGreatEqualLabel;
    private JTextField mLimitTextField;
    private String mOrderID;
    private OrdersComboBox mOrdersComboBox;
    private double mRate;
    private RateSpinner mRateSpinner;
    private JButton mRemoveButton;
    private ResourceManager mResMan;
    private JButton mSetButton;
    private int mStopLimit;
    private KeySensitiveComboBox mStopLimitComboBox;
    private TradeDesk mTradeDesk;
    private JCheckBox mTrailingStopCheckBox;
    private JSpinner mTrailStopSpinner;

    /**
     * Constructor SetStopLimitDialog.
     *
     * @param aParent parent frame
     */
    public SetStopLimitOrderDialog(Frame aParent) {
        super(aParent);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            mTradeDesk = TradeDesk.getInst();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
            //Creates main panel
            initComponents();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks mTicketComboBox to allow the default action will be enable.
     *
     * @return false if selected item is disabled or absence
     */
    protected boolean checkAllControls() {
        Item item = (Item) mOrdersComboBox.getSelectedItem();
        if (item == null) {
            return false;
        }
        return item.isEnabled();
    }

    /**
     * Test condition displaying by GreatEqualLabel is broken
     *
     * @return true - if condition is executed, false
     */
    public boolean checkRule() {
        try {
            String txt = mRateSpinner.getValue().toString();
            if (">".equals(mGreatEqualLabel.getText())) {
                return Util.parseDouble(txt) > Util.parseDouble(mLimitTextField.getText());
            } else {
                return Util.parseDouble(txt) < Util.parseDouble(mLimitTextField.getText());
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Closes the dialog.
     *
     * @param aExitCode code of exiting
     */
    @Override
    public void closeDialog(int aExitCode) {
        mExitCode = aExitCode;
        super.closeDialog(mExitCode);
    }

    /**
     * Enable or disable SetStopLimit dialog from up command (after
     * reconnect e.g.) and check admissibility given operations by itself.
     *
     * @param aEnabled direct enable (true) or (false) dialog
     */
    @Override
    public void enableDialog(boolean aEnabled) {
        if (aEnabled) {
            if (!checkAllControls() || !checkRule()) {
                mSetButton.setEnabled(false);
                mRemoveButton.setEnabled(false);
            } else {
                mSetButton.setEnabled(true);
                mRemoveButton.setEnabled(!isRemoveForbidden());
            }
        } else {
            //ALWAYS!!!
            mSetButton.setEnabled(false);
            mRemoveButton.setEnabled(false);
        }
        //ALWAYS:
        super.enableDialog(aEnabled);
    }

    /* Is called to initialize and refresh the
     *  Limit text field.
     */
    private void fillLimitTextFields() {
        if (mOrderID == null) {
            return;
        }
        try {
            mOrderID = mOrdersComboBox.getSelectedOrder();
            Order order = mTradeDesk.getOrders().getOrder(mOrderID);
            String currency = order.getCurrency();
            double rate = order.getOrderRate();
            int index = mStopLimitComboBox.getSelectedIndex();
            Rate r = TradeDesk.getInst().getRates().getRate(order.getCurrency());
            double spread = TradeDesk.getSpread(order.getCurrency());
            if (index == STOP) {
                double distance = r.getFXCMCondDistEntryStop();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                if (order.getSide() == Side.BUY) {
                    rate -= TradeDesk.getPipsPrice(currency) * distance;
                    rate -= TradeDesk.getPipsPrice(currency) * spread;
                } else {
                    rate += TradeDesk.getPipsPrice(currency) * distance;
                    rate += TradeDesk.getPipsPrice(currency) * spread;
                }
            } else if (index == LIMIT) {
                double distance = r.getFXCMCondDistEntryLimit();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                if (order.getSide() == Side.SELL) {
                    rate -= TradeDesk.getPipsPrice(currency) * distance;
                } else {
                    rate += TradeDesk.getPipsPrice(currency) * distance;
                }
            }
            mLimitTextField.setText(TradeDesk.formatPrice(currency, rate));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Is called one-shot to initialize the Ratetextfield
     *  and select view the label (>= <=)
     */
    private void fillRatesTextFields() {
        if (mOrderID == null) {
            return;
        }
        try {
            //determine currency for our trade by ticket-position-currensy-Rate
            mOrderID = mOrdersComboBox.getSelectedOrder();
            Order order = mTradeDesk.getOrders().getOrder(mOrderID);
            int index = mStopLimitComboBox.getSelectedIndex();
            double rate = order.getOrderRate();
            BigDecimal bd = new BigDecimal(TradeDesk.getSpread(order.getCurrency()));
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
            double spread = bd.doubleValue();
            Rate r = TradeDesk.getInst().getRates().getRate(order.getCurrency());
            if (index == STOP) {
                double distance = r.getFXCMCondDistEntryStop();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                distance++;
                if (order.getSide() == Side.BUY) {
                    mGreatEqualLabel.setText("<");
                    rate -= TradeDesk.getPipsPrice(order.getCurrency()) * spread;
                    rate -= TradeDesk.getPipsPrice(order.getCurrency()) * distance;
                } else {
                    mGreatEqualLabel.setText(">");
                    rate += TradeDesk.getPipsPrice(order.getCurrency()) * spread;
                    rate += TradeDesk.getPipsPrice(order.getCurrency()) * distance;
                }
            } else if (index == LIMIT) {
                double distance = r.getFXCMCondDistEntryLimit();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                distance++;
                if (order.getSide() == Side.SELL) {
                    mGreatEqualLabel.setText("<");
                    rate -= TradeDesk.getPipsPrice(order.getCurrency()) * distance;
                } else {
                    mGreatEqualLabel.setText(">");
                    rate += TradeDesk.getPipsPrice(order.getCurrency()) * distance;
                }
            }
            if (index == STOP && order.getStopRate() != 0) {
                rate = order.getStopRate();
            } else if (index == LIMIT && order.getLimitRate() != 0) {
                rate = order.getLimitRate();
            }
            mRateSpinner.setCurrency(order.getCurrency());
            mRateSpinner.setValue(rate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* This method is called from the constructor to
    * initialize the StopLimitComboBox.
    */
    private void fillStopLimitComboBox() {
        mStopLimitComboBox.addItem(mResMan.getString("IDS_STOPLIMIT_DIALOG_ITEM_STOP"));
        mStopLimitComboBox.addItem(mResMan.getString("IDS_STOPLIMIT_DIALOG_ITEM_LIMIT"));
        mStopLimitComboBox.setSelectedIndex(mStopLimit);
    }

    /**
     * Method provided external calling
     *
     * @return Current selected value of rate
     */
    public double getRate() {
        return mRate;
    }

    /**
     * Method provided external calling
     *
     * @return stop (0) or limit (1) type selected operation
     */
    public int getStopLimit() {
        return mStopLimitComboBox.getSelectedIndex();
    }

    /**
     * Method provided external calling
     *
     * @return current ID of market order
     */
    public String getTicketID() {
        return mOrderID;
    }

    /**
     * @return trailstop value
     */
    public int getTrailStop() {
        if (mTrailStopSpinner.isEnabled()) {
            return (Integer) mTrailStopSpinner.getValue();
        } else {
            return 0;
        }
    }

    /**
     * Composition window of dialog.
     */
    private void initComponents() {
        //Composition of window
        getContentPane().setLayout(new RiverLayout());
        setModal(true);
        setTitle(mResMan.getString("IDS_STOPLIMIT_DIALOG_TITLE"));
        setBackground(Color.WHITE);
        mTrailingStopCheckBox = UIManager.getInst().createCheckBox("Trailing Stop");
        mSetButton = UIManager.getInst().createButton(mResMan.getString("IDS_STOPLIMIT_DIALOG_SET"));
        mRemoveButton = UIManager.getInst().createButton(mResMan.getString("IDS_STOPLIMIT_DIALOG_REMOVE"));
        JButton cancelButton = UIManager.getInst().createButton(mResMan.getString("IDS_STOPLIMIT_DIALOG_CANCEL"));
        mDefaultActor = new DefaultActor();
        mOrdersComboBox = new OrdersComboBox();
        mOrdersComboBox.init(mDefaultActor);
        mOrdersComboBox.setDialog(this);
        mStopLimitComboBox = new KeySensitiveComboBox();
        mStopLimitComboBox.setDialog(this);
        JLabel ticketLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_CHANGE_ENTRY_DIALOG_ORDER_ID"));
        JLabel stopLimitLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_STOPLIMIT_DIALOG_LABEL"));
        JLabel rateLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_STOPLIMIT_DIALOG_RATE_LABEL"));
        mGreatEqualLabel = UIManager.getInst().createLabel(">");
        JPanel textFieldPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JPanel spinboxPanel = new JPanel();
        JPanel conditionalPanel = new JPanel();

        mRateSpinner = UIManager.getInst().createRateSpinner();
        JFormattedTextField text = ((JSpinner.DefaultEditor) mRateSpinner.getEditor()).getTextField();
        text.setHorizontalAlignment(JTextField.LEFT);
        text.setFont(ticketLabel.getFont());
        text.setPreferredSize(new Dimension(50, 20));
        text.setEditable(true);
        text.setMargin(new Insets(0, 2, 0, 2));

        JLabel rateMinMove = UIManager.getInst().createLabel("Rate Min. Move");
        mLimitTextField = UIManager.getInst().createTextField();
        textFieldPanel.setLayout(new GridLayout(1, 2, 4, 0));
        spinboxPanel.setLayout(new BorderLayout());
        spinboxPanel.add(mRateSpinner, BorderLayout.CENTER);
        textFieldPanel.add(spinboxPanel);
        conditionalPanel.setLayout(new BorderLayout(4, 0));
        //Pointer to compare
        mGreatEqualLabel.setFont(new Font("Dialog", 0, 12));
        conditionalPanel.add(mGreatEqualLabel, BorderLayout.WEST);
        //Disabled field to indicate criteria to compare
        mLimitTextField.setEditable(false);
        mLimitTextField.addFocusListener(new FocusAdapter() {
            /**
             * Invoked when a component gains the keyboard focus.
             */
            @Override
            public void focusGained(FocusEvent aEvent) {
                mSetButton.requestFocus();
            }
        });
        conditionalPanel.add(mLimitTextField, BorderLayout.CENTER);
        textFieldPanel.add(conditionalPanel);
        fillLimitTextFields();
        fillRatesTextFields();

        buttonPanel.add(mSetButton);
        buttonPanel.add(mRemoveButton);
        buttonPanel.add(cancelButton);
        //Labels place on window
        ticketLabel.setHorizontalAlignment(SwingConstants.LEFT);
        ticketLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        stopLimitLabel.setHorizontalAlignment(SwingConstants.LEFT);
        stopLimitLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        rateLabel.setHorizontalAlignment(SwingConstants.LEFT);
        rateLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        mTrailingStopCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                JCheckBox source = (JCheckBox) aEvent.getSource();
                mTrailStopSpinner.setEnabled(source.isSelected());
            }
        });
        String smin = TradingServerSession.getInstance().getParameterValue("TRAILING_STOP_MIN");
        String smax = TradingServerSession.getInstance().getParameterValue("TRAILING_STOP_MAX");
        int min = smin == null ? 10 : Integer.parseInt(smin);
        int max = smax == null ? 500 : Integer.parseInt(smax);
        mTrailStopSpinner = UIManager.getInst().createSpinner(new SpinnerNumberModel(min, min, max, 1));
        JFormattedTextField text2 = ((JSpinner.DefaultEditor) mTrailStopSpinner.getEditor()).getTextField();
        ((DefaultFormatter) text2.getFormatter()).setAllowsInvalid(false);
        text2.setHorizontalAlignment(JTextField.LEFT);
        mTrailStopSpinner.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent aMouseWheelEvent) {
                if (aMouseWheelEvent.getWheelRotation() <= 0) {
                    mTrailStopSpinner.setValue(mTrailStopSpinner.getNextValue());
                } else {
                    mTrailStopSpinner.setValue(mTrailStopSpinner.getPreviousValue());
                }
            }
        });
        mTrailStopSpinner.setEnabled(false);

        //Combo-boxes placed on window
        mOrdersComboBox.selectOrder(mOrderID);
        mOrdersComboBox.addItemListener(new TicketComboBoxSelectItemListener());
        mStopLimitComboBox.addItemListener(new StopLimitComboBoxSelectItemListener());
        //assign behaviour a window
        getRootPane().setDefaultButton(mSetButton);
        mSetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (verify()) {
                    closeDialog(JOptionPane.YES_OPTION);
                }
            }
        });
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.NO_OPTION);
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                        "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        Util.setAllToBiggest(new JComponent[]{mSetButton, mRemoveButton, cancelButton});

        getContentPane().add("left", ticketLabel);
        getContentPane().add("tab hfill", mOrdersComboBox);

        getContentPane().add("br left", stopLimitLabel);
        getContentPane().add("tab hfill", mStopLimitComboBox);

        getContentPane().add("br left", rateLabel);
        getContentPane().add("tab hfill", textFieldPanel);

        getContentPane().add("br left", mTrailingStopCheckBox);

        getContentPane().add("br left", rateMinMove);
        getContentPane().add("tab hfill", mTrailStopSpinner);

        getContentPane().add("br center", buttonPanel);
    }

    /* Checks for remove button may be enable or not? */
    protected boolean isRemoveForbidden() {
        mOrderID = mOrdersComboBox.getSelectedOrder();
        if (mOrderID == null) {
            return true;
        }
        try {
            Order order = mTradeDesk.getOrders().getOrder(mOrderID);
            if (mStopLimitComboBox.getSelectedIndex() == STOP && order.getStopRate() == 0) {
                return true;
            }
            return mStopLimitComboBox.getSelectedIndex() == LIMIT && order.getLimitRate() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * Used for processing the signals
     * to which we subscribed.
     *
     * @param aSignal type incoming of signal.
     * @param aSrc source of signal. In this instance source can be only one.
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal.getType() == SignalType.CHANGE) {
            try {
                ChangeSignal changeSignal = (ChangeSignal) aSignal;
                Order element = (Order) changeSignal.getNewElement();
                if (element.getOrderID().equals(mOrdersComboBox.getSelectedOrder())) {
                    fillLimitTextFields();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.REMOVE) {
            try {
                RemoveSignal signal = (RemoveSignal) aSignal;
                Order element = (Order) signal.getElement();
                if (element.getOrderID().equals(mOrdersComboBox.getSelectedOrder())) {
                    String msg = "The currently selected open order has been already closed."
                                 + "\nPlease, choose another open order";
                    JOptionPane.showMessageDialog(getOwner(), msg, null, JOptionPane.ERROR_MESSAGE);
                    mOrdersComboBox.setSelectedIndex(-1);
                } else if (mOrdersComboBox.getItemCount() == 0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            closeDialog(JOptionPane.CANCEL_OPTION);
                        }
                    });
                }
                mOrdersComboBox.selectOrder(mOrdersComboBox.getSelectedOrder()); //xxx reselect to make sure
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.ADD) {
            mOrdersComboBox.selectOrder(mOrdersComboBox.getSelectedOrder()); //xxx reselect to make sure
        }
    }

    /**
     * Set combobox on required ticket in list.
     *
     * @param aTicketID ideneficator of ticket which should be set in combobox.
     */
    public void setOrderID(String aTicketID) {
        mOrderID = aTicketID;
        mOrdersComboBox.selectOrder(mOrderID);
        Order order = mTradeDesk.getOrders().getOrder(mOrderID);
        if (order != null && order.getTrailStop() > 0) {
            mTrailingStopCheckBox.setSelected(true);
            mTrailStopSpinner.setEnabled(true);
            mTrailStopSpinner.setValue(order.getTrailStop());
        }
        fillLimitTextFields();
        fillRatesTextFields();
    }

    /**
     * Service external call. Allows to set type of dialog from outside.
     *
     * @param aStopLimit set Stop (0) or Limit (1) type operation
     */
    public void setStopLimit(int aStopLimit) {
        mStopLimit = aStopLimit;
        fillLimitTextFields();
        fillRatesTextFields();
        fillStopLimitComboBox();
    }

    /**
     * Assign behaviour of window, subscribe on changing the openning positions,
     * show window and process its closing.
     *
     * @return Code of terminations
     *
     * @throws NumberFormatException when input rate is incorrect (unlikely).
     */
    @Override
    public int showModal() {
        mExitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOrders().subscribe(this, SignalType.REMOVE);
        setVisible(true);
        //Store all incorporated values
        if (mExitCode == JOptionPane.OK_OPTION) {
            //Determing selected ticket and get him
            mOrderID = mOrdersComboBox.getSelectedOrder();
            mStopLimit = mStopLimitComboBox.getSelectedIndex();
            try {
                mRate = Util.parseDouble(mRateSpinner.getValue().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                mRate = 0;
            }
        }
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOrders().unsubscribe(this, SignalType.REMOVE);
        return mExitCode;
    }

    /**
     * @return correctness all incorporated values.
     */
    private boolean verify() {
        double value;
        try {
            value = Util.parseDouble(mRateSpinner.getValue().toString());
        } catch (NumberFormatException e) {
            String message = mResMan.getString("IDS_STOPLIMIT_DIALOG_ERROR_OF_DIGIT");
            String title = mResMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }
        Order order = mTradeDesk.getOrders().getOrder(mOrderID);
        double pip = TradeDesk.getPipsPrice(order.getCurrency());
        double maxPipDist = 5000 * pip;
        double maxPrice = order.getOrderRate() + maxPipDist;
        double minPrice = order.getOrderRate() - maxPipDist;
        System.out.println("maxPipDist = " + maxPipDist);
        System.out.println("value = " + value);
        System.out.println("maxPrice = " + maxPrice);
        System.out.println("minPrice = " + minPrice);
        //Brute check
        if (value <= 0) {
            String message = mResMan.getString("IDS_STOPLIMIT_DIALOG_ZERO_RATE_ERROR");
            String title = mResMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        } else if (!checkRule()) {
            String message = mResMan.getString("IDS_STOPLIMIT_DIALOG_ERROR_OF_CONDITION");
            String title = mResMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        } else if (value < minPrice) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(this,
                                          mResMan.getString("IDS_ENTRY_DIALOG_RATE_LESS_ALLOWABLE"),
                                          mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        } else if (value > maxPrice) {
            //shows dialog with error message
            JOptionPane.showMessageDialog(this,
                                          mResMan.getString("IDS_ENTRY_DIALOG_RATE_MORE_ALLOWABLE"),
                                          mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * DefaultActor.<br>
     * Used to disable default button on dialog and enable/disable
     * Remove button if set/reset value in Stop Limit of position
     * Creation date (07.10.2003 14:16)
     */
    private class DefaultActor implements IDefaultActor {
        /**
         * Enable or disable actions buttons by command up and self conditions.
         *
         * @param aEnabled : flag to enable (true), or disable (false) buttons.
         */
        public void setEnabled(boolean aEnabled) {
            if (mOrdersComboBox.getSelectedIndex() == -1) {
                mLimitTextField.setText("");
                mRateSpinner.setValue(null);
            }
            if (aEnabled) {
                if (isDialogEnabled() && checkAllControls()) {
                    mSetButton.setEnabled(true);
                    mRemoveButton.setEnabled(!isRemoveForbidden());
                }
            } else {
                mSetButton.setEnabled(false);
                mRemoveButton.setEnabled(false);
            }
        }
    }

    /**
     * StopLimitComboBoxSelectItemListener
     * .<br>Intercepts change of combobox, updates its current contents and
     * <br>initiate check status Remove button.
     * Creation date (07.10.2003 16:28)
     */
    private class StopLimitComboBoxSelectItemListener implements ItemListener {
        /**
         * itemStateChanged.
         * Item listener. Force fill fields when Stop - Limit status is change.
         *
         * @param aEvent event
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                int iSelected = mStopLimitComboBox.getSelectedIndex();
                if (iSelected < 0) {
                    mLimitTextField.setText("XXXX.XX");
                } else if (iSelected >= 0) {
                    if (mStopLimitComboBox.getSelectedItem()
                            .equals(mResMan.getString("IDS_STOPLIMIT_DIALOG_ITEM_LIMIT"))) {
                        mTrailingStopCheckBox.setEnabled(false);
                        mTrailingStopCheckBox.setSelected(false);
                        mTrailStopSpinner.setEnabled(false);
                        mTrailStopSpinner.setValue(0);
                    } else {
                        mTrailingStopCheckBox.setEnabled(true);
                        Order order = mTradeDesk.getOrders().getOrder(mOrderID);
                        if (order.getTrailStop() == 0) {
                            mTrailStopSpinner.setValue(10);
                        } else {
                            mTrailingStopCheckBox.setSelected(true);
                            mTrailStopSpinner.setEnabled(true);
                            mTrailStopSpinner.setValue(order.getTrailStop());
                        }
                    }
                    //determine value Rate (with adjustment in one pips)
                    //for Stop/Limit situation, Sell/Buy too.
                    fillLimitTextFields();
                    fillRatesTextFields();
                    // This force check of presence stop/limit in current position
                    mDefaultActor.setEnabled(true);
                }
            }
        }
    }

    /**
     * TicketComboBoxSelectItemListener
     * Intercepts change of combobox and updates its current contents.
     * Creation date (07.10.2003 16:25)
     */
    private class TicketComboBoxSelectItemListener implements ItemListener {
        /**
         * Force fill fields when change ticket.
         *
         * @param aEvent event
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                int iSelected = mOrdersComboBox.getSelectedIndex();
                if (iSelected < 0) {
                    mLimitTextField.setText("XXXX.XX");
                } else if (iSelected >= 0) {
                    fillLimitTextFields();
                    fillRatesTextFields();
                }
            }
        }
    }
}
