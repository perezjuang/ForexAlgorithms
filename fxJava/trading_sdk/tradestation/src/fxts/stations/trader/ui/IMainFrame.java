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
package fxts.stations.trader.ui;

import fxts.stations.util.ActionTypes;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Is used for hiding MainFrame instance from customers of MainFrame.<br>
 * <br>
 * The customers can be the a base class. Its childs are might be used by MainFrame
 * It is very importan that the base class doesn't have direct reference to main
 * frame because one child cannot be resolved without another else.
 * <br>
 *
 * @Creation date (11/4/2003 7:21 PM)
 */
public interface IMainFrame {
    /**
     * Checks for for presents of visible internal frame.
     */
    void checkForVisibleChilds();

    /**
     * Find the menu with specified name.
     *
     * @param aName name of the menu
     *              return founded menu and null if the menu not founded
     */
    JMenu findMenu(String aName);

    /**
     * Find the item with specified name.
     *
     * @param aName name of the menu item
     * @param aMenu place of the search
     *              return founded menu item and null if the menu item not founded
     */
    JMenuItem findMenuItem(String aName, JMenu aMenu);

    /**
     * Returns Action of specified type.
     *
     * @param aTypes    Action type
     * @param asCommand Action command
     *
     * @return aproppriate action
     */
    Action getAction(ActionTypes aTypes, String asCommand);
}