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
package imi.scene.camera.behaviors;

import com.jme.math.Vector3f;
import imi.character.CharacterMotionListener;
import imi.scene.PMatrix;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.ThirdPersonCamState;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.SwingUtilities;

/**
 * This model performs object tumbling. The camera is locked at a certain perspective
 * and generally not manipulated. The primary intent is to inspect objects. This model
 * also provides the capability to smoothly move to a new position while watching the
 * focal point, as well as smoothly changing focal points.
 * @author Ronald E Dahlgren
 */
public class ThirdPersonCamModel implements CameraModel, CharacterMotionListener
{
    /** The state that is actively tracking the avatar's motion **/
    private ThirdPersonCamState activeState = null;
    /** Rotation matrix that is applied while turning, per frame **/
    private final PMatrix perFrameRotationMatrix = new PMatrix(
            new Vector3f(0, (float)Math.toRadians(0.01), 0),
            Vector3f.UNIT_XYZ,
            Vector3f.ZERO);
    /** Workspace **/
    private final Vector3f vectorBuffer    = new Vector3f();
    private final Vector3f vectorBufferTwo = new Vector3f();
    private final PMatrix  matrixBuffer    = new PMatrix();

    /**
     * Implementation of CameraModel interface. This method determines the
     * transform of the camera based on the configuration of the provided state.
     * @param state
     * @param transform
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void determineTransform(CameraState state, PMatrix transform) throws WrongStateTypeException
    {
        // Type checking
        if (state.getType() != CameraState.CameraStateType.ThirdPerson)
        {
            throw new WrongStateTypeException("Was not TumbleObject type! " +
                    "Type was " + state.getType().toString() + "!");
        }
        // get a reference to the derived type
        ThirdPersonCamState camState = (ThirdPersonCamState)state;
        // Apply to the camera
        transform.set(camState.getCameraTransform());
    }

    /**
     * Implementation of CameraModel interface. This method handles the events
     * provided and alters the provided state accordingly.
     * @param state
     * @param events
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void handleInputEvents(CameraState state, Object[] events) throws WrongStateTypeException
    {
        // Check type
        if (state.getType() != CameraState.CameraStateType.ThirdPerson)
            throw new WrongStateTypeException("Was not TumbleObject type! Type was " + state.getType().toString() + "!");
        // Used as a minor optimization if it is unnecessary.
        boolean updateRotations = false;
        // Cast to our derived state type
        ThirdPersonCamState camState = (ThirdPersonCamState)state;
        // parse the events
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                boolean result = me.getID() == MouseEvent.MOUSE_PRESSED;
                if (state.isRightMouseButtonOnly())
                    result = result && SwingUtilities.isRightMouseButton(me);
                if ( result )
                {
                    // Mouse pressed, reset initial settings
                    camState.setCurrentMouseX(me.getX());
                    camState.setCurrentMouseY(me.getY());
                    camState.setLastMouseX(me.getX());
                    camState.setLastMouseY(me.getY());
                }

                result = me.getID() == MouseEvent.MOUSE_DRAGGED;
                if (state.isRightMouseButtonOnly())
                    result = result && SwingUtilities.isRightMouseButton(me);
                if ( result )
                {
                    // Mouse dragged, handle model rotation
                    processRotations(me, camState);
                    updateRotations = true;
                }

//                if (me.getID() == MouseEvent.MOUSE_WHEEL) // Wheel action, do some zooming business
//                    processMouseWheel((MouseWheelEvent) me,camState);
            }
            else if (events[i] instanceof KeyEvent) // Process key presses
            {
                // We handle zooming through the key presses currently
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke, camState);
            }
        }
        // Manipulate the state's target
        updateOurTransform(camState, updateRotations);
        // Synchronize the camera's transform with the rest of the state.
        updateCameraTransform(camState);
    }

    /**
     * Process any mouse wheel events that are received from AWT
     * @param mwe
     * @param camState
     */
    private void processMouseWheel(MouseWheelEvent mwe, ThirdPersonCamState camState)
    {
        if (camState.getNextPosition() != null) // No zooming while animating
            return;
        // Negative, zoom in; Positive, zoom out
        int clicks = mwe.getWheelRotation() * -1;

        Vector3f zoomVec = new Vector3f(camState.getCameraTransform().getLocalZNormalized());
        zoomVec.multLocal(clicks * 0.05f);
        // Make sure we aren't zooming in too far--
        // Generate a vector from the camera's future position to the focal point and check the length
        Vector3f futurePosition = camState.getCameraPosition().add(zoomVec);
        Vector3f futurePosToTarget = camState.getTargetFocalPoint().subtract(futurePosition);
        // Dist squared is used to save a square root operation
        float futureToTargetLengthSquared = futurePosToTarget.lengthSquared();
        if (futureToTargetLengthSquared >= camState.getMinimumDistanceSquared() &&
                futureToTargetLengthSquared <= camState.getMaximumDistanceSquared())
        {
            // No turn-to needed, we are moving along the correct vector
            camState.setCameraPosition(camState.getCameraPosition().add(zoomVec), false);
            camState.setTargetNeedsUpdate(true);
        }
    }

    /**
     * Process any mouse dragging as a rotation input to the state's target
     * @param me
     * @param camState
     */
    private void processRotations(MouseEvent me, ThirdPersonCamState camState)
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
    private void processKeyEvent(KeyEvent ke, ThirdPersonCamState camState)
    {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_UP)
                camState.setMovementState(ThirdPersonCamState.ZOOMING_IN);
            if (ke.getKeyCode() == KeyEvent.VK_DOWN)
                camState.setMovementState(ThirdPersonCamState.ZOOMING_OUT);
            if (ke.getKeyCode() == KeyEvent.VK_LEFT)
                camState.setMovementState(ThirdPersonCamState.MOVING_LEFT);
            if (ke.getKeyCode() == KeyEvent.VK_RIGHT)
                camState.setMovementState(ThirdPersonCamState.MOVING_RIGHT);

        }

        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_UP ||
                ke.getKeyCode() == KeyEvent.VK_DOWN ||
                ke.getKeyCode() == KeyEvent.VK_LEFT ||
                ke.getKeyCode() == KeyEvent.VK_RIGHT
                )
            {
                // Stop all movement
                camState.setMovementState(ThirdPersonCamState.STOPPED);
            }
        }
    }

    /**
     * Manipulate the matrix to match the representation in the provided state
     * @param camState
     * @param targetTransform
     */
    private void updateRotations(ThirdPersonCamState camState, PMatrix targetTransform)
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
    private void updateCameraTransform(ThirdPersonCamState camState)
    {
        // Cache necessary data
        PMatrix camXForm = camState.getCameraTransform();
        Vector3f targetPosition = camState.getTargetFocalPoint();

        if (camState.getMovementState() == ThirdPersonCamState.MOVING_LEFT)
        {
            camXForm.getTranslation(vectorBuffer);
            camState.getTargetFocalPoint(vectorBufferTwo);
            if (vectorBuffer.subtractLocal(vectorBufferTwo).lengthSquared() < camState.getMaximumDistanceSquared())
            {
                camXForm.getTranslation(vectorBuffer);
                camXForm.getLocalX(vectorBufferTwo);
                vectorBufferTwo.multLocal(camState.getMovementRate());
                vectorBuffer.addLocal(vectorBufferTwo);
                camXForm.setTranslation(vectorBuffer);
                camState.setTargetNeedsUpdate(true);
            }
        }
        else if (camState.getMovementState() == ThirdPersonCamState.MOVING_RIGHT)
        {
            camXForm.getTranslation(vectorBuffer);
            camState.getTargetFocalPoint(vectorBufferTwo);
            if (vectorBuffer.subtractLocal(vectorBufferTwo).lengthSquared() < camState.getMaximumDistanceSquared())
            {
                camXForm.getTranslation(vectorBuffer);
                camXForm.getLocalX(vectorBufferTwo);
                vectorBufferTwo.multLocal(-camState.getMovementRate());
                vectorBuffer.addLocal(vectorBufferTwo);
                camXForm.setTranslation(vectorBuffer);
                camState.setTargetNeedsUpdate(true);
            }
        }

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
        int state = camState.getMovementState();
        switch (state)
        {
            case ThirdPersonCamState.ZOOMING_IN:
                if (distSquared > camState.getMinimumDistanceSquared()) // Go ahead and zoom
                    position.addLocal(toTarget.mult(camState.getMovementRate())); // TODO : Make time based ( value * deltaTime )
                break;
            case ThirdPersonCamState.ZOOMING_OUT: // No long range clamping
                if (distSquared < camState.getMaximumDistanceSquared())
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
    private void updateOurTransform(ThirdPersonCamState camState, boolean bUpdateRotations)
    {
       if (camState.getTargetModelInstance() == null) // Model to rotate yet
           return;
    }

    /**
     * Process the update
     * @param state
     * @param deltaTime
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void update(CameraState state, float deltaTime) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.ThirdPerson)
            throw new WrongStateTypeException("Wrong state type");
        // Get derived state type
        ThirdPersonCamState camState = (ThirdPersonCamState) state;

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
    private void updatePositionTransition(ThirdPersonCamState camState, Vector3f nextPosition)
    {
        float s = camState.getTimeInPositionTransition() / camState.getTransitionDuration();
        synchronized (vectorBuffer)
        {
            camState.getOriginalPosition(vectorBuffer);
            vectorBuffer.interpolate(nextPosition, s);
            camState.setCameraPosition(vectorBuffer, true); // lookAt
        }
    }

    /**
     * This method lerps between the camera's initial focus point and the provided
     * next point.
     * @param camState
     * @param nextPosition
     */
    private void updateFocusTransition(ThirdPersonCamState camState, Vector3f nextPosition)
    {
        float s = camState.getTimeInFocusTransition() / camState.getTransitionDuration();
        synchronized (vectorBuffer)
        {
            camState.getOriginalFocalPoint(vectorBuffer);
            vectorBuffer.interpolate(nextPosition, s);
            camState.setTargetFocalPoint(vectorBuffer);
        }
    }

    /**
     * This method allows for smoothly transitioning to a new location, but it
     * will interrupt any pending transitions
     * @param newPosition
     */
    public void moveTo(Vector3f newPosition, ThirdPersonCamState camState)
    {
        // out with the old
        camState.setNextPosition(null);
        camState.setOriginalPosition(null);
        camState.setTimeInPositionTransition(0.0f);
        // in with the new
        camState.setNextPosition(newPosition);
        camState.setOriginalPosition(camState.getCameraPosition());
    }

    /**
     * This method allows for smooth transitioning to a new focus point
     * @param newFocusPoint
     * @param camState
     */
    public void turnTo(Vector3f newFocusPoint, ThirdPersonCamState camState)
    {
        // out with the old
        camState.setNextFocalPoint(null);
        camState.setOriginalFocalPoint(null);
        camState.setTimeInFocusTransition(0.0f);
        // in with the new
        camState.setNextFocalPoint(newFocusPoint);
        camState.setOriginalFocalPoint(camState.getTargetFocalPoint());
    }

    // CharacterMotionListener implementation
    public void transformUpdate(Vector3f translation, PMatrix rotation)
    {
        final float threshold = 16.0f; // Distance before camera position updates
        if (activeState != null) // Anybody care?
        {
            // Grab stuff before the synchronization block
            PMatrix camTransform = activeState.getCameraTransform();
            synchronized (vectorBuffer)
            {
                vectorBuffer.set(translation);
                activeState.getOffsetFromCharacter(vectorBuffer);
                vectorBuffer.addLocal(translation);
                activeState.setTargetFocalPoint(vectorBuffer);

                camTransform.getTranslation(vectorBuffer);
                activeState.getTargetFocalPoint(vectorBufferTwo);
                vectorBuffer.subtractLocal(vectorBufferTwo);

                float lengthSquared = vectorBuffer.lengthSquared();
                if (activeState.getNextPosition() == null &&
                   (lengthSquared > threshold || Math.abs(vectorBuffer.normalize().dot(Vector3f.UNIT_Y)) > 0.7))
                {
                    activeState.getToCamera(vectorBufferTwo);
                    rotation.transformNormal(vectorBufferTwo);
                    vectorBufferTwo.addLocal(translation);
                    moveTo(vectorBufferTwo, activeState);
                }
            }
            activeState.setTargetNeedsUpdate(true);
            updateCameraTransform(activeState);
        }
    }


    public ThirdPersonCamState getActiveState()
    {
        return activeState;
    }

    public void setActiveState(ThirdPersonCamState activeState)
    {
        this.activeState = activeState;
    }


    @Override
    public CameraState.CameraStateType getRequiredStateType() {
        return CameraState.CameraStateType.ThirdPerson;
    }

}
