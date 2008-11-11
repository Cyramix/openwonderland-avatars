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
package imi.scene.camera.behaviors;

import imi.scene.PMatrix;
import imi.scene.camera.state.CameraState;

/**
 * Defines the way a camera model may be interacted with.
 * @author Ronald E Dahlgren
 */
public interface CameraModel 
{
    /**
     * Determine the position and orientation of the camera given the specified
     * state. The result is stored in the transform. Implementations may require
     * that the transform not be null or have the current camera transform. See
     * their documentation for details.
     * @param state The current state
     * @param transform Output
     * @throws WrongStateTypeException Thrown if the provided state is not the type
     * required for the implementing camera model
     */
    public void determineTransform(CameraState state, PMatrix transform) throws WrongStateTypeException;
    
    /**
     * This method should be used to tunnel AWT events into the model. The model
     * then operates on the state to reflect any changes caused by the input events.
     * @param state The state to affect
     * @param events AWT event array
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void handleInputEvents(CameraState state, Object[] events) throws WrongStateTypeException;

    /**
     * Update the camera, this allows for animated camera models
     * @param state
     * @param deltaTime
     */
    public void update(CameraState state, float deltaTime) throws WrongStateTypeException;
}
