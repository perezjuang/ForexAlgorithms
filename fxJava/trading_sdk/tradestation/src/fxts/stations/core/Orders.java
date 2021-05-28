/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/Orders.java#3 $
 *
 * Copyright (c) 2010 FXCM, LLC.
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
 * 09/10/2003   ID   Created
 * 12/8/2004    Andre Mermegas  added method to get order by TradeID
 */
package fxts.stations.core;

import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.signals.ChangeSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A collection of Order objects.
 */
public class Orders extends ABusinessTable implements ISignalListener {
    private Map mTradeIdMap = new HashMap();

    /**
     * Overrides method of SignalVector for filling closePrice if it empty.
     */
    @Override
    public void add(Object aObj) {
        if (aObj instanceof Order) {
            Order order = (Order) aObj;
            if (order.getOfferRate() == 0.0) {
                String currency = order.getCurrency();
                Rate rate = TradeDesk.getInst().getRates().getRate(currency);
                double offerRate = order.getSide() == Side.BUY ? rate.getBuyPrice() : rate.getSellPrice();
                order.setOfferRate(offerRate);
            }
            mTradeIdMap.put(order.getTradeId(), order);
        }
        super.add(aObj);
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

    /**
     * Finds the order by order id, returns null if not found.
     *
     * @param aOrderID id of order
     */
    public Order getOrder(String aOrderID) {
        return (Order) get(aOrderID);
    }

    public Order getOrderByTradeId(String aTradeId) {
        return (Order) mTradeIdMap.get(aTradeId);
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
            List<Integer> indexList = new ArrayList<Integer>();
            for (int i = 0; i < size(); i++) {
                Order order = (Order) get(i);
                if (rate.getCurrency().equals(order.getCurrency())) {
                    if (order.isCurrencyTradable() != rate.isTradable()) {
                        order.setCurrencyTradable(rate.isTradable());
                        indexList.add(i);
                    } else if (order.getSide() == Side.BUY) {
                        if (order.getOfferRate() != rate.getBuyPrice()) {
                            order.setOfferRate(rate.getBuyPrice());
                            indexList.add(i);
                        }
                    } else {
                        if (order.getOfferRate() != rate.getSellPrice()) {
                            order.setOfferRate(rate.getSellPrice());
                            indexList.add(i);
                        }
                    }
                }
            }
            for (Integer integer : indexList) {
                elementChanged(integer);
            }
        }
    }

    @Override
    public boolean remove(Object aObj) {
        if (aObj instanceof Order) {
            Order o = (Order) aObj;
            mTradeIdMap.remove(o.getTradeId());
        }
        return super.remove(aObj);
    }

    @Override
    public Object remove(int aIndex) {
        Object obj = super.remove(aIndex);
        if (obj instanceof Order) {
            Order o = (Order) obj;
            mTradeIdMap.remove(o.getTradeId());
        }
        return obj;
    }

    @Override
    public Object set(int aIndex, Object aObj) {
        Object obj = super.set(aIndex, aObj);
        Order incoming = (Order) aObj;
        Order outgoing = (Order) obj;
        if (!incoming.getTradeId().equals(outgoing.getTradeId())) {
            mTradeIdMap.remove(outgoing.getTradeId());
            mTradeIdMap.put(incoming.getTradeId(), incoming);
        }
        return obj;
    }
}
