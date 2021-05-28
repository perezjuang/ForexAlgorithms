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

import javax.swing.*;

/**
 * Abstract class AbstractComboBoxModel.<br>
 * <br>
 * Is used to implement two method of ComboBoxModel interface .<br>
 * <br>
 *
 * @Creation date (9/27/2003 4:07 PM)
 */
public abstract class AbstractComboBoxModel extends AbstractListModel implements ComboBoxModel {
    Object mSelectedItem;

    /**
     * Returns the selected item
     */
    public Object getSelectedItem() {
        return mSelectedItem;
    }

    /**
     * Sets the selected item.
     *
     * @param aSelectedItem currently selected item
     */
    public void setSelectedItem(Object aSelectedItem) {
        if (mSelectedItem != null && !mSelectedItem.equals(aSelectedItem) || mSelectedItem == null && aSelectedItem != null) {
            mSelectedItem = aSelectedItem;
            fireContentsChanged(this, -1, -1);
        }
    }
}