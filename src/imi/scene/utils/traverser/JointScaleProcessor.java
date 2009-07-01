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
import imi.scene.PNode;
import imi.scene.SkinnedMeshJoint;

/**
 * This processor applies a scale vector to all of the nodes it visits.
 * @author ptruong
 */
public class JointScaleProcessor implements NodeProcessor {
    /** Used in scale calculations **/
    private final Vector3f vscale;
    
    /**
     * Create a new joint scale processor with the specified scaling vector.
     * @param scale A non-null scale vector
     */
    public JointScaleProcessor(Vector3f scale) {
        if (scale == null)
            throw new IllegalArgumentException("Null scale vector provided!");
        vscale = scale;
    }

    /**
     * Get the current value of the scale vector.
     * @param vOut A non-null storage object.
     * @throws NullPointerException If {@code vOut == null}
     */
    public void getScale(Vector3f vOut) {
        vOut.set(vscale);
    }

    /**
     * Applies a scale vector to each node it visits.
     * {@inheritDoc NodeProcessor}
     */
    public boolean processNode(PNode currentNode) {
        ((SkinnedMeshJoint)currentNode).getLocalModifierMatrix().setScale(vscale);
        return true;
    }

}
