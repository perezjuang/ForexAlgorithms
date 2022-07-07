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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Serializable;

/**
 * The color swatch chooser.
 */
class SwatchChooserPanel extends AColorChooserPanel {
    private static String cRecentStr = UIManager.getString("ColorChooser.swatchesRecentText");

    private class MainSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent aEvent) {
            Color color = mSwatchPanel.getColorForLocation(aEvent.getX(), aEvent.getY());
            getColorSelectionModel().setSelectedColor(color);
            mRecentSwatchPanel.setMostRecentColor(color);
        }
    }

    private class RecentSwatchListener extends MouseAdapter implements Serializable {
        public void mousePressed(MouseEvent aEvent) {
            Color color = mRecentSwatchPanel.getColorForLocation(aEvent.getX(), aEvent.getY());
            getColorSelectionModel().setSelectedColor(color);
        }
    }

    private MouseListener mMainSwatchListener;
    private MouseListener mRecentSwatchListener;
    private RecentSwatchPanel mRecentSwatchPanel;
    private SwatchPanel mSwatchPanel;

    public SwatchChooserPanel() {
    }

    protected void buildChooser() {
        //sets panel
        setLayout(UIFrontEnd.getInstance().getSideLayout());

        //JPanel superHolder = new JPanel(new BorderLayout());
        JPanel superHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        mSwatchPanel = new MainSwatchPanel();
        mSwatchPanel.getAccessibleContext().setAccessibleName(getDisplayName());
        mRecentSwatchPanel = new RecentSwatchPanel();
        mRecentSwatchPanel.getAccessibleContext().setAccessibleName(cRecentStr);

        //adds listeners
        mMainSwatchListener = new MainSwatchListener();
        mSwatchPanel.addMouseListener(mMainSwatchListener);
        mRecentSwatchListener = new RecentSwatchListener();
        mRecentSwatchPanel.addMouseListener(mRecentSwatchListener);

        //sets main holder
        JPanel mainHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        Border border = new CompoundBorder(new LineBorder(Color.BLACK),
                                           new LineBorder(Color.WHITE));
        mainHolder.setBorder(border);
        SideConstraints sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        mainHolder.add(mSwatchPanel, sideConstraints);

        //adds main holder
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        superHolder.add(mainHolder, sideConstraints);

        //sets recent holder
        JPanel recentHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        mRecentSwatchPanel.addMouseListener(mRecentSwatchListener);
        recentHolder.setBorder(border);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        recentHolder.add(mRecentSwatchPanel, sideConstraints);

        //JPanel recentLabelHolder = new JPanel(new BorderLayout());
        JPanel recentLabelHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        //recentLabelHolder.add(recentHolder, BorderLayout.CENTER);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        sideConstraints.fill = SideConstraints.BOTH;
        recentLabelHolder.add(recentHolder, sideConstraints);
        JLabel l = fxts.stations.ui.UIManager.getInst().createLabel(cRecentStr);
        l.setLabelFor(mRecentSwatchPanel);
        //recentLabelHolder.add(l, BorderLayout.NORTH);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        recentLabelHolder.add(l, sideConstraints);
        //!!
        //recentLabelHolder.setBackground(new Color(50, 24, 245));

        //JPanel recentHolderHolder = new JPanel(new CenterLayout());
        JPanel recentHolderHolder = new JPanel(UIFrontEnd.getInstance().getSideLayout());
        if (getComponentOrientation().isLeftToRight()) {
            recentHolderHolder.setBorder(new EmptyBorder(2, 10, 2, 2));
        } else {
            recentHolderHolder.setBorder(new EmptyBorder(2, 2, 2, 10));
        }
        //recentHolderHolder.add(recentLabelHolder);
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        recentHolderHolder.add(recentLabelHolder, sideConstraints);
        //!!
        //recentHolderHolder.setBackground(new Color(50, 24, 245));

        //adds recent holder
        //superHolder.add( recentHolderHolder, BorderLayout.AFTER_LINE_ENDS );
        sideConstraints = new SideConstraints();
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 0;
        sideConstraints.fill = SideConstraints.BOTH;
        superHolder.add(recentHolderHolder, sideConstraints);
        //!!
        //superHolder.setBackground(new Color(50, 24, 245));

        //adds super holder
//        add(superHolder);
        sideConstraints = new SideConstraints();
        sideConstraints.fill = SideConstraints.BOTH;
        ResizeParameter resizeParameter = new ResizeParameter();
        resizeParameter.init(0.5, 0.0, 0.5, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        //sideConstraints.resize = new ResizeParameter(0.5, 0.0, 0.5, 1.0);
        sideConstraints.insets = new Insets(30, 10, 0, 10);
        add(superHolder, sideConstraints);
        //!!
        //setBackground(new Color(50, 24, 245));
    }

    public String getDisplayName() {
        return UIManager.getString("ColorChooser.swatchesNameText");
    }

    /**
     * The background color, foreground color, and font are already set to the
     * defaults from the defaults table before this method is called.
     */
    public void installChooserPanel(ColorChooser aEnclosingChooser) {
        super.installChooserPanel(aEnclosingChooser);
    }

    public void uninstallChooserPanel(ColorChooser aEnclosingChooser) {
        super.uninstallChooserPanel(aEnclosingChooser);
        mSwatchPanel.removeMouseListener(mMainSwatchListener);
        mRecentSwatchPanel.removeMouseListener(mRecentSwatchListener);
        mSwatchPanel = null;
        mRecentSwatchPanel = null;
        mMainSwatchListener = null;
        mRecentSwatchListener = null;
        removeAll();  // strip out all the sub-components
    }

    public void updateChooser() {
    }
}

class SwatchPanel extends JPanel {
    protected Color[] colors;
    protected Dimension gap;
    protected Dimension numSwatches;
    protected Dimension swatchSize;

    public SwatchPanel() {
        initValues();
        initColors();
        setToolTipText(""); // register for events
        setOpaque(true);
        setBackground(Color.WHITE);
        setRequestFocusEnabled(false);
    }

    private Color getColorForCell(int aColumn, int aRow) {
        return colors[(aRow * numSwatches.width + aColumn)]; // (STEVE) - change data orientation here
    }

    public Color getColorForLocation(int aX, int aY) {
        int column;
        if (!getComponentOrientation().isLeftToRight() && this instanceof RecentSwatchPanel) {
            column = numSwatches.width - aX / (swatchSize.width + gap.width) - 1;
        } else {
            column = aX / (swatchSize.width + gap.width);
        }
        int row = aY / (swatchSize.height + gap.height);
        return getColorForCell(column, row);
    }

    public Dimension getPreferredSize() {
        int x = numSwatches.width * (swatchSize.width + gap.width) - 1;
        int y = numSwatches.height * (swatchSize.height + gap.height) - 1;
        return new Dimension(x, y);
    }

    public String getToolTipText(MouseEvent aEvent) {
        Color color = getColorForLocation(aEvent.getX(), aEvent.getY());
        return color.getRed() + ", " + color.getGreen() + ", " + color.getBlue();
    }

    protected void initColors() {
    }

    protected void initValues() {
    }

    public boolean isFocusTraversable() {
        return false;
    }

    public void paintComponent(Graphics aGraphics) {
        aGraphics.setColor(getBackground());
        aGraphics.fillRect(0, 0, getWidth(), getHeight());
        for (int row = 0; row < numSwatches.height; row++) {
            for (int column = 0; column < numSwatches.width; column++) {
                aGraphics.setColor(getColorForCell(column, row));
                int x;
                if (!getComponentOrientation().isLeftToRight() && this instanceof RecentSwatchPanel) {
                    x = (numSwatches.width - column - 1) * (swatchSize.width + gap.width);
                } else {
                    x = column * (swatchSize.width + gap.width);
                }
                int y = row * (swatchSize.height + gap.height);
                aGraphics.fillRect(x, y, swatchSize.width, swatchSize.height);
                aGraphics.setColor(Color.BLACK);
                aGraphics.drawLine(x + swatchSize.width - 1, y, x + swatchSize.width - 1, y + swatchSize.height - 1);
                aGraphics.drawLine(x, y + swatchSize.height - 1, x + swatchSize.width - 1, y + swatchSize.height - 1);
            }
        }
    }
}

class RecentSwatchPanel extends SwatchPanel {
    protected void initColors() {
        Color defaultRecentColor = UIManager.getColor("ColorChooser.swatchesDefaultRecentColor");
        int numColors = numSwatches.width * numSwatches.height;
        colors = new Color[numColors];
        for (int i = 0; i < numColors; i++) {
            colors[i] = defaultRecentColor;
        }
    }

    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesRecentSwatchSize");
        numSwatches = new Dimension(5, 7);
        gap = new Dimension(1, 1);
    }

    public void setMostRecentColor(Color aColor) {
        System.arraycopy(colors, 0, colors, 1, colors.length - 1);
        colors[0] = aColor;
        repaint();
    }
}

class MainSwatchPanel extends SwatchPanel {
    protected void initColors() {
        int[] rawValues = initRawValues();
        int numColors = rawValues.length / 3;
        colors = new Color[numColors];
        for (int i = 0; i < numColors; i++) {
            colors[i] = new Color(rawValues[(i * 3)], rawValues[(i * 3 + 1)], rawValues[(i * 3 + 2)]);
        }
    }

    private int[] initRawValues() {
        int[] rawValues = {
                255, 255, 255, // first row.
                204, 255, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                204, 204, 255,
                255, 204, 255,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 204, 204,
                255, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 255, 204,
                204, 204, 204,  // second row.
                153, 255, 255,
                153, 204, 255,
                153, 153, 255,
                153, 153, 255,
                153, 153, 255,
                153, 153, 255,
                153, 153, 255,
                153, 153, 255,
                153, 153, 255,
                204, 153, 255,
                255, 153, 255,
                255, 153, 204,
                255, 153, 153,
                255, 153, 153,
                255, 153, 153,
                255, 153, 153,
                255, 153, 153,
                255, 153, 153,
                255, 153, 153,
                255, 204, 153,
                255, 255, 153,
                204, 255, 153,
                153, 255, 153,
                153, 255, 153,
                153, 255, 153,
                153, 255, 153,
                153, 255, 153,
                153, 255, 153,
                153, 255, 153,
                153, 255, 204,
                204, 204, 204,  // third row
                102, 255, 255,
                102, 204, 255,
                102, 153, 255,
                102, 102, 255,
                102, 102, 255,
                102, 102, 255,
                102, 102, 255,
                102, 102, 255,
                153, 102, 255,
                204, 102, 255,
                255, 102, 255,
                255, 102, 204,
                255, 102, 153,
                255, 102, 102,
                255, 102, 102,
                255, 102, 102,
                255, 102, 102,
                255, 102, 102,
                255, 153, 102,
                255, 204, 102,
                255, 255, 102,
                204, 255, 102,
                153, 255, 102,
                102, 255, 102,
                102, 255, 102,
                102, 255, 102,
                102, 255, 102,
                102, 255, 102,
                102, 255, 153,
                102, 255, 204,
                153, 153, 153, // fourth row
                51, 255, 255,
                51, 204, 255,
                51, 153, 255,
                51, 102, 255,
                51, 51, 255,
                51, 51, 255,
                51, 51, 255,
                102, 51, 255,
                153, 51, 255,
                204, 51, 255,
                255, 51, 255,
                255, 51, 204,
                255, 51, 153,
                255, 51, 102,
                255, 51, 51,
                255, 51, 51,
                255, 51, 51,
                255, 102, 51,
                255, 153, 51,
                255, 204, 51,
                255, 255, 51,
                204, 255, 51,
                153, 244, 51,
                102, 255, 51,
                51, 255, 51,
                51, 255, 51,
                51, 255, 51,
                51, 255, 102,
                51, 255, 153,
                51, 255, 204,
                153, 153, 153, // Fifth row
                0, 255, 255,
                0, 204, 255,
                0, 153, 255,
                0, 102, 255,
                0, 51, 255,
                0, 0, 255,
                51, 0, 255,
                102, 0, 255,
                153, 0, 255,
                204, 0, 255,
                255, 0, 255,
                255, 0, 204,
                255, 0, 153,
                255, 0, 102,
                255, 0, 51,
                255, 0, 0,
                255, 51, 0,
                255, 102, 0,
                255, 153, 0,
                255, 204, 0,
                255, 255, 0,
                204, 255, 0,
                153, 255, 0,
                102, 255, 0,
                51, 255, 0,
                0, 255, 0,
                0, 255, 51,
                0, 255, 102,
                0, 255, 153,
                0, 255, 204,
                102, 102, 102, // sixth row
                0, 204, 204,
                0, 204, 204,
                0, 153, 204,
                0, 102, 204,
                0, 51, 204,
                0, 0, 204,
                51, 0, 204,
                102, 0, 204,
                153, 0, 204,
                204, 0, 204,
                204, 0, 204,
                204, 0, 204,
                204, 0, 153,
                204, 0, 102,
                204, 0, 51,
                204, 0, 0,
                204, 51, 0,
                204, 102, 0,
                204, 153, 0,
                204, 204, 0,
                204, 204, 0,
                204, 204, 0,
                153, 204, 0,
                102, 204, 0,
                51, 204, 0,
                0, 204, 0,
                0, 204, 51,
                0, 204, 102,
                0, 204, 153,
                0, 204, 204,
                102, 102, 102, // seventh row
                0, 153, 153,
                0, 153, 153,
                0, 153, 153,
                0, 102, 153,
                0, 51, 153,
                0, 0, 153,
                51, 0, 153,
                102, 0, 153,
                153, 0, 153,
                153, 0, 153,
                153, 0, 153,
                153, 0, 153,
                153, 0, 153,
                153, 0, 102,
                153, 0, 51,
                153, 0, 0,
                153, 51, 0,
                153, 102, 0,
                153, 153, 0,
                153, 153, 0,
                153, 153, 0,
                153, 153, 0,
                153, 153, 0,
                102, 153, 0,
                51, 153, 0,
                0, 153, 0,
                0, 153, 51,
                0, 153, 102,
                0, 153, 153,
                0, 153, 153,
                51, 51, 51, // eigth row
                0, 102, 102,
                0, 102, 102,
                0, 102, 102,
                0, 102, 102,
                0, 51, 102,
                0, 0, 102,
                51, 0, 102,
                102, 0, 102,
                102, 0, 102,
                102, 0, 102,
                102, 0, 102,
                102, 0, 102,
                102, 0, 102,
                102, 0, 102,
                102, 0, 51,
                102, 0, 0,
                102, 51, 0,
                102, 102, 0,
                102, 102, 0,
                102, 102, 0,
                102, 102, 0,
                102, 102, 0,
                102, 102, 0,
                102, 102, 0,
                51, 102, 0,
                0, 102, 0,
                0, 102, 51,
                0, 102, 102,
                0, 102, 102,
                0, 102, 102,
                0, 0, 0, // ninth row
                0, 51, 51,
                0, 51, 51,
                0, 51, 51,
                0, 51, 51,
                0, 51, 51,
                0, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 51,
                51, 0, 0,
                51, 51, 0,
                51, 51, 0,
                51, 51, 0,
                51, 51, 0,
                51, 51, 0,
                51, 51, 0,
                51, 51, 0,
                51, 51, 0,
                0, 51, 0,
                0, 51, 51,
                0, 51, 51,
                0, 51, 51,
                0, 51, 51,
                51, 51, 51};
        return rawValues;
    }

    protected void initValues() {
        swatchSize = UIManager.getDimension("ColorChooser.swatchesSwatchSize");
        numSwatches = new Dimension(31, 9);
        gap = new Dimension(1, 1);
    }
}