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


import java.util.ArrayList;




/**
 *
 * @author Chris Nagle
 */
public class PColladaMaterialInstance
{
    private String              m_instanceSymbol = null;
    private String              m_targetMaterialURL = null;

    private ArrayList<String>   m_VertexInputs = null;

    
    
    //  Constructor.
    public PColladaMaterialInstance()
    {
    }


    
    //  Gets the Instance Name.
    public String getInstanceSymbolString()
    {
        return m_instanceSymbol;
    }

    //  Sets the InstanceName.
    public void setInstanceSymbolString(String symbolString)
    {
        m_instanceSymbol = symbolString;
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

    public String getTargetMaterialURL()
    {
        return m_targetMaterialURL;
    }

    public void setTargetMaterialURL(String effectURL)
    {
        m_targetMaterialURL = effectURL;
        // dereference.
        if (m_targetMaterialURL.startsWith("#"))
            m_targetMaterialURL = m_targetMaterialURL.substring(1);
    }
}




