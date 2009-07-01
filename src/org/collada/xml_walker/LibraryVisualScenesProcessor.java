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

import imi.loaders.Collada;

import imi.scene.PMatrix;

import javax.xml.bind.JAXBElement;

import imi.scene.SkeletonNode;
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

        List<VisualScene> visualScenes = pNode.getVisualScenes();

        int index = 0;
        for (VisualScene vs : visualScenes)
        {
            processVisualScene(vs, index);
            index++;
        }
    }
    
    void assignNameToAnimationGroup(String name)
    {
        SkeletonNode skeleton = m_colladaRef.getSkeletonNode();
        if (skeleton != null)
        {
            if (skeleton.getAnimationComponent() != null &&
                skeleton.getAnimationComponent().getGroups() != null &&
                skeleton.getAnimationComponent().getGroupCount() > 0)
            {
                AnimationGroup lastGroup = skeleton.getAnimationComponent().getLastGroup();
                AnimationCycle animCycle = lastGroup.getCycle(0);
            
                animCycle.setName(name);

            }
        }
    }

    void processVisualScene(VisualScene theScene, int index)
    {
        //  Should we process the name of the VisualScene.
        if (index == 0)
        {
            String name = theScene.getName();
            m_colladaRef.setName(name);

            assignNameToAnimationGroup(name);
        }
        for (Node currentNode : theScene.getNodes())
        {
            PColladaNode colladaNode = new PColladaNode();
            if (processNode(colladaNode, currentNode) == true)
                m_colladaRef.addColladaNode(colladaNode);
        }
    }

    //  Processes a Node.
    private boolean processNode(PColladaNode colladaNode, Node currentNode)
    {

        colladaNode.setName(currentNode.getName());

        readBindMaterials(colladaNode, currentNode);

        //  Process the assigned InstanceControllers.
        processInstanceControllers(colladaNode, currentNode);

        //  Read in the Node's matrix.
        readNodeMatrix(colladaNode, currentNode);


        //  Node might be a camera.
        if (readCameraInfo(colladaNode, currentNode))
            return(false);


        if (currentNode.getType() == NodeType.JOINT)
        {
            colladaNode.isJoint(true);
            colladaNode.setJointName(currentNode.getSid());

        }

        //  Read in the MeshInstance information for the Node.
        readNodeMeshInstance(colladaNode, currentNode);


        //  Read in the NodeInstance information for the Node.
        readNodeInstanceNode(colladaNode, currentNode);

        //  **********************
        //  Now, process all child nodes.
        //  **********************
        if (currentNode.getNodes() != null)
        {
            for (Node kid : currentNode.getNodes())
            {
                PColladaNode newNode = new PColladaNode();
                if (processNode(newNode, kid))
                    colladaNode.addChildNode(newNode);
            }
        }

        return true;
    }

    private void processInstanceControllers(PColladaNode colladaNode, Node currentNode)
    {
        if (currentNode.getInstanceControllers().size() == 0)
            return;

        String controllerName = null;
        
        for (InstanceController instController : currentNode.getInstanceControllers())
        {
            controllerName = instController.getUrl();
            if (controllerName.startsWith("#"))
                controllerName = controllerName.substring(1);

            if (controllerName.endsWith("-skin"))
                controllerName = controllerName.substring(0, controllerName.length()-5);

            colladaNode.setControllerName(controllerName);
            // handle the bind material
            List<InstanceMaterial> materialList = instController.getBindMaterial().getTechniqueCommon().getInstanceMaterials();
//            if (materialList != null && materialList.size() > 0)
//                bindMaterial = materialList.get(0);
            //  Loop through the skeletons.
            for (String skeletonName : instController.getSkeletons())
            {
                if (skeletonName.startsWith("#"))
                    skeletonName = skeletonName.substring(1);

                colladaNode.addSkeleton(skeletonName);
            }
        }
    }
                          
          
    private boolean readCameraInfo(PColladaNode pColladaNode, Node pNode)
    {
        if (pNode.getInstanceCameras() != null && pNode.getInstanceCameras().size() > 0)
        {
            String cameraName = pNode.getName();
            String cameraParamsName = null;
            PMatrix cameraMatrix = null;
            PColladaCameraParams cameraParams = null;
            PColladaCamera camera = null;
            
            for (InstanceWithExtra extraInst : pNode.getInstanceCameras())
            {
                cameraParamsName = extraInst.getUrl();
                if (cameraParamsName.startsWith("#"))
                    cameraParamsName = cameraParamsName.substring(1);
            }

            cameraMatrix = pColladaNode.getMatrix();

            //  Find the ColladaCameraParams which were already encountered.
            cameraParams = m_colladaRef.findColladaCameraParams(cameraParamsName);
            
            //  Create the ColladaCamera.
            camera = new PColladaCamera(cameraName, cameraParams, cameraMatrix);

            //  Let the ColladaLoader know about the Camera.
            m_colladaRef.addColladaCamera(camera);


            return(true);
        }
        else
            return(false);
    }


                         
    private void readNodeMatrix(PColladaNode colladaNode, Node standardNode)
    {
        //  Process the Node's matrix parameters.
        Rotate pRotate = null;
        //  Iterate through all the translateAndMatrixesAndLookAts.
        for (Object matrixObject : standardNode.getTranslatesAndMatrixesAndLookats())
        {
            if (matrixObject instanceof Matrix)
            {
                Matrix theMatrix = (Matrix)matrixObject;
                float []matrixFloats = new float[16];

                int index = 0;
                for (Double val : theMatrix.getValues())
                {
                    matrixFloats[index] = val.floatValue();
                    index++;
                }

                colladaNode.setMatrix(matrixFloats);
            }
            else if (matrixObject instanceof JAXBElement)
            {
                JAXBElement pJAXBElement = (JAXBElement)matrixObject;
                
                if (pJAXBElement.getName().getLocalPart().equals("translate"))
                    parseTranslation((TargetableFloat3)pJAXBElement.getValue());
                
            }
            else if (matrixObject instanceof Rotate)
            {
                pRotate = (Rotate)matrixObject;
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

    private void readBindMaterials(PColladaNode colladaNode, Node standardNode)
    {
        TechniqueCommon commonTech = null;

        if (standardNode.getInstanceControllers() != null && standardNode.getInstanceControllers().size() > 0)
        {
            for (InstanceController controller : standardNode.getInstanceControllers())
            {
                if (controller.getBindMaterial() != null)
                {
                    commonTech = controller.getBindMaterial().getTechniqueCommon();
                    processNodeMaterial(colladaNode, commonTech);
                }
            }
        }
        else if (standardNode.getInstanceGeometries() != null && standardNode.getInstanceGeometries().size() > 0)
        {
            for (InstanceGeometry instGeom : standardNode.getInstanceGeometries())
            {
                // Now, iterate through all the bind Materials.
                if (instGeom.getBindMaterial() != null)
                {
                    commonTech = instGeom.getBindMaterial().getTechniqueCommon();
                    processNodeMaterial(colladaNode, commonTech);
                }
            }
        }
    }

    // Now, iterate through all the bind Materials.
    private void processNodeMaterial(PColladaNode pColladaNode, TechniqueCommon pTechniqueCommon)
    {
        PColladaMaterialInstance    pMaterialInstance = null;
        String                      materialSymbol = null;
        String                      materialTargetURL = null;
    
        
        for (InstanceMaterial instMat : pTechniqueCommon.getInstanceMaterials())
        {
            materialSymbol = instMat.getSymbol();
            materialTargetURL = instMat.getTarget();

            pMaterialInstance = new PColladaMaterialInstance();

            pMaterialInstance.setInstanceSymbolString(materialSymbol);
            pMaterialInstance.setTargetMaterialURL(materialTargetURL);
            m_colladaRef.addColladaMaterialInstance(pMaterialInstance);


            //  Assign the MaterialInstance to the ColladaNode.
            pColladaNode.setMaterial(m_colladaRef.findColladaMaterialByIdentifier(pMaterialInstance.getTargetMaterialURL()));

            if (instMat.getBindVertexInputs() != null)
            {                
                for (BindVertexInput bvi : instMat.getBindVertexInputs())
                    pMaterialInstance.addVertexInput(bvi.getSemantic());
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
//                ColladaMaterial colladaMaterial = m_colladaRef.findColladaMaterialByIdentifier(bindMaterial.getTarget().substring(1));
//                colladaNode.setMaterial(colladaMaterial);
            //}
        }
    }

    //  Reads in the NodeInstance information for the Node.
    private void readNodeInstanceNode(PColladaNode colladaNode, Node standardNode)
    {
        if (standardNode.getInstanceNodes() != null && standardNode.getInstanceNodes().size() > 0)
        {
            String instanceName = null;
            
            for (InstanceWithExtra instanceNode : standardNode.getInstanceNodes())
            {
                instanceName = instanceNode.getUrl();
                if (instanceName.startsWith("#"))
                    instanceName = instanceName.substring(1);
            
                colladaNode.setInstanceNodeName(instanceName);
            }
        }
    }
    
    private float []parseTranslation(TargetableFloat3 pFloat3)
    {
        float []values = new float[3];
        
        for (int a=0; a<pFloat3.getValues().size(); a++)
            values[a] = pFloat3.getValues().get(a).floatValue();

        return values;
    }

    private float []parseRotateJointOrient(Rotate pRotate)
    {
        float []values = new float[4];
        
        for (int a=0; a<pRotate.getValues().size(); a++)
            values[a] = pRotate.getValues().get(a).floatValue();

        return(values);
    }

}



