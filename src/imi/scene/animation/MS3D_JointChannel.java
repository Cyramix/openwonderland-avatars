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

    public void calculateFrame(PJoint jointToAffect, float fTime)
    {
        // test
        //fTime = 0.01f;
        // do we even have animation data?
        if (m_TranslationKeyframes.size() == 0 || m_RotationKeyframes.size() == 0)
            return; // Do nothing
        
        // determine what two keyframes to interpolate between for translation
        VectorKeyframe currentFrame = m_TranslationKeyframes.getFirst();
        VectorKeyframe nextFrame    = m_TranslationKeyframes.getFirst();
        float s = 0.0f; // this determines how far in we should interpolate
        
        for (VectorKeyframe frame : m_TranslationKeyframes)
        {
            if (frame.getTime() < fTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        s = (fTime - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
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
            if (frame.getTime() < fTime)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        
        // determine s
        s = (fTime - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
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

    public void calculateBlendedFrame(PJoint jointToAffect, float fTime1, float fTime2, float s)
    {
        // do we even have animation data?
        if (m_TranslationKeyframes.size() == 0 || m_RotationKeyframes.size() == 0)
            return; // Do nothing
        
        // determine the first pose
        // determine what two keyframes to interpolate between for translation
        VectorKeyframe currentFrame = m_TranslationKeyframes.getFirst();
        VectorKeyframe nextFrame = m_TranslationKeyframes.getFirst();
        float fLerpValue = 0.0f; // this determines how far in we should interpolate
        
        for (VectorKeyframe frame : m_TranslationKeyframes)
        {
            if (frame.getTime() < fTime1)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fTime1 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
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
            if (frame.getTime() < fTime1)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fTime1 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
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
            if (frame.getTime() < fTime2)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fTime2 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
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
            if (frame.getTime() < fTime2)
                currentFrame = frame;
            else // passed the mark
            {
                nextFrame = frame;
                break; // finished checking
            }
        }
        // determine s
        fLerpValue = (fTime2 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
        // lerp and determine final translation vector
        Vector3f rotationVector2 = new Vector3f();
        if (currentFrame != nextFrame)
            rotationVector2.interpolate(currentFrame.getValue(), nextFrame.getValue(), fLerpValue);
        else
            rotationVector2.set(nextFrame.getValue());
        
        //////////////////////////////
        // interpolate the results  //
        //////////////////////////////
        translationVector.interpolate(translationVector2, s);
        rotationVector.interpolate(rotationVector2, s);
        
        PMatrix delta = new PMatrix(rotationVector, Vector3f.UNIT_XYZ, translationVector);
        
        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(m_targetJointBindPose);
        jointToAffect.getTransform().getLocalMatrix(true).mul(delta);
    }
    
    public float calculateDuration()
    {
        // check out the start times and the end times
        float fStartTime = Math.min(m_TranslationKeyframes.getFirst().getTime(), m_RotationKeyframes.getFirst().getTime());
        float fEndTime = Math.max(m_TranslationKeyframes.getLast().getTime(), m_RotationKeyframes.getLast().getTime());
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
        m_TranslationKeyframes.add(new VectorKeyframe(fTime, Value));
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
    {
        m_RotationKeyframes.add(new VectorKeyframe(fTime, Value));
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
            System.out.print(spacing + "      Keyframe Time=" + pKeyframe.getTime());
            System.out.println(", Value=(" + pKeyframe.getValue().x + ", " + pKeyframe.getValue().y + ", " + pKeyframe.getValue().z + ")");
        }

        System.out.println(spacing + "   RotationKeyframes:  " + getRotationKeyframeCount());
        for (a=0; a<getRotationKeyframeCount(); a++)
        {
            pKeyframe = getRotationKeyframe(a);
            System.out.print(spacing + "      Keyframe Time=" + pKeyframe.getTime());
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
            fStartTime = m_TranslationKeyframes.getFirst().getTime();
        else if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() > 0)
            fStartTime = m_RotationKeyframes.getFirst().getTime();
        else
            fStartTime = Math.max(m_TranslationKeyframes.getFirst().getTime(), m_RotationKeyframes.getFirst().getTime());

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
            fEndTime = m_TranslationKeyframes.getLast().getTime();
        else if (m_TranslationKeyframes.size() == 0 && m_RotationKeyframes.size() > 0)
            fEndTime = m_RotationKeyframes.getLast().getTime();
        else
            fEndTime = Math.max(m_TranslationKeyframes.getLast().getTime(), m_RotationKeyframes.getLast().getTime());

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

            pKeyframe.setTime(pKeyframe.getTime() + fAmount);
        }

        for (a=0; a<getRotationKeyframeCount(); a++)
        {
            pKeyframe = getRotationKeyframe(a);

            pKeyframe.setTime(pKeyframe.getTime() + fAmount);
        }
    }

    /**
     * Appends a JointChannel onto the end of this JointChannel.
     * @param pJointChannel The JointChannel to append onto this one.
     */
    public void append(PJointChannel pJointChannel)
    {
        float fEndTime = getEndTime();
        int a;
        VectorKeyframe pKeyframe;

        //  Adjust all the KeyframeTimes.
        pJointChannel.adjustKeyframeTimes(fEndTime);

        for (a=0; a<getTranslationKeyframeCount(); a++)
        {
            pKeyframe = getTranslationKeyframe(a);
            m_TranslationKeyframes.add(pKeyframe);
        }

        for (a=0; a<getRotationKeyframeCount(); a++)
        {
            pKeyframe = getRotationKeyframe(a);
            m_RotationKeyframes.add(pKeyframe);
        }

        pJointChannel.clear();
    }

}




