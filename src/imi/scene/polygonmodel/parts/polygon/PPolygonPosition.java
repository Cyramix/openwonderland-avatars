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
package imi.scene.polygonmodel.parts.polygon;

import com.jme.math.Vector3f;
import java.io.Serializable;

public class PPolygonPosition implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    public Vector3f	m_Position = new Vector3f();
    public int m_ReferenceCount = 0;

    public PPolygonPosition(Vector3f position)
    {
        m_Position.set(position);
        
        m_ReferenceCount++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PPolygonPosition other = (PPolygonPosition) obj;
        if (this.m_Position != other.m_Position && (this.m_Position == null || !this.m_Position.equals(other.m_Position))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.m_Position != null ? this.m_Position.hashCode() : 0);
        return hash;
    }

    
}






