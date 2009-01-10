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

import com.jme.scene.SharedMesh;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import java.util.ArrayList;

/**
 * This processor will flatten all transforms on the hierarchy and gather up
 * SharedMesh objects along the way.
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class PSceneSubmitHelper  implements NodeProcessor
{
    ArrayList<SharedMesh> m_sharedMeshCollection    = new ArrayList<SharedMesh>(); // Collection for gathering meshes

    /**
     * Constructs a brand new flattener!
     */
    public PSceneSubmitHelper()
    {
    }

    public void clear()
    {
        m_sharedMeshCollection.clear();
    }


    /**
     * Flattens the heirarchy and accumulates SharedMesh objects
     * @param current The current node
     * @return true to continue, false to prune
     */
    public boolean processNode(PNode current) 
    {
        if (current.getRenderStop() == true)
            return false;
        PNode parent = current.getParent();

        // Get the parent's world matrix
        PTransform parentTransform = null;
        if (parent == null)
            parentTransform = new PTransform();
        else
            parentTransform = parent.getTransform();

        // If we have a parent without a transform prune the branch!
        if (parentTransform == null)
        {
            parentTransform = new PTransform();
//            // Before we prune... check to see if this is the one special case
//            if (current.getName().equals("m_Instances"))
//                return true;
//            return false;
        }

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
                SkinnedMeshJoint currentSkinnedMeshJoint = ((SkinnedMeshJoint)current);
                
                if (parent instanceof SkinnedMeshJoint)
                {
                    PMatrix meshSpace = currentSkinnedMeshJoint.getMeshSpace();
                    meshSpace.mul(((SkinnedMeshJoint)parent).getMeshSpace(), current.getTransform().getLocalMatrix(false));
                }
                else
                    currentSkinnedMeshJoint.setMeshSpace(current.getTransform().getLocalMatrix(false));
            }
        }
        if (current instanceof PPolygonMeshInstance)
        {
            // ensure we have indices
            if (((PPolygonMeshInstance)current).getGeometry().getGeometry().getMaxIndex() >= 0) // If no indices, don't attach this mesh.
                m_sharedMeshCollection.add(((PPolygonMeshInstance)current).updateSharedMesh());
        }

        // Special case for skeleton node
        if (current instanceof SkeletonNode)
        {
            m_sharedMeshCollection.addAll(((SkeletonNode)current).collectSharedMeshes());
            return false;
        }
        
        return true;
    }

    public ArrayList<SharedMesh> getSharedMeshes()
    {
        return m_sharedMeshCollection;
    }
}
