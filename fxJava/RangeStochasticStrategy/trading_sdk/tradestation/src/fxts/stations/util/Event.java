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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Event class
 * Event is used with EventProcessor.
 * It encapsulates data sent from background thread
 * to the main dispatch thread and event recipient.
 * Data are stored as named key-value pairs.
 */
public class Event {
    /**
     * Event's data (parameters).
     */
    private Hashtable mParams = new Hashtable();
    /**
     * Recipient of the event.
     */
    private IEventRecipient mRecipient;
    /**
     * Type of the event.
     */
    private int miType;

    /**
     * Adds parameter to the event.
     *
     * @param asName name of the parameter
     * @param aValue value of the parameter
     */
    public void addParameter(String asName, Object aValue) {
        if (asName != null && aValue != null) {
            mParams.put(asName, aValue);
        }
    }

    /**
     * Returns specified parameter or null if not found.
     *
     * @param asName the name of the parameter to get
     *
     * @throws Exception if asName == null
     */
    public Object getParameter(String asName)
            throws Exception {
        return mParams.get(asName);
    }

    /**
     * Returns names of all parameters.
     */
    public Enumeration getParameterNames() {
        return mParams.keys();
    }

    /**
     * Returns the recipient of the event.
     */
    public IEventRecipient getRecipient() {
        return mRecipient;
    }

    /**
     * Returns the type of the event.
     */
    public int getType() {
        return miType;
    }

    /**
     * Sets the recipient of the event.
     */
    public void setRecipient(IEventRecipient aRecipient) {
        if (aRecipient != null) {
            mRecipient = aRecipient;
        }
    }

    /**
     * Sets the type of the event.
     */
    public void setType(int aiType) {
        miType = aiType;
    }
}
