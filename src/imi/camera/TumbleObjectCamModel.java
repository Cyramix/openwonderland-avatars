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

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * This model performs object tumbling. The camera is locked at a certain perspective
 * and generally not manipulated. The primary intent is to inspect objects. This model
 * also provides the capability to smoothly move to a new position while watching the
 * focal point, as well as smoothly changing focal points.
 * @author Ronald E Dahlgren
 */
public class TumbleObjectCamModel extends CameraModel
{
    // To ensure package construction only
    TumbleObjectCamModel() {}

    /**
     * {@inheritDoc CameraModel}
     */
    public void determineTransform(AbstractCameraState state, PMatrix transform)
    {
        // get a reference to the derived type
        TumbleObjectCamState camState = (TumbleObjectCamState)state;
        // Apply to the camera
        transform.set(camState.getCameraTransform());
    }


    /**
     * {@inheritDoc CameraModel}
     */
    public void handleInputEvents(AbstractCameraState state, Object[] events)
    {
        // Used as a minor optimization if it is unnecessary.
        boolean updateRotations = false;
        // Cast to our derived state type
        TumbleObjectCamState camState = (TumbleObjectCamState)state;
        // parse the events
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                if ( state.isLook(me, MouseEvent.MOUSE_PRESSED) )
                {
                    // Mouse pressed, reset initial settings
                    camState.setCurrentMouseX(me.getX());
                    camState.setCurrentMouseY(me.getY());
                    camState.setLastMouseX(me.getX());
                    camState.setLastMouseY(me.getY());
                }

                if ( state.isLook(me, MouseEvent.MOUSE_DRAGGED) )
                {
                    // Mouse dragged, handle model rotation
                    processRotations(me, camState);
                    updateRotations = true;
                }

                if (me.getID() == MouseEvent.MOUSE_WHEEL)
                {
                    // Wheel action, do some zooming business
                    processMouseWheel((MouseWheelEvent) me,camState);
                }
            } 
            else if (events[i] instanceof KeyEvent) // Process key presses
            {
                // We handle zooming through the key presses currently
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke, camState);
            }
        }
        // Manipulate the state's target
        updateTargetTransform(camState, updateRotations);
        // Synchronize the camera's transform with the rest of the state.
        updateCameraTransform(camState);
    }


    /**
     * Process any mouse wheel events that are received from AWT
     * @param mwe
     * @param camState
     */
    private void processMouseWheel(MouseWheelEvent mwe, TumbleObjectCamState camState)
    {
        // Negative, zoom in; Positive, zoom out
        int clicks = mwe.getWheelRotation() * -1;

        Vector3f zoomVec = new Vector3f(camState.getCameraTransform().getLocalZNormalized());
        zoomVec.multLocal(clicks * 0.05f);

        // Generate a vector from the camera's future position to the focal point and check the length
        Vector3f newPos = camState.getCameraPosition().add(zoomVec);
        Vector3f newPosToTarget = camState.getTargetFocalPointRef().subtract(newPos);


        if (newPosToTarget.dot(camState.getCameraTransform().getLocalZNormalized()) < 0) // Antagonist (tunneling occured)
            return;
        // Dist squared is used to save a square root operation
        if (newPosToTarget.lengthSquared() >= camState.getMinimumDistanceSquared()
           && newPosToTarget.lengthSquared() <= camState.getMaximumDistanceSquared())
            camState.setCameraPosition(newPos, false); // No turn-to needed, we are moving along the correct vector
    }

    /**
     * Process any mouse dragging as a rotation input to the state's target
     * @param me
     * @param camState
     */
    private void processRotations(MouseEvent me, TumbleObjectCamState camState)
    {
        int deltaX = 0;
        
        int localcurrentX = 0;
        int localcurrentY = 0;

        localcurrentX = me.getX();
        localcurrentY = me.getY();

        if (camState.getLastMouseX() == -1) {
            // First time through, just initialize
            camState.setLastMouseX(localcurrentX);
            camState.setLastMouseY(localcurrentY);
        } else {
            deltaX = localcurrentX - camState.getLastMouseX();
            //deltaY = localcurrentY - camState.getLastMouseY(); // <-- May have a use eventually
            // The assignment looks reversed, this is because dragging the mouse
            // along the X axis produces in-game rotation about the Y-axis
            camState.setRotationY(camState.getRotationY() + (deltaX * camState.getScaleRotationX()));
            
            camState.setLastMouseX(localcurrentX);
            camState.setLastMouseY(localcurrentY);
        }
    }

    /**
     * Handle keypresses!
     * @param ke
     * @param camState
     */
    private void processKeyEvent(KeyEvent ke, TumbleObjectCamState camState)
    {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_UP) {
                // Start zooming in
                camState.setMovementState(TumbleObjectCamState.MovementStates.ZOOMING_IN);
            }
            if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                // Start zooming out
                camState.setMovementState(TumbleObjectCamState.MovementStates.ZOOMING_OUT);
            }

        }
        
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_UP ||
                ke.getKeyCode() == KeyEvent.VK_DOWN)
            {
                // Stop all movement
                camState.setMovementState(TumbleObjectCamState.MovementStates.STOPPED);
            }
        }
    }

    /**
     * Manipulate the matrix to match the representation in the provided state
     * @param camState
     * @param targetTransform
     */
    private void updateRotations(TumbleObjectCamState camState, PMatrix targetTransform)
    {
        Vector3f scales = targetTransform.getScaleVector();
        Vector3f translation = targetTransform.getTranslation();
        // Reorient rotation
        targetTransform.set(new Vector3f(0,(float) Math.toRadians(camState.getRotationY()), 0), translation, scales);
    }

    /**
     * Update the camera matrix based on the contents of the state object
     * @param camState
     */
    private void updateCameraTransform(TumbleObjectCamState camState)
    {
        // Cache necessary data
        PMatrix camXForm = camState.getCameraTransform();
        Vector3f targetPosition = camState.getTargetFocalPointRef();
        
        if (camState.needsTargetUpdate())
        {
            // perform lookat to focal point
            Vector3f forward = targetPosition.subtract(camState.getCameraPosition());
            Vector3f right = Vector3f.UNIT_Y.cross(forward);
            Vector3f realUp = forward.cross(right);
            Vector3f translation = (camState.getCameraPosition());
            // load it up manually
            float[] floats = new float[16];
            floats[ 0] = right.x;  floats[ 1] = realUp.x;  floats[ 2] = forward.x;  floats[ 3] = translation.x;
            floats[ 4] = right.y;  floats[ 5] = realUp.y;  floats[ 6] = forward.y;  floats[ 7] = translation.y;
            floats[ 8] = right.z;  floats[ 9] = realUp.z;  floats[10] = forward.z;  floats[11] = translation.z;
            floats[12] = 0.0f;     floats[13] = 0.0f;      floats[14] = 0.0f;       floats[15] = 1.0f;
            
            camXForm.set(floats);
        }

        // Grab here, just in case the above lookAt was executed
        Vector3f position = camXForm.getTranslation();
        Vector3f toTarget = (targetPosition.subtract(position));

        // Used to determine zooming clamp range
        float distSquared = toTarget.lengthSquared();
        toTarget.normalizeLocal();
        // move depending on whether we are zooming or not
        TumbleObjectCamState.MovementStates state = camState.getMovementState();
        switch (state)
        {
            case ZOOMING_IN:
                if (distSquared > camState.getMinimumDistanceSquared()) // Go ahead and zoom
                    position.addLocal(toTarget.mult(camState.getMovementRate())); // TODO : Make time based ( value * deltaTime )
                break;
            case ZOOMING_OUT: // No long range clamping
                position.subtractLocal(toTarget.mult(camState.getMovementRate())); // TODO : Make time based ( value * deltaTime )
                break;
            default:
                break;
        }

        // now position to the transform
        camXForm.setTranslation(position);
        // Apply transform to the camera
        camState.setCameraTransform(camXForm);
        // No longer needing an update
        camState.setTargetNeedsUpdate(false);
    }

    /**
     * Update the transform of the target based on the contents of the state.
     * @param camState
     * @param bUpdateRotations Minor optimization if false
     */
    private void updateTargetTransform(TumbleObjectCamState camState, boolean bUpdateRotations)
    {
       if (camState.getTargetModelInstanceRef() == null) // Model to rotate yet
           return;
       PMatrix transform = camState.getTargetModelInstanceRef().getTransform().getLocalMatrix(true);
       // update the transform with rotational info if necessary
       if (bUpdateRotations)
           updateRotations(camState, transform);
       
       camState.getTargetModelInstanceRef().setDirty(true, true);
    }

    /**
     * Process the update
     * @param state
     * @param deltaTime
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void update(AbstractCameraState state, float deltaTime)
    {
        // Get derived state type
        TumbleObjectCamState camState = (TumbleObjectCamState) state;

        // are we even animating?
        if (camState.getNextFocalPoint() == null && camState.getNextPosition() == null)
            return;

        // Ok grab the two key pieces of data
        Vector3f nextPosition = camState.getNextPosition();
        Vector3f nextFocalPoint = camState.getNextFocalPoint();

        // Check bounds
        if (nextPosition != null)
        {
            float newTime = camState.getTimeInPositionTransition() + deltaTime;
            if (newTime > camState.getTransitionDuration()) // Done
            {
                // Clear out transitioning info
                camState.setTimeInPositionTransition(0.0f);
                camState.setNextPosition(null);
                camState.setOriginalPosition(null);
            }
            else // update the time and do some lerping
            {
                camState.setTimeInPositionTransition(newTime);
                updatePositionTransition(camState, nextPosition);
            }

        }

        if (nextFocalPoint != null)
        {
            float newTime = camState.getTimeInFocusTransition() + deltaTime;
            if (newTime > camState.getTransitionDuration()) // Done
            {
                // Clear out transitioning info
                camState.setTimeInFocusTransition(0.0f);
                camState.setNextFocalPoint(null);
                camState.setOriginalFocalPoint(null);
            }
            else // update the time and do some lerping
            {
                camState.setTimeInFocusTransition(newTime);
                updateFocusTransition(camState, nextFocalPoint);
            }
        }
    }

    /**
     * This method uses lerps between the camera's original position and the
     * provided next position
     * @param camState
     * @param nextPosition
     */
    private void updatePositionTransition(TumbleObjectCamState camState, Vector3f nextPosition)
    {
        float s = camState.getTimeInPositionTransition() / camState.getTransitionDuration();
        Vector3f newPosition = new Vector3f(camState.getOriginalPosition());
        newPosition.interpolate(nextPosition, s);
        camState.setCameraPosition(newPosition, true); // lookAt
    }

    /**
     * This method lerps between the camera's initial focus point and the provided
     * next point.
     * @param camState
     * @param nextPosition
     */
    private void updateFocusTransition(TumbleObjectCamState camState, Vector3f nextPosition)
    {
        float s = camState.getTimeInFocusTransition() / camState.getTransitionDuration();
        Vector3f newFocus = new Vector3f(camState.getOriginalFocalPoint());
        newFocus.interpolate(nextPosition, s);
        camState.setTargetFocalPoint(newFocus);
    }

    /**
     * {@inheritDoc CameraModel}
     */
    public boolean isStateClassValid(Class<? extends AbstractCameraState> classz) {
        return (classz == TumbleObjectCamState.class);
    }
}
