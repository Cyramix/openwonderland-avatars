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

import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * This class provides a row model that allows for using multiple cell editors
 * within a given column.
 * @author Ronald E Dahlgren
 */
public class EdittableRowModel 
{
    /** Map classes to their corresponding cell editors **/
    private final HashMap<Class, TableCellEditor> m_editorMap = new HashMap<Class, TableCellEditor>();

    /**
     * Construct a new instance
     */
    public EdittableRowModel()
    {
        
    }
    
    /**
     * Add the specified mapping to this row model
     * @param classz The type to associate
     * @param editor The editor for that type
     * @return true if this is the first entry for this class (false otherwise)
     */
    public boolean addClassEditor(Class classz, TableCellEditor editor)
    {
        boolean result = !m_editorMap.containsKey(classz);
        m_editorMap.put(classz, editor);
        return result;
    }
    
    /**
     * This method retrieves the associated editor for this class.
     * @param classz
     * @return The associated editor, or the default if no editor is specified.
     */
    public TableCellEditor getEditor(Class classz)
    {
        if (m_editorMap.containsKey(classz))
            return m_editorMap.get(classz);
        else
            return new DefaultCellEditor(new JTextField("Default"));
    }
}
