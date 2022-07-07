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

import fxts.stations.ui.ResizeParameterWrapper;
import fxts.stations.ui.UIFrontEnd;
import fxts.stations.ui.UIManager;
import fxts.stations.util.ResourceManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Actions of editing.
 * Creation date (10/28/2003 1:36 PM)
 */
public class EditAction implements ActionListener {
    private final Log mLogger = LogFactory.getLog(EditAction.class);
    /**
     * Panel for editing value.
     */
    private AValueEditorPanel mEditorPanel;
    /**
     * Resource manager.
     */
    private ResourceManager mResMan;
    /**
     * Type of data.
     */
    private APropertyType mType;
    /**
     * Current value.
     */
    private Object mValue;

    /**
     * Constructor.
     */
    public EditAction(APropertyType aType, AValueEditorPanel aEditorPanel, Object aValue) {
        try {
            mResMan = ResourceManager.getManager("fxts.stations.util.preferences.resources.Resources");
        } catch (Exception e) {
            mLogger.error("The fatal error");
            e.printStackTrace();
        }
        mType = aType;
        mEditorPanel = aEditorPanel;
        mValue = aValue;
    }

    public void actionPerformed(ActionEvent aEvent) {
        JButton okButton = UIManager.getInst().createButton();
        JButton cancelButton = UIManager.getInst().createButton();
        final JDialog dialog = new JDialog(mEditorPanel.getParentDialog());
        dialog.setTitle(mEditorPanel.getTitle());
        JPanel editPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(UIFrontEnd.getInstance().getSideLayout());
        //mainPanel.setLayout(new SideLayout());

        //sets button panel
        buttonPanel.setLayout(UIFrontEnd.getInstance().getSideLayout());
        okButton.setText(mResMan.getString("IDS_OK_BUTTON"));
        //okButton.setPreferredSize(new Dimension(80, 27));
        GridBagConstraints sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        sideConstraints.insets = new Insets(10, 10, 10, 10);
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        ResizeParameterWrapper resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.5, 0.0, 0.5, 0.0);
        resizeParameter.setToConstraints(sideConstraints);
        buttonPanel.add(okButton, sideConstraints);
        cancelButton.setText(mResMan.getString("IDS_CANCEL_BUTTON"));
        //cancelButton.setPreferredSize(new Dimension(80, 27));
        sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        sideConstraints.insets = new Insets(10, 10, 10, 10);
        sideConstraints.gridx = 1;
        sideConstraints.gridy = 0;
        resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.5, 0.0, 0.5, 0.0);
        resizeParameter.setToConstraints(sideConstraints);
        buttonPanel.add(cancelButton, sideConstraints);

        //adds button panel
        sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        sideConstraints.insets = new Insets(10, 10, 10, 10);
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 1;
        resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.0, 1.0, 1.0, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        mainPanel.add(buttonPanel, sideConstraints);

        //sets edit panel
        final IEditor editor = mType.getEditor();
        editor.setValue(mValue);
        editPanel.setLayout(UIFrontEnd.getInstance().getSideLayout());
        sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.0, 0.0, 1.0, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        Component editComp = editor.getComponent();

        //Mar 25 2004 - kav: added for right tab order at Font Chooser at java 1.4.
        if (editComp instanceof FontChooser) {
            FontChooser fc = (FontChooser) editComp;
            fc.setNextFocusedComp(okButton);
        }
        editPanel.add(editComp, sideConstraints);

        //adds editor panel
        sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        sideConstraints.gridx = 0;
        sideConstraints.gridy = 0;
        resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.0, 0.0, 1.0, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        mainPanel.add(editPanel, sideConstraints);

        //adds main panel
        dialog.getContentPane().setLayout(UIFrontEnd.getInstance().getSideLayout());
        //dialog.getContentPane().setLayout(new SideLayout());
        sideConstraints = UIFrontEnd.getInstance().getSideConstraints();
        sideConstraints.fill = GridBagConstraints.BOTH;
        resizeParameter = UIFrontEnd.getInstance().getResizeParameter();
        resizeParameter.init(0.0, 0.0, 1.0, 1.0);
        resizeParameter.setToConstraints(sideConstraints);
        dialog.getContentPane().add(mainPanel, sideConstraints);

        //adds listeners to buttons
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                if (editor.getValue().equals(mValue)) {
                    //
                } else {
                    mValue = editor.getValue();
                    mEditorPanel.setValue(mValue);
                    mEditorPanel.refreshControls();
                    mEditorPanel.setValueChanged(true);
                }
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        okButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                                                    "ExitAction");
        okButton.getActionMap().put("ExitAction", new AbstractAction() {
            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent aEvent) {
                editor.setValue(mValue);
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        okButton.requestFocus();
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvent) {
                editor.setValue(mValue);
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        //dialog.setResizable(false);
        dialog.setModal(true);
        dialog.pack();

        //sets minimal sizes for components
        Dimension dim = mainPanel.getSize();
        mainPanel.setMinimumSize(dim);
        mainPanel.setPreferredSize(dim);

        //sets size of buttons
        Dimension dimOkButton = okButton.getSize();
        Dimension dimCancelButton = cancelButton.getSize();
        int nMaxWidth = dimOkButton.getWidth() > dimCancelButton.getWidth()
                        ? (int) dimOkButton.getWidth()
                        : (int) dimCancelButton.getWidth();
        okButton.setPreferredSize(new Dimension(nMaxWidth, (int) dimOkButton.getHeight()));
        okButton.setSize(new Dimension(nMaxWidth, (int) dimOkButton.getHeight()));
        cancelButton.setPreferredSize(new Dimension(nMaxWidth, (int) dimCancelButton.getHeight()));
        cancelButton.setSize(new Dimension(nMaxWidth, (int) dimCancelButton.getHeight()));
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    /**
     * Sets current value.
     *
     * @param aValue current value.
     */
    public void setValue(Object aValue) {
        mValue = aValue;
        mType.getEditor().setValue(aValue);
    }
}