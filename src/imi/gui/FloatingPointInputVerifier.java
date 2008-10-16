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
package imi.gui;

import java.text.ParseException;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;

/**
 * Verify that some text is a valid representation of a floating point number
 * @author Ronald E Dahlgren
 */
public class FloatingPointInputVerifier extends InputVerifier
{

    @Override
    public boolean verify(JComponent input) 
    {
        if (input instanceof JFormattedTextField) // only implemented for formatted text field
        {
            JFormattedTextField field = (JFormattedTextField)input;
            AbstractFormatter formatter = field.getFormatter();
            if (formatter != null) 
            {
                String text = field.getText();
                try 
                {
                    formatter.stringToValue(text);
                    try
                    {
                        Double.parseDouble(text);
                    }
                    catch (NumberFormatException e)
                    {
                        // Invalid format, not validated
                        return false;
                    }
                    // parsing succeeded, must be ok
                    return true;
                } 
                catch (ParseException pe) // Formatter couldn't even handle it... must be bad
                {
                    return false;
                }
            }
          }
        // unimplemented control
        return true;
    }

}
