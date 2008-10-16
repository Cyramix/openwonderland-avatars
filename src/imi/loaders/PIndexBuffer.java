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

import java.util.ArrayList;

/**
 *
 * @author Ronald E Dahlgren
 */
public class PIndexBuffer {
    //public int [] m_Indices; // The list of indices
    public ArrayList<Integer> m_Indices;
    // Constructor
    public PIndexBuffer()
    {
        m_Indices = new ArrayList<Integer>();
    }
    
    /**
     * Clears out the index buffer
     */
    public void Clear()
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
    
    public int[] GetArray()
    {
        int[] result = new int[m_Indices.size()];
        for (int i = 0; i < m_Indices.size(); ++i)
            result[i] = m_Indices.get(i);
        return result;
    }
    
    public void Add(int nNewIndexEntry)
    {
        m_Indices.add((Integer)nNewIndexEntry);
    }
    

}
