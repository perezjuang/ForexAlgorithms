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
package fxts.stations.trader.ui.dialogs;

import fxts.stations.trader.IFXTSConstants;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.util.ResourceManager;

import javax.swing.JOptionPane;
import java.awt.Frame;

public abstract class AmountDialog extends ABaseDialog {
    /* Localized resources's manager */
    protected ResourceManager mResMan;

    /**
     * Protectes constructor.
     *
     * @param aParent parent window
     */
    protected AmountDialog(Frame aParent) {
        super(aParent);
    }

    /* Returns  localized resources's manager */
    public ResourceManager getResMan() {
        return mResMan;
    }

    /* Sets  Localized resources's manager */
    public void setResMan(ResourceManager aResMan) {
        mResMan = aResMan;
    }

    /**
     * Initializes components.
     */
    protected abstract void initComponents();

    protected abstract boolean verify();

    /**
     * Checks of the amount.
     *
     * @param aAmountComboBox amount combobox
     *
     * @return amount
     */
    protected long verifyAmount(AmountsComboBox aAmountComboBox) {
        //check to see if we have a pip distance in tss
        String max = TradingServerSession.getInstance().getParameterValue("MAX_QUANTITY");
        return verifyAmount(aAmountComboBox,
                            max == null ? IFXTSConstants.MAXIMUM_AMOUNT : Long.parseLong(max),
                            "IDS_ENTRY_DIALOG_AMOUNT_MORE_MAXIMUM");
    }

    /**
     * Checks of the amount.
     *
     * @param aAmountComboBox amount combobox
     * @param aMaximumAmount maximum value of the amount
     * @param aMaxMessageId id of the error message
     *
     * @return value
     */
    protected long verifyAmount(AmountsComboBox aAmountComboBox, long aMaximumAmount, String aMaxMessageId) {
        String message = null;
        long unitSize = aAmountComboBox.getContractSize();
        boolean ret;
        long amount = 0;
        try {
            amount = aAmountComboBox.getSelectedAmountLong();
            ret = amount <= aMaximumAmount;
            if (!ret) {
                message = mResMan.getString(aMaxMessageId) + " (" + aMaximumAmount / 1000 + ")";
            } else if (amount % unitSize != 0) {
                amount = -1;
            }
        } catch (Exception e) {
            ret = false;
        }
        if (ret) {
            ret = amount > 0;
            if (!ret) {
                if (amount == 0) {
                    message = mResMan.getString("IDS_ENTRY_DIALOG_ZERO_AMOUNT") + " ";
                } else {
                    String str = mResMan.getString("IDS_MARKET_DIALOG_AMOUNT_ERROR_MESSAGE");
                    message = " " + str + " " + unitSize / 1000 + ".";
                }
            }
        }
        if (!ret) {
            JOptionPane.showMessageDialog(this, message,
                                          mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            aAmountComboBox.requestFocus();
            return -1;
        }
        return amount;
    }
}
