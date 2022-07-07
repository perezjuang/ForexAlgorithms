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
 * 10/10/2006   Andre Mermegas: update frame fix
 *
 */
package fxts.stations.trader.ui.frames;

import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;

import javax.swing.table.AbstractTableModel;

/**
 * Abstract class for simplifying writing an inner &lt;&lt;ViewTableModel&gt;&gt; classes<br>
 * Generaly an implementation of ATableFrame need to implements next methods only:
 * <ol>
 * <li>constructor, which should call the superclass constructor</li>
 * <li>getRowCount</li>
 * <li>getValueAt</li>
 * </ol>
 * See the
 * <code>ATableFrame</code> for how to simplify &lt;&lt;ViewFrame&gt;&gt; implementation
 *
 * @Creation date (9/22/2003 12:55 PM)
 */
public abstract class AFrameTableModel extends AbstractTableModel implements ISignalListener {
    /**
     * Not sortable columns.
     */
    public static final int NOT_SORTABLE_COLUMN = 0;
    /**
     * Column with string values.
     */
    public static final int STRING_COLUMN = 1;
    /**
     * Column with int values.
     */
    public static final int INT_COLUMN = 2;
    /**
     * Column with double values.
     */
    public static final int DOUBLE_COLUMN = 3;
    /**
     * Column with date values.
     */
    public static final int DATE_COLUMN = 4;
    /**
     * Array of descriptions of columns.
     */
    private String[] mColDescriptors;
    /**
     * Array of names of columns.
     */
    private String[] mColumnNames;

    /**
     * Protected constructor.
     *
     * @param aResourceMan
     * @param aColDescriptors Two directional array, its each member repersents
     * the array of two String. The first from them is ID of resource, the second
     * one is default value
     */
    protected AFrameTableModel(ResourceManager aResourceMan, String[] aColDescriptors) {
        mColDescriptors = aColDescriptors.clone();
        mColumnNames = new String[aColDescriptors.length];
        setColunmNames(aResourceMan, aColDescriptors);
    }

    /**
     * sets the column name using Column Descriptors biderectional array and Resource Manager
     */
    private void setColunmNames(ResourceManager aResourceMan, String[] aColDescriptors) {
        if (aResourceMan != null) {
            for (int i = 0; i < aColDescriptors.length; i++) {
                mColumnNames[i] = aResourceMan.getString(aColDescriptors[i]);
            }
        }
    }

    /**
     * Returns columns count as it is defined by JTableModel interface
     */
    public int getColumnCount() {
        return mColDescriptors.length;
    }

    /**
     * Returns column name value as it is defined by JTableModel interface
     */
    public String getColumnName(int aiCol) {
        return mColumnNames[aiCol];
    }

    /**
     * Returns columns names.
     */
    public abstract int getColumnType(int aiCol);

    /**
     * This method is called when current locale of the aMan is changed and becomes aLocale.
     *
     * @param aResourceMan resource manager.
     */
    protected void onChangeLocale(ResourceManager aResourceMan) {
        //refreshes colunm names of table
        setColunmNames(aResourceMan, mColDescriptors);
        fireTableStructureChanged();
    }

    /**
     * This method is called when signal is fired.
     *
     * @param aSrc source of the signal
     * @param aSignal signal
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        if (aSignal == null) {
            return;
        }
        if (aSignal.getType() == SignalType.CHANGE) {
            fireTableRowsUpdated(aSignal.getIndex(), aSignal.getIndex());
        } else if (aSignal.getType() == SignalType.ADD) {
            fireTableRowsInserted(aSignal.getIndex(), aSignal.getIndex());
        } else if (aSignal.getType() == SignalType.REMOVE) {
            fireTableRowsDeleted(aSignal.getIndex(), aSignal.getIndex());
        }
    }
}
