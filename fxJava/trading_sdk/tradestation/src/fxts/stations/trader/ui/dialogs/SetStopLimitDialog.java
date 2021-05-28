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
 * 03/23/2006   Andre Mermegas: fixed a bug resetting rate when changing ticket
 * 05/18/2006   Andre Mermegas: fix for updating the rate text field
 * 05/22/2006   Andre Mermegas: fix to use JSpinner instead of hacked scrollbars
 * 10/04/2006   Andre Mermegas: scrollwheel support in spinner
 * 10/10/2006   Andre Mermegas: added mousewheel support to trailstop spinner
 * 05/11/2007   Andre Mermegas: spinner align left, trail stop min 10
 *
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Position;
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
public class SetStopLimitDialog extends ABaseDialog implements ISignalListener {
    public static final int STOP = 0;
    public static final int LIMIT = 1;

    private DefaultActor mDefaultActor;
    private int mExitCode;
    private JLabel mGreatEqualLabel;
    private JTextField mLimitTextField;
    private double mRate;
    private RateSpinner mRateSpinner;
    private JButton mRemoveButton;
    private ResourceManager mResMan;
    private JButton mSetButton;
    private int mStopLimit;
    private KeySensitiveComboBox mStopLimitComboBox;
    private PositionsComboBox mTicketComboBox;
    private String mTicketID;
    private TradeDesk mTradeDesk;
    private JCheckBox mTrailingStopCheckBox;
    private JSpinner mTrailStopSpinner;

    /**
     * Constructor SetStopLimitDialog.
     *
     * @param aParent parent frame
     */
    public SetStopLimitDialog(Frame aParent) {
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
        Item item = (Item) mTicketComboBox.getSelectedItem();
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
            double spinnerValue = Util.parseDouble(mRateSpinner.getValue().toString());
            double limitValue = Util.parseDouble(mLimitTextField.getText());
            if (">".equals(mGreatEqualLabel.getText())) {
                return spinnerValue > limitValue;
            } else {
                return spinnerValue < limitValue;
            }
        } catch (Exception e) {
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
        if (mTicketID == null) {
            return;
        }
        try {
            //determine currency for our trade by ticket-position-currensy-Rate
            mTicketID = mTicketComboBox.getSelectedPosition();
            Position position = mTradeDesk.getOpenPositions().getPosition(mTicketID);
            int index = mStopLimitComboBox.getSelectedIndex();
            double rate = mTradeDesk.getOpenPositions().getPosition(mTicketID).getClosePrice();
            Rate r = TradeDesk.getInst().getRates().getRate(position.getCurrency());
            if (index == STOP && position.getSide() == Side.BUY || index == LIMIT && position.getSide() == Side.SELL) {
                double distance = r.getFXCMCondDistStop();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                rate -= TradeDesk.getPipsPrice(position.getCurrency()) * distance;
            } else {
                double distance = r.getFXCMCondDistLimit();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                rate += TradeDesk.getPipsPrice(position.getCurrency()) * distance;
            }
            mLimitTextField.setText(TradeDesk.formatPrice(position.getCurrency(), rate));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Is called one-shot to initialize the Ratetextfield
     *  and select view the label (> <)
     */
    private void fillRatesTextFields(boolean aReset) {
        if (mTicketID == null) {
            return;
        }
        try {
            //determine currency for our trade by ticket-position-currensy-Rate
            mTicketID = mTicketComboBox.getSelectedPosition();
            Position position = mTradeDesk.getOpenPositions().getPosition(mTicketID);
            int index = mStopLimitComboBox.getSelectedIndex();
            double dRate = mTradeDesk.getOpenPositions().getPosition(mTicketID).getClosePrice();
            double ratePrice = 0;
            if (mRateSpinner.getValue() != null && !"".equals(mRateSpinner.getValue().toString().trim())) {
                ratePrice = Util.parseDouble(mRateSpinner.getValue().toString());
            }
            Rate r = TradeDesk.getInst().getRates().getRate(position.getCurrency());
            double rate;
            if (index == STOP && position.getSide() == Side.BUY || index == LIMIT && position.getSide() == Side.SELL) {
                double distance = r.getFXCMCondDistStop();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                distance++;
                mGreatEqualLabel.setText("<");
                dRate -= TradeDesk.getPipsPrice(position.getCurrency()) * distance;
                if (ratePrice < dRate && !aReset) {
                    rate = ratePrice;
                } else {
                    rate = dRate;
                }
            } else {
                double distance = r.getFXCMCondDistLimit();
                if (distance == 0) {
                    distance = TradeDesk.getConditionalDistance();
                }
                distance++;
                mGreatEqualLabel.setText(">");
                dRate += TradeDesk.getPipsPrice(position.getCurrency()) * distance;
                if (ratePrice > dRate && !aReset) {
                    rate = ratePrice;
                } else {
                    rate = dRate;
                }
            }
            if (index == STOP && position.getStop() != 0) {
                rate = position.getStop();
            } else if (index == LIMIT && position.getLimit() != 0) {
                rate = position.getLimit();
            }
            boolean changed = false;
            if ("<".equals(mGreatEqualLabel.getText())) {
                if (ratePrice > rate) {
                    changed = true;
                }
                if (ratePrice < dRate && !aReset) {
                    changed = false;
                }
            } else if (">".equals(mGreatEqualLabel.getText())) {
                if (ratePrice < rate) {
                    changed = true;
                }
                if (ratePrice > dRate && !aReset) {
                    changed = false;
                }
            }
            if (changed || aReset) {
                mRateSpinner.setCurrency(position.getCurrency());
                mRateSpinner.setValue(rate);
            }
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
        return mTicketID;
    }

    /**
     * @return trail stop value
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
        mSetButton = UIManager.getInst().createButton(mResMan.getString("IDS_STOPLIMIT_DIALOG_SET"));
        mRemoveButton = UIManager.getInst().createButton(mResMan.getString("IDS_STOPLIMIT_DIALOG_REMOVE"));
        JButton cancelButton = UIManager.getInst().createButton(mResMan.getString("IDS_STOPLIMIT_DIALOG_CANCEL"));
        mDefaultActor = new DefaultActor();
        mTicketComboBox = new PositionsComboBox();
        mTicketComboBox.init(mDefaultActor);
        mTicketComboBox.setDialog(this);
        mStopLimitComboBox = new KeySensitiveComboBox();
        mStopLimitComboBox.setDialog(this);
        JLabel ticketLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_STOPLIMIT_DIALOG_TICKET"));
        JLabel stopLimitLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_STOPLIMIT_DIALOG_LABEL"));
        JLabel rateLabel = UIManager.getInst().createLabel(mResMan.getString("IDS_STOPLIMIT_DIALOG_RATE_LABEL"));
        mGreatEqualLabel = UIManager.getInst().createLabel();
        JPanel buttonPanel = new JPanel();
        JPanel textFieldPanel = new JPanel();
        JPanel spinboxPanel = new JPanel();
        JPanel conditionalPanel = new JPanel();

        mRateSpinner = UIManager.getInst().createRateSpinner();
        JFormattedTextField text = ((JSpinner.DefaultEditor) mRateSpinner.getEditor()).getTextField();
        text.setHorizontalAlignment(JTextField.LEFT);
        text.setFont(rateLabel.getFont());
        text.setPreferredSize(new Dimension(50, 20));
        text.setEditable(true);
        text.setMargin(new Insets(0, 2, 0, 2));

        JLabel rateMinMove = UIManager.getInst().createLabel("Rate Min. Move");
        //Disabled text field
        mLimitTextField = UIManager.getInst().createTextField();
        mTrailingStopCheckBox = UIManager.getInst().createCheckBox("Trailing Stop");

        //Composition of window
        setModal(true);
        setTitle(mResMan.getString("IDS_STOPLIMIT_DIALOG_TITLE"));
        setBackground(Color.WHITE);

        //sets main panel
        getContentPane().setLayout(new RiverLayout());

        //Entry fields placed on theirself pane
        textFieldPanel.setLayout(new GridLayout(1, 2, 4, 0));
        spinboxPanel.setLayout(new BorderLayout());
        spinboxPanel.add(mRateSpinner, BorderLayout.CENTER);
        textFieldPanel.add(spinboxPanel);
        conditionalPanel.setLayout(new BorderLayout(4, 0));
        //Pointer to compare
        mGreatEqualLabel.setText(">");
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
        buttonPanel.add(mSetButton);
        buttonPanel.add(mRemoveButton);
        buttonPanel.add(cancelButton);
        ticketLabel.setHorizontalAlignment(SwingConstants.LEFT);
        ticketLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        stopLimitLabel.setHorizontalAlignment(SwingConstants.LEFT);
        stopLimitLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        textFieldPanel.add(conditionalPanel);
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
        mTicketComboBox.selectPosition(mTicketID);
        mTicketComboBox.addItemListener(new TicketComboBoxSelectItemListener());
        mStopLimitComboBox.addItemListener(new StopLimitComboBoxSelectItemListener());

        //assign behaviour a window
        getRootPane().setDefaultButton(mSetButton);
        // Set-select of user is a YES-analogy
        mSetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (verify()) {
                    closeDialog(JOptionPane.YES_OPTION);
                }
            }
        });
        // Remove-select of user is a NO-analogy
        mRemoveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.NO_OPTION);
            }
        });
        // Cancel-select of user is already Cancel
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exiting by escape
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

        getContentPane().add("left", ticketLabel);
        getContentPane().add("tab hfill", mTicketComboBox);

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
        mTicketID = mTicketComboBox.getSelectedPosition();
        if (mTicketID == null) {
            return true;
        }
        try {
            Position position = mTradeDesk.getOpenPositions().getPosition(mTicketID);
            if (mStopLimitComboBox.getSelectedIndex() == STOP && position.getStop() == 0.0) {
                return true;
            }
            return mStopLimitComboBox.getSelectedIndex() == LIMIT && position.getLimit() == 0.0;
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
                ChangeSignal signal = (ChangeSignal) aSignal;
                Position element = (Position) signal.getNewElement();
                if (element.getTicketID().equals(mTicketComboBox.getSelectedPosition())) {
                    fillLimitTextFields();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.REMOVE) {
            try {
                RemoveSignal signal = (RemoveSignal) aSignal;
                Position element = (Position) signal.getElement();
                if (element.getTicketID().equals(mTicketComboBox.getSelectedPosition())) {
                    String msg = "The currently selected open position has been already closed."
                                 + "\nPlease, choose another open position";
                    JOptionPane.showMessageDialog(getOwner(), msg, null, JOptionPane.ERROR_MESSAGE);
                    mTicketComboBox.setSelectedIndex(-1);
                } else if (mTicketComboBox.getItemCount() == 0) {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            closeDialog(JOptionPane.CANCEL_OPTION);
                        }
                    });
                }
                mTicketComboBox.selectPosition(mTicketComboBox.getSelectedPosition()); //xxx reselect to make sure
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSignal.getType() == SignalType.ADD) {
            mTicketComboBox.selectPosition(mTicketComboBox.getSelectedPosition()); //xxx reselect to make sure
        }
    }

    /**
     * Service external call. Allows to set type of dialog from outside.
     *
     * @param aStopLimit set Stop (0) or Limit (1) type operation
     */
    public void setStopLimit(int aStopLimit) {
        mStopLimit = aStopLimit;
        fillLimitTextFields();
        fillRatesTextFields(false);
        fillStopLimitComboBox();
    }

    /**
     * Set combobox on required ticket in list.
     *
     * @param aTicketID ideneficator of ticket which should be set in combobox.
     */
    public void setTicketID(String aTicketID) {
        mTicketID = aTicketID;
        mTicketComboBox.selectPosition(mTicketID);
        Position position = mTradeDesk.getOpenPositions().getPosition(mTicketID);
        if (position != null && position.getTrailStop() > 0) {
            mTrailingStopCheckBox.setSelected(true);
            mTrailStopSpinner.setEnabled(true);
            mTrailStopSpinner.setValue(position.getTrailStop());
        }
        fillLimitTextFields();
        fillRatesTextFields(false);
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
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.REMOVE);
        fillRatesTextFields(false);
        fillLimitTextFields();
        setVisible(true);
        //Store all incorporated values
        if (mExitCode == JOptionPane.OK_OPTION) {
            //Determing selected ticket and get him
            mTicketID = mTicketComboBox.getSelectedPosition();
            mStopLimit = mStopLimitComboBox.getSelectedIndex();
            try {
                mRate = Util.parseDouble(mRateSpinner.getValue().toString());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                mRate = 0.0;
            }
        }
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.ADD);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.CHANGE);
        TradeDesk.getInst().getOpenPositions().unsubscribe(this, SignalType.REMOVE);
        return mExitCode;
    }

    /**
     * @return Checks correctness all incorporated values.
     */
    private boolean verify() {
        double value;
        try {
            value = Util.parseDouble(mRateSpinner.getValue().toString());
        } catch (NumberFormatException e) {
            String sMessage = mResMan.getString("IDS_STOPLIMIT_DIALOG_ERROR_OF_DIGIT");
            String sTitle = mResMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(this, sMessage, sTitle, JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }
        //Brute check
        if (value <= 0.0) {
            String sMessage = mResMan.getString("IDS_STOPLIMIT_DIALOG_ZERO_RATE_ERROR");
            String sTitle = mResMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(this, sMessage, sTitle, JOptionPane.ERROR_MESSAGE);
            mRateSpinner.requestFocus();
            return false;
        }

        //checking to satisfy of possible range
        Position position = mTradeDesk.getOpenPositions().getPosition(mTicketID);
        Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
        if (TradeDesk.getConditionalDistance() > 0
            || rate.getFXCMCondDistStop() > 0
            || rate.getFXCMCondDistLimit() > 0) {
            String ccy = position.getCurrency();
            double pip = TradeDesk.getPipsPrice(ccy);
            double price = TradeDesk.formatPrice2(ccy, Util.parseDouble(mLimitTextField.getText()));
            double maxPipDist = 5000 * pip;
            double maxPrice = TradeDesk.formatPrice2(ccy, price + maxPipDist);
            double minPrice = TradeDesk.formatPrice2(ccy, price - maxPipDist);
            System.out.println("\nmaxPipDist = " + TradeDesk.formatPrice(ccy, maxPipDist));
            System.out.println("value = " + TradeDesk.formatPrice(ccy, value));
            System.out.println("price = " + TradeDesk.formatPrice(ccy, price));
            System.out.println("maxPrice = " + TradeDesk.formatPrice(ccy, maxPrice));
            System.out.println("minPrice = " + TradeDesk.formatPrice(ccy, minPrice));
            //checking to satisfy of possible range
            if ("<".equals(mGreatEqualLabel.getText()) && value >= price
                || ">".equals(mGreatEqualLabel.getText()) && value > maxPrice) {
                JOptionPane.showMessageDialog(this,
                                              mResMan.getString("IDS_ENTRY_DIALOG_RATE_MORE_ALLOWABLE"),
                                              mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                              JOptionPane.ERROR_MESSAGE);
                mRateSpinner.requestFocus();
                return false;
            } else if (">".equals(mGreatEqualLabel.getText()) && value <= price
                       || "<".equals(mGreatEqualLabel.getText()) && value < minPrice) {
                JOptionPane.showMessageDialog(this,
                                              mResMan.getString("IDS_ENTRY_DIALOG_RATE_LESS_ALLOWABLE"),
                                              mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                              JOptionPane.ERROR_MESSAGE);
                mRateSpinner.requestFocus();
                return false;
            }
        } else if (!checkRule()) {
            String sMessage = mResMan.getString("IDS_STOPLIMIT_DIALOG_ERROR_OF_CONDITION");
            String sTitle = mResMan.getString("IDS_MAINFRAME_SHORT_TITLE");
            JOptionPane.showMessageDialog(this, sMessage, sTitle, JOptionPane.ERROR_MESSAGE);
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
            if (mTicketComboBox.getSelectedIndex() == -1) {
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
                        Position position = mTradeDesk.getOpenPositions().getPosition(mTicketID);
                        if (position.getTrailStop() == 0) {
                            mTrailStopSpinner.setValue(10);
                        } else {
                            mTrailingStopCheckBox.setSelected(true);
                            mTrailStopSpinner.setEnabled(true);
                            mTrailStopSpinner.setValue(position.getTrailStop());
                        }
                    }
                    //determine value Rate (with adjustment in one pips)
                    //for Stop/Limit situation, Sell/Buy too.
                    fillLimitTextFields();
                    fillRatesTextFields(true);
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
                int iSelected = mTicketComboBox.getSelectedIndex();
                if (iSelected < 0) {
                    mLimitTextField.setText("XXXX.XX");
                } else if (iSelected >= 0) {
                    fillLimitTextFields();
                    fillRatesTextFields(true);
                }
            }
        }
    }
}
