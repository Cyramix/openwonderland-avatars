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

import imi.loaders.collada.Collada;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;

/**
 * The PColladaImage class represents an image referenced in a collada file.
 * The PColladaMaterial class will reference this class.
 *
 * @author Chris Nagle
 */
public class PColladaImage
{
    private String              m_Name = null;
    private String              m_ShortFilename = null;
    private String              m_Filename = null;
    /** Any special loading requirements for this image **/
    private TextureMaterialProperties m_imageProperties = null;
    
    private Collada m_loaderRef = null;



    public PColladaImage()
    {
        
    }
    /**
     * Default Constructor.
     */
    public PColladaImage(Collada loader)
    {
        m_loaderRef = loader;
    }

    /**
     * Constructs a new PColladaImage with the specified name and shortFilename.
     * @param name The desired name.
     * @param shortFilename The short filename of the image (no directory).
     */
    public PColladaImage(String name, String shortFilename, Collada loader, TextureMaterialProperties imageProps)
    {
        m_loaderRef = loader;
        m_Name = name;
        m_ShortFilename = shortFilename;
        m_imageProperties = imageProps;
        //  Calculate the Filename.
        calculateFilename();
    }



    /**
     * Get the name of the Image
     * @return m_Name (String)
     */
    public String getName()
    {
        return(m_Name);
    }

    /**
     * Set the name for this Image
     * @param name
     */
    public void setName(String name)
    {
        m_Name = name;
    }



    /**
     * Get the short filename of the Image
     * @return m_ShortFilename (String)
     */
    public String getShortFilename()
    {
        return(m_ShortFilename);
    }

    /**
     * Set the short filename for this Image
     * @param shortFilename
     */
    public void setShortFilename(String shortFilename)
    {
        m_ShortFilename = shortFilename;
    }



    /**
     * Get the filename of the Image
     * @return m_Filename (String)
     */
    public String getFilename()
    {
        return(m_Filename);
    }

    /**
     * Set the filename for this Image
     * @param filename
     */
    public void setFilename(String filename)
    {
        m_Filename = filename;
    }

    /**
     * Set the properties for this image
     * @param imageProps
     */
    public void setImageProperties(TextureMaterialProperties imageProps)
    {
        m_imageProperties = imageProps;
    }
    
    /**
     * Retrieve the current properties for this image
     * @return
     */
    public TextureMaterialProperties getImageProperties()
    {
        return m_imageProperties;
    }

    /**
     * Calculates the filename.
     */
    public void calculateFilename()
    {
        // HACK : Again with the file location hacks
        String currentFolder = m_loaderRef.getFileLocation().toString().substring(0, m_loaderRef.getFileLocation().toString().lastIndexOf('/') + 1);
        m_Filename = currentFolder + m_ShortFilename;
    }

}



