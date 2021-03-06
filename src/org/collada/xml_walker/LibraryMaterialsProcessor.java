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
package org.collada.xml_walker;

import javolution.util.FastTable;
import java.util.List;
import org.collada.colladaschema.LibraryMaterials;
import org.collada.colladaschema.Material;

import imi.loaders.Collada;




/**
 *
 * @author paulby
 */
public class LibraryMaterialsProcessor extends Processor {
 
    
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


    private void processMaterial(Material theMaterial)
    {
        ColladaMaterial colladaMaterial = new ColladaMaterial(theMaterial.getName(),
                                                                theMaterial.getId(),
                                                                theMaterial.getAsset(),
                                                                theMaterial.getInstanceEffect(),
                                                                theMaterial.getExtras());
        m_colladaRef.addColladaMaterial(colladaMaterial);
//
//        PColladaEffect colladaMaterial = new PColladaEffect(m_colladaRef, effect.);
//        m_colladaRef.addColladaMaterial(pColladaMaterial);

        // THESE ARE NOT INSTANCES
//
//        String instanceName = pMaterial.getName();
//        String effectURL = pMaterial.getInstanceEffect().getUrl();
//
//        PColladaMaterialInstance pMaterialInstance = new PColladaMaterialInstance();
//
//        pMaterialInstance.setInstanceSymbolString(instanceName);
//        pMaterialInstance.setTargetMaterialURL(effectURL);
//
//        m_colladaRef.addColladaMaterialInstance(pMaterialInstance);
    }
}
