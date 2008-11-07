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

/**
 * Provides an interface for manipulating an animated character. This deals
 * with the specialized case of animation "decaling"
 * @author Ronald E Dahlgren
 */
public interface AnimatedCharacter extends Animated
{
    /**
     * Advance the time for every AnimationState contained within the implementor.
     * @param deltaTime
     */
    public void advanceAllStateTimes(float deltaTime);
}
