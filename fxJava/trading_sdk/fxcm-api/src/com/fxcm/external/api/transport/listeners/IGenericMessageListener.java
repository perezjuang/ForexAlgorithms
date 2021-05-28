/*
 * Copyright (c) 2004 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 */
package com.fxcm.external.api.transport.listeners;

import com.fxcm.messaging.ITransportable;

/**
 * This listener gets all messages that are related to the trading
 * platform Quote,OrderSingle,ExecutionReport, etc...
 *
 * @author Andre Mermegas
 *         Date: Dec 16, 2004
 *         Time: 9:32:41 AM
 */
public interface IGenericMessageListener {
    /**
     * receive a message.
     *
     * @param aMessage
     */
    void messageArrived(ITransportable aMessage);
}