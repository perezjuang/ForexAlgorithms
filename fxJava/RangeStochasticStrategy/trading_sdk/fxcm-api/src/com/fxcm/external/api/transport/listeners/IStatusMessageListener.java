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

import com.fxcm.messaging.ISessionStatus;

/**
 * This listener recieves messages pertaining to the status of your current session.
 *
 * @author Andre Mermegas
 *         Date: Dec 16, 2004
 *         Time: 9:36:30 AM
 */
public interface IStatusMessageListener {
    /**
     * receive a message
     *
     * @param aStatus
     */
    void messageArrived(ISessionStatus aStatus);
}