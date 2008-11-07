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
package imi.scene.animation;

import imi.scene.PJoint;

/**
 * An interface for an animated instance
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public interface Animated 
{
    /**
     * Retrieve the animation state at index zero. 
     * @return The first animation state, or null if none is present
     */
    public AnimationState getAnimationState();
    
    /**
     * Retrieve the animation component
     * @return The component
     */
    public AnimationComponent getAnimationComponent();
    /**
     * Retrieve the animation state at the specified index.
     * @param index 
     * @return The animation state or null if not available
     */
    public AnimationState getAnimationState(int index);
    
    /**
     * Add a new animation state to the collection
     * @param newState
     * @return The index of the newly added state
     */
    public int addAnimationState(AnimationState newState);
    /**
     * This method retrieves the joint with the specified name
     * @param jointName The name of the joint
     * @return the joint!
     */
    public PJoint getJoint(String jointName);
    
    /**
     * Controls the dirtiness of this automaton.
     * @param bDirty
     * @param bAffectKids
     */
    public void setDirty(boolean bDirty, boolean bAffectKids);
}
