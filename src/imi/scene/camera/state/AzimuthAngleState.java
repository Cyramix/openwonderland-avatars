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
package imi.scene.camera.state;

/**
 * This class provides all the state information needed by a 
 * @author Ronald E Dahlgren
 */
public class AzimuthAngleState extends CameraState
{
    /** The current azimuth **/
    private Double m_azimuth    = null;
    /** The current elevation **/
    private Double m_elevation      = null;
    /** The current radius **/
    private Double m_radius     = null;
    
    /**
     * Construct a new instance
     */
    public AzimuthAngleState()
    {
        set(0.0, 0.0, 0.0);
    }
    
    /**
     * Construct a new instance with the specified characteristics.
     * @param azimuth Radians 
     * @param angle Radians
     * @param radius
     */
    public AzimuthAngleState(double azimuth, double elevation, double radius)
    {
        set(azimuth, elevation, radius);
    }
    
    /**
     * Copy construction
     * @param that The one to copy
     */
    public AzimuthAngleState(AzimuthAngleState that)
    {
        set(that.getAzimuth(), that.getElevation(), that.getRadius());
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
    
    private void set(double azimuth, double elevation, double radius)
    {
        m_azimuth = Double.valueOf(azimuth);
        m_elevation   = Double.valueOf(elevation);
        m_radius  = Double.valueOf(radius);
    }
    
    private void clear()
    {
        m_azimuth = null;
        m_elevation   = null;
        m_radius  = null;
    }
    
}
