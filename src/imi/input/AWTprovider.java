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

package imi.input;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;

/**
 * This processor receives AWT events and provides them to the InputManagerEntity
 * @author Lou Hayt
 */
public final class AWTprovider extends AWTEventProcessorComponent
{
    InputManagerEntity inputManagerEntity = null;

    public AWTprovider(AWTInputComponent listener, InputManagerEntity inputManagerEntity)
    {
        super(listener);
        setEntity(inputManagerEntity);
        this.inputManagerEntity = inputManagerEntity;
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) 
    {
        Object [] events = getEvents();
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof KeyEvent)
                inputManagerEntity.processKeyEvent((KeyEvent)events[i]);
            else if (events[i] instanceof MouseEvent)
                inputManagerEntity.processMouseEvent((MouseEvent)events[i]);
        }
    }    
}
