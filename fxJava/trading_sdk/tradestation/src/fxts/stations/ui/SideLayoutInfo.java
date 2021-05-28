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

import fxts.stations.ui.SideLayout;

import java.io.Serializable;

/**
 * Containts information about layout parameters.
 */
class SideLayoutInfo implements Serializable {
    int[] minHeight;        /* largest minHeight in each row */
    int[] minWidth;         /* largest minWidth in each column */
    int startx, starty;     /* starting point for layout */
    double[] weightX;       /* largest weight in each column */
    double[] weightY;       /* largest weight in each row */
    int width, height;      /* number of cells horizontally, vertically */

    SideLayoutInfo() {
        minWidth = new int[SideLayout.MAXGRIDSIZE];
        minHeight = new int[SideLayout.MAXGRIDSIZE];
        weightX = new double[SideLayout.MAXGRIDSIZE];
        weightY = new double[SideLayout.MAXGRIDSIZE];
    }
}