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


import com.jme.renderer.ColorRGBA;
import java.util.ArrayList;
import org.collada.colladaschema.InputLocalOffset;
import org.collada.colladaschema.Source;

import com.jme.math.Vector3f;
import com.jme.math.Vector2f;






/**
 * The VertexDataArray contains data for some vertex data.
 * (Position, Normal, TexCoord1, TexCoord2, etc...)
 * 
 * @author Chris Nagle
 */

class VertexDataArray
{
    public String           m_Name;
    public float            []m_Data;


    /**
     * Constructor.
     * 
     * @param pSource
     */
    public VertexDataArray(Source pSource)
    {
        m_Name = pSource.getId();

        if (pSource.getFloatArray().getValues().size() > 0)
        {
            m_Data = new float[pSource.getFloatArray().getValues().size()];
            for (int a=0; a<m_Data.length; a++)
                m_Data[a] = pSource.getFloatArray().getValues().get(a).floatValue();
        }
    }

    /**
     * Gets the size of the array.
     * @return int
     */
    public int getSize()
    {
        if (m_Data == null)
            return(0);

        return(m_Data.length);
    }


    /**
     * Gets the Vector3f value at the specified index.
     *
     * @param index
     * @return Vector3f
     */
    public void getVector3f(int index, Vector3f output)
    {
        int elementIndex = index * 3;
        output.set(m_Data[elementIndex], m_Data[elementIndex+1], m_Data[elementIndex+2]);
    }

    /**
     * Gets the Vector2f value at the specified index.
     * 
     * @param index
     * @return
     */
    public Vector2f getVector2f(int index)
    {
        int elementIndex = index * 2;

        Vector2f value = new Vector2f(m_Data[elementIndex], m_Data[elementIndex+1]);
        
        return(value);
    }

    void getColorRGBA(int i, ColorRGBA color) {
        int elementIndex = i * 4;
        color.set(m_Data[elementIndex], m_Data[elementIndex+1], m_Data[elementIndex+2], m_Data[elementIndex+3]);
    }
    
}
/**
 * The VectorDataSemantic class represents a Vertex element.  It stores
 * the name of the element and the offset within the data the element occurs.
 * 
 * @author Chris Nagle
 */
class VertexDataSemantic
{
    public String           m_Name;
    public int              m_Offset;

    public String           m_DataName;
    public VertexDataArray  m_DataArray;


    /**
     * Constructor.
     * 
     * @param name
     * @param offset
     * @param dataName
     * @param pVertexDataArray
     */
    public VertexDataSemantic(String name, int offset, String dataName, VertexDataArray pVertexDataArray)
    {
        m_Name = name;
        m_Offset = offset;
        m_DataName = dataName;
        m_DataArray = pVertexDataArray;
    }

    /**
     * Constructor.
     * 
     * @param pInputLocalOffset
     */
    public VertexDataSemantic(InputLocalOffset pInputLocalOffset)
    {
        m_Name = pInputLocalOffset.getSemantic();
        m_Offset = pInputLocalOffset.getOffset().intValue();
        m_DataName = pInputLocalOffset.getSource();
        m_DataArray = null;

/*
        if (m_Name.equals("VERTEX"))
        {
            if (m_DataName.endsWith("-vertices"))
                m_DataName = m_DataName.replaceFirst("-vertices", "-positions");
            else if (m_DataName.endsWith("-vertex"))
                m_DataName = m_DataName.replaceFirst("-vertex", "-position");
        }
*/

        //  Remove the '#' at the beginning of the m_DataName if it's there.
        if (m_DataName.startsWith("#"))
            m_DataName = m_DataName.substring(1);


        //System.out.println("   VertexDataSemantic:  Name=" + m_Name + ", Offset=" + m_Offset + ", DataName=" + m_DataName);
    }


    /**
     * Gets the name of the VertexDataSemantic.
     * 
     * @return String
     */
    public String getName()
    {
        return(m_Name);
    }

    /**
     * Gets the data name of the VertexDataSemantic.
     * 
     * @return String
     */
    public String getDataName()
    {
        return(m_DataName);
    }

    /**
     * Gets the size of the data.
     * 
     * @return int
     */
    public int getDataSize()
    {
        return(m_DataArray.getSize());
    }



    /**
     * Gets the number of Vector3fs.
     * 
     * @return int
     */
    public int getVector3fCount()
    {
        return(m_DataArray.getSize() / 3);
    }

    int getColorRGBACount() {
        return(m_DataArray.getSize() / 4);
    }
    /**
     * checks to see if the specified index is a valid index.
     * 
     * @param index
     * @return boolean
     */
    public boolean isValidVector3fIndex(int index)
    {
        if (index * 3 < m_DataArray.getSize())
            return(true);
        return(false);
    }

    /**
     * Gets a Vector3f value.
     *
     * @param index
     * @return Vector3f
     */
    public void getVector3f(int index, Vector3f output)
    {
        m_DataArray.getVector3f(index, output);
    }



    /**
     * Gets the number of Vector2fs.
     * 
     * @return int
     */
    public int getVector2fCount()
    {
        return(m_DataArray.getSize() / 2);
    }

    /**
     * Checks to see if the specified index is a valid index.
     * 
     * @param index
     * @return boolean
     */
    public boolean isValidVector2fIndex(int index)
    {
        if (index * 2 < m_DataArray.getSize())
            return(true);
        return(false);
    }

    /**
     * Gets a Vector2f value.
     * 
     * @param index
     * @return Vector2f
     */
    public Vector2f getVector2f(int index)
    {
        return(m_DataArray.getVector2f(index));
    }

    void getColorRGBA(int i, ColorRGBA color) {
        m_DataArray.getColorRGBA(i, color);
    }



}
/**
 * The VertexDataDescription class describes the format of a Vertex via
 * multiple VertexDataSemantics.
 * One Vertex might be of the format (Position, Normal, TexCoord1) and
 * another vertex might of the format (Position, Normal, TexCoord1, TexCoord2).
 * 
 * @author Chris Nagle
 */
public class VertexDataDescription
{
    ArrayList               m_Semantics = new ArrayList();



    /**
     * Default constructor.
     */
    public VertexDataDescription()
    {
    }


    /**
     * Adds a Semantic.
     * 
     * @param pSemantic
     */
    public void addSemantic(VertexDataSemantic pSemantic)
    {
        m_Semantics.add(pSemantic);
    }

    /**
     * Gets the number of Semantics.
     * 
     * @return int
     */
    public int getSemanticCount()
    {
        return(m_Semantics.size());
    }

    /**
     * Gets the Semantic at the specified index.
     * 
     * @param Index
     * @return VertexDataSemantic.
     */
    public VertexDataSemantic getSemantic(int Index)
    {
        return( (VertexDataSemantic)m_Semantics.get(Index));
    }

    /**
     * Gets the Semantic with the specified name.
     * 
     * @param name
     * @return VertexDataSemantic.
     */
    public VertexDataSemantic findSemantic(String name)
    {
        int a;
        VertexDataSemantic pSemantic;
        
        for (a=0; a<m_Semantics.size(); a++)
        {
            pSemantic = (VertexDataSemantic)m_Semantics.get(a);
            
            if (pSemantic.m_Name.equals(name))
                return(pSemantic);
        }

        return(null);
    }

    
    public int          m_VertexOffset = -1;
    public int          m_NormalOffset = -1;
    public int          m_TangentOffset = -1;
    public int          m_BinormalOffset = -1;
    public int          m_TexCoordOffset = -1;
    public int          m_TexTangentOffset = -1;
    public int          m_TexBinormalOffset = -1;

}



