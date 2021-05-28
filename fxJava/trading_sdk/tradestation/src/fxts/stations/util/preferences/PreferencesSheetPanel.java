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
package fxts.stations.util.preferences;

import fxts.stations.ui.AAJTable;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

public class PreferencesSheetPanel extends JScrollPane implements ILocaleListener {
    private static int cInitialHeight = UIManager.getInst()
            .createLabel()
            .getFontMetrics(new Font("dialog", 0, 12))
            .getHeight() + 2;
    private static int cDirtyCount;

    //    private Action mDefaultCancelAction;
    private Action mDefaultEditingCancelAction;
    private Action mDefaultExitAction;
    private PreferencesTableCellEditor mEditor;
    private final Log mLogger = LogFactory.getLog(PreferencesSheetPanel.class);
    private PreferencesDialog mParentDialog;
    private ResourceManager mResMan;
    private PropertiesSheet mSheet;
    private JTable mTable;
    private PrefTableModel mTableModel;
    private String mUserName;

    /**
     * Constructor PreferencesSheetPanel.
     *
     * @param aUserName parent dialog included this panel.
     */
    public PreferencesSheetPanel(String aUserName) {
        mUserName = aUserName;
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            mLogger.error("The fatal error");
            e.printStackTrace();
        }
        mResMan.addLocaleListener(this);

        //Define table
        mTableModel = new PrefTableModel();
        mTable = new AAJTable(mTableModel);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        PreferencesTableCellRenderer renderer = new PreferencesTableCellRenderer();
        mTable.setDefaultRenderer(AValueEditorPanel.class, renderer);
        mEditor = new PreferencesTableCellEditor();
        mTable.setDefaultEditor(AValueEditorPanel.class, mEditor);
        //Do not change columns order
        mTable.getTableHeader().setReorderingAllowed(false);
        //Assign exterior a table
        mTable.setBorder(new EtchedBorder());
        //Prepare to used Escape key
        mDefaultEditingCancelAction = SwingUtilities.getUIActionMap(mTable).get("cancel");
        AbstractAction exitAction = new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                if (mEditor.isEditing()) {
                    mEditor.cancelCellEditing();
                    if (mDefaultEditingCancelAction != null) {
                        mDefaultEditingCancelAction.actionPerformed(aEvent);
                    }
                } else {
                    mDefaultExitAction.actionPerformed(aEvent);
                }
            }
        };
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        mTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, "ExitAction");
        SwingUtilities.getUIActionMap(mTable).put("ExitAction", exitAction);
        super.setViewportView(mTable);
    }

    /**
     * @return stop editing
     */
    public boolean allowStopEditing() {
        return !mEditor.isEditing() || mEditor.stopCellEditing();
    }

    /**
     * cancel editing
     */
    public void cancelEditing() {
        if (mEditor.isEditing()) {
            mEditor.cancelCellEditing();
        }
    }

    protected static void clearDirtyCounter() {
        cDirtyCount = 0;
    }

    /**
     * @return sheet
     */
    public PropertiesSheet getSheet() {
        return mSheet;
    }

    /**
     * @return table
     */
    public JTable getTable() {
        return mTable;
    }

    /**
     * init table
     */
    public void initTable() {
        int width = -1;
        TableColumnModel columnModel = mTable.getColumnModel();
        if (columnModel.getColumnCount() > 1) {
            width = columnModel.getColumn(0).getPreferredWidth();
        }
        mTableModel.init();
        mTableModel.fireTableStructureChanged();
        if (width >= 0) {
            columnModel = mTable.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(width);
        }
    }

    /**
     * @return dirty status
     */
    public static boolean isDirty() {
        return cDirtyCount > 0;
    }

    /**
     * onChangeLocale
     * Called when current locale is changed.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        mTableModel.fireTableStructureChanged();
    }

    /**
     * Panels of complex types should remember about up level dialog
     *
     * @param aParentDialog dialog
     * @param aDefaultExitAction exitaction
     */
    public void setParentDialog(PreferencesDialog aParentDialog, Action aDefaultExitAction) {
        mParentDialog = aParentDialog;
        mDefaultExitAction = aDefaultExitAction;
    }

    /**
     * @param aPrefProperty pref property
     * @param aDirtyFlag dirtyflag
     */
    public static void setPropertyDirty(PrefProperty aPrefProperty, boolean aDirtyFlag) {
        if (aDirtyFlag) {
            if (!aPrefProperty.isDirty()) {
                cDirtyCount++;
                aPrefProperty.setDirty(true);
            }
        } else {
            if (aPrefProperty.isDirty()) {
                if (isDirty()) {
                    cDirtyCount--;
                }
                aPrefProperty.setDirty(false);
            }
        }
    }

    /**
     * setSheet.
     *
     * @param aSheet set sheet of data for this panel and displayed it.
     */
    public void setSheet(PropertiesSheet aSheet) {
        mSheet = aSheet;
        initTable();
    }

    /**
     * PrefTableModel
     * <br>Class tuned AbstractTableModel for really task.
     * <br>
     * <br>
     * Creation date (19.12.2003 20:36)
     */
    private class PrefTableModel extends AbstractTableModel {
        /**
         * Array stored renderer panels
         */
        private AValueEditorPanel[] mPanels;
        private Hashtable mParameters;

        /**
         * getColumnClass.
         *
         * @param aCol of column.
         *
         * @return for column 1 Class string, for column 2 Class AValueEditorPanel.
         */
        public Class getColumnClass(int aCol) {
            if (aCol == 0) {
                return super.getColumnClass(aCol);
            }
            return AValueEditorPanel.class;
        }

        /**
         * getColumnCount.
         *
         * @return really amount column in the table (2)
         */
        public int getColumnCount() {
            if (mSheet == null) {
                return 0;
            } else {
                return 2;
            }
        }

        /**
         * getColumnName.
         *
         * @param aCol of column.
         *
         * @return rendered name of column
         */
        public String getColumnName(int aCol) {
            if (aCol < 1) {
                return mResMan.getString("IDS_PROPERTY_HEADER");
            } else {
                return mResMan.getString("IDS_VALUE_HEADER");
            }
        }

        /**
         * getRowCount.
         *
         * @return number lines of current table in the sheet.
         */
        public int getRowCount() {
            if (mSheet == null) {
                return 0;
            } else {
                return mSheet.size();
            }
        }

        /**
         * getValueAt.
         * If called cell is empty, then adds in it from array,
         * and return contents in any case.
         *
         * @param aRow and Column, where getting value
         *
         * @return Object stored in this cell.
         */
        public Object getValueAt(int aRow, int aCol) {
            if (aCol != 1) {
                return " " + mSheet.get(aRow).getName();
            } else if (mPanels[aRow] != null) {
                if (mPanels[aRow].isInvalid()) {
                    mPanels[aRow].setInvalid(false);
                    if (!isDirty()) {
                        mPanels[aRow].getParentDialog().setApplyButtonEnable(false);
                        if (UserPreferences.getUserPreferences(mUserName).isDefault()) {
                            mPanels[aRow].getParentDialog().setResetButtonEnable(false);
                        }
                    }
                    //Escape key is pressed by user (refusal of entering)
                    mPanels[aRow].setValue(mSheet.get(aRow).getValue());
                    mPanels[aRow].refreshControls();
                }
                return mPanels[aRow];
            } else {
                Object oValue = mSheet.get(aRow).getValue();
                mPanels[aRow] = mSheet.get(aRow).getType().getRenderer(oValue);
                if (mPanels[aRow] == null) {
                    Exception exception = new Exception("-" + aRow + "-" + oValue.getClass().getName());
                    exception.printStackTrace();
                    mPanels[aRow] = null;
                }
                mPanels[aRow].setParameters(mParameters);
                mParameters.put(mPanels[aRow], aRow);
                mPanels[aRow].setValue(oValue);
                if (mParentDialog != null) {
                    //indicate where to report on completion of input for complex types
                    mPanels[aRow].setParentDialog(mParentDialog);
                } else {
                    mLogger.debug("!!! Calling of member function: "
                                  + "PreferencesSheetPanel::getValueAt before creating of dialog.");
                }
                //remember what property connect with this panel
                mPanels[aRow].setPrefProperty(mSheet.get(aRow));
                return mPanels[aRow];
            }
        }

        /**
         * init
         * Create new array determine dimension.
         */
        void init() {
            mTable.setRowHeight(cInitialHeight);
            mPanels = new AValueEditorPanel[getRowCount()];
            mParameters = new Hashtable();
            mParameters.put("table", mTable);
        }

        /**
         * Editable only second column
         */
        public boolean isCellEditable(int aRow, int aCol) {
            return aCol == 1;
        }

        /**
         * setValueAt.
         * Method called by model if in cell is changed value.
         *
         * @param aValue what is setting
         * @param aRow and Column where
         */
        public void setValueAt(Object aValue, int aRow, int aCol) {
            if (aCol != 1) {
                return;
            }
            if (aRow < 0 || aRow >= mSheet.size()) {
                return;
            }
            if (aValue != null) {
                PrefProperty property = mSheet.get(aRow);
                //for complex type avoid (values already equals) but for simple as once get
                if (!property.getValue().equals(aValue)) {
                    //Value is changed. Check this for further work
                    property.set(aValue);
                    setPropertyDirty(property, true);
                    if (mParentDialog != null) {
                        mParentDialog.setApplyButtonEnable(true);
                        mParentDialog.setCancelButtonEnable(true);
                    } else {
                        mLogger.debug("!!! Calling of function: "
                                      + "PreferencesSheetPanel::setValueAt before creating of dialog.");
                    }
                }
            }
        }
    }
}
