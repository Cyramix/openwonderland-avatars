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

import java.util.List;
import java.lang.Double;

import java.util.logging.Logger;
import org.collada.colladaschema.LibraryVisualScenes;
import org.collada.colladaschema.Node;
import org.collada.colladaschema.VisualScene;
import org.collada.colladaschema.Rotate;
import org.collada.colladaschema.Matrix;
import org.collada.colladaschema.TargetableFloat3;
import org.collada.colladaschema.InstanceGeometry;
import org.collada.colladaschema.InstanceMaterial;
import org.collada.colladaschema.InstanceMaterial.BindVertexInput;
import org.collada.colladaschema.NodeType;
import org.collada.colladaschema.InstanceController;
import org.collada.colladaschema.BindMaterial.TechniqueCommon;
import org.collada.colladaschema.InstanceWithExtra;

import imi.loaders.collada.Collada;

import imi.scene.PMatrix;

import javax.xml.bind.JAXBElement;

import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationCycle;



/**
 *
 * @author paulby
 */
public class LibraryVisualScenesProcessor extends Processor
{

    //  Constructor.
    public LibraryVisualScenesProcessor(Collada pCollada, LibraryVisualScenes pNode, Processor pParent)
    {
        super(pCollada, pNode, pParent);
        logger.info("LibraryVisualScene");
        List<VisualScene> visualScenes = pNode.getVisualScenes();

        for (int a=0; a<visualScenes.size(); a++)
        {
            processVisualScene((VisualScene)visualScenes.get(a), a);
        }
    }
    
    void assignNameToAnimationGroup(String name)
    {
        SkeletonNode pSkeleton = m_pCollada.getSkeletonNode();
        if (pSkeleton != null)
        {
            if (pSkeleton.getAnimationComponent() != null &&
                pSkeleton.getAnimationComponent().getGroups() != null &&
                pSkeleton.getAnimationComponent().getGroups().size() > 0)
            {
                AnimationGroup pLastAnimationGroup = pSkeleton.getAnimationComponent().getGroups().get(pSkeleton.getAnimationComponent().getGroups().size()-1);
                AnimationCycle pAnimationCycle = pLastAnimationGroup.getCycle(0);
            
                pAnimationCycle.setName(name);

            }
        }
    }

    void processVisualScene(VisualScene pVisualScene, int index)
    {
        PColladaNode pColladaNode;
        Node pNode;

        //  Should we process the name of the VisualScene.
        if (index == 0)
        {
            String name = pVisualScene.getName();
            m_pCollada.setName(name);

            assignNameToAnimationGroup(name);
        }
        
        //  Iterate through all the Nodes in the VisualScene.
        for (int a=0; a<pVisualScene.getNodes().size(); a++)
        {
            pNode = (Node)pVisualScene.getNodes().get(a);

            pColladaNode = new PColladaNode();
            
            //  Process the Node.
            if (processNode(pColladaNode, pNode))
            {
                //  Add the root ColladaNode.
                m_pCollada.addColladaNode(pColladaNode);
            }

//            pColladaNode.dump();
        }
    }

    //  Processes a Node.
    private boolean processNode(PColladaNode pColladaNode, Node pNode)
    {

        pColladaNode.setName(pNode.getName());

        //  Process the assigned InstanceControllers.
        processInstanceControllers(pColladaNode, pNode);

        //  Read in the Node's matrix.
        readNodeMatrix(pColladaNode, pNode);


        //  Node might be a camera.
        if (readCameraInfo(pColladaNode, pNode))
            return(false);


        if (pNode.getType() == NodeType.JOINT)
        {
            pColladaNode.isJoint(true);
            pColladaNode.setJointName(pNode.getSid());

        }


        //  Read in the Material associated with the Bone.
        readNodeMaterial(pColladaNode, pNode);


        //  Read in the MeshInstance information for the Node.
        readNodeMeshInstance(pColladaNode, pNode);


        //  Read in the NodeInstance information for the Node.
        readNodeInstanceNode(pColladaNode, pNode);

        //  **********************
        //  Now, process all child nodes.
        //  **********************
        if (pNode.getNodes() != null)
        {
            PColladaNode pChildColladaNode;
                    
            for (int i = 0; i < pNode.getNodes().size(); i++)
            {
                pChildColladaNode = new PColladaNode();
                
                if (processNode(pChildColladaNode, (Node)pNode.getNodes().get(i)))
                    pColladaNode.addChildNode(pChildColladaNode);
            }
        }

        return(true);
    }


    private void processInstanceControllers(PColladaNode pColladaNode, Node pNode)
    {
        if (pNode.getInstanceControllers().size() == 0)
            return;

        int a, b;
        InstanceController pInstanceController;
        String controllerName;
        String skeletonString;
        
        for (a=0; a<pNode.getInstanceControllers().size(); a++)
        {
            pInstanceController = pNode.getInstanceControllers().get(a);

            controllerName = pInstanceController.getUrl();
            if (controllerName.startsWith("#"))
                controllerName = controllerName.substring(1);

            if (controllerName.endsWith("-skin"))
                controllerName = controllerName.substring(0, controllerName.length()-5);

            pColladaNode.setControllerName(controllerName);

            //  Loop through the skeletons.
            for (b=0; b<pInstanceController.getSkeletons().size(); b++)
            {
                skeletonString = pInstanceController.getSkeletons().get(b);
                if (skeletonString.startsWith("#"))
                    skeletonString = skeletonString.substring(1);

                pColladaNode.addSkeleton(skeletonString);
            }
        }
    }
                          
          
    private boolean readCameraInfo(PColladaNode pColladaNode, Node pNode)
    {
        if (pNode.getInstanceCameras() != null && pNode.getInstanceCameras().size() > 0)
        {
            String cameraName = pNode.getName();
            int a;
            InstanceWithExtra pInstanceWithExtra;
            String cameraParamsName = "";
            PMatrix cameraMatrix;
            PColladaCameraParams pCameraParams;
            PColladaCamera pCamera;
            
            for (a=0; a<pNode.getInstanceCameras().size(); a++)
            {
                pInstanceWithExtra = pNode.getInstanceCameras().get(a);
                
                cameraParamsName = pInstanceWithExtra.getUrl();
                if (cameraParamsName.startsWith("#"))
                    cameraParamsName = cameraParamsName.substring(1);
            }

            cameraMatrix = pColladaNode.getMatrix();

            //  Find the ColladaCameraParams which were already encountered.
            pCameraParams = m_pCollada.findColladaCameraParams(cameraParamsName);
            
            //  Create the ColladaCamera.
            pCamera = new PColladaCamera(cameraName, pCameraParams, cameraMatrix);

            //  Let the ColladaLoader know about the Camera.
            m_pCollada.addColladaCamera(pCamera);


            return(true);
        }

        return(false);
    }


                         
    private void readNodeMatrix(PColladaNode pColladaNode, Node pNode)
    {
        //  Process the Node's matrix parameters.
        int a = 0;
        Object pMatrixElement;
        Rotate pRotate;


        //  Iterate through all the translateAndMatrixesAndLookAts.
        for (a=0; a<pNode.getTranslatesAndMatrixesAndLookats().size(); a++)
        {
            pMatrixElement = pNode.getTranslatesAndMatrixesAndLookats().get(a);

            if (pMatrixElement instanceof Matrix)
            {
                Matrix pMatrix = (Matrix)pMatrixElement;
                float []matrixFloats = new float[16];

                for (int b=0; b<pMatrix.getValues().size(); b++)
                    matrixFloats[b] = ((Double)pMatrix.getValues().get(b)).floatValue();

                pColladaNode.setMatrix(matrixFloats);
            }
            else if (pMatrixElement instanceof JAXBElement)
            {
                JAXBElement pJAXBElement = (JAXBElement)pMatrixElement;
                
                if (pJAXBElement.getName().getLocalPart().equals("translate"))
                    parseTranslation((TargetableFloat3)pJAXBElement.getValue());
                
            }
            else if (pMatrixElement instanceof Rotate)
            {
                pRotate = (Rotate)pMatrixElement;
                String sidValue = pRotate.getSid();
                if (sidValue != null)
                {
                    if (sidValue.equals("jointOrientX"))
                        parseRotateJointOrient(pRotate);
                    else if (sidValue.equals("jointOrientY"))
                        parseRotateJointOrient(pRotate);
                    else if (sidValue.equals("jointOrientZ"))
                        parseRotateJointOrient(pRotate);
                }
            }
        }
    }

    private void readNodeMaterial(PColladaNode pColladaNode, Node pNode)
    {
        int a;
        TechniqueCommon pTechniqueCommon;

        if (pNode.getInstanceControllers() != null && pNode.getInstanceControllers().size() > 0)
        {
            InstanceController pInstanceController;
            
            for (a=0; a<pNode.getInstanceControllers().size(); a++)
            {
                pInstanceController = (InstanceController)pNode.getInstanceControllers().get(a);

                if (pInstanceController.getBindMaterial() != null)
                {
                    pTechniqueCommon = pInstanceController.getBindMaterial().getTechniqueCommon();

                    processNodeMaterial(pColladaNode, pNode, pTechniqueCommon);
                }
            }
        }
        else if (pNode.getInstanceGeometries() != null && pNode.getInstanceGeometries().size() > 0)
        {
            InstanceGeometry pInstanceGeometry;
            
            for (a=0; a<pNode.getInstanceGeometries().size(); a++)
            {
                pInstanceGeometry = (InstanceGeometry)pNode.getInstanceGeometries().get(a);

                // Now, iterate through all the bind Materials.
                if (pInstanceGeometry.getBindMaterial() != null)
                {
                    pTechniqueCommon = pInstanceGeometry.getBindMaterial().getTechniqueCommon();

                    processNodeMaterial(pColladaNode, pNode, pTechniqueCommon);
                }
            }
        }
    }

    // Now, iterate through all the bind Materials.
    private void processNodeMaterial(PColladaNode pColladaNode, Node pNode, TechniqueCommon pTechniqueCommon)
    {
        int                         c;
        InstanceMaterial            pInstanceMaterial;
        PColladaMaterialInstance    pMaterialInstance;
        String                      instanceName;
        String                      materialName;
    
        
        for (c=0; c<pTechniqueCommon.getInstanceMaterials().size(); c++)
        {
            pInstanceMaterial = (InstanceMaterial)pTechniqueCommon.getInstanceMaterials().get(c);

            instanceName = pInstanceMaterial.getSymbol();
            materialName = pInstanceMaterial.getTarget();

            pMaterialInstance = new PColladaMaterialInstance();

            pMaterialInstance.setInstanceName(instanceName);
            pMaterialInstance.setMaterialName(materialName);

            m_pCollada.addColladaMaterialInstance(pMaterialInstance);


            //  Assign the MaterialInstance to the ColladaNode.
            pColladaNode.setMaterialInstance(pMaterialInstance);

            if (pInstanceMaterial.getBindVertexInputs() != null)
            {
                int d;
                BindVertexInput pBindVertexInput;
                                
                for (d=0; d<pInstanceMaterial.getBindVertexInputs().size(); d++)
                {
                    pBindVertexInput = (BindVertexInput)pInstanceMaterial.getBindVertexInputs().get(d);
                                    
                    pMaterialInstance.addVertexInput(pBindVertexInput.getSemantic());
                }
            }
        }
    }

    private void readNodeMeshInstance(PColladaNode colladaNode, Node pNode)
    {
        if (pNode.getInstanceGeometries() != null && pNode.getInstanceGeometries().size() > 0)
        {
            InstanceGeometry instance = null;
            String instancedGeometryURL = null;
            // OK, so this logic is obviously flawed.. the loop is counter productive
//            for (int i = 0; i < pNode.getInstanceGeometries().size(); i++)
//            {
                instance = (InstanceGeometry)pNode.getInstanceGeometries().get(0); // Change to get i for looping

                instancedGeometryURL = instance.getUrl();
                if (instancedGeometryURL.startsWith("#"))
                    instancedGeometryURL = instancedGeometryURL.substring(1, instancedGeometryURL.length());

                colladaNode.setMeshURL(instancedGeometryURL);
                colladaNode.setMeshName(pNode.getName());
            //}
        }
    }

    //  Reads in the NodeInstance information for the Node.
    private void readNodeInstanceNode(PColladaNode pColladaNode, Node pNode)
    {
        if (pNode.getInstanceNodes() != null && pNode.getInstanceNodes().size() > 0)
        {
            int a;
            InstanceWithExtra pInstanceWithExtra;
            String instanceName;
            
            for (a=0; a<pNode.getInstanceNodes().size(); a++)
            {
                pInstanceWithExtra = pNode.getInstanceNodes().get(a);

                instanceName = pInstanceWithExtra.getUrl();
                if (instanceName.startsWith("#"))
                    instanceName = instanceName.substring(1);
            
                pColladaNode.setInstanceNodeName(instanceName);
            }
        }
    }
    
    private float []parseTranslation(TargetableFloat3 pFloat3)
    {
        float []values = new float[3];
        
        for (int a=0; a<pFloat3.getValues().size(); a++)
            values[a] = ((Double)pFloat3.getValues().get(a)).floatValue();

        return(values);
    }

    private float []parseRotateJointOrient(Rotate pRotate)
    {
        float []values = new float[4];
        
        for (int a=0; a<pRotate.getValues().size(); a++)
            values[a] = ((Double)pRotate.getValues().get(a)).floatValue();

        return(values);
    }

}



