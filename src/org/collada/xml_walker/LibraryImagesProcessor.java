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
    

    //  Constructor.
    public LibraryImagesProcessor(Collada pCollada, LibraryImages pImages, Processor pParent)
    {
        super(pCollada, pImages, pParent);

        //System.out.println("LibraryImagesProcessor");

        PColladaImage pColladaImage;
        Image pImage;

        //  Populate list of all the Images so we can look them up later.
        for (int a=0; a<pImages.getImages().size(); a++)
        {
            pImage = (Image)pImages.getImages().get(a);
            
            processImage(pImage);
        }
    }

    private void processImage(Image pImage)
    {
        m_pCollada.addColladaImage(pImage.getId(), pImage.getInitFrom());
    }
            
}




