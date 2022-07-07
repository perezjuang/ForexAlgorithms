/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/util/WeakListener.java#1 $
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
 * Created: Oct 1, 2008 4:36:46 PM
 *
 * $History: $
 */
package fxts.stations.util;

import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * Utility: listeners will be removed by garbage collection if WeakListener contains the only active reference
 *
 * @see WeakHashMap
 * @see Iterable
 */
public class WeakListener<E> implements Iterable<E> {
    private final WeakHashMap<E, Object> mListeners = new WeakHashMap<E, Object>();

    public void add(E aElement) {
        mListeners.put(aElement, null);
    }

    public Iterator iterator() {
        return mListeners.keySet().iterator();
    }

    public void remove(E aElement) {
        mListeners.remove(aElement);
    }
}
