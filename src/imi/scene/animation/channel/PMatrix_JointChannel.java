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
import imi.scene.animation.channel.PJointChannel;
import imi.scene.animation.keyframe.PMatrixKeyframe;
import imi.scene.animation.keyframe.KeyframeInterface;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.utils.Interpolator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javolution.util.FastList;
import javolution.util.FastTable;


/**
 * Concrete channel implementation.
 * 
 * This implementation uses matrices as frames for transform animation
 * 
 * @author Ronald E Dahlgren
 */
public class PMatrix_JointChannel implements PJointChannel, Serializable
{
    /** name of the joint this channel affects**/
    private String  m_TargetJointName = null;
    /** Keyframes for this channel's animation **/
    private final   FastTable<PMatrixKeyframe>    m_KeyFrames = new FastTable<PMatrixKeyframe>();
    
    // Assorted data that is explicitely calculated
    private float   m_fDuration = 0.0f;
    private float   m_fAverageFrameStep = 0.0f;

    /** Buffer variables to cut down on object creation **/
    private transient PMatrix m_blendedFrameLeft = new PMatrix();
    private transient PMatrix m_blendedFrameRight = new PMatrix();
    private transient PMatrix m_blendBuffer = new PMatrix();
    private transient PMatrix leftSideBuffer = new PMatrix();
    private transient PMatrix rightSideBuffer = new PMatrix();
    private transient Interpolator m_interpolator = new Interpolator();


    
    /**
     * Construct a new instance. Interpolation strategy defaults to ElementInterpolation
     */
    public PMatrix_JointChannel()
    {
        // initialization needed?
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
    }
    
    /**
     * Construct a new instance with the provided name.
     * Interpolation strategy defaults to ElementInterpolation.
     * @param name
     */
    public PMatrix_JointChannel(String name)
    {
        m_TargetJointName = name;
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
    }

    /**
     * Set the joint's local transform to the appropriate value for the state.
     * @param jointToAffect
     * @param state
     */
    public void calculateFrame(PJoint jointToAffect, AnimationState state)
    {
        if (calculateBlendedMatrix(state.getCurrentCycleTime(), m_blendBuffer, state, false) == true)
            jointToAffect.getTransform().setLocalMatrix(m_blendBuffer);
        return;
    }

    @Override
    public float calculateDuration()
    {
        float fEndTime = m_KeyFrames.get(m_KeyFrames.size()-1).getFrameTime();
        float fStartTime = m_KeyFrames.get(1).getFrameTime();
        // Calculate
        m_fDuration = fEndTime - fStartTime;

        if (fStartTime > 0.0f)
        {
            m_fDuration += fStartTime;
        }
        else
        {
            float fSecondKeyframeTime = m_KeyFrames.get(1).getFrameTime();
            m_fDuration += fSecondKeyframeTime;
        }
        
        // Return
        return m_fDuration;
    }
    
    /**
     * Calculate the result of blending the transform matrices found between the
     * specified boundary times at the provided time.
     * @param fTime
     * @param output This matrix is used to receive the calculation.
     * @return False if there was no relevant calculation.
     */
    private boolean calculateBlendedMatrix(float fTime, PMatrix output, AnimationState state,
            boolean bTransitionCycle)
    {
        boolean result = true;
        boolean overTheEdgeInReverse = false;
        float interpolationCoefficient = 0.0f;

        // determine what two keyframes to interpolate between
        KeyframeInterface leftFrame = null;
        KeyframeInterface rightFrame = null;
        KeyframeInterface currentFrame = null;
        // Get cursor
        int currentIndex = -1;
        if (bTransitionCycle)
            currentIndex = state.getCursor().getCurrentTransitionJointPosition();
        else
            currentIndex = state.getCursor().getCurrentJointPosition();

        int numKeyframes = m_KeyFrames.size();
        if (currentIndex < 0 || currentIndex >= numKeyframes) // Current index valid?
        {
            if ((state.isReverseAnimation() && !bTransitionCycle) ||
                (state.isTransitionReverseAnimation() && bTransitionCycle))
                currentIndex = numKeyframes - 1; // start at the end
            else
                currentIndex = 0;
        }

        // GC optimization, removed Iterator usage
        if ((state.isReverseAnimation() && !bTransitionCycle) ||
            (state.isTransitionReverseAnimation() && bTransitionCycle))
        { // In reverse, iterate backwards
            while (currentIndex > 0)
            {
                currentFrame = m_KeyFrames.get(currentIndex);
                if (currentFrame.getFrameTime() > fTime)
                    rightFrame = currentFrame;
                else // passed the mark
                {
                    leftFrame = currentFrame;
                    if (bTransitionCycle)
                        state.getCursor().setCurrentTransitionJointIndex(currentIndex + 1);
                    else
                        state.getCursor().setCurrentJointPosition(currentIndex + 1);
                    break; // finished checking
                }
                currentIndex--;
            }
            if (rightFrame == null)
            {
                overTheEdgeInReverse = true;
                rightFrame = m_KeyFrames.getLast();
            }
        }
        else // playing forward
        {
            while (currentIndex < numKeyframes-1)
            {
                currentFrame = m_KeyFrames.get(currentIndex);
                if (currentFrame.getFrameTime() <= fTime)
                    leftFrame = currentFrame;
                else // passed the mark
                {
                    rightFrame = currentFrame;
                    if (bTransitionCycle)
                        state.getCursor().setCurrentTransitionJointIndex(currentIndex - 1);
                    else
                        state.getCursor().setCurrentJointPosition(currentIndex - 1);
                    break; // finished checking
                }
                currentIndex++;
            }
        }

        if (leftFrame != null && rightFrame != null) // Need to blend between two poses
        {
            if (!overTheEdgeInReverse)
                interpolationCoefficient = (fTime - leftFrame.getFrameTime()) / (rightFrame.getFrameTime() - leftFrame.getFrameTime());
            else
                interpolationCoefficient = (fTime - leftFrame.getFrameTime()) / m_fAverageFrameStep;
            leftFrame.valueAsPMatrix(leftSideBuffer);
            rightFrame.valueAsPMatrix(rightSideBuffer);
            m_interpolator.interpolate(interpolationCoefficient, leftSideBuffer, rightSideBuffer, output);
        }
        else
            result = false;
        return result;
    }

    /**
     * This method calculates and returns the average timestep,
     * it also calls the calculateDuration method. So if you need both,
     * just call this one =)
     * @return m_fAverageFrameStep (float)
     */
    @Override
    public float calculateAverageStepTime()
    {
        // in case it wasn't done yet
        calculateDuration();
        m_fAverageFrameStep = m_fDuration / (float)m_KeyFrames.size();
        return m_fAverageFrameStep;
    }

    /**
     * Gets and returns the average step time.
     * @return float
     */
    @Override
    public float getAverageStepTime()
    {
        return m_fAverageFrameStep;
    }

    /**
     * Add a keyframe to the internal collection
     * @param keyframe
     */
    private void addKeyframe(PMatrixKeyframe keyframe)
    {
        // maintain chronological ordering
        int index = 0;
        for (; index < m_KeyFrames.size(); ++index)
            if (m_KeyFrames.get(index).getFrameTime() > keyframe.getFrameTime())
                break;
        // now index points to the first frame after this time
        m_KeyFrames.add(index, keyframe);
    }

    //  Adds a Keyframe.
    public void addKeyframe(float fTime, PMatrix Value)
    {
        addKeyframe(new PMatrixKeyframe(fTime, Value));
    }

    @Override
    public String getTargetJointName()
    {
        return m_TargetJointName;
    }

    public void setTargetJointName(String name)
    {
        m_TargetJointName = name;
    }


    public float getDuration()
    {
        return(m_fDuration);
    }
    
    public float getAverageFrameStep()
    {
        return(m_fAverageFrameStep);
    }


    /**
     * Dumps the JointChannel.
     */
    public void dump(String spacing)
    {
        System.out.println("   JointChannel=" + m_TargetJointName + ", Duration=" + m_fDuration);
    }

    /**
     * Clears the JointChannel.
     */
    public void clear()
    {
        m_TargetJointName = null;
    
        m_KeyFrames.clear();

        m_fDuration = 0.0f;
        m_fAverageFrameStep = 0.0f;
    }

    /**
     * Returns the endtime of the JointChannel.
     * @return float
     */
    public float getEndTime()
    {
        float fEndTime = 0.0f;
        if (m_KeyFrames.size() > 0)
            fEndTime = m_KeyFrames.get(m_KeyFrames.size()-1).getFrameTime();
        return fEndTime;
    }

    /**
     * Adjusts all the keyframe times.
     * @param fAmount The amount to adjust each keyframe time by.
     */
    public void adjustKeyframeTimes(float fAmount)
    {
        for (KeyframeInterface keyFrame : m_KeyFrames)
            keyFrame.adjustTime(fAmount);
    }

    @Override
    public String toString() {
        return new String("Target: " + m_TargetJointName + ", Duration: " + m_fDuration);
    }


    @Override
    public void fractionalReduction(int ratio) {
        FastList<KeyframeInterface> removals = new FastList();
        if (m_KeyFrames.size() < ratio * 3)
            return; // Too small to bother
        for (int i = 0; i < m_KeyFrames.size(); ++i)
        {
            if (i == 0 || i == m_KeyFrames.size() - 1 || i % ratio == 0)
                continue;
            else
                removals.add(m_KeyFrames.get(i));
        }
        // Now remove all of those
        for (KeyframeInterface frame : removals)
            m_KeyFrames.remove(frame);
    }

    @Override
    public void timeBasedReduction(int newSampleFPS) {
        float spacing = 1.0f / (float)newSampleFPS;

        FastList<KeyframeInterface> removals = new FastList();

        float lastTime = 0.0f;
        KeyframeInterface firstFrame = m_KeyFrames.getFirst();
        KeyframeInterface lastFrame = m_KeyFrames.getLast();
        for (KeyframeInterface frame : m_KeyFrames)
        {
            if (    frame == firstFrame ||
                    frame == lastFrame  ||
                    frame.getFrameTime() - lastTime >= spacing)
            {
                lastTime = frame.getFrameTime();
                continue;
            }
            else
                removals.add(frame);
        }
        // Now remove all of those
        for (KeyframeInterface frame : removals)
            m_KeyFrames.remove(frame);
    }

    /**
     * Close the channel!
     */
    public void closeChannel()
    {
        calculateAverageStepTime();
        m_KeyFrames.getLast().valueAsPMatrix(m_blendBuffer);
        PMatrixKeyframe newFrame = new PMatrixKeyframe(m_fDuration + m_fAverageFrameStep, m_blendBuffer);
        m_KeyFrames.add(newFrame);
        calculateAverageStepTime();
    }

    @Override
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient)
    {
        if (calculateBlendedMatrix(state.getTransitionCycleTime(), rightSideBuffer, state, true))
        {
            leftSideBuffer.set(joint.getTransform().getLocalMatrix(false));
            m_interpolator.interpolate(lerpCoefficient,
                    leftSideBuffer, rightSideBuffer,
                    joint.getTransform().getLocalMatrix(true));
        }
    }

    /****************************
     * SERIALIZATION ASSISTANCE *
     ****************************/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all transient objects
        m_interpolator = new Interpolator();
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
        m_blendedFrameLeft = new PMatrix();
        m_blendedFrameRight = new PMatrix();
        leftSideBuffer = new PMatrix();
        rightSideBuffer = new PMatrix();
        m_blendBuffer = new PMatrix();
    }
}




