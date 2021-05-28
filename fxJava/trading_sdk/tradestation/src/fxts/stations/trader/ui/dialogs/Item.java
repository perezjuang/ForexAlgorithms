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
 * Class Item.<br>
 * <br>
 * The class which instances present check boxes items.<br>
 * <br>
 *
 * @Creation date (9/27/2003 4:07 PM)
 */
public class Item {
    /**
     * index of item
     */
    private int mIndex;
    /**
     * sign that item is enabled
     */
    private boolean mbEnabled;
    /**
     * title of item will be shown in combo box
     */
    private String msTitle;

    /**
     * Constructor.
     *
     * @param aIndex    zero based index of item in check box
     * @param asTitle   title of item will be shown into combo box
     * @param abEnabled sign that item is enabled
     *
     * @return
     *
     * @throws
     */
    public Item(int aIndex, String asTitle, boolean abEnabled) {
        mIndex = aIndex;
        msTitle = asTitle;
        mbEnabled = abEnabled;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     */
    public boolean equals(Object aObject) {
        return aObject == null ? false : msTitle.equals(aObject.toString());
    }

    /**
     * Returns index of item
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * Sets index of item
     */
    public void setIndex(int aiIndex) {
        mIndex = aiIndex;
    }

    /**
     * Returns title of item will be shown in combo box
     */
    public String getTitle() {
        return msTitle;
    }

    /**
     * Checks that item is enabled
     */
    public boolean isEnabled() {
        return mbEnabled;
    }

    /**
     * Set that item is enabled
     */
    public void setEnabled(boolean abEnabled) {
        mbEnabled = abEnabled;
    }

    /**
     * Sets title of item will be shown in combo box
     */
    public void setTitle(String asTitle) {
        msTitle = asTitle;
    }

    /**
     * returns string will be shown in combo box
     */
    public String toString() {
        return msTitle;
    }
}
