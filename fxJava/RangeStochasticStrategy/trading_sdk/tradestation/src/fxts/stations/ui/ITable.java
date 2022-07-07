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

/**
 * This interface is abstract table interface.
 */
public interface ITable {
    /**
     * Adds selection listener.
     * It will be notified when selection in the table is changed.
     *
     * @param aListener selection listener
     */
    void addSelectionListener(ITableSelectionListener aListener);

    /**
     * Returns name of the table.
     * It's assumed that all tables has different names.
     */
    String getName();

    /**
     * Returns selected (current) row.
     */
    int getSelectedRow();

    /**
     * Removes selection listener.
     *
     * @param aListener selection listener
     */
    void removeSelectionListener(ITableSelectionListener aListener);

    /**
     * Sets the selected row.
     * @param aRow
     */
    void setSelectedRow(int aRow);
}
