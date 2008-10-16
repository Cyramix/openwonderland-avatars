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
package imi.scene.animation;

import imi.scene.PMatrix;

/**
 * Represents a transformation at a certain time
 * @author Ronald E Dahlgren
 */
public class PMatrixKeyframe 
{
    float       m_fTime = 0.0f;
    PMatrix     m_Value = new PMatrix();

    //  Constructor.
    public PMatrixKeyframe(float time, PMatrix value)
    {
        m_fTime = time;
        m_Value.set(value);
    }

    public PMatrixKeyframe(PMatrixKeyframe frame)
    {
        m_fTime = frame.m_fTime;
        m_Value.set(frame.m_Value);
    }

    //  Gets the Time.
    public float getTime()
    {
        return m_fTime;
    }

    //  Sets the Time.
    public void setTime(float fTime)
    {
        m_fTime = fTime;
    }

    //  Gets the Value.
    public PMatrix getValue()
    {
        return m_Value;
    }

    //  Sets the Value.
    public void setValue(PMatrix theValue)
    {
        m_Value.set(theValue);
    }

}
