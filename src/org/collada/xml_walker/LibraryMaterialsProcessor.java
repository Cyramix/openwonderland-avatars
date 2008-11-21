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
import org.collada.colladaschema.LibraryMaterials;
import org.collada.colladaschema.Material;

import imi.loaders.collada.Collada;




/**
 *
 * @author paulby
 */
public class LibraryMaterialsProcessor extends Processor {
    
    private ArrayList<MaterialProcessor> materialProcessors = new ArrayList();

    
    //  Constructor.
    public LibraryMaterialsProcessor(Collada pCollada, LibraryMaterials pLibraryMaterials, Processor pParent)
    {
        super(pCollada, pLibraryMaterials, pParent);
        
        List<Material> materials = pLibraryMaterials.getMaterials();
        for (Material pMaterial : materials)
        {
            processMaterial(pMaterial);
        }
    }


    private void processMaterial(Material pMaterial)
    {
        String instanceName = pMaterial.getName();
        String materialName = pMaterial.getInstanceEffect().getUrl();
        
        PColladaMaterialInstance pMaterialInstance = new PColladaMaterialInstance();
        
        pMaterialInstance.setInstanceName(instanceName);
        pMaterialInstance.setMaterialName(materialName);
        
        m_colladaRef.addColladaMaterialInstance(pMaterialInstance);
    }
}
