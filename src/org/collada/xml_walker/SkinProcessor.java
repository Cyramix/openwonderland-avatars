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
package org.collada.xml_walker;


import java.lang.Comparable;
import java.util.Collections;
import java.util.ArrayList;
import java.math.BigInteger;

import org.collada.colladaschema.Skin;
import org.collada.colladaschema.Source;
import org.collada.colladaschema.FloatArray;
import org.collada.colladaschema.NameArray;

import imi.loaders.collada.Collada;

import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.PPolygonMesh;

import imi.scene.PMatrix;
import imi.scene.PTransform;

import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.polygon.PPolygonVertexIndices;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;

import com.jme.math.Vector3f;




/**
 *
 * @author Chris Nagle
 */
public class SkinProcessor extends Processor
{    
    private Skin m_pSkin = null;
    private String m_MeshName = "";

    private PColladaSkin m_pColladaSkin = null;
    private PMatrix m_pMatrix = new PMatrix();

            
    
    private float []m_BindShapeMatrix = new float[16];

    private ArrayList m_JointNames = new ArrayList();
    private ArrayList m_JointBindPoseMatrices = new ArrayList();
    private ArrayList m_Joints = new ArrayList();
    private float []m_pSkinWeights = null;

    private ArrayList m_SkinnedMeshJoints = new ArrayList();

    private int []m_VertexJointCounts = null;
    private int []m_VertexJoints = null;

    private PPolygonSkinnedMesh m_pPolygonSkinnedMesh = null;

    private boolean m_bPrintStats = false;




    //  Constructor.
    public SkinProcessor(Collada collada, Skin skin, Processor parent)
    {
        super(collada, skin, parent);

        m_pSkin = skin;

        m_MeshName = m_pSkin.getSource();
        if (m_MeshName.startsWith("#"))
            m_MeshName = m_MeshName.substring(1, m_MeshName.length());


        
        //  Create the PolygonSkinnedMesh.
        //  Will have to convert the PolygonMesh to a PolygonSkinnedMesh.
        PPolygonMesh pPolygonMesh = collada.findPolygonMesh(m_MeshName);

        m_pPolygonSkinnedMesh = new PPolygonSkinnedMesh(pPolygonMesh);

        //  Clear the PolygonMesh of all Geometry.
        pPolygonMesh.clear(false);

        //  Remove the PolygonMesh, we'll be creating a PolygonSkinnedMesh instead.
        collada.removePolygonMesh(pPolygonMesh);

        //  Let the Collada loader know about the created PolygonSkinnedMesh.
        collada.addPolygonSkinnedMesh(m_pPolygonSkinnedMesh);



        int a;
        for (a=0; a<skin.getBindShapeMatrix().size(); a++)
        {
            m_BindShapeMatrix[a] = ((Double)skin.getBindShapeMatrix().get(a)).floatValue();
        }

        for (a=0; a<skin.getSources().size(); a++)
        {
            Source pSource = (Source)skin.getSources().get(a);
            
            if (pSource.getId().endsWith("skin-joints"))
            {
                processSkinJoints(collada, pSource);
            }
            else if (pSource.getId().endsWith("bind_poses"))
            {
                processBindPoses(collada, pSource);
            }
            else if (pSource.getId().endsWith("skin-weights"))
            {
                processSkinWeights(collada, pSource);
            }
        }


        //  Build list of all the SkinnedMeshJoints.
        String jointName;
        PMatrix jointBindPoseMatrix;
        PTransform jointTransform;
        SkinnedMeshJoint pSkinnedMeshJoint;

        for (a=0; a<m_JointNames.size(); a++)
        {
            jointName = (String)m_JointNames.get(a);
            jointBindPoseMatrix = (PMatrix)m_JointBindPoseMatrices.get(a);

            jointTransform = new PTransform(jointBindPoseMatrix);

            pSkinnedMeshJoint = new SkinnedMeshJoint(jointName, jointTransform);

            m_SkinnedMeshJoints.add(pSkinnedMeshJoint);
        }

        processVertexWeights(collada);
    }

    void processSkinJoints(Collada collada, Source pSource)
    {
        NameArray nameArray = pSource.getNameArray();
        if (nameArray != null)
        {
            if (nameArray.getValues() != null)
            {
                for (int a=0; a<nameArray.getValues().size(); a++)
                {
                    m_JointNames.add(new String(nameArray.getValues().get(a)));
                }
            }
        }

        //System.out.flush();
    }

    void processBindPoses(Collada collada, Source pSource)
    {
        FloatArray          pFloatArray = pSource.getFloatArray();
        Double              pValue;
        int                 Index = 0;
        int                 a;
        float               []matrixFloats = new float[16];
        PMatrix             pBindPoseMatrix;

        while (Index < pFloatArray.getValues().size())
        {
            for (a=0; a<16; a++)
            {
                pValue = (Double)pFloatArray.getValues().get(Index+a);
                matrixFloats[a] = pValue.floatValue();
            }

            pBindPoseMatrix = new PMatrix(matrixFloats);
            m_JointBindPoseMatrices.add(pBindPoseMatrix);

            Index += 16;
        }
    }

    void processSkinWeights(Collada collada, Source pSource)
    {
        FloatArray pFloatArray = pSource.getFloatArray();
        Double pValue;

        m_pSkinWeights = new float[pFloatArray.getValues().size()];
        
        for (int a=0; a<pFloatArray.getValues().size(); a++)
        {
            pValue = (Double)pFloatArray.getValues().get(a);
            
            m_pSkinWeights[a] = pValue.floatValue();
        }
    }
    
    void processVertexWeights(Collada collada)
    {
        int a, b;
        BigInteger pBigInteger;

        m_VertexJoints = new int[m_pSkin.getVertexWeights().getV().size()];
        
        for (a=0; a<m_pSkin.getVertexWeights().getV().size(); a++)
            m_VertexJoints[a] = ((Long)m_pSkin.getVertexWeights().getV().get(a)).intValue();

        m_VertexJointCounts = new int[m_pSkin.getVertexWeights().getVcount().size()];
        for (a=0; a<m_pSkin.getVertexWeights().getVcount().size(); a++)
        {
            pBigInteger = (BigInteger)m_pSkin.getVertexWeights().getVcount().get(a);
            
            m_VertexJointCounts[a] = pBigInteger.intValue();
        }

        int              NumberOfWeightsForVertex = 0;
        int              VertexJointsIndex = 0;
        int              JointIndex = 0;
        int              WeightIndex = 0;
        float            fWeight = 0.0f;
        SkinnedMeshJoint pJoint;
        ArrayList        boneSkinWeights = new ArrayList();
        BoneSkinWeight   pBoneSkinWeight;
        int              maxNumberOfWeights = collada.getMaxNumberOfWeights();
            
        PBoneIndices     pBoneIndices = new PBoneIndices();
        Vector3f         pWeightVec = new Vector3f();
        int              BoneIndiceIndex;
        int              BoneWeightIndex;
        int              VertexIndex = 0;



        for (a=0; a<m_VertexJointCounts.length; a++)
        {
            NumberOfWeightsForVertex = m_VertexJointCounts[a];

            for (b=0; b<NumberOfWeightsForVertex; b++)
            {
                JointIndex = m_VertexJoints[VertexJointsIndex];
                WeightIndex = m_VertexJoints[VertexJointsIndex+1];
                fWeight = m_pSkinWeights[WeightIndex];
        
                pBoneSkinWeight = new BoneSkinWeight(JointIndex, fWeight);

                boneSkinWeights.add(pBoneSkinWeight);

//                pJoint = (SkinnedMeshJoint)m_SkinnedMeshJoints.get(JointIndex);
//                System.out.print("(" + pJoint.getName() + ", " + fWeight + "), ");

                VertexJointsIndex += 2;
            }

            if (boneSkinWeights.size() > 0)
                Collections.sort(boneSkinWeights);

            processJointSkinWeights(maxNumberOfWeights, boneSkinWeights);

            
            
            if (m_bPrintStats)
            {
                //System.out.print("Vertex[" + a + "]:  " + boneSkinWeights.size() + "  ");

                for (b=0; b<boneSkinWeights.size(); b++)
                {
                    pBoneSkinWeight = (BoneSkinWeight)boneSkinWeights.get(b);

                    pJoint = (SkinnedMeshJoint)m_SkinnedMeshJoints.get(pBoneSkinWeight.m_BoneIndex);

                    //System.out.print("(" + pJoint.getName() + ", " + pBoneSkinWeight.m_fWeight + "), ");
                }

                //System.out.println("");
            }

            
            pBoneIndices = new PBoneIndices();
            pWeightVec = new Vector3f();
            
            //  Convert to BoneIndices.
            toBoneIndices(maxNumberOfWeights, boneSkinWeights, pBoneIndices);

            //  Convert to Vector3f.
            toWeights(maxNumberOfWeights, boneSkinWeights, pWeightVec);
            
            BoneIndiceIndex = m_pPolygonSkinnedMesh.getBoneIndices(pBoneIndices);
            BoneWeightIndex = m_pPolygonSkinnedMesh.getBoneWeights(pWeightVec);

            assignSkinningIndicesToVertex(VertexIndex, BoneIndiceIndex, BoneWeightIndex);

            boneSkinWeights.clear();

            VertexIndex++;
        }

        //System.out.flush();
    }

    void toBoneIndices(int maxNumberOfWeights, ArrayList boneSkinWeights, PBoneIndices pBoneIndices)
    {
        for (int a=0; a<boneSkinWeights.size(); a++)
            pBoneIndices.index[a] = ((BoneSkinWeight)boneSkinWeights.get(a)).m_BoneIndex;
    }

    void toWeights(int maxNumberOfWeights, ArrayList boneSkinWeights, Vector3f pWeightVec)
    {
        if (boneSkinWeights.size() > 0)
            pWeightVec.x = ((BoneSkinWeight)boneSkinWeights.get(0)).m_fWeight;
        if (boneSkinWeights.size() > 1)
            pWeightVec.y = ((BoneSkinWeight)boneSkinWeights.get(1)).m_fWeight;
        if (boneSkinWeights.size() > 2)
            pWeightVec.z = ((BoneSkinWeight)boneSkinWeights.get(2)).m_fWeight;
    }
            
    void processJointSkinWeights(int maxNumberOfWeights, ArrayList boneSkinWeights)
    {
        float fWeightSum = 0.0f;
        int NumberOfWeightsToProcess = maxNumberOfWeights;
        int a;
        BoneSkinWeight pBoneSkinWeight;

        if (NumberOfWeightsToProcess > boneSkinWeights.size())
            NumberOfWeightsToProcess = boneSkinWeights.size();

        for (a=0; a<NumberOfWeightsToProcess; a++)
        {
            pBoneSkinWeight = (BoneSkinWeight)boneSkinWeights.get(a);
            fWeightSum += pBoneSkinWeight.m_fWeight;
        }

        for (a=0; a<NumberOfWeightsToProcess; a++)
        {
            pBoneSkinWeight = (BoneSkinWeight)boneSkinWeights.get(a);
            pBoneSkinWeight.m_fWeight /= fWeightSum;
        }


        //  Delete the remaining BoneSkinWeights.
        if (boneSkinWeights.size() > maxNumberOfWeights)
        {
            for (a=boneSkinWeights.size()-1; a>=maxNumberOfWeights; a--)
                boneSkinWeights.remove(a);
        }
        else if (boneSkinWeights.size() < maxNumberOfWeights)
        {
            int count = maxNumberOfWeights-boneSkinWeights.size();
            for (a=0; a<count; a++)
            {
                pBoneSkinWeight = new BoneSkinWeight(1, 0.0f);
                boneSkinWeights.add(pBoneSkinWeight);
            }
        }
    }

    private void assignSkinningIndicesToVertex(int positionIndex, int boneIndiceIndex, int boneWeightIndex)
    {
        int polygonIndex;
        PPolygon pPolygon;
        int vertexIndex;
        PPolygonVertexIndices pPolygonVertexIndices;
        PPolygonSkinnedVertexIndices pPolygonSkinnedVertexIndices;


        //  Iterate throuh all the Polygons.
        for (polygonIndex=0; polygonIndex<m_pPolygonSkinnedMesh.getPolygonCount(); polygonIndex++)
        {
            pPolygon = m_pPolygonSkinnedMesh.getPolygon(polygonIndex);

            //  Iterate through all the Vertices in the Polygon.
            for (vertexIndex=0; vertexIndex<pPolygon.getVertexCount(); vertexIndex++)
            {
                pPolygonVertexIndices = pPolygon.getVertex(vertexIndex);

                //  Is this Vertice using the Position.
                if (pPolygonVertexIndices.m_PositionIndex == positionIndex)
                {
                    pPolygonSkinnedVertexIndices = (PPolygonSkinnedVertexIndices)pPolygonVertexIndices;
                    
                    pPolygonSkinnedVertexIndices.m_BoneIndicesIndex = boneIndiceIndex;
                    pPolygonSkinnedVertexIndices.m_BoneWeightIndex = boneWeightIndex;
                }
            }
        }
    }




    class BoneSkinWeight implements Comparable  
    {
        public int     m_BoneIndex = 0;
        public float   m_fWeight = 0.0f;

        //  Constructor.
        public BoneSkinWeight()
        {
        }
        public BoneSkinWeight(int boneIndex, float fWeight)
        {
            m_BoneIndex = boneIndex;
            m_fWeight = fWeight;
        }

        public int compareTo(Object o)
        {
            BoneSkinWeight pSkinWeight = (BoneSkinWeight)o;
            
            if (m_fWeight < pSkinWeight.m_fWeight)
                return(1);
            else if (m_fWeight > pSkinWeight.m_fWeight)
                return(-1);
            return(0);
        }
    }
    
}



