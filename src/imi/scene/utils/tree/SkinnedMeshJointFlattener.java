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
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import java.util.ArrayList;
import javolution.util.FastList;

/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class SkinnedMeshJointFlattener  implements NodeProcessor
{
    private ArrayList<PMatrix> m_MatrixStack      = null;
    private boolean m_bCalculateInverseTransforms = false;
    
    /**
     * Constructs a brand new heirarchy flattener! Tweak <code>initialMatrixStackSize</code>
     * in order to achieve some performance gains (avoid unnecessary resizing).
     * @param initialMatrixStackSize
     */
    public SkinnedMeshJointFlattener(int initialMatrixStackSize, boolean bCalculateInverseTransforms)
    {
        assert(initialMatrixStackSize > 0) : "initialMatrixStackSize is less than 0!";
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
     * @return boolean
     */
    public boolean processNode(PNode current) 
    {
        if (!(current instanceof SkinnedMeshJoint))
            return false;
   
        PNode parent  = current.getParent();

        // Get the parent's world matrix
        PTransform parentTransform = null;
        if (parent != null)
        {
            parentTransform = parent.getTransform();
            
            if (parent instanceof SkinnedMeshJoint)
            {
                PMatrix meshSpace = ((SkinnedMeshJoint)current).getMeshSpace();
                meshSpace.mul(((SkinnedMeshJoint)parent).getMeshSpace(), current.getTransform().getLocalMatrix(false));
            }
            else
                ((SkinnedMeshJoint)current).setMeshSpace(current.getTransform().getLocalMatrix(false));
            
            PMatrix stackAdd = new PMatrix(((SkinnedMeshJoint)current).getMeshSpace());
            
            if (m_bCalculateInverseTransforms)
                m_MatrixStack.add(stackAdd.getInversedPMatrix());
            else
            {
                stackAdd.mul(((SkinnedMeshJoint)current).getLocalModifierMatrix());
                m_MatrixStack.add(stackAdd);
            }
        }
        else
            parentTransform = new PTransform();
        
        // If we have a parent without a transform prune the branch!
        if (parentTransform == null)
            return false;

        // Flatening the hierarchy
        if (current.getTransform() != null)
        {
            // Build the world matrix for the current instance
            if (current.getTransform().isDirtyWorldMat() || current.isDirty())
            {
                current.getTransform().buildWorldMatrix(parentTransform.getWorldMatrix(false));
                //current.getTransform().getLocalMatrix(true).setScale(((PJoint)current).getScale());
                
                // Now we are clean!
                current.setDirty(false, false);
            }
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
