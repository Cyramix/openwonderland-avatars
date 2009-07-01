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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This component is responsible for displaying an array of up to four floats
 * @author Ronald E Dahlgren
 */
public class FloatVectorCellRenderer extends DefaultTableCellRenderer
{
    /** The container for the four labels **/
    private final JPanel      m_panelContainer = new JPanel();
    /** Labels to display the component values **/
    private final JLabel[]    m_displayLabels = new JLabel[4];

    /**
     * Construct a new instance
     */
    public FloatVectorCellRenderer()
    {
        super();
        // initialize collection
        m_displayLabels[0] = new JLabel("L0");
        m_displayLabels[1] = new JLabel("L1");
        m_displayLabels[2] = new JLabel("L2");
        m_displayLabels[3] = new JLabel("L3");
        
        // place them in the JPanel
        m_panelContainer.add(m_displayLabels[0]);
        m_panelContainer.add(m_displayLabels[1]);
        m_panelContainer.add(m_displayLabels[2]);
        m_panelContainer.add(m_displayLabels[3]);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        
        float[] fArray = (float[])value;
        int i = 0;
        for (i = 0; i < fArray.length && i < 4; ++i)
            m_displayLabels[i].setText(Float.toString(fArray[i]) + ",");
        if (fArray.length < 4) // Did we fill up all the labels?
        {
            while (i < 4)
            {
                m_displayLabels[i].setText("N/A,");
                m_displayLabels[i].setEnabled(false);
                i++;
            }
        }
        
        return m_panelContainer;
    }
}
