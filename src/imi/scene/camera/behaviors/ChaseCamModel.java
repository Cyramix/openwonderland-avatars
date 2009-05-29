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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.CharacterMotionListener;
import imi.scene.PMatrix;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.CameraState.CameraStateType;
import imi.scene.camera.state.ChaseCamState;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 *
 * @author Lou Hayt
 */
public class ChaseCamModel implements CameraModel, CharacterMotionListener
{
    ChaseCamState activeState = null;

    Vector3f desiredCameraPosition = new Vector3f();
    Vector3f lookAtPosition = new Vector3f();
    Vector3f desiredCameraPositionZoomModifier = new Vector3f();

    Vector3f velocity = new Vector3f();
    Vector3f force = new Vector3f();
    Vector3f lookAtHelper = new Vector3f();
    Vector3f lookAtHelperTwo = new Vector3f();
    Vector3f lookAtVelocity = new Vector3f();

    int zoom = 0;
    int maxZoom = 10;

    public ChaseCamModel() {
    }

    private void updateCameraProperties()
    {
        activeState.getTransformedAndPositionedDesiredPositionOffset(desiredCameraPosition);
        activeState.getTransformedAndPositionedLookAtOffset(lookAtPosition);
        // Zoom
        if (activeState.isZoomEnabled())
        {
            desiredCameraPositionZoomModifier.set(lookAtPosition.subtract(desiredCameraPosition));
            desiredCameraPositionZoomModifier.normalizeLocal();
            desiredCameraPositionZoomModifier.multLocal(0.75f * zoom);
            desiredCameraPosition.addLocal(desiredCameraPositionZoomModifier);
        }
    }

    public void Reset(CameraState state) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.Chase)
            throw new WrongStateTypeException("Wrong state type");
        activeState = (ChaseCamState)state;
        // Calculate desired camera properties in world space
        updateCameraProperties();
        // Stop motion
        velocity.set(0.0f, 0.0f, 0.0f);
        lookAtVelocity.set(0.0f, 0.0f, 0.0f);
        // Force desired position
        activeState.setCameraPosition(desiredCameraPosition);
        lookAt();
    }

    public void handleInputEvents(CameraState state, Object[] events) throws WrongStateTypeException 
    {
        if (state.getType() != CameraState.CameraStateType.Chase)
            throw new WrongStateTypeException("Wrong state type");
        activeState = (ChaseCamState)state;

        // parse the events
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_WHEEL)
                {
                    MouseWheelEvent mwe = (MouseWheelEvent)me;

                    // Negative, zoom in; Positive, zoom out
                    int clicks = mwe.getWheelRotation() * -1;
                    zoom += clicks;
                    if (zoom > maxZoom)
                        zoom = maxZoom;
                    if (zoom < -maxZoom)
                        zoom = -maxZoom;
                    System.out.println(zoom);
                }
            }
        }
    }

    public void update(CameraState state, float deltaTime) throws WrongStateTypeException 
    {
        if (state.getType() != CameraState.CameraStateType.Chase)
            throw new WrongStateTypeException("Wrong state type");
        activeState = (ChaseCamState)state;

        if (activeState.getHardAttachMode())
        {
            Reset(state);
            return;
        }

        // Update timed effects
        activeState.update(deltaTime);

        // Calculate desired camera properties in world space
        lookAtHelper.set(lookAtPosition);
        updateCameraProperties();

        // Calculate spring force
        activeState.getCameraPosition(force);
        force.subtractLocal(desiredCameraPosition);
        force.multLocal(-activeState.getStiffness());
        force.subtractLocal(velocity.mult(activeState.getDamping()));
        // Apply acceleration
        force.divideLocal(activeState.getMass());
        velocity.addLocal(force.mult(deltaTime));
        // Apply velocity
        activeState.addToCameraPosition(velocity.mult(deltaTime));

        if (activeState.isLookAtSpringEnabled())
        {
            // Smooth look at position ( a secound spring )
            lookAtHelperTwo.set(lookAtHelper);
            // Calculate spring force
            lookAtHelper.subtractLocal(lookAtPosition);
            lookAtHelper.multLocal(-activeState.getLookAtStiffnes());
            lookAtHelper.subtractLocal(lookAtVelocity.mult(activeState.getLookAtDamping()));
            // Apply acceleration
            lookAtHelper.divideLocal(activeState.getLookAtMass());
            lookAtVelocity.addLocal(lookAtHelper.mult(deltaTime));
            // Apply velocity
            lookAtPosition.set(lookAtHelperTwo.add(lookAtVelocity.mult(deltaTime)));
        }
        else // Linear
        {
//            lookAtHelper   = current
//            lookAtPosition = desired
//            System.out.println("current " + lookAtHelper);
//            System.out.println("desired " + lookAtPosition);
//            System.out.println(activeState.getLookAtLinearTimeCounter() + "  /  " + activeState.getLookAtLinearTimeLength());
            float s = activeState.getLookAtLinearTimeCounter() / activeState.getLookAtLinearTimeLength();
            lookAtHelper.interpolate(lookAtPosition, s);
            lookAtPosition.set(lookAtHelper);
            //System.out.println("look at is set: " + lookAtPosition + " at " + s);
        }

        lookAt();
    }

    Quaternion q = new Quaternion();
    private void lookAt()
    {
//        activeState.getCameraPosition(lookAtHelper);
//        Vector3f forward = lookAtPosition.subtract(lookAtHelper).normalize();
//        Vector3f right   = Vector3f.UNIT_Y.cross(forward).normalize();
//        Vector3f realUp  =  forward.cross(right).normalize();
//        // load it up manually
//        float[] floats = new float[16];
//        floats[ 0] = right.x;  floats[ 1] = realUp.x;  floats[ 2] = forward.x;  floats[ 3] = lookAtHelper.x;
//        floats[ 4] = right.y;  floats[ 5] = realUp.y;  floats[ 6] = forward.y;  floats[ 7] = lookAtHelper.y;
//        floats[ 8] = right.z;  floats[ 9] = realUp.z;  floats[10] = forward.z;  floats[11] = lookAtHelper.z;
//        floats[12] = 0.0f;     floats[13] = 0.0f;      floats[14] = 0.0f;       floats[15] = 1.0f;
//
//        activeState.setCameraTransform(floats);

        activeState.getCameraPosition(lookAtHelper);
        Vector3f forward = lookAtPosition.subtract(lookAtHelper).normalize();
        q.lookAt(forward, Vector3f.UNIT_Y);
        PMatrix m = new PMatrix();
        m.setRotation(q);
        m.setTranslation(lookAtHelper);
        activeState.setCameraTransform(m);
    }

    public void getRotation(Quaternion rot)
    {
        rot.set(q);
    }

    public void determineTransform(CameraState state, PMatrix transform) throws WrongStateTypeException 
    {
        if (state.getType() != CameraState.CameraStateType.Chase)
            throw new WrongStateTypeException("Wrong state type");
        activeState = (ChaseCamState)state;
        transform.set(activeState.getCameraTransform());
    }

    public CameraStateType getRequiredStateType() {
        return CameraState.CameraStateType.Chase;
    }

    boolean firstTransformUpdate = true;
    public void transformUpdate(Vector3f translation, PMatrix rotation) 
    {
        if (activeState == null)
            return;
        
        activeState.setChasePosition(translation);
        activeState.setChaseOrientation(rotation);

//        // Safety check
//        if (/*activeState.getCameraTransform().getTranslation().distanceSquared(translation) > 2500.0f ||*/ firstTransformUpdate)
//        {
//            firstTransformUpdate = false;
//            Reset(activeState);
//        }
    }

}
