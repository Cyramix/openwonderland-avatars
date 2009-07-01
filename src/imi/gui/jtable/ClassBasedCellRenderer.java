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
package imi.gui.jtable;

import java.awt.Component;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * This TableCellRenderer provides class based rendering of data based on the
 * internally maintained map of TableCellRenderer's to Class types. It is essentially
 * delegating the cell rendering to the appropriate cell renderer.
 * @author Ronald E Dahlgren
 */
public class ClassBasedCellRenderer implements TableCellRenderer 
{
    /** Mapping of class to cell renderer for dynamic binding of classes to renderers **/
    private final HashMap<Class, TableCellRenderer>   m_classMap  = new HashMap<Class, TableCellRenderer>();

    /**
     * Return the appropriate component given the provided parameters. Required
     * for TableCellRenderer interface.
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return
     */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        TableCellRenderer result = null;
        if (value != null)
            result = m_classMap.get(value.getClass());
        if (result == null)
            result = new DefaultTableCellRenderer();
        
        return result.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    /**
     * Set the TableCellRenderer that should be associated with the specified
     * class.
     * @param classz The class type in question
     * @param renderer The renderer that should be used for this class type.
     */
    public void setClassRenderer(Class classz, TableCellRenderer renderer)
    {
        m_classMap.put(classz, renderer);
    }

}
