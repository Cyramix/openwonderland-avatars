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




    /**
     * Default constructor.
     */
    public PColladaCamera()
    {
    }

    /**
     * Constructor.
     * 
     * @param name - The name of the collada camera.
     * @param pCameraParams - Parameters of the camera.
     * @param pMatrix - Matrix of the camera representing it's position and
     *                  orientation.
     */
    public PColladaCamera(String name, PColladaCameraParams pCameraParams, PMatrix pMatrix)
    {
        initialize(name, pCameraParams, pMatrix);
    }



    /**
     * Initializes the Camera.
     * 
     * @param name - The name of the collada camera.
     * @param pCameraParams - Parameters of the camera.
     * @param pMatrix - Matrix of the camera representing it's position and
     *                  orientation.
     */
    public void initialize(String name, PColladaCameraParams pCameraParams, PMatrix pMatrix)
    {
        m_Name = name;
        m_pCameraParams = pCameraParams;
        m_Matrix.set(pMatrix);
    }



    /**
     * Gets the name of the Camera.
     * 
     * @return String
     */
    public String getName()
    {
        return(m_Name);
    }

    /**
     * Sets the name of the Camera.
     * 
     * @param name
     */
    public void setName(String name)
    {
        m_Name = name;
    }



    /**
     * Gets the camera parameters.
     * 
     * @return PColladaCameraParams
     */
    public PColladaCameraParams getCameraParams()
    {
        return(m_pCameraParams);
    }



    /**
     * Gets the camera's matrix representing it's position and orientation.
     * 
     * @return PMatrix
     */
    public PMatrix getMatrix()
    {
        return(m_Matrix);
    }

    /**
     * Sets the camera's matrix representing it's position and orientation.
     * 
     * @param pMatrix
     */
    public void setMatrix(PMatrix pMatrix)
    {
        m_Matrix.set(pMatrix);
    }

}




