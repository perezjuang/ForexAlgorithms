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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.Date;

/**
 * Class QuarterDatePage is used for getting report of Quarter year.<br>
 * <br>
 * .<br>
 * <br>
 * Creation date (10/16/2003 12:32 PM)
 */
public class QuarterDatePage extends ADatePage {
    private IChangeDateListener mCalendarListener = new IChangeDateListener() {
        public void onDateChange(Date aOldDate, Date aNewDate) {
            if (isShowing()) {
                getCalendarComboBegin().setDate(deriveBeginCal(aNewDate).getTime());
                getCalendarComboEnd().setDate(deriveEndCal(aNewDate).getTime());
            }
        }
    };
    private JLabel mPeriodParametersLabel;
    private KeySensitiveComboBox mQuarterComboBox;
    private JLabel mQuarterParametersLabel;
    private KeySensitiveComboBox mYearComboBox;
    private JLabel mYearLabel;

    /**
     * Constructor.
     *
     * @param aCurrentDate current date
     */
    public QuarterDatePage(Date aCurrentDate) {
        super(aCurrentDate);
    }

    /**
     * Sets begin date.
     *
     * @param aDate
     *
     * @return
     */
    private Calendar deriveBeginCal(Date aDate) {
        Calendar cal = Calendar.getInstance();
        if (aDate != null) {
            cal.setTime(aDate);
        }
        switch (mQuarterComboBox.getSelectedIndex()) {
            case 0:
                cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
                break;
            case 1:
                cal.set(Calendar.MONTH, Calendar.APRIL);
                break;
            case 2:
                cal.set(Calendar.MONTH, Calendar.JULY);
                break;
            default:
                cal.set(Calendar.MONTH, Calendar.OCTOBER);
                break;
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal;
    }

    /**
     * Sets end date.
     *
     * @param aDate
     *
     * @return
     */
    private Calendar deriveEndCal(Date aDate) {
        Calendar cal = Calendar.getInstance();
        if (aDate != null) {
            cal.setTime(aDate);
        }
        switch (mQuarterComboBox.getSelectedIndex()) {
            case 0:
                cal.set(Calendar.MONTH, Calendar.MARCH);
                break;
            case 1:
                cal.set(Calendar.MONTH, Calendar.JUNE);
                break;
            case 2:
                cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
                break;
            default:
                cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
                break;
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.YEAR, Integer.parseInt(mYearComboBox.getSelectedItem().toString()));
        return cal;
    }

    /**
     * Initiates page dates.
     */
    @Override
    public void init() {
        ResourceManager resMan = TradeApp.getInst().getResourceManager();
        setLayout(new RiverLayout());

        if (getCalendarComboBegin() == null) {
            setCalendarComboBegin(new CalendarComboBoxes(getCurrentDate()));
        }
        if (getCalendarComboEnd() == null) {
            setCalendarComboEnd(new CalendarComboBoxes(getCurrentDate()));
        }

        if (mPeriodParametersLabel == null) {
            String s = resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_PARAMETERS");
            mPeriodParametersLabel = UIManager.getInst().createLabel(s);
        } else {
            remove(mPeriodParametersLabel);
        }

        if (mQuarterParametersLabel == null) {
            String s = resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_QUARTER");
            mQuarterParametersLabel = UIManager.getInst().createLabel(s);
        } else {
            remove(mQuarterParametersLabel);
        }

        if (mYearLabel == null) {
            mYearLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_YEAR"));
        } else {
            remove(mYearLabel);
        }
        if (mQuarterComboBox == null) {
            mQuarterComboBox = new KeySensitiveComboBox();
            mQuarterComboBox.addItem(resMan.getString("IDS_REPORTWIZARD_FIRST_QUARTER"));
            mQuarterComboBox.addItem(resMan.getString("IDS_REPORTWIZARD_SECOND_QUARTER"));
            mQuarterComboBox.addItem(resMan.getString("IDS_REPORTWIZARD_THIRD_QUARTER"));
            mQuarterComboBox.addItem(resMan.getString("IDS_REPORTWIZARD_FOURTH_QUARTER"));
            mQuarterComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent aEvent) {
                    if (QuarterDatePage.this.isShowing()) {
                        if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                            getCalendarComboBegin().setDate(deriveBeginCal(null).getTime());
                            getCalendarComboEnd().setDate(deriveEndCal(null).getTime());
                        }
                    }
                }
            });
            mQuarterComboBox.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH) / 3);
        } else {
            remove(mQuarterComboBox);
        }

        if (mYearComboBox == null) {
            mYearComboBox = getCalendarComboBegin().getYearComboBox();
        } else {
            remove(mYearComboBox);
            mYearComboBox = getCalendarComboBegin().getYearComboBox();
        }

        getCalendarComboBegin().removeChangeListener(mCalendarListener);
        getCalendarComboBegin().addChangeListener(mCalendarListener);

        getCalendarComboBegin().setDate(deriveBeginCal(null).getTime());
        getCalendarComboEnd().setDate(deriveEndCal(null).getTime());

        add(mPeriodParametersLabel);
        add("br", mQuarterParametersLabel);
        add("tab hfill", mQuarterComboBox);
        add("br", mYearLabel);
        add("tab hfill", mYearComboBox);
        mQuarterComboBox.setDialog(mDialog);
        mYearComboBox.setDialog(mDialog);
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