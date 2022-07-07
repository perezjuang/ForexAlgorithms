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
package fxts.stations.util;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.transport.tradingapi.TradingServerSession;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Vector;

public class Util {
    private static final DecimalFormat JPY_FORMAT = new ThreadSafeNumberFormat().getInstance();
    private static final DecimalFormat USD_FORMAT = new ThreadSafeNumberFormat().getInstance();

    static {
        JPY_FORMAT.applyPattern("#,##0");
        USD_FORMAT.applyPattern("#,##0.00");
    }

    /**
     * Parse double in a i18n friendly way
     *
     * @param aTxt string to parse into double
     *
     * @return double value of string
     */
    public static double parseDouble(String aTxt) {
        double ret;
        try {
            ret = Double.parseDouble(aTxt);
        } catch (Exception e) {
            synchronized (USD_FORMAT) {
                try {
                    ret = USD_FORMAT.parse(aTxt).doubleValue();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                    ret = 0;
                }
            }
        }
        return ret;
    }

    public static String format(double aDouble) {
        TimeZone tz = TimeZone.getTimeZone(TradingServerSession.getInstance().getParameterValue("BASE_TIME_ZONE"));
        if (TimeZone.getTimeZone("Japan").getDisplayName().equals(tz.getDisplayName())) {
            synchronized (JPY_FORMAT) {
                return JPY_FORMAT.format(aDouble);
            }
        } else {
            synchronized (USD_FORMAT) {
                return USD_FORMAT.format(aDouble);
            }
        }
    }

    /**
     * Sets given components preferred size and minimum width to the same as
     * the one with the max preferred size.
     *
     * @param aComponents JComponent[] the components who's size is checked and set.
     */
    public static void setAllToBiggest(JComponent[] aComponents) {
        Dimension widest = null;
        Dimension tallest = null;
        for (JComponent comp : aComponents) {
            Dimension dim = comp.getPreferredSize();
            if (widest == null || dim.width > widest.width) {
                widest = dim;
            }
            if (tallest == null || dim.height > tallest.height) {
                tallest = dim;
            }
        }
        for (JComponent comp : aComponents) {
            if (widest != null && tallest != null) {
                comp.setPreferredSize(new Dimension(widest.width, tallest.height));
                comp.setMinimumSize(new Dimension(widest.width, tallest.height));
            }
        }
    }

    /**
     * Splits a string into vector of strings by delim
     * ",0:proc_00,1,2:proc_02,3:,4:quer_04," returns "0:proc_00", "1", "2:proc_02", "4:quer_04"
     * ",,0:proc_00,,2:proc_02,3:,4:quer_04,,," returns "", "0:proc_00", "", "2:proc_02", "4:quer_04", "", ""
     *
     * @param aStr String to split
     * @param aDelim delimiter for separating tokens
     *
     * @return vector of strings
     */
    public static Vector split(String aStr, String aDelim) {
        return aStr == null || aDelim == null ? new Vector() : com.fxcm.util.Util.split(aStr, aDelim);
    }

    /**
     * Splits a string into array of strings by delim
     *
     * @param aStr String to split
     * @param aDelim delimiter for separating tokens
     *
     * @return array of strings
     */
    public static String[] splitToArray(String aStr, String aDelim) {
        Vector v = split(aStr, aDelim);
        String[] ret = new String[v.size()];
        v.copyInto(ret);
        return ret;
    }
}
