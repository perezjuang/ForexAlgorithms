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
 * Class HalfYearDatePage is used for getting report of half year.<br>
 * <br>
 * .<br>
 * <br>
 * Creation date (10/16/2003 11:47 AM)
 */
public class HalfYearDatePage extends ADatePage {
    private IChangeDateListener mCalendarListener = new IChangeDateListener() {
        public void onDateChange(Date aOldDate, Date aNewDate) {
            if (HalfYearDatePage.this.isShowing()) {
                getCalendarComboBegin().setDate(deriveBeginCal(aNewDate).getTime());
                getCalendarComboEnd().setDate(deriveEndCal(aNewDate).getTime());
            }
        }
    };
    private KeySensitiveComboBox mHalfYearComboBox;
    private JLabel mHalfYearLabel;
    private JLabel mParametersLabel;
    private KeySensitiveComboBox mYearComboBox;
    private JLabel mYearLabel;

    /**
     * Constructor.
     *
     * @param aCurrentDate current date
     */
    public HalfYearDatePage(Date aCurrentDate) {
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
        if (mHalfYearComboBox.getSelectedIndex() == 0) {
            cal.set(Calendar.MONTH, cal.getActualMinimum(Calendar.MONTH));
        } else {
            cal.set(Calendar.MONTH, 6);
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
        if (mHalfYearComboBox.getSelectedIndex() > 0) {
            cal.set(Calendar.MONTH, cal.getActualMaximum(Calendar.MONTH));
        } else {
            cal.set(Calendar.MONTH, 5);
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal;
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

        if (mHalfYearLabel == null) {
            mHalfYearLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_HALFYEAR"));
        } else {
            remove(mHalfYearLabel);
        }

        if (mYearLabel == null) {
            mYearLabel = UIManager.getInst().createLabel(resMan.getString("IDS_REPORTWIZARD_SET_PERIOD_YEAR"));
        } else {
            remove(mYearLabel);
        }

        if (mHalfYearComboBox == null) {
            mHalfYearComboBox = new KeySensitiveComboBox();
            mHalfYearComboBox.addItem(resMan.getString("IDS_REPORTWIZARD_FIRST_HALFYEAR"));
            mHalfYearComboBox.addItem(resMan.getString("IDS_REPORTWIZARD_SECOND_HALFYEAR"));
            mHalfYearComboBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent aEvent) {
                    if (HalfYearDatePage.this.isShowing()) {
                        if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                            getCalendarComboBegin().setDate(deriveBeginCal(null).getTime());
                            getCalendarComboEnd().setDate(deriveEndCal(null).getTime());
                        }
                    }
                }
            });
            mHalfYearComboBox.setSelectedIndex(Calendar.getInstance().get(Calendar.MONTH) / 6);
        } else {
            remove(mHalfYearComboBox);
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

        add(mParametersLabel);
        add("br", mHalfYearLabel);
        add("tab hfill", mHalfYearComboBox);
        add("br", mYearLabel);
        add("tab hfill", mYearComboBox);
        mHalfYearComboBox.setDialog(mDialog);
        mYearComboBox.setDialog(mDialog);
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