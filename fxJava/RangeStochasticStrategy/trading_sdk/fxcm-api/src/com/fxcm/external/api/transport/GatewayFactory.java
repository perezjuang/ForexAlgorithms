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
package com.fxcm.external.api.transport;

import com.fxcm.internal.transport.FXCMGateway;


/**
 * This class creates gateways
 *
 * @author Andre Mermegas
 * Date: Dec 16, 2004
 * Time: 10:17:06 AM
 */
public class GatewayFactory {
    /**
     * This method returns an instance of IGateway for use.
     *
     * @return IGateway
     */
    public static IGateway createGateway() {
        return new FXCMGateway();
    }
}