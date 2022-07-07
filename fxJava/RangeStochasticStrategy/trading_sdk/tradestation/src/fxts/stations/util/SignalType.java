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
package fxts.stations.util;

/**
 * SignalType class
 * This class defines enumeration of all possible signal types.
 */
public class SignalType {
    /**
     * Signal type ADD
     */
    public static final SignalType ADD = new SignalType("ADD");
    /**
     * Signal type CHANGE
     */
    public static final SignalType CHANGE = new SignalType("CHANGE");
    /**
     * Signal type REMOVE
     */
    public static final SignalType REMOVE = new SignalType("REMOVE");
    /**
     * Name of the signal
     */
    private String msSignalName;

    /**
     * Private constructor
     */
    private SignalType(String asName) {
        msSignalName = asName;
    }

    /**
     * Tests if this equals to the specified object
     */
    public boolean equals(Object aObj) {
        if (aObj instanceof SignalType) {
            return msSignalName.equals(((SignalType) aObj).msSignalName);
        }
        return false;
    }

    /**
     * Gets name of the object.
     */
    public String getName() {
        return msSignalName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("SignalType");
        sb.append("{msSignalName='").append(msSignalName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
