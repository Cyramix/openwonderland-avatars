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
package imi.utils.input;

import java.awt.event.KeyEvent;

/**
 *
 * @author Lou Hayt
 */
public class LousMostBestSchemeBetterThanOtherOnes extends InputScheme
{

    @Override
    public void processEvents(Object[] events) 
    {
        for (int i=0; i<events.length; i++) 
        {
            if (events[i] instanceof KeyEvent) 
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
    }
      
    private void processKeyEvent(KeyEvent ke) 
    {
        if (ke.getID() == KeyEvent.KEY_PRESSED) 
        {
            if (ke.getKeyCode() == KeyEvent.VK_U)
            {
                m_jscene.toggleWireframe();
            }
        }
    }

    @Override
    public void processMouseEvents(Object[] events) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
