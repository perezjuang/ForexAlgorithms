/*
 * $Header:$
 *
 * Copyright (c) 2008 FXCM, LLC.
 * 32 Old Slip, New York NY, 10005 USA
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
 *
 * Author: Andre Mermegas
 * Created: Nov 13, 2006 11:04:56 AM
 *
 * $History: $
 */
package fxts.stations.transport.tradingapi.processors;

import com.fxcm.fix.posttrade.RequestForPositionsAck;
import com.fxcm.messaging.ITransportable;
import fxts.stations.transport.tradingapi.TradingServerSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 */
public class RequestForPositionsAckProcessor implements IProcessor {
    private final Log mLogger = LogFactory.getLog(RequestForPositionsAckProcessor.class);

    public void process(ITransportable aTransportable) {
        TradingServerSession aTradingServerSession = TradingServerSession.getInstance();
        RequestForPositionsAck aAck = (RequestForPositionsAck) aTransportable;
        String mRequestID = aTradingServerSession.getRequestID();
        mLogger.debug("client: inc req pos ack = " + aAck);
        if (mRequestID.equals(aAck.getPosReqID())) {
            if (aAck.getTotalNumPosReports() == 0) {
                aTradingServerSession.doneProcessing();
            }
        }
    }
}
