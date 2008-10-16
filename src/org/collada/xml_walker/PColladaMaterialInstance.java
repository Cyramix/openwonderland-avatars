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
    String              m_InstanceName;
    String              m_MaterialName;

    ArrayList           m_VertexInputs = new ArrayList();

    
    
    //  Constructor.
    public PColladaMaterialInstance()
    {
    }


    
    //  Gets the Instance Name.
    public String getInstanceName()
    {
        return(m_InstanceName);
    }

    //  Sets the InstanceName.
    public void setInstanceName(String instanceName)
    {
        m_InstanceName = instanceName;

        //System.out.println("PColladaMaterialInstance.m_InstanceName = " + m_InstanceName);
    }



    //  Gets the MaterialName.
    public String getMaterialName()
    {
        return(m_MaterialName);
    }

    //  Sets the MaterialName.
    public void setMaterialName(String materialName)
    {
        m_MaterialName = materialName;
        if (m_MaterialName.startsWith("#"))
            m_MaterialName = m_MaterialName.substring(1, m_MaterialName.length());

        int indexOfMaterialNameEnd = m_MaterialName.lastIndexOf("-");
        if (indexOfMaterialNameEnd != -1)
            m_MaterialName = m_MaterialName.substring(0, indexOfMaterialNameEnd);

        //System.out.println("PColladaMaterialInstance.m_MaterialName = " + m_MaterialName);
    }



    //  Adds a VertexInput.
    public void addVertexInput(String vertexInput)
    {
        m_VertexInputs.add(vertexInput);
    }

    //  Gets the number of VertexInputs.
    public int getVertexInputCount()
    {
        return(m_VertexInputs.size());
    }

    //  Gets the VertexInput at the specified index.
    public String getVertexInput(int index)
    {
        return( (String)m_VertexInputs.get(index));
    }

}




