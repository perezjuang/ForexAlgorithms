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

import fxts.stations.ui.colorchooser.ColorChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JColorChooser;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

/**
 * User interface front end.
 * Class provide method to create objects from
 * com.fxcm.ui package or default objects
 * if no package is present.
 */
public class UIFrontEnd {
    private static UIFrontEnd cUIFrontEnd = new UIFrontEnd();
    private final Log mLogger = LogFactory.getLog(UIFrontEnd.class);

    private UIFrontEnd() {
    }

    /**
     * Creating wrapper around com.fxcm.ui.colochooser.ColorChooser object or
     * wrapper around javax.swing.JColorChooser object
     *
     * @return ColorChooserWrapper
     */
    public ColorChooserWrapper getColorChooser() {
        try {
            return new ColorChooserWrapper(new ColorChooser());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            return new ColorChooserWrapper(new JColorChooser());
        } catch (Exception e) {
            mLogger.error("!!!! Standard class javax.swing.JColorChooser loading error");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return single object instance
     *
     * @return UIFrontEnd
     */
    public static UIFrontEnd getInstance() {
        return cUIFrontEnd;
    }

    /**
     * Creating wrapper around fxts.stations.ui.ResizeParameter object or
     * wrapper around dumm object
     *
     * @return ResizeParameterWrapper
     */
    public ResizeParameterWrapper getResizeParameter() {
        try {
            return new ResizeParameterWrapper(new ResizeParameter());
        } catch (Exception e) {
            mLogger.error("Load ResizeParameter error: " + e + " load default");
            return new ResizeParameterWrapper();
        }
    }

    /**
     * Creating new fxts.stations.ui.SideConstraints object or java.awt.GridBagConstraints by default.
     *
     * @return GridBagConstraints
     */
    public GridBagConstraints getSideConstraints() {
        try {
            return new SideConstraints();
        } catch (Exception e) {
            mLogger.error("Load layout error: " + e + " load default : java.awt.GridBagConstraints");
            return new GridBagConstraints();
        }
    }

    /**
     * Creating new fxts.stations.ui.SideLayout object or java.awt.GridBagLayout by default.
     *
     * @return LayoutManager
     */
    public LayoutManager getSideLayout() {
        try {
            return new SideLayout();
        } catch (Exception e) {
            mLogger.error("Load layout error: " + e + " load default : java.awt.GridBagLayout");
            return new GridBagLayout();
        }
    }
}