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

/**
 * interface IRequestSender.<br>
 * <br>
 * Each class that sends requests to trade server should specify IRequestSender
 * interface. It's used to notify sender when request has completed
 * (requests are async).<br>
 * <br>
 *
 * @Creation date (9/3/2003 6:29 PM)
 */
public interface IRequestSender {
    /**
     * This method is called when sent request is completed.
     *
     * @param aRequest  Request that has been completed
     * @param aResponse Server response
     */
    void onRequestCompleted(IRequest aRequest,
                            IResponse aResponse);

    /**
     * This method is called when sent request is failed.
     *
     * @param aRequest Request that has been failied
     * @param aEx      Exception that occurs
     */
    void onRequestFailed(IRequest aRequest,
                         LiaisonException aEx);
}
