/*
 * $History: $
 */
package fxts.stations.trader.ui;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class EachRowEditor implements TableCellEditor {
    protected TableCellEditor mDefaultEditor;
    protected TableCellEditor mEditor;
    protected Map<Integer, TableCellEditor> mEditors;
    protected JTable mTable;

    public EachRowEditor(JTable aTable) {
        mTable = aTable;
        mEditors = new HashMap<Integer, TableCellEditor>();
        mDefaultEditor = new DefaultCellEditor(new JTextField());
    }

    public void addCellEditorListener(CellEditorListener aCellEditorListener) {
        mEditor.addCellEditorListener(aCellEditorListener);
    }

    public void cancelCellEditing() {
        mEditor.cancelCellEditing();
    }

    public Object getCellEditorValue() {
        return mEditor.getCellEditorValue();
    }

    public Component getTableCellEditorComponent(JTable aTable,
                                                 Object aValue,
                                                 boolean aSelected,
                                                 int aRow,
                                                 int aColumn) {
        return mEditor.getTableCellEditorComponent(aTable, aValue, aSelected, aRow, aColumn);
    }

    public boolean isCellEditable(EventObject anEvent) {
        selectEditor((MouseEvent) anEvent);
        return mEditor.isCellEditable(anEvent);
    }

    public void removeCellEditorListener(CellEditorListener aCellEditorListener) {
        mEditor.removeCellEditorListener(aCellEditorListener);
    }

    protected void selectEditor(MouseEvent aEvent) {
        int row;
        if (aEvent == null) {
            row = mTable.getSelectionModel().getAnchorSelectionIndex();
        } else {
            row = mTable.rowAtPoint(aEvent.getPoint());
        }
        mEditor = mEditors.get(row);
        if (mEditor == null) {
            mEditor = mDefaultEditor;
        }
    }

    public void cleanup() {
        mEditors.clear();
        mTable = null;
    }

    public void setEditorAt(int aRow, TableCellEditor aEditor) {
        mEditors.put(aRow, aEditor);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        selectEditor((MouseEvent) anEvent);
        return mEditor.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing() {
        return mEditor.stopCellEditing();
    }
}
