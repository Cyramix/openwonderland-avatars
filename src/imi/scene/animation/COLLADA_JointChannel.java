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
import imi.utils.BooleanPointer;



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
    private float                       m_fLastFrameStep = 0.0f;

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

    // may return null
    private PMatrixKeyframe [] calculateFrames(float fCurrentTime, float fStartTime, float fEndTime, boolean bReverse, BooleanPointer bEdgeOfLoop)
    {
        // determine what two keyframes to interpolate between
        bEdgeOfLoop.set(false);
        PMatrixKeyframe currentFrame            = null;
        PMatrixKeyframe nextFrame               = null;
        PMatrixKeyframe currentCycleFirstFrame  = null;
        PMatrixKeyframe currentCycleLastFrame   = null;
        
        //  Determine the keyframe to the left and right. (current and next)
        for (PMatrixKeyframe frame : m_KeyFrames)
        {
            // Chaching the first frame for later use
            if (frame.getTime() <= fStartTime)
            {
                currentCycleFirstFrame = frame;
            }
            
            if (bReverse)
            {   
                if (frame.getTime() > fCurrentTime)
                {
                    if (currentFrame == null)
                        currentFrame = frame;

                    // Get the last frame
                    if (frame.getTime() >= fEndTime)
                    {
                        currentCycleLastFrame = frame;
                        break;
                    }
                }
                else 
                    nextFrame = frame;
            }
            else
            {
                if (frame.getTime() <= fCurrentTime)
                    currentFrame = frame;
                else 
                {
                    nextFrame = frame;
                    break;
                }
            }
        }
        
        // These are not good cases
        if (  currentFrame == null || !( currentFrame.getTime() < fEndTime && currentFrame.getTime() > fStartTime )  )
                return null;
        
        // It should only be null if in reverse and hitting the edge
        if (nextFrame == null)
            nextFrame = currentCycleLastFrame;
        
        // Last frame in cycle case
        if (!bReverse && nextFrame.getTime() >= fEndTime)
        {
            nextFrame = currentCycleFirstFrame;
            bEdgeOfLoop.set(true);
        }
        
        // First frame in cycle case during reverse animation
        if (bReverse && nextFrame.getTime() <= fStartTime)
        {
            nextFrame = currentCycleLastFrame;
            bEdgeOfLoop.set(true);   
        }
        
        PMatrixKeyframe [] result = new PMatrixKeyframe[2];
        result[0] = currentFrame;
        result[1] = nextFrame;
        return result;
    }
    
    public void calculateFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_KeyFrames.size() == 0)
            return; // Do nothing

        float []rotationAngles = new float[3];
        PMatrix delta = new PMatrix();
        float s = 0.0f; // this determines how far in we should interpolate

        float fCurrentTime = state.getCurrentCycleTime();
        float fStartTime   = state.getCurrentCycleStartTime();
        float fEndTime     = state.getCurrentCycleEndTime();
        boolean bReverse   = state.isReverseAnimation();
        BooleanPointer bEdgeOfLoop = new BooleanPointer(false);
        
        PMatrixKeyframe [] calculateFrames = calculateFrames(fCurrentTime, fStartTime, fEndTime, bReverse, bEdgeOfLoop);
        if (calculateFrames == null)
            return;
        
        PMatrixKeyframe currentFrame            = calculateFrames[0];
        PMatrixKeyframe nextFrame               = calculateFrames[1];
        
        delta.set(currentFrame.getValue());
                    
        //  Are we right at a keyframe.
        if (currentFrame == nextFrame)
        {
            delta.set(currentFrame.getValue());
        }
        else if (currentFrame != null && nextFrame != null)
        {
            if (bEdgeOfLoop.get())
            {
                //System.out.println("! - EDGE OF LOOP!");
                
                if (state.isReverseAnimation())
                    s = (currentFrame.getTime() - fCurrentTime) / m_fLastFrameStep;
                else
                    s = (fCurrentTime - currentFrame.getTime()) / m_fLastFrameStep;
            }
            else
            {
                m_fLastFrameStep = Math.abs(currentFrame.getTime() - nextFrame.getTime());
                
                // determine s
                if (state.isReverseAnimation())
                    s = (currentFrame.getTime() - fCurrentTime) / currentFrame.getTime() - (nextFrame.getTime());
                else
                    s = (fCurrentTime - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
            }


            delta.setIdentity();
  
            // Clamp
            if (s < 0.0f || s > 1.0f)
            {
                s = Math.min(s, 1.0f);
                s = Math.max(s, 0.0f);
            }

            // grab out the rotation and slerp it
            Quaternion rotationComponent = currentFrame.getValue().getRotationJME();
            rotationComponent.slerp(rotationComponent, nextFrame.getValue().getRotationJME(), s);
            rotationComponent.toAngles(rotationAngles);

            // grab the translation and lerp it
            Vector3f translationComponent = new Vector3f(currentFrame.getValue().getTranslation());
            translationComponent.interpolate(nextFrame.getValue().getTranslation(), s);

            // apply this to our delta matrix
            delta.set2(rotationComponent, translationComponent, 1.0f);
        }

        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(delta);
        jointToAffect.setDirty(true, true);
        
        // Debug
        //System.out.println("Cycle " + state.getCurrentCycle() + " | " + state.getCurrentCycleTime() + " Start: " + state.getCurrentCycleStartTime() + " End: " + state.getCurrentCycleEndTime());
    }
    
    public void calculateBlendedFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_KeyFrames.size() == 0)
            return; // Do nothing
        
        // determine what two keyframes to interpolate between for the first pose
        float fCurrentTime = state.getCurrentCycleTime();
        float fStartTime   = state.getCurrentCycleStartTime();
        float fEndTime     = state.getCurrentCycleEndTime();
        boolean bReverse   = state.isReverseAnimation();
        BooleanPointer bEdgeOfLoop = new BooleanPointer(false);
        
        float fTransitionCycleTime = state.getTransitionCycleTime();
        float s = state.getTimeInTransition() / state.getTransitionDuration();
        
        PMatrixKeyframe [] calculateFrames = calculateFrames(fCurrentTime, fStartTime, fEndTime, bReverse, bEdgeOfLoop);
        if (calculateFrames == null)
            return;
      
        PMatrixKeyframe currentFrame            = calculateFrames[0];
        PMatrixKeyframe nextFrame               = calculateFrames[1];

        //float lerpValue = 0.0f; // this determines how far in we should interpolate
        //lerpValue = (fTime1 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());

        // grab out the rotation and slerp it
        Quaternion rotationComponent1 = currentFrame.getValue().getRotation();
        rotationComponent1.slerp(nextFrame.getValue().getRotation(), s);

        // grab the translation and lerp it
        Vector3f translationComponent1 = currentFrame.getValue().getTranslation();
        translationComponent1.interpolate(nextFrame.getValue().getTranslation(), s);
        
        //////////////////////////////////////////////////////
        // determine the information for the second pose    //
        //////////////////////////////////////////////////////
        
        fStartTime   = state.getTransitionCycleStartTime();
        fEndTime     = state.getTransitionCycleEndTime();
        bReverse     = state.isTransitionReverseAnimation();
        bEdgeOfLoop.set(false);
        calculateFrames = calculateFrames(fTransitionCycleTime, fStartTime, fEndTime, bReverse, bEdgeOfLoop);
        if (calculateFrames == null)
            return;
        
        currentFrame            = calculateFrames[0];
        nextFrame               = calculateFrames[1];
        
        //lerpValue = 0.0f; // this determines how far in we should interpolate
        //lerpValue = (fTime2 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
        // grab out the rotation and slerp it
        Quaternion rotationComponent2 = currentFrame.getValue().getRotation();
        rotationComponent2.slerp(nextFrame.getValue().getRotation(), s);
        
        // grab the translation and lerp it
        Vector3f translationComponent2 = currentFrame.getValue().getTranslation();
        translationComponent2.interpolate(nextFrame.getValue().getTranslation(), s);
        
        // Interpolate the two poses
        rotationComponent1.slerp(rotationComponent2, s);
        translationComponent1.interpolate(translationComponent2, s);
        
        PMatrix delta = new PMatrix();
        delta.set(rotationComponent1, translationComponent1, 1.0f);
        
        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(delta);
    }

    public float calculateDuration()
    {
        float fEndTime = m_KeyFrames.getLast().getTime();
        float fStartTime = m_KeyFrames.getFirst().getTime();
        // Calculate
        m_fDuration = fEndTime - fStartTime;

        if (fStartTime > 0.0f)
        {
            m_fDuration += fStartTime;
        }
        else
        {
            float fSecondKeyframeTime = m_KeyFrames.get(1).getTime();
            m_fDuration += fSecondKeyframeTime;
        }
        
        // Return
        return m_fDuration;
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

    //  Adds a Keyframe.
    public void addKeyframe(float fTime, PMatrix Value)
    {
        m_KeyFrames.add(new PMatrixKeyframe(fTime, Value));
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
        int a;
        PMatrixKeyframe pKeyframe;
        
        while (m_KeyFrames.size() > 0)
        {
            pKeyframe = m_KeyFrames.get(m_KeyFrames.size()-1);
            if (pKeyframe.getTime() > fMaxTime)
                m_KeyFrames.remove(m_KeyFrames.size()-1);
            else
            {
//                System.out.println("   Last keyframe time = " + pKeyframe.getTime());
                break;
            }
        }
    
        calculateDuration();
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
    }
    
    /**
     * Returns the starttime of the JointChannel.
     * @return float
     */
    public float getStartTime()
    {
        float fStartTime = 0.0f;

        if (m_KeyFrames.size() > 0)
            fStartTime = m_KeyFrames.getFirst().getTime();

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
            fEndTime = m_KeyFrames.getLast().getTime();

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
                
            pKeyframe.setTime(pKeyframe.getTime() + fAmount);
        }
    }

    /**
     * Appends a JointChannel onto the end of this JointChannel.
     * @param pJointChannel The JointChannel to append onto this one.
     */
    public void append(PJointChannel pJointChannel)
    {
        COLLADA_JointChannel pColladaJointChannel = (COLLADA_JointChannel)pJointChannel;
        float fEndTime = getEndTime();
        int a;
        PMatrixKeyframe pKeyframe;
        int KeyframeCount = pColladaJointChannel.getKeyframeCount();

//        System.out.println("COLLADA_JointChannel.append(), fEndTime = " + fEndTime);

        //  Adjust all the KeyframeTimes.
        pJointChannel.adjustKeyframeTimes(fEndTime);

        for (a=0; a<KeyframeCount; a++)
        {
            pKeyframe = pColladaJointChannel.getKeyframe(a);

            m_KeyFrames.add(pKeyframe);
        }

        pJointChannel.clear();

        calculateDuration();
    }

}




