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
package fxts.stations.core;

/**
 * @author Andre Mermegas
 *         Date: Jan 16, 2006
 *         Time: 3:19:04 PM
 */
public class FXCMConnection {
    private String mTerminal;
    private String mUrl;
    private String mUsername;

    FXCMConnection(String aUsername, String aTerminal, String aUrl) {
        mUsername = aUsername;
        mTerminal = aTerminal;
        mUrl = aUrl;
    }

    public String getTerminal() {
        return mTerminal;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setTerminal(String aTerminal) {
        mTerminal = aTerminal;
    }

    public void setUrl(String aUrl) {
        mUrl = aUrl;
    }

    public void setUsername(String aUsername) {
        mUsername = aUsername;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("FXCMConnection");
        sb.append("{mTerminal='").append(mTerminal).append('\'');
        sb.append(", mUrl='").append(mUrl).append('\'');
        sb.append(", mUsername='").append(mUsername).append('\'');
        sb.append('}');
        return sb.toString();
    }
}