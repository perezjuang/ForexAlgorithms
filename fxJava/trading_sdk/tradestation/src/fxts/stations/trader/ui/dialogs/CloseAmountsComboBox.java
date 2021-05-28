/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/CloseAmountsComboBox.java#1 $
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
 * Created: Aug 21, 2007 11:32:24 AM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;

import java.text.DecimalFormat;

/**
 */
public class CloseAmountsComboBox extends AmountsComboBox {
    @Override
    public AbstractComboBoxModel getComboBoxModel() {
        if (mModel == null) {
            mModel = new Model();
        }
        return mModel;
    }

    @Override
    public void setMaximumValue(long aMaximumValue) {
        mMaximumValue = aMaximumValue;
    }

    private class Model extends AbstractComboBoxModel {
        private DecimalFormat mFormat = new ThreadSafeNumberFormat().getInstance();

        private Model() {
            mFormat.applyPattern("#.###");
        }

        /**
         * Returns element at combo box by index.
         *
         * @param aIndex index of element
         */
        public Object getElementAt(int aIndex) {
            double d = mContractSize * (aIndex + 1);
            if (aIndex == 9 || mMaximumValue < mContractSize) {
                d = mMaximumValue;
            }
            if (mContractSize / 1000 <= 0) {
                mFormat.setMinimumFractionDigits(3);
            } else {
                mFormat.setMinimumFractionDigits(0);
            }
            String title = mFormat.format(d / 1000);
            return new Item(aIndex, title, true);
        }

        /**
         * Returns size of combobox.
         */
        public int getSize() {
            if (mMaximumValue <= mContractSize) {
                return 1;
            } else {
                int i = 10;
                if (mContractSize == 0 || mMaximumValue == 0) {
                    i = 0;
                } else {
                    long lots = mMaximumValue / mContractSize;
                    if (lots < 10) {
                        i = (int) lots;
                    }
                }
                return i;
            }
        }
    }
}
