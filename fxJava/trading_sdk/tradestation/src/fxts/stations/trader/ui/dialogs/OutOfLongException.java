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
package fxts.stations.trader.ui.dialogs;

/**
 * An exception that is raised by Amount Combo box when user enter value being out of long value limits.<br>
 * .<br>
 * <br>
 *
 * @Creation date (11/10/2003 12:58 PM)
 */
public abstract class OutOfLongException extends Exception {
    /**
     * Protected constructor
     */
    protected OutOfLongException() {
    }

    /**
     * Protected constructor.
     *
     * @param aMsg error message
     */
    protected OutOfLongException(String aMsg) {
        super(aMsg);
    }

    /**
     * Returns true if entered value is bigger then maximal posible
     */
    public abstract boolean isBigger();

    /**
     * Returns true if entered value is lower then maximal posible
     */
    public abstract boolean isLower();
}
