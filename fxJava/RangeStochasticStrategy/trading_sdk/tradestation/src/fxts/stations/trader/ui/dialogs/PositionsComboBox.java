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

import fxts.stations.core.OpenPositions;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.util.SignalType;
import fxts.stations.util.signals.ChangeSignal;

/**
 * Provides work with data from TradeDesk (open positions), in combobox.
 * <br>
 * Creation date (9/27/2003 4:07 PM)
 */
public class PositionsComboBox extends ABusinessDataComboBox {
    /**
     * Combobox model.
     */
    private AbstractComboBoxModel mModel;

    /**
     * Returns model of combobox.
     */
    @Override
    public AbstractComboBoxModel getComboBoxModel() {
        if (mModel == null) {
            mModel = new Model();
        }
        return mModel;
    }

    /**
     * Returns selected open position.
     */
    public String getSelectedPosition() {
        Object selectedItem = getSelectedItem();
        return selectedItem == null ? null : selectedItem.toString();
    }

    /**
     * Set current position in combobox at define of argument
     *
     * @param aTicketID id of positions which wil be setting combobox
     */
    public void selectPosition(String aTicketID) {
        if (aTicketID != null) {
            try {
                OpenPositions positions = TradeDesk.getInst().getOpenPositions();
                Position position = positions.getPosition(aTicketID);
                setSelectedIndex(positions.indexOf(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            setSelectedIndex(-1);
        }
    }

    /**
     * Subscribes to receiving of business data.
     */
    @Override
    public void subscribeBusinessData() {
        OpenPositions positions = TradeDesk.getInst().getOpenPositions();
        positions.subscribe(this, SignalType.ADD);
        positions.subscribe(this, SignalType.CHANGE);
        positions.subscribe(this, SignalType.REMOVE);
    }

    /**
     * Calls when a Change Signal has come in.
     *
     * @param aSignal incoming signal.
     * @param aItem line in combobox where was change
     *
     * @return true when signal was change for selected item, else false.
     */
    @Override
    public boolean updateOnSignal(ChangeSignal aSignal, Item aItem) {
        boolean bRes = false;
        //If there're no changes, don't redraw
        if (getSelectedIndex() == aItem.getIndex()) {
            if (isStatusEnabled() ^ aItem.isEnabled()) {
                setStatusEnabled(aItem.isEnabled());
                bRes = true;
            }
        }
        return bRes;
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
                Position position = (Position) TradeDesk.getInst().getOpenPositions().get(aIndex);
                Account acnt = TradeDesk.getInst().getAccounts().getAccount(position.getAccount());
                Rate rate = TradeDesk.getInst().getRates().getRate(position.getCurrency());
                boolean isEnabled = !acnt.isUnderMarginCall() && rate.isTradable() && !position.isBeingClosed();
                return newItem(aIndex, position.getTicketID(), isEnabled);
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
                size = TradeDesk.getInst().getOpenPositions().size();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return size;
        }
    }
}
