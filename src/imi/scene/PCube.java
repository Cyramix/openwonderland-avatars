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
package imi.scene;


import com.jme.math.Vector3f;
import java.io.Serializable;

/**
 * Simple representation of an Axis Aligned Bounding Box
 * @author Lou Hayt
 */
public class PCube implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private Vector3f m_MinCorner = new Vector3f(Vector3f.ZERO);
    private Vector3f m_MaxCorner = new Vector3f(Vector3f.ZERO);

    //  Empty constructor.
    public PCube() {
    }

    /**
     * Constructor with all relevant data
     * @param min
     * @param max
     */
    public PCube(Vector3f min, Vector3f max) {
        m_MinCorner.set(min);
        m_MaxCorner.set(max);
    }

    /**
     * Copy constructor
     * @param cube
     */
    public PCube(PCube cube) {
        m_MinCorner.set(cube.getMin());
        m_MaxCorner.set(cube.getMax());
    }

    /**
     * Copy constructor with an added offset
     * @param cube
     * @param offset
     */
    public PCube(PCube cube, Vector3f offset) {
        m_MinCorner.set(cube.getMin());
        m_MinCorner.addLocal(offset);
        m_MaxCorner.set(cube.getMax());
        m_MaxCorner.addLocal(offset);
    }

    /**
     * Get the minimum corner position
     * @return
     */
    public Vector3f getMin()
    {
        return m_MinCorner;
    }

    /**
     * Get the maximum corner position
     * @return
     */
    public Vector3f getMax()
    {
        return m_MaxCorner;
    }

    /**
     * Set all relevant data
     * @param min
     * @param max
     */
    public void set(Vector3f min, Vector3f max)
    {
        if (min != null)
            m_MinCorner = min;
        if (max != null)
            m_MaxCorner = max;
    }

    /**
     * Get the width
     * @return
     */
    public float getWidth()
    {
        return m_MaxCorner.x - m_MinCorner.x;
    }

    /**
     * Get the height
     * @return
     */
    public float getHeight()
    {
        return m_MaxCorner.y - m_MinCorner.y;
    }

    /**
     * Get the depth
     * @return
     */
    public float getDepth()
    {
        return m_MaxCorner.z - m_MinCorner.z; 
    }

    /**
     * Get the center
     * @return
     */
    public Vector3f getCenter()
    {
        Vector3f center = m_MaxCorner.add(m_MinCorner);
        return center.mult(0.5f);
    }

    /**
     * Clear to zero values
     */
    public void clear()
    {
        m_MinCorner.zero();
        m_MaxCorner.zero();
    }

    /**
     * Dump to the console
     * @param spacing
     * @param name
     */
    public void dump(String spacing, String name)
    {
        System.out.println(spacing + name + ":");
        System.out.println(spacing + "   MinCorner:  (" + m_MinCorner.x + ", " + m_MinCorner.y + ", " + m_MinCorner.z + ")");
        System.out.println(spacing + "   MaxCorner:  (" + m_MaxCorner.x + ", " + m_MaxCorner.y + ", " + m_MaxCorner.z + ")");
    }
}
