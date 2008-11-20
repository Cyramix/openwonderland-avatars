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




/**
 *
 * @author Chris Nagle
 */
public class PColladaMaterialInstance
{
    private String              m_InstanceName = null;
    private String              m_MaterialName = null;

    private ArrayList<String>   m_VertexInputs = null;

    
    
    //  Constructor.
    public PColladaMaterialInstance()
    {
    }


    
    //  Gets the Instance Name.
    public String getInstanceName()
    {
        return m_InstanceName;
    }

    //  Sets the InstanceName.
    public void setInstanceName(String instanceName)
    {
        m_InstanceName = instanceName;
    }

    //  Gets the MaterialName.
    public String getMaterialName()
    {
        return(m_MaterialName);
    }

    /**
     * Set the material name. If the material is a link (starts with '#'), the
     * first character will be removed.
     * @param materialName
     */
    public void setMaterialName(String materialName)
    {
        m_MaterialName = materialName;
        if (m_MaterialName.startsWith("#"))
            m_MaterialName = m_MaterialName.substring(1, m_MaterialName.length());

        int indexOfMaterialNameEnd = m_MaterialName.lastIndexOf("-"); // <-- What is this hack?
        if (indexOfMaterialNameEnd != -1)
            m_MaterialName = m_MaterialName.substring(0, indexOfMaterialNameEnd);

    }



    //  Adds a VertexInput.
    public void addVertexInput(String vertexInput)
    {
        if (m_VertexInputs == null)
            m_VertexInputs = new ArrayList<String>();
        m_VertexInputs.add(vertexInput);
    }

    //  Gets the number of VertexInputs.
    public int getVertexInputCount()
    {
        if (m_VertexInputs != null)
            return m_VertexInputs.size();
        else
            return 0;
    }

    //  Gets the VertexInput at the specified index.
    public String getVertexInput(int index)
    {
        if (m_VertexInputs != null)
            return m_VertexInputs.get(index);
        else
            return null;
    }

}




