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
import java.util.Date;

/**
 * Class SingleDatePage is used for getting report of one day.<br>
 * Creation date (10/15/2003 8:41 PM)
 */
public class SingleDatePage extends ADatePage {
    private IChangeDateListener mCalendarListener = new IChangeDateListener() {
        public void onDateChange(Date aOldDate, Date aNewDate) {
            if (isShowing()) {
                getCalendarComboEnd().setDate(aNewDate);
            }
        }
    };
    private JPanel mCalendarPanel;
    private JLabel mParametersLabel;
    private JLabel mPeriodLabel;

    /**
     * Constructor.
     *
     * @param aCurrentDate current date
     */
    public SingleDatePage(Date aCurrentDate) {
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
        } else {
            getCalendarComboBegin().setDate(getCurrentDate());
        }

        if (getCalendarComboEnd() == null) {
            setCalendarComboEnd(new CalendarComboBoxes(getCurrentDate()));
        } else {
            getCalendarComboEnd().setDate(getCalendarComboBegin().getDate());
        }

        if (mParametersLabel == null) {
            String s = resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_PARAMETERS");
            mParametersLabel = UIManager.getInst().createLabel(s);
        } else {
            remove(mParametersLabel);
        }

        if (mPeriodLabel == null) {
            mPeriodLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_DATE"));
        } else {
            remove(mPeriodLabel);
        }

        if (mCalendarPanel == null) {
            mCalendarPanel = getCalendarComboBegin().getCalendarPanel();
        } else {
            remove(mCalendarPanel);
            mCalendarPanel = getCalendarComboBegin().getCalendarPanel();
        }

        getCalendarComboBegin().removeChangeListener(mCalendarListener);
        getCalendarComboBegin().addChangeListener(mCalendarListener);

        add(mParametersLabel);
        add("br", mPeriodLabel);
        add("hfill", mCalendarPanel);
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