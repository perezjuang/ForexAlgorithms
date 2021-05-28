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

import fxts.stations.ui.ResizeParameter;
import fxts.stations.ui.SideConstraints;
import fxts.stations.ui.UIFrontEnd;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Insets;
import java.io.Serializable;

/**
 * The standard RGB chooser.
 */
class RGBChooserPanel extends AColorChooserPanel implements ChangeListener {
    private class NumberListener implements DocumentListener, Serializable {
        public void changedUpdate(DocumentEvent aEvent) {
        }

        public void insertUpdate(DocumentEvent aEvent) {
            updatePanel(aEvent);
        }

        public void removeUpdate(DocumentEvent aEvent) {
            updatePanel(aEvent);
        }

        private void updatePanel(DocumentEvent aEvent) {
            int red = mRedField.getIntegerValue();
            int green = mGreenField.getIntegerValue();
            int blue = mBlueField.getIntegerValue();
            Color color = new Color(red, green, blue);
            getColorSelectionModel().setSelectedColor(color);
        }
    }

    protected UIntegerTextField mBlueField;
    protected JSlider mBlueSlider;
    protected UIntegerTextField mGreenField;
    protected JSlider mGreenSlider;
    private boolean mAdjusting; // indicates the fields are being set internally
    private final int maxValue = 255;
    private final int minValue = 0;
    protected UIntegerTextField mRedField;
    protected JSlider mRedSlider;

    public RGBChooserPanel() {
    }

    protected void buildChooser() {
        SideConstraints sideConstraints;
        ResizeParameter resizeParameter;
        String redString = UIManager.getString("ColorChooser.rgbRedText");
        String greenString = UIManager.getString("ColorChooser.rgbGreenText");
        String blueString = UIManager.getString("ColorChooser.rgbBlueText");

        //setLayout( new BorderLayout() );
        setLayout(UIFrontEnd.getInstance().getSideLayout());
        Color color = getColorFromModel();
        JPanel enclosure = new JPanel();
        //enclosure.setLayout(new SmartGridLayout( 3, 3 ) );
        enclosure.setLayout(UIFrontEnd.getInstance().getSideLayout());
        //add(enclosure, BorderLayout.CENTER);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        sideConstraints.insets = new Insets(30, 10, 0, 10);
        resizeParameter = new ResizeParameter();
        resizeParameter.init(0.5, 0.0, 0.5, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        //sideConstraints.resize = new ResizeParameter(0.5, 0.0, 0.5, 1.0);
        add(enclosure, sideConstraints);

        //        sliderPanel.setBorder(new LineBorder(Color.black));
        DocumentListener numberChange = new NumberListener();

        // The row for the red value
        JLabel l = fxts.stations.ui.UIManager.getInst().createLabel(redString);
        l.setDisplayedMnemonic(UIManager.getInt("ColorChooser.rgbRedMnemonic"));
        //enclosure.add(l);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(l, sideConstraints);
        mRedSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, color.getRed());
        mRedSlider.setMajorTickSpacing(85);
        mRedSlider.setMinorTickSpacing(17);
        mRedSlider.setPaintTicks(true);
        mRedSlider.setPaintLabels(true);
        //enclosure.add( mRedSlider );
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(mRedSlider, sideConstraints);
        mRedField = new UIntegerTextField(minValue, maxValue, color.getRed());
        l.setLabelFor(mRedSlider);
        JPanel redFieldHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        //mRedField.addChangeListener(this);
        mRedField.getDocument().addDocumentListener(numberChange);

        //redFieldHolder.add(mRedField);
        sideConstraints = new SideConstraints();
        sideConstraints.fill = SideConstraints.BOTH;
        redFieldHolder.add(mRedField, sideConstraints);

        //enclosure.add(redFieldHolder);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 2;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(redFieldHolder, sideConstraints);

        // The row for the green value
        l = fxts.stations.ui.UIManager.getInst().createLabel(greenString);
        l.setDisplayedMnemonic(UIManager.getInt("ColorChooser.rgbGreenMnemonic"));
        //enclosure.add(l);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(l, sideConstraints);
        mGreenSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, color.getGreen());
        mGreenSlider.setMajorTickSpacing(85);
        mGreenSlider.setMinorTickSpacing(17);
        mGreenSlider.setPaintTicks(true);
        mGreenSlider.setPaintLabels(true);
        //enclosure.add(mGreenSlider);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(mGreenSlider, sideConstraints);
        mGreenField = new UIntegerTextField(minValue, maxValue, color.getGreen());
        l.setLabelFor(mGreenSlider);
        JPanel greenFieldHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        //greenFieldHolder.add(mGreenField);
        sideConstraints = new SideConstraints();
        sideConstraints.fill = SideConstraints.BOTH;
        greenFieldHolder.add(mGreenField, sideConstraints);
        //mGreenField.addChangeListener(this);
        mGreenField.getDocument().addDocumentListener(numberChange);
        //enclosure.add(greenFieldHolder);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 2;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(greenFieldHolder, sideConstraints);

        // The slider for the blue value
        l = fxts.stations.ui.UIManager.getInst().createLabel(blueString);
        l.setDisplayedMnemonic(UIManager.getInt("ColorChooser.rgbBlueMnemonic"));
        //enclosure.add(l);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(l, sideConstraints);
        mBlueSlider = new JSlider(JSlider.HORIZONTAL, 0, 255, color.getBlue());
        mBlueSlider.setMajorTickSpacing(85);
        mBlueSlider.setMinorTickSpacing(17);
        mBlueSlider.setPaintTicks(true);
        mBlueSlider.setPaintLabels(true);
        //enclosure.add(mBlueSlider);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(mBlueSlider, sideConstraints);
        mBlueField = new UIntegerTextField(minValue, maxValue, color.getBlue());
        l.setLabelFor(mBlueSlider);
        JPanel blueFieldHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        //blueFieldHolder.add(mBlueField);
        sideConstraints = new SideConstraints();
        sideConstraints.fill = SideConstraints.BOTH;
        blueFieldHolder.add(mBlueField, sideConstraints);
        //mBlueField.addChangeListener(this);
        mBlueField.getDocument().addDocumentListener(numberChange);
        //enclosure.add(blueFieldHolder);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 2;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        enclosure.add(blueFieldHolder, sideConstraints);
        mRedSlider.addChangeListener(this);
        mGreenSlider.addChangeListener(this);
        mBlueSlider.addChangeListener(this);
        mRedSlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        mGreenSlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
        mBlueSlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
    }

    public String getDisplayName() {
        return UIManager.getString("ColorChooser.rgbNameText");
    }

    /**
     * The background color, foreground color, and font are already set to the
     * defaults from the defaults table before this method is called.
     */
    public void installChooserPanel(ColorChooser aEnclosingChooser) {
        super.installChooserPanel(aEnclosingChooser);
    }

    /**
     * Sets the values of the controls to reflect the color
     */
    private void setColor(Color aNewColor) {
        int red = aNewColor.getRed();
        int blue = aNewColor.getBlue();
        int green = aNewColor.getGreen();
        if (mRedSlider.getValue() != red) {
            mRedSlider.setValue(red);
        }
        if (mGreenSlider.getValue() != green) {
            mGreenSlider.setValue(green);
        }
        if (mBlueSlider.getValue() != blue) {
            mBlueSlider.setValue(blue);
        }
        if (mRedField.getIntegerValue() != red) {
            mRedField.setText(String.valueOf(red));
        }
        if (mGreenField.getIntegerValue() != green) {
            mGreenField.setText(String.valueOf(green));
        }
        if (mBlueField.getIntegerValue() != blue) {
            mBlueField.setText(String.valueOf(blue));
        }
    }

    public void stateChanged(ChangeEvent aEvent) {
        if (aEvent.getSource() instanceof JSlider && !mAdjusting) {
            int red = mRedSlider.getValue();
            int green = mGreenSlider.getValue();
            int blue = mBlueSlider.getValue();
            Color color = new Color(red, green, blue);
            getColorSelectionModel().setSelectedColor(color);
        }
    }

    public void uninstallChooserPanel(ColorChooser aEnclosingChooser) {
        super.uninstallChooserPanel(aEnclosingChooser);
    }

    public void updateChooser() {
        if (!mAdjusting) {
            mAdjusting = true;
            setColor(getColorFromModel());
            mAdjusting = false;
        }
    }
}