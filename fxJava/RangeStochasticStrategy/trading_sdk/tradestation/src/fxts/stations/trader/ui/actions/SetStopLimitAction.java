/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/SetStopLimitAction.java#2 $
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
import fxts.stations.trader.ui.dialogs.SetStopLimitDialog;
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

/**
 * An action for setting stop-limits and for remove them for a open position.
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
 * Creation date (10/2/2003 11:02 AM)
 */
public class SetStopLimitAction extends ATradeAction implements ISignalListener {
    private static final String ACTION_NAME = "SetStopLimitAction";
    /**
     * Singeleton of this class
     */
    private static final SetStopLimitAction SET_STOP_LIMIT_ACTION = new SetStopLimitAction();
    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;

    /**
     * Dialog
     */
    private SetStopLimitDialog mDlg;
    /**
     * Flag of enabling action.
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
     * if there is no such position or table not regisered then it's first
     * position (may be with account under margin call or being-closed)
     */
    private String mTicketID;

    /**
     * Constructor of Set Stop Limit action.
     * Note: It adds action to ActionManager, a creator shouldn't take care of adding and removing it.
     */
    private SetStopLimitAction() {
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
     * Invoked when an action occurs
     */
    public void actionPerformed(ActionEvent aEvent) {
        if (mEnabled && mCanAct && !TradeDesk.getInst().getOpenPositions().isEmpty()) {
            mDlg = new SetStopLimitDialog(TradeApp.getInst().getMainFrame());
            mDlg.setTicketID(mTicketID);
            if ("LIMIT".equals(aEvent.getActionCommand())) {
                mDlg.setStopLimit(SetStopLimitDialog.LIMIT);
            } else {
                mDlg.setStopLimit(SetStopLimitDialog.STOP);
            }
            int res = TradeApp.getInst().getMainFrame().showDialog(mDlg);
            Liaison liaison = Liaison.getInstance();
            IRequestFactory requestFactory = liaison.getRequestFactory();
            IRequest request = null;
            if (res == JOptionPane.YES_OPTION) {
                String sTicketID = mDlg.getTicketID();
                int iStopLimit = mDlg.getStopLimit();
                double dStop = iStopLimit == SetStopLimitDialog.STOP ? mDlg.getRate() : -1.0;
                double dLimit = iStopLimit == SetStopLimitDialog.LIMIT ? mDlg.getRate() : -1.0;
                request = requestFactory.setStopLimitPosition(sTicketID,
                                                              dStop,
                                                              dLimit,
                                                              mDlg.getTrailStop());
            } else if (res == JOptionPane.NO_OPTION) {
                String sTicketID = mDlg.getTicketID();
                int iStopLimit = mDlg.getStopLimit();
                boolean bResetStop = iStopLimit == SetStopLimitDialog.STOP;
                boolean bResetLimit = iStopLimit == SetStopLimitDialog.LIMIT;
                request = requestFactory.resetStopLimit(sTicketID,
                                                        bResetStop,
                                                        bResetLimit,
                                                        mDlg.getTrailStop());
            }
            mDlg = null;
            if (request != null) {
                try {
                    liaison.sendRequest(request);
                } catch (LiaisonException aEx) {
                    EventQueue.invokeLater(new MessageBoxRunnable(aEx));
                }
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
        Action action = SET_STOP_LIMIT_ACTION.new StopLimitAction();
        if (aCommandString != null) {
            action.putValue(ACTION_COMMAND_KEY, aCommandString);
        }
        SET_STOP_LIMIT_ACTION.mInnerActions.add(action);
        SET_STOP_LIMIT_ACTION.checkActionEnabled();
        return action;
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (mPositionTableExists) {
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
     * Is called when the position table hasn't been added
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
     * Is called when the position table has been added.
     *
     * @param aIndex index of open position
     */
    private void setTicketIDByIndex(int aIndex) {
        if (aIndex < 0) {
            mTicketID = null;
            mEnabled = false;
        } else {
            try {
                OpenPositions openPositions = TradeDesk.getInst().getOpenPositions();
                Position position = (Position) openPositions.get(aIndex);
                mTicketID = position.getTicketID();
                String sAccountId = position.getAccount();
                mEnabled = !TradeDesk.getInst().getAccounts().getAccount(sAccountId).isUnderMarginCall()
                           && TradeDesk.getInst().getRates().getRate(position.getCurrency()).isTradable()
                           && !position.isBeingClosed()
                           && position.getClosePrice() > 0.0;
            } catch (Exception ex1) {
                //ex1.printStackTrace();
            }
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
                setTicketIDByIndex(getOpenPositionsCurRow());
            } else {
                setOpenPositionsCurRow(-1);
                setTicketIDByIndex(-1);
            }
            checkActionEnabled();
        }
    }

    /**
     * Inner class instances implements Actions which are added to user
     * interface controls
     */
    private class StopLimitAction extends AbstractAction {
        /**
         * Invoked when an action occurs
         */
        public void actionPerformed(ActionEvent aEvent) {
            SetStopLimitAction.this.actionPerformed(aEvent);
        }
    }

    /**
     * An instance of this class listens adding and removing tables to add and
     * remove its selection listeners
     */
    private class TableListener implements ITableListener {
        /**
         * Instance implementing the interface ITableSelectionListener. It watches changing
         * current OpenPosition
         */
        private ITableSelectionListener mOpenPositionsTableListener;

        /**
         * This method is called when new table is added.
         *
         * @param aTable table that was removed
         */
        public void onAddTable(ITable aTable) {
            try {
                String sTableName = aTable.getName();
                int iTableSelectedRow = aTable.getSelectedRow();
                if (OpenPositionsFrame.NAME.equals(sTableName) && mOpenPositionsTableListener == null) {
                    setOpenPositionsCurRow(iTableSelectedRow);
                    mPositionTableExists = true;
                    mOpenPositionsTableListener = new PositionSelectionListener();
                    aTable.addSelectionListener(mOpenPositionsTableListener);
                    setTicketIDByIndex(iTableSelectedRow);
                    checkActionEnabled();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * This method is called when new table is removed.
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
