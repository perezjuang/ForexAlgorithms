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
package fxts.stations.ui.help;

import fxts.stations.trader.TradeApp;
import fxts.stations.util.ResourceManager;
import fxts.stations.ui.UIManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import uk.co.wilson.xml.MinML;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * This clases respondes for creating tree-control and
 * initialising it`s by contents of help from specifies xml-file.
 */
public class ContentTree {
    private final Log mLogger = LogFactory.getLog(ContentTree.class);

    /* It`s class incapsulated through moving by contents. */
    protected class ContentsIterator {
        /* -- Data members -- */
        /**
         * Current node.
         */
        private int mCurrent = 0;

        /* -- Public methods -- */

        /**
         * Is next position at contents?
         */
        public boolean hasNext() {
            return mCurrent < mContents.size() - 1;
        }

        /**
         * Is previous position at contents?
         */
        public boolean hasPrevious() {
            return mCurrent > 0;
        }

        /**
         * Goes to the next position at contents and returns url.
         */
        public String next() {
            DefaultMutableTreeNode node;
            if (mCurrent < mContents.size() - 1) {
                node = (DefaultMutableTreeNode) mContents.get(++mCurrent);
                if (node != null) {
                    return ((NodeInfo) node.getUserObject()).getId();
                }
            }
            return null;
        }

        /**
         * Go to the previous position at contents.
         */
        public String previous() {
            DefaultMutableTreeNode node;
            if (mCurrent > 0) {
                node = (DefaultMutableTreeNode) mContents.get(--mCurrent);
                if (node != null) {
                    return ((NodeInfo) node.getUserObject()).getId();
                }
            }
            return null;
        }

        /**
         * Sets current node.
         *
         * @param aIndex index of node
         */
        public void setCurrent(int aIndex) {
            mCurrent = aIndex;
        }

        /**
         * Sets current node by ID.
         *
         * @param aID specified id
         */
        public void setCurrentByID(String aID) {
            DefaultMutableTreeNode node;
            for (int i = 0; i < mContents.size(); i++) {
                node = (DefaultMutableTreeNode) mContents.get(i);
                if (aID.equals(((NodeInfo) node.getUserObject()).getId())) {
                    mCurrent = i;
                    return;
                }
            }
        }

        /**
         * Sets current node by Url.
         *
         * @param aUrl specified url
         */
        public void setCurrentByUrl(String aUrl) {
            DefaultMutableTreeNode node;
            for (int i = 0; i < mContents.size(); i++) {
                node = (DefaultMutableTreeNode) mContents.get(i);
                if (aUrl.equals(((NodeInfo) node.getUserObject()).getUrl())) {
                    mCurrent = i;
                    return;
                }
            }
        }

        /**
         * Goes to begin of contents and returns url of first element.
         */
        public String toBegin() {
            DefaultMutableTreeNode node;
            if (!mContents.isEmpty()) {
                node = (DefaultMutableTreeNode) mContents.get(0);
                if (node != null) {
                    return ((NodeInfo) node.getUserObject()).getId();
                }
            }
            return null;
        }
    }

    /**
     * Information for seting to node.
     */
    private class NodeInfo {
        /* -- Data types -- */
        private String mId = null;
        private String mUrl = null;

        /**
         * Constructor.
         */
        public NodeInfo(String aId, String aUrl) {
            mId = aId;
            mUrl = aUrl;
        }

        /**
         * Returns identifier.
         */
        public String getId() {
            return mId;
        }

        /**
         * Returns url.
         */
        public String getUrl() {
            return mUrl;
        }

        /* Returns string representation of NodeInfo. */
        public String toString() {
            return mResMan.getString(mId, mId);
        }
    }

    /* -- Inner classes -- */

    /**
     * Realisation of TreeSelectionListener intarface.
     */
    private class SelectionAdapter implements TreeSelectionListener {
        private IContentSelectionListener mListener;

        /**
         * Constructor.
         */
        SelectionAdapter(IContentSelectionListener aListener) {
            mListener = aListener;
        }

        /**
         * Called whenever the value of the selection changes.
         */
        public void valueChanged(TreeSelectionEvent aEvent) {
            DefaultMutableTreeNode node;
            Object userObject;
            node = (DefaultMutableTreeNode) mTree.getLastSelectedPathComponent();
            if (node == null) {
                return;
            }
            userObject = node.getUserObject();
            if (userObject instanceof NodeInfo) {
                NodeInfo nodeInfo = (NodeInfo) userObject;
                if (nodeInfo.getId() != null) {
                    mListener.onSelectContent(nodeInfo.getId());
                }
            }
        }
    }

    /**
     * MimML based parser represents for parsing of xml-file with contents of help.
     */
    private class XmlParser extends MinML {
        /*-- Data members --*/
        /**
         * Current node.
         */
        private DefaultMutableTreeNode mCurrentNode = null;
        /**
         * Current root.
         */
        private DefaultMutableTreeNode mCurrentRoot = null;
        /**
         * Stack of nodes.
         */
        private Stack mStack = new Stack();
        /**
         * Node url prefix.
         */
        private String mNodeUrlPrefix = null;
        /**
         * Main root.
         */
        private DefaultMutableTreeNode mRootNode = null;

        /**
         * Invokes on end of parsing of new elements.
         *
         * @param aName name of element
         */
        public void endElement(String aName)
                throws SAXException {
            try {
                //pop from stack
                mStack.pop();
            } catch (EmptyStackException e) {
                mLogger.error("Stack is empty.");
                e.printStackTrace();
            }
        }

        /**
         * Returns node url prefix.
         */
        public String getNodeUrlPrefix() {
            return mNodeUrlPrefix;
        }

        /**
         * Returns root node.
         */
        public DefaultMutableTreeNode getRootNode() {
            return mRootNode;
        }

        /**
         * Resets state of parser.
         */
        public void reset() {
            mCurrentNode = null;
            mRootNode = null;
            mCurrentRoot = null;
            while (!mStack.empty()) {
                mStack.pop();
            }
        }

        /**
         * Invokes on begin of parsing of new elements.
         *
         * @param aName       name of element
         * @param aAttributes list of attributes
         */
        public void startElement(String aName, AttributeList aAttributes)
                throws SAXException {
            NodeInfo nodeInfo;
            String sId = null;
            String sUrl = null;
            if ("ContentTree".equals(aName)) {
                if (mRootNode != null) {
                    throw new SAXException("Invalid XML. More then one elements with name \"ContentTree\".");
                }
                for (int i = 0; i < aAttributes.getLength(); ++i) {
                    if ("NodeUrlPrefix".equals(aAttributes.getName(i))) {
                        mNodeUrlPrefix = aAttributes.getValue(i);
                    }
                }
                nodeInfo = new NodeInfo(mResMan.getString("IDS_HELP_CONTENTS", "Contents"), null);
                //creates root node
                mRootNode = new DefaultMutableTreeNode(nodeInfo);
                //add to stack
                mStack.push(mRootNode);
            } else {
                if (mRootNode == null) {
                    throw new SAXException("Invalid XML. Not found element with name \"ContentTree\".");
                }
                if (!"node".equals(aName)) {
                    throw new SAXException("Invalid XML. Not correct name of element: "
                                           + aName
                                           + " (Must be \"node\" or \"ContentTree\").");
                }
                for (int i = 0; i < aAttributes.getLength(); ++i) {
                    if ("id".equals(aAttributes.getName(i))) {
                        sId = aAttributes.getValue(i);
                    } else if ("url".equals(aAttributes.getName(i))) {
                        sUrl = aAttributes.getValue(i);
                    }
                }
                if (sId == null) {
                    throw new SAXException("Invalid XML. Not specified obligatory argument \"id\"!");
                }
                if (sUrl != null && "".equals(sUrl)) {
                    sUrl = null;
                }
                nodeInfo = new NodeInfo(sId, sUrl);
                mCurrentNode = new DefaultMutableTreeNode(nodeInfo);

                //adds node to tree
                mCurrentRoot = (DefaultMutableTreeNode) mStack.peek();
                mCurrentRoot.add(mCurrentNode);
                if (sUrl != null) {
                    //adds to vector
                    mContents.add(mCurrentNode);
                    //adds to hashtables
                    mId2UrlMap.put(sId, sUrl);
                    if (!mUrl2IdMap.containsKey(sUrl)) {
                        mUrl2IdMap.put(sUrl, sId);
                    }
                }
                //add to stack
                mStack.push(mCurrentNode);
            }
        }
    }

    /**
     * Hash table with identifiers and URLs.
     */
    private Hashtable mId2UrlMap = new Hashtable();
    /**
     * Iterator by contents.
     */
    private ContentsIterator mIterator = null;
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;

    /* -- Data members -- */
    /**
     * Instance of tree.
     */
    private JTree mTree;
    /**
     * Hash table with URLs and identifiers.
     */
    private Hashtable mUrl2IdMap = new Hashtable();
    /**
     * Contents vector.
     */
    private Vector mContents = new Vector();
    /**
     * Path to root of help.
     */
    private String mHelpPrefix = null;

    /* -- Constructor -- */

    /**
     * Constructor.
     *
     * @param aUrl url to contents
     */
    public ContentTree(String aUrl) {
        DefaultMutableTreeNode topNode;

        //gets resource manager
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
        } catch (Exception e) {
            mLogger.error("Not opened ResourceManager!");
            e.printStackTrace();
        }

        //create tree
        topNode = parseXML(aUrl);
        if (topNode == null) {
            topNode = new DefaultMutableTreeNode(new NodeInfo(
                    mResMan.getString("IDS_HELP_CONTENTS", "Contents"), null));
        }
        mTree = UIManager.getInst().createTree(topNode);
        //sets root invisible
        mTree.setRootVisible(false);
        //fires expanding of top nodes
        expandTopNodes();

        //creates iterator
        mIterator = new ContentsIterator();

        //sets styles
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Optionally play with line styles.  Possible values are
        //"Angled", "Horizontal", and "None" (the default).
        mTree.putClientProperty("JTree.lineStyle", "Angled");
    }

    /* -- Public methods -- */

    /**
     * Adds content selection listener .
     *
     * @param aListener listener of content selection
     */
    public void addListener(IContentSelectionListener aListener) {
        mTree.addTreeSelectionListener(new SelectionAdapter(aListener));
    }

    /**
     * Fires expanding of top nodes.
     */
    private void expandTopNodes() {
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode root;
        TreePath path;
        TreePath rootPath;
        root = (DefaultMutableTreeNode) mTree.getModel().getRoot();
        rootPath = new TreePath(root);
        for (Enumeration enumeration = root.children(); enumeration.hasMoreElements();) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();

            //increase path
            path = rootPath.pathByAddingChild(node);
            //expands node
            mTree.expandPath(path);
        }
    }

    /**
     * Returns node containing the specified url.
     *
     * @param aParent parent node
     * @param aUrl    specified url
     *
     * @return node containing specified url
     */
    public DefaultMutableTreeNode findNodeByUrl(DefaultMutableTreeNode aParent, String aUrl) {
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode result;
        NodeInfo info;
        if (!aParent.getAllowsChildren()) {
            return null;
        }
        for (Enumeration enumeration = aParent.children(); enumeration.hasMoreElements();) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();
            info = (NodeInfo) node.getUserObject();
            if (aUrl.equals(info.getUrl())) {
                return node;
            } else {
                result = findNodeByUrl(node, aUrl);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Returns path to node containing the specified url.
     *
     * @param aParentPath parent node
     * @param aUrl        specified url
     *
     * @return path to node containing specified url
     */
    public TreePath findPathByUrl(TreePath aParentPath, String aUrl) {
        DefaultMutableTreeNode parentNode;
        DefaultMutableTreeNode node;
        TreePath resultPath;
        NodeInfo info;
        parentNode = (DefaultMutableTreeNode) aParentPath.getLastPathComponent();
        if (!parentNode.getAllowsChildren()) {
            return null;
        }
        for (Enumeration enumeration = parentNode.children(); enumeration.hasMoreElements();) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();
            info = (NodeInfo) node.getUserObject();
            if (aUrl.equals(info.getUrl())) {
                return aParentPath.pathByAddingChild(node);
            } else {
                resultPath = findPathByUrl(aParentPath.pathByAddingChild(node), aUrl);
                if (resultPath != null) {
                    return resultPath;
                }
            }
        }
        return null;
    }

    /**
     * Returns string representation of path to help.
     */
    public String getHelpPrefix() {
        return mHelpPrefix;
    }

    /**
     * Returns iterator by contents.
     */
    protected ContentsIterator getIterator() {
        return mIterator;
    }

    /**
     * Returns identifier of help page by url.
     *
     * @param aUrl url to the page
     */
    public String getPageId(String aUrl) {
        if (aUrl == null) {
            return null;
        }
        return (String) mUrl2IdMap.get(aUrl);
    }

    /**
     * Returns url of help page by identifier.
     *
     * @param aId id of the page
     */
    public String getPageUrl(String aId) {
        if (aId == null) {
            return null;
        }
        return (String) mId2UrlMap.get(aId);
    }

    /**
     * Returns tree instance.
     */
    public JTree getTree() {
        return mTree;
    }

    /* -- Private methods -- */

    /**
     * Parses xml file to load contents.
     *
     * @param aUrl url to xml-file
     *
     * @return the root of contents tree
     */
    private DefaultMutableTreeNode parseXML(String aUrl) {
        ClassLoader classLoader;
        InputStream istream;
        URL url;
        XmlParser parser;

        //clears vector of contents
        mContents.clear();
        //clears hashtables
        mId2UrlMap.clear();
        mUrl2IdMap.clear();
        parser = new XmlParser();
        classLoader = getClass().getClassLoader();
        url = classLoader.getResource(aUrl);
        if (url == null) {
            mLogger.error("Xml-file by url = \"" + aUrl + "\" not found!");
            return null;
        }
        try {
            istream = url.openStream();
            parser.parse(new InputStreamReader(istream));
            mHelpPrefix = parser.getNodeUrlPrefix();
        } catch (Exception e) {
            mLogger.error("Not parsed xml-file with contents by url = " + aUrl);
            e.printStackTrace();
            return null;
        }
        return parser.getRootNode();
    }

    /**
     * Removes content selection listener.
     *
     * @param aListener listener of content selection
     */
    public void removeListener(IContentSelectionListener aListener) {
        mTree.removeTreeSelectionListener((TreeSelectionListener) aListener);
    }
}