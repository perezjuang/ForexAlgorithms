/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/SetStopLimitOrderAction.java#2 $
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
 */
package fxts.stations.trader.ui.actions;

import com.fxcm.fix.IFixDefs;
import fxts.stations.core.ATradeAction;
import fxts.stations.core.ActionManager;
import fxts.stations.core.Orders;
import fxts.stations.core.Rates;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Rate;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MessageBoxRunnable;
import fxts.stations.trader.ui.dialogs.SetStopLimitDialog;
import fxts.stations.trader.ui.dialogs.SetStopLimitOrderDialog;
import fxts.stations.trader.ui.frames.OrdersFrame;
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
 * An action for setting stop-limits and for remove them for a entry order.
 * The action is enabled when:
 * <br>
 * <ol>
 * <li> If orders table is registered
 * <ul>
 * <li>there is selected a order;</li>
 * <li>it is not being close;</li>
 * <li>and it's account not under margin call;</li>
 * <li>and this rate is available for trades;</li></li></ul>
 * <li>Or the table is not registered and
 * <ul>
 * <li>there is open order;</li>
 * <li>which account not under margin call;</li>
 * <li>which is not being closed;</li>
 * <li>which rate is available for trades;</li></li></ul>
 * </ol>
 * <br>
 * Note. The instance of that class should be created after initialization
 * of core component, TradeDesk especially, but before creating of ITable instances
 * Creation date (10/2/2003 11:02 AM)
 */
public class SetStopLimitOrderAction extends ATradeAction implements ISignalListener {
    private static final String ACTION_NAME = "SetStopLimitOrderAction";
    /**
     * Singeleton of this class
     */
    private static final SetStopLimitOrderAction SET_STOP_LIMIT_ACTION = new SetStopLimitOrderAction();
    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;

    /**
     * Dialog
     */
    private SetStopLimitOrderDialog mDlg;
    /**
     * Flag of enabling action.
     */
    private boolean mEnabled;
    /**
     * Inner Actions
     */
    private final WeakListener<Action> mInnerActions = new WeakListener<Action>();
    /**
     * order which is selected in the orders table if it registered or
     * if there is no such order or table not regisered then it's first
     * order
     */
    private String mOrderID;
    /**
     * Stores the index of the current row in the Orders table. It is changed
     * in RateSelectionListener.onTableChangeSelection method
     */
    private int mOrdersCurRow = -1;
    /**
     * Sign of existion order Table
     */
    private boolean mOrdersTableExists;

    /**
     * Constructor of Set Stop Limit action.
     * Note: It adds action to ActionManager, a creator shouldn't take care of adding and removing it.
     */
    private SetStopLimitOrderAction() {
        super(ACTION_NAME);
        try {
            ActionManager.getInst().add(this);
            TableManager.getInst().addListener(new TableListener());
            TradeDesk.getInst().getOrders().subscribe(this, SignalType.CHANGE);
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
        if (mEnabled && mCanAct) {
            mDlg = new SetStopLimitOrderDialog(TradeApp.getInst().getMainFrame());
            mDlg.setOrderID(mOrderID);
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
                double dStop = iStopLimit == SetStopLimitDialog.STOP ? mDlg.getRate() : -1;
                double dLimit = iStopLimit == SetStopLimitDialog.LIMIT ? mDlg.getRate() : -1;
                request = requestFactory.setStopLimitOrder(sTicketID,
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
     * Returns stores the index of the current row in the Orders table. It is changed
     * in RateSelectionListener.onTableChangeSelection method
     *
     * @return stores the index of the current row in the Orders table. It is changed
     */
    private int getOrdersCurRow() {
        if (mOrdersCurRow <= TradeDesk.getInst().getOrders().size()) {
            return mOrdersCurRow;
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
        if (mOrdersTableExists) {
            if (aSignal.getType() == SignalType.CHANGE) {
                Order order = (Order) ((ChangeSignal) aSignal).getNewElement();
                String orderID = order.getOrderID();
                String currentOrder = null;
                if (getOrdersCurRow() >= 0) {
                    currentOrder = ((Order) TradeDesk.getInst().getOrders().get(getOrdersCurRow())).getOrderID();
                }
                if (orderID.equals(currentOrder)) {
                    mEnabled = order.isCurrencyTradable() && order.isEntryOrder();
                    checkActionEnabled();
                }
            }
        } else {
            mEnabled = false;
            checkActionEnabled();
        }
    }

    /**
     * Is called when the orders table hasn't been added
     */
    private void setInitialEnable() {
        try {
            Orders orders = TradeDesk.getInst().getOrders();
            Rates rates = TradeDesk.getInst().getRates();
            if (orders == null || rates == null) {
                mEnabled = false;
                return;
            }
            if (mEnabled) {
                mEnabled = false;
                for (int i = 0; i < orders.size(); i++) {
                    Order order = (Order) orders.get(i);
                    Rate rate = rates.getRate(order.getCurrency());
                    if (rate != null && rate.isTradable() && order.isEntryOrder()) {
                        mEnabled = true;
                        break;
                    }
                }
            }
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }
    }

    /**
     * Is called when the order table has been added.
     *
     * @param aIndex index of order
     */
    private void setOrderByIndex(int aIndex) {
        if (aIndex < 0) {
            mOrderID = null;
            mEnabled = false;
        } else {
            try {
                Orders orders = TradeDesk.getInst().getOrders();
                if (orders == null || orders.size() <= aIndex) {
                    return;
                }
                Order order = (Order) orders.get(aIndex);
                Rates rates = TradeDesk.getInst().getRates();
                if (rates == null) {
                    return;
                }
                Rate rate = rates.getRate(order.getCurrency());
                if (rate == null) {
                    return;
                }
                mOrderID = order.getOrderID();
                mEnabled = rate.isTradable() && order.isEntryOrder();
            } catch (Exception ex1) {
                ex1.printStackTrace();
            }
        }
    }

    /**
     * Sets stores the index of the current row in the Orders table. It is changed
     * in RateSelectionListener.onTableChangeSelection method
     *
     * @param aOrdersCurRow stores the index of the current row in the Orders table. It is changed
     */
    private void setOrdersCurRow(int aOrdersCurRow) {
        mOrdersCurRow = aOrdersCurRow;
    }

    /**
     * An instance of this class listens changing selected order on orders table
     */
    private class OrderSelectionListener implements ITableSelectionListener {
        /**
         * This method is called when selection is changed.
         *
         * @param aTable table which row was changed
         * @param aRow changed row
         */
        public void onTableChangeSelection(ITable aTable, int[] aRow) {
            if (aRow.length > 0 && aRow[0] <= TradeDesk.getInst().getOrders().size()) {
                setOrdersCurRow(aRow[0]);
                setOrderByIndex(getOrdersCurRow());
            } else {
                mEnabled = false;
                setOrdersCurRow(-1);
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
            SetStopLimitOrderAction.this.actionPerformed(aEvent);
        }
    }

    /**
     * An instance of this class listens adding and removing tables to add and
     * remove its selection listeners
     */
    private class TableListener implements ITableListener {
        /**
         * Instance implementing the interface ITableSelectionListener. It watches changing
         * current order
         */
        private ITableSelectionListener mOrdersTableListener;

        /**
         * This method is called when new table is added.
         *
         * @param aTable table that was added
         */
        public void onAddTable(ITable aTable) {
            try {
                if (OrdersFrame.NAME.equals(aTable.getName()) && mOrdersTableListener == null) {
                    setOrdersCurRow(-1);
                    mOrdersTableExists = true;
                    mOrdersTableListener = new OrderSelectionListener();
                    aTable.addSelectionListener(mOrdersTableListener);
                    setOrderByIndex(-1);
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
                if (OrdersFrame.NAME.equals(aTable.getName())) {
                    mOrdersTableExists = false;
                    ITableSelectionListener tmp = mOrdersTableListener;
                    mOrdersTableListener = null;
                    if (tmp != null) {
                        aTable.removeSelectionListener(tmp);
                    }
                    setOrderByIndex(0);
                    checkActionEnabled();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
