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
 *
 * $History: $
 * 08/28/2003   ID   Created
 *  12/1/2004   Andre   added a convenience lookup method
 */
package fxts.stations.datatypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines enumeration of two possible trading sides.
 * To compare objects of this type simple '==' operator can be used.
 */
public class Side {
    /**
     * Sell side.
     */
    public static final Side SELL = new Side("SELL");
    /**
     * Buy side.
     */
    public static final Side BUY = new Side("BUY");
    private static final Map sideMap = new HashMap();
    /**
     * Name of the side
     */
    public String msName = "";

    static {
        sideMap.put(SELL.getName(), SELL);
        sideMap.put(BUY.getName(), BUY);
    }

    /**
     * Private constructor.
     *
     * @param asName name of side
     */
    private Side(String asName) {
        msName = asName;
    }

    /**
     * Compares two Sides for equality.
     */
    public boolean equals(Object aObj) {
        if (aObj instanceof Side) {
            return msName.equals(((Side) aObj).msName);
        }
        return false;
    }

    /**
     * Returns name of the object.
     */
    public String getName() {
        return msName;
    }

    public static Side getSide(String side) {
        return (Side) sideMap.get(side);
    }
}