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
 * $History: $
 * 9/3/2003 Created by USHIK
 * 1/6/2004 USHIK Adds Connection name get and set methods
 * 12/8/2004    Andre Mermegas  -- added a get order by TradeID method
 */
package fxts.stations.transport;

import fxts.stations.core.Accounts;
import fxts.stations.core.ClosedPositions;
import fxts.stations.core.Messages;
import fxts.stations.core.OpenPositions;
import fxts.stations.core.Orders;
import fxts.stations.core.Rates;
import fxts.stations.core.Summaries;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Message;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;

import java.util.Date;

/**
 * Interface ITradeDesk.
 * <br>
 * This is trade desk object interface.
 * Trade desk contains business information (such as business tables) and this
 * interface allows to get and change these data.<br>
 * <br>
 *
 * @Creation date (9/3/2003 4:23 PM)
 */
public interface ITradeDesk {
    /**
     * Adds account.
     *
     * @param aAcc
     */
    void addAccount(Account aAcc);

    /**
     * Adds closed position to the trade desk.
     *
     * @param aPos
     */
    void addClosedPosition(Position aPos);

    /**
     * add message
     *
     * @param aMessage
     */
    void addMessage(Message aMessage);

    /**
     * Adds open position to the trade desk.
     *
     * @param aPos
     */
    void addOpenPosition(Position aPos);

    /**
     * Adds new order to trade desk.
     *
     * @param aOrder
     */
    void addOrder(Order aOrder);

    /**
     * Adds new rate to trade desk.
     *
     * @param aRate
     */
    void addRate(Rate aRate);

    void clear();

    /**
     * Returns specified account.
     *
     * @param asAccountID
     *
     * @return instance of class fxts.stations.datatypes.Account
     */
    Account getAccount(String asAccountID);

    /**
     * get accounts table
     */
    Accounts getAccounts();

    /**
     * get closed positions table
     */
    ClosedPositions getClosedPositions();

    /**
     * Returns current connection name
     */
    String getConnectionName();

    /**
     * Get the database name
     */
    String getDatabaseName();

    /**
     * return messages table
     */
    Messages getMessages();

    /**
     * Returns open position with specified ticket.
     *
     * @param asTicketID
     *
     * @return instance of class fxts.stations.datatypes.Position
     */
    Position getOpenPosition(String asTicketID);

    /**
     * Returns order with specified id.
     *
     * @param asOrderID
     *
     * @return instance of class fxts.stations.datatypes.Order
     */
    Order getOrder(String asOrderID);

    /**
     * Returns order with specified trade id.
     *
     * @param aTradeID
     *
     * @return instance of class fxts.stations.datatypes.Order
     */
    Order getOrderByTradeId(String aTradeID);

    /**
     * get Orders table
     */
    Orders getOrders();

    /**
     * get open positions table
     */
    OpenPositions getPositions();

    /**
     * Returns rate for the currency.
     *
     * @param asCurrency
     *
     * @return instance of class fxts.stations.datatypes.Rate
     */
    Rate getRate(String asCurrency);

    /**
     * return Rates table
     */
    Rates getRates();

    /**
     * Returns current value of server time as known on client side.
     */
    Date getServerTime();

    /**
     * return summaries table
     */
    Summaries getSummaries();

    /**
     * Returns current user name
     */
    String getUserName();

    /**
     * Removes position.
     *
     * @param asTicketID
     */
    void removeOpenPosition(String asTicketID);

    /**
     * Removes the order.
     *
     * @param asOrderID
     */
    void removeOrder(String asOrderID);

    /**
     * Sets current connection name
     */
    void setConnectionName(String asConnectionName);

    /**
     * @param aDatabaseName
     */
    void setDatabaseName(String aDatabaseName);

    /**
     * Sets current user name
     */
    void setUserName(String asUserName);

    /**
     * Synchronizes server time stored on the client side with actual server time.
     * Trade desk keeps current server time and updates it.
     *
     * @param adtTime
     */
    void syncServerTime(Date adtTime);

    /**
     * Updates account.
     *
     * @param aAcc
     */
    void updateAccount(Account aAcc);

    /**
     * Updates open position in the trade desk.
     *
     * @param aPos
     */
    void updateOpenPosition(Position aPos);

    /**
     * Updates order in the trade desk.
     *
     * @param aOrder
     */
    void updateOrder(Order aOrder);

    /**
     * Updates rate in the trade desk.
     *
     * @param aRate
     */
    void updateRate(Rate aRate);
}