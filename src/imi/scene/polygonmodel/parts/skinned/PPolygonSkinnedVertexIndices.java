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
package imi.scene.polygonmodel.parts.skinned;

import imi.scene.polygonmodel.parts.polygon.PPolygonVertexIndices;
import java.io.Serializable;

/**
 *
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class PPolygonSkinnedVertexIndices  extends PPolygonVertexIndices implements Serializable
{
    public int          m_BoneWeightIndex;  // Index into the parent mesh's bone weight collection
    public int          m_BoneIndicesIndex; // Index into the parent mesh's bone indices collection
    
    // Constructor
    public PPolygonSkinnedVertexIndices()
    {
        super();
        // Set up some defaults
        m_BoneIndicesIndex = -1;
        m_BoneWeightIndex  = -1;
    }
    
    public PPolygonSkinnedVertexIndices(PPolygonVertexIndices RHS)
    {
        super(RHS);
        // Set up some defaults
        m_BoneIndicesIndex = -1;
        m_BoneWeightIndex  = -1;
    }
    
    // Copy Constructor
    public PPolygonSkinnedVertexIndices(PPolygonSkinnedVertexIndices RHS)
    {
        m_PositionIndex = RHS.m_PositionIndex;
        m_NormalIndex = RHS.m_NormalIndex;
        m_ColorIndex = RHS.m_ColorIndex;
        for (int i = 0; i < 8; i++)
            m_TexCoordIndex[i] = RHS.m_TexCoordIndex[i];
        // Copy skinning data
        m_BoneWeightIndex = RHS.m_BoneWeightIndex;
        m_BoneIndicesIndex = RHS.m_BoneIndicesIndex;
    }
  
    /**
     * Set all the data members in one fell swoop!
     * @param PositionIndex
     * @param NormalIndex
     * @param ColorIndex
     * @param TexCoordIndices This array can be of any size, only the first 8 entries will be used
     * @param BoneWeightIndex
     * @param BoneInfluenceIndex
     */
    public void set(int PositionIndex, int NormalIndex, int ColorIndex, int[] TexCoordIndices, int BoneWeightIndex, int BoneInfluenceIndex)
    {
        m_PositionIndex = PositionIndex;
        m_NormalIndex = NormalIndex;
        m_ColorIndex = ColorIndex;
        // Tex coords
        for (int i = 0; (i < TexCoordIndices.length) && (i<8); ++i)
            m_TexCoordIndex[i] = TexCoordIndices[i];
        m_BoneWeightIndex = BoneWeightIndex;
        m_BoneIndicesIndex = BoneInfluenceIndex;
    }


}
