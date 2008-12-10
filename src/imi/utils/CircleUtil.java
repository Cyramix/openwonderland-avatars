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
package imi.utils;

import com.jme.math.Vector2f;

/**
 *
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class CircleUtil 
{
    private int     m_points = 0;
    private float   m_radius = 0.0f;
    Vector2f []     m_circle = null;
    
    public CircleUtil(int points, float radius)
    {
        m_points = points;
        m_radius = radius;
    }
    
    public Vector2f [] calculatePoints()
    {
        m_circle = new Vector2f [m_points];
        
        double angleStep = Math.PI * 2 / m_points;
        double theta = 0.0f;
        
        for (int i = 0; i < m_points; i++)
        {
            m_circle[i] = new Vector2f();
            m_circle[i].x = (float)Math.cos(theta) * -1.0f * m_radius;
            m_circle[i].y = (float)Math.sin(theta) * m_radius;
            
            theta += angleStep;
        }
        
        return m_circle;
    }
    
    public Vector2f [] getCircleRef()
    {
        return m_circle;
    }
    
    public Vector2f getPoint(int index)
    {
        if (m_circle != null)
            return m_circle[index];
        return null;
    }
}
