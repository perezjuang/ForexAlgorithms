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
package fxts.stations.transport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract class ARequest.<br>
 * <br>
 * This is default abstract implementation of the request interface.<br>
 * <br>
 * Creation date (9/3/2003 7:14 PM)
 */
public class BaseRequest implements IRequest {
    protected final Log mLogger = LogFactory.getLog(BaseRequest.class);

    /**
     * Request sender
     */
    private IRequestSender mSender;

    public BaseRequest() {
        mSender = new DefaultRequestSender();
    }

    /**
     * Returns request sender
     */
    public IRequestSender getSender() {
        return mSender;
    }

    /**
     * Sets request sender
     *
     * @param aSender request sender
     */
    public void setSender(IRequestSender aSender) {
        /** Sets request sender */
        mSender = aSender;
    }
}