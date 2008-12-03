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
package org.collada.xml_walker;


import org.collada.colladaschema.LibraryAnimations;

import imi.loaders.collada.Collada;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.COLLADA_JointChannel;
import imi.scene.PMatrix;
import imi.scene.PJoint;

import org.collada.colladaschema.Animation;
import org.collada.colladaschema.Source;
import org.collada.colladaschema.Source.TechniqueCommon;
import org.collada.colladaschema.Param;




/**
 * The LibraryAnimationsProcessor class processes all animation tracks defined
 * in the collada file.
 * 
 * @author Chris Nagle
 */
public class LibraryAnimationsProcessor extends Processor
{
    private String m_AnimatedItemID     = null;
    private String m_AnimatedItemName   = null;
    
    private Source m_pTransformInputSource  = null;
    private Source m_pTransformOutputSource = null;

    private int m_KeyframeCount = -1;


    /**
     * Constructor.
     * 
     * @param pCollada
     * @param pAnimations
     * @param pParent
     */
    public LibraryAnimationsProcessor(Collada colladaRef, LibraryAnimations animationLibrary, Processor pParent)
    {
        super(colladaRef, animationLibrary, pParent);


        if (animationLibrary.getAnimations().size() > 0)
        {
            AnimationGroup newGroup = new AnimationGroup();

            for (Animation anim : animationLibrary.getAnimations())
                processAnimation(anim, newGroup);

            newGroup.calculateDuration();
            newGroup.createDefaultCycle();

            newGroup.getCycle(0).setName(m_colladaRef.getName());
            System.out.println("   Animation:  '" + animationLibrary.getId() + "'");

            //  Add the AnimationLoop to the SkeletonNode.
            m_colladaRef.getSkeletonNode().getAnimationComponent().getGroups().add(newGroup);
        }
    }

    /**
     * Processes an Animation defined in the collada file.
     * 
     * @param pCollada
     * @param pAnimation
     * @param pAnimationLoop
     */
    private void processAnimation(Animation pAnimation, AnimationGroup pAnimationLoop)
    {
        int a;
        float fKeyframeTime;
        PMatrix pKeyframeMatrix = new PMatrix();
        COLLADA_JointChannel pAnimationChannel = null;


        m_AnimatedItemID = pAnimation.getId();
        int periodIndex = m_AnimatedItemID.indexOf(".");
        if (periodIndex != -1)
        {
            m_AnimatedItemName = m_AnimatedItemID.substring(0, periodIndex);
        }
        else
        {
            m_AnimatedItemName = m_AnimatedItemID;
        }

        
        String jointName = m_AnimatedItemName;

        PJoint pJoint = m_colladaRef.getSkeletonNode().getJoint(jointName);
        if (pJoint == null)
        {
//            System.out.println("   Skipping AnimationChannel for joint " + jointName + ".");
            return;
        }
        
        //System.out.println("AnimationChannel:  " + m_AnimatedItemName);

        //  The 'input' source contains keyframe times.  1 float per keyframe.
        Source pInputSource = getSource(pAnimation, "input");
        if (pInputSource != null)
        {
            String inputSourceParamName = getSourceParamName(pInputSource);
            String inputSourceParamType = getSourceParamType(pInputSource);
            if (pInputSource.getId().endsWith("transform-input"))
            {
                m_pTransformInputSource = pInputSource;
                m_KeyframeCount = pInputSource.getFloatArray().getValues().size();
            }
        }

        //  The 'output' source contains keyframe matrices.  16 floats per keyframe.
        Source pOutputSource = getSource(pAnimation, "output");
        if (pOutputSource != null)
        {
            String outputSourceParamName = getSourceParamName(pOutputSource);
            String outputSourceParamType = getSourceParamType(pOutputSource);
            if (pOutputSource.getId().endsWith("transform-output"))
            {
                m_pTransformOutputSource = pOutputSource;
            }
        }

        
                
        //  Create the JointChannel.
        pAnimationChannel = new COLLADA_JointChannel(m_AnimatedItemName);


        //  Create all the MatrixKeyframes.
        for (a=0; a<m_KeyframeCount; a++)
        {
            fKeyframeTime = getKeyframeTime(a);
            getKeyframeMatrix(a, pKeyframeMatrix);

            pAnimationChannel.addKeyframe(fKeyframeTime, pKeyframeMatrix);
        }

        pAnimationChannel.calculateDuration();

//        System.out.println("      AnimationChannel:  " + m_AnimatedItemName + ", KeyframeCount=" + m_KeyframeCount);

        pAnimationChannel.setBindMatrix(pJoint.getTransform().getLocalMatrix(false));

        //  Add the JointAnimation to the AnimationLoop.
        pAnimationLoop.getChannels().add(pAnimationChannel);
    }

    /**
     * Gets the Source of the specified type from the Animation.
     * 
     * @param pAnimation
     * @param sourceType
     * @return Source
     */
    private Source getSource(Animation pAnimation, String sourceType)
    {
        Source pSource;

        for (int a=0; a<pAnimation.getSources().size(); a++)
        {
            pSource = pAnimation.getSources().get(a);
            if (pSource.getId().endsWith(sourceType))
                return(pSource);
        }

        return(null);
    }

    /**
     * Gets the param name of a Source.
     * 
     * @param pSource
     * @return String
     */
    private String getSourceParamName(Source pSource)
    {
        TechniqueCommon pTechniqueCommon = pSource.getTechniqueCommon();
        Param pParam = (Param)pTechniqueCommon.getAccessor().getParams().get(0);

        return(pParam.getName());
    }

    /**
     * Gets the param type of a Source.
     * 
     * @param pSource
     * @return String
     */
    private String getSourceParamType(Source pSource)
    {
        TechniqueCommon pTechniqueCommon = pSource.getTechniqueCommon();
        Param pParam = (Param)pTechniqueCommon.getAccessor().getParams().get(0);

        return(pParam.getType());
    }

    /**
     * Gets the time of a keyframe.
     * 
     * @param Index
     * @return float
     */
    private float getKeyframeTime(int Index)
    {
        float fKeyframeTime = ((Double)m_pTransformInputSource.getFloatArray().getValues().get(Index)).floatValue();

        return(fKeyframeTime);
    }

    /**
     * Gets the matrix of a keyframe.
     * 
     * @param Index
     * @param pMatrix
     * @return boolean
     */
    private boolean getKeyframeMatrix(int Index, PMatrix matrixOut)
    {
        int FloatIndex = Index * 16;

        //  Sanity check.
        if (FloatIndex < 0 || FloatIndex+16 > m_pTransformOutputSource.getFloatArray().getValues().size())
            return(false);

        float [] matrixFloats = new float[16];

        for (int i = 0; i < 16; i++)
            matrixFloats[i] = ((Double)m_pTransformOutputSource.getFloatArray().getValues().get(FloatIndex + i)).floatValue();

        // Load it into the output matrix
        matrixOut.set(matrixFloats);

        return(true);
    }

}




