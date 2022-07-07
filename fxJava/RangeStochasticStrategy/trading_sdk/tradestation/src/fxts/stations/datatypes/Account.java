/*
 * $Header:$
 *
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
 * $History: $
 *
 * 08/28/2003   ID   Created
 * 12/1/2004    Andre   added accountNumber property
 */
package fxts.stations.datatypes;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;

import java.text.DecimalFormat;

/**
 * Represents trader account.
 */
public class Account implements IKey {
    private static final DecimalFormat NUMBER_FORMATTER = new ThreadSafeNumberFormat().getInstance();
    /**
     * account number
     */
    private String mAccount;
    /**
     * Account id
     */
    private String mAccountID;
    /**
     * Balance of the account
     */
    private double mBalance;
    /**
     * Base Unit Size
     */
    private double mBaseUnitSize;
    /**
     * Is part of batch
     */
    private boolean mBatch;
    /**
     * Equity of the account
     */
    private double mEquity;
    /**
     * Gross Profit-Loss
     */
    private double mGrossPnL;
    /**
     * Position Maintenance - Y|N|0 Yes|No|Netting
     */
    private String mHedging;
    private boolean mInvisible;
    /**
     * Is the account under margin call?
     */
    private boolean mIsUnderMarginCall;
    /**
     * Is last report in batch
     */
    private boolean mLastRptRequested;
    /**
     * is account locked?
     */
    private boolean mLocked;
    /**
     * Margin requirement
     */
    private double mMarginReq;
    private double mStartCash;
    /**
     * Usable margin
     */
    private double mUsableMargin;
    /**
     * Used margin
     */
    private double mUsedMargin;

    /**
     * @return account number
     */
    public String getAccount() {
        return mAccount;
    }

    /**
     * @param aAccount
     */
    public void setAccount(String aAccount) {
        mAccount = aAccount;
    }

    /**
     * Gets account ID.
     *
     * @return account ID.
     */
    public String getAccountID() {
        return mAccountID;
    }

    /**
     * Sets account ID.
     *
     * @param asAccountID account ID.
     */
    public void setAccountID(String asAccountID) {
        if (asAccountID != null) {
            mAccountID = asAccountID;
        }
    }

    /**
     * Gets balance of the account.
     *
     * @return balance of the account.
     */
    public double getBalance() {
        return mBalance;
    }

    /**
     * Sets balance of the account.
     *
     * @param aBalance balance of the account.
     */
    public void setBalance(double aBalance) {
        mBalance = aBalance;
    }

    public double getBaseUnitSize() {
        return mBaseUnitSize;
    }

    public void setBaseUnitSize(double aBaseUnitSize) {
        mBaseUnitSize = aBaseUnitSize;
    }

    /**
     * Gets equity of the account.
     *
     * @return equity of the account.
     */
    public double getEquity() {
        return mEquity;
    }

    /**
     * Sets equity of the account.
     *
     * @param aEquity equity of the account.
     */
    public void setEquity(double aEquity) {
        mEquity = aEquity;
    }

    /**
     * Gets gross Profit-Loss.
     *
     * @return gross Profit-Loss.
     */
    public double getGrossPnL() {
        return mGrossPnL;
    }

    /**
     * Sets gross Profit-Loss.
     *
     * @param aGrossPnL gross Profit-Loss.
     */
    public void setGrossPnL(double aGrossPnL) {
        mGrossPnL = aGrossPnL;
    }

    public String getHedging() {
        return mHedging;
    }

    public void setHedging(String aHedging) {
        mHedging = aHedging;
    }

    public Object getKey() {
        return mAccount;
    }

    /**
     * Gets margin requirement.
     *
     * @return margin requirement.
     */
    public double getMarginReq() {
        return mMarginReq;
    }

    /**
     * Sets margin requirement.
     *
     * @param aMarginReq margin requirement.
     */
    public void setMarginReq(double aMarginReq) {
        mMarginReq = aMarginReq;
    }

    public double getStartCash() {
        return mStartCash;
    }

    public void setStartCash(double aStartCash) {
        mStartCash = aStartCash;
    }

    /**
     * Gets usable margin.
     *
     * @return usable margin.
     */
    public double getUsableMargin() {
        return mUsableMargin;
    }

    /**
     * Sets usable margin.
     *
     * @param aUsableMargin usable margin.
     */
    public void setUsableMargin(double aUsableMargin) {
        mUsableMargin = aUsableMargin;
    }

    /**
     * Gets used margin.
     *
     * @return used margin.
     */
    public double getUsedMargin() {
        return mUsedMargin;
    }

    /**
     * Sets used margin.
     *
     * @param aUsedMargin used margin.
     */
    public void setUsedMargin(double aUsedMargin) {
        mUsedMargin = aUsedMargin;
    }

    /**
     * Is part of batch
     *
     * @return
     */
    public boolean isBatch() {
        return mBatch;
    }

    /**
     * Is part of batch
     *
     * @param aBatch
     */
    public void setBatch(boolean aBatch) {
        mBatch = aBatch;
    }

    public boolean isInvisible() {
        return mInvisible;
    }

    public void setInvisible(boolean aInvisible) {
        mInvisible = aInvisible;
    }

    /**
     * Under Mrgn
     */
    public boolean isIsUnderMarginCall() {
        return mIsUnderMarginCall;
    }

    /**
     * Is last report in batch
     */
    public boolean isLastRptRequested() {
        return mLastRptRequested;
    }

    /**
     * is account locked
     */
    public boolean isLocked() {
        return mLocked;
    }

    /**
     * lock account
     *
     * @param aLocked
     */
    public void setLocked(boolean aLocked) {
        mLocked = aLocked;
    }

    /**
     * Gets whether the account under margin call.
     *
     * @return true is the account under margin call, false otherwise.
     */
    public boolean isUnderMarginCall() {
        return mIsUnderMarginCall;
    }

    /**
     * Sets whether the account under margin call.
     *
     * @param abIsUnderMarginCall whether the account under margin call.
     */
    public void setUnderMarginCall(boolean abIsUnderMarginCall) {
        mIsUnderMarginCall = abIsUnderMarginCall;
    }

    /**
     * @param aLastRptRequested
     */
    public void setLast(boolean aLastRptRequested) {
        mLastRptRequested = aLastRptRequested;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Account");
        sb.append("{mAccount='").append(mAccount).append('\'');
        sb.append(", mAccountID='").append(mAccountID).append('\'');
        sb.append(", mBalance=").append(NUMBER_FORMATTER.format(mBalance));
        sb.append(", mBaseUnitSize=").append(NUMBER_FORMATTER.format(mBaseUnitSize));
        sb.append(", mBatch=").append(mBatch);
        sb.append(", mEquity=").append(NUMBER_FORMATTER.format(mEquity));
        sb.append(", mGrossPnL=").append(NUMBER_FORMATTER.format(mGrossPnL));
        sb.append(", mHedging='").append(mHedging).append('\'');
        sb.append(", mIsUnderMarginCall=").append(mIsUnderMarginCall);
        sb.append(", mLastRptRequested=").append(mLastRptRequested);
        sb.append(", mLocked=").append(mLocked);
        sb.append(", mMarginReq=").append(mMarginReq);
        sb.append(", mUsableMargin=").append(mUsableMargin);
        sb.append(", mUsedMargin=").append(mUsedMargin);
        sb.append(", mVisible=").append(mInvisible);
        sb.append('}');
        return sb.toString();
    }
}
