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
    final   FastTable<PMatrixKeyframe>    m_KeyFrames = new FastTable<PMatrixKeyframe>();
    
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
    }

    @Override
    public float calculateDuration()
    {
        float fEndTime = m_KeyFrames.get(m_KeyFrames.size()-1).time;
        float fStartTime = m_KeyFrames.get(1).time;
        // Calculate
        m_fDuration = fEndTime - fStartTime;

        if (fStartTime > 0.0f)
        {
            m_fDuration += fStartTime;
        }
        else
        {
            float fSecondKeyframeTime = m_KeyFrames.get(1).time;
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
        boolean overTheEdge = false;
        float interpolationCoefficient = 0.0f;
        float relativeTime = 0;

        // determine what two keyframes to interpolate between
        PMatrixKeyframe leftFrame = null;
        PMatrixKeyframe rightFrame = null;
        PMatrixKeyframe currentFrame = null;

        // Get cursor
        int currentIndex = -1;
        if (bTransitionCycle)
            currentIndex = state.getCursor().getCurrentTransitionJointPosition();
        else
            currentIndex = state.getCursor().getCurrentJointPosition();

        int numKeyframes = m_KeyFrames.size();
        if (currentIndex < 0 || currentIndex >= numKeyframes - 1) // Current index valid?
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
                if (currentFrame.time > fTime)
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
            if (leftFrame == null)
            {
                overTheEdge = true;
                leftFrame = m_KeyFrames.getLast();
                if (rightFrame != null)
                    interpolationCoefficient = fTime / rightFrame.time;
                else
                    interpolationCoefficient = 0;
            }
            else
                relativeTime = fTime - leftFrame.time;
        }
        else // playing forward
        {
            while (currentIndex < numKeyframes-1)
            {
                currentFrame = m_KeyFrames.get(currentIndex);
                if (currentFrame.time <= fTime)
                    leftFrame = currentFrame;
                else // passed the mark
                {
                    rightFrame = currentFrame;
                    if (bTransitionCycle)
                        state.getCursor().setCurrentTransitionJointIndex(currentIndex - 2);
                    else
                        state.getCursor().setCurrentJointPosition(currentIndex - 2);
                    break; // finished checking
                }
                currentIndex++;
            }
            if (rightFrame == null)
            {
                rightFrame = m_KeyFrames.getFirst();
                overTheEdge = true;
                if (leftFrame != null)
                    interpolationCoefficient = (fTime - leftFrame.time) / (m_fDuration + m_fAverageFrameStep - leftFrame.time);
                else
                    return false;
            }
            else if (leftFrame != null)
                relativeTime = fTime - leftFrame.time;
        }

        if (leftFrame != null && rightFrame != null) // Need to blend between two poses
        {
            if (!overTheEdge)
                interpolationCoefficient = relativeTime / (rightFrame.time - leftFrame.time);
                
            leftSideBuffer.set(leftFrame.value);
            rightSideBuffer.set(rightFrame.value);
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
            if (m_KeyFrames.get(index).time > keyframe.time)
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

    /**
     * Returns the endtime of the JointChannel.
     * @return float
     */
    public float getEndTime()
    {
        float fEndTime = 0.0f;
        if (m_KeyFrames.size() > 0)
            fEndTime = m_KeyFrames.get(m_KeyFrames.size()-1).time;
        return fEndTime;
    }

    @Override
    public String toString() {
        return new String("Target: " + m_TargetJointName + ", Duration: " + m_fDuration);
    }


    /**
     * This method reduces the density of keyframes to one in <code>ratio</code>.
     * @param ratio
     */
    public void fractionalReduction(int ratio) {
        FastList<PMatrixKeyframe> removals = new FastList();
        int numFrames = m_KeyFrames.size();
        if (numFrames < ratio * 3)
            return; // Too small to bother
        for (int i = 0; i < numFrames; ++i)
        {
            if (i == 0 || i == numFrames - 1 || i % ratio == 0)
                continue;
            else
                removals.add(m_KeyFrames.get(i));
        }
        // Now remove all of those
        for (PMatrixKeyframe frame : removals)
            m_KeyFrames.remove(frame);
    }

    /**
     * This method reduces keyframe density to no greater than the specified
     * number of frames per second (FPS)
     * @param newSampleFPS
     */
    public void timeBasedReduction(int newSampleFPS) {
        float spacing = 1.0f / (float)newSampleFPS;

        FastList<PMatrixKeyframe> removals = new FastList();

        float lastTime = 0.0f;
        PMatrixKeyframe firstFrame = m_KeyFrames.getFirst();
        PMatrixKeyframe lastFrame = m_KeyFrames.getLast();
        for (PMatrixKeyframe frame : m_KeyFrames)
        {
            if (    frame == firstFrame ||
                    frame == lastFrame  ||
                    frame.time - lastTime >= spacing)
            {
                lastTime = frame.time;
                continue;
            }
            else
                removals.add(frame);
        }
        // Now remove all of those
        for (PMatrixKeyframe frame : removals)
            m_KeyFrames.remove(frame);
    }

    @Override
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient)
    {
        if (lerpCoefficient > 0.01f)
        {
            if (calculateBlendedMatrix(state.getTransitionCycleTime(), rightSideBuffer, state, true))
            {
                if (lerpCoefficient > 0.99f)
                    joint.getTransform().setLocalMatrix(rightSideBuffer);
                else
                {
                    leftSideBuffer.set(joint.getTransform().getLocalMatrix(false));
                    m_interpolator.interpolate(lerpCoefficient,
                            leftSideBuffer, rightSideBuffer,
                            joint.getTransform().getLocalMatrix(true));
                    joint.getTransform().getLocalMatrix(true).normalizeAxes();
                }
            }
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
        calculateAverageStepTime();
    }

    protected class PMatrixKeyframe implements Serializable
    {
        public float               time = 0.0f;
        public transient PMatrix   value = new PMatrix();

        // Convenience constructor
        public PMatrixKeyframe(float time, PMatrix value)
        {
            this.time = time;
            this.value.set(value);
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            float[] matrix = new float[16];
            value.getFloatArray(matrix);
            for (int i = 0; i < 12; ++i)
                stream.writeFloat(matrix[i]);
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
        {
            stream.defaultReadObject();
            float[] matrix = new float[16];
            for (int i = 0; i < 12; ++i)
                matrix[i] = stream.readFloat();

            matrix[12] = 0;
            matrix[13] = 0;
            matrix[14] = 0;
            matrix[15] = 1;

            value = new PMatrix(matrix);
        }

    }

}




