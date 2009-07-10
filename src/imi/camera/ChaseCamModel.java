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

    int zoom = 0;
    int maxZoom = 10;

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
                if (me.getID() == MouseEvent.MOUSE_WHEEL)
                {
                    MouseWheelEvent mwe = (MouseWheelEvent)me;

                    // Negative, zoom in; Positive, zoom out
                    int clicks = mwe.getWheelRotation() * -1;
                    zoom += clicks;
                    zoom = (zoom > maxZoom) ? maxZoom : (zoom < -maxZoom) ? -maxZoom : zoom;
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
        System.out.println("Force (campos): " + force);
        force.subtractLocal(desiredCameraPosition);
        force.multLocal(-activeState.getStiffness());
        force.subtractLocal(velocity.mult(activeState.getDamping()));
        // Apply acceleration
        force.divideLocal(activeState.getMass());
        velocity.addLocal(force.mult(deltaTime));
        // Apply velocity
//        System.out.println("Pos pre: " + activeState.getCameraTransform().getTranslation());
//        System.out.println("Add: " + velocity.mult(deltaTime));
//        System.out.println("DeltaTime: " + deltaTime);
//        System.out.println("Velocity: " + velocity);
//        System.out.println("Force: " + force);
        activeState.addToCameraPosition(velocity.mult(deltaTime));
        //System.out.println("Pos post: " + activeState.getCameraTransform().getTranslation());

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
    void determineTransform(AbstractCameraState state, PMatrix transform)
    {
        ChaseCamState activeState = (ChaseCamState)state;
        transform.set2(q, activeState.getCameraTransform().getTranslation(), 1.0f);
    }
    
    public boolean isStateClassValid(Class<? extends AbstractCameraState> classz) {
        return (classz == ChaseCamState.class);
    }

}
