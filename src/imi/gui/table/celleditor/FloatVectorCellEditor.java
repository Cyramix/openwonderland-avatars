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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

/**
 * This class handles editing of float arrays up to size four
 * @author Ronald E Dahlgren
 */
public class FloatVectorCellEditor extends AbstractCellEditor implements TableCellEditor
{
    /** Panel that stores the text fields **/
    private final JPanel          m_panelCollection = new JPanel();
    /** Text fields for showing vector component values **/
    private final JTextField[]    m_textFields      = new JTextField[4];

    public FloatVectorCellEditor()
    {
        super();
        // do some initialization
        m_textFields[0] = new JTextField("Unset");
        m_textFields[1] = new JTextField("Unset");
        m_textFields[2] = new JTextField("Unset");
        m_textFields[3] = new JTextField("Unset");
        
        
        // add to the panel
        m_panelCollection.add(m_textFields[0]);
        m_panelCollection.add(m_textFields[1]);
        m_panelCollection.add(m_textFields[2]);
        m_panelCollection.add(m_textFields[3]);
    }
    
    public Object getCellEditorValue()
    {
        float[] fArray = new float[4];
        int i = 0;
        for (i = 0; i < 4; ++i)
        {
            if (m_textFields[i].getText() != null && m_textFields[i].isEnabled())
            {
                try
                {
                    fArray[i] = Float.parseFloat((String)m_textFields[i].getText());
                }
                catch(NumberFormatException ex)
                {
                    Logger.getLogger(FloatVectorCellEditor.class.getName()).log(Level.WARNING,
                            "Incorrect format for a float!", ex);
                    // Set that element to zero
                    fArray[i] = 0.0f;
                }
            }
            else // null text value
                break;
        }
        // are all slots used?
        float[] result = new float[i];
        for (i = 0; i < result.length; ++i)
            result[i] = fArray[i];
        return result;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        if (!isSelected)
            return null;
        float[] fArray = (float[])value;
        int i = 0;
        // load up the text fields for editting
        for (i = 0; i < fArray.length && i < 4; ++i)
        {
            // set size restrictions
            m_textFields[i].setEnabled(true);
            m_textFields[i].setText(Float.toString(fArray[i]));
            m_textFields[i].setColumns(3);
        }
        if (fArray.length < 4)
        {
            while (i < 4)
            {
                m_textFields[i].setText("N/A");
                i++;
            }
        }
        return m_panelCollection;
    }

}
