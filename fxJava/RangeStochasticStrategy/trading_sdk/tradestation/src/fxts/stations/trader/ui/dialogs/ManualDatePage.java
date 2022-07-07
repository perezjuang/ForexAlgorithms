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
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.Calendar;
import java.util.Date;

/**
 * Class ManualDatePage is used for getting report of manualy entered period.<br>
 * <br>
 * .<br>
 * <br>
 * Creation date (10/15/2003 8:41 PM)
 */
public class ManualDatePage extends ADatePage {
    private JPanel mCalendarPanelBegin;
    private JPanel mCalendarPanelEnd;
    private JLabel mFromLabel;
    private JLabel mParametersLabel;
    private JLabel mToLabel;

    /**
     * Constructor.
     *
     * @param aCurrentDate current date
     */
    public ManualDatePage(Date aCurrentDate) {
        super(aCurrentDate);
    }

    /**
     * Initiates page dates.
     */
    public void init() {
        ResourceManager resMan = TradeApp.getInst().getResourceManager();
        setLayout(new RiverLayout());

        if (getCalendarComboBegin() == null) {
            setCalendarComboBegin(new CalendarComboBoxes(getCurrentDate()));
        }

        if (getCalendarComboEnd() == null) {
            setCalendarComboEnd(new CalendarComboBoxes(getCurrentDate()));
        }

        if (mParametersLabel == null) {
            mParametersLabel = fxts.stations.ui.UIManager
                    .getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_PARAMETERS"));
        } else {
            remove(mParametersLabel);
        }

        if (mFromLabel == null) {
            mFromLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_FROM"));
        } else {
            remove(mFromLabel);
        }

        if (mToLabel == null) {
            mToLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_TO"));
        } else {
            remove(mToLabel);
        }

        if (mCalendarPanelBegin == null) {
            mCalendarPanelBegin = getCalendarComboBegin().getCalendarPanel();
        } else {
            remove(mCalendarPanelBegin);
            mCalendarPanelBegin = getCalendarComboBegin().getCalendarPanel();
        }
        if (mCalendarPanelEnd == null) {
            mCalendarPanelEnd = getCalendarComboEnd().getCalendarPanel();
        } else {
            remove(mCalendarPanelEnd);
            mCalendarPanelEnd = getCalendarComboEnd().getCalendarPanel();
        }

        getCalendarComboBegin().setDate(Calendar.getInstance().getTime());
        getCalendarComboEnd().setDate(Calendar.getInstance().getTime());

        add(mParametersLabel);
        add("br left", mFromLabel);
        add("tab hfill", mCalendarPanelBegin);
        add("br left", mToLabel);
        add("tab hfill", mCalendarPanelEnd);
    }

    /**
     * Sets focus on the receiving component .
     */
    public void requestFocus() {
        getCalendarComboEnd().requestFocus();
    }

    /**
     * Sets owner dialog for this panel.
     *
     * @param aDialog owner dialog
     */
    public void setDialog(ABaseDialog aDialog) {
        mDialog = aDialog;
    }
}