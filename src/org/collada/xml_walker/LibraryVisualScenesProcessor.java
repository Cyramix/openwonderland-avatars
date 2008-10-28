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
            if (processNode("", pColladaNode, pNode))
            {
                //  Add the root ColladaNode.
                m_pCollada.addColladaNode(pColladaNode);
            }

//            pColladaNode.dump();
        }
    }

    //  Processes a Node.
    private boolean processNode(String spacing, PColladaNode pColladaNode, Node pNode)
    {
        int a;

        //System.out.println(spacing + "Node:  " + pNode.getId());


        pColladaNode.setName(pNode.getName());


        //  Process the assigned InstanceControllers.
        processInstanceControllers(spacing, pColladaNode, pNode);


        if (m_pCollada.getPrintStats())
            System.out.println("ColladaNode='" + pColladaNode.getName() + "', ControllerName='" + pColladaNode.getControllerName() + "'");
        
        //  Read in the Node's matrix.
        readNodeMatrix(spacing, pColladaNode, pNode);


        //  Node might be a camera.
        if (readCameraInfo(spacing, pColladaNode, pNode))
            return(false);


        if (pNode.getType() == NodeType.JOINT)
        {
            pColladaNode.isJoint(true);
            pColladaNode.setJointName(pNode.getSid());

        }


        //  Read in the Material associated with the Bone.
        readNodeMaterial(spacing, pColladaNode, pNode);


        //  Read in the MeshInstance information for the Node.
        readNodeMeshInstance(spacing, pColladaNode, pNode);


        //  Read in the NodeInstance information for the Node.
        readNodeInstanceNode(spacing, pColladaNode, pNode);


/*
        if (pNode.getInstanceGeometries() != null && pNode.getInstanceGeometries().size() > 0)
        {
            int b;
            InstanceGeometry pInstanceGeometry;
            String meshName;
            
            for (b=0; b<pNode.getInstanceGeometries().size(); b++)
            {
                pInstanceGeometry = (InstanceGeometry)pNode.getInstanceGeometries().get(b);

                meshName = pInstanceGeometry.getUrl();
                if (meshName.startsWith("#"))
                    meshName = meshName.substring(1, meshName.length());

                pColladaNode.setMeshName(meshName);
            
                // Now, iterate through all the bind Materials.
                if (pInstanceGeometry.getBindMaterial() != null)
                {
                    if (pInstanceGeometry.getBindMaterial().getTechniqueCommon() != null)
                    {
                        int c;
                        InstanceMaterial pInstanceMaterial;
                        String materialSymbol;
                        String materialTarget;
                        PColladaMaterialInstance pMaterialInstance;
                                
                        for (c=0; c<pInstanceGeometry.getBindMaterial().getTechniqueCommon().getInstanceMaterials().size(); c++)
                        {
                            pInstanceMaterial = (InstanceMaterial)pInstanceGeometry.getBindMaterial().getTechniqueCommon().getInstanceMaterials().get(c);

                            pMaterialInstance = new PColladaMaterialInstance();

                            materialSymbol = pInstanceMaterial.getSymbol();
                            materialTarget = pInstanceMaterial.getTarget();

                            pMaterialInstance.setMeshMaterialName(materialSymbol);
                            pMaterialInstance.setEffectName(materialTarget);

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
                }
            }
        }

        if (pNode.getInstanceNodes() != null && pNode.getInstanceNodes().size() > 0)
        {
            int bb = 0;
        }
*/


        //  **********************
        //  Now, process all child nodes.
        //  **********************
        if (pNode.getNodes() != null)
        {
            PColladaNode pChildColladaNode;
                    
            for (a=0; a<pNode.getNodes().size(); a++)
            {
                pChildColladaNode = new PColladaNode();
                
                if (processNode(spacing + "   ", pChildColladaNode, (Node)pNode.getNodes().get(a)))
                    pColladaNode.addChildNode(pChildColladaNode);
            }
        }

        return(true);
    }


    private void processInstanceControllers(String spacing, PColladaNode pColladaNode, Node pNode)
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

//            if (controllerName.endsWith("-skin"))
//                controllerName = controllerName.substring(0, controllerName.length()-5);

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
                          
          
    private boolean readCameraInfo(String spacing, PColladaNode pColladaNode, Node pNode)
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
                
/*
         <node id="Camera" name="Camera">
            <matrix>
               0.678325 0.054385 0.732747 205.150618
               0.734762 -0.050208 -0.676464 -204.150926
               0.000000 0.997257 -0.074017 59.925539
               0.000000 0.000000 0.000000 1.000000
            </matrix>
            <instance_camera url="#Camera-camera"/>
         </node>
*/

            return(true);
        }

        return(false);
    }


                         
    private void readNodeMatrix(String spacing, PColladaNode pColladaNode, Node pNode)
    {
        //  Process the Node's matrix parameters.
        int a = 0;
        Object pMatrixElement;
        Rotate pRotate;
        float []pTranslation = null;
        float []pRotateJointOrientX = null;
        float []pRotateJointOrientY = null;
        float []pRotateJointOrientZ = null;


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
                {
                    pTranslation = parseTranslation((TargetableFloat3)pJAXBElement.getValue());
                    //System.out.println(spacing + "   translation:  (" + pTranslation[0] + ", " + pTranslation[1] + ", " + pTranslation[2] + ")");
                }
                
            }
            else if (pMatrixElement instanceof Rotate)
            {
                pRotate = (Rotate)pMatrixElement;
                
                if (pRotate.getSid().equals("jointOrientX"))
                {
                    pRotateJointOrientX = parseRotateJointOrient(pRotate);
                    //System.out.println(spacing + "   jointOrientX:  (" + pRotateJointOrientX[0] + ", " + pRotateJointOrientX[1] + ", " + pRotateJointOrientX[2] + ", " + pRotateJointOrientX[3] + ")");
                }
                else if (pRotate.getSid().equals("jointOrientY"))
                {
                    pRotateJointOrientY = parseRotateJointOrient(pRotate);
                    //System.out.println(spacing + "   jointOrientY:  (" + pRotateJointOrientY[0] + ", " + pRotateJointOrientY[1] + ", " + pRotateJointOrientY[2] + ", " + pRotateJointOrientY[3] + ")");
                }
                else if (pRotate.getSid().equals("jointOrientZ"))
                {
                    pRotateJointOrientZ = parseRotateJointOrient(pRotate);
                    //System.out.println(spacing + "   jointOrientZ:  (" + pRotateJointOrientZ[0] + ", " + pRotateJointOrientZ[1] + ", " + pRotateJointOrientZ[2] + ", " + pRotateJointOrientZ[3] + ")");
                }
            }
        }
    }

    private void readNodeMaterial(String spacing, PColladaNode pColladaNode, Node pNode)
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

                    //System.out.println("hhhhhhhhhhhhhhhhhhhhh");
                    //System.out.println("   InstanceController has MaterialInstance.");
                    //System.out.println("hhhhhhhhhhhhhhhhhhhhh");
                    //System.out.flush();
                    processNodeMaterial(spacing, pColladaNode, pNode, pTechniqueCommon);
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

                    //System.out.println("hhhhhhhhhhhhhhhhhhhhh");
                    //System.out.println("   InstanceGeometry has MaterialInstance.");
                    //System.out.println("hhhhhhhhhhhhhhhhhhhhh");
                    //System.out.flush();
                    processNodeMaterial(spacing, pColladaNode, pNode, pTechniqueCommon);
                }
            }
        }
      /*
        <instance_controller url="#HeadShape-skin">
          <skeleton>#Neck</skeleton>
          <bind_material>
            <technique_common>
              <instance_material symbol="lambert2SG" target="#blinn1">
                <bind_vertex_input semantic="TEX0" input_semantic="TEXCOORD" input_set="0"/>
                <bind_vertex_input semantic="TEX1" input_semantic="TEXCOORD" input_set="0"/>
                <bind_vertex_input semantic="TEX2" input_semantic="TEXCOORD" input_set="0"/>
              </instance_material>
            </technique_common>
          </bind_material>
        </instance_controller>
      */
    }

    // Now, iterate through all the bind Materials.
    private void processNodeMaterial(String spacing, PColladaNode pColladaNode, Node pNode, TechniqueCommon pTechniqueCommon)
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

    private void readNodeMeshInstance(String spacing, PColladaNode pColladaNode, Node pNode)
    {
        if (pNode.getInstanceGeometries() != null && pNode.getInstanceGeometries().size() > 0)
        {
            int b;
            InstanceGeometry pInstanceGeometry;
            String meshName;
            
            for (b=0; b<pNode.getInstanceGeometries().size(); b++)
            {
                pInstanceGeometry = (InstanceGeometry)pNode.getInstanceGeometries().get(b);

                meshName = pInstanceGeometry.getUrl();
                if (meshName.startsWith("#"))
                    meshName = meshName.substring(1, meshName.length());

                pColladaNode.setMeshName(meshName);
            }
        }
    }

    //  Reads in the NodeInstance information for the Node.
    private void readNodeInstanceNode(String spacing, PColladaNode pColladaNode, Node pNode)
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



