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
 * This represents all of the animation frames a joint can go through
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public interface PJointChannel 
{
    /**
     * This method should return a brand new copy with the same innards as 
     * this instance.
     * @return The new copy
     */
    public PJointChannel copy();
    
    /**
     * This method is used to determine a particular joint's position
     * at the specified time in the channel
     * @param jointToAffect The joint whose PTransform will be modified
     * @param fTime
     */
    public void calculateFrame(PJoint jointToAffect, AnimationState state);
    
    /**
     * This method performs weighted blending on the two frames and
     * applies the result to the jointToAffect
     * @param jointToAffect The target joint
     * @param fTime1 The time of the first frame
     * @param fTime2 The time of the second frame
     * @param s used in Frame1 * (1-s) + Frame2 * s; ranges from 0 to 1
     */
    public void calculateBlendedFrame(PJoint jointToAffect, AnimationState state);

    /**
     * Calculates and returns the duration of the BoneAnimation.
     * @return float
     */
    public float calculateDuration();

    /**
     * Calculates and returns the average step time.
     * @return float
     */
    public float calculateAverageStepTime();

    /**
     * Gets and returns the average step time.
     * @return float
     */
    public float getAverageStepTime();

    /**
     * Returns the string representation of the joint this PJointChannel is intended for
     * @return String
     */
    public String getTargetJointName(); 

    /**
     * Dumps the JointChannel.
     */
    public void dump(String spacing);

    /**
     * Trims the JointChannel of Keyframes that are after the specified time.
     * @param fMaxTime The max keyframe time that should remain in the JointChannel.
     */
    public void trim(float fMaxTime);

    /**
     * Clears the JointChannel.
     */
    public void clear();

    /**
     * Returns the starttime of the JointChannel.
     * @return float
     */
    public float getStartTime(); 

    /**
     * Returns the endtime of the JointChannel.
     * @return float
     */
    public float getEndTime(); 

    /**
     * Adjusts all the keyframe times.
     * @param fAmount The amount to adjust each keyframe time by.
     */
    public void adjustKeyframeTimes(float fAmount);

    /**
     * Appends a JointChannel onto the end of this JointChannel.
     * @param pJointChannel The JointChannel to append onto this one.
     */
    public void append(PJointChannel pJointChannel);

}




