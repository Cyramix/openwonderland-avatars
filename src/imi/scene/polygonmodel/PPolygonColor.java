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
package imi.scene.polygonmodel;


import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import java.io.Serializable;

/**
 * Wraps a ColorRGBA
 * @author Lou Hayt
 */
public class PPolygonColor implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    public ColorRGBA	m_Color = new ColorRGBA();
    public int          m_ReferenceCount = 0;

    public PPolygonColor(Vector3f color)
    {
        m_Color.r = color.x;
        m_Color.g = color.y;
        m_Color.b = color.z;
        m_Color.a = 1.0f;
        
        m_ReferenceCount++;
    }
    
    public PPolygonColor(ColorRGBA color)
    {
        m_Color.set(color);
        
        m_ReferenceCount++;
    }

    @Override
    public boolean equals(Object obj) 
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PPolygonColor other = (PPolygonColor) obj;
        if (this.m_Color != other.m_Color && (this.m_Color == null || !colorEquals(other.m_Color))) {
            return false;
        }
        return true;
    }
    
    private boolean colorEquals(ColorRGBA other)
    {
        boolean result = true;
        
        if (m_Color.r != other.r)
            result = false;
        
        if (m_Color.g != other.g)
            result = false;
        
        if (m_Color.b != other.b)
            result = false;
        
        if (m_Color.a != other.a)
            result = false;
        
        return result;
    }

    @Override
    public int hashCode() 
    {
        int hash = 37;
        hash += 37 * hash + Float.floatToIntBits(m_Color.r);
        hash += 37 * hash + Float.floatToIntBits(m_Color.g);
        hash += 37 * hash + Float.floatToIntBits(m_Color.b);
        hash += 37 * hash + Float.floatToIntBits(m_Color.a);
        return hash;
    }

}






