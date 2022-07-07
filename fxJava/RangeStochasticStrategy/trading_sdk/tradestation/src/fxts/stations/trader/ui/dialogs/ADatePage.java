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

import com.fxcm.fix.pretrade.TradingSessionStatus;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.transport.tradingapi.TradingServerSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JPanel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Abstract class ADatePage is based class for all date pages of wizard.<br>
 * <br>
 * .<br>
 * <br>
 * Creation date (10/15/2003 8:29 PM)
 */
public abstract class ADatePage extends JPanel {
    protected final SimpleDateFormat mDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    protected CalendarComboBoxes mCalendarComboBegin;
    protected CalendarComboBoxes mCalendarComboEnd;
    protected Date mCurrentDate;
    protected ABaseDialog mDialog;
    protected final Log mLogger = LogFactory.getLog(ADatePage.class);

    protected ADatePage(Date aCurrentDate) {
        setCurrentDate(aCurrentDate);
    }

    /**
     * Returns begining date
     *
     * @return
     */
    public String getBeginDate() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(mCalendarComboBegin.getDate());
        boolean date = instance.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE);
        boolean month = instance.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH);
        boolean year = instance.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
        if (date && month && year) {
            TradingSessionStatus tss = TradingServerSession.getInstance().getTradingSessionStatus();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone(tss.getFXCMServerTimeZoneName()));
            String etd = TradingServerSession.getInstance().getParameterValue("END_TRADING_DAY");
            String now = sdf.format(new Date());
            if (now.compareTo(etd) > 0) {
                instance.roll(Calendar.DATE, true);
            }
            return mDateFormat.format(instance.getTime());
        } else {
            return mDateFormat.format(mCalendarComboBegin.getDate());
        }
    }

    /**
     * Returns calendar control of begining date
     *
     * @return
     */
    public CalendarComboBoxes getCalendarComboBegin() {
        return mCalendarComboBegin;
    }

    /**
     * Sets calendar control of begining date
     *
     * @param aCalendarComboBegin
     */
    public void setCalendarComboBegin(CalendarComboBoxes aCalendarComboBegin) {
        mCalendarComboBegin = aCalendarComboBegin;
    }

    /**
     * Returns calendar control of ending date
     *
     * @return
     */
    public CalendarComboBoxes getCalendarComboEnd() {
        return mCalendarComboEnd;
    }

    /**
     * Sets calendar control of ending date
     *
     * @param aCalendarComboEnd
     */
    public void setCalendarComboEnd(CalendarComboBoxes aCalendarComboEnd) {
        mCalendarComboEnd = aCalendarComboEnd;
    }

    /**
     * Returns  current date
     *
     * @return
     */
    public Date getCurrentDate() {
        return mCurrentDate;
    }

    /**
     * Sets Current date
     *
     * @param aCurrentDate
     */
    public void setCurrentDate(Date aCurrentDate) {
        mCurrentDate = aCurrentDate;
    }

    /**
     * Returns ending date
     *
     * @return
     */
    public String getEndDate() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(mCalendarComboEnd.getDate());
        boolean date = instance.get(Calendar.DATE) == Calendar.getInstance().get(Calendar.DATE);
        boolean month = instance.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH);
        boolean year = instance.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR);
        if (date && month && year) {
            return "now";
        } else {
            return mDateFormat.format(mCalendarComboEnd.getDate());
        }
    }

    /**
     * Initiates page dates
     */
    public abstract void init();

    /**
     * Returns current selected date
     *
     * @param aDialog
     */
    public abstract void setDialog(ABaseDialog aDialog);
}
