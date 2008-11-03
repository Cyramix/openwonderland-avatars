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
        // extract relevant data from the animation state provided
        float fCurrentTime = state.getCurrentCycleTime();
        
        // do we even have animation data?
        if (m_KeyFrames.size() == 0)
            return; // Do nothing

        float []rotationAngles = new float[3];

        // determine what two keyframes to interpolate between
        PMatrixKeyframe currentFrame = m_KeyFrames.getFirst();
        PMatrixKeyframe nextFrame = null;//m_KeyFrames.getFirst();
        float s = 0.0f; // this determines how far in we should interpolate

        PMatrixKeyframe firstFrame = m_KeyFrames.getFirst();
        PMatrixKeyframe lastFrame = m_KeyFrames.getLast();
        boolean bLooping = false;

        PMatrix delta = new PMatrix();

        // Single direction looping check
        if (fCurrentTime <= firstFrame.getFrameTime()) // Before beginning
            currentFrame = firstFrame;
        else if (fCurrentTime >= lastFrame.getFrameTime()) // After ending
        {
            currentFrame = lastFrame;
            nextFrame = firstFrame;
            bLooping = true;
        }
        else // Determine left and right keyframes for blending
        {
            for (PMatrixKeyframe frame : m_KeyFrames)
            {
                if (frame.getFrameTime() < fCurrentTime)
                    currentFrame = frame;
                else
                {
                    nextFrame = frame;
                    break;
                }
            }
        }

        // At this point we now have the current and next frames, if the current 
        // or the next frame is not within the cycle limits, then this channel has
        // no animaton data for that cycle and no transformation should be done.
        if (currentFrame != null)
            if (currentFrame.getFrameTime() < state.getCurrentCycleStartTime() || currentFrame.getFrameTime() > state.getCurrentCycleEndTime())
                return; // No data, do not manipulate anything
        if (nextFrame != null)
            if (nextFrame.getFrameTime() < state.getCurrentCycleStartTime() || currentFrame.getFrameTime() > state.getCurrentCycleEndTime())
                return; // No data, do nothing
                
        delta.set(currentFrame.getValue());
                    
        //  Are we right at a keyframe.
        if (currentFrame != null && nextFrame == null)
        {
            delta.set(currentFrame.getValue());
        }
        else if (currentFrame == nextFrame)
        {
            delta.set(currentFrame.getValue());
        }
        else if (currentFrame != null && nextFrame != null)
        {
            if (bLooping)
            {
                s = (fCurrentTime - currentFrame.getFrameTime()) / (this.m_fDuration - lastFrame.getFrameTime());
            }
            else
            {
                // determine s
                s = (fCurrentTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
            }


            delta.setIdentity();
            
//            if (s < 0.0f || s > 1.0f)
//            {
//                int debuggingBreakPoint = 0;
//            }

            if (state.isReverseAnimation())
                s *= -1.0f; // Reverese interpolation weights
            
            Quaternion rotationComponent = currentFrame.getValue().getRotationJME();
            rotationComponent.slerp(rotationComponent, nextFrame.getValue().getRotationJME(), s);

            rotationComponent.toAngles(rotationAngles);

            // grab the translation and lerp it
            Vector3f translationComponent = new Vector3f(currentFrame.getValue().getTranslation());
            translationComponent.interpolate(nextFrame.getValue().getTranslation(), s);

            delta.set2(rotationComponent, translationComponent, 1.0f);
        }

        jointToAffect.getTransform().getLocalMatrix(true).set(delta);

        jointToAffect.setDirty(true, true);
    }
    
    public void Matrix4fToPMatrix(Matrix4f matrix4f, PMatrix pMatrix)
    {
        float []matrixFloats = pMatrix.getData();
        
        matrixFloats[0] = matrix4f.m00;
        matrixFloats[1] = matrix4f.m01;
        matrixFloats[2] = matrix4f.m02;
        matrixFloats[3] = matrix4f.m03;

        matrixFloats[4] = matrix4f.m10;
        matrixFloats[5] = matrix4f.m11;
        matrixFloats[6] = matrix4f.m12;
        matrixFloats[7] = matrix4f.m13;

        matrixFloats[8] = matrix4f.m20;
        matrixFloats[9] = matrix4f.m21;
        matrixFloats[10] = matrix4f.m22;
        matrixFloats[11] = matrix4f.m23;

        matrixFloats[12] = matrix4f.m30;
        matrixFloats[13] = matrix4f.m31;
        matrixFloats[14] = matrix4f.m32;
        matrixFloats[15] = matrix4f.m33;
    }

    
    
    public void calculateBlendedFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_KeyFrames.size() == 0)
            return; // Do nothing
        
        // extract relevant data
        float fCurrentCycleTime = state.getCurrentCycleTime();
        float fTransitionCycleTime = state.getTransitionCycleTime();
        
        float interpolationCoefficient = state.getTimeInTransition() / state.getTransitionDuration();
        
        // determine what two keyframes to interpolate between for the first pose
        PMatrixKeyframe currentFrame = m_KeyFrames.getFirst();
        PMatrixKeyframe nextFrame = m_KeyFrames.getFirst();
        
        for (PMatrixKeyframe frame : m_KeyFrames)
        {
            if (frame.getFrameTime() < fCurrentCycleTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        
        // Bounds checking within the specified cycle
        boolean noDataForCurrentCycle = false;
        if (currentFrame != null)
            if (currentFrame.getFrameTime() < state.getCurrentCycleStartTime() || currentFrame.getFrameTime() > state.getCurrentCycleEndTime())
                noDataForCurrentCycle = true; // No data, do not manipulate anything
        if (nextFrame != null)
            if (nextFrame.getFrameTime() < state.getCurrentCycleStartTime() || currentFrame.getFrameTime() > state.getCurrentCycleEndTime())
                noDataForCurrentCycle = true; // No data, do nothing
        
        // grab out the rotation and slerp it
        Quaternion rotationComponent1 = currentFrame.getValue().getRotation();
        rotationComponent1.slerp(nextFrame.getValue().getRotation(), interpolationCoefficient);
        
        // grab the translation and lerp it
        Vector3f translationComponent1 = currentFrame.getValue().getTranslation();
        translationComponent1.interpolate(nextFrame.getValue().getTranslation(), interpolationCoefficient);
        
        //////////////////////////////////////////////////////
        // determine the information for the second pose    //
        //////////////////////////////////////////////////////
        currentFrame = m_KeyFrames.getFirst();
        nextFrame = m_KeyFrames.getFirst();
        
        
        for (PMatrixKeyframe frame : m_KeyFrames)
        {
            if (frame.getFrameTime() < fTransitionCycleTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        
        // bounds checking
        if (currentFrame != null)
        {
            if (currentFrame.getFrameTime() < state.getCurrentCycleStartTime() || currentFrame.getFrameTime() > state.getCurrentCycleEndTime())
            {
                if (noDataForCurrentCycle) // double failure, do nothing
                    return;
                else
                {
                    PMatrix delta = new PMatrix();
                    delta.set(rotationComponent1, translationComponent1, 1.0f);
                    // apply to the joint
                    jointToAffect.getTransform().getLocalMatrix(true).set(delta);
                    return;
                }
            }
        }
        if (nextFrame != null)
        {
            if (nextFrame.getFrameTime() < state.getCurrentCycleStartTime() || currentFrame.getFrameTime() > state.getCurrentCycleEndTime())
            {
                if (noDataForCurrentCycle) // double failure, do nothing
                    return;
                else
                {
                    PMatrix delta = new PMatrix();
                    delta.set(rotationComponent1, translationComponent1, 1.0f);
                    // apply to the joint
                    jointToAffect.getTransform().getLocalMatrix(true).set(delta);
                    return;
                }
            }
        }
        // grab out the rotation and slerp it
        Quaternion rotationComponent2 = currentFrame.getValue().getRotation();
        rotationComponent2.slerp(nextFrame.getValue().getRotation(), interpolationCoefficient);
        
        // grab the translation and lerp it
        Vector3f translationComponent2 = currentFrame.getValue().getTranslation();
        translationComponent2.interpolate(nextFrame.getValue().getTranslation(), interpolationCoefficient);
        
        // Interpolate the two poses
        rotationComponent1.slerp(rotationComponent2, interpolationCoefficient);
        translationComponent1.interpolate(translationComponent2, interpolationCoefficient);
        
        PMatrix delta = new PMatrix();
        delta.set(rotationComponent1, translationComponent1, 1.0f);
        
        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(delta);
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
            if (keyframe.getFrameTime() <= fCycleEndTime)
                endFrame = keyframe;
        }
        
        assert((startFrame != null && endFrame != null)) : "Someone set up us the bomb";
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




