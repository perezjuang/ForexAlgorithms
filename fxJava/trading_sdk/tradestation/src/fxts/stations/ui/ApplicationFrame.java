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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * Base class for application MDI main frame.
 * It`s represents framework of the application.
 */
public class ApplicationFrame extends JFrame {
    /**
     * Manager of internal frames (MDI child windows).
     */
    private ChildManager mChildManager;
    protected final Log mLogger = LogFactory.getLog(ApplicationFrame.class);
    /**
     * Application menu.
     */
    private JMenuBar mMenuBar;
    /**
     * Application status bar.
     */
    private StatusBar mStatusBar;
    /**
     * Application toolbar.
     */
    private JToolBar mToolBar;

    /**
     * Default construction.
     */
    public ApplicationFrame() {
        this("");
    }

    /**
     * Construction.
     *
     * @param aTitle title of application
     */
    public ApplicationFrame(String aTitle) {
        super(aTitle);
    }

    /**
     * Fires creation of menu, statusbar and toolbar.
     */
    public void create() {
        //creating of the toolbar
        mToolBar = createToolBar();
        //creating of the statusbar
        mStatusBar = createStatusBar();
        //creating of the menu
        mMenuBar = createMenu();
        //setting of childmanager
        mChildManager = new ChildManager();

        //adds the menu
        if (mMenuBar != null) {
            setJMenuBar(mMenuBar);
        }
        Container cp = getContentPane();

        //adds the StatusBar
        if (mStatusBar != null) {
            mStatusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            cp.add(BorderLayout.SOUTH, mStatusBar);
        }

        //adds ToolBar
        if (mToolBar != null) {
            mToolBar.setFloatable(true);
            mToolBar.setBorder(new EtchedBorder());
            cp.add(BorderLayout.NORTH, mToolBar);
        }

        //adds childmanager
        if (mChildManager != null) {
            cp.add(BorderLayout.CENTER, mChildManager);
        }
    }

    /**
     * Creates and returns constructed menu object or null if menu is not needed.
     */
    public JMenuBar createMenu() {
        return null;
    }

    /**
     * Creates and returns constructed statusbar object or null if menu is not needed.
     */
    public StatusBar createStatusBar() {
        return null;
    }

    /**
     * Creates and returns constructed toolbar object or null if menu is not needed.
     */
    public JToolBar createToolBar() {
        return null;
    }

    /**
     * Returns child manager.
     */
    public ChildManager getChildManager() {
        return mChildManager;
    }

    /**
     * Returns menu or null if there is no it.
     */
    public JMenuBar getMenu() {
        return mMenuBar;
    }

    /**
     * Returns status bar or null if there is no it.
     */
    public StatusBar getStatusBar() {
        return mStatusBar;
    }

    /**
     * Returns toolbar or null if there is no it.
     */
    public JToolBar getToolBar() {
        return mToolBar;
    }

    /**
     * Allows to change application frame's menu bar.
     *
     * @param aMenu menu bar
     */
    public void setMenu(JMenuBar aMenu) {
        mMenuBar = aMenu;
        setJMenuBar(mMenuBar);
    }
}
