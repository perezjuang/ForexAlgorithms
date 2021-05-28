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

import javax.swing.JMenu;

/**
 * Interface that is used to localize JMenu.
 *
 * @see UIManager
 */
public class MenuLocalizator implements ILocalizator {
    /**
     * Localized menu.
     */
    private JMenu mMenu;
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
     * @param aMenu      target menu
     * @param asLabel    id of the label that binds with target menu
     * @param asMnemonic id of the mnemonic that binds with target menu
     */
    public MenuLocalizator(JMenu aMenu, String asLabel, String asMnemonic) {
        //settings of fields
        mMenu = aMenu;
        msLabel = asLabel;
        msMnemonic = asMnemonic;
    }

    /**
     * Returns object that is localized by this localizator instance.
     */
    public Object getLocalizedObject() {
        return mMenu;
    }

    /**
     * Is called when current locale is changed.
     *
     * @param aMan resource manager
     */
    public void onChangeLocale(ResourceManager aMan) {
        //setting of the label
        if (msLabel != null) {
            mMenu.setText(aMan.getString(msLabel, msLabel));
        } else {
            mMenu.setText("");
        }

        //setting of the mnemonic
        if (msMnemonic != null) {
            String sMnemon = aMan.getString(msMnemonic);
            if (sMnemon != null) {
                mMenu.setMnemonic(sMnemon.charAt(0));
            }
        }
        mMenu.revalidate();
        mMenu.repaint();
    }
}
