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
import com.jme.math.Vector3f;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import java.io.Serializable;
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
public class Vector_JointChannel implements PJointChannel, Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private PMatrix                     m_targetJointBindPose    = null; // The bind pose for this joint
    private String                      m_targetJointName        = null; // The joint that is being manipulated
    
    private FastList<VectorKeyframe>    m_TranslationKeyframes   = new FastList<VectorKeyframe>();
    private FastList<VectorKeyframe>    m_RotationKeyframes      = new FastList<VectorKeyframe>();
    
    // Assorted data that is explicitely calculated
    private float                       m_fDuration              = 0.0f;
    private float                       m_fAverageFrameStep      = 0.0f;


    
    //  Constructor land!
    public Vector_JointChannel(String targetBone)
    {
        m_targetJointName = targetBone;
    }
    
    // Copy constructor
    public Vector_JointChannel(Vector_JointChannel jointAnimation)
    {
        if (jointAnimation.m_targetJointBindPose != null)
            m_targetJointBindPose = new PMatrix(jointAnimation.m_targetJointBindPose);
        
        m_targetJointName = jointAnimation.getTargetJointName();
        
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
            if (frame.time < fTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        s = (fTime - currentFrame.time) / (nextFrame.time - currentFrame.time);
        // lerp and determine final translation vector
        Vector3f translationVector = new Vector3f();
        
        if (currentFrame != nextFrame)
            translationVector.interpolate(currentFrame.value, nextFrame.value, s);
        else
            translationVector.set(nextFrame.value);
        
        // do the same for rotation
        currentFrame = m_RotationKeyframes.getFirst();
        nextFrame    = m_RotationKeyframes.getFirst();
        for (VectorKeyframe frame : m_RotationKeyframes)
        {
            if (frame.time < fTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        
        // determine s
        s = (fTime - currentFrame.time) / (nextFrame.time - currentFrame.time);
        // lerp and determine final translation vector
        Vector3f rotationVector = new Vector3f();
        
        if (currentFrame != nextFrame)
            rotationVector.interpolate(currentFrame.value, nextFrame.value, s);
        else
            rotationVector.set(nextFrame.value);
        
        // apply this to the PJoint transform matrix
        PMatrix delta = new PMatrix(rotationVector, Vector3f.UNIT_XYZ, translationVector);
        jointToAffect.getTransform().getLocalMatrix(true).set(m_targetJointBindPose);
        jointToAffect.getTransform().getLocalMatrix(true).fastMul(delta);
    }

   
    public float calculateDuration()
    {
        // check out the start times and the end times
        float fStartTime = Math.min(m_TranslationKeyframes.getFirst().time, m_RotationKeyframes.getFirst().time);
        float fEndTime = Math.max(m_TranslationKeyframes.getLast().time, m_RotationKeyframes.getLast().time);
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
            if (m_TranslationKeyframes.get(index).time > fTime)
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
            if (m_RotationKeyframes.get(index).time > fTime)
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
     * Returns the endtime of the JointChannel.
     * @return float
     */
    @Override
    public float getEndTime()
    {
        float fEndTime = 0.0f;

        if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() == 0)
            fEndTime = 0.0f;
        else if (m_TranslationKeyframes.size() > 0 && m_RotationKeyframes.size() == 0)
            fEndTime = m_TranslationKeyframes.getLast().time;
        else if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() > 0)
            fEndTime = m_RotationKeyframes.getLast().time;
        else
            fEndTime = Math.max(m_TranslationKeyframes.getLast().time, m_RotationKeyframes.getLast().time);

        return fEndTime;
    }

    @Override
    public String toString() {
        return "Target: " + m_targetJointName + ", Duration: " + m_fDuration;
    }

    @Override
    public void applyTransitionPose(PJoint joint, AnimationState state, float lerpCoefficient) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected static class VectorKeyframe implements Serializable
    {
        /** Serialization version number **/
        private static final long serialVersionUID = 1l;

        float    time = 0.0f;
        Vector3f value = new Vector3f();

        //  Constructor.
        public VectorKeyframe(float time, Vector3f value)
        {
            this.time = time;
            this.value.set(value);
        }

        public VectorKeyframe(VectorKeyframe frame)
        {
            time = frame.time;
            value.set(frame.value);
        }
    }
}




