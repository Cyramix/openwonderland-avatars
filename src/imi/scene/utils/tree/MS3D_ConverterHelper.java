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
package imi.scene.utils.tree;

import imi.scene.PNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class MS3D_ConverterHelper implements NodeProcessor 
{

    private HashMap<String, Integer> m_indexMap = new HashMap<String, Integer>();
    private int m_currentBFTIndex = 0; // The current index for the node

    public boolean processNode(PNode currentNode) 
    {
        // If not the proper type, prune branch, do not increment BFT index
        if (!(currentNode instanceof SkinnedMeshJoint)) 
            return false;
        
        SkinnedMeshJoint joint = (SkinnedMeshJoint) currentNode;
        
        // Save the index mapping
        m_indexMap.put(joint.getName(), m_currentBFTIndex);

        m_currentBFTIndex++;
        return true;
    }
    
    public HashMap<String, Integer> getMapping()
    {
        return m_indexMap;
    }
}
