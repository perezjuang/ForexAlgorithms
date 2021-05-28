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
package fxts.stations.transport;

/**
 * LiaisonStatus class.<br>
 * <br>
 * This class contains enumeration of all liaison status values.
 * Constructor is private to deny of creation of this objects outside.
 * For methods descriptions see other enumeration classes.<br>
 * <br>
 *
 * @Creation date (9/3/2003 5:17 PM)
 */
public class LiaisonStatus {
    /**
     * Liaison is in disconnected state. Login command should be send
     */
    public static final LiaisonStatus DISCONNECTED = new LiaisonStatus("DISCONNECTED");
    /**
     * Liaison is connecting to the server now. It in this state after login command
     */
    public static final LiaisonStatus CONNECTING = new LiaisonStatus("CONNECTING");
    /**
     * Connection was lost and liaison is desperately trying to restore it
     */
    public static final LiaisonStatus RECONNECTING = new LiaisonStatus("RECONNECTING");
    /**
     * Liaison is connected and ready to receive requests
     */
    public static final LiaisonStatus READY = new LiaisonStatus("READY");
    /**
     * Liaison is sending command now
     */
    public static final LiaisonStatus SENDING = new LiaisonStatus("SENDING");
    /**
     * Liaison is receiving data now. In this state it can send requests too
     */
    public static final LiaisonStatus RECEIVING = new LiaisonStatus("RECEIVING");
    /**
     * Liaison is disconnecting from the server. It's in this state after logout command
     */
    public static final LiaisonStatus DISCONNECTING = new LiaisonStatus("DISCONNECTING");
    /**
     * Status name
     */
    private String mStatusName;

    /**
     * Constructor guards other classes from trying to create this instance.
     *
     * @param aName
     */
    private LiaisonStatus(String aName) {
        mStatusName = aName;
    }

    /**
     * Compares two objects.
     *
     * @param aObj
     *
     * @return true if them are the same objects
     */
    public boolean equals(Object aObj) {
        if (aObj instanceof LiaisonStatus) {
            return mStatusName.equals(((LiaisonStatus) aObj).mStatusName);
        }
        return false;
    }

    /**
     * Returns name of Status.
     */
    public String getName() {
        return mStatusName;
    }

    /**
     * Returns name of Status.
     */
    public String toString() {
        return mStatusName;
    }
}
