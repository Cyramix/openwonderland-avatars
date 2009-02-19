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

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.util.BitSet;
import java.util.logging.Logger;

/**
 * This class is responsible for optimizing joint channels.
 * @author Ronald E Dahlgren
 */
public class ChannelOptimizer
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(ChannelOptimizer.class.getName());
    /** State indication**/
    private final BitSet indicationBits = new BitSet(8);
    /** Quality indicator **/
    private float quality = 1.0f;

    public ChannelOptimizer()
    {

    }

    /**
     * Optimizes the provided channel using the provided quality guidelines. The
     * quality float should be a normalized value with 1.0 representing lossless
     * techniques only and 0.0 representing maximum compression and optimization.
     * The provided channel MAY BE MODIFIED as part of the optimization process.
     * @param channel
     * @param quality
     * @return
     */
    public PJointChannel optimize(PJointChannel channel, float quality) {
        this.quality = quality;
        indicationBits.clear();
        if (channel instanceof PMatrix_JointChannel)
            return optimizeMatrixChannel((PMatrix_JointChannel)channel);
        else
            return channel;
    }

    private void fillChannelWithKeyframes(PMatrix_JointChannel channel, OptimizedChannel result)
    {
        int index = 0;
        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : channel.m_KeyFrames)
        {
            if (index == 0 && keyframe.time > 0)
                logger.warning("First keyframe was not at time zero, was at " + keyframe.time +", I will fix it.");
            result.addKeyframe(keyframe.time, keyframe.value);
            index++;
        }
    }

    private PJointChannel optimizeMatrixChannel(PMatrix_JointChannel channel)
    {
        PJointChannel result = null;
        // first, do some frame reduction
        if (quality < 0.99f)
            channel.timeBasedReduction((int) (60 * quality)); // New FPS is a function of the base 60 FPS
        // Determine properties of the channel
        indicationBits.set(OptimizedChannel.CONSTANT_TRANSLATION);
        indicationBits.set(OptimizedChannel.CONSTANT_X_AXIS);
        indicationBits.set(OptimizedChannel.CONSTANT_Y_AXIS);
        indicationBits.set(OptimizedChannel.CONSTANT_Z_AXIS);

        // Grab some defaults to compare against
        PMatrix firstTransform = channel.m_KeyFrames.getFirst().value;
        Vector3f translationVec = firstTransform.getTranslation();
        Vector3f initialxAxis = firstTransform.getLocalX();
        Vector3f initialyAxis = firstTransform.getLocalY();
        Vector3f initialzAxis = firstTransform.getLocalZ();

        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : channel.m_KeyFrames)
        {
            if (keyframe.value.getTranslation().equals(translationVec) == false)// difference
                indicationBits.clear(OptimizedChannel.CONSTANT_TRANSLATION);

            // x axis
            if (initialxAxis.equals(keyframe.value.getLocalX()) == false)
                indicationBits.clear(OptimizedChannel.CONSTANT_X_AXIS);
            // y axis
            if (initialyAxis.equals(keyframe.value.getLocalY()) == false)
                indicationBits.clear(OptimizedChannel.CONSTANT_Y_AXIS);
            // z axis
            if (initialzAxis.equals(keyframe.value.getLocalZ()) == false)
                indicationBits.clear(OptimizedChannel.CONSTANT_Z_AXIS);

        }

        // categorize
        if (indicationBits.isEmpty() == false)
        {
            result = new OptimizedChannel(channel.getTargetJointName(), indicationBits, firstTransform, channel.m_KeyFrames.size());
            fillChannelWithKeyframes(channel,(OptimizedChannel) result);
        }
        else // No compression techniques to use
            result = channel;
        return result;
    }
}
