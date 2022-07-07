/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/fxcm-api/src/main/ClientTester.java#4 $
 *
 * Copyright (c) 2010 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 *
 * $History: $
 * 01/10/2005   Andre Mermegas  updated to demo setting/updating stop/limit on entry/market orders
 * 02/11/2005   Andre Mermegas  changes for new msg format.
 * 06/03/2005   Andre Mermegas  sendMessage(), follow up to interface changes
 * 06/13/2005   Miron follow up to FXCMLoginProperties changes
 * 08/10/2010   Andre Mermegas: touchup
 */

import com.fxcm.external.api.transport.FXCMLoginProperties;
import com.fxcm.external.api.transport.GatewayFactory;
import com.fxcm.external.api.transport.IGateway;
import com.fxcm.external.api.transport.listeners.IGenericMessageListener;
import com.fxcm.external.api.transport.listeners.IStatusMessageListener;
import com.fxcm.external.api.util.MessageGenerator;
import com.fxcm.fix.OrdTypeFactory;
import com.fxcm.fix.SideFactory;
import com.fxcm.fix.SubscriptionRequestTypeFactory;
import com.fxcm.fix.TradingSecurity;
import com.fxcm.fix.admin.Logout;
import com.fxcm.fix.other.BusinessMessageReject;
import com.fxcm.fix.other.UserResponse;
import com.fxcm.fix.posttrade.ClosedPositionReport;
import com.fxcm.fix.posttrade.CollateralInquiryAck;
import com.fxcm.fix.posttrade.CollateralReport;
import com.fxcm.fix.posttrade.PositionReport;
import com.fxcm.fix.posttrade.RequestForPositionsAck;
import com.fxcm.fix.pretrade.MarketDataRequest;
import com.fxcm.fix.pretrade.MarketDataSnapshot;
import com.fxcm.fix.pretrade.SecurityList;
import com.fxcm.fix.pretrade.SecurityStatus;
import com.fxcm.fix.pretrade.TradingSessionStatus;
import com.fxcm.fix.trade.ExecutionReport;
import com.fxcm.fix.trade.OrderCancelReject;
import com.fxcm.fix.trade.OrderSingle;
import com.fxcm.messaging.ISessionStatus;
import com.fxcm.messaging.ITransportable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Example of how to use the FXCM API
 *
 * @author: Andre Mermegas
 * Date: Dec 15, 2004
 * Time: 4:19:00 PM
 */
public class ClientTester {
    private static List cAccounts = new ArrayList();
    private static String cAccountMassID = "";
    private static String cOpenOrderMassID = "";
    private static String cOpenPositionMassID = "";
    private static String cClosedPositionMassID = "";
    private static String cTradingSessionStatusID = "";
    private static TradingSessionStatus cTradingSessionStatus;
    private static boolean cPrintMarketData;

    public static void main(String[] args) {
        // step 1: get an instance of IGateway from the GatewayFactory
        final IGateway fxcmGateway = GatewayFactory.createGateway();
        /*
            step 2: register a generic message listener with the gateway, this
            listener in particular gets all messages that are related to the trading
            platform MarketDataSnapshot,OrderSingle,ExecutionReport, etc...
        */
        fxcmGateway.registerGenericMessageListener(new IGenericMessageListener() {
            public void messageArrived(ITransportable aMessage) {
                if (aMessage instanceof MarketDataSnapshot) {
                    MarketDataSnapshot incomingQuote = (MarketDataSnapshot) aMessage;
                    if (incomingQuote.getMDReqID() != null) {
                        System.out.println("client: snapshot = " + incomingQuote);
                    } else if (cPrintMarketData) {
                        System.out.println("client: streaming = " + incomingQuote);
                    }
                } else if (aMessage instanceof OrderCancelReject) {
                    System.out.println("client: streaming = " + aMessage);
                } else if (aMessage instanceof SecurityStatus) {
                    System.out.println("client: streaming = " + aMessage);
                } else if (aMessage instanceof SecurityList) {
                    System.out.println("client: streaming = " + aMessage);
                } else if (aMessage instanceof UserResponse) {
                    System.out.println("client: streaming = " + aMessage);
                } else if (aMessage instanceof Logout) {
                    System.out.println("client: streaming = " + aMessage);
                } else if (aMessage instanceof TradingSessionStatus) {
                    cTradingSessionStatus = (TradingSessionStatus) aMessage;
                    if (cTradingSessionStatusID.equals(cTradingSessionStatus.getRequestID())) {
                        System.out.println("client: reply = " + cTradingSessionStatus);
                        try {
                            MarketDataRequest mdr = new MarketDataRequest();
                            Enumeration securities = cTradingSessionStatus.getSecurities();
                            while (securities.hasMoreElements()) {
                                TradingSecurity o = (TradingSecurity) securities.nextElement();
                                mdr.addRelatedSymbol(o);
                            }
                            mdr.setSubscriptionRequestType(SubscriptionRequestTypeFactory.SUBSCRIBE);
                            mdr.setMDEntryTypeSet(MarketDataRequest.MDENTRYTYPESET_ALL);
                            fxcmGateway.sendMessage(mdr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("client: streaming = " + cTradingSessionStatus);
                    }
                } else if (aMessage instanceof RequestForPositionsAck) {
                    RequestForPositionsAck rfpa = (RequestForPositionsAck) aMessage;
                    if (cOpenPositionMassID.equals(rfpa.getRequestID())) {
                        System.out.println("client: open positions = " + rfpa);
                    } else if (cClosedPositionMassID.equals(rfpa.getRequestID())) {
                        System.out.println("client: close positions = " + rfpa);
                    } else {
                        System.out.println("client: rfpa = " + rfpa);
                    }
                } else if (aMessage instanceof ClosedPositionReport) {
                    ClosedPositionReport cpr = (ClosedPositionReport) aMessage;
                    if (cClosedPositionMassID.equals(cpr.getRequestID())) {
                        System.out.println("client: reply = " + cpr);
                    } else {
                        System.out.println("client: streaming = " + cpr);
                    }
                } else if (aMessage instanceof PositionReport) {
                    PositionReport pr = (PositionReport) aMessage;
                    if (cOpenPositionMassID.equals(pr.getRequestID())) {
                        System.out.println("client: reply = " + pr);
                    } else {
                        System.out.println("client: streaming = " + pr);
                    }
                } else if (aMessage instanceof ExecutionReport) {
                    // when the order has been recieved  you get an ExecutionReport
                    ExecutionReport ep = (ExecutionReport) aMessage;
                    if (cOpenOrderMassID.equals(ep.getRequestID())) {
                        System.out.println("client: reply = " + ep);
                    } else {
                        System.out.println("client: streaming = " + ep);
                    }
                } else if (aMessage instanceof CollateralInquiryAck) {
                    CollateralInquiryAck cia = (CollateralInquiryAck) aMessage;
                    if (cAccountMassID.equals(cia.getRequestID())) {
                        System.out.println("client: reply = " + cia);
                    } else {
                        System.out.println("client: streaming = " + cia);
                    }
                } else if (aMessage instanceof CollateralReport) {
                    // got a response to our request for accounts
                    CollateralReport cr = (CollateralReport) aMessage;
                    if (cAccountMassID.equals(cr.getRequestID())) {
                        System.out.println("client: reply = " + cr);
                        cAccounts.add(cr);
                    } else {
                        System.out.println("client: streaming = " + cr);
                    }
                } else if (aMessage instanceof BusinessMessageReject) {
                    BusinessMessageReject bmr = (BusinessMessageReject) aMessage;
                    System.out.println("client: streaming =" + bmr);
                }
            }
        });

        /*
            step 3: register a status message listener, this listener recieves messages
            pertaining to the status of your current session.
        */
        fxcmGateway.registerStatusMessageListener(new IStatusMessageListener() {
            public void messageArrived(ISessionStatus aStatus) {
                switch (aStatus.getStatusCode()) {
                    case ISessionStatus.STATUSCODE_READY:
                    case ISessionStatus.STATUSCODE_SENDING:
                    case ISessionStatus.STATUSCODE_RECIEVING:
                    case ISessionStatus.STATUSCODE_PROCESSING:
                    case ISessionStatus.STATUSCODE_WAIT:
                        break;
                    default:
                        System.out.println((
                                "client: inc status msg = ["
                                + aStatus.getStatusCode()
                                + "] ["
                                + aStatus.getStatusName()
                                + "] ["
                                + aStatus.getStatusMessage()
                                + "]").toUpperCase());
                        if (aStatus.getStatusCode() == ISessionStatus.STATUSCODE_DISCONNECTED) {
                            try {
                                fxcmGateway.relogin();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        });

        try {
            /*
                step 4: call login on the gateway, this method takes an instance of FXCMLoginProperties
                which takes 4 parameters: username,password,terminal and server or path to a Hosts.xml
                file which it uses for resolving servers. As soon as the login  method executes your listeners
                begin receiving asynch messages from the FXCM servers.
            */
            String username = args[0];
            String password = args[1];
            String terminal = args[2];
            String server = args[3];
            String file = null;
            if (args.length == 5) {
                file = args[4];
            }

            FXCMLoginProperties properties;
            if (file == null) {
                properties = new FXCMLoginProperties(username, password, terminal, server);
            } else {
                properties = new FXCMLoginProperties(username, password, terminal, server, file);
            }
            //Properties p = new Properties();
            //p.put(IUserSession.PIN, "111111");
            //properties.setProperties(p);

            System.out.println("client: start logging in");
            fxcmGateway.login(properties);

            cTradingSessionStatusID = fxcmGateway.requestTradingSessionStatus();
            System.out.println(">>> requestTradingSessionStatus = " + cTradingSessionStatusID);
            cAccountMassID = fxcmGateway.requestAccounts();
            System.out.println(">>> requestAccounts = " + cAccountMassID);
            cOpenOrderMassID = fxcmGateway.requestOpenOrders();
            System.out.println(">>> requestOpenOrders = " + cOpenOrderMassID);
            cOpenPositionMassID = fxcmGateway.requestOpenPositions();
            System.out.println(">>> requestOpenPositions = " + cOpenPositionMassID);
            cClosedPositionMassID = fxcmGateway.requestClosedPositions();
            System.out.println(">>> requestClosedPositions = " + cClosedPositionMassID);

            //step 5: remember to call fxcmGateway.logout(); when you are done with your connection and wish to logout
            System.out.println("client: done logging in\n");
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String str = in.readLine();
                if (str != null) {
                    if ("o".equalsIgnoreCase(str.trim())) {
                        CollateralReport acct = (CollateralReport) cAccounts.get(0);
                        OrderSingle orderSingle = MessageGenerator.generateMarketOrder(
                                acct.getAccount(),
                                acct.getQuantity(),
                                SideFactory.BUY,
                                "EUR/USD",
                                "true market order test " + System.currentTimeMillis());
                        try {
                            String reqid = fxcmGateway.sendMessage(orderSingle);
                            System.out.println("client: submitting order = " + reqid);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if ("eo".equalsIgnoreCase(str.trim())) {
                        CollateralReport acct = (CollateralReport) cAccounts.get(0);
                        OrderSingle orderSingle = MessageGenerator.generateStopLimitEntry(
                                null,
                                1.1,
                                OrdTypeFactory.LIMIT,
                                acct.getAccount(),
                                acct.getQuantity(),
                                SideFactory.BUY,
                                "EUR/USD",
                                "entry order - limit profit");
                        String reqid = fxcmGateway.sendMessage(orderSingle);
                        System.out.println("client: entry order requestId = " + reqid);
                    } else if ("m".equalsIgnoreCase(str.trim())) {
                        cPrintMarketData = !cPrintMarketData;
                    } else if ("exit".equalsIgnoreCase(str.trim())) {
                        fxcmGateway.logout();
                        System.exit(0);
                    } else {
                        String out = new StringBuilder()
                                .append("Commands:\n o [submit market order],")
                                .append(" m [toggle streaming marketdata],")
                                .append(" eo [submit entry order]\n")
                                .append(" exit [stop process]\n").toString();
                        System.out.println(out);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
