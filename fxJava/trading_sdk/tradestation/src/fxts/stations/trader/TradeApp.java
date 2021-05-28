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
 * 05/09/2006   Andre Mermegas: updates for some platform look and feel stuff
 * 05/10/2006   Andre Mermeags: set/get lookandfeel using system property preference
 * 07/18/2006   Andre Mermegas: performance update
 * 05/17/2007   Andre Mermegas: preload for ui perf
 */
package fxts.stations.trader;

import fxts.stations.core.FXCMConnectionsManager;
import fxts.stations.core.TradeDesk;
import fxts.stations.trader.ui.MainFrame;
import fxts.stations.trader.ui.dialogs.LoginDialog;
import fxts.stations.trader.ui.frames.AccountsFrame;
import fxts.stations.trader.ui.frames.ClosedPositionsFrame;
import fxts.stations.trader.ui.frames.MessagesFrame;
import fxts.stations.trader.ui.frames.OpenPositionsFrame;
import fxts.stations.trader.ui.frames.OrdersFrame;
import fxts.stations.trader.ui.frames.RatesFrame;
import fxts.stations.trader.ui.frames.SummaryFrame;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.LoginRequest;
import fxts.stations.ui.UIManager;
import fxts.stations.util.PersistentStorage;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.preferences.FontChooser;
import fxts.stations.util.preferences.PreferencesManager;

import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Locale;

/**
 * This class represents trader application.
 * It contains entry point and responsible for create
 * and initialize main UI objects.
 */
public class TradeApp {
    private static final TradeApp INST = new TradeApp();
    private static String cUserName;
    private static String cPassword;
    private static String cTerminal;
    private static String cHost;
    private static String cCfgFile;
    private MainFrame mMainFrame;
    private ResourceManager mResourceManager;

    /**
     * Private constructor.
     */
    private TradeApp() {
    }

    /**
     * This method is called in shutdown hook.
     */
    protected void exitInstance() {
        //saves current primary language to the persistent storage
        saveLocale();
        Liaison.getInstance().cleanup();
        PersistentStorage storage;
        try {
            storage = PersistentStorage.getStorage();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        try {
            Dimension size = mMainFrame.getSize();
            storage.set("app.frame.width", String.valueOf(size.width));
            storage.set("app.frame.height", String.valueOf(size.height));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //flushes persistent storage
        try {
            storage.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public AccountsFrame getAccountsFrame() {
        return (AccountsFrame) mMainFrame.getChildManager().findFrameByName(AccountsFrame.NAME);
    }

    public ClosedPositionsFrame getClosedPositionsFrame() {
        return (ClosedPositionsFrame) mMainFrame.getChildManager().findFrameByName(ClosedPositionsFrame.NAME);
    }

    /**
     * Returns one and only instance of the trader application.
     */
    public static TradeApp getInst() {
        return INST;
    }

    /**
     * Returns main application frame.
     */
    public MainFrame getMainFrame() {
        return mMainFrame;
    }

    public MessagesFrame getMessagesFrame() {
        return (MessagesFrame) mMainFrame.getChildManager().findFrameByName(MessagesFrame.NAME);
    }

    public OpenPositionsFrame getOpenPositionsFrame() {
        return (OpenPositionsFrame) mMainFrame.getChildManager().findFrameByName(OpenPositionsFrame.NAME);
    }

    public OrdersFrame getOrdersFrame() {
        return (OrdersFrame) mMainFrame.getChildManager().findFrameByName(OrdersFrame.NAME);
    }

    public RatesFrame getRatesFrame() {
        return (RatesFrame) mMainFrame.getChildManager().findFrameByName(RatesFrame.NAME);
    }

    /**
     * Returns main resource manager.
     */
    public ResourceManager getResourceManager() {
        return mResourceManager;
    }

    public SummaryFrame getSummaryFrame() {
        return (SummaryFrame) mMainFrame.getChildManager().findFrameByName(SummaryFrame.NAME);
    }

    /**
     * Creates application object.
     */
    protected void initInstance() {
        //adds shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                exitInstance();
            }
        });

        try {
            mResourceManager = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mMainFrame,
                                          "Resource manager not created!",
                                          "Java Trading Station",
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        PersistentStorage storage;
        try {
            storage = PersistentStorage.getStorage();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mMainFrame,
                                          mResourceManager.getString("IDS_ER_PERS_STORAGE_NOT_OPENED"),
                                          mResourceManager.getString("IDS_MAINFRAME_SHORT_TITLE"),
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        //gets locale parameters from the storage
        String language = storage.getString("app.locale.language", null);
        String country = storage.getString("app.locale.country", null);

        //Create Locale or Get default Locale of the ResourceManager
        Locale locale;
        if (language != null && country != null) {
            locale = new Locale(language, country);
        } else {
            locale = mResourceManager.getDefaultLocale();
        }
        ResourceManager.setPrimaryLocale(locale);
        UIManager.getInst().setResourceManager(mResourceManager);

        //preload for ui experience
        FontChooser.init();
        PreferencesManager.getPreferencesManager(TradeDesk.getInst().getUserName()).init();

        mMainFrame = new MainFrame();
        //gets windows parameters from the storage
        int width = storage.getInt("app.frame.width", 800);
        int height = storage.getInt("app.frame.height", 600);
        mMainFrame.setSize(width, height);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        mMainFrame.setLocation((screen.width - width) / 2, (screen.height - 20 - height) / 2);
        mMainFrame.setVisible(true);
        if (cUserName != null && cPassword != null && cTerminal != null && cHost != null) {
            Liaison.getInstance().login(new LoginRequest(cUserName, cPassword, cTerminal, cHost, cCfgFile));
        } else {
            LoginDialog loginDialog = new LoginDialog(mMainFrame);
            if (loginDialog.showModal() == JOptionPane.OK_OPTION) {
                Liaison.getInstance().login(loginDialog.getLoginParameters());
            }
        }
    }

    /**
     * Saves information about current locale to persistence storage.
     */
    private void saveLocale() {
        if (mResourceManager != null) {
            Locale locale = mResourceManager.getLocale();
            PersistentStorage storage;
            try {
                storage = PersistentStorage.getStorage();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            String language = locale.getLanguage();
            String country = locale.getCountry();

            //sets locale parameters to the storage
            storage.set("app.locale.language", language);
            storage.set("app.locale.country", country);
        }
    }

    /**
     * Trader application entry point.
     */
    public static void main(String[] args) {
        UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        String cx = preferences.getString("Server.Connections");
        if (cx == null) {
            String def = System.getProperty("Server.Default");
            if (def == null) {
                FXCMConnectionsManager.setConnections(preferences.getString("Server.Default"));
            } else {
                FXCMConnectionsManager.setConnections(def);
            }
        } else {
            FXCMConnectionsManager.setConnections(cx);
        }
        String laf = preferences.getString("Server.lookandfeel");
        if (laf != null) {
            try {
                javax.swing.UIManager.setLookAndFeel(laf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // try and load the native laf by default
            try {
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                //do nothing
            }
        }
        if (args.length == 4 || args.length == 5) {
            cUserName = args[0];
            cPassword = args[1];
            cTerminal = args[2];
            cHost = args[3];
        }
        if (args.length == 5) {
            cCfgFile = args[4];
        }
        INST.initInstance();
    }
}
