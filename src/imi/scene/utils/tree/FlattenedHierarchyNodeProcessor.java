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

import com.jme.math.Matrix4f;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.util.ArrayList;

/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class FlattenedHierarchyNodeProcessor  implements NodeProcessor
{
    ArrayList<PMatrix> m_MatrixStack      = null;
    boolean m_bCalculateInverseTransforms = false;

    /**
     * Constructs a brand new heirarchy flattener! Tweak <code>initialMatrixStackSize</code>
     * in order to achieve some performance gains (avoid unnecessary resizing).
     * @param initialMatrixStackSize
     */
    public FlattenedHierarchyNodeProcessor(int initialMatrixStackSize, boolean bCalculateInverseTransforms)
    {
        assert(initialMatrixStackSize > 0) : "initialMatrixStackSize is bigger than 0!";
        m_MatrixStack = new ArrayList<PMatrix>(initialMatrixStackSize);
        m_bCalculateInverseTransforms = bCalculateInverseTransforms;
    }

    public void clear()
    {
        m_MatrixStack.clear();
        m_bCalculateInverseTransforms = false;
    }

    public boolean isCalculatingInverseTransforms()
    {
        return m_bCalculateInverseTransforms;
    }

    public void setCalculateInverseTransforms(boolean bCalculateInverses)
    {
        m_bCalculateInverseTransforms = bCalculateInverses;
    }

    /**
     * Flattens the heirarchy and accumalates global transforms into
     * the matrix stack data member
     * @param current The currente node
     * @return true/false (boolean)
     */
    public boolean processNode(PNode current) 
    {
        PNode parent  = current.getParent();

        // Get the parent's world matrix
        PTransform parentTransform = null;
        if (parent == null)
            parentTransform = new PTransform();
        else
            parentTransform = parent.getTransform();
        
        // If we have a parent without a transform prune the branch!
        if (parentTransform == null)
            return false;

        if (current.getTransform() != null)
        {
            // Build the world matrix for the current instance
            if (current.getTransform().isDirtyWorldMat() || current.isDirty())
            {
                current.getTransform().buildWorldMatrix(parentTransform.getWorldMatrix(false));
                // Now we are clean!
                current.setDirty(false, false);
            }
            
            // handle mesh space case
            if (current instanceof SkinnedMeshJoint)
            {
                if (parent instanceof SkinnedMeshJoint)
                {
                    PMatrix meshSpace = ((SkinnedMeshJoint)current).getMeshSpace();
                    meshSpace.mul(((SkinnedMeshJoint)parent).getMeshSpace(), current.getTransform().getLocalMatrix(false));
                }
                else
                    ((SkinnedMeshJoint)current).setMeshSpace(current.getTransform().getLocalMatrix(false));
            }
            if (m_bCalculateInverseTransforms)
                m_MatrixStack.add(current.getTransform().getWorldMatrix(false).getInversedPMatrix());
            else
                m_MatrixStack.add(current.getTransform().getWorldMatrix(false));
        }
        return true;
    }

    public Matrix4f[] computeMatrix4fArray()
    {
        Matrix4f [] result = new Matrix4f[m_MatrixStack.size()];
        for (int i = 0; i < m_MatrixStack.size(); ++i)
            result[i] = m_MatrixStack.get(i).getMatrix4f();
        return result;
    }

    public PMatrix[] getPMatrixArray()
    {
        PMatrix [] result = new PMatrix[m_MatrixStack.size()];
        return m_MatrixStack.toArray(result);
    }
}
