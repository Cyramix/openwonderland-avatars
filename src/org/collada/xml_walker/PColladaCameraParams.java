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
package org.collada.xml_walker;



/**
 *
 * @author Chris Nagle
 */
public class PColladaCameraParams
{
    private String          m_Name = "";
    private boolean         m_bPerspective = true;
    private float           m_xfov;
    private float           m_yfov;
    private float           m_znear;
    private float           m_zfar;



    //  Constructor.
    public PColladaCameraParams()
    {
    }



    //  Initializes the Camera with perspective parameters.
    public void initialize(String name, float xfov, float yfov, float znear, float zfar, boolean bPerspective)
    {
        m_Name = name;

        m_xfov  = xfov;
        m_yfov  = yfov;
        m_znear = znear;
        m_zfar  = zfar;

        m_bPerspective = bPerspective;  
    }

    

    //  Gets the name of the CameraParams.
    public String getName()
    {
        return(m_Name);
    }
    
    //  Sets the name of the CameraParams.
    public void setName(String name)
    {
        m_Name = name;
    }



    //  Gets boolean indicating whether the Camera is in perspective mode.
    public boolean isPerspective()
    {
        return(m_bPerspective);
    }

    public float getxfov()
    {
        return(m_xfov);
    }

    public float getyfov()
    {
        return(m_yfov);
    }

    public float getznear()
    {
        return(m_znear);
    }
    
    public float getzfar()
    {
        return(m_zfar);
    }

    
}



