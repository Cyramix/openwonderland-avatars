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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import javolution.util.FastList;

import com.jme.math.Matrix4f;



/**
 * Concrete channel implementation.
 * 
 * This implementation uses matrices as frames for transform animation
 * 
 * @author Ronald E Dahlgren
 */
public class COLLADA_JointChannel implements PJointChannel
{
    private String                      m_TargetJointName = null;
    private PMatrix                     m_TargetBindMatrix = null;
    
    private FastList<PMatrixKeyframe>   m_KeyFrames = new FastList<PMatrixKeyframe>();
    
    // Assorted data that is explicitely calculated
    private float                       m_fDuration = 0.0f;
    private float                       m_fAverageFrameStep = 0.0f;

    
    
    //  Constructor land!
    public COLLADA_JointChannel()
    {
        // initialization needed?
    }
    
    //  Constructor land!
    public COLLADA_JointChannel(String name)
    {
        m_TargetJointName = name;
    }
    
    // Copy constructor
    public COLLADA_JointChannel(COLLADA_JointChannel jointAnimation)
    {
        setTargetJointName(jointAnimation.getTargetJointName());
        // Copy translation key frames
        for (PMatrixKeyframe frame : jointAnimation.m_KeyFrames)
            m_KeyFrames.add(new PMatrixKeyframe(frame));
    }

    //  Sets the BindMatrix.
    public void setBindMatrix(PMatrix pBindMatrix)
    {
        m_TargetBindMatrix = pBindMatrix;
    }

    public void calculateFrame(PJoint jointToAffect, AnimationState state)
    {
        if (true)
        {
            PMatrix result = calculateBlendedMatrix(state.getCurrentCycleTime(), state.getCurrentCycleStartTime(), state.getCurrentCycleEndTime(), state.isReverseAnimation());
            if (result != null)
                jointToAffect.getTransform().setLocalMatrix(result);
            return;
        }
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
        
        PMatrix firstTransform  = calculateBlendedMatrix(fCurrentCycleTime, state.getCurrentCycleStartTime(), state.getCurrentCycleEndTime(), state.isReverseAnimation());
        PMatrix secondTransform = calculateBlendedMatrix(fTransitionCycleTime, state.getTransitionCycleStartTime(), state.getTransitionCycleEndTime(), state.isTransitionReverseAnimation());
        
        PMatrix result = null;
        
        if (secondTransform == null)
            result = firstTransform;
        else if (firstTransform == null)
            result = secondTransform;
        
        
        if (firstTransform != null && firstTransform.equals(secondTransform)) // no blend
            result = firstTransform;
        else if (firstTransform != null && secondTransform != null) // interpolate!
        {
            // if we got here, then these two transforms need to be blended
            Quaternion rotationComponent1 = firstTransform.getRotation();
            Quaternion rotationComponent2 = secondTransform.getRotation();
            rotationComponent1.slerp(rotationComponent2, interpolationCoefficient);

            // grab the translation and lerp it
            Vector3f translationComponent1 = firstTransform.getTranslation();
            Vector3f translationComponent2 = secondTransform.getTranslation();
            translationComponent1.interpolate(translationComponent2, interpolationCoefficient);
            
            result = new PMatrix();
            result.set2(rotationComponent1, translationComponent1, 1.0f);
        }
        if (result != null)
            jointToAffect.getTransform().setLocalMatrix(result);
    }

    public float calculateDuration()
    {
        float fEndTime = m_KeyFrames.getLast().getFrameTime();
        float fStartTime = m_KeyFrames.getFirst().getFrameTime();
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

    private PMatrix calculateBlendedMatrix(float fTime, float fLeftBoundaryTime, float fRightBoundaryTime, boolean bReverse)
    {
        float interpolationCoefficient = 0.0f;
        // determine what two keyframes to interpolate between for the first pose
        PMatrixKeyframe leftFrame = null;
        PMatrixKeyframe rightFrame = null;
        
        for (PMatrixKeyframe frame : m_KeyFrames)
        {
            if (frame.getFrameTime() <= fTime)
                leftFrame = frame;
            else // passed the mark
            {
                rightFrame = frame;
                break; // finished checking
            }
        }
        
        // Bounds checking within the specified cycle
        if (leftFrame != null)
            if (leftFrame.getFrameTime() < fLeftBoundaryTime || leftFrame.getFrameTime() > fRightBoundaryTime)
                leftFrame = null;
        if (rightFrame != null)
            if (rightFrame.getFrameTime() < fLeftBoundaryTime || rightFrame.getFrameTime() > fRightBoundaryTime)
                rightFrame = null; // No data, do nothing
        
        PMatrix delta = null;
        //  Are we directly aligned with a keyframe?!!?
        if (leftFrame != null && rightFrame == null)
            delta = new PMatrix(leftFrame.getValue());
        else if (leftFrame != null && leftFrame == rightFrame) // Same relationship, different manifestation
            delta = new PMatrix(leftFrame.getValue());
        else if (leftFrame != null && rightFrame != null) // Need to blend between two poses
        {
            interpolationCoefficient = (fTime - leftFrame.getFrameTime()) / (rightFrame.getFrameTime() - leftFrame.getFrameTime());

//            if (bReverse)
//                interpolationCoefficient = 1.0f - interpolationCoefficient; // Reverese interpolation weights
            
            Quaternion rotationComponent = leftFrame.getValue().getRotationJME();
            rotationComponent.slerp(rotationComponent, rightFrame.getValue().getRotationJME(), interpolationCoefficient);

            // grab the translation and lerp it
            Vector3f translationComponent = new Vector3f(leftFrame.getValue().getTranslation());
            translationComponent.interpolate(rightFrame.getValue().getTranslation(), interpolationCoefficient);
            delta = new PMatrix();
            delta.set2(rotationComponent, translationComponent, 1.0f);
        }
        
        return delta;
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
        m_TargetBindMatrix = null;
    
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
            fStartTime = m_KeyFrames.getFirst().getFrameTime();

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
            fEndTime = m_KeyFrames.getLast().getFrameTime();

        return fEndTime;
    }

    /**
     * Adjusts all the keyframe times.
     * @param fAmount The amount to adjust each keyframe time by.
     */
    public void adjustKeyframeTimes(float fAmount)
    {
        int a;
        PMatrixKeyframe pKeyframe;
            
        for (a=0; a<getKeyframeCount(); a++)
        {
            pKeyframe = getKeyframe(a);
                
            pKeyframe.setFrameTime(pKeyframe.getFrameTime() + fAmount);
        }
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
}




