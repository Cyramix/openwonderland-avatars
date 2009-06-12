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
package imi.loaders.collada;

import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.collada.colladaschema.COLLADA;
import org.collada.xml_walker.ProcessorFactory;
import org.collada.xml_walker.PColladaNode;
import org.collada.xml_walker.PColladaMaterialInstance;
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
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.PJoint;
import imi.scene.utils.tree.PPolygonMeshAssemblingProcessor;
import imi.scene.utils.tree.TreeTraverser;
import java.io.IOException;
import java.io.InputStream;
import java.lang.IllegalArgumentException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import javolution.util.FastMap;
import org.collada.colladaschema.Asset.Contributor;
import org.collada.xml_walker.ColladaMaterial;



/**
 * Collada loading class.
 * @author paulby
 * @author Chris Nagle
 * @author Ronald E Dahlgren
 */
public class Collada
{
    /** Logger reference **/
    private static final Logger logger = Logger.getLogger(Collada.class.getName());
    /** Context for the unmarshaller to operate within **/
    private static javax.xml.bind.JAXBContext jaxbContext = null;
    /** Used to synchronize access to the jaxb context **/
    private static final Integer contextLock = Integer.valueOf(0);
    /** The unmarshaller used to generate the DOM each time a file is loaded **/
    private static javax.xml.bind.Unmarshaller unmarshaller = null;

    static
    {
        // Attempt to create the context and unmarshaller for JAXB
        try
        {
            jaxbContext = javax.xml.bind.JAXBContext.newInstance("org.collada.colladaschema", Collada.class.getClassLoader());
        }
        catch (JAXBException ex)
        {
//            ex.printStackTrace();
            logger.log(Level.SEVERE, "Problem initializing JAXB Context and / or unmarshaller! -- " + ex.getMessage());
        }
    }

    private List    m_Libraries = null;
    private String  m_Name = "ColladaLoader";
    private URL     m_fileLocation = null;

    /** Assorted library convenience references **/
    private LibraryCameras      m_libraryCameras        = null;
    private LibraryImages       m_libraryImages         = null;
    private LibraryEffects      m_libraryEffects        = null;
    private LibraryMaterials    m_libraryMaterials      = null;
    private LibraryAnimations   m_libraryAnimations     = null;
    private LibraryVisualScenes m_libraryVisualScenes   = null;
    private LibraryGeometries   m_libraryGeometries     = null;
    private LibraryControllers  m_libraryControllers    = null;
    private LibraryNodes        m_libraryNodes          = null;

    // Effect mapping and list
    private final FastMap<String, PColladaEffect>   m_EffectMap = new FastMap<String, PColladaEffect>();
    private final ArrayList<PColladaEffect>         m_EffectList = new ArrayList<PColladaEffect>();
    // Material mapping and list
    private final FastMap<String, ColladaMaterial>  m_MaterialMap = new FastMap<String, ColladaMaterial>();
    private final ArrayList<ColladaMaterial>        m_MaterialList = new ArrayList<ColladaMaterial>();
    // Material instance mapping and list
    private final FastMap<String, PColladaMaterialInstance>   m_MaterialInstances = new FastMap<String, PColladaMaterialInstance>();
    private final ArrayList                                   m_ColladaMaterialInstances = new ArrayList();

    private SkeletonNode    m_skeletonNode  = null;
    private PScene          m_loadingPScene = null;

    // Maximum number of weights to be read per-vertex
    private int                             m_MaxNumberOfWeights = 4;
    //  Contains the PolygonMeshes loaded.
    private final ArrayList<PPolygonMesh>    m_PolygonMeshes = new ArrayList<PPolygonMesh>();

    //  Contains the PolygonSkinnedMeshes loaded.
    private final ArrayList<PPolygonSkinnedMesh> m_PolygonSkinnedMeshes = new ArrayList<PPolygonSkinnedMesh>();
    private final ArrayList<PColladaSkin>        m_ColladaSkins = new ArrayList<PColladaSkin>();
    private final ArrayList<PColladaNode>        m_ColladaNodes = new ArrayList<PColladaNode>();
    private final ArrayList<PColladaNode>        m_FactoryColladaNodes = new ArrayList<PColladaNode>();
    
    private final ArrayList<PColladaAnimatedItem>     m_ColladaAnimatedItems = new ArrayList<PColladaAnimatedItem>();
    private final ArrayList<PColladaCameraParams>     m_ColladaCameraParams = new ArrayList<PColladaCameraParams>();
    private final ArrayList<PColladaCamera>           m_ColladaCameras = new ArrayList<PColladaCamera>();

    private boolean m_bLoadRig                      = false;
    private boolean m_bLoadGeometry                 = true;
    private boolean m_bLoadAnimations               = false;
    private boolean m_bAddSkinnedMeshesToSkeleton   = true;
    private boolean m_bPrintStats                   = false;

    /**
     * Default construction
     */
    public Collada()
    {
    }

    /**
     * Clear out any residual state in the collada loader
     */
    public void clear()
    {
        m_bPrintStats = false;
        m_bAddSkinnedMeshesToSkeleton = true;
        m_bLoadAnimations = false;
        m_bLoadGeometry = true;
        m_bLoadRig = false;
        m_ColladaCameras.clear();
        m_ColladaCameraParams.clear();
        m_ColladaAnimatedItems.clear();
        m_MaxNumberOfWeights = 4;
        m_FactoryColladaNodes.clear();
        m_ColladaNodes.clear();
        m_ColladaSkins.clear();
        m_PolygonSkinnedMeshes.clear();
        m_PolygonMeshes.clear();
        m_Libraries = null;
        m_Name = "ColladaLoader";
        m_fileLocation = null;
        m_libraryCameras = null;
        m_libraryImages = null;
        m_libraryEffects = null;
        m_libraryMaterials = null;
        m_libraryAnimations = null;
        m_libraryVisualScenes = null;
        m_libraryGeometries = null;
        m_libraryControllers = null;
        m_libraryNodes = null;
        m_EffectMap.clear();
        m_EffectList.clear();
        m_MaterialMap.clear();
        m_MaterialList.clear();
        m_MaterialInstances.clear();
        m_ColladaMaterialInstances.clear();
        m_skeletonNode = null;
        m_loadingPScene = null;
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
        return m_loadingPScene;
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


    /**
     * Same as the single parameter load, except rather than using a newly created
     * PScene, the one provided will be used as the target for the loading process.
     * @param loadingPScene The pscene to load into
     * @param colladaFile
     * @return
     */
    public boolean load(PScene loadingPScene, URL colladaFile) throws ColladaLoadingException {
        boolean result = false;
        m_fileLocation = colladaFile;
        m_loadingPScene = loadingPScene;
        m_loadingPScene.setUseRepository(false); // the repository will extract the data later
        
        final int maxNumberOfRetries = 5;
        int retry = 1; // Retry while > 0 and <= maxNumberOfRetries

        m_fileLocation = colladaFile;
        URLConnection conn = null;
        InputStream in = null;

        while (retry > 0 && retry <= maxNumberOfRetries)
        {
            org.collada.colladaschema.COLLADA collada = null;
            try // to open the connection and unmarshal the file
            {
                conn = colladaFile.openConnection();
                in = conn.getInputStream();
                synchronized(contextLock) {
                    unmarshaller = jaxbContext.createUnmarshaller();
                    collada = (org.collada.colladaschema.COLLADA) unmarshaller.unmarshal(in);
                }
                retry = 0; // No retry necessary
            }
            catch (Exception exception)
            {
                if (exception.getMessage().equals("Connection refused"))
                {
                    logger.warning(exception.getMessage() + "... Retrying");
                    retry++;
                }
                else
                {
                    logger.severe("Exception while attempting to open collada file! : " +
                            exception.getMessage());
                    exception.printStackTrace();
                    // Do not retry, simple abort
                    retry = Integer.MAX_VALUE;
                }
            }
            finally
            {
                try // to close the connection
                {
                    if (in != null)
                        in.close();
                }
                catch (IOException ex)
                {
                    logger.warning("Caught exception closing: " + ex.getMessage());
                    result = false;
                }
                if (retry > 0)
                    continue;
            }
            doLoad(collada);
            result = true;
        } // End while loop

        // If there are no joints in the skeleton we don't need a skeleton
        // (this is probably a non skinned mesh)
        if (m_skeletonNode==null || m_skeletonNode.getSkeletonRoot()==null || m_skeletonNode.getSkeletonRoot().getChildrenCount() == 0)
            m_loadingPScene.getInstances().removeChild(m_skeletonNode);

        return result;
    }

    /**
     * Perform the actual processing
     * @param collada
     */
    private void doLoad(COLLADA collada) throws ColladaLoadingException
    {
        // verify bakeTransforms was armed
        if (!isUsingBakeTransforms(collada))
            throw new ColladaLoadingException("COLLADA files exported with Feeling Software's ColladaMaya " +
                    "exporter must be exported with the bakeTransforms option enabled.");
        m_Libraries = collada.getLibraryLightsAndLibraryGeometriesAndLibraryAnimationClips();

        m_libraryCameras       = getInstanceOfLibraryCameras();
        m_libraryImages        = getInstanceOfLibraryImages();
        m_libraryEffects       = getInstanceOfLibraryEffects();
        m_libraryMaterials     = getInstanceOfLibraryMaterials();
        m_libraryAnimations    = getInstanceOfLibraryAnimations();
        m_libraryVisualScenes  = getInstanceOfLibraryVisualScenes();
        m_libraryGeometries    = getInstanceOfLibraryGeometries();
        m_libraryControllers   = getInstanceOfLibraryControllers();
        m_libraryNodes         = getInstanceOfLibraryNodes();

        //  Create the SkeletonNode if we're loading the rig.
        if (m_bLoadRig)
            m_skeletonNode = createSkeletonNode();
        //  Need to process these Libraries in this order.
        if (m_bLoadGeometry)
        {
            ProcessorFactory.createProcessor(this, m_libraryCameras, null);
            ProcessorFactory.createProcessor(this, m_libraryImages, null);
            ProcessorFactory.createProcessor(this, m_libraryEffects, null);
            ProcessorFactory.createProcessor(this, m_libraryMaterials, null);
        }



        if (m_bLoadGeometry)
            ProcessorFactory.createProcessor(this, m_libraryNodes, null);

        //  Preprocess the node hiearchy.
        //  Builds the node hiearchy.
        ProcessorFactory.createProcessor(this, m_libraryVisualScenes, null);

        //  Gets the load option.
        if (m_bLoadGeometry)
        {
            ProcessorFactory.createProcessor(this, m_libraryGeometries, null);
            ProcessorFactory.createProcessor(this, m_libraryControllers, null);
        }

        if (m_bLoadRig)
            processRig();
        
        //  Only load the Animations if we should.
        if (m_bLoadAnimations)
            ProcessorFactory.createProcessor(this, m_libraryAnimations, null);

        if (m_bLoadGeometry && m_bAddSkinnedMeshesToSkeleton)
            attachSkinnedMeshToSkeleton();

        //if (m_skeletonNode == null)
            processColladaNodes();

        // Submit the geometry of every PPolygonMesh we encounter
        TreeTraverser.breadthFirst(m_loadingPScene, new PPolygonMeshAssemblingProcessor());
    }



    //  Processes the Rig.
    private PNode processRig()
    {
        PNode rootNode = new PNode("RootNode created in Collada.java : processRig", new PTransform(new PMatrix()));

        for (PColladaNode currentColladaNode : m_ColladaNodes)
            if (currentColladaNode.isJoint())
                processRig(rootNode, currentColladaNode, false);

        m_skeletonNode.setSkeletonRoot(rootNode);
        m_loadingPScene.addInstanceNode(m_skeletonNode);

        return(rootNode);
    }

    private PNode processRig(PNode parentNode, PColladaNode colladaNode, boolean bIgnoreMeshInstances)
    {
        String meshURL = colladaNode.getMeshURL();
        String meshName = colladaNode.getMeshName();
        String nodeInstanceName = colladaNode.getInstanceNodeName();

        PNode result = null;

        try
        {
            if (meshURL != null && !bIgnoreMeshInstances) //  ****  MeshInstance Node.
            {

                PPolygonMesh polyMesh = findPolygonMesh(meshURL);
                if (polyMesh != null)
                    result = createMeshInstance(polyMesh, colladaNode, meshName);
            }
            else if (nodeInstanceName != null) //  ****  NodeInstance Node.
            {
                PColladaNode instancedNode = findFactoryColladaNode(nodeInstanceName);
                if (instancedNode != null)
                {
                    result = new PNode(colladaNode.getName());
                    result.setTransform(new PTransform(colladaNode.getMatrix()));

                    colladaNode = instancedNode;
                }
            }
            else //  If there is no MS3D_Mesh, then this is a joint.
            {
                if (colladaNode.isJoint())
                    result = new SkinnedMeshJoint(colladaNode.getName(), new PTransform(colladaNode.getMatrix()));
                else
                {
                    //  Just create a Node.
                    result = new PJoint(new PTransform(colladaNode.getMatrix()));
                    result.setName(colladaNode.getName());
                    // if it has a local transform, then set it
                    if (colladaNode.getMatrix() != null)
                        result.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());
                }
            }

            if (parentNode != null)
                parentNode.addChild(result);

            //  recurse through all the child Nodes
            if (colladaNode.getChildren() != null)
                for (PColladaNode childNode : colladaNode.getChildren())
                    processRig(result, childNode, bIgnoreMeshInstances);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return(result);
    }



    private void attachSkinnedMeshToSkeleton()
    {
        int skinnedMeshCount = m_PolygonSkinnedMeshes.size();
        if (skinnedMeshCount > 0 && m_skeletonNode != null)
        {
            PPolygonSkinnedMesh mesh = null;
            for (int i = 0; i < skinnedMeshCount; ++i)
            {
                mesh = m_PolygonSkinnedMeshes.get(i);
                m_skeletonNode.addChild(mesh);
                mesh.linkJointsToSkeletonNode(m_skeletonNode);
            }

            m_skeletonNode.setDirty(true, true);
        }
    }




    //  ********************************
    //  SkeletonNode methods.
    //  ********************************

    //  Creates the SkeletonNode.
    public SkeletonNode createSkeletonNode()
    {
        if (m_skeletonNode == null)
            m_skeletonNode = new SkeletonNode("Untitled");

        return(m_skeletonNode);
    }

    //  Gets the SkeletonNode.
    public SkeletonNode getSkeletonNode()
    {
        return(m_skeletonNode);
    }

    //  Sets the SkeletonNode.
    public void setSkeletonNode(SkeletonNode skeletonNode)
    {
        m_skeletonNode = skeletonNode;
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
    public void addColladaCameraParams(PColladaCameraParams cameraParams)
    {
        m_ColladaCameraParams.add(cameraParams);
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
        for (PColladaCameraParams camParam : m_ColladaCameraParams)
            if (name.equals(camParam.getName()))
                return camParam;

        return null;
    }

//  ******************************
//  ColladaCamera methods.
//  ******************************

    //  Adds a ColladaCamera.
    public void addColladaCamera(PColladaCamera camera)
    {
        m_ColladaCameras.add(camera);
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
        for (PColladaCamera cam : m_ColladaCameras)
            if (name.equals(cam.getName()))
                return cam;
        return null;
    }

//  ******************************
//  ColladaSkin methods.
//  ******************************

    //  Adds a ColladaSkin.
    public void addColladaSkin(PColladaSkin colladaSkin)
    {
        m_ColladaSkins.add(colladaSkin);
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
            if (name.equals(skin.getName()))
                return skin;
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
    public void addColladaMaterialInstance(PColladaMaterialInstance colladaMaterialInstance)
    {
        //  Put the ColladaMaterialInstance into the MaterialInstances HashMap.
        m_MaterialInstances.put(colladaMaterialInstance.getInstanceSymbolString(), colladaMaterialInstance);

        m_ColladaMaterialInstances.add(colladaMaterialInstance);
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
    public void addFactoryColladaNode(PColladaNode colladaNode)
    {
        m_FactoryColladaNodes.add(colladaNode);
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
        for (PColladaNode node : m_FactoryColladaNodes)
            if (name.equals(node.getName()))
                return node;
        return null;
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
        for (PColladaNode node : m_ColladaNodes)
            if (node.getChildNodeCount() > 0)
                return node;
        return null;
    }

    //  Finds the ColladaNode with the specified name.
    public PColladaNode findColladaNode(String nodeName)
    {
        for (PColladaNode node : m_ColladaNodes)
        {
            PColladaNode result = node.findNode(nodeName);
            if (result != null)
                return result;
        }
        return null;
    }

    //  Finds the ColladaNode with the specified joint name.
    public PColladaNode findJointColladaNode(String jointName)
    {
        for (PColladaNode node : m_ColladaNodes)
        {
            PColladaNode result = node.findJoint(jointName);
            if (result != null)
                return result;
        }
        return null;
    }

    public void dumpColladaNodes()
    {
//        for (PColladaNode node : m_ColladaNodes)
//            dumpColladaNode(node);
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
        for (PColladaAnimatedItem anim : m_ColladaAnimatedItems)
            if (name.equals(anim.getName()))
                return anim;
        return null;
    }




//  ******************************
//  PolygonMesh management methods.
//  ******************************

    //  Creates a PolygonMesh.
    public PPolygonMesh createPolygonMesh()
    {
        return new PPolygonMesh() ;
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
    public void removePolygonMesh(PPolygonMesh polygonMesh)
    {
        m_PolygonMeshes.remove(polygonMesh);
    }




    //  ******************************
    //  PolygonSkinnedMesh management methods.
    //  ******************************
    //  Adds a PolygonSkinnedMesh.
    public void addPolygonSkinnedMesh(PPolygonSkinnedMesh polygonSkinnedMesh)
    {
        m_PolygonSkinnedMeshes.add(polygonSkinnedMesh);
    }

    //  Gets the number of PolygonSkinnedMesh.
    public int getPolygonSkinnedMeshCount()
    {
        return(m_PolygonSkinnedMeshes.size());
    }
    
    //  Gets the PolygonSkinnedMesh at the specified index.
    public PPolygonSkinnedMesh getPolygonSkinnedMesh(int index)
    {
        return( (PPolygonSkinnedMesh)m_PolygonSkinnedMeshes.get(index));
    }

    //  Finds the PolygonSkinnedMesh with the specified name.
    public PPolygonSkinnedMesh findPolygonSkinnedMesh(String meshName)
    {
        for (PPolygonSkinnedMesh skinMesh : m_PolygonSkinnedMeshes)
            if (meshName.equals(skinMesh.getName()))
                return skinMesh;
        return null;
    }

    private void processColladaNodes()
    {   
        for (PColladaNode node : m_ColladaNodes)
        {
            // Debugging / diagnostic output
//            logger.info("Root ColladaNode '" + node.getName() + "', isJoint " + ((node.isJoint()) ? "yes" : "no"));
            processColladaNode(node, m_loadingPScene.getInstances());
        }
    }
    
    private void processColladaNode(PColladaNode colladaNode, PNode parentNode)
    {
        PNode processedNode = null;
        //  Ignore Joints.
        if (colladaNode.isJoint())
            return;

        //  If the ColladaNode contains skeletons, we need to build a
        //  PolygonSkinnedMeshInstance tree.
        if (colladaNode.getSkeletonCount() > 0)
        {
            String meshName = null;
            meshName = colladaNode.getMeshName();
            // Set a default mesh name if none was used
            if (meshName == null) // try the controller name
                meshName = colladaNode.getControllerName();
            if (meshName == null) // still null? Give a default then
                meshName = "SkinnedMeshInstance from Collada.java : processColladaNode";

            processedNode = buildPolygonSkinnedMeshInstance(colladaNode, parentNode, meshName);
            if (processedNode != null)
                parentNode.addChild(processedNode);
        }
        //  Otherwise, if the ColladaNode was assigned a MeshName, we need
        //  to create a PolygonMeshInstance.
        else if (colladaNode.getMeshName() != null)
        {
            String meshURL = colladaNode.getMeshURL();
            String meshName = colladaNode.getMeshName();
            PPolygonMesh polyMesh = null;
            polyMesh = findPolygonMesh(meshURL);

            if (polyMesh != null)
                    processedNode = createMeshInstance(polyMesh, colladaNode, meshName);
            else
                logger.warning("   Unable to find Mesh named " + meshName + " with URL'" + meshURL + "'!");
        }
        //  Otherwise, if the ColladaNode was assigned a InstanceNodeName, we need
        //  to create an instance of a PolygonMesh.
        else if (colladaNode.getInstanceNodeName() != null)
        {
            String nodeInstanceName = colladaNode.getInstanceNodeName();
                    
            PColladaNode instancedNode = findFactoryColladaNode(nodeInstanceName);
            if (instancedNode != null)
            {
                colladaNode = instancedNode;
                processedNode = new PNode(colladaNode.getName(), new PTransform(colladaNode.getMatrix()));
            }
        }
        //  Otherwise, we need to create a Node.
        else
            processedNode = new PNode(colladaNode.getName(), new PTransform(colladaNode.getMatrix()));

        if (processedNode != null)
            parentNode.addChild(processedNode);


        //  Process all child nodes.
        if (colladaNode.getChildNodeCount() > 0)
        {
            for (PColladaNode kid : colladaNode.getChildren())
                processColladaNode(kid, processedNode);
        }
    }

    private PPolygonSkinnedMeshInstance buildPolygonSkinnedMeshInstance(PColladaNode colladaNode, PNode parent, String meshName)
    {
        PPolygonSkinnedMeshInstance result = null;

        for (PPolygonSkinnedMesh skinMesh : m_PolygonSkinnedMeshes)
        {
            if (skinMesh.getName().equals(colladaNode.getControllerName()))
            {

                result = m_loadingPScene.processSkinnedMesh(skinMesh);
                result.setName(meshName);
                result.setTransform(new PTransform(colladaNode.getMatrix()));

                parent.addChild(result);
            }
        }
        

        return result;
    }

    //  Creates a MeshInstance.
    public PPolygonMeshInstance createMeshInstance(PPolygonMesh polygonMesh, PColladaNode colladaNode, String meshName)
    {
        PPolygonMeshInstance meshInstance = null;

        //  Create the MeshInstance.
        SharedAsset asset = new SharedAsset(    m_loadingPScene.getRepository(),
                                                new AssetDescriptor(SharedAsset.SharedAssetType.Unknown,
                                                                    polygonMesh.getName()));
        asset.setAssetData(polygonMesh);

        meshInstance = (PPolygonMeshInstance)m_loadingPScene.addMeshInstance(meshName, asset);

        if (colladaNode != null && colladaNode.getMatrix() != null)
            meshInstance.getTransform().getLocalMatrix(true).set(colladaNode.getMatrix());

        if (colladaNode != null)
        {
            ColladaMaterial colladaMaterial = null;
            PColladaEffect colladaEffect    = null;
            PMeshMaterial meshMat           = null;
        
            colladaMaterial = colladaNode.getMaterial();

            if (colladaMaterial != null)
            {
                colladaEffect = findColladaEffectByIdentifier(colladaMaterial.getInstanceEffectTargetURL());
                if (colladaEffect != null)
                {
                    meshMat = colladaEffect.createMeshMaterial();
                    meshInstance.setMaterial(meshMat); // This should be applied elsewhere
                }
                else
                    logger.warning("Unable to locate effect with URL " + colladaMaterial.getInstanceEffectTargetURL());
 
            }
        }


        //  Are there sub-meshes?
        if (polygonMesh.getChildrenCount() > 0)
        {
            for (PNode node : polygonMesh.getChildren())
            {
                if (!(node instanceof PPolygonMesh))
                    continue;

                PPolygonMesh polyMesh = (PPolygonMesh)node;
                                
                //  Create a sub MeshInstance.
                PPolygonMeshInstance childMeshInstance = createMeshInstance(polyMesh, null, "SomeSubMesh");
                meshInstance.addChild(childMeshInstance);
            }
        }

        return(meshInstance);
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
     * Applies the provided configuration
     * @param params
     */
    public void applyConfiguration(ColladaLoaderParams params)
    {
        if (params == null) // use defaults
            clear();
        else
        {
            setLoadRig(params.isLoadingSkeleton());
            setLoadGeometry(params.isLoadingGeometry());
            setLoadAnimations(params.isLoadingAnimations());
            setPrintStats(params.isShowingDebugInfo());
            m_MaxNumberOfWeights = params.getMaxInfluences();
        }

    }
    
    public URL getFileLocation()
    {
        return m_fileLocation;
    }

    private boolean isUsingBakeTransforms(COLLADA collada) {
        boolean result = true;
        if (collada == null)
            throw new IllegalArgumentException("Must have a valid COLLADA document");
        List<Contributor> contributors = collada.getAsset().getContributors();
        if (contributors != null && contributors.size() > 0)
        {
            Contributor contributorZero = contributors.get(0);
            if (contributorZero != null)
            {
                String commentsString = contributorZero.getComments();
                int indexOfBakeTransforms = commentsString.indexOf("bakeTransforms=");
                // move index forward
                indexOfBakeTransforms += 15;
                Integer oneOrZero = Integer.parseInt(commentsString.substring(indexOfBakeTransforms, indexOfBakeTransforms+1));
                if (oneOrZero != 1)
                    result = false;
            }
            else
                logger.warning("Contributor Zero was null....");
        }
        else
            logger.warning("No contributors for for this asset.");
        return result;
    }
}





