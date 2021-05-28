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

/**
 * Parameters that used for resize parameter of SideConstraints.
 */
public class ResizeParameter {
    /**
     * Bottom side`s coefficient.
     */
    double mdBottom = 0.0;
    /*-- Data members --*/
    /**
     * Left side`s coefficient.
     */
    double mdLeft = 0.0;
    /**
     * Right side`s coefficient.
     */
    double mdRight = 0.0;
    /**
     * Top side`s coefficient.
     */
    double mdTop = 0.0;

    /*-- Constructors --*/

    /**
     * Returns bottom side resize coefficient.
     */
    public double getBottom() {
        return mdBottom;
    }

    /*-- Get/set methods */

    /**
     * Returns left side resize coefficient.
     */
    public double getLeft() {
        return mdLeft;
    }

    /**
     * Returns right side resize coefficient.
     */
    public double getRight() {
        return mdRight;
    }

    /**
     * Returns top side resize coefficient.
     */
    public double getTop() {
        return mdTop;
    }

    /**
     * Constructor.
     */
    //public ResizeParameter(double adLeft, double adTop,
    //                       double adRight, double adBottom) {
    //    mdLeft = adLeft;
    //    mdTop = adTop;
    //    mdRight = adRight;
    //    mdBottom = adBottom;
    //};
    public void init(double adLeft, double adTop,
                     double adRight, double adBottom) {
        mdLeft = adLeft;
        mdTop = adTop;
        mdRight = adRight;
        mdBottom = adBottom;
    }

    /**
     * Sets bottom side resize coefficient.
     */
    public void setBottom(double adBottom) {
        mdBottom = adBottom;
    }

    /**
     * Sets left side resize coefficient.
     */
    public void setLeft(double adLeft) {
        mdLeft = adLeft;
    }

    /**
     * Sets right side resize coefficient.
     */
    public void setRight(double adRight) {
        mdRight = adRight;
    }

    public void setToConstraints(Object aObject) {
        ((SideConstraints) aObject).resize = this;
    }

    /**
     * Sets top side resize coefficient.
     */
    public void setTop(double adTop) {
        mdTop = adTop;
    }
}