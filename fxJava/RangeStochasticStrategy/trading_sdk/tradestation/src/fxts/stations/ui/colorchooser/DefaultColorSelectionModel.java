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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.io.Serializable;

/**
 * A generic implementation of <code>ColorSelectionModel</code>.
 */
public class DefaultColorSelectionModel implements ColorSelectionModel, Serializable {
    /**
     * Only one <code>ChangeEvent</code> is needed per model instance
     * since the event's only (read-only) state is the source property.
     * The source of events generated here is always "this".
     */
    protected transient ChangeEvent mChangeEvent;
    protected EventListenerList mListenerList = new EventListenerList();
    private Color mSelectedColor;

    /**
     * Creates a <code>DefaultColorSelectionModel</code> with the
     * current color set to <code>color</code>, which should be
     * non-<code>null</code>.  Note that setting the color to
     * <code>null</code> is undefined and may have unpredictable
     * results.
     *
     * @param aColor the new <code>Color</code>
     */
    public DefaultColorSelectionModel(Color aColor) {
        mSelectedColor = aColor;
    }

    /**
     * Adds a <code>ChangeListener</code> to the model.
     *
     * @param aListener the <code>ChangeListener</code> to be added
     */
    public void addChangeListener(ChangeListener aListener) {
        mListenerList.add(ChangeListener.class, aListener);
    }

    /**
     * Runs each <code>ChangeListener</code>'s
     * <code>stateChanged</code> method.
     */
    protected void fireStateChanged() {
        Object[] listeners = mListenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (mChangeEvent == null) {
                    mChangeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(mChangeEvent);
            }
        }
    }

    /**
     * Returns the selected <code>Color</code> which should be
     * non-<code>null</code>.
     *
     * @return the selected <code>Color</code>
     */
    public Color getSelectedColor() {
        return mSelectedColor;
    }

    /**
     * Removes a <code>ChangeListener</code> from the model.
     *
     * @param aListener the <code>ChangeListener</code> to be removed
     */
    public void removeChangeListener(ChangeListener aListener) {
        mListenerList.remove(ChangeListener.class, aListener);
    }

    /**
     * Sets the selected color to <code>color</code>.
     * Note that setting the color to <code>null</code>
     * is undefined and may have unpredictable results.
     * This method fires a state changed event if it sets the
     * current color to a new non-<code>null</code> color;
     * if the new color is the same as the current color,
     * no event is fired.
     *
     * @param aColor the new <code>Color</code>
     */
    public void setSelectedColor(Color aColor) {
        if (aColor != null && !mSelectedColor.equals(aColor)) {
            mSelectedColor = aColor;
            fireStateChanged();
        }
    }
}