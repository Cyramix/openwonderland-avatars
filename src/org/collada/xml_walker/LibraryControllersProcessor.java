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


import java.math.BigInteger;
import java.lang.Comparable;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import com.jme.math.Vector3f;

import org.collada.colladaschema.LibraryControllers;
import org.collada.colladaschema.Controller;
import org.collada.colladaschema.Skin;
import org.collada.colladaschema.Source;

import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.polygon.PPolygonVertexIndices;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;

import imi.loaders.collada.Collada;

import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;

import imi.scene.PMatrix;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.COLLADA_JointChannel;



/**
 *
 * @author Chris Nagle
 */
public class LibraryControllersProcessor extends Processor
{    
    private float []                    m_BindShapeMatrix = new float[16];

    private List<String>                m_TheJointNames = null;
//    private ArrayList m_Joints = new ArrayList();
//    private float []m_pSkinWeights = null;
//    private int []m_VertexJointCounts = null;
//    private int []m_VertexJoints = null;
//    private PMatrix m_pTempMatrix = new PMatrix();

    private boolean                     m_bPrintStats = false;

    private PPolygonSkinnedMesh         m_pPolygonSkinnedMesh = null;

    private int                         m_BoneIndexStart = 0;

    private ArrayList<String>           m_BoneNames = new ArrayList<String>();
    private ArrayList<String>           m_JointNames = new ArrayList<String>();
    private int[]                       m_BoneUsageCounts = null;
    private int[]                       m_ReassignedBoneIndexes = null;

    private ArrayList<BoneSkinWeight>   m_BoneSkinWeights = new ArrayList<BoneSkinWeight>();

    private ArrayList                   m_SkinBoneIndices = new ArrayList();
    private ArrayList                   m_SkinWeightVecs = new ArrayList();



    //  Constructor.
    public LibraryControllersProcessor(Collada pCollada, LibraryControllers pLibraryControllers, Processor pParent)
    {
        super(pCollada, pLibraryControllers, pParent);

        m_bPrintStats = pCollada.getPrintStats();

        for (int a=0; a<pLibraryControllers.getControllers().size(); a++)
        {
            processController(pLibraryControllers.getControllers().get(a));
        }
    }

    private void processController(Controller pController)
    {
        //  Create the SkinProcessor.
        processSkin(pController, pController.getSkin());
    }

    private void processSkin(Controller pController, Skin pSkin)
    {
        String controllerName;
        String meshName;
        PPolygonMesh pPolygonMesh;
        float []matrixFloats = new float[16];
        PMatrix pMatrix = new PMatrix();
        int a, b;
        String boneName;
        String jointName;
        int jointIndex;
        float []skinWeights = null;
        int[] influenceIndices = null;
        PColladaNode pJointColladaNode;


/*
        //  Gets the load option.
        if (m_pCollada.getLoadOption() != Collada.LoadOption.LOAD_OPTION_LOAD_ANIMATION)
        {
            //  Make sure a SkeletonNode has been created.
            m_pSkeleton = m_pCollada.createSkeletonNode();
        }
        else
        {
            m_pSkeleton = m_pCollada.getSkeletonNode();
        }
*/

        controllerName = pController.getId();

        meshName = pSkin.getSource();
        if (meshName.startsWith("#"))
            meshName = meshName.substring(1);

        
//        if (meshName.equals("LHandShape"))
//            m_bPrintStats = true;

        PColladaSkin pColladaSkin = new PColladaSkin(controllerName);

        pColladaSkin.setMeshName(meshName);
        

        //  Create the PolygonSkinnedMesh.
        //  Will have to convert the PolygonMesh to a PolygonSkinnedMesh.
        pPolygonMesh = m_colladaRef.findPolygonMesh(meshName);

        m_pPolygonSkinnedMesh = new PPolygonSkinnedMesh(pPolygonMesh);

        //  Clear the PolygonMesh of all Geometry.
        pPolygonMesh.clear(false);

        //  Remove the PolygonMesh, we'll be creating a PolygonSkinnedMesh instead.
        m_colladaRef.removePolygonMesh(pPolygonMesh);

        //  Let the Collada loader know about the created PolygonSkinnedMesh.
        m_colladaRef.addPolygonSkinnedMesh(m_pPolygonSkinnedMesh);


        if (m_bPrintStats)
            System.out.println("SkinnedMesh:  " + m_pPolygonSkinnedMesh.getName());



        //  Read in the BindMatrix.
        for (a=0; a<pSkin.getBindShapeMatrix().size(); a++)
            matrixFloats[a] = pSkin.getBindShapeMatrix().get(a).floatValue();
        pMatrix.set(matrixFloats);
        pColladaSkin.setBindMatrix(pMatrix);



        //  Read in all the Joints.
        Source pSkinJoints = findSkinSourceElement(pSkin, "skin-joints");
        m_TheJointNames = pSkinJoints.getNameArray().getValues();
    
        m_BoneNames.clear();
        m_JointNames.clear();
        
        m_BoneUsageCounts = null;
        m_BoneUsageCounts = new int[pSkinJoints.getNameArray().getValues().size()];
        m_ReassignedBoneIndexes = new int[pSkinJoints.getNameArray().getValues().size()];

        m_SkinBoneIndices.clear();
        m_SkinWeightVecs.clear();
            

        if (m_bPrintStats)
            System.out.println("Bones:  " + m_TheJointNames.size());

        
        for (a=0; a<pSkinJoints.getNameArray().getValues().size(); a++)
        {
            boneName = pSkinJoints.getNameArray().getValues().get(a);
            pJointColladaNode = m_colladaRef.findJoint(boneName);
            jointName = pJointColladaNode.getName();

            m_pPolygonSkinnedMesh.addJointName(jointName);
            
            m_BoneNames.add(new String(boneName));
            m_BoneUsageCounts[a] = 0;
            m_JointNames.add(new String(jointName));
        
            if (m_bPrintStats)
                System.out.println("   bone=" + boneName + ", jointName=" + jointName);
        }


        //  Read in all the matrics.
        Source pSkinBindMatrices = findSkinSourceElement(pSkin, "skin-bind_poses");
        for (a=0; a<pSkinBindMatrices.getFloatArray().getValues().size(); a+=16)
        {
            for (b=0; b<16; b++)
            {
                matrixFloats[b] = pSkinBindMatrices.getFloatArray().getValues().get(a+b).floatValue();
            }

            pMatrix.set(matrixFloats);

            pColladaSkin.addBindMatrix(pMatrix);
        }
        

        //  Read in all the SkinWeights.
        Source pSkinWeights = findSkinSourceElement(pSkin, "skin-weights");
        skinWeights = new float[pSkinWeights.getFloatArray().getValues().size()];
        for (a=0; a<pSkinWeights.getFloatArray().getValues().size(); a++)
            skinWeights[a] = pSkinWeights.getFloatArray().getValues().get(a).floatValue();


        if (m_bPrintStats)
            System.out.println("Assigning JointNames to SkinnedMesh");

        //  Assign all the JointNames to the PolygonSkinnedMesh.
        for (a=0; a<m_JointNames.size(); a++)
        {
            jointName = (String)m_JointNames.get(a);

            if (m_bPrintStats)
                System.out.println("   Joint[" + a + "]:  " + jointName);

            m_pPolygonSkinnedMesh.addJointName(jointName);
        }


        processVertexWeights(pController, pSkin, m_pPolygonSkinnedMesh, skinWeights);

        m_bPrintStats = false;

        m_colladaRef.addColladaSkin(pColladaSkin);
    }

    

    void createAnimationLoopForSkinnedMesh(PColladaSkin pColladaSkin, PPolygonSkinnedMesh pSkinnedMesh)
    {
        int                  a;
        String               jointName;
        PColladaAnimatedItem pAnimatedItem;
        PColladaNode         pJointColladaNode;


        //  Create the AnimationLoop.
        AnimationGroup pAnimationLoop = new AnimationGroup();


        for (a=0; a<pColladaSkin.getJointNameCount(); a++)
        {
            jointName = pColladaSkin.getJointName(a);

            pJointColladaNode = m_colladaRef.findColladaNode(jointName);

            pAnimatedItem = m_colladaRef.findAnimatedItem(jointName);
            if (pAnimatedItem != null && pAnimatedItem.getKeyframeCount() > 0)
            {
                int b;
                float fFirstKeyframeTime = pAnimatedItem.getKeyframeTime(0);
                float fKeyframeTime;
                PMatrix pKeyframeMatrix = new PMatrix();

                if (m_bPrintStats)
                    System.out.println("   Processing animation channel for joint '" + jointName + "'.");

                //  Create a JointChannel.
                COLLADA_JointChannel pAnimationChannel = new COLLADA_JointChannel(jointName);


                //  Now, populate the newly created JointAnimation.
                for (b=0; b<pAnimatedItem.getKeyframeCount(); b++)
                {
                    fKeyframeTime = pAnimatedItem.getKeyframeTime(b);
                    //fKeyframeTime -= fFirstKeyframeTime;
                    pAnimatedItem.getKeyframeMatrix(b, pKeyframeMatrix);

                    pAnimationChannel.addKeyframe(fKeyframeTime, pKeyframeMatrix);
                }

                pAnimationChannel.calculateDuration();
                pAnimationChannel.setBindMatrix(pJointColladaNode.getMatrix());


                if (m_bPrintStats)
                {
                    System.out.println("   COLLADA_JointChannel created.");
                    System.out.println("      Duration:  " + pAnimationChannel.getDuration());
                }

                //  Add the JointAnimation to the AnimationLoop.
                pAnimationLoop.getChannels().add(pAnimationChannel);
            }
        }

        //  Trim the AnimationLoop.
//        pAnimationLoop.trim(1.55f);

        float fAnimationLoopDuration = pAnimationLoop.getDuration();
        
        if (m_bPrintStats)
            System.out.println("AnimationLoop contains " + pAnimationLoop.getChannels().size() + " animations.  It is " + fAnimationLoopDuration + " seconds long.");
        if (pAnimationLoop.getChannels().size() == 0)
        {
            pAnimationLoop = null;
        }
        else
        {
            pAnimationLoop.calculateDuration();
            pAnimationLoop.createDefaultCycle();

            pAnimationLoop.getCycle(0).setName(m_colladaRef.getName());
        }
    }

    
//  ******************************
//  ******************************
//  Private methods.
//  ******************************
//  ******************************
    void processVertexWeights(Controller pController,
                              Skin pSkin,
                              PPolygonSkinnedMesh pPolygonSkinnedMesh,
                              float []skinWeights)
    {
        int maxNumberOfWeights = 4;//m_pCollada.getMaxNumberOfWeights();
        int []vertexWeights = null;
        int []vertexBoneCounts = null;
        int a, b;
        BigInteger pBigInteger;
        int vertexBoneCount;
        int jointIndex = 0;
        int weightIndex = 0;
        int vertexWeightsIndex = 0;
        ArrayList<BoneSkinWeight> boneSkinWeights = new ArrayList<BoneSkinWeight>();
        BoneSkinWeight pBoneSkinWeight;
        String jointName;
        PBoneIndices pBoneIndices;
        Vector3f pWeightVec;
        int boneIndiceIndex;
        int boneWeightIndex;
        int vertexIndex = 0;
        boolean bWarningPrinted = false;
            


        //  Read in the VertexJoints.
        vertexWeights = new int[pSkin.getVertexWeights().getV().size()];
        for (a=0; a<pSkin.getVertexWeights().getV().size(); a++)
            vertexWeights[a] = ((Long)pSkin.getVertexWeights().getV().get(a)).intValue();

        //  Read in the VertexJointCounts.
        vertexBoneCounts = new int[pSkin.getVertexWeights().getVcount().size()];
        for (a=0; a<pSkin.getVertexWeights().getVcount().size(); a++)
        {
            pBigInteger = (BigInteger)pSkin.getVertexWeights().getVcount().get(a);
            vertexBoneCounts[a] = pBigInteger.intValue();
        }


        for (a=0; a<vertexBoneCounts.length; a++)
        {
            vertexBoneCount = vertexBoneCounts[a];

            boneSkinWeights.clear();

            //  Populate the array list with all the BoneSkinWeights.
            for (b=0; b<vertexBoneCount; b++)
            {
                jointIndex = vertexWeights[vertexWeightsIndex];
                weightIndex = vertexWeights[vertexWeightsIndex+1];

                pBoneSkinWeight = new BoneSkinWeight(jointIndex, skinWeights[weightIndex]);

                boneSkinWeights.add(pBoneSkinWeight);

                vertexWeightsIndex += 2;
            }

            
            //  Sort the array of BoneSkinWeights.
            if (boneSkinWeights.size() > 0)
                Collections.sort(boneSkinWeights);


            if (!bWarningPrinted)
            {
                if (boneSkinWeights.size() >= maxNumberOfWeights)
                {
//                    System.out.println("-----------------" + pPolygonSkinnedMesh.getName() + " Bone Weights '" + boneSkinWeights.size() + "' exceeds limit of '" + maxNumberOfWeights + "' limit.");
                    bWarningPrinted = true;
                }
            }

                        
            //  Process the SkinWeights for the Vertex.
            //  If there are more SkinWeights that supported, then the SkinWeights
            //  will be normalized.
            processJointSkinWeights(maxNumberOfWeights, boneSkinWeights);


            //  Print out the SkinWeights.
            if (m_bPrintStats)
            {
                System.out.print("Vertex[" + a + "]:  " + boneSkinWeights.size() + "  ");
                for (b=0; b<boneSkinWeights.size(); b++)
                {
                    pBoneSkinWeight = (BoneSkinWeight)boneSkinWeights.get(b);
                    jointName = m_TheJointNames.get(pBoneSkinWeight.m_BoneIndex);

//                    System.out.print("(" + pBoneSkinWeight.m_BoneIndex + ", " + pBoneSkinWeight.m_fWeight + "), ");
                    System.out.print("([" + pBoneSkinWeight.m_BoneIndex + ", " + jointName + "], " + pBoneSkinWeight.m_fWeight + "), ");
                }
                System.out.println("");
            }

            
            pBoneIndices = new PBoneIndices();
            pWeightVec = new Vector3f();
            
            //  Convert to BoneIndices.
            toBoneIndices(maxNumberOfWeights, boneSkinWeights, pBoneIndices);

/*
            //  Increment the usage counts of each Bone used.
            for (int bb=0; bb<pBoneIndices.index.length; bb++)
            {
                m_BoneUsageCounts[pBoneIndices.index[bb]]++;
//                if (pBoneIndices.index[bb] >= this.m_JointNames.size())
//                    int ggh = 0;
            }
*/

            //  Convert to Vector3f.
            toWeights(maxNumberOfWeights, boneSkinWeights, pWeightVec);

            m_SkinBoneIndices.add(pBoneIndices);
            m_SkinWeightVecs.add(pWeightVec);

    

            boneIndiceIndex = pPolygonSkinnedMesh.getBoneIndices(pBoneIndices);
            boneWeightIndex = pPolygonSkinnedMesh.getBoneWeights(pWeightVec);

            assignSkinningIndicesToVertex(pPolygonSkinnedMesh, vertexIndex, boneIndiceIndex, boneWeightIndex);

            boneSkinWeights.clear();


            vertexIndex++;
        }
    }


    void toBoneIndices(int maxNumberOfWeights, ArrayList boneSkinWeights, PBoneIndices pBoneIndices)
    {
        for (int a=0; a<boneSkinWeights.size(); a++)
            pBoneIndices.index[a] = m_BoneIndexStart + ((BoneSkinWeight)boneSkinWeights.get(a)).m_BoneIndex;
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
                pBoneSkinWeight = new BoneSkinWeight(0, 0.0f);
                boneSkinWeights.add(pBoneSkinWeight);
            }
        }
    }


    private void assignSkinningIndicesToVertex(PPolygonSkinnedMesh pPolygonSkinnedMesh,
                                               int positionIndex, int boneIndiceIndex, int boneWeightIndex)
    {
        int polygonIndex;
        PPolygon pPolygon;
        int vertexIndex;
        PPolygonVertexIndices pPolygonVertexIndices;
        PPolygonSkinnedVertexIndices pPolygonSkinnedVertexIndices;


        //  Iterate throuh all the Polygons.
        for (polygonIndex=0; polygonIndex<pPolygonSkinnedMesh.getPolygonCount(); polygonIndex++)
        {
            pPolygon = pPolygonSkinnedMesh.getPolygon(polygonIndex);

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


    //  Finds a source element in the Skin.
    public Source findSkinSourceElement(Skin pSkin, String elementName)
    {
        int a;
        Source pSource;
        
        for (a=0; a<pSkin.getSources().size(); a++)
        {
            pSource = pSkin.getSources().get(a);
            if (pSource.getId().endsWith(elementName))
                return(pSource);
        }

        return(null);
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



