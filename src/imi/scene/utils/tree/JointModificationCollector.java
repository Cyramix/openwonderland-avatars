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

import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.serialization.xml.bindings.xmlJointModification;
import imi.serialization.xml.bindings.xmlMatrix;
import java.util.ArrayList;
import java.util.List;

/**
 * This class traverses a given tree and 
 * accumulates the local modifier matrices.
 * @author Ronald E Dahlgren
 */
public class JointModificationCollector implements NodeProcessor
{
    // Accumulate herein!
    private ArrayList<xmlJointModification> modifiers = new ArrayList<xmlJointModification>();
    
    public JointModificationCollector()
    {
        // Do nothing special currently
    }

    public boolean processNode(PNode currentNode) 
    {
        if ((currentNode instanceof SkinnedMeshJoint) == false)
            return false;
        SkinnedMeshJoint joint = (SkinnedMeshJoint) currentNode;
        if ((joint.getLocalModifierMatrix() != null && joint.getLocalModifierMatrix().equals(PMatrix.IDENTITY) == false) ||
            (joint.getSkeletonModifier() != null && joint.getSkeletonModifier().equals(PMatrix.IDENTITY) == false)) // Contains data
        {
            xmlJointModification jointMod = new xmlJointModification();
            // first write the name
            jointMod.setTargetJointName(joint.getName());
            // Local modifier matrix
            PMatrix mat = joint.getLocalModifierMatrix();
            xmlMatrix xmlMat = new xmlMatrix();
            if (mat != null && mat.equals(PMatrix.IDENTITY) == false) // data worth storing
            {
                xmlMat.set(mat);
                jointMod.setLocalModifierMatrix(xmlMat);
            }
            // Skeleton modifier matrix
            mat = joint.getSkeletonModifier();
            xmlMat = new xmlMatrix();
            if (mat != null && mat.equals(PMatrix.IDENTITY) == false) // useful data
            {
                xmlMat.set(mat);
                jointMod.setSkeletonModifierMatrix(xmlMat);
            }
            modifiers.add(jointMod);
        }
        return true;
    }

    public List<xmlJointModification> getModifierList()
    {
        return modifiers;
    }

    public void clearList()
    {
        modifiers.clear();
    }

}
