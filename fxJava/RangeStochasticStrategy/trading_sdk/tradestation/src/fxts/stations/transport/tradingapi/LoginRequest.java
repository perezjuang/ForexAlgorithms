/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/LoginRequest.java#2 $
 *
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

import com.fxcm.GenericException;
import fxts.stations.transport.BaseRequest;
import fxts.stations.transport.IReqCollection;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequester;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;

/**
 * Class LoginRequest.<br>
 * <br>
 * It is responsible for:
 * <ul>
 * <li> storing user id and password; </li>
 * <li> preparing and sending to FX Trade Server appropriate for it login request object. </li>
 * </ul>
 * <br>
 * Creation date (9/5/2003 3:30 PM)
 */
public class LoginRequest extends BaseRequest implements IRequester {
    private String mConnectionName;
    private String mPWD;
    private String mSessionID;
    private String mUID;
    private String mURL;
    private String mCfgFile;

    /**
     * Constructor with package access.
     *
     * @param aUID
     * @param aUPWD
     * @param aConnectionName
     */
    public LoginRequest(String aUID, String aUPWD, String aConnectionName, String aURL, String aCfgFile) {
        mUID = aUID;
        mPWD = aUPWD;
        mConnectionName = aConnectionName;
        mURL = aURL;
        mCfgFile = aCfgFile;
    }

    public LiaisonStatus doIt() throws LiaisonException {
        try {
            TradingServerSession.getInstance().login(mUID, mPWD, mConnectionName, mURL, mCfgFile);
            mSessionID = TradingServerSession.getInstance().getSessionID();
        } catch (LiaisonException le) {
            throw le;
        } catch (GenericException ce) {
            throw new TradingAPIException(ce, "IDS_LOGIN_FAILURE");
        } catch (Exception e) {
            e.printStackTrace();
            throw new TradingAPIException(e, "IDS_CANNOT_GET_HOST_DESCRIPTOR");
        }
        return null;
    }

    /**
     * Returns connection name
     */
    public String getConnectionName() {
        return mConnectionName;
    }

    /**
     * Returns user password
     *
     * @return user password
     */
    public String getPWD() {
        return mPWD;
    }

    public IRequest getRequest() {
        return this;
    }

    public String getSessionID() {
        return mSessionID;
    }

    public void setSessionID(String aSessionID) {
        mSessionID = aSessionID;
    }

    public IRequester getSibling() {
        return null;
    }

    /**
     * Returns user id
     */
    public String getUID() {
        return mUID;
    }

    public String getURL() {
        return mURL;
    }

    public void setURL(String aURL) {
        mURL = aURL;
    }

    public void toQueue(IReqCollection aQueue) {
        aQueue.add(this);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("LoginRequest");
        sb.append(" --- begin superclass toString ");
        sb.append(super.toString());
        sb.append(" --- end superclass toString ");
        sb.append("{mConnectionName='").append(mConnectionName).append('\'');
        sb.append(", mPWD='").append(mPWD).append('\'');
        sb.append(", mSessionID='").append(mSessionID).append('\'');
        sb.append(", mUID='").append(mUID).append('\'');
        sb.append(", mURL='").append(mURL).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
