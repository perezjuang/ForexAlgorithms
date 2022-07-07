/*
 * $Header: //depot/FXCM/New_CurrentSystem/Main/FXCM_SRC/TRADING_SDK/tradestation/src/main/fxts/stations/trader/ui/dialogs/MessageDialog.java#1 $
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
 * Created: Jul 6, 2007 3:15:57 PM
 *
 * $History: $
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.datatypes.Message;
import fxts.stations.trader.TradeApp;
import fxts.stations.trader.ui.ABaseDialog;
import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;

public class MessageDialog extends ABaseDialog {
    private final SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("hh:mm:ss a");
    private static boolean cShowMe = true;
    private JLabel mFromLabel;
    private Message mMessage;
    private JLabel mSentLabel;
    private JLabel mTextLabel;
    private JTextArea mTxtArea;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public MessageDialog(Frame aOwner) {
        super(aOwner);
        setTitle(TradeApp.getInst().getResourceManager().getString("IDS_MESSAGES_TITLE"));
        getContentPane().setLayout(new RiverLayout());
        getContentPane().add("br left", UIManager.getInst().createLabel("From:"));
        mFromLabel = UIManager.getInst().createLabel();
        getContentPane().add("tab hfill", mFromLabel);
        getContentPane().add("br left", UIManager.getInst().createLabel("Sent:"));
        mSentLabel = UIManager.getInst().createLabel();
        getContentPane().add("tab hfill", mSentLabel);
        getContentPane().add("br left", UIManager.getInst().createLabel("Subject:"));
        mTextLabel = UIManager.getInst().createLabel();
        getContentPane().add("tab hfill", mTextLabel);

        mTxtArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics aGraphics) {
                if (UIManager.getInst().isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };

        JScrollPane textScrollPane = new JScrollPane(mTxtArea);
        mTxtArea.setBackground(Color.WHITE);
        mTxtArea.setCaretPosition(0);
        mTxtArea.setEditable(false);
        mTxtArea.setColumns(30);
        mTxtArea.setRows(5);
        mTxtArea.setLineWrap(true);
        mTxtArea.setWrapStyleWord(true);
        mTxtArea.setFocusAccelerator('\0');
        mTxtArea.setForeground(Color.BLACK);
        Border loweredbevel = BorderFactory.createLoweredBevelBorder();
        textScrollPane.setBorder(loweredbevel);
        getContentPane().add("br hfill vfill", textScrollPane);

        JButton okButton = UIManager.getInst().createButton("OK");
        okButton.requestFocus();
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
        getContentPane().add("br center", okButton);
    }

    public Message getMessage() {
        return mMessage;
    }

    public void setMessage(Message aMessage) {
        mMessage = aMessage;
        mFromLabel.setText(aMessage.getFrom());
        mSentLabel.setText(mSimpleTimeFormat.format(aMessage.getDate()));
        mTextLabel.setText(aMessage.getText());
        mTxtArea.setText(aMessage.getFullText());
    }

    @Override
    public int showModal() {
        if (cShowMe) {
            setModal(true);
            pack();
            setLocationRelativeTo(getOwner());
            cShowMe = false;
            setVisible(true);
            cShowMe = true;
        }
        return 0;
    }
}
