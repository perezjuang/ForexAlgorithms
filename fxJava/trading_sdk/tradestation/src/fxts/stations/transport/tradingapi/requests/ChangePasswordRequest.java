/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/requests/ChangePasswordRequest.java#1 $
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
 * Created: Oct 17, 2008 11:11:18 AM
 *
 * $History: $
 */
package fxts.stations.transport.tradingapi.requests;

import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.other.UserRequest;
import fxts.stations.transport.BaseRequest;
import fxts.stations.transport.IReqCollection;
import fxts.stations.transport.IRequest;
import fxts.stations.transport.IRequester;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.TradingAPIException;
import fxts.stations.transport.tradingapi.TradingServerSession;

/**
 */
public class ChangePasswordRequest extends BaseRequest implements IRequester {
    private String mConfirmNewPassword;
    private String mNewPassword;
    private String mOldPassword;

    public LiaisonStatus doIt() throws LiaisonException {
        try {
            UserRequest ur = new UserRequest();
            ur.setUserRequestType(IFixDefs.USERREQUESTTYPE_CHANGEPASSWORD);
            ur.setPassword(mOldPassword);
            ur.setNewPassword(mNewPassword);
            TradingServerSession.getInstance().setPassword(mNewPassword);
            TradingServerSession.getInstance().send(ur);
            return LiaisonStatus.READY;
        } catch (Exception e) {
            e.printStackTrace();
            throw new TradingAPIException(e, "IDS_INVALID_REQUEST_FIELD");
        }
    }

    public IRequest getRequest() {
        return this;
    }

    public IRequester getSibling() {
        return null;
    }

    public void setConfirmNewPassword(String aConfirmNewPassword) {
        mConfirmNewPassword = aConfirmNewPassword;
    }

    public void setNewPassword(String aNewPassword) {
        mNewPassword = aNewPassword;
    }

    public void setOldPassword(String aOldPassword) {
        mOldPassword = aOldPassword;
    }

    public void toQueue(IReqCollection aQueue) {
        aQueue.add(this);
    }
}
