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
package imi.character.behavior;

import imi.objects.SpatialObject;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This interface represents an individual task for the controller to handle.
 * @author Lou Hayt
 */
@ExperimentalAPI
public interface Task 
{
    /**
     * Return a human-readable string describing this task
     * @return
     */
    public String getDescription();

    /**
     * Return a human-readable string describing the current status of the task.
     * @return
     */
    public String getStatus();

    /**
     * Verifies that the task is still valid (not timed out, still has good
     * data, etc)
     * @return true if valid, false otherwise.
     */
    public boolean verify();

    /**
     * Drive the update of this task
     * @param deltaTime The timestep
     */
    public void update(float deltaTime);

    /**
     * This method informs the task that it is now on hold.
     */
    public void onHold();

    /**
     * Retrieve the goal of this task (if applicable)
     * @return
     */
    public SpatialObject getGoal();
}
