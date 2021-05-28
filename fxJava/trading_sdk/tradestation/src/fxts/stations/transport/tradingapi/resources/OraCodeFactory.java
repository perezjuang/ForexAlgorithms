/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/transport/tradingapi/resources/OraCodeFactory.java#3 $
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
 * Created: Jan 31, 2008 3:01:41 PM
 *
 * $History: $
 */
package fxts.stations.transport.tradingapi.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class OraCodeFactory {
    private static final Map<String, String> CODES = new HashMap<String, String>();

    static {
        CODES.put("20001", "System error");
        CODES.put("20002", "Logical error");
        CODES.put("20003", "Access denied");
        CODES.put("20004", "Object does not exist");
        CODES.put("20005", "Object already exists");
        CODES.put("20006", "Wrong parameters");
        CODES.put("20007", "Unsupported feature");
        CODES.put("20008", "You cannot disable a major currency pair if you have one of its crosses enabled. First disable the crosses, and then disable the major pair.");
        CODES.put("20009", "You cannot use this Login ID, as it already exists in the system.");
        CODES.put("20010", "It is impossible to change a user that is currently connected to the system.");
        CODES.put("20011", "You cannot use this Login ID, as it already exists as a database user.");
        CODES.put("20012", "That is impossible to use predefined user profile");

        CODES.put("20100", "Internal error");
        CODES.put("20101", "Wrong user name or password");
        CODES.put("20102", "Access violation");
        CODES.put("20103", "Your connection has been lost. Please close this window and try to login again.");
        CODES.put("20104", "Your session has been lost by the database, possibly due to losing your internet connection. Please login again.");
        CODES.put("20105", "You are either trying to place an order too far away from the current market price, or you are trying to place a Stop or Limit order on the wrong side of the market price. Please change the rate or the type of order.");
        CODES.put("20106", "Your order did not execute because the Market Price has moved.");
        CODES.put("20107", "You cannot change the parameters of a Currency Offer if there are pending orders in the system.");
        CODES.put("20108", "You cannot change the parameters of a Currency Offer if there are open positions in the system.");
        CODES.put("20109", "Rate for this deal is not avaiable");
        CODES.put("20110", "Two dealers have tried to deactivate an order and process an order at the same time.");
        CODES.put("20111", "Quote is not assigned");

        CODES.put("20112", "The quoted price has expired and can no longer be hit.");
        CODES.put("20113", "You have insufficient available margin to open this position.");
        CODES.put("20114", "You can only place the same order from one trading station at a time.");
        CODES.put("20115", "You are trying to place a Stop or a Limit order on the wrong side of the market price. Please change the rate or the type of order.");
        CODES.put("20116", "You cannot move your oder price through an existing Stop or Limit. You must first change the rate for the Stop or Limit order.");
        CODES.put("20117", "You cannot disable this currency pair because there is an open position for this pair.");
        CODES.put("20118", "You cannot disable this currency pair because there is an open position for this pair.");
        CODES.put("20119", "You cannot disable this currency pair because you have just requested a quote on that pair.");
        CODES.put("20120", "You cannot change the rate on an order that has been rejected by the system.");
        CODES.put("20121", "You cannot change the rate on an order that is in the process of being executed.");
        CODES.put("20122", "You cannot change the rate on an order that has been rejected by the dealing desk.");
        CODES.put("20123", "This account is closed, so you cannot perform this operation at this time.");
        CODES.put("20124", "You cannot close this account because there is still an open position or a pending order in the system for this account.");
        CODES.put("20125", "You cannot delete and order that is in the process of being executed.");
        CODES.put("20126", "The Market is closed, so you will not be able to place or modify any orders at this time.");
        CODES.put("20127", "You do not have the sufficient rights to delete a margin call order.");

        CODES.put("20128", "Order have to be stop, limit or entry for this kind of operation");
        CODES.put("20129", "This currency pair must be subscribed to receive the live chart");
        CODES.put("20130", "Account not found");
        CODES.put("20131", "Account is locked. Trading is not available");
        CODES.put("20132", "This account has received a margin call and is still below the required margin level. Therefore, trading is currently unavailable.");
        CODES.put("20133", "Price change is out of range. Call Eddie immediately!");
        CODES.put("20134", "You cannot trade that many lots on this account. Please contact customer support if you feel there is a problem with your account.");
        CODES.put("20135", "You cannot delete an order that is in the process of being executed.");
        CODES.put("20136", "You cannot change the rate on an order that is in the process of being executed.");
        CODES.put("20137", "You cannot reject an order that is in the process of being executed.");
        CODES.put("20138", "Request a Quote trading is not available for this type of account.");
        CODES.put("20140", "You are not permitted to place market orders on this type of account.");
        CODES.put("20141", "You are not permitted to place conditional orders on this type of account.");
        CODES.put("20142", "Two dealers have tried to process the same order simultaneously.");
        CODES.put("20143", "The value for this order is either too far away from the market price, or it contains an invalid character.");
        CODES.put("20144", "This account is not permitted to buy SPOT options.");
        CODES.put("20147", "Function is impossible. Check no balance, trades or options on account.");
        CODES.put("20148", "This account has been locked due to an Equity Alert. You cannot open new positions during this trading session, but you can close any existing positions.");
        CODES.put("20149", "This account has been locked due to an Equity Stop. You can begin trading again normally after today's trading session has ended, and the new one has begun.");
        CODES.put("20152", "Price is expired.");
        CODES.put("20160", "The following special characters are prohibited:<>;`~!@#&$%^{}[]\\|?/. Please change name.");
        CODES.put("20164", "Incorrect PIN.  Please try again.");
        CODES.put("20165", "You must enter the correct PIN in your next attempt; otherwise you will be locked out of your account.");
        CODES.put("20166", "You have been locked out of your account; please contact us to regain access.");

        CODES.put("20168", "Your order could not execute because the selected ticket is already closed.");
        CODES.put("20169", "The selected order already has been removed.");
        CODES.put("20170", "Stop/Limit order already exists.");
        CODES.put("20171", "Duplicate_Trade");
        CODES.put("20172", "Minimum order quantity violated");
        CODES.put("20173", "Fraction of order quantity violated");
        CODES.put("20174", "You have been locked out of your account. Please contact us to regain access");
        CODES.put("20175", "Attemtping to connect from a disallowed IP address");
        CODES.put("20176", "Incomplete setup of one or more instruments neeed to correctly calculate floating PnL");
    }

    public static String toMessage(String aMessage) {
        try {
            Pattern pattern = Pattern.compile("ORA-(.*?):");
            Matcher matcher = pattern.matcher(aMessage);
            while (matcher.find()) {
                String grp = matcher.group(1);
                if (CODES.containsKey(grp)) {
                    return CODES.get(grp);
                }
            }
        } catch (Exception e) {
            //swallow
        }
        return aMessage;
    }
}
