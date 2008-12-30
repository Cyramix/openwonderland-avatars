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
package imi.scene.polygonmodel.skinned;

import com.jme.scene.SharedMesh;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.ShaderProperty;
import imi.scene.utils.PRenderer;


/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class PPolygonSkinnedMeshInstance extends PPolygonMeshInstance
{
    protected boolean bInit = true;
    
    protected PMatrix[]    m_InverseBindPose  = null; // TODO: Caching this may not be desireable
    
    protected SkeletonNode m_skeletonNode    = null;
    
    protected int[]        m_influenceIndices = null;

    //private PostAnimationJointManipulator m_jointManipulator = null;

    //  Constructor.
    public PPolygonSkinnedMeshInstance(String name, PPolygonSkinnedMesh geometry, PMatrix origin, PScene pscene, boolean applyMaterial)
    {
        super(name, geometry, origin, pscene, applyMaterial);
        if (geometry.getInfluenceIndices() != null)
            setInfluenceIndices(geometry.getInfluenceIndices());
        
    }
    
    //  Copy Constructor.
    public PPolygonSkinnedMeshInstance(PPolygonSkinnedMeshInstance meshInstance, PScene pscene, boolean applyMaterial)
    {
        super(meshInstance.getName(), meshInstance.getGeometry(), meshInstance.getTransform().getLocalMatrix(false), pscene, applyMaterial);
        if (meshInstance.getInfluenceIndices() != null)
            setInfluenceIndices(meshInstance.getInfluenceIndices());
        setAndLinkSkeletonNode(meshInstance.getSkeletonNode());
    }
    
        
    //  Gets the SkeletonNode.
    public SkeletonNode getSkeletonNode()
    {
        return(m_skeletonNode);
    }

    public void setInverseBindPose(PMatrix[] newInverseBindPose) {
        m_InverseBindPose = newInverseBindPose;
    }
    
    public void setAndLinkSkeletonNode(SkeletonNode skeleton)
    {
        m_skeletonNode = skeleton;
        linkJointsToSkeletonNode();
    }

    @Override
    public void draw(PRenderer renderer) {
        if (m_geometry != null) {
            // Set world origin
            PMatrix origin = getTransform().getWorldMatrix(false);
            renderer.setOrigin(origin);

            // Draw geometry if it is ready
            if (m_skeletonNode != null)
                ((PPolygonSkinnedMesh) m_geometry).draw(renderer, m_skeletonNode.getSkeletonRoot());
        }


        // TODO is this needed? .... draw mesh kids - mesh that belongs to a model...
        for (int i = 0; i < getChildrenCount(); i++) {
            getChild(i).drawAll(renderer);
        }
    }

    /**
     * TODO : move this method somewhere else
     * @param pNode
     * @return true or false (boolean)
     */
    public boolean containsPolygonMeshInstance(PNode pNode)
    {
        if (pNode instanceof PPolygonMeshInstance)
        {
            if (!(pNode instanceof PPolygonSkinnedMeshInstance))
                return(true);
        }
        
        for (int a=0; a<pNode.getChildrenCount(); a++)
        {
            if (containsPolygonMeshInstance(pNode.getChild(a)))
                return(true);
        }
        
        return(false);
    }

    private void linkJointsToSkeletonNode()
    {
        int jointIndex = -1;
        PPolygonSkinnedMesh skinnedGeometry = (PPolygonSkinnedMesh)m_geometry;
        int[] influenceIndices = new int[skinnedGeometry.getJointNameCount()];

        int counter = 0;
        for (String jointName : skinnedGeometry.getJointNames())
        {
            jointIndex = m_skeletonNode.getSkinnedMeshJointIndex(jointName);
            if (jointIndex == -1) // Not found!
            {
                logger.severe("Joint not found for influence #" + counter + ", name: " + jointName + " - Skipping...");
                continue;
            }
            else
                influenceIndices[counter] = jointIndex;
            counter++;
        }

        setInfluenceIndices(influenceIndices);
    }
    
   
    @Override
    public SharedMesh updateSharedMesh() 
    {
        super.updateSharedMesh();
        // The new skinning model has this mesh query its skeleton for
        // the appropriate collection of transform matrices
        if (m_skeletonNode == null)
        {
            // make sure our parent is actually a skeleton node
            if (getParent() instanceof SkeletonNode)
                m_skeletonNode = ((SkeletonNode)getParent());
        }

        if (m_skeletonNode == null)
            return m_instance;
        
        int [] influenceIndices = getInfluenceIndices();
        
        if (m_InverseBindPose == null) // Initialize the bind pose by querying the skeleton for its bind pose
            m_InverseBindPose = m_skeletonNode.getFlattenedInverseBindPose(influenceIndices);  // the group's transform is ignored
        // Retrieve the collection of influences in their current pose
        PMatrix [] matrixStack = m_skeletonNode.getPose(influenceIndices);
        
        if (m_shaderState != null) // may not have loaded yet
        {    
            // populate the matrix stack
            final float[] pose = new float[m_InverseBindPose.length * 16];
            
            for (int i = 0; i < matrixStack.length && i < m_InverseBindPose.length; i++)
            {
                PMatrix matrix = new PMatrix(matrixStack[i]);
                //postAnimationModifiedMeshSpaceMatrixHook(matrix, influenceIndices[i]);

                matrix.mul(m_InverseBindPose[i]);

                float [] matrixFloats = matrix.getFloatArray();
                for(int j = 0; j < 16; j++)
                {
                    pose[j+(i*16)] = matrixFloats[j];
                }
            }
            if (doesShaderContainDeformer() == true)
                m_shaderState.setUniformMatrix4Array("pose", pose, false);
        }


        if (m_instance.getTarget().getIndexBuffer() == null)
            return null;
        return m_instance;
    }
    

    // This method tells the skeleton to rebind its mappings
    public void recalculateInverseBindPose()
    {
        SkeletonNode skeleton = ((SkeletonNode)getParent());
        skeleton.refresh();
        m_InverseBindPose = skeleton.getFlattenedInverseBindPose(((PPolygonSkinnedMesh)m_geometry).getInfluenceIndices());
    }
    
    
    /**
     * Retrieve the BFT indices of the joints in the associated skeleton that
     * deform this mesh instance
     * @return The array of influence indices
     */
    public int[] getInfluenceIndices()
    {
        return m_influenceIndices;
    }
    
    /**
     * Set the list of influences for this instance. These should be generated
     * by querying the skeleton that owns this instance. The inverse bind pose
     * mapping should be regenerated through <code>recalculateInverseBindPose</code>
     * if this step is performed after initialization
     * @param indexArray
     */
    public void setInfluenceIndices(int[] indexArray)
    {
        // out with the old
        m_influenceIndices = new int[indexArray.length];
        
        for (int i = 0; i < indexArray.length; ++i)
            m_influenceIndices[i] = indexArray[i];
    }
    
    private boolean bDeforming = false;
    private boolean doesShaderContainDeformer()
    {
        if (bDeforming)
            return true;
        AbstractShaderProgram shader = getMaterialRef().getMaterial().getShader();

        if (shader == null) // No shader, no deformer!
            return false;
        ShaderProperty[] props = shader.getProperties();
        
        for (ShaderProperty prop : props)
        {
            if (prop.name.equals("pose"))
            {
                bDeforming = true;
                return true;
            }
        }
        return false;
    }

//    protected void postAnimationModifiedMeshSpaceMatrixHook(PMatrix matrix, int jointIndex) 
//    {
//        if (m_jointManipulator == null)
//            return;
//        
//        m_jointManipulator.postAnimationModifiedMeshSpaceMatrixHook(matrix, jointIndex);
//    }
//
//    public PostAnimationJointManipulator getJointManipulator() {
//        return m_jointManipulator;
//    }
//
//    public void setJointManipulator(PostAnimationJointManipulator jointManipulator) {
//        this.m_jointManipulator = jointManipulator;
//    }
    
    
}
