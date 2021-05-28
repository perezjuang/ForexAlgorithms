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
 * Author: Andre Mermegas
 * Created: Nov 28, 2006 4:14:01 PM
 *
 * 08/04/2008   Andre Mermegas: min/max length in PIN validate
 */
package fxts.stations.trader.ui.dialogs;

import fxts.stations.ui.RiverLayout;
import fxts.stations.ui.UIManager;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 */
public class PINDialog extends JDialog {
    private JPasswordField mPasswordField;

    /**
     * Constructor.
     *
     * @param aOwner owner frame
     */
    public PINDialog(Frame aOwner) {
        super(aOwner);
        setModal(true);
        setTitle("PIN");
        getContentPane().setLayout(new RiverLayout());
        String msg1 = "You must enter a digital PIN code to login this connection.";
        getContentPane().add("br left", UIManager.getInst().createLabel(msg1));
        getContentPane().add("br left", UIManager.getInst().createLabel("PIN"));
        mPasswordField = UIManager.getInst().createPasswordField();
        mPasswordField.setColumns(6);
        mPasswordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent aEvent) {
                int length = mPasswordField.getPassword().length;
                if (length == 6) {
                    aEvent.consume();
                }
            }
        });
        getContentPane().add("tab hfill", mPasswordField);
        JButton button = UIManager.getInst().createButton("Submit");
        String msg2 = "If you have not received your PIN, please contact the Sales and Client Services desk";
        getContentPane().add("br left", UIManager.getInst().createLabel(msg2));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (mPasswordField.getPassword().length == 0 || mPasswordField.getPassword().length < 6) {
                    //custom title, error icon
                    JOptionPane.showMessageDialog(null, "PIN Required", "PIN Required", JOptionPane.ERROR_MESSAGE);
                } else {
                    setVisible(false);
                    dispose();
                }
            }
        });
        getContentPane().add("br center", button);
        getRootPane().setDefaultButton(button);
        pack();
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public String getPIN() {
        return String.valueOf(mPasswordField.getPassword());
    }
}
