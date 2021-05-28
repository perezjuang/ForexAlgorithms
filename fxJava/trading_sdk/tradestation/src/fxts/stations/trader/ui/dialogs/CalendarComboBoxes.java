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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 * Class CalendarComboBoxes presents the group of combo boxes:<br>
 * <ol>
 * <li>Date combo box</li>
 * <li>Month combo box</li>
 * <li>Year combo box</li>
 * </ol>
 * It can produce eather whole panel with these combo boxes or each combo box alone
 * Creation date (10/15/2003 3:15 PM)
 */
public class CalendarComboBoxes {
    private final Log mLogger = LogFactory.getLog(CalendarComboBoxes.class);

    /**
     * Resource pairs for months.
     */
    private static final String[] MONTHS_NAMES = {"IDS_REPORTWIZARD_MONTH_JANUARY",
                                                   "IDS_REPORTWIZARD_MONTH_FEBRUARY",
                                                   "IDS_REPORTWIZARD_MONTH_MARCH",
                                                   "IDS_REPORTWIZARD_MONTH_APRIL",
                                                   "IDS_REPORTWIZARD_MONTH_MAY",
                                                   "IDS_REPORTWIZARD_MONTH_JUNE",
                                                   "IDS_REPORTWIZARD_MONTH_JULY",
                                                   "IDS_REPORTWIZARD_MONTH_AUGUST",
                                                   "IDS_REPORTWIZARD_MONTH_SEPTEMBER",
                                                   "IDS_REPORTWIZARD_MONTH_OCTOBER",
                                                   "IDS_REPORTWIZARD_MONTH_NOVEMBER",
                                                   "IDS_REPORTWIZARD_MONTH_DECEMBER"};
    /**
     * current selected date calendar
     */
    private final Calendar mCalendar;
    /**
     * new selected date calendar
     */
    private Calendar mCalendarNew;
    /**
     * current date
     */
    private Date mDate;
    /**
     * Date combo box instance
     */
    private KeySensitiveComboBox mDateComboBox;
    /**
     * array of ids and default names of month
     */
    private ItemListener mDateListener;
    /**
     * listeners of change date
     */
    private final Vector<IChangeDateListener> mListeners = new Vector<IChangeDateListener>();
    /**
     * Month combo box instance
     */
    private KeySensitiveComboBox mMonthComboBox;
    /**
     * Listener of month items.
     */
    private ItemListener mMonthItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                mCalendarNew.setTime(mCalendar.getTime());
                int iDay = mCalendarNew.get(Calendar.DAY_OF_MONTH);
                mCalendarNew.set(Calendar.DAY_OF_MONTH, mCalendarNew.getActualMinimum(Calendar.DAY_OF_MONTH));
                mCalendarNew.set(Calendar.MONTH, mMonthComboBox.getSelectedIndex());
                if (iDay > mCalendarNew.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    iDay = mCalendarNew.getActualMaximum(Calendar.DAY_OF_MONTH);
                }
                mCalendarNew.set(Calendar.DAY_OF_MONTH, iDay);
                changeDate(mCalendarNew.getTime());
                fillDateComboBox();
                mDateComboBox.setSelectedIndex(iDay - 1);
            }
        }
    };
    /**
     * Year combo box
     */
    private KeySensitiveComboBox mYearComboBox;
    /**
     * Listener of year items.
     */
    private ItemListener mYearItemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                mCalendarNew.setTime(mCalendar.getTime());
                int iDay = mCalendarNew.get(Calendar.DAY_OF_MONTH);
                mCalendarNew.set(Calendar.DAY_OF_MONTH, mCalendarNew.getActualMinimum(Calendar.DAY_OF_MONTH));
                mCalendarNew.set(Calendar.YEAR, mYearComboBox.getSelectedIndex() + mStartYear);
                if (iDay > mCalendarNew.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    iDay = mCalendarNew.getActualMaximum(Calendar.DAY_OF_MONTH);
                }
                mCalendarNew.set(Calendar.DAY_OF_MONTH, iDay);
                changeDate(mCalendarNew.getTime());
                fillDateComboBox();
                mDateComboBox.setSelectedIndex(iDay - 1);
            }
        }
    };
    /**
     * End year in list
     */
    private int mEndYear = 2020;
    /**
     * Start year in list
     */
    private int mStartYear = 1999;

    /**
     * Constructor that sets mCalendar with passed date.
     *
     * @param aDate - initial selected date
     */
    public CalendarComboBoxes(Date aDate) {
        mCalendar = Calendar.getInstance();
        mCalendarNew = Calendar.getInstance();
        mDate = aDate;
        mCalendar.setTime(mDate);
        initComboBoxes();
        setComboBoxes();
    }

    /**
     * Adds listener on change data
     * @param aListener
     */
    public void addChangeListener(IChangeDateListener aListener) {
        if (mListeners.indexOf(aListener) < 0) {
            mListeners.add(aListener);
        }
    }

    /**
     * Dispatches the date changes.
     *
     * @param aNewDate new value of the date
     */
    protected void changeDate(Date aNewDate) {
        Date oldDate;
        Date newDate;
        synchronized (mCalendar) {
            oldDate = mCalendar.getTime();
            mCalendar.setTime(aNewDate);
            newDate = mCalendar.getTime();
        }
        synchronized (mListeners) {
            for (int i = 0; i < mListeners.size(); i++) {
                mListeners.elementAt(i).onDateChange(oldDate, newDate);
            }
        }
    }

    /**
     * Fills the days combo box
     */
    private void fillDateComboBox() {
        if (mDateListener == null) {
            mDateListener = new ItemListener() {
                public void itemStateChanged(ItemEvent aEvent) {
                    if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                        mCalendarNew.setTime(mCalendar.getTime());
                        mCalendarNew.set(Calendar.DATE, mDateComboBox.getSelectedIndex() + 1);
                        changeDate(mCalendarNew.getTime());
                    }
                }
            };
        } else {
            mDateComboBox.removeItemListener(mDateListener);
        }
        int savedSelectedIndex = mDateComboBox.getSelectedIndex();
        int maxDay = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        mDateComboBox.removeAllItems();
        for (int i = 1; i <= maxDay; i++) {
            mDateComboBox.addItem(new Integer(i));
        }
        try {
            mDateComboBox.setSelectedIndex(savedSelectedIndex);
        } catch (Exception e) {
            mDateComboBox.setSelectedIndex(0);
        }
        mDateComboBox.addItemListener(mDateListener);
    }

    /**
     * Fills combobox with years.
     */
    private void fillYearComboBox() {
        if (mYearItemListener != null) {
            mYearComboBox.removeItemListener(mYearItemListener);
        }
        mYearComboBox.removeAllItems();
        for (int i = mStartYear; i <= mEndYear; i++) {
            mYearComboBox.addItem(new Integer(i));
        }
        int index = mCalendar.get(Calendar.YEAR) - mStartYear;
        if (index > 0 && index < mYearComboBox.getItemCount()) {
            mYearComboBox.setSelectedIndex(mCalendar.get(Calendar.YEAR) - mStartYear);
        }
        fillDateComboBox();
        if (mYearItemListener != null) {
            mYearComboBox.addItemListener(mYearItemListener);
        }
    }

    /**
     * Returns whole calendar panel
     * @return
     */
    public JPanel getCalendarPanel() {
        JPanel pane = new JPanel(new RiverLayout());
        pane.add(mDateComboBox);
        pane.add(mMonthComboBox);
        pane.add("hfill", mYearComboBox);
        String txt = TradeApp.getInst().getResourceManager().getString("IDS_REPORTWIZARD_TODAY");
        JButton todayButton = UIManager.getInst().createButton(txt);
        todayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                resetDate();
            }
        });
        pane.add(todayButton);
        return pane;
    }

    /**
     * Returns current selected date
     * @return
     */
    public Date getDate() {
        return mCalendar.getTime();
    }

    /**
     * Returns date combo box instance
     * @return
     */
    public KeySensitiveComboBox getDateComboBox() {
        return mDateComboBox;
    }

    /**
     * Returns end year in list
     * @return
     */
    public int getEndYear() {
        return mEndYear;
    }

    /**
     * Returns month combo box instance
     * @return
     */
    public KeySensitiveComboBox getMonthComboBox() {
        return mMonthComboBox;
    }

    /**
     * Returns start year in list
     * @return
     */
    public int getStartYear() {
        return mStartYear;
    }

    /**
     * Returns year combo box
     * @return
     */
    public KeySensitiveComboBox getYearComboBox() {
        return mYearComboBox;
    }

    /**
     * initiation of combo boxes
     */
    private void initComboBoxes() {
        mDateComboBox = new KeySensitiveComboBox();
        mMonthComboBox = new KeySensitiveComboBox();
        mYearComboBox = new KeySensitiveComboBox();
        try {
            ResourceManager resMan = TradeApp.getInst().getResourceManager();
            for (String month : MONTHS_NAMES) {
                mMonthComboBox.addItem(resMan.getString(month));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMonthComboBox.addItemListener(mMonthItemListener);
        fillYearComboBox();
    }

    /**
     * Removes listener on change data
     * @param aListener
     */
    public void removeChangeListener(IChangeDateListener aListener) {
        mListeners.remove(aListener);
    }

    /**
     * Dispatches the focus request to the first shown control
     */
    public void requestFocus() {
        if (mDateComboBox.isShowing()) {
            mDateComboBox.requestFocus();
        } else if (mMonthComboBox.isShowing()) {
            mMonthComboBox.requestFocus();
        } else if (mYearComboBox.isShowing()) {
            mYearComboBox.requestFocus();
        }
    }

    /**
     * Sets current selected date to current date
     */
    public void resetDate() {
        mCalendar.setTime(mDate);
        setComboBoxes();
    }

    /**
     * Sets combo boxes to current date
     */
    private void setComboBoxes() {
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DATE);
        int index = year - mStartYear;
        if (index > 0 && index < mYearComboBox.getItemCount()) {
            mYearComboBox.setSelectedIndex(index);
        }
        mMonthComboBox.setSelectedIndex(month);
        fillDateComboBox();
        mDateComboBox.setSelectedIndex(day - 1);
    }

    /**
     * Sets current selected date
     * @param aDate
     */
    public void setDate(Date aDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(aDate);
        int iSetYear = calendar.get(Calendar.YEAR);
        if (iSetYear < mStartYear || iSetYear > mEndYear) {
            return;
        }
        mCalendar.setTime(aDate);
        setComboBoxes();
    }

    /**
     * Returns current selected date
     * @param aDialog
     */
    public void setDialog(ABaseDialog aDialog) {
        mDateComboBox.setDialog(aDialog);
        mMonthComboBox.setDialog(aDialog);
        mYearComboBox.setDialog(aDialog);
    }

    /**
     * Sets end year in list
     * @param aEndYear
     */
    public void setEndYear(int aEndYear) {
        mEndYear = aEndYear;
        if (mCalendar.get(Calendar.YEAR) > mEndYear) {
            mCalendar.set(Calendar.YEAR, mEndYear);
        }
        fillYearComboBox();
    }

    /**
     * Sets start year in list
     * @param aStartYear
     */
    public void setStartYear(int aStartYear) {
        mStartYear = aStartYear;
        if (mCalendar.get(Calendar.YEAR) < mStartYear) {
            mCalendar.set(Calendar.YEAR, mStartYear);
        }
        fillYearComboBox();
    }

    /**
     * Sets today date
     * @param aDate
     */
    public void setTodayDate(Date aDate) {
        mDate = aDate;
    }
}
