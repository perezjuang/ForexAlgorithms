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

import fxts.stations.util.IHeightVariableComponent;
import fxts.stations.util.InvokerSetRowHeight;
import fxts.stations.util.ResourceManager;
import fxts.stations.ui.UIManager;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class FontEditorPanel extends AValueEditorPanel implements IHeightVariableComponent {

    private class HeightVariablePanel extends JPanel implements IHeightVariableComponent {
        private boolean mHeightChanged = true;
        private int mHeight;

        public int getHeight() {
            return mHeight;
        }

        void setHeight(int aHeight) {
            int tmp = mHeight;
            mHeight = aHeight;
            mHeightChanged = tmp != mHeight;
        }

        public boolean wasHeightChanged() {
            if (mHeightChanged) {
                mHeightChanged = false;
                return true;
            }
            return false;
        }
    }

    private EditAction mEditAction;
    private JButton mFontButton;
    private JLabel mFontLabel;
    private JLabel mNEFontLabel;
    private HeightVariablePanel mNEditComponent;
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;
    private boolean mHeightChanged = true;
    private int mHeight;

    public FontEditorPanel(Object aValue, APropertyType aType) {
        super(aValue, aType);
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }
        GridBagConstraints gridBagConstraints;
        JPanel panel;
// Create not ediatble component
        mNEditComponent = new HeightVariablePanel();
        mNEditComponent.setLayout(new BorderLayout());
        mNEFontLabel = UIManager.getInst().createLabel();
        mNEFontLabel.setText(aType.toStringValue(aValue));
        mNEFontLabel.setFont((Font) aValue);
        mNEFontLabel.setBackground(Color.white);
        mNEFontLabel.setOpaque(true);
        mNEditComponent.add(mNEFontLabel, BorderLayout.CENTER);

        mNEditComponent.setHeight(mNEFontLabel.getFontMetrics((Font) aValue).getHeight() + 2);
        mNEditComponent.setBorder(new LineBorder(Color.white));
//Create ediatble component
        setLayout(new BorderLayout());
        mFontLabel = UIManager.getInst().createLabel();
        mFontLabel.setText(aType.toStringValue(aValue));
        mFontLabel.setBackground(Color.white);
        mFontLabel.setOpaque(true);
        add(mFontLabel, BorderLayout.CENTER);
        mFontLabel.setFont((Font) aValue);

        setHeight(mFontLabel.getFontMetrics((Font) aValue).getHeight() + 2);
        panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(65, 17));
        panel.setMinimumSize(new Dimension(72, 17));
        panel.setMaximumSize(new Dimension(72, 17));
        mFontButton = UIManager.getInst().createButton("...");
        mFontButton.setPreferredSize(new Dimension(16, 16));
        mFontButton.setMaximumSize(new Dimension(16, 16));
        mFontButton.setMinimumSize(new Dimension(16, 16));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 42, 0, 2);
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        panel.add(mFontButton, gridBagConstraints);
        panel.setBackground(Color.white);
        panel.setOpaque(true);
        add(panel, BorderLayout.EAST);
        mEditAction = new EditAction(aType, this, aValue);
        mFontButton.addActionListener(mEditAction);
        add(mFontLabel, BorderLayout.CENTER);
        setBorder(new LineBorder(Color.black));
    }

    public int getHeight() {
        return mHeight;
    }

    public Component getNotEditedComponent() {
/*
        //In NE-mode string appears normally size
        Font tempFont = ((Font)getValue()).deriveFont(12f);
        mNEFontLabel.setFont(tempFont);
        //Before editing string appears as is
        mNEFontLabel.setText(getType().toStringValue((Font)getValue()));
        mFontLabel.setFont((Font)getValue());
        mFontLabel.setText(getType().toStringValue((Font)getValue()));
*/
        return mNEditComponent;
    }

    public String getTitle() {
        return mResMan.getString("IDS_FONT_EDITOR_TITLE");
    }

    public void refreshControls() {
        if (mNEFontLabel != null && mFontLabel != null) {
            mEditAction.setValue(getValue());
            mNEFontLabel.setFont((Font) getValue());
            mNEFontLabel.setText(getType().toStringValue(getValue()));

            mNEditComponent.setHeight(mNEFontLabel.getFontMetrics((Font) getValue()).getHeight());
            mFontLabel.setFont((Font) getValue());
            mFontLabel.setText(getType().toStringValue(getValue()));

            setHeight(mFontLabel.getFontMetrics((Font) getValue()).getHeight());
            JTable table = (JTable) getParameterValue("table");
            if (table != null) {
                Integer oRow = (Integer) getParameterValue(this);
                if (oRow != null && wasHeightChanged()) {
                    EventQueue.invokeLater(new InvokerSetRowHeight(table, oRow.intValue(), getHeight()));
                }
            } else {
                super.repaint();
            }
        } else {
            super.repaint();
        }
    }

    public void requestFocus() {
        mFontButton.requestFocus();
    }

    void setHeight(int aHeight) {
        int tmp = mHeight;
        mHeight = aHeight;
        mHeightChanged = tmp != mHeight;
    }

    public boolean wasHeightChanged() {
        if (mHeightChanged) {
            mHeightChanged = false;
            return true;
        }
        return false;
    }
}