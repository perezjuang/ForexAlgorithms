/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/Accounts.java#1 $
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
package fxts.stations.core;

import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Position;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.signals.AddSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of Account objects.
 */
public class Accounts extends ABusinessTable implements ISignalListener {
    private final List<Account> mInvisbleAccounts = new ArrayList<Account>();
    private Map<String, Account> mInvisKeyMap = new HashMap<String, Account>();
    private double mTotalBalance;
    private double mTotalEquity;
    private double mTotalGrossPnL;
    private double mTotalUsableMargin;
    private double mTotalUsedMargin;

    /**
     * overrides method of SignalVector because the calculating of PnL should be made
     */
    @Override
    public void add(Object aObj) {
        Account acc = (Account) aObj;
        if (acc.isInvisible()) {
            mInvisbleAccounts.add(acc);
            mInvisKeyMap.put(acc.getAccount(), acc);
            notify(new AddSignal(0, null));
        } else {
            super.add(aObj);
            update(acc);
        }
    }

    @Override
    public void clear() {
        mInvisbleAccounts.clear();
        mInvisKeyMap.clear();
        super.clear();
        updateTotals();
    }

    /**
     * This method enables/disables recalculate business data mode for the table.
     * It must be overridden in subclasses.
     */
    @Override
    public void enableRecalc(boolean abEnable) {
        if (abEnable) {
            TradeDesk.getInst().getAccounts().subscribe(this, SignalType.ADD);
            TradeDesk.getInst().getAccounts().subscribe(this, SignalType.CHANGE);
            TradeDesk.getInst().getAccounts().subscribe(this, SignalType.REMOVE);
        } else {
            TradeDesk.getInst().getAccounts().unsubscribe(this, SignalType.ADD);
            TradeDesk.getInst().getAccounts().unsubscribe(this, SignalType.CHANGE);
            TradeDesk.getInst().getAccounts().unsubscribe(this, SignalType.REMOVE);
        }
    }

    /**
     * Finds the account by account id, returns null if not found.
     */
    public Account getAccount(String asAccount) {
        Account account = (Account) get(asAccount);
        if (account == null) {
            account = mInvisKeyMap.get(asAccount);
        }
        return account;
    }

    public List<Account> getInvisbleAccounts() {
        return Collections.unmodifiableList(mInvisbleAccounts);
    }

    public double getTotalBalance() {
        return mTotalBalance;
    }

    public void setTotalBalance(double aTotalBalance) {
        mTotalBalance = aTotalBalance;
    }

    public double getTotalEquity() {
        return mTotalEquity;
    }

    public void setTotalEquity(double aTotalEquity) {
        mTotalEquity = aTotalEquity;
    }

    public double getTotalGrossPnL() {
        return mTotalGrossPnL;
    }

    public void setTotalGrossPnL(double aTotalGrossPnL) {
        mTotalGrossPnL = aTotalGrossPnL;
    }

    public double getTotalUsableMargin() {
        return mTotalUsableMargin;
    }

    public void setTotalUsableMargin(double aTotalUsableMargin) {
        mTotalUsableMargin = aTotalUsableMargin;
    }

    public double getTotalUsedMargin() {
        return mTotalUsedMargin;
    }

    public void setTotalUsedMargin(double aTotalUsedMargin) {
        mTotalUsedMargin = aTotalUsedMargin;
    }

    public void onSignal(Signaler aSrc, ISignal aSignal) {
        updateTotals();
    }

    /**
     * overrides method of SignalVector because the calculating of PnL should be made
     */
    @Override
    public Object set(int aIndex, Object aObj) {
        Object o = super.set(aIndex, aObj);
        if (aObj instanceof Account) {
            update((Account) aObj);
        }
        return o;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Accounts");
        sb.append(" --- begin superclass toString ");
        sb.append(super.toString());
        sb.append(" --- end superclass toString ");
        sb.append("{mTotalBalance=").append(mTotalBalance);
        sb.append(", mTotalEquity=").append(mTotalEquity);
        sb.append(", mTotalGrossPnL=").append(mTotalGrossPnL);
        sb.append(", mTotalUsableMargin=").append(mTotalUsableMargin);
        sb.append(", mTotalUsedMargin=").append(mTotalUsedMargin);
        sb.append('}');
        return sb.toString();
    }

    public void update(String aAccount) {
        update(getAccount(aAccount));
    }

    private Account update(Account aAccount) {
        OpenPositions positions = TradeDesk.getInst().getOpenPositions();
        double dblGrossPnLSum = 0.0;
        // calculating sum of all open positions for the account
        for (int i = 0; i < positions.size(); i++) {
            Position openPos = (Position) positions.get(i);
            if (aAccount.getAccount().equals(openPos.getAccount())) {
                dblGrossPnLSum += openPos.getGrossPnL();
            }
        }
        aAccount.setGrossPnL(dblGrossPnLSum);
        aAccount.setEquity(aAccount.getBalance() + dblGrossPnLSum);
        aAccount.setUsableMargin(aAccount.getEquity() - aAccount.getUsedMargin());
        updateTotals();
        elementChanged(indexOf(aAccount));
        return aAccount;
    }

    private void updateTotals() {
        double totalBalance = 0;
        double totalEquity = 0;
        double totalUsedMargin = 0;
        double totalGrossPnL = 0;
        double totalUsableMargin = 0;
        for (int i = 0; i < size(); i++) {
            Account account = (Account) get(i);
            totalBalance += account.getBalance();
            totalEquity += account.getEquity();
            totalUsedMargin += account.getUsedMargin();
            totalGrossPnL += account.getGrossPnL();
            totalUsableMargin += account.getUsableMargin();
        }
        mTotalBalance = totalBalance;
        mTotalEquity = totalEquity;
        mTotalUsedMargin = totalUsedMargin;
        mTotalGrossPnL = totalGrossPnL;
        mTotalUsableMargin = totalUsableMargin;
    }
}
