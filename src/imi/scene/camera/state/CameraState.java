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
package imi.scene.camera.state;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;

/**
 * The basis for Camera states
 * @author Ronald E Dahlgren
 */
public abstract class CameraState 
{
    /**
     * True to move the camera only if the right mouse button is pressed
     */
    private boolean rightMouseButtonOnly = false;
    
    public boolean isRightMouseButtonOnly() {
        return rightMouseButtonOnly;
    }

    public void setRightMouseButtonOnly(boolean rightMouseButtonOnly) {
        this.rightMouseButtonOnly = rightMouseButtonOnly;
    }

    /**
     * This enumeration is used to specify which type of state
     * a 
     */
    public enum CameraStateType
    {
        AzimuthAngle,
        FirstPerson,
        TumbleObject,
        ThirdPerson,
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
    
    /**
     * Sets the camera position. A state may choose to implement this
     * method and ignore the parameter if it does not make sense for
     * the model.
     * @param position
     */
    protected abstract void setCameraPosition(Vector3f position);

    /**
     * Set the camera's transform
     * @param transform
     */
    public abstract void setCameraTransform(PMatrix transform);
}
