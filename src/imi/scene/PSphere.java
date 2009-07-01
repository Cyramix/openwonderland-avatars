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
 * Simple geometric sphere representation
 * @author Lou Hayt
 */
public class PSphere implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private Vector3f m_Center = new Vector3f();
    private float m_fRadius = 0.0f;
    
    /**
     * Empty constructor
     */
    public PSphere()
    {
    }

    /**
     * Constructor
     * @param center
     * @param radius
     */
    public PSphere(Vector3f center, float radius)
    {
        m_Center.set(center);
        m_fRadius = radius;
    }

    /**
     * Copy constructor
     * @param other
     */
    public PSphere(PSphere other)
    {
        m_Center.set(other.m_Center);
        m_fRadius = other.m_fRadius;
    }

    /**
     * Get a reference to the center
     * @return
     */
    public Vector3f getCenterRef()
    {
        return m_Center;
    }

    /**
     * Get the center
     * @param center
     */
    public void getCenter(Vector3f center)
    {
        center.set(m_Center);
    }

    /**
     * Get the radius
     * @return
     */
    public float getRadius()
    {
        return m_fRadius;
    }

    /**
     * Set the data for this sphere
     * @param center
     * @param radius
     */
    public void set(Vector3f center, float radius)
    {
        m_Center.set(center);
        m_fRadius = radius;
    }

    /**
     * Set the center of this sphere
     * @param Center
     */
    public void setCenter(Vector3f Center) {
        this.m_Center.set(Center);
    }

    /**
     * Set the radius
     * @param Radius
     */
    public void setRadius(float Radius) {
        this.m_fRadius = Radius;
    }

    /**
     * Set zero values
     */
    public void clear()
    {
        m_Center.zero();
        m_fRadius = 0.0f;
    }

    /**
     * Check for collision
     * @param check
     * @return
     */
    public boolean isColliding(PSphere check) 
    {
        float distanceSqr = m_Center.distanceSquared(check.getCenterRef());
        float sumRadi = m_fRadius + check.getRadius();
        if ( distanceSqr < (sumRadi * sumRadi) )
            return true;
        return false;
    }

    /**
     * Check for collision and get the projection vector that will position
     * outside of intersection.
     * @param check
     * @param projection
     * @return
     */
    public boolean isColliding(PSphere check, Vector3f projection) 
    {
        projection.set(check.getCenterRef().subtract(m_Center));
        float distance = projection.lengthSquared();
        float sumRadi = m_fRadius + check.getRadius();
        if ( (sumRadi * sumRadi) < distance )
            return false;
        
        distance = (float)Math.sqrt((float)distance);
        float delta = sumRadi - Math.abs(distance);
        projection.normalizeLocal();
        projection.multLocal(delta);
        return true;
    }

    @Override
    public String toString() {
        return "PSphere center: " + m_Center + " radius: " + m_fRadius + " " + super.toString();
    }

    /**
     * Debug dump to the console
     * @param spacing
     * @param name
     */
    public void dump(String spacing, String name)
    {
        System.out.println(spacing + name + ":");
        System.out.println(spacing + "   Center:   (" + m_Center.x + ", " + m_Center.y + ", " + m_Center.z + ")");
        System.out.println(spacing + "   fRadius:  " + m_fRadius);
    }
}
