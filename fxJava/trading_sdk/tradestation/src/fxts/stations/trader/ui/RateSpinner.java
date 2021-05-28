/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/RateSpinner.java#1 $
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
 * Created: Feb 5, 2008 11:29:10 AM
 *
 * $History: $
 */
package fxts.stations.trader.ui;

import fxts.stations.core.TradeDesk;
import fxts.stations.util.Util;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.ParseException;

/**
 * @author Andre Mermegas
 */
public class RateSpinner extends JSpinner {
    private int mFirst;
    private NumberFormatter mFormatter;
    private int mLast;
    private int mSize;

    public RateSpinner() {
        super(new RateSpinnerModel());
        JFormattedTextField text = ((JSpinner.DefaultEditor) getEditor()).getTextField();
        mFormatter = new NumberFormatter();
        text.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public JFormattedTextField.AbstractFormatter getFormatter(JFormattedTextField aTextField) {
                return mFormatter;
            }
        });
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent aEvent) {
                validate(aEvent);
            }
        });
        text.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(final FocusEvent aEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JFormattedTextField source = (JFormattedTextField) aEvent.getSource();
                        int selectionEnd = source.getText().length();
                        source.select(selectionEnd - 3, selectionEnd);
                    }
                });
            }
        });
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent aMouseWheelEvent) {
                try {
                    commitEdit();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                final JFormattedTextField field = ((JSpinner.DefaultEditor) getEditor()).getTextField();
                field.requestFocusInWindow();
                if (aMouseWheelEvent.getWheelRotation() <= 0) {
                    getModel().getNextValue();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            int selectionEnd = field.getText().length();
                            field.select(selectionEnd - 3, selectionEnd);
                        }
                    });
                } else {
                    getModel().getPreviousValue();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            int selectionEnd = field.getText().length();
                            field.select(selectionEnd - 3, selectionEnd);
                        }
                    });
                }
            }
        });
    }

    private void validate(KeyEvent aEvent) {
        JFormattedTextField jtf = (JFormattedTextField) aEvent.getSource();
        String value = jtf.getText() + aEvent.getKeyChar();
        int length = value.length() - (jtf.getSelectedText() == null ? 0 : jtf.getSelectedText().length());
        if (!Character.isDigit(aEvent.getKeyChar()) && aEvent.getKeyChar() != KeyEvent.VK_PERIOD) {
            aEvent.consume();
        } else if (length > mSize) {
            aEvent.consume();
        } else {
            if (jtf.getSelectedText() == null) {
                int dot = jtf.getCaret().getDot();
                StringBuffer sb = new StringBuffer();
                sb.append(jtf.getText().substring(0, dot));
                sb.append(aEvent.getKeyChar());
                sb.append(jtf.getText().substring(dot, jtf.getText().length()));
                value = sb.toString();
            } else {
                char[] chars = jtf.getText().toCharArray();
                boolean once = true;
                for (int i = jtf.getSelectionStart(); i < jtf.getSelectionEnd(); i++) {
                    if (once) {
                        chars[i] = aEvent.getKeyChar();
                        once = false;
                    } else {
                        chars[i] = ' ';
                    }
                }
                value = new String(chars).trim();
            }

            String[] price = Util.splitToArray(value, ".");
            if (price.length == 1 && price[0].length() > mFirst) {
                aEvent.consume();
            } else if (price.length == 2 && (price[0].length() > mFirst || price[1].length() > mLast)) {
                aEvent.consume();
            }
        }
    }

    public void setCurrency(String aCurrency) {
        if (aCurrency.contains("JPY")) {
            //123.123
            mFirst = 3;
            mLast = 3;
        } else {
            //123.12345
            mFirst = 3;
            mLast = 5;
        }
        int period = 1;
        mSize = mFirst + mLast + period;
        ((RateSpinnerModel) getModel()).setCurrency(aCurrency);
        mFormatter.setFormat(TradeDesk.getFormat(aCurrency));
    }
}
