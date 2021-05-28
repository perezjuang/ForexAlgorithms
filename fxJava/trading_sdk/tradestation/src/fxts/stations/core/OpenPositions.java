/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/OpenPositions.java#2 $
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
 * 05/05/2006   Andre Mermegas: fix for stopmove,pippl
 * 07/18/2006   Andre Mermegas: performance update
 * 01/16/2006   Andre Mermegas: bugfix in pnl
 */
package fxts.stations.core;

import com.fxcm.fix.IFixDefs;
import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.ConversionRate;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.Util;
import fxts.stations.util.signals.ChangeSignal;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A collection of Position objects for all open positions.
 */
public class OpenPositions extends ABusinessTable implements ISignalListener {
    private static final DecimalFormat NUMBER_FORMATTER = new ThreadSafeNumberFormat().getInstance();
    private double mPipPL;
    private long mTotalAmount;
    private double mTotalCommision;
    private double mTotalGrossPnL;
    private double mTotalInterest;
    private double mTotalNetPnL;
    private double mTotalUsedMargin;

    @Override
    public void add(Object aObj) {
        Position position = (Position) aObj;
        if (position.getClosePrice() == 0) {
            String currency = position.getCurrency();
            Rate rate = TradeDesk.getInst().getRates().getRate(currency);
            double closePrice = position.getSide() == Side.BUY ? rate.getSellPrice() : rate.getBuyPrice();
            position.setClosePrice(closePrice);
            fillPosition(position, rate);
        }
        super.add(aObj);
        updateTotals();
    }

    /**
     * Calculates GrossPnL for specified fields of a position
     *
     * @param aRate rate which identifies the positions currency
     * @param aPosition position for pnl
     *
     * @return gross P/L
     */
    private double calcGrossPnL(Rate aRate, Position aPosition) {
        if (aRate == null || aPosition.getClosePrice() == 0.0) {
            return 0.0;
        }

        double pnl; //raw pnl
        if (aPosition.getSide() == Side.BUY) {
            pnl = (aPosition.getClosePrice() - aPosition.getOpenPrice()) * aPosition.getAmount();
        } else {
            pnl = (aPosition.getOpenPrice() - aPosition.getClosePrice()) * aPosition.getAmount();
        }

        String[] currencies = TradeDesk.splitCurrencyPair(aRate.getCurrency());
        String ccy1 = ""; //position
        String ccy2 = ""; //tradable
        if (currencies != null && currencies.length == 2) {
            ccy1 = currencies[0];
            ccy2 = currencies[1];
        }
        String book = Liaison.getInstance().getAccountCurrency();
        // try and find the crosspairs
        Rates rates = TradeDesk.getInst().getRates();
        Rate simpleCross1 = rates.getRate(TradeDesk.toPair(ccy2, book));
        Rate simpleCross2 = rates.getRate(TradeDesk.toPair(book, ccy2));
        Rate unusualCross1 = rates.getRate(TradeDesk.toPair(ccy1, book));
        Rate unusualCross2 = rates.getRate(TradeDesk.toPair(book, ccy1));
        Rate simpleCFD1 = rates.getRate(TradeDesk.toPair(aRate.getContractCurrency(), book));
        Rate simpleCFD2 = rates.getRate(TradeDesk.toPair(book, aRate.getContractCurrency()));
        if (aRate.getProduct() != 0 && aRate.getProduct() != IFixDefs.PRODUCT_CURRENCY) {
            if (aRate.getContractCurrency().equals(book)) {
                //xxx do nothing
            } else if (simpleCFD1 != null) {
                pnl *= TradeDesk.findAverage(simpleCFD1);
            } else if (simpleCFD2 != null) {
                pnl *= 1 / TradeDesk.findAverage(simpleCFD2);
            }
        } else if (ccy2.equals(book)) { //1
            //xxx do nothing
        } else if (ccy1.equals(book)) { //2
            pnl /= aPosition.getClosePrice();
        } else if (simpleCross1 != null) { //3
            pnl *= TradeDesk.findAverage(simpleCross1);
        } else if (simpleCross2 != null) { //3
            pnl *= 1 / TradeDesk.findAverage(simpleCross2);
        } else if (unusualCross1 != null) { //4
            pnl /= aPosition.getClosePrice();
            pnl *= TradeDesk.findAverage(unusualCross1);
        } else if (unusualCross2 != null) { //4
            pnl /= aPosition.getClosePrice();
            pnl *= 1 / TradeDesk.findAverage(unusualCross2);
        } else { //5,6
            ConversionRate toMajor = TradeDesk.findConversionRateToMajor(aRate);
            ConversionRate toBook = TradeDesk.findConversionRateToBook();
            if (TradeDesk.isCurrencyInThePair(ccy1, toMajor.getCurrency())) {
                pnl /= aPosition.getClosePrice();
            }
            pnl *= toMajor.getPrice();
            pnl *= toBook.getPrice();
        }

        if (aRate.getContractMultiplier() != 0) {
            pnl *= aRate.getContractMultiplier();
        }

        return pnl;
    }

    @Override
    public void clear() {
        super.clear();
        updateTotals();
    }

    /**
     * This method enables/disables recalculate business data mode for the table.
     * It must be overridden in subclasses.
     */
    @Override
    public void enableRecalc(boolean aEnable) {
        if (aEnable) {
            TradeDesk.getInst().getRates().subscribe(this, SignalType.CHANGE);
        } else {
            TradeDesk.getInst().getRates().unsubscribe(this, SignalType.CHANGE);
        }
    }

    private void fillPosition(Position aOpenPos, Rate aRate) {
        double pipsPrice = TradeDesk.getPipsPrice(aOpenPos.getCurrency());
        if (aOpenPos.getTrailStop() > 0 && aOpenPos.getClosePrice() != 0) {
            if (Side.BUY == aOpenPos.getSide()) {
                double rawPrice = aOpenPos.getTrailingRate() + pipsPrice * aOpenPos.getTrailStop();
                double price = Util.parseDouble(TradeDesk.formatPrice(aOpenPos.getCurrency(), rawPrice));
                double diff = price - aRate.getSellPrice();
                double pip = Util.parseDouble(TradeDesk.formatPrice(aOpenPos.getCurrency(), diff));
                double stopmove = pip / pipsPrice;
                aOpenPos.setStopMove(stopmove);
            } else {
                double rawPrice = aOpenPos.getTrailingRate() - pipsPrice * aOpenPos.getTrailStop();
                double price = Util.parseDouble(TradeDesk.formatPrice(aOpenPos.getCurrency(), rawPrice));
                double diff = aRate.getBuyPrice() - price;
                double pip = Util.parseDouble(TradeDesk.formatPrice(aOpenPos.getCurrency(), diff));
                double stopmove = pip / pipsPrice;
                aOpenPos.setStopMove(stopmove);
            }
        }
        if (aOpenPos.getOpenPrice() != 0 && aOpenPos.getClosePrice() != 0) {
            Account account = TradeDesk.getInst().getAccounts().getAccount(aOpenPos.getAccount());
            double contractSize = aRate.getContractSize();
            if (account != null && (aRate.getProduct() == IFixDefs.PRODUCT_CURRENCY || aRate.getProduct() == 0)) {
                contractSize = account.getBaseUnitSize();
            }
            if (aOpenPos.getSide() == Side.BUY) {
                String price = TradeDesk.formatPrice(aOpenPos.getCurrency(),
                                                     aOpenPos.getClosePrice() - aOpenPos.getOpenPrice());
                double pipdiff = Util.parseDouble(price) / pipsPrice;
                double adjustedPipDiff = pipdiff * (aOpenPos.getAmount() / contractSize);
                aOpenPos.setPipPL(adjustedPipDiff);
            } else {
                String price = TradeDesk.formatPrice(aOpenPos.getCurrency(),
                                                     aOpenPos.getOpenPrice() - aOpenPos.getClosePrice());
                double pipdiff = Util.parseDouble(price) / pipsPrice;
                double adjustedPipDiff = pipdiff * (aOpenPos.getAmount() / contractSize);
                aOpenPos.setPipPL(adjustedPipDiff);
            }
        }
        if (aOpenPos.getClosePrice() != 0) {
            aOpenPos.setGrossPnL(calcGrossPnL(aRate, aOpenPos));
            aOpenPos.setNetPnL(aOpenPos.getGrossPnL() - aOpenPos.getCommission() + aOpenPos.getInterest());
        }
    }

    public double getPipPL() {
        return mPipPL;
    }

    public void setPipPL(double aPipPL) {
        mPipPL = aPipPL;
    }

    /**
     * Finds the open position by ticket id, returns null if not found.
     * asTicketID   id of position
     * @param aTicketID ticket
     * @return position
     */
    public Position getPosition(String aTicketID) {
        return (Position) get(aTicketID);
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
        if (aSignal != null && aSignal.getType() == SignalType.CHANGE) {
            ChangeSignal signal = (ChangeSignal) aSignal;
            Rate rate = (Rate) signal.getNewElement();
            // For all open positions for this currency
            long totalAmount = 0;
            double totalPipPL = 0;
            double totalGrossPnL = 0;
            double totalNetPnL = 0;
            double totalComm = 0;
            double totalIntr = 0;
            double totalUsedMargin = 0;
            Set<String> ccySet = new HashSet<String>();
            Set<String> accountSet = new HashSet<String>();
            List<Integer> indexList = new ArrayList<Integer>();
            for (int i = 0; i < size(); i++) {
                Position position = (Position) get(i);
                if (rate.getCurrency().equals(position.getCurrency())) {
                    boolean changed = false;
                    if (position.getSide() == Side.BUY) {
                        if (position.getClosePrice() != rate.getSellPrice()) {
                            position.setClosePrice(rate.getSellPrice());
                            changed = true;
                        }
                    } else {
                        if (position.getClosePrice() != rate.getBuyPrice()) {
                            position.setClosePrice(rate.getBuyPrice());
                            changed = true;
                        }
                    }
                    if (changed) {
                        position.setCurrencyTradable(rate.isTradable());
                        fillPosition(position, rate);
                        ////xxx change all values before calling elementchanged later
                        indexList.add(i);
                        ccySet.add(position.getCurrency());
                        accountSet.add(position.getAccount());
                    }
                }
                totalGrossPnL += position.getGrossPnL();
                totalNetPnL += position.getNetPnL();
                totalComm += position.getCommission();
                totalIntr += position.getInterest();
                totalUsedMargin += position.getUsedMargin();
                totalAmount += position.getAmount();
                totalPipPL += position.getPipPL();
            }
            for (Integer integer : indexList) {
                elementChanged(integer);
            }
            setTotalGrossPnL(totalGrossPnL);
            setTotalNetPnL(totalNetPnL);
            setTotalCommision(totalComm);
            setTotalInterest(totalIntr);
            setTotalUsedMargin(totalUsedMargin);
            setPipPL(totalPipPL);
            setTotalAmount(totalAmount);
            //this is done here for performance reasons. only update these once per cycle
            for (String ccy : ccySet) {
                Summaries summaries = TradeDesk.getInst().getSummaries();
                summaries.update(ccy, aSignal.getType());
            }
            for (String acc : accountSet) {
                Accounts accounts = TradeDesk.getInst().getAccounts();
                accounts.update(acc);
            }
        }
    }

    @Override
    public Object remove(int aIndex) {
        Object o = super.remove(aIndex);
        updateTotals();
        return o;
    }

    @Override
    public Object set(int aIndex, Object aObj) {
        Object obj = super.set(aIndex, aObj);
        Position position = (Position) aObj;
        String currency = position.getCurrency();
        Rate rate = TradeDesk.getInst().getRates().getRate(currency);
        fillPosition(position, rate);
        updateTotals();
        return obj;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("OpenPositions");
        sb.append(" --- begin superclass toString ");
        sb.append(super.toString());
        sb.append(" --- end superclass toString ");
        sb.append("{mPipPL=").append(NUMBER_FORMATTER.format(mPipPL));
        sb.append(", mTotalAmount=").append(NUMBER_FORMATTER.format(mTotalAmount));
        sb.append(", mTotalCommision=").append(NUMBER_FORMATTER.format(mTotalCommision));
        sb.append(", mTotalGrossPnL=").append(NUMBER_FORMATTER.format(mTotalGrossPnL));
        sb.append(", mTotalInterest=").append(NUMBER_FORMATTER.format(mTotalInterest));
        sb.append(", mTotalNetPnL=").append(NUMBER_FORMATTER.format(mTotalNetPnL));
        sb.append(", mTotalUsedMargin=").append(NUMBER_FORMATTER.format(mTotalUsedMargin));
        sb.append('}');
        return sb.toString();
    }

    private void updateTotals() {
        long totalAmount = 0;
        double totalPipPL = 0;
        double totalGrossPnL = 0;
        double totalNetPnL = 0;
        double totalComm = 0;
        double totalIntr = 0;
        double totalUsedMargin = 0;
        for (int i = 0; i < size(); i++) {
            Position openPos = (Position) get(i);
            totalGrossPnL += openPos.getGrossPnL();
            totalNetPnL += openPos.getNetPnL();
            totalComm += openPos.getCommission();
            totalIntr += openPos.getInterest();
            totalUsedMargin += openPos.getUsedMargin();
            totalAmount += openPos.getAmount();
            totalPipPL += openPos.getPipPL();
        }
        mTotalGrossPnL = totalGrossPnL;
        mTotalNetPnL = totalNetPnL;
        mTotalCommision = totalComm;
        mTotalInterest = totalIntr;
        mTotalUsedMargin = totalUsedMargin;
        mPipPL = totalPipPL;
        mTotalAmount = totalAmount;
    }
}
