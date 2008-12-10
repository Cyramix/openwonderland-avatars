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
package imi.gui.table.celleditor;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author dahlgren
 */
public class IntegerVectorCellEditor extends AbstractCellEditor implements TableCellEditor
{
    private JPanel      m_panelCollection   = new JPanel();
    private JSpinner[]  m_spinnerArray      = new JSpinner[4];
    
    public IntegerVectorCellEditor()
    {
        super();
        // inialize!
        m_spinnerArray[0] = new JSpinner();
        m_spinnerArray[1] = new JSpinner();
        m_spinnerArray[2] = new JSpinner();
        m_spinnerArray[3] = new JSpinner();
        
        // add them to the panel
        m_panelCollection.add(m_spinnerArray[0]);
        m_panelCollection.add(m_spinnerArray[1]);
        m_panelCollection.add(m_spinnerArray[2]);
        m_panelCollection.add(m_spinnerArray[3]);
    }
    
    public Object getCellEditorValue()
    {
        int[] result = new int[4];
        
        result[0] = (Integer)m_spinnerArray[0].getValue();
        result[1] = (Integer)m_spinnerArray[1].getValue();
        result[2] = (Integer)m_spinnerArray[2].getValue();
        result[3] = (Integer)m_spinnerArray[3].getValue();
        
        return result;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (!isSelected)
            return null;
        int[] iArray = (int[])value;
        int i = 0;
        for (i = 0; i < iArray.length && i < 4; ++i)
        {
            m_spinnerArray[i].setEnabled(true);
            m_spinnerArray[i].setValue(Integer.valueOf(iArray[i]));
        }
        if (iArray.length < 4)
        {
            while (i < 4)
            {
                m_spinnerArray[i].setValue(Integer.valueOf(0));
                m_spinnerArray[i].setEnabled(false);
                ++i;
            }
        }
        
        return m_panelCollection;
    }

}
