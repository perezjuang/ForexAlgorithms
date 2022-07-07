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
 * History:
 * 09/10/2003   ID   Created
 * 01/06/2004   USHIK Adds Connection name. msConnectionName property.
 * 12/08/2004   Andre Mermegas: added method to get order by tradeId
 * 12/10/2004   Andre Mermegas: updated the method that calculates PnL
 * 02/20/2006   Andre Mermegas: fix for ignoring java console frame if shown in webstart
 * 07/18/2006   Andre Mermegas: performance update
 * 12/08/2006   Andre Mermegas: update
 * 03/30/2007   Andre Mermegas: fixed bug in pnl, fixed dynamic updates to summary frame.
 */
package fxts.stations.core;

import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.NotDefinedException;
import com.fxcm.fix.TradingSecurity;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.ConversionRate;
import fxts.stations.datatypes.Message;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.trader.IFXTSConstants;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.frames.ATableFrame;
import fxts.stations.trader.ui.frames.AccountsFrame;
import fxts.stations.transport.ALiaisonListener;
import fxts.stations.transport.ITradeDesk;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.util.IServerTimeListener;
import fxts.stations.util.PersistentStorage;
import fxts.stations.util.ServerTime;
import fxts.stations.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Main core class.
 * It stores all business data and performs necessary their transformations.
 */
public class TradeDesk {
    private static final TradeDesk INSTANCE = new TradeDesk();
    private static final Map<Integer, DecimalFormat> PRICE_FORMATS = new HashMap<Integer, DecimalFormat>();

    static {
        DecimalFormat decimalFormat = new ThreadSafeNumberFormat().getInstance();
        decimalFormat.setMinimumFractionDigits(0);
        PRICE_FORMATS.put(0, decimalFormat);

        DecimalFormat decimalFormat1 = new ThreadSafeNumberFormat().getInstance();
        decimalFormat1.applyPattern("#.#");
        decimalFormat1.setMinimumFractionDigits(1);
        PRICE_FORMATS.put(1, decimalFormat1);

        DecimalFormat decimalFormat2 = new ThreadSafeNumberFormat().getInstance();
        decimalFormat2.applyPattern("#.##");
        decimalFormat2.setMinimumFractionDigits(2);
        PRICE_FORMATS.put(2, decimalFormat2);

        DecimalFormat decimalFormat3 = new ThreadSafeNumberFormat().getInstance();
        decimalFormat3.applyPattern("#.###");
        decimalFormat3.setMinimumFractionDigits(3);
        PRICE_FORMATS.put(3, decimalFormat3);

        DecimalFormat decimalFormat4 = new ThreadSafeNumberFormat().getInstance();
        decimalFormat4.applyPattern("#.####");
        decimalFormat4.setMinimumFractionDigits(4);
        PRICE_FORMATS.put(4, decimalFormat4);

        DecimalFormat decimalFormat5 = new ThreadSafeNumberFormat().getInstance();
        decimalFormat5.applyPattern("#.#####");
        decimalFormat5.setMinimumFractionDigits(5);
        PRICE_FORMATS.put(5, decimalFormat5);
    }

    /**
     * Collection of accounts.
     */
    private Accounts mAccounts = new Accounts();
    /**
     * Collection of closed Positions
     */
    private ClosedPositions mClosedPositions = new ClosedPositions();
    /**
     * Connection name.
     */
    private String mConnectionName;
    /**
     * Database name
     */
    private String mDatabaseName;
    private final Log mLogger = LogFactory.getLog(TradeDesk.class);
    /**
     * messages
     */
    private Messages mMessages = new Messages();
    /**
     * Collection of open positions.
     */
    private OpenPositions mOpenPositions = new OpenPositions();
    /**
     * Collection of orders.
     */
    private Orders mOrders = new Orders();
    /**
     * Collection of rates.
     */
    private Rates mRates = new Rates();
    /**
     * Current server time. It's valid only when in Connected state.
     */
    private final ServerTime mServerTime = ServerTime.UNKNOWN;
    /**
     * Vector of Server Time  listeners
     */
    private final Vector<IServerTimeListener> mServerTimeListeners = new Vector<IServerTimeListener>();
    /**
     * Thread that updates server time mServerTime.
     * It's started when liaison becomes in Connected state
     * and stops when liaison is disconnected.
     */
    private Thread mServerTimeThread;
    /**
     * Flag if the mServerTimeThread should stop
     */
    private boolean mStop;
    /**
     * Collection of summary info.
     */
    private Summaries mSummaries = new Summaries();
    /**
     * User name.
     */
    private String mUserName;
    /**
     * This is instance of the inner class that is used as intermediate
     * between TradeDesk and liaison (callback).
     */
    private TradeDeskWorker mWorker;

    /**
     * Constructor
     */
    private TradeDesk() {
        mWorker = new TradeDeskWorker();
        Liaison liaison = Liaison.getInstance();
        liaison.setTradeDesk(mWorker);
        liaison.addLiaisonListener(mWorker);
        try {
            PersistentStorage storage = PersistentStorage.getStorage();
            mUserName = storage.getString("UserName", "");
            mConnectionName = storage.getString("ConnectionName", "");
        } catch (Exception ex) {
            mLogger.error("Not got persistance storage!");
            ex.printStackTrace();
        }
    }

    /**
     * Adds Server Time listener to the trade desk.
     * It will get notified when event is arrived from the server.
     *
     * @param aListener Listener
     */
    public void addServerTimeListener(IServerTimeListener aListener) {
        if (aListener != null) {
            mServerTimeListeners.add(aListener);
        }
    }

    private static void calculatePipCost(Rate aRate) {
        AccountsFrame frame = TradeApp.getInst().getAccountsFrame();
        Account account = null;
        if (frame == null) {
            if (!INSTANCE.getAccounts().isEmpty()) {
                account = (Account) INSTANCE.getAccounts().get(0);
            }
        } else {
            account = frame.getSelectedAccount();
            if (account == null) {
                account = (Account) INSTANCE.getAccounts().get(0);
            }
        }
        double contractSize = aRate.getContractSize();
        double pipSize = TradeDesk.getPipsPrice(aRate.getCurrency());
        if (account != null) {
            if (aRate.getProduct() == IFixDefs.PRODUCT_CURRENCY || aRate.getProduct() == 0) {
                contractSize = account.getBaseUnitSize();
            }
        }
        String book = Liaison.getInstance().getAccountCurrency();
        String[] pair = TradeDesk.splitCurrencyPair(aRate.getCurrency());
        String ccy1 = null; //position
        String ccy2 = null; //tradable
        if (pair != null && pair.length == 2) {
            ccy1 = pair[0];
            ccy2 = pair[1];
        }
        // try and find the crosspairs
        Rates rates = INSTANCE.getRates();
        Rate simpleCross1 = rates.getRate(TradeDesk.toPair(ccy2, book));
        Rate simpleCross2 = rates.getRate(TradeDesk.toPair(book, ccy2));
        Rate unusualCross1 = rates.getRate(TradeDesk.toPair(ccy1, book));
        Rate unusualCross2 = rates.getRate(TradeDesk.toPair(book, ccy1));
        Rate simpleCFD1 = rates.getRate(TradeDesk.toPair(aRate.getContractCurrency(), book));
        Rate simpleCFD2 = rates.getRate(TradeDesk.toPair(book, aRate.getContractCurrency()));
        if (aRate.getProduct() != 0 && aRate.getProduct() != IFixDefs.PRODUCT_CURRENCY) {
            if (aRate.getContractCurrency().equals(book)) {
                aRate.setPipCost(contractSize);
            } else if (simpleCFD1 != null) {
                aRate.setPipCost(TradeDesk.findAverage(simpleCFD1) * contractSize);
            } else if (simpleCFD2 != null) {
                aRate.setPipCost(1 / TradeDesk.findAverage(simpleCFD2) * contractSize);
            }
        } else if (aRate.getCurrency().startsWith(book)) { //1
            double avg = TradeDesk.findAverage(aRate);
            double pipcost = contractSize * pipSize / avg;
            aRate.setPipCost(pipcost);
        } else if (aRate.getCurrency().endsWith(book)) { //2
            double pipcost = contractSize * pipSize;
            aRate.setPipCost(pipcost);
        } else if (simpleCross1 != null) { //3
            double avg = TradeDesk.findAverage(simpleCross1);
            double pipcost = contractSize * pipSize * avg;
            aRate.setPipCost(pipcost);
        } else if (simpleCross2 != null) { //3
            double avg = 1 / TradeDesk.findAverage(simpleCross2);
            double pipcost = contractSize * pipSize * avg;
            aRate.setPipCost(pipcost);
        } else if (unusualCross1 != null) { //4
            double avg = TradeDesk.findAverage(unusualCross1);
            double current = TradeDesk.findAverage(aRate);
            double pipCost = contractSize * pipSize * avg / current;
            aRate.setPipCost(pipCost);
        } else if (unusualCross2 != null) { //4
            double avg = TradeDesk.findAverage(unusualCross2);
            double current = TradeDesk.findAverage(aRate);
            double pipCost = contractSize * pipSize * avg / current;
            aRate.setPipCost(pipCost);
        } else { //5,6
            // exotic cross, try and find a major for conversions
            double toMajor = findConversionRateToMajor(aRate).getPrice();
            double tobook = findConversionRateToBook().getPrice();
            if (tobook != 0 && toMajor != 0) {
                double pipcost = contractSize * pipSize * toMajor * tobook;
                aRate.setPipCost(pipcost);
            }
        }
    }

    /**
     * Performs necessary cleanup actions.
     * Called before application exit.
     */
    public void cleanup() {
        Liaison liaison = Liaison.getInstance();
        liaison.setTradeDesk(null);
        liaison.removeLiaisonListener(mWorker);
    }

    public static double findAverage(Rate aRate) {
        return (aRate.getBuyPrice() + aRate.getSellPrice()) / 2;
    }

    public static ConversionRate findConversionRateToBook() {
        String book = Liaison.getInstance().getAccountCurrency();
        Rates rates = INSTANCE.getRates();
        // exotic cross, try and find a major for conversions
        String major1 = "USD";
        String major2 = "EUR";
        Rate major1Book = rates.getRate(TradeDesk.toPair(major1, book));
        Rate major2book = rates.getRate(TradeDesk.toPair(major2, book));
        Rate bookMajor1 = rates.getRate(TradeDesk.toPair(book, major1));
        Rate bookMajor2 = rates.getRate(TradeDesk.toPair(book, major2));
        double tobook = 0;
        ConversionRate cr = new ConversionRate();
        if (major1Book != null || major2book != null) {
            Rate r = major1Book;
            if (r == null) {
                r = major2book;
            }
            cr.setCurrency(r.getCurrency());
            tobook = TradeDesk.findAverage(r);
        } else if (bookMajor1 != null || bookMajor2 != null) {
            Rate r = bookMajor1;
            if (r == null) {
                r = bookMajor2;
            }
            cr.setCurrency(r.getCurrency());
            tobook = 1 / TradeDesk.findAverage(r);
        }

        if (tobook == 0) {
            // almost impossible exotic cross
            tobook = 0; //reset
            if (major1Book != null || major2book != null) {
                Rate r = major1Book;
                if (r == null) {
                    r = major2book;
                }
                cr.setCurrency(r.getCurrency());
                tobook = TradeDesk.findAverage(r);
            } else if (bookMajor1 != null || bookMajor2 != null) {
                Rate r = bookMajor1;
                if (r == null) {
                    r = bookMajor2;
                }
                cr.setCurrency(r.getCurrency());
                tobook = 1 / TradeDesk.findAverage(r);
            }
        }
        cr.setPrice(tobook);
        return cr;
    }

    public static ConversionRate findConversionRateToMajor(Rate aRate) {
        Rates rates = INSTANCE.getRates();
        // exotic cross, try and find a major for conversions
        String[] pair = TradeDesk.splitCurrencyPair(aRate.getCurrency());
        String ccy1 = null; //position
        String ccy2 = null; //tradable
        if (pair != null && pair.length == 2) {
            ccy1 = pair[0];
            ccy2 = pair[1];
        }
        String major1 = "USD";
        String major2 = "EUR";
        Rate exoticCross1 = rates.getRate(TradeDesk.toPair(ccy2, major1));
        Rate exoticCross2 = rates.getRate(TradeDesk.toPair(ccy2, major2));
        Rate exoticCross3 = rates.getRate(TradeDesk.toPair(major1, ccy2));
        Rate exoticCross4 = rates.getRate(TradeDesk.toPair(major2, ccy2));
        Rate ccy1Major1 = rates.getRate(TradeDesk.toPair(ccy1, major1));
        Rate ccy1major2 = rates.getRate(TradeDesk.toPair(ccy1, major2));
        Rate major1ccy1 = rates.getRate(TradeDesk.toPair(major1, ccy1));
        Rate major2ccy1 = rates.getRate(TradeDesk.toPair(major2, ccy1));
        double toMajor = 0;
        ConversionRate cr = new ConversionRate();
        if (exoticCross1 != null || exoticCross2 != null) {
            Rate r = exoticCross1;
            if (r == null) {
                r = exoticCross2;
            }
            cr.setCurrency(r.getCurrency());
            toMajor = TradeDesk.findAverage(r);
        } else if (exoticCross3 != null || exoticCross4 != null) {
            Rate r = exoticCross3;
            if (r == null) {
                r = exoticCross4;
            }
            cr.setCurrency(r.getCurrency());
            toMajor = 1 / TradeDesk.findAverage(r);
        }

        if (toMajor == 0) {
            // almost impossible exotic cross
            toMajor = 0; //reset
            if (ccy1Major1 != null || ccy1major2 != null) {
                Rate r = ccy1Major1;
                if (r == null) {
                    r = ccy1major2;
                }
                cr.setCurrency(r.getCurrency());
                toMajor = TradeDesk.findAverage(r);
            } else if (major1ccy1 != null || major2ccy1 != null) {
                Rate r = major1ccy1;
                if (r == null) {
                    r = major2ccy1;
                }
                cr.setCurrency(r.getCurrency());
                toMajor = 1 / TradeDesk.findAverage(r);
            }
        }
        cr.setPrice(toMajor);
        return cr;
    }

    /**
     * This method is used to format double values with pips precision
     * (price, rate, interest etc) to string representation
     * that will be shown in tables, dialogs and other UI controls.
     *
     * @param aCurrency currency pair
     * @param aPrice price to format
     *
     * @return formatted string of adPrice according to asCurrency
     */
    public static String formatPrice(String aCurrency, double aPrice) {
        if (aCurrency == null) {
            return null;
        }
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(aCurrency);
            int precision = security.getFXCMSymPrecision();
            return PRICE_FORMATS.get(precision).format(aPrice);
        } catch (Exception e) {
            //checks for infinity
            if (aPrice == Double.POSITIVE_INFINITY || aPrice == Double.NEGATIVE_INFINITY) {
                return "Infinity";
            }
            if (isCurrencyInThePair("JPY", aCurrency)) {
                return PRICE_FORMATS.get(3).format(aPrice);
            } else {
                return PRICE_FORMATS.get(5).format(aPrice);
            }
        }
    }

    public static double formatPrice2(String aCurrency, double aPrice) {
        if (aCurrency == null) {
            return 0;
        }
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(aCurrency);
            int precision = security.getFXCMSymPrecision();
            return Util.parseDouble(PRICE_FORMATS.get(precision).format(aPrice));
        } catch (Exception e) {
            if (isCurrencyInThePair("JPY", aCurrency)) {
                return Util.parseDouble(PRICE_FORMATS.get(3).format(aPrice));
            } else {
                return Util.parseDouble(PRICE_FORMATS.get(5).format(aPrice));
            }
        }
    }

    /**
     * Returns Accounts collection.
     *
     * @return Accounts
     */
    public Accounts getAccounts() {
        return mAccounts;
    }

    /**
     * Returns collection of closed Positions
     *
     * @return collection of closed Positions
     */
    public ClosedPositions getClosedPositions() {
        return mClosedPositions;
    }

    /**
     * Determines entry distance
     *
     * @return entry distance
     */
    public static double getConditionalDistance() {
        double condDist = 1; //default value of 1
        //check to see if we have a pip distance in tss
        String parameter = TradingServerSession.getInstance().getParameterValue("COND_DIST");
        if (parameter != null) {
            condDist = Util.parseDouble(parameter);
        }
        if (condDist == 0) {
            condDist = 1;
        }
        return condDist;
    }

    /**
     * Determines entry distance
     *
     * @return entry distance
     */
    public static double getConditionalEntryDistance() {
        double entryPipDistance = IFXTSConstants.RATE_DISPERSION; //default value of 5
        //check to see if we have a pip distance in tss
        String parameter = TradingServerSession.getInstance().getParameterValue("COND_DIST_ENTRY");
        if (parameter != null) {
            entryPipDistance = Util.parseDouble(parameter);
        }
        if (entryPipDistance == 0) {
            entryPipDistance = 1;
        }
        return entryPipDistance;
    }

    /**
     * Returns current connection name
     *
     * @return connection name
     */
    public String getConnectionName() {
        return mConnectionName;
    }

    /**
     * return database name
     *
     * @return db name
     */
    public String getDatabaseName() {
        return mDatabaseName;
    }

    /**
     * set database name
     *
     * @param aDatabaseName DatabaseName
     */
    public void setDatabaseName(String aDatabaseName) {
        mDatabaseName = aDatabaseName;
    }

    public static DecimalFormat getFormat(String aCurrency) {
        int precision;
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(aCurrency);
            precision = security.getFXCMSymPrecision();
        } catch (NotDefinedException e) {
            if (isCurrencyInThePair("JPY", aCurrency)) {
                precision = 3;
            } else {
                precision = 5;
            }
        }
        return PRICE_FORMATS.get(precision);
    }

    /**
     * Returns double value that corresponds to change of price on 1 pip for the currency.
     * For JPY containing pairs it's 0.001 for others it's 0.00001.
     *
     * @param aCurrency currency
     *
     * @return pip price
     */
    public static double getFractionalPipsPrice(String aCurrency) {
        if (aCurrency == null) {
            return 0.0;
        }
        try {
            TradingSessionStatus sessionStatus = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = sessionStatus.getSecurity(aCurrency);
            int precision = security.getFXCMSymPrecision();
            double pp = 1;
            for (int i = 0; i < precision; i++) {
                pp /= 10;
            }
            return pp;
        } catch (NotDefinedException e) {
            e.printStackTrace();
            double dblRet = 0.00001;
            if (isCurrencyInThePair("JPY", aCurrency)) {
                dblRet = 0.001;
            }
            return dblRet;
        }
    }

    /**
     * Returns the one and only TradeDesk instance.
     *
     * @return TradeDesk
     */
    public static TradeDesk getInst() {
        return INSTANCE;
    }

    public Messages getMessages() {
        return mMessages;
    }

    /**
     * Returns open positions collection.
     *
     * @return OpenPositions
     */
    public OpenPositions getOpenPositions() {
        return mOpenPositions;
    }

    /**
     * Returns Orders collection.
     *
     * @return Orders
     */
    public Orders getOrders() {
        return mOrders;
    }

    public static double getPipsPrice(String aCurrency) {
        if (isFractional(aCurrency)) {
            return getFractionalPipsPrice(aCurrency) * 10;
        } else {
            return getFractionalPipsPrice(aCurrency);
        }
    }

    /**
     * Returns Rates collection.
     *
     * @return Rates
     */
    public Rates getRates() {
        return mRates;
    }

    /**
     * Returns server time.
     *
     * @return Date
     */
    public Date getServerTime() {
        synchronized (mServerTime) {
            return (Date) mServerTime.clone();
        }
    }

    /**
     * Get spread
     *
     * @param aCurrency Currency
     *
     * @return Spread
     */
    public static double getSpread(String aCurrency) {
        Rate rate = INSTANCE.getRates().getRate(aCurrency);
        if (rate != null) {
            double spread = rate.getBuyPrice() - rate.getSellPrice();
            double pipsPrice = getPipsPrice(aCurrency);
            return formatPrice2(aCurrency, spread / pipsPrice);
        } else {
            return 0;
        }
    }

    /**
     * Returns Summary collection.
     *
     * @return Summaries
     */
    public Summaries getSummaries() {
        return mSummaries;
    }

    /**
     * Returns current user name
     *
     * @return username
     */
    public String getUserName() {
        return mUserName;
    }

    /**
     * Checks if specified currency pair contains specified currency
     *
     * @param aCurrency currency
     * @param aPair pair
     *
     * @return true if the pair contains the currency, otherwise false
     *         (if asCurrency or asPair or both equals null false returned)
     */
    public static boolean isCurrencyInThePair(String aCurrency, String aPair) {
        try {
            if (aCurrency == null || aPair == null) {
                return false;
            }
            String[] currencies = splitCurrencyPair(aPair);
            String base = currencies[0]; //position
            String counter = currencies[1]; //tradable
            return aCurrency.equalsIgnoreCase(base) || aCurrency.equalsIgnoreCase(counter);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isFractional(String aCurrency) {
        try {
            TradingSessionStatus tss = TradingServerSession.getInstance().getTradingSessionStatus();
            TradingSecurity security = tss.getSecurity(aCurrency);
            if (isCurrencyInThePair("JPY", aCurrency)) {
                return security.getFXCMSymPrecision() > 2;
            } else {
                return security.getFXCMSymPrecision() > 4;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Removes servertime listener from the trade desk.
     *
     * @param aListener Listener
     */
    public void removeServerTimeListener(IServerTimeListener aListener) {
        mServerTimeListeners.remove(aListener);
    }

    /**
     * Sets current connection name
     *
     * @param aConnectionName ConnectionName
     */
    public void setConnectionName(String aConnectionName) {
        mConnectionName = aConnectionName;
        try {
            PersistentStorage storage = PersistentStorage.getStorage();
            storage.set("ConnectionName", aConnectionName);
        } catch (Exception ex) {
            ex.printStackTrace();
            mLogger.error("Not got persistance storage!");
        }
    }

    /**
     * Sets current user name
     *
     * @param aUserName UserName
     */
    public void setUserName(String aUserName) {
        mUserName = aUserName;
        try {
            PersistentStorage storage = PersistentStorage.getStorage();
            storage.set("UserName", aUserName);
        } catch (Exception ex) {
            ex.printStackTrace();
            mLogger.error("Not got persistance storage!");
        }
    }

    /**
     * Splits specified currency pair into 2 currency names
     *
     * @param aPair currency pair
     *
     * @return array
     */
    public static String[] splitCurrencyPair(String aPair) {
        try {
            if (aPair == null) {
                return null;
            }
            String[] currencies = new String[2];
            int indexOfSlash = aPair.indexOf("/");
            currencies[0] = aPair.substring(0, indexOfSlash);
            currencies[1] = aPair.substring(indexOfSlash + 1, aPair.length());
            return currencies;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Merges 2 currencies into a pair and return the pair.
     *
     * @param aCCY1 1st currency to be in the pair
     * @param aCCY2 2nd currency to be in the pair
     *
     * @return currency pair or null CCY1 or CCY2 or both == null
     */
    public static String toPair(String aCCY1, String aCCY2) {
        if (aCCY1 == null || aCCY2 == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(aCCY1).append("/").append(aCCY2);
        return sb.toString();
    }

    public static void updatePipCosts() {
        Rates rates = INSTANCE.getRates();
        Enumeration<Rate> enumeration = rates.elements();
        while (enumeration.hasMoreElements()) {
            Rate rate = enumeration.nextElement();
            TradeDesk.calculatePipCost(rate);
        }
    }

    private class TradeDeskWorker extends ALiaisonListener implements ITradeDesk {
        /**
         * current liaison status
         */
        private LiaisonStatus mCurStat = LiaisonStatus.DISCONNECTED;

        /**
         * Adds account.
         *
         * @param aAcc account
         */
        public void addAccount(Account aAcc) {
            if (aAcc != null) {
                mAccounts.add(aAcc);
                updateAccountsFrameTitle();
                updateSummaryFrameTitle();
            }
        }

        /**
         * Adds closed position to the trade desk.
         *
         * @param aPos open position
         */
        public void addClosedPosition(Position aPos) {
            if (aPos != null) {
                mClosedPositions.add(aPos);
                updateClosedPositionFrameTitle();
            }
        }

        public void addMessage(Message aMessage) {
            if (aMessage != null) {
                mMessages.add(aMessage);
                updateMessagesFrameTitle();
            }
        }

        /**
         * Adds open position to the trade desk.
         *
         * @param aPos open position
         */
        public void addOpenPosition(Position aPos) {
            if (aPos != null) {
                mOpenPositions.add(aPos);
                updatePositionFrameTitle();
            }
        }

        /**
         * Adds new order to trade desk.
         *
         * @param aOrder order
         */
        public void addOrder(Order aOrder) {
            if (aOrder != null) {
                mOrders.add(aOrder);
                updateOrderFrameTitle();
            }
        }

        /**
         * Adds new rate to trade desk.
         *
         * @param aRate rate
         */
        public void addRate(Rate aRate) {
            if (aRate != null) {
                mRates.add(aRate);
            }
        }

        /**
         * Clears all business data.
         */
        public void clear() {
            mOrders.clear();
            updateOrderFrameTitle();

            mOpenPositions.clear();
            updatePositionFrameTitle();

            mAccounts.clear();
            updateAccountsFrameTitle();

            mSummaries.clear();
            updateSummaryFrameTitle();

            mClosedPositions.clear();
            updateClosedPositionFrameTitle();

            mMessages.clear();
            updateMessagesFrameTitle();
        }

        /**
         * Creates time thread.
         */
        void createServerTimeThread() {
            if (mServerTimeThread != null) {
                return;
            }
            mStop = false;
            mServerTimeThread = new Thread() {
                private final Object mObj = new Object();

                @Override
                public void run() {
                    while (!mStop) {
                        synchronized (mObj) {
                            try {
                                mObj.wait(1000);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        syncServerTime(mServerTime.getTime() + 1000);
                    }
                }
            };
            mServerTimeThread.start();
        }

        /**
         * Dispatches of setting of server time.
         */
        private void dispatchSetServerTime() {
            synchronized (mServerTimeListeners) {
                for (IServerTimeListener listener : mServerTimeListeners) {
                    listener.timeUpdated(mServerTime);
                }
            }
        }

        /**
         * Returns specified account.
         *
         * @param aAccountID id of the accont
         *
         * @return instance of class fxts.stations.datatypes.Account
         */
        public Account getAccount(String aAccountID) {
            return mAccounts.getAccount(aAccountID);
        }

        /**
         * get accounts table
         */
        public Accounts getAccounts() {
            return mAccounts;
        }

        public ClosedPositions getClosedPositions() {
            return mClosedPositions;
        }

        /**
         * Returns current connection name
         */
        public String getConnectionName() {
            return TradeDesk.this.getConnectionName();
        }

        public String getDatabaseName() {
            return TradeDesk.this.getDatabaseName();
        }

        public Messages getMessages() {
            return mMessages;
        }

        /**
         * Returns open position with specified ticket.
         *
         * @param aTicketID id of the open position
         *
         * @return instance of class fxts.stations.datatypes.Position
         */
        public Position getOpenPosition(String aTicketID) {
            return mOpenPositions.getPosition(aTicketID);
        }

        /**
         * Returns order with specified id.
         *
         * @param aOrderID id of the order
         *
         * @return instance of class fxts.stations.datatypes.Order
         */
        public Order getOrder(String aOrderID) {
            return mOrders.getOrder(aOrderID);
        }

        public Order getOrderByTradeId(String aTradeID) {
            return mOrders.getOrderByTradeId(aTradeID);
        }

        /**
         * get Orders table
         */
        public Orders getOrders() {
            return mOrders;
        }

        /**
         * get open positions table
         */
        public OpenPositions getPositions() {
            return mOpenPositions;
        }

        /**
         * Returns rate for the currency.
         *
         * @param aCurrency name of the currency
         *
         * @return instance of class fxts.stations.datatypes.Rate
         */
        public Rate getRate(String aCurrency) {
            return mRates.getRate(aCurrency);
        }

        /**
         * return Rates table
         */
        public Rates getRates() {
            return mRates;
        }

        /**
         * Returns current value of server time as known on client side.
         */
        public Date getServerTime() {
            return TradeDesk.this.getServerTime();
        }

        /**
         * summaries table
         */
        public Summaries getSummaries() {
            return mSummaries;
        }

        /**
         * Returns current user name
         */
        public String getUserName() {
            return TradeDesk.this.getUserName();
        }

        /**
         * This method is called when status of liaison has changed.
         *
         * @param aStatus Status
         */
        @Override
        public void onLiaisonStatus(LiaisonStatus aStatus) {
            if (aStatus == null) {
                return;
            }
            if (mCurStat == LiaisonStatus.CONNECTING && aStatus == LiaisonStatus.READY) {
                if (mServerTime != ServerTime.UNKNOWN && mServerTimeThread == null) {
                    createServerTimeThread();
                }
            } else if (aStatus == LiaisonStatus.DISCONNECTED) {
                // stopping mServerTimeThread thread
                Thread tmp = mServerTimeThread;
                mServerTimeThread = null;
                if (tmp != null) {
                    mStop = true;
                    tmp.interrupt();
                    try {
                        tmp.join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (mCurStat == LiaisonStatus.DISCONNECTED && aStatus == LiaisonStatus.CONNECTING
                || mCurStat == LiaisonStatus.RECONNECTING && aStatus != LiaisonStatus.DISCONNECTED) {
                mAccounts.enableRecalc(true);
                mOpenPositions.enableRecalc(true);
                mOrders.enableRecalc(true);
                mSummaries.enableRecalc(true);
                mRates.enableRecalc(true);
                mClosedPositions.enableRecalc(true);
            }
            if (aStatus == LiaisonStatus.DISCONNECTED
                || aStatus == LiaisonStatus.DISCONNECTING
                || aStatus == LiaisonStatus.RECONNECTING) {
                mAccounts.enableRecalc(false);
                mOpenPositions.enableRecalc(false);
                mOrders.enableRecalc(false);
                mSummaries.enableRecalc(false);
                mRates.enableRecalc(false);
                mClosedPositions.enableRecalc(false);
            }
            mCurStat = aStatus;
        }

        /**
         * Removes position.
         *
         * @param aTicketID id of open position
         */
        public void removeOpenPosition(String aTicketID) {
            Position openPos = mOpenPositions.getPosition(aTicketID);
            if (openPos != null) {
                mOpenPositions.remove(openPos);
                updatePositionFrameTitle();
            }
        }

        /**
         * Removes the order.
         *
         * @param aOrderID id of the order
         */
        public void removeOrder(String aOrderID) {
            Order order = mOrders.getOrder(aOrderID);
            if (order != null) {
                mOrders.remove(order);
                updateOrderFrameTitle();
            }
        }

        /**
         * Sets current connection name
         */
        public void setConnectionName(String aConnectionName) {
            TradeDesk.this.setConnectionName(aConnectionName);
        }

        public void setDatabaseName(String aDatabaseName) {
            TradeDesk.this.setDatabaseName(aDatabaseName);
        }

        /**
         * Sets current user name.
         *
         * @param aUserName new user name
         */
        public void setUserName(String aUserName) {
            TradeDesk.this.setUserName(aUserName);
        }

        /**
         * Synchronizes server time stored on the client side with actual server time.
         * Trade desk keeps current server time and updates it.
         *
         * @param aTime current time
         */
        public void syncServerTime(Date aTime) {
            if (aTime != null) {
                synchronized (mServerTime) {
                    GregorianCalendar server = new GregorianCalendar();
                    server.setTime(mServerTime);
                    GregorianCalendar incoming = new GregorianCalendar();
                    incoming.setTime(aTime);
                    if (incoming.after(server)) {
                        mServerTime.setTime(aTime.getTime());
                    }
                }
                dispatchSetServerTime();
                createServerTimeThread();
            }
        }

        /**
         * Synchronizes server time.
         *
         * @param aTime time
         */
        void syncServerTime(long aTime) {
            synchronized (mServerTime) {
                mServerTime.setTime(aTime);
            }
            dispatchSetServerTime();
        }

        /**
         * Updates account.
         *
         * @param aAcc account
         */
        public void updateAccount(Account aAcc) {
            if (aAcc == null) {
                return;
            }
            int index = mAccounts.indexOf(aAcc);
            if (index != -1) {
                try {
                    mAccounts.set(index, aAcc);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            updateSummaryFrameTitle();
        }

        private void updateAccountsFrameTitle() {
            ATableFrame frame = TradeApp.getInst().getAccountsFrame();
            if (frame != null) {
                frame.updateTitle();
                frame.revalidate();
                frame.repaint();
            }
        }

        private void updateClosedPositionFrameTitle() {
            ATableFrame frame = TradeApp.getInst().getClosedPositionsFrame();
            if (frame != null) {
                frame.updateTitle();
                frame.revalidate();
                frame.repaint();
            }
        }

        private void updateMessagesFrameTitle() {
            ATableFrame frame = TradeApp.getInst().getMessagesFrame();
            if (frame != null) {
                frame.updateTitle();
                frame.revalidate();
                frame.repaint();
            }
        }

        /**
         * Updates open position in the trade desk.
         *
         * @param aPos open position
         */
        public void updateOpenPosition(Position aPos) {
            if (aPos == null) {
                return;
            }
            int index = mOpenPositions.indexOf(aPos);
            if (index != -1) {
                try {
                    mOpenPositions.set(index, aPos);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        /**
         * Updates order in the trade desk.
         *
         * @param aOrder order
         */
        public void updateOrder(Order aOrder) {
            if (aOrder == null) {
                return;
            }
            int index = mOrders.indexOf(aOrder);
            if (index != -1) {
                try {
                    mOrders.set(index, aOrder);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void updateOrderFrameTitle() {
            ATableFrame frame = TradeApp.getInst().getOrdersFrame();
            if (frame != null) {
                frame.updateTitle();
                frame.revalidate();
                frame.repaint();
            }
        }

        private void updatePositionFrameTitle() {
            ATableFrame frame = TradeApp.getInst().getOpenPositionsFrame();
            if (frame != null) {
                frame.updateTitle();
                frame.revalidate();
                frame.repaint();
            }
        }

        /**
         * Updates rate in the trade desk.
         *
         * @param aRate rate
         */
        public void updateRate(Rate aRate) {
            if (aRate == null) {
                return;
            }
            int index = mRates.indexOf(aRate);
            if (index != -1) {
                try {
                    mRates.set(index, aRate);
                } catch (Exception ex) {
                    //swallow
                }
            }
            updateSummaryFrameTitle();
        }

        private void updateSummaryFrameTitle() {
            ATableFrame frame = TradeApp.getInst().getSummaryFrame();
            if (frame != null) {
                frame.updateTitle();
                frame.revalidate();
                frame.repaint();
            }
        }
    }
}
