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


import imi.scene.PMatrix;



/**
 *
 * @author Chris Nagle
 */
public class PColladaCamera
{
    private String                  m_Name = "";
    private PColladaCameraParams    m_pCameraParams;
    private PMatrix                 m_Matrix = new PMatrix();




    //  Constructor.
    public PColladaCamera()
    {
    }
    public PColladaCamera(String name, PColladaCameraParams pCameraParams, PMatrix pMatrix)
    {
        initialize(name, pCameraParams, pMatrix);
    }



    //  Initializes the Camera.
    public void initialize(String name, PColladaCameraParams pCameraParams, PMatrix pMatrix)
    {
        m_Name = name;
        m_pCameraParams = pCameraParams;
        m_Matrix.set(pMatrix);
    }



    //  Gets the name of the Camera.
    public String getName()
    {
        return(m_Name);
    }

    //  Sets the name of the Camera.
    public void setName(String name)
    {
        m_Name = name;
    }



    //  Gets the CameraParams.
    public PColladaCameraParams getCameraParams()
    {
        return(m_pCameraParams);
    }



    //  Gets the Camera's matrix.
    public PMatrix getMatrix()
    {
        return(m_Matrix);
    }

    //  Sets the Camera's matrix.
    public void setMatrix(PMatrix pMatrix)
    {
        m_Matrix.set(pMatrix);
    }

}




