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
package imi.gui.jtable.cellrenderer;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class takes care of rendering a boolean value within a table cell.
 * DefaultTableCellRenderer was chosen as a superclass because of some painting
 * optimizations put into that class by sun microsystems
 * @author Ronald E Dahlgren
 */
public class BooleanCellRenderer extends DefaultTableCellRenderer
{
    /** The check box used to indicate the boolean state **/
    private final JCheckBox   m_checkBox = new JCheckBox();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        m_checkBox.setSelected(((Boolean)value).booleanValue());
        return m_checkBox;
    }

}
