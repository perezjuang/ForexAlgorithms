/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/fxcm-api/src/main/com/fxcm/external/api/transport/exception/SessionNotEstablishedException.java#1 $
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
 * Author:  Andre Mermegas
 * Created: Nov 21, 2008 11:28:18 AM
 *
 * $History: $
 */
package com.fxcm.external.api.transport.exception;

/**
 */
public class SessionNotEstablishedException extends RuntimeException {
    public SessionNotEstablishedException() {
    }

    public SessionNotEstablishedException(String aMessage) {
        super(aMessage);
    }

    public SessionNotEstablishedException(String aMessage, Throwable aCause) {
        super(aMessage, aCause);
    }

    public SessionNotEstablishedException(Throwable aCause) {
        super(aCause);
    }
}