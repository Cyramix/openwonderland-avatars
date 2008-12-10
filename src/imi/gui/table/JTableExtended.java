/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package imi.gui.table;

import imi.gui.table.EdittableRowModel;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author Ronald E Dahlgren
 */
public class JTableExtended extends JTable
{
    private EdittableRowModel m_rowModel = null;
    
    public JTableExtended()
    {
        super();
        m_rowModel = null;
    }

    public JTableExtended(TableModel tm)
    {
        super(tm);
        m_rowModel = null;
    }

    public JTableExtended(TableModel tm, TableColumnModel cm)
    {
        super(tm, cm);
        m_rowModel = null;
    }

    public JTableExtended(TableModel tm, TableColumnModel cm,
                   ListSelectionModel sm)
    {
        super(tm, cm, sm);
        m_rowModel = null;
    }

    public JTableExtended(int rows, int cols)
    {
        super(rows, cols);
        m_rowModel = null;
    }

    public JTableExtended(final Vector rowData, final Vector columnNames)
    {
        super(rowData, columnNames);
        m_rowModel = null;
    }

    public JTableExtended(final Object[][] rowData, final Object[] colNames)
    {
        super(rowData, colNames);
        m_rowModel = null;
    }

    // new constructor
    public JTableExtended(TableModel tm, EdittableRowModel rm)
    {
        super(tm, null, null);
        m_rowModel = rm;
    }

    public void setRowEditorModel(EdittableRowModel rm)
    {
        m_rowModel = rm;
    }

    public EdittableRowModel getRowEditorModel()
    {
        return m_rowModel;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int col)
    {
        TableCellEditor result = null;
        
        if (m_rowModel != null)
        {
            if (getValueAt(row, col) != null)
                result = m_rowModel.getEditor(getValueAt(row, col).getClass());
            else
                result = null;
        }
        
        if (result != null)
        {
            return result;
        }
        return super.getCellEditor(row, col);
    }
}


