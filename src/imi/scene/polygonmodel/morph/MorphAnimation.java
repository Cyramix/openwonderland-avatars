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

import com.jme.math.Vector2f;
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimation 
{
    private String      m_Name      = "";
    
    private ArrayList   m_MorphAnimationLoops = new ArrayList();
    
    private Vector2f [] m_TexCoords = null;
    
    private int []      m_Indices   = null;
    
    public MorphAnimation(String name)
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
    
    public Vector2f getTexCoord(int index)
    {
        if (index < 0 || index > m_TexCoords.length)
            return null;
        
        return m_TexCoords[index];
    }
    
    public Vector2f [] getTexCoords()
    {
        return m_TexCoords;
    }
    
    public void setTexCoords(Vector2f [] coords)
    {
        m_TexCoords = coords;
    }
    
    public int getIndex(int index)
    {
        return m_Indices[index];
    }
    
    public int [] getIndices()
    {
        return m_Indices;
    }
    
    public void setIndices(int [] indecies)
    {
        m_Indices = indecies;
    }
    
    public void addMorphAnimationLoop(MorphAnimationLoop pAnimationLoop)
    {
        if (pAnimationLoop != null)
            m_MorphAnimationLoops.add(pAnimationLoop);
    }
    
    public int getMorphAnimationLoopCount()
    {
        return(m_MorphAnimationLoops.size());
    }
    
    public MorphAnimationLoop getMorphAnimationLoop(int Index)
    {
        if (Index < 0 || Index >= m_MorphAnimationLoops.size())
            return(null);
        
        return( (MorphAnimationLoop)m_MorphAnimationLoops.get(Index));
    }
   
    public MorphAnimationLoop getMorphAnimationLoop(String name)
    {
        MorphAnimationLoop pAnimationLoop;
        
        for (int i = 0; i < getMorphAnimationLoopCount(); i++)
        {
            pAnimationLoop = getMorphAnimationLoop(i);
            
            if (pAnimationLoop.getName().equals(name))
                return(pAnimationLoop);
        }
        
        return(null);
    }

    
    
    public void populateWithTestData()
    {
        //  setTexCoords
        m_TexCoords    = new Vector2f[3];
        m_TexCoords[0] = new Vector2f(0.5f, 0.0f);
        m_TexCoords[1] = new Vector2f(1.0f, 1.0f);
        m_TexCoords[2] = new Vector2f(0.0f, 1.0f);
        
        //  setIndices
        m_Indices    = new int[3];
        m_Indices[0] = 0;
        m_Indices[1] = 1;
        m_Indices[2] = 2;
    
        // addMorphAnimationLoop - Test loop 1
        MorphAnimationLoop pAnimationLoop1 = new MorphAnimationLoop("TestLoop1");
        
        MorphAnimationKeyframe pKeyframe1 = new MorphAnimationKeyframe();
        pKeyframe1.addPosition(0, -5, 1);
        pKeyframe1.addNormal(0, 1, 0);
        pKeyframe1.addPosition(1, -5, -1);
        pKeyframe1.addNormal(0, 1, 0);
        pKeyframe1.addPosition(-1, -5, -1);
        pKeyframe1.addNormal(0, 1, 0);
        
        MorphAnimationKeyframe pKeyframe2 = new MorphAnimationKeyframe();
        pKeyframe2.addPosition(0, -5, 10);
        pKeyframe2.addNormal(0, 1, 0);
        pKeyframe2.addPosition(10, -5, -10);
        pKeyframe2.addNormal(0, 1, 0);
        pKeyframe2.addPosition(-10, -5, -10);
        pKeyframe2.addNormal(0, 1, 0);
        
        pAnimationLoop1.addMorphAnimationKeyframe(pKeyframe1);
        pAnimationLoop1.addMorphAnimationKeyframe(pKeyframe2);

        pAnimationLoop1.setDuration(5.0f);
        this.addMorphAnimationLoop(pAnimationLoop1);
        
        // addMorphAnimationLoop - Test loop 2
        MorphAnimationLoop pAnimationLoop2 = new MorphAnimationLoop("TestLoop2");
        
        MorphAnimationKeyframe pKeyframe21 = new MorphAnimationKeyframe();
        pKeyframe21.addPosition(0, -5, 1);
        pKeyframe21.addNormal(0, 1, 0);
        pKeyframe21.addPosition(1, -5, -1);
        pKeyframe21.addNormal(0, 1, 0);
        pKeyframe21.addPosition(-1, -5, -1);
        pKeyframe21.addNormal(0, 1, 0);
        
        MorphAnimationKeyframe pKeyframe22 = new MorphAnimationKeyframe();
        pKeyframe22.addPosition(0, 5, 1);
        pKeyframe22.addNormal(0, 1, 0);
        pKeyframe22.addPosition(1, 5, -1);
        pKeyframe22.addNormal(0, 1, 0);
        pKeyframe22.addPosition(-1, 5, -1);
        pKeyframe22.addNormal(0, 1, 0);
        
        pAnimationLoop2.addMorphAnimationKeyframe(pKeyframe21);
        pAnimationLoop2.addMorphAnimationKeyframe(pKeyframe22);

        pAnimationLoop2.setDuration(5.0f);
        this.addMorphAnimationLoop(pAnimationLoop2);
    }
    
    
}
