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
import org.collada.colladaschema.COLLADA;
import org.collada.xml_walker.ProcessorFactory;
import org.collada.xml_walker.PColladaNode;
import org.collada.xml_walker.PColladaMaterialInstance;
import org.collada.xml_walker.PColladaImage;
import org.collada.xml_walker.PColladaEffect;
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


import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PPolygonMeshInstance;

import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;

import imi.scene.animation.AnimationGroup;
import imi.scene.animation.COLLADA_JointChannel;
        
import imi.scene.PJoint;


import imi.scene.utils.tree.PPolygonMeshAssemblingProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.net.URL;
import javolution.util.FastMap;
import org.collada.xml_walker.ColladaMaterial;



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

    FastMap<String, PColladaEffect>             m_EffectMap = new FastMap<String, PColladaEffect>();
    private ArrayList<PColladaEffect>           m_EffectList = new ArrayList<PColladaEffect>();

    FastMap<String, ColladaMaterial>             m_MaterialMap = new FastMap<String, ColladaMaterial>();
    private ArrayList<ColladaMaterial>           m_MaterialList = new ArrayList<ColladaMaterial>();

    FastMap<String, PColladaMaterialInstance>   m_MaterialInstances = new FastMap<String, PColladaMaterialInstance>();
    private ArrayList                           m_ColladaMaterialInstances = new ArrayList();

    SkeletonNode                                m_pSkeletonNode = null;
            

    
    
    private PScene                              m_pLoadingPScene = null;

    public PPolygonModel                        m_pCurrentPolygonModel = null;


    //  Constains the PolygonMeshes loaded.
    public ArrayList<PPolygonMesh>              m_PolygonMeshes = new ArrayList<PPolygonMesh>();

    //  Contains the PolygonSkinnedMeshes loaded.
    public ArrayList<PPolygonSkinnedMesh>      m_PolygonSkinnedMeshes = new ArrayList<PPolygonSkinnedMesh>();

    public ArrayList<PColladaSkin>              m_ColladaSkins = new ArrayList<PColladaSkin>();

    private ArrayList<PColladaNode>             m_ColladaNodes = new ArrayList<PColladaNode>();

    private ArrayList                           m_FactoryColladaNodes = new ArrayList();



    private int                                 m_MaxNumberOfWeights = 4;
    private ArrayList<PColladaAnimatedItem>     m_ColladaAnimatedItems = new ArrayList<PColladaAnimatedItem>();
    private ArrayList<PColladaCameraParams>     m_ColladaCameraParams = new ArrayList<PColladaCameraParams>();
    private ArrayList<PColladaCamera>           m_ColladaCameras = new ArrayList<PColladaCamera>();

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
        return m_pLoadingPScene;
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
            doLoad(collada);
            return true;
        }
        catch (JAXBException ex)
        {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            return false;
        }
    }

    
    public boolean load(PScene loadingPScene, URL colladaFile)
    {
        boolean result = false;
        m_fileLocation = colladaFile;
        try
        {
            m_pLoadingPScene = loadingPScene;
            m_pLoadingPScene.setUseRepository(false); // the repository will extract the data later

            javax.xml.bind.JAXBContext jc = javax.xml.bind.JAXBContext.newInstance("org.collada.colladaschema");
            javax.xml.bind.Unmarshaller unmarshaller = jc.createUnmarshaller();
            org.collada.colladaschema.COLLADA collada =
                    (org.collada.colladaschema.COLLADA) unmarshaller.unmarshal(colladaFile);

            doLoad(collada);
            result = true;
        }
        catch (JAXBException ex)
        {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public void loadSkinnedMesh(PScene pScene, URL location)
    {
        load(pScene, location);
    }

    private void doLoad(COLLADA collada)
    {
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
        TreeTraverser.breadthFirst(m_pLoadingPScene, new PPolygonMeshAssemblingProcessor());
    }



    //  Processes the Rig.
    private PNode processRig()
    {
        PColladaNode pColladaNode;
        PNode pRootNode = new PNode("RootNode created in Collada.java : 374", new PTransform(new PMatrix()));

        for (int i = 0; i < m_ColladaNodes.size(); i++)
        {
            pColladaNode = (PColladaNode)m_ColladaNodes.get(i);
            if (pColladaNode.isJoint())
                processRig(pRootNode, pColladaNode, false);
        }

        m_pSkeletonNode.setSkeletonRoot(pRootNode);
        m_pLoadingPScene.addInstanceNode(m_pSkeletonNode);

        return(pRootNode);
    }

    private PNode processRig(PNode pParentNode, PColladaNode colladaNode, boolean bIgnoreMeshInstances)
    {
        String meshURL = colladaNode.getMeshURL();
        String meshName = colladaNode.getMeshName();
        String nodeInstanceName = colladaNode.getInstanceNodeName();

        PPolygonMeshInstance pMeshInstance;
        PNode pThisNode = null;

        try
        {
            //  ****  MeshInstance Node.
            if (meshURL != null)
            {
                //System.out.println("MeshName:  " + meshName);
                if (!bIgnoreMeshInstances)
                {
                    PPolygonMesh pPolygonMesh = findPolygonMesh(meshURL);
                    if (pPolygonMesh != null)
                    {
                        //  Create a MeshInstance.
                        pMeshInstance = createMeshInstance(pPolygonMesh, colladaNode, meshName);

                        pThisNode = pMeshInstance;
                    }
                }
            }

            //  ****  NodeInstance Node.
            else if (nodeInstanceName != null)
            {
                PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);
                if (pInstancedColladaNode != null)
                {
                    //pThisNode = m_pLoadingPScene.processJoint(null, pColladaNode.getMatrix());
                    pThisNode = new PNode(colladaNode.getName());
                    pThisNode.setTransform(new PTransform());
                    pThisNode.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());

                    colladaNode = pInstancedColladaNode;
                }
            }

            //  If there is no MS3D_Mesh, then this is a joint.
            else
            {
                if (colladaNode.isJoint())
                {
                    //  Create a SkinnedMeshJoint.
                    SkinnedMeshJoint pSkinnedMeshJoint = new SkinnedMeshJoint(colladaNode.getName(),
                            new PTransform(colladaNode.getMatrix()));

                    pThisNode = pSkinnedMeshJoint;
                }
                else
                {
                    //  Just create a Node.
                    pThisNode = new PJoint(new PTransform(colladaNode.getMatrix()));
                    pThisNode.setName(colladaNode.getName());
                    pThisNode.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());
                }
            }

            if (pThisNode != null)
            {
                if (pParentNode != null)
                    pParentNode.addChild(pThisNode);

                //  Iterate through all the child Nodes.
                int a;
                PColladaNode pChildColladaNode;

                for (a=0; a<colladaNode.getChildNodeCount(); a++)
                {
                    pChildColladaNode = colladaNode.getChildNode(a);

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
            {
                m_pSkeletonNode.addChild(pPolygonSkinnedMesh);

                pPolygonSkinnedMesh.linkJointsToSkeletonNode(m_pSkeletonNode);
            }
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
        PColladaNode jointNode = null;
        for (PColladaNode node : m_ColladaNodes)
        {
            jointNode = node.findJoint(jointName);
            if (jointNode != null)
                return jointNode;
        }

        return null;
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
            
            if (name.equals(pCameraParams.getName()))
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
        PColladaCamera result = null;
        for (PColladaCamera cam : m_ColladaCameras)
        {
            if (name.equals(cam.getName()))
            {
                result = cam;
                break;
            }
        }
        return result;
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
        for (PColladaSkin skin : m_ColladaSkins)
        {
            if (name.equals(skin.getName()))
                return skin;
        }
        return null;
    }


//  ******************************
//  Collada Effect methods.
//  ******************************

    //  Adds a Collada Effect
    public void addColladaEffect(PColladaEffect colladaEffect)
    {
        //  Put the ColladaMaterial into the Materials HashMap.
        m_EffectMap.put(colladaEffect.getEffectIdentifier(), colladaEffect);

        m_EffectList.add(colladaEffect);
    }

    public PColladaEffect getColladaEffect(String effectName)
    {
        return m_EffectMap.get(effectName);
    }

    //  Gets the number of ColladaMaterials.
    public int getColladaEffectCount()
    {
        return(m_EffectList.size());
    }

    //  Gets the ColladaMaterial at the specified index.
    public PColladaEffect getColladaEffect(int index)
    {
        return( (PColladaEffect)m_EffectList.get(index));
    }

    //  Finds the ColladaMaterial with the specified name.
    public PColladaEffect findColladaEffectByIdentifier(String identifier)
    {
        //  First, attempt to find a ColladaMaterialInstance.
        PColladaEffect result = m_EffectMap.get(identifier);
        return result;
    }

    // Material management

    //  Adds a ColladaMaterial.
    public void addColladaMaterial(ColladaMaterial colladaMaterial)
    {
        //  Put the ColladaMaterial into the Materials HashMap.
        m_MaterialMap.put(colladaMaterial.getID(), colladaMaterial);
        m_MaterialList.add(colladaMaterial);
    }

    public ColladaMaterial getColladaMaterial(String materialName)
    {
        return m_MaterialMap.get(materialName);
    }

    //  Gets the number of ColladaMaterials.
    public int getColladaMaterialCount()
    {
        return m_MaterialList.size();
    }

    //  Gets the ColladaMaterial at the specified index.
    public ColladaMaterial getColladaMaterial(int index)
    {
        return m_MaterialList.get(index);
    }

    //  Finds the ColladaMaterial with the specified name.
    public ColladaMaterial findColladaMaterialByIdentifier(String identifier)
    {
        //  First, attempt to find a ColladaMaterialInstance.
        return m_MaterialMap.get(identifier);
    }



    
    

//  ******************************
//  ColladaMaterialInstance methods.
//  ******************************

    //  Adds a ColladaMaterialInstance.
    public void addColladaMaterialInstance(PColladaMaterialInstance pColladaMaterialInstance)
    {
        //  Put the ColladaMaterialInstance into the MaterialInstances HashMap.
        m_MaterialInstances.put(pColladaMaterialInstance.getInstanceSymbolString(), pColladaMaterialInstance);

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
    public PColladaMaterialInstance findColladaMaterialInstanceBySymbol(String instanceSymbol)
    {
        //  Get the ColladaMaterialInstance from the HashMap.
        return(m_MaterialInstances.get(instanceSymbol));
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
        PColladaNode pColladaNode;

        for (int i = 0; i < getFactoryColladaNodeCount(); i++)
        {
            pColladaNode = getFactoryColladaNode(i);
            if (name.equals(pColladaNode.getName()))
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
        for (PColladaNode node : m_ColladaNodes)
        {
            dumpColladaNode(node);
        }
    }

    public void dumpColladaNode(PColladaNode theNode)
    {
//        System.out.println("Node:  " + theNode.getName());
//        System.out.println("   Mesh:    " + theNode.getMeshName());
//        if (theNode.isJoint())
//            System.out.println("   Joint:   " + theNode.getJointName());
//
//        PColladaMaterialInstance pMaterialInstance = theNode.getMaterialInstance();
//        if (pMaterialInstance != null)
//        {
//            System.out.println("   MaterialInstance:");
//            System.out.println("      InstanceName:   " + pMaterialInstance.getInstanceSymbolString());
//            System.out.println("      targetMaterialURL:   " + pMaterialInstance.getTargetMaterialURL());
//            System.out.println("      VertexInputs:");
//            for (int b=0; b<pMaterialInstance.getVertexInputCount(); b++)
//                System.out.println("         " + pMaterialInstance.getVertexInput(b));
//        }
//
//        // recurse
//        for (int i = 0; i < theNode.getChildNodeCount(); i++)
//            dumpColladaNode(theNode.getChildNode(i));
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
    public PPolygonMesh createPolygonMesh()
    {
        PPolygonMesh newMesh = new PPolygonMesh();

        return newMesh ;
    }

    //  Adds a PolygonMesh.
    public void addPolygonMesh(PPolygonMesh polyMesh)
    {
        if (polyMesh instanceof PPolygonSkinnedMesh)
            m_PolygonSkinnedMeshes.add((PPolygonSkinnedMesh)polyMesh);
        else
            m_PolygonMeshes.add(polyMesh);
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
        for (PPolygonMesh polyMesh : m_PolygonMeshes)
        {
            if (meshName.equals(polyMesh.getName()))
                return polyMesh;
        }

        return null;
    }

    //  Removes a PolygonMesh.
    public void removePolygonMesh(PPolygonMesh pPolygonMesh)
    {
        m_PolygonMeshes.remove(pPolygonMesh);
    }




    //  ******************************
    //  PolygonSkinnedMesh management methods.
    //  ******************************
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
        String meshURL = pColladaNode.getMeshURL();
        String nodeInstanceName = pColladaNode.getInstanceNodeName();
        PPolygonMeshInstance pMeshInstance;
        PNode pThisNode = null;

        try
        {
            //  ****  MeshInstance Node.
            if (meshURL != null)
            {
                //System.out.println("MeshName:  " + meshName);
                if (!bIgnoreMeshInstances)
                {
                    PPolygonMesh pPolygonMesh = findPolygonMesh(meshURL);
                    if (pPolygonMesh != null)
                    {
                        //  Create a MeshInstance.
                        pMeshInstance = createMeshInstance(pPolygonMesh, pColladaNode, meshName);
                        pThisNode = pMeshInstance;
                    }
                }
            }

            //  ****  NodeInstance Node.
            else if (nodeInstanceName != null)
            {
                PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);
                if (pInstancedColladaNode != null)
                {
                    pThisNode = new PNode(pColladaNode.getName());
                    pThisNode.setTransform(new PTransform());
                    pThisNode.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

                    pColladaNode = pInstancedColladaNode;
                }
            }

            //  If there is no MS3D_Mesh, then this is a joint.
            else
            {
                if (pColladaNode.isJoint())
                {
                    //  Create a SkinnedMeshJoint
                    SkinnedMeshJoint pSkinnedMeshJoint = new SkinnedMeshJoint(pColladaNode.getName(),
                            new PTransform(pColladaNode.getMatrix()));

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

    public void processColladaNodeForSkeleton(PColladaNode pColladaNode, SkeletonNode pSkeleton)
    {
        //  Ignore Joints.
        if (pColladaNode.isJoint())
            return;

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

                        //  Get the JointName of the first Bone in the MS3D_SkinnedMesh1.
                        firstJointName = pColladaSkin.getJointName(0);
                        if (firstJointName.equals(skeletonName))
                        {
                            //  Create a PolygonSkinnedMeshInstance.
                            PPolygonSkinnedMesh pSkinnedMesh = findPolygonSkinnedMesh(skinnedMeshName);

                            m_pSkeletonNode.addChild(pSkinnedMesh);
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
        for (PColladaNode node : m_ColladaNodes)
        {
            System.out.println("Root ColladaNode '" + node.getName() + "', isJoint " + ((node.isJoint()) ? "yes" : "no"));

            processColladaNode(node, m_pLoadingPScene.getInstances());
        }
    }
    
    public void processColladaNode(PColladaNode colladaNode, PNode pParentNode)
    {
        PNode processedNode = null;
        //  Ignore Joints.
        if (colladaNode.isJoint())
            return;

        boolean bSkeletonsCreated = true;

        //  If the ColladaNode contains skeletons, we need to build a
        //  PolygonSkinnedMeshInstance tree.
        if (colladaNode.getSkeletonCount() > 0)
        {
            String skeletonName = null;
            PColladaNode pSkeletonColladaNode = null;
            String meshName = null;
            for (int i = 0; i < colladaNode.getSkeletonCount(); i++)
            {
                skeletonName = colladaNode.getSkeleton(i);
                meshName = colladaNode.getMeshName();
                // Set a default mesh name if none was used
                if (meshName == null)
                    meshName = new String("SkinnedMeshInstance from Collada.java : 1521");
                pSkeletonColladaNode = findColladaNode(skeletonName);

                if (pSkeletonColladaNode == null)
                    System.out.println("   Unable to find skeleton node '" + skeletonName + "'!");

                processedNode = buildPolygonSkinnedMeshInstance(colladaNode, pParentNode, meshName);
                if (processedNode != null)
                    pParentNode.addChild(processedNode);
            }
            
            bSkeletonsCreated = false;
        }
        //  Otherwise, if the ColladaNode was assigned a MeshName, we need
        //  to create a PolygonMeshInstance.
        else if (colladaNode.getMeshName() != null)
        {
            String meshURL = colladaNode.getMeshURL();
            String meshName = colladaNode.getMeshName();
            PPolygonMesh polyMesh;

            polyMesh = findPolygonMesh(meshURL);

            if (polyMesh != null)
            {
                processedNode = createMeshInstance(polyMesh, colladaNode, meshName);
                bSkeletonsCreated = false;
            }
            else
                System.out.println("   Unable to find Mesh named " + meshName + " with URL'" + meshURL + "'!");
        }
        //  Otherwise, if the ColladaNode was assigned a InstanceNodeName, we need
        //  to create an instance of a PolygonMesh.
        else if (colladaNode.getInstanceNodeName() != null)
        {
            String nodeInstanceName = colladaNode.getInstanceNodeName();
                    
            PColladaNode instancedNode = findFactoryColladaNode(nodeInstanceName);
            if (instancedNode != null)
            {
                processedNode = new PNode(colladaNode.getName());
                processedNode.setTransform(new PTransform());
                processedNode.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());

                colladaNode = instancedNode;

                bSkeletonsCreated = false;
            }
        }
        //  Otherwise, we need to create a Node.
        else
        {
            processedNode = new PNode(colladaNode.getName());
            processedNode.setTransform(new PTransform());
            processedNode.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());
            bSkeletonsCreated = false;
        }


//        if (bSkeletonsCreated)
//            return;


        if (processedNode != null)
            pParentNode.addChild(processedNode);


        //  Process all child nodes.
        if (colladaNode.getChildNodeCount() > 0)
        {
            int a;
            PColladaNode pChildColladaNode;
            
            for (a=0; a<colladaNode.getChildNodeCount(); a++)
            {
                pChildColladaNode = colladaNode.getChildNode(a);

                processColladaNode(pChildColladaNode, processedNode);
            }
        }
    }

    private PPolygonSkinnedMeshInstance buildPolygonSkinnedMeshInstance(PColladaNode colladaNode, PNode parent, String meshName)
    {
        PPolygonSkinnedMeshInstance result = null;

        PPolygonSkinnedMesh skinnedMeshGeometry = findPolygonSkinnedMesh(colladaNode.getControllerName());

        result = m_pLoadingPScene.processSkinnedMesh(skinnedMeshGeometry);
        result.setName(meshName);
        result.setTransform(new PTransform(colladaNode.getMatrix()));

        parent.addChild(result);

        return result;
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

        createColladaNode(m_pLoadingPScene.getInstances(), getColladaNodeWithChildren(), false);
            
        //  Must process the Node hiearchy loaded.
        PNode pJointHierarchy = null;//pSkinnedMesh.getBindPoseTransformHierarchy();
        
        for (a=0; a<m_pLoadingPScene.getInstances().getChildrenCount(); a++)
        {
            pChildNode = m_pLoadingPScene.getInstances().getChild(a);

            //  Only add PJoints to the PolygonSkinnedMesh joint hiearchy node.
            if (pChildNode instanceof PJoint)
            {
                m_pLoadingPScene.getInstances().removeChild(pChildNode);
                a--;

                pJointHierarchy.addChild(pChildNode);
            }
        }

        AnimationGroup pAnimationLoop = new AnimationGroup();
    
        //  Create all the JointAnimations.
        processNodeInJointHierarchy(pJointHierarchy, false, pAnimationLoop);
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

            m_pLoadingPScene.addModelInstance(pPolygonMesh, identityMatrix);
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
                
                SharedAsset asset = new SharedAsset(m_pLoadingPScene.getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, pSkinnedMesh.getName()));
                asset.setAssetData(pSkinnedMesh);
                
                PPolygonModelInstance pModelInstance = m_pLoadingPScene.addModelInstance(asset, pSkinnedMeshMatrix);
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

                createColladaNode(m_pLoadingPScene.getInstances(), pColladaNode, false);
            }

            //  Add the root node to the Scene.
            //m_pLoadingPScene.addInstanceNode(pRootNode);
            
            //m_pLoadingPScene.setDirty(true, true);
        }
    }

    //  Creates a MeshInstance.
    public PPolygonMeshInstance createMeshInstance(PPolygonMesh pPolygonMesh, PColladaNode pColladaNode, String meshName)
    {
        PPolygonMeshInstance pMeshInstance = null;

        //  Create the MeshInstance.
        SharedAsset asset = new SharedAsset(    m_pLoadingPScene.getRepository(),
                                                new AssetDescriptor(SharedAsset.SharedAssetType.Unknown,
                                                                    pPolygonMesh.getName()));
        asset.setAssetData(pPolygonMesh);

        pMeshInstance = (PPolygonMeshInstance)m_pLoadingPScene.addMeshInstance(meshName, asset);

        if (pColladaNode != null)
            pMeshInstance.getTransform().getLocalMatrix(true).set(pColladaNode.getMatrix());

        if (pColladaNode != null)
        {
            ColladaMaterial colladaMaterial    = null;
            PColladaEffect colladaEffect                    = null;
            PMeshMaterial meshMat                               = null;
        
            colladaMaterial = pColladaNode.getMaterial();

            if (colladaMaterial != null)
            {
                colladaEffect = findColladaEffectByIdentifier(colladaMaterial.getInstanceEffectTargetURL());
                try
                {
                    meshMat = colladaEffect.createMeshMaterial();
                    pMeshInstance.setMaterial(meshMat);
                    pMeshInstance.setUseGeometryMaterial(false);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }


        //  Are there sub-meshes?
        if (pPolygonMesh.getChildrenCount() > 0)
        {
            PPolygonMesh pSubPolygonMesh;
            PPolygonMeshInstance pSubMeshInstance;
            PMeshMaterial pMeshMaterial = null;

            for (int i = 0; i < pPolygonMesh.getChildrenCount(); i++)
            {
                pSubPolygonMesh = (PPolygonMesh)pPolygonMesh.getChild(i);
                                
                //  Create a sub MeshInstance.
                pSubMeshInstance = createMeshInstance(pSubPolygonMesh, null, "SomeSubMesh");

                pMeshMaterial = pSubPolygonMesh.getMaterialCopy();
                pSubMeshInstance.setMaterial(pMeshMaterial);
                pSubMeshInstance.setUseGeometryMaterial(false);                        

                pMeshInstance.addChild(pSubMeshInstance);
            }
        }


        return(pMeshInstance);
    }

    void createColladaNode(PNode pParentNode, PColladaNode colladaNode, boolean bIgnoreMeshInstances)
    {
        String meshURL = colladaNode.getMeshURL();
        String meshName = colladaNode.getMeshName();
        String nodeInstanceName = colladaNode.getInstanceNodeName();
        PNode processedNode = null;

        if (meshURL != null)
        {
            if (!bIgnoreMeshInstances)
            {
                PPolygonMesh pPolygonMesh = findPolygonMesh(meshURL);
                if (pPolygonMesh != null)
                {
                    // Determine a good name
                    if (meshName == null)
                        meshName = new String("Unnamed Mesh created at Collada.java : 1864");
                    //  Create a MeshInstance.
                    processedNode = createMeshInstance(pPolygonMesh, colladaNode, meshName);
                }
            }
        }
        else if (nodeInstanceName != null) // No mesh URL, maybe this is an instance node?
        {
            PColladaNode pInstancedColladaNode = findFactoryColladaNode(nodeInstanceName);

            if (pInstancedColladaNode != null)
            {
                processedNode = new PNode(colladaNode.getName());
                processedNode.setTransform(new PTransform());
                processedNode.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());
            }
        }
        else // Nope, is it a joint perhaps?
        {
            if (colladaNode.isJoint() == true)
            {
                //  Create a SkinnedMeshJoint
                SkinnedMeshJoint pSkinnedMeshJoint = new SkinnedMeshJoint(colladaNode.getName(),
                        new PTransform(colladaNode.getMatrix()));
                processedNode = pSkinnedMeshJoint;
            }
            else
            {
                processedNode = new PJoint(new PTransform(colladaNode.getMatrix()));
                processedNode.setName(colladaNode.getName());
                processedNode.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());
            }
        }

        if (processedNode != null)
        {
            pParentNode.addChild(processedNode);

            for (int i = 0; i < colladaNode.getChildNodeCount(); i++)
            {
                PColladaNode child = colladaNode.getChildNode(i);

                createColladaNode(processedNode, child, bIgnoreMeshInstances);
            }
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





