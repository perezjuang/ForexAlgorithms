/*
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
 */
package fxts.stations.datatypes;

/**
 * Represent summary for a single currency.
 */
public class Summary implements IKey {
    /**
     * Total contract size for all open buy positions
     */
    private long mAmountBuy;
    /**
     * Total contract size for all open sell positions
     */
    private long mAmountSell;
    /**
     * Average buy positions open price
     */
    private double mAvgBuyRate;
    /**
     * Average sell positions open price
     */
    private double mAvgSellRate;
    /**
     * Total buy profit-loss
     */
    private double mBuyPnL;
    /**
     * Currency
     */
    private String mCurrency = "";
    /**
     * Total Gross profit-loss
     */
    private double mGrossTotalPnL;
    /**
     * Total Net profit-loss
     */
    private double mNetTotalPnL;
    /**
     * Total open positions count for the currency.
     */
    private int mPositionsCount;
    /**
     * Total sell profit-loss
     */
    private double mSellPnL;
    /**
     * Total contract size for the currency
     */
    private long mTotalAmount;

    /**
     * Gets total contract size for all open buy positions.
     */
    public long getAmountBuy() {
        return mAmountBuy;
    }

    /**
     * Sets total contract size for all open buy positions.
     */
    public void setAmountBuy(long alAmountBuy) {
        mAmountBuy = alAmountBuy;
    }

    /**
     * Gets total contract size for all open sell positions.
     */
    public long getAmountSell() {
        return mAmountSell;
    }

    /**
     * Sets total contract size for all open sell positions.
     */
    public void setAmountSell(long alAmountSell) {
        mAmountSell = alAmountSell;
    }

    /**
     * Gets average buy positions open price.
     */
    public double getAvgBuyRate() {
        return mAvgBuyRate;
    }

    /**
     * Sets average buy positions open price.
     */
    public void setAvgBuyRate(double adAvgBuyRate) {
        mAvgBuyRate = adAvgBuyRate;
    }

    /**
     * Gets average sell positions open price.
     */
    public double getAvgSellRate() {
        return mAvgSellRate;
    }

    /**
     * Sets average sell positions open price.
     */
    public void setAvgSellRate(double adAvgSellRate) {
        mAvgSellRate = adAvgSellRate;
    }

    /**
     * Gets total buy profit-loss.
     */
    public double getBuyPnL() {
        return mBuyPnL;
    }

    /**
     * Sets total buy profit-loss.
     */
    public void setBuyPnL(double adBuyPnL) {
        mBuyPnL = adBuyPnL;
    }

    /**
     * Gets currency pair in format CCY1/CCY2.
     */
    public String getCurrency() {
        return mCurrency;
    }

    /**
     * Gets total profit-loss.
     */
    public double getGrossTotalPnL() {
        return mGrossTotalPnL;
    }

    /**
     * Sets total profit-loss.
     */
    public void setGrossTotalPnL(double adTotalPnL) {
        mGrossTotalPnL = adTotalPnL;
    }

    public Object getKey() {
        return getCurrency();
    }

    public double getNetTotalPnL() {
        return mNetTotalPnL;
    }

    public void setNetTotalPnL(double aNetTotalPnL) {
        mNetTotalPnL = aNetTotalPnL;
    }

    /**
     * Gets total open position count for the currency.
     */
    public int getPositionsCount() {
        return mPositionsCount;
    }

    /**
     * Sets total open position count for the currency.
     */
    public void setPositionsCount(int aiPositionsCount) {
        mPositionsCount = aiPositionsCount;
    }

    /**
     * Gets total sell profit-loss.
     */
    public double getSellPnL() {
        return mSellPnL;
    }

    /**
     * Sets total sell profit-loss.
     */
    public void setSellPnL(double adSellPnL) {
        mSellPnL = adSellPnL;
    }

    /**
     * Gets total contract size for the currency.
     */
    public long getTotalAmount() {
        return mTotalAmount;
    }

    /**
     * Sets total contract size for the currency.
     */
    public void setTotalAmount(long alTotalAmount) {
        mTotalAmount = alTotalAmount;
    }

    /**
     * Sets currency pair in format CCY1/CCY2.
     */
    public void setCurrency(String asCurrency) {
        if (asCurrency != null) {
            mCurrency = asCurrency;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Summary");
        sb.append("{mdAvgBuyRate=").append(mAvgBuyRate);
        sb.append(", mdAvgSellRate=").append(mAvgSellRate);
        sb.append(", mdBuyPnL=").append(mBuyPnL);
        sb.append(", mdSellPnL=").append(mSellPnL);
        sb.append(", mdTotalPnL=").append(mGrossTotalPnL);
        sb.append(", miPositionsCount=").append(mPositionsCount);
        sb.append(", mlAmountBuy=").append(mAmountBuy);
        sb.append(", mlAmountSell=").append(mAmountSell);
        sb.append(", mlTotalAmount=").append(mTotalAmount);
        sb.append(", msCurrency='").append(mCurrency).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
