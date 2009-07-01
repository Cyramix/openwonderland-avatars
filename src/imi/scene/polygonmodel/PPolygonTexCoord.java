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


import com.jme.math.Vector2f;
import java.io.Serializable;

/**
 * Wraps texture coordates
 * @author Lou Hayt
 */
public class PPolygonTexCoord implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    public Vector2f	m_TexCoord = new Vector2f();
    public int m_ReferenceCount = 0;

    public PPolygonTexCoord(Vector2f texCoord)
    {
        m_TexCoord.set(texCoord);
        
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
        final PPolygonTexCoord other = (PPolygonTexCoord) obj;
        if (this.m_TexCoord != other.m_TexCoord && (this.m_TexCoord == null || !this.m_TexCoord.equals(other.m_TexCoord))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() 
    {
        int hash = 7;
        hash = 97 * hash + (this.m_TexCoord != null ? this.m_TexCoord.hashCode() : 0);
        return hash;
    }

}


