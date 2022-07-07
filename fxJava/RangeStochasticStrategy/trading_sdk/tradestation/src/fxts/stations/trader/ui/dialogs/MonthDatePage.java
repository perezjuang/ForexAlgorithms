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
import java.util.Calendar;
import java.util.Date;

/**
 * Class MonthDatePage is used for getting report of one month.<br>
 * <br>
 * .<br>
 * <br>
 * Creation date (10/16/2003 10:40 AM)
 */
public class MonthDatePage extends ADatePage {
    private IChangeDateListener mCalendarListener = new IChangeDateListener() {
        public void onDateChange(Date aOldDate, Date aNewDate) {
            if (isShowing()) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(aNewDate);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                getCalendarComboEnd().setDate(calendar.getTime());
            }
        }
    };
    private KeySensitiveComboBox mMonthComboBox;
    private JLabel mMonthLabel;
    private JLabel mParametersLabel;
    private KeySensitiveComboBox mYearComboBox;
    private JLabel mYearLabel;

    /**
     * Constructor.
     *
     * @param aCurrentDate current date
     */
    public MonthDatePage(Date aCurrentDate) {
        super(aCurrentDate);
    }

    /**
     * Initiates page dates.
     */
    @Override
    public void init() {
        ResourceManager resMan = TradeApp.getInst().getResourceManager();
        setLayout(new RiverLayout());

        if (mYearComboBox == null) {
            mYearComboBox = getCalendarComboBegin().getYearComboBox();
        } else {
            remove(mYearComboBox);
        }

        if (mMonthComboBox == null) {
            mMonthComboBox = getCalendarComboBegin().getMonthComboBox();
        } else {
            remove(mMonthComboBox);
        }

        if (getCalendarComboBegin() == null) {
            setCalendarComboBegin(new CalendarComboBoxes(getCurrentDate()));
        }

        if (getCalendarComboEnd() == null) {
            setCalendarComboEnd(new CalendarComboBoxes(getCurrentDate()));
        }

        if (mParametersLabel == null) {
            String txt = resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_PARAMETERS");
            mParametersLabel = UIManager.getInst().createLabel(txt);
        } else {
            remove(mParametersLabel);
        }

        if (mMonthLabel == null) {
            mMonthLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_MONTH"));
        } else {
            remove(mMonthLabel);
        }

        if (mYearLabel == null) {
            mYearLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_YEAR"));
        } else {
            remove(mYearLabel);
        }

        getCalendarComboBegin().removeChangeListener(mCalendarListener);
        getCalendarComboBegin().addChangeListener(mCalendarListener);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getCalendarComboBegin().getDate());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        getCalendarComboBegin().setDate(calendar.getTime());
        getCalendarComboEnd().setDate(calendar.getTime());

        add(mParametersLabel);
        add("br left", mMonthLabel);
        add("tab hfill", mMonthComboBox);
        add("br left", mYearLabel);
        add("tab hfill", mYearComboBox);
        mMonthComboBox.setDialog(mDialog);
        mYearComboBox.setDialog(mDialog);
    }

    @Override
    public String getEndDate() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(mCalendarComboEnd.getDate());
        boolean month = instance.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH);
        boolean year = instance.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
        if (month && year) {
            return "now";
        } else {
            return mDateFormat.format(mCalendarComboEnd.getDate());
        }
    }

    /**
     * Sets owner dialog for this panel.
     *
     * @param aDialog owner dialog
     */
    @Override
    public void setDialog(ABaseDialog aDialog) {
        mDialog = aDialog;
    }
}
