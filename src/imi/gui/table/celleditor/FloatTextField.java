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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

/**
 * This class allows for editing of floating point values.
 * @author Ronald E Dahlgren
 */
public class FloatTextField extends DefaultCellEditor
{
    /**
     * Construct a new instance with the provided text field
     * @param text
     */
    public FloatTextField(JTextField text)
    {
        super(text);
    }

    @Override
    public Object getCellEditorValue()
    {
        Float result = null;
        try
        {
            result = Float.valueOf((String)super.getCellEditorValue());
        }
        catch (NumberFormatException ex)
        {
            Logger.getLogger(FloatTextField.class.getName()).log(Level.WARNING,
                    "Incorrect format for a float!", ex);
            // Set that element to zero
            result = Float.valueOf(0.0f);
        }
        return result;
    }

}
