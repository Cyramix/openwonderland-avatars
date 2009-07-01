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

import java.util.List;
import javax.xml.bind.JAXBElement;

import org.collada.colladaschema.Extra;
import org.collada.colladaschema.Effect;
import org.collada.colladaschema.LibraryEffects;
import org.collada.colladaschema.ProfileCOMMON;
import org.collada.colladaschema.ProfileCOMMON.Technique.Blinn;
import org.collada.colladaschema.ProfileCOMMON.Technique.Lambert;
import org.collada.colladaschema.ProfileCOMMON.Technique.Phong;

import imi.loaders.Collada;




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
        String effectIdentifier = pEffect.getId();
        JAXBElement pJAXBElement = null;
        ProfileCOMMON pProfileCOMMON = null;

        pJAXBElement = (JAXBElement)pEffect.getFxProfileAbstracts().get(0);
        
        
        //  'value' member of pJAXBElement should be a ProfileCOMMON.
        if (pJAXBElement.getValue() instanceof ProfileCOMMON)
        {
            pProfileCOMMON = (ProfileCOMMON)pJAXBElement.getValue();

            processProfileCOMMON(pProfileCOMMON, effectIdentifier);
        }
    }
    
    private void processProfileCOMMON(ProfileCOMMON pProfileCommon, String effectIdentifier)
    {
        ProfileCOMMON.Technique pTechnique = pProfileCommon.getTechnique();
        if (pTechnique == null)
            return;

        //  Process the Blinn type of Material if the Profile contains it.
        if (pTechnique.getBlinn() != null)
            processBlinn(pProfileCommon, pTechnique.getBlinn(), effectIdentifier);

        //  Otherwise, process the Phong type of Material if the Profile contains it.
        else if (pTechnique.getPhong() != null)
            processPhong(pProfileCommon, pTechnique.getPhong(), effectIdentifier);

        //  Otherwise, process the Lambert type of Material if the Profile contains it.
        if (pTechnique.getLambert() != null)
            processLambert(pProfileCommon, pTechnique.getLambert(), effectIdentifier);
        
        for (Extra extra : pTechnique.getExtras())
            processExtra(extra, effectIdentifier);
    }

    private void processExtra(Extra extra, String effectIdentifier)
    {
        PColladaEffect colladaMaterial = m_colladaRef.getColladaEffect(effectIdentifier);
        if (colladaMaterial != null)
        {
            colladaMaterial.applyBumpMappingData(extra);
        }
        else // new material, do nothing for now
        {
            
        }
       
    }

    
    private void processBlinn(ProfileCOMMON pProfileCommon, Blinn pBlinn, String effectIdentifier)
    {
        //  Create a ColladaMaterial.
        PColladaEffect blinnEffect = new PColladaEffect(m_colladaRef, pProfileCommon);
        
        blinnEffect.initialize(effectIdentifier, pBlinn);

        m_colladaRef.addColladaEffect(blinnEffect);
    }
    
    private void processPhong(ProfileCOMMON pProfileCommon, Phong pPhong, String effectIdentifier)
    {
        //  Create a ColladaMaterial.
        PColladaEffect phongEffect = new PColladaEffect(m_colladaRef, pProfileCommon);
        
        phongEffect.initialize(effectIdentifier, pPhong);

        m_colladaRef.addColladaEffect(phongEffect);
    }

    private void processLambert(ProfileCOMMON pProfileCommon, Lambert pLambert, String effectIdentifier)
    {
        //  Create a ColladaMaterial.
        PColladaEffect lambertEffect = new PColladaEffect(m_colladaRef, pProfileCommon);
        
        lambertEffect.initialize(effectIdentifier, pLambert);

        m_colladaRef.addColladaEffect(lambertEffect);
    }
}




