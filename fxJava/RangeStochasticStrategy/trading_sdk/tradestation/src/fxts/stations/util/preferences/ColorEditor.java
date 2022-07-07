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

import fxts.stations.ui.ColorChooserWrapper;
import fxts.stations.ui.ResizeParameterWrapper;
import fxts.stations.ui.UIFrontEnd;
import fxts.stations.util.ResourceManager;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

/**
 * Implementation of IEditor interface for color data.
 */
public class ColorEditor implements IEditor {
    /**
     * Panel for showing of prewiew.
     */
    private JPanel mBannerPanel;
    /**
     * Color chooser dialog.
     */
    private ColorChooserWrapper mColorChooser;
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;
    /**
     * Current value of color.
     */
    private Color mValue;

    /**
     * Constructor.
     */
    public ColorEditor() {
        GridBagConstraints sideConstraints;
        ResizeParameterWrapper resizeParameter;
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }
        UIManager.put("ColorChooser.rgbRedText", mResMan.getString("IDS_RGB_RED_TEXT"));
        UIManager.put("ColorChooser.rgbGreenText", mResMan.getString("IDS_RGB_GREEN_TEXT"));
        UIManager.put("ColorChooser.rgbBlueText", mResMan.getString("IDS_RGB_BLUE_TEXT"));
        UIManager.put("ColorChooser.swatchesNameText", mResMan.getString("IDS_SWATCHES_TEXT"));
        UIManager.put("ColorChooser.previewText", mResMan.getString("IDS_PREVIEW_TEXT"));
        UIManager.put("ColorChooser.swatchesRecentText", mResMan.getString("IDS_RECENT_TEXT"));

        mBannerPanel = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        sideConstraints.fill = GridBagConstraints.BOTH;
        resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.0, 0.0, 1.0, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        mBannerPanel.setBackground(Color.white);
        mBannerPanel.setPreferredSize(new Dimension(100, 65));
    }

    /**
     * Returns component for placing on editor dialog.
     */
    public Component getComponent() {
        mColorChooser = UIFrontEnd.getInstance().getColorChooser();
        //Set up color chooser for setting text color
        mColorChooser.getSelectionModel().addChangeListener(
                new ChangeListener() {
                    public void stateChanged(ChangeEvent aEvent) {
                        Color newColor = mColorChooser.getColor();
                        mBannerPanel.setBackground(newColor);
                    }
                }
        );
        mColorChooser.setColor(mValue);
        mColorChooser.setPreviewPanel(mBannerPanel);
        return (Component) mColorChooser.getWrappedObj();
    }

    /**
     * Returns current value.
     */
    public Object getValue() {
        return mColorChooser.getColor();
    }

    /**
     * Sets new value to editor.
     *
     * @param aValue new value
     */
    public void setValue(Object aValue) {
        mValue = (Color) aValue;
        if (mColorChooser != null) {
            mColorChooser.setColor((Color) aValue);
        }
    }
}
