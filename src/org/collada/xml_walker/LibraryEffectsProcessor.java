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

import java.util.List;
import javax.xml.bind.JAXBElement;

import org.collada.colladaschema.Extra;
import org.collada.colladaschema.Effect;
import org.collada.colladaschema.LibraryEffects;
import org.collada.colladaschema.ProfileCOMMON;
import org.collada.colladaschema.ProfileCOMMON.Technique.Blinn;
import org.collada.colladaschema.ProfileCOMMON.Technique.Lambert;
import org.collada.colladaschema.ProfileCOMMON.Technique.Phong;

import imi.loaders.collada.Collada;




/**
 *
 * @author paulby
 */
public class LibraryEffectsProcessor extends Processor
{
    //  Constructor.
    public LibraryEffectsProcessor(Collada pCollada, LibraryEffects pLibraryEffects, Processor pParent)
    {
        super(pCollada, pLibraryEffects, pParent);

        List<Effect> effects = pLibraryEffects.getEffects();
        for(Effect pEffect : effects)
        {
            processEffect(pEffect);
        }
    }

    private void processEffect(Effect pEffect)
    {
        String materialName = pEffect.getId();
        JAXBElement pJAXBElement;
        ProfileCOMMON pProfileCOMMON;
        
        int indexOfMaterialNameEnd = materialName.lastIndexOf("-");
        if (indexOfMaterialNameEnd != -1)
            materialName = materialName.substring(0, indexOfMaterialNameEnd);

        pJAXBElement = (JAXBElement)pEffect.getFxProfileAbstracts().get(0);
        
        
        //  'value' member of pJAXBElement should be a ProfileCOMMON.
        if (pJAXBElement.getValue() instanceof ProfileCOMMON)
        {
            pProfileCOMMON = (ProfileCOMMON)pJAXBElement.getValue();

            processProfileCOMMON(pProfileCOMMON, materialName);
        }
    }
    
    private void processProfileCOMMON(ProfileCOMMON pProfileCommon, String materialName)
    {
        ProfileCOMMON.Technique pTechnique = pProfileCommon.getTechnique();
        if (pTechnique == null)
            return;

        //  Process the Blinn type of Material if the Profile contains it.
        if (pTechnique.getBlinn() != null)
            processBlinn(pProfileCommon, pTechnique.getBlinn(), materialName);

        //  Otherwise, process the Phong type of Material if the Profile contains it.
        else if (pTechnique.getPhong() != null)
            processPhong(pProfileCommon, pTechnique.getPhong(), materialName);

        //  Otherwise, process the Lambert type of Material if the Profile contains it.
        if (pTechnique.getLambert() != null)
            processLambert(pProfileCommon, pTechnique.getLambert(), materialName);
        
        for (Extra extra : pTechnique.getExtras())
            processExtra(pProfileCommon, extra, materialName);
    }

    private void processExtra(ProfileCOMMON profile, Extra extra, String materialName)
    {
        PColladaMaterial colladaMaterial = m_pCollada.getColladaMaterial(materialName);
        if (colladaMaterial != null)
        {
            colladaMaterial.applyBumpMappingData(extra);
        }
        else // new material, do nothing for now
        {
            
        }
       
    }

    
    private void processBlinn(ProfileCOMMON pProfileCommon, Blinn pBlinn, String materialName)
    {
       // System.out.println("Material Blinn '" + materialName + "'");

        //  Create a ColladaMaterial.
        PColladaMaterial pColladaMaterial = new PColladaMaterial(m_pCollada, pProfileCommon);
        
        pColladaMaterial.initialize(materialName, pBlinn);

        m_pCollada.addColladaMaterial(pColladaMaterial);
    }
    
    private void processPhong(ProfileCOMMON pProfileCommon, Phong pPhong, String materialName)
    {
        //System.out.println("Material Phong '" + materialName + "'");

        //  Create a ColladaMaterial.
        PColladaMaterial pColladaMaterial = new PColladaMaterial(m_pCollada, pProfileCommon);
        
        pColladaMaterial.initialize(materialName, pPhong);

        m_pCollada.addColladaMaterial(pColladaMaterial);
    }

    private void processLambert(ProfileCOMMON pProfileCommon, Lambert pLambert, String materialName)
    {
        //System.out.println("Material Lambert '" + materialName + "'");

        //  Create a ColladaMaterial.
        PColladaMaterial pColladaMaterial = new PColladaMaterial(m_pCollada, pProfileCommon);
        
        pColladaMaterial.initialize(materialName, pLambert);

        m_pCollada.addColladaMaterial(pColladaMaterial);
    }
}




