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
package fxts.stations.util.preferences;

import fxts.stations.ui.UIManager;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Component;

public class SimpleEditorPanel extends AValueEditorPanel {
    private class DocumentListener implements javax.swing.event.DocumentListener {
        public void changedUpdate(DocumentEvent aEvent) {
//            System.out.println("changedUpdate -------");
            setValueChanged(true);
        }

        public void insertUpdate(DocumentEvent aEvent) {
//            System.out.println("insertUpdate -------");
            setValueChanged(true);
        }

        public void removeUpdate(DocumentEvent aEvent) {
//            System.out.println("removeUpdate -------");
            setValueChanged(true);
        }
    }

    private DocumentListener mDocumentListener;
    private JLabel mPresentationLabel;
    private JTextComponent mTextField;

    public SimpleEditorPanel(Object aValue, APropertyType aType) {
        super(aValue, aType);
        mDocumentListener = new DocumentListener();
        mPresentationLabel = UIManager.getInst().createLabel();
        setLayout(new BorderLayout());
        mTextField = newTextControl();
        add(mTextField, BorderLayout.CENTER);
    }

    public void beginEditing() {
        mTextField.getDocument().addDocumentListener(mDocumentListener);
    }

    public void endEditing(boolean abCancelFlag) {
        mTextField.getDocument().removeDocumentListener(mDocumentListener);
    }

    public Component getNotEditedComponent() {
        mPresentationLabel.setText(getType().toStringValue(getValue()));
        return mPresentationLabel;
    }

    protected JTextComponent getTextControl() {
        return mTextField;
    }

    /**
     * The AValueEditorPanel subclass that doesn't have editor should refine
     * method getUserInput()
     */
    public Object getUserInput() {
        return mTextField.getText();
    }

    protected JTextComponent newTextControl() {
        JTextField textField = UIManager.getInst().createTextField(getType().toStringValue(getValue()));
        textField.setHorizontalAlignment(JTextField.LEFT);
        return textField;
    }

    public void refreshControls() {
        if (mTextField != null && mPresentationLabel != null) {
            mTextField.setText(getType().toStringValue(getValue()));
            mPresentationLabel.setText(getType().toStringValue(getValue()));
        }
        super.repaint();
    }

    public void requestFocus() {
        mTextField.setCaretPosition(mTextField.getDocument().getLength());
        mTextField.setSelectionStart(0);
        mTextField.setSelectionEnd(mTextField.getDocument().getLength());
        mTextField.requestFocus();
    }
}