/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/datatypes/Position.java#1 $
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
 * 12/8/2004    Andre Mermegas: added stopOrderID/limitOrderID properties it makes sense for a
 *                              position to know and have access to this attribute about itself.
 */
package fxts.stations.datatypes;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Represents open position.
 */
public class Position implements IKey {
    private String mAccount;
    private String mAccountID;
    private long mAmount;
    private boolean mBatch;
    private double mClosePrice;
    private Date mCloseTime;
    private double mCommission;
    private String mCurrency;
    private boolean mCurrencyTradable;
    private String mCustomText;
    private String mCustomText2;
    private double mGrossPnL;
    private double mInterest;
    private boolean mIsBeingClosed;
    private boolean mLastRptRequested;
    private double mLimit;
    private String mLimitOrderID;
    private double mNetPnL;
    private double mOpenPrice;
    private Date mOpenTime;
    private double mPipPL;
    private Side mSide;
    private double mStop;
    private double mStopMove;
    private String mStopOrderID;
    private String mTicketID;
    private DecimalFormat mToStringFormatter = new ThreadSafeNumberFormat().getInstance();
    private double mTrailingRate;
    private int mTrailStop;
    private double mUsedMargin;

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String aAccount) {
        mAccount = aAccount;
    }

    /**
     * Gets account ID.
     */
    public String getAccountID() {
        return mAccountID;
    }

    /**
     * Gets contract size in contract size units.
     */
    public long getAmount() {
        return mAmount;
    }

    /**
     * Sets contract size in contract size units.
     */
    public void setAmount(long alAmount) {
        mAmount = alAmount;
    }

    /**
     * Gets position close price.
     */
    public double getClosePrice() {
        return mClosePrice;
    }

    /**
     * Sets position close price.
     */
    public void setClosePrice(double adClosePrice) {
        mClosePrice = adClosePrice;
    }

    public Date getCloseTime() {
        return (Date) mCloseTime.clone();
    }

    /**
     * Gets commission.
     */
    public double getCommission() {
        return mCommission;
    }

    /**
     * Sets commission.
     */
    public void setCommission(double adCommission) {
        mCommission = adCommission;
    }

    /**
     * Gets currency pair in format CCY1/CCY2.
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * Custom text on this position
     */
    public String getCustomText() {
        return mCustomText;
    }

    /**
     * Set the custom text on this position
     *
     * @param aCustomText
     */
    public void setCustomText(String aCustomText) {
        mCustomText = aCustomText;
    }

    public String getCustomText2() {
        return mCustomText2;
    }

    public void setCustomText2(String aCustomText2) {
        mCustomText2 = aCustomText2;
    }

    /**
     * Gets gross Profit-Loss.
     */
    public double getGrossPnL() {
        return mGrossPnL;
    }

    /**
     * Sets gross Profit-Loss.
     */
    public void setGrossPnL(double adGrossPnL) {
        mGrossPnL = adGrossPnL;
    }

    /**
     * Gets interest.
     */
    public double getInterest() {
        return mInterest;
    }

    /**
     * Sets interest.
     */
    public void setInterest(double adInterest) {
        mInterest = adInterest;
    }

    public Object getKey() {
        return mTicketID;
    }

    /**
     * Gets limit value.
     */
    public double getLimit() {
        return mLimit;
    }

    /**
     * Sets limit value.
     */
    public void setLimit(double adLimit) {
        mLimit = adLimit;
    }

    public String getLimitOrderID() {
        return mLimitOrderID;
    }

    public void setLimitOrderID(String aLimitOrderID) {
        mLimitOrderID = aLimitOrderID;
    }

    /**
     * Gets Net Profit-Loss.
     */
    public double getNetPnL() {
        return mNetPnL;
    }

    /**
     * Sets Net Profit-Loss.
     */
    public void setNetPnL(double adNetPnL) {
        mNetPnL = adNetPnL;
    }

    /**
     * Gets position open price.
     */
    public double getOpenPrice() {
        return mOpenPrice;
    }

    /**
     * Sets position open price.
     */
    public void setOpenPrice(double adOpenPrice) {
        mOpenPrice = adOpenPrice;
    }

    /**
     * Gets position open time.
     */
    public Date getOpenTime() {
        return (Date) mOpenTime.clone();
    }

    public double getPipPL() {
        return mPipPL;
    }

    public void setPipPL(double aPipPL) {
        mPipPL = aPipPL;
    }

    /**
     * Gets side of the order (buy or sell).
     */
    public Side getSide() {
        return mSide;
    }

    /**
     * Gets stop-loss value.
     */
    public double getStop() {
        return mStop;
    }

    /**
     * Sets stop-loss value.
     */
    public void setStop(double adStop) {
        mStop = adStop;
    }

    public double getStopMove() {
        return mStopMove;
    }

    public void setStopMove(double aStopMove) {
        mStopMove = aStopMove;
    }

    public String getStopOrderID() {
        return mStopOrderID;
    }

    public void setStopOrderID(String aStopOrderID) {
        mStopOrderID = aStopOrderID;
    }

    /**
     * Gets ticket (open position) id.
     */
    public String getTicketID() {
        return mTicketID;
    }

    public double getTrailingRate() {
        return mTrailingRate;
    }

    public void setTrailingRate(double aTrailingRate) {
        mTrailingRate = aTrailingRate;
    }

    public int getTrailStop() {
        return mTrailStop;
    }

    public void setTrailStop(int aTrailStop) {
        mTrailStop = aTrailStop;
    }

    public double getUsedMargin() {
        return mUsedMargin;
    }

    public void setUsedMargin(double aUsedMargin) {
        mUsedMargin = aUsedMargin;
    }

    public boolean isBatch() {
        return mBatch;
    }

    public void setBatch(boolean aBatch) {
        mBatch = aBatch;
    }

    /**
     * Gets whether this position is closing now.
     */
    public boolean isBeingClosed() {
        return mIsBeingClosed;
    }

    /**
     * Returns currency tradable
     */
    public boolean isCurrencyTradable() {
        return mCurrencyTradable;
    }

    /**
     * Sets currency tradable
     */
    public void setCurrencyTradable(boolean abCurrencyTradable) {
        mCurrencyTradable = abCurrencyTradable;
    }

    public boolean isLastRptRequested() {
        return mLastRptRequested;
    }

    /**
     * Sets account ID.
     */
    public void setAccountID(String asAccountID) {
        if (asAccountID != null) {
            mAccountID = asAccountID;
        }
    }

    public void setCloseTime(Date aCloseTime) {
        mCloseTime = (Date) aCloseTime.clone();
    }

    /**
     * Sets currency pair in format CCY1/CCY2.
     */
    public void setCurrency(String asCurrency) {
        if (asCurrency != null) {
            mCurrency = asCurrency;
        }
    }

    /**
     * Sets whether this position is closing now.
     */
    public void setIsBeingClosed(boolean abIsBeingClosed) {
        mIsBeingClosed = abIsBeingClosed;
    }

    public void setLast(boolean aLastRptRequested) {
        mLastRptRequested = aLastRptRequested;
    }

    /**
     * Sets position open time.
     */
    public void setOpenTime(Date adtOpenTime) {
        if (adtOpenTime != null) {
            mOpenTime = (Date) adtOpenTime.clone();
        }
    }

    /**
     * Sets side of the order (buy or sell).
     */
    public void setSide(Side aSide) {
        if (aSide != null) {
            mSide = aSide;
        }
    }

    /**
     * Sets ticket (open position) id.
     */
    public void setTicketID(String asTicketID) {
        if (asTicketID != null) {
            mTicketID = asTicketID;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Position");
        sb.append("{mAccountID='").append(mAccountID).append('\'');
        sb.append(", mAccount='").append(mAccount).append('\'');
        sb.append(", mAmount=").append(mToStringFormatter.format(mAmount));
        sb.append(", mBatch=").append(mBatch);
        sb.append(", mClosePrice=").append(mToStringFormatter.format(mClosePrice));
        sb.append(", mCloseTime=").append(mCloseTime);
        sb.append(", mCommission=").append(mToStringFormatter.format(mCommission));
        sb.append(", mCurrency='").append(mCurrency).append('\'');
        sb.append(", mCurrencyTradable=").append(mCurrencyTradable);
        sb.append(", mCustomText='").append(mCustomText).append('\'');
        sb.append(", mCustomText2='").append(mCustomText2).append('\'');
        sb.append(", mGrossPnL=").append(mToStringFormatter.format(mGrossPnL));
        sb.append(", mInterest=").append(mToStringFormatter.format(mInterest));
        sb.append(", mIsBeingClosed=").append(mIsBeingClosed);
        sb.append(", mLastRptRequested=").append(mLastRptRequested);
        sb.append(", mLimit=").append(mToStringFormatter.format(mLimit));
        sb.append(", mLimitOrderID='").append(mLimitOrderID).append('\'');
        sb.append(", mNetPnL=").append(mToStringFormatter.format(mNetPnL));
        sb.append(", mOpenPrice=").append(mToStringFormatter.format(mOpenPrice));
        sb.append(", mOpenTime=").append(mOpenTime);
        sb.append(", mPipPL=").append(mToStringFormatter.format(mPipPL));
        sb.append(", mSide=").append(mSide);
        sb.append(", mStop=").append(mToStringFormatter.format(mStop));
        sb.append(", mStopMove=").append(mToStringFormatter.format(mStopMove));
        sb.append(", mStopOrderID='").append(mStopOrderID).append('\'');
        sb.append(", mTicketID='").append(mTicketID).append('\'');
        sb.append(", mTrailStop=").append(mToStringFormatter.format(mTrailStop));
        sb.append(", mTrailingRate=").append(mToStringFormatter.format(mTrailingRate));
        sb.append(", mUsedMargin=").append(mToStringFormatter.format(mUsedMargin));
        sb.append('}');
        return sb.toString();
    }
}