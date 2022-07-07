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
 *
 */
package fxts.stations.transport.tradingapi;

import fxts.stations.transport.LiaisonException;

public interface IApplication {
    void communicationBroken();

    void communicationConnecting();

    void communicationDisconnecting();

    void communicationError();

    void communicationEstablished();

    String getHostUrl(String asUserID) throws LiaisonException;

    String getProxyHost(String asUserID);

    int getProxyPort(String asUserID);

    String getProxyPwd(String asUserID);

    String getProxyUser(String asUserID);

    String getServerConfigFile(String asUserID);

    int getServerTcpTimeout(String asUserID);

    boolean isProxyUsed(String asUserID);
}