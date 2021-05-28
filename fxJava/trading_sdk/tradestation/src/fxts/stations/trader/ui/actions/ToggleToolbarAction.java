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
package fxts.stations.trader.ui.actions;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.util.UserPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Andre Mermegas
 *         Date: Jan 13, 2006
 *         Time: 11:49:07 AM
 */
public class ToggleToolbarAction extends AbstractAction {
    public static final String HIDE_TOGGLE_TOOLBAR = "toggle.toolbar";

    public void actionPerformed(ActionEvent e) {
        JToolBar toolBar = TradeApp.getInst().getMainFrame().getToolBar();
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        if (toolBar.isVisible()) {
            uiPrefs.set(HIDE_TOGGLE_TOOLBAR, true);
            toolBar.setVisible(false);
        } else {
            uiPrefs.set(HIDE_TOGGLE_TOOLBAR, false);
            toolBar.setVisible(true);
        }
    }
}