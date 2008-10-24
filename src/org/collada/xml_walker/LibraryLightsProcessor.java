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

import java.util.ArrayList;
import java.util.List;
import org.collada.colladaschema.LibraryLights;
import org.collada.colladaschema.Light;

import imi.loaders.collada.Collada;



/**
 *
 * @author paulby
 */
public class LibraryLightsProcessor extends Processor
{

    /**
     * Constructor.
     * 
     * @param collada
     * @param libraryLights
     * @param parent
     */
    public LibraryLightsProcessor(Collada pCollada, LibraryLights pLibraryLights, Processor pParent)
    {
        super(pCollada, pLibraryLights, pParent);
        
        List<Light> lights = pLibraryLights.getLights();
        for(Light pLight : lights)
        {
            processLight(pLight);
        }
    }

    /**
     * Process a collada Light.
     * 
     * @param pLight
     */
    private void processLight(Light pLight)
    {
        //  Should process the light and add it to the PScene.
        //  Should probably only do this when level geometry is loaded.
    }

}


