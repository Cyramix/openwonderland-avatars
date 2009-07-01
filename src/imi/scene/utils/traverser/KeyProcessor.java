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

import com.jme.math.Vector3f;
import imi.scene.PJoint;
import imi.scene.PNode;
import javolution.util.FastTable;

/**
 * ScaleResetProcessor Class
 * @author Viet Nguyen Truong
 */
public class KeyProcessor implements NodeProcessor {

    // Class Data Members
    private FastTable<String> keys = new FastTable<String>();
    private FastTable<Vector3f> values = new FastTable<Vector3f>();
    
    // Accessors
    public FastTable<String> getKeys() { return keys; }
    public FastTable<Vector3f> getValues() { return values; }
    
    /**
     * Gets the name of the node to be used as a key when pairing scaling data
     * @param currentNode (PNode)
     * @return boolean (not really used in this implementation)
     */
    public boolean processNode(PNode currentNode) {
        if (currentNode instanceof PJoint)
        {
            keys.add(currentNode.getName());
            Vector3f scale = new Vector3f();
            ((PJoint)currentNode).getLocalModifierMatrix().getScale(scale);
            values.add(scale);
            return true;
        }
        return false;
    }

}
