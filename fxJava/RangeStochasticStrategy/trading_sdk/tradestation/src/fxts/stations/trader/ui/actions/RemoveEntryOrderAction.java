/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/RemoveEntryOrderAction.java#2 $
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
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Order;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MessageBoxRunnable;
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
import fxts.stations.util.WeakListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This action is used to handle action for deleting of entry order.
 * <br>
 * The instance of that class should be created after initialization
 * of core component, TradeDesk especially, but before creating of ITable instances
 */
public class RemoveEntryOrderAction extends ATradeAction {
    private static final String ACTION_NAME = "RemoveEntryOrderAction";
    /**
     * Instance.
     */
    private static final RemoveEntryOrderAction REMOVE_ENTRY_ORDER_ACTION = new RemoveEntryOrderAction();

    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;
    /**
     * Flag of enabling action The action is enabled when:
     * 1)  if the Orders table is registered:
     * a) and some entry order is selected;
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
    private List<String> mOrderIDList;

    /**
     * Constructor of Update Entry Order action.
     * Note: It adds action to ActionManager, a creator shouldn't take care of adding and removing it.
     */
    private RemoveEntryOrderAction() {
        super(ACTION_NAME);
        try {
            ActionManager.getInst().add(this);
            TableManager.getInst().addListener(new TableListener());
            mEnabled = false;
            checkActionEnabled();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Invoked when an action occurs
     */
    public void actionPerformed(ActionEvent aEvent) {
        if (mEnabled && mCanAct && !TradeDesk.getInst().getOrders().isEmpty()) {
            if (!mOrderIDList.isEmpty()) {
                int result = JOptionPane.showConfirmDialog(TradeApp.getInst().getMainFrame(),
                                                           "Do you want to remove "
                                                           + mOrderIDList.size()
                                                           + " order(s): ?");
                if (result == JOptionPane.OK_OPTION) {
                    for (String orderID : mOrderIDList) {
                        Liaison liaison = Liaison.getInstance();
                        IRequestFactory requestFactory = liaison.getRequestFactory();
                        IRequest request = requestFactory.removeEntryOrder(orderID);
                        try {
                            liaison.sendRequest(request);
                        } catch (LiaisonException aEx) {
                            EventQueue.invokeLater(new MessageBoxRunnable(aEx));
                        }
                    }
                }
            }
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
     * Retuns Inner class instances implements Actions which are added to user interface controls.
     *
     * @param aCommandString command string of the action
     * @return action
     */
    public static Action newAction(String aCommandString) {
        Action action = REMOVE_ENTRY_ORDER_ACTION.new EntryOrderAction();
        if (aCommandString != null) {
            action.putValue(ACTION_COMMAND_KEY, aCommandString);
        }
        REMOVE_ENTRY_ORDER_ACTION.mInnerActions.add(action);
        REMOVE_ENTRY_ORDER_ACTION.checkActionEnabled();
        return action;
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
            RemoveEntryOrderAction.this.actionPerformed(aEvent);
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
                setOrderByIndex(aRow);
            } else {
                setOrderByIndex(null);
            }
            checkActionEnabled();
        }

        private void setOrderByIndex(int[] aIndex) {
            if (aIndex == null || aIndex.length < 0) {
                mOrderIDList = null;
                mEnabled = false;
            } else {
                try {
                    Orders orders = TradeDesk.getInst().getOrders();
                    mOrderIDList = new ArrayList<String>();
                    for (int pos : aIndex) {
                        if (pos >= 0) {
                            Order order = (Order) orders.get(pos);
                            mOrderIDList.add(order.getOrderID());
                            mEnabled = order.isEntryOrder() || order.isChangeable();
                        }
                    }
                } catch (Exception e) {
                    //
                }
            }
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
                    mOrdersTableListener = new OrderSelectionListener();
                    aTable.addSelectionListener(mOrdersTableListener);
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
                if (OrdersFrame.NAME.equals(sTableName)) {
                    ITableSelectionListener tmp = mOrdersTableListener;
                    mOrdersTableListener = null;
                    if (tmp != null) {
                        aTable.removeSelectionListener(tmp);
                    }
                    mEnabled = false;
                    checkActionEnabled();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
