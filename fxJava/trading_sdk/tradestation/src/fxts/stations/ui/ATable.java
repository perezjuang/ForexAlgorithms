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
package fxts.stations.ui;

import java.util.Vector;

/**
 * This is abstract implementation of the ITable interface.
 * This class implemants work with selection listeners.
 */
public abstract class ATable implements ITable {
    /**
     * Vector with selection listeners.
     */
    private Vector mSelectionListeners = new Vector();
    /**
     * Name of the table.
     */
    private String msName;

    /**
     * Constructor is protected and takes name of the table.
     *
     * @param asName table name
     */
    protected ATable(String asName) {
        msName = asName;
    }

    /**
     * Adds selection listener.
     * It will be notified when selection in the table is changed.
     *
     * @param aListener selection listener
     */
    public void addSelectionListener(ITableSelectionListener aListener) {
        if (aListener != null) {
            mSelectionListeners.add(aListener);
        }
    }

    /**
     * Notifies all listeners about changing of selection.
     *
     * @param aRow number of row
     */
    protected void fireChangeSelection(int[] aRow) {
        for (Object selectionListener : mSelectionListeners) {
            ITableSelectionListener tsl = (ITableSelectionListener) selectionListener;
            try {
                tsl.onTableChangeSelection(this, aRow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clear() {
        mSelectionListeners.clear();
    }

    /**
     * Returns name of table.
     */
    public String getName() {
        return msName;
    }

    /**
     * Removes selection listener.
     *
     * @param aListener selection listener
     */
    public void removeSelectionListener(ITableSelectionListener aListener) {
        mSelectionListeners.remove(aListener);
    }
}