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
 */
package fxts.stations.ui.help;

import fxts.stations.trader.TradeApp;
import fxts.stations.ui.UIManager;
import fxts.stations.ui.WeakHTMLEditorKit;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * This class incapsulated panel with components for showing of help.
 * It class was separated at force that help may be as dialog-based as frame-based.
 */
public class HelpPane extends JPanel implements IContentSelectionListener, ILocaleListener {
    private final Log mLogger = LogFactory.getLog(HelpPane.class);
    /**
     * Button for browsing by history to back direction.
     */
    private JButton mBackButton;
    /**
     * Tree with contents of help.
     */
    private ContentTree mContentTree;
    /**
     * Buttons for moving by contents to down direction.
     */
    private JButton mDownButton;
    /**
     * Button for browsing by history to forward direction.
     */
    private JButton mForwardButton;
    /**
     * History of browsing by help.
     */
    private HelpContentHistory mHistory;
    /**
     * Page with help.
     */
    private JEditorPane mHtmlPage;
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;
    /**
     * Splitter panel.
     */
    private JSplitPane mSplitPane;
    /**
     * Tabbed panel.
     */
    private JTabbedPane mTabbedPane;
    /**
     * Buttons for moving by contents to up direction.
     */
    private JButton mUpButton;
    /**
     * Sinchonizing of the contents mode.
     */
    private boolean mContentSinchronize;
    /**
     * Browsing by contents mode.
     */
    private boolean mContentsBrowsing;
    /**
     * Step at history or no?
     */
    private boolean mIsHistorycalStep;
    /**
     * Is switch on repaint mode? (For change locale.)
     */
    private boolean mRepaintMode;
    /**
     * Identifier of last opened page.
     */
    private String mCurrentPageId;

    /**
     * Constructor.
     */
    public HelpPane() {
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
        } catch (Exception e) {
            mLogger.error("Resource manager not loaded.");
            e.printStackTrace();
        }
        initComponents();
    }

    /**
     * Returns devider position.
     */
    public int getDeviderPosition() {
        //gets splitter position
        return mSplitPane.getDividerLocation();
    }

    /**
     * Finds short url at full url.
     *
     * @param aUrl specified url
     */
    private String getShortUrl(URL aUrl) {
        int iPrefixInd;
        int iPostfixInd;
        String sFullUrl;
        String sPrefix;
        String sShortUrl;
        sFullUrl = aUrl.toString();
        if (sFullUrl == null || "".equals(sFullUrl)) {
            return null;
        }
        sPrefix = mContentTree.getHelpPrefix();
        if (sPrefix == null) {
            sPrefix = "";
        }
        sPrefix += "/";
        sPrefix += mResMan.getLocale().toString();
        sPrefix += "/";
        iPrefixInd = sFullUrl.lastIndexOf(sPrefix);
        iPostfixInd = sFullUrl.lastIndexOf("#");
        if (iPrefixInd == -1 || iPostfixInd == -1) {
            return null;
        }
        sShortUrl = sFullUrl.substring(iPrefixInd + sPrefix.length(), iPostfixInd);
        return sShortUrl;
    }

    /**
     * Inits all components.
     */
    private void initComponents() {
        //creates history
        mHistory = new HelpContentHistory();
        //Create the text area for contents
        mTabbedPane = new JTabbedPane();

        //creates content tree
        mContentTree = new ContentTree("fxts/stations/trader/resources/help/contents.xml");
        mContentTree.addListener(this);

        //Create the scroll pane and add the tree to it.
        JScrollPane treeView = new JScrollPane(mContentTree.getTree());
        mTabbedPane.addTab(mResMan.getString("IDS_HELP_CONTENTS", "Contents"), treeView);

        //xxx workaround for bug #6424509, memory leak
        JEditorPane.registerEditorKitForContentType("text/html", WeakHTMLEditorKit.class.getName());
        //creates the text area for the showing of the help.
        mHtmlPage = new JEditorPane();
        mHtmlPage.setEditable(false);
        mHtmlPage.putClientProperty("charset", "UTF-16");
        mHtmlPage.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent aEvent) {
                if (aEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        onSelectContentByHyperlink(aEvent.getURL());
                        mHtmlPage.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    } catch (Exception e) {
                        mLogger.error("Hiperlink not processed!");
                        e.printStackTrace();
                    }
                }
            }
        });
       JScrollPane scrollPane = new JScrollPane(mHtmlPage);

        //creates a split pane for the change log and the text area.
        mSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mTabbedPane, scrollPane);
        mSplitPane.setOneTouchExpandable(true);

        //Creates the toolbar area.
        JToolBar toolbar = UIManager.getInst().createToolBar();
        toolbar.setFloatable(false);

        //creates label with left arrow
        UIManager uiMan = UIManager.getInst();
        mBackButton = uiMan.createButton(null, "ID_HELP_LEFT_ARROW",
                                                "ID_HELP_LEFT_ARROW_DESC", "ID_HELP_LEFT_ARROW_DESC");
        mBackButton.setEnabled(false);
        mBackButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (mHistory.hasBackStep()) {
                    mIsHistorycalStep = true;
                    onSelectContent(mHistory.back());
                    mBackButton.setEnabled(mHistory.hasBackStep());
                    mForwardButton.setEnabled(mHistory.hasForwardStep());
                }
            }
        });
        toolbar.add(mBackButton);

        //creates label with right arrow
        mForwardButton = uiMan.createButton(null, "ID_HELP_RIGHT_ARROW",
                                                   "ID_HELP_RIGHT_ARROW_DESC", "ID_HELP_RIGHT_ARROW_DESC");
        mForwardButton.setEnabled(false);
        mForwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (mHistory.hasForwardStep()) {
                    mIsHistorycalStep = true;
                    onSelectContent(mHistory.forward());
                    mBackButton.setEnabled(mHistory.hasBackStep());
                    mForwardButton.setEnabled(mHistory.hasForwardStep());
                }
            }
        });
        toolbar.add(mForwardButton);

        //creates label with up arrow
        mUpButton = uiMan.createButton(null, "ID_HELP_UP_ARROW",
                                              "ID_HELP_UP_ARROW_DESC", "ID_HELP_UP_ARROW_DESC");
        mUpButton.setEnabled(mContentTree.getIterator().hasPrevious());
        mUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                mContentsBrowsing = true;
                onSelectContent(mContentTree.getIterator().previous());
                mUpButton.setEnabled(mContentTree.getIterator().hasPrevious());
                mDownButton.setEnabled(mContentTree.getIterator().hasNext());
            }
        });
        toolbar.add(mUpButton);

        //creates label with down arrow
        mDownButton = uiMan.createButton(null, "ID_HELP_DOWN_ARROW",
                                                "ID_HELP_DOWN_ARROW_DESC", "ID_HELP_DOWN_ARROW_DESC");
        mDownButton.setEnabled(mContentTree.getIterator().hasNext());
        mDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                mContentsBrowsing = true;
                onSelectContent(mContentTree.getIterator().next());
                mUpButton.setEnabled(mContentTree.getIterator().hasPrevious());
                mDownButton.setEnabled(mContentTree.getIterator().hasNext());
            }
        });
        toolbar.add(mDownButton);

        //sets layout
        setLayout(new BorderLayout());

        //add the components to the frame.
        add(mSplitPane, BorderLayout.CENTER);
        add(toolbar, BorderLayout.NORTH);

        //sets first page
        onSelectContent(mContentTree.getIterator().toBegin());
    }

    /**
     * This method is called when current locale of the aMan is changed.
     * It`s a ILiaisonListener method.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        //set repaint mode
        mRepaintMode = true;

        //change tab`s name
        mTabbedPane.setTitleAt(0, mResMan.getString("IDS_HELP_CONTENTS", "Contents"));

        //reloads current page
        onSelectContent(mCurrentPageId);

        //repaints tree control
        mContentTree.getTree().repaint();
        mContentTree.getTree().updateUI();
    }

    /**
     * This method is called when new item selected at content tree
     *
     * @param aId identifer of the page.
     */
    public void onSelectContent(String aId) {
        //if synchronizing of contents mode
        if (mContentSinchronize) {
            mContentSinchronize = false;
            return;
        }

        //check id
        if (aId == null) {
            mLogger.debug("Zero id of page.");
            return;
        }
        if (!mRepaintMode) {
            if (aId.equals(mCurrentPageId)) {
                return;
            }
        }

        //gets url of page by id
        String sUrl = mContentTree.getPageUrl(aId);
        if (sUrl == null) {
            //System.out.println("URL for page with id = " + asId + " not fond");
            return;
        }

        //creates full url
        String sFullUrl = mContentTree.getHelpPrefix();
        if (sFullUrl == null) {
            sFullUrl = "";
        }
        sFullUrl += "/";
        sFullUrl += mResMan.getLocale().toString();
        sFullUrl += "/";
        sFullUrl += sUrl;

        //loads url
        URL url;
        try {
            url = getClass().getClassLoader().getResource(sFullUrl);
        } catch (Exception e) {
            url = null;
        }

        //if localized help not presence
        if (url == null) {
            //creates full url to help on default language
            sFullUrl = mContentTree.getHelpPrefix();
            if (sFullUrl == null) {
                sFullUrl = "";
            }
            sFullUrl += "/";
            sFullUrl += mResMan.getDefaultLocale().toString();
            sFullUrl += "/";
            sFullUrl += sUrl;

            //loads url to help on default language
            try {
                url = getClass().getClassLoader().getResource(sFullUrl);
            } catch (Exception e) {
                url = null;
            }

            //if help this page not presence even at default language
            if (url == null) {
                mLogger.debug("Page by url \"" + sFullUrl + "\" not found!");
                mHtmlPage.setText(mResMan.getString("IDS_HELP_WINDOW_NO_PAGE",
                                                    "No page with url = ") + sFullUrl);
                return;
            }
        }
        try {
            mHtmlPage.setPage(url);
            //sets last opened page
            mCurrentPageId = aId;

            //if browsing by contents
            if (mContentsBrowsing) {
                //sinchronize contenst
                JTree tree = mContentTree.getTree();
                TreePath rootPath = new TreePath(mContentTree.getTree().getModel().getRoot());
                TreePath path = mContentTree.findPathByUrl(rootPath, sUrl);
                if (path != null) {
                    mContentSinchronize = true;
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                } else {
                    mLogger.debug("Contents not synchronized!");
                }
                mContentsBrowsing = false;
            } else {
                //setting browising by contents
                mContentTree.getIterator().setCurrentByUrl(sUrl);
                mUpButton.setEnabled(mContentTree.getIterator().hasPrevious());
                mDownButton.setEnabled(mContentTree.getIterator().hasNext());
            }

            //saving at history
            if (mIsHistorycalStep) {
                mIsHistorycalStep = false;

                //sinchronize contenst
                JTree tree = mContentTree.getTree();
                TreePath rootPath = new TreePath(mContentTree.getTree().getModel().getRoot());
                TreePath path = mContentTree.findPathByUrl(rootPath, sUrl);
                if (path != null) {
                    mContentSinchronize = true;
                    tree.setSelectionPath(path);
                    tree.scrollPathToVisible(path);
                } else {
                    mLogger.debug("Contents not synchronized!");
                }
            } else {
                if (!mRepaintMode) {
                    mHistory.put(aId);
                    mBackButton.setEnabled(mHistory.hasBackStep());
                    mForwardButton.setEnabled(mHistory.hasForwardStep());
                } else {
                    mRepaintMode = false;
                }
            }
        } catch (Exception e) {
            mLogger.debug("Attempted to read a bad URL: " + url);
            mHtmlPage.setText(mResMan.getString("IDS_HELP_WINDOW_NO_PAGE",
                                                "No page with url:") + url.toString());
        }
    }

    /**
     * This method is called when hyperlink activated.
     *
     * @param aUrl url to page.
     */
    public void onSelectContentByHyperlink(URL aUrl) {
        if (aUrl == null) {
            mLogger.debug("Hyperlink is null!");
            return;
        }
        try {
            mHtmlPage.setPage(aUrl);
            String sUrl = getShortUrl(aUrl);
            if (sUrl != null) {
                //sinchronize contenst
                JTree tree = mContentTree.getTree();
                TreePath rootPath = new TreePath(mContentTree.getTree().getModel().getRoot());
                TreePath path = mContentTree.findPathByUrl(rootPath, sUrl);
                if (path != null) {
                    mContentSinchronize = true;
                    tree.setSelectionPath(path);
                } else {
                    //System.out.println("Contents not synchronized!");
                }

                //setting browising by contents
                mContentTree.getIterator().setCurrentByUrl(sUrl);
                mUpButton.setEnabled(mContentTree.getIterator().hasPrevious());
                mDownButton.setEnabled(mContentTree.getIterator().hasNext());

                //saving at history
                String sId = mContentTree.getPageId(sUrl);
                mHistory.put(sId);
                mBackButton.setEnabled(mHistory.hasBackStep());
                mForwardButton.setEnabled(mHistory.hasForwardStep());

                //sets last opened page
                mCurrentPageId = sId;
            } else {
                mLogger.debug("Short url not found for full url = " + aUrl.toString());
            }
        } catch (Exception e) {
            mLogger.error("Attempted to read a bad URL from hyperlink: " + aUrl);
            mHtmlPage.setText(mResMan.getString("IDS_HELP_WINDOW_NO_PAGE",
                                                "No page with url:") + aUrl.toString());
        }
    }

   /**
     * Sets position of splitter`d devider.
     */
    public void setDividerPosition(int aPosition) {
        //sets splitter position
        mSplitPane.setDividerLocation(aPosition);
    }
}
