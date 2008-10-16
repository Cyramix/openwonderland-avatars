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

import com.jme.math.Vector3f;
import imi.scene.PNode;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;

/**
 *
 * @author ptruong
 */
public class JointScaleProcessor implements NodeProcessor {
    Vector3f vscale = new Vector3f();
    
    public Vector3f getScale() { return vscale; }
    public void setScale(Vector3f scale) { vscale = scale; }
    
    public boolean processNode(PNode currentNode) {
        ((SkinnedMeshJoint)currentNode).getLocalModifierMatrix().setScale(vscale);
        return true;
    }

}
