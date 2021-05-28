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
package fxts.stations.trader.ui.dialogs;

import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.UIManager;
import fxts.stations.ui.WeakActionPropertyChangeListener;
import fxts.stations.ui.WeakPropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;

/**
 * ComboBox that sensitive to pressing of escape button.
 */
public class KeySensitiveComboBox extends JComboBox {

    public void addPropertyChangeListener(PropertyChangeListener aListener) {
        super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
    }

    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
        return new WeakActionPropertyChangeListener(this, a);
    }

    protected void paintComponent(Graphics aGraphics) {
        if (UIManager.getInst().isAAEnabled()) {
            Graphics2D g2d = (Graphics2D) aGraphics;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(aGraphics);
    }

    /**
     * Sets dialog that is owner of this combobox.
     * This dialog will be closed by escape button pressing.
     *
     * @param aDialog owner dialog
     */
    public void setDialog(ABaseDialog aDialog) {
        final ABaseDialog dlg = aDialog;
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                       "Exit");
        getActionMap().put("Exit", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                //System.out.println("Go out from sensitive combo!");
                dlg.closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
    }
}
