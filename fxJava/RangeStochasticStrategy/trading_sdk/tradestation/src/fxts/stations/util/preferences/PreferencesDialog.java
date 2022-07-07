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
 * 05/18/2006   Andre Mermegas: extends ABaseDialog
 */
package fxts.stations.util.preferences;

import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * PreferencesDialog allows to client to adjust program<br>
 * <br> in accordance with own preferences.
 * <ul>
 * <li> Herewith it can choose any one of its positions.</li>
 * <li> When closing is checked admissibility to this operations.</li>
 * </ul>
 * <br>
 * Creation date (24/09/2003 13:36 )
 */
public class PreferencesDialog extends ABaseDialog implements ILocaleListener {
    private JButton mApplyButton;
    private JButton mCancelButton;
    private JButton mOkButton;
    private PreferencesMainPanel mPreferencesPanel;
    private ResourceManager mResMan;
    private JButton mResetToDefaultButton;
    private String mUserName;

    /**
     * Constructor.
     *
     * @param aParent parent
     * @param aPreferencesPanel pref panel
     * @param aUserName username
     */
    public PreferencesDialog(Frame aParent, PreferencesMainPanel aPreferencesPanel, String aUserName) {
        super(aParent);
        PreferencesSheetPanel.clearDirtyCounter();
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            e.printStackTrace();
        }
        setTitle(mResMan.getString("IDS_TITLE"));
        mUserName = aUserName;
        mPreferencesPanel = aPreferencesPanel;
        mPreferencesPanel.setParentDialog(this);
        mResMan.addLocaleListener(this);
        mResMan.addLocaleListener(mPreferencesPanel);

        //creates buttons
        mApplyButton = UIManager.getInst().createButton(mResMan.getString("IDS_APPLY_BUTTON"));
        mOkButton = UIManager.getInst().createButton(mResMan.getString("IDS_OK_BUTTON"));
        mCancelButton = UIManager.getInst().createButton(mResMan.getString("IDS_CANCEL_BUTTON"));
        mResetToDefaultButton = UIManager.getInst().createButton(mResMan.getString("IDS_RESET_TO_DEFAULT_BUTTON"));
        mResetToDefaultButton.setEnabled(!UserPreferences.getUserPreferences(aUserName).isDefault());
        getRootPane().setDefaultButton(mOkButton);

        //sets layout
        getContentPane().setLayout(new RiverLayout());
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new RiverLayout());

        // Define buttons in main window of dialog
        mApplyButton.setEnabled(false);
        buttonsPanel.add(mApplyButton);
        buttonsPanel.add(mOkButton);
        buttonsPanel.add(mCancelButton);
        buttonsPanel.add(mResetToDefaultButton);

        //sets for exiting
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        mCancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "Exit");
        mCancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                mPreferencesPanel.cancelEditing();
                mPreferencesPanel.cancelAction();
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        //sets main panel
        JPanel fullPanel = new JPanel();
        fullPanel.setLayout(new RiverLayout());
        fullPanel.add("vfill hfill", mPreferencesPanel);
        fullPanel.add("br center", buttonsPanel);
        getContentPane().add("vfill hfill", fullPanel);

        /*Assign listeners to buttons*/
        mApplyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (mPreferencesPanel.allowStopEditing()) {
                    mPreferencesPanel.saveValue();
                    mApplyButton.setEnabled(false);
                    mCancelButton.setEnabled(false);
                    mResetToDefaultButton.setEnabled(true);
                }
            }
        });
        mOkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (mPreferencesPanel.allowStopEditing()) {
                    mPreferencesPanel.saveValue();
                    closeDialog(JOptionPane.OK_OPTION);
                }
            }
        });
        mResetToDefaultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                mPreferencesPanel.cancelEditing();
                mPreferencesPanel.resetToDefaultAction();
                UserPreferences.getUserPreferences(mUserName).resetToDefault();
                mApplyButton.setEnabled(false);
                mResetToDefaultButton.setEnabled(false);
            }
        });
        mCancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                mPreferencesPanel.cancelEditing();
                mPreferencesPanel.cancelAction();
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        //adds window listener
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent aEvent) {
                mPreferencesPanel.cancelEditing();
                mPreferencesPanel.cancelAction();
                mApplyButton.setEnabled(false);
            }
        });
        Util.setAllToBiggest(new JComponent[]{mApplyButton, mCancelButton, mOkButton, mResetToDefaultButton});
    }

    public void closeDialog(int aExitCode) {
        super.closeDialog(aExitCode);
        mResMan.removeLocaleListener(this);
        mResMan.removeLocaleListener(mPreferencesPanel);
    }

    /**
     * onChangeLocale
     * Called when current locale is changed.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        //sets title of window
        setTitle(mResMan.getString("IDS_TITLE"));
        mApplyButton.setText(mResMan.getString("IDS_APPLY_BUTTON"));
        mOkButton.setText(mResMan.getString("IDS_OK_BUTTON"));
        mCancelButton.setText(mResMan.getString("IDS_CANCEL_BUTTON"));
        ColorPropertyType type = (ColorPropertyType) PropertiesSheet.getType(new Color(0, 0, 0));
        type.setEditor(new ColorEditor());
    }

    /**
     * setApplyButtonEnable.
     * Method providing of external management of condition Apply button.
     *
     * @param aEnabled true - Apply button set enabled, else false.
     */
    public void setApplyButtonEnable(boolean aEnabled) {
        mApplyButton.setEnabled(aEnabled);
    }

    /**
     * setCancelButtonEnable.
     * Method providing of external management of condition Cancel button.
     *
     * @param aEnabled true - Cancel button set enabled, else false.
     */
    public void setCancelButtonEnable(boolean aEnabled) {
        mCancelButton.setEnabled(aEnabled);
    }

    /**
     * setApplyButtonEnable.
     * Method providing of external management of condition Apply button.
     *
     * @param aEnabled true - Apply button set enabled, else false.
     */
    public void setResetButtonEnable(boolean aEnabled) {
        mResetToDefaultButton.setEnabled(aEnabled);
    }

    /**
     * showModal
     * Make visible dialog how modal window.
     * Determing exterior of dialog
     *
     * @return miExitCode Code of return
     */
    public int showModal() {
        mPreferencesPanel.expandTree();
        mPreferencesPanel.setTreeListener();
        int exitCode = JOptionPane.CLOSED_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        mPreferencesPanel.removeTreeListener();
        return exitCode;
    }
}
