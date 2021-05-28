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
 * 05/10/2006   Andre Mermegas: cleanup of header table model, added mTimeFormat for time only fields
 * 07/05/2006   Andre Mermegas: added tooltips to column headers
 */
package fxts.stations.trader.ui.frames;

import com.fxcm.messaging.util.ThreadSafeNumberFormat;
import fxts.stations.core.IClickModel;
import fxts.stations.core.TradeDesk;
import fxts.stations.trader.ui.IMainFrame;
import fxts.stations.transport.tradingapi.TradingServerSession;
import fxts.stations.ui.AAJTable;
import fxts.stations.ui.ATable;
import fxts.stations.ui.ITable;
import fxts.stations.ui.TableManager;
import fxts.stations.ui.UIManager;
import fxts.stations.ui.WeakActionPropertyChangeListener;
import fxts.stations.ui.WeakPropertyChangeListener;
import fxts.stations.util.ILocaleListener;
import fxts.stations.util.ISignal;
import fxts.stations.util.ISignalListener;
import fxts.stations.util.ResourceManager;
import fxts.stations.util.SignalType;
import fxts.stations.util.SignalVector;
import fxts.stations.util.Signaler;
import fxts.stations.util.UserPreferences;
import fxts.stations.util.preferences.IUserPreferencesListener;
import fxts.stations.util.preferences.PreferencesManager;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Abstract class for simplifying writing a &lt;&lt;ViewFrame&gt;&gt; classes
 * Generaly an implementation of ATableFrame need to implements next methods only: <br>
 * <ol>
 * <li>constructor, which should call the superclass constructor</li>
 * <li>onCloseFrame, which simulates a distructor</li>
 * <li>getLocalizedTitle</li>
 * <li>getTableName</li>
 * <li>getTableModel for accesing to inner class &lt;&lt;ViewTableModel&gt;&gt;. See the
 * <code>AFrameTableModel</code> for how to simplify ViewTableModel implementation
 * </ol>
 * Next code is an idiom of &lt;&lt;ViewFrame&gt;&gt; implementation:
 * <pre>
 * public class &lt;&lt;ViewFrame&gt;&gt; extends ATableFrame {
 *     private static final String[][] cCOL_LOCAL_NAMES = {{"IDS_COL_0_NAME", "DefaultNameCol0"},
 *                                                         {"IDS_COL_1_NAME", "DefaultNameCol1"},
 *                                                         {"IDS_COL_2_NAME", "DefaultNameCol2"},
 *                                                         {"IDS_COL_3_NAME", "DefaultNameCol3"}};
 *     public &lt;&lt;ViewFrame&gt;&gt; (ResourceManager aMan) {
 *         super(aMan);
 *         //      subscribe on types of the signals which are needed
 *     }
 *     protected void onCloseFrame(InternalFrameEvent event) {
 *         //      onsubscribe on types of the signals which are was suscribed
 *     }
 *     protected String getLocalizedTitle(ResourceManager aResourceMan) {
 *         if (aResourceMan != null) {
 *             return aResourceMan.getString("IDS_FRAME_TITLE", "FrameDefaultTitle");
 *         } else {
 *             System.out.println("Error: OpenPositionsFrame.getLocalizedTitle: aResourceMan is null");
 *         }
 *         return null;
 *     }
 *     protected String getTableName() {
 *         return "TableName";
 *     }
 *     public AFrameTableModel getTableModel(ResourceManager aResourceMan) {
 *         if (mTableModel == null) {
 *             mTableModel = new &lt;&lt;ViewTableModel&gt;&gt;(aResourceMan);
 *         }
 *         return mTableModel;
 *     }
 *     class  &lt;&lt;ViewTableModel&gt;&gt; extends AFrameTableModel {
 *         &lt;&lt;ViewTableModel&gt;&gt; (ResourceManager aResourceMan) {
 *             super(aResourceMan, cCOL_LOCAL_NAMES);
 *         }
 *         public int getRowCount() {
 *         }
 *         public Object getValueAt(int iRow, int iCol) {
 *             //      Returns specified by iRow, iCol cell value
 *         }
 *     }
 * }
 * </pre>
 * Creation date (9/20/2003 1:05 PM)
 */
public abstract class ATableFrame<E> extends ChildFrame implements ILocaleListener,
                                                                   IUserPreferencesListener,
                                                                   Observer,
                                                                   ISignalListener {
    /**
     * Initial height of table.
     */
    protected final int mInitialHeight = UIManager.getInst()
            .createLabel()
            .getFontMetrics(new Font("dialog", 0, 12))
            .getHeight() + 2;
    /**
     * Cell renderer of columns of table.
     */
    protected DefaultTableCellRenderer mCellRenderer;
    /**
     * Map of checkboxes
     */
    protected Map<String, JCheckBoxMenuItem> mCheckBoxMap = new HashMap<String, JCheckBoxMenuItem>();

    private ComparatorFactory mComparatorFactory;

    /**
     * Current sorting column.
     */
    private int mCurSortColumn = -1;
    /**
     * Is Descending mode now?
     */
    protected boolean mDescendingMode = true;
    /**
     * Image icon for anscending mode of sorting.
     */
    protected ImageIcon mDownSortIcon;
    /**
     * ITable-based instance associated with this frame.
     */
    private Table mFrameTable;
    /**
     * Header renderer of columns of table.
     */
    protected DefaultTableCellRenderer mHeaderRenderer;
    protected final DecimalFormat mFormat = new ThreadSafeNumberFormat().getInstance();
    /**
     * Localized resource manager
     */
    protected ResourceManager mResourceManager;
    /**
     * Scroll panel.
     */
    protected JScrollPane mScrollPane;

    private final Object mSDFMutex = new Object();
    /**
     * Formater for localization of date format
     */
    private SimpleDateFormat mSimpleDateFormat;
    private SimpleDateFormat mSimpleTimeFormat;
    /**
     * Column for that popup menu was opened.
     */
    protected int mSortMenuColumn = -1;
    /**
     * Popup menu for selecting of sorting mode.
     */
    private SortPopupMenu mSortPopupMenu;
    private final Object mSTFMutex = new Object();
    /**
     * Sorted table's model.
     */
    //protected SortedTableModel mSortedTableModel;
    /**
     * Table instance.
     */
    private JTable mTable;
    /**
     * Table's model.
     */
    protected AFrameTableModel mTableModel;
    private int mTitleBarHeight;
    /**
     * Image icon for descending mode of sorting.
     */
    protected ImageIcon mUpSortIcon;
    private boolean mColumnsUnset = true;

    /**
     * Protected constructor.
     * Does next:
     * <ul>
     * <li> sets title of frame;</li>
     * <li> creates table model and creates a table;</li>
     * <li> creates ITable implementation and registers it in Table Manager;</li>
     * <li> creates Scrolling pane and adds iself to Scrolling pane;</li>
     * <li> adds iself to ResourceManager to listen the locale changing;</li>
     * <li> adds Internal Frame Listener to superclass;</li>
     * </ul>
     *
     * @param aMan resource manager
     * @param aMainFrame main frame
     */
    protected ATableFrame(ResourceManager aMan, IMainFrame aMainFrame) {
        super(null, aMainFrame);
        mResourceManager = aMan;
        mComparatorFactory = new ComparatorFactory<E>(this);
        setTitle(getLocalizedTitle(mResourceManager));
        mTableModel = getTableModel(mResourceManager);
        UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName()).addObserver(this);
        PreferencesManager.getPreferencesManager(TradeDesk.getInst().getUserName()).addPreferencesListener(this);

        //xxx commented out, not compatible w/ apple laf
        //boolean b = pref.getBoolean(ToggleTitlebarAction.HIDE_TOGGLE_TOOLBAR);
        //BasicInternalFrameUI ui = (BasicInternalFrameUI) getUI();
        //try {
        //    JComponent northPane = ui.getNorthPane();
        //    Dimension preferredSize = northPane.getPreferredSize();
        //    mTitleBarHeight = preferredSize.height;
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    mTitleBarHeight = 20;
        //}
        //toggleTitleBar(b);
        //creates table

        mTable = new AAJTable(mTableModel);
        mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        mTable.setRowHeight(mInitialHeight);
        JTableHeader tableHeader = new JTableHeader(mTable.getColumnModel());
        tableHeader.setReorderingAllowed(false);
        mTable.setTableHeader(tableHeader);

        mFrameTable = new Table(getTableName());
        setName(getTableName());
        ListSelectionModel listSelectionModel = mTable.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listSelectionModel.addListSelectionListener(mFrameTable);

        //creates the scroll pane and add the table to it.
        mScrollPane = new JScrollPane(mTable);
        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent aEvent) {
                if (!isSelected()) {
                    try {
                        setSelected(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //adds mouse listenes to scrollpane and it`s scroll bars
        mScrollPane.addMouseListener(mouseListener);
        try {
            mScrollPane.getHorizontalScrollBar().addMouseListener(mouseListener);
            mScrollPane.getVerticalScrollBar().addMouseListener(mouseListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //adds the scroll pane to this window.
        getContentPane().add(mScrollPane, BorderLayout.CENTER);
        mResourceManager.addLocaleListener(this);
        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameOpened(InternalFrameEvent aEvent) {
                TableManager.getInst().add(mFrameTable);
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent aEvent) {
                onCloseFrame(aEvent);
                TableManager.getInst().remove(mFrameTable);
            }
        });

        //initializes sort icons
        URL iconUrl = mResourceManager.getResource("ID_UP_SORT_ICON");
        if (iconUrl != null) {
            mUpSortIcon = new ImageIcon(iconUrl);
        }
        URL iconUrl2 = mResourceManager.getResource("ID_DOWN_SORT_ICON");
        if (iconUrl2 != null) {
            mDownSortIcon = new ImageIcon(iconUrl2);
        }
        mSortPopupMenu = new SortPopupMenu();
        mTable.setColumnSelectionAllowed(false);
        addMouseListenerToHeaderInTable(mTable);
        mTable.addMouseListener(mouseListener);
        mSimpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        mSimpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone(TradingServerSession.getInstance().getParameterValue("BASE_TIME_ZONE"));
        if (tz != null) {
            mSimpleDateFormat.setTimeZone(tz);
            mSimpleTimeFormat.setTimeZone(tz);
        }
        mFormat.applyPattern("#,##0.0");
    }

    @Override
    protected void paintComponent(Graphics aGraphics) {
        if (UIManager.getInst().isAAEnabled()) {
            Graphics2D g2d = (Graphics2D) aGraphics;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        super.paintComponent(aGraphics);
    }

    /**
     * Adds mouse listener to header of table.
     */
    private void addMouseListenerToHeaderInTable(final JTable aTable) {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aEvent) {
                TableColumnModel columnModel = aTable.getColumnModel();
                int currentColumn = columnModel.getColumnIndexAtX(aEvent.getX());
                if (getTableModel().getColumnType(currentColumn) != AFrameTableModel.NOT_SORTABLE_COLUMN) {
                    if (aEvent.getButton() == MouseEvent.BUTTON3 || aEvent.isPopupTrigger()) {
                        //sets checked item at menu
                        if (getCurSortColumn() == currentColumn) {
                            mSortPopupMenu.setSortMode(mDescendingMode
                                                       ? SortPopupMenu.DESCENT
                                                       : SortPopupMenu.ASCENT);
                        }
                        mSortMenuColumn = currentColumn;
                        mSortPopupMenu.show(aEvent.getComponent(), aEvent.getX(), aEvent.getY());
                        revalidate();
                        repaint();
                    } else {
                        if (isSortable()) {
                            if (getCurSortColumn() == currentColumn) {
                                mDescendingMode = !mDescendingMode;
                            } else {
                                mDescendingMode = true;
                            }
                            setCurSortColumn(currentColumn);
                            fireSorting();
                        }
                    }
                }
            }
        };
        aTable.getTableHeader().addMouseListener(mouseAdapter);

        aTable.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent aEvent) {
                JTableHeader header = (JTableHeader) aEvent.getSource();
                TableColumnModel columnModel = aTable.getColumnModel();
                int colIndex = columnModel.getColumnIndexAtX(aEvent.getX());

                // Return if not clicked on any column header
                if (colIndex >= 0) {
                    TableColumn col = columnModel.getColumn(colIndex);
                    header.setToolTipText(String.valueOf(col.getIdentifier()));
                }
            }
        });

        aTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent aEvent) {
                AAJTable table = (AAJTable) aEvent.getSource();
                int row = table.rowAtPoint(aEvent.getPoint());
                int col = table.columnAtPoint(aEvent.getPoint());
                JComponent comp = (JComponent) table.getCellRenderer(row, col).getTableCellRendererComponent(aTable,
                                                                                               null,
                                                                                               false,
                                                                                               false,
                                                                                               row,
                                                                                               col);
                if (comp != null) {
                    Object valueAt = table.getValueAt(row, col);
                    if (valueAt != null) {
                        comp.setToolTipText(valueAt.toString());
                    } else {
                        comp.setToolTipText(null);
                    }
                }
            }
        });
    }

    /**
     * Fires sorting of the table.
     */
    protected void fireSorting() {
        List selected = null;
        int[] rows = mTable.getSelectedRows();
        for (int row : rows) {
            if (row < getSignalVector().size()) {
                if (selected == null) {
                    selected = new ArrayList();
                }
                selected.add(getSignalVector().get(row));
            }
        }

        sort();
        revalidate();
        repaint();

        if (selected != null && !selected.isEmpty()) {
            for (int i = 0; i < selected.size(); i++) {
                int index = getSignalVector().indexOf(selected.get(i));
                if (index != -1) {
                    if (i == 0) {
                        mTable.setRowSelectionInterval(index, index);
                    } else {
                        mTable.addRowSelectionInterval(index, index);
                    }
                }
            }
        }
    }

    protected String formatDate(Date aDate) {
        synchronized (mSDFMutex) {
            return mSimpleDateFormat.format(aDate);
        }
    }

    protected String formatTime(Date aDate) {
        synchronized (mSTFMutex) {
            return mSimpleTimeFormat.format(aDate);
        }
    }

    protected abstract Object getColumnValue(int aColumn, Object aObject);

    private Comparator getComparator() {
        if (mTableModel.getColumnType(mCurSortColumn) == AFrameTableModel.STRING_COLUMN) {
            return mComparatorFactory.getStringComparator();
        } else if (mTableModel.getColumnType(mCurSortColumn) == AFrameTableModel.INT_COLUMN) {
            return mComparatorFactory.getIntComparator();
        } else if (mTableModel.getColumnType(mCurSortColumn) == AFrameTableModel.DOUBLE_COLUMN) {
            return mComparatorFactory.getDoubleComparator();
        } else if (mTableModel.getColumnType(mCurSortColumn) == AFrameTableModel.DATE_COLUMN) {
            return mComparatorFactory.getDateComparator();
        } else {
            return mComparatorFactory.getStringComparator();
        }
    }

    protected Object getComparatorValue(Object aObj) {
        TableColumn column = mTable.getColumnModel().getColumn(mCurSortColumn);
        return getColumnValue(column.getModelIndex(), aObj);
    }

    public int getCurSortColumn() {
        return mCurSortColumn;
    }

    public void setCurSortColumn(int aCurSortColumn) {
        mCurSortColumn = aCurSortColumn;
    }

    /**
     * Returns itable-based instance associated with this frame.
     */
    public ITable getFrameTable() {
        return mFrameTable;
    }

    /**
     * Method is called for getting Localized Frame Title.<br>
     */
    protected abstract String getLocalizedTitle(ResourceManager aResourceMan);

    /**
     * Returns localized resource manager
     */
    public ResourceManager getResourceManager() {
        return mResourceManager;
    }

    /**
     * Sets localized resource manager.
     *
     * @param aMan resource manager
     */
    public void setResourceManager(ResourceManager aMan) {
        mResourceManager = aMan;
    }

    /**
     * Returns table instance.
     */
    public JScrollPane getScrollPane() {
        return mScrollPane;
    }

    protected abstract SignalVector getSignalVector();

    /**
     * Returns table instance.
     */
    public JTable getTable() {
        return mTable;
    }

    /**
     * Returns table's model.
     */
    public abstract AFrameTableModel getTableModel(ResourceManager aResourceMan);

    /**
     * Returns table's model.
     */
    public AFrameTableModel getTableModel() {
        return mTableModel;
    }

    /**
     * Method is called for getting internal table name, which identifies a table
     * into the table manager.<br>
     */
    protected abstract String getTableName();

    private void initColumnWidths(UserPreferences aPreferences) {
        //setting of column width
        String tableColumnWidthes = aPreferences.getString("childframe.table." + getTableName());
        if (tableColumnWidthes != null) {
            TableColumnModel columnModel = mTable.getColumnModel();
            StringTokenizer st = new StringTokenizer(tableColumnWidthes, ";", false);
            for (int i = 0; st.hasMoreTokens(); i++) {
                try {
                    int width = Integer.parseInt(st.nextToken());
                    int columnCount = columnModel.getColumnCount();
                    if (i < columnCount) {
                        TableColumn column = columnModel.getColumn(i);
                        column.setPreferredWidth(width);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isDescendingMode() {
        return mDescendingMode;
    }

    public abstract boolean isSortable();

    @Override
    public void loadSettings() {
        UserPreferences preferences;
        try {
            preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            super.loadSettings();
            return;
        }
        reOrderColumns();
        initColumnWidths(preferences);
        super.loadSettings();
    }

    /**
     * This method is called when current locale of the aMan is changed and becomes aLocale.
     *
     * @param aMan resource manager.
     */
    public void onChangeLocale(ResourceManager aMan) {
        mResourceManager = aMan;
        synchronized (mSDFMutex) {
            mSimpleDateFormat = new SimpleDateFormat();
        }

        //sets title
        setTitle(getLocalizedTitle(aMan));
        if (mTableModel != null) {
            int iRowSaved = mTable.getSelectedRow();
            mTableModel.onChangeLocale(aMan);
            mTable.changeSelection(iRowSaved, -1, false, false);
        }

        //sets renderer
        if (mCellRenderer != null || mHeaderRenderer != null) {
            setRenderers(mCellRenderer, mHeaderRenderer);
        }
        mTable.setRowHeight(mInitialHeight);
        mTable.revalidate();
        mTable.repaint();
    }

    /**
     * Method is called by Internal Frame Listener when frame has been closed.<br>
     * The implementation should unregister itself from Signaler and do other destruction work
     *
     * @param aE
     */
    protected void onCloseFrame(InternalFrameEvent aE) {
        mTable.removeAll();
        mTable.removeNotify();
        mResourceManager.removeLocaleListener(this);
        mCheckBoxMap.clear();
        mFrameTable.clear();
        mScrollPane.removeAll();
        mScrollPane.removeNotify();
        removeAll();
        removeNotify();
        UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName()).deleteObserver(this);
        PreferencesManager.getPreferencesManager(TradeDesk.getInst().getUserName()).removePreferencesListener(this);
    }

    public void onSignal(Signaler aSrc, ISignal aSignal) {
        mTableModel.onSignal(aSrc, aSignal);
        if (mCurSortColumn != -1 && aSignal.getType() == SignalType.CHANGE) {
            Comparator comparator = getComparator();
            if (comparator != null) {
                int aIndex = aSignal.getIndex();
                SignalVector data = getSignalVector();
                Object updated = data.get(aIndex);

                Object before = aIndex <= 0 && aIndex <= data.size() - 1 ? null : data.get(aIndex - 1);
                boolean changed = false;
                if (before != null) { //1st level test
                    changed = comparator.compare(before, updated) > 0;
                }

                if (!changed) { //2nd level test
                    Object after = aIndex >= data.size() - 1 ? null : data.get(aIndex + 1);
                    if (after != null) {
                        changed = comparator.compare(after, updated) < 0;
                    }
                }

                if (changed) {
                    fireSorting();
                }
            }
        } else if (aSignal.getType() == SignalType.REMOVE || aSignal.getType() == SignalType.ADD) {
            List selected = null;
            int[] selectedRows = mTable.getSelectedRows();
            for (int row : selectedRows) {
                if (row < getSignalVector().size()) {
                    if (selected == null) {
                        selected = new ArrayList();
                    }
                    selected.add(getSignalVector().get(row));
                }
            }
            if (selected != null && !selected.isEmpty()) {
                for (int i = 0; i < selected.size(); i++) {
                    int index = getSignalVector().indexOf(selected.get(i));
                    if (index != -1) {
                        // old index selected row value before insert into bottom or top of table
                        if (index == 0) {
                            if (mDescendingMode) {
                                index++;
                            } else {
                                index--;
                            }
                        }
                        if (i == 0) {
                            mTable.setRowSelectionInterval(index, index);
                        } else {
                            mTable.addRowSelectionInterval(index, index);
                        }
                    }
                }
            }
        }
        revalidate();
        repaint();
    }

    /**
     * Invoked on updating of preferences.
     *
     * @param aChangings vector that containts changed values
     */
    public void preferencesUpdated(Vector aChangings) {
        //This code was added for correct appling of header`s height.
        mTable.setRowHeight(mInitialHeight);
        mTable.revalidate();
        mTable.repaint();
        Rectangle rec = getBounds();
        reshape(rec.x, rec.y, rec.width + 1, rec.height);
        reshape(rec.x, rec.y, rec.width, rec.height);
    }

    protected void reOrderColumns() {
        TreeMap<Integer, TableColumn> map = new TreeMap<Integer, TableColumn>();
        TableColumnModel columnModel = mTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            map.put(tableColumn.getModelIndex(), tableColumn);
        }
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn tableColumn = columnModel.getColumn(i);
            mTable.removeColumn(tableColumn);
        }
        for (TableColumn tableColumn : map.values()) {
            mTable.removeColumn(tableColumn);
            mTable.addColumn(tableColumn);
        }
    }

    /* Saves settings to the use preferences. */
    @Override
    public void saveSettings() {
        //calls method of the base class
        super.saveSettings();
        //Get PersistenceStorage
        UserPreferences preferences;
        try {
            preferences = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        TableColumnModel columnModel = mTable.getColumnModel();
        //saving of column width
        Enumeration enumeration = columnModel.getColumns();
        String storageStr = "";
        for (int i = 0; enumeration.hasMoreElements(); i++) {
            TableColumn tc = (TableColumn) enumeration.nextElement();
            if (i != 0) {
                storageStr += ";";
            }
            storageStr += tc.getPreferredWidth();
        }
        preferences.set("childframe.table." + getTableName(), storageStr);

        //saving of column order
        Enumeration enumeration1 = columnModel.getColumns();
        String storageStr1 = "";
        for (int i = 0; enumeration1.hasMoreElements(); i++) {
            TableColumn tc = (TableColumn) enumeration1.nextElement();
            if (i != 0) {
                storageStr1 += ";";
            }
            storageStr1 += tc.getModelIndex();
        }
        preferences.set("childframe.table." + getTableName() + ".order", storageStr1);
    }

    /**
     * Sets renderers to all columns.
     */
    protected void setRenderers(DefaultTableCellRenderer aCellRender, DefaultTableCellRenderer aHeaderRender) {
        JTable table = mTable;
        TableColumnModel columnModel = table.getColumnModel();
        for (Enumeration enumeration = columnModel.getColumns(); enumeration.hasMoreElements();) {
            TableColumn column = (TableColumn) enumeration.nextElement();
            if (aCellRender != null) {
                column.setCellRenderer(aCellRender);
            }
            if (aHeaderRender != null) {
                column.setHeaderRenderer(aHeaderRender);
            }
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag && mColumnsUnset) {
            mColumnsUnset = false;
            UserPreferences prefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            int columnCount = mTable.getColumnModel().getColumnCount();
            List<TableColumn> removals = new ArrayList<TableColumn>();
            for (int i = 0; i < columnCount; i++) {
                TableColumn tableColumn = mTable.getColumnModel().getColumn(i);
                boolean show = true;
                String key = "childframe.table." + getTableName() + ".col." + i;
                if (prefs.getString(key) != null) {
                    show = prefs.getBoolean(key);
                }
                if (!show) {
                    removals.add(tableColumn);
                }
            }
            TableColumn tc = null;
            if (mCurSortColumn != -1) {
                tc = mTable.getColumnModel().getColumn(mCurSortColumn);
            }
            for (TableColumn column : removals) {
                mTable.removeColumn(column);
            }
            //reset the sort column if count changed
            if (tc != null) {
                try {
                    mCurSortColumn = mTable.getColumnModel().getColumnIndex(tc.getIdentifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            revalidate();
            repaint();
        }
    }

    protected void sort() {
        if (isSortable()) {
            getSignalVector().setComparator(getComparator());
        }
    }

    public void toggleTitleBar(boolean aBoolean) {
        if (aBoolean) {
            BasicInternalFrameUI frameUI = (BasicInternalFrameUI) getUI();
            JComponent northPane = frameUI.getNorthPane();
            northPane.setPreferredSize(new Dimension(0, 0));
            revalidate();
            repaint();
        } else {
            BasicInternalFrameUI frameUI = (BasicInternalFrameUI) getUI();
            frameUI.getNorthPane().setPreferredSize(new Dimension(getWidth(), mTitleBarHeight));
            revalidate();
            repaint();
        }
    }

    /**
     * @param aObservable
     * @param arg
     */
    public void update(Observable aObservable, Object arg) {
        if (arg.toString().endsWith(IClickModel.TRADING_MODE)) {
            updateTitle();
        }
    }

    public void updateTitle() {
        setTitle(getLocalizedTitle(mResourceManager));
    }

    /**
     * Popup menu for selecting of type of sorting of column.
     * This popup menu appears by right boutton of mouse on column header.
     */
    private class SortPopupMenu extends JPopupMenu {
        /**
         * Ascent sorting.
         */
        private static final int ASCENT = 1;
        /**
         * Descent sorting.
         */
        private static final int DESCENT = 2;
        /**
         * Ascend mode menu item.
         */
        private JCheckBoxMenuItem mAscentItem;
        /**
         * Descend mode menu item.
         */
        private JCheckBoxMenuItem mDescentItem;

        /**
         * Constructor.
         */
        SortPopupMenu() {
            UIManager oMan = UIManager.getInst();
            if (isSortable()) {
                //first menu item
                mAscentItem = oMan.createCheckBoxMenuItem("IDS_ASCEND_MODE", null, null, "IDS_ASCEND_MODE_DESC");
                mAscentItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent aE) {
                        setCurSortColumn(mSortMenuColumn);
                        mDescendingMode = false;
                        fireSorting();
                        getTable().getTableHeader().revalidate();
                        getTable().getTableHeader().repaint();
                    }
                });
                add(mAscentItem);
            }

            if (isSortable()) {
                //second menu item
                mDescentItem = oMan.createCheckBoxMenuItem("IDS_DESCEND_MODE", null, null, "IDS_DESCEND_MODE_DESC");
                mDescentItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent aE) {
                        setCurSortColumn(mSortMenuColumn);
                        mDescendingMode = true;
                        fireSorting();
                        getTable().getTableHeader().revalidate();
                        getTable().getTableHeader().repaint();
                    }
                });
                add(mDescentItem);
            }

            add(UIManager.getInst().createMenuItem(new AbstractAction("Reset Columns") {
                public void actionPerformed(ActionEvent aEvent) {
                    UIManager.getInst().packColumns(mTable, 10);
                }
            }));
            addSeparator();
            final UserPreferences prefs = UserPreferences.getUserPreferences(TradeDesk.getInst().getUserName());
            int columnCount = mTable.getColumnCount();
            for (int i = 0; i < columnCount; i++) {
                final String name = mTable.getColumnName(i);
                final TableColumn tableColumn = mTable.getColumn(name);
                final String key = "childframe.table." + getTableName() + ".col." + i;
                Action action = new AbstractAction(name) {
                    public void actionPerformed(ActionEvent aEvent) {
                        JCheckBoxMenuItem source = (JCheckBoxMenuItem) aEvent.getSource();
                        TableColumn sortedTC  = null;
                        if (source.isSelected()) {
                            mTable.addColumn(tableColumn);
                            reOrderColumns();
                            if (mTable.getColumnCount() == 2) {
                                mCheckBoxMap.get(mTable.getColumnName(0)).setEnabled(true);
                                mCheckBoxMap.get(mTable.getColumnName(1)).setEnabled(true);
                            }
                        } else {
                            if (isSortable()) {
                                sortedTC = mTable.getColumnModel().getColumn(getCurSortColumn());
                            }
                            mTable.removeColumn(tableColumn);
                            if (mTable.getColumnCount() == 1) {
                                mCheckBoxMap.get(mTable.getColumnName(0)).setEnabled(false);
                            }
                        }

                        prefs.set(key, source.isSelected());
                        if (sortedTC != null) {
                            try {
                                setCurSortColumn(mTable.getColumnModel().getColumnIndex(sortedTC.getIdentifier()));
                            } catch (Exception e) {
                                setCurSortColumn(0);
                            }
                            fireSorting();
                        }
                    }
                };
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(action) {
                    @Override
                    protected PropertyChangeListener createActionPropertyChangeListener(Action a) {
                        return new WeakActionPropertyChangeListener(this, a);
                    }

                    @Override
                    public void addPropertyChangeListener(PropertyChangeListener aListener) {
                        super.addPropertyChangeListener(new WeakPropertyChangeListener(aListener, this));
                    }
                };
                mCheckBoxMap.put(name, item);
                boolean show = true;
                if (prefs.getString(key) != null) {
                    show = prefs.getBoolean(key);
                }
                item.setSelected(show);
                add(item);
            }
        }

        @Override
        protected JMenuItem createActionComponent(Action aAction) {
            JMenuItem mi = UIManager.getInst().createMenuItem();
            mi.setText((String) aAction.getValue(Action.NAME));
            mi.setIcon((Icon) aAction.getValue(Action.SMALL_ICON));
            mi.setHorizontalTextPosition(JButton.TRAILING);
            mi.setVerticalTextPosition(JButton.CENTER);
            mi.setEnabled(aAction.isEnabled());
            return mi;
        }

        /**
         * Sets sort mode.
         *
         * @param aMode mode of sorting
         */
        public void setSortMode(int aMode) {
            if (aMode == ASCENT) {
                mAscentItem.setState(true);
                mDescentItem.setState(false);
            } else if (aMode == DESCENT) {
                mAscentItem.setState(false);
                mDescentItem.setState(true);
            }
        }
    }

    /**
     * An inner class which implements ITable interface for TableManager.<br>
     * Notifies the TableManager about selection changing<br>
     */
    private class Table extends ATable implements ListSelectionListener {
        /**
         * Constructor is protected and takes name of the table.
         */
        Table(String aName) {
            super(aName);
        }

        /**
         * Returns selected (current) row.
         */
        public int getSelectedRow() {
            return getTable().getSelectedRow();
        }

        public void setSelectedRow(int aRow) {
            fireChangeSelection(new int[]{aRow});
        }

        /**
         * Method notifies the TableManager about selection changing.
         */
        public void valueChanged(ListSelectionEvent aEvent) {
            if (!aEvent.getValueIsAdjusting()) {
                fireChangeSelection(getTable().getSelectedRows());
            }
        }
    }
}
