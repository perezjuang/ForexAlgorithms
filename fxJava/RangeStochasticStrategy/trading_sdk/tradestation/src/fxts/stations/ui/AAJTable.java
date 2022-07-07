/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/ui/AAJTable.java#1 $
 *
 * Copyright (c) 2008 FXCM, LLC.
 * 32 Old Slip, New York NY, 10005 USA
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
 *
 * Author: Andre Mermegas
 * Created: Apr 10, 2008 4:51:22 PM
 *
 * $History: $
 */
package fxts.stations.ui;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 */
public class AAJTable extends JTable {
    public AAJTable(TableModel aTableModel) {
        super(aTableModel);
    }

    @Override
    protected void paintComponent(Graphics aGraphics) {
        if (UIManager.getInst().isAAEnabled()) {
            Graphics2D g2d = (Graphics2D) aGraphics;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(aGraphics);
    }
}
