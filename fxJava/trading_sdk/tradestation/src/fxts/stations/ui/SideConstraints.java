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

import fxts.stations.ui.ResizeParameter;

import java.awt.*;

/**
 * The class that specifies constraints
 * for components that are laid out using the
 * SideLayout class.
 */
public class SideConstraints extends GridBagConstraints {
    /**
     * Place the component centered along the edge of its display area
     * associated with the start of a page for the current
     * <code>ComponentOrienation</code>.  Equal to NORTH for horizontal
     * orientations.
     */
    public static final int PAGE_START = 19;
    /**
     * Place the component centered along the edge of its display area
     * associated with the end of a page for the current
     * <code>ComponentOrienation</code>.  Equal to SOUTH for horizontal
     * orientations.
     */
    public static final int PAGE_END = 20;
    /**
     * Place the component centered along the edge of its display area where
     * lines of text would normally begin for the current
     * <code>ComponentOrienation</code>.  Equal to WEST for horizontal,
     * left-to-right orientations and EAST for horizontal, right-to-left
     * orientations.
     */
    public static final int LINE_START = 21;
    /**
     * Place the component centered along the edge of its display area where
     * lines of text would normally end for the current
     * <code>ComponentOrienation</code>.  Equal to EAST for horizontal,
     * left-to-right orientations and WEST for horizontal, right-to-left
     * orientations.
     */
    public static final int LINE_END = 22;
    /**
     * Place the component in the corner of its display area where
     * the first line of text on a page would normally begin for the current
     * <code>ComponentOrienation</code>.  Equal to NORTHWEST for horizontal,
     * left-to-right orientations and NORTHEAST for horizontal, right-to-left
     * orientations.
     */
    public static final int FIRST_LINE_START = 23;
    /**
     * Place the component in the corner of its display area where
     * the first line of text on a page would normally end for the current
     * <code>ComponentOrienation</code>.  Equal to NORTHEAST for horizontal,
     * left-to-right orientations and NORTHWEST for horizontal, right-to-left
     * orientations.
     */
    public static final int FIRST_LINE_END = 24;
    /**
     * Place the component in the corner of its display area where
     * the last line of text on a page would normally start for the current
     * <code>ComponentOrienation</code>.  Equal to SOUTHWEST for horizontal,
     * left-to-right orientations and SOUTHEAST for horizontal, right-to-left
     * orientations.
     */
    public static final int LAST_LINE_START = 25;
    /**
     * Place the component in the corner of its display area where
     * the last line of text on a page would normally end for the current
     * <code>ComponentOrienation</code>.  Equal to SOUTHEAST for horizontal,
     * left-to-right orientations and SOUTHWEST for horizontal, right-to-left
     * orientations.
     */
    public static final int LAST_LINE_END = 26;
    int minHeight;
    int minWidth;
    /**
     * Parameter that sets coefficients for positioning of component`s
     * sides after resing of parents.
     */
    public ResizeParameter resize;
    int tempHeight;
    int tempWidth;
    int tempX;
    int tempY;

    /**
     * Creates a <code>GridBagConstraint</code> object with
     * all of its fields set to their default value.
     */
    public SideConstraints() {
        resize = new ResizeParameter();
        resize.init(0.0, 0.0, 0.0, 0.0);
    }

    /**
     * Creates a <code>SideConstraints</code> object with
     * all of its fields set to the passed-in arguments.
     * Note: Because the use of this constructor hinders readability
     * of source code, this constructor should only be used by
     * automatic source code generation tools.
     *
     * @param gridx      The initial gridx value.
     * @param gridy      The initial gridy value.
     * @param gridwidth  The initial gridwidth value.
     * @param gridheight The initial gridheight value.
     * @param weightx    The initial weightx value.
     * @param weighty    The initial weighty value.
     * @param anchor     The initial anchor value.
     * @param fill       The initial fill value.
     * @param insets     The initial insets value.
     * @param ipadx      The initial ipadx value.
     * @param ipady      The initial ipady value.
     */
    public SideConstraints(int gridx, int gridy,
                           int gridwidth, int gridheight,
                           double weightx, double weighty,
                           int anchor, int fill,
                           Insets insets, int ipadx, int ipady,
                           ResizeParameter aParam) {
        super(gridx, gridy, gridwidth, gridheight,
              weightx, weighty, anchor, fill,
              insets, ipadx, ipady);
        resize = aParam;
    }

    /**
     * Creates a copy of this side constraint.
     * @return a copy of this side constraint
     */
    //public Object clone () {
    //    try {
    //        SideConstraints c = (SideConstraints)super.clone();
    //        c.resize = (ResizeParameter)resize.clone();
    //        return c;
    //    } catch (CloneNotSupportedException e) {
    //        // this shouldn't happen, since we are Cloneable
    //        throw new InternalError();
    //    }
    //}
}