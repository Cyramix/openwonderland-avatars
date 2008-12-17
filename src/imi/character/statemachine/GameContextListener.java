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
package imi.character.statemachine;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;

/**
 * Listener interface for CharacterController.
 *
 * @author paulby
 */
public interface GameContextListener {

    /**
     * Inform the listener that a trigger has occured.
     * @param pressed True if the trigger is being engaged, false is disengaging
     * @param trigger The trigger type
     * @param location
     * @param rotation
     */
    public void trigger(boolean pressed, int trigger, Vector3f location, Quaternion rotation);


}
