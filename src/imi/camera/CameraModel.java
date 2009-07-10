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
package imi.camera;

import imi.scene.PMatrix;

/**
 * Defines the way a camera model may be generalized.
 * @author Ronald E Dahlgren
 */
public abstract class CameraModel
{
    /**
     * Determine the position and orientation of the camera given the specified
     * state and store it in the provided transform.
     *
     * <p>The result is stored in the transform. Implementations may require
     * that the transform not be null or have the current camera transform. See
     * their documentation for details.</p>
     * @param state The current state
     * @param transform Output
     */
    public abstract void determineTransform(AbstractCameraState state, PMatrix transform);

    /**
     * This method should be used to tunnel AWT events into the model.
     *
     * <p>The model then operates on the state to reflect any changes caused by
     * the input events.</p>
     *
     * @param state The state to affect
     * @param events AWT event array
     */
    public abstract void handleInputEvents(AbstractCameraState state, Object[] events);

    /**
     * Update the camera, this allows for animated camera models.
     * @param state CameraState to affect
     * @param deltaTime The timeslice
     */
    public abstract void update(AbstractCameraState state, float deltaTime);
    /**
     * Determine if the specified type of camera state is valid for this model.
     * @param classz The type of camera state
     * @return True if compatible, false otherwise
     */
    public abstract boolean isStateClassValid(Class<? extends AbstractCameraState> classz);
}
