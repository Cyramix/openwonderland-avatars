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
package imi.gui;

import imi.scene.PMatrix;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Ronald E Dahlgren
 */
public class PMatrixTableModel extends AbstractTableModel
{
    private PMatrix         m_Matrix = null; // the data to operate on
    private String[]        m_ColumnNames = new String[4];
    private NumberFormat    m_format            = new DecimalFormat("#,###.######");
    private String          m_formattedNumber   = null;
    
    public PMatrixTableModel(PMatrix target)
    {
        super();
        m_Matrix = target;
        m_ColumnNames[0] = new String("X");
        m_ColumnNames[1] = new String("X");
        m_ColumnNames[2] = new String("Z");
        m_ColumnNames[3] = new String("T");
    }
    
    public int getRowCount() 
    {
        return 4;
    }

    public int getColumnCount() 
    {
        return 4;
    }

    public Object getValueAt(int rowIndex, int columnIndex) 
    {
        m_formattedNumber = m_format.format(m_Matrix.getFloatArray()[columnIndex + rowIndex * 4]);
        return Float.valueOf(m_formattedNumber);
    }

    @Override
    public int findColumn(String columnName) 
    {
        for (int i = 0; i < m_ColumnNames.length; ++i)
            if (m_ColumnNames[i].equals(columnName))
                return i;
        return -1;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) 
    {
        return Float.class;
    }

    @Override
    public String getColumnName(int column) 
    {
        if (column < m_ColumnNames.length)
            return m_ColumnNames[column];
        else
            return null;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) 
    {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) 
    {
        // First turn this bastard into a float
        float[] fArray = m_Matrix.getFloatArray();
        m_formattedNumber = m_format.format((Float)aValue);
        fArray[columnIndex + rowIndex * 4] = Float.valueOf(m_formattedNumber);
        m_Matrix.set(fArray);
    }
    
    // accessor
    public PMatrix getMatrix()
    {
        return m_Matrix;
    }
    
    // mutator
    public void setMatrix(PMatrix newMatrix)
    {
        m_Matrix = newMatrix;
    }
    
    public void refreshComponents()
    {
        this.fireTableRowsUpdated(0, 3);
    }
}
