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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

/**
 * Panel for presentation of Boolean type
 *
 * @Creation date (10/24/2003 1:36 PM)
 */
public class BooleanEditorPanel extends AValueEditorPanel {
    /**
     * Listener to check box state.
     */
    private class CheckBoxListener implements ItemListener {
        /**
         * Invoked when an item has been selected or deselected.
         */
        public void itemStateChanged(ItemEvent aEvent) {
            if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                mBooleanLabel.setText(Boolean.TRUE.toString());
            } else {
                mBooleanLabel.setText(Boolean.FALSE.toString());
            }
            setValueChanged(true);
        }
    }

    /**
     * Checkbox for setting of boolean value.
     */
    private JCheckBox mBooleanCheckBox;
    /**
     * Label for boolean value.
     */
    private JLabel mBooleanLabel;
    /**
     * Label for not editable boolean value.
     */
    private JLabel mNEditBooleanLabel;

    /**
     * Constructor.
     * Image of editor panel for boolean data type.
     *
     * @param aValue value of data
     * @param aType  type of data
     */
    public BooleanEditorPanel(Object aValue, APropertyType aType) {
        super(aValue, aType);
        // Create not ediatble component - image when panel is not editted
        JPanel editComponent = new JPanel();
        editComponent.setLayout(new BorderLayout());
        mNEditBooleanLabel = UIManager.getInst().createLabel();
        mNEditBooleanLabel.setText(aType.toStringValue(aValue));
        mNEditBooleanLabel.setBackground(Color.white);
        mNEditBooleanLabel.setOpaque(true);
        editComponent.add(mNEditBooleanLabel, BorderLayout.CENTER);
        //Create ediatble component - image when panel is editted
        setLayout(new BorderLayout());
        mBooleanLabel = UIManager.getInst().createLabel();
        mBooleanLabel.setText(aType.toStringValue(aValue));
        mBooleanLabel.setBackground(Color.white);
        mBooleanLabel.setOpaque(true);
        add(mBooleanLabel, BorderLayout.WEST);
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(65, 17));
        panel.setMinimumSize(new Dimension(72, 17));
        panel.setMaximumSize(new Dimension(72, 17));
        mBooleanCheckBox = UIManager.getInst().createCheckBox();
        mBooleanCheckBox.setMnemonic(KeyEvent.VK_SPACE);
        mBooleanCheckBox.setSelected(((Boolean) aValue).booleanValue());
        mBooleanCheckBox.setPreferredSize(new Dimension(17, 17));
        mBooleanCheckBox.setMaximumSize(new Dimension(17, 17));
        mBooleanCheckBox.setMinimumSize(new Dimension(17, 17));
        mBooleanCheckBox.setBackground(Color.white);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 52, 0, 2);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        panel.add(mBooleanCheckBox, gridBagConstraints);
        panel.setBackground(Color.white);
        panel.setOpaque(true);
        add(panel, BorderLayout.CENTER);
        CheckBoxListener checkBoxListener = new CheckBoxListener();
        mBooleanCheckBox.addItemListener(checkBoxListener);
    }

    /**
     * Sets image of not edited component (label) and return it.
     *
     * @return not edited image of panel.
     */
    public Component getNotEditedComponent() {
        if (mBooleanCheckBox.isSelected()) {
            mNEditBooleanLabel.setText(Boolean.TRUE.toString());
        } else {
            mNEditBooleanLabel.setText(Boolean.FALSE.toString());
        }
        return mNEditBooleanLabel;
    }

    /**
     * Returns string presentation data that is selected by user.
     *
     * @return string presentation of boolean type
     */
    public Object getUserInput() {
        if (mBooleanCheckBox.isSelected()) {
            return Boolean.TRUE.toString();
        } else {
            return Boolean.FALSE.toString();
        }
    }

    /**
     * Provides renovation data in the panel.
     */
    public void refreshControls() {
        if (mNEditBooleanLabel != null) {
            mNEditBooleanLabel.setText(getType().toStringValue(getValue()));
        }
        super.repaint();
    }

    /**
     * Sets focus on the check box.
     */
    public void requestFocus() {
        mBooleanCheckBox.requestFocus();
    }
}