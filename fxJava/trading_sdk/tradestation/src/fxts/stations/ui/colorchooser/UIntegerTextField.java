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
package fxts.stations.ui.colorchooser;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * A text field which takes integer values.
 * History:
 * 01/20/2004 updated
 */
class UIntegerTextField extends JTextField {
    class ValueDelta extends AbstractAction {
        int delta;

        public ValueDelta(int delta) {
            this.delta = delta;
        }

        public void actionPerformed(ActionEvent e) {
            NumericDocument doc = (NumericDocument) getDocument();
            int min = doc.min;
            int max = doc.max;
            int value = getIntegerValue();
            value += delta;
            if (value < min) {
                value = max;
            } else if (value > max) {
                value = min;
            }
            setText(String.valueOf(value));
        }
    }

    public UIntegerTextField(int min, int max, int initialValue) {
        super(new NumericDocument(min, max), initialValue + "", String.valueOf(max).length() + 1);
        //was added by KAV 01/20/2004
        if (initialValue == 0) {
            setText("1");
            setText("0");
        }
        installKeyboardActions();
    }

    public int getIntegerValue() {
        return ((NumericDocument) getDocument()).getIntegerValue();
    }

    protected void installKeyboardActions() {
        InputMap keyMap = getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = getActionMap();
        if (keyMap != null && actionMap != null) {
            KeyStroke upKey = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
            KeyStroke downKey = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
            KeyStroke numUpKey = KeyStroke.getKeyStroke("KP_UP");
            KeyStroke numDownKey = KeyStroke.getKeyStroke("KP_DOWN");
            keyMap.put(upKey, "incrementValue");
            keyMap.put(downKey, "decrementValue");
            if (upKey != numUpKey) {
                keyMap.put(numUpKey, "incrementValue");
                keyMap.put(numDownKey, "decrementValue");
            }
            actionMap.put("incrementValue", new ValueDelta(1));
            actionMap.put("decrementValue", new ValueDelta(-1));
        }
    }

    public void setText(String s) {
        NumericDocument doc = (NumericDocument) getDocument();
        int oldValue = doc.currentVal;
        try {
            doc.currentVal = doc.parse(s);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        if (oldValue != doc.currentVal) {
            doc.checkingEnabled = false;
            super.setText(s);
            doc.checkingEnabled = true;
        }
    }
}

class NumericDocument extends PlainDocument {
    boolean checkingEnabled = true;
    int currentVal = 0;
    int max;
    int min;

    public NumericDocument(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getIntegerValue() {
        return currentVal;
    }

    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null) {
            return;
        }
        if (!checkingEnabled) {
            super.insertString(offs, str, a);
            return;
        }
        String proposedResult = null;
        if (getLength() == 0) {
            proposedResult = str;
        } else {
            StringBuffer currentBuffer = new StringBuffer(getText(0, getLength()));
            currentBuffer.insert(offs, str);
            proposedResult = currentBuffer.toString();
        }
        try {
            currentVal = parse(proposedResult);
            super.insertString(offs, str, a);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public int parse(String proposedResult) throws NumberFormatException {
        int value = 0;
        if (proposedResult.length() != 0) {
            value = Integer.parseInt(proposedResult);
        }
        if (value >= min && value <= max) {
            return value;
        } else {
            throw new NumberFormatException();
        }
    }

    public void remove(int offs, int len) throws BadLocationException {
        if (!checkingEnabled) {
            super.remove(offs, len);
            return;
        }
        String currentText = getText(0, getLength());
        String beforeOffset = currentText.substring(0, offs);
        String afterOffset = currentText.substring(len + offs, currentText.length());
        String proposedResult = beforeOffset + afterOffset;
        try {
            currentVal = parse(proposedResult);
            super.remove(offs, len);
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}