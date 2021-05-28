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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.io.Serializable;

/**
 * This is the abstract superclass for color choosers.  If you want to add
 * a new color chooser panel into a <code>ColorChooser</code>, subclass
 * this class.
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases.  The current serialization support is appropriate
 * for short term storage or RMI between applications running the same
 * version of Swing.  A future release of Swing will provide support for
 * long term persistence.
 */
public abstract class AColorChooserPanel extends JPanel {
    /**
     * Listener of chooser model.
     */
    class ModelListener implements ChangeListener, Serializable {
        public void stateChanged(ChangeEvent e) {
            if (isShowing()) {  // isVisible
                updateChooser();
                dirty = false;
            } else {
                dirty = true;
            }
        }
    }

    /*-- Data members --*/
    /**
     * Color chooser panel.
     */
    private ColorChooser chooser;
    /**
     * Chaange listener.
     */
    private ChangeListener colorListener;
    /**
     * Is panel dirty?
     */
    private boolean dirty = true;

    /**
     * Builds a new chooser panel.
     */
    protected abstract void buildChooser();

    /**
     * Returns the color that is currently selected.
     *
     * @return the <code>Color</code> that is selected
     */
    protected Color getColorFromModel() {
        return getColorSelectionModel().getSelectedColor();
    }

    /**
     * Returns the model that the chooser panel is editing.
     *
     * @return the <code>ColorSelectionModel</code> model this panel
     *         is editing
     */
    public ColorSelectionModel getColorSelectionModel() {
        return chooser.getSelectionModel();
    }

    /**
     * Returns a string containing the display name of the panel.
     *
     * @return the name of the display panel
     */
    public abstract String getDisplayName();

    /**
     * Returns an integer from the defaults table. If <code>key</code> does
     * not map to a valid <code>Integer</code>, <code>default</code> is
     * returned.
     *
     * @param key          an <code>Object</code> specifying the int
     * @param defaultValue Returned value if <code>key</code> is not available,
     *                     or is not an Integer
     *
     * @return the int
     */
    static int getInt(Object key, int defaultValue) {
        Object value = UIManager.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException nfe) {
            }
        }
        return defaultValue;
    }

    /**
     * Invoked when the panel is added to the chooser.
     * If you override this, be sure to call <code>super</code>.
     *
     * @param enclosingChooser the panel to be added
     *
     * @throws RuntimeException if the chooser panel has already been
     *                          installed
     */
    public void installChooserPanel(ColorChooser enclosingChooser) {
        if (chooser != null) {
            throw new RuntimeException("This chooser panel is already installed");
        }
        chooser = enclosingChooser;
        buildChooser();
        updateChooser();
        colorListener = new ModelListener();
        getColorSelectionModel().addChangeListener(colorListener);
    }

    /**
     * Draws the panel.
     *
     * @param g the <code>Graphics</code> object
     */
    public void paint(Graphics g) {
        if (dirty) {
            updateChooser();
            dirty = false;
        }
        super.paint(g);
    }

    /**
     * Invoked when the panel is removed from the chooser.
     * If override this, be sure to call <code>super</code>.
     */
    public void uninstallChooserPanel(ColorChooser enclosingChooser) {
        getColorSelectionModel().removeChangeListener(colorListener);
        chooser = null;
    }

    /*-- Public methods --*/

    /**
     * Invoked automatically when the model's state changes.
     * It is also called by <code>installChooserPanel</code> to allow
     * you to set up the initial state of your chooser.
     * Override this method to update your <code>ChooserPanel</code>.
     */
    public abstract void updateChooser();
}