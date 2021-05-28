/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/MainFrame.java#2 $
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
 * $History: $
 *
 * 09/05/2003   KAV     Initial creation
 * 1/6/2004     USHIK   Ask connection name from Tradedesk instead liaison
 * 4/27/2004    USHIK   Adds button "Charts" and menu "Charts"
 * 05/10/2006   Andre Mermeags: set/get lookandfeel using system property preference
 * 07/05/2006   Andre Mermegas: fixes for dynamic look and feel changes,
        autoarrange will always reset windows to default position
        bugfix for autoarrange logic, no rates window was causing all windows to disappear
 * 05/17/2007   Andre Mermegas: show restart message on language,look and feel change
 */
package fxts.stations.trader.ui;

import com.fxcm.fix.IFixDefs;
import com.fxcm.util.Util;
import fxts.stations.core.IClickModel;
import fxts.stations.core.TradeDesk;
import fxts.stations.datatypes.Account;
import fxts.stations.datatypes.Message;
import fxts.stations.datatypes.Order;
import fxts.stations.datatypes.Position;
import fxts.stations.datatypes.Rate;
import fxts.stations.datatypes.Side;
import fxts.stations.datatypes.Summary;
import fxts.stations.trader.IFXTSConstants;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.actions.ChangePasswordAction;
import fxts.stations.trader.ui.actions.ClosePositionAction;
import fxts.stations.trader.ui.actions.CreateEntryOrderAction;
import fxts.stations.trader.ui.actions.CreateMarketOrderAction;
import fxts.stations.trader.ui.actions.CurrencySubscriptionAction;
import fxts.stations.trader.ui.actions.LoginAction;
import fxts.stations.trader.ui.actions.RemoveEntryOrderAction;
import fxts.stations.trader.ui.actions.ReportAction;
import fxts.stations.trader.ui.actions.RequestForQuoteAction;
import fxts.stations.trader.ui.actions.SetStopLimitAction;
import fxts.stations.trader.ui.actions.SetStopLimitOrderAction;
import fxts.stations.trader.ui.actions.ToggleStatusBarAction;
import fxts.stations.trader.ui.actions.ToggleToolbarAction;
import fxts.stations.trader.ui.actions.TradingModeAction;
import fxts.stations.trader.ui.actions.UpdateEntryOrderAction;
import fxts.stations.trader.ui.dialogs.AboutDialog;
import fxts.stations.trader.ui.frames.AccountsFrame;
import fxts.stations.trader.ui.frames.AdvancedRatesFrame;
import fxts.stations.trader.ui.frames.ChildFrame;
import fxts.stations.trader.ui.frames.ClosedPositionsFrame;
import fxts.stations.trader.ui.frames.MessagesFrame;
import fxts.stations.trader.ui.frames.OpenPositionsFrame;
import fxts.stations.trader.ui.frames.OrdersFrame;
import fxts.stations.trader.ui.frames.RatesFrame;
import fxts.stations.trader.ui.frames.SummaryFrame;
import fxts.stations.transport.ALiaisonListener;
import fxts.stations.transport.ILiaisonListener;
import fxts.stations.transport.LiaisonException;
import fxts.stations.transport.LiaisonListenerStub;
import fxts.stations.transport.LiaisonStatus;
import fxts.stations.transport.tradingapi.Liaison;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.ApplicationFrame;
import fxts.stations.ui.ChildManager;
import fxts.stations.ui.StatusBar;
import fxts.stations.ui.UIManager;
import fxts.stations.ui.help.HelpManager;
import fxts.stations.util.ActionTypes;
import fxts.stations.util.BrowserLauncher;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.preferences.PreferencesManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

/**
 * It's main frame of the application.
 * This class if application frame work.
 */
public class MainFrame extends ApplicationFrame implements ILocaleListener, ILiaisonListener, IMainFrame {
    private static final int INSERT_SIZE = 70;
    private static final int CONNECTING_INDICATOR = 1;
    private static final int CONNECTING_STATUS = 2;
    private static final int USER_NAME = 3;
    private static final int SERVER_NAME = 4;
    private static final int SERVER_TIME = 5;
    private double mOrdersHeight = 0.34;
    private double mAccountsHeight = 0.33;
    private double mSummaryHeight = 0.33;
    private boolean mAutoArrange = true;
    private List<ChartAction> mChartsActions;
    private JPopupMenu mChartsButtonPopupMenu;
    private JMenuBar mConnectedMenu;
    private LiaisonStatus mCurStat = LiaisonStatus.DISCONNECTED;
    private JMenuBar mDisconnectedMenu;
    private boolean mExitingRequired;
    private LiaisonStatusIndicator mStatusIndicator;
    private ServerTimePane mTimePane;
    private Observer mTitleObserver;
    private JDialog mWaitDialog;

    public MainFrame() {
        ResourceManager resmng = TradeApp.getInst().getResourceManager();

        //creates time panel
        mTimePane = new ServerTimePane();

        //adds to array of locale listeners
        resmng.addLocaleListener(mTimePane);

        //creates indicator of LiaisonStatus
        mStatusIndicator = new LiaisonStatusIndicator();

        //adds to array of locale listeners
        resmng.addLocaleListener(this);

        //creates the LiaisonListenerStub
        LiaisonListenerStub liaisonStub = new LiaisonListenerStub(this);

        //registers LiaisonListenerStub at Liaison
        Liaison liaison = Liaison.getInstance();
        liaison.addLiaisonListener(liaisonStub);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //adds window listener
        addWindowListener(
                new WindowAdapter() {
                    /**
                     * Invoked when a window has been closed as the
                     * result of calling dispose on the window.
                     */
                    @Override
                    public void windowClosed(WindowEvent aEvent) {
                        System.exit(1);
                    }

                    /**
                     * Invokes when a window is in the process of being closed.
                     */
                    @Override
                    public void windowClosing(WindowEvent aEvent) {
                        //stars procedure of exiting from application
                        exitFromApp();
                    }
                });
        //gets user preferences
        UserPreferences uiPrefs = UserPreferences.getUserPreferences();
        mChartsActions = new ArrayList<ChartAction>();
        if (uiPrefs != null) {
            mChartsButtonPopupMenu = UIManager.getInst().createPopupMenu();
            String charts = System.getProperty("charts.urls");
            System.out.println("charts = " + charts);
            if (charts == null) {
                charts = uiPrefs.getString("charts.urls");
            }
            System.out.println("charts = " + charts);
            String[] lines = Util.splitToArray(charts, "|");
            for (String line : lines) {
                String[] pair = Util.splitToArray(line, ",");
                String title = pair[0];
                String url = pair[1];
                ChartAction action = new ChartAction(url);
                UIManager uiManager = UIManager.getInst();
                uiManager.addAction(action, title, null, null, null, null);
                mChartsActions.add(action);
                mChartsButtonPopupMenu.add(uiManager.createMenuItem(action));
            }
        }

        //fires creation of menu, statusbar and toolbar
        create();

        //sets size of the frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(INSERT_SIZE, INSERT_SIZE, screenSize.width - INSERT_SIZE * 2, screenSize.height - INSERT_SIZE * 2);

        //fires loading of localised resources
        onChangeLocale(resmng);

        //adds component listener to the frame
        addComponentListener(
                new ComponentAdapter() {
                    /**
                     * Invoked when the component's size changes.
                     */
                    @Override
                    public void componentResized(ComponentEvent aEvent) {
                        if (aEvent == null) {
                            return;
                        }
                        if (mAutoArrange) {
                            setTileOrdering();
                        }
                    }
                });
    }

    /**
     * @param aMenu look and feel menu
     */
    public void addProgramLookandFeel(JMenu aMenu) {
        final ButtonGroup group = new ButtonGroup();
        LookAndFeelInfo[] laf = javax.swing.UIManager.getInstalledLookAndFeels();
        for (LookAndFeelInfo type : laf) {
            JRadioButtonMenuItem rbmi = UIManager.getInst().createRadioButtonMenuItem(type.getName(),
                                                                                      null,
                                                                                      null,
                                                                                      "IDS_LANGUAGE_DESC");
            rbmi.putClientProperty("Server.lookandfeel", type.getClassName());
            if (type.getName().equals(javax.swing.UIManager.getLookAndFeel().getName())) {
                rbmi.setSelected(true);
            }
            Liaison.getInstance().addLiaisonListener(
                    new ALiaisonListener() {
                        @Override
                        public void onLoginCompleted() {
                            UserPreferences userPreferences = UserPreferences.getUserPreferences();
                            Enumeration enumeration = group.getElements();
                            while (enumeration.hasMoreElements()) {
                                JRadioButtonMenuItem o = (JRadioButtonMenuItem) enumeration.nextElement();
                                if (o.getText().equalsIgnoreCase(userPreferences.getString("Server.lookandfeel"))) {
                                    o.setSelected(true);
                                }
                            }
                        }
                    });

            //adds listener to button
            rbmi.addActionListener(
                    new ActionListener() {
                        private UserPreferences mUserPreferences = UserPreferences.getUserPreferences();

                        public void actionPerformed(ActionEvent aEvent) {
                            JRadioButtonMenuItem item = (JRadioButtonMenuItem) aEvent.getSource();
                            String type = (String) item.getClientProperty("Server.lookandfeel");
                            mLogger.debug("setting look and feel  to = " + type);
                            try {
                                mUserPreferences.setUserName(TradeDesk.getInst().getUserName());
                                mUserPreferences.set("Server.lookandfeel", type);
                                javax.swing.UIManager.setLookAndFeel(type);
                                ChildManager childManager = getChildManager();
                                childManager.removeAll();
                                Frame[] frames = getFrames();
                                for (Frame frame : frames) {
                                    SwingUtilities.updateComponentTreeUI(frame);
                                }
                                if (mConnectedMenu.isShowing()) {
                                    //do this after update to avoid problems in table selection
                                    createInternalFrames();
                                    SwingUtilities.updateComponentTreeUI(mDisconnectedMenu);
                                } else {
                                    SwingUtilities.updateComponentTreeUI(mConnectedMenu);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            JOptionPane.showMessageDialog(MainFrame.this,
                                                          "A restart is required to completely apply the changes.");
                        }
                    });
            aMenu.add(rbmi);
            group.add(rbmi);
        }
    }

    /**
     * Checks for for presents of visible internal frame.
     */
    public void checkForVisibleChilds() {
        //gets child manager
        ChildManager childMan = getChildManager();
        JInternalFrame[] frames = childMan.getAllFrames();
        boolean visible = false;
        for (JInternalFrame frame : frames) {
            if (frame != null) {
                if (frame.isVisible()) {
                    visible = true;
                    break;
                }
            }
        }
        if (!visible) {
            JToolBar toolbar = getToolBar();
            Component[] components = toolbar.getComponents();
            for (int i = components.length - 1; i >= 0; i--) {
                Component comp = components[i];
                if (comp != null) {
                    if (comp.isEnabled()) {
                        comp.requestFocus();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates the connected menu.
     *
     * @return connected menu
     */
    private JMenuBar createConnectedMenu() {
        UIManager uiManager = UIManager.getInst();
        //creates the menu bar
        JMenuBar menuBar = new JMenuBar();

        //gets user preferences
        UserPreferences uiPrefs = UserPreferences.getUserPreferences();
        SetStopLimitOrderAction.newAction(null); //xxx added this so table listener gets set up correctly.

        //Build the first menu.
        JMenu menu = uiManager.createMenu("IDS_FILE", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "File");
        menu.setMnemonic('F');
        menuBar.add(menu);

        //a group of JMenuItems
        JMenuItem menuItem = uiManager.createMenuItem("IDS_LOGOUT", "ID_LOGOUT_ICON", null, "IDS_LOGOUT_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Logout")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Logout");
        menuItem.setMnemonic('L');
        menu.add(menuItem);
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        //initiaites logout procedure.
                        Liaison liaison = Liaison.getInstance();
                        liaison.logout();
                    }
                });

        ChangePasswordAction passwordAction = new ChangePasswordAction();
        uiManager.addAction(passwordAction, "IDS_CHANGE_PASS", null, null, null, null);
        menuItem = uiManager.createMenuItem(passwordAction);
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Change Password");
        menuItem.setMnemonic('C');
        menu.add(menuItem);

        menuItem = uiManager.createMenuItem("IDS_REFRESH", null, null, "IDS_REFRESH_DESC");
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Refresh");
        menuItem.setMnemonic('R');
        menu.add(menuItem);
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        Liaison.getInstance().refresh();
                    }
                });

        //a group of JMenuItems
        menuItem = uiManager.createMenuItem("IDS_OPTIONS", null, null, "IDS_OPTIONS_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Options")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Options");
        menuItem.setMnemonic('O');
        menu.add(menuItem);
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        PreferencesManager preferencesManager =
                                PreferencesManager.getPreferencesManager(TradeDesk.getInst().getUserName());
                        preferencesManager.showPreferencesDialog();
                    }
                });

        menu.addSeparator();
        ToggleToolbarAction showhide = new ToggleToolbarAction();
        uiManager.addAction(showhide, "IDS_TOGGLE_TOOLBAR", null, null, null, null);
        menuItem = uiManager.createMenuItem(showhide);
        menuItem.setMnemonic('T');
        menu.add(menuItem);

        ToggleStatusBarAction statusBarAction = new ToggleStatusBarAction();
        uiManager.addAction(statusBarAction, "IDS_TOGGLE_STATUSBAR", null, null, null, null);
        menuItem = uiManager.createMenuItem(statusBarAction);
        menuItem.setMnemonic('S');
        menu.add(menuItem);
        menu.addSeparator();

        //creates additional menu
        JMenu submenu;
        //submenu = uiManager.createMenu("Language", null);
        //submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Language");
        //menu.add(submenu);
        //addSupportedLanguages(submenu);
        //menu.addSeparator();

        submenu = uiManager.createMenu("IDS_LOOK_AND_FEEL", null);
        submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "PLAF");
        menu.add(submenu);
        addProgramLookandFeel(submenu);
        menu.addSeparator();

        menuItem = uiManager.createMenuItem("IDS_EXIT", "ID_EXIT_ICON", null, "IDS_EXIT_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Exit")));
        }
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        exitFromApp();
                    }
                });
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Exit");
        menuItem.setMnemonic('X');
        menu.add(menuItem);

        //Build the second menu.
        menu = uiManager.createMenu("IDS_ACTION", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Action");
        menu.setMnemonic('A');
        menuBar.add(menu);

        //first submenu
        submenu = uiManager.createMenu("IDS_ACCOUNTS", null);
        submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Account");
        menu.add(submenu);
        Action reportAction = ReportAction.newAction(null);
        uiManager.addAction(reportAction, "IDS_REPORT", "ID_REPORT_ICON", null, "IDS_REPORT_DESC", "IDS_REPORT_DESC");
        menuItem = uiManager.createMenuItem(reportAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Report")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Report");
        submenu.add(menuItem);

        //second submenu
        submenu = uiManager.createMenu("IDS_DEALING_RATES", null);
        submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Dealing Rates");
        menu.add(submenu);
        Action marketOrderAction = CreateMarketOrderAction.newAction(null);
        uiManager.addAction(
                marketOrderAction,
                "IDS_CREATE_MARKET_ORDER",
                "ID_MARKET_ORDER_ICON",
                null,
                "IDS_CREATE_MARKET_ORDER_DESC",
                "IDS_CREATE_MARKET_ORDER_DESC");
        menuItem = uiManager.createMenuItem(marketOrderAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.MarketOrder")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Create Market Order");
        submenu.add(menuItem);

        Action rfqAction = RequestForQuoteAction.newAction(null);
        uiManager.addAction(
                rfqAction,
                "IDS_REQUEST_FOR_QUOTE",
                "ID_MARKET_ORDER_ICON",
                null,
                "IDS_REQUEST_FOR_QUOTE_DESC",
                "IDS_REQUEST_FOR_QUOTE_DESC");
        menuItem = uiManager.createMenuItem(rfqAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.RFQ")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Request For Quote");
        submenu.add(menuItem);

        Action entryOrderAction = CreateEntryOrderAction.newAction(null);
        uiManager.addAction(
                entryOrderAction,
                "IDS_CREATE_ENTRY_ORDER",
                "ID_ENTRY_ICON",
                null,
                "IDS_CREATE_ENTRY_ORDER_DESC",
                "IDS_CREATE_ENTRY_ORDER_DESC");
        menuItem = uiManager.createMenuItem(entryOrderAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.EntryOrder")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Create Entry Order");
        submenu.add(menuItem);

        Action subscriptionAction = new CurrencySubscriptionAction();
        uiManager.addAction(
                subscriptionAction,
                "IDS_CCY_SUBSCRIPTION_LIST",
                "ID_RATES_FRAME_ICON",
                null,
                "IDS_CCY_SUBSCRIPTION_LIST",
                "IDS_CCY_SUBSCRIPTION_LIST");
        menuItem = uiManager.createMenuItem(subscriptionAction);
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Currency Subscription List");
        submenu.add(menuItem);

        //third submenu
        submenu = uiManager.createMenu("IDS_ORDERS", null);
        submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, OrdersFrame.NAME);
        menu.add(submenu);
        Action entryOrderAction2 = CreateEntryOrderAction.newAction(null);
        uiManager.addAction(
                entryOrderAction2,
                "IDS_CREATE_ENTRY_ORDER_2",
                "ID_ENTRY_ICON",
                null,
                "IDS_CREATE_ENTRY_ORDER_DESC_2",
                "IDS_CREATE_ENTRY_ORDER_DESC_2");
        menuItem = uiManager.createMenuItem(entryOrderAction2);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.EntryOrder")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Create Entry Order");
        submenu.add(menuItem);
        Action updateEntryOrderAction = UpdateEntryOrderAction.newAction(null);
        uiManager.addAction(
                updateEntryOrderAction,
                "IDS_UPDATE_ENTRY_ORDER",
                "ID_ENTRY_ICON",
                null,
                "IDS_UPDATE_ENTRY_ORDER_DESC",
                "IDS_UPDATE_ENTRY_ORDER_DESC");
        menuItem = uiManager.createMenuItem(updateEntryOrderAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.UpdateEntryOrder")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Update Entry Order");
        submenu.add(menuItem);

        Action stopLimitOrderAction = SetStopLimitOrderAction.newAction("STOP");
        uiManager.addAction(
                stopLimitOrderAction,
                "IDS_STOP_LIMIT",
                "ID_S_L_ICON",
                null,
                "IDS_STOP_LIMIT_DESC",
                "IDS_STOP_LIMIT_DESC");
        menuItem = uiManager.createMenuItem(stopLimitOrderAction);
        submenu.add(menuItem);

        Action removeEntryOrderAction = RemoveEntryOrderAction.newAction(null);
        uiManager.addAction(
                removeEntryOrderAction,
                "IDS_REMOVE_ORDER",
                "ID_CLOSE_ICON",
                null,
                "IDS_REMOVE_ORDER_DESC",
                "IDS_REMOVE_ORDER_DESC");
        menuItem = uiManager.createMenuItem(removeEntryOrderAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.RemoveEntryOrder")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Remove Order");
        submenu.add(menuItem);

        //forth submenu
        submenu = uiManager.createMenu("IDS_POSITIONS", null);
        submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Positions");
        menu.add(submenu);
        Action stopLimitPositionAction = SetStopLimitAction.newAction(null);
        uiManager.addAction(
                stopLimitPositionAction,
                "IDS_STOP_LIMIT",
                "ID_S_L_ICON",
                null,
                "IDS_STOP_LIMIT_DESC",
                "IDS_STOP_LIMIT_DESC");
        menuItem = uiManager.createMenuItem(stopLimitPositionAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.StopLimit")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Stop/Limit");
        submenu.add(menuItem);
        Action closePositionAction = ClosePositionAction.newAction(null);
        uiManager.addAction(closePositionAction,
                            "IDS_CLOSE",
                            "ID_CLOSE_ICON",
                            null,
                            "IDS_CLOSE_DESC",
                            "IDS_CLOSE_DESC");
        menuItem = uiManager.createMenuItem(closePositionAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.ClosePosition")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Close");
        submenu.add(menuItem);

        //Build the third menu
        menu = uiManager.createMenu("IDS_WINDOW", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Window");
        menu.setMnemonic('W');
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = uiManager.createMenuItem("IDS_TILE", null, null, "IDS_TILE_DESC");
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Tile");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.TileOrdering")));
        }
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        setTileOrdering();
                    }
                });
        menu.add(menuItem);
        menuItem = uiManager.createMenuItem("IDS_CASCADE", null, null, "IDS_CASCADE_DESC");
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Cascade");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.CascadeOrdering")));
        }
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        setCascadeOrdering();
                    }
                });
        menu.add(menuItem);

        //Get PersistenceStorage
        try {
            UserPreferences preferences = UserPreferences.getUserPreferences();
            //sets the settings to preferences
            if (preferences.getString("autoarrange") != null) {
                mAutoArrange = preferences.getBoolean("autoarrange");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JCheckBoxMenuItem cbmi = uiManager.createCheckBoxMenuItem("IDS_AUTO_ARRANGE",
                                                                  null,
                                                                  null,
                                                                  "IDS_AUTO_ARRANGE_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.AutoArrange")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Auto Arrange");
        cbmi.setState(mAutoArrange);
        cbmi.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        //reset all the windows by default
                        ChildManager childMan = TradeApp.getInst().getMainFrame().getChildManager();
                        JInternalFrame[] frames = childMan.getAllFrames();
                        for (JInternalFrame frame : frames) {
                            if (frame instanceof ChildFrame) {
                                ChildFrame child = (ChildFrame) frame;
                                child.setVisible(true);
                                child.setMenuItemState(true);
                            }
                        }
                        checkForVisibleChilds();
                        mAutoArrange = !mAutoArrange; //flip boolean
                        setTileOrdering();
                    }
                });
        menu.add(cbmi);
        menu.addSeparator();

        //listener is applied to show/hide the frame
        ActionListener actListener = new ActionListener() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                if (aEvent != null) {
                    //gets main frame
                    MainFrame frame = TradeApp.getInst().getMainFrame();

                    //gets child manager
                    ChildManager childMan = frame.getChildManager();

                    //gets source of the event
                    Object source = aEvent.getSource();
                    if (source != null) {
                        JCheckBoxMenuItem item = (JCheckBoxMenuItem) source;
                        //gets name of the frame (coincidented with item name) from the menu item
                        String sFrameName = (String) item.getClientProperty(IFXTSConstants.MENUITEM_NAME);
                        //finds the frame at the child manager
                        ChildFrame childFrame = (ChildFrame) childMan.findFrameByName(sFrameName);
                        if (childFrame != null) {
                            childFrame.setVisible(item.isSelected());
                            checkForVisibleChilds();
                        }
                    }
                }
            }
        };
        cbmi = uiManager.createCheckBoxMenuItem("IDS_ACCOUNTS", "ID_ACCOUNTS_FRAME_ICON", null, "IDS_ACCOUNTS_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.AccountsWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, AccountsFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        cbmi = uiManager.createCheckBoxMenuItem("IDS_RATES", "ID_RATES_FRAME_ICON", null, "IDS_RATES_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.RatesWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, RatesFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        cbmi = uiManager.createCheckBoxMenuItem("IDS_POSITIONS",
                                                "ID_OPENPOSITIONS_FRAME_ICON",
                                                null,
                                                "IDS_POSITIONS_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.OpenPositionsWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, OpenPositionsFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        cbmi = uiManager.createCheckBoxMenuItem("IDS_ORDERS", "ID_ORDERS_FRAME_ICON", null, "IDS_ORDERS_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.OrdersWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, OrdersFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        cbmi = uiManager.createCheckBoxMenuItem("IDS_SUMMARY", "ID_SUMMARY_FRAME_ICON", null, "IDS_SUMMARY_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.SummaryWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, SummaryFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        cbmi = uiManager.createCheckBoxMenuItem(
                "IDS_CLOSED_POSITIONS_TITLE",
                "ID_OPENPOSITIONS_FRAME_ICON",
                null,
                "IDS_POSITIONS_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.ClosedPositionsWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, ClosedPositionsFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        cbmi = uiManager.createCheckBoxMenuItem(
                "IDS_MESSAGES_TITLE",
                "ID_MESSAGES_FRAME_ICON",
                null,
                "IDS_MESSAGES_DESC");
        if (uiPrefs != null) {
            cbmi.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.MessagesWindow")));
        }
        cbmi.putClientProperty(IFXTSConstants.MENUITEM_NAME, MessagesFrame.NAME);
        cbmi.addActionListener(actListener);
        menu.add(cbmi);

        menu = uiManager.createMenu("IDS_CHARTS_TITLE", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Charts");
        menu.setMnemonic('C');
        menuBar.add(menu);
        for (Object mChartsAction : mChartsActions) {
            Action action = (Action) mChartsAction;
            menu.add(uiManager.createMenuItem(action));
        }

        //Build the fourth menu.
        menu = uiManager.createMenu("IDS_HELP_MENU", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Help");
        menu.setMnemonic('H');
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = uiManager.createMenuItem("IDS_HELP", "ID_HELP_ICON", null, "IDS_HELP_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Help")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Help");
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        HelpManager mgr = HelpManager.getInst();
                        //shows help window
                        mgr.showHelp();
                    }
                });
        menu.add(menuItem);
        menuItem = uiManager.createMenuItem("IDS_ABOUT", null, null, "IDS_ABOUT_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.About")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "About");
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        AboutDialog dialog = new AboutDialog(MainFrame.this);
                        dialog.showModal();
                    }
                });
        menu.add(menuItem);
        return menuBar;
    }

    /**
     * Initializes the disconnected menu
     *
     * @return disconnected menu
     */
    private JMenuBar createDisconnectedMenu() {
        UIManager uiManager = UIManager.getInst();
        //creates the menu bar
        JMenuBar menuBar = new JMenuBar();
        //gets user preferences
        UserPreferences uiPrefs = UserPreferences.getUserPreferences();

        //Build the first menu.
        JMenu menu = uiManager.createMenu("IDS_FILE", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "File");
        menu.setMnemonic('F');
        menuBar.add(menu);

        //a group of JMenuItems
        Action loginAction = new LoginAction();
        uiManager.addAction(loginAction, "IDS_LOGIN", "ID_LOGIN_ICON", null, null, "IDS_LOGIN_DESC");
        JMenuItem menuItem = uiManager.createMenuItem(loginAction);
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Login")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Login");
        menuItem.setMnemonic('L');
        menu.add(menuItem);

        //a group of JMenuItems
        menuItem = uiManager.createMenuItem("IDS_OPTIONS", null, null, "IDS_OPTIONS_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Options")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Options");
        menuItem.setMnemonic('O');
        menu.add(menuItem);
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        PreferencesManager preferencesManager = PreferencesManager.
                                getPreferencesManager(TradeDesk.getInst().getUserName());
                        preferencesManager.showPreferencesDialog();
                    }
                });

        ToggleToolbarAction showhide = new ToggleToolbarAction();
        uiManager.addAction(showhide, "IDS_TOGGLE_TOOLBAR", null, null, null, null);
        menuItem = uiManager.createMenuItem(showhide);
        menuItem.setMnemonic('T');
        menu.add(menuItem);

        ToggleStatusBarAction statusBarAction = new ToggleStatusBarAction();
        uiManager.addAction(statusBarAction, "IDS_TOGGLE_STATUSBAR", null, null, null, null);
        menuItem = uiManager.createMenuItem(statusBarAction);
        menuItem.setMnemonic('S');
        menu.add(menuItem);
        menu.addSeparator();

        //creates additional menu
        JMenu submenu;
        //submenu = uiManager.createMenu("Language", null);
        //submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Language");
        //menu.add(submenu);
        //addSupportedLanguages(submenu);
        //menu.addSeparator();

        submenu = uiManager.createMenu("IDS_LOOK_AND_FEEL", null);
        submenu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "PLAF");
        menu.add(submenu);
        addProgramLookandFeel(submenu);
        menu.addSeparator();
        menuItem = uiManager.createMenuItem("IDS_EXIT", "ID_EXIT_ICON", null, "IDS_EXIT_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Exit")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Exit");
        menuItem.setMnemonic('X');
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        exitFromApp();
                    }
                });
        menu.add(menuItem);
        menu = uiManager.createMenu("IDS_CHARTS_TITLE", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Charts");
        menu.setMnemonic('C');
        menuBar.add(menu);
        for (Object mChartsAction : mChartsActions) {
            Action action = (Action) mChartsAction;
            menu.add(uiManager.createMenuItem(action));
        }

        //Build the second menu.
        menu = uiManager.createMenu("IDS_HELP_MENU", null);
        menu.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Help");
        menu.setMnemonic('H');
        menuBar.add(menu);

        //a group of JMenuItems
        menuItem = uiManager.createMenuItem("IDS_HELP", "ID_HELP_ICON", null, "IDS_HELP_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.Help")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "Help");
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        HelpManager mgr = HelpManager.getInst();
                        //shows help window
                        mgr.showHelp();
                    }
                });
        //menuItem.setEnabled(false);
        menu.add(menuItem);
        menuItem = uiManager.createMenuItem("IDS_ABOUT", null, null, "IDS_ABOUT_DESC");
        if (uiPrefs != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(uiPrefs.getString("mainframe.shortcuts.About")));
        }
        menuItem.putClientProperty(IFXTSConstants.MENUITEM_NAME, "About");
        menuItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        AboutDialog dialog = new AboutDialog(MainFrame.this);
                        dialog.showModal();
                    }
                });
        menu.add(menuItem);
        return menuBar;
    }

    private void createInternalFrames() {
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        ComponentAdapter componentAdapter = new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent aEvent) {
                if (mAutoArrange) {
                    setTileOrdering();
                }
            }

            @Override
            public void componentHidden(ComponentEvent aEvent) {
                if (mAutoArrange) {
                    setTileOrdering();
                }
            }
        };
        UserPreferences preferences;
        //gets PersistenceStorage
        try {
            preferences = UserPreferences.getUserPreferences();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        //creates RatesFrame
        RatesFrame ratesFrame = new AdvancedRatesFrame<Rate>(resmng, this);
        ratesFrame.loadSettings();
        getChildManager().add(ratesFrame);
        ratesFrame.addComponentListener(componentAdapter);

        //creates OrdersFrame
        OrdersFrame ordersFrame = new OrdersFrame<Order>(resmng, this);
        ordersFrame.loadSettings();
        getChildManager().add(ordersFrame);
        ordersFrame.addComponentListener(componentAdapter);

        //creates OpenPositionsFrame
        OpenPositionsFrame positionFrame = new OpenPositionsFrame<Position>(resmng, this);
        positionFrame.loadSettings();
        getChildManager().add(positionFrame);
        positionFrame.addComponentListener(componentAdapter);

        //creates ClosedPositionsFrame
        ClosedPositionsFrame closedPositionsFrame = new ClosedPositionsFrame<Position>(resmng, this);
        closedPositionsFrame.loadSettings();
        getChildManager().add(closedPositionsFrame);
        closedPositionsFrame.addComponentListener(componentAdapter);

        //creates AccountsFrame
        AccountsFrame accountsFrame = new AccountsFrame<Account>(resmng, this);
        accountsFrame.loadSettings();
        getChildManager().add(accountsFrame);
        accountsFrame.addComponentListener(componentAdapter);

        //creates SummaryFrame
        SummaryFrame summaryFrame = new SummaryFrame<Summary>(resmng, this);
        summaryFrame.loadSettings();
        getChildManager().add(summaryFrame);
        summaryFrame.addComponentListener(componentAdapter);

        //creates MessagesFrame
        MessagesFrame messagesFrame = new MessagesFrame<Message>(resmng, this);
        messagesFrame.loadSettings();
        getChildManager().add(messagesFrame);
        messagesFrame.addComponentListener(componentAdapter);

        //gets all child frames
        JInternalFrame[] frames = getChildManager().getAllFrames();
        int minPos = 100;
        int minInd = 0;
        //sets z-order of frames
        for (int i = 0; i < frames.length; i++) {
            if (frames[i] != null) {
                ChildFrame childFrame = (ChildFrame) frames[i];
                //sets z-order
                int pos = preferences.getInt("childframe." + childFrame.getName() + "z-position");
                getChildManager().setPosition(childFrame, pos);
                if (pos < minPos) {
                    minPos = pos;
                    minInd = i;
                }
            }
        }
        //fires autoarrange
        if (mAutoArrange) {
            setTileOrdering();
        }
        //sets selecting of upper frame
        try {
            frames[minInd].setSelected(true);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            mLogger.error("Selecting not set");
        }
        checkForVisibleChilds();
    }

    /**
     * Creates and returns constructed menu object or null if menu is not needed.
     */
    @Override
    public JMenuBar createMenu() {
        mConnectedMenu = createConnectedMenu();
        mDisconnectedMenu = createDisconnectedMenu();
        return mDisconnectedMenu;
    }

    /**
     * Creates modal message dialog.
     *
     * @param asMessage the message to display
     * @param asTitle the title string for the dialog
     * @param aiMessageType message type
     *
     * @return message dialog
     */
    public JDialog createMessageDlg(String asMessage, String asTitle, int aiMessageType) {
        JOptionPane pane = new JOptionPane(asMessage, aiMessageType, JOptionPane.DEFAULT_OPTION, null, null, null);
        pane.setInitialValue(null);
        JDialog dialog = pane.createDialog(this, asTitle);
        pane.selectInitialValue();
        return dialog;
    }

    /**
     * Creates and returns constructed statusbar object.
     */
    @Override
    public StatusBar createStatusBar() {
        StatusBar statusBar = new StatusBar();
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        UserPreferences uiPrefs = UserPreferences.getUserPreferences();

        //first pane
        statusBar.addLabel(resmng.getString("IDS_READY"), 1.0);

        //second pane
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.setPreferredSize(new Dimension(16, 16));
        if (mStatusIndicator != null) {
            jp.add(BorderLayout.CENTER, mStatusIndicator);
        }
        jp.setBorder(BorderFactory.createEtchedBorder());
        statusBar.addPane(jp);
        //adds for prompting
        jp = (JPanel) statusBar.getPane(CONNECTING_INDICATOR);
        jp.getAccessibleContext().setAccessibleDescription(resmng.getString("IDS_STATUSBAR_CONNECTING_INDICATOR"));

        //third pane
        statusBar.addLabel(resmng.getString("IDS_STATE_DISCONNECTED"), 140, BorderFactory.createEtchedBorder());
        //adds for prompting
        jp = (JPanel) statusBar.getPane(CONNECTING_STATUS);
        jp.getAccessibleContext().setAccessibleDescription(resmng.getString("IDS_STATUSBAR_CONNECTING_STATUS"));

        //fourth pane
        statusBar.addLabel("", 80, BorderFactory.createEtchedBorder());
        //adds for prompting
        jp = (JPanel) statusBar.getPane(USER_NAME);
        jp.getAccessibleContext().setAccessibleDescription(resmng.getString("IDS_STATUSBAR_USER_NAME"));

        //fifth pane
        statusBar.addLabel("", 80, BorderFactory.createEtchedBorder());
        //adds for prompting
        jp = (JPanel) statusBar.getPane(SERVER_NAME);
        jp.getAccessibleContext().setAccessibleDescription(resmng.getString("IDS_STATUSBAR_SERVER_NAME"));

        //sixth pane
        if (mTimePane != null) {
            mTimePane.setBorder(BorderFactory.createEtchedBorder());
            statusBar.addPane(mTimePane, 180);
        }
        //adds for prompting
        JComponent jc = statusBar.getPane(SERVER_TIME);
        jc.getAccessibleContext().setAccessibleDescription(resmng.getString("IDS_STATUSBAR_SERVER_TIME"));
        if (uiPrefs.getBoolean(ToggleToolbarAction.HIDE_TOGGLE_TOOLBAR)) {
            statusBar.setVisible(false);
        }
        statusBar.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent aEvent) {
                        if (mAutoArrange) {
                            setTileOrdering();
                        }
                    }

                    @Override
                    public void componentShown(ComponentEvent aEvent) {
                        if (mAutoArrange) {
                            setTileOrdering();
                        }
                    }
                });
        return statusBar;
    }

    /**
     * Creates and returns constructed toolbar object.
     */
    @Override
    public JToolBar createToolBar() {
        UserPreferences uiPrefs = UserPreferences.getUserPreferences();
        //creates toolbar
        JToolBar toolBar = UIManager.getInst().createToolBar();
        //gets instance of user interface manager
        UIManager uiManager = UIManager.getInst();

        //first button
        Action marketOrderSellAction = CreateMarketOrderAction.newAction(Side.SELL.getName());
        uiManager.addAction(marketOrderSellAction,
                            "IDS_SELL_TEXT",
                            "ID_SELL_ICON",
                            null,
                            "IDS_SELL_DESC",
                            "IDS_SELL_DESC");
        JButton button = uiManager.createButton(marketOrderSellAction);
        button.setActionCommand(Side.SELL.getName());
        toolBar.add(button);

        //second button
        Action marketOrderBuyAction = CreateMarketOrderAction.newAction(Side.BUY.getName());
        uiManager.addAction(marketOrderBuyAction, "IDS_BUY_TEXT", "ID_BUY_ICON", null, "IDS_BUY_DESC", "IDS_BUY_DESC");
        button = uiManager.createButton(marketOrderBuyAction);
        button.setActionCommand(Side.BUY.getName());
        toolBar.add(button);

        //third button
        Action stopLimitOrderAction = SetStopLimitAction.newAction(null);
        uiManager.addAction(stopLimitOrderAction, "IDS_S_L_TEXT", "ID_S_L_ICON", null, "IDS_S_L_DESC", "IDS_S_L_DESC");
        button = uiManager.createButton(stopLimitOrderAction);
        toolBar.add(button);

        //forth button
        Action closePositionAction = ClosePositionAction.newAction(null);
        uiManager.addAction(
                closePositionAction,
                "IDS_CLOSE_TEXT",
                "ID_CLOSE_ICON",
                null,
                "IDS_CLOSE_DESC",
                "IDS_CLOSE_DESC");
        button = uiManager.createButton(closePositionAction);
        toolBar.add(button);

        //fifth button
        Action entryOrderAction = CreateEntryOrderAction.newAction(Side.BUY.getName());
        uiManager.addAction(entryOrderAction,
                            "IDS_ENTRY_TEXT",
                            "ID_ENTRY_ICON",
                            null,
                            "IDS_ENTRY_DESC",
                            "IDS_ENTRY_DESC");
        button = uiManager.createButton(entryOrderAction);
        toolBar.add(button);

        //Sixth button
        Action reportAction = ReportAction.newAction(null);
        uiManager.addAction(reportAction,
                            "IDS_REPORT_TEXT",
                            "ID_REPORT_ICON",
                            null,
                            "IDS_REPORT_DESC",
                            "IDS_REPORT_DESC");
        button = uiManager.createButton(reportAction);
        toolBar.add(button);

        //Seventh button
        button = uiManager.createButton("IDS_CHARTS_TITLE", "ID_CHARTS_ICON", "IDS_CHARTS_DESC", "IDS_CHARTS_DESC");
        toolBar.add(button);
        button.addActionListener(new ChartsActionListener(button));

        //8th button
        button = uiManager.createButton("IDS_HELP_TEXT", "ID_HELP_ICON", "IDS_HELP_DESC", "IDS_HELP_DESC");
        toolBar.add(button);
        button.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent aEvent) {
                        HelpManager mgr = HelpManager.getInst();
                        //shows help window
                        mgr.showHelp();
                    }
                });

        //9th button
        button = uiManager.createButton("IDS_TRADING_MODE",
                                        "ID_SUMMARY_FRAME_ICON",
                                        "IDS_TRADING_MODE",
                                        "IDS_TRADING_MODE");
        button.addActionListener(new TradingModeAction(button));
        toolBar.add(button);

        if (uiPrefs.getBoolean(ToggleToolbarAction.HIDE_TOGGLE_TOOLBAR)) {
            toolBar.setVisible(false);
        }
        toolBar.addComponentListener(
                new ComponentAdapter() {
                    @Override
                    public void componentHidden(ComponentEvent aEvent) {
                        if (mAutoArrange) {
                            setTileOrdering();
                        }
                    }

                    @Override
                    public void componentShown(ComponentEvent aEvent) {
                        if (mAutoArrange) {
                            setTileOrdering();
                        }
                    }
                });
        return toolBar;
    }

    /**
     * Create waiting dialog.
     *
     * @param aText text of waiting message
     *
     * @return created waiting dialog
     */
    public JDialog createWaitDialog(String aText) {
        //get resouce manager
        ResourceManager resmng = TradeApp.getInst().getResourceManager();

        //creates frame
        JDialog dialog = new JDialog(this);
        dialog.setModal(true);
        dialog.setTitle(resmng.getString("IDS_MAINFRAME_SHORT_TITLE"));
        dialog.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        //adds label
        JLabel jl = UIManager.getInst().createLabel(aText);
        jl.setHorizontalAlignment(SwingConstants.CENTER);
        jl.setBorder(new EmptyBorder(15, 40, 15, 40));
        Container cp = dialog.getContentPane();
        cp.setLayout(new BorderLayout());
        cp.add(jl, BorderLayout.CENTER);

        //sets size of the frame
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getOwner());
        return dialog;
    }

    /**
     * Exit fom application.
     */
    private void exitFromApp() {
        //get resouce manager
        ResourceManager resmng = TradeApp.getInst().getResourceManager();

        //shows confirmation dialog
        if (showConfirmationDlg(
                resmng.getString("IDS_EXIT_DLG_MESSAGE"),
                resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            //gets liaison
            Liaison liaison = Liaison.getInstance();
            //gets current status
            LiaisonStatus status = liaison.getStatus();
            if (status.equals(LiaisonStatus.DISCONNECTED)) {
                //disposes mainframe window
                dispose();
            } else {
                //initiaites logout procedure.
                liaison.logout();
                //sets demand for exit
                mExitingRequired = true;
            }
        }
    }

    /**
     * Find the menu with specified name.
     *
     * @param aName name of the menu
     * return founded menu and null if the menu not founded
     */
    public JMenu findMenu(String aName) {
        //gets current menu bar
        JMenuBar menuBar = getMenu();
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            JMenu menu = menuBar.getMenu(i);
            if (menu != null) {
                //gets menu item name
                String sCurrentMenuName = (String) menu.getClientProperty(IFXTSConstants.MENUITEM_NAME);
                if (sCurrentMenuName != null) {
                    if (aName.equals(sCurrentMenuName)) {
                        return menu;
                    }
                }
            }
        }

        //if the item with specified name not found
        return null;
    }

    /**
     * Find the item with specified name.
     *
     * @param aName name of the menu item
     * @param aMenu place of the search
     * return founded menu item and null if the menu item not founded
     */
    public JMenuItem findMenuItem(String aName, JMenu aMenu) {
        for (int i = 0; i < aMenu.getItemCount(); i++) {
            JMenuItem menuItem = aMenu.getItem(i);
            if (menuItem != null) {
                //gets menu item name
                String sCurrentItemName = (String) menuItem.getClientProperty(IFXTSConstants.MENUITEM_NAME);
                if (sCurrentItemName != null) {
                    if (aName.equals(sCurrentItemName)) {
                        return menuItem;
                    }
                }
            }
        }

        //if the item with specified name not found
        return null;
    }

    /**
     * Returns Action of specified type.
     *
     * @param aTypes Action type
     * @param asCommand Action command
     *
     * @return aproppriate action
     */
    public Action getAction(ActionTypes aTypes, String asCommand) {
        if (aTypes == ActionTypes.CLOSE_POSITION) {
            return ClosePositionAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.CREATE_ENTRY_ORDER) {
            return CreateEntryOrderAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.CREATE_MARKET_ORDER) {
            return CreateMarketOrderAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.REMOVE_ENTRY_ORDER) {
            return RemoveEntryOrderAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.SET_STOP_LIMIT) {
            return SetStopLimitAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.SET_STOP_LIMIT_ORDER) {
            return SetStopLimitOrderAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.UPDATE_ENTRY_ORDER) {
            return UpdateEntryOrderAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.REQUEST_FOR_QUOTE) {
            return RequestForQuoteAction.newAction(asCommand);
        }
        if (aTypes == ActionTypes.REPORT) {
            return ReportAction.newAction(asCommand);
        }
        return null;
    }

    /**
     * Returns localized string representation of Liaison status.
     *
     * @param aMan resource manager
     *
     * @return liaison status
     */
    private String getLiaisonStatusString(ResourceManager aMan) {
        //gets liaison
        Liaison liaison = Liaison.getInstance();
        //gets current status
        LiaisonStatus status = liaison.getStatus();

        //return a string what correspond current liaison status
        if (status.equals(LiaisonStatus.CONNECTING)
            || status.equals(LiaisonStatus.RECEIVING)
            || status.equals(LiaisonStatus.SENDING)) {
            return aMan.getString("IDS_STATE_CONNECTING");
        } else if (status.equals(LiaisonStatus.READY)) {
            return aMan.getString("IDS_STATE_CONNECTED");
        } else if (status.equals(LiaisonStatus.RECONNECTING)) {
            return aMan.getString("IDS_STATE_RECONNECTING");
        } else if (status.equals(LiaisonStatus.DISCONNECTING)) {
            return aMan.getString("IDS_STATE_DISCONNECTING");
        } else if (status.equals(LiaisonStatus.DISCONNECTED)) {
            return aMan.getString("IDS_STATE_DISCONNECTED");
        }
        return null;
    }

    /**
     * Returns localized title.
     *
     * @param aResourceMan current resource manager
     */
    private String getLocalizedTitle(ResourceManager aResourceMan) {
        if (aResourceMan != null) {
            //localizes title of main frame
            StringBuffer titleBuffer = new StringBuffer();
            titleBuffer.append(aResourceMan.getString("IDS_MAINFRAME_TITLE"));
            titleBuffer.append(" ");
            titleBuffer.append(IFXTSConstants.CURRENT_VERSION);
            return titleBuffer.toString();
        }
        mLogger.debug("MainFrame.getLocalizedTitle: aResourceMan is null");
        return null;
    }

    /**
     * Returns waiting dialog.
     *
     * @return waiting dialog.
     */
    public JDialog getWaitDialog() {
        return mWaitDialog;
    }

    /**
     * Sets waiting dialog.
     *
     * @param aWaitDialog waiting dialog.
     */
    public void setWaitDialog(JDialog aWaitDialog) {
        mWaitDialog = aWaitDialog;
    }

    private boolean isFrameInvisible(JInternalFrame aFrame) {
        return aFrame != null && !aFrame.isVisible();
    }

    private boolean isFrameVisible(int aVisibleframes, int aFrameMode) {
        return (aVisibleframes & aFrameMode) == aFrameMode;
    }

    /**
     * This method is called when current locale of the aMan is changed.
     * It`s a ILiaisonListener method.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        setTitle(getLocalizedTitle(aMan));
        //localized icon of application
        URL iconUrl = aMan.getResource("ID_APPLICATION_ICON");
        if (iconUrl != null) {
            ImageIcon imageIcon = new ImageIcon(iconUrl);
            setIconImage(imageIcon.getImage());
        }

        //localizes status bar
        StatusBar statusBar = getStatusBar();
        if (statusBar != null) {
            if (statusBar.getPaneCount() > 3) {
                statusBar.setText(0, aMan.getString("IDS_READY"));
                statusBar.setText(2, getLiaisonStatusString(aMan));
            }
            try {
                //localizes statusbar panel`s prompting
                JComponent jc = statusBar.getPane(CONNECTING_INDICATOR);
                jc.getAccessibleContext().setAccessibleDescription(aMan.getString("IDS_STATUSBAR_CONNECTING_STATUS"));
                jc = statusBar.getPane(CONNECTING_STATUS);
                jc.getAccessibleContext().setAccessibleDescription(aMan.getString("IDS_STATUSBAR_CONNECTING_STATUS"));
                jc = statusBar.getPane(USER_NAME);
                jc.getAccessibleContext().setAccessibleDescription(aMan.getString("IDS_STATUSBAR_USER_NAME"));
                jc = statusBar.getPane(SERVER_NAME);
                jc.getAccessibleContext().setAccessibleDescription(aMan.getString("IDS_STATUSBAR_SERVER_NAME"));
                jc = statusBar.getPane(SERVER_TIME);
                jc.getAccessibleContext().setAccessibleDescription(aMan.getString("IDS_STATUSBAR_SERVER_TIME"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //if not at disconnected state
        if (mCurStat != LiaisonStatus.DISCONNECTED) {
            updateStatusBar();
        }
    }

    /**
     * This method is called when critical error occurred. Connection is closed.
     *
     * @param aEx liaison exception
     */
    public void onCriticalError(LiaisonException aEx) {
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        JOptionPane.showMessageDialog(
                this,
                aEx.getLocalizedMessage(),
                resmng.getString("IDS_MAINFRAME_SHORT_TITLE"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method is called when status of liaison has changed.
     *
     * @param aStatus new setted liaison status
     */
    public void onLiaisonStatus(LiaisonStatus aStatus) {
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        if (aStatus.equals(LiaisonStatus.CONNECTING)) {
            //sets text in status bar
            StatusBar statusBar = getStatusBar();
            statusBar.setText(2, resmng.getString("IDS_STATE_CONNECTING"));

            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.CONNECTING);

            //sets current status
            mCurStat = LiaisonStatus.CONNECTING;

            //shows modal window with label Connecting: Please wait.
            mWaitDialog = createWaitDialog(resmng.getString("IDS_WAIT_CONNECTING"));
            mWaitDialog.setVisible(true);
        } else if (aStatus.equals(LiaisonStatus.READY)) {
            //Depending on previous state
            if (mCurStat.equals(LiaisonStatus.CONNECTING)) {
                //Previous state = Connecting
                StatusBar statusBar = getStatusBar();
                statusBar.setText(2, resmng.getString("IDS_STATE_CONNECTED"));
                //sets connection name
                String sConnectionName = TradeDesk.getInst().getConnectionName(); //USHIK 1/6/2004
                String database = TradeDesk.getInst().getDatabaseName();
                if (sConnectionName != null && database != null) {
                    statusBar.setText(4, sConnectionName + ":" + database);
                } else {
                    statusBar.setText(4, "");
                }
                String sUserName = TradeDesk.getInst().getUserName();
                if (sUserName != null) {
                    statusBar.setText(3, sUserName);
                } else {
                    statusBar.setText(3, "");
                }

                //Change menu bar from disconnected to connected
                setMenu(mConnectedMenu);
                setCurrentLanguage(mConnectedMenu);

                //starts timer
                mTimePane.startTimer();
                createInternalFrames();
                updateStatusBar();

                //Dispose Connecting modal dialog
                if (mWaitDialog != null) {
                    mWaitDialog.setVisible(false);
                    mWaitDialog.dispose();
                    mWaitDialog = null;
                    if (TradingServerSession.getInstance().getUserKind() == IFixDefs.FXCM_SESSION_TYPE_CUSTOMER) {
                        JOptionPane.showMessageDialog(this,
                                                      "This account is in read only mode.\nYou will be unable to place trades on this account.");
                    }
                }
            } else if (mCurStat.equals(LiaisonStatus.RECONNECTING)) {
                //Previous state = Reconnecting
                //sets text in status bar to Connected
                StatusBar statusBar = getStatusBar();
                statusBar.setText(2, resmng.getString("IDS_STATE_CONNECTED"));
            } else if (mCurStat.equals(LiaisonStatus.SENDING) || mCurStat.equals(LiaisonStatus.RECEIVING)) {
                //nothing
            }

            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.READY);

            //sets current status
            mCurStat = LiaisonStatus.READY;
        } else if (aStatus.equals(LiaisonStatus.RECONNECTING)) {
            //sets text in status bar
            StatusBar statusBar = getStatusBar();
            statusBar.setText(2, resmng.getString("IDS_STATE_RECONNECTING"));

            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.RECONNECTING);

            //sets current status
            mCurStat = LiaisonStatus.RECONNECTING;
        } else if (aStatus.equals(LiaisonStatus.DISCONNECTING)) {
            //sets text in status bar
            StatusBar statusBar = getStatusBar();
            statusBar.setText(2, resmng.getString("IDS_STATE_DISCONNECTING"));

            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.DISCONNECTING);

            //sets current status
            mCurStat = LiaisonStatus.DISCONNECTING;

            //shows modal window with label Disconnecting: Please wait
            mWaitDialog = createWaitDialog(resmng.getString("IDS_WAIT_DISCONNECTING"));
            mWaitDialog.setVisible(true);
        } else if (aStatus.equals(LiaisonStatus.SENDING)) {
            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.SENDING);

            //sets current status
            mCurStat = LiaisonStatus.SENDING;
        } else if (aStatus.equals(LiaisonStatus.RECEIVING)) {
            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.RECEIVING);

            //sets current status
            mCurStat = LiaisonStatus.RECEIVING;
        } else if (aStatus.equals(LiaisonStatus.DISCONNECTED)) {
            //gets child manager
            ChildManager childMan = getChildManager();

            //gets all child frames
            JInternalFrame[] frames = childMan.getAllFrames();

            //Get PersistenceStorage
            UserPreferences preferences;
            try {
                preferences = UserPreferences.getUserPreferences();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            //saving of z-order of child frames
            for (JInternalFrame frame : frames) {
                if (frame != null) {
                    ChildFrame childFrame = (ChildFrame) frame;
                    //saves z-order to storage
                    preferences.set("childframe." + childFrame.getName() + "z-position",
                                    childMan.getPosition(childFrame));
                }
            }
            //calls saveSettings() for all our child frames and disposes all child frames
            for (JInternalFrame frame : frames) {
                if (frame != null) {
                    ChildFrame childFrame = (ChildFrame) frame;
                    childFrame.saveSettings();
                    childFrame.dispose();
                    childMan.remove(childFrame);
                }
            }

            //sets autoarrange mode
            preferences.set("autoarrange", mAutoArrange);

            //sets text in status bar
            StatusBar statusBar = getStatusBar();
            statusBar.setText(2, resmng.getString("IDS_STATE_DISCONNECTED"));
            statusBar.setText(3, "");
            statusBar.setText(4, "");

            //dispose Disconnecting modal dialog
            if (mWaitDialog != null) {
                mWaitDialog.dispose();
                mWaitDialog = null;
            }

            //change menu bar to disconnected
            setMenu(mDisconnectedMenu);
            setCurrentLanguage(mDisconnectedMenu);

            //stops timer
            mTimePane.stopTimer();

            //sets status indicator
            mStatusIndicator.onLiaisonStatus(LiaisonStatus.DISCONNECTED);

            //sets current status
            mCurStat = LiaisonStatus.DISCONNECTED;
            if (mExitingRequired) {
                //disposes mainframe window
                dispose();
            }
        }
    }

    /**
     * This method is called when initiated login command has completed successfully.
     */
    public void onLoginCompleted() {
        updateTitle();
        UserPreferences pref = UserPreferences.getUserPreferences();
        if (mTitleObserver == null) {
            mTitleObserver = new Observer() {
                public void update(Observable aObservable, Object arg) {
                    if (arg.toString().contains(IClickModel.TRADING_MODE)) {
                        updateTitle();
                    }
                }
            };
        }
        pref.addObserver(mTitleObserver);
    }

    /**
     * This method is called when initiated login command has failed. aEx
     * contains information about error.
     * param aEx
     */
    public void onLoginFailed(LiaisonException aEx) {
        aEx.printStackTrace();
    }

    /**
     * This method is called when initiated logout command has completed.
     */
    public void onLogoutCompleted() {
        setTitle(getLocalizedTitle(TradeApp.getInst().getResourceManager()));
        UserPreferences pref = UserPreferences.getUserPreferences();
        pref.deleteObserver(mTitleObserver);
    }

    /**
     * Sets tile ordering.
     */
    protected void setCascadeOrdering() {
        //gets desktop pane sizes
        int totalWidth = getChildManager().getWidth();
        int totalHeight = getChildManager().getHeight();

        //gets all child frames
        JInternalFrame[] frames = getChildManager().getAllFrames();

        //gets count of visible window
        int iCount = 0;
        for (int i = frames.length - 1; i >= 0; i--) {
            if (frames[i] != null) {
                JInternalFrame frame = frames[i];
                if (frame.isVisible()) {
                    iCount++;
                }
            }
        }
        //calcules sizes
        double shiftValue = 25;
        double desktopClearty = 0.9;
        int wndWidth = (int) (totalWidth * desktopClearty - shiftValue * (iCount - 1));
        int wndHeight = (int) (totalHeight * desktopClearty - shiftValue * (iCount - 1));

        //sets position and sizes not selected frame
        int positionX = 0;
        int positionY = 0;
        for (int i = frames.length - 1; i >= 0; i--) {
            if (frames[i] != null) {
                JInternalFrame frame = frames[i];
                if (frame.isVisible()) {
                    if (frame.isMaximum()) {
                        try {
                            frame.setMaximum(false);
                        } catch (PropertyVetoException ex) {
                            //
                        }
                    }
                    frame.setSize(wndWidth, wndHeight);
                    frame.setLocation(positionX, positionY);
                    frame.toFront();
                    if (i == 0) {
                        //sets selecting of upper frame
                        try {
                            frame.setSelected(true);
                        } catch (PropertyVetoException e) {
                            e.printStackTrace();
                        }
                    } else {
                        positionX += shiftValue;
                        positionY += shiftValue;
                    }
                }
            }
        }
    }

    /**
     * Sets radiobutton to selected state in apropriate with current language.
     *
     * @param aMenuBar menu bar of main frame
     */
    private void setCurrentLanguage(JMenuBar aMenuBar) {
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        Locale curLocale = resmng.getLocale();

        //if menu bar not created
        if (aMenuBar == null) {
            return;
        }
        JMenu menu = findMenu("File");

        //if first menu not initialised
        if (menu == null) {
            return;
        }
        JMenu submenu = (JMenu) findMenuItem("Language", menu);

        //if submenu not initialised
        if (submenu == null) {
            return;
        }
        for (int i = 0; i < submenu.getItemCount(); i++) {
            JMenuItem item = submenu.getItem(i);
            if (item == null) {
                continue;
            }
            Locale locale = (Locale) item.getClientProperty(IFXTSConstants.LOCALE_KEY);
            if (locale.getISO3Language().equals(curLocale.getISO3Language())
                && locale.getISO3Country().equals(curLocale.getISO3Country())) {
                //set item to checked state
                item.setSelected(true);
            }
        }
    }

    /**
     * Sets tile ordering.
     */
    public void setTileOrdering() {
        //gets child manager
        ChildManager childMan = getChildManager();
        JInternalFrame ratesFrame = childMan.findFrameByName(RatesFrame.NAME);
        JInternalFrame accountFrame = childMan.findFrameByName(AccountsFrame.NAME);
        JInternalFrame summaryFrame = childMan.findFrameByName(SummaryFrame.NAME);
        JInternalFrame ordersFrame = childMan.findFrameByName(OrdersFrame.NAME);
        JInternalFrame openPositionsFrame = childMan.findFrameByName(OpenPositionsFrame.NAME);
        JInternalFrame closedPositionsFrame = childMan.findFrameByName(ClosedPositionsFrame.NAME);
        JInternalFrame messagesFrame = childMan.findFrameByName(MessagesFrame.NAME);

        //sets total values
        double ratesWidth = 0.5;
        double totalWidth = ratesWidth;
        double ratesHeight = 1.0;
        double totalHeight = ratesHeight;
        double openPositionsHeight1 = 0.60;
        if (openPositionsFrame != null && openPositionsFrame.isVisible()) {
            totalHeight += openPositionsHeight1;
        }
        double closedPositionsHeight1 = 0.40;
        if (closedPositionsFrame != null && closedPositionsFrame.isVisible()) {
            totalHeight += closedPositionsHeight1;
        }
        double messagesHeight1 = 0.40;
        if (messagesFrame != null && messagesFrame.isVisible()) {
            totalHeight += messagesHeight1;
        }
        double accountsWidth = 0.5;
        if (accountFrame != null && accountFrame.isVisible()
            || ordersFrame != null && ordersFrame.isVisible()
            || summaryFrame != null && summaryFrame.isVisible()) {
            totalWidth += accountsWidth;
        }
        int visibleMiniFrames = 0;
        int miniFrameCount = 0;
        int summaryFrameMode = 4;
        if (summaryFrame != null && summaryFrame.isVisible()) {
            visibleMiniFrames |= summaryFrameMode;
            miniFrameCount++;
        }
        int accountsFrameMode = 2;
        if (accountFrame != null && accountFrame.isVisible()) {
            visibleMiniFrames |= accountsFrameMode;
            miniFrameCount++;
        }
        int ordersFrameMode = 1;
        if (ordersFrame != null && ordersFrame.isVisible()) {
            visibleMiniFrames |= ordersFrameMode;
            miniFrameCount++;
        }
        if (messagesFrame != null && messagesFrame.isVisible()) {
            visibleMiniFrames |= ordersFrameMode;
            miniFrameCount++;
        }
        switch (miniFrameCount) {
            case 0:
                if (isFrameVisible(visibleMiniFrames, ordersFrameMode)) {
                    mOrdersHeight = 0;
                }
                if (isFrameVisible(visibleMiniFrames, accountsFrameMode)) {
                    mAccountsHeight = 0;
                }
                if (isFrameVisible(visibleMiniFrames, summaryFrameMode)) {
                    mSummaryHeight = 0;
                }
                break;
            case 1:
                if (isFrameVisible(visibleMiniFrames, ordersFrameMode)) {
                    mOrdersHeight = 1.0;
                }
                if (isFrameVisible(visibleMiniFrames, accountsFrameMode)) {
                    mAccountsHeight = 1.0;
                }
                if (isFrameVisible(visibleMiniFrames, summaryFrameMode)) {
                    mSummaryHeight = 1.0;
                }
                break;
            case 2:
                if (isFrameVisible(visibleMiniFrames, ordersFrameMode)) {
                    mOrdersHeight = 0.5;
                }
                if (isFrameVisible(visibleMiniFrames, accountsFrameMode)) {
                    mAccountsHeight = 0.5;
                }
                if (isFrameVisible(visibleMiniFrames, summaryFrameMode)) {
                    mSummaryHeight = 0.5;
                }
                break;
            case 3:
                if (isFrameVisible(visibleMiniFrames, ordersFrameMode)) {
                    mOrdersHeight = 0.34;
                }
                if (isFrameVisible(visibleMiniFrames, accountsFrameMode)) {
                    mAccountsHeight = 0.33;
                }
                if (isFrameVisible(visibleMiniFrames, summaryFrameMode)) {
                    mSummaryHeight = 0.33;
                }
                break;
            case 4:
                if (isFrameVisible(visibleMiniFrames, ordersFrameMode)) {
                    mOrdersHeight = 0.34;
                }
                if (isFrameVisible(visibleMiniFrames, accountsFrameMode)) {
                    mAccountsHeight = 0.33;
                }
                if (isFrameVisible(visibleMiniFrames, summaryFrameMode)) {
                    mSummaryHeight = 0.33;
                }
                break;
        }
        //gets desktop pane sizes
        int absTotalHeight = (int) (childMan.getHeight() / totalHeight);
        int absTotalWidth = (int) (childMan.getWidth() / totalWidth);

        //sets position and sizes
        int rateHeight = (int) (ratesHeight * absTotalHeight);
        int rateWidth = (int) (ratesWidth * absTotalWidth);
        if (isFrameInvisible(ratesFrame)) {
            rateWidth = 0;
        }
        int openPositionsHeight = (int) (openPositionsHeight1 * absTotalHeight);
        double openPositionsWidth1 = 1.0;
        int openPositionsWidth = (int) (openPositionsWidth1 * childMan.getWidth());
        int accountHeight = (int) (mAccountsHeight * absTotalHeight);
        int accountWidth = (int) (accountsWidth * absTotalWidth);
        int summaryHeight = (int) (mSummaryHeight * absTotalHeight);
        double summaryWidth1 = 0.5;
        int summaryWidth = (int) (summaryWidth1 * absTotalWidth);
        int orderHeight = (int) (mOrdersHeight * absTotalHeight);
        double ordersWidth = 0.5;
        int orderWidth = (int) (ordersWidth * absTotalWidth);
        int closedPositionsHeight = (int) (closedPositionsHeight1 * absTotalHeight);
        double closedPositionsWidth1 = 1.0;
        int closedPositionsWidth = (int) (closedPositionsWidth1 * childMan.getWidth());
        int messagesHeight = (int) (messagesHeight1 * absTotalHeight);
        double messagesWidth1 = 1.0;
        int messagesWidth = (int) (messagesWidth1 * childMan.getWidth());
        if (ratesFrame != null) {
            if (ratesFrame.isMaximum()) {
                try {
                    ratesFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            ratesFrame.setBounds(0, 0, rateWidth, rateHeight);
        }
        if (accountFrame != null) {
            if (accountFrame.isMaximum()) {
                try {
                    accountFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            if (isFrameInvisible(ratesFrame)) {
                accountWidth = (int) closedPositionsWidth1 * childMan.getWidth();
            }
            accountFrame.setBounds(rateWidth, 0, accountWidth, accountHeight);
        }
        if (summaryFrame != null) {
            if (summaryFrame.isMaximum()) {
                try {
                    summaryFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            int y = 0;
            if (accountFrame.isVisible()) {
                y += accountHeight;
            }
            if (isFrameInvisible(ratesFrame)) {
                summaryWidth = (int) closedPositionsWidth1 * childMan.getWidth();
            }
            summaryFrame.setBounds(rateWidth, y, summaryWidth, summaryHeight);
        }
        if (ordersFrame != null) {
            if (ordersFrame.isMaximum()) {
                try {
                    ordersFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            int y = 0;
            if (accountFrame.isVisible()) {
                y += accountHeight;
            }
            if (summaryFrame.isVisible()) {
                y += summaryHeight;
            }
            if (isFrameInvisible(ratesFrame)) {
                orderWidth = (int) closedPositionsWidth1 * childMan.getWidth();
            }
            ordersFrame.setBounds(rateWidth, y, orderWidth, orderHeight);
        }
        if (openPositionsFrame != null) {
            if (openPositionsFrame.isMaximum()) {
                try {
                    openPositionsFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            openPositionsFrame.setBounds(0, rateHeight, openPositionsWidth, openPositionsHeight);
        }
        if (closedPositionsFrame != null) {
            if (closedPositionsFrame.isMaximum()) {
                try {
                    closedPositionsFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            int y = rateHeight;
            if (openPositionsFrame.isVisible()) {
                y += openPositionsHeight;
            }
            closedPositionsFrame.setBounds(0, y, closedPositionsWidth, closedPositionsHeight);
        }
        if (messagesFrame != null) {
            if (messagesFrame.isMaximum()) {
                try {
                    messagesFrame.setMaximum(false);
                } catch (PropertyVetoException ex) {
                    //swallow
                }
            }
            int y = rateHeight;
            if (openPositionsFrame.isVisible()) {
                y += openPositionsHeight;
            }
            if (closedPositionsFrame.isVisible()) {
                y += closedPositionsHeight;
            }
            messagesFrame.setBounds(0, y, messagesWidth, messagesHeight);
        }
    }

    /**
     * Shows confirmation dialog.
     *
     * @param asMessage the message to display
     * @param asTitle the title string for the dialog
     * @param aiMessageType message type
     *
     * @return confirmation dialog
     */
    public int showConfirmationDlg(String asMessage, String asTitle, int aiMessageType) {
        //get resouce manager
        ResourceManager resmng = TradeApp.getInst().getResourceManager();
        //load localized labels
        String[] arsLabels = new String[2];
        arsLabels[0] = resmng.getString("IDS_YES_BUTTON_LABEL");
        arsLabels[1] = resmng.getString("IDS_NO_BUTTON_LABEL");
        JOptionPane pane = new JOptionPane(asMessage, aiMessageType, JOptionPane.YES_NO_OPTION, null, arsLabels, null);
        pane.setInitialValue(null);
        JDialog dialog = pane.createDialog(this, asTitle);
        pane.selectInitialValue();
        dialog.setVisible(true);
        Object selectedValue = pane.getValue();
        if (selectedValue == null) {
            return JOptionPane.CLOSED_OPTION;
        }
        for (int counter = 0, maxCounter = arsLabels.length; counter < maxCounter; counter++) {
            if (arsLabels[counter].equals(selectedValue)) {
                return counter;
            }
        }
        return JOptionPane.CLOSED_OPTION;
    }

    /**
     * Shows a modal dialog and adds it to collections of shown dialogs.
     *
     * @param aDialog dialog for showing
     *
     * @return show the dialogs status
     */
    public int showDialog(ABaseDialog aDialog) {
        return aDialog.showModal();
    }

    /**
     * Updates width of status bar`s panels.
     */
    protected void updateStatusBar() {
        StatusBar statusBar = getStatusBar();
        try {
            for (int i = 2; i < 5; i++) {
                JComponent comp = statusBar.getPane(i);
                Dimension oldSize = comp.getSize();
                String sText = statusBar.getText(i);
                if (sText != null) {
                    FontMetrics fm = comp.getGraphics().getFontMetrics(comp.getFont());
                    int iWidth = fm.stringWidth(sText);
                    if (i == 2) {
                        iWidth *= 1.5;
                    }
                    comp.setSize(new Dimension(iWidth + 15, (int) oldSize.getHeight()));
                    comp.setPreferredSize(new Dimension(iWidth + 15, (int) oldSize.getHeight()));
                }
            }
        } catch (Exception e) {
            //
        }
    }

    public void updateTitle() {
        StringBuffer titleBuffer = new StringBuffer(getLocalizedTitle(TradeApp.getInst().getResourceManager()));
        UserPreferences prefs = UserPreferences.getUserPreferences();
        String mode = prefs.getString(IClickModel.TRADING_MODE);
        if (IClickModel.SINGLE_CLICK.equals(mode)) {
            titleBuffer.append(" ~~~~~~ ONE CLICK TRADING ~~~~~~");
        } else if (IClickModel.DOUBLE_CLICK.equals(mode)) {
            titleBuffer.append(" ~~~~~~ DOUBLE CLICK TRADING ~~~~~~");
        }
        setTitle(titleBuffer.toString());
    }

    private static class ChartAction extends AbstractAction {
        private String msUrl;

        ChartAction(String asUrl) {
            msUrl = asUrl;
        }

        public void actionPerformed(ActionEvent aEvent) {
            StringTokenizer st = new StringTokenizer(msUrl, "$", true);
            StringBuffer url = new StringBuffer();
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                StringBuffer sb = new StringBuffer();
                sb.append(token);
                if ("$".equals(token) && st.hasMoreTokens()) {
                    String name = st.nextToken();
                    sb.append(name);
                    if (st.hasMoreTokens()) {
                        token = st.nextToken();
                        sb.append(token);
                        if ("$".equals(token)) {
                            if ("pair".equalsIgnoreCase(name)) {
                                String pair;
                                if (TradeApp.getInst().getRatesFrame().getSelectedCurrency() != null) {
                                    pair = TradeApp.getInst().getRatesFrame().getSelectedCurrency();
                                } else {
                                    pair = "EUR/USD";
                                }
                                url.append(pair);
                                continue;
                            }
                        }
                    }
                }
                url.append(sb.toString());
            }
            if (!BrowserLauncher.showDocument(url.toString())) {
                System.out.println("Not launched by JNLP");
                try {
                    BrowserLauncher.openURL(url.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class ChartsActionListener implements ActionListener {
        private Component mComponent;

        ChartsActionListener(Component aComponent) {
            mComponent = aComponent;
        }

        public void actionPerformed(ActionEvent aEvent) {
            Rectangle bounds = mComponent.getBounds();
            if (mChartsButtonPopupMenu != null) {
                //System.out.println("bounds = " + bounds);
                mChartsButtonPopupMenu.show(mComponent, 0, bounds.height);
            }
        }
    }
}
