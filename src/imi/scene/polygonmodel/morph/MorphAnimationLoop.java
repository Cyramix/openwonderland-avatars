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
package imi.scene.polygonmodel.morph;

import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimationLoop 
{
    private String      m_Name                      =   "";
    private ArrayList   m_MorphAnimationKeyframes   =   new ArrayList();
    private float       m_fDuration                 =   0.0f;
    
    public MorphAnimationLoop(String name)
    {
        setName(name);
    }

    public String getName()
    {
        return m_Name;
    }
    
    public void setName(String name)
    {
        if (name != null)
        {
            m_Name = name;
        }
        else
        {
            m_Name = "No Name";
        }
    }
    
    public void addMorphAnimationKeyframe(MorphAnimationKeyframe pAnimationKeyframe)
    {
        if (pAnimationKeyframe != null)
            m_MorphAnimationKeyframes.add(pAnimationKeyframe);
    }
    
    public int getMorphAnimationKeyframeCount()
    {
        return m_MorphAnimationKeyframes.size();
    }
    
    public int getPositionsCount()
    {
        if (m_MorphAnimationKeyframes.size() > 0)
        {
            return ((MorphAnimationKeyframe)(m_MorphAnimationKeyframes.get(0))).getPositionsCount();
        }
        return 0;
    }

    public MorphAnimationKeyframe getMorphAnimationKeyframe(int Index)
    {
        if (Index < 0 || Index >= m_MorphAnimationKeyframes.size())
            return null;

        return (MorphAnimationKeyframe)m_MorphAnimationKeyframes.get(Index);
    }
    
    public void setDuration(float fDuration)
    {
        if (fDuration < 0.0f)
            return;
        
        m_fDuration = fDuration;

        if (m_MorphAnimationKeyframes.size() > 0)
        {
            float fTimeStep = m_fDuration / (float)getMorphAnimationKeyframeCount();
            float fTime     = 0.0f;
            
            MorphAnimationKeyframe pKeyframe;
            for (int i = 0; i < getMorphAnimationKeyframeCount(); i++)
            {
                pKeyframe = getMorphAnimationKeyframe(i);

                pKeyframe.setTime(fTime);
                pKeyframe.setDuration(fTimeStep);

                fTime += fTimeStep;
            }
        }
    }
    
    public float getDuration()
    {
        return m_fDuration;
    }
    
}
