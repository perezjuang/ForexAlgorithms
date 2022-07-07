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
 * ChangeSignal class
 * This signal is sent when some element in the SignalVector has been changed.
 */
public class ChangeSignal implements ISignal {
    /**
     * This signal type
     */
    public static final SignalType SIGNAL_TYPE = SignalType.CHANGE;
    /**
     * New element value
     */
    private Object mNewElement;
    /**
     * Old element value. Can be null
     */
    private Object mOldElement;
    /**
     * Index of changed element
     */
    private int mIndex;

    /**
     * Constructor
     *
     * @param aIndex     index of changed element.
     * @param aNewElement new element value.
     * @param aOldElement old element value.
     */
    public ChangeSignal(int aIndex, Object aNewElement, Object aOldElement) {
        mIndex = aIndex;
        mNewElement = aNewElement;
        mOldElement = aOldElement;
    }

    /**
     * Gets index where element was changed.
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * Gets new element value.
     */
    public Object getNewElement() {
        return mNewElement;
    }

    /**
     * Gets old element value.
     */
    public Object getOldElement() {
        return mOldElement;
    }

    /**
     * Gets type of the signal.
     */
    public SignalType getType() {
        return SIGNAL_TYPE;
    }
}
