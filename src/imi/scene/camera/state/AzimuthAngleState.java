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
package imi.scene.camera.state;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;

/**
 * This class provides all the state information needed by a 
 * @author Ronald E Dahlgren
 */
public class AzimuthAngleState extends CameraState
{
    /** The current azimuth **/
    private Double m_azimuth    = null;
    /** The current elevation **/
    private Double m_elevation  = null;
    /** The current radius **/
    private Double m_radius     = null;
    /** The target we are inspecting **/ 
    private Vector3f m_target   = null;
    
    /**
     * Construct a new instance
     */
    public AzimuthAngleState()
    {
        setType(CameraStateType.AzimuthAngle);
        set(0.0, 0.0, 0.0, new Vector3f());
    }
    
    /**
     * Construct a new instance with the specified characteristics.
     * @param azimuth Radians 
     * @param angle Radians
     * @param radius
     */
    public AzimuthAngleState(double azimuth, double elevation, double radius, Vector3f target)
    {
        setType(CameraStateType.AzimuthAngle);
        set(azimuth, elevation, radius, target);
    }
    
    /**
     * Copy construction
     * @param that The one to copy
     */
    public AzimuthAngleState(AzimuthAngleState that)
    {
        setType(CameraStateType.AzimuthAngle);
        set(that.getAzimuth(), that.getElevation(), that.getRadius(), that.getTarget());
    }
    
    
    ////////////////////////////////////////////////////////////
    // Utility methods, getters, and setters: aka The Dungeon //
    ////////////////////////////////////////////////////////////
    public double getAzimuth()
    {
        return m_azimuth.doubleValue();
    }
    
    public double getElevation()
    {
        return m_elevation.doubleValue();
    }
    
    public double getRadius()
    {
        return m_radius.doubleValue();
    }
    
    public Vector3f getTarget()
    {
        return m_target;
    }
    
    public void setAzimuth(double angle)
    {
        m_azimuth = Double.valueOf(angle);
    }
    
    public void setElevation(double angle)
    {
        m_elevation = Double.valueOf(angle);
    }
    
    public void setRadius(double radius)
    {
        m_radius = Double.valueOf(radius);
    }
    
    public void setTarget(Vector3f target)
    {
        m_target = target;
    }
    
    private void set(double azimuth, double elevation, double radius, Vector3f target)
    {
        m_azimuth = Double.valueOf(azimuth);
        m_elevation   = Double.valueOf(elevation);
        m_radius  = Double.valueOf(radius);
        m_target = target;
    }
    
    private void clear()
    {
        m_azimuth = null;
        m_elevation   = null;
        m_radius  = null;
    }
    
    public void setCameraPosition(Vector3f position)
    {
       // Do nothing!
    }

    public void setCameraTransform(PMatrix transform)
    {
        // TODO
    }

    @Override
    public PMatrix getCameraTransform() {
        return null; // TODO
    }
}
