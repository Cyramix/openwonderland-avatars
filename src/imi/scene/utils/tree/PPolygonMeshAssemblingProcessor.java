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

import imi.loaders.PPolygonTriMeshAssembler;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;

/**
 * This class submits the geometry if it comes across a PPolygonMesh or
 * one of its derivatives.
 * @author Ronald E Dahlgren
 */
public class PPolygonMeshAssemblingProcessor implements NodeProcessor 
{
    public PPolygonMeshAssemblingProcessor()
    {
        // do nothing
    }
    
    public boolean processNode(PNode currentNode) {
        if (currentNode instanceof PPolygonMesh)
            ((PPolygonMesh)currentNode).submit(new PPolygonTriMeshAssembler());
        else if (currentNode instanceof PPolygonMeshInstance)
            ((PPolygonMeshInstance)currentNode).getGeometry().submit(new PPolygonTriMeshAssembler());
        // return true either way
        return true;
    }

}
