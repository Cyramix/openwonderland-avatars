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
package imi.loaders.scenebindings;

import com.jme.math.Vector3f;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents a row of floats. This row can be interpreted in many
 * ways, we are using it to represent a row in a 4x4 transform matrix
 * @author Ronald E Dahlgren
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Scale", propOrder = {
    "x",
    "y",
    "z"
})
public class sbScale 
{
    @XmlElement(name = "X", required = true)
    private float x;
    @XmlElement(name = "Y", required = true)
    private float y;
    @XmlElement(name = "Z", required = true)
    private float z;
    
    public sbScale()
    {
        x = y = z = 0.0f;
    }
    
    public sbScale(float x, float y, float z)
    {
        set(x, y, z);
    }
    
    /////////////////////////////////////////////////////////////
    
    public Vector3f getVec3()
    {
        return new Vector3f(x,y,z);
    }

    /////////////////////////////////////////////////////////////
    
    public void set(float x, float y, float z)
    {   
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public void set(sbScale scale)
    {
        set(scale.x, scale.y, scale.z);
    }

    public float getX() {
        return x;
    }

    public void setX(float m_x) {
        this.x = m_x;
    }

    public float getY() {
        return y;
    }

    public void setY(float m_y) {
        this.y = m_y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float m_z) {
        this.z = m_z;
    }


    
}
