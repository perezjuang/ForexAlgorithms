/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/datatypes/Rate.java#5 $
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
 * 07/18/2006   Andre Mermegas: performance update
 * 05/01/2007   Andre Mermegas: added MMR
 *
 */
package fxts.stations.datatypes;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Corresponds to the single offer rate for some currency pair.
 */
public class Rate implements IKey {
    /**
     *
     */
    private boolean mBuyBlocked;
    /**
     * Buy interest.
     */
    private double mBuyInterest;
    /**
     * Buy price Layer ID
     */
    private int mBuyLID;
    /**
     * Buy price ID
     */
    private long mBuyPID;
    /**
     * Buy price for the currency
     * (from trader point of view). The value defines how much units of CCY2 gives for 1 unit of CCY1 when it's buy.
     */
    private double mBuyPrice;
    /**
     * buy price is tradable
     */
    private boolean mBuyTradable;
    /**
     * Defines contract currency
     */
    private String mContractCurrency;
    /**
     *
     */
    private double mContractMultiplier;
    /**
     * Minimum contract size in units of contract currency for this currency pair.
     */
    private int mContractSize;
    /**
     * Currency pair in format CCY1/CCY2.
     */
    private String mCurrency = "";
    private double mFXCMCondDistEntryLimit;
    private double mFXCMCondDistEntryStop;
    private double mFXCMCondDistLimit;

    private double mFXCMCondDistStop;
    private double mFXCMMaxQuantity;
    private double mFXCMMinQuantity;
    private String mFXCMTradingStatus;
    /**
     * Maximum price value for some period of time (server defined).
     */
    private double mHighPrice;
    /**
     * ID of rate
     */
    private long mID;
    /**
     * Last time when the rate was created/updated.
     */
    private Date mLastDate;
    /**
     * Minimum price value for some period of time (server defined).
     */
    private double mLowPrice;
    /**
     * Old buy price. This property updated automatically when buy price is changed.
     */
    private double mOldBuyPrice;
    /**
     * Old sell price. This property updated automatically when sell price is changed.
     */
    private double mOldSellPrice;
    /**
     * Market opening Ask price
     */
    private double mOpenAsk;
    /**
     * Market opening Bid price
     */
    private double mOpenBid;
    /**
     * price per pip
     */
    private double mPipCost;
    /**
     * product
     */
    private int mProduct;
    /**
     * quote id
     */
    private String mQuoteID;
    /**
     *
     */
    private boolean mSellBocked;
    /**
     * Sell interest.
     */
    private double mSellInterest;
    /**
     * Sell price Layer ID
     */
    private int mSellLID;
    /**
     * Sell price ID
     */
    private long mSellPID;
    /**
     * Sell price for the currency
     * (from trader point of view). The value defines how much units of CCY2 are needed to buy 1 unit of CCY1.
     */
    private double mSellPrice;
    /**
     * is sell tradable
     */
    private boolean mSellTradable;
    /**
     * is rate subscribed on server
     */
    private boolean mSubscribed;

    private DecimalFormat mToStringFormatter = new ThreadSafeNumberFormat().getInstance();
    /**
     * Is operation is possible for this rate?
     */
    private boolean mTradable;

    /**
     * Gets buy interest.
     *
     * @return buy interest.
     */
    public double getBuyInterest() {
        return mBuyInterest;
    }

    /**
     * Sets buy interest.
     *
     * @param aBuyInterest buy interest.
     */
    public void setBuyInterest(double aBuyInterest) {
        mBuyInterest = aBuyInterest;
    }

    /**
     * Returns buy price layer id
     *
     * @return LID
     */
    public int getBuyLID() {
        return mBuyLID;
    }

    /**
     * Sets buy price Layer ID
     *
     * @param aBuyLID lid
     */
    public void setBuyLID(int aBuyLID) {
        mBuyLID = aBuyLID;
    }

    /**
     * Returns buy price id
     *
     * @return PID
     */
    public long getBuyPID() {
        return mBuyPID;
    }

    /**
     * Sets buy price ID
     *
     * @param aBuyPID id
     */
    public void setBuyPID(long aBuyPID) {
        mBuyPID = aBuyPID;
    }

    /**
     * Gets buy price for the currency.
     *
     * @return buy price for the currency (from trader point of view).
     */
    public double getBuyPrice() {
        return mBuyPrice;
    }

    /**
     * Sets buy price for the currency.
     *
     * @param aBuyPrice buy price for the currency (from trader point of view).
     */
    public void setBuyPrice(double aBuyPrice) {
        mOldBuyPrice = mBuyPrice;
        mBuyPrice = aBuyPrice;
    }

    /**
     * Gets contract currency
     *
     * @return CCY
     */
    public String getContractCurrency() {
        return mContractCurrency;
    }

    /**
     * Sets contract currency.
     *
     * @param aContractCurrency contract currency.
     */
    public void setContractCurrency(String aContractCurrency) {
        mContractCurrency = aContractCurrency;
    }

    public double getContractMultiplier() {
        return mContractMultiplier;
    }

    public void setContractMultiplier(double aContractMultiplier) {
        mContractMultiplier = aContractMultiplier;
    }

    /**
     * Gets minimum contract size in units of contract currency for this currency pair.
     *
     * @return minimum contract size in units of contract currency for this currency pair.
     */
    public int getContractSize() {
        return mContractSize;
    }

    /**
     * Sets minimum contract size in units of contract currency for this currency pair.
     *
     * @param aContractSize minimum contract size in units of contract currency for this currency pair.
     */
    public void setContractSize(int aContractSize) {
        mContractSize = aContractSize;
    }

    /**
     * Gets currency pair.
     *
     * @return currency pair in format CCY1/CCY2.
     */
    public String getCurrency() {
        return mCurrency;
    }

    public double getFXCMCondDistEntryLimit() {
        return mFXCMCondDistEntryLimit;
    }

    public void setFXCMCondDistEntryLimit(double aFXCMCondDistEntryLimit) {
        mFXCMCondDistEntryLimit = aFXCMCondDistEntryLimit;
    }

    public double getFXCMCondDistEntryStop() {
        return mFXCMCondDistEntryStop;
    }

    public void setFXCMCondDistEntryStop(double aFXCMCondDistEntryStop) {
        mFXCMCondDistEntryStop = aFXCMCondDistEntryStop;
    }

    public double getFXCMCondDistLimit() {
        return mFXCMCondDistLimit;
    }

    public void setFXCMCondDistLimit(double aFXCMCondDistLimit) {
        mFXCMCondDistLimit = aFXCMCondDistLimit;
    }

    public double getFXCMCondDistStop() {
        return mFXCMCondDistStop;
    }

    public void setFXCMCondDistStop(double aFXCMCondDistStop) {
        mFXCMCondDistStop = aFXCMCondDistStop;
    }

    public double getFXCMMaxQuantity() {
        return mFXCMMaxQuantity;
    }

    public void setFXCMMaxQuantity(double aFXCMMaxQuantity) {
        mFXCMMaxQuantity = aFXCMMaxQuantity;
    }

    public double getFXCMMinQuantity() {
        return mFXCMMinQuantity;
    }

    public void setFXCMMinQuantity(double aFXCMMinQuantity) {
        mFXCMMinQuantity = aFXCMMinQuantity;
    }

    public String getFXCMTradingStatus() {
        return mFXCMTradingStatus;
    }

    public void setFXCMTradingStatus(String aFXCMTradingStatus) {
        mFXCMTradingStatus = aFXCMTradingStatus;
    }

    /**
     * Gets maximum price value for some period of time.
     *
     * @return maximum price value for some period of time (server defined).
     */
    public double getHighPrice() {
        return mHighPrice;
    }

    /**
     * Sets maximum price value for some period of time.
     *
     * @param aHighPrice maximum price value for some period of time (server defined).
     */
    public void setHighPrice(double aHighPrice) {
        mHighPrice = aHighPrice;
    }

    /**
     * Returns id of rate
     *
     * @return ID
     */
    public long getID() {
        return mID;
    }

    /**
     * Sets id of rate
     *
     * @param aID id
     */
    public void setID(long aID) {
        mID = aID;
    }

    public Object getKey() {
        return mCurrency;
    }

    /**
     * Gets last time when the rate was created/updated.
     *
     * @return last time when the rate was created/updated.
     */
    public Date getLastDate() {
        return (Date) mLastDate.clone();
    }

    /**
     * Gets minimum price value for some period of time.
     *
     * @return minimum price value for some period of time (server defined).
     */
    public double getLowPrice() {
        return mLowPrice;
    }

    /**
     * Sets minimum price value for some period of time.
     *
     * @param aLowPrice minimum price value for some period of time (server defined).
     */
    public void setLowPrice(double aLowPrice) {
        mLowPrice = aLowPrice;
    }

    /**
     * Gets old buy price for the currency.
     *
     * @return old buy price for the currency.
     */
    public double getOldBuyPrice() {
        return mOldBuyPrice;
    }

    /**
     * Sets old buy price for the currency.
     *
     * @param aOldBuyPrice old buy price for the currency.
     */
    public void setOldBuyPrice(double aOldBuyPrice) {
        mOldBuyPrice = aOldBuyPrice;
    }

    /**
     * Gets old sell price for the currency.
     *
     * @return old sell price for the currency.
     */
    public double getOldSellPrice() {
        return mOldSellPrice;
    }

    /**
     * Sets old sell price for the currency.
     *
     * @param aOldSellPrice sell price for the currency.
     */
    public void setOldSellPrice(double aOldSellPrice) {
        mOldSellPrice = aOldSellPrice;
    }

    /**
     * Returns market opening ask price
     *
     * @return price
     */
    public double getOpenAsk() {
        return mOpenAsk;
    }

    /**
     * Sets market opening Ask price
     *
     * @param aOpenAsk price
     */
    public void setOpenAsk(double aOpenAsk) {
        mOpenAsk = aOpenAsk;
    }

    /**
     * Returns market opening bid price
     *
     * @return Bid
     */
    public double getOpenBid() {
        return mOpenBid;
    }

    /**
     * Sets market opening Bid price
     *
     * @param aOpenBid price
     */
    public void setOpenBid(double aOpenBid) {
        mOpenBid = aOpenBid;
    }

    public double getPipCost() {
        return mPipCost;
    }

    public void setPipCost(double aPipCost) {
        mPipCost = aPipCost;
    }

    public int getProduct() {
        return mProduct;
    }

    public String getQuoteID() {
        return mQuoteID;
    }

    public void setQuoteID(String aQuoteID) {
        mQuoteID = aQuoteID;
    }

    /**
     * Gets sell interest.
     *
     * @return sell interest.
     */
    public double getSellInterest() {
        return mSellInterest;
    }

    /**
     * Sets sell interest.
     *
     * @param aSellInterest sell interest.
     */
    public void setSellInterest(double aSellInterest) {
        mSellInterest = aSellInterest;
    }

    /**
     * Returns sell price layer id
     *
     * @return Sell LID
     */
    public int getSellLID() {
        return mSellLID;
    }

    /**
     * Sets sell price Layer ID
     *
     * @param aSellLID id
     */
    public void setSellLID(int aSellLID) {
        mSellLID = aSellLID;
    }

    /**
     * Returns sell price id
     *
     * @return Sell PID
     */
    public long getSellPID() {
        return mSellPID;
    }

    /**
     * Sets sell price ID
     *
     * @param aSellPID id
     */
    public void setSellPID(long aSellPID) {
        mSellPID = aSellPID;
    }

    /**
     * Gets sell price for the currency.
     *
     * @return sell price for the currency (from trader point of view).
     */
    public double getSellPrice() {
        return mSellPrice;
    }

    /**
     * Sets sell price for the currency.
     *
     * @param aSellPrice sell price for the currency (from trader point of view).
     */
    public void setSellPrice(double aSellPrice) {
        mOldSellPrice = mSellPrice;
        mSellPrice = aSellPrice;
    }

    public boolean isBuyBlocked() {
        return mBuyBlocked;
    }

    public void setBuyBlocked(boolean aBuyBlocked) {
        mBuyBlocked = aBuyBlocked;
    }

    public boolean isBuyTradable() {
        return mBuyTradable;
    }

    public void setBuyTradable(boolean aBuyTradable) {
        mBuyTradable = aBuyTradable;
    }

    public boolean isForex() {
        return mProduct == 0 || mProduct == 4;
    }

    public boolean isSellBocked() {
        return mSellBocked;
    }

    public void setSellBocked(boolean aSellBocked) {
        mSellBocked = aSellBocked;
    }

    public boolean isSellTradable() {
        return mSellTradable;
    }

    public void setSellTradable(boolean aSellTradable) {
        mSellTradable = aSellTradable;
    }

    public boolean isSubscribed() {
        return mSubscribed;
    }

    public void setSubscribed(boolean aSubscribed) {
        mSubscribed = aSubscribed;
    }

    /**
     * Gets possibility of selling operation for this rate.
     *
     * @return true if selling operation is possible, false otherwise.
     */
    public boolean isTradable() {
        return mBuyTradable || mSellTradable;
    }

    /**
     * Recalculates rate by buy price
     *
     * @param aBuyPrice price
     */
    public void recalculateBuySide(double aBuyPrice) {
        if (aBuyPrice > mHighPrice) {
            mHighPrice = aBuyPrice;
        } else if (aBuyPrice < mLowPrice) {
            mLowPrice = aBuyPrice;
        }
    }

    /**
     * Recalculates rate by sell price
     *
     * @param aSellPrice price
     */
    public void recalculateSellSide(double aSellPrice) {
        if (aSellPrice > mHighPrice) {
            mHighPrice = aSellPrice;
        } else if (aSellPrice < mLowPrice) {
            mLowPrice = aSellPrice;
        }
    }

    /**
     * Sets currency pair.
     *
     * @param aCurrency currency pair in format CCY1/CCY2.
     */
    public void setCurrency(String aCurrency) {
        if (aCurrency != null) {
            mCurrency = aCurrency;
        }
    }

    /**
     * Sets highest buy price
     *
     * @param aBuyPrice price
     */
    public void setHighBuyPrice(double aBuyPrice) {
        mHighPrice = aBuyPrice;
    }

    /**
     * Sets highest Sell Price
     *
     * @param aSellPrice price
     */
    public void setHighSellPrice(double aSellPrice) {
        mHighPrice = aSellPrice;
    }

    /**
     * Sets last time when the rate was created/updated.
     *
     * @param aLastDate last time when the rate was created/updated.
     */
    public void setLastDate(Date aLastDate) {
        if (aLastDate != null) {
            mLastDate = (Date) aLastDate.clone();
        }
    }

    /**
     * Sets lowest Buy price
     *
     * @param aBuyPrice price
     */
    public void setLowBuyPrice(double aBuyPrice) {
        mLowPrice = aBuyPrice;
    }

    /**
     * Sets lowest Sell price
     *
     * @param aSellPrice price
     */
    public void setLowSellPrice(double aSellPrice) {
        mLowPrice = aSellPrice;
    }

    public void setProduct(int aProduct) {
        mProduct = aProduct;
    }

    /**
     * Sets possibility of selling operation for this rate.
     *
     * @param aTradable possibility of selling operation for this rate.
     */
    public void setTradable(boolean aTradable) {
        mTradable = aTradable;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Rate");
        sb.append("{mBuyBlocked=").append(mBuyBlocked);
        sb.append(", mBuyInterest=").append(mBuyInterest);
        sb.append(", mBuyLID=").append(mBuyLID);
        sb.append(", mBuyPID=").append(mBuyPID);
        sb.append(", mBuyPrice=").append(mBuyPrice);
        sb.append(", mBuyTradable=").append(mBuyTradable);
        sb.append(", mContractCurrency='").append(mContractCurrency).append('\'');
        sb.append(", mContractMultiplier=").append(mContractMultiplier);
        sb.append(", mContractSize=").append(mContractSize);
        sb.append(", mCurrency='").append(mCurrency).append('\'');
        sb.append(", mFXCMCondDistEntryLimit=").append(mFXCMCondDistEntryLimit);
        sb.append(", mFXCMCondDistEntryStop=").append(mFXCMCondDistEntryStop);
        sb.append(", mFXCMCondDistLimit=").append(mFXCMCondDistLimit);
        sb.append(", mFXCMCondDistStop=").append(mFXCMCondDistStop);
        sb.append(", mFXCMMaxQuantity=").append(mFXCMMaxQuantity);
        sb.append(", mFXCMMinQuantity=").append(mFXCMMinQuantity);
        sb.append(", mFXCMTradingStatus='").append(mFXCMTradingStatus).append('\'');
        sb.append(", mHighPrice=").append(mHighPrice);
        sb.append(", mID=").append(mID);
        sb.append(", mLastDate=").append(mLastDate);
        sb.append(", mLowPrice=").append(mLowPrice);
        sb.append(", mOldBuyPrice=").append(mOldBuyPrice);
        sb.append(", mOldSellPrice=").append(mOldSellPrice);
        sb.append(", mOpenAsk=").append(mOpenAsk);
        sb.append(", mOpenBid=").append(mOpenBid);
        sb.append(", mPipCost=").append(mPipCost);
        sb.append(", mProduct=").append(mProduct);
        sb.append(", mQuoteID='").append(mQuoteID).append('\'');
        sb.append(", mSellBocked=").append(mSellBocked);
        sb.append(", mSellInterest=").append(mSellInterest);
        sb.append(", mSellLID=").append(mSellLID);
        sb.append(", mSellPID=").append(mSellPID);
        sb.append(", mSellPrice=").append(mSellPrice);
        sb.append(", mSellTradable=").append(mSellTradable);
        sb.append(", mSubscribed=").append(mSubscribed);
        sb.append(", mToStringFormatter=").append(mToStringFormatter);
        sb.append(", mTradable=").append(mTradable);
        sb.append('}');
        return sb.toString();
    }
}
