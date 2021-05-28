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
package fxts.stations.util.signals;

import fxts.stations.util.ISignal;
import fxts.stations.util.SignalType;

/**
 * AddSignal class
 * This signal is sent when some element in the SignalVector has been added.
 */
public class AddSignal implements ISignal {
    /**
     * This signal type
     */
    public static final SignalType SIGNAL_TYPE = SignalType.ADD;
    /**
     * Element value
     */
    private Object mElement;
    /**
     * Index of added element
     */
    private int miIndex;

    /**
     * Constructor
     *
     * @param aiIndex  index of added element.
     * @param aElement added element value.
     */
    public AddSignal(int aiIndex, Object aElement) {
        miIndex = aiIndex;
        mElement = aElement;
    }

    /**
     * Gets added element value.
     */
    public Object getElement() {
        return mElement;
    }

    /**
     * Gets index where element was added.
     */
    public int getIndex() {
        return miIndex;
    }

    /**
     * Gets type of the signal.
     */
    public SignalType getType() {
        return SIGNAL_TYPE;
    }
}
