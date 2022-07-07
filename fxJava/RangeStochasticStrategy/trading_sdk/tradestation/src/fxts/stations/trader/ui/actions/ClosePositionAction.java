/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/ClosePositionAction.java#3 $
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
 * $History: $
 * 05/05/2006   Andre Mermegas: ignore totals row
 */
package fxts.stations.trader.ui.actions;

import com.fxcm.fix.IFixDefs;
import fxts.stations.core.ATradeAction;
import fxts.stations.core.ActionManager;
import fxts.stations.core.OpenPositions;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Position;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MessageBoxRunnable;
import fxts.stations.trader.ui.dialogs.CloseAmountsComboBox;
import fxts.stations.trader.ui.dialogs.CloseMultiplePositionsDialog;
import fxts.stations.trader.ui.dialogs.ClosePositionDialog;
import fxts.stations.trader.ui.dialogs.OutOfLongException;
import fxts.stations.trader.ui.frames.OpenPositionsFrame;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequestFactory;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.ITable;
import fxts.stations.ui.ITableListener;
import fxts.stations.ui.ITableSelectionListener;
import fxts.stations.ui.TableManager;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.WeakListener;
import fxts.stations.util.signals.ChangeSignal;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An action of closing open position. <br>
 * The action is enabled when:<br>
 * <ol>
 * <li> If OpenPositions table is registered
 * <ul>
 * <li>there is selected a position;</li>
 * <li>it is not being close;</li>
 * <li>and it's account not under margin call;</li>
 * <li>and this rate is available for trades;</li></li></ul>
 * <li>Or the table is not registered and
 * <ul>
 * <li>there is open position;</li>
 * <li>which account not under margin call;</li>
 * <li>which is not being closed;</li>
 * <li>which rate is available for trades;</li></li></ul>
 * </ol>
 * <br>
 * Note. The instance of that class should be created after initialization
 * of core component, TradeDesk especially, but before creating of ITable instances
 * Creation date (10/4/2003 9:50 AM)
 */
public class ClosePositionAction extends ATradeAction implements ISignalListener {
    /**
     * Name of the action.
     */
    private static final String ACTION_NAME = "ClosePositionAction";
    /**
     * Singeleton of this class
     */
    private static final ClosePositionAction CLOSE_POSITION_ACTION = new ClosePositionAction();
    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;

    /**
     * Dialog
     */
    private ClosePositionDialog mDlg;
    /**
     * Flag of enabling action
     */
    private boolean mEnabled;
    /**
     * Inner Actions
     */
    private final WeakListener<Action> mInnerActions = new WeakListener<Action>();
    /**
     * Stores the index of the current row in the OpenPositions table. It is changed
     * in PositionSelectionListener.onTableChangeSelection method
     */
    private int mOpenPositionsCurRow = -1;
    /**
     * Sign of existion Position Table
     */
    private boolean mPositionTableExists;
    /**
     * Position which is selected in the OpenPositions table if it registered or
     * if there is no such position or table not regisered then it's first position
     */
    private List<String> mTicketList;

    /**
     * Constructor of Close Position action.
     * Note: It adds action to ActionManager, a creator shouldn't take care of adding and removing it.
     */
    private ClosePositionAction() {
        super(ACTION_NAME);
        try {
            ActionManager.getInst().add(this);
            TableManager.getInst().addListener(new TableListener());
            TradeDesk.getInst().getOpenPositions().subscribe(this, SignalType.CHANGE);
            setInitialEnable();
            checkActionEnabled();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Invoked when an action is called
     */
    public void actionPerformed(ActionEvent aEvent) {
        OpenPositions positions = TradeDesk.getInst().getOpenPositions();
        if (mEnabled && mCanAct && !positions.isEmpty()) {
            TradeApp app = TradeApp.getInst();
            if (mTicketList.size() > 1) {
                CloseMultiplePositionsDialog cmpd = new CloseMultiplePositionsDialog(app.getMainFrame(), mTicketList);
                int res = app.getMainFrame().showDialog(cmpd);
                if (res == JOptionPane.OK_OPTION) {
                    Map<String, CloseAmountsComboBox> amountMap = cmpd.getAmountMap();
                    for (String ticket : mTicketList) {
                        Liaison liaison = Liaison.getInstance();
                        IRequestFactory requestFactory = liaison.getRequestFactory();
                        long amount;
                        try {
                            amount = amountMap.get(ticket).getSelectedAmountLong();
                        } catch (OutOfLongException e) {
                            e.printStackTrace();
                            amount = positions.getPosition(ticket).getAmount();
                        }
                        IRequest request;
                        if (cmpd.isAtBest()) {
                            request = requestFactory.closeTrueMarket(ticket, amount, cmpd.getCustomText());
                        } else {
                            request = requestFactory.closePosition(ticket,
                                                                   amount,
                                                                   cmpd.getCustomText(),
                                                                   cmpd.getAtMarket());
                        }
                        try {
                            Liaison.getInstance().sendRequest(request);
                        } catch (LiaisonException aEx) {
                            EventQueue.invokeLater(new MessageBoxRunnable(aEx));
                        }
                    }
                }
            } else if (mTicketList.size() == 1) {
                mDlg = new ClosePositionDialog(app.getMainFrame());
                Position position = positions.getPosition(mTicketList.get(0));
                mDlg.setTicketID(position.getTicketID());
                int res = app.getMainFrame().showDialog(mDlg);
                if (res == JOptionPane.OK_OPTION) {
                    Liaison liaison = Liaison.getInstance();
                    IRequestFactory requestFactory = liaison.getRequestFactory();
                    String ticketID = mDlg.getTicketID();
                    long amount = mDlg.getAmount();
                    IRequest request;
                    if (mDlg.isAtBest()) {
                        request = requestFactory.closeTrueMarket(ticketID, amount, mDlg.getCustomText());
                    } else {
                        request = requestFactory.closePosition(ticketID,
                                                               amount,
                                                               mDlg.getCustomText(),
                                                               mDlg.getAtMarket());
                    }
                    try {
                        liaison.sendRequest(request);
                    } catch (LiaisonException aEx) {
                        EventQueue.invokeLater(new MessageBoxRunnable(aEx));
                    }
                }
                mDlg = null;
            }
        }
    }

    /**
     * Sets Flag of enabling action.Called by Action manager:
     */
    @Override
    public void canAct(boolean aCanAct) {
        if (TradingServerSession.getInstance().getUserKind() != IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
            mCanAct = aCanAct;
            checkActionEnabled();
            if (mDlg != null) {
                mDlg.enableDialog(aCanAct);
            }
        }
    }

    /**
     * Dispatches enabling flag to inner action objects
     */
    public void checkActionEnabled() {
        for (Action action : mInnerActions) {
            action.setEnabled(mEnabled && mCanAct);
        }
    }

    /**
     * Returns stores the index of the current row in the OpenPositions table. It is changed
     * in PositionSelectionListener.onTableChangeSelection method
     *
     * @return stores the index of the current row in the OpenPositions table. It is changed
     */
    public int getOpenPositionsCurRow() {
        if (mOpenPositionsCurRow < TradeDesk.getInst().getOpenPositions().size()) {
            return mOpenPositionsCurRow;
        } else {
            return -1;
        }
    }

    /**
     * Returns Inner class instances that implements Actions, which are added to user interface controls.
     *
     * @param aCommandString Action kommand key
     *
     * @return Inner class instance that implements Action.
     */
    public static Action newAction(String aCommandString) {
        Action action = CLOSE_POSITION_ACTION.new CloseAction();
        if (aCommandString != null) {
            action.putValue(ACTION_COMMAND_KEY, aCommandString);
        }
        CLOSE_POSITION_ACTION.mInnerActions.add(action);
        CLOSE_POSITION_ACTION.checkActionEnabled();
        return action;
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (mPositionTableExists && TradingServerSession.getInstance().getUserKind() != IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
            if (aSignal.getType() == SignalType.CHANGE) {
                Position position = (Position) ((ChangeSignal) aSignal).getNewElement();
                String updatedTicket = position.getTicketID();
                String currentTicket = null;
                if (getOpenPositionsCurRow() >= 0) {
                    OpenPositions positions = TradeDesk.getInst().getOpenPositions();
                    currentTicket = ((Position) positions.get(getOpenPositionsCurRow())).getTicketID();
                }
                if (updatedTicket.equals(currentTicket)) {
                    String accountID = position.getAccount();
                    Account account = TradeDesk.getInst().getAccounts().getAccount(accountID);
                    mEnabled = !account.isUnderMarginCall()
                               && position.getClosePrice() > 0.0
                               && !position.isBeingClosed();
                    checkActionEnabled();
                }
            }
        } else {
            /** TBD for future using
             if the Position table is not registered:
             a) there is open position;
             b) which account not under margin call;
             c) which is not being-closed;
             */
            mEnabled = false;
            checkActionEnabled();
        }
    }

    /**
     * Initiates beginning state of enable flag
     */
    private void setInitialEnable() {
        mEnabled = false;
    }

    /**
     * Sets stores the index of the current row in the OpenPositions table. It is changed
     * in PositionSelectionListener.onTableChangeSelection method
     *
     * @param aOpenPositionsCurRow stores the index of the current row in the OpenPositions table. It is changed
     */
    public void setOpenPositionsCurRow(int aOpenPositionsCurRow) {
        mOpenPositionsCurRow = aOpenPositionsCurRow;
    }

    /**
     * Sets msTicketID by the index of selected row in the position table.
     *
     * @param aIndex index of the open position
     */
    private void setTicketIDByIndex(int[] aIndex) {
        if (aIndex == null || aIndex.length < 0 || aIndex[0] == TradeDesk.getInst().getOpenPositions().size()) {
            mTicketList = null;
            mEnabled = false;
        } else {
            try {
                OpenPositions openPositions = TradeDesk.getInst().getOpenPositions();
                mTicketList = new ArrayList<String>();
                for (int pos : aIndex) {
                    if (pos >= 0) {
                        Position position = (Position) openPositions.get(pos);
                        mTicketList.add(position.getTicketID());
                        String sAccountId = position.getAccount();
                        mEnabled = !TradeDesk.getInst().getAccounts().getAccount(sAccountId).isUnderMarginCall()
                                   && TradeDesk.getInst().getRates().getRate(position.getCurrency()).isTradable()
                                   && !position.isBeingClosed()
                                   && position.getClosePrice() > 0.0;
                    }
                }
            } catch (Exception aException) {
                //
            }
        }
    }

    /**
     * Inner class instances implement Actions which are added to user
     * interface controls
     */
    private class CloseAction extends AbstractAction {
        /**
         * Invoked when an action occurs
         */
        public void actionPerformed(ActionEvent aEvent) {
            ClosePositionAction.this.actionPerformed(aEvent);
        }
    }

    /**
     * An instance of this class listens changing selected position on Positions table
     */
    private class PositionSelectionListener implements ITableSelectionListener {
        /**
         * This method is called when selection is changed.
         *
         * @param aTable table which row was changed
         * @param aRow changed row
         */
        public void onTableChangeSelection(ITable aTable, int[] aRow) {
            if (aRow.length > 0 && aRow[0] <= TradeDesk.getInst().getOpenPositions().size()) {
                setOpenPositionsCurRow(aRow[0]);
                setTicketIDByIndex(aRow);
            } else {
                setOpenPositionsCurRow(-1);
                setTicketIDByIndex(null);
            }
            checkActionEnabled();
        }
    }

    /**
     * An instance of this class listens adding and removing tables to add and
     * remove its selection listeners
     */
    private class TableListener implements ITableListener {
        /**
         * Instance implementing the interface ITableSelectionListener. It watches changing
         * current Open Position
         */
        private ITableSelectionListener mOpenPositionsTableListener;

        /**
         * Is called when a table has been added.
         *
         * @param aTable table that was added
         */
        public void onAddTable(ITable aTable) {
            try {
                if (OpenPositionsFrame.NAME.equals(aTable.getName()) && mOpenPositionsTableListener == null) {
                    setOpenPositionsCurRow(aTable.getSelectedRow());
                    mPositionTableExists = true;
                    mOpenPositionsTableListener = new PositionSelectionListener();
                    aTable.addSelectionListener(mOpenPositionsTableListener);
                    setTicketIDByIndex(new int[]{aTable.getSelectedRow()});
                    checkActionEnabled();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Is called when a table has been removed
         *
         * @param aTable table that was removed
         */
        public void onRemoveTable(ITable aTable) {
            try {
                String sTableName = aTable.getName();
                if (OpenPositionsFrame.NAME.equals(sTableName)) {
                    mPositionTableExists = false;
                    ITableSelectionListener tmp = mOpenPositionsTableListener;
                    mOpenPositionsTableListener = null;
                    if (tmp != null) {
                        aTable.removeSelectionListener(tmp);
                    }
                    setInitialEnable();
                    checkActionEnabled();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
