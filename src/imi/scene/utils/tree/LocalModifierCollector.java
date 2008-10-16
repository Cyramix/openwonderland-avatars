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

import imi.loaders.scenebindings.sbFloatRow;
import imi.loaders.scenebindings.sbLocalModifier;
import imi.loaders.scenebindings.sbMatrix;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.util.ArrayList;
import org.lwjgl.util.vector.Vector4f;

/**
 * This class traverses a given tree and 
 * accumulates the local modifier matrices.
 * @author Ronald E Dahlgren
 */
public class LocalModifierCollector implements NodeProcessor
{
    // Accumulate herein!
    private ArrayList<sbLocalModifier> modifiers = new ArrayList<sbLocalModifier>();
    
    public LocalModifierCollector()
    {
        // Do nothing special currently
    }

    public boolean processNode(PNode currentNode) 
    {
        Vector4f matrixRow = null;
        if ((currentNode instanceof SkinnedMeshJoint) == false)
            return false;
        SkinnedMeshJoint joint = (SkinnedMeshJoint) currentNode;
        if (joint.getLocalModifierMatrix().equals(PMatrix.IDENTITY) == false) // Contains data
        {
            // create an sbLocalModifier
            sbLocalModifier mod = new sbLocalModifier();
            // fill it out
            mod.setTargetJointName(joint.getName());
            mod.setTransform(new sbMatrix());
            // Row 0
            matrixRow = joint.getLocalModifierMatrix().getRow(0);
            mod.getTransform().setRow0(new sbFloatRow());
            mod.getTransform().getRow0().set(matrixRow.x, matrixRow.y, matrixRow.z, matrixRow.w);
            // Row 1
            matrixRow = joint.getLocalModifierMatrix().getRow(1);
            mod.getTransform().setRow1(new sbFloatRow());
            mod.getTransform().getRow1().set(matrixRow.x, matrixRow.y, matrixRow.z, matrixRow.w);
            // Row 2
            matrixRow = joint.getLocalModifierMatrix().getRow(2);
            mod.getTransform().setRow2(new sbFloatRow());
            mod.getTransform().getRow2().set(matrixRow.x, matrixRow.y, matrixRow.z, matrixRow.w);
            // Row 3
            matrixRow = joint.getLocalModifierMatrix().getRow(3);
            mod.getTransform().setRow3(new sbFloatRow());
            mod.getTransform().getRow3().set(matrixRow.x, matrixRow.y, matrixRow.z, matrixRow.w);
            
            // add it to the list
            modifiers.add(mod);
        }
        return true;
    }

    public ArrayList<sbLocalModifier> getModifierList()
    {
        return modifiers;
    }

}
