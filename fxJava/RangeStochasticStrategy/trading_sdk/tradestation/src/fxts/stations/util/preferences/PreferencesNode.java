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

import fxts.stations.util.ResourceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.tree.DefaultMutableTreeNode;

public class PreferencesNode implements PreferencesConstants {
    private final Log mLogger = LogFactory.getLog(PreferencesNode.class);
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;
    private DefaultMutableTreeNode mTreeNode;
    private PropertiesSheet mSheet;
    private String mNameResId;

    public PreferencesNode(String aNameResId) {
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            mLogger.error("The fatal error");
            e.printStackTrace();
        }
        mNameResId = aNameResId;
    }

    public PreferencesNode addNode(String aNameResID) {
        PreferencesNode childNode;
        for (int i = 0; i < mTreeNode.getChildCount(); i++) {
            childNode = (PreferencesNode) ((DefaultMutableTreeNode) mTreeNode.getChildAt(i)).getUserObject();
            if (childNode.getName().equals(aNameResID)) {
                return childNode;
            }
        }
        //Same node not found.
        childNode = new PreferencesNode(aNameResID);
        DefaultMutableTreeNode newTreeNode = new DefaultMutableTreeNode();
        newTreeNode.setUserObject(childNode);
        mTreeNode.add(newTreeNode);
        childNode.setTreeNode(newTreeNode);
        return childNode;
    }

    public String getName() {
        return mResMan.getString(mNameResId, mNameResId);
    }

    public PropertiesSheet getSheet() {
        return mSheet;
    }

    public DefaultMutableTreeNode getTreeNode() {
        return mTreeNode;
    }

    public void setTreeNode(DefaultMutableTreeNode aTreeNode) {
        mTreeNode = aTreeNode;
    }

    public void setName(String aNameResId) {
        mNameResId = aNameResId;
    }

    public void setSheet(PropertiesSheet aSheet) {
        mSheet = aSheet;
    }

    public String toString() {
        return getName();
    }
}