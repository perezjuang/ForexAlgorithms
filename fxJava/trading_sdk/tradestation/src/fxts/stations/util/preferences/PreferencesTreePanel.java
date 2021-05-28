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

import fxts.stations.ui.UIManager;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * PreferencesTreePanel is a part of PreferencesDialog allows to client
 * to select needed property.<br>
 * <ul>
 * <li> .</li>
 * <li> .</li>
 * </ul>
 * <br>
 *
 * @Creation date (27/10/2003 13:36 )
 */
public class PreferencesTreePanel extends JScrollPane implements KeyListener {
    private JTree mTree;

    /**
     * Constructor PreferencesTreePanel
     * <br>
     * <br>
     */
    public PreferencesTreePanel() {
        DefaultMutableTreeNode mRootNode = new DefaultMutableTreeNode("...");
        mTree = UIManager.getInst().createTree(mRootNode);
        mTree.setRootVisible(false);
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        mTree.setToggleClickCount(1);
        //To use standard L&F UI keyboard controls
        mTree.addKeyListener(this);
        super.setViewportView(mTree);
    }

    /**
     * @return tree
     */
    public JTree getTree() {
        return mTree;
    }

    /**
     * @param aEvent KeyEvent
     */
    public void keyPressed(KeyEvent aEvent) {
    }

    /**
     * @param aEvent KeyEvent
     */
    public void keyReleased(KeyEvent aEvent) {
    }

    /**
     * @param aEvent KeyEvent
     */
    public void keyTyped(KeyEvent aEvent) {
    }

    public void requestFocus() {
        mTree.requestFocus();
    }
}