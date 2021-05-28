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
 * 08/22/2006   Andre Mermegas: dynamic copyright, manifest version
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.trader.IFXTSConstants;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;

/**
 * This dialog shows information about application.
 */
public class AboutDialog extends ABaseDialog {
    private JTextArea mDescriptionTextArea;
    private ResourceManager mResMan;
    private JScrollPane mTextScrollPane;

    /**
     * Creates new form AboutDialog.
     *
     * @param aParent parent window
     */
    public AboutDialog(Frame aParent) {
        super(aParent);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
            setTitle(mResMan.getString("IDS_ABOUT_CAPTION") + " " + mResMan.getString("IDS_MAINFRAME_SHORT_TITLE"));
            mDescriptionTextArea = new JTextArea();
            mTextScrollPane = new JScrollPane(mDescriptionTextArea);
            initComponents();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initailzation of components.
     */
    private void initComponents() {
        JButton okButton = UIManager.getInst().createButton();
        JLabel nameLabel = UIManager.getInst().createLabel();
        JLabel versionLabel = UIManager.getInst().createLabel();
        JLabel javaVersionLabel = UIManager.getInst().createLabel();
        JLabel buildLabel = UIManager.getInst().createLabel();
        JLabel copyrightLabel = UIManager.getInst().createLabel();

        //name
        nameLabel.setText(mResMan.getString("IDS_ABOUT_NAME"));
        nameLabel.setFont(new Font("Dialog", 1, 16));
        nameLabel.setForeground(Color.BLACK);

        //version
        versionLabel.setText(mResMan.getString("IDS_ABOUT_VERSION") + ": " + IFXTSConstants.CURRENT_VERSION);
        versionLabel.setForeground(Color.BLACK);

        javaVersionLabel.setText("JRE: " + System.getProperty("java.version"));
        //build number
        try {
            String implementationVersion = AboutDialog.class.getPackage().getImplementationVersion();
            buildLabel.setText(mResMan.getString("IDS_ABOUT_BUILD") + ": J" + implementationVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
        buildLabel.setForeground(Color.BLACK);

        //copyright
        StringBuilder sb = new StringBuilder();
        sb.append("Copyright (C) ").append(Calendar.getInstance().get(Calendar.YEAR));
        copyrightLabel.setText(sb.toString());
        copyrightLabel.setForeground(Color.BLACK);

        //button ok
        okButton.setFont(new Font("Dialog", 1, 11));
        okButton.setText("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.OK_OPTION);
            }
        });
        getRootPane().setDefaultButton(okButton);
        okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                    "ExitAction");
        okButton.getActionMap().put("ExitAction", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.OK_OPTION);
            }
        });

        //description
        mDescriptionTextArea.setText(mResMan.getString("IDS_ABOUT_DESCRIPTION"));
        mDescriptionTextArea.setBackground(Color.WHITE);
        mDescriptionTextArea.setCaretPosition(0);
        mDescriptionTextArea.setEditable(false);
        mDescriptionTextArea.setColumns(30);
        mDescriptionTextArea.setRows(5);
        mDescriptionTextArea.setLineWrap(true);
        mDescriptionTextArea.setWrapStyleWord(true);
        mDescriptionTextArea.setFocusAccelerator('\0');
        mDescriptionTextArea.setForeground(Color.BLACK);
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        mTextScrollPane.setBorder(loweredbevel);

        getContentPane().setLayout(new RiverLayout());
        getContentPane().add("br left", nameLabel);
        getContentPane().add("br left", versionLabel);
        getContentPane().add("br left", javaVersionLabel);
        getContentPane().add("br left", buildLabel);
        getContentPane().add("br left", copyrightLabel);
        getContentPane().add("br hfill vfill", mTextScrollPane);
        getContentPane().add("br center", okButton);

        okButton.requestFocus();
    }

    public int showModal() {
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return 0;
    }
}