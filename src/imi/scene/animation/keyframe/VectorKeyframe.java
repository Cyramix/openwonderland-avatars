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
package imi.scene.animation.keyframe;


import imi.scene.PMatrix;
import imi.scene.animation.keyframe.KeyframeInterface;
import com.jme.math.Vector3f;
import java.io.Serializable;



/**
 *
 * @author Chris Nagle
 */
public class VectorKeyframe implements KeyframeInterface, Serializable
{
    float    m_fTime = 0.0f;
    Vector3f m_Value = new Vector3f();

    //  Constructor.
    public VectorKeyframe(float time, Vector3f value)
    {
        m_fTime = time;
        m_Value.set(value);
    }

    public VectorKeyframe(VectorKeyframe frame)
    {
        m_fTime = frame.m_fTime;
        m_Value.set(frame.m_Value);
    }

    //  Gets the Time.
    public float getFrameTime()
    {
        return(m_fTime);
    }

    //  Sets the Time.
    public void setFrameTime(float fTime)
    {
        m_fTime = fTime;
    }

    //  Gets the Value.
    public Vector3f getValue()
    {
        return(m_Value);
    }

    //  Sets the Value.
    public void setValue(Vector3f theValue)
    {
        m_Value.set(theValue);
    }

    public boolean equals(KeyframeInterface other)
    {
        if (other instanceof VectorKeyframe)
        {
            return m_Value.equals(((VectorKeyframe)other).getValue());
        }
        else
            return false;
    }

    @Override
    public void adjustTime(float fDelta) {
        m_fTime += fDelta;
    }

    @Override
    public KeyframeInterface duplicate() {
        VectorKeyframe result = new VectorKeyframe(this);
        return result;
    }

    @Override
    public void valueAsPMatrix(PMatrix out) {
        // Load into translation vector
        out.setIdentity();
        out.setTranslation(m_Value);
    }

}



