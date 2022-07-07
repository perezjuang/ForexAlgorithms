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

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.EventObject;

public class PreferencesTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private AValueEditorPanel mEditorOldPanel;
    private AValueEditorPanel mEditorPanel;
    private boolean mbStupor;

    public void cancelCellEditing() {
        if (!isEditing()) {
            return;
        }
        mEditorPanel.endEditing(true);
        mEditorOldPanel = mEditorPanel;
        mEditorPanel = null;
        mbStupor = false;
//        System.out.println("("+mEditorOldPanel+").setInvalid(true)");
        mEditorOldPanel.setInvalid(true);
    }

    public Object getCellEditorValue() {
//        System.out.println("getCellEditorValue() = " + (mEditorPanel != null ? mEditorPanel.getValue() : null) );
        return isEditing() ? mEditorPanel.getValue() : wasEditing() ? mEditorOldPanel.getValue() : null;
    }

//Redefy precense method from DefaultCellEditor that it returned component

    //It force use Panel with any component how editor.
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column) {
        //Change component of value in depedence of isSelected
        mEditorOldPanel = null;
        if (value instanceof AValueEditorPanel) {
            mEditorPanel = (AValueEditorPanel) value;
//            System.out.println("mEditorPanel.setValueToEditor("+mEditorPanel.getValue()+")");
            mEditorPanel.setValueToEditor(mEditorPanel.getValue());
//            System.out.println("PreferencesTableCellEditor getEditedComponent()");
            mEditorPanel.beginEditing();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    mEditorPanel.requestFocus();
                }
            });
            return mEditorPanel.getEditedComponent();
        } else {
            throw new RuntimeException("getTableCellEditorComponent : invalid value = " + value);
        }
    }

    public boolean isEditing() {
        return mEditorPanel != null;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        boolean rc = !mbStupor ? super.shouldSelectCell(anEvent) : false;
        if (mbStupor) {
            mEditorPanel.requestFocus();
        }
        return rc;
    }

    public boolean stopCellEditing() {
        if (!isEditing()) {
            return true;
        }
        IValidator validator;
        if (mEditorPanel != null && mEditorPanel.isValueChanged()) {
/* TBD Simply types are killed by that  */
            try {
//                System.out.println("mEditorPanel.refreshValue()");
                mEditorPanel.refreshValue();
                mbStupor = false;
            } catch (ValidationException e) {
                JOptionPane.showMessageDialog(mEditorPanel.getRootPane(),
                                              //e.getMessage(),
                                              e.getLocalizedMessage(),
                                              "!!!!!",
                                              JOptionPane.ERROR_MESSAGE);
                mbStupor = true;
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        mEditorPanel.requestFocus();
                    }
                });
            }
/* */
        }
        if (!mbStupor) {
            mEditorPanel.endEditing(false);
            mEditorOldPanel = mEditorPanel;
            mEditorPanel = null;
            return super.stopCellEditing();
        }
        return false;
    }

    public boolean wasEditing() {
        return mEditorOldPanel != null;
    }
}