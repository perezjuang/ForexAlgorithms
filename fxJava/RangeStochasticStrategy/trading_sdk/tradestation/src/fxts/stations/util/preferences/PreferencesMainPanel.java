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
package fxts.stations.util.preferences;

import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Main panel of preferences dialog.
 * Creation date (24/09/2003 13:36 )
 */
public class PreferencesMainPanel extends JSplitPane implements ILocaleListener {
    /*Right part of screen*/
    private PreferencesSheetPanel mSheetPanel;
    /*Left part of screen*/
    private PreferencesTreePanel mTreePanel;
    private PreferencesMainPanel.TreeSelectionListener mTreeSelectionListener;
    private String mUserName;

    /**
     * Constructor.
     *
     * @param aUserName username
     */
    public PreferencesMainPanel(String aUserName) {
        mUserName = aUserName;
        mTreePanel = new PreferencesTreePanel();
        mSheetPanel = new PreferencesSheetPanel(aUserName);
        //sets split panel
        setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        setLeftComponent(mTreePanel);
        setRightComponent(mSheetPanel);
        setDividerLocation(150);
        setOneTouchExpandable(true);
    }

    /**
     * addNode
     * Used method addNode from PreferencesNode
     *
     * @param aParent node WHERE will be added new node
     * @param aNameResID name of new node
     *
     * @return PreferencesNode new or already presenced
     */
    public PreferencesNode addNode(PreferencesNode aParent, String aNameResID) {
        return aParent.addNode(aNameResID);
    }

    /**
     * @return allow stop editing
     */
    public boolean allowStopEditing() {
        return mSheetPanel.allowStopEditing();
    }

    /**
     *
     */
    public void cancelAction() {
        searchChildWhenCancel((DefaultMutableTreeNode) mTreePanel.getTree().getModel().getRoot());
        mSheetPanel.initTable();
    }

    /**
     * searchChildWhenCancel.
     * Found all childs nodes for indicates parent node.
     * Called recurrently.
     *
     * @param aRootNode root node where searching childs
     */
    protected void searchChildWhenCancel(DefaultMutableTreeNode aRootNode) {
        DefaultMutableTreeNode childNode;
        PreferencesNode childPrefNode;
        PropertiesSheet sheetOfNode;
        PrefProperty propertyFromSheet;
        for (int i = 0; i < aRootNode.getChildCount(); i++) {
            childNode = (DefaultMutableTreeNode) aRootNode.getChildAt(i);
            childPrefNode = (PreferencesNode) childNode.getUserObject();
            sheetOfNode = childPrefNode.getSheet();
            //Does have preference node a property sheet?
            if (sheetOfNode != null) {
                //walk on all elements of sheet
                for (int j = 0; j < sheetOfNode.size(); j++) {
                    propertyFromSheet = sheetOfNode.get(j);
                    if (propertyFromSheet.isDirty()) {
                        //UserPreferences Listener always added first in the list
                        UserPreferences userPreferences = UserPreferences.getUserPreferences(mUserName);
                        propertyFromSheet.resetValue(userPreferences);
                        PreferencesSheetPanel.setPropertyDirty(propertyFromSheet, false);
                    }
                }
            }
            //and so on
            searchChildWhenCancel(childNode);
        }
    }

    /**
     *
     */
    public void cancelEditing() {
        mSheetPanel.cancelEditing();
    }

    /**
     * expandTree
     * Call to display all nodes first level with hidden null-level root node.
     */
    public void expandTree() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) mTreePanel.getTree().getModel().getRoot();
        TreePath stealth = new TreePath(rootNode);
        mTreePanel.getTree().expandPath(stealth);
    }

    /**
     * getRootNode.
     * Added new root node to tree and/or return early created with this name.
     *
     * @param aNameResID nameres
     *
     * @return PreferencesNode
     */
    public PreferencesNode getRootNode(String aNameResID) {
        PreferencesNode childPrefNode;
        DefaultMutableTreeNode rootObject = (DefaultMutableTreeNode) mTreePanel.getTree().getModel().getRoot();
        //Search  already existing root node
        for (int i = 0; i < rootObject.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootObject.getChildAt(i);
            childPrefNode = (PreferencesNode) childNode.getUserObject();
            if (childPrefNode.getName().equals(aNameResID)) {
                return childPrefNode;
            }
        }
        //We are here if not found same node. Create new PN with name
        PreferencesNode newPrefNode = new PreferencesNode(aNameResID);
        //created new DefaultMutableTreeNode
        DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode();
        //Set new DMTN for PN how UserObject
        newTreeNode.setUserObject(newPrefNode);
        //Store new DMTN in PN
        newPrefNode.setTreeNode(newTreeNode);
        //add new DMTN in root (DMTN)
        rootObject.add(newTreeNode);
        return newPrefNode;
    }

    /**
     * onChangeLocale
     * Called when current locale is changed.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        mTreePanel.getTree().updateUI();
    }

    public void removeTreeListener() {
        mTreePanel.getTree().removeTreeSelectionListener(mTreeSelectionListener);
    }

    /**
     *
     */
    public void resetToDefaultAction() {
        searchChildWhenReset((DefaultMutableTreeNode) mTreePanel.getTree().getModel().getRoot());
        PreferencesManager.getPreferencesManager(mUserName).firePreferencesUpdated(new Vector());
        mSheetPanel.initTable();
    }

    /**
     * searchChildWhenCancel.
     * Found all childs nodes for indicates parent node.
     * Called recurrently.
     *
     * @param aRootNode root node where searching childs
     */
    protected void searchChildWhenReset(DefaultMutableTreeNode aRootNode) {
        DefaultMutableTreeNode childNode;
        PreferencesNode childPrefNode;
        PropertiesSheet sheetOfNode;
        PrefProperty propertyFromSheet;
        String tempPropertyID;
        for (int i = 0; i < aRootNode.getChildCount(); i++) {
            childNode = (DefaultMutableTreeNode) aRootNode.getChildAt(i);
            childPrefNode = (PreferencesNode) childNode.getUserObject();
            sheetOfNode = childPrefNode.getSheet();
            //Does have preference node a property sheet?
            if (sheetOfNode != null) {
                //walk on all elements of sheet
                for (int j = 0; j < sheetOfNode.size(); j++) {
                    propertyFromSheet = sheetOfNode.get(j);
                    //UserPreferences Listener always added first in the list
                    UserPreferences userPreferences = UserPreferences.getUserPreferences(mUserName);
                    tempPropertyID = propertyFromSheet.getPropertyID();
                    userPreferences.remove(tempPropertyID);
                    propertyFromSheet.resetValue(userPreferences);
                    PreferencesSheetPanel.setPropertyDirty(propertyFromSheet, false);
                }
                //and so on
                searchChildWhenReset(childNode);
            }
        }
    }

    /**
     *
     */
    public void saveValue() {
        Vector resultVector = new Vector();
        searchChildWhenApply((DefaultMutableTreeNode) mTreePanel.getTree().getModel().getRoot(), resultVector);
        if (!resultVector.isEmpty()) {
            //for each listener from vector
            PreferencesManager.getPreferencesManager(mUserName).firePreferencesUpdated(resultVector);
            //for each preference property from vector mark it
            for (Enumeration e = resultVector.elements(); e.hasMoreElements();) {
                PrefProperty property = (PrefProperty) e.nextElement();
                PreferencesSheetPanel.setPropertyDirty(property, false);
            }
        }
        mSheetPanel.initTable();
    }

    /**
     * searchChildWhenApply.
     * Found all childs nodes for indicates parent node.
     * Called recurrently.
     *
     * @param aRootNode root node where searching childs
     * @param aResultVector resultVector place for founded childs
     */
    protected void searchChildWhenApply(DefaultMutableTreeNode aRootNode, Vector aResultVector) {
        DefaultMutableTreeNode childNode;
        PreferencesNode childPrefNode;
        PropertiesSheet sheetOfNode;
        PrefProperty propertyFromSheet;
        for (int i = 0; i < aRootNode.getChildCount(); i++) {
            childNode = (DefaultMutableTreeNode) aRootNode.getChildAt(i);
            childPrefNode = (PreferencesNode) childNode.getUserObject();
            sheetOfNode = childPrefNode.getSheet();
            //Does has preference node a property sheet?
            if (sheetOfNode != null) {
                //walk on all elements of sheet
                for (int j = 0; j < sheetOfNode.size(); j++) {
                    propertyFromSheet = sheetOfNode.get(j);
                    // add to vector only if propety was change
                    if (propertyFromSheet.isDirty()) {
                        aResultVector.addElement(propertyFromSheet);
                    }
                }
            }
            //and so on
            searchChildWhenApply(childNode, aResultVector);
        }
    }

    /**
     * Sets parent dialog.
     *
     * @param aParentDialog parent dialog
     */
    public void setParentDialog(PreferencesDialog aParentDialog) {
        //sets for exiting by escape button
        AbstractAction exitAction = new ExitAction(aParentDialog);
        mSheetPanel.setParentDialog(aParentDialog, exitAction);
        mTreePanel.getTree().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ESCAPE,
                0), "ExitAction");
        SwingUtilities.getUIActionMap(mTreePanel.getTree()).put("ExitAction", exitAction);
    }

    public void setTreeListener() {
        mTreeSelectionListener = new TreeSelectionListener(mTreePanel.getTree());
        mTreePanel.getTree().addTreeSelectionListener(mTreeSelectionListener);
    }

    private class ExitAction extends AbstractAction {
        private PreferencesDialog mDialog;

        ExitAction(PreferencesDialog aDialog) {
            mDialog = aDialog;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent aEvent) {
            cancelAction();
            mDialog.closeDialog(JOptionPane.CANCEL_OPTION);
        }
    }

    private class TreeSelectionListener implements javax.swing.event.TreeSelectionListener {
        private JTree mTree;

        TreeSelectionListener(JTree aTree) {
            mTree = aTree;
        }

        public void valueChanged(TreeSelectionEvent aEvent) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) aEvent.getPath().getLastPathComponent();
            if (node == null) {
                if (!allowStopEditing()) {
                    mTree.setSelectionPath(aEvent.getOldLeadSelectionPath());
                }
                return;
            }
            PreferencesNode preferencesNode = (PreferencesNode) node.getUserObject();
            if (preferencesNode != null) {
                PropertiesSheet sheet = preferencesNode.getSheet();
                if (sheet != null && sheet != mSheetPanel.getSheet()) {
                    if (!allowStopEditing()) {
                        mTree.setSelectionPath(aEvent.getOldLeadSelectionPath());
                        return;
                    }
                    mSheetPanel.setSheet(preferencesNode.getSheet());
                    mTreePanel.requestFocus();
                }
            }
        }
    }
}
