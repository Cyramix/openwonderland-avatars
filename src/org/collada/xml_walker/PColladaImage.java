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

import imi.utils.FileUtils;




/**
 *
 * @author Chris Nagle
 */
public class PColladaImage
{
    String              m_Name;
    String              m_ShortFilename;
    String              m_Filename;



    //  Constructor.
    public PColladaImage()
    {
    }

    public PColladaImage(String name, String shortFilename)
    {
        m_Name = name;
        m_ShortFilename = shortFilename;

        //  Calculate the Filename.
        calculateFilename();
    }



    //  Gets the Name of the Image.
    public String getName()
    {
        return(m_Name);
    }

    //  Sets the Name of the Image.
    public void setName(String name)
    {
        m_Name = name;
    }



    //  Gets the ShortFilename of the Image.
    public String getShortFilename()
    {
        return(m_ShortFilename);
    }

    //  Sets the ShortFilename of the Image.
    public void setShortFilename(String shortFilename)
    {
        m_ShortFilename = shortFilename;
    }



    //  Gets the Filename of the Image.
    public String getFilename()
    {
        return(m_Filename);
    }

    //  Sets the Filename of the Image.
    public void setFilename(String filename)
    {
        m_Filename = filename;
    }

    //  Calculates the Filename.
    public void calculateFilename()
    {
        m_Filename = FileUtils.findTextureFile(m_ShortFilename);
        if (m_Filename.length() == 0)
            m_Filename = FileUtils.findTextureFile("default.png");
    }

}



