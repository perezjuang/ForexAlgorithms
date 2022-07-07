/*
 * $Header:$
 *
 * Copyright (c) 2008 FXCM, LLC.
 * 32 Old Slip, New York NY, 10005 USA
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
 *
 * Author: Andre Mermegas
 * Created: Sep 18, 2006 3:23:44 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.actions;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.ui.StatusBar;
import fxts.stations.util.UserPreferences;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ToggleStatusBarAction extends AbstractAction {
    public static final String HIDE_TOGGLE_STATUSBAR = "toggle.statusbar";

    public void actionPerformed(ActionEvent aEvent) {
        StatusBar bar = TradeApp.getInst().getMainFrame().getStatusBar();
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        if (bar.isVisible()) {
            uiPrefs.set(HIDE_TOGGLE_STATUSBAR, true);
            bar.setVisible(false);
        } else {
            uiPrefs.set(HIDE_TOGGLE_STATUSBAR, false);
            bar.setVisible(true);
        }
    }
}
