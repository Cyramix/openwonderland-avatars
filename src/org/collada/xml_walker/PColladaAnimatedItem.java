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
    private String                  m_AnimatedItemID = null;
    private String                  m_AnimatedItemName = null;
    private String                  m_Type = null;
    
    private Animation               m_animation = null;
    private Source                  m_transformInputSource = null;
    private Source                  m_transformOutputSource = null;

    private int                     m_KeyframeCount = 0;

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
    public void setAnimation(Animation theAnimation)
    {
        m_animation = theAnimation;

        //  Sanity check.
        if (m_animation == null)
            return;

        m_AnimatedItemID = theAnimation.getId();
        int periodIndex = m_AnimatedItemID.indexOf(".");
        if (periodIndex != -1)
        {
            m_AnimatedItemName = m_AnimatedItemID.substring(0, periodIndex);
            m_Type = m_AnimatedItemID.substring(periodIndex+1);
        }
        else
        {
            m_AnimatedItemName = m_AnimatedItemID;
            m_Type = "NO PERIOD FOUND IN STRING : PColladaAnimatedItem.java :: setAnimation()";
        }

        Source pInputSource = getSource("input");
        if (pInputSource != null)
        {
            if (pInputSource.getId().endsWith("transform-input"))
            {
                m_transformInputSource = pInputSource;
                m_KeyframeCount = pInputSource.getFloatArray().getValues().size();
            }
        }

        Source pOutputSource = getSource("output");
        if (pOutputSource != null)
        {
            if (pOutputSource.getId().endsWith("transform-output"))
                m_transformOutputSource = pOutputSource;
        }
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
        Source result = null;
        for (Source source : m_animation.getSources())
        {
            if (source.getId().endsWith(sourceType))
            {
                result = source;
                break;
            }
        }
        return result;
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
        float result = (m_transformInputSource.getFloatArray().getValues().get(Index)).floatValue();

        return result;
    }

    /**
     * Gets the matrix of the keyframe at the specified index.
     * @param Index - Index of the keyframe.
     * @param pMatrix - Gets filled in with the keyframe's matrix.
     * @return boolean - true if Index is valid, false otherwise.
     */
    public boolean getKeyframeMatrix(int index, PMatrix matrixOut)
    {
        int FloatIndex = index * 16; // Scale to 1-D array index
        //  Bounds checking
        if (FloatIndex < 0 || FloatIndex+16 > m_transformOutputSource.getFloatArray().getValues().size())
            return false;

        float [] matrixFloats = new float[16];
        
        for (int i = 0; i < 16; i++)
            matrixFloats[i] = ((Double)m_transformOutputSource.getFloatArray().getValues().get(FloatIndex + i)).floatValue();

        // Load it into the output matrix
        matrixOut.set(matrixFloats);
        
        return true;
    }
}








