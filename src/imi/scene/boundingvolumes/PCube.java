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
package imi.scene.boundingvolumes;


import com.jme.math.Vector3f;
import java.io.Serializable;




public class PCube implements Serializable
{
    private Vector3f m_MinCorner = new Vector3f(Vector3f.ZERO);
    private Vector3f m_MaxCorner = new Vector3f(Vector3f.ZERO);

    //  Constructor.
    public PCube()
    {
        
    }
    
    public Vector3f getMin()
    {
        return m_MinCorner;
    }
    
    public Vector3f getMax()
    {
        return m_MaxCorner;
    }

    public void set(Vector3f min, Vector3f max)
    {
        if (min != null)
            m_MinCorner = min;
        if (max != null)
            m_MaxCorner = max;
    }

    public float getWidth()
    {
        return m_MaxCorner.x - m_MinCorner.x;
    }
    
    public float getHeight()
    {
        return m_MaxCorner.y - m_MinCorner.y;
    }
    
    public float getDepth()
    {
        return m_MaxCorner.z - m_MinCorner.z; 
    }
    
    public Vector3f getCenter()
    {
        Vector3f center = m_MaxCorner.add(m_MinCorner);
        return center.divide(2.0f);
    }
    
    //  Clears the Cube.
    public void clear()
    {
        m_MinCorner.zero();
        m_MaxCorner.zero();
    }


    public void dump(String spacing, String name)
    {
        System.out.println(spacing + name + ":");
        System.out.println(spacing + "   MinCorner:  (" + m_MinCorner.x + ", " + m_MinCorner.y + ", " + m_MinCorner.z + ")");
        System.out.println(spacing + "   MaxCorner:  (" + m_MaxCorner.x + ", " + m_MaxCorner.y + ", " + m_MaxCorner.z + ")");
    }
}
