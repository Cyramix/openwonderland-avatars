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

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.TumbleObjectCamState;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * This model performs object tumbling. The camera is locked at a certain perspective
 * and generally not manipulated. The primary intent is to inspect objects.
 * @author Ronald E Dahlgren
 */
public class TumbleObjectCamModel implements CameraModel
{

    public void determineTransform(CameraState state, PMatrix transform) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.TumbleObject)
            throw new WrongStateTypeException("Was not TumbleObject type! Type was " + state.getType().toString() + "!");
        
        TumbleObjectCamState camState = (TumbleObjectCamState)state;
        transform.set(camState.getCameraTransform());
    }

    public void handleInputEvents(CameraState state, Object[] events) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.TumbleObject)
            throw new WrongStateTypeException("Was not TumbleObject type! Type was " + state.getType().toString() + "!");
        
        boolean updateRotations = false;
        
        TumbleObjectCamState camState = (TumbleObjectCamState)state;
        
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_PRESSED)
                {
                    // Mouse pressed, reset initial settings
                    camState.setCurrentMouseX(me.getX());
                    camState.setCurrentMouseY(me.getY());
                    camState.setLastMouseX(me.getX());
                    camState.setLastMouseY(me.getY());
                }
                
                if (me.getID() == MouseEvent.MOUSE_DRAGGED) {
                    processRotations(me, camState);
                    camState.setDirty(true);
                    updateRotations = true;
                }
            } else if (events[i] instanceof KeyEvent)
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke, camState);
                camState.setDirty(true);
            }
        }
        
        updateTargetTransform(camState, updateRotations);
        updateCameraTransform(camState);
    }

    private void processRotations(MouseEvent me, TumbleObjectCamState camState)
    {
        int deltaX = 0;
        //int deltaY = 0;
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
            //deltaY = localcurrentY - camState.getLastMouseY();
            deltaX = -deltaX;
            // The assignment looks reversed, this is because dragging the mouse
            // along the X axis produces in-game rotation about the Y-axis
            camState.setRotationY(camState.getRotationY() + (deltaX * camState.getScaleRotationX()));
            //state.setRotationX(state.getRotationX() + (deltaY * state.getScaleRotationY()));
            
//            if (camState.getRotationX() > 60.0f) {
//                camState.setRotationX(60.0f);
//            } else if (camState.getRotationX() < -60.0f) {
//                camState.setRotationX( -60.0f);
//            }
            
            camState.setLastMouseX(localcurrentX);
            camState.setLastMouseY(localcurrentY);
        }
    }
    
    private void processKeyEvent(KeyEvent ke, TumbleObjectCamState camState)
    {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_ADD) {
                // Start zooming in
                camState.setMovementState(TumbleObjectCamState.ZOOMING_IN);
            }
            if (ke.getKeyCode() == KeyEvent.VK_SUBTRACT) {
                // Start zooming out
                camState.setMovementState(TumbleObjectCamState.ZOOMING_OUT);
            }

        }
        
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_ADD ||
                ke.getKeyCode() == KeyEvent.VK_SUBTRACT)
            {
                // Stop all movement
                camState.setMovementState(TumbleObjectCamState.STOPPED);
            }
        }
    }
    
    private void updateRotations(TumbleObjectCamState camState, PMatrix targetTransform)
    {
        Vector3f scales = targetTransform.getScaleVector();
        Vector3f translation = targetTransform.getTranslation();
        // Reorient rotation
        targetTransform.set(new Vector3f(0,(float) Math.toRadians(camState.getRotationY()), 0), translation, scales);
    }
    
    private void updateCameraTransform(TumbleObjectCamState camState)
    {
        PMatrix camXForm = camState.getCameraTransform();
        Vector3f targetPosition = camState.getTargetFocalPoint();
        
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

        Vector3f position = camXForm.getTranslation();
        Vector3f toTarget = (targetPosition.subtract(position));
        float distSquared = toTarget.lengthSquared();
        toTarget.normalizeLocal();
        // move depending on whether we are zooming or not
        int state = camState.getMovementState();
        switch (state)
        {
            case TumbleObjectCamState.ZOOMING_IN:
                if (distSquared > 2.0f) // Go ahead and zoom
                    position.addLocal(toTarget.mult(camState.getMovementRate()));
                break;
            case TumbleObjectCamState.ZOOMING_OUT:
                position.subtractLocal(toTarget.mult(camState.getMovementRate()));
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
    
    private void updateTargetTransform(TumbleObjectCamState camState, boolean bUpdateRotations)
    {
       PMatrix transform = camState.getTargetModelInstance().getTransform().getLocalMatrix(true);
       // update the transform with rotational info if necessary
       if (bUpdateRotations)
           updateRotations(camState, transform);
       
       camState.getTargetModelInstance().setDirty(true, true);
    }
}
