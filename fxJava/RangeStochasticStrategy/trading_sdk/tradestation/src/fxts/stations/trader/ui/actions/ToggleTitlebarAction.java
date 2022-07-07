/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/actions/ToggleTitlebarAction.java#1 $
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
 * Created: Oct 25, 2007 4:10:25 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.actions;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.frames.ATableFrame;
import fxts.stations.ui.ChildManager;
import fxts.stations.util.UserPreferences;

import javax.swing.AbstractAction;
import javax.swing.JInternalFrame;
import java.awt.event.ActionEvent;

/**
 */
public class ToggleTitlebarAction extends AbstractAction {
    public static final String HIDE_TOGGLE_TOOLBAR = "toggle.titlebars";

    public void actionPerformed(ActionEvent aEvent) {
        UserPreferences uiPrefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        ChildManager cm = TradeApp.getInst().getMainFrame().getChildManager();
        boolean b = uiPrefs.getBoolean(HIDE_TOGGLE_TOOLBAR);
        uiPrefs.set(HIDE_TOGGLE_TOOLBAR, !b);
        JInternalFrame[] allFrames = cm.getAllFrames();
        for (JInternalFrame aframe : allFrames) {
            if (aframe instanceof ATableFrame) {
                ATableFrame a = (ATableFrame) aframe;
                a.toggleTitleBar(!b);
            }
        }
    }
}
