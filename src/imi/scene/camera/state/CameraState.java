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
package imi.scene.camera.state;

/**
 * The basis for Camera states
 * @author Ronald E Dahlgren
 */
public abstract class CameraState 
{
    /**
     * This enumeration is used to specify which type of state
     * a 
     */
    public enum CameraStateType
    {
        AzimuthAngle,
        Default
    }
    
    private CameraStateType m_type = CameraStateType.Default;
    
    /**
     * Retrieve the type of this CameraState
     * @return The type
     */
    public CameraStateType getType()
    {
        return m_type;
    }
    
    /**
     * Set the type of this camera state
     * @param type The new type
     */
    protected void setType(CameraStateType type)
    {
        m_type = type;
    }
}
