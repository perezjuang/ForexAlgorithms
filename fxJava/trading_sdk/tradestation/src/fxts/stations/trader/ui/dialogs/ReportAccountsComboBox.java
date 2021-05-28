/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/ReportAccountsComboBox.java#1 $
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
 * Created: Aug 29, 2008 4:01:38 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.Accounts;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;

/**
 */
public class ReportAccountsComboBox extends SimpleAccountsComboBox {
    /**
     * Combobox model.
     */
    private AbstractComboBoxModel mModel;

    /**
     * Returns combobox model.
     */
    @Override
    public AbstractComboBoxModel getComboBoxModel() {
        if (mModel == null) {
            mModel = new Model();
        }
        return mModel;
    }

    @Override
    public void selectAccount(String aAccount) {
        if (aAccount != null) {
            try {
                Accounts accounts = TradeDesk.getInst().getAccounts();
                Account account = accounts.getAccount(aAccount);
                int nIndex = accounts.indexOf(account);
                if (nIndex == -1 && mModel.getSize() > 0) {
                    nIndex = 0;
                }
                setSelectedIndex(nIndex);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setSelectedIndex(-1);
        }
    }

    /**
     * Concrete implementation of AbstractComboBoxModel.
     */
    private class Model extends AbstractComboBoxModel {
        /**
         * Returns element at combo box by index.
         *
         * @param aIndex index of element
         */
        public Object getElementAt(int aIndex) {
            try {
                Accounts accounts = TradeDesk.getInst().getAccounts();
                if (aIndex > accounts.size() - 1) {
                    Account account = accounts.getInvisbleAccounts().get(aIndex - accounts.size());
                    return newItem(aIndex, account.getAccount(), true);
                } else {
                    Account account = (Account) accounts.get(aIndex);
                    return newItem(aIndex, account.getAccount(), true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        /**
         * Returns size of combobox.
         */
        public int getSize() {
            int size = 0;
            try {
                Accounts accounts = TradeDesk.getInst().getAccounts();
                size = accounts.size() + accounts.getInvisbleAccounts().size();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return size;
        }
    }
}
