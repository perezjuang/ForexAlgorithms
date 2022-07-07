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

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * <code>ColorChooser</code> provides a pane of controls designed to allow
 * a user to manipulate and select a color.
 */
public class ColorChooser extends JComponent implements Accessible {
    /**
     * The selection model property name.
     */
    public static final String SELECTION_MODEL_PROPERTY = "selectionModel";
    /**
     * The preview panel property name.
     */
    public static final String PREVIEW_PANEL_PROPERTY = "previewPanel";
    /**
     * The chooserPanel array property name.
     */
    public static final String CHOOSER_PANELS_PROPERTY = "chooserPanels";

    /**
     * This class implements accessibility support for the
     * <code>ColorChooser</code> class.  It provides an implementation of the
     * Java Accessibility API appropriate to color chooser user-interface
     * elements.
     */
    protected class AccessibleColorChooser extends AccessibleJComponent {
        /**
         * Get the role of this object.
         *
         * @return an instance of AccessibleRole describing the role of the
         *         object
         *
         * @see AccessibleRole
         */
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.COLOR_CHOOSER;
        }
    }

    /**
     * Preview listener for color`s changes.
     */
    private class PreviewListener implements ChangeListener {
        public void stateChanged(ChangeEvent aEvent) {
            ColorSelectionModel model = (ColorSelectionModel) aEvent.getSource();
            if (mPreviewPanel != null) {
                mPreviewPanel.setForeground(model.getSelectedColor());
                mPreviewPanel.repaint();
            }
        }
    }

    /**
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    private class PropertyHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent aEvent) {
            SideConstraints sideConstraints;
            ResizeParameter resizeParameter;
            if (aEvent.getPropertyName().equals(ColorChooser.CHOOSER_PANELS_PROPERTY)) {
                AColorChooserPanel[] oldPanels = (AColorChooserPanel[]) aEvent.getOldValue();
                AColorChooserPanel[] newPanels = (AColorChooserPanel[]) aEvent.getNewValue();
                for (int i = 0; i < oldPanels.length; i++) {  // remove old panels
                    Container wrapper = oldPanels[i].getParent();
                    if (wrapper != null) {
                        Container parent = wrapper.getParent();
                        if (parent != null) {
                            parent.remove(wrapper);  // remove from hierarchy
                        }
                        oldPanels[i].uninstallChooserPanel(ColorChooser.this); // uninstall
                    }
                }
                int numNewPanels = newPanels.length;
                if (numNewPanels == 0) {  // removed all panels and added none
                    remove(mTabbedPane);
                    return;
                } else if (numNewPanels == 1) {  // one panel case
                    remove(mTabbedPane);
                    JPanel centerWrapper = new JPanel(UIFrontEnd.getInstance().getSideLayout());
                    sideConstraints = new SideConstraints();
                    sideConstraints.gridx = 0;
                    sideConstraints.gridy = 0;
                    sideConstraints.fill = SideConstraints.BOTH;
                    resizeParameter = new ResizeParameter();
                    resizeParameter.init(0.0, 0.0, 1.0, 1.0);
                    resizeParameter.setToConstraints(sideConstraints);
                    centerWrapper.add(newPanels[0], sideConstraints);
                    sideConstraints = new SideConstraints();
                    sideConstraints.gridx = 0;
                    sideConstraints.gridy = 0;
                    sideConstraints.fill = SideConstraints.BOTH;
                    resizeParameter = new ResizeParameter();
                    resizeParameter.init(0.0, 0.0, 1.0, 1.0);
                    resizeParameter.setToConstraints(sideConstraints);
                    mSinglePanel.add(centerWrapper, sideConstraints);
                    sideConstraints = new SideConstraints();
                    sideConstraints.gridx = 0;
                    sideConstraints.gridy = 0;
                    sideConstraints.fill = SideConstraints.BOTH;
                    resizeParameter = new ResizeParameter();
                    resizeParameter.init(0.0, 0.0, 1.0, 1.0);
                    resizeParameter.setToConstraints(sideConstraints);
                    add(mSinglePanel, sideConstraints);
                } else {   // multi-panel case
                    if (oldPanels.length < 2) {
                        remove(mSinglePanel);
                        mTabbedPane = new JTabbedPane();
                        sideConstraints = new SideConstraints();
                        sideConstraints.gridx = 0;
                        sideConstraints.gridy = 0;
                        sideConstraints.fill = SideConstraints.BOTH;
                        resizeParameter = new ResizeParameter();
                        resizeParameter.init(0.0, 0.0, 1.0, 1.0);
                        resizeParameter.setToConstraints(sideConstraints);
                        add(mTabbedPane, sideConstraints);
                    }
                    for (int i = 0; i < newPanels.length; i++) {
                        JPanel centerWrapper = new JPanel(UIFrontEnd.getInstance().getSideLayout());
                        sideConstraints = new SideConstraints();
                        sideConstraints.gridx = 0;
                        sideConstraints.gridy = 0;
                        sideConstraints.fill = SideConstraints.BOTH;
                        resizeParameter = new ResizeParameter();
                        resizeParameter.init(0.0, 0.0, 1.0, 1.0);
                        resizeParameter.setToConstraints(sideConstraints);
                        centerWrapper.add(newPanels[i], sideConstraints);
                        mTabbedPane.addTab(newPanels[i].getDisplayName(), centerWrapper);
                    }
                }
                for (int i = 0; i < newPanels.length; i++) {
                    newPanels[i].installChooserPanel(ColorChooser.this);
                }
            }
            if (aEvent.getPropertyName().equals(ColorChooser.PREVIEW_PANEL_PROPERTY)) {
                JComponent oldPanel = (JComponent) aEvent.getOldValue();
                JComponent newPanel = (JComponent) aEvent.getNewValue();
                if (oldPanel != null) {
                    mPreviewPanelHolder.remove(oldPanel);
                }
                sideConstraints = new SideConstraints();
                sideConstraints.gridx = 0;
                sideConstraints.gridy = 0;
                sideConstraints.fill = SideConstraints.BOTH;
                resizeParameter = new ResizeParameter();
                resizeParameter.init(0.0, 0.0, 1.0, 1.0);
                resizeParameter.setToConstraints(sideConstraints);
                mPreviewPanelHolder.add(newPanel, sideConstraints);
            }
        }
    }

    /**
     * Accessible context.
     */
    protected AccessibleContext mAccessibleContext;
    /**
     * Chooser panels.
     */
    private AColorChooserPanel[] mChooserPanels = new AColorChooserPanel[0];
    /**
     * Preview panel.
     */
    private JComponent mPreviewPanel;
    /**
     * Holder of preview panel.
     */
    private JPanel mPreviewPanelHolder;

    /**
     * Selection model.
     */
    private ColorSelectionModel mSelectionModel;
    /**
     * Single panel.
     */
    private JPanel mSinglePanel;
    /**
     * Tabbed panel.
     */
    private JTabbedPane mTabbedPane;

    /**
     * Creates a color chooser pane with an initial color of white.
     */
    public ColorChooser() {
        this(Color.WHITE);
    }

    /**
     * Creates a color chooser pane with the specified initial color.
     *
     * @param aInitialColor the initial color set in the chooser
     */
    public ColorChooser(Color aInitialColor) {
        this(new DefaultColorSelectionModel(aInitialColor));
    }

    /**
     * Creates a color chooser pane with the specified
     * <code>ColorSelectionModel</code>.
     *
     * @param aModel the <code>ColorSelectionModel</code> to be used
     */
    public ColorChooser(ColorSelectionModel aModel) {
        mSelectionModel = aModel;
        initComponents();
    }

    /**
     * Adds a color chooser panel to the color chooser.
     *
     * @param aPanel the <code>AColorChooserPanel</code> to be added
     */
    public void addChooserPanel(AColorChooserPanel aPanel) {
        AColorChooserPanel[] oldPanels = getChooserPanels();
        AColorChooserPanel[] newPanels = new AColorChooserPanel[oldPanels.length + 1];
        System.arraycopy(oldPanels, 0, newPanels, 0, oldPanels.length);
        newPanels[newPanels.length - 1] = aPanel;
        setChooserPanels(newPanels);
    }

    /**
     * Gets the AccessibleContext associated with this ColorChooser.
     * For color choosers, the AccessibleContext takes the form of an
     * AccessibleColorChooser.
     * A new AccessibleColorChooser instance is created if necessary.
     *
     * @return an AccessibleColorChooser that serves as the
     *         AccessibleContext of this ColorChooser
     */
    public AccessibleContext getAccessibleContext() {
        if (mAccessibleContext == null) {
            mAccessibleContext = new AccessibleColorChooser();
        }
        return mAccessibleContext;
    }

    /**
     * Returns the specified color panels.
     *
     * @return an array of <code>AColorChooserPanel</code> objects
     */
    public AColorChooserPanel[] getChooserPanels() {
        return mChooserPanels;
    }

    /**
     * Gets the current color value from the color chooser.
     * By default, this delegates to the model.
     *
     * @return the current color value of the color chooser
     */
    public Color getColor() {
        return mSelectionModel.getSelectedColor();
    }

    /**
     * Returns the preview panel that shows a chosen color.
     *
     * @return a <code>JComponent</code> object -- the preview panel
     */
    public JComponent getPreviewPanel() {
        return mPreviewPanel;
    }

    /**
     * Returns the data model that handles color selections.
     *
     * @return a <code>ColorSelectionModel</code> object
     */
    public ColorSelectionModel getSelectionModel() {
        return mSelectionModel;
    }

    /**
     * Initailzation of components.
     */
    private void initComponents() {
        AColorChooserPanel[] defaultChoosers;
        ChangeListener previewListener;
        PropertyChangeListener propertyChangeListener;
        SideConstraints sideConstraints;
        ResizeParameter resizeParameter;

        //adds listeners
        propertyChangeListener = new PropertyHandler();
        addPropertyChangeListener(propertyChangeListener);
        previewListener = new PreviewListener();
        getSelectionModel().addChangeListener(previewListener);
        mTabbedPane = new JTabbedPane();
        mSinglePanel = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        setLayout(UIFrontEnd.getInstance().getSideLayout());

        //creates choser`s panels
        defaultChoosers = new AColorChooserPanel[]{new SwatchChooserPanel(), new HSBChooserPanel(), new RGBChooserPanel()};
        setChooserPanels(defaultChoosers);

        //instals preview panel
        mPreviewPanelHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        String previewString = UIManager.getString("ColorChooser.previewText");
        mPreviewPanelHolder.setBorder(new TitledBorder(previewString));
        if (mPreviewPanel == null || mPreviewPanel instanceof UIResource) {
            mPreviewPanel = new PreviewPanel();
        }
        mPreviewPanel.setForeground(getColor());
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.insets = new Insets(5, 50, 5, 50);
        sideConstraints.fill = SideConstraints.BOTH;
        resizeParameter = new ResizeParameter();
        resizeParameter.init(0.5, 0.0, 0.5, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        mPreviewPanelHolder.add(mPreviewPanel, sideConstraints);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        sideConstraints.fill = SideConstraints.BOTH;
        resizeParameter = new ResizeParameter();
        resizeParameter.init(0.0, 1.0, 1.0, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        add(mPreviewPanelHolder, sideConstraints);
    }

    /**
     * Removes the Color Panel specified.
     *
     * @param aPanel color panel
     * @return the color panel
     *
     * @throws IllegalArgumentException if panel is not in list of
     *                                  known chooser panels
     */
    public AColorChooserPanel removeChooserPanel(AColorChooserPanel aPanel) {
        int containedAt = -1;
        for (int i = 0; i < mChooserPanels.length; i++) {
            if (mChooserPanels[i] == aPanel) {
                containedAt = i;
                break;
            }
        }
        if (containedAt == -1) {
            throw new IllegalArgumentException("chooser panel not in this chooser");
        }
        AColorChooserPanel[] newArray = new AColorChooserPanel[mChooserPanels.length - 1];
        if (containedAt == mChooserPanels.length - 1) {  // at end
            System.arraycopy(mChooserPanels, 0, newArray, 0, newArray.length);
        } else if (containedAt == 0) {  // at start
            System.arraycopy(mChooserPanels, 1, newArray, 0, newArray.length);
        } else {  // in middle
            System.arraycopy(mChooserPanels, 0, newArray, 0, containedAt);
            System.arraycopy(mChooserPanels, containedAt + 1,
                             newArray, containedAt, mChooserPanels.length - containedAt - 1);
        }
        setChooserPanels(newArray);
        return aPanel;
    }

    /**
     * Specifies the Color Panels used to choose a color value.
     *
     * @param aPanels an array of <code>AColorChooserPanel</code> objects
     */
    public void setChooserPanels(AColorChooserPanel[] aPanels) {
        AColorChooserPanel[] oldValue = mChooserPanels;
        mChooserPanels = aPanels;
        firePropertyChange(CHOOSER_PANELS_PROPERTY, oldValue, aPanels);
    }

    /**
     * Sets the current color of the color chooser to the specified color.
     * The <code>ColorSelectionModel</code> will fire a <code>ChangeEvent</code>
     *
     * @param aColor the color to be set in the color chooser
     *
     * @see JComponent#addPropertyChangeListener
     *      bound: false
     *      hidden: false
     *      description: The current color the chooser is to display.
     */
    public void setColor(Color aColor) {
        mSelectionModel.setSelectedColor(aColor);
    }

    /**
     * Sets the current color of the color chooser to the
     * specified color.
     *
     * @param aColor an integer value that sets the current color in the chooser
     *          where the low-order 8 bits specify the Blue value,
     *          the next 8 bits specify the Green value, and the 8 bits
     *          above that specify the Red value.
     */
    public void setColor(int aColor) {
        setColor(aColor >> 16 & 0xFF, aColor >> 8 & 0xFF, aColor & 0xFF);
    }

    /**
     * Sets the current color of the color chooser to the
     * specified RGB color.  Note that the values of red, green,
     * and blue should be between the numbers 0 and 255, inclusive.
     *
     * @param aRed an int specifying the amount of Red
     * @param aGreen an int specifying the amount of Green
     * @param aBlue an int specifying the amount of Blue
     *
     * @throws IllegalArgumentException if r,g,b values are out of range
     */
    public void setColor(int aRed, int aGreen, int aBlue) {
        setColor(new Color(aRed, aGreen, aBlue));
    }

    /**
     * Sets the current preview panel.
     * This will fire a <code>PropertyChangeEvent</code> for the property
     * named "previewPanel".
     *
     * @param aPreview the <code>JComponent</code> which displays the current color
     */
    public void setPreviewPanel(JComponent aPreview) {
        if (mPreviewPanel != aPreview) {
            JComponent oldPreview = mPreviewPanel;
            mPreviewPanel = aPreview;
            firePropertyChange(PREVIEW_PANEL_PROPERTY, oldPreview, aPreview);
        }
    }

    /**
     * Sets the model containing the selected color.
     *
     * @param aNewModel the new <code>ColorSelectionModel</code> object
     *
     */
    public void setSelectionModel(ColorSelectionModel aNewModel) {
        ColorSelectionModel oldModel = mSelectionModel;
        mSelectionModel = aNewModel;
        firePropertyChange(SELECTION_MODEL_PROPERTY, oldModel, aNewModel);
    }
}