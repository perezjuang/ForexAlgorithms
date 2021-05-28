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

import fxts.stations.util.ResourceManager;
import fxts.stations.ui.UIManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * ColorEditorPanel
 * Panel for presentation of Color type
 * Creation date (11/06/2003 12:30 PM)
 */
public class ColorEditorPanel extends AValueEditorPanel {
    private JButton mColorButton;
    private JLabel mColorLabel;
    private JLabel mColorTextLabel;
    private EditAction mEditAction;
    private JLabel mNEColorLabel;
    private JLabel mNEColorTextLabel;
    private JPanel mNEditComponent;
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;

    /**
     * Constructor ColorEditorPanel.
     * Image of editor panel for color data type.
     *
     * @param aValue aValue value of data.
     * @param aType  aType type of data (color).
     */
    public ColorEditorPanel(Object aValue, APropertyType aType) {
        super(aValue, aType);
        GridBagConstraints gridBagConstraints;
        JPanel labelPanel;
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }
// Create not editable component - image when panel is not edited
        mNEditComponent = new JPanel();
        mNEditComponent.setLayout(new BorderLayout());
        mNEColorTextLabel = UIManager.getInst().createLabel();
        mNEColorTextLabel.setText(aType.toStringValue(aValue));
        mNEColorTextLabel.setBackground(Color.white);
        mNEColorTextLabel.setOpaque(true);
        mNEditComponent.add(mNEColorTextLabel, BorderLayout.CENTER);
        labelPanel = new JPanel();
        labelPanel.setLayout(new GridBagLayout());
        labelPanel.setPreferredSize(new Dimension(65, 17));
        labelPanel.setBackground(Color.white);
        mNEColorLabel = UIManager.getInst().createLabel();
        mNEColorLabel.setBackground((Color) aValue);
        mNEColorLabel.setBorder(new LineBorder(Color.black));
        mNEColorLabel.setPreferredSize(new Dimension(15, 15));
        mNEColorLabel.setMinimumSize(new Dimension(15, 15));
        mNEColorLabel.setMaximumSize(new Dimension(15, 15));
        mNEColorLabel.setOpaque(true);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 23, 0, 24);
        labelPanel.add(mNEColorLabel, gridBagConstraints);
        mNEditComponent.add(labelPanel, BorderLayout.EAST);
        mNEditComponent.setBorder(new LineBorder(Color.white));
//Create editable component - image when panel is edited
        setLayout(new BorderLayout());
        mColorTextLabel = UIManager.getInst().createLabel();
        mColorTextLabel.setText(aType.toStringValue(aValue));
        mColorTextLabel.setBackground(Color.white);
        mColorTextLabel.setOpaque(true);
        add(mColorTextLabel, BorderLayout.CENTER);
        labelPanel = new JPanel();
        labelPanel.setLayout(new GridBagLayout());
        labelPanel.setPreferredSize(new Dimension(63, 17));
        mColorLabel = UIManager.getInst().createLabel();
        mColorLabel.setBackground((Color) aValue);
        mColorLabel.setBorder(new LineBorder(Color.black));
        mColorLabel.setPreferredSize(new Dimension(15, 15));
        mColorLabel.setMinimumSize(new Dimension(15, 15));
        mColorLabel.setMaximumSize(new Dimension(15, 15));
        mColorLabel.setOpaque(true);
        gridBagConstraints = new GridBagConstraints();
        //25
        gridBagConstraints.insets = new Insets(0, 19, 0, 2);
        labelPanel.add(mColorLabel, gridBagConstraints);
        mColorButton = UIManager.getInst().createButton("...");
        mColorButton.setPreferredSize(new Dimension(16, 16)); //17
        mColorButton.setMaximumSize(new Dimension(16, 16));
        mColorButton.setMinimumSize(new Dimension(16, 16));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 2, 0, 2);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        labelPanel.add(mColorButton, gridBagConstraints);
        labelPanel.setBackground(Color.white);
        labelPanel.setOpaque(true);
        add(labelPanel, BorderLayout.EAST);
        mEditAction = new EditAction(aType, this, aValue);
        mColorButton.addActionListener(mEditAction);
        add(mColorTextLabel, BorderLayout.CENTER);
        setBorder(new LineBorder(Color.black));
    }

    /**
     * getNotEditedComponent.
     * Set image of not edited component (colored label and text label)
     * and return it.
     *
     * @return not edited image of panel.
     */
    public Component getNotEditedComponent() {
        mNEColorLabel.setBackground((Color) getValue());
        mColorLabel.setBackground((Color) getValue());
        mColorTextLabel.setText(getType().toStringValue(getValue()));
        mNEColorTextLabel.setText(getType().toStringValue(getValue()));
        return mNEditComponent;
    }

    public String getTitle() {
        return mResMan.getString("IDS_COLOR_EDITOR_TITLE");
    }

    /**
     * refreshControls.
     * Provides renovation data in the panel.
     */
    public void refreshControls() {
        if (mNEColorLabel != null
            && mColorLabel != null
            && mColorTextLabel != null
            && mNEColorTextLabel != null) {
            mNEColorLabel.setBackground((Color) getValue());
            mColorLabel.setBackground((Color) getValue());
            mColorTextLabel.setText(getType().toStringValue(getValue()));
            mNEColorTextLabel.setText(getType().toStringValue(getValue()));
        }
        super.repaint();
    }

    /**
     * requestFocus.
     * Set focus on the button called dialog.
     */
    public void requestFocus() {
        mColorButton.requestFocus();
    }

    public void setValueToEditor(Object aValue) {
        mEditAction.setValue(aValue);
    }
}