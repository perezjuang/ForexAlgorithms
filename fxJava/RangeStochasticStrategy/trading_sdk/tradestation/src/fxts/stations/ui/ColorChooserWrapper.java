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
package fxts.stations.ui;

import javax.swing.JComponent;
import javax.swing.colorchooser.ColorSelectionModel;
import java.awt.Color;
import java.lang.reflect.Method;

/**
 * Simple wrapper for ColorChooser class.
 * Class support all necessary method, which needed
 * for correct usage of ColorChooser class.
 */
public class ColorChooserWrapper {
    private Object mChooser;
    private Method mGetColorMethod;
    private Method mGetSelectionModelMethod;
    private Method mSetColorMethod;
    private Method mSetPreviewPanelMethod;

    /**
     * Construct wrapper object and get all necessary methods.
     *
     * @param aChooser wrapped object
     */
    public ColorChooserWrapper(Object aChooser) throws Exception {
        mChooser = aChooser;
        mGetSelectionModelMethod = aChooser.getClass().getMethod("getSelectionModel", new Class[]{});
        mSetColorMethod = aChooser.getClass().getMethod("setColor", new Class[]{Color.class});
        mGetColorMethod = aChooser.getClass().getMethod("getColor", new Class[]{});
        mSetPreviewPanelMethod = aChooser.getClass().getMethod("setPreviewPanel", new Class[]{JComponent.class});
    }

    /**
     * Invoke "getColor" method from internal object
     */
    public Color getColor() {
        try {
            return (Color) mGetColorMethod.invoke(mChooser, new Object[]{});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Invoke "getSelectionModel" method from internal object
     */
    public ColorSelectionModel getSelectionModel() {
        try {
            return (ColorSelectionModel) mGetSelectionModelMethod.invoke(mChooser, new Object[]{});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return internal wrapped object
     */
    public Object getWrappedObj() {
        return mChooser;
    }

    /**
     * Invoke "setColor" method from internal object
     */
    public void setColor(Color aColor) {
        try {
            mSetColorMethod.invoke(mChooser, new Object[]{aColor});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoke "setPreviewPanel" method from internal object
     */
    public void setPreviewPanel(JComponent aPreview) {
        try {
            mSetPreviewPanelMethod.invoke(mChooser, new Object[]{aPreview});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}