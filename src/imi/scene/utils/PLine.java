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
package imi.scene.utils;

import com.jme.math.Vector3f;

/**
 *
 * @author Lou Hayt
 */
public class PLine 
{
    public Vector3f m_point1 = null;
    public Vector3f m_point2 = null;
    
    public PLine(Vector3f point1, Vector3f point2)
    {
        if (point1 == null || point2 == null)
            return;
        
        m_point1 = new Vector3f(point1);
        m_point2 = new Vector3f(point2);
    }
    
    @Override
    public boolean equals(Object otherObject)
    {
        if (otherObject == null)
            return false;
     
        if (this == otherObject)
            return true;
        
        if (otherObject.getClass() != this.getClass())
            return false;
        
        PLine other = (PLine)otherObject;
        
        if (  m_point1.equals(other.m_point1) && m_point2.equals(other.m_point2)  )
            return true;
        
        if (  m_point1.equals(other.m_point2) && m_point2.equals(other.m_point1)  )
            return true;
        
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.m_point1 != null ? this.m_point1.hashCode() : 0);
        hash = 47 * hash + (this.m_point2 != null ? this.m_point2.hashCode() : 0);
        return hash;
    }
}
