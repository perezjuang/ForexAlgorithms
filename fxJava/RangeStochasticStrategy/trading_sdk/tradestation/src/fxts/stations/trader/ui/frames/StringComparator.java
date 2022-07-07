/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/StringComparator.java#1 $
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
 * Author: Andre Mermegas
 * Created: Apr 2, 2008 1:40:45 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.trader.TradeApp;

/**
 */
public abstract class StringComparator<E> implements IFrameComparator<E> {
    public int compare(E aObject1, E aObject2) {
        Object value1 = getValue1(aObject1);
        Object value2 = getValue2(aObject2);
        int mode = isDescendingMode() ? -1 : 1;
        if (value1 instanceof String && value2 instanceof String) {
            if (TradeApp.getInst().getResourceManager().getString("IDS_SUMMARY_TOTAL").equals(value1)) {
                return 1;
            } else if (TradeApp.getInst().getResourceManager().getString("IDS_SUMMARY_TOTAL").equals(value2)) {
                return 0;
            } else {
                return value1.toString().compareTo(value2.toString()) * mode;
            }
        } else {
            return 0;
        }
    }
}
