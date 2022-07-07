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
 * 05/17/2007   Andre Mermegas: name update
 * 
 */
package fxts.stations.ui.help;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

/**
 */
public class HelpManager {
    /**
     * Instance of the HelpManager.
     */
    private static HelpManager cInst = new HelpManager();
    /**
     * Help frame.
     */
    private HelpFrame mFrame;
    /**
     * Is window shown at this moment?
     */
    private boolean mIsShowing;
    private final Log mLogger = LogFactory.getLog(HelpManager.class);
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;

    /**
     * Constructor
     */
    private HelpManager() {
        //gets resource manager
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
        } catch (Exception e) {
            mLogger.error("Resource manager not loaded.");
            e.printStackTrace();
        }
    }

    /**
     * Returns one and only instance of the trader application.
     */
    public static HelpManager getInst() {
        return cInst;
    }

    /**
     * Show the help window with opened first page.
     */
    public void showHelp() {
        if (mIsShowing) {
            if (mFrame == null) {
                return;
            }
            mFrame.toFront();
            //displays if window was at iconified state
            if (mFrame.getState() == JFrame.ICONIFIED) {
                mFrame.setState(JFrame.NORMAL);
            }
        } else {
            //if help window isn't showing now
            mFrame = new HelpFrame();
            mFrame.setVisible(true);
            mIsShowing = true;
        }
    }

    /**
     * JFrame based class provides specifies operatins at time of creating frame.
     */
    private class HelpFrame extends JFrame implements ILocaleListener {
        /**
         * Panel with all components of Help frame.
         */
        private HelpPane mHelpPane;

        /**
         * Constructor.
         */
        public HelpFrame() {
            //sets title of window
            setTitle(mResMan.getString("IDS_MAINFRAME_SHORT_TITLE", "Java Trading Station")
                     + " " + mResMan.getString("IDS_HELP_CAPTION", "Help"));
            //localized icon of application
            URL iconUrl = mResMan.getResource("ID_APPLICATION_ICON");
            if (iconUrl != null) {
                ImageIcon imageIcon = new ImageIcon(iconUrl);
                setIconImage(imageIcon.getImage());
            }
            mHelpPane = new HelpPane();
            setContentPane(mHelpPane);
            addWindowListener(
                    new WindowAdapter() {
                        public void windowClosed(WindowEvent aEvent) {
                            saveSettings();
                            mResMan.removeLocaleListener(HelpFrame.this);
                            mResMan.removeLocaleListener(mHelpPane);
                            mFrame = null;
                            mHelpPane = null;
                            mIsShowing = false;
                        }
                    }
            );
            //adds to array of locale listeners
            mResMan.addLocaleListener(mHelpPane);
            mResMan.addLocaleListener(this);
            loadSettings();
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        private void saveSettings() {
            //saves position at storage
            try {
                UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
                preferences.set("helpwnd.x", getX());
                preferences.set("helpwnd.y", getY());
                preferences.set("helpwnd.width", getWidth());
                preferences.set("helpwnd.height", getHeight());
                preferences.set("helpwnd.split", mHelpPane.getDeviderPosition());
            } catch (Exception ex) {
                mLogger.error("Position of help window not saved!");
            }
        }

        private void loadSettings() {
            //Get PersistenceStorage
            UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            //loads settings from preferences
            int x = preferences.getInt("helpwnd.x");
            int y = preferences.getInt("helpwnd.y");
            int width = preferences.getInt("helpwnd.width");
            int height = preferences.getInt("helpwnd.height");
            int splitPosition = preferences.getInt("helpwnd.split");

            //checking for imposible combination of settings
            if (width == 0 && height == 0) {
                //sets default positions
                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                //sets position of the frame
                setBounds(new Rectangle(screen.width - 500, 0, 500, 400));
                //sets splitter position
                mHelpPane.setDividerPosition(200);
            } else {
                //sets position of the frame
                setBounds(new Rectangle(x, y, width, height));
                //sets splitter position
                mHelpPane.setDividerPosition(splitPosition);
            }
        }

        /**
         * This method is called when current locale of the aMan is changed.
         * It`s a ILiaisonListener method.
         *
         * @param aMan resource manager.
         */
        public void onChangeLocale(ResourceManager aMan) {
            //sets title of window
            setTitle(mResMan.getString("IDS_MAINFRAME_SHORT_TITLE", "Java Trading Station")
                     + " " + mResMan.getString("IDS_HELP_CAPTION", "Help"));
            //localized icon of application
            URL iconUrl = aMan.getResource("ID_APPLICATION_ICON");
            if (iconUrl != null) {
                ImageIcon imageIcon = new ImageIcon(iconUrl);
                setIconImage(imageIcon.getImage());
            }
        }
    }
}
