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
import imi.utils.FileUtils;




/**
 * The PColladaImage class represents an image referenced in a collada file.
 * The PColladaMaterial class will reference this class.
 *
 * @author Chris Nagle
 */
public class PColladaImage
{
    String              m_Name;
    String              m_ShortFilename;
    String              m_Filename;
    
    private Collada m_loaderRef = null;



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
    public PColladaImage(String name, String shortFilename, Collada loader)
    {
        this(loader);
        m_Name = name;
        m_ShortFilename = shortFilename;

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
     * Calculates the filename.
     */
    public void calculateFilename()
    {
        String currentFolder = m_loaderRef.getFileLocation().toString().substring(0, m_loaderRef.getFileLocation().toString().lastIndexOf('/') + 1);
        m_Filename = currentFolder + m_ShortFilename;
//        m_Filename = FileUtils.findTextureFile(m_ShortFilename);
//        if (m_Filename.length() == 0)
//            m_Filename = FileUtils.findTextureFile("default.png");
    }

}



