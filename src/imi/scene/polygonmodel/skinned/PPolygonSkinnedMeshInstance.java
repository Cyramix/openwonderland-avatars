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
package imi.scene.polygonmodel.skinned;

import com.jme.scene.SharedMesh;
import imi.loaders.scenebindings.sbConfigurationData;
import imi.loaders.scenebindings.sbLocalModifier;
import imi.loaders.scenebindings.sbShaderPair;
import imi.loaders.scenebindings.sbTexture;
import imi.loaders.scenebindings.sbMaterial;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.ShaderProperty;
import imi.scene.utils.PRenderer;
import imi.scene.utils.tree.LocalModifierCollector;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class PPolygonSkinnedMeshInstance extends PPolygonMeshInstance
{
    boolean bInit = true;
    
    private PMatrix[]    m_InverseBindPose  = null; // TODO: Caching this may not be desireable
    
    private SkeletonNode m_pSkeletonNode    = null;
    
    private int[]        m_influenceIndices = null;



    //  Constructor.
    public PPolygonSkinnedMeshInstance(String name, PPolygonSkinnedMesh geometry, PMatrix origin, PScene pscene) 
    {
        super(name, geometry, origin, pscene);
        if (geometry.getInfluenceIndices() != null)
            setInfluenceIndices(geometry.getInfluenceIndices());
        
    }
    
    //  Copy Constructor.
    public PPolygonSkinnedMeshInstance(PPolygonSkinnedMeshInstance meshInstance, PScene pscene)
    {
        super(meshInstance.getName(), meshInstance.getGeometry(), meshInstance.getTransform().getLocalMatrix(false), pscene);
        if (meshInstance.getInfluenceIndices() != null)
            setInfluenceIndices(meshInstance.getInfluenceIndices());
        setSkeletonNode(meshInstance.getSkeletonNode());
    }
    
        
    //  Gets the SkeletonNode.
    public SkeletonNode getSkeletonNode()
    {
        return(m_pSkeletonNode);
    }
    
    public void setSkeletonNode(SkeletonNode skeleton)
    {
        m_pSkeletonNode = skeleton;
    }

    @Override
    public void draw(PRenderer renderer) {
        if (m_geometry != null) {
            // Set world origin
            PMatrix origin = getTransform().getWorldMatrix(false);
            renderer.setOrigin(origin);

            // Draw geometry
            ((PPolygonSkinnedMesh) m_geometry).draw(renderer, ((SkeletonNode)getParent()).getSkeletonRoot());
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

    public void linkJointsToSkeletonNode(SkeletonNode skeleton) 
    {
        int a;
        String jointName = "";
        int jointIndex;
        PPolygonSkinnedMesh skinnedGeometry = (PPolygonSkinnedMesh)m_geometry;
        int []influenceIndices = new int[skinnedGeometry.getJointNameCount()];

        for (a=0; a<skinnedGeometry.getJointNameCount(); a++)
        {
            jointName = skinnedGeometry.getJointName(a);

            //  Get the index of the Joint.
            jointIndex = m_pSkeletonNode.getSkinnedMeshJointIndex(jointName);

            influenceIndices[a] = jointIndex;
        }

        setInfluenceIndices(influenceIndices);
    }
    
   
    @Override
    public SharedMesh updateSharedMesh() 
    {
        super.updateSharedMesh();
        // The new skinning model has this mesh query its skeleton for
        // the appropriate collection of transform matrices
        if (m_pSkeletonNode == null)
            m_pSkeletonNode = ((SkeletonNode)getParent());

        if (m_pSkeletonNode == null)
            return m_instance;
        
        if (m_InverseBindPose == null) // Initialize the bind pose by querying the skeleton for its bind pose
            m_InverseBindPose = m_pSkeletonNode.getInverseBindPose(getInfluenceIndices());  // the group's transform is ignored
        // Retrieve the collection of influences in their current pose
        PMatrix [] matrixStack = m_pSkeletonNode.getPose(getInfluenceIndices());
        
        if (m_shaderState != null) // may not have loaded yet
        {    
            // populate the matrix stack
            final float[] pose = new float[m_InverseBindPose.length * 16];
            
            for (int i = 0; i < matrixStack.length && i < m_InverseBindPose.length; i++)
            {
                PMatrix matrix = new PMatrix(matrixStack[i]);
                matrix.mul(m_InverseBindPose[i]);
                postAnimationMatrixModifier(matrix, m_InverseBindPose[i], i);
                float [] matrixFloats = matrix.getFloatArray();
                for(int j = 0; j < 16; j++)
                {
                    pose[j+(i*16)] = matrixFloats[j];
                }
            }
            if (doesShaderContainDeformer() == true)
                m_shaderState.setUniformMatrix4Array("pose", pose, false);
            else
            {
                int hack = 0;
                hack++;
            }
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
        m_InverseBindPose = skeleton.getInverseBindPose(((PPolygonSkinnedMesh)m_geometry).getInfluenceIndices());
    }
    
    @Override
    public sbConfigurationData load_createConfigurationData()
    {
        sbConfigurationData result = super.load_createConfigurationData();
        
        boolean bNeeded = true;
        
        if (result == null)
        {
            bNeeded = false;
            
            // Allocate memory
            result = new sbConfigurationData();
        }
            
        // Local modifiers
        LocalModifierCollector processor = new LocalModifierCollector();
        // TODO: Port
        //TreeTraverser.breadthFirst(m_TransformHierarchy.getChild(0), processor);
        ArrayList<sbLocalModifier> modifierList = processor.getModifierList();
        if (!modifierList.isEmpty())
        {
            bNeeded = true;
            result.setLocalModifiers(modifierList);
        }
        
        if (bNeeded)
            return result;
        
        return null;
    }
    
    @Override
    protected void load_processConfiguration(sbConfigurationData config) 
    {
        PMatrix local = getTransform().getLocalMatrix(true);
        local.setIdentity();
        
        // Scale
        if (config.getScale() != null)
            local.setScale(config.getScale().getVec3());
        
        // Material
        if (config.getMaterial() != null)
        {
            PMeshMaterial mat = new PMeshMaterial();
            sbMaterial configMat = config.getMaterial();
            
            // Textures
            if (configMat.getTextureFiles() != null)
            {
                List<sbTexture> textures = configMat.getTextureFiles();
                for (sbTexture tex : textures)
                    mat.setTexture(tex.getPath(), tex.getTextureUnit());
            }

            // Shaders
            if (configMat.getShaderPair() != null)
            {
                sbShaderPair shades = configMat.getShaderPair();
                // TODO: COnvert to the new system
//                mat.setVertShader(new File(shades.getVertexShaderPath()));
//                mat.setFragShader(new File(shades.getFragmentShaderPath()));
            }
            
            // Set the material (applies it)
            setMaterial(mat);
            setUseGeometryMaterial(false);
        }
            
        // Apply local modifiers
        if (config.getLocalModifiers() != null)
        {
            List<sbLocalModifier> modifiers = config.getLocalModifiers();
            for (sbLocalModifier modi : modifiers)
            {
                PNode joint = findChild(modi.getTargetJointName());
                if (joint != null && joint instanceof PJoint)
                    ((PJoint)joint).setLocalModifierMatrix(modi.getTransform().asPMatrix());
            }
        }
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

    protected void postAnimationMatrixModifier(PMatrix matrix, PMatrix inverseBindPose, int index) 
    {
        
    }
}
