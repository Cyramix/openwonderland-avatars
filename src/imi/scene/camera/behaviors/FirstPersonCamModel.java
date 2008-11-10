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

import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 *
 * @author Ronald E Dahlgren
 */
public class FirstPersonCamModel implements CameraModel
{

    public void determineTransform(CameraState state, PMatrix transform) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.FirstPerson)
            throw new WrongStateTypeException("Was not first person type! Type was " + state.getType().toString() + "!");
       FirstPersonCamState camState = (FirstPersonCamState)state;
        transform.set2(camState.getQuaternion(), camState.getPosition(), 1.0f);
    }

    public void handleInputEvents(CameraState state, Object[] events) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.FirstPerson)
            throw new WrongStateTypeException("Was not first person type! Type was " + state.getType().toString() + "!");
        
        boolean updateRotations = false;
        
        FirstPersonCamState camState = (FirstPersonCamState)state;
        
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
                    updateRotations = true;
                }
            } else if (events[i] instanceof KeyEvent)
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke, camState);
            }
        }
        
        if (updateRotations)
            updateRotations(camState);
        updatePosition(camState);
    }
    
    private void processRotations(MouseEvent me, FirstPersonCamState state)
    {
        int deltaX = 0;
        int deltaY = 0;
        int localcurrentX = 0;
        int localcurrentY = 0;
        localcurrentX = me.getX();
        localcurrentY = me.getY();

        if (state.getLastMouseX() == -1) {
            // First time through, just initialize
            state.setLastMouseX(localcurrentX);
            state.setLastMouseY(localcurrentY);
        } else {
            deltaX = localcurrentX - state.getLastMouseX();
            deltaY = localcurrentY - state.getLastMouseY();
            deltaX = -deltaX;
            // The assignment looks reversed, this is because dragging the mouse
            // along the X axis produces in-game rotation about the Y-axis
            state.setRotationY(state.getRotationY() + (deltaX * state.getScaleRotationX()));
            state.setRotationX(state.getRotationX() + (deltaY * state.getScaleRotationY()));
            
            if (state.getRotationX() > 60.0f) {
                state.setRotationX(60.0f);
            } else if (state.getRotationX() < -60.0f) {
                state.setRotationX( -60.0f);
            }
            
            state.setLastMouseX(localcurrentX);
            state.setLastMouseY(localcurrentY);
        }
    }
    
   private void processKeyEvent(KeyEvent ke, FirstPersonCamState state) {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_W) {
                state.setMovementState(FirstPersonCamState.WALKING_FORWARD);
            }
            if (ke.getKeyCode() == KeyEvent.VK_S) {
                state.setMovementState(FirstPersonCamState.WALKING_BACK);
            }
            if (ke.getKeyCode() == KeyEvent.VK_A) {
                state.setMovementState(FirstPersonCamState.STRAFE_LEFT);
            }
            if (ke.getKeyCode() == KeyEvent.VK_D) {
                state.setMovementState(FirstPersonCamState.STRAFE_RIGHT);
            }
            if (ke.getKeyCode() == KeyEvent.VK_Q) {
                state.setMovementState(FirstPersonCamState.MOVE_UP);
            }
            if (ke.getKeyCode() == KeyEvent.VK_Z) {
                state.setMovementState(FirstPersonCamState.MOVE_DOWN);
            }
        }
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_W ||
                ke.getKeyCode() == KeyEvent.VK_S ||
                ke.getKeyCode() == KeyEvent.VK_A ||
                ke.getKeyCode() == KeyEvent.VK_D ||
                ke.getKeyCode() == KeyEvent.VK_Q ||
                ke.getKeyCode() == KeyEvent.VK_Z) {
                state.setMovementState(FirstPersonCamState.STOPPED);
            }
        }
    }

    private void updatePosition(FirstPersonCamState camState)
    {
        Vector3f position = camState.getPosition();
        float walkInc = camState.getMovementRate();
        Vector3f rotatedFwdDirection = camState.getRotatedFwdDirection();
        Vector3f rotatedSideDirection = camState.getRotatedSideDirection();
        
        switch (camState.getMovementState()) 
        {
        case FirstPersonCamState.WALKING_FORWARD:
            position.x += (walkInc * rotatedFwdDirection.x);
            position.y += (walkInc * rotatedFwdDirection.y);
            position.z += (walkInc * rotatedFwdDirection.z);
            break;
        case FirstPersonCamState.WALKING_BACK:
            position.x -= (walkInc * rotatedFwdDirection.x);
            position.y -= (walkInc * rotatedFwdDirection.y);
            position.z -= (walkInc * rotatedFwdDirection.z);
            break;
        case FirstPersonCamState.STRAFE_LEFT:
            position.x += (walkInc * rotatedSideDirection.x);
            position.y += (walkInc * rotatedSideDirection.y);
            position.z += (walkInc * rotatedSideDirection.z);
            break;
        case FirstPersonCamState.STRAFE_RIGHT:
            position.x -= (walkInc * rotatedSideDirection.x);
            position.y -= (walkInc * rotatedSideDirection.y);
            position.z -= (walkInc * rotatedSideDirection.z);
            break;
        case FirstPersonCamState.MOVE_UP:
            position.y += (walkInc);
            break;
        case FirstPersonCamState.MOVE_DOWN:
            position.y -= (walkInc);
            break;
        }
    }

    private void updateRotations(FirstPersonCamState camState)
    {
        Matrix3f rotMatrix = camState.getDirectionRotation();
        rotMatrix.fromAngleAxis(camState.getRotationY() * (float)Math.PI/180.0f, camState.getYDir());
        rotMatrix.mult(camState.getFwdDirection(), camState.getRotatedFwdDirection());
        rotMatrix.mult(camState.getSideDirection(), camState.getRotatedSideDirection());
        //System.out.println("Forward: " + rotatedFwdDirection);
        Quaternion quat = camState.getQuaternion();
        quat.fromAngles(camState.getRotationX() * (float)Math.PI/180.0f, camState.getRotationY() * (float)Math.PI/180.0f, 0.0f);
    }

}
