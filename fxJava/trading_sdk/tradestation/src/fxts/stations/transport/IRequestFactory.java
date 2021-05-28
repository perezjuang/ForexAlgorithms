/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/IRequestFactory.java#1 $
 *
 * Copyright (c) 2006 FXCM, LLC.
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
 */
package fxts.stations.transport;

import fxts.stations.datatypes.Side;

/**
 * Interface IRequestFactory.<br>
 * <br>
 * This is interface of request factory which is used to create requests. Note
 * that all requests should be created by using this factory.
 * Instance of the factory can be received from the liaison.<br>
 * <br>
 *
 * @Creation date (9/4/2003 10:54 AM)
 */
public interface IRequestFactory {
    /**
     * Change Password
     *
     * @param aOldPassword
     * @param aNewPassword
     * @param aConfirmNewPassword
     *
     * @return
     */
    IRequest changePassword(String aOldPassword, String aNewPassword, String aConfirmNewPassword);

    /**
     * Creates ClosePositionRequest.
     *
     * @param aSender Object sends requests
     * @param asTicketID Position Ticket id
     * @param aCustomText
     * @param aAtMarket
     *
     * @return ClosePositionRequest
     */
    IRequest closePosition(String asTicketID, long alAmount, String aCustomText, int aAtMarket);

    /**
     * Create true market close position request
     *
     * @param aSender
     * @param aTicketID
     * @param aAmount
     * @param aCustomText
     *
     * @return
     */
    IRequest closeTrueMarket(String aTicketID, long aAmount, String aCustomText);

    /**
     * Creates CreateEntryOrderRequest.
     *
     * @param aSender Object sends requests
     * @param asCurrency Entry order currency pair
     * @param asAccount Account id
     * @param aSide Opeartion Side
     * @param adRate Entry order rate
     * @param alAmount Contract size
     * @param aCustomText
     *
     * @return CreateEntryOrder Request
     */
    IRequest createEntryOrder(
            String asCurrency,
            String asAccount,
            Side aSide,
            double adRate,
            long alAmount,
            String aCustomText);

    /**
     * Creates CreateMarketOrderRequest.
     *
     * @param aSender Object sends requests
     * @param asCurrency Market order currency pair
     * @param asAccount Account id
     * @param aSide Opeartion Side
     * @param alAmount Contract size
     * @param aCustomText
     * @param aAtMarketPoints
     *
     * @return CreateMarketOrder Request
     */
    IRequest createMarketOrder(
            String asCurrency,
            String asAccount,
            Side aSide,
            long alAmount,
            String aCustomText,
            int aAtMarketPoints);

    /**
     * Create RFQ
     *
     * @param aSender
     * @param aAccount
     * @param aCCY
     * @param aAmount
     *
     * @return RFQRequest
     */
    IRequest createRequestForQuote(String aAccount, String aCCY, long aAmount);

    /**
     * Creates CreateMarketOrderRequest.
     *
     * @param aSender Object sends requests
     * @param aCurrency Market order currency pair
     * @param aAccount Account id
     * @param aSide Opeartion Side
     * @param aCustomText
     *
     * @return CreateMarketOrder Request
     */
    IRequest createTrueMarketOrder(
            String aCurrency,
            String aAccount,
            Side aSide,
            long aAmount,
            String aCustomText);

    /**
     * Creates RemoveEntryOrderRequest.
     *
     * @param aSender
     * @param asOrderID Entry order ID
     *
     * @return RemoveEntryOrder Request
     */
    IRequest removeEntryOrder(String asOrderID);

    /**
     * Creates ResetStopLimitRequest.
     *
     * @param aSender Object sends requests
     * @param asTicketID Position Ticket id
     * @param abResetStop flag of do not use Stop-loss
     * @param abResetLimit flag of do not use limit
     * @param aTrailStop
     *
     * @return ResetStopLimit Request
     */
    IRequest resetStopLimit(
            String asTicketID,
            boolean abResetStop,
            boolean abResetLimit,
            int aTrailStop);

    /**
     * @param aSender
     * @param asTicketID
     * @param adStop
     * @param adLimit
     * @param aTrailStop
     */
    IRequest setStopLimitOrder(
            String asTicketID,
            double adStop,
            double adLimit,
            int aTrailStop);

    /**
     * Creates SetStopLimitRequest.
     *
     * @param aSender Object sends requests
     * @param asTicketID Position Ticket id
     * @param adStop Stop-loss value. If less than 0.0 than it's not used
     * @param adLimit Limit value. If less than 0.0 than it's not used
     * @param aTrailStep
     */
    IRequest setStopLimitPosition(
            String asTicketID,
            double adStop,
            double adLimit,
            int aTrailStop);

    /**
     * Creates UpdateEntryOrderRequest.
     *
     * @param aSender Object sends requests
     * @param asOrderID Entry order ID
     * @param adRate Entry order rate
     * @param alAmount Contract size
     * @param aCustomText
     *
     * @return UpdateEntryOrder Request
     */
    IRequest updateEntryOrder(
            String asOrderID,
            double adRate,
            long alAmount,
            String aCustomText);
}
