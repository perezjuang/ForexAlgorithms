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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This singleton class allows to handle creation/destruction of table objects.
 */
public class TableManager {
    /**
     * The one and only instance of the manager.
     */
    private static TableManager cInst = null;
    /**
     * Vector of manager listeners.
     */
    private Vector mListeners = new Vector();
    /**
     * Tables hashtable. Key is table name.
     */
    private Hashtable mTables = new Hashtable();

    /**
     * Constructor
     */
    private TableManager() {
        cInst = this;
    }

    /**
     * Adds new table to the manager.
     *
     * @param aTable instance of the table
     */
    public void add(ITable aTable) {
        if (aTable != null && aTable.getName() != null) {
            Object oldTable = mTables.put(aTable.getName(), aTable);
            if (oldTable != null) {
                if (oldTable == aTable) {
                    return;
                }
                synchronized (mListeners) {
                    for (Enumeration e = mListeners.elements(); e.hasMoreElements();) {
                        ((ITableListener) e.nextElement()).onRemoveTable((ITable) oldTable);
                    }
                }
            }
            synchronized (mListeners) {
                for (Enumeration e = mListeners.elements(); e.hasMoreElements();) {
                    ((ITableListener) e.nextElement()).onAddTable(aTable);
                }
            }
        }
    }

    /**
     * Adds table listener.
     *
     * @param aListener table listener
     */
    public void addListener(ITableListener aListener) {
        if (aListener != null) {
            mListeners.add(aListener);
        }
    }

    /**
     * Returns instance of the manager.
     */
    public static TableManager getInst() {
        return cInst != null ? cInst : new TableManager();
    }

    /**
     * Removes table from the manager.
     *
     * @param aTable instance of the table
     */
    public void remove(ITable aTable) {
        if (aTable != null) {
            Object oldTable = mTables.remove(aTable.getName());
            if (oldTable != null) {
                synchronized (mListeners) {
                    for (Enumeration e = mListeners.elements(); e.hasMoreElements();) {
                        ((ITableListener) e.nextElement()).onRemoveTable((ITable) oldTable);
                    }
                }
            }
        }
    }

    /**
     * Removes table listener.
     *
     * @param aListener table listener
     */
    public void removeListener(ITableListener aListener) {
        mListeners.remove(aListener);
    }

    /**
     * Returns enumeration of all registered tables.
     * Implementation note: clone mTables and return elements of the clone vector.
     */
    public Enumeration tables() {
        Hashtable clone = (Hashtable) mTables.clone();
        return clone.elements();
    }
}