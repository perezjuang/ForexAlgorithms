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
 * This is selection listener interface for abstract tables ITable.
 */
public interface ITableSelectionListener {
    /**
     * This method is called when selection is changed.
     *
     * @param aTable table which row was changed
     * @param aiRow  changed row
     */
    public void onTableChangeSelection(ITable aTable, int[] aiRow);
}