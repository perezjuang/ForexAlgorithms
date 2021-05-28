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
 *
 * $History: $
 * 10/3/2003 Created by USHIK
 * 12/13/2004   Andre Mermegas  added a handle to the Logger.
 * 05/18/2006   Andre Mermegas: added ability to snap mouse to default button
 */
package fxts.stations.trader.ui;

import fxts.stations.core.TradeDesk;
import fxts.stations.util.UserPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Robot;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * An interface class for closing and disbling dialog by
 * other components.<br>
 * Creation date (10/3/2003 8:45 AM)
 */
public abstract class ABaseDialog extends JDialog implements ComponentListener {
    protected static final Log LOG = LogFactory.getLog(ABaseDialog.class);
    private boolean mEnabled = true;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    protected ABaseDialog(Frame aOwner) {
        super(aOwner);
        addComponentListener(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Forces closing dialog.
     * That is a mandatory executing command.
     * No verifies of user input are allowed here
     * <b>Important note</b>: The base class deletes dialog.
     * Sub class should close it, and <b>must</b> call this
     * method of superclass
     */
    public void closeDialog(int aExitCode) {
        setVisible(false);
        removeComponentListener(this);
        dispose();
    }

    public void componentHidden(ComponentEvent aEvent) {
    }

    public void componentMoved(ComponentEvent aEvent) {
    }

    public void componentResized(ComponentEvent aEvent) {
    }

    public void componentShown(ComponentEvent aEvent) {
        try {
            UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            JButton button = getRootPane().getDefaultButton();
            if (preferences.getBoolean("Mouse.snapDefaultButton") && button != null) {
                // snap mouse to middle of default button
                Robot robot = new Robot();
                int x = (int) button.getLocationOnScreen().getX();
                int y = (int) button.getLocationOnScreen().getY();
                int width = (int) button.getSize().getWidth();
                int height = (int) button.getSize().getHeight();
                int x1 = x + width / 2;
                int y1 = y + height / 2;
                robot.mouseMove(x1, y1);
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the dialog enable or disable.
     * The ABaseDialog sets mbEnabled flag only.
     * the implementation should refine this method
     * and call enableDialog of superclass for correct return value of
     * isDialogEnabled method
     */
    public void enableDialog(boolean aEnabled) {
        mEnabled = aEnabled;
    }

    /**
     * Retuns enabling status
     */
    public boolean isDialogEnabled() {
        return mEnabled;
    }

    /**
     * shows dialog as modal
     */
    public abstract int showModal();
}
