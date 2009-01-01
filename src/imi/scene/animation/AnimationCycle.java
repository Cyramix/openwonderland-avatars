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
package imi.scene.animation;

import java.io.Serializable;

/**
 * Defines an animation cycle within an animation group
 * 
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class AnimationCycle implements Serializable
{
    private String   m_name       = "Untitled Cycle";
    private float    m_fStartTime = 0.0f;
    private float    m_fEndTime   = 0.0f;
    
    /**
     * Empty Constructor
     */
    public AnimationCycle()
    {
        
    }
    
    public AnimationCycle(AnimationCycle other)
    {
        m_name = new String(other.m_name);
        m_fStartTime = other.m_fStartTime;
        m_fEndTime = other.m_fEndTime;
    }
    
    /**
     * The start and end time define the animation cycle within the animation group,
     * the name can be used to refer to the animation later.
     * @param name
     * @param startTime
     * @param endTime
     */
    public AnimationCycle(String name, float startTime, float endTime)
    {
        if (name != null)
            m_name = name;
        
        m_fStartTime = startTime;
        m_fEndTime   = endTime;
    }

    /**
     * @return the time (in relation to the animation group) at the end of the animation
     */
    public float getEndTime() {
        return m_fEndTime;
    }

    /**
     * @param fEndTime - the time (in relation to the animation group) at the end of the animation
     */
    public void setEndTime(float fEndTime) {
        m_fEndTime = fEndTime;
    }

    /**
     * @return the time (in relation to the animation group) at the start of the animation
     */
    public float getStartTime() {
        return m_fStartTime;
    }

    /**
     * @param fStartTime the time (in relation to the animation group) at the start of the animation
     */
    public void setStartTime(float fStartTime) {
        m_fStartTime = fStartTime;
    }

    /**
     * @return the name of this animation cycle
     */
    public String getName() {
        return m_name;
    }

    /**
     * @param name - the name of this animation cycle
     */
    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String toString()
    {
        return new String (m_name + ": " + m_fStartTime + " - " + m_fEndTime);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final AnimationCycle other = (AnimationCycle) obj;
        if ((this.m_name == null) ? (other.m_name != null) : !this.m_name.equals(other.m_name))
        {
            return false;
        }
        if (this.m_fStartTime != other.m_fStartTime)
        {
            return false;
        }
        if (this.m_fEndTime != other.m_fEndTime)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + (this.m_name != null ? this.m_name.hashCode() : 0);
        hash = 29 * hash + Float.floatToIntBits(this.m_fStartTime);
        hash = 29 * hash + Float.floatToIntBits(this.m_fEndTime);
        return hash;
    }


}