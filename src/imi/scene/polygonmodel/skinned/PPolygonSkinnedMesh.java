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

import imi.scene.polygonmodel.*;
import com.jme.math.Vector3f;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.parts.polygon.PPolygonVertexIndices;
import imi.scene.utils.PRenderer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.util.List;



/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class PPolygonSkinnedMesh extends PPolygonMesh
{   
    // Skinning loaded data (no duplicates), these fields are nulled after reconstruction of the mesh when setSkinningData() is called.
    private ArrayList<Vector3f>     m_BoneWeights   	  = null; // indexed by polygons, weight of 4 influence from the indexed materices\bones
    private ArrayList<PBoneIndices> m_PBoneIndices  	  = null; // indexed by polygons, 4 indices of bones in the flatened matrix stack

    // Skinning final data (calculated in PPolygonTriMeshAssembler)
    private FloatBuffer       		m_WeightBuffer        = null;    // per vertex, weight of 4 influence from the indexed materices\bones
    private FloatBuffer       		m_BoneIndexBuffer     = null;    // per vertex, 4 indices of bones in the flatened matrix stack

    private ArrayList<String>   	m_JointNames          = new ArrayList<String>();
    private int[]             		m_influenceIndices    = null;

    //private AnimationComponent m_Animation = new AnimationComponent();
    
    //private ArrayList<SkinnedMeshJoint> m_animationJointMapping = new ArrayList<SkinnedMeshJoint>();

    
	//  Constructor.
	public PPolygonSkinnedMesh()
    {
        super("PPolygonSkinnedMesh");
        m_BoneWeights          = new ArrayList();
        m_PBoneIndices         = new ArrayList();
        //addChild(m_BindPoseSkeleton);
    }
    
    public PPolygonSkinnedMesh(String name)
    {
        super(name);
        m_BoneWeights          = new ArrayList();
        m_PBoneIndices         = new ArrayList();
        //addChild(m_BindPoseSkeleton);
    }

    /***
     * This is copying as much as possible by reference
     * @param mesh
     */
    public PPolygonSkinnedMesh(PPolygonMesh mesh)
    {
        m_BoneWeights          = new ArrayList();
        m_PBoneIndices         = new ArrayList();
        
        setName(mesh.getName());
        setTransform(new PTransform(mesh.getTransform()));
        
        beginBatch();
        
        setUniformTexCoords(mesh.isUniformTexCoords());
        setNumberOfTextures(mesh.getNumberOfTextures());
        setSmoothNormals(mesh.getSmoothNormals());
        setMaterial(mesh.getMaterialCopy());
        
        m_Positions = mesh.getPositionsRef();
        m_Normals   = mesh.getNormalsRef();
        m_Colors    = mesh.getColorsRef();
        m_TexCoords = mesh.getTexCoordsRef();
        
        m_Polygons  = new ArrayList<PPolygon>();
        
        for (int i = 0; i < mesh.getPolygonCount(); i++)
        {
            m_Polygons.add(new PPolygon(mesh.getPolygon(i), true));
            m_Polygons.get(i).setPolygonMesh(this);
        }
        
        // Great Success!
        endBatch(false);
    }
    
    public void draw(PRenderer renderer, PNode TransformHierarchy)
    {
        renderer.drawPPolygonSkinnedMesh(this, TransformHierarchy);
    }
    
    
    public int[] getInfluenceIndices()
    {
        return m_influenceIndices;
    }

    /**
     * Set the list of influences for this mesh
     * @param array An array of type Integer
     */
    public void setInfluenceIndices(List<Integer> influences)
    {
        m_influenceIndices = new int[influences.size()];
        int counter = 0;
        for (Integer index : influences)
            m_influenceIndices[counter] = index;
    }
    
    /**
     * Set the list of influences for this mesh
     * @param indices The list of influences
     */
    public void setInfluenceIndices(int[] indices)
    {
        m_influenceIndices = indices;
    }
    
    /**
     * This method retrieves the influence index at the specified position
     * in this mesh's list of influences.
     * @param index Index into this meshes list of influences
     * @return The influence index at the specified position, or -1 on failure
     */
    public int getInfluenceIndex(int index)
    {
        if (m_influenceIndices == null || m_influenceIndices.length <= index)
            return -1;
        else
            return m_influenceIndices[index];
    }
    
    /**
     * This method sets the influence index at the specified position
     * in this mesh's list of influences.
     * @param index The index into this meshes list of influences
     * @param influenceIndexValue the new value for this influence index
     * @return true on success, false on failure (index out of bounds, etc)
     */
    public boolean setInfluenceIndex(int index, int influenceIndexValue)
    {
        if (m_influenceIndices == null || m_influenceIndices.length <= index)
            return false;
        else
            m_influenceIndices[index] = influenceIndexValue;
        return true;
    }
    
    /**
     * This method returns the number of bones that influence
     * this mesh. 
     * @return Number of influences
     */
    public int getNumberOfInfluences()
    {
        if (m_influenceIndices == null)
            return 0;
        else
            return m_influenceIndices.length;
    }
    
    public int getBoneIndices(PBoneIndices PBoneIndices)
    {
        int Index = findBoneIndices(PBoneIndices);
	if (Index == -1)
        {
            m_PBoneIndices.add(PBoneIndices);
            Index = m_PBoneIndices.size() - 1;
        }

        return(Index);
    }
    
    //  Finds the index of the BoneIndices.
    public int findBoneIndices(PBoneIndices pBoneIndices)
    {
        for (int i = 0; i < m_PBoneIndices.size(); i++)
	{
//            if (m_PBoneIndices.get(i).hashCode() == pBoneIndices.hashCode())
            if (pBoneIndices.equals(m_PBoneIndices.get(i)))
                return i;
        }

        return(-1);
    }
    
    public int getBoneWeights(Vector3f boneWeights)
    {
        int Index = findBoneWeights(boneWeights);
	if (Index == -1)
        {
            m_BoneWeights.add(boneWeights);
            Index = m_BoneWeights.size() - 1;
        }

        return(Index);
    }
    
    //  Finds the index of the PolygonMeshPosition.
    public int findBoneWeights(Vector3f boneWeights)
    {
        for (int i = 0; i < m_BoneWeights.size(); i++)
	{
//            if (m_BoneWeights.get(i).hashCode() == boneWeights.hashCode())
            if (boneWeights.equals(m_BoneWeights.get(i)))
                return i;
        }

        return(-1);
    }
    
    /**
     * Retrieves a reference to the backing ArrayList
     * @return m_BoneWeights (ArrayList<Vector3f>)
     */
    public ArrayList<Vector3f> getBoneWeightArray()
    {
        return m_BoneWeights;
    }
    
    /**
     * Retrieves a reference to the backing ArrayList
     * @return m_PBoneIndices (ArrayList(PBoneIndices>)
     */
    public ArrayList<PBoneIndices> getBoneIndexArray()
    {
        return m_PBoneIndices;
    }
    
    /**
     * This will null earlier representations of the data.
     * @param weightArray
     * @param boneIndexArray
     */
    public void setSkinningData(FloatBuffer weightArray, FloatBuffer boneIndexArray)
    {
        m_WeightBuffer      = weightArray;
        m_BoneIndexBuffer   = boneIndexArray;
        
        m_BoneWeights       = null;
        m_PBoneIndices      = null;
    }
    
//    public ArrayList<SkinnedMeshJoint> getAnimationJointMapping()
//    {
//        return m_animationJointMapping;
//    }
//    
//    public void buildAnimationJointMapping(SkeletonNode owningSkeleton) 
//    {
//        if (!m_animationJointMapping.isEmpty())
//            return;
//        
//        AnimationGroup animGroup = m_Animation.getGroup(); // If every bone is mentioned this should be fine
//        
//        for (int i = 0; i < animGroup.getChannels().size(); ++i)
//        {
//            String name = animGroup.getChannels().get(i).getTargetJointName();
//            m_animationJointMapping.add( (SkinnedMeshJoint) owningSkeleton.findSkinnedMeshJoint(name) );
//        }   
//    }
//
//    public AnimationComponent getAnimationComponent() {
//        return m_Animation;
//    }
//    
//    // Defaults to group 0
//    public AnimationGroup getAnimationGroup() {
//        return m_Animation.getGroup();
//    }
    
    public FloatBuffer getBoneIndexBuffer() 
    {
        return m_BoneIndexBuffer;
    }

    public FloatBuffer getWeightBuffer() 
    {
        return m_WeightBuffer;
    }
    
    @Override
    public void submit(PPolygonTriMeshAssembler assembler) 
    {
        super.submit(assembler);
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        return super.equals(obj);
    }

    @Override
    public int hashCode() 
    {
        return super.hashCode();
    }
    
    @Override
    public void addPolygon(PPolygon pPolygon) 
    {
        if (pPolygon == null)
        {
            logger.info("PPolygonSkinnedMesh does not accept null PPolygons");
            return;
        }
        
        PPolygonVertexIndices vert = pPolygon.getVertex(0);
        
        if (vert == null)
        {
            logger.info("PPolygonSkinnedMesh does not accept empty PPolygons");
            return;
        }
        
        if (!(vert instanceof PPolygonSkinnedVertexIndices))
        {
            logger.info("PPolygonSkinnedMesh does not accept PPolygons that uses non-skinned verts");
            return;
        }

        // You made the cut!
        super.addPolygon(pPolygon);
    }

    @Override
    public void addPolygon(int[] pPositionIndices, int[] pTexCoordIndices, int VertexCount) 
    {
        //super.addPolygon(pPositionIndices, pTexCoordIndices, VertexCount);
        logger.info("PPolygonSkinnedMesh does not use this method");
    }

    @Override
    public void addQuad(int Position1Index, int Position2Index, int Position3Index, int Position4Index, int ColorIndex, int TexCoord1Index, int TexCoord2Index, int TexCoord3Index, int TexCoord4Index) 
    {
        //super.addQuad(Position1Index, Position2Index, Position3Index, Position4Index, ColorIndex, TexCoord1Index, TexCoord2Index, TexCoord3Index, TexCoord4Index);
        logger.info("PPolygonSkinnedMesh does not use this method");
    }

    @Override
    public void addTriangle(int Position1Index, int Position2Index, int Position3Index, int ColorIndex, int TexCoord1Index, int TexCoord2Index, int TexCoord3Index) 
    {
        //super.addTriangle(Position1Index, Position2Index, Position3Index, ColorIndex, TexCoord1Index, TexCoord2Index, TexCoord3Index);
        logger.info("PPolygonSkinnedMesh does not use this method");
    }
     
    @Override
    public void combinePolygonMesh(PPolygonMesh other, boolean bKeepOriginalMaterial) 
    {
        //super.combinePolygonMesh(other, bKeepOriginalMaterial);
        logger.info("PPolygonSkinnedMesh does not use this method");
    }
     
    @Override
    public PPolygon createPolygon() 
    {
        logger.info("PPolygonSkinnedMesh does not use this method");
        return null;//super.createPolygon();
    }



	//  Adds a JointName.
    public void addJointName(String jointName)
    {
        m_JointNames.add(new String(jointName));
    }

    //  Gets the number of JointNames.
    public int getJointNameCount()
    {
        return(m_JointNames.size());
    }

    //  Gets the JointName at the specified index.
    public String getJointName(int index)
    {
        return(m_JointNames.get(index));
    }


    //  Builds the array of bone influences.
    public void linkJointsToSkeletonNode(SkeletonNode skeleton)
    {
        int index = 0;
        int [] influenceIndices = new int[m_JointNames.size()];
        for (String jointName : m_JointNames)
        {
            int BFTIndex = skeleton.getSkinnedMeshJointIndex(jointName);
            if (BFTIndex == -1) // not found!
            {
                logger.severe("Joint not found for influence #" + index + ", name: " + jointName);
                continue;
            }
            else
                influenceIndices[index] = BFTIndex;
            index++;
        }
       
        setInfluenceIndices(influenceIndices);
    }

    public Iterable<String> getJointNames() {
        return m_JointNames;
    }

}



