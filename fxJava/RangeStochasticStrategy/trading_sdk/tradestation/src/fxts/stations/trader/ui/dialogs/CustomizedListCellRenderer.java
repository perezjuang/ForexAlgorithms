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

import fxts.stations.trader.TradeApp;
import fxts.stations.util.ResourceManager;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.net.URL;

/**
 * Implementation of ListCellRenderer interface.
 * This class adds the icons to disabled list items.
 * <br>
 *
 * @Creation date (9/27/2003 4:15 PM)
 */
public class CustomizedListCellRenderer implements ListCellRenderer {
    /**
     * Default renderer.
     */
    private ListCellRenderer mDefaultRenderer;
    /**
     * Image icon for disable items.
     */
    private ImageIcon mDisableIcon;

    /**
     * Constructor.
     *
     * @param aDefaultRenderer default list cell renderer
     */
    public CustomizedListCellRenderer(ListCellRenderer aDefaultRenderer) {
        mDefaultRenderer = aDefaultRenderer;
    }

    /**
     * Loads icon for disabled menu item.
     *
     * @return loaded icon
     */
    private ImageIcon createImageIcon() {
        //get resouce manager
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        //sets icon to internal frame
        URL iconUrl = resmng.getResource("ID_DISABLED_ITEM_ICON");
        if (iconUrl != null) {
            return new ImageIcon(iconUrl);
        } else {
            return null;
        }
    }

    /**
     * Return a component that has been configured to display the specified value.
     *
     * @param aList The JList we're painting.
     * @param aValue The value returned by list.getModel().getElementAt(index).
     * @param aIndex The cells index.
     * @param aIsSelected True if the specified cell was selected.
     * @param aCellHasFocus True if the specified cell has the focus.
     */
    public Component getListCellRendererComponent(
            JList aList,
            Object aValue,
            int aIndex,
            boolean aIsSelected,
            boolean aCellHasFocus) {
        boolean useDefaultRenderer = aValue == null || ((Item) aValue).isEnabled();
        Component comp = mDefaultRenderer.getListCellRendererComponent(aList,
                                                                       aValue,
                                                                       aIndex,
                                                                       aIsSelected,
                                                                       aCellHasFocus);
        if (!useDefaultRenderer) {
            //creates icon if it needs
            if (mDisableIcon == null) {
                mDisableIcon = createImageIcon();
            }
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                //sets icon
                label.setIcon(mDisableIcon);
            }
            return comp;
        } else {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                //sets icon
                label.setIcon(null);
            }
            return comp;
        }
    }
}