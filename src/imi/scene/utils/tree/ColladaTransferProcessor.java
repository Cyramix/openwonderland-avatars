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
package imi.scene.utils.tree;

import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import java.util.ArrayList;
import java.util.List;

/**
 * Assists in relocating meshes from one pscene to another
 * @author Ronald E Dahlgren
 */
public class ColladaTransferProcessor implements NodeProcessor
{
    /** Accumulation point **/
    private ArrayList<Boolean>  m_textureStates = new ArrayList<Boolean>();
    /** The new pscene to use **/
    private PScene              m_newPScene = null;
    
    /**
     * Default c-tor
     */
    public ColladaTransferProcessor(PScene newOwningPScene)
    {
        m_newPScene = newOwningPScene;
    }
    
    /**
     * Process!
     * @param currentNode
     * @return
     */
    public boolean processNode(PNode currentNode)
    {
        if (currentNode instanceof PPolygonMeshInstance)
        {
            PPolygonMeshInstance meshInst = (PPolygonMeshInstance)currentNode;
            meshInst.setPScene(m_newPScene);
            meshInst.setUseGeometryMaterial(meshInst.isUseGeometryMaterial());
        }
        return true;
    }
    
    public void clear()
    {
        m_textureStates.clear();
    }

    public List<Boolean> getTextureStatusList()
    {
        return m_textureStates;
    }
}
