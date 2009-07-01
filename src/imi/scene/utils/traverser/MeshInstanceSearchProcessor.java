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
package imi.scene.utils.traverser;

import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;

/**
 *
 * @author ptruong
 */
public class MeshInstanceSearchProcessor implements NodeProcessor {
    java.util.Vector<PPolygonMeshInstance> meshInstances = null;

    public void setProcessor() { meshInstances = new java.util.Vector<PPolygonMeshInstance>(); }
    
    public java.util.Vector<PPolygonMeshInstance> getNodeMeshInstances() { return meshInstances; }
    
    public java.util.Vector<PPolygonMeshInstance> getMeshInstances() {
        java.util.Vector<PPolygonMeshInstance> meshInst = new java.util.Vector<PPolygonMeshInstance>();
        for(int i = 0; i < meshInstances.size(); i++) {
            meshInst.add( ((PPolygonMeshInstance)meshInstances.get(i)) );
        }
        return meshInst;
    }
    
    public boolean processNode(PNode currentNode) {
        if(currentNode instanceof PPolygonMeshInstance)
            meshInstances.add((PPolygonMeshInstance)currentNode);
        return true;
    }

}
