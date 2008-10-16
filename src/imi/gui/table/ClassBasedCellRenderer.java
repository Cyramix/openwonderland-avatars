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
 * $Revision$
 * $Date$
 * $State$
 */
package imi.gui.table;

import java.awt.Component;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Ronald E Dahlgren
 */
public class ClassBasedCellRenderer implements TableCellRenderer 
{
    private HashMap<Class, TableCellRenderer>   m_classMap  = new HashMap<Class, TableCellRenderer>();
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        TableCellRenderer result = null;
        if (value != null)
            result = m_classMap.get(value.getClass());
        if (result == null)
            result = new DefaultTableCellRenderer();
        
        return result.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
    
    public void setClassRenderer(Class classz, TableCellRenderer renderer)
    {
        m_classMap.put(classz, renderer);
    }

}
