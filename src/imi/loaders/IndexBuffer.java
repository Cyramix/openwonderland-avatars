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

import java.util.ArrayList;

/**
 * Wrap a list of integers. This is used along with the PGeometryVertexBuffer
 * class to assist in vert and index buffer creation.
 * @see PGeometryVertexBuffer
 * @author Ronald E Dahlgren
 */
public class IndexBuffer {

    public final ArrayList<Integer> m_Indices = new ArrayList<Integer>();

    /**
     * Construct a new instance
     */
    public IndexBuffer()
    {
    }
    
    /**
     * Clears out the index buffer
     */
    public void clear()
    {
        m_Indices.clear();
    }
    
    /**
     * Returns the number of entries in the index buffer
     * @return Number of entries.
     */
    public int size()
    {
        return m_Indices.size();
    }
    
    public int[] getArray()
    {
        int[] result = new int[m_Indices.size()];
        for (int i = 0; i < m_Indices.size(); ++i)
            result[i] = m_Indices.get(i);
        return result;
    }
    
    public void add(int nNewIndexEntry)
    {
        m_Indices.add((Integer)nNewIndexEntry);
    }
    

}
