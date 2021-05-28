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
import java.awt.*;

/**
 * The standard preview panel for the color chooser.
 */
class PreviewPanel extends JPanel {
    private Font font = new Font("Dialog", Font.PLAIN, 12);
    private int innerGap = 5;
    private Color oldColor = null;
    private String sampleText = UIManager.getString("ColorChooser.sampleText");
    private int squareGap = 5;
    /*-- Data members --*/
    private int squareSize = 25;
    private int swatchWidth = 50;
    private int textGap = 5;

    /**
     * Returns preferred size.
     */
    public Dimension getPreferredSize() {
        //FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(getFont());
        FontMetrics fm = getGraphics().getFontMetrics(getFont());
        int ascent = fm.getAscent();
        int height = fm.getHeight();
        int width = fm.stringWidth(sampleText);
        int y = height * 3 + textGap * 3;
        int x = squareSize * 3 + squareGap * 2 + swatchWidth + width + textGap * 3;
        return new Dimension(x, y);
    }

    /**
     * Paints component.
     */
    public void paintComponent(Graphics g) {
        if (oldColor == null) {
            oldColor = getForeground();
        }
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (getComponentOrientation().isLeftToRight()) {
            int squareWidth = paintSquares(g, 0);
            int textWidth = paintText(g, squareWidth);
            paintSwatch(g, squareWidth + textWidth);
        } else {
            int swatchWidth = paintSwatch(g, 0);
            int textWidth = paintText(g, swatchWidth);
            paintSquares(g, swatchWidth + textWidth);
        }
    }

    /**
     * Paints squares.
     */
    private int paintSquares(Graphics g, int offsetX) {
        int squareXOffset = offsetX;
        Color color = getForeground();
        g.setColor(Color.white);
        g.fillRect(squareXOffset, 0, squareSize, squareSize);
        g.setColor(color);
        g.fillRect(squareXOffset + innerGap,
                   innerGap,
                   squareSize - innerGap * 2,
                   squareSize - innerGap * 2);
        g.setColor(Color.white);
        g.fillRect(squareXOffset + innerGap * 2,
                   innerGap * 2,
                   squareSize - innerGap * 4,
                   squareSize - innerGap * 4);
        g.setColor(color);
        g.fillRect(squareXOffset, squareSize + squareGap, squareSize, squareSize);
        g.translate(squareSize + squareGap, 0);
        g.setColor(Color.black);
        g.fillRect(squareXOffset, 0, squareSize, squareSize);
        g.setColor(color);
        g.fillRect(squareXOffset + innerGap,
                   innerGap,
                   squareSize - innerGap * 2,
                   squareSize - innerGap * 2);
        g.setColor(Color.white);
        g.fillRect(squareXOffset + innerGap * 2,
                   innerGap * 2,
                   squareSize - innerGap * 4,
                   squareSize - innerGap * 4);
        g.translate(-(squareSize + squareGap), 0);
        g.translate(squareSize + squareGap, squareSize + squareGap);
        g.setColor(Color.white);
        g.fillRect(squareXOffset, 0, squareSize, squareSize);
        g.setColor(color);
        g.fillRect(squareXOffset + innerGap,
                   innerGap,
                   squareSize - innerGap * 2,
                   squareSize - innerGap * 2);
        g.translate(-(squareSize + squareGap), -(squareSize + squareGap));
        g.translate((squareSize + squareGap) * 2, 0);
        g.setColor(Color.white);
        g.fillRect(squareXOffset, 0, squareSize, squareSize);
        g.setColor(color);
        g.fillRect(squareXOffset + innerGap,
                   innerGap,
                   squareSize - innerGap * 2,
                   squareSize - innerGap * 2);
        g.setColor(Color.black);
        g.fillRect(squareXOffset + innerGap * 2,
                   innerGap * 2,
                   squareSize - innerGap * 4,
                   squareSize - innerGap * 4);
        g.translate(-((squareSize + squareGap) * 2), 0);
        g.translate((squareSize + squareGap) * 2, squareSize + squareGap);
        g.setColor(Color.black);
        g.fillRect(squareXOffset, 0, squareSize, squareSize);
        g.setColor(color);
        g.fillRect(squareXOffset + innerGap,
                   innerGap,
                   squareSize - innerGap * 2,
                   squareSize - innerGap * 2);
        g.translate(-((squareSize + squareGap) * 2), -(squareSize + squareGap));
        return squareSize * 3 + squareGap * 2;
    }

    /**
     * Paints swatch.
     */
    private int paintSwatch(Graphics g, int offsetX) {
        int swatchX = offsetX;
        g.setColor(oldColor);
        g.fillRect(swatchX, 0, swatchWidth, squareSize + squareGap / 2);
        g.setColor(getForeground());
        g.fillRect(swatchX, squareSize + squareGap / 2, swatchWidth, squareSize + squareGap / 2);
        return swatchX + swatchWidth;
    }

    /**
     * Paints text.
     */
    private int paintText(Graphics g, int offsetX) {
        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int ascent = fm.getAscent();
        int height = fm.getHeight();
        int width = fm.stringWidth(sampleText);
        int textXOffset = offsetX + textGap;
        Color color = getForeground();
        g.setColor(color);
        g.drawString(sampleText, textXOffset + textGap / 2, ascent + 2);
        g.fillRect(textXOffset,
                   height + textGap,
                   width + textGap,
                   height + 2);
        g.setColor(Color.black);
        g.drawString(sampleText,
                     textXOffset + textGap / 2,
                     height + ascent + textGap + 2);
        g.setColor(Color.white);
        g.fillRect(textXOffset,
                   (height + textGap) * 2,
                   width + textGap,
                   height + 2);
        g.setColor(color);
        g.drawString(sampleText,
                     textXOffset + textGap / 2,
                     (height + textGap) * 2 + ascent + 2);
        return width + textGap * 3;
    }
}