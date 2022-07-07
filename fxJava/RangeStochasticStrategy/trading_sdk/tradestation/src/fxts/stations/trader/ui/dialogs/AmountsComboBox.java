/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/AmountsComboBox.java#2 $
 *
 * Copyright (c) 2009 FXCM, LLC.
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
 * $History: $
 * 05/14/2009   Andre Mermegas: CFD update
 */
package fxts.stations.trader.ui.dialogs;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.trader.IFXTSConstants;
import fxts.stations.trader.TradeApp;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.Util;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

/**
 * Combobox with amount`s values.
 */
public class AmountsComboBox extends ABusinessDataComboBox {
    protected long mContractSize = 100000;
    protected long mMaximumValue;
    protected AbstractComboBoxModel mModel;
    private String mValue;

    public AmountsComboBox() {
        setEditor(new Editor());
        JFormattedTextField jtf = (JFormattedTextField) getEditor().getEditorComponent();
        jtf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent aEvent) {
                if (!Character.isDigit(aEvent.getKeyChar())) {
                    getToolkit().beep();
                    aEvent.consume();
                }
            }
        });
        jtf.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent aEvent) {
                mValue = getSelectedAmountString();
            }

            public void focusLost(FocusEvent aEvent) {
                JFormattedTextField field = (JFormattedTextField) aEvent.getSource();
                setSelectedItem(field.getText());
            }
        });
    }

    /**
     * Contract Size
     * @return size
     */
    public long getContractSize() {
        return mContractSize;
    }

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

    public double getMaximumValue() {
        return mMaximumValue;
    }

    /**
     * Returns long representation of the selected amount.
     * @return amt
     * @throws OutOfLongException exception
     */
    public long getSelectedAmountLong() throws OutOfLongException {
        return getSelectedAmountLong(getSelectedAmountString());
    }

    /**
     * Returns long representation of the selected amount.
     * @return amt
     * @throws OutOfLongException exception
     */
    public long getSelectedAmountLong(String aLong) throws OutOfLongException {
        String selectedAmountString = aLong;
        if (selectedAmountString == null) {
            return 0L;
        }
        selectedAmountString = selectedAmountString.trim();
        if (selectedAmountString.length() != 0 && selectedAmountString.charAt(0) == '+') {
            selectedAmountString = selectedAmountString.substring(1).trim();
        }
        double rc = Util.parseDouble(selectedAmountString);
        long retLong = (long) (rc * 1000);
        if ((double) retLong / 1000 != rc) {
            if (selectedAmountString.startsWith("-")) {
                throw new LowerThenMinLongException(selectedAmountString);
            }
            throw new BiggerThenMaxLongException(selectedAmountString);
        }
        return retLong;
    }

    /**
     * Returns string representation of the selected amount.
     * @return amt
     */
    public String getSelectedAmountString() {
        Object selectedItem = getSelectedItem();
        return selectedItem == null ? null : selectedItem.toString().trim();
    }

    /**
     * Sets selected item by value of the amount.
     *
     * @param aAmount value of the amount
     */
    public void selectAmount(long aAmount) {
        int anIndex = (int) aAmount;
        if (anIndex == 0) {
            setSelectedIndex(anIndex);
        } else if (getModel().getSize() >= anIndex) {
            setSelectedIndex(anIndex - 1);
        } else {
            setSelectedIndex(0);
        }
    }

    /**
     * Contract Size
     *
     * @param aContractSize size
     */
    public void setContractSize(long aContractSize) {
        if (aContractSize != 0) {
            mContractSize = aContractSize;
        }
        refreshAll();
    }

    /**
     * Sets maximum value.
     *
     * @param aMaximumValue maximum value of the amount
     */
    public void setMaximumValue(long aMaximumValue) {
        if (aMaximumValue > mContractSize * 10) {
            mMaximumValue = mContractSize * 10;
        } else {
            mMaximumValue = aMaximumValue;
        }
        refreshAll();
    }

    /**
     * Subscribes to receiving of business data.
     */
    @Override
    public void subscribeBusinessData() {
    }

    public boolean verifyAmount() {
        JFormattedTextField jtf = (JFormattedTextField) getEditor().getEditorComponent();
        String max = TradingServerSession.getInstance().getParameterValue("MAX_QUANTITY");
        long aMaximumAmount = max == null ? IFXTSConstants.MAXIMUM_AMOUNT : Long.parseLong(max);
        ResourceManager mResMan = TradeApp.getInst().getResourceManager();
        String message = null;
        boolean ret;
        long amount = 0;
        try {
            amount = getSelectedAmountLong(jtf.getText());
            ret = amount <= aMaximumAmount;
            if (!ret) {
                message = mResMan.getString("IDS_ENTRY_DIALOG_AMOUNT_MORE_MAXIMUM")
                          + " ("
                          + aMaximumAmount / 1000
                          + ")";
            } else if (amount % mContractSize != 0) {
                amount = -1;
            }
        } catch (Exception e) {
            message = mResMan.getString("IDS_INVALID_AMOUNT");
            ret = false;
            mLogger.error(e.getMessage(), e);
        }
        if (ret) {
            ret = amount > 0;
            if (!ret) {
                if (amount == 0) {
                    message = mResMan.getString("IDS_ENTRY_DIALOG_ZERO_AMOUNT") + " ";
                } else {
                    String str = mResMan.getString("IDS_MARKET_DIALOG_AMOUNT_ERROR_MESSAGE");
                    message = " " + str + " " + mContractSize / 1000 + ".";
                }
            }
        }
        if (ret) {
            setSelectedItem(jtf.getText());
        } else {
            JOptionPane.showMessageDialog(TradeApp.getInst().getMainFrame(),
                                          message,
                                          mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            setSelectedItem(mValue);
            jtf.setText(mValue);
            jtf.requestFocusInWindow();
        }
        return ret;
    }

    private static class Editor extends BasicComboBoxEditor {
        private Editor() {
            editor = new JFormattedTextField();
        }
    }

    /**
     * Concrete implementation of AbstractComboBoxModel.
     */
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
            double value = (double) mContractSize * (aIndex + 1) / 1000;
            if (mContractSize / 1000 <= 0) {
                mFormat.setMinimumFractionDigits(3);
            } else {
                mFormat.setMinimumFractionDigits(0);
            }
            String title = mFormat.format(value);
            return new Item(aIndex, title, true);
        }

        /**
         * Returns size of combobox.
         */
        public int getSize() {
            return 10;
        }
    }
}
