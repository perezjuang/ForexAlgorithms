/*
 * Copyright (c) 2005 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 */
package com.fxcm.external.api.util;

import com.fxcm.fix.FXCMOrdTypeFactory;
import com.fxcm.fix.OrdTypeFactory;
import com.fxcm.fix.trade.ExecutionReport;

/**
 * @author Andre Mermegas
 *         Date: Jul 25, 2005
 *         Time: 10:44:40 AM
 */
public class MessageAnalyzer {
    /**
     * Determine if Execution Report is a limit order
     *
     * @param aExe the reference object
     *
     * @return <code>true</code> if this object argument is; <code>false</code> otherwise.
     */
    public static boolean isLimitOrder(ExecutionReport aExe) {
        return FXCMOrdTypeFactory.LIMIT == aExe.getFXCMOrdType() && OrdTypeFactory.LIMIT == aExe.getOrdType();
    }

    /**
     * Determine if Execution Report is an open order
     *
     * @param aExe the reference object
     *
     * @return <code>true</code> if this object argument is; <code>false</code> otherwise.
     */
    public static boolean isOpenOrder(ExecutionReport aExe) {
        return OrdTypeFactory.PREVIOUSLY_QUOTED == aExe.getOrdType() && FXCMOrdTypeFactory.OPEN == aExe.getFXCMOrdType();
    }

    /**
     * Determine if Execution Report is a stop/limit close order
     *
     * @param aExe the reference object
     *
     * @return <code>true</code> if this object argument is; <code>false</code> otherwise.
     */
    public static boolean isStopLimitCloseOrder(ExecutionReport aExe) {
        return FXCMOrdTypeFactory.STOP == aExe.getFXCMOrdType() && OrdTypeFactory.STOP == aExe.getOrdType() ||
               FXCMOrdTypeFactory.LIMIT == aExe.getFXCMOrdType() && OrdTypeFactory.LIMIT == aExe.getOrdType();
    }

    /**
     * Determine if Execution Report is a stop/limit entry order
     *
     * @param aExe the reference object
     *
     * @return <code>true</code> if this object argument is; <code>false</code> otherwise.
     */
    public static boolean isStopLimitEntryOrder(ExecutionReport aExe) {
        return FXCMOrdTypeFactory.ENTRY_LIMIT == aExe.getFXCMOrdType()
               || FXCMOrdTypeFactory.ENTRY_STOP == aExe.getFXCMOrdType()
               || FXCMOrdTypeFactory.STOP_TRAILING_ENTRY == aExe.getFXCMOrdType()
               || FXCMOrdTypeFactory.LIMIT_TRAILING_ENTRY == aExe.getFXCMOrdType();
    }

    /**
     * Determine if Execution Report is a stop order
     *
     * @param aExe the reference object
     *
     * @return <code>true</code> if this object argument is; <code>false</code> otherwise.
     */
    public static boolean isStopOrder(ExecutionReport aExe) {
        return FXCMOrdTypeFactory.STOP == aExe.getFXCMOrdType() && OrdTypeFactory.STOP == aExe.getOrdType();
    }

    /**
     * Determine if Execution Report is a trailing stop order
     *
     * @param aExe the reference object
     *
     * @return <code>true</code> if this object argument is; <code>false</code> otherwise.
     */
    public static boolean isTrailingStopCloseOrder(ExecutionReport aExe) {
        return (FXCMOrdTypeFactory.TRAILING_STOP == aExe.getFXCMOrdType() || FXCMOrdTypeFactory.TRAILING_LIMIT == aExe.getFXCMOrdType()) && OrdTypeFactory.PEG == aExe.getOrdType();
    }
}
