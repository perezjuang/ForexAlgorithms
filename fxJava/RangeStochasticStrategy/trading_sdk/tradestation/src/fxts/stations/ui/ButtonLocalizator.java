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

import fxts.stations.util.ResourceManager;

import javax.accessibility.AccessibleContext;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.net.URL;

/**
 * Class that is used to localize JButtons.
 *
 * @see UIManager
 */
public class ButtonLocalizator implements ILocalizator {
    /**
     * Localized button.
     */
    private JButton mItem;
    /**
     * Resource id of the description.
     */
    private String msDesc;
    /**
     * Resource id of the icon.
     */
    private String msIcon;
    /**
     * Resource id of the label.
     */
    private String msLabel;
    /**
     * Resource id of the tooltip.
     */
    private String msToolTip;

    /**
     * Constructor.
     *
     * @param aItem     target button
     * @param asLabel   id of the label that binds with target button
     * @param asIcon    id of the icon that binds with target button
     * @param asToolTip id of the tooltip message that binds with target button
     * @param asDesc    id of the descripton string (to show at status bar)
     *                  that binds with target button
     */
    public ButtonLocalizator(JButton aItem, String asLabel, String asIcon,
                             String asToolTip, String asDesc) {
        //settings of fields
        mItem = aItem;
        msLabel = asLabel;
        msIcon = asIcon;
        msToolTip = asToolTip;
        msDesc = asDesc;
    }

    /**
     * -Returns object that is localized by this localizator instance.
     */
    public Object getLocalizedObject() {
        return mItem;
    }

    /**
     * Is called when current locale is changed.
     *
     * @param aMan new resource manager
     */
    public void onChangeLocale(ResourceManager aMan) {
        //setting of the label
        if (msLabel != null) {
            mItem.setText(aMan.getString(msLabel, msLabel));
        } else {
            mItem.setText("");
        }

        //setting of the icon
        if (msIcon != null) {
            URL url = aMan.getResource(msIcon);
            if (url != null) {
                mItem.setIcon(new ImageIcon(url));
            }
        }

        //setting of the tool tip
        if (msToolTip != null) {
            mItem.setToolTipText(aMan.getString(msToolTip, msToolTip));
        } else {
            mItem.setToolTipText("");
        }

        //setting of the accessable description
        AccessibleContext ac = mItem.getAccessibleContext();
        if (msDesc != null) {
            ac.setAccessibleDescription(aMan.getString(msDesc, msDesc));
        } else {
            ac.setAccessibleDescription("");
        }
        mItem.revalidate();
        mItem.repaint();
    }
}
