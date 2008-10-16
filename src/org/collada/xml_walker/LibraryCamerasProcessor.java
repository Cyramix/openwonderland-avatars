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
    //  Constructor.
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

    //  Processes a Camera node.
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

                    float xfov, yfov, znear, zfar;

                    yfov  = (float)pPerspective.getYfov().getValue();
                    znear = (float)pPerspective.getZnear().getValue();
                    zfar  = (float)pPerspective.getZfar().getValue();
                    
                    if (pPerspective.getAspectRatio() != null)
                    {
                        float fAspectRatio = (float)pPerspective.getAspectRatio().getValue();
                        
                        xfov = yfov * fAspectRatio;
                    }
                    else
                        xfov  = (float)pPerspective.getXfov().getValue();


                    PColladaCameraParams pCameraParams = new PColladaCameraParams();

                    pCameraParams.initialize(cameraName, xfov, yfov, znear, zfar, true);

                    m_pCollada.addColladaCameraParams(pCameraParams);
                }
            }
        }
    }

}




