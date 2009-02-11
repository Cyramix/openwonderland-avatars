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
package imi.scene.animation.channel;

import imi.scene.animation.*;
import imi.scene.animation.keyframe.KeyframeInterface;
import imi.scene.PJoint;
import java.io.Serializable;

/**
 * This represents all of the animation frames a joint can go through.
 * Implementing classes should take note to ensure that the implementing methods
 * are as optimal as possible and avoid object creation. The methods will be
 * called many times per frame.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public interface PJointChannel 
{
    /**
     * This method is used to determine a particular joint's position
     * at the specified time in the channel
     * @param jointToAffect The joint whose PTransform will be modified
     * @param fTime
     */
    public void calculateFrame(PJoint jointToAffect, AnimationState state);

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
     * This method reduces the number of keyframes such that only one in
     * <code>ratio</code> remain. The first and last keyframes will be retained,
     * as well as ensuring that some minimum number of keyframes remains
     * to ensure visually correct functionality.
     * @param ratio
     */
    public void fractionalReduction(int ratio);

    /**
     * Reduce the density of keyframes
     * @param newSampleFPS
     */
    public void timeBasedReduction(int newSampleFPS);

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
     * Clears the JointChannel.
     */
    public void clear();

    /**
     * Returns the end time of the JointChannel.
     * @return float
     */
    public float getEndTime();

    /**
     * Adjusts all the keyframe times.
     * @param fAmount The amount to adjust each keyframe time by.
     */
    public void adjustKeyframeTimes(float fAmount);

    /**
     * Duplicate the first keyframe and place it at the end with the same
     * average step between frames.
     */
    public void closeChannel();

    /**
     * Applies the pose for this transition.
     * @param joint
     * @param state
     * @param lerpCoefficient
     */
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient);
}




