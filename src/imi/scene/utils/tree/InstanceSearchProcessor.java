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
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 * Searches through the PScene to find all modelinstances
 * @author  Paul Viet Nguyen Truong
 */
public class InstanceSearchProcessor implements NodeProcessor {
    // Class Data Members
    private java.util.Vector<PNode> modelInstances = null;

    public void setProcessor() { modelInstances = new java.util.Vector<PNode>(); }
    
    public java.util.Vector<PNode> getModelInstances() { return modelInstances; }
    
    public boolean processNode(PNode currentNode) {
        if(currentNode instanceof PPolygonModelInstance)
            modelInstances.add(currentNode);            
        return true;
    }
}
