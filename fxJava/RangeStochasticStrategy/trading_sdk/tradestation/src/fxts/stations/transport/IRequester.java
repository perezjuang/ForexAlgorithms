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
 * Interface IRequester.<br>
 * <br>
 * Unites the request and possibility of it executing.<br>
 * It is used for executing the request.
 * <br>
 *
 * @Creation date (9/4/2003 9:28 PM)
 */
public interface IRequester {
    /**
     * Executes the request
     *
     * @return Liaison Status
     */
    LiaisonStatus doIt() throws LiaisonException;

    /**
     * Returns parent if request is child of batch or this else
     */
    IRequest getRequest();

    /**
     * Returns next request of batch request
     */
    IRequester getSibling();

    /**
     * Adds itself or other objects implementing IRequester interface to
     * IReqCollection implementation passed as parameter.
     */
    void toQueue(IReqCollection aQueue);
}
