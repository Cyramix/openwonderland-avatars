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
package imi.loaders.collada;


import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.collada.colladaschema.Asset;
import org.collada.colladaschema.COLLADA;
import org.collada.xml_walker.ProcessorFactory;
import org.collada.xml_walker.PColladaNode;
import org.collada.xml_walker.PColladaMaterialInstance;
import org.collada.xml_walker.PColladaImage;
import org.collada.xml_walker.PColladaMaterial;
import org.collada.xml_walker.PColladaAnimatedItem;
import org.collada.xml_walker.PColladaCameraParams;
import org.collada.xml_walker.PColladaCamera;
import org.collada.xml_walker.PColladaSkin;

import org.collada.colladaschema.LibraryCameras;
import org.collada.colladaschema.LibraryImages;
import org.collada.colladaschema.LibraryEffects;
import org.collada.colladaschema.LibraryMaterials;
import org.collada.colladaschema.LibraryAnimations;
import org.collada.colladaschema.LibraryVisualScenes;
import org.collada.colladaschema.LibraryGeometries;
import org.collada.colladaschema.LibraryControllers;
import org.collada.colladaschema.LibraryNodes;

import imi.scene.polygonmodel.parts.*;
import imi.scene.polygonmodel.*;

import imi.scene.PScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;

import imi.utils.FileUtils;

import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PPolygonMeshInstance;

import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;

import imi.scene.animation.AnimationGroup;
import imi.scene.animation.COLLADA_JointChannel;
        
import imi.scene.PJoint;


import imi.scene.shader.programs.VertexDeformer;
import imi.scene.utils.tree.PPolygonMeshAssemblingProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.net.URL;
import javolution.util.FastMap;



/**
 *
 * @author paulby
 */
public class Collada
{
    private static Logger logger = Logger.getLogger("imi.loaders.collada");

    private String                              m_Name = "";
    private List                                m_Libraries = null; 
    
    private URL                                 m_fileLocation = null;

    //  Pointers to the various Libraries found in the collada file.
    LibraryCameras                              m_pLibraryCameras;
    LibraryImages                               m_pLibraryImages;
    LibraryEffects                              m_pLibraryEffects;
    LibraryMaterials                            m_pLibraryMaterials;
    LibraryAnimations                           m_pLibraryAnimations;
    LibraryVisualScenes                         m_pLibraryVisualScenes;
    LibraryGeometries                           m_pLibraryGeometries;
    LibraryControllers                          m_pLibraryControllers;
    LibraryNodes                                m_pLibraryNodes;

    FastMap<String, PColladaImage>              m_Images = new FastMap<String, PColladaImage>();
    private ArrayList<PColladaImage>            m_ColladaImages = new ArrayList<PColladaImage>();

    FastMap<String, PColladaMaterial>           m_Materials = new FastMap<String, PColladaMaterial>();
    private ArrayList                           m_ColladaMaterials = new ArrayList();

    FastMap<String, PColladaMaterialInstance>   m_MaterialInstances = new FastMap<String, PColladaMaterialInstance>();
    private ArrayList                           m_ColladaMaterialInstances = new ArrayList();

    SkeletonNode                                m_pSkeletonNode;
            

    
    
    private PScene                              m_pScene = null;

    public PPolygonModel                        m_pCurrentPolygonModel = null;


    //  Constains the PolygonMeshes loaded.
    public ArrayList                            m_PolygonMeshes = new ArrayList();

    //  Contains the PolygonSkinnedMeshes loaded.
    public ArrayList                            m_PolygonSkinnedMeshes = new ArrayList();

    public ArrayList                            m_ColladaSkins = new ArrayList();

    private ArrayList                           m_ColladaNodes = new ArrayList();

    private ArrayList                           m_FactoryColladaNodes = new ArrayList();



    private int                                 m_MaxNumberOfWeights = 4;
    private ArrayList                           m_ColladaAnimatedItems = new ArrayList();
    private ArrayList                           m_ColladaCameraParams = new ArrayList();
    private ArrayList                           m_ColladaCameras = new ArrayList();

    private boolean                             m_bLoadRig = false;
    private boolean                             m_bLoadGeometry = true;
    private boolean                             m_bLoadAnimations = false;
    private boolean                             m_bAddSkinnedMeshesToSkeleton = true;
    
    private boolean                             m_bPrintStats = false;


    
    

    //  *****************************************
    //  Public methods.
    //  *****************************************

    //  Constructor.
    public Collada()
    {
    }

    /**
     * Explicitly controlled construction
     * @param params The parameters to use for the loader
     */
    public Collada(ColladaLoaderParams params)
    {
        applyConfiguration(params);
    }

    //  Gets the name of the Collada file.
    public String getName()
    {
        return(m_Name);
    }

    //  Sets the name of the Collada file.
    public void setName(String name)
    {
        m_Name = name;
    }


    //  Retrieves boolean indicating whether stats should be printed.
    public boolean getPrintStats()
    {
        return(m_bPrintStats);
    }

    //  Sets the boolean indicating whether stats should be printed.
    public void setPrintStats(boolean bPrintStats)
    {
        m_bPrintStats = bPrintStats;
    }

    
    
    public void setLoadFlags(boolean bLoadRig, boolean bLoadGeometry, boolean bLoadAnimations)
    {
        setLoadRig(bLoadRig);
        setLoadGeometry(bLoadGeometry);
        setLoadAnimations(bLoadAnimations);
    }

    
    //  Gets the boolean indicating whether the Rig should be loaded.
    public boolean getLoadRig()
    {
        return(m_bLoadRig);
    }
    //  Sets the boolean indicating whether the Rig should be loaded.
    public void setLoadRig(boolean bLoadRig)
    {
        m_bLoadRig = bLoadRig;
    }

    
    //  Gets the boolean indicating whether the Geometry should be loaded.
    public boolean getLoadGeometry()
    {
        return(m_bLoadGeometry);
    }
    //  Sets the boolean indicating whether the Geometry should be loaded.
    public void setLoadGeometry(boolean bLoadGeometry)
    {
        m_bLoadGeometry = bLoadGeometry;
    }

    public PScene getPScene()
    {
        return m_pScene;
    }
    
    //  Gets the boolean indicating whether the Animations should be loaded.
    public boolean getLoadAnimations()
    {
        return(m_bLoadAnimations);
    }
    //  Sets the boolean indicating whether the Animations should be loaded.
    public void setLoadAnimations(boolean bLoadAnimations)
    {
        m_bLoadAnimations = bLoadAnimations;
    }

    //  Gets the boolean indicating whether the SkinnedMeshes should be added to the Skeleton.
    public boolean getAddSkinnedMeshesToSkeleton()
    {
        return(m_bAddSkinnedMeshesToSkeleton);
    }

    //  Sets the boolean indicating whether the SkinnedMeshes should be added to the Skeleton.
    public void setAddSkinnedMeshesToSkeleton(boolean bAddSkinnedMeshesToSkeleton)
    {
        m_bAddSkinnedMeshesToSkeleton = bAddSkinnedMeshesToSkeleton;
    }



    public boolean load(URL colladaFile)
    {
        m_fileLocation = colladaFile;
        try
        {
            javax.xml.bind.JAXBContext jc = javax.xml.bind.JAXBContext.newInstance("org.collada.colladaschema");
            javax.xml.bind.Unmarshaller unmarshaller = jc.createUnmarshaller();
            org.collada.colladaschema.COLLADA collada = (org.collada.colladaschema.COLLADA) unmarshaller.unmarshal(colladaFile);
            return doLoad(collada);
        }
        catch (JAXBException ex)
        {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            return false;
        }
    }

    
    public boolean load(PScene pScene, URL colladaFile)
    {
        m_fileLocation = colladaFile;
        try
        {
            m_pScene = pScene;
            m_pScene.setUseRepository(false); // the repository will extract the data later

            javax.xml.bind.JAXBContext jc = javax.xml.bind.JAXBContext.newInstance("org.collada.colladaschema");
            javax.xml.bind.Unmarshaller unmarshaller = jc.createUnmarshaller();
            org.collada.colladaschema.COLLADA collada =
                    (org.collada.colladaschema.COLLADA) unmarshaller.unmarshal(colladaFile);

            return(doLoad(collada));
        }
        catch (JAXBException ex)
        {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
        
        return(false);
    }

    public boolean loadSkinnedMesh(PScene pScene, URL location)
    {

        boolean bResult = load(pScene, location);

        return(bResult);
    }


    private boolean doLoad(COLLADA collada)
    {
        Asset asset = collada.getAsset();

        m_Libraries = collada.getLibraryLightsAndLibraryGeometriesAndLibraryAnimationClips();

        m_pLibraryCameras       = getInstanceOfLibraryCameras();
        m_pLibraryImages        = getInstanceOfLibraryImages();
        m_pLibraryEffects       = getInstanceOfLibraryEffects();
        m_pLibraryMaterials     = getInstanceOfLibraryMaterials();
        m_pLibraryAnimations    = getInstanceOfLibraryAnimations();
        m_pLibraryVisualScenes  = getInstanceOfLibraryVisualScenes();
        m_pLibraryGeometries    = getInstanceOfLibraryGeometries();
        m_pLibraryControllers   = getInstanceOfLibraryControllers();
        m_pLibraryNodes         = getInstanceOfLibraryNodes();

        //  Create the SkeletonNode if we're loading the rig.
        if (m_bLoadRig)
        {
            m_pSkeletonNode = createSkeletonNode();
        }
        //  Need to process these Libraries in this order.
        if (m_bLoadGeometry)
        {
            ProcessorFactory.createProcessor(this, m_pLibraryCameras, null);
            ProcessorFactory.createProcessor(this, m_pLibraryImages, null);
            ProcessorFactory.createProcessor(this, m_pLibraryEffects, null);
            ProcessorFactory.createProcessor(this, m_pLibraryMaterials, null);
        }

        //  Only load the Animations if we should.
        if (m_bLoadAnimations)
            ProcessorFactory.createProcessor(this, m_pLibraryAnimations, null);

        if (m_bLoadGeometry)
            ProcessorFactory.createProcessor(this, m_pLibraryNodes, null);

        //  Preprocess the node hiearchy.
        //  Builds the node hiearchy.
        ProcessorFactory.createProcessor(this, m_pLibraryVisualScenes, null);

        //  Gets the load option.
        if (m_bLoadGeometry)
        {
            ProcessorFactory.createProcessor(this, m_pLibraryGeometries, null);
            ProcessorFactory.createProcessor(this, m_pLibraryControllers, null);
        }

        if (m_bLoadRig)
            processRig();

        if (m_bLoadGeometry)
            processGeometry();

        if (m_pSkeletonNode == null)
            processColladaNodes();

        // Submit the geometry of every PPolygonMesh we encounter
        TreeTraverser.breadthFirst(m_pScene, new PPolygonMeshAssemblingProcessor());


        return(true);
    }



    //  Processes the Rig.
    private PNode processRig()
    {
        int a;
        PColladaNode pColladaNode;
        PNode pRootNode = new PNode("Untitled", new PTransform(new PMatrix()));

        for (a=0; a<m_ColladaNodes.size(); a++)
        {
            pColladaNode = (PColladaNode)m_ColladaNodes.get(a);
            if (pColladaNode.isJoint())
                processRig(pRootNode, pColladaNode, false);
        }

        m_pSkeletonNode.setSkeletonRoot(pRootNode);
        m_pScene.addInstanceNode(m_pSkeletonNode);

        return(pRootNode);
    }

    private PNode processRig(PNode pParentNode, PColladaNode pColladaNode, boolean bIgnoreMeshInstances)
    {
        String meshName = pColladaNode.getMeshName();
        String nodeInstanceName = pColladaNode.getInstanceNodeName();

        PPolygonMeshInstance pMeshInstance;
        PNode pThisNode = null;

        try
        {
            //  ****  MeshInstance Node.
            if (meshName.length() > 0)
            {
                //System.out.println("MeshName:  " + meshName);
                if (!bIgnoreMeshInstances)
                {
                    PPolygonMesh pPolygonMesh = findPolygonMesh(meshName);
                    if (pPolygonMesh != null)
                    {
                        //  Create a MeshInstance.
                        pMeshInstance = createMeshInstance(pPolygonMesh, pColladaNode);

                        pThisNode = pMeshInstance;
                    }
                }
            }

            //  ****  NodeInstance Node.
            else if (nodeInstanceName.length() > 0)
            {
                PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);
                if (pInstancedColladaNode != null)
                {
                    //pThisNode = m_pScene.processJoint(null, pColladaNode.getMatrix());
                    pThisNode = new PNode(pColladaNode.getName());
                    pThisNode.setTransform(new PTransform());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pColladaNode = pInstancedColladaNode;
                }
            }

            //  If there is no Mesh, then this is a joint.
            else
            {
                if (pColladaNode.isJoint())
                {
                    //  Create a SkinnedMeshJoint.
                    SkinnedMeshJoint pSkinnedMeshJoint = new SkinnedMeshJoint();

                    pSkinnedMeshJoint.setTransform(new PTransform());
                    pSkinnedMeshJoint.setName(pColladaNode.getName());
                    pSkinnedMeshJoint.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pThisNode = pSkinnedMeshJoint;
                }
                else
                {
                    //  Just create a Node.
                    pThisNode = new PJoint(new PTransform(pColladaNode.getMatrix()));
                    pThisNode.setName(pColladaNode.getName());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());
                }
            }

            if (pThisNode != null)
            {
                if (pParentNode != null)
                    pParentNode.addChild(pThisNode);

                //  Iterate through all the child Nodes.
                int a;
                PColladaNode pChildColladaNode;

                for (a=0; a<pColladaNode.getChildNodeCount(); a++)
                {
                    pChildColladaNode = pColladaNode.getChildNode(a);

                    processRig(pThisNode, pChildColladaNode, bIgnoreMeshInstances);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(pThisNode);
    }



    private void processGeometry()
    {
        int a;
        PPolygonSkinnedMesh pPolygonSkinnedMesh;

//        System.out.println("Processing SkinnedMeshes for SkeletonNode.");
        for (a=0; a<getPolygonSkinnedMeshCount(); a++)
        {
            pPolygonSkinnedMesh = getPolygonSkinnedMesh(a);

            if (m_bAddSkinnedMeshesToSkeleton)
                m_pSkeletonNode.addChild(pPolygonSkinnedMesh);

            pPolygonSkinnedMesh.linkJointsToSkeletonNode(m_pSkeletonNode);
        }

        if (m_pSkeletonNode != null)
            m_pSkeletonNode.setDirty(true, true);
    }




    //  ********************************
    //  SkeletonNode methods.
    //  ********************************

    //  Creates the SkeletonNode.
    public SkeletonNode createSkeletonNode()
    {
        if (m_pSkeletonNode == null)
            m_pSkeletonNode = new SkeletonNode("Untitled");

        return(m_pSkeletonNode);
    }

    //  Gets the SkeletonNode.
    public SkeletonNode getSkeletonNode()
    {
        return(m_pSkeletonNode);
    }

    //  Sets the SkeletonNode.
    public void setSkeletonNode(SkeletonNode pSkeletonNode)
    {
        m_pSkeletonNode = pSkeletonNode;
    }

    private void populateSkeletonJointHierarchy(SkeletonNode pSkeletonNode)
    {
        PNode pSkeletonHierarchy = buildJointHierarchy();

        pSkeletonNode.setSkeletonRoot(pSkeletonHierarchy);
    }

    //  Finds the ColladaNode with the specified jointName.
    public PColladaNode findJoint(String jointName)
    {
        int a;
        PColladaNode pColladaNode;
        PColladaNode pJointColladaNode;

        for (a=0; a<m_ColladaNodes.size(); a++)
        {
            pColladaNode = (PColladaNode)m_ColladaNodes.get(a);

            pJointColladaNode = pColladaNode.findJoint(jointName);
            if (pJointColladaNode != null)
                return(pJointColladaNode);
        }

        return(null);
    }

    private void postProcessInstances() 
    {
        if (getPolygonSkinnedMeshCount() > 0)
        {
            for (int i = 0; i < getPolygonSkinnedMeshCount(); i++)
            {
                PPolygonSkinnedMesh pSkinnedMesh = getPolygonSkinnedMesh(i);
                PMeshMaterial pMaterial = new PMeshMaterial("Collada Skinned Default");
                
                pMaterial.setShader(new VertexDeformer(m_pScene.getWorldManager()));
                
                pSkinnedMesh.setMaterial(pMaterial);
                //pSkinnedMesh.setNumberOfTextures(0);

                SharedAsset asset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pSkinnedMesh.getName()));
                asset.setAssetData(pSkinnedMesh);
                PPolygonModelInstance pModelInstance = m_pScene.addModelInstance(asset, new PMatrix());
            }
        }
        else if (getPolygonMeshCount() > 0)
        {
            if (m_ColladaNodes.size() > 0)
                populateSceneWithNodeHiearchy();
            else
                populateWithPolygonMeshes();
        }
        // Submit the geometry of every PPolygonMesh we encounter
        TreeTraverser.breadthFirst(m_pScene, new PPolygonMeshAssemblingProcessor());
    }

    
    private void populateWithPolygonMeshes()
    {
        PMatrix pIdentityMatrix = new PMatrix();
        PPolygonMesh pPolygonMesh;
        PPolygonMeshInstance pMeshInstance;
        PPolygonModelInstance pModelInstance;
        SharedAsset pSharedAsset;

        for (int a=0; a<getPolygonMeshCount(); a++)
        {
            pPolygonMesh = getPolygonMesh(a);

            //  Create a MeshInstance.
            pMeshInstance = createMeshInstance(pPolygonMesh, null);

            m_pScene.addInstanceNode(pMeshInstance);

/*
            //  Are there sub-meshes.
            if (pPolygonMesh.getChildrenCount() > 0)
            {
                PPolygonMesh pSubPolygonMesh;
                PPolygonMeshInstance pSubMeshInstance;

                for (int b=0; b<pPolygonMesh.getChildrenCount(); b++)
                {
                    pSubPolygonMesh = (PPolygonMesh)pPolygonMesh.getChild(b);
                    
                    pSubMeshInstance = createMeshInstance(pSubPolygonMesh, null);
                    
                    pMeshInstance.addChild(pSubMeshInstance);
                }
            }
*/

/*
            pSharedAsset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pPolygonMesh.getName()));
            pSharedAsset.setData(pPolygonMesh);
            pModelInstance = m_pScene.addModelInstance(pSharedAsset, new PMatrix());

            //  Are there sub-meshes.
            if (pPolygonMesh.getChildrenCount() > 0)
            {
                PPolygonMesh pSubPolygonMesh;
                PPolygonModelInstance pSubPolygonModelInstance;

                for (int b=0; b<pPolygonMesh.getChildrenCount(); b++)
                {
                    pSubPolygonMesh = (PPolygonMesh)pPolygonMesh.getChild(b);
                    
                    pSharedAsset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pSubPolygonMesh.getName()));
                    pSharedAsset.setData(pSubPolygonMesh);
                    pSubPolygonModelInstance = m_pScene.addModelInstance(pSharedAsset, new PMatrix());

                    m_pScene.removeModelInstance(pSubPolygonModelInstance);
                    
                    pModelInstance.addChild(pSubPolygonModelInstance);
                }
            }
*/
        }
    }

                
    //  Gets the max number of weights.
    public int getMaxNumberOfWeights()
    {
        return(m_MaxNumberOfWeights);
    }

    //  Sets the max number of weights.
    public void setMaxNumberOfWeights(int maxNumberOfWeights)
    {
        m_MaxNumberOfWeights = maxNumberOfWeights;
    }


//    public int populateSceneWithLoadedStuff()
//    {
//        //  Iterate through all the PolygonMeshes and add each to the Scene.
//        int a;
//        PPolygonMesh pPolygonMesh;
//        PPolygonModel pPolygonModel;
//        PPolygonModelInstance pPolygonModelInstance;
//        PPolygonMeshInstance pPolygonMeshInstance;
//        ArrayList<PPolygonMeshInstance> polygonMeshInstances = new ArrayList();
//        PMatrix identityMatrix = new PMatrix();
//
//        int objectCount = 0;
//
//
//        //  Constains the PolygonMeshes loaded.
//        for (a=0; a<getPolygonMeshCount(); a++)
//        {
//            pPolygonMesh = getPolygonMesh(a);
//
//            objectCount++;
//
//            m_pScene.addModelInstance(pPolygonMesh, identityMatrix);
//
///*
//            //  Add the PolygonMesh to the scene.
//            m_pScene.addMeshGeometry(pPolygonMesh);
//
//            pPolygonMeshInstance = new PPolygonMeshInstance(pPolygonMesh.getName(), pPolygonMesh, identityMatrix);
//
//            polygonMeshInstances.add(pPolygonMeshInstance);
//*/
//        }
//        
////        //  Create the PolygonModelInstance that will contain all the geometry.
////        pPolygonModelInstance = new PPolygonModelInstance("Untitled", identityMatrix, polygonMeshInstances);
//
//        return(objectCount);
//    }


/*
    public PPolygonModel createCurrentPolygonModel()
    {
        if (m_pCurrentPolygonModel != null)
        {
            ((PScene)m_pScene).addModelGeometry(m_pCurrentPolygonModel);
        }

        m_pCurrentPolygonModel = new PPolygonModel();
        ((PScene)m_pScene).addModelGeometry(m_pCurrentPolygonModel);

        return(m_pCurrentPolygonModel);
    }

    public PPolygonModel getCurrentPolygonModel()
    {
        return(m_pCurrentPolygonModel);
    }
*/







//  ******************************
//  ColladaCameraParams methods.
//  ******************************

    //  Adds a ColladaCameraParams.
    public void addColladaCameraParams(PColladaCameraParams pCameraParams)
    {
        m_ColladaCameraParams.add(pCameraParams);
    }

    //  Gets the number of ColladaCameraParams.
    public int getColladaCameraParamsCount()
    {
        return(m_ColladaCameraParams.size());
    }

    //  Gets the ColladaCameraParams at the specified index.
    public PColladaCameraParams getColladaCameraParams(int index)
    {
        return( (PColladaCameraParams)m_ColladaCameraParams.get(index));
    }

    //  Finds the ColladaCameraParams with the specified name.
    public PColladaCameraParams findColladaCameraParams(String name)
    {
        int a;
        PColladaCameraParams pCameraParams;

        for (a=0; a<m_ColladaCameraParams.size(); a++)
        {
            pCameraParams = (PColladaCameraParams)m_ColladaCameraParams.get(a);
            
            if (pCameraParams.getName().equals(name))
                return(pCameraParams);
        }

        return(null);
    }
    

    
    


//  ******************************
//  ColladaCamera methods.
//  ******************************

    //  Adds a ColladaCamera.
    public void addColladaCamera(PColladaCamera pCamera)
    {
        m_ColladaCameras.add(pCamera);
    }

    //  Gets the number of ColladaCameras.
    public int getColladaCameraCount()
    {
        return(m_ColladaCameras.size());
    }

    //  Gets the ColladaCamera at the specified index.
    public PColladaCamera getColladaCamera(int index)
    {
        return( (PColladaCamera)m_ColladaCameras.get(index));
    }

    //  Finds the ColladaCamera with the specified name.
    public PColladaCamera findColladaCamera(String name)
    {
        int a;
        PColladaCamera pCamera;
        
        for (a=0; a<m_ColladaCameras.size(); a++)
        {
            pCamera = (PColladaCamera)m_ColladaCameras.get(a);
            
            if (pCamera.getName().equals(name))
                return(pCamera);
        }
        
        return(null);
    }

    
    

//  ******************************
//  ColladaSkin methods.
//  ******************************

    //  Adds a ColladaSkin.
    public void addColladaSkin(PColladaSkin pColladaSkin)
    {
        m_ColladaSkins.add(pColladaSkin);
    }

    //  Gets the number of ColladaSkins.
    public int getColladaSkinCount()
    {
        return(m_ColladaSkins.size());
    }

    //  Gets the ColladaSkin at the specified index.
    public PColladaSkin getColladaSkin(int index)
    {
        return( (PColladaSkin)m_ColladaSkins.get(index));
    }

    //  Finds the ColladaSkin with the specified name.
    public PColladaSkin findColladaSkin(String name)
    {
        int a;
        PColladaSkin pColladaSkin;

        for (a=0; a<m_ColladaSkins.size(); a++)
        {
            pColladaSkin = (PColladaSkin)m_ColladaSkins.get(a);

            if (pColladaSkin.getName().equals(name))
                return(pColladaSkin);
        }

        return(null);
    }






//  ******************************
//  ColladaImage methods.
//  ******************************

    //  Adds a ColladaImage with minimal info.
    public void addColladaImage(String name, String originalFilename)
    {
        String shortFilename = FileUtils.getShortFilename(originalFilename);

        PColladaImage pColladaImage = new PColladaImage(name, shortFilename, this, null);

        //  Put the ColladaImage into the Images HashMap.
        m_Images.put(name, pColladaImage);

        m_ColladaImages.add(pColladaImage);
    }
    
    public void addColladaImage(PColladaImage colladaImage)
    {
        // add it to the two collections used to track this
        m_Images.put(colladaImage.getName(), colladaImage);
        m_ColladaImages.add(colladaImage);
    }

    //  Gets the number of ColladaImages.
    public int getColladaImageCount()
    {
        return(m_ColladaImages.size());
    }

    //  Gets the ColladaImage at the specified index.
    public PColladaImage getColladaImage(int index)
    {
        return( (PColladaImage)m_ColladaImages.get(index));
    }

    //  Finds the ColladaImage with the specified name.
    public PColladaImage findColladaImage(String name)
    {
        //  Get the ColladaImage from the HashMap.
        return(m_Images.get(name));
    }





//  ******************************
//  ColladaMaterial methods.
//  ******************************

    //  Adds a ColladaMaterial.
    public void addColladaMaterial(PColladaMaterial pColladaMaterial)
    {
        //  Put the ColladaMaterial into the Materials HashMap.
        m_Materials.put(pColladaMaterial.getName(), pColladaMaterial);

        m_ColladaMaterials.add(pColladaMaterial);
    }
    
    public PColladaMaterial getColladaMaterial(String materialName)
    {
        return m_Materials.get(materialName);
    }

    //  Gets the number of ColladaMaterials.
    public int getColladaMaterialCount()
    {
        return(m_ColladaMaterials.size());
    }

    //  Gets the ColladaMaterial at the specified index.
    public PColladaMaterial getColladaMaterial(int index)
    {
        return( (PColladaMaterial)m_ColladaMaterials.get(index));
    }

    //  Finds the ColladaMaterial with the specified name.
    public PColladaMaterial findColladaMaterial(String name)
    {
        //  First, attempt to find a ColladaMaterialInstance.
        PColladaMaterialInstance pColladaMaterialInstance = findColladaMaterialInstance(name);
        if (pColladaMaterialInstance != null)
            name = pColladaMaterialInstance.getMaterialName();// + "-fx";

        //  Get the ColladaMaterial from the HashMap.
        PColladaMaterial pColladaMaterial = m_Materials.get(name);

        return(pColladaMaterial);
    }

    
    
    
    

//  ******************************
//  ColladaMaterialInstance methods.
//  ******************************

    //  Adds a ColladaMaterialInstance.
    public void addColladaMaterialInstance(PColladaMaterialInstance pColladaMaterialInstance)
    {
        //  Put the ColladaMaterialInstance into the MaterialInstances HashMap.
        m_MaterialInstances.put(pColladaMaterialInstance.getInstanceName(), pColladaMaterialInstance);

        m_ColladaMaterialInstances.add(pColladaMaterialInstance);
    }

    //  Gets the number of ColladaMaterialInstances.
    public int getColladaMaterialInstanceCount()
    {
        return(m_ColladaMaterialInstances.size());
    }

    //  Gets the ColladaMaterialInstance at the specified index.
    public PColladaMaterialInstance getColladaMaterialInstance(int index)
    {
        return( (PColladaMaterialInstance)m_ColladaMaterialInstances.get(index));
    }

    //  Finds the ColladaMaterialInstance with the specified name.
    public PColladaMaterialInstance findColladaMaterialInstance(String name)
    {
        //  Get the ColladaMaterialInstance from the HashMap.
        return(m_MaterialInstances.get(name));
    }

    


//  ******************************
//  Factory ColladaNode management methods.
//  ******************************

    //  Adds a factory ColladaNode.
    public void addFactoryColladaNode(PColladaNode pColladaNode)
    {
        m_FactoryColladaNodes.add(pColladaNode);
    }

    //  Gets the number of factory ColladaNodes.
    public int getFactoryColladaNodeCount()
    {
        return(m_FactoryColladaNodes.size());
    }

    //  Gets the factory ColladaNode at the specified index.
    public PColladaNode getFactoryColladaNode(int index)
    {
        return( (PColladaNode)m_FactoryColladaNodes.get(index));
    }

    //  Gets the factory ColladaNode with the specified name.
    public PColladaNode findFactoryColladaNode(String name)
    {
        int a;
        PColladaNode pColladaNode;

        for (a=0; a<getFactoryColladaNodeCount(); a++)
        {
            pColladaNode = getFactoryColladaNode(a);

            if (pColladaNode.getName().equals(name))
                return(pColladaNode);
        }

        return(null);
    }




//  ******************************
//  ColladaNode management methods.
//  ******************************

    //  Adds a root ColladaNode.
    public void addColladaNode(PColladaNode pColladaNode)
    {
        m_ColladaNodes.add(pColladaNode);
    }

    //  Gets the number of root ColladaNodes.
    public int getColladaNodeCount()
    {
        return(m_ColladaNodes.size());
    }

    //  Gets the root ColladaNode at the specified index.
    public PColladaNode getColladaNode(int index)
    {
        return( (PColladaNode)m_ColladaNodes.get(index));
    }

    //  Gets the first ColladaNode with children.
    public PColladaNode getColladaNodeWithChildren()
    {
        int a;
        PColladaNode pColladaNode;

        for (a=0; a<getColladaNodeCount(); a++)
        {
            pColladaNode = getColladaNode(a);

            if (pColladaNode.getChildNodeCount() > 0)
                return(pColladaNode);
        }

        return(null);
    }

    //  Finds the ColladaNode with the specified name.
    public PColladaNode findColladaNode(String nodeName)
    {
        int a;
        PColladaNode pColladaNode;
        PColladaNode pColladaNodeFound;

        for (a=0; a<getColladaNodeCount(); a++)
        {
            pColladaNode = getColladaNode(a);

            pColladaNodeFound = pColladaNode.findNode(nodeName);
            if (pColladaNodeFound != null)
                return(pColladaNodeFound);
        }

        return(null);
    }

    //  Finds the ColladaNode with the specified joint name.
    public PColladaNode findJointColladaNode(String jointName)
    {
        int a;
        PColladaNode pColladaNode;
        PColladaNode pColladaNodeFound;

        for (a=0; a<getColladaNodeCount(); a++)
        {
            pColladaNode = getColladaNode(a);

            pColladaNodeFound = pColladaNode.findJoint(jointName);
            if (pColladaNodeFound != null)
                return(pColladaNodeFound);
        }

        return(null);
    }

    public void dumpColladaNodes()
    {
        int a;
        PColladaNode pColladaNode;

        //System.out.println("dumpColladaNodes()");

        for (a=0; a<getColladaNodeCount(); a++)
        {
            pColladaNode = getColladaNode(a);

            dumpColladaNode("", pColladaNode);
        }

        //System.out.println("dumpColladaNodes()");
    }

    public void dumpColladaNode(String spacing, PColladaNode pColladaNode)
    {
        float []pMatrixFloats = pColladaNode.getMatrixFloats();
        int a;

        System.out.println(spacing + "Node:  " + pColladaNode.getName());
        System.out.println(spacing + "   Mesh:    " + pColladaNode.getMeshName());
        if (pColladaNode.isJoint())
            System.out.println(spacing + "   Joint:   " + pColladaNode.getJointName());
        System.out.print(spacing + "   Matrix:  (" + pMatrixFloats[0] + ", " + pMatrixFloats[1] + ", " + pMatrixFloats[2] + ", " + pMatrixFloats[3] + ")");
        System.out.print(" (" + pMatrixFloats[4] + ", " + pMatrixFloats[5] + ", " + pMatrixFloats[6] + ", " + pMatrixFloats[7] + ")");
        System.out.print(" (" + pMatrixFloats[8] + ", " + pMatrixFloats[9] + ", " + pMatrixFloats[10] + ", " + pMatrixFloats[11] + ")");
        System.out.println(" (" + pMatrixFloats[12] + ", " + pMatrixFloats[13] + ", " + pMatrixFloats[14] + ", " + pMatrixFloats[15] + ")");


        PColladaMaterialInstance pMaterialInstance = pColladaNode.getMaterialInstance();
        if (pMaterialInstance != null)
        {
            System.out.println(spacing + "   MaterialInstance:");
            System.out.println(spacing + "      InstanceName:   " + pMaterialInstance.getInstanceName());
            System.out.println(spacing + "      MaterialName:   " + pMaterialInstance.getMaterialName());
            System.out.println(spacing + "      VertexInputs:");
            for (int b=0; b<pMaterialInstance.getVertexInputCount(); b++)
                System.out.println(spacing + "         " + pMaterialInstance.getVertexInput(b));
        }



        //  Now , dump all the child nodes.
        PColladaNode pChildColladaNode;

        for (a=0; a<pColladaNode.getChildNodeCount(); a++)
        {
            pChildColladaNode = pColladaNode.getChildNode(a);

            dumpColladaNode(spacing + "   ", pChildColladaNode);
        }
    }




    //  *******************************
    //  AnimatedItem management methods.
    //  *******************************

    //  Adds an AnimatedItem.
    public void addAnimatedItem(PColladaAnimatedItem pAnimatedItem)
    {
        m_ColladaAnimatedItems.add(pAnimatedItem);
    }

    //  Gets the number of AnimatedItems.
    public int getAnimatedItemCount()
    {
        return(m_ColladaAnimatedItems.size());
    }

    //  Gets the AnimatedItem at the specified index.
    public PColladaAnimatedItem getAnimatedItem(int index)
    {
        return( (PColladaAnimatedItem)m_ColladaAnimatedItems.get(index));
    }

    //  Finds the AnimatedItem that contains animation data for an object of the specified name.
    public PColladaAnimatedItem findAnimatedItem(String name)
    {
        int a;
        PColladaAnimatedItem pAnimatedItem;
        
        for (a=0; a<getAnimatedItemCount(); a++)
        {
            pAnimatedItem = getAnimatedItem(a);
            if (pAnimatedItem.getName().equals(name))
                return(pAnimatedItem);
        }
        
        return(null);
    }




//  ******************************
//  PolygonMesh management methods.
//  ******************************

    //  Creates a PolygonMesh.
    //  If we're supposed to load a SkinnedMesh, a PPolygonSkinnedMesh is created.
    //  Otherwise, a PPolygonMesh is created.
    public PPolygonMesh createPolygonMesh()
    {
        PPolygonMesh pPolygonMesh = new PPolygonMesh();

        return(pPolygonMesh);
    }

    //  Adds a PolygonMesh.
    public void addPolygonMesh(PPolygonMesh pPolygonMesh)
    {
        if (pPolygonMesh instanceof PPolygonSkinnedMesh)
            m_PolygonSkinnedMeshes.add(pPolygonMesh);
        else
            m_PolygonMeshes.add(pPolygonMesh);
    }

    //  Gets the number of PolygonMeshes.
    public int getPolygonMeshCount()
    {
        return(m_PolygonMeshes.size());
    }

    //  Gets the PolygonMesh at the specified index.
    public PPolygonMesh getPolygonMesh(int Index)
    {
        return( (PPolygonMesh)m_PolygonMeshes.get(Index));
    }

    //  Finds the PolygonMesh with the specified name.
    public PPolygonMesh findPolygonMesh(String meshName)
    {
        PPolygonMesh pPolygonMesh;
        for (int a=0; a<getPolygonMeshCount(); a++)
        {
            pPolygonMesh = getPolygonMesh(a);
            if (pPolygonMesh.getName().equals(meshName))
                return(pPolygonMesh);
        }

        return(null);
    }

    //  Removes a PolygonMesh.
    public void removePolygonMesh(PPolygonMesh pPolygonMesh)
    {
        m_PolygonMeshes.remove(pPolygonMesh);
    }




//  ******************************
//  PolygonSkinnedMesh management methods.
//  ******************************

/*
    //  Gets the PolygonSkinnedMesh that has been loaded.
    public PPolygonSkinnedMesh getPolygonSkinnedMesh()
    {
        return(m_pPolygonSkinnedMesh);
    }

    //  Sets the PolygonSkinnedMesh that is being loaded.
    public void setPolygonSkinnedMesh(PPolygonSkinnedMesh pPolygonSkinnedMesh)
    {
        m_pPolygonSkinnedMesh = pPolygonSkinnedMesh;
    }
*/

    //  Adds a PolygonSkinnedMesh.
    public void addPolygonSkinnedMesh(PPolygonSkinnedMesh pPolygonSkinnedMesh)
    {
        m_PolygonSkinnedMeshes.add(pPolygonSkinnedMesh);
    }

    //  Gets the number of PolygonSkinnedMesh.
    public int getPolygonSkinnedMeshCount()
    {
        return(m_PolygonSkinnedMeshes.size());
    }
    
    //  Gets the PolygonSkinnedMesh at the specified index.
    public PPolygonSkinnedMesh getPolygonSkinnedMesh(int Index)
    {
        return( (PPolygonSkinnedMesh)m_PolygonSkinnedMeshes.get(Index));
    }

    //  Finds the PolygonSkinnedMesh with the specified name.
    public PPolygonSkinnedMesh findPolygonSkinnedMesh(String meshName)
    {
        PPolygonSkinnedMesh pPolygonSkinnedMesh;
        for (int a=0; a<getPolygonSkinnedMeshCount(); a++)
        {
            pPolygonSkinnedMesh = getPolygonSkinnedMesh(a);
            if (pPolygonSkinnedMesh.getName().equals(meshName))
                return(pPolygonSkinnedMesh);
        }

        return(null);
    }



//    private int getRootJointCount()
//    {
//        int a;
//        PColladaNode pColladaNode;
//        int count = 0;
//
//        for (a=0; a<m_ColladaNodes.size(); a++)
//        {
//            pColladaNode = (PColladaNode)m_ColladaNodes.get(a);
//            if (pColladaNode.isJoint())
//                count++;
//        }
//
//        return(count);
//    }

    public PNode buildJointHierarchy()
    {
        PNode pRootNode = new PNode();
        int a;
        PColladaNode pColladaNode;        

        for (a=0; a<m_ColladaNodes.size(); a++)
        {
            pColladaNode = (PColladaNode)m_ColladaNodes.get(a);

            if (pColladaNode.isJoint())
                buildJointHierarchy(pRootNode, pColladaNode, false);
        }

        pRootNode.setTransform(new PTransform(new PMatrix()));
                
        return(pRootNode);
    }

    private PNode buildJointHierarchy(PNode pParentNode, PColladaNode pColladaNode, boolean bIgnoreMeshInstances)
    {
        String meshName = pColladaNode.getMeshName();
        String nodeInstanceName = pColladaNode.getInstanceNodeName();
//        PPolygonModelInstance pModelInstance;
        PPolygonMeshInstance pMeshInstance;
        PNode pThisNode = null;

        try
        {
            //  ****  MeshInstance Node.
            if (meshName.length() > 0)
            {
                //System.out.println("MeshName:  " + meshName);
                if (!bIgnoreMeshInstances)
                {
                    PPolygonMesh pPolygonMesh = findPolygonMesh(meshName);
                    if (pPolygonMesh != null)
                    {
                        //  Create a MeshInstance.
                        pMeshInstance = createMeshInstance(pPolygonMesh, pColladaNode);

                        pThisNode = pMeshInstance;
                    }
                }
            }

            //  ****  NodeInstance Node.
            else if (nodeInstanceName.length() > 0)
            {
                PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);
                if (pInstancedColladaNode != null)
                {
                    //pThisNode = m_pScene.processJoint(null, pColladaNode.getMatrix());
                    pThisNode = new PNode(pColladaNode.getName());
                    pThisNode.setTransform(new PTransform());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pColladaNode = pInstancedColladaNode;
                }
            }

            //  If there is no Mesh, then this is a joint.
            else
            {
                if (pColladaNode.isJoint())
                {
                    //  Create a SkinnedMeshJoint.
                    SkinnedMeshJoint pSkinnedMeshJoint = new SkinnedMeshJoint();

                    pSkinnedMeshJoint.setTransform(new PTransform());
                    pSkinnedMeshJoint.setName(pColladaNode.getName());
                    pSkinnedMeshJoint.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pThisNode = pSkinnedMeshJoint;
                }
                else
                {
                    //  Just create a Node.
                    pThisNode = new PJoint(new PTransform(pColladaNode.getMatrix()));
                    pThisNode.setName(pColladaNode.getName());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());
                }
            }

            if (pThisNode != null)
            {
                if (pParentNode != null)
                    pParentNode.addChild(pThisNode);

                //  Iterate through all the child Nodes.
                int a;
                PColladaNode pChildColladaNode;

                for (a=0; a<pColladaNode.getChildNodeCount(); a++)
                {
                    pChildColladaNode = pColladaNode.getChildNode(a);

                    buildJointHierarchy(pThisNode, pChildColladaNode, bIgnoreMeshInstances);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(pThisNode);
    }

//    public void postProcess()
//    {
//        if (m_pSkeletonNode != null)
//            processSkeleton();
//        else
//            processColladaNodes();
//    }

//    public void processSkeleton()
//    {
//        int a;
//        PColladaNode pColladaNode;
//
//        for (a=0; a<m_ColladaNodes.size(); a++)
//        {
//            pColladaNode = (PColladaNode)m_ColladaNodes.get(a);
//
//            processColladaNodeForSkeleton(pColladaNode, m_pSkeletonNode);
//        }
//
////        m_pSkeletonNode.dump();
//
//        m_pScene.addInstanceNode(m_pSkeletonNode);
//    }

    public void processColladaNodeForSkeleton(PColladaNode pColladaNode, SkeletonNode pSkeleton)
    {
        //  Ignore Joints.
        if (pColladaNode.isJoint())
            return;

        //System.out.println("ColladaNode::: '" + pColladaNode.getName() + "'");

        //  If the ColladaNode contains skeletons, we need to build a
        //  PolygonSkinnedMeshInstance tree.
        if (pColladaNode.getSkeletonCount() > 0)
        {
            String skinnedControllerName = pColladaNode.getControllerName();
            if (skinnedControllerName.length() > 0)
            {
                PColladaSkin pColladaSkin = findColladaSkin(skinnedControllerName);
                if (pColladaSkin != null)
                {
                    int b;
                    String skeletonName;
                    String firstJointName;
                    String skinnedMeshName = pColladaSkin.getMeshName();

                    for (b=0; b<pColladaNode.getSkeletonCount(); b++)
                    {
                        skeletonName = pColladaNode.getSkeleton(b);

                        //  Get the JointName of the first Bone in the SkinnedMesh.
                        firstJointName = pColladaSkin.getJointName(0);
                        if (firstJointName.equals(skeletonName))
                        {
                            //  Create a PolygonSkinnedMeshInstance.
                            PPolygonSkinnedMesh pSkinnedMesh = findPolygonSkinnedMesh(skinnedMeshName);

                            m_pSkeletonNode.addChild(pSkinnedMesh);
/*
                            PPolygonSkinnedMeshInstance pSkinnedMeshInstance = null;
                            if (pSkinnedMesh != null)
                            {
                                pSkinnedMeshInstance = createPolygonSkinnedMeshInstance(pSkinnedMesh, skeletonName);

                                pSkinnedMeshInstance.setTransform(new PTransform(new PMatrix()));

                                pSkeleton.addSkinnedMeshInstance(pSkinnedMeshInstance);

                                pSkinnedMeshInstance.buildAnimationJointMapping(pSkeleton);
                            }
*/
                        }
                    }
                }
            }
        }


        //  Process all child nodes.
        if (pColladaNode.getChildNodeCount() > 0)
        {
            int a;
            PColladaNode pChildColladaNode;

            for (a=0; a<pColladaNode.getChildNodeCount(); a++)
            {
                pChildColladaNode = pColladaNode.getChildNode(a);

                processColladaNodeForSkeleton(pChildColladaNode, pSkeleton);
            }
        }
    }



    public void processColladaNodes()
    {
        int a;
        PColladaNode pColladaNode;
        
        for (a=0; a<m_ColladaNodes.size(); a++)
        {
            pColladaNode = (PColladaNode)m_ColladaNodes.get(a);

            System.out.println("Root ColladaNode '" + pColladaNode.getName() + "', isJoint " + ((pColladaNode.isJoint()) ? "yes" : "no"));

            processColladaNode(pColladaNode, m_pScene.getInstances());
        }
    }
    
    public void processColladaNode(PColladaNode pColladaNode, PNode pParentNode)
    {
        //  Ignore Joints.
        if (pColladaNode.isJoint())
            return;

        PNode pThisNode = null;
        boolean bSkeletonsCreated = true;


        //System.out.println("ColladaNode::: '" + pColladaNode.getName() + "'");


        //  If the ColladaNode contains skeletons, we need to build a
        //  PolygonSkinnedMeshInstance tree.
        if (pColladaNode.getSkeletonCount() > 0)
        {
            int b;
            String skeletonName;
            PColladaNode pSkeletonColladaNode;
            PPolygonSkinnedMeshInstance pPolygonSkinnedMeshInstance;
            
            for (b=0; b<pColladaNode.getSkeletonCount(); b++)
            {
                skeletonName = pColladaNode.getSkeleton(b);
                
                pSkeletonColladaNode = findColladaNode(skeletonName);
                if (pSkeletonColladaNode == null)
                {
                    System.out.println("   Unable to find skeleton node '" + skeletonName + "'!");
                }
                
                //System.out.println("   Creating Skeleton '" + skeletonName + "'.");

                pThisNode = buildPolygonSkinnedMeshInstance(pColladaNode, skeletonName, pParentNode);
                if (pThisNode != null)
                    pParentNode.addChild(pThisNode);
            }
            
            bSkeletonsCreated = false;
        }

        //  Otherwise, if the ColladaNode was assigned a MeshName, we need
        //  to create a PolygonMeshInstance.
        else if (pColladaNode.getMeshName().length() > 0)
        {
            String meshName = pColladaNode.getMeshName();
            PPolygonMesh pPolygonMesh;

            pPolygonMesh = findPolygonMesh(meshName);

            if (pPolygonMesh != null)
            {
                //System.out.println("   Creating Mesh '" + meshName + "'.");

                PPolygonMeshInstance pPolygonMeshInstance;

                pPolygonMeshInstance = createMeshInstance(pPolygonMesh, pColladaNode);

                pThisNode = pPolygonMeshInstance;
                bSkeletonsCreated = false;
            }
            else
                System.out.println("   Unable to find Mesh '" + pColladaNode.getMeshName() + "'!");
        }

        //  Otherwise, if the ColladaNode was assigned a InstanceNodeName, we need
        //  to create an instance of a PolygonMesh.
        else if (pColladaNode.getInstanceNodeName().length() > 0)
        {
            String nodeInstanceName = pColladaNode.getInstanceNodeName();
                    
            PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);
            if (pInstancedColladaNode != null)
            {
                //System.out.println("   Creating node instance '" + nodeInstanceName + "'.");

                //pThisNode = m_pScene.processJoint(null, pColladaNode.getMatrix());
                pThisNode = new PNode(pColladaNode.getName());
                pThisNode.setTransform(new PTransform());
                pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                pColladaNode = pInstancedColladaNode;

                bSkeletonsCreated = false;
            }
        }
        //  Otherwise, we need to create a Node.
        else
        {
            pThisNode = new PNode(pColladaNode.getName());
            pThisNode.setTransform(new PTransform());
            pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());
            bSkeletonsCreated = false;
        }


        if (bSkeletonsCreated)
            return;


        if (pThisNode != null)
            pParentNode.addChild(pThisNode);


        //  Process all child nodes.
        if (pColladaNode.getChildNodeCount() > 0)
        {
            int a;
            PColladaNode pChildColladaNode;
            
            for (a=0; a<pColladaNode.getChildNodeCount(); a++)
            {
                pChildColladaNode = pColladaNode.getChildNode(a);

                processColladaNode(pChildColladaNode, pThisNode);
            }
        }
    }


    
    private PPolygonSkinnedMeshInstance createPolygonSkinnedMeshInstance(PPolygonSkinnedMesh pSkinnedMesh, String skeletonName)
    {
        SharedAsset asset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pSkinnedMesh.getName()));
        asset.setAssetData(pSkinnedMesh);

//        PPolygonModelInstance pModelInstance = m_pScene.addModelInstance(asset, pColladaNode.getMatrix());
//        pParentNode.addChild(pModelInstance);

        PNode pSkinnedMeshInstanceNode = m_pScene.processSkinnedMesh(pSkinnedMesh);
//        pSkinnedMeshInstanceNode.setTransform(new PTransform(pColladaNode.getMatrix()));

        return( (PPolygonSkinnedMeshInstance)pSkinnedMeshInstanceNode);
    }

    private PNode buildPolygonSkinnedMeshInstance(PColladaNode pColladaNode, String skeletonName, PNode pParentNode)
    {
        String skinnedMeshName = pColladaNode.getControllerName();
        PPolygonSkinnedMesh pSkinnedMesh = findPolygonSkinnedMesh(skinnedMeshName);

        SharedAsset asset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pSkinnedMesh.getName()));
        asset.setAssetData(pSkinnedMesh);
                
//        PPolygonModelInstance pModelInstance = m_pScene.addModelInstance(asset, pColladaNode.getMatrix());
//        pParentNode.addChild(pModelInstance);

        PNode pSkinnedMeshInstanceNode = m_pScene.processSkinnedMesh(pSkinnedMesh);
        pSkinnedMeshInstanceNode.setTransform(new PTransform(pColladaNode.getMatrix()));

        pParentNode.addChild(pSkinnedMeshInstanceNode);

        return(pSkinnedMeshInstanceNode);
    }

    public void postProcessPolygonSkinnedMeshes()
    {
        if (getPolygonSkinnedMeshCount() == 0)
            return;

        for (int a=0; a<getPolygonSkinnedMeshCount(); a++)
            postProcessPolygonSkinnedMesh(getPolygonSkinnedMesh(a));
    }

    public void postProcessPolygonSkinnedMesh(PPolygonSkinnedMesh pSkinnedMesh)
    {
        int a;
        
        PNode pChildNode = null;

        createColladaNode(m_pScene.getInstances(), getColladaNodeWithChildren(), false);
            
        //  Must process the Node hiearchy loaded.
        PNode pJointHierarchy = null;//pSkinnedMesh.getBindPoseTransformHierarchy();
        
        for (a=0; a<m_pScene.getInstances().getChildrenCount(); a++)
        {
            pChildNode = m_pScene.getInstances().getChild(a);

            //  Only add PJoints to the PolygonSkinnedMesh joint hiearchy node.
            if (pChildNode instanceof PJoint)
            {
                m_pScene.getInstances().removeChild(pChildNode);
                a--;

                pJointHierarchy.addChild(pChildNode);
            }
        }

        AnimationGroup pAnimationLoop = new AnimationGroup();
    
        //  Create all the JointAnimations.
        processNodeInJointHierarchy(pJointHierarchy, false, pAnimationLoop);
// BROKEN --- The animation states are now kept at the skeleton node!
//        if (pAnimationLoop.getChannels().size() == 0)
//            pAnimationLoop = null;
//        else
//            pSkinnedMesh.getAnimationComponent().getGroups().add(pAnimationLoop);
    }

    private void processNodeInJointHierarchy(PNode pNode, boolean bProcessThisNode, AnimationGroup pAnimationLoop)
    {
        if (bProcessThisNode)
        {
            if (pNode instanceof PJoint)
            {
                PJoint pJoint = (PJoint)pNode;
                PColladaAnimatedItem pAnimatedItem = findAnimatedItem(pJoint.getName());
                if (pAnimatedItem != null)
                {
                    int a;
                    float fKeyframeTime;
                    PMatrix pKeyframeMatrix = new PMatrix();

                    //  Create a JointChannel.
                    COLLADA_JointChannel pAnimationChannel = new COLLADA_JointChannel(pJoint.getName());


                    //  Now, populate the newly created JointAnimation.
                    for (a=0; a<pAnimatedItem.getKeyframeCount(); a++)
                    {
                        fKeyframeTime = pAnimatedItem.getKeyframeTime(a);
                        pAnimatedItem.getKeyframeMatrix(a, pKeyframeMatrix);

                        pAnimationChannel.addKeyframe(fKeyframeTime, pKeyframeMatrix);
                    }

           //         System.out.print("Adding joint animation for joint '" + pAnimationChannel.getTargetJointName() + "' to AnimationLoop.");
//                    System.out.println("  " + pAnimationChannel.getKeyframeCount() + " keyframes.");

                    //  Add the JointAnimation to the AnimationLoop.
                    pAnimationLoop.getChannels().add(pAnimationChannel);
                }
            }
        }

        if (pNode.getChildrenCount() > 0)
        {
            int a;
            PNode pChildNode;

            for (a=0; a<pNode.getChildrenCount(); a++)
            {
                pChildNode = pNode.getChild(a);
                processNodeInJointHierarchy(pChildNode, true, pAnimationLoop);
            }
        }
    }

    public void populateSceneWithNodeHiearchy()
    {
/*
        int b;
        PPolygonMesh pPolygonMesh;
        PMatrix identityMatrix = new PMatrix();

        //  Populate the scene with all the loaded PolygonMeshes.
        for (b=0; b<getPolygonMeshCount(); b++)
        {
            pPolygonMesh = getPolygonMesh(b);

            m_pScene.addModelInstance(pPolygonMesh, identityMatrix);
        }
*/
        
        //  If a PolygonSkinnedMesh was loaded, then create a ModelInstance
        //  containing a PolygonSkinnedMeshInstance.
        if (getPolygonSkinnedMeshCount() > 0)
        {
            PPolygonSkinnedMesh pSkinnedMesh;
            PMatrix pSkinnedMeshMatrix = new PMatrix();

            for (int a=0; a<getPolygonSkinnedMeshCount(); a++)
            {
                pSkinnedMesh = getPolygonSkinnedMesh(a);
                
                SharedAsset asset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pSkinnedMesh.getName()));
                asset.setAssetData(pSkinnedMesh);
                
                PPolygonModelInstance pModelInstance = m_pScene.addModelInstance(asset, pSkinnedMeshMatrix);
            }
        }
        else
        {
            int a;

//            System.out.println("populateSceneWithNodeHiearchy()");
//            System.out.println("   Materials:  " + getColladaMaterialCount());
//            for (a=0; a<getColladaMaterialCount(); a++)
//                System.out.println("      " + getColladaMaterial(a).getName());
//            System.out.println("   PolygonMeshes:  " + getPolygonMeshCount());
//            for (a=0; a<getPolygonMeshCount(); a++)
//                System.out.println("      " + getPolygonMesh(a).getName());


            //PMatrix rootMatrix = new PMatrix();
            //PNode pRootNode = new PNode("Root");
            
            //  Iterate through all the root Nodes.
            PColladaNode pColladaNode;
            int colladaNodeCount = getColladaNodeCount();

            for (a=0; a<colladaNodeCount; a++)
            {
                pColladaNode = getColladaNode(a);

                createColladaNode(m_pScene.getInstances(), pColladaNode, false);
            }

            //  Add the root node to the Scene.
            //m_pScene.addInstanceNode(pRootNode);
            
            //m_pScene.setDirty(true, true);
        }
    }

    //  Creates a MeshInstance.
    public PPolygonMeshInstance createMeshInstance(PPolygonMesh pPolygonMesh, PColladaNode pColladaNode)
    {
        PPolygonMeshInstance pMeshInstance = null;

        //  Create the MeshInstance.
        SharedAsset asset = new SharedAsset(m_pScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pPolygonMesh.getName()));
        asset.setAssetData(pPolygonMesh);

        if (pColladaNode != null)
        {
            pMeshInstance = (PPolygonMeshInstance)m_pScene.addMeshInstance(pColladaNode.getName(), asset);
            pMeshInstance.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());
        }
        else
        {
            pMeshInstance = (PPolygonMeshInstance)m_pScene.addMeshInstance(pPolygonMesh.getName(), asset);
            pMeshInstance.getTransform().getLocalMatrix(true).set(new PMatrix());
        }

        
        if (pColladaNode != null)
        {
            PColladaMaterialInstance pMaterialInstance = null;
            PColladaMaterial pColladaMaterial = null;
            PMeshMaterial pMeshMaterial = null;
        
            pMaterialInstance = pColladaNode.getMaterialInstance();
            if (pMaterialInstance != null)
            {
                pColladaMaterial = findColladaMaterial(pMaterialInstance.getMaterialName());
                if (pColladaMaterial != null)
                {
                    try
                    {
                        pMeshMaterial = pColladaMaterial.createMeshMaterial();
                        // System.out.println("Assigning Material " + pMeshMaterial.getName() + " to MeshInstance.");
                        pMeshInstance.setMaterial(pMeshMaterial);
                        pMeshInstance.setUseGeometryMaterial(false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }


        //  Are there sub-meshes?
        if (pPolygonMesh.getChildrenCount() > 0)
        {
            int b;
            PPolygonMesh pSubPolygonMesh;
            PPolygonMeshInstance pSubMeshInstance;
            PMeshMaterial pMeshMaterial = null;

            for (b=0; b<pPolygonMesh.getChildrenCount(); b++)
            {
                pSubPolygonMesh = (PPolygonMesh)pPolygonMesh.getChild(b);
                                
                //  Create a sub MeshInstance.
                pSubMeshInstance = createMeshInstance(pSubPolygonMesh, null);

                pMeshMaterial = pSubPolygonMesh.getMaterialCopy();
                pSubMeshInstance.setMaterial(pMeshMaterial);
                pSubMeshInstance.setUseGeometryMaterial(false);                        

                pMeshInstance.addChild(pSubMeshInstance);
            }
        }


        return(pMeshInstance);
    }

    void createColladaNode(PNode pParentNode, PColladaNode pColladaNode, boolean bIgnoreMeshInstances)
    {
        String meshName = pColladaNode.getMeshName();
        String nodeInstanceName = pColladaNode.getInstanceNodeName();
//        PPolygonModelInstance pModelInstance;
        PPolygonMeshInstance pMeshInstance;
        PNode pThisNode = null;
        
        try
        {
            //  This node will be a MeshInstance if there is a Mesh assigned.
            if (meshName.length() > 0)
            {
                //System.out.println("MeshName:  " + meshName);
                if (!bIgnoreMeshInstances)
                {
                    PPolygonMesh pPolygonMesh = findPolygonMesh(meshName);
                    if (pPolygonMesh != null)
                    {
                        //  Create a MeshInstance.
                        pMeshInstance = createMeshInstance(pPolygonMesh, pColladaNode);
                        
                        pThisNode = pMeshInstance;
/*
                        //  Are there sub-meshes?
                        if (pPolygonMesh.getChildrenCount() > 0)
                        {
                            int b;
                            PPolygonMesh pSubPolygonMesh;
                            PPolygonMeshInstance pSubMeshInstance;
                            
                            for (b=0; b<pPolygonMesh.getChildrenCount(); b++)
                            {
                                pSubPolygonMesh = (PPolygonMesh)pPolygonMesh.getChild(b);
                                
                                //  Create a sub MeshInstance.
                                pSubMeshInstance = createMeshInstance(pSubPolygonMesh, null);
                                
                                pMeshInstance.addChild(pSubMeshInstance);
                            }
                        }
*/
                    }
                }
            }

            else if (nodeInstanceName.length() > 0)
            {
                PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);
                if (pInstancedColladaNode != null)
                {
                    //pThisNode = m_pScene.processJoint(null, pColladaNode.getMatrix());
                    pThisNode = new PNode(pColladaNode.getName());
                    pThisNode.setTransform(new PTransform());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pColladaNode = pInstancedColladaNode;
                }
            }

            //  If there is no Mesh, then this is a joint.
            else
            {
                if (pColladaNode.isJoint())
                {
                    SkinnedMeshJoint pSkinnedMeshJoint = new SkinnedMeshJoint();

                    pSkinnedMeshJoint.setTransform(new PTransform());
                    pSkinnedMeshJoint.setName(pColladaNode.getName());
                    pSkinnedMeshJoint.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pThisNode = pSkinnedMeshJoint;
                }
                else
                {
                    //pThisNode = m_pScene.processJoint(null, pColladaNode.getMatrix());
                    pThisNode = new PJoint(new PTransform(pColladaNode.getMatrix()));
                    pThisNode.setName(pColladaNode.getName());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());
                }
            }

            if (pThisNode != null)
            {
                pParentNode.addChild(pThisNode);

                //  Iterate through all the child Nodes.
                int a;
                PColladaNode pChildColladaNode;

                for (a=0; a<pColladaNode.getChildNodeCount(); a++)
                {
                    pChildColladaNode = pColladaNode.getChildNode(a);

                    createColladaNode(pThisNode, pChildColladaNode, bIgnoreMeshInstances);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }





    //  *****************************************
    //  Private methods.
    //  *****************************************

    //  Gets the instance of LibraryCameras in the list of Libraries.
    private LibraryCameras getInstanceOfLibraryCameras()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryCameras)
                return( (LibraryCameras)obj);
        }

        return(null);
    }

    //  Gets the instance of LibraryImages in the list of Libraries.
    private LibraryImages getInstanceOfLibraryImages()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryImages)
                return( (LibraryImages)obj);
        }

        return(null);
    }

    //  Gets the instance of LibraryEffects in the list of Libraries.
    private LibraryEffects getInstanceOfLibraryEffects()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryEffects)
                return( (LibraryEffects)obj);
        }

        return(null);
    }

    //  Gets the instance of LibraryMaterials in the list of Libraries.
    private LibraryMaterials getInstanceOfLibraryMaterials()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryMaterials)
                return( (LibraryMaterials)obj);
        }

        return(null);
    }
    
    //  Gets the instance of LibraryAnimations in the list of Libraries.
    private LibraryAnimations getInstanceOfLibraryAnimations()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryAnimations)
                return( (LibraryAnimations)obj);
        }

        return(null);
    }

    //  Gets the instance of LibraryVisualScenes in the list of Libraries.
    private LibraryVisualScenes getInstanceOfLibraryVisualScenes()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryVisualScenes)
                return( (LibraryVisualScenes)obj);
        }

        return(null);
    }

    //  Gets the instance of LibraryGeometries in the list of Libraries.
    private LibraryGeometries getInstanceOfLibraryGeometries()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryGeometries)
                return( (LibraryGeometries)obj);
        }

        return(null);
    }

    //  Gets the instance of LibraryControllers in the list of Libraries.
    private LibraryControllers getInstanceOfLibraryControllers()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryControllers)
                return( (LibraryControllers)obj);
        }

        return(null);
    }
    
    //  Gets the instance of LibraryNodes in the list of Libraries.
    private LibraryNodes getInstanceOfLibraryNodes()
    {
        for(Object obj : m_Libraries)
        {
            if (obj instanceof LibraryNodes)
                return( (LibraryNodes)obj);
        }

        return(null);
    }

    /**
     * Applies the following configuration
     * @param params
     */
    public void applyConfiguration(ColladaLoaderParams params)
    {
        if (params == null) // use defaults
            return;
        setLoadRig(params.isLoadingSkeleton());
        setLoadGeometry(params.isLoadingGeometry());
        setLoadAnimations(params.isLoadingAnimations());
        
        setPrintStats(params.isShowingDebugInfo());
        
        m_MaxNumberOfWeights = params.getMaxInfluences();
        
        m_Name = params.getName();
    }
    
    public URL getFileLocation()
    {
        return m_fileLocation;
    }
}





