/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/datatypes/Order.java#2 $
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
 * 12/2/2004    Andre   added an orderType field
 * 12/8/2004    Andre Mermegas  -- added stage/stop/limit/tradeid properties
 */
package fxts.stations.datatypes;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Represents trade order.
 */
public class Order implements IKey {
    private String mAccount;
    /**
     * Account id
     */
    private String mAccountID;
    /**
     * Currency pair
     */
    private String mCurrency;
    /**
     * Currency tradable
     */
    private boolean mCurrencyTradable;
    private String mCustomText;
    /**
     * Limit value
     */
    private double mdLimit;
    /**
     * Current offer rate for the currency
     */
    private double mdOfferRate;
    /**
     * Rate of the order
     */
    private double mdOrderRate;
    /**
     * Stop-loss value
     */
    private double mdStop;
    /**
     * Time when the order was created
     */
    private Date mdtTime;
    /**
     * Contract size in contract size units
     */
    private long mlAmount;
    private boolean mLimit;
    private String mLimitOrderID;
    /**
     * Order id
     */
    private String mOrderID;
    /**
     * order type
     */
    private String mOrderType;
    /**
     * Side of the order (buy or sell)
     */
    private Side mSide;
    private char mStage;
    /**
     * Status of the order
     */
    private String mStatus;
    private boolean mStop;
    private String mStopOrderID;
    private DecimalFormat mToStringFormatter = new ThreadSafeNumberFormat().getInstance();
    private String mTradeId;
    private boolean mTrailingStop;
    private int mTrailStop;
    /**
     * Type of the order
     */
    private String mType;

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String accountName) {
        mAccount = accountName;
    }

    /**
     * Gets account ID.
     *
     * @return accountid
     */
    public String getAccountID() {
        return mAccountID;
    }

    /**
     * Gets contract size in contract size units.
     *
     * @return amt
     */
    public long getAmount() {
        return mlAmount;
    }

    /**
     * Gets currency pair in format CCY1/CCY2.
     *
     * @return ccy
     */
    public String getCurrency() {
        return mCurrency;
    }

    public String getCustomText() {
        return mCustomText;
    }

    public void setCustomText(String aCustomText) {
        mCustomText = aCustomText;
    }

    public Object getKey() {
        return mOrderID;
    }

    public String getLimitOrderID() {
        return mLimitOrderID;
    }

    public void setLimitOrderID(String aLimitOrderID) {
        mLimitOrderID = aLimitOrderID;
    }

    /**
     * Gets mLimit value.
     *
     * @return limit
     */
    public double getLimitRate() {
        return mdLimit;
    }

    /**
     * Gets current offer rate for the currency.
     *
     * @return offer
     */
    public double getOfferRate() {
        return mdOfferRate;
    }

    /**
     * Gets order ID.
     *
     * @return orderid
     */
    public String getOrderID() {
        return mOrderID;
    }

    /**
     * Gets rate of the order.
     *
     * @return orderrate
     */
    public double getOrderRate() {
        return mdOrderRate;
    }

    public String getOrdType() {
        return mOrderType;
    }

    /**
     * Gets side of the order (buy or sell).
     *
     * @return side
     */
    public Side getSide() {
        return mSide;
    }

    public char getStage() {
        return mStage;
    }

    public void setStage(char aStage) {
        mStage = aStage;
    }

    /**
     * Gets status of the order.
     *
     * @return status
     */
    public String getStatus() {
        return mStatus;
    }

    public String getStopOrderID() {
        return mStopOrderID;
    }

    public void setStopOrderID(String aStopOrderID) {
        mStopOrderID = aStopOrderID;
    }

    /**
     * Gets mStop-loss value.
     *
     * @return stoprate
     */
    public double getStopRate() {
        return mdStop;
    }

    /**
     * Gets time when the order was created.
     *
     * @return time
     */
    public Date getTime() {
        return (Date) mdtTime.clone();
    }

    public String getTradeId() {
        return mTradeId;
    }

    public void setTradeId(String aTradeId) {
        mTradeId = aTradeId;
    }

    public int getTrailStop() {
        return mTrailStop;
    }

    public void setTrailStop(int aTrailStop) {
        mTrailStop = aTrailStop;
    }

    /**
     * Gets type of the order.
     *
     * @return type
     */
    public String getType() {
        return mType;
    }

    public boolean isChangeable() {
        return "Waiting".equalsIgnoreCase(mStatus);
    }

    /**
     * Returns currency tradable
     *
     * @return is ccy tradable
     */
    public boolean isCurrencyTradable() {
        return mCurrencyTradable;
    }

    /**
     * Sets currency tradable
     *
     * @param abCurrencyTradable tradeable
     */
    public void setCurrencyTradable(boolean abCurrencyTradable) {
        mCurrencyTradable = abCurrencyTradable;
    }

    /**
     * Is it a entry order?
     *
     * @return isentry
     */
    public boolean isEntryOrder() {
        return "SE".equalsIgnoreCase(mType)
               || "LE".equalsIgnoreCase(mType)
               || "STE".equalsIgnoreCase(mType)
               || "LTE".equalsIgnoreCase(mType);
    }

    public boolean isLimit() {
        return mLimit;
    }

    public void setLimit(boolean aLimit) {
        mLimit = aLimit;
    }

    public boolean isStop() {
        return mStop;
    }

    public void setStop(boolean aStop) {
        mStop = aStop;
    }

    public boolean isTrailingStop() {
        return mTrailingStop;
    }

    public void setTrailingStop(boolean aTrailingStop) {
        mTrailingStop = aTrailingStop;
    }

    /**
     * Sets account ID.
     *
     * @param asAccountID acctid
     */
    public void setAccountID(String asAccountID) {
        if (asAccountID != null) {
            mAccountID = asAccountID;
        }
    }

    /**
     * Sets contract size in contract size units.
     *
     * @param alAmount amt
     */
    public void setAmount(long alAmount) {
        mlAmount = alAmount;
    }

    /**
     * Sets currency pair in format CCY1/CCY2.
     *
     * @param asCurrency ccy
     */
    public void setCurrency(String asCurrency) {
        if (asCurrency != null) {
            mCurrency = asCurrency;
        }
    }

    /**
     * Sets mLimit value.
     *
     * @param adLimit limitrate
     */
    public void setLimitRate(double adLimit) {
        mdLimit = adLimit;
    }

    /**
     * Sets current offer rate for the currency.
     *
     * @param adOfferRate offerrate
     */
    public void setOfferRate(double adOfferRate) {
        mdOfferRate = adOfferRate;
    }

    /**
     * Sets order ID.
     *
     * @param asOrderID id
     */
    public void setOrderID(String asOrderID) {
        if (asOrderID != null) {
            mOrderID = asOrderID;
        }
    }

    /**
     * Sets rate of the order.
     *
     * @param adOrderRate rate
     */
    public void setOrderRate(double adOrderRate) {
        mdOrderRate = adOrderRate;
    }

    public void setOrdType(String aMcOrderType) {
        mOrderType = aMcOrderType;
    }

    /**
     * Sets side of the order (buy or sell).
     *
     * @param aSide side
     */
    public void setSide(Side aSide) {
        if (aSide != null) {
            mSide = aSide;
        }
    }

    /**
     * Sets status of the order.
     *
     * @param asStatus status
     */
    public void setStatus(String asStatus) {
        if (asStatus != null) {
            mStatus = asStatus;
        }
    }

    /**
     * Sets mStop-loss value.
     *
     * @param adStop stoprate
     */
    public void setStopRate(double adStop) {
        mdStop = adStop;
    }

    /**
     * Sets time when the order was created.
     *
     * @param adtTime time
     */
    public void setTime(Date adtTime) {
        if (adtTime != null) {
            mdtTime = (Date) adtTime.clone();
        }
    }

    /**
     * Sets type of the order.
     *
     * @param asType type
     */
    public void setType(String asType) {
        if (asType != null) {
            mType = asType;
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Order");
        sb.append("{mAccount='").append(mAccount).append('\'');
        sb.append(", mCurrencyTradable=").append(mCurrencyTradable);
        sb.append(", mCustomText='").append(mCustomText).append('\'');
        sb.append(", mLimit=").append(mLimit);
        sb.append(", mLimitOrderID='").append(mLimitOrderID).append('\'');
        sb.append(", mSide=").append(mSide);
        sb.append(", mStage=").append(mStage);
        sb.append(", mStop=").append(mStop);
        sb.append(", mStopOrderID='").append(mStopOrderID).append('\'');
        sb.append(", mTradeId='").append(mTradeId).append('\'');
        sb.append(", mTrailStop=").append(mToStringFormatter.format(mTrailStop));
        sb.append(", mTrailingStop=").append(mTrailingStop);
        sb.append(", mcOrderType='").append(mOrderType).append('\'');
        sb.append(", mdLimit=").append(mToStringFormatter.format(mdLimit));
        sb.append(", mdOfferRate=").append(mToStringFormatter.format(mdOfferRate));
        sb.append(", mdOrderRate=").append(mToStringFormatter.format(mdOrderRate));
        sb.append(", mdStop=").append(mToStringFormatter.format(mdStop));
        sb.append(", mdtTime=").append(mdtTime);
        sb.append(", mlAmount=").append(mToStringFormatter.format(mlAmount));
        sb.append(", msAccountID='").append(mAccountID).append('\'');
        sb.append(", msCurrency='").append(mCurrency).append('\'');
        sb.append(", msOrderID='").append(mOrderID).append('\'');
        sb.append(", msStatus='").append(mStatus).append('\'');
        sb.append(", msType='").append(mType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
