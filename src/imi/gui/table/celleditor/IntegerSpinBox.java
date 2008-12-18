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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * This class provides a JSpinner as the editing mechanism for a given integral
 * type.
 * @author Ronald E Dahlgren
 */
public class IntegerSpinBox extends AbstractCellEditor implements TableCellEditor
{
    private final JSpinner m_spinBox = new JSpinner();
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (!isSelected)
            return null;
        m_spinBox.setValue((Integer)value);
        return m_spinBox;
    }

    public Object getCellEditorValue()
    {
        return m_spinBox.getValue();
    }

}
