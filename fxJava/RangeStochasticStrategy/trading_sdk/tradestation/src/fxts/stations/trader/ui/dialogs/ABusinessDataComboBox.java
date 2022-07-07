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

import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.SignalType;
import fxts.stations.util.Signaler;
import fxts.stations.util.signals.AddSignal;
import fxts.stations.util.signals.ChangeSignal;
import fxts.stations.util.signals.RemoveSignal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.accessibility.Accessible;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Color;
import java.awt.Component;

/**
 * Abstract class  ABusinessDataComboBox.<br>
 * Is developid for simpified creating the classes implementing the bussines data row selection combo boxes<br>
 * The idioma of classes extending ABusinessDataComboBox:.<br>
 * <pre>
 * public class  &lt;&lt;bussines data name&gt;&gt;sComboBox extends ABusinessDataComboBox {
 *     public AbstractComboBoxModel mModel;
 *    public &lt;&lt;bussines data name&gt;&gt;sComboBox() {
 *        super();
 *    }
 *    public String getSelected&lt;&lt;bussines data id name&gt;&gt;() {
 *        Object selectedItem = getSelectedItem();
 *        return selectedItem == null? null : selectedItem.toString();
 *    }
 *    public void select&lt;&lt;bussines data id name&gt;&gt;(String as&lt;&lt;bussines data id name&gt;&gt;) {
 *        if (as&lt;&lt;bussines data id name&gt;&gt; != null) {
 *            try {
 *              // Place here a code calculating comboBox position from value of as&lt;&lt;bussines data id name&gt;&gt;
 *                setSelectedIndex(&lt;&lt;calculated position&gt;&gt;);
 *                mDefaultActor.setEnabled(&lt;&lt;determination of enabling the parent dialog default button&gt;&gt;);
 *            } catch (Exception e) {
 *                e.printStackTrace();
 *            }
 *        }
 *    }
 *    public AbstractComboBoxModel getComboBoxModel() {
 *        if (mModel == null) {
 *            mModel = new Model();
 *        }
 *        return mModel;
 *    }
 *    public void subscribeBusinessData() throws Exception {
 *         //here  place a code subscribing this instance to needed Data bussines signaller for
 *         // ADD if it's needed
 *         // CHANGE if it's needed
 *         // REMOVE if it's needed
 *    }
 *    public void unsubscribeBusinessData() {
 *        // here place the code unsubscribing all subscription had been made
 *    }
 *    public boolean updateOnSignal(ChangeSignal aSignal, Item aItem) throws Exception {
 *         //here place the code of reaction on update signal. In most cases you need to
 *         //return true or false only. If it is true, the popup menu of combo box and combo box will repaint.
 *    }
 *    public boolean updateOnSignal(AddSignal aSignal, Item aItem) throws Exception {
 *         //here place the code of reaction on update signal. In most cases you need to
 *         //return true or false only. If it is true, the popup menu of combo box and combo box will repaint.
 *    }
 *    public boolean updateOnSignal(RemoveSignal aSignal, Item aItem) throws Exception {
 *         //here place the code of reaction on update signal. In most cases you need to
 *         //return true or false only. If it is true, the popup menu of combo box and combo box will repaint.
 *    }
 *    class Model extends AbstractComboBoxModel {
 *        public Object getElementAt(int index) {
 *            try {
 *                //here define the item, which will be shown in combo box, using the Business table
 * //dont use the new operator, use newItem instead
 *                return newItem(index, &lt;&lt;title will be shown&gt;&gt;, &lt;&lt;boolean flag of enabling&gt;&gt;);
 *            } catch (Exception e) {
 *                e.printStackTrace();
 *                return "???";
 *            }
 *        }
 *        public int getSize() {
 *             // return here amount of item, the size of Business table in most cases
 *        }
 *    }
 * }
 * </pre>
 * <br>
 * Creation date (9/27/2003 4:27 PM)
 */
public abstract class ABusinessDataComboBox extends KeySensitiveComboBox implements ISignalListener {
    /**
     * Default button of parent dialog
     */
    protected IDefaultActor mDefaultActor;
    /**
     * Logger
     */
    protected final Log mLogger;
    /**
     * Render that is responsible for item drawing
     */
    protected ListCellRenderer mRenderer;
    /**
     * Status of color and mDefaultActor control
     */
    private boolean mStatusEnabled = true;

    /**
     * Protected constructor
     */
    protected ABusinessDataComboBox() {
        mLogger = LogFactory.getLog(ABusinessDataComboBox.class);
    }

    /**
     * Method returns the combo box items data model
     */
    public abstract AbstractComboBoxModel getComboBoxModel();

    /**
     * Initiation the combo box.
     *
     * @param aDefaultActor instance of default actor
     */
    public void init(IDefaultActor aDefaultActor) {
        mDefaultActor = aDefaultActor;
        setModel(getComboBoxModel());
        try {
            mRenderer = new CustomizedListCellRenderer(getRenderer());
            setRenderer(mRenderer);
            subscribeBusinessData();
            Accessible acc = getAccessibleContext().getAccessibleChild(0);
            if (acc != null && acc instanceof JPopupMenu) {
                ((JPopupMenu) acc).addPopupMenuListener(new PopupMenuListener() {
                    public void popupMenuCanceled(PopupMenuEvent aEvent) {
                    }

                    public void popupMenuWillBecomeInvisible(PopupMenuEvent aEvent) {
                        Object itemObj = getSelectedItem();
                        if (itemObj instanceof Item) {
                            Item item = (Item) getSelectedItem();
                            if (item != null) {
                                setStatusEnabled(item.isEnabled());
                            }
                        }
                    }

                    public void popupMenuWillBecomeVisible(PopupMenuEvent aEvent) {
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns status of combo box enabled or disbled. That doesn't means that it is
     * enabled/diasbled control, the only its color. Aditional that means  enabling or
     * disabling default button of dialog
     */
    public boolean isStatusEnabled() {
        return mStatusEnabled;
    }

    /**
     * Set status of combo box enabled or disbled. That doesn't means that it become
     * enabled/diasbled control, the only color is changed. Aditional it made enabled or
     * disabled default button of dialog
     */
    public void setStatusEnabled(boolean aStatusEnabled) {
        setForeground(aStatusEnabled ? Color.BLACK : Color.GRAY);
        if (mDefaultActor != null) {
            mDefaultActor.setEnabled(aStatusEnabled);
        }
        mStatusEnabled = aStatusEnabled;
    }

    /**
     * Creates new Item if it is not exist.
     * The combo box model implementations use this method for
     * creation item.<br>
     * Using this method instead new Item is more preferable for next reason:
     * <ol>
     * <li>The endless amount of objects may allocated, because combo box model
     * is queried frequently</li>
     * <li>Using of this method allows correct refreshing the combo box
     * popup window in case of changing enable flag only.</li>
     * </ol>
     * <b>Note:</b>Do not need to analyse enable flag of item in case of this method
     * using. So do not need with implementation of updateOnSignal method in this case,
     * if their purposes are refreshing combo box popup relating to enable flag only
     *
     * @param aIndex zero based index of item in check box
     * @param aTitle title of item will be shown into combo box
     * @param aEnabled sign that item is enabled
     */
    public Item newItem(int aIndex, String aTitle, boolean aEnabled) {
        if (aIndex >= 0) {
            return new Item(aIndex, aTitle, aEnabled);
        } else {
            return null;
        }
    }

    /**
     * The reaction on signal from subscription. Don't override it
     */
    public void onSignal(Signaler aSrc, ISignal aSignal) {
        try {
            boolean shouldUpdate = false;
            if (aSignal.getType() == SignalType.CHANGE) {
                int index = aSignal.getIndex();
                Item item = (Item) getItemAt(index);
                shouldUpdate = updateOnSignal((ChangeSignal) aSignal, item);
            } else if (aSignal.getType() == SignalType.ADD) {
                shouldUpdate = updateOnSignal((AddSignal) aSignal, (Item) getItemAt(aSignal.getIndex()));
                //xxx reset, dont know why but this updates the dropdown correctly
                int index = getSelectedIndex();
                setSelectedIndex(-1);
                setSelectedIndex(index);
            } else if (aSignal.getType() == SignalType.REMOVE) {
                shouldUpdate = updateOnSignal((RemoveSignal) aSignal);
            }
            if (shouldUpdate) {
                refreshAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Refresh all the components of ComboBox*/
    public void refreshAll() {
        if (isPopupVisible()) {
            for (int i = 0; i < getAccessibleContext().getAccessibleChildrenCount(); i++) {
                Accessible acc = getAccessibleContext().getAccessibleChild(i);
                if (acc != null && acc instanceof Component) {
                    ((Component) acc).repaint();
                }
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Method suscribes instance to Business data table changing
     */
    public abstract void subscribeBusinessData() throws Exception;

    /**
     * Calls when a Remove Signal has come in
     */
    public boolean updateOnSignal(RemoveSignal aSignal) throws Exception {
        return true;
    }

    /**
     * Calls when a Change Signal has come in
     */
    public boolean updateOnSignal(ChangeSignal aSignal, Item aItem) throws Exception {
        return true;
    }

    /**
     * Calls when a Add Signal has come in
     */
    public boolean updateOnSignal(AddSignal aSignal, Item aItem) throws Exception {
        return true;
    }
}