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
 * 05/17/2007   Andre Mermegas: preload for ui perf
 *
 */
package fxts.stations.util.preferences;

import fxts.stations.trader.TradeApp;
import fxts.stations.ui.UIManager;
import fxts.stations.util.Password;
import fxts.stations.util.UserPreferences;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import uk.co.wilson.xml.MinML;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

/**
 * Singleton class that responsible for create Preferences Dialog and
 * filling it by tree-structure and related tables.
 */
public class PreferencesManager {
    private static PreferencesManager cInstance;
    private PreferencesMainPanel mDialogPanel;
    private final List mListenersList;
    private final Log mLogger = LogFactory.getLog(PreferencesManager.class);
    private String mUserName;

    /**
     * Protected constructor.
     */
    protected PreferencesManager(String aUserName) {
        mUserName = aUserName;
        mListenersList = new ArrayList();
    }

    /**
     * addPreferencesListener
     *
     * @param aListener listener that add in array
     */
    public void addPreferencesListener(IUserPreferencesListener aListener) {
        mListenersList.add(aListener);
    }

    /**
     * remove listener
     * @param aListener
     */
    public void removePreferencesListener(IUserPreferencesListener aListener) {
        mListenersList.remove(aListener);
    }

    protected void firePreferencesUpdated(Vector aUpdates) {
        synchronized (mListenersList) {
            for (Object listenersList : mListenersList) {
                ((IUserPreferencesListener) listenersList).preferencesUpdated(aUpdates);
            }
        }
    }

    /**
     * Get instance of PreferencesManager. Really constructor
     */
    public static PreferencesManager getPreferencesManager(String aUserName) {
        if (cInstance == null || !aUserName.equals(cInstance.getUserName())) {
            if (cInstance != null && !aUserName.equals(cInstance.getUserName())) {
                cInstance.mListenersList.clear();
            }
            cInstance = new PreferencesManager(aUserName);
            cInstance.addPreferencesListener(UserPreferences.getUserPreferences(aUserName));
        }
        return cInstance;
    }

    /**
     * Returns current user name
     */
    public String getUserName() {
        return mUserName;
    }

    public void init() {
        if (mDialogPanel == null) {
            mDialogPanel = new PreferencesMainPanel(mUserName);
            parseXML("fxts/stations/util/preferences/PropertiesTree.xml");
        }
    }

    /**
     * Parses xml file to load contents.
     *
     * @param aUrl url to xml-file
     */
    private void parseXML(String aUrl) {
        XmlParser parser = new XmlParser();
        ClassLoader classLoader = getClass().getClassLoader();
        URL url = classLoader.getResource(aUrl);
        if (url == null) {
            mLogger.debug("Xml-file by url = \"" + aUrl + "\" not found!");
            return;
        }
        try {
            InputStream istream = url.openStream();
            parser.parse(new InputStreamReader(istream));
        } catch (Exception e) {
            mLogger.debug("Not parsed xml-file with contents by url = " + aUrl, e);
        }
    }

    public void showPreferencesDialog() {
        init();
        SwingUtilities.updateComponentTreeUI(mDialogPanel);
        PreferencesDialog dlg = new PreferencesDialog(TradeApp.getInst().getMainFrame(), mDialogPanel, mUserName);
        dlg.showModal();
    }

    /**
     * MinML based parser.
     */
    private class XmlParser extends MinML implements PreferencesConstants {
        private PreferencesNode mCurrentNode;
        private DefaultMutableTreeNode mRootNode;
        private Stack mStack = new Stack();
        private UserPreferences mUserPreferences = UserPreferences.getUserPreferences(mUserName);

        /**
         * Actions on end of parsing of element.
         */
        public void endElement(String aName) throws SAXException {
            //To save stack pointer on previous node
            if (!"Node".equals(aName)) {
                return;
            }
            try {
                //node already processed
                mStack.pop();
            } catch (EmptyStackException e) {
                e.printStackTrace();
            }
        }

        /**
         * Returns root node.
         */
        public DefaultMutableTreeNode getRootNode() {
            return mRootNode;
        }

        /**
         * Actions on reset of parsing.
         */
        public void reset() {
            mRootNode = null;
            while (!mStack.empty()) {
                mStack.pop();
            }
        }

        /**
         * Actions on begin of parsing of element.
         */
        public void startElement(String aName, AttributeList aAttributes) throws SAXException {
            PropertiesSheet sheet;
            String nameAttr = null;
            String idAttr = null;
            String typeAttr = null;
            if ("PropertiesTree".equals(aName)) {
                if (mRootNode != null) {
                    throw new SAXException("Invalid XML. More then one elements with name \"ContentTree\".");
                }
                //creates illusory root node. Real root node created in PreferencesTreePanel
                mRootNode = new DefaultMutableTreeNode("Root");
                //add to stack (just pointer to current node)
                mStack.push(mRootNode);
            } else if ("Node".equals(aName)) {
                if (mRootNode == null) {
                    throw new SAXException("Invalid XML. Not found element with name \"ContentTree\".");
                }
                for (int i = 0; i < aAttributes.getLength(); ++i) {
                    if ("name".equals(aAttributes.getName(i))) {
                        nameAttr = aAttributes.getValue(i);
                        break;
                    }
                }
                if (nameAttr == null) {
                    throw new SAXException("Invalid XML. Not specified obligatory argument \"name\"!");
                }
                //if it is a root (second order root)
                if (mStack.peek() == mRootNode) {
                    mCurrentNode = mDialogPanel.getRootNode(nameAttr);
                } else {
                    mCurrentNode = mDialogPanel.addNode((PreferencesNode) mStack.peek(), nameAttr);
                }
                //sets property sheet to node
                sheet = new PropertiesSheet();
                mCurrentNode.setSheet(sheet);

                //add to stack
                mStack.push(mCurrentNode);
            } else if ("Property".equals(aName)) {
                if (mStack.empty()) {
                    throw new SAXException("Property cannot be bind with any node.");
                }
                for (int i = 0; i < aAttributes.getLength(); ++i) {
                    if ("id".equals(aAttributes.getName(i))) {
                        idAttr = aAttributes.getValue(i);
                    } else if ("name".equals(aAttributes.getName(i))) {
                        nameAttr = aAttributes.getValue(i);
                    } else if ("type".equals(aAttributes.getName(i))) {
                        typeAttr = aAttributes.getValue(i);
                    }
                }
                if (nameAttr == null) {
                    throw new SAXException("Invalid XML. Not specified obligatory argument \"name\"!");
                }
                if (typeAttr == null) {
                    throw new SAXException("Invalid XML. Not specified obligatory argument \"type\"!");
                }
                if (idAttr == null) {
                    //not need sheet for this element
                } else {
                    //adds of property to node
                    sheet = mCurrentNode.getSheet();
                    //next reaction is depended from type of property
                    try {
                        if (typeAttr.equals(COLOR_PROPERTY_NAME)) {
                            sheet.add(idAttr, nameAttr, mUserPreferences.getColor(idAttr));
                        } else if (typeAttr.equals(FONT_PROPERTY_NAME)) {
                            JLabel jl = UIManager.getInst().createLabel("test");
                            sheet.add(idAttr, nameAttr, mUserPreferences.getFont(idAttr, jl.getFont()));
                        } else if (typeAttr.equals(INT_PROPERTY_NAME)) {
                            sheet.add(idAttr, nameAttr, mUserPreferences.getInt(idAttr));
                        } else if (typeAttr.equals(DOUBLE_PROPERTY_NAME)) {
                            sheet.add(idAttr, nameAttr, mUserPreferences.getDouble(idAttr));
                        } else if (typeAttr.equals(STRING_PROPERTY_NAME)) {
                            sheet.add(idAttr, nameAttr, mUserPreferences.getString(idAttr));
                        } else if (typeAttr.equals(PASSWORD_PROPERTY_NAME)) {
                            sheet.add(idAttr, nameAttr, new Password(mUserPreferences.getString(idAttr)));
                        } else if (typeAttr.equals(BOOLEAN_PROPERTY_NAME)) {
                            sheet.add(idAttr, nameAttr, mUserPreferences.getBoolean(idAttr));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                throw new SAXException("Invalid XML. Not correct name of element: "
                                       + aName
                                       + " (Must be \"Node\" , \"Property\" or \"PropertiesTree\").");
            }
        }
    }
}
