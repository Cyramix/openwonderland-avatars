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
import org.collada.colladaschema.LibraryImages;
import org.collada.colladaschema.Image;

import imi.loaders.collada.Collada;



/**
 *
 * @author Chris Nagle
 */
public class LibraryImagesProcessor extends Processor
{
    
    /**
     * Constructor.
     * 
     * @param pCollada
     * @param pImages
     * @param pParent
     */
    public LibraryImagesProcessor(Collada pCollada, LibraryImages pImages, Processor pParent)
    {
        super(pCollada, pImages, pParent);

        PColladaImage pColladaImage = null;
        Image pImage = null;

        //  Populate list of all the Images so we can look them up later.
        for (int a=0; a<pImages.getImages().size(); a++)
        {
            pImage = (Image)pImages.getImages().get(a);
            
            processImage(pImage, pColladaImage);
            
            
        }
    }

    /**
     * Processes a collada image.
     * 
     * @param pImage
     */
    private void processImage(Image pImage, PColladaImage colladaImage)
    {
        PColladaImage newImage = new PColladaImage(pImage.getId(), pImage.getInitFrom(), m_pCollada, null);
        // process image property tags
    }
            
}




