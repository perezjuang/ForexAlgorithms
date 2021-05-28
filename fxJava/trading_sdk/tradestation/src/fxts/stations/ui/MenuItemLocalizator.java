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
import javax.swing.JMenuItem;
import java.net.URL;

/**
 * Class that is used to localize JMenuItems.
 *
 * @see UIManager
 */
public class MenuItemLocalizator implements ILocalizator {
    /**
     * Localized menu item.
     */
    private JMenuItem mItem;
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
     * Resource id of the mnemonic.
     */
    private String msMnemonic;

    /**
     * Constructor
     *
     * @param aItem      target menu item
     * @param asLabel    id of the label that binds with target menu item
     * @param asIcon     id of the icon that binds with target menu item
     * @param asMnemonic id of the mnemonic that binds with target menu item
     * @param asDesc     id of the descripton string (to show at status bar)
     *                   that binds with target menu item
     */
    public MenuItemLocalizator(JMenuItem aItem, String asLabel, String asIcon,
                               String asMnemonic, String asDesc) {
        //settings of fields
        mItem = aItem;
        msLabel = asLabel;
        msIcon = asIcon;
        msMnemonic = asMnemonic;
        msDesc = asDesc;
    }

    /**
     * Returns object that is localized by this localizator instance.
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
        String sMnemon;

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

        //setting of the mnemonic
        if (msMnemonic != null) {
            sMnemon = aMan.getString(msMnemonic, msMnemonic);
            if (sMnemon != null) {
                mItem.setMnemonic(sMnemon.charAt(0));
            }
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
