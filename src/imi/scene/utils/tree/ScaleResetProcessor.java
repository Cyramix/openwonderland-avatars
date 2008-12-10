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

import imi.scene.PJoint;
import imi.scene.PNode;
import com.jme.math.Vector3f;

/**
 * ScaleResetProcessor Class
 * @author Viet Nguyen Truong
 */
public class ScaleResetProcessor implements NodeProcessor {
    /**
     * Sets the scaling for each bone/joint back to 1
     * @param currentNode (PNode)
     * @return boolean
     */
    public boolean processNode(PNode currentNode) {
        if(!(currentNode instanceof PJoint))
            return false;
        ((PJoint)currentNode).getLocalModifierMatrix().setScale(new Vector3f(1.0f, 1.0f, 1.0f));
        return true;
    }    
}
