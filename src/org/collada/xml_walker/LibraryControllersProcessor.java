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
import imi.scene.PNode;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.channel.PMatrix_JointChannel;



/**
 *
 * @author Chris Nagle
 */
public class LibraryControllersProcessor extends Processor
{
    private int                         m_BoneIndexStart = 0;

    //  Constructor.
    public LibraryControllersProcessor(Collada colladaRef, LibraryControllers libraryControllers, Processor parent)
    {
        super(colladaRef, libraryControllers, parent);

        for (Controller controller : libraryControllers.getControllers())
            processController(controller);
    }

    private void processController(Controller controller)
    {
        //  Create the SkinProcessor.
        processSkin(controller);
        // TODO process everything else
    }

    private void processSkin(Controller controller)
    {
        Skin skin = controller.getSkin();
        if (skin == null)
            return; // No skin to process

        String meshName = skin.getSource();
        if (meshName.startsWith("#")) // Indicates a URL
            meshName = meshName.substring(1);
        // HACK HACK HACK HACK HACK
        if (meshName.contains(":")) // scoped ID, just hack around it
        {
            meshName = meshName.substring(meshName.lastIndexOf(":") + 1);
        }

        float[] floatBuffer =  new float[16]; // Used for filling the PMatrix below
        PMatrix matrixBuffer = new PMatrix();

        PColladaSkin colladaSkinNode = new PColladaSkin(controller.getId());

        colladaSkinNode.setMeshName(meshName);


        //  Create the PolygonSkinnedMesh.
        //  Will have to convert the PolygonMesh to a PolygonSkinnedMesh.
        PPolygonMesh polyMesh = m_colladaRef.findPolygonMesh(meshName); // Could potentially be many
        if (polyMesh == null)
            System.out.println("Polymesh is not found, name is " + meshName);
        PPolygonSkinnedMesh skinnedMesh = new PPolygonSkinnedMesh(polyMesh);
        ArrayList<PPolygonSkinnedMesh> skinnedChildren = new ArrayList<PPolygonSkinnedMesh>();
        // handle any children
        for (PNode kid : polyMesh.getChildren())
            if (kid instanceof PPolygonMesh)
                skinnedChildren.add(new PPolygonSkinnedMesh((PPolygonMesh)kid));
        // Debugging / Diagnostic Output
//        logger.info("SkinnedParent : " + skinnedMesh.getName());
//        logger.info("SkinnedChildren : " + skinnedChildren.size());
//        for (PPolygonSkinnedMesh mesh : skinnedChildren)
//            logger.info("-Mesh " + mesh.getName()+ ", # influences: " + mesh.getNumberOfInfluences());

        //  Read in the BindMatrix and convert it into a pmatrix
        int counter = 0;
        for (Double d : skin.getBindShapeMatrix())
        {
            floatBuffer[counter] = d.floatValue();
            counter++;
        }
        matrixBuffer.set(floatBuffer);
        colladaSkinNode.setBindMatrix(matrixBuffer);


        //  Read in all the Joints.
        Source pSkinJoints = findSkinSourceElement(skin, "skin-joints");
        for (String boneName : pSkinJoints.getNameArray().getValues())
        {
            PColladaNode colladaJointNode = m_colladaRef.findJoint(boneName);
            if (colladaJointNode != null)
            {
                String jointName = colladaJointNode.getName();
                skinnedMesh.addJointName(jointName);
                for (PPolygonSkinnedMesh skinKid : skinnedChildren)
                    skinKid.addJointName(jointName);
            }
            else {
                System.out.println("Joint not found: " + boneName);
            }
        }


        //  Read in all the matrics.
        Source pSkinBindMatrices = findSkinSourceElement(skin, "skin-bind_poses");
        List<Double> valueList = pSkinBindMatrices.getFloatArray().getValues();
        for (int i = 0; i < valueList.size(); i++)
        {
            int matrixIndex = i%16;
            floatBuffer[matrixIndex] = valueList.get(i).floatValue();
            if (matrixIndex == 15) // Just finished a matrix
            {
                // add it to the skin node
                matrixBuffer.set(floatBuffer);
                colladaSkinNode.addBindMatrix(matrixBuffer);
            }
        }


        //  Read in all the SkinWeights.
        Source pSkinWeights = findSkinSourceElement(skin, "eights");
        valueList = pSkinWeights.getFloatArray().getValues();
        float[] skinWeights = new float[valueList.size()];
        counter = 0;
        for (Double d : valueList)
        {
            skinWeights[counter] = d.floatValue();
            counter++;
        }


        processVertexWeights(skin, skinnedMesh, skinWeights);
        for (PPolygonSkinnedMesh skinKid : skinnedChildren)
            processVertexWeights(skin, skinKid, skinWeights);

        // add stuff to the loader
        m_colladaRef.addPolygonSkinnedMesh(skinnedMesh);
        for (PPolygonSkinnedMesh skinKid : skinnedChildren)
            m_colladaRef.addPolygonSkinnedMesh(skinKid);
        m_colladaRef.addColladaSkin(colladaSkinNode);
    }


    private void processVertexWeights(Skin skin,
                                      PPolygonSkinnedMesh skinnedMesh,
                                      float []skinWeights)
    {
        int maxNumberOfWeights = 4;//m_colladaRef.getMaxNumberOfWeights();
        // Grab the vertex weights of this skin
        Skin.VertexWeights vertWeights = skin.getVertexWeights();

        //  Read in the VertexWeights.
        int[] vertexWeights = new int[vertWeights.getV().size()];
        int counter = 0;
        for (Long value : vertWeights.getV())
        {
            vertexWeights[counter] = value.intValue();
            counter++;
        }

        //  Read in the VertexJointCounts.
        int[] vertexBoneCounts = new int[vertWeights.getVcount().size()];
        counter = 0;
        for (BigInteger value : vertWeights.getVcount())
        {
            vertexBoneCounts[counter] = value.intValue();
            counter++;
        }


        ArrayList<BoneSkinWeight> boneSkinWeights = new ArrayList<BoneSkinWeight>();
        counter = 0;
        int vertexWeightsIndex = 0;
        for (int vertexBoneCount : vertexBoneCounts)
        {
            for (int i = 0; i < vertexBoneCount; i++)
            {
                boneSkinWeights.add(new BoneSkinWeight(vertexWeights[vertexWeightsIndex],
                                                       skinWeights[vertexWeights[vertexWeightsIndex+1]]));
                vertexWeightsIndex += 2;
            }

            //  Sort the array of BoneSkinWeights.
            if (boneSkinWeights.size() > 0)
                Collections.sort(boneSkinWeights);

            processJointSkinWeights(maxNumberOfWeights, boneSkinWeights);

            PBoneIndices boneIndices = new PBoneIndices();
            Vector3f weightVec = new Vector3f();

            //  Convert to BoneIndices.
            toBoneIndices(boneSkinWeights, boneIndices);
            //  Convert to Vector3f.
            toWeights(boneSkinWeights, weightVec);

            int boneIndiceIndex = skinnedMesh.addBoneIndices(boneIndices);
            int boneWeightIndex = skinnedMesh.getBoneWeights(weightVec);

            assignSkinningIndicesToVertex(skinnedMesh, counter, boneIndiceIndex, boneWeightIndex);

            boneSkinWeights.clear();

            counter++;
        }
    }





    private void toBoneIndices(ArrayList<BoneSkinWeight> boneSkinWeights, PBoneIndices boneIndices)
    {
        for (int a=0; a<boneSkinWeights.size(); a++)
            boneIndices.index[a] = m_BoneIndexStart + boneSkinWeights.get(a).m_BoneIndex;
    }


    private void toWeights(ArrayList<BoneSkinWeight> boneSkinWeights, Vector3f weightVec)
    {
        if (boneSkinWeights.size() > 0)
            weightVec.x = boneSkinWeights.get(0).m_fWeight;
        if (boneSkinWeights.size() > 1)
            weightVec.y = boneSkinWeights.get(1).m_fWeight;
        if (boneSkinWeights.size() > 2)
            weightVec.z = boneSkinWeights.get(2).m_fWeight;
    }


    private void processJointSkinWeights(int maxNumberOfWeights, ArrayList<BoneSkinWeight> boneSkinWeights)
    {
        int NumberOfWeightsToProcess = maxNumberOfWeights;
        float fWeightSum = 0.0f;

        if (NumberOfWeightsToProcess > boneSkinWeights.size())
            NumberOfWeightsToProcess = boneSkinWeights.size();

        // Normalize the weights based on the max number of influences
        for (int a=0; a<NumberOfWeightsToProcess; a++)
            fWeightSum += boneSkinWeights.get(a).m_fWeight;
        for (int a=0; a<NumberOfWeightsToProcess; a++)
            boneSkinWeights.get(a).m_fWeight /= fWeightSum;


        //  Resize to the correct amount
        while (boneSkinWeights.size() > maxNumberOfWeights)
            boneSkinWeights.remove(boneSkinWeights.size() - 1);
        while (boneSkinWeights.size() < maxNumberOfWeights)
            boneSkinWeights.add(new BoneSkinWeight(-1, 0.0f)); // May blow stuff up.
    }


    private void assignSkinningIndicesToVertex(PPolygonSkinnedMesh skinnedMesh,
                                               int positionIndex, int boneIndex, int weightIndex)
    {
        for (PPolygon poly : skinnedMesh.getPolygons())
        {
            for (PPolygonVertexIndices vertIndices : poly.getVertexCollection())
            {
                if (vertIndices.m_PositionIndex == positionIndex)
                {
                    PPolygonSkinnedVertexIndices pPolygonSkinnedVertexIndices = (PPolygonSkinnedVertexIndices)vertIndices;
                    pPolygonSkinnedVertexIndices.m_BoneIndicesIndex = boneIndex;
                    pPolygonSkinnedVertexIndices.m_BoneWeightIndex = weightIndex;
                }
            }
        }
    }


    //  Finds i source element in the Skin.
    private Source findSkinSourceElement(Skin skin, String elementName)
    {
        for (Source source : skin.getSources())
            if (source.getId().endsWith(elementName))
                return source;
        return null;
    }

    private class BoneSkinWeight implements Comparable
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



