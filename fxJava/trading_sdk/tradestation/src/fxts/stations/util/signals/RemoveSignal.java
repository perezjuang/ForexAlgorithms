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
 * RemoveSignal class
 * This signal is sent when some element in the SignalVector has been removed.
 */
public class RemoveSignal implements ISignal {
    /**
     * This signal type
     */
    public static final SignalType SIGNAL_TYPE = SignalType.REMOVE;
    /**
     * Element value
     */
    private Object mElement;
    /**
     * Index of removed element
     */
    private int miIndex;

    /**
     * Constructor
     *
     * @param aiIndex  index of removed element.
     * @param aElement removed element value.
     */
    public RemoveSignal(int aiIndex, Object aElement) {
        miIndex = aiIndex;
        mElement = aElement;
    }

    /**
     * Gets removed element value.
     */
    public Object getElement() {
        return mElement;
    }

    /**
     * Gets index where element was removed.
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
