/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/fxcm-api/src/main/com/fxcm/external/api/util/MessageGenerator.java#7 $
 *
 * Copyright (c) 2005 FXCM, LLC. All Rights Reserved.
 * 32 Old Slip, 10th Floor, New York, NY 10005 USA
 *
 * THIS SOFTWARE IS THE CONFIDENTIAL AND PROPRIETARY INFORMATION OF
 * FXCM, LLC. ("CONFIDENTIAL INFORMATION"). YOU SHALL NOT DISCLOSE
 * SUCH CONFIDENTIAL INFORMATION AND SHALL USE IT ONLY IN ACCORDANCE
 * WITH THE TERMS OF THE LICENSE AGREEMENT YOU ENTERED INTO WITH
 * FXCM.
 *
 * $History: $
 * 03/15/2005   Andre Mermegas: added javadocs
 * 04/04/2005   Andre Mermegas: removed clOrdID from method params
 * 06/24/2005   Andre Mermegas: added generateAcceptOrder()
 * 06/30/2005   Andre Mermegas: use proper enum types, and long for acct id
 * 07/07/2005   Andre Mermegas: updates to use double instead of long for quantity
 * 07/22/2005   Andre Mermegas: peg instructions fix
 * 07/23/2005   Miron:          FXCMOrdType removed from OrderSingle
 * 07/25/2005   Andre Mermegas: commented out peg instruction stuff from generate methods
 * 09/13/2005   Andre Mermegas: added generateMarketOrder
 * 11/29/2005   Andre Mermegas: removed price, quoteid from signature of generateMarketOrder
 * 06/02/2005   Andre Mermegas: added generateCloseMarketOrder, update to javadocs
 * 09/12/2006   Andre Mermegas: update javadocs, update for at market point orders
 * 04/08/2009   Andre Mermegas: remove unneccessary FXCMAcctID from signatures, add Account to
 *                              generateOrderCancelRequest(...), generateOrderReplaceRequest(...)
 * 01/27/2010   Andre Mermegas: update
 * 03/05/2010   Andre Mermegas: correct timeinforce
 * 04/19/2010   Andre Mermegas: touchup
 * 07/29/2010   Andre Mermegas: add extra constructors which require no quoteid
 * 02/28/2014   Andre Mermegas: set default order type fallthrough to LIMIT instead of PQ
 */
package com.fxcm.external.api.util;

import com.fxcm.fix.IFixDefs;
import com.fxcm.fix.IOrdType;
import com.fxcm.fix.ISide;
import com.fxcm.fix.Instrument;
import com.fxcm.fix.OrdTypeFactory;
import com.fxcm.fix.PegInstruction;
import com.fxcm.fix.QuoteRespTypeFactory;
import com.fxcm.fix.TimeInForceFactory;
import com.fxcm.fix.UTCTimestamp;
import com.fxcm.fix.pretrade.QuoteResponse;
import com.fxcm.fix.trade.OrderCancelReplaceRequest;
import com.fxcm.fix.trade.OrderCancelRequest;
import com.fxcm.fix.trade.OrderSingle;

import java.util.Date;

/**
 *
 */
public class MessageGenerator {
    /**
     * Generates an Accept OrderSingle in response to a Requote
     *
     * @param aQuoteID quoteid
     * @param aCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateAcceptOrder(String aQuoteID, String aCustomText) {
        OrderSingle os = generateDefaultOrderSingle(null, null, 0, 0, null, null, aCustomText);
        os.setOrdType(OrdTypeFactory.PREVIOUSLY_QUOTED);
        os.setQuoteID(aQuoteID);
        return os;
    }

    /**
     * Generate a close order at market price
     *
     * @param aOpenPosID pos id
     * @param aAccount acct
     * @param aAmount amount
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateCloseMarketOrder(
            String aOpenPosID,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        OrderSingle orderSingle = generateCloseOrder(0,
                                                     aOpenPosID,
                                                     aAccount,
                                                     aAmount,
                                                     aSide,
                                                     aCurrency,
                                                     aClCustomText,
                                                     0);
        orderSingle.setOrdType(OrdTypeFactory.MARKET);  //"1"
        orderSingle.setTimeInForce(TimeInForceFactory.GOOD_TILL_CANCEL);
        return orderSingle;
    }

    /**
     * Generate close order
     *
     * @param aQuoteID quoteid
     * @param aPrice price
     * @param aOpenPosID posid
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateCloseOrder(
            String aQuoteID,
            double aPrice,
            String aOpenPosID,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        return generateCloseOrder(aQuoteID,
                                  aPrice,
                                  aOpenPosID,
                                  aAccount,
                                  aAmount,
                                  aSide,
                                  aCurrency,
                                  aClCustomText,
                                  0);
    }

    /**
     * Generate close order
     *
     * @param aPrice price
     * @param aOpenPosID posid
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateCloseOrder(
            double aPrice,
            String aOpenPosID,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        return generateCloseOrder(aPrice,
                                  aOpenPosID,
                                  aAccount,
                                  aAmount,
                                  aSide,
                                  aCurrency,
                                  aClCustomText,
                                  0);
    }

    /**
     * Generate close order at market points
     *
     * @param aQuoteID quoteid
     * @param aPrice price
     * @param aOpenPosID posid
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aAtMarketPoints at mkt pts
     *
     * @return OrderSingle
     */
    public static OrderSingle generateCloseOrder(
            String aQuoteID,
            double aPrice,
            String aOpenPosID,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aAtMarketPoints) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    aQuoteID,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(OrdTypeFactory.PREVIOUSLY_QUOTED);
        os.setTimeInForce(TimeInForceFactory.IMMEDIATE_OR_CANCEL);
        os.setFXCMPosID(aOpenPosID);
        if (aAtMarketPoints > 0) {
            os.setOrdType(OrdTypeFactory.STOP_LIMIT);
            PegInstruction peg = new PegInstruction();
            peg.setPegPriceType(IFixDefs.PEGPRICETYPE_MARKET);
            peg.setPegMoveType(IFixDefs.PEGOFFSETTYPE_PRICE);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aAtMarketPoints);
            os.setPegInstructions(peg);
        }
        return os;
    }

    /**
     * Generate close order at market points
     *
     * @param aPrice price
     * @param aOpenPosID posid
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aAtMarketPoints at mkt pts
     *
     * @return OrderSingle
     */
    public static OrderSingle generateCloseOrder(
            double aPrice,
            String aOpenPosID,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aAtMarketPoints) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    null,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(OrdTypeFactory.PREVIOUSLY_QUOTED);
        os.setTimeInForce(TimeInForceFactory.IMMEDIATE_OR_CANCEL);
        os.setFXCMPosID(aOpenPosID);
        if (aAtMarketPoints > 0) {
            os.setOrdType(OrdTypeFactory.STOP_LIMIT);
            PegInstruction peg = new PegInstruction();
            peg.setPegPriceType(IFixDefs.PEGPRICETYPE_MARKET);
            peg.setPegMoveType(IFixDefs.PEGOFFSETTYPE_PRICE);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aAtMarketPoints);
            os.setPegInstructions(peg);
        }
        return os;
    }

    /**
     * @param aAccount acct
     * @param aQuoteID quoteid
     * @param aAmount amt
     * @param aRate rate
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    private static OrderSingle generateDefaultOrderSingle(
            String aAccount,
            String aQuoteID,
            double aAmount,
            double aRate,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        OrderSingle os = new OrderSingle();
        os.setAccount(aAccount);
        os.setQuoteID(aQuoteID);
        os.setOrderQty(aAmount);
        os.setPrice(aRate);
        os.setSide(aSide);
        os.setInstrument(new Instrument(aCurrency));
        os.setSecondaryClOrdID(aClCustomText);
        return os;
    }

    /**
     * Generates a true market order.
     *
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateMarketOrder(
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    null,
                                                    aAmount,
                                                    0,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(OrdTypeFactory.MARKET);
        os.setTimeInForce(TimeInForceFactory.GOOD_TILL_CANCEL);
        return os;
    }

    /**
     * Generate Open order
     *
     * @param aQuoteID quoteid
     * @param aPrice price
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateOpenOrder(
            String aQuoteID,
            double aPrice,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        return generateOpenOrder(aQuoteID, aPrice, aAccount, aAmount, aSide, aCurrency, aClCustomText, 0);
    }

    /**
     * Generate Open order
     *
     * @param aPrice price
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateOpenOrder(
            double aPrice,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        return generateOpenOrder(aPrice, aAccount, aAmount, aSide, aCurrency, aClCustomText, 0);
    }

    /**
     * Generate Open order at market points
     *
     * @param aQuoteID quoteid
     * @param aPrice price
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aAtMarketPoints at mkt pts
     *
     * @return OrderSingle
     */
    public static OrderSingle generateOpenOrder(
            String aQuoteID,
            double aPrice,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aAtMarketPoints) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    aQuoteID,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(OrdTypeFactory.PREVIOUSLY_QUOTED);
        os.setTimeInForce(TimeInForceFactory.IMMEDIATE_OR_CANCEL);
        if (aAtMarketPoints > 0) {
            os.setOrdType(OrdTypeFactory.STOP_LIMIT);
            PegInstruction peg = new PegInstruction();
            peg.setPegPriceType(IFixDefs.PEGPRICETYPE_MARKET);
            peg.setPegMoveType(IFixDefs.PEGOFFSETTYPE_PRICE);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aAtMarketPoints);
            os.setPegInstructions(peg);
        }
        return os;
    }

    /**
     * Generate Open order at market points
     *
     * @param aPrice price
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aAtMarketPoints at mkt pts
     *
     * @return OrderSingle
     */
    public static OrderSingle generateOpenOrder(
            double aPrice,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aAtMarketPoints) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    null,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(OrdTypeFactory.LIMIT);
        os.setTimeInForce(TimeInForceFactory.IMMEDIATE_OR_CANCEL);
        if (aAtMarketPoints > 0) {
            os.setOrdType(OrdTypeFactory.STOP_LIMIT);
            PegInstruction peg = new PegInstruction();
            peg.setPegPriceType(IFixDefs.PEGPRICETYPE_MARKET);
            peg.setPegMoveType(IFixDefs.PEGOFFSETTYPE_PRICE);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aAtMarketPoints);
            os.setPegInstructions(peg);
        }
        return os;
    }

    /**
     * Generate request to cancel a pending order.
     *
     * @param aClCustomText txt
     * @param aOrderID orderid
     * @param aSide side
     * @param aAccount account
     *
     * @return OrderCancelRequest
     */
    public static OrderCancelRequest generateOrderCancelRequest(
            String aClCustomText,
            String aOrderID,
            ISide aSide,
            String aAccount) {
        OrderCancelRequest ocr = new OrderCancelRequest();
        ocr.setSecondaryClOrdID(aClCustomText);
        ocr.setOrderID(aOrderID);
        ocr.setSide(aSide);
        ocr.setAccount(aAccount);
        ocr.setTransactTime(new UTCTimestamp(new Date()));
        return ocr;
    }

    /**
     * Generate an update to a pendnig order
     *
     * @param aClCustomText txt
     * @param aOrderID orderid
     * @param aSide side
     * @param aOrdType ordtype
     * @param aPrice price
     * @param aAccount account
     *
     * @return OrderCancelReplaceRequest
     */
    public static OrderCancelReplaceRequest generateOrderReplaceRequest(
            String aClCustomText,
            String aOrderID,
            ISide aSide,
            IOrdType aOrdType,
            double aPrice,
            String aAccount) {
        return generateOrderReplaceRequest(aClCustomText, aOrderID, aSide, aOrdType, aPrice, 0, aAccount);
    }

    /**
     * Generate an update to a pending order at market points
     *
     * @param aClCustomText txt
     * @param aOrderID orderid
     * @param aSide side
     * @param aOrdType ordtype
     * @param aPrice price
     * @param aTrailing trailing distance
     * @param aAccount account
     *
     * @return OrderCancelReplaceRequest
     */
    public static OrderCancelReplaceRequest generateOrderReplaceRequest(
            String aClCustomText,
            String aOrderID,
            ISide aSide,
            IOrdType aOrdType,
            double aPrice,
            int aTrailing,
            String aAccount) {
        OrderCancelReplaceRequest ocr = new OrderCancelReplaceRequest();
        ocr.setSecondaryClOrdID(aClCustomText);
        ocr.setOrderID(aOrderID);
        ocr.setSide(aSide);
        ocr.setOrdType(aOrdType);
        ocr.setPrice(aPrice);
        ocr.setTransactTime(new UTCTimestamp(new Date()));
        ocr.setAccount(aAccount);
        if (aTrailing > 0 && OrdTypeFactory.STOP == aOrdType) {
            PegInstruction peg = new PegInstruction();
            peg.setPegMoveType(IFixDefs.PEGMOVETYPE_FLOATING);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aPrice);
            peg.setFXCMPegFluctuatePts(aTrailing);
            ocr.setPegInstructions(peg);
        }
        return ocr;
    }

    /**
     * Generates a delete response for a requote or request for quote
     *
     * @param aQuoteID quoteid
     *
     * @return QuoteResponse
     */
    public static QuoteResponse generatePassResponse(String aQuoteID) {
        QuoteResponse qr = new QuoteResponse();
        qr.setQuoteID(aQuoteID);
        qr.setQuoteRespType(QuoteRespTypeFactory.PASS);
        return qr;
    }

    /**
     * Place a stop or a limit on an existing position or order.
     * For aOrderType, specify IFixDefs.ORDTYPE_LIMIT or IFixDefs.ORDERTYPE_STOP
     *
     * @param aPrice price
     * @param aPosID posid
     * @param aOrderType ordertype
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateStopLimitClose(
            double aPrice,
            String aPosID,
            IOrdType aOrderType,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        return generateStopLimitClose(aPrice,
                                      aPosID,
                                      aOrderType,
                                      aAccount,
                                      aAmount,
                                      aSide,
                                      aCurrency,
                                      aClCustomText,
                                      0);
    }

    /**
     * Place a stop or a limit on an existing position or order.
     * For aOrderType, specify IFixDefs.ORDTYPE_LIMIT or IFixDefs.ORDERTYPE_STOP
     *
     * @param aPrice price
     * @param aPosID posid
     * @param aOrderType ord type
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aTrailing trailing distance
     *
     * @return OrderSingle
     */
    public static OrderSingle generateStopLimitClose(
            double aPrice,
            String aPosID,
            IOrdType aOrderType,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aTrailing) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    null,
                                                    aAmount,
                                                    0,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(aOrderType);
        os.setFXCMPosID(aPosID);
        if (OrdTypeFactory.STOP == aOrderType) {
            os.setStopPx(aPrice);
        } else {
            os.setPrice(aPrice);
        }
        if (aTrailing > 0) {
            PegInstruction peg = new PegInstruction();
            peg.setPegMoveType(IFixDefs.PEGMOVETYPE_FLOATING);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aPrice);
            peg.setFXCMPegFluctuatePts(aTrailing);
            os.setPegInstructions(peg);
        }
        return os;
    }

    /**
     * Place a stop or a limit to enter the market.
     * For aOrderType, specify IFixDefs.ORDTYPE_LIMIT or IFixDefs.ORDERTYPE_STOP
     *
     * @param aQuoteID quoteid
     * @param aPrice price
     * @param aOrderType ord type
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateStopLimitEntry(
            String aQuoteID,
            double aPrice,
            IOrdType aOrderType,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    aQuoteID,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(aOrderType);
        return os;
    }

    /**
     * Place a stop or a limit to enter the market.
     * For aOrderType, specify IFixDefs.ORDTYPE_LIMIT or IFixDefs.ORDERTYPE_STOP
     *
     * @param aPrice price
     * @param aOrderType ord type
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     *
     * @return OrderSingle
     */
    public static OrderSingle generateStopLimitEntry(
            double aPrice,
            IOrdType aOrderType,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    null,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(aOrderType);
        return os;
    }

    /**
     * Place a stop or a limit to enter the market.
     * For aOrderType, specify IFixDefs.ORDTYPE_LIMIT or IFixDefs.ORDERTYPE_STOP
     *
     * @param aQuoteID quoteid
     * @param aPrice price
     * @param aOrderType ord type
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aTrailing trailing distance
     *
     * @return OrderSingle
     */
    public static OrderSingle generateStopLimitEntry(
            String aQuoteID,
            double aPrice,
            IOrdType aOrderType,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aTrailing) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    aQuoteID,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(aOrderType);
        if (aTrailing > 0) {
            PegInstruction peg = new PegInstruction();
            peg.setPegMoveType(IFixDefs.PEGMOVETYPE_FLOATING);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setPegOffsetValue(aPrice);
            peg.setFXCMPegFluctuatePts(aTrailing);
            os.setPegInstructions(peg);
        }
        return os;
    }

    /**
     * Place a stop or a limit to enter the market.
     * For aOrderType, specify IFixDefs.ORDTYPE_LIMIT or IFixDefs.ORDERTYPE_STOP
     *
     * @param aPrice price
     * @param aOrderType ord type
     * @param aAccount acct
     * @param aAmount amt
     * @param aSide side
     * @param aCurrency ccy
     * @param aClCustomText txt
     * @param aTrailing trailing distance
     *
     * @return OrderSingle
     */
    public static OrderSingle generateStopLimitEntry(
            double aPrice,
            IOrdType aOrderType,
            String aAccount,
            double aAmount,
            ISide aSide,
            String aCurrency,
            String aClCustomText,
            int aTrailing) {
        OrderSingle os = generateDefaultOrderSingle(aAccount,
                                                    null,
                                                    aAmount,
                                                    aPrice,
                                                    aSide,
                                                    aCurrency,
                                                    aClCustomText);
        os.setOrdType(aOrderType);
        if (aTrailing > 0) {
            PegInstruction peg = new PegInstruction();
            peg.setPegMoveType(IFixDefs.PEGMOVETYPE_FLOATING);
            peg.setPegOffsetType(IFixDefs.PEGOFFSETTYPE_BASIS_POINTS);
            peg.setFXCMPegFluctuatePts(aTrailing);
            os.setPegInstructions(peg);
        }
        return os;
    }
}
