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

import javax.swing.*;
import java.net.URL;

/**
 * This class is used to localize <code>Actions</code>.
 *
 * @see UIManager
 */
public class ActionLocalizator implements ILocalizator {
    /* -- Data fields -- */
    /**
     * Localized action.
     */
    private Action mAction;
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
     * Resource id of the tooltip.
     */
    private String msToolTip;

    /* -- Constructors -- */

    /**
     * Constructor.
     *
     * @param aAction    target action
     * @param asLabel    id of the label that binds with target action
     * @param asIcon     id of the icon that binds with target action
     * @param asMnemonic id of the mnemonic that binds with target action
     * @param asToolTip  id of the tooltip message that binds with target action
     * @param asDesc     id of the descripton string (to show at status bar)
     *                   that binds with target action
     */
    public ActionLocalizator(Action aAction, String asLabel, String asIcon,
                             String asMnemonic, String asToolTip, String asDesc) {
        //settings of fields
        mAction = aAction;
        msLabel = asLabel;
        msIcon = asIcon;
        msMnemonic = asMnemonic;
        msToolTip = asToolTip;
        msDesc = asDesc;
    }

    /* -- Public methods -- */

    /**
     * Returns object that is localized by this localizator instance.
     */
    public Object getLocalizedObject() {
        return mAction;
    }

    /**
     * Is called when current locale is changed.
     *
     * @param aMan new resource manager
     */
    public void onChangeLocale(ResourceManager aMan) {
        URL Url;

        //setting of the label
        if (msLabel != null) {
            mAction.putValue(Action.NAME, aMan.getString(msLabel, msLabel));
        } else {
            mAction.putValue(Action.NAME, "");
        }

        //setting of the icon
        if (msIcon != null) {
            Url = aMan.getResource(msIcon);
            if (Url != null) {
                mAction.putValue(Action.SMALL_ICON, new ImageIcon(Url));
            }
        }

        //setting of the mnemonic
        if (msMnemonic != null) {
            mAction.putValue(Action.MNEMONIC_KEY, new Integer(aMan.getString(msMnemonic, msMnemonic).charAt(0)));
        }

        //setting of the tooltip
        if (msToolTip != null) {
            mAction.putValue(Action.SHORT_DESCRIPTION, aMan.getString(msToolTip, msToolTip));
        } else {
            mAction.putValue(Action.SHORT_DESCRIPTION, "");
        }

        //setting of the accessable description
        if (msDesc != null) {
            mAction.putValue(Action.LONG_DESCRIPTION, aMan.getString(msDesc, msDesc));
        } else {
            mAction.putValue(Action.LONG_DESCRIPTION, "");
        }
    }
}