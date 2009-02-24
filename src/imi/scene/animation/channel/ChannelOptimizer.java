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
import javolution.util.FastList;


/**
 * This class is responsible for optimizing joint channels. It is provided with
 * a PMatrix joint channel and returns an optimized form.
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

    /**
     * Construct a new instance
     */
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
        {
            logger.warning("There is currently no support for optimizing " +
                    "channels of type \"" + channel.getClass().getName() + "\"");
            return channel;
        }
    }

    private PJointChannel optimizeMatrixChannel(PMatrix_JointChannel channel)
    {
        PJointChannel result = null;
        // first, do some frame reduction
        int initialFrameCount = channel.m_KeyFrames.size();
        smartKeyframeReduction(channel);
        if ((initialFrameCount - channel.m_KeyFrames.size()) > 20)
            logger.info("Keyframes reduced: " + (initialFrameCount - channel.m_KeyFrames.size()));
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

    /**
     * This method uses the "quality" data member as an error threshold when
     * dropping keyframes.
     * @param channel The channel to be slimmed down.
     */
    private void smartKeyframeReduction(PMatrix_JointChannel channel)
    {
        float maxFrameError = 0.00004f;
        // Collection for assembling removals
        FastList<PMatrix_JointChannel.PMatrixKeyframe> removals = new FastList();
        int frameCount = channel.m_KeyFrames.size();
        if (frameCount < 3) // Give me something to work with!
            return;

        int leftIndex = 0;
        int rightIndex = 2;
        int currentIndex = 1;

        PMatrix_JointChannel.PMatrixKeyframe left = null;
        PMatrix_JointChannel.PMatrixKeyframe right = null;
        PMatrix_JointChannel.PMatrixKeyframe current = null;
        PMatrix lerpBuffer = new PMatrix();

        while(rightIndex < frameCount)
        {
            float lerpFactor = 0;
            // grab the left frame
            left = channel.m_KeyFrames.get(leftIndex);
            // grab the right frame
            right = channel.m_KeyFrames.get(rightIndex++);
            // Grab the 'current' frame
            current = channel.m_KeyFrames.get(currentIndex);
            // determine interpolation coefficient
            lerpFactor = (current.time - left.time) / (right.time - left.time);
            lerpBuffer.lerp(left.value, right.value, lerpFactor);
            float variance = computePMatrixVariance(lerpBuffer, current.value);
            if (variance <= maxFrameError)
            {
                // Drop the frame
                removals.add(channel.m_KeyFrames.get(currentIndex));
                // look at simulating the next keyframe
                currentIndex++;
            }
            else
            {
                // Next
                leftIndex++;
                currentIndex++;
            }
        }
        // Now remove all of the marked frames
        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : removals)
            channel.m_KeyFrames.remove(keyframe);
        // let the channel know some things have changed
        channel.calculateAverageStepTime(); // Also calculates the duration
    }

    private void fillChannelWithKeyframes(PMatrix_JointChannel channel, OptimizedChannel result)
    {
        int index = 0;
        for (PMatrix_JointChannel.PMatrixKeyframe keyframe : channel.m_KeyFrames)
        {
            if (index == 0 && keyframe.time > 0)
                logger.fine("First keyframe was not at time zero, was at " + keyframe.time +", I will fix it.");
            result.addKeyframe(keyframe.time, keyframe.value);
            index++;
        }
    }

    /**
     * Compute and return the variance from the left to the right
     * @param left
     * @param right
     * @return The variance from the left to the right matrix.
     */
    private float computePMatrixVariance(PMatrix left, PMatrix right)
    {
        float[] leftFloats = new float[16];
        left.getFloatArray(leftFloats);
        float[] rightFloats = new float[16];
        right.getFloatArray(rightFloats);

        float variance = 0; // This will be the sum of all member-wise variance
        for (int i = 0; i < 12; ++i)
            variance += Math.abs(rightFloats[i] - leftFloats[i]);
        return variance;
    }
}
