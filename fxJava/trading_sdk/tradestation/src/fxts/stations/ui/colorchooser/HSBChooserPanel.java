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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.ImageConsumer;

/**
 * Implements the HSB Color chooser.
 */
class HSBChooserPanel extends AColorChooserPanel implements ChangeListener, HierarchyListener {
    private static final int PALETTE_DIMENSION = 150;
    private static final int MAX_HUE_VALUE = 359;
    private static final int MAX_SATURATION_VALUE = 100;
    private static final int MAX_BRIGHTNESS_VALUE = 100;
    private static final int HUE_MODE = 0;
    private static final int SATURATION_MODE = 1;
    private static final int BRIGHTNESS_MODE = 2;

    private UIntegerTextField bField;
    private JTextField blueField;
    private JRadioButton bRadio;
    private int currentMode = HUE_MODE;
    private JTextField greenField;
    private UIntegerTextField hField;
    private JRadioButton hRadio;
    private boolean isAdjusting = false; // Flag which indicates that values are set internally
    /*-- Data members --*/
    private HSBImage palette;
    private Image paletteImage;
    private JLabel paletteLabel;
    private Point paletteSelection = new Point();
    private JTextField redField;
    private UIntegerTextField sField;
    private JSlider slider;
    private HSBImage sliderPalette;
    private Image sliderPaletteImage;
    private JLabel sliderPaletteLabel;
    private JRadioButton sRadio;

    /**
     * Constructor.
     */
    public HSBChooserPanel() {
    }

    /**
     * Adds mouse listener to pallete.
     */
    private void addPaletteListeners() {
        paletteLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                float[] hsb = new float[3];
                palette.getHSBForLocation(e.getX(), e.getY(), hsb);
                updateHSB(hsb[0], hsb[1], hsb[2]);
            }
        });
        paletteLabel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int labelWidth = paletteLabel.getWidth();
                int labelHeight = paletteLabel.getHeight();
                int x = e.getX();
                int y = e.getY();
                if (x >= labelWidth) {
                    x = labelWidth - 1;
                }
                if (y >= labelHeight) {
                    y = labelHeight - 1;
                }
                if (x < 0) {
                    x = 0;
                }
                if (y < 0) {
                    y = 0;
                }
                float[] hsb = new float[3];
                palette.getHSBForLocation(x, y, hsb);
                updateHSB(hsb[0], hsb[1], hsb[2]);
            }
        });
    }

    /**
     * Builds a new chooser panel.
     */
    protected void buildChooser() {
        SideConstraints sideConstraints;
        ResizeParameter resizeParameter;
        setLayout(UIFrontEnd.getInstance().getSideLayout());

        //adds main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(UIFrontEnd.getInstance().getSideLayout());
        sideConstraints = new SideConstraints();
        sideConstraints.insets = new Insets(5, 10, 0, 10);
        resizeParameter = new ResizeParameter();
        resizeParameter.init(0.5, 0.0, 0.5, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        //sideConstraints.resize = new ResizeParameter(0.5, 0.0, 0.5, 1.0);
        add(mainPanel, sideConstraints);
        JComponent spp = buildSliderPalettePanel();
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        resizeParameter = new ResizeParameter();
        resizeParameter.init(0.0, 0.0, 0.1, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        //sideConstraints.resize = new ResizeParameter(0.0, 0.0, 0.1, 1.0);
        mainPanel.add(spp, sideConstraints);
        JPanel controlHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        JComponent hsbControls = buildHSBControls();
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        controlHolder.add(hsbControls, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        sideConstraints.fill = SideConstraints.BOTH;
        controlHolder.add(fxts.stations.ui.UIManager.getInst().createLabel(" "), sideConstraints);
        JComponent rgbControls = buildRGBControls();
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 2;
        sideConstraints.fill = SideConstraints.BOTH;
        controlHolder.add(rgbControls, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(10, 5, 10, 5);
        sideConstraints.fill = SideConstraints.BOTH;
        mainPanel.add(controlHolder, sideConstraints);
    }

    /**
     * Creates the panel with the editable HSB fields and the radio buttons.
     */
    private JComponent buildHSBControls() {
        SideConstraints sideConstraints;
        String hueString = UIManager.getString("ColorChooser.hsbHueText");
        String saturationString = UIManager.getString("ColorChooser.hsbSaturationText");
        String brightnessString = UIManager.getString("ColorChooser.hsbBrightnessText");
        RadioButtonHandler handler = new RadioButtonHandler();
        hRadio = new JRadioButton(hueString);
        hRadio.addActionListener(handler);
        hRadio.setSelected(true);
        sRadio = new JRadioButton(saturationString);
        sRadio.addActionListener(handler);
        bRadio = new JRadioButton(brightnessString);
        bRadio.addActionListener(handler);
        ButtonGroup group = new ButtonGroup();
        group.add(hRadio);
        group.add(sRadio);
        group.add(bRadio);
        float[] hsb = getHSBColorFromModel();
        hField = new UIntegerTextField(0, 359, (int) (hsb[0] * 359));
        sField = new UIntegerTextField(0, 100, (int) (hsb[1] * 100));
        bField = new UIntegerTextField(0, 100, (int) (hsb[2] * 100));
        NumberListener fieldListener = new NumberListener();
        hField.getDocument().addDocumentListener(fieldListener);
        sField.getDocument().addDocumentListener(fieldListener);
        bField.getDocument().addDocumentListener(fieldListener);
        JPanel panel = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(hRadio, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(hField, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(sRadio, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(sField, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(bRadio, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(bField, sideConstraints);
        return panel;
    }

    /**
     * Creates the panel with the uneditable RGB field.
     */
    private JComponent buildRGBControls() {
        JPanel panel = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        SideConstraints sideConstraints;
        Color color = getColorFromModel();
        redField = new JTextField(String.valueOf(color.getRed()), 3);
        redField.setEditable(false);
        redField.addFocusListener(new FocusAdapter() {
            /**
             * Invoked when a component gains the keyboard focus.
             */
            public void focusGained(FocusEvent e) {
                redField.transferFocus();
            }
        });
        redField.setHorizontalAlignment(JTextField.RIGHT);
        greenField = new JTextField(String.valueOf(color.getGreen()), 3);
        greenField.setEditable(false);
        greenField.addFocusListener(new FocusAdapter() {
            /**
             * Invoked when a component gains the keyboard focus.
             */
            public void focusGained(FocusEvent e) {
                greenField.transferFocus();
            }
        });
        greenField.setHorizontalAlignment(JTextField.RIGHT);
        blueField = new JTextField(String.valueOf(color.getBlue()), 3);
        blueField.setEditable(false);
        blueField.addFocusListener(new FocusAdapter() {
            /**
             * Invoked when a component gains the keyboard focus.
             */
            public void focusGained(FocusEvent e) {
                blueField.transferFocus();
            }
        });
        blueField.setHorizontalAlignment(JTextField.RIGHT);
        String redString = UIManager.getString("ColorChooser.hsbRedText");
        String greenString = UIManager.getString("ColorChooser.hsbGreenText");
        String blueString = UIManager.getString("ColorChooser.hsbBlueText");
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(fxts.stations.ui.UIManager.getInst().createLabel(redString), sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(redField, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(fxts.stations.ui.UIManager.getInst().createLabel(greenString), sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 1;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(greenField, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(fxts.stations.ui.UIManager.getInst().createLabel(blueString), sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 2;
        sideConstraints.insets = new Insets(1, 1, 1, 1);
        sideConstraints.fill = SideConstraints.BOTH;
        panel.add(blueField, sideConstraints);
        return panel;
    }

    /**
     * Initializes components of panel.
     */
    protected JComponent buildSliderPalettePanel() {
        // This slider has to have a minimum of 0.  A lot of math in this file is simplified due to this.
        slider = new JSlider(JSlider.VERTICAL, 0, MAX_HUE_VALUE, 0);
        slider.setInverted(true);
        slider.setPaintTrack(false);
        slider.setPreferredSize(new Dimension(slider.getPreferredSize().width, PALETTE_DIMENSION + 15));
        slider.addChangeListener(this);
        paletteLabel = createPaletteLabel();
        addPaletteListeners();
        sliderPaletteLabel = fxts.stations.ui.UIManager.getInst().createLabel();
        JPanel panel = new JPanel();
        panel.add(paletteLabel);
        panel.add(slider);
        panel.add(sliderPaletteLabel);
        initializePalettesIfNecessary();
        return panel;
    }

    /**
     * Cleans palettes.
     */
    private void cleanupPalettesIfNecessary() {
        if (palette == null) {
            return;
        }
        palette.aborted = true;
        sliderPalette.aborted = true;
        palette.nextFrame();
        sliderPalette.nextFrame();
        palette = null;
        sliderPalette = null;
        paletteImage = null;
        sliderPaletteImage = null;
        paletteLabel.setIcon(null);
        sliderPaletteLabel.setIcon(null);
    }

    /**
     * Creates palette label.
     */
    protected JLabel createPaletteLabel() {
        return new JLabel() {
            protected void paintComponent(Graphics g) {
                if (fxts.stations.ui.UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                g.setColor(Color.WHITE);
                g.drawOval(paletteSelection.x - 4, paletteSelection.y - 4, 8, 8);
                super.paintComponent(g);
            }
        };
    }

    /**
     * Returns display name.
     */
    public String getDisplayName() {
        return UIManager.getString("ColorChooser.hsbNameText");
    }

    /**
     * Returns an float array containing the HSB values of the selected color from
     * the ColorSelectionModel
     */
    private float[] getHSBColorFromModel() {
        Color color = getColorFromModel();
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        return hsb;
    }

    /**
     * HierarchyListener`s method.
     * Called when the hierarchy has been changed.
     */
    public void hierarchyChanged(HierarchyEvent he) {
        if ((he.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
            if (isDisplayable()) {
                initializePalettesIfNecessary();
            } else {
                cleanupPalettesIfNecessary();
            }
        }
    }

    /**
     * Initializes palettes.
     */
    private void initializePalettesIfNecessary() {
        if (palette != null) {
            return;
        }
        float[] hsb = getHSBColorFromModel();
        palette = new HSBImage(HSBImage.HSQUARE, PALETTE_DIMENSION, PALETTE_DIMENSION, hsb[0], 1.0f, 1.0f);
        sliderPalette = new HSBImage(HSBImage.HSLIDER, 16, PALETTE_DIMENSION, 0f, 1.0f, 1.0f);
        paletteImage = Toolkit.getDefaultToolkit().createImage(palette);
        sliderPaletteImage = Toolkit.getDefaultToolkit().createImage(sliderPalette);
        paletteLabel.setIcon(new ImageIcon(paletteImage));
        sliderPaletteLabel.setIcon(new ImageIcon(sliderPaletteImage));
    }

    /**
     * Installs chooser panels.
     */
    public void installChooserPanel(ColorChooser enclosingChooser) {
        super.installChooserPanel(enclosingChooser);
        addHierarchyListener(this);
    }

    /**
     * Sets mode of chooser panel.
     */
    private void setMode(int mode) {
        if (currentMode == mode) {
            return;
        }
        isAdjusting = true;  // Ensure no events propagate from changing slider value.
        currentMode = mode;
        float[] hsb = getHSBColorFromModel();
        switch (currentMode) {
            case HUE_MODE:
                slider.setInverted(true);
                slider.setMaximum(MAX_HUE_VALUE);
                palette.setValues(HSBImage.HSQUARE, hsb[0], 1.0f, 1.0f);
                sliderPalette.setValues(HSBImage.HSLIDER, 0f, 1.0f, 1.0f);
                break;
            case SATURATION_MODE:
                slider.setInverted(false);
                slider.setMaximum(MAX_SATURATION_VALUE);
                palette.setValues(HSBImage.SSQUARE, hsb[0], hsb[1], 1.0f);
                sliderPalette.setValues(HSBImage.SSLIDER, hsb[0], 1.0f, 1.0f);
                break;
            case BRIGHTNESS_MODE:
                slider.setInverted(false);
                slider.setMaximum(MAX_BRIGHTNESS_VALUE);
                palette.setValues(HSBImage.BSQUARE, hsb[0], 1.0f, hsb[2]);
                sliderPalette.setValues(HSBImage.BSLIDER, hsb[0], 1.0f, 1.0f);
                break;
        }
        isAdjusting = false;
        palette.nextFrame();
        sliderPalette.nextFrame();
        updateChooser();
    }

    /**
     * ChangeListener`s method.
     * Invoked when the target of the listener has changed its state.
     */
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == slider) {
            boolean modelIsAdjusting = slider.getModel().getValueIsAdjusting();
            if (!modelIsAdjusting && !isAdjusting) {
                int sliderValue = slider.getValue();
                int sliderRange = slider.getMaximum();
                float value = (float) sliderValue / (float) sliderRange;
                float[] hsb = getHSBColorFromModel();
                switch (currentMode) {
                    case HUE_MODE:
                        updateHSB(value, hsb[1], hsb[2]);
                        break;
                    case SATURATION_MODE:
                        updateHSB(hsb[0], value, hsb[2]);
                        break;
                    case BRIGHTNESS_MODE:
                        updateHSB(hsb[0], hsb[1], value);
                        break;
                }
            }
        }
    }

    /**
     * Invoked when the panel is removed from the chooser.
     */
    public void uninstallChooserPanel(ColorChooser enclosingChooser) {
        super.uninstallChooserPanel(enclosingChooser);
        cleanupPalettesIfNecessary();
        removeAll();
        removeHierarchyListener(this);
    }

    /**
     * Invoked automatically when the model's state changes.
     * It is also called by <code>installChooserPanel</code> to allow
     * you to set up the initial state of your chooser.
     * Override this method to update your <code>ChooserPanel</code>.
     */
    public void updateChooser() {
        if (!isAdjusting) {
            float[] hsb = getHSBColorFromModel();
            updateHSB(hsb[0], hsb[1], hsb[2]);
        }
    }

    /**
     * Main internal method of updating the ui controls and the color model.
     */
    private void updateHSB(float h, float s, float b) {
        if (!isAdjusting) {
            isAdjusting = true;
            updatePalette(h, s, b);
            updateSlider(h, s, b);
            updateHSBTextFields(h, s, b);
            Color color = Color.getHSBColor(h, s, b);
            updateRGBTextFields(color);
            getColorSelectionModel().setSelectedColor(color);
            isAdjusting = false;
        }
    }

    /**
     * Updates HSB text fields.
     */
    private void updateHSBTextFields(float hue, float saturation, float brightness) {
        int h = Math.round(hue * 359);
        int s = Math.round(saturation * 100);
        int b = Math.round(brightness * 100);
        if (hField.getIntegerValue() != h) {
            hField.setText(String.valueOf(h));
        }
        if (sField.getIntegerValue() != s) {
            sField.setText(String.valueOf(s));
        }
        if (bField.getIntegerValue() != b) {
            bField.setText(String.valueOf(b));
        }
    }

    /**
     * Updates palette.
     */
    private void updatePalette(float h, float s, float b) {
        int x = 0;
        int y = 0;
        switch (currentMode) {
            case HUE_MODE:
                if (h != palette.getHue()) {
                    palette.setHue(h);
                    palette.nextFrame();
                }
                x = PALETTE_DIMENSION - (int) (s * PALETTE_DIMENSION);
                y = PALETTE_DIMENSION - (int) (b * PALETTE_DIMENSION);
                break;
            case SATURATION_MODE:
                if (s != palette.getSaturation()) {
                    palette.setSaturation(s);
                    palette.nextFrame();
                }
                x = (int) (h * PALETTE_DIMENSION);
                y = PALETTE_DIMENSION - (int) (b * PALETTE_DIMENSION);
                break;
            case BRIGHTNESS_MODE:
                if (b != palette.getBrightness()) {
                    palette.setBrightness(b);
                    palette.nextFrame();
                }
                x = (int) (h * PALETTE_DIMENSION);
                y = PALETTE_DIMENSION - (int) (s * PALETTE_DIMENSION);
                break;
        }
        paletteSelection.setLocation(x, y);
        paletteLabel.repaint();
    }

    /**
     * Updates the values of the RGB fields to reflect the new color change.
     */
    private void updateRGBTextFields(Color color) {
        redField.setText(String.valueOf(color.getRed()));
        greenField.setText(String.valueOf(color.getGreen()));
        blueField.setText(String.valueOf(color.getBlue()));
    }

    /**
     * Updates slider.
     */
    private void updateSlider(float h, float s, float b) {
        // Update the slider palette if necessary.
        // When the slider is the hue slider or the hue hasn't changed,
        // the hue of the palette will not need to be updated.
        if (currentMode != HUE_MODE && h != sliderPalette.getHue()) {
            sliderPalette.setHue(h);
            sliderPalette.nextFrame();
        }
        float value = 0f;
        switch (currentMode) {
            case HUE_MODE:
                value = h;
                break;
            case SATURATION_MODE:
                value = s;
                break;
            case BRIGHTNESS_MODE:
                value = b;
                break;
        }
        slider.setValue(Math.round(value * slider.getMaximum()));
    }

    /**
     * Class for the slider and palette images.
     */
    private class HSBImage extends SyntheticImage {
        private static final int HSQUARE = 0;
        private static final int SSQUARE = 1;
        private static final int BSQUARE = 2;
        private static final int HSLIDER = 3;
        private static final int SSLIDER = 4;
        private static final int BSLIDER = 5;
        protected float b = .0f;
        protected int cachedColor;
        protected int cachedY;
        /*-- Data members --*/
        protected float h = .0f;
        protected float[] hsb = new float[3];
        protected boolean isDirty = true;
        protected float s = .0f;
        protected int type;

        /**
         * Protected constructor.
         */
        protected HSBImage(int type, int width, int height, float h, float s, float b) {
            super(width, height);
            setValues(type, h, s, b);
        }

        /**
         * Adds customer.
         */
        public synchronized void addConsumer(ImageConsumer ic) {
            isDirty = true;
            super.addConsumer(ic);
        }

        /**
         * Overriden method from SyntheticImage.
         */
        protected void computeRow(int y, int[] row) {
            if (y == 0) {
                synchronized (this) {
                    try {
                        while (!isDirty) {
                            wait();
                        }
                    } catch (InterruptedException ie) {
                    }
                    isDirty = false;
                }
            }
            if (aborted) {
                return;
            }
            for (int i = 0; i < row.length; ++i) {
                row[i] = getRGBForLocation(i, y);
            }
        }

        /**
         * Returns brightness.
         */
        public final float getBrightness() {
            return b;
        }

        /**
         * Return HSB value for specified point.
         */
        public void getHSBForLocation(int x, int y, float[] hsbArray) {
            switch (type) {
                case HSQUARE: {
                    float saturationStep = (float) x / width;
                    float brightnessStep = (float) y / height;
                    hsbArray[0] = h;
                    hsbArray[1] = s - saturationStep;
                    hsbArray[2] = b - brightnessStep;
                    break;
                }
                case SSQUARE: {
                    float brightnessStep = (float) y / height;
                    float step = 1.0f / (float) width;
                    hsbArray[0] = x * step;
                    hsbArray[1] = s;
                    hsbArray[2] = 1.0f - brightnessStep;
                    break;
                }
                case BSQUARE: {
                    float saturationStep = (float) y / height;
                    float step = 1.0f / (float) width;
                    hsbArray[0] = x * step;
                    hsbArray[1] = 1.0f - saturationStep;
                    hsbArray[2] = b;
                    break;
                }
                case HSLIDER: {
                    float step = 1.0f / (float) height;
                    hsbArray[0] = y * step;
                    hsbArray[1] = s;
                    hsbArray[2] = b;
                    break;
                }
                case SSLIDER: {
                    float saturationStep = (float) y / height;
                    hsbArray[0] = h;
                    hsbArray[1] = s - saturationStep;
                    hsbArray[2] = b;
                    break;
                }
                case BSLIDER: {
                    float brightnessStep = (float) y / height;
                    hsbArray[0] = h;
                    hsbArray[1] = s;
                    hsbArray[2] = b - brightnessStep;
                    break;
                }
            }
        }

        /**
         * Returns hue.
         */
        public final float getHue() {
            return h;
        }

        /**
         * Returns RGB value for soecified point.
         */
        private int getRGBForLocation(int x, int y) {
            if (type >= HSLIDER && y == cachedY) {
                return cachedColor;
            }
            getHSBForLocation(x, y, hsb);
            cachedY = y;
            cachedColor = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
            return cachedColor;
        }

        /**
         * Returns saturation.
         */
        public final float getSaturation() {
            return s;
        }

        /**
         * Check for static.
         */
        protected boolean isStatic() {
            return false;
        }

        /**
         * Steps to next frame.
         */
        public synchronized void nextFrame() {
            isDirty = true;
            notifyAll();
        }

        /**
         * Sets brightness.
         */
        public final void setBrightness(float brightness) {
            b = brightness;
        }

        /**
         * Sets hue.
         */
        public final void setHue(float hue) {
            h = hue;
        }

        /**
         * Sets saturation.
         */
        public final void setSaturation(float saturation) {
            s = saturation;
        }

        /**
         * Sets values.
         */
        public void setValues(int type, float h, float s, float b) {
            this.type = type;
            cachedY = -1;
            cachedColor = 0;
            setHue(h);
            setSaturation(s);
            setBrightness(b);
        }
    }

    /**
     * Listener for changes of the text fields.
     */
    private class NumberListener implements DocumentListener {
        /**
         * Gives notification that an attribute or set of attributes changed.
         */
        public void changedUpdate(DocumentEvent e) {
        }

        /**
         * Gives notification that there was an insert into the document.
         */
        public void insertUpdate(DocumentEvent e) {
            updatePanel(e);
        }

        /**
         * Gives notification that a portion of the document has been removed.
         */
        public void removeUpdate(DocumentEvent e) {
            updatePanel(e);
        }

        /**
         * Upates panel.
         */
        private void updatePanel(DocumentEvent e) {
            float hue = (float) hField.getIntegerValue() / 359f;
            float saturation = (float) sField.getIntegerValue() / 100f;
            float brightness = (float) bField.getIntegerValue() / 100f;
            updateHSB(hue, saturation, brightness);
        }
    }

    /**
     * Handler for the radio button classes.
     */
    private class RadioButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Object obj = evt.getSource();
            if (obj instanceof JRadioButton) {
                JRadioButton button = (JRadioButton) obj;
                if (button == hRadio) {
                    setMode(HUE_MODE);
                } else if (button == sRadio) {
                    setMode(SATURATION_MODE);
                } else if (button == bRadio) {
                    setMode(BRIGHTNESS_MODE);
                }
            }
        }
    }
}
