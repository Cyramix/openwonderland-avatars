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
package imi.scene.polygonmodel;

import com.jme.math.Vector3f;
import com.jme.scene.SharedMesh;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.SkeletonNode;
import imi.shader.AbstractShaderProgram;
import imi.shader.ShaderProperty;
import imi.utils.instruments.Instrumentation.InstrumentedSubsystem;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * This class represents an instance of a skinned mesh.
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class PPolygonSkinnedMeshInstance extends PPolygonMeshInstance implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    protected PMatrix[]   m_InverseBindPose  = null; // TODO: Caching this may not be desireable

    protected SkeletonNode m_skeletonNode    = null;

    protected transient int[]        m_influenceIndices = new int[0];

    protected transient PMatrix[]    m_pose = null;


    private transient float[] m_matrixFloats = new float[16];
    private transient float[] m_poseFloats = null;

    //  Constructor.
    public PPolygonSkinnedMeshInstance(String name, PPolygonSkinnedMesh geometry, PMatrix origin, PScene pscene, boolean applyMaterial)
    {
        super(name, geometry, origin, pscene, applyMaterial);
        setInfluenceIndices(geometry.getInfluenceIndices());

    }

    //  Copy Constructor.
    public PPolygonSkinnedMeshInstance(PPolygonSkinnedMeshInstance meshInstance, PScene pscene, boolean applyMaterial)
    {
        super(meshInstance, pscene, applyMaterial);
        setInfluenceIndices(meshInstance.getInfluenceIndices());
    }


    //  Gets the SkeletonNode.
    public SkeletonNode getSkeletonNode()
    {
        return(m_skeletonNode);
    }

    /**
     * Clears the internally cached inverse bind pose.
     */
    public void clearInverseBindPose() {
        m_InverseBindPose = null;
    }

    /**
     * This method assigns the provided skeleton node as the skeleton this skinned
     * mesh instance will be attached to. The provided skeleton must not be null
     * and should be fully initialized.
     * @param skeleton
     * @throws IllegalArgumentException If skeleton == null.
     */
    public void setAndLinkSkeletonNode(SkeletonNode skeleton)
    {
        if (skeleton == null)
            throw new IllegalArgumentException("Skeleton provided to the Skinned" +
                    "MeshInstance must not be null.");
        m_skeletonNode = skeleton;
        linkJointsToSkeletonNode();
    }

    /**
     * Experimental!
     * This method transforms the bounding volume for the shared mesh to match
     * the joint it is attached to.
     */
    @Deprecated
    private void transformBounds() {
        if (m_influenceIndices.length > 0 && m_skeletonNode != null)
        {
            // get its transform
            PMatrix jointTransform = m_skeletonNode.getSkinnedMeshJoint(m_influenceIndices[0]).getMeshSpace().inverse();
            // apply this transform to the jME bounding volume
//            m_instance.getModelBound().transform(jointTransform.getRotationJME(), jointTransform.getTranslation(), jointTransform.getScaleVector());
            Vector3f location = new Vector3f();
            location.set(m_instance.getLocalTranslation());
            m_instance.unlockBounds();
            m_instance.setLocalTranslation(jointTransform.getTranslation());
            m_instance.updateModelBound();
//            m_instance.setLocalTranslation(location);
            m_instance.lockBounds();
            
            // dassit
        }
    }

    /** experimental :D **/
    private Vector3f getAproxMeshPosition()
    {
        Vector3f pos = new Vector3f();
        for (int i = 0; i < m_influenceIndices.length; i++)
        {
            pos.addLocal(m_skeletonNode.getSkinnedMeshJoint(m_influenceIndices[i]).getTransform().getWorldMatrix(false).getTranslation());
            pos.subtractLocal(m_skeletonNode.getSkinnedMeshJoint(m_influenceIndices[i]).getBindPoseRef().getTranslation());
        }
        pos.multLocal(1.0f / (float)m_influenceIndices.length);
        return pos;
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

    /**
     * This method builds the influence indices for this mesh instance by querying
     * the skeleton node for the BFT index of each joint listed as an influence.
     * For success, this method requires that a fully initialized skeleton node
     * be available.
     */
    private void linkJointsToSkeletonNode()
    {
        int jointIndex = -1;
        PPolygonSkinnedMesh skinnedGeometry = (PPolygonSkinnedMesh)m_geometry;
        int[] influenceIndices = new int[skinnedGeometry.getJointNameCount()];

        int counter = 0;
        for (String jointName : skinnedGeometry.getJointNames())
        {
            jointIndex = m_skeletonNode.getSkinnedMeshJointIndex(jointName);
            if (jointIndex != -1) // Not found!
                influenceIndices[counter] = jointIndex;
            counter++;
        }

        setInfluenceIndices(influenceIndices);
    }

    @Override
    public SharedMesh updateSharedMesh()
    {
        super.updateSharedMesh();
//        transformBounds();
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

        if (m_InverseBindPose == null) // Initialize the bind pose by querying the skeleton for its bind pose
            m_InverseBindPose = m_skeletonNode.getFlattenedInverseBindPose(m_influenceIndices);  // the group's transform is ignored
        // Retrieve the collection of influences in their current pose
        m_skeletonNode.getPose(m_influenceIndices, m_pose);

        // populate the matrix stack
        for (int i = 0; i < m_pose.length && i < m_InverseBindPose.length; i++)
        {
            //postAnimationModifiedMeshSpaceMatrixHook(matrix, influenceIndices[i]);

            m_pose[i].fastMul(m_InverseBindPose[i]);

            m_pose[i].getFloatArray(m_matrixFloats);
            for(int j = 0; j < 16; j++)
            {
                m_poseFloats[j+(i*16)] = m_matrixFloats[j];
            }
        }
        if (doesShaderContainDeformer() == true)
        {
            if (m_skeletonNode.getInstruments() == null ||
                m_skeletonNode.getInstruments().isSubsystemEnabled(InstrumentedSubsystem.PoseTransferToGPU) == true)
                m_materialStates.getShaderState().setUniformMatrix4Array("pose", m_poseFloats, false);
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
        int[] result = new int[m_influenceIndices.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = m_influenceIndices[i];
        return result;
    }

    /**
     * Set the list of influences for this instance. These should be generated
     * by querying the skeleton that owns this instance. The inverse bind pose
     * mapping should be regenerated through <code>recalculateInverseBindPose</code>
     * if this step is performed after initialization
     * @param indexArray A non-null array of integers
     * @throws NullPointerException If indexArray == null
     */
    public void setInfluenceIndices(int[] indexArray)
    {
        // out with the old
        m_influenceIndices = new int[indexArray.length];
        m_poseFloats = new float[indexArray.length * 16];
        m_pose = new PMatrix[indexArray.length];
        // in with the new
        for (int i = 0; i < indexArray.length; ++i)
        {
            m_influenceIndices[i] = indexArray[i];
            m_pose[i] = new PMatrix();
        }
    }

    private boolean bDeforming = false;
    private boolean doesShaderContainDeformer()
    {
        if (bDeforming)
            return true;
        AbstractShaderProgram shader = getMaterialRef().getShader();

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

    // setInfluenceIndices
    /****************************
     * SERIALIZATION ASSISTANCE *
     ****************************/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Skeleton node parent will call setAndLinkSkeletonNode.
        m_matrixFloats = new float[16];
        m_influenceIndices = new int[0];
    }
}
