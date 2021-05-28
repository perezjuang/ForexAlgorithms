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
package fxts.stations.ui;

import fxts.stations.core.TradeDesk;
import fxts.stations.trader.ui.RateSpinner;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.UserPreferences;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SpinnerModel;
import javax.swing.JToggleButton;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Singleton class that is responsible for dinamic support of localization
 * and prompt support for menu items and toolbar buttons.
 */
public class UIManager implements ILocaleListener {
    private static final UIManager INSTANCE = new UIManager();
    private ResourceManager mResourceManager;
    private Boolean mIsAAEnabled = null;

    /**
     * Private constructor.
     */
    private UIManager() {
    }

    /**
     * Adds localization and prompt support to the specified action.
     *
     * @param aAction target action
     * @param aLabel label's id
     * @param aIcon icon's id
     * @param aMnemonic mnemonic's id
     * @param aToolTip tooltip's id
     * @param aDesc accessable description's id
     */
    public void addAction(Action aAction,
                          String aLabel,
                          String aIcon,
                          String aMnemonic,
                          String aToolTip,
                          String aDesc) {
        ActionLocalizator al = new ActionLocalizator(aAction, aLabel, aIcon, aMnemonic, aToolTip, aDesc);
        al.onChangeLocale(mResourceManager);
        //mLocalizators.add(al);
    }

    public JButton createButton() {
        return createButton(null, null, null, null);
    }

    /**
     * Creates toolbar button.
     *
     * @param aAction an action registered through the method addAction
     *
     * @return created toolbar button
     */
    public JButton createButton(Action aAction) {
        return new JButton(aAction) {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JButton createButton(String aLabel) {
        return createButton(aLabel, null, null, null);
    }

    /**
     * Creates toolbar button.
     *
     * @param aLabel label's id
     * @param aIcon icon's id
     * @param aToolTip tooltip's id
     * @param aDesc accessable description's id
     *
     * @return created toolbar button
     */
    public JButton createButton(String aLabel, String aIcon, String aToolTip, String aDesc) {
        JButton jb = new JButton() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        ButtonLocalizator bl = new ButtonLocalizator(jb, aLabel, aIcon, aToolTip, aDesc);
        bl.onChangeLocale(mResourceManager);
        //mLocalizators.add(bl);
        return jb;
    }

    public JCheckBox createCheckBox() {
        return createCheckBox(null);
    }

    public JCheckBox createCheckBox(String aLabel) {
        return new JCheckBox(aLabel) {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    /**
     * Creates check box menu item.
     *
     * @param aLabel label's id
     * @param aIcon icon's id
     * @param aMnemonic mnemonic's id
     * @param aDesc accessable description's id
     *
     * @return created CheckBox menu item
     */
    public JCheckBoxMenuItem createCheckBoxMenuItem(String aLabel, String aIcon, String aMnemonic, String aDesc) {
        JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        MenuItemLocalizator mil = new MenuItemLocalizator(cbmi, aLabel, aIcon, aMnemonic, aDesc);
        mil.onChangeLocale(mResourceManager);
        //mLocalizators.add(mil);
        return cbmi;
    }

    public JLabel createLabel() {
        return createLabel(null);
    }

    public JLabel createLabel(String aLabel) {
        return new JLabel(aLabel) {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JToggleButton createToggleButton(String aLabel) {
        return new JToggleButton(aLabel) {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JList createList() {
        return new JList() {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    /**
     * Creates generic menu.
     *
     * @param aLabel label's id
     * @param aMnemonic mnemonic's id
     *
     * @return created menu item
     */
    public JMenu createMenu(String aLabel, String aMnemonic) {
        JMenu jm = new JMenu() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        MenuLocalizator ml = new MenuLocalizator(jm, aLabel, aMnemonic);
        ml.onChangeLocale(mResourceManager);
        //mLocalizators.add(ml);
        return jm;
    }

    /**
     * Creates generic menu item.
     *
     * @param aAction an action registered through the method addAction
     *
     * @return created menu item
     */
    public JMenuItem createMenuItem(Action aAction) {
        return new JMenuItem(aAction) {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    /**
     * Creates generic menu item.
     *
     * @param aLabel label's id
     * @param aIcon icon's id
     * @param aMnemonic mnemonic's id
     * @param aDesc accessable description's id
     *
     * @return created menu item
     */
    public JMenuItem createMenuItem(String aLabel, String aIcon, String aMnemonic, String aDesc) {
        JMenuItem jmi = new JMenuItem() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        MenuItemLocalizator mil = new MenuItemLocalizator(jmi, aLabel, aIcon, aMnemonic, aDesc);
        mil.onChangeLocale(mResourceManager);
        //mLocalizators.add(mil);
        return jmi;
    }

    public JMenuItem createMenuItem() {
        return new JMenuItem() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JPasswordField createPasswordField() {
        return createPasswordField(null);
    }

    public JPasswordField createPasswordField(String aLabel) {
        return new JPasswordField(aLabel) {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JPopupMenu createPopupMenu() {
        return new JPopupMenu() {
            protected JMenuItem createActionComponent(Action aAction) {
                JMenuItem mi = createMenuItem();
                mi.setText((String) aAction.getValue(Action.NAME));
                mi.setIcon((Icon) aAction.getValue(Action.SMALL_ICON));
                mi.setHorizontalTextPosition(JButton.TRAILING);
                mi.setVerticalTextPosition(JButton.CENTER);
                mi.setEnabled(aAction.isEnabled());
                return mi;
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JRadioButton createRadioButton(String aLabel) {
        return new JRadioButton(aLabel) {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    /**
     * Creates radio box menu item.
     *
     * @param aLabel label's id
     * @param aIcon icon's id
     * @param aMnemonic mnemonic's id
     * @param aDesc accessable description's id
     *
     * @return created radio box menu item
     */
    public JRadioButtonMenuItem createRadioButtonMenuItem(String aLabel,
                                                          String aIcon,
                                                          String aMnemonic,
                                                          String aDesc) {
        JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem() {
            protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                return new WeakActionPropertyChangeListener(this, a);
            }

            public void addPropertyChangeListener(PropertyChangeListener aListener) {
                super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
            }

            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
        MenuItemLocalizator mil = new MenuItemLocalizator(rbmi, aLabel, aIcon, aMnemonic, aDesc);
        mil.onChangeLocale(mResourceManager);
        //mLocalizators.add(mil);
        return rbmi;
    }

    public RateSpinner createRateSpinner() {
        return new RateSpinner() {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JSpinner createSpinner(SpinnerModel aModel) {
        return new JSpinner(aModel) {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JTextField createTextField() {
        return createTextField(null);
    }

    public JTextField createTextField(String aLabel) {
        return new JTextField(aLabel) {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JToolBar createToolBar() {
        return new JToolBar() {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    public JTree createTree(TreeNode aTreeNode) {
        return new JTree(aTreeNode) {
            protected void paintComponent(Graphics aGraphics) {
                if (isAAEnabled()) {
                    Graphics2D g2d = (Graphics2D) aGraphics;
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                }
                super.paintComponent(aGraphics);
            }
        };
    }

    /**
     * Returns the one and only instance of the UIManager.
     */
    public static UIManager getInst() {
        return INSTANCE;
    }

    public boolean isAAEnabled() {
        if (mIsAAEnabled == null) {
            UserPreferences preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            preferences.addObserver(new Observer() {
                public void update(Observable aObservable, Object aArgument) {
                    if (aArgument.toString().contains("Anti-aliasing")) {
                        UserPreferences p = (UserPreferences) aObservable;
                        mIsAAEnabled = p.getBoolean("Anti-aliasing");
                    }
                }
            });
            mIsAAEnabled = preferences != null && preferences.getBoolean("Anti-aliasing");
        }
        return mIsAAEnabled;
    }

    /**
     * ILocaleListener method.
     *
     * @param aMan resource manager
     */
    public void onChangeLocale(ResourceManager aMan) {
        //for (ILocalizator mLocalizator : mLocalizators) {
        //    mLocalizator.onChangeLocale(aMan);
        //}
    }

    /**
     * Sets the preferred width of the visible column specified by vColIndex. The column
     * will be just wide enough to show the column head and the widest cell in the column.
     * margin pixels are added to the left and right
     * (resulting in an additional width of 2*margin pixels).
     *
     * @param aTable
     * @param aColIndex
     * @param aMargin
     */
    private void packColumn(JTable aTable, int aColIndex, int aMargin) {
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) aTable.getColumnModel();
        TableColumn col = colModel.getColumn(aColIndex);

        // Get width of column header
        TableCellRenderer renderer = col.getHeaderRenderer();
        if (renderer == null) {
            renderer = aTable.getTableHeader().getDefaultRenderer();
        }
        Component comp = renderer.getTableCellRendererComponent(aTable, col.getHeaderValue(), false, false, 0, 0);
        int width = comp.getPreferredSize().width;

        // Get maximum width of column data
        for (int i = 0; i < aTable.getRowCount(); i++) {
            renderer = aTable.getCellRenderer(i, aColIndex);
            comp = renderer.getTableCellRendererComponent(aTable,
                                                          aTable.getModel().getValueAt(i, aColIndex),
                                                          false,
                                                          false,
                                                          i,
                                                          aColIndex);
            width = Math.max(width, comp.getPreferredSize().width);
        }

        // Add margin
        width += 2 * aMargin;

        // Set the width
        col.setPreferredWidth(width);
    }

    public void packColumns(JTable aTable, int aMargin) {
        for (int i = 0; i < aTable.getColumnCount(); i++) {
            packColumn(aTable, i, aMargin);
        }
    }

    /**
     * Remove localization and prompt support from the object
     *
     * @param aObj object
     */
    public void remove(ILocalizator aObj) {
        //mLocalizators.remove(aObj);
    }

    /**
     * Set resource manager that is used for get localized resources (labels, icons, descriptors etc) for menu items
     * and toolbar buttons. This method should be called before use of factory methods.
     */
    public void setResourceManager(ResourceManager aMan) {
        //remove listener from previous resource manager
        if (mResourceManager != null && mResourceManager == aMan) {
            mResourceManager.removeLocaleListener(this);
        }
        mResourceManager = aMan;
        aMan.addLocaleListener(this);
    }
}
