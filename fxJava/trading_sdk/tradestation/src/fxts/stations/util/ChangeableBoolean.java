/*
 * $Header:$
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
 * 10/31/2003 creted by Ushik
 */
package fxts.stations.util;

/**
 * Wrapper for boolean primitive type suitable for using for synchronization.<br>
 * <br>
 *
 * @Creation date (10/31/2003 4:28 PM)
 */
public class ChangeableBoolean {
    /**
     * value
     */
    private boolean mValue;

    /**
     * Constructor
     *
     * @param abValue initial value
     *
     * @return
     *
     * @throws
     */
    public ChangeableBoolean(boolean abValue) {
        mValue = abValue;
    }

    /**
     * Returns value
     */
    public synchronized boolean getValue() {
        return mValue;
    }

    /**
     * Sets value and returns its previous value
     */
    public synchronized void setValue(boolean abValue) {
        mValue = abValue;
    }

    /**
     * Returns value and sets new one.
     *
     * @param abNewValue new value
     *
     * @return old value
     *
     * @throws
     */
    public synchronized boolean wasSet(boolean abNewValue) {
        boolean rc = mValue;
        mValue = abNewValue;
        return rc;
    }
}
