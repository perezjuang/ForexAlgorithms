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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.Vector;

/**
 * Class of status bar component.
 * This class provides more easy interface for creating and
 * filling by elements of status bar then standart layout managers.
 */
public class StatusBar extends JComponent {
    private static final int INSERTS_VALUE = 2;
    //Vector of panes in the same order as they are appeared in status bar.
    private Vector mPanes = new Vector();

    /**
     * Constructor
     */
    public StatusBar() {
        setLayout(UIFrontEnd.getInstance().getSideLayout());
    }

    /**
     * Adds JLabel with the specified text as the pane to the status bar.
     * Creates non-resizable pane which size is equal to the size defined by a layout manager.
     *
     * @param aLabel   component to adding
     * @param aWeight accords to weight parameter in SideLayout
     *
     * @return index of the pane (0-based)
     */
    public int addLabel(String aLabel, double aWeight) {
        JPanel jp = new JPanel(new BorderLayout());
        jp.add(UIManager.getInst().createLabel(aLabel), BorderLayout.WEST);

        add(jp, createConstraints(mPanes.size(), 0, GridBagConstraints.BOTH, INSERTS_VALUE, aWeight));
        mPanes.add(jp);
        return mPanes.size() - 1;
    }

    /**
     * Adds JLabel with the specified text and preferred width as the pane to the status bar.
     * Creates non-resizable pane which size is equal to the size defined by a layout manager.
     *
     * @param aLabel   component to adding
     * @param aWidth  preferred width of component
     * @param aBorder border object
     *
     * @return index of the pane (0-based)
     */
    public int addLabel(String aLabel, int aWidth, Border aBorder) {
        JPanel jp = new JPanel(new BorderLayout());
        jp.setPreferredSize(new Dimension(aWidth, 0));
        jp.setMinimumSize(new Dimension(aWidth, 0));
        jp.add(UIManager.getInst().createLabel(aLabel), BorderLayout.WEST);
        jp.setBorder(aBorder);

        add(jp, createConstraints(mPanes.size(), 0, GridBagConstraints.BOTH, INSERTS_VALUE));
        mPanes.add(jp);
        return mPanes.size() - 1;
    }

    /**
     * Adds arbitrary JComponent as the pane to the status bar.
     * Creates non-resizable pane which size is equal to the size defined by a layout manager.
     *
     * @param aPane component to adding
     *
     * @return index of the pane (0-based)
     */
    public int addPane(JComponent aPane) {
        add(aPane, createConstraints(mPanes.size(), 0, GridBagConstraints.BOTH, INSERTS_VALUE));
        mPanes.add(aPane);
        return mPanes.size() - 1;
    }

    /**
     * Adds arbitrary JComponent as the pane to the status bar.
     * Additional parameter defines, how much of extra space is given
     * to the component during resize.
     *
     * @param aPane  component to adding
     * @param aWidth preferred width of component
     *
     * @return index of the pane (0-based)
     */
    public int addPane(JComponent aPane, int aWidth) {
        aPane.setPreferredSize(new Dimension(aWidth, 0));
        aPane.setMinimumSize(new Dimension(aWidth, 0));
        add(aPane, createConstraints(mPanes.size(), 0, GridBagConstraints.BOTH, INSERTS_VALUE));
        mPanes.add(aPane);
        return mPanes.size() - 1;
    }

    /**
     * Creates and sets the SideConstraints object
     *
     * @param aGridx   corresponds to gridx's value of SideConstraints
     * @param aGridy   corresponds to gridy's value of SideConstraints
     * @param aFill    corresponds to fill's value of SideConstraints
     * @param aInserts corresponds to insets's values of SideConstraints
     *
     * @return created SideConstraints object
     */
    private GridBagConstraints createConstraints(int aGridx, int aGridy, int aFill, int aInserts) {
        GridBagConstraints aContr = UIFrontEnd.getInstance().getSideConstraints();
        aContr.gridx = aGridx;
        aContr.gridy = aGridy;
        aContr.fill = aFill;
        aContr.insets.top = aInserts;
        aContr.insets.bottom = aInserts;
        aContr.insets.left = aInserts;
        aContr.insets.right = aInserts;
        return aContr;
    }

    /**
     * Creates and sets the SideConstraints object
     *
     * @param aGridx   corresponds to gridx's value of SideConstraints
     * @param aGridy   corresponds to gridy's value of SideConstraints
     * @param aFill    corresponds to fill's value of SideConstraints
     * @param aInserts corresponds to insets's values of SideConstraints
     * @param aWeightx corresponds to weightx's value of SideConstraints
     *
     * @return created SideConstraints object
     */
    private GridBagConstraints createConstraints(int aGridx, int aGridy, int aFill, int aInserts, double aWeightx) {
        GridBagConstraints aContr = UIFrontEnd.getInstance().getSideConstraints();
        aContr.gridx = aGridx;
        aContr.gridy = aGridy;
        aContr.fill = aFill;
        aContr.insets.top = aInserts;
        aContr.insets.bottom = aInserts;
        aContr.insets.left = aInserts;
        aContr.insets.right = aInserts;
        aContr.weightx = aWeightx;
        return aContr;
    }

    /**
     * Finds the specified component as the pane
     *
     * @param aPane component
     *
     * @return components's index or -1 if not found.
     */
    public int findPane(JComponent aPane) {
        for (int i = 0; i < 0; i++) {
            if (mPanes.get(i) == aPane) {
                return i;
            }
        }
        //if not found
        return -1;
    }

    /**
     * Returns pane's component at the specified index.
     *
     * @param aPaneIdx index at status bat
     *
     * @return pane's component
     */
    public JComponent getPane(int aPaneIdx) {
        return (JComponent) mPanes.get(aPaneIdx);
    }

    /**
     * Get count of panes.
     *
     * @return count of panes
     */
    public int getPaneCount() {
        return mPanes.size();
    }

    /**
     * Gets text of JLabel-based component
     *
     * @param aPaneIdx index of component
     *
     * @return text, if the specified pane is JLabel-based, and null - else.
     */
    public String getText(int aPaneIdx) {
        JLabel jl;
        if (mPanes.get(aPaneIdx) instanceof JPanel) {
            //gets pane with specified id
            JPanel jp = (JPanel) mPanes.get(aPaneIdx);
            //gets array of components
            Component[] comps = jp.getComponents();
            if (comps != null) {
                if (comps[0] != null) {
                    if (comps[0] instanceof JLabel) {
                        jl = (JLabel) comps[0];
                        return jl.getText();
                    }
                }
            }
        } else if (mPanes.get(aPaneIdx) instanceof JLabel) {
            jl = (JLabel) mPanes.get(aPaneIdx);
            return jl.getText();
        }
        return null;
    }

    /**
     * Sets text of JLabel-based component
     *
     * @param aPaneIdx index of component
     * @param asText   new text
     *
     * @return true, if the specified pane is JLabel-based, and false - else.
     */
    public boolean setText(int aPaneIdx, String asText) {
        Component[] comps;
        if (mPanes.get(aPaneIdx) instanceof JPanel) {
            //gets pane with specified id
            JPanel jp = (JPanel) mPanes.get(aPaneIdx);
            //gets array of components
            comps = jp.getComponents();
            if (comps != null) {
                if (comps[0] != null) {
                    if (comps[0] instanceof JLabel) {
                        JLabel jl = (JLabel) comps[0];
                        jl.setText(asText);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}