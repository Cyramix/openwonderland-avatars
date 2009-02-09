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
package imi.scene.animation;

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
public class COLLADA_JointChannel implements PJointChannel, Serializable
{
    private String                      m_TargetJointName = null;
    
    private final FastTable<PMatrixKeyframe>   m_KeyFrames = new FastTable<PMatrixKeyframe>();
    
    // Assorted data that is explicitely calculated
    private float                       m_fDuration = 0.0f;
    private float                       m_fAverageFrameStep = 0.0f;

    /** Buffer variables to cut down on object creation **/
    private final PMatrix m_blendedFrameLeft = new PMatrix();
    private final PMatrix m_blendedFrameRight = new PMatrix();
    private final PMatrix m_blendBuffer = new PMatrix();

    private final Interpolator m_interpolator = new Interpolator();


    
    //  Constructor land!
    public COLLADA_JointChannel()
    {
        // initialization needed?
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
    }
    
    //  Constructor land!
    public COLLADA_JointChannel(String name)
    {
        m_TargetJointName = name;
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
    }
    
    // Copy constructor
    public COLLADA_JointChannel(COLLADA_JointChannel jointAnimation)
    {
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
        setTargetJointName(jointAnimation.getTargetJointName());
        // Copy translation key frames
        for (PMatrixKeyframe frame : jointAnimation.m_KeyFrames)
            m_KeyFrames.add(new PMatrixKeyframe(frame));
    }

    public void calculateFrame(PJoint jointToAffect, AnimationState state)
    {
        if (calculateBlendedMatrix(state.getCurrentCycleTime(), state.getCurrentCycleStartTime(),
                state.getCurrentCycleEndTime(), m_blendBuffer, state, false) == true)
            jointToAffect.getTransform().setLocalMatrix(m_blendBuffer);
        return;
    }

    /**
     * Calculate the pose given the specified transitioning animation state.
     * The current cycle pose is determined and then blended via a weighting
     * derived from the transition time / transition duration
     * @param jointToAffect 
     * @param state
     */
    public void calculateBlendedFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_KeyFrames.size() == 0)
            return; // Do nothing
        // extract relevant data
        float fCurrentCycleTime = state.getCurrentCycleTime();
        float fTransitionCycleTime = state.getTransitionCycleTime();
        
        float interpolationCoefficient = state.getTimeInTransition() / state.getTransitionDuration();
        
        boolean resultOne = calculateBlendedMatrix(fCurrentCycleTime, state.getCurrentCycleStartTime(),
                                state.getCurrentCycleEndTime(), m_blendedFrameLeft, state, false);
        boolean resultTwo = calculateBlendedMatrix(fTransitionCycleTime, state.getTransitionCycleStartTime(),
                state.getTransitionCycleEndTime(), m_blendedFrameRight, state, true);
        
        PMatrix result = null;
        if (!resultOne && !resultTwo)
            return; // No frame data
        else if (resultTwo == false)
            result = m_blendedFrameLeft;
        else if (resultOne == false)
            result = m_blendedFrameRight;
        else if (m_blendedFrameLeft.equals(m_blendedFrameRight)) // no blend
            result = m_blendedFrameLeft;
        else // Interpolate
        {
            m_interpolator.interpolate(interpolationCoefficient, m_blendedFrameLeft, m_blendedFrameRight, m_blendBuffer);
            result = m_blendBuffer;
        }

        jointToAffect.getTransform().setLocalMatrix(result);
    }

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
     * @param fLeftBoundaryTime
     * @param fRightBoundaryTime
     * @param output This matrix is used to receive the calculation.
     * @return False if there was no relevant calculation.
     */
    private boolean calculateBlendedMatrix(float fTime, float fLeftBoundaryTime,
            float fRightBoundaryTime, PMatrix output, AnimationState state,
            boolean bTransitionCycle)
    {
        boolean result = true;
        float interpolationCoefficient = 0.0f;
        // determine what two keyframes to interpolate between for the first pose
        PMatrixKeyframe leftFrame = null;
        PMatrixKeyframe rightFrame = null;
        PMatrixKeyframe currentFrame = null;

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
                currentIndex = m_KeyFrames.size() - 1; // start at the end
            else
                currentIndex = 0;
        }

        // GC optimization, removed Iterator usage
        //ListIterator<PMatrixKeyframe> i = m_KeyFrames.listIterator(currentIndex);
        if ((state.isReverseAnimation() && !bTransitionCycle) ||
            (state.isTransitionReverseAnimation() && bTransitionCycle))
        {
            while (currentIndex > 0)
            {
                currentFrame = m_KeyFrames.get(currentIndex-1);
                if (currentFrame.getFrameTime() > fTime)
                    rightFrame = currentFrame;
                else // passed the mark
                {
                    leftFrame = currentFrame;
                    if (bTransitionCycle)
                        state.getCursor().setCurrentTransitionJointIndex(currentIndex);
                    else
                        state.getCursor().setCurrentJointPosition(currentIndex);
                    break; // finished checking
                }
                currentIndex--;
            }
        }
        else // playing forward
        {
            while (currentIndex < m_KeyFrames.size()-1)
            {
                currentFrame = m_KeyFrames.get(currentIndex+1);
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

//        for (PMatrixKeyframe frame : m_KeyFrames)
//        {
//
//            if (frame.getFrameTime() <= fTime)
//                leftFrame = frame;
//            else // passed the mark
//            {
//                rightFrame = frame;
//                break; // finished checking
//            }
//        }
        
        // Bounds checking within the specified cycle
        if (leftFrame != null)
            if (leftFrame.getFrameTime() < fLeftBoundaryTime || leftFrame.getFrameTime() > fRightBoundaryTime)
                leftFrame = null;
        if (rightFrame != null)
            if (rightFrame.getFrameTime() < fLeftBoundaryTime || rightFrame.getFrameTime() > fRightBoundaryTime)
                rightFrame = null; // No data, do nothing

        //  Are we directly aligned with a keyframe?!!?
        if (leftFrame != null && rightFrame == null)
            output.set(leftFrame.getValue());
        else if (leftFrame != null && leftFrame == rightFrame) // Same relationship, different manifestation
            output.set(leftFrame.getValue());
        else if (leftFrame != null && rightFrame != null) // Need to blend between two poses
        {
            interpolationCoefficient = (fTime - leftFrame.getFrameTime()) / (rightFrame.getFrameTime() - leftFrame.getFrameTime());
            m_interpolator.interpolate(interpolationCoefficient, leftFrame.getValue(), rightFrame.getValue(), output);
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
    public float getAverageStepTime()
    {
        return m_fAverageFrameStep;
    }

    public void addKeyframe(PMatrixKeyframe keyframe)
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

    //  Gets the number of Keyframes.
    public int getKeyframeCount()
    {
        return(m_KeyFrames.size());
    }

    //  Gets the Keyframe at the specified index.
    public PMatrixKeyframe getKeyframe(int index)
    {
        return(m_KeyFrames.get(index));
    }


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
     * Trims the JointChannel of Keyframes that are after the specified time.
     * @param fMaxTime The max keyframe time that should remain in the JointChannel.
     */
    public void trim(float fMaxTime)
    {
        PMatrixKeyframe pKeyframe;
        
        while (m_KeyFrames.size() > 0)
        {
            pKeyframe = m_KeyFrames.get(m_KeyFrames.size()-1);
            if (pKeyframe.getFrameTime() > fMaxTime)
                m_KeyFrames.remove(m_KeyFrames.size()-1);
            else
                break;
        }
    
        calculateDuration();
        calculateAverageStepTime();
    }

    public PJointChannel copy()
    {
        COLLADA_JointChannel result = new COLLADA_JointChannel(this);
        return result;
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
     * Returns the starttime of the JointChannel.
     * @return float
     */
    public float getStartTime()
    {
        float fStartTime = 0.0f;

        if (m_KeyFrames.size() > 0)
            fStartTime = m_KeyFrames.get(1).getFrameTime();

        return fStartTime;
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
        for (PMatrixKeyframe keyFrame : m_KeyFrames)
            keyFrame.m_fTime += fAmount;
    }

    /**
     * Appends a JointChannel onto the end of this JointChannel.
     * @param pJointChannel The JointChannel to append onto this one.
     */
    public void append(PJointChannel pJointChannel, float fOffset)
    {
        COLLADA_JointChannel pColladaJointChannel = (COLLADA_JointChannel)pJointChannel;
        
        PMatrixKeyframe pKeyframe;
        int KeyframeCount = pColladaJointChannel.getKeyframeCount();

        
        //  Adjust all the KeyframeTimes.
        pJointChannel.adjustKeyframeTimes(fOffset);

        for (int i = 0; i < KeyframeCount; i++)
        {
            pKeyframe = pColladaJointChannel.getKeyframe(i);

            m_KeyFrames.add(pKeyframe);
        }

        pJointChannel.clear();

        calculateDuration();
        calculateAverageStepTime();
    }
    
    public void closeCycle(AnimationCycle cycle)
    {
        // 1 - Determine relevancy
        if (getStartTime() > cycle.getEndTime() || getEndTime() < cycle.getStartTime())
                return; // No relevant key frames
        
        float fCycleStartTime = cycle.getStartTime();
        float fCycleEndTime = cycle.getEndTime();
        // 2 - Check to see if the first and last frame of the cycle is the same
        PMatrixKeyframe startFrame = null;
        PMatrixKeyframe endFrame = null;
        
        for (PMatrixKeyframe keyframe : m_KeyFrames)
        {
            if (keyframe.getFrameTime() > fCycleEndTime)
                break;
            else if (keyframe.getFrameTime() < fCycleStartTime)
                continue;
            
            if (startFrame == null && keyframe.getFrameTime() >= fCycleStartTime)
                startFrame = keyframe;
            if (keyframe.getFrameTime() < fCycleStartTime && keyframe.getFrameTime() <= fCycleEndTime)
                endFrame = keyframe;
        }
        
        if (endFrame == null) // Weirdness
            endFrame = startFrame;
        if (endFrame == null) // both are null, not relevant for this channel
            return;
        // Are the transforms the same?
        if (startFrame.getValue().equals(endFrame.getValue()) == false)
        {
            float fTimeStep = m_KeyFrames.get(m_KeyFrames.indexOf(startFrame) + 1).getFrameTime();
            fTimeStep -= startFrame.getFrameTime();
            
            //      - if not, duplicate the first frame (time = endOfCycle + paddingAmount [less than using during group append])
            //          and add it to the channel (it will sort)
            PMatrixKeyframe newFrame = new PMatrixKeyframe(endFrame.getFrameTime() + fTimeStep, startFrame.getValue());
            addKeyframe(newFrame);
            // 3 - Adjust this cycle's end time to meet the new frame if it was created
            cycle.setEndTime(newFrame.getFrameTime());
        }
    }

    @Override
    public String toString() {
        return new String("Target: " + m_TargetJointName + ", Duration: " + m_fDuration);
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
        m_interpolator.setStrategy(Interpolator.InterpolationStrategy.ElementInterpolation);
    }

    @Override
    public void fractionalReduction(int ratio) {
        FastList<PMatrixKeyframe> removals = new FastList();
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
        for (PMatrixKeyframe frame : removals)
            m_KeyFrames.remove(frame);
    }

    @Override
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
                    frame.getFrameTime() - lastTime >= spacing)
            {
                lastTime = frame.getFrameTime();
                continue;
            }
            else
                removals.add(frame);
        }
        // Now remove all of those
        for (PMatrixKeyframe frame : removals)
            m_KeyFrames.remove(frame);
    }
}




