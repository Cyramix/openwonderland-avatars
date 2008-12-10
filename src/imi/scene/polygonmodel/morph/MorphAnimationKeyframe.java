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

import com.jme.math.Vector3f;
import java.util.ArrayList;

/**
 *
 * @author Lou Hayt
 */
public class MorphAnimationKeyframe 
{
    private float       m_Time      =   0.0f;
    private float       m_Duration  =   0.0f;
    
    private ArrayList   m_Positions =   new ArrayList();
    private ArrayList   m_Normals   =   new ArrayList();
    
    public MorphAnimationKeyframe()
    {
        
    }
    
    public float getTime()
    {
        return m_Time;
    }
    
    public void setTime(float time)
    {
        if (time >= 0.0f)
            m_Time = time;
    }
    
    public float getDuration()
    {
        return m_Duration;
    }
    
    public void setDuration(float duration)
    {
        if (duration >= 0.0f)
            m_Duration = duration;
    }
    
    public void addPosition(Vector3f position)
    {
        if (position != null)
            m_Positions.add(position);
    }
    
    public void addPosition(float x, float y, float z)
    {
        m_Positions.add(new Vector3f(x, y, z));
    }
    
    public int getPositionsCount()
    {
        return m_Positions.size();
    }
    
    public Vector3f getPosition(int index)
    {
        return (Vector3f)m_Positions.get(index);
    }
    
    public ArrayList getPositions()
    {
        return m_Positions;
    }
    
    public void addNormal(Vector3f normal)
    {
        if (normal != null)
        m_Normals.add(normal);
    }
    
    public void addNormal(float x, float y, float z)
    {
        m_Normals.add(new Vector3f(x, y, z));
    }
    
    public int getNormalsCount()
    {
        return m_Normals.size();
    }
    
    public Vector3f getNormal(int index)
    {
        return (Vector3f)m_Normals.get(index);
    }
    
    public ArrayList getNormals()
    {
        return m_Normals;
    }
    
}
