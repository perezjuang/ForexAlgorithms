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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andre Mermegas
 *         Date: Oct 31, 2005
 *         Time: 12:19:10 PM
 */
public class OrdStatusRequestType {
    private static final Map TYPES = new HashMap();
    public static final OrdStatusRequestType CLORDID = new OrdStatusRequestType("CLORDID");
    public static final OrdStatusRequestType SECONDARYCLORDID = new OrdStatusRequestType("SECONDARYCLORDID");
    public static final OrdStatusRequestType ORDERID = new OrdStatusRequestType("ORDER_ID");

    private String mType;

    private OrdStatusRequestType(String aType) {
        mType = aType;
        TYPES.put(aType, this);
    }

    public static OrdStatusRequestType getCommandType(String aType) {
        return (OrdStatusRequestType) TYPES.get(aType);
    }

    public String toString() {
        return mType;
    }
}
