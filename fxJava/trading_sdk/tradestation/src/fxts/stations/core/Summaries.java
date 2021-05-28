/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/Summaries.java#2 $
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

import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Side;
import fxts.stations.datatypes.Summary;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.signals.AddSignal;
import fxts.stations.util.signals.RemoveSignal;

/**
 * A collection of Summary objects.
 */
public class Summaries extends ABusinessTable implements ISignalListener {
    /**
     * Total Gross profit-loss
     */
    private double mGrossTotalPnL;
    /**
     * Total Net profit-loss
     */
    private double mNetTotalPnL;
    /**
     * Total contract size for the currency
     */
    private long mTotalAmount;
    /**
     * Total contract size for all open buy positions
     */
    private long mTotalAmountBuy;
    /**
     * Total contract size for all open sell positions
     */
    private long mTotalAmountSell;
    /**
     * Total buy profit-loss
     */
    private double mTotalBuyPnL;
    /**
     * Total open position count for the currency.
     */
    private int mTotalPositionCount;
    /**
     * Total sell profit-loss
     */
    private double mTotalSellPnL;

    @Override
    public void clear() {
        super.clear();
        mTotalAmount = 0;
        mTotalAmountBuy = 0;
        mTotalAmountSell = 0;
        mTotalBuyPnL = 0;
        mGrossTotalPnL = 0;
        mNetTotalPnL = 0;
        mTotalPositionCount = 0;
        mTotalSellPnL = 0;
    }

    /**
     * This method enables/disables recalculate business data mode for the table.
     */
    @Override
    public void enableRecalc(boolean aEnable) {
        OpenPositions positions = TradeDesk.getInst().getOpenPositions();
        if (aEnable) {
            positions.subscribe(this, SignalType.ADD);
            positions.subscribe(this, SignalType.REMOVE);
        } else {
            positions.unsubscribe(this, SignalType.ADD);
            positions.unsubscribe(this, SignalType.REMOVE);
        }
    }

    /**
     * Get total Profit-Loss for Total row.
     */
    public double getGrossTotalPnL() {
        return mGrossTotalPnL;
    }

    /**
     * Set total Profit-Loss for Total row.
     */
    public void setGrossTotalPnL(double aVal) {
        mGrossTotalPnL = aVal;
    }

    public double getNetTotalPnL() {
        return mNetTotalPnL;
    }

    public void setNetTotalPnL(double aNetTotalPnL) {
        mNetTotalPnL = aNetTotalPnL;
    }

    /**
     * Finds the summary by currency, returns null if not found.
     */
    public Summary getSummary(String aCurrency) {
        return (Summary) get(aCurrency);
    }

    /**
     * Get Amount value for Total row.
     */
    public long getTotalAmount() {
        return mTotalAmount;
    }

    /**
     * Set Amount value for Total row.
     */
    public void setTotalAmount(long aVal) {
        mTotalAmount = aVal;
    }

    /**
     * Get Amount Buy value for Total row.
     */
    public long getTotalAmountBuy() {
        return mTotalAmountBuy;
    }

    /**
     * Set Amount Buy value for Total row.
     */
    public void setTotalAmountBuy(long aVal) {
        mTotalAmountBuy = aVal;
    }

    /**
     * Get Amount Sell value for Total row.
     */
    public long getTotalAmountSell() {
        return mTotalAmountSell;
    }

    /**
     * Set Amount Sell value for Total row.
     */
    public void setTotalAmountSell(long aVal) {
        mTotalAmountSell = aVal;
    }

    /**
     * Get total buy Profit-Loss for Total row.
     */
    public double getTotalBuyPnL() {
        return mTotalBuyPnL;
    }

    /**
     * Set total buy Profit-Loss for Total row.
     */
    public void setTotalBuyPnL(double aVal) {
        mTotalBuyPnL = aVal;
    }

    /**
     * Get total position count for Total row.
     */
    public int getTotalPositionCount() {
        return mTotalPositionCount;
    }

    /**
     * Set total position count for Total row.
     */
    public void setTotalPositionCount(int aVal) {
        mTotalPositionCount = aVal;
    }

    /**
     * Get total sell Profit-Loss for Total row.
     */
    public double getTotalSellPnL() {
        return mTotalSellPnL;
    }

    /**
     * Set total sell Profit-Loss for Total row.
     */
    public void setTotalSellPnL(double aVal) {
        mTotalSellPnL = aVal;
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        Position receivedPosition = null;
        if (aSignal.getType() == SignalType.ADD) {
            AddSignal addSignal = (AddSignal) aSignal;
            receivedPosition = (Position) addSignal.getElement();
        } else if (aSignal.getType() == SignalType.REMOVE) {
            RemoveSignal removeSignal = (RemoveSignal) aSignal;
            receivedPosition = (Position) removeSignal.getElement();
        }
        if (receivedPosition != null) {
            update(receivedPosition.getCurrency(), aSignal.getType());
        }
    }

    public void update(String aCurrency, SignalType aSignalType) {
        double grossPnlBuy = 0;
        double grossPnlSell = 0;
        double netPnlBuy = 0;
        double netPnlSell = 0;
        long amountBuy = 0;
        long amountSell = 0;
        double sumOfMultBuyByBuyAmount = 0;
        double sumOfMultSellBySellAmount = 0;
        int numOfPositions = 0;
        long totalBuyAmount = 0;
        long totalSellAmount = 0;
        double totalGrossBuyPnL = 0;
        double totalGrossSellPnL = 0;
        double totalNetBuyPnL = 0;
        double totalNetSellPnL = 0;
        OpenPositions positions = TradeDesk.getInst().getOpenPositions();
        for (int i = 0; i < positions.size(); i++) {
            Position position = (Position) positions.get(i);
            if (aCurrency.equals(position.getCurrency())) {
                if (position.getSide() == Side.BUY) {
                    amountBuy += position.getAmount();
                    grossPnlBuy += position.getGrossPnL();
                    netPnlBuy += position.getNetPnL();
                    sumOfMultBuyByBuyAmount += position.getAmount() * position.getOpenPrice();
                } else {
                    amountSell += position.getAmount();
                    grossPnlSell += position.getGrossPnL();
                    netPnlSell += position.getNetPnL();
                    sumOfMultSellBySellAmount += position.getAmount() * position.getOpenPrice();
                }
                numOfPositions++;
            } else {
                if (position.getSide() == Side.BUY) {
                    totalBuyAmount += position.getAmount();
                    totalGrossBuyPnL += position.getGrossPnL();
                    totalNetBuyPnL += position.getNetPnL();
                } else {
                    totalSellAmount += position.getAmount();
                    totalGrossSellPnL += position.getGrossPnL();
                    totalNetSellPnL += position.getNetPnL();
                }
            }
        }

        totalBuyAmount += amountBuy;
        totalGrossBuyPnL += grossPnlBuy;
        totalNetBuyPnL += netPnlBuy;
        totalSellAmount += amountSell;
        totalGrossSellPnL += grossPnlSell;
        totalNetSellPnL += netPnlSell;
        long totalAmount = totalBuyAmount + totalSellAmount;
        double totalGrossPnL = totalGrossBuyPnL + totalGrossSellPnL;
        double totalNetPnL = totalNetBuyPnL + totalNetSellPnL;

        mTotalSellPnL = totalGrossSellPnL;
        mTotalBuyPnL = totalGrossBuyPnL;
        mTotalAmountSell = totalSellAmount;
        mTotalAmountBuy = totalBuyAmount;
        mTotalAmount = totalAmount;
        mGrossTotalPnL = totalGrossPnL;
        mNetTotalPnL = totalNetPnL;
        mTotalPositionCount = positions.size();
        // looking for summary with specified currency
        Summary summary = (Summary) get(aCurrency);
        int index = indexOf(summary);

        if (summary == null) {
            summary = new Summary();
            summary.setCurrency(aCurrency);
        }

        // filling summary
        summary.setSellPnL(grossPnlSell);
        if (amountSell > 0) {
            summary.setAvgSellRate(sumOfMultSellBySellAmount / amountSell);
        } else {
            summary.setAvgSellRate(0);
        }
        summary.setAmountSell(amountSell);
        summary.setBuyPnL(grossPnlBuy);

        if (amountBuy > 0) {
            summary.setAvgBuyRate(sumOfMultBuyByBuyAmount / amountBuy);
        } else {
            summary.setAvgBuyRate(0);
        }
        summary.setAmountBuy(amountBuy);

        summary.setPositionsCount(numOfPositions);
        summary.setTotalAmount(amountSell + amountBuy);
        summary.setGrossTotalPnL(grossPnlSell + grossPnlBuy);
        summary.setNetTotalPnL(netPnlBuy + netPnlSell);
        // if new summary was created, add it to Summaries
        if (index == -1) {
            add(summary);
        } else {
            if (aSignalType == SignalType.REMOVE && numOfPositions == 0) {
                remove(summary);
            } else {
                elementChanged(index);
            }
        }
    }
}
