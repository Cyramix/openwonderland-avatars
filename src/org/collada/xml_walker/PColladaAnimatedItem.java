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


import java.lang.Double;
import org.collada.colladaschema.Animation;
import org.collada.colladaschema.Source;
import org.collada.colladaschema.Source.TechniqueCommon;
import org.collada.colladaschema.Param;

import imi.scene.PMatrix;



/**
 * The PColladaAnimatedItem class represents animation data in a collada file.
 * This class is used to store the keyframe animation data for skinned meshes.
 *
 * @author Chris Nagle
 */
public class PColladaAnimatedItem
{
    String                  m_AnimatedItemID;
    String                  m_AnimatedItemName;
    String                  m_Type;
    
    Animation               m_pAnimation;
    Source                  m_pTransformInputSource;
    Source                  m_pTransformOutputSource;
    Source                  m_pTransformInterpolationSource;

    int                     m_KeyframeCount;



    /**
     * Default constructor.
     */
    public PColladaAnimatedItem()
    {
    }

    /**
     * Constructor.
     * 
     * @param pAnimation Collada animation PColladaAnimatedItem wrappers.
     */
    public PColladaAnimatedItem(Animation pAnimation)
    {
        setAnimation(pAnimation);
    }



    /**
     * Sets the collada animation
     * 
     * @param pAnimation Collada animation PColladaAnimatedItem wrappers.
     */
    public void setAnimation(Animation pAnimation)
    {
        m_pAnimation = pAnimation;

        //  Sanity check.
        if (m_pAnimation == null)
            return;

        int a;
        Source pSource;
        String sourceID;

        m_AnimatedItemID = pAnimation.getId();
        int periodIndex = m_AnimatedItemID.indexOf(".");
        if (periodIndex != -1)
        {
            m_AnimatedItemName = m_AnimatedItemID.substring(0, periodIndex);
            m_Type = m_AnimatedItemID.substring(periodIndex+1);
        }
        else
        {
            m_AnimatedItemName = m_AnimatedItemID;
            m_Type = "Unknown";
        }

        //System.out.println("Animated Item:  " + m_AnimatedItemName);

        Source pInputSource = getSource("input");
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

        Source pOutputSource = getSource("output");
        if (pOutputSource != null)
        {
            String outputSourceParamName = getSourceParamName(pOutputSource);
            String outputSourceParamType = getSourceParamType(pOutputSource);
            if (pOutputSource.getId().endsWith("transform-output"))
            {
                m_pTransformOutputSource = pOutputSource;
            }
        }

/*
        for (a=0; a<pAnimation.getSources().size(); a++)
        {
            pSource = pAnimation.getSources().get(a);
            sourceID = pSource.getId();

            //  Keyframe Time.
            if (sourceID.endsWith("transform-input"))
            {
                m_pTransformInputSource = pSource;
                m_KeyframeCount = pSource.getFloatArray().getValues().size();
            }
            //  Keyframe Matrices.
            else if (sourceID.endsWith("transform-output"))
            {
                m_pTransformOutputSource = pSource;
            }
            //  Keyframe Interpolation.
            else if (sourceID.endsWith("transform-interpolations"))
            {
                m_pTransformInterpolationSource = pSource;
            }
        }
*/

        System.out.println("Animation:  " + pAnimation.getId() + ", KeyframeCount=" + m_KeyframeCount);
    }


    /**
     * Gets the collada source that is of the specified type. 
     * 'input' source contains times of keyframes.
     * 'output' source contains data for all keyframes.  (matrices)
     *
     * @param sourceType
     * @return Source - Collada source.
     */
    private Source getSource(String sourceType)
    {
        Source pSource;

        for (int a=0; a<m_pAnimation.getSources().size(); a++)
        {
            pSource = m_pAnimation.getSources().get(a);
            if (pSource.getId().endsWith(sourceType))
                return(pSource);
        }

        return(null);
    }

    /**
     * Gets the name of a collada source.
     *
     * @param pSource
     * @return String The name of the collada source.
     */
    private String getSourceParamName(Source pSource)
    {
        TechniqueCommon pTechniqueCommon = pSource.getTechniqueCommon();
        Param pParam = (Param)pTechniqueCommon.getAccessor().getParams().get(0);
        
        return(pParam.getName());
    }

    /**
     * Gets the type of a collada source.
     *
     * @param pSource
     * @return String The type of the collada source.
     */
    private String getSourceParamType(Source pSource)
    {
        TechniqueCommon pTechniqueCommon = pSource.getTechniqueCommon();
        Param pParam = (Param)pTechniqueCommon.getAccessor().getParams().get(0);
        
        return(pParam.getType());
    }



    /**
     * Gets the ID of the AnimatedItem.
     * 
     * @return String The ID of the AnimatedItem.
     */
    public String getAnimatedItemID()
    {
        return(m_AnimatedItemID);
    }

    /**
     * Gets the name of the AnimatedItem.
     * 
     * @return String - The name of the AnimatedItem.
     */
    public String getName()
    {
        return(m_AnimatedItemName);
    }

    /**
     * Gets the type of the AnimatedItem.
     *
     * @return String - The type of the AnimatedItem.
     */
    public String getType()
    {
        return(m_Type);
    }



    /**
     * Gets the number of keyframes.
     * 
     * @return int
     */
    public int getKeyframeCount()
    {
        return(m_KeyframeCount);
    }

    /**
     * Gets the time of the keyframe at the specified index.
     * 
     * @param Index - Index of the keyframe.
     * @return float - The time of the requested keyframe.
     */
    public float getKeyframeTime(int Index)
    {
        float fKeyframeTime = ((Double)m_pTransformInputSource.getFloatArray().getValues().get(Index)).floatValue();

        return(fKeyframeTime);
    }

    /**
     * Gets the matrix of the keyframe at the specified index.
     * @param Index - Index of the keyframe.
     * @param pMatrix - Gets filled in with the keyframe's matrix.
     * @return boolean - true if Index is valid, false otherwise.
     */
    public boolean getKeyframeMatrix(int Index, PMatrix pMatrix)
    {
        int FloatIndex = Index * 16;
        
        //  Sanity check.
        if (FloatIndex < 0 || FloatIndex+16 > m_pTransformOutputSource.getFloatArray().getValues().size())
            return(false);

        float []pMatrixFloats = pMatrix.getData();
        
        for (int a= 0; a<16; a++)
        {
            pMatrixFloats[a] = ((Double)m_pTransformOutputSource.getFloatArray().getValues().get(FloatIndex+a)).floatValue();
        }
        
        return(true);
    }
}








