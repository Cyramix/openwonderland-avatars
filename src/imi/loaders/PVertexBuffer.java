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
package imi.loaders;

import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.scene.polygonmodel.parts.PGeometryVertex;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Ronald E Dahlgren
 */
public class PVertexBuffer {
    // TODO: Use a better data structure; check out javalution (FastList)
    private ArrayList<PGeometryVertex> m_Vertices = new ArrayList<PGeometryVertex>(); // The master list of vertices
    
    /**
     * Attempts to add a vertex to the master list.
     * If a duplicate vertex is found, its index is
     * returned, otherwise the vert is added to the
     * end of the list and the new index is returned.
     * @param newVert The vertex to add
     * @return The index of the vertex within the master list
     */
    public int AddVertex(PGeometryVertex newVert)
    {
        // Is the vert present?
        int index = m_Vertices.indexOf(newVert);
        if (index < 0)
        {
            m_Vertices.add(newVert);
            index = m_Vertices.size() - 1;
        }
        return index;
    }
    
    /**
     * Retrieves the specified vertex. Returns null
     * if the requested index is out of bounds
     * @param index Index of the desired vertex
     * @return The vertex
     */
    public PGeometryVertex GetVertex(int index)
    {
        if (index >= m_Vertices.size() || index < 0)
            return null;
        return m_Vertices.get(index);
    }
    
    /**
     * Removes a vertex from the master list
     * @param theVert The vertex to test against for removal
     * @return True is the vertex was found, false otherwise
     */
    public boolean RemoveVertex(PGeometryVertex theVert)
    {
        return m_Vertices.remove(theVert);
    }
    
    /**
     * Searches for the specified vertex
     * @param theVert The vertex to search for
     * @return The index of the vertex is found, -1 otherwise
     */
    public int FindVertex(PGeometryVertex theVert)
    {
        for (int i = 0; i < m_Vertices.size(); ++i)
        {
            if (m_Vertices.get(i).hashCode() == theVert.hashCode())
                return i;
        }
        // Otherwise, no match
        return -1;
    }
    
    /**
     * Determine the number of vertices
     * Same as <code>Count</code>
     * @return The number of vertices contained
     */
    public int size()
    {
        return m_Vertices.size();
    }
    
    /**
     * Determine the number of vertices.
     * Same as <code>size</code>
     * @return The number of vertices contained
     */
    public int Count()
    {
        return m_Vertices.size();
    }
    /**
     * Clears out the master list
     */
    public void Clear()
    {
        m_Vertices.clear();
    }
    
    public Vector3f[] GetPositionArray()
    {
        Vector3f[] result = new Vector3f[m_Vertices.size()];
        for (int i = 0; i < m_Vertices.size(); ++i)
        {
            result[i] = m_Vertices.get(i).m_Position;
        }
        return result;
    }
    
    public Vector3f[] GetNormalArray()
    {
        
        Vector3f[] result = new Vector3f[m_Vertices.size()];
        for (int i = 0; i < m_Vertices.size(); ++i)
        {
            result[i] = m_Vertices.get(i).m_Normal;
        }
        return result;
    }
    public Vector3f[] GetTangentArray()
    {
        
        Vector3f[] result = new Vector3f[m_Vertices.size()];
        for (int i = 0; i < m_Vertices.size(); ++i)
        {
            result[i] = m_Vertices.get(i).m_Tangent;
        }
        return result;
    }
    
    public ColorRGBA[] GetColorArray()
    {
        
        ColorRGBA[] result = new ColorRGBA[m_Vertices.size()];
        for (int i = 0; i < m_Vertices.size(); ++i)
        {
           result[i] = new ColorRGBA(m_Vertices.get(i).m_Diffuse);
        }
        return result;
    }
    
    /**
     * Retrieves the texture coordinates for the specified
     * texture target.
     * @param nTextureTarget The texture target to retieve
     * @return The array
     */
    public Vector2f[] GetTextureCoordinateArray(int nTextureTarget)
    {
        Vector2f[] result = new Vector2f[m_Vertices.size()]; // Allocate space for the array
        for (int i = 0; i < m_Vertices.size(); i++)
        {
                result[i] = new Vector2f(m_Vertices.get(i).m_TexCoords[nTextureTarget]);
        }
        return result;
    }
    
    /**
     * Returns the texture coordinates for target 0
     * @return The texture coordinates
     */
    public Vector2f[] GetTextureCoordinateArray()
    {
        return GetTextureCoordinateArray(0);
    }
}
