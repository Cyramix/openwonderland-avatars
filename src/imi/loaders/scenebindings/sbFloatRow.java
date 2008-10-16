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
import org.lwjgl.util.vector.Vector4f;

/**
 * This class represents a row of floats. This row can be interpreted in many
 * ways, we are using it to represent a row in a 4x4 transform matrix
 * *NOTE* Comment out all vector references for schema generation.
 * @author Ronald E Dahlgren
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FloatRow", propOrder = {
    "x",
    "y",
    "z",
    "w"
})
public class sbFloatRow 
{
    @XmlElement(name = "X", required = true)
    private float x;
    @XmlElement(name = "Y", required = true)
    private float y;
    @XmlElement(name = "Z", required = true)
    private float z;
    @XmlElement(name = "W", required = true)
    private float w;
    
    public sbFloatRow()
    {
        x = y = z = w = 0.0f;
    }
    
    public sbFloatRow(sbFloatRow other)
    {
        set(other.x, other.y, other.z, other.w);
    }
    
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Sets the components, assumes the w component to be 0
     * @param vec
     */
    public sbFloatRow(Vector3f vec)
    {
        set(vec.x, vec.y, vec.z, 1.0f);
    }

    public sbFloatRow(Vector4f vec)
    {
        set(vec.x, vec.y, vec.z, vec.w);
    }
    
    public Vector3f getVec3()
    {
        return new Vector3f(x,y,z);
    }

    public Vector4f getVec4()
    {
        return new Vector4f(x,y,z,w);
    }
    
    /////////////////////////////////////////////////////////////////////////
    
    public sbFloatRow(float x, float y, float z, float w)
    {
        set(x, y, z, w);
    }

    public void set(float x, float y, float z, float w)
    {   
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    public void set(sbFloatRow row)
    {
        set(row.x, row.y, row.z, row.w);
    }

    public float getW() {
        return w;
    }

    public void setW(float m_w) {
        this.w = m_w;
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
