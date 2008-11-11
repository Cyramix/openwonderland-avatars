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
import imi.scene.camera.state.AzimuthAngleState;
import imi.scene.camera.state.CameraState;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * This camera model provides an azimuth and angle model to determine a 
 * camera's position
 * @author Ronald E Dahlgren
 */
public class AzimuthAngleModel implements CameraModel 
{
    /** Input handling helpers **/
    private int m_currentX = -1;
    private int m_currentY = -1;
    private int m_lastMouseX = -1;
    private int m_lastMouseY = -1;
    /**
     * This model uses the state's azimuth, elevation, target, and sphere radius
     * to determine where the camera should be located.
     * @param state
     * @param transform
     * @throws imi.scene.camera.behaviors.WrongStateTypeException
     */
    public void determineTransform(CameraState state, PMatrix transform) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.AzimuthAngle)
            throw new WrongStateTypeException("Expected AzimuthAngleState, got " + state.getClass());
        AzimuthAngleState azState = (AzimuthAngleState)state;
        /**
         * x = r * sin(elevation) * cos(azimuth)
         * y = r * sin(elevation) * sin(azimuth)
         * z = r * cos(elevation)
         */
        Vector3f position = new Vector3f();
        position.x = (float) (azState.getRadius() * Math.sin(azState.getElevation()) * Math.cos(azState.getAzimuth()));
        position.y = (float) (azState.getRadius() * Math.sin(azState.getElevation()) * Math.sin(azState.getAzimuth()));
        position.z = (float) (azState.getRadius() * Math.cos(azState.getElevation()));
        
        transform.lookAt(position, azState.getTarget(), transform.getLocalY());
    }

    public void handleInputEvents(CameraState state, Object[] events) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.AzimuthAngle)
            throw new WrongStateTypeException("Expected AzimuthAngleState, got " + state.getClass());
        AzimuthAngleState azState = (AzimuthAngleState)state;
        
        for (int i = 0; i < events.length; i++) // For each event
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_PRESSED)
                {
                    m_currentX = me.getX();
                    m_currentY = me.getY();
                    m_lastMouseX = m_currentX;
                    m_lastMouseY = m_currentY;
                }
                if (me.getID() == MouseEvent.MOUSE_DRAGGED) 
                {
                    m_currentX = me.getX();
                    m_currentY = me.getY();
                    processMouseDragging(azState, me);
                    m_lastMouseX = m_currentX;
                    m_lastMouseY = m_currentY;
                }
            } else if (events[i] instanceof KeyEvent)
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(azState, ke);
            }
        }
    }

    private void processKeyEvent(AzimuthAngleState state, KeyEvent event)
    {
        // do nothing currently
    }
    
    private void processMouseDragging(AzimuthAngleState state, MouseEvent me)
    {
        int deltaX = m_currentX - m_lastMouseX;
        int deltaY = m_currentY - m_lastMouseY;

        state.setAzimuth(state.getAzimuth() + Math.toRadians(deltaY));
        state.setElevation(state.getElevation() + Math.toRadians(deltaX));
    }

    public void update(CameraState state, float deltaTime) throws WrongStateTypeException
    {
        if (state.getType() != CameraState.CameraStateType.AzimuthAngle)
            throw new WrongStateTypeException("Wrong state type");
        // Do nothing;
    }
}
