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
package imi.loaders;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import java.util.ArrayList;

/**     
 * MeshBuffer - Utility to re-arange mesh data from:
 *  multiple index buffers (one for positions, one for texture coordinates... etc)
 *  - suitable for editing in Digital Conetent Creation tools 
 *  
 * to: 
 *  a single index buffer, this is how :
 * 
 *    int index1 = addPosition(pos1);
 *    addNormal(norm1);
 *    addTexCoord(multiTextureIndex, uv1);
 * 
 *    int index2 = addPosition(pos2);
 *    addNormal(norm2);
 *    addTexCoord(multiTextureIndex, uv2);
 * 
 *    int index3 = addPosition(pos3);
 *    addNormal(norm3);
 *    addTexCoord(multiTextureIndex, uv3);
 * 
 *    addTriangle(index1, index2, index3);
 * 
 *    ... repeat to add more triangles
 * 
 *    then to use with a TriMesh:
 * 
 *       Vector3f[] vertices    = mb.getPositions();
 *       Vector3f[] normals     = mb.getNormals();
 *       Vector2f[] texCoords   = mb.getTexCoordZero();
 *       int[]      indecies    = mb.getIndices();
 *
 *       triMesh1.reconstruct(BufferUtils.createFloatBuffer(vertices), BufferUtils.createFloatBuffer(normals),
 *                            null, BufferUtils.createFloatBuffer(texCoords), BufferUtils.createIntBuffer(indecies));
 * 
 *   This will arrange the data using a sindle index array 
 *  - suitable for fast rendering in a game run-time engine.
 * 
 * @author Lou Hayt
 * @author Chris Nagle
 */
public class MeshBuffer
{
    static final int MAX_TEXTURES = 8;  // multi-texturing supported
    /** Collection of positions **/
    private ArrayList<Vector3f> m_Positions = new ArrayList<Vector3f>();
    /** Collection of normals **/
    private ArrayList<Vector3f> m_Normals   = new ArrayList<Vector3f>();
    /** Collection of texture coordinates **/
    private ArrayList<Vector2f> []    m_TexCoords = new ArrayList[MAX_TEXTURES];
    /** Index collection **/
    private ArrayList<Integer>  m_TriangleIndices   = new ArrayList<Integer>();

    /**
     * Construct a new instance!
     */
    public MeshBuffer()
    {
        for (int i = 0; i < MAX_TEXTURES; i++)
        {
            m_TexCoords[i] = new ArrayList<Vector2f>();
        }
    }

    /**
     * Add this position to the collection
     * @param position
     * @return index in the collection
     */
    public int addPosition(Vector3f position)
    {
        if (position != null)
        {
            m_Positions.add(position);
        }

        return (m_Positions.size() - 1);
    }

    /**
     * Add the vector represented by the provided components to the collection
     * @param x
     * @param y
     * @param z
     * @return index in the collection
     */
    public int addPosition(float x, float y, float z)
    {
        m_Positions.add(new Vector3f(x, y, z));

        return (m_Positions.size() - 1);
    }

    /**
     * Add the provided normal to the collection
     * @param normal
     */
    public void addNormal(Vector3f normal)
    {
        //  TODO : should I normalize?
        
        if (normal != null)
        {
            m_Normals.add(normal);
        }
    }
    
    public void addNormal(float x, float y, float z)
    {
        //  TODO : should I normalize?
        
        m_Normals.add(new Vector3f(x, y, z));
    }

    public void addTexCoord(int index, Vector2f texCoord)
    {
        if (texCoord != null)
        {
            m_TexCoords[index].add(texCoord);
        }
    }
    
    public void addTexCoord(int index, float u, float v)
    {
        m_TexCoords[index].add(new Vector2f(u, v));
    }

    public void addTriangle(int indexOne, int indexTwo, int indexThree)
    {
        m_TriangleIndices.add(new Integer(indexOne));
        m_TriangleIndices.add(new Integer(indexTwo));
        m_TriangleIndices.add(new Integer(indexThree));
    }

    public Vector3f[] getPositions()
    {
        Vector3f[] result = (Vector3f[])m_Positions.toArray(new Vector3f[0]);
        return result;
    }

    public Vector3f[] getNormals()
    {
        Vector3f[] result = (Vector3f[])m_Normals.toArray(new Vector3f[0]);
        return result;
    }

    public Vector2f[] getTexCoordZero()
    {
        return getTexCoords(0);
    }
    
    public Vector2f[] getTexCoords(int multiTextureIndex)
    {
        if (multiTextureIndex < 0 || multiTextureIndex > MAX_TEXTURES)
            return null;
        
        Vector2f[] arrayOfTexCoords = new Vector2f[m_TexCoords[multiTextureIndex].size()];

        for (int i = 0; i < m_TexCoords[multiTextureIndex].size(); i++) 
        {
            arrayOfTexCoords[i] = m_TexCoords[multiTextureIndex].get(i);
        }

        return (arrayOfTexCoords);
    }

    public int[] getIndices()
    {
        int[] arrayOfIndices = new int[m_TriangleIndices.size()];

        for (int i = 0; i < m_TriangleIndices.size(); i++) 
        {
            arrayOfIndices[i] = (m_TriangleIndices.get(i));
        }

        return (arrayOfIndices);
    }
    
}
