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
package imi.scene.animation.keyframe;

import imi.scene.PMatrix;

/**
 * Provide a common interface for querying Keyframe type objects.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public interface KeyframeInterface
{

    /**
     * Adjusts the keyframe's time by the specified amount (in seconds)
     * @param fAmount
     */
    public void adjustTime(float fDelta);

    /**
     * Return the time that this keyframe occurs
     * @return The time
     */
    public float getFrameTime();

    /**
     * Sets this keyframe's time to the value provided
     * @param newTime
     * @return
     */
    public void setFrameTime(float newTime);
    
    /**
     * Determine equality between two implementors
     * @param other
     * @return
     */
    public boolean equals(KeyframeInterface other);

    /**
     * Return a newly constructed copy of this key frame
     * @return
     */
    public KeyframeInterface duplicate();

    /**
     * Place the value of the keyframe in the provided matrix
     * @param out
     */
    public void valueAsPMatrix(PMatrix out);
}
