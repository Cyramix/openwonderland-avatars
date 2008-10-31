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
                    // Just the first time
                    if (currentFrame == null)
                        currentFrame = frame;

                    // Get the last frame
                    if (frame.getTime() >= fEndTime - Float.MIN_VALUE)
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
                {
                    currentFrame = frame;
                    // In the case that we exhaust the collection of frames and
                    // never hit the else clause, this will ensure that the 
                    // nextFrame will be assigned to a meaningful value.
                    //nextFrame = currentCycleFirstFrame; 
                }
                else 
                {
                    nextFrame = frame;
                    break;
                }
                
            }
        }
        
        // No keyframe in this joint belong to this animation cycle?
        if (!bReverse && nextFrame == null)
        {
            return null;
        }
        
        // These are not good cases, there are no keyframes for this joint at this time in this animation cycle
        if ( currentFrame == null  )
        {
            //System.out.println("current frame is null " + m_TargetJointName + " " + fCurrentTime);
            return null;
        }
        if (  !( currentFrame.getTime() < fEndTime && currentFrame.getTime() > fStartTime )  )
        {
            //System.out.println("outside cycle bounds " + m_TargetJointName + " " + fCurrentTime);
            return null;
        }
        
        
        // Last frame in cycle case
        if (!bReverse && nextFrame.getTime() > fEndTime)
        {
            nextFrame = currentCycleFirstFrame;
            bEdgeOfLoop.set(true);
        }
        
        // It should only be null if we are reversed and hitting the edge
        if (bReverse && nextFrame == null)
        {
            nextFrame = currentCycleLastFrame;
            bEdgeOfLoop.set(true);   
        }
        
        // First frame in cycle case during reverse animation
        if (bReverse && nextFrame.getTime() <=  fStartTime)
        {
            nextFrame = currentCycleLastFrame;
            bEdgeOfLoop.set(true);   
        }
        
        System.out.println(fCurrentTime + " |vReverse " + bReverse + ": " + " start: " + fStartTime + " end: " + fEndTime + " edge loop " + bEdgeOfLoop);
        
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
                    
        //  Are we right at a keyframe.
        if (currentFrame == nextFrame)
        {
            System.out.println(fCurrentTime + " both frames are the same!");
            delta.set(currentFrame.getValue());
        }
        else if (currentFrame != null && nextFrame != null)
        {
            if (bEdgeOfLoop.get())
            {
                //System.out.println(fCurrentTime + "! - EDGE OF LOOP!");
                
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
  
            // Clamp
            if (s < 0.0f || s > 1.0f)
            {
                s = Math.min(s, 1.0f);
                s = Math.max(s, 0.0f);
            }

            delta.set(blendFrames(currentFrame, nextFrame, s).getValue());
        }

        if (nextFrame == null)
            System.out.println("everything is null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        
        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(delta);
        jointToAffect.setDirty(true, true);
        
   }
    
    public void calculateBlendedFrame(PJoint jointToAffect, AnimationState state)
    {
        // do we even have animation data?
        if (m_KeyFrames.size() == 0)
            return; // Do nothing
     
        // determine if only one blend succeeds
        boolean bFirstFailed = false;
        
        // determine what two keyframes to interpolate between for the first pose
        float fCurrentTime = state.getCurrentCycleTime();
        float fStartTime   = state.getCurrentCycleStartTime();
        float fEndTime     = state.getCurrentCycleEndTime();
        boolean bReverse   = state.isReverseAnimation();
        BooleanPointer bEdgeOfLoop = new BooleanPointer(false);
        
        float fTransitionCycleTime = state.getTransitionCycleTime();
        
        float s = state.getTimeInTransition() / state.getTransitionDuration(); // For the transition
        
        PMatrixKeyframe [] calculateFrames = calculateFrames(fCurrentTime, fStartTime, fEndTime, bReverse, bEdgeOfLoop);
        
        PMatrixKeyframe resultOne = null;
        
        if (calculateFrames == null)
           bFirstFailed = true;
        else
        {
            PMatrixKeyframe currentFrame            = calculateFrames[0];
            PMatrixKeyframe nextFrame               = calculateFrames[1];
            resultOne = blendFrames(currentFrame, nextFrame, Math.abs((fCurrentTime - fStartTime) / (fEndTime - fStartTime))); // other 's'
        }
        
        // Part two
        fStartTime   = state.getTransitionCycleStartTime();
        fEndTime     = state.getTransitionCycleEndTime();
        bReverse     = state.isTransitionReverseAnimation();
        bEdgeOfLoop.set(false);
        
        calculateFrames = calculateFrames(fTransitionCycleTime, fStartTime, fEndTime, bReverse, bEdgeOfLoop);
        
        if (calculateFrames == null && bFirstFailed == false)
        {
            // apply to the joint
            jointToAffect.getTransform().getLocalMatrix(true).set(resultOne.getValue());
            return;
        }
        else if (calculateFrames == null && bFirstFailed == true)
            return; // double failure!
        
        PMatrixKeyframe currentFrame2           = calculateFrames[0];
        PMatrixKeyframe nextFrame2              = calculateFrames[1];
        PMatrixKeyframe resultTwo               = blendFrames(currentFrame2, nextFrame2, Math.abs((fTransitionCycleTime - fStartTime) / (fEndTime - fStartTime))); // other 's'
        
        PMatrixKeyframe finalValue = blendFrames(resultOne, resultTwo, s);
        
        
        // apply to the joint
        jointToAffect.getTransform().getLocalMatrix(true).set(finalValue.getValue());
//
//        //float lerpValue = 0.0f; // this determines how far in we should interpolate
//        //lerpValue = (fTime1 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
//
//        // grab out the rotation and slerp it
//        Quaternion rotationComponent1 = currentFrame.getValue().getRotation();
//        rotationComponent1.slerp(nextFrame.getValue().getRotation(), s);
//
//        // grab the translation and lerp it
//        Vector3f translationComponent1 = currentFrame.getValue().getTranslation();
//        translationComponent1.interpolate(nextFrame.getValue().getTranslation(), s);
        
        //////////////////////////////////////////////////////
        // determine the information for the second pose    //
        //////////////////////////////////////////////////////
//        
//        fStartTime   = state.getTransitionCycleStartTime();
//        fEndTime     = state.getTransitionCycleEndTime();
//        bReverse     = state.isTransitionReverseAnimation();
//        bEdgeOfLoop.set(false);
//        calculateFrames = calculateFrames(fTransitionCycleTime, fStartTime, fEndTime, bReverse, bEdgeOfLoop);
//        if (calculateFrames == null)
//            return;
//        
//        currentFrame            = calculateFrames[0];
//        nextFrame               = calculateFrames[1];
//        
//        //lerpValue = 0.0f; // this determines how far in we should interpolate
//        //lerpValue = (fTime2 - currentFrame.getTime()) / (nextFrame.getTime() - currentFrame.getTime());
//        // grab out the rotation and slerp it
//        Quaternion rotationComponent2 = currentFrame.getValue().getRotation();
//        rotationComponent2.slerp(nextFrame.getValue().getRotation(), s);
//        
//        // grab the translation and lerp it
//        Vector3f translationComponent2 = currentFrame.getValue().getTranslation();
//        translationComponent2.interpolate(nextFrame.getValue().getTranslation(), s);
//        
//        // Interpolate the two poses
//        rotationComponent1.slerp(rotationComponent2, s);
//        translationComponent1.interpolate(translationComponent2, s);
//        
//        PMatrix delta = new PMatrix();
//        delta.set(rotationComponent1, translationComponent1, 1.0f);
        
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
        PMatrixKeyframe pKeyframe;
            
        for (int i = 0; i < getKeyframeCount(); i++)
        {
            pKeyframe = getKeyframe(i);
                
            pKeyframe.setTime(pKeyframe.getTime() + fAmount);
        }
    }

    /**
     * Appends a JointChannel onto the end of this JointChannel.
     * @param pJointChannel The JointChannel to append onto this one.
     */
    public void append(PJointChannel pJointChannel, float fTimePadding)
    {
        COLLADA_JointChannel pColladaJointChannel = (COLLADA_JointChannel)pJointChannel;
        
        float fEndTime = getEndTime();
        
        PMatrixKeyframe pKeyframe;
        int KeyframeCount = pColladaJointChannel.getKeyframeCount();

//        System.out.println("COLLADA_JointChannel.append(), fEndTime = " + fEndTime);

        //  Adjust all the KeyframeTimes.
        pJointChannel.adjustKeyframeTimes(fEndTime + fTimePadding);

        for (int i = 0; i < KeyframeCount; i++)
        {
            pKeyframe = pColladaJointChannel.getKeyframe(i);

            m_KeyFrames.add(pKeyframe);
        }

        pJointChannel.clear();

        calculateDuration();
    }
    
    /**
     * This method blends the two provided keyframes and returns a result. The blend
     * process is (abstractly) s * frameOne + (1 - s) * frameTwo
     * The time value for the return frame is not relevant and should not be
     * relied upon in any calculations.
     * @param frameOne
     * @param frameTwo
     * @param s The interpolation coefficient
     * @return The blended result of the two provided keyframes.
     */
    public PMatrixKeyframe blendFrames(PMatrixKeyframe frameOne, PMatrixKeyframe frameTwo, float s)
    {
        // check for null scenarios
        if (frameOne == null && frameTwo != null)
            return new PMatrixKeyframe(frameTwo);
        else if (frameOne != null && frameTwo == null)
            return new PMatrixKeyframe(frameOne);
        else if (frameOne == null && frameTwo == null)
            return null;
        
        PMatrixKeyframe result = new PMatrixKeyframe(1, new PMatrix());
        float []rotationAngles = new float[3];
        
        PMatrix matrixOne = frameOne.getValue();
        PMatrix matrixTwo = frameTwo.getValue();
        
        // blend the two matrices
        // grab out the rotation and slerp it
        Quaternion rotationComponent = matrixOne.getRotationJME();
        rotationComponent.slerp(rotationComponent, matrixTwo.getRotationJME(), s);
        
        rotationComponent.toAngles(rotationAngles);

        // grab the translation and lerp it
        Vector3f translationComponent = new Vector3f(matrixOne.getTranslation());
        translationComponent.interpolate(matrixTwo.getTranslation(), s);

        // apply this to our delta matrix
        PMatrix finalMatrix = result.getValue();
        
        finalMatrix.set2(rotationComponent, translationComponent, 1.0f);
        
        result.setTime(-1.0f);
        
        return result;
    }

}




