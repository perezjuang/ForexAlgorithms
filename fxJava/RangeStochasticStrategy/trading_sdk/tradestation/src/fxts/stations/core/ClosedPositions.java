/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/ClosedPositions.java#1 $
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
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;

/**
 * @author Andre Mermegas
 *         Date: Jan 26, 2006
 *         Time: 10:24:01 AM
 */
public class ClosedPositions extends ABusinessTable implements ISignalListener {
    private long mTotalAmount;
    private double mTotalCommision;
    private double mTotalGrossPnL;
    private double mTotalInterest;
    private double mTotalNetPnL;
    private double mTotalUsedMargin;

    @Override
    public void add(Object aObj) {
        super.add(aObj);
        updateTotals();
    }

    @Override
    public void clear() {
        super.clear();
        updateTotals();
    }

    @Override
    public void enableRecalc(boolean abEnable) {
        if (abEnable) {
            TradeDesk.getInst().getClosedPositions().subscribe(this, SignalType.ADD);
            TradeDesk.getInst().getClosedPositions().subscribe(this, SignalType.CHANGE);
            TradeDesk.getInst().getClosedPositions().subscribe(this, SignalType.REMOVE);
        } else {
            TradeDesk.getInst().getClosedPositions().unsubscribe(this, SignalType.ADD);
            TradeDesk.getInst().getClosedPositions().unsubscribe(this, SignalType.CHANGE);
            TradeDesk.getInst().getClosedPositions().unsubscribe(this, SignalType.REMOVE);
        }
    }

    public Position getPosition(String asTicketID) {
        return (Position) get(asTicketID);
    }

    public long getTotalAmount() {
        return mTotalAmount;
    }

    public void setTotalAmount(long aTotalAmount) {
        mTotalAmount = aTotalAmount;
    }

    public double getTotalCommision() {
        return mTotalCommision;
    }

    public void setTotalCommision(double aTotalCommision) {
        mTotalCommision = aTotalCommision;
    }

    public double getTotalGrossPnL() {
        return mTotalGrossPnL;
    }

    public void setTotalGrossPnL(double aTotalGrossPnL) {
        mTotalGrossPnL = aTotalGrossPnL;
    }

    public double getTotalInterest() {
        return mTotalInterest;
    }

    public void setTotalInterest(double aTotalInterest) {
        mTotalInterest = aTotalInterest;
    }

    public double getTotalNetPnL() {
        return mTotalNetPnL;
    }

    public void setTotalNetPnL(double aTotalNetPnL) {
        mTotalNetPnL = aTotalNetPnL;
    }

    public double getTotalUsedMargin() {
        return mTotalUsedMargin;
    }

    public void setTotalUsedMargin(double aTotalUsedMargin) {
        mTotalUsedMargin = aTotalUsedMargin;
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal == null) {
            return;
        }
        updateTotals();
    }

    private void updateTotals() {
        long totalAmount = 0;
        //long totalPipPL = 0;
        double totalGrossPnL = 0;
        double totalNetPnL = 0;
        double totalComm = 0;
        double totalIntr = 0;
        double totalUsedMargin = 0;
        for (int i = 0; i < size(); i++) {
            Position position = (Position) get(i);
            totalGrossPnL += position.getGrossPnL();
            totalNetPnL += position.getNetPnL();
            totalComm += position.getCommission();
            totalIntr += position.getInterest();
            totalUsedMargin += position.getUsedMargin();
            totalAmount += position.getAmount();
            //totalPipPL += position.getPipPL();
        }
        setTotalGrossPnL(totalGrossPnL);
        setTotalNetPnL(totalNetPnL);
        setTotalCommision(totalComm);
        setTotalInterest(totalIntr);
        setTotalUsedMargin(totalUsedMargin);
        //setPipPL(totalPipPL);
        setTotalAmount(totalAmount);
    }
}
