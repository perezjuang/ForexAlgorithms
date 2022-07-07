/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/DisclaimerDialog.java#1 $
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
 * Author: Andre Mermegas
 * Created: Aug 28, 2007 2:03:33 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;
import fxts.stations.ui.WeakHTMLEditorKit;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.Util;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

/**
 */
public class DisclaimerDialog extends ABaseDialog {
    private final JCheckBox mAcceptCheckBox = new JCheckBox("Accept");
    private int mExitCode;
    private JButton mOkButton = UIManager.getInst().createButton();
    private ResourceManager mResMan;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public DisclaimerDialog(Frame aOwner) {
        super(aOwner);
        try {
            mResMan = TradeApp.getInst().getResourceManager();
            if (mResMan == null) {
                mResMan = ResourceManager.getManager("fxts.stations.trader.resources.Resources");
            }
            setTitle("Disclaimer");
            initComponents();
            pack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        JTextPane.registerEditorKitForContentType("text/html", WeakHTMLEditorKit.class.getName());
        JTextPane descriptionTextArea = new JTextPane() {
            @Override
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        JScrollPane scrollPane = new JScrollPane(descriptionTextArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        descriptionTextArea.setContentType("text/html");
        String disclaimer = System.getProperty("disclaimer");
        System.out.println("disclaimer = " + disclaimer);
        if (disclaimer == null) {
            UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            disclaimer = preferences.getString("disclaimer");
        }
        System.out.println("disclaimer = " + disclaimer);
        try {
            URL resource = getClass().getClassLoader().getResource(disclaimer);
            System.out.println("resource = " + resource);
            descriptionTextArea.setPage(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        descriptionTextArea.setCaretPosition(0);
        descriptionTextArea.setEditable(false);
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        scrollPane.setBorder(loweredbevel);

        mOkButton.setEnabled(false);
        mOkButton.setText(mResMan.getString("IDS_MARKET_DIALOG_OK"));
        mOkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                mExitCode = JOptionPane.OK_OPTION;
                closeDialog(JOptionPane.OK_OPTION);
            }
        });
        getRootPane().setDefaultButton(mOkButton);
        mOkButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                     "ExitAction");
        mOkButton.getActionMap().put("ExitAction", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                mExitCode = JOptionPane.OK_OPTION;
                closeDialog(JOptionPane.OK_OPTION);
            }
        });

        JButton cancelButton = UIManager.getInst().createButton();
        cancelButton.setText(mResMan.getString("IDS_MARKET_DIALOG_CANCEL"));
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });
        //sets for exiting by escape
        cancelButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                        "Exit");
        cancelButton.getActionMap().put("Exit", new AbstractAction() {
            public void actionPerformed(ActionEvent aEvent) {
                closeDialog(JOptionPane.CANCEL_OPTION);
            }
        });

        mAcceptCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent aEvent) {
                if (aEvent.getStateChange() == ItemEvent.SELECTED) {
                    mOkButton.setEnabled(true);
                } else {
                    mOkButton.setEnabled(false);
                }
            }
        });
        getContentPane().setLayout(new RiverLayout());
        getContentPane().add("br hfill vfill", scrollPane);
        getContentPane().add("br center", mAcceptCheckBox);
        getContentPane().add("center", mOkButton);
        getContentPane().add("center", cancelButton);
        Util.setAllToBiggest(new JComponent[]{mOkButton, cancelButton});
        mOkButton.requestFocus();
    }

    @Override
    public int showModal() {
        mExitCode = JOptionPane.CANCEL_OPTION;
        setModal(true);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        return mExitCode;
    }
}
