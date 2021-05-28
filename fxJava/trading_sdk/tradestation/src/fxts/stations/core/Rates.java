/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/core/Rates.java#1 $
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
 * 04/19/2007   Andre Mermegas: sort rates by preferred order, id.
 */
package fxts.stations.core;

import fxts.stations.datatypes.Rate;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;

/**
 * A collection of Rate objects.
 */
public class Rates extends ABusinessTable implements ISignalListener {
    public Rates() {
        subscribe(this, SignalType.CHANGE);
        setComparator(new RatesComparator());
    }

    /**
     * Finds the rate by currency, returns null if not found.
     */
    public Rate getRate(String aCurrency) {
        return (Rate) get(aCurrency);
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal != null && aSignal.getType() == SignalType.CHANGE) {
            TradeDesk.updatePipCosts();
        }
    }
}
