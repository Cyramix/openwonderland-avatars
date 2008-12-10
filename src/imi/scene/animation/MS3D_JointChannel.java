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

import com.jme.math.Vector3f;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import javolution.util.FastList;

/**
 * Concrete channel implementation for milkshape animation data.
 * 
 * This class implements the PJointChannel interface. It is used for processing
 * milkshape animation data (Vector3f channels for rotation and translation)
 * 
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class MS3D_JointChannel implements PJointChannel 
{
    private PMatrix                     m_targetJointBindPose    = null; // The bind pose for this joint
    private String                      m_targetJointName        = null; // The joint that is being manipulated
    
    private FastList<VectorKeyframe>    m_TranslationKeyframes   = new FastList<VectorKeyframe>();
    private FastList<VectorKeyframe>    m_RotationKeyframes      = new FastList<VectorKeyframe>();
    
    // Assorted data that is explicitely calculated
    private float                       m_fDuration              = 0.0f;
    private float                       m_fAverageFrameStep      = 0.0f;


    
    //  Constructor land!
    public MS3D_JointChannel(String targetBone)
    {
        m_targetJointName = new String(targetBone);
    }
    
    // Copy constructor
    public MS3D_JointChannel(MS3D_JointChannel jointAnimation)
    {
        if (jointAnimation.m_targetJointBindPose != null)
            m_targetJointBindPose = new PMatrix(jointAnimation.m_targetJointBindPose);
        
        m_targetJointName = new String(jointAnimation.getTargetJointName());
        
        // Copy translation key frames
        for (VectorKeyframe frame : jointAnimation.m_TranslationKeyframes)
            m_TranslationKeyframes.add(new VectorKeyframe(frame));
        
        // copy rotational key frames
        for (VectorKeyframe frame : jointAnimation.m_RotationKeyframes)
            m_RotationKeyframes.add(new VectorKeyframe(frame));
        // Copy timing info
        m_fDuration = jointAnimation.m_fDuration;
        m_fAverageFrameStep = jointAnimation.m_fAverageFrameStep;
    }

    public void calculateFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_TranslationKeyframes.size() == 0 || m_RotationKeyframes.size() == 0)
            return; // Do nothing
        
        // Extract relevant data
        float fTime = state.getCurrentCycleTime();
        
        // determine what two keyframes to interpolate between for translation
        VectorKeyframe currentFrame = m_TranslationKeyframes.getFirst();
        VectorKeyframe nextFrame    = m_TranslationKeyframes.getFirst();
        float s = 0.0f; // this determines how far in we should interpolate
        
        for (VectorKeyframe frame : m_TranslationKeyframes)
        {
            if (frame.getFrameTime() < fTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        s = (fTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
        // lerp and determine final translation vector
        Vector3f translationVector = new Vector3f();
        
        if (currentFrame != nextFrame)
            translationVector.interpolate(currentFrame.getValue(), nextFrame.getValue(), s);
        else
            translationVector.set(nextFrame.getValue());
        
        // do the same for rotation
        currentFrame = m_RotationKeyframes.getFirst();
        nextFrame    = m_RotationKeyframes.getFirst();
        for (VectorKeyframe frame : m_RotationKeyframes)
        {
            if (frame.getFrameTime() < fTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        
        // determine s
        s = (fTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
        // lerp and determine final translation vector
        Vector3f rotationVector = new Vector3f();
        
        if (currentFrame != nextFrame)
            rotationVector.interpolate(currentFrame.getValue(), nextFrame.getValue(), s);
        else
            rotationVector.set(nextFrame.getValue());
        
        // apply this to the PJoint transform matrix
        PMatrix delta = new PMatrix(rotationVector, Vector3f.UNIT_XYZ, translationVector);
        jointToAffect.getTransform().getLocalMatrix(true).set(m_targetJointBindPose);
        jointToAffect.getTransform().getLocalMatrix(true).mul(delta);
    }

    public void calculateBlendedFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_TranslationKeyframes.size() == 0 || m_RotationKeyframes.size() == 0)
            return; // Do nothing
        
        // Extract relevant data
        float fCurrentCycleTime = state.getCurrentCycleTime();
        float fTransitionCycleTime = state.getTransitionCycleTime();
        float interpolationCoefficient = state.getTimeInTransition() / state.getTransitionDuration();
        // determine the first pose
        // determine what two keyframes to interpolate between for translation
        VectorKeyframe currentFrame = m_TranslationKeyframes.getFirst();
        VectorKeyframe nextFrame = m_TranslationKeyframes.getFirst();
        float fLerpValue = 0.0f; // this determines how far in we should interpolate
        
        for (VectorKeyframe frame : m_TranslationKeyframes)
        {
            if (frame.getFrameTime() < fCurrentCycleTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fCurrentCycleTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
        // lerp and determine final translation vector
        Vector3f translationVector = new Vector3f();
        if (currentFrame != nextFrame)
            translationVector.interpolate(currentFrame.getValue(), nextFrame.getValue(), fLerpValue);
        else
            translationVector.set(nextFrame.getValue());
        
        // do the same for rotation
        currentFrame = m_RotationKeyframes.getFirst();
        nextFrame = m_RotationKeyframes.getFirst();
        for (VectorKeyframe frame : m_RotationKeyframes)
        {
            if (frame.getFrameTime() < fCurrentCycleTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fCurrentCycleTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
        // lerp and determine final translation vector
        Vector3f rotationVector = new Vector3f();
        if (currentFrame != nextFrame)
            rotationVector.interpolate(currentFrame.getValue(), nextFrame.getValue(), fLerpValue);
        else
            rotationVector.set(nextFrame.getValue());
        
        
        
        //////////////////////////////////////////////////////
        // determine the information for the second pose    //
        //////////////////////////////////////////////////////
        // determine what two keyframes to interpolate between for translation
        currentFrame = m_TranslationKeyframes.getFirst();
        nextFrame = m_TranslationKeyframes.getFirst();
        fLerpValue = 0.0f; // this determines how far in we should interpolate
        
        for (VectorKeyframe frame : m_TranslationKeyframes)
        {
            if (frame.getFrameTime() < fTransitionCycleTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fTransitionCycleTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
        // lerp and determine final translation vector
        Vector3f translationVector2 = new Vector3f();
        if (currentFrame != nextFrame)
            translationVector2.interpolate(currentFrame.getValue(), nextFrame.getValue(), fLerpValue);
        else
            translationVector2.set(nextFrame.getValue());
        
        // do the same for rotation
        currentFrame = m_RotationKeyframes.getFirst();
        nextFrame = m_RotationKeyframes.getFirst();
        for (VectorKeyframe frame : m_RotationKeyframes)
        {
            if (frame.getFrameTime() < fTransitionCycleTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fTransitionCycleTime - currentFrame.getFrameTime()) / (nextFrame.getFrameTime() - currentFrame.getFrameTime());
        // lerp and determine final translation vector
        Vector3f rotationVector2 = new Vector3f();
        if (currentFrame != nextFrame)
            rotationVector2.interpolate(currentFrame.getValue(), nextFrame.getValue(), fLerpValue);
        else
            rotationVector2.set(nextFrame.getValue());
        
        //////////////////////////////
        // interpolate the results  //
        //////////////////////////////
        translationVector.interpolate(translationVector2, interpolationCoefficient);
        rotationVector.interpolate(rotationVector2, interpolationCoefficient);
        
        PMatrix delta = new PMatrix(rotationVector, Vector3f.UNIT_XYZ, translationVector);
        
        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(m_targetJointBindPose);
        jointToAffect.getTransform().getLocalMatrix(true).mul(delta);
    }
    
    public float calculateDuration()
    {
        // check out the start times and the end times
        float fStartTime = Math.min(m_TranslationKeyframes.getFirst().getFrameTime(), m_RotationKeyframes.getFirst().getFrameTime());
        float fEndTime = Math.max(m_TranslationKeyframes.getLast().getFrameTime(), m_RotationKeyframes.getLast().getFrameTime());
        // Calculate
        m_fDuration = fEndTime - fStartTime;
        // Return
        return m_fDuration;
        
    }

    public float calculateAverageStepTime()
    {
        // in case it hasn't been done...
        calculateDuration();
        // divide duration by the number of frames for each
        float fAvgTransTime = m_fDuration / ((float)m_TranslationKeyframes.size());
        float fAvgRotTime = m_fDuration / ((float)m_RotationKeyframes.size());
        // Calculate it
        m_fAverageFrameStep = (fAvgRotTime + fAvgTransTime) * 0.5f;
        // Return it!
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

    public String getTargetJointName()
    {
        return m_targetJointName;
    }

    public void setBindPose(PMatrix bindPoseTransform)
    {
        m_targetJointBindPose = bindPoseTransform;
    }

    //  Adds a TranslationKeyframe.
    public void addTranslationKeyframe(float fTime, Vector3f Value)
    {
        // maintain chronological ordering
        int index = 0;
        for (; index < m_TranslationKeyframes.size(); ++index)
            if (m_TranslationKeyframes.get(index).getFrameTime() > fTime)
                break;
        // now index points to the first frame after this time
        m_TranslationKeyframes.add(index, new VectorKeyframe(fTime, Value));
    }

    //  Gets the number of TranslationKeyframes.
    public int getTranslationKeyframeCount()
    {
        return(m_TranslationKeyframes.size());
    }

    //  Gets the TranslationKeyframe at the specified index.
    public VectorKeyframe getTranslationKeyframe(int Index)
    {
        return(m_TranslationKeyframes.get(Index));
    }

    //  Adds a RotationKeyframe.
    public void addRotationKeyframe(float fTime, Vector3f Value)
    {                // maintain chronological ordering
        int index = 0;
        for (; index < m_RotationKeyframes.size(); ++index)
            if (m_RotationKeyframes.get(index).getFrameTime() > fTime)
                break;
        // now index points to the first frame after this time
        m_RotationKeyframes.add(index, new VectorKeyframe(fTime, Value));
    }

    //  Gets the number of RotationKeyframes.
    public int getRotationKeyframeCount()
    {
        return(m_RotationKeyframes.size());
    }

    //  Gets the RotationKeyframe at the specified index.
    public VectorKeyframe getRotationKeyframe(int Index)
    {
        return(m_RotationKeyframes.get(Index));
    }


    /**
     * Dumps the JointChannel.
     */
    public void dump(String spacing)
    {
        int a;
        VectorKeyframe pKeyframe;

        System.out.println(spacing + "PPolygonSkinnedMeshJointAnimation:");

        System.out.println(spacing + "   TranslationKeyframes:  " + getTranslationKeyframeCount());
        for (a=0; a<getTranslationKeyframeCount(); a++)
        {
            pKeyframe = getTranslationKeyframe(a);
            System.out.print(spacing + "      Keyframe Time=" + pKeyframe.getFrameTime());
            System.out.println(", Value=(" + pKeyframe.getValue().x + ", " + pKeyframe.getValue().y + ", " + pKeyframe.getValue().z + ")");
        }

        System.out.println(spacing + "   RotationKeyframes:  " + getRotationKeyframeCount());
        for (a=0; a<getRotationKeyframeCount(); a++)
        {
            pKeyframe = getRotationKeyframe(a);
            System.out.print(spacing + "      Keyframe Time=" + pKeyframe.getFrameTime());
            System.out.println(", Value=(" + pKeyframe.getValue().x + ", " + pKeyframe.getValue().y + ", " + pKeyframe.getValue().z + ")");
        }
    }
    
    /**
     * Trims the JointChannel of Keyframes that are after the specified time.
     * @param fMaxTime The max keyframe time that should remain in the JointChannel.
     */
    public void trim(float fMaxTime)
    {

    }

    public PJointChannel copy()
    {
        MS3D_JointChannel result = new MS3D_JointChannel(this);
        return result;
    }

    /**
     * Clears the JointChannel.
     */
    public void clear()
    {
        m_targetJointBindPose    = null;
        m_targetJointName        = null;
    
        m_TranslationKeyframes.clear();
        m_RotationKeyframes.clear();
    
        m_fDuration              = 0.0f;
        m_fAverageFrameStep      = 0.0f;
    }

    /**
     * Returns the starttime of the JointChannel.
     * @return float
     */
    public float getStartTime()
    {
        float fStartTime = 0.0f;

        if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() == 0)
            fStartTime = 0.0f;
        else if (m_TranslationKeyframes.size() > 0 && m_RotationKeyframes.size() == 0)
            fStartTime = m_TranslationKeyframes.getFirst().getFrameTime();
        else if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() > 0)
            fStartTime = m_RotationKeyframes.getFirst().getFrameTime();
        else
            fStartTime = Math.max(m_TranslationKeyframes.getFirst().getFrameTime(), m_RotationKeyframes.getFirst().getFrameTime());

        return fStartTime;
    }

    /**
     * Returns the endtime of the JointChannel.
     * @return float
     */
    public float getEndTime()
    {
        float fEndTime = 0.0f;

        if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() == 0)
            fEndTime = 0.0f;
        else if (m_TranslationKeyframes.size() > 0 && m_RotationKeyframes.size() == 0)
            fEndTime = m_TranslationKeyframes.getLast().getFrameTime();
        else if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() > 0)
            fEndTime = m_RotationKeyframes.getLast().getFrameTime();
        else
            fEndTime = Math.max(m_TranslationKeyframes.getLast().getFrameTime(), m_RotationKeyframes.getLast().getFrameTime());

        return fEndTime;
    }
    
    /**
     * Adjusts all the keyframe times.
     * @param fAmount The amount to adjust each keyframe time by.
     */
    public void adjustKeyframeTimes(float fAmount)
    {
        int a;
        VectorKeyframe pKeyframe;

        for (a=0; a<getTranslationKeyframeCount(); a++)
        {
            pKeyframe = getTranslationKeyframe(a);

            pKeyframe.setFrameTime(pKeyframe.getFrameTime() + fAmount);
        }

        for (a=0; a<getRotationKeyframeCount(); a++)
        {
            pKeyframe = getRotationKeyframe(a);

            pKeyframe.setFrameTime(pKeyframe.getFrameTime() + fAmount);
        }
    }

    /**
     * Appends a JointChannel onto the end of this JointChannel.
     * @param pJointChannel The JointChannel to append onto this one.
     */
    public void append(PJointChannel pJointChannel, float fOffset)
    {
        VectorKeyframe pKeyframe;

        //  Adjust all the KeyframeTimes.
        pJointChannel.adjustKeyframeTimes(fOffset);

        for (int i = 0; i < getTranslationKeyframeCount(); i++)
        {
            pKeyframe = getTranslationKeyframe(i);
            m_TranslationKeyframes.add(pKeyframe);
        }

        for (int i = 0; i < getRotationKeyframeCount(); i++)
        {
            pKeyframe = getRotationKeyframe(i);
            m_RotationKeyframes.add(pKeyframe);
        }

        pJointChannel.clear();
    }

    public void closeCycle(AnimationCycle cycle)
    {
        // throw new UnsupportedOperationException("TODO! hahahahahhahaha");
    }
}




