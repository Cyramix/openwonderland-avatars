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
package imi.scene.polygonmodel.parts.polygon;


public class PPolygonVertexIndices
{
    public int          m_PositionIndex;
    public int          m_NormalIndex;
    public int          m_ColorIndex;
    public int          []m_TexCoordIndex = new int[8];

    
    //  Constructor.
    public PPolygonVertexIndices()
    {
        for (int i = 0; i < 8; i++)
            m_TexCoordIndex[i] = -1;
    }
    
    // Copy Constructor
    public PPolygonVertexIndices(PPolygonVertexIndices RHS)
    {
        m_PositionIndex = RHS.m_PositionIndex;
        m_NormalIndex   = RHS.m_NormalIndex;
        m_ColorIndex    = RHS.m_ColorIndex;
        for (int i = 0; i < 8; i++)
            m_TexCoordIndex[i] = RHS.m_TexCoordIndex[i];
    }
    

    
    /**
     * 
     * @param PositionIndex
     * @param NormalIndex
     * @param TexCoord1Index
     * @param TexCoord2Index
     * @param TexCoord3Index
     * @param TexCoord4Index
     */
    public void initialize(int PositionIndex,
                           int NormalIndex,
                           int TexCoord1Index,
                           int TexCoord2Index,
                           int TexCoord3Index,
                           int TexCoord4Index)
    {
        m_PositionIndex = PositionIndex;
        m_ColorIndex = -1;
        m_NormalIndex = NormalIndex;
        m_TexCoordIndex[0] = TexCoord1Index;
        m_TexCoordIndex[1] = TexCoord2Index;
        m_TexCoordIndex[2] = TexCoord3Index;
        m_TexCoordIndex[3] = TexCoord4Index;
    }
    
    /**
     * Overload to account for color indices
     * @param PositionIndex
     * @param ColorIndex
     * @param NormalIndex
     * @param TexCoord1Index
     * @param TexCoord2Index
     * @param TexCoord3Index
     * @param TexCoord4Index
     */
    public void initialize(int PositionIndex,
                           int ColorIndex,
                           int NormalIndex,
                           int TexCoord1Index,
                           int TexCoord2Index,
                           int TexCoord3Index,
                           int TexCoord4Index)
    {
        m_PositionIndex = PositionIndex;
        m_ColorIndex = ColorIndex;
        m_NormalIndex = NormalIndex;
        m_TexCoordIndex[0] = TexCoord1Index;
        m_TexCoordIndex[1] = TexCoord2Index;
        m_TexCoordIndex[2] = TexCoord3Index;
        m_TexCoordIndex[3] = TexCoord4Index;
    }
    
     /**
      * Overload for color index and additional texture
      * coordinates
      * @param PositionIndex
      * @param ColorIndex
      * @param NormalIndex
      * @param TexCoord1Index
      * @param TexCoord2Index
      * @param TexCoord3Index
      * @param TexCoord4Index
      * @param TexCoord5Index
      * @param TexCoord6Index
      * @param TexCoord7Index
      * @param TexCoord8Index
      */
    public void initialize(int PositionIndex,
                           int ColorIndex,
                           int NormalIndex,
                           int TexCoord1Index,
                           int TexCoord2Index,
                           int TexCoord3Index,
                           int TexCoord4Index,
                           int TexCoord5Index,
                           int TexCoord6Index,
                           int TexCoord7Index,
                           int TexCoord8Index)
    {
        m_PositionIndex = PositionIndex;
        m_ColorIndex = ColorIndex;
        m_NormalIndex = NormalIndex;
        m_TexCoordIndex[0] = TexCoord1Index;
        m_TexCoordIndex[1] = TexCoord2Index;
        m_TexCoordIndex[2] = TexCoord3Index;
        m_TexCoordIndex[3] = TexCoord4Index;
        m_TexCoordIndex[4] = TexCoord5Index;
        m_TexCoordIndex[5] = TexCoord6Index;
        m_TexCoordIndex[6] = TexCoord7Index;
        m_TexCoordIndex[7] = TexCoord8Index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PPolygonVertexIndices other = (PPolygonVertexIndices) obj;
        if (this.m_PositionIndex != other.m_PositionIndex) {
            return false;
        }
        if (this.m_NormalIndex != other.m_NormalIndex) {
            return false;
        }
        if (this.m_ColorIndex != other.m_ColorIndex) {
            return false;
        }
        if (this.m_TexCoordIndex != other.m_TexCoordIndex && (this.m_TexCoordIndex == null || !texCoordEquals(other.m_TexCoordIndex))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.m_PositionIndex;
        hash = 89 * hash + this.m_ColorIndex;
        for(int i = 0; i < m_TexCoordIndex.length; i++)
            hash = 89 * hash + m_TexCoordIndex[i];
        //hash = 89 * hash + (this.m_TexCoordIndex != null ? this.m_TexCoordIndex.hashCode() : 0);
        return hash;
    }

    private boolean texCoordEquals(int[] OtherTexCoordIndex) 
    {
        boolean result = true;
        
        for (int i = 0; i < 8; i++)
        {
            if (m_TexCoordIndex[i] != OtherTexCoordIndex[i])
                result = false;
        } 
        
        return result;
    }


}




