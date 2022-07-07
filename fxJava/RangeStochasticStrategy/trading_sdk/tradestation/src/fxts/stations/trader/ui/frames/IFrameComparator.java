/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/frames/IFrameComparator.java#1 $
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
 * Created: Apr 22, 2008 4:24:55 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.frames;

import java.io.Serializable;
import java.util.Comparator;

/**
 */
public interface IFrameComparator<E> extends Comparator<E>, Serializable {
    Object getValue1(E aElement);

    Object getValue2(E aElement);

    boolean isDescendingMode();
}
