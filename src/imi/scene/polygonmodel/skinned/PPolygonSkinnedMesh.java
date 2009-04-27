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
import imi.scene.PMatrix;
import imi.scene.PTransform;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.parts.polygon.PPolygonVertexIndices;
import imi.scene.utils.PRenderer;
import java.nio.FloatBuffer;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.io.Serializable;
import java.util.List;
import javolution.util.FastTable;



/**
 *
 * @author Ronald Dahlgren
 * @author Lou Hayt
 */
public class PPolygonSkinnedMesh extends PPolygonMesh implements Serializable
{   
    // Skinning loaded data (no duplicates), these fields are nulled after reconstruction of the mesh when setSkinningData() is called.
    // indexed by polygons, weight of 4 influence from the indexed materices\bones
    private final FastTable<Vector3f>     m_boneWeights   	  = new FastTable<Vector3f>();
    // indexed by polygons, 4 indices of bones in the flatened matrix stack
    private final FastTable<PBoneIndices> m_boneIndices  	  = new FastTable<PBoneIndices>();

    // Skinning final data (calculated in PPolygonTriMeshAssembler)
    private transient FloatBuffer       		m_WeightBuffer        = null;    // per vertex, weight of 4 influence from the indexed materices\bones
    private transient FloatBuffer       		m_BoneIndexBuffer     = null;    // per vertex, 4 indices of bones in the flatened matrix stack

    private FastTable<String>   	m_JointNames          = new FastTable<String>();
    private int[]             		m_influenceIndices    = null;

	//  Constructor.
	public PPolygonSkinnedMesh()
    {
        super("PPolygonSkinnedMesh");
    }
    
    public PPolygonSkinnedMesh(String name)
    {
        super(name);
    }

    /***
     * This is copying as much as possible by reference
     * @param mesh
     */
    public PPolygonSkinnedMesh(PPolygonMesh mesh)
    {
        
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

        // Copy skinning stuffs
        if (mesh instanceof PPolygonSkinnedMesh)
        {
            Iterable<Vector3f> otherWeights = ((PPolygonSkinnedMesh)mesh).getBoneWeightArray();
            Iterable<PBoneIndices> otherIndices = ((PPolygonSkinnedMesh)mesh).getBoneIndexArray();
            // now copy them over onto ourselves
            // weights
            m_boneWeights.clear();
            for (Vector3f vec : otherWeights)
                m_boneWeights.add(new Vector3f(vec));
            // Indices
            m_boneIndices.clear();
            for (PBoneIndices bones : otherIndices)
                m_boneIndices.add(new PBoneIndices(bones));
        }
        
        m_Polygons  = new FastTable<PPolygon>();
        
        for (int i = 0; i < mesh.getPolygonCount(); i++)
        {
            m_Polygons.add(new PPolygon(mesh.getPolygon(i), true));
            m_Polygons.get(i).setPolygonMesh(this);
        }
        
        // Great Success!
        endBatch(false);
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
     * @param index indexe into this meshes list of influences
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

    /**
     * Add a new set of bone indices. Return the index.
     * @param boneIndices
     * @return
     */
    public int addBoneIndices(PBoneIndices boneIndices)
    {
        int index = findBoneIndices(boneIndices);
        if (index == -1)
        {
            m_boneIndices.add(boneIndices);
            index = m_boneIndices.size() - 1;
        }

        return(index);
    }
    
    /**
     * Determine the index of the provided bone indices object.
     * @param boneIndices
     * @return index, or -1 if not found.
     */
    public int findBoneIndices(PBoneIndices boneIndices)
    {
        return m_boneIndices.indexOf(boneIndices);
    }
    
    public int getBoneWeights(Vector3f boneWeights)
    {
        int index = findBoneWeights(boneWeights);
        if (index == -1)
        {
            m_boneWeights.add(boneWeights);
            index = m_boneWeights.size() - 1;
        }

        return(index);
    }
    
    /**
     * Determine the index of the provided boneweights.
     * @param boneWeights
     * @return
     */
    public int findBoneWeights(Vector3f boneWeights)
    {
        return m_boneWeights.indexOf(boneWeights);
    }
    
    /**
     * Retrieves a reference to the backing ArrayList
     * @return m_boneWeights (ArrayList<Vector3f>)
     */
    public Iterable<Vector3f> getBoneWeightArray()
    {
        return m_boneWeights;
    }
    
    /**
     * Retrieves a reference to the backing ArrayList
     * @return m_boneIndices (ArrayList(PBoneIndices>)
     */
    public Iterable<PBoneIndices> getBoneIndexArray()
    {
        return m_boneIndices;
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
    }
    
    public FloatBuffer getBoneIndexBuffer() 
    {
        return m_BoneIndexBuffer;
    }

    public FloatBuffer getWeightBuffer() 
    {
        return m_WeightBuffer;
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


    /**
     * Use the internal collection of joint names to build influence indices
     * for the provided SkeletonNode.
     * @param skeleton
     */
    public void linkJointsToSkeletonNode(SkeletonNode skeleton)
    {
        int index = 0;
        int [] influenceIndices = new int[m_JointNames.size()];
        for (String jointName : m_JointNames)
        {
            int BFTIndex = skeleton.getSkinnedMeshJointIndex(jointName);
            if (BFTIndex == -1) // not found!
            {
                logger.info("Joint not found for influence #" + index + ", name: " + jointName);
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

}



