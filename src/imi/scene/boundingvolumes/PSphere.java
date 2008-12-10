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

public class PSphere
{
    private Vector3f m_Center = new Vector3f();
    private float m_fRadius = 0.0f;

    //  Constructor.
    public PSphere()
    {
    }
    
    public PSphere(Vector3f center, float radius)
    {
        m_Center.set(center);
        m_fRadius = radius;
    }
    
    public PSphere(PSphere other)
    {
        m_Center.set(other.m_Center);
        m_fRadius = other.m_fRadius;
    }
    
    public Vector3f getCenter()
    {
        return m_Center;
    }
    
    public float getRadius()
    {
        return m_fRadius;
    }

    public void set(Vector3f center, float radius)
    {
        m_Center.set(center);
        m_fRadius = radius;
    }

    public void setCenter(Vector3f Center) {
        this.m_Center = Center;
    }

    public void setRadius(float Radius) {
        this.m_fRadius = Radius;
    }

    //  Clears the Sphere.
    public void clear()
    {
        m_Center.zero();
        m_fRadius = 0.0f;
    }
    
    public boolean isColliding(PSphere check) 
    {
        float distanceSqr = m_Center.distanceSquared(check.getCenter());
        float sumRadi = m_fRadius + check.getRadius();
        if ( distanceSqr < (sumRadi * sumRadi) )
            return true;
        return false;
    }
    

    public void dump(String spacing, String name)
    {
        System.out.println(spacing + name + ":");
        System.out.println(spacing + "   Center:   (" + m_Center.x + ", " + m_Center.y + ", " + m_Center.z + ")");
        System.out.println(spacing + "   fRadius:  " + m_fRadius);
    }
}
