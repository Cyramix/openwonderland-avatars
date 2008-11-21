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

import java.util.logging.Logger;
import org.collada.colladaschema.Camera;
import org.collada.colladaschema.Camera.Optics.TechniqueCommon.Perspective;
import org.collada.colladaschema.LibraryCameras;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class LibraryCamerasProcessor extends Processor
{
    /**
     * Constructor.
     * @param pCollada
     * @param pCameras
     * @param pParent
     */
    public LibraryCamerasProcessor(Collada pCollada, LibraryCameras pCameras, Processor pParent)
    {
        super(pCollada, pCameras, pParent);

        if (pCameras.getCameras() != null && pCameras.getCameras().size() > 0)
        {
            int a;
            Camera pCamera;
            
            for (a=0; a<pCameras.getCameras().size(); a++)
            {
                pCamera = pCameras.getCameras().get(a);
                
                processCamera(pCamera);
            }
        }
    }

    /**
     * Processes a Camera node.
     * @param pCamera
     */
    private void processCamera(Camera pCamera)
    {
        String cameraName = pCamera.getName();

        if (pCamera.getOptics() != null)
        {
            if (pCamera.getOptics().getTechniqueCommon() != null)
            {
                if (pCamera.getOptics().getTechniqueCommon().getPerspective() != null)
                {
                    Perspective pPerspective = pCamera.getOptics().getTechniqueCommon().getPerspective();

                    float xfov = 0.0f;
                    float yfov = 0.0f;
                    float znear = 0.0f;
                    float zfar = 0.0f;

                    // Attempt to grab parameters; if any are not present we use
                    // some default values.
                    if (pPerspective.getYfov() != null)
                        yfov  = (float)pPerspective.getYfov().getValue();
                    else
                        yfov = 45.0f;

                    if (pPerspective.getZnear() != null)
                        znear = (float)pPerspective.getZnear().getValue();
                    else
                        znear = 0.1f;

                    if (pPerspective.getZfar() != null)
                        zfar  = (float)pPerspective.getZfar().getValue();
                    else
                        zfar = 1000.0f;

                    // If an aspect ratio was provided, use it instead
                    if (pPerspective.getAspectRatio() != null)
                    {
                        float fAspectRatio = (float)pPerspective.getAspectRatio().getValue();
                        
                        xfov = yfov * fAspectRatio;
                    }
                    else if (pPerspective.getXfov() != null)
                        xfov  = (float)pPerspective.getXfov().getValue();


                    //  Build the ColladaCameraParams.
                    PColladaCameraParams pCameraParams = new PColladaCameraParams();

                    pCameraParams.initialize(cameraName, xfov, yfov, znear, zfar, true);

                    m_colladaRef.addColladaCameraParams(pCameraParams);
                }
            }
        }
    }

}




