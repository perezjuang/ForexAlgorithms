/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/UpdateEntryOrderAction.java#2 $
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
import fxts.stations.trader.ui.dialogs.UpdateEntryOrderDialog;
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
 * This action is used to handle action for updating of entry order.
 * <br>
 * The instance of that class should be created after initialization
 * of core component, TradeDesk especially, but before creating of ITable instances
 */
public class UpdateEntryOrderAction extends ATradeAction implements ISignalListener {
    private static final String ACTION_NAME = "UpdateEntryOrderAction";
    /**
     * Instance.
     */
    private static final UpdateEntryOrderAction UPDATE_ENTRY_ORDER_ACTION = new UpdateEntryOrderAction();
    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;
    /**
     * Dialog
     */
    private UpdateEntryOrderDialog mDialog;
    /**
     * Flag of enabling action The action is enabled when:
     * 1)  if the Orders table is registered:
     * a) and some entry order is selected;
     * b) and it's currency available for trade;
     * 2)  or if the table is not registered, but:
     * a) there is entry order;
     * b) which currency available for trade;
     */
    private boolean mEnabled;
    /**
     * Inner Actions
     */
    private final WeakListener<Action> mInnerActions = new WeakListener<Action>();
    /**
     * OrderID. Is determined by the selected row in the Orders table if it registered.
     * If table is not registered (or no such position) then it's first order
     * where currency is available for trades;
     */
    private String mOrderID;
    /**
     * Stores the index of the current row in the Orders table. It is changed
     * in RateSelectionListener.onTableChangeSelection method
     */
    private int mOrdersCurRow = -1;

    /**
     * Sign of existion Orders Table
     */
    private boolean mOrdersTableExists;
    /**
     * TradeDesk singleton
     */
    private TradeDesk mTradeDesk = TradeDesk.getInst();

    /**
     * Constructor of Update Entry Order action.
     * Note: It adds action to ActionManager, a creator shouldn't take care of adding and removing it.
     */
    private UpdateEntryOrderAction() {
        super(ACTION_NAME);
        try {
            ActionManager.getInst().add(this);
            TableManager.getInst().addListener(new TableListener());
            TradeDesk.getInst().getOrders().subscribe(this, SignalType.ADD);
            TradeDesk.getInst().getOrders().subscribe(this, SignalType.CHANGE);
            TradeDesk.getInst().getOrders().subscribe(this, SignalType.REMOVE);
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
            double prevRateValue = 0;
            try {
                mDialog = new UpdateEntryOrderDialog(TradeApp.getInst().getMainFrame());
                Orders orders = mTradeDesk.getOrders();
                if (mOrderID == null) {
                    if (orders.isEmpty()) {
                        return;
                    } else {
                        Order order = (Order) orders.get(0);
                        mOrderID = order.getOrderID();
                        prevRateValue = order.getOrderRate();
                    }
                } else {
                    Order order = orders.getOrder(mOrderID);
                    prevRateValue = order.getOrderRate();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mDialog.setOrderID(mOrderID);
            int res = TradeApp.getInst().getMainFrame().showDialog(mDialog);
            if (res == JOptionPane.OK_OPTION) {
                //checking data for changing
                if (prevRateValue != mDialog.getOrderRate()) {
                    // Send Request
                    Liaison liaison = Liaison.getInstance();
                    IRequestFactory requestFactory = liaison.getRequestFactory();
                    IRequest request = requestFactory.updateEntryOrder(mDialog.getOrderID(),
                                                                       mDialog.getOrderRate(),
                                                                       0,
                                                                       mDialog.getCustomText());

                    try {
                        liaison.sendRequest(request);
                    } catch (LiaisonException aEx) {
                        EventQueue.invokeLater(new MessageBoxRunnable(aEx));
                    }
                }
            }
            mDialog = null;
        }
    }

    /**
     * Sets Flag of enabling action.Called by Action manager.
     */
    @Override
    public void canAct(boolean aCanAct) {
        if (TradingServerSession.getInstance().getUserKind() != IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
            mCanAct = aCanAct;
            checkActionEnabled();
            if (mDialog != null) {
                mDialog.enableDialog(aCanAct);
            }
        }
    }

    /**
     * Checking action for enable.
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
        if (mOrdersCurRow <= mTradeDesk.getOrders().size()) {
            return mOrdersCurRow;
        } else {
            return -1;
        }
    }

    /**
     * Retuns Inner class instances implements Actions which are added to user interface controls.
     *
     * @param aCommandString action command string
     */
    public static Action newAction(String aCommandString) {
        Action action = UPDATE_ENTRY_ORDER_ACTION.new EntryOrderAction();
        if (aCommandString != null) {
            action.putValue(ACTION_COMMAND_KEY, aCommandString);
        }
        UPDATE_ENTRY_ORDER_ACTION.mInnerActions.add(action);
        UPDATE_ENTRY_ORDER_ACTION.checkActionEnabled();
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
                String updatedOrderID = order.getOrderID();
                String currentOrderID = null;
                if (getOrdersCurRow() >= 0) {
                    currentOrderID = ((Order) mTradeDesk.getOrders().get(getOrdersCurRow())).getOrderID();
                }
                if (updatedOrderID.equals(currentOrderID)) {
                    Rate rate = mTradeDesk.getRates().getRate(order.getCurrency());
                    if (rate != null) {
                        mEnabled = rate.isTradable() && order.isEntryOrder();
                        checkActionEnabled();
                    }
                }
            }
        }
    }

    /**
     * Is called when the orders table hasn't been added
     */
    private void setInitialEnable() {
        try {
            Orders orders = mTradeDesk.getOrders();
            Rates rates = mTradeDesk.getRates();
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
                Orders orders = mTradeDesk.getOrders();
                if (orders == null || orders.size() <= aIndex) {
                    return;
                }
                Order order = (Order) orders.get(aIndex);
                Rates rates = mTradeDesk.getRates();
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
     * @param aIOrdersCurRow stores the index of the current row in the Orders table. It is changed
     */
    private void setOrdersCurRow(int aIOrdersCurRow) {
        mOrdersCurRow = aIOrdersCurRow;
    }

    /**
     * Inner class instances implements Actions which are added to
     * user interface controls.
     */
    private class EntryOrderAction extends AbstractAction {
        /**
         * Invoked when an action occurs
         */
        public void actionPerformed(ActionEvent aEvent) {
            UpdateEntryOrderAction.this.actionPerformed(aEvent);
        }
    }

    /**
     * This class implements listener from the selection of orders table row.
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
     * Implementation of Table Listener interface.
     */
    private class TableListener implements ITableListener {
        /**
         * Instance implementing the interface ITableSelectionListener. It watches changing value of
         * current Order
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
