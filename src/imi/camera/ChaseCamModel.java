/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * This class performs a dual spring chase-cam style behavior. The various spring
 * parameters may be manipulated via an instance of {@code ChaseCamState}. This
 * class is <b>NOT THREAD SAFE FOR MULTIPLE</b> camera states.
 * @author Lou Hayt
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public final class ChaseCamModel extends CameraModel
{
    private final Vector3f desiredCameraPosition = new Vector3f();
    private final Vector3f lookAtPosition = new Vector3f();
    private final Vector3f desiredCameraPositionZoomModifier = new Vector3f();

    private final Vector3f velocity = new Vector3f();
    private final Vector3f force = new Vector3f();
    private final Vector3f lookAtHelper = new Vector3f();
    private final Vector3f lookAtHelperTwo = new Vector3f();
    private final Vector3f lookAtVelocity = new Vector3f();
    private final Quaternion q = new Quaternion();
    private final PMatrix rotationHelper = new PMatrix();

    int zoom = 0;
    int maxZoom = 10;

    float mouseDeltaXModifier = 0.4f;
    float mouseDeltaYModifier = 0.01f;

    // Ensure proper accesibility
    ChaseCamModel() {}

    private void updateCameraProperties(ChaseCamState state)
    {
        state.getTransformedAndPositionedDesiredPositionOffset(desiredCameraPosition);
        state.getTransformedAndPositionedLookAtOffset(lookAtPosition);
        // Zoom
        if (state.isZoomEnabled())
        {
            desiredCameraPositionZoomModifier.set(lookAtPosition.subtract(desiredCameraPosition));
            desiredCameraPositionZoomModifier.normalizeLocal();
            desiredCameraPositionZoomModifier.multLocal(0.75f * zoom);
            desiredCameraPosition.addLocal(desiredCameraPositionZoomModifier);
        }
    }

    /**
     * reset the provided state so that it's inertia is cancelled.
     * @param state The state to operate on
     */
    public void reset(AbstractCameraState state)
    {
        ChaseCamState activeState = (ChaseCamState)state;
        // Calculate desired camera properties in world space
        updateCameraProperties(activeState);
        // Stop motion
        velocity.set(0.0f, 0.0f, 0.0f);
        lookAtVelocity.set(0.0f, 0.0f, 0.0f);
        // Force desired position
        activeState.setCameraPosition(desiredCameraPosition);
        lookAt(activeState);
    }

    /**
     * {@inheritDoc CameraModel}
     */
    public void handleInputEvents(AbstractCameraState state, Object[] events)
    {
        // parse the events
        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                ChaseCamState camState = (ChaseCamState)state;
                if (me.getID() == MouseEvent.MOUSE_WHEEL)
                {
                    MouseWheelEvent mwe = (MouseWheelEvent)me;

                    // Negative, zoom in; Positive, zoom out
                    int clicks = mwe.getWheelRotation() * -1;
                    zoom += clicks;
                    zoom = (zoom > maxZoom) ? maxZoom : (zoom < -maxZoom) ? -maxZoom : zoom;
                } else if (me.getID() == MouseEvent.MOUSE_DRAGGED) {
                    // Deform offset vector
                    int deltaX = camState.lastMouseX - me.getX();
                    camState.lastMouseX = me.getX();
                    int deltaY = camState.lastMouseY - me.getY();
                    camState.lastMouseY = me.getY();

                    // COnvert mouse Y-axis motion into y-translation of the
                    // focal point
                    Vector3f lookAtOffset = camState.getLookAtOffsetRef();
                    lookAtOffset.y += deltaY * mouseDeltaYModifier * camState.getyModifier();

                    // Convert mouse X-axis motion into rotation of the
                    // focal point
                    Vector3f offsetVec = camState.getDesiredPositionOffsetRef();
                    rotationHelper.set(new Vector3f(0, (float)Math.toRadians(deltaX * mouseDeltaXModifier * camState.getxModifier()),0), Vector3f.ZERO, Vector3f.UNIT_XYZ);
                    rotationHelper.transformNormal(offsetVec);
                } else if (me.getID() == MouseEvent.MOUSE_PRESSED) {
                    camState.lastMouseX = me.getX();
                    camState.lastMouseY = me.getY();
                    camState.setLookAtSpringEnabled(false);
                } else if (me.getID() == MouseEvent.MOUSE_RELEASED) {
                    camState.setLookAtSpringEnabled(true);
                }
            }
        }
    }

    /**
     * {@inheritDoc CameraModel}
     */
    public void update(AbstractCameraState state, float deltaTime)
    {
        ChaseCamState activeState = (ChaseCamState)state;

        if (activeState.isHardAttached())
        {
            reset(state);
            return;
        }

        // Update timed effects
        activeState.update(deltaTime);

        // Calculate desired camera properties in world space
        lookAtHelper.set(lookAtPosition);
        updateCameraProperties(activeState);

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
            lookAtHelper.multLocal(-activeState.getLookAtStiffness());
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

        lookAt(activeState);
    }

    
    private void lookAt(ChaseCamState activeState)
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
        PMatrix m = activeState.getCameraTransform();
        m.setRotation(q);
        m.setTranslation(lookAtHelper);
    }

    /**
     * {@inheritDoc CameraModel}
     */
    @Override
    public void determineTransform(AbstractCameraState state, PMatrix transform)
    {
        ChaseCamState activeState = (ChaseCamState)state;
        transform.set2(q, activeState.getCameraTransform().getTranslation(), 1.0f);
    }

    public boolean isStateClassValid(Class<? extends AbstractCameraState> classz) {
        return (classz == ChaseCamState.class);
    }

    private void dumpStateParams(ChaseCamState state) {
        System.out.println("Dumping state parameters for " + state);
        // Target
        System.out.println("Transform: " + state.getCameraTransform());
        // Chase pos
        System.out.println("Chase position: " + state.getChasePosition());
        // Desired offsets
        System.out.println("Pos offset: " + state.getDesiredPositionOffsetRef() + ", LookAt offset: " + state.getLookAtOffsetRef());
    }
    
    /**
     * added for reset camera in view menu
     * @param zoom 
     */
    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

}
