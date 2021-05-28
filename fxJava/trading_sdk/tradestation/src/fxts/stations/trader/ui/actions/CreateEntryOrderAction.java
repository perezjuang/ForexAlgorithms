/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/CreateEntryOrderAction.java#2 $
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
import fxts.stations.core.Accounts;
import fxts.stations.core.ActionManager;
import fxts.stations.core.Rates;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.MessageBoxRunnable;
import fxts.stations.trader.ui.dialogs.CreateEntryOrderDialog;
import fxts.stations.trader.ui.frames.AccountsFrame;
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;

/**
 * This action is used to handle action for creating of entry order.
 * <br>
 * The instance of that class should be created after initialization
 * of core component, TradeDesk especially, but before creating of ITable instances
 */
public class CreateEntryOrderAction extends ATradeAction implements ISignalListener {
    private static final String ACTION_NAME = "CreateEntryOrderAction";
    /**
     * Instance.
     */
    private static final CreateEntryOrderAction CREATE_ENTRY_ORDER_ACTION = new CreateEntryOrderAction();
    /**
     * Account. Is determined by the current account in Accounts table (if registered).
     * If table is not registered (or no such account) then it's first account
     * (may be under margin call!);
     */
    private String mAccountID;
    /**
     * Stores the index of the current row in the Accounts table. It is changed
     * in AccountSelectionListener.onTableChangeSelection method
     */
    private int mAccountsCurRow;
    /**
     * Flag of enabling action that is set by Action manager:
     */
    private boolean mCanAct;

    /**
     * Dialog
     */
    private CreateEntryOrderDialog mDlg;
    /**
     * Flag of enabling action The action is enabled when:
     * 1) there is account not under margin call;
     * 2) if Rates table is registered:
     * a) there is current rate;
     * b) it's tradable
     * OR if rates table is not registered:
     * a) there is at least one rate with trade available;
     */
    private boolean mEnabled;
    /**
     * Inner Actions
     */
    private final WeakListener<Action> mInnerActions = new WeakListener<Action>();

    /**
     * Constructor of Create Entry Order action.
     * Note: It adds action to ActionManager, a creator shouldn't take care of adding and removing it.
     */
    private CreateEntryOrderAction() {
        super(ACTION_NAME);
        try {
            ActionManager.getInst().add(this);
            TableManager.getInst().addListener(new TableListener());
            //TradeDesk.getInst().getAccounts().subscribe(accountsSignalListener, SignalType.ADD);
            TradeDesk.getInst().getAccounts().subscribe(this, SignalType.CHANGE);
            //TradeDesk.getInst().getAccounts().subscribe(accountsSignalListener, SignalType.REMOVE);

            //TradeDesk.getInst().getRates().subscribe(ratesSignalListener, SignalType.ADD);
            TradeDesk.getInst().getRates().subscribe(this, SignalType.CHANGE);
            //TradeDesk.getInst().getRates().subscribe(ratesSignalListener, SignalType.REMOVE);
            setInitialEnable();
            setAccountByIndex(0);
            checkActionEnabled();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Invoked when an action occurs
     */
    public void actionPerformed(ActionEvent aEvent) {
        Rate rate = TradeDesk.getInst().getRates().getRate(TradeApp.getInst().getRatesFrame().getSelectedCurrency());
        if (rate != null && rate.isTradable()) {
            mEnabled = rate.isTradable();
            checkActionEnabled();
            if (mEnabled && mCanAct) {
                mDlg = new CreateEntryOrderDialog(TradeApp.getInst().getMainFrame());
                if (mAccountID == null) {
                    try {
                        Accounts accounts = TradeDesk.getInst().getAccounts();
                        if (!accounts.isEmpty()) {
                            mAccountID = ((Account) accounts.get(0)).getAccount();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                mDlg.setAccount(mAccountID);
                mDlg.setCurrency(TradeApp.getInst().getRatesFrame().getSelectedCurrency());
                if (Side.SELL.getName().equals(aEvent.getActionCommand())) {
                    mDlg.setSide(Side.SELL);
                } else {
                    mDlg.setSide(Side.BUY);
                }
                int res = TradeApp.getInst().getMainFrame().showDialog(mDlg);
                if (res == JOptionPane.OK_OPTION) {
                    // Send Request
                    Liaison liaison = Liaison.getInstance();
                    IRequestFactory requestFactory = liaison.getRequestFactory();
                    IRequest request = requestFactory.createEntryOrder(mDlg.getCurrency(),
                                                                       mDlg.getAccount(),
                                                                       mDlg.getSide(),
                                                                       mDlg.getRate(),
                                                                       mDlg.getAmount(),
                                                                       mDlg.getCustomText());
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
     * Sets Flag of enabling action.Called by Action manager.
     */
    @Override
    public void canAct(boolean aCanAct) {
        mCanAct = aCanAct;
        checkActionEnabled();
        if (mDlg != null) {
            mDlg.enableDialog(aCanAct);
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
     * Retuns Inner class instances implements Actions which are added to user interface controls
     */
    public static Action newAction(String aCommandString) {
        Action action = CREATE_ENTRY_ORDER_ACTION.new EntryOrderAction();
        if (aCommandString != null) {
            action.putValue(ACTION_COMMAND_KEY, aCommandString);
        }
        CREATE_ENTRY_ORDER_ACTION.mInnerActions.add(action);
        CREATE_ENTRY_ORDER_ACTION.checkActionEnabled();
        return action;
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSrc instanceof Accounts) {
            try {
                setAccountByIndex(mAccountsCurRow);
                checkActionEnabled();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (aSrc instanceof Rates) {
            if (aSignal.getType() == SignalType.CHANGE) {
                checkActionEnabled();
            }
        }
    }

    /**
     * Sets selected account by index.
     */
    private void setAccountByIndex(int aIndex) {
        try {
            Accounts accounts = TradeDesk.getInst().getAccounts();
            if (aIndex < 0) {
                mAccountID = null;
            } else {
                if (accounts != null
                    && accounts.size() > aIndex) {
                    Account account = (Account) accounts.get(aIndex);
                    if (account != null) {
                        mAccountID = account.getAccount();
                    }
                }
            }
            if (mEnabled) {
                mEnabled = false;
                for (int i = 0; i < accounts.size(); i++) {
                    if (!((Account) accounts.get(i)).isUnderMarginCall()) {
                        mEnabled = true;
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setEnabled(boolean aEnabled) {
        if (TradingServerSession.getInstance().getUserKind() != IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
            super.setEnabled(aEnabled);
            mEnabled = aEnabled;
            checkActionEnabled();
        }
    }

    /**
     * Is called when the currency table hasn't been added.
     */
    private void setInitialEnable() {
        try {
            Rates rates = TradeDesk.getInst().getRates();
            if (rates == null) {
                mEnabled = false;
                return;
            }
            if (mEnabled) {
                mEnabled = false;
                for (int i = 0; i < rates.size(); i++) {
                    if (((Rate) rates.get(i)).isTradable()) {
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
     * This class implements listener from the selection of accounts table row.
     */
    private class AccountSelectionListener implements ITableSelectionListener {
        /**
         * This method is called when selection is changed.
         *
         * @param aTable table which row was changed
         * @param aRow changed row
         */
        public void onTableChangeSelection(ITable aTable, int[] aRow) {
            if (aRow.length > 0) {
                mAccountsCurRow = aRow[0];
                setAccountByIndex(aRow[0]);
            } else {
                mAccountsCurRow = -1;
                setAccountByIndex(-1);
            }
            checkActionEnabled();
        }
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
            CreateEntryOrderAction.this.actionPerformed(aEvent);
        }
    }

    /**
     * Implementation of Table Listener interface.
     */
    private class TableListener implements ITableListener {
        /**
         * Instance implementing the interface ITableSelectionListener. It watches changing value of
         * current Account
         */
        private ITableSelectionListener mAccountsTableListener;

        /**
         * This method is called when new table is added.
         *
         * @param aTable table that was added
         */
        public void onAddTable(ITable aTable) {
            try {
                String sTableName = aTable.getName();
                if (AccountsFrame.NAME.equals(sTableName) && mAccountsTableListener == null) {
                    mAccountsCurRow = 0;
                    mAccountsTableListener = new AccountSelectionListener();
                    aTable.addSelectionListener(mAccountsTableListener);
                    setAccountByIndex(0);
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
                if (AccountsFrame.NAME.equals(aTable.getName())) {
                    mAccountsCurRow = -1;
                    ITableSelectionListener tmp = mAccountsTableListener;
                    mAccountsTableListener = null;
                    if (tmp != null) {
                        aTable.removeSelectionListener(tmp);
                    }
                    setAccountByIndex(0);
                    checkActionEnabled();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
