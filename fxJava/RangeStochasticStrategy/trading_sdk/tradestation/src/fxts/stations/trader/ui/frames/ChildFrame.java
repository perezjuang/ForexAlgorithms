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
package fxts.stations.trader.ui.frames;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.util.UserPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Base class for all child frames in trader application.
 */
public class ChildFrame extends JInternalFrame {
    protected final Log mLogger = LogFactory.getLog(ChildFrame.class);

    /**
     * reference to MainFrame
     */
    private IMainFrame mMainFrame;

    /**
     * Constructor.
     *
     * @param aName      name of frame
     * @param aMainFrame main frame
     */
    public ChildFrame(String aName, IMainFrame aMainFrame) {
        super(aName,
              true,  //resizable
              true,  //closable
              true); //maximizable
        setMainFrame(aMainFrame);

        //sets reaction on closings
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        //adds listener
        addInternalFrameListener(new InternalFrameAdapter() {
            //overriden
            public void internalFrameClosing(InternalFrameEvent aEvent) {
                //unselects associated menu item
                setMenuItemState(false);
                ChildFrame.this.setVisible(false);
                //checking for existing of visible frames
                mMainFrame.checkForVisibleChilds();
            }
        });

        //sets name of the frame
        setName(aName);

        //adds mouse listener
        addMouseListener(new MouseAdapter() {
            /**
             * Invoked when a mouse button has been pressed on a component.
             */
            public void mousePressed(MouseEvent aEvent) {
                if (!ChildFrame.this.isSelected()) {
                    try {
                        ChildFrame.this.setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Returns reference to mainframe
     */
    public IMainFrame getMainFrame() {
        return mMainFrame;
    }

    /**
     * Sets reference to MainFrame
     */
    public void setMainFrame(IMainFrame aMainFrame) {
        mMainFrame = aMainFrame;
    }

    /**
     * Loads settings from the persistent preferences.
     */
    public void loadSettings() {
        UserPreferences preferences;
        try {
            preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //loads settings from preferences
        int x = preferences.getInt("childframe." + getName() + ".x");
        int y = preferences.getInt("childframe." + getName() + ".y");
        int width = preferences.getInt("childframe." + getName() + ".width");
        int height = preferences.getInt("childframe." + getName() + ".height");
        boolean visible = preferences.getBoolean("childframe." + getName() + ".visible");

        //sets position of the frame
        setBounds(new Rectangle(x, y, width, height));

        //sets visibility of the frame
        setVisible(visible);

        //sets state of the corresponding menu item
        setMenuItemState(visible);
    }

    /* -- Public methods -- */

    /**
     * Saves settings to the persistent preferences.
     */
    public void saveSettings() {
        UserPreferences preferences;

        //Get PersistenceStorage
        try {
            preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //sets the settings to preferences
        preferences.set("childframe." + getName() + ".x", getX());
        preferences.set("childframe." + getName() + ".y", getY());
        preferences.set("childframe." + getName() + ".width", getWidth());
        preferences.set("childframe." + getName() + ".height", getHeight());
        preferences.set("childframe." + getName() + ".visible", isVisible());
    }

    /**
     * Set state of the corresponding menu item.
     *
     * @param aVisible state of the visibility
     */
    public void setMenuItemState(boolean aVisible) {
        //finds menu by name
        JMenu menu = mMainFrame.findMenu("Window");

        //if third menu not initialised
        if (menu == null) {
            return;
        }

        //finds menu item by name
        JMenuItem item = mMainFrame.findMenuItem(getName(), menu);

        //if item not initialised
        if (item == null) {
            return;
        }

        //set item state
        item.setSelected(aVisible);
    }
}