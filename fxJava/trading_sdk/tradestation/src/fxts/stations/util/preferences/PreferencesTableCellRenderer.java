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

import fxts.stations.util.IHeightVariableComponent;
import fxts.stations.util.InvokerSetRowHeight;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class PreferencesTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * Returns the default table cell renderer.
     *
     * @param table      the <code>JTable</code>
     * @param value      the value to assign to the cell at
     *                   <code>[row, column]</code>
     * @param isSelected true if cell is selected
     * @param isFocus    true if cell has focus
     * @param row        the row of the cell to render
     * @param column     the column of the cell to render
     *
     * @return the default table cell renderer
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component template = super.getTableCellRendererComponent(table, value,
                                                                 isSelected, hasFocus, row, column);
//System.out.println("getTableCellRendererComponent() value = \"" + value + "\" isSelected = " + isSelected + " hasFocus = " + hasFocus);
        if (value instanceof AValueEditorPanel) {
            Component component = (Component) value;
            if (!(isSelected && hasFocus)) {
                component = ((AValueEditorPanel) component).getNotEditedComponent();
            } else {
//                System.out.println("getTableCellRendererComponent getEditedComponent()");
                component = ((AValueEditorPanel) component).getEditedComponent();
            }
            component.setForeground(template.getForeground());
            component.setBackground(template.getBackground());
            if (component instanceof IHeightVariableComponent) {
                if (((IHeightVariableComponent) component).wasHeightChanged()) {
                    EventQueue.invokeLater(
                            new InvokerSetRowHeight(table, row, component.getHeight()));
                }
            }
            return component;
        } else {
            throw new RuntimeException("getTableCellRendererComponent : invalid value = " + value);
        }
    }
}