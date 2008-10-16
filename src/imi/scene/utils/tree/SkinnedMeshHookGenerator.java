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

import imi.loaders.scenebindings.sbBaseNode;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ronald E Dahlgren
 */
public class SkinnedMeshHookGenerator  implements NodeProcessor
{

    ArrayList<sbBaseNode> foundAttachments = new ArrayList<sbBaseNode>();
    PPolygonModelInstance bufferInst = new PPolygonModelInstance("Buffer instance");

    public SkinnedMeshHookGenerator()
    {
    
    }
    public boolean processNode(PNode currentNode) 
    {
        // is this not a skinned mesh joint?
        if (currentNode instanceof SkinnedMeshJoint == false) // something weird
        {
            sbBaseNode baseNode = bufferInst.load_buildSceneBindings(currentNode);

            baseNode.setName(new String(currentNode.getName()));  
            baseNode.setParentName(new String(currentNode.getParent().getName()));
            foundAttachments.add(baseNode);
            return false; // we already processed the kids, prune this branch
        }
        return true;
    }

    // accessor to get the data out
    public List<sbBaseNode> getAttachments()
    {
        return foundAttachments;
    }
}
