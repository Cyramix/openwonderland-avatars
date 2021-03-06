/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package imi.scene;

import com.jme.scene.Node;
import com.jme.scene.Spatial;
import imi.loaders.Collada;
import imi.loaders.ColladaLoaderParams;
import imi.loaders.ColladaLoadingException;
import imi.repository.AssetDescriptor;
import imi.repository.AssetInitializer;
import imi.repository.Repository;
import imi.repository.RepositoryUser;
import imi.repository.SharedAsset;
import imi.repository.SharedAsset.SharedAssetType;
import imi.repository.SharedAssetPlaceHolder;
import imi.scene.JScene.ExternalKidsType;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.polygonmodel.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.scene.utils.traverser.PSceneSubmitHelper;
import imi.scene.utils.traverser.TreeTraverser;
import imi.utils.BooleanPointer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;
import javolution.util.FastList;
import javolution.util.FastTable;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * This scene is capable of procedural manipulation and editing of geometry. 
 * The data is shared localy or globaly via the repository. 
 * A PScene needs to be submited to its JScene for rendering.
 * 
 * @author Chris Nagle
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PScene extends PNode implements RepositoryUser, Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    // Local Geometry
    private FastList<PPolygonMesh> m_LocalGeometry = new FastList<PPolygonMesh>();
    
    // Shared Assets
    private boolean m_bUseRepository = true;
    private transient List<SharedAsset> m_SharedAssets  = new FastList<SharedAsset>();

    private transient List<SharedAssetPlaceHolder> m_SharedAssetWaitingList = new FastList<SharedAssetPlaceHolder>();

    // Instances in the scene
    private PNode m_Instances   = null;
    private transient JScene m_JScene = null;
    // Utility
    private transient WorldManager                m_WorldManager      = null;
    
    /**
     * Constructor, will create "m_Instances" node and add it as a child.
     * @param wm
     */
    public PScene(WorldManager wm)
    {
        setName("PScene");
        m_WorldManager = wm;
        m_Instances = new PNode("m_Instances", new PTransform());
        this.addChild(m_Instances);
    }

    /**
     * Constructor, will create "m_Instances" node and add it as a child.
     * @param name
     * @param wm   
     */
    public PScene(String name, WorldManager wm)
    {
        this(wm);
        setName(name);
    }

    /**
     * Special support method for deserialization. This should be called after
     * the world manager reference has been tied up.
     */
    public void finalizeDeserialization()
    {

    }

    /**
     * Set the jscene this pscene belongs to
     * @param j
     */
    void setJScene(JScene j)
    {
        if (j != null)
            m_JScene = j;
    }

    /**
     * The PScene will flatten its hierarchy (build the transform matrices)
     * and reconstruct the JScene it belongs to (reconstruct dirty triMeshes)
     * this will clean the dirty JScene.
     */
    public void submitTransformsAndGeometry(boolean updateModelBounds)
    {
        // potentially this could run in parallel
        submitGeometry();
        boolean anyMeshes = submitTransforms();
        if (updateModelBounds || anyMeshes) {
            m_JScene.updateModelBound();
        }
        m_JScene.updateWorldBound();
    }

    /**
     * Must be on the render thread!
     * The PScene will flatten its hierarchy (build the transform world matrices)
     * This method will clear all the children from m_JScene and resubmit thier
     * references according to the current PScene structure.
     */
    private transient int oldNumberOfKids = 0; // Used for making sure update model bounds happens judiciously
    boolean submitTransforms()
    {
        List<Spatial> kids = m_JScene.getChildren();
        
        // Nuke the m_JScene
        if (kids == null)
        {
            Node dead = new Node("kill me"); // kids = new... will not work
            m_JScene.attachChild(dead);
            m_JScene.detachChild(dead);
            kids = m_JScene.getChildren();
        }
        else
            kids.clear();
        
        // Flatten the hierarchy and gather the shared meshes
        PSceneSubmitHelper helper = new PSceneSubmitHelper();
        TreeTraverser.breadthFirst(m_Instances, helper);
        kids.addAll(helper.getSharedMeshes());

        // Add external kids
        Node externalKids = m_JScene.getExternalKidsRoot(ExternalKidsType.TRANSFORMED);
        externalKids.setLocalTranslation(m_JScene.getExternalKidsRootPosition());
        externalKids.setLocalRotation(m_JScene.getExternalKidsRootRotation());
        m_WorldManager.addToUpdateList(externalKids);
        kids.add(externalKids);

        Node externalKidsUntransformed = m_JScene.getExternalKidsRoot(ExternalKidsType.UNTRANSFORMED);
        m_WorldManager.addToUpdateList(externalKidsUntransformed);
        kids.add(externalKidsUntransformed);

        ////////////////////////////////////////
        /**
         * The oldNUmberOfKids ensures that this doesnt return true too much,
         * as each time it does the jscene's model bounds are recalculated!
         */
        m_Instances.setDirty(false, false);
        boolean result = (helper.getSharedMeshes().size() != oldNumberOfKids);
        oldNumberOfKids = helper.getSharedMeshes().size();
        return result;
    }

    /**
     * Any geometry data that is shared localy within this scene will check 
     * its dirty boolean and if true it will reconstruct its TriMesh.
     */
    @InternalAPI
    public synchronized void submitGeometry()
    {
        for (PPolygonMesh geometry : m_LocalGeometry)
            geometry.submit();
    }
    
    /**
     * Flip normals on local geometry
     */
    public synchronized void flipNormals()
    {
        // flip the normals for all the geometry in this scene
        for (PPolygonMesh geometry : m_LocalGeometry)
            geometry.flipNormals();
        
        setDirty(true, false);
    }
    
    /**
     * Toggling smooth normals will make the scene dirty
     */
    public synchronized void toggleSmoothNormals()
    {
        for (PPolygonMesh geometry : m_LocalGeometry)
            geometry.setSmoothNormals(!geometry.getSmoothNormals());
        
        setDirty(true, false);
    }

    /**
     * Add geometry to this scene to be shared locally
     * @param   meshAsset
     * @return  index of the meshAsset geometry
     */
    @InternalAPI
    public synchronized int addMeshGeometry(PPolygonMesh mesh)
    {
        // Duplicate checking
        int index = m_LocalGeometry.indexOf(mesh);
        
        if (index == -1)
        {
            mesh.setDirty(true, true);
            m_LocalGeometry.add(mesh);
            index = m_LocalGeometry.size()-1;
        }
        
        return index;
    }

    /**
     * Access the list of local (not shared) geometry
     * Note: this is a low level method used by the COLLADA loader...
     * @return
     */
    @InternalAPI
    public List<PPolygonMesh> getLocalGeometryList()
    {
        return m_LocalGeometry;
    }
    
    /**
     * Add an instance node directly to m_Instances
     * @param node
     */
    public void addInstanceNode(PNode node)
    {
        m_Instances.addChild(node);
    }
     
    /**
     * Add a model instance to this scene.
     * 
     * Adding an instance makes the scene dirty
     * @param model
     * @return modelInstance (PPolygonModelInstance)
     */
    public PPolygonModelInstance addModelInstance(PPolygonModelInstance model)
    {   
        PNode node = processNode(model);
        model.removeAllChildren();
        FastTable<PNode> children = node.getChildren();
        while (children.isEmpty() == false)
            model.addChild(children.get(0)); 
        
        m_Instances.addChild(model);
        setDirty(true, true);
        model.buildFlattenedHierarchy();
        
        return model;
    }
    
    /**
     * Add a model instance to this scene by supplying a PNode and a PMatrix
     * 
     * Adding an instance makes the scene dirty
     * @param node
     * @param origin
     * @return modelInstance (PPolygonModelInstance)
     */
    public PPolygonModelInstance addModelInstance(PNode node, PMatrix origin)
    {   
        PPolygonModelInstance modelInstance = new PPolygonModelInstance(node.getName());
        modelInstance.addChild(processNode(node));
        modelInstance.getTransform().getLocalMatrix(true).set(origin);
        modelInstance.setDirty(true, true);
        
        setDirty(true, true);
        m_Instances.addChild(modelInstance);
        
        modelInstance.buildFlattenedHierarchy();
        return modelInstance;
    }

    /**
     * Add an instance to this scene by supplying a PNode
     * 
     * Adding an instance makes the scene dirty
     * @param node
     * @param origin
     * @return meshInstance (PPolygonMeshInstance)
     */
    public PPolygonMeshInstance addMeshInstance(PNode node, PMatrix origin)
    {   
        PNode pNewNode = processNode(node);
        pNewNode.getTransform().getLocalMatrix(true).set(origin);
        pNewNode.setDirty(true, true);
        
        setDirty(true, true);
        m_Instances.addChild(pNewNode);
        
        pNewNode.buildFlattenedHierarchy();
        return (PPolygonMeshInstance)pNewNode;
    }

    /**
     * Retruns the root node of all instances in this scene
     * @return m_Instances (PNode)
     */
    public PNode getInstances() 
    {
        return m_Instances;
    }
    
    /**
     * Returns a reference to the single repository of this application
     * from the world manager.
     * @return Repository
     */
    public Repository getRepository()
    {
        return ((Repository)m_WorldManager.getUserData(Repository.class));
    }
    
    /**
     * Returns a reference to the world manager
     * @return m_WorldManager (WorldManager)
     */
    public WorldManager getWorldManager()
    {
        return m_WorldManager;
    }

    /**
     * Set the world manager
     * @param wm
     */
    @InternalAPI
    public void setWorldManager(WorldManager wm)
    {
        m_WorldManager = wm;
    }

    /** Returns the list of shared assets in the repository
     * @return m_SharedAssets (FastList<SharedAssets>)
     */
    List<SharedAsset> getAssetList() {
        return m_SharedAssets;
    }
    
    /**
     * Returns the list of placeholder assets
     * @return m_SharedAssetWaitingList (FastTable<SharedAssetPlaceHolder>)
     */
    List<SharedAssetPlaceHolder> getAssetWaitingList() {
        return m_SharedAssetWaitingList;
    }
    
    /**
     * Clears the scene
     */
    @InternalAPI
    public synchronized void clear() {
        m_SharedAssetWaitingList.clear();
        m_Instances.removeAllChildren();
        m_LocalGeometry.clear();
        m_SharedAssets.clear();
    }

    /***
     * Process a graph
     * @param node
     * @return
     */
    private PNode processNode(PNode node)
    {  
        if (node instanceof PPolygonSkinnedMesh)
            return processSkinnedMesh((PPolygonSkinnedMesh)node);
        else if (node instanceof PPolygonMesh)
            return processMesh((PPolygonMesh)node);
        else if (node instanceof SkinnedMeshJoint)
            return processSkinnedMeshJoint((SkinnedMeshJoint)node);
        else if (node instanceof PJoint)
            return processJoint((PJoint)node);
        else if (node instanceof PPolygonMeshInstance)
            return processMeshInstance((PPolygonMeshInstance)node);
        else if (node instanceof SkeletonNode)
            return processSkeletonNode((SkeletonNode)node);
//        else if (node instanceof PPolygonSkinnedMeshInstance)
//            return processSkinnedMesh((PPolygonSkinnedMesh)((PPolygonSkinnedMeshInstance)node).getGeometry());
//        else if (node instanceof PPolygonMeshInstance)
//            return processMesh((PPolygonMesh)((PPolygonMeshInstance)node).getGeometry());
         
        PNode nodeCopy     = new PNode(node.getName(), node.getTransform());
        
        for (int i = 0; i < node.getChildrenCount(); i++)
        {
            PNode kid = node.getChild(i);
            // skip the transform heirarchy nodes, they are processed by the
            // buildSkinnedMeshInstance method
            if (kid.getName().equals("m_TransformHierarchy"))
                continue;

            nodeCopy.addChild(processNode(kid));
        }
        
        return nodeCopy;
    }
    
    /**
     * Process a conceret meshAsset (contains geometry, not an instance)
     * and produce a meshAsset instance 
     * @param mesh
     * @return meshInst (PPolygonMeshInstance)
     */
    @InternalAPI
    public synchronized PPolygonMeshInstance processMesh(PPolygonMesh mesh)
    {
        // Put the kids in a sack
        FastTable<PNode> kids = new FastTable<PNode>(mesh.getChildren());

        // Check the geometry for duplicates
        int index = addMeshGeometry(mesh);
        PPolygonMesh geometry = m_LocalGeometry.get(index);
        
        // Initialize the meshAsset instance
        PPolygonMeshInstance meshInst  = buildMeshInstance(geometry, mesh.getTransform().getLocalMatrix(false), null);
        meshInst.setName(mesh.getName());
        // Must use the transform of the passed in geometry, not the (potentially) found duplicate
        meshInst.getTransform().getLocalMatrix(true).set(mesh.getTransform().getLocalMatrix(false));

        // Take the kids out of the sack
        for (int i = 0; i < kids.size(); i++)
        {
            PNode kid = kids.get(i);

            meshInst.addChild(processNode(kid));
        }

        return meshInst;
    }     
    
    /**
     * This method takes an instance and converts it to belong this
     * PScene.
     * @param originalMeshInstance
     * @return PPolygonMeshInstance
     */
    private PPolygonMeshInstance processMeshInstance(PPolygonMeshInstance originalMeshInstance)
    {
        // Put the kids in a sack
        FastTable<PNode> kids = new FastTable<PNode>(originalMeshInstance.getChildren());

        PPolygonMesh geometry = null;
        
        // Try to find the geometry in the shared assets list (e.g. might get there from the collada loader)
        for (SharedAsset shared : m_SharedAssets)
        {
            if (shared.getAssetData() == originalMeshInstance.getGeometry())
            {
                geometry = originalMeshInstance.getGeometry();
                break;
            }
        }
        
        if (geometry == null)
        {
            
            SharedAsset asset = new SharedAsset(getRepository(), new AssetDescriptor(SharedAsset.SharedAssetType.Unknown, originalMeshInstance.getGeometry().getName()));
            asset.setAssetData(originalMeshInstance.getGeometry());
            m_SharedAssets.add(asset);
            geometry = originalMeshInstance.getGeometry();
        }
        
        // Get the parent transform
        PMatrix parentWorldMat = null;
        if (originalMeshInstance.getParent() == null)
            parentWorldMat = new PMatrix();
        else if (originalMeshInstance.getParent().getTransform() == null)
            parentWorldMat = new PMatrix();
        else
            parentWorldMat = originalMeshInstance.getParent().getTransform().getWorldMatrix(false);
        
        
        // Initialize the new instance
        PPolygonMeshInstance newMeshInst  = null;
        
        if (originalMeshInstance instanceof PPolygonSkinnedMeshInstance)
            newMeshInst = buildSkinnedMeshInstance((PPolygonSkinnedMeshInstance)originalMeshInstance, parentWorldMat);
        else
            newMeshInst = buildMeshInstance(geometry, parentWorldMat, originalMeshInstance.getMaterialRef());

        newMeshInst.setName(originalMeshInstance.getName());
        // Must use the transform of the passed in geometry, not the (potentially) found duplicate
        newMeshInst.getTransform().getLocalMatrix(true).set(originalMeshInstance.getTransform().getLocalMatrix(false));
        
        newMeshInst.setPScene(this);

        // Take the kids out of the sack
        for (int i = 0; i < kids.size(); i++)
        {
            PNode kid = kids.get(i);
            
            if (kid.getName().equals("m_TransformHierarchy"))
                continue;

            newMeshInst.addChild(processNode(kid));
        }

        return newMeshInst;
    }     
    
    /**
     * Process a conceret skinned meshAsset (contains geometry, not an instance)
     * and produce a skinned meshAsset instance 
     * @param mesh
     * @return meshInst (PNode)
     */
    @InternalAPI
    public synchronized PPolygonSkinnedMeshInstance processSkinnedMesh(PPolygonSkinnedMesh mesh)
    {
        // Put the kids in a sack
        FastTable<PNode> kids = new FastTable<PNode>(mesh.getChildren());

        // Check the geometry for duplicates
        int index = addMeshGeometry(mesh);
        PPolygonSkinnedMesh geometry = (PPolygonSkinnedMesh)m_LocalGeometry.get(index);
        
        // Initialize the meshAsset instance
        PPolygonSkinnedMeshInstance meshInst  = buildSkinnedMeshInstance(geometry, mesh.getTransform().getLocalMatrix(false));
        meshInst.setName(mesh.getName());
        
        // Take the kids out of the sack
        if (kids != null)
        {
        for (PNode kid : kids)
            meshInst.addChild(processNode(kid));
        }
        return meshInst;
    }
    
    /**
     * Process a conceret joint (may have children that contain geometry, not instances)
     * and produce a joint instance 
     * @param joint
     * @return jointInst (PNode)
     */
    private PNode processJoint(PJoint joint)
    {
        PJoint jointInst = new PJoint(joint.getName(), joint.getTransform());

        FastTable<PNode> kids = new FastTable<PNode>(joint.getChildren());
        for (int i = 0; i < kids.size(); i++)
        {
            PNode kid = kids.get(i);

            jointInst.addChild(processNode(kid));
        }
        
        return jointInst;
    }
   
    /**
     * Removes a modelAsset instance from this scene by reference
     * @param instance
     */
    public void removeModelInstance(PNode instance)
    {
        m_Instances.removeChild(instance);
    }
    
    /**
     * Removes a modelAsset instance from this scene by name
     * @param name
     * @return result (PPolygonModelInstance)
     */
    public PPolygonModelInstance removeModelInstance(String name)
    {
        PPolygonModelInstance result = (PPolygonModelInstance)m_Instances.removeChild(name);
        if (result != null)
            result.removeCleanUp();
        
        return result;
    }
    
    /**
     * Installs a rechieved asset (received from the repository) to
     * its apropriate place that is defined by the placeholder
     * @param placeHolder
     * @param asset
     */
    private void installAsset(SharedAssetPlaceHolder placeHolder, SharedAsset asset) 
    {
        m_SharedAssets.add(asset);
        // replace the placeholder and keep the kids from both
        PNode parent = placeHolder.getParent();
        if (parent == null) 
            logger.info("Deprecated \"target\" usage.");
        else if (asset.getAssetData() instanceof PScene)
        {
            PScene otherScene = ((PScene)asset.getAssetData());
            // copy over the shared assets from the other PScene
            for (SharedAsset sa : otherScene.getAssetList())
                m_SharedAssets.add(sa);
            
            // The first model in the collada scene is the model we loaded from the collada file
            PNode newInstance = processNode(otherScene.getInstances());
            newInstance.setName(asset.getDescriptor().getType() + " " + asset.getDescriptor().getLocation().getFile());
            
            parent.removeChild(placeHolder);
            while(newInstance.getChildrenCount() > 0)
                parent.addChild(newInstance.getChild(0));
             
             if (placeHolder.getInitializer() != null)
                 placeHolder.getInitializer().initialize(parent);
        }
        else // A mesh or a skinned mesh
        {
            if (asset.getAssetData() instanceof SkeletonNode)
            {
                SkeletonNode skeleton = (SkeletonNode)processSkeletonNode((SkeletonNode)asset.getAssetData());
//                PPolygonSkinnedMeshInstance newInstance = buildSkinnedMeshInstance((PPolygonSkinnedMesh)asset.getData(), parent.getTransform().getWorldMatrix(false));
                skeleton.setName(placeHolder.getName());
//                newInstance.setName(placeHolder.getName());
                parent.replaceChild(placeHolder, skeleton, true);
                
                // Initialize this asset now that it is loaded
                if (placeHolder.getInitializer() != null)
                    placeHolder.getInitializer().initialize(skeleton);
            }
            else if (asset.getAssetData() instanceof PPolygonMesh)
            {
                PPolygonMeshInstance newInstance = buildMeshInstance((PPolygonMesh)asset.getAssetData(), parent.getTransform().getWorldMatrix(false), null);
                newInstance.setName(placeHolder.getName());
                parent.replaceChild(placeHolder, newInstance, true);
                parent.buildFlattenedHierarchy();
                
                // Initialize this asset now that it is loaded
                if (placeHolder.getInitializer() != null)
                    placeHolder.getInitializer().initialize(newInstance);
            }
        }
    }
        
    /**
     * Implements RepositoryUser interface
     * @param asset
     */
    public void receiveAsset(SharedAsset asset) 
    {
        if (asset.getAssetData() == null)
            logger.severe("PScene - receiveAsset - timed out (data is null) - " + asset.getDescriptor().getLocation().getFile());

       SharedAssetPlaceHolder target = null;

       synchronized(m_SharedAssetWaitingList) // Watch out for concurrent modifications!
       {
           for (SharedAssetPlaceHolder placeHolder : m_SharedAssetWaitingList)
           {
               if (asset.getDescriptor().equals(placeHolder.getDescriptor()))
               {
                    target = placeHolder;
                    m_SharedAssetWaitingList.remove(target);
                    break;
               }
           }
       }

       if (target != null)
       {
            // install the asset into the scene graph,
            // this will swap the placeHolder with the asset while maintaining the graph structure (kids from both will remain)
            // If the asset was able to load
            if (asset.getAssetData() != null)
                installAsset(target, asset);
            else
            {
                target.setName(target.getName() + " ERROR : Asset was unable to load");
                logger.severe("Unable to load asset for " + target.getName());
            }

            // take care of all the freeloaders
            for (SharedAssetPlaceHolder freeLoader : target.getFreeLoaders())
                installAsset(freeLoader, asset);
            target.clearFreeloaders();
       }
       else
           logger.warning(asset.getDescriptor().getType().toString() + " : " + getName() + "'s SharedAsset received but no one came to pick it up! " + asset.getDescriptor().getLocation().getPath());
    }
    
    /**
     * Add an instance to this scene by supplying a SharedAsset that contains
     * a descriptor
     * @param meshAsset
     * @param name
     * @return PNode
     */
    public PNode addMeshInstance(String name, SharedAsset meshAsset)
    {
        return addMeshInstance(name, meshAsset, null);
    }
    
    private PNode processSkeletonNode(SkeletonNode skeletonNode)
    {
        SkeletonNode result = new SkeletonNode(skeletonNode);
        
        for (PNode kid : skeletonNode.getChildren())
            result.addChild(processNode(kid));
        
        result.setSkeletonRoot(result.getSkeletonRoot());
        result.refresh();
        return result;
    }
    
    /**
     * Add an instance to this scene by supplying a SharedAsset that contains
     * a descriptor
     * @param meshAsset
     * @param origin
     * @param forAModel
     * @return
     */
    private PNode addMeshInstance(String name, SharedAsset meshAsset, PPolygonModelInstance forThisModelInstance)
    {
        if (!m_bUseRepository)
        {
            boolean foundSharedAsset = false;
            
            // Check if this asset already exists
            for (SharedAsset asset : m_SharedAssets)
            {
                if (meshAsset.getDescriptor().equals(asset.getDescriptor()))
                {
                    // We have this asset! Share...
                    meshAsset.setAssetData(asset.getAssetData());
                    foundSharedAsset = true;
                    break;
                }
            }
            
            // If we didn't find an asset to share
            if (!foundSharedAsset)
            {
                if (meshAsset.getDescriptor().getType() == SharedAssetType.MS3D_SkinnedMesh)
                {
//                    if (meshAsset.getDescriptor().getLocation().getPath().endsWith("ms3d"))
//                    {
//                        SkeletonNode skeleton = new SkeletonNode(meshAsset.getDescriptor().getLocation().getFile() + " skeleton");
//                        try
//                        {
//                            new MS3DSkinnedMeshImporter().load(skeleton, meshAsset.getDescriptor().getLocation());
//                        }
//                        catch (Exception e){e.printStackTrace();}
//
//                        // Set the data for the shared asset
//                        meshAsset.setAssetData(skeleton);
//                    }
//                    else if (meshAsset.getDescriptor().getLocation().getPath().endsWith("dae")) // COLLADA
//                    {
//
//                    }
                }
                else if(meshAsset.getDescriptor().getType() == SharedAssetType.MS3D_Mesh)
                {
                    if (meshAsset.getDescriptor().getLocation().getPath().endsWith("ms3d"))
                    { 
                    }
                }
                else if (meshAsset.getDescriptor().getType() == SharedAssetType.COLLADA)
                {
                    // Load the collada file to the PScene manually
                    Collada colladaLoader = new Collada();
                    if (meshAsset.getUserData() != null && meshAsset.getUserData() instanceof ColladaLoaderParams)
                    {
                        ColladaLoaderParams loaderParams = (ColladaLoaderParams)meshAsset.getUserData();
                        colladaLoader.applyConfiguration(loaderParams);
                    }
                    PScene colladaScene = new PScene("COLLADA : " + 
                            meshAsset.getDescriptor().getLocation().getFile(), m_WorldManager);
                    //colladaScene.setUseRepository(true);
                    try {
                        colladaLoader.load(colladaScene, meshAsset.getDescriptor().getLocation());
                    }
                    catch (ColladaLoadingException ex)
                    {
                        logger.severe("Unable to load COLLADA file: " +
                                meshAsset.getDescriptor().getLocation() + ":" +
                                ex.getMessage());
                        throw new RuntimeException("Failed to load provided collada file.", ex);
                    } catch (IOException ex) {

                        logger.severe("Unable to load COLLADA file: " +
                                meshAsset.getDescriptor().getLocation() + ":" +
                                ex.getMessage());
                        throw new RuntimeException("Failed to load provided collada file.", ex);
                    }
                        
                    meshAsset.setAssetData(colladaScene);
                }
                
                // Add the loaded asset to the shared assets list
                m_SharedAssets.add(meshAsset);
            }
        }
        
        BooleanPointer load = new BooleanPointer(false);
        
        // Add the processed shared asset (either a placeholder or the actual thing)
        PNode asset = processSharedAsset(meshAsset, load);
        asset.setName(name);
        
        // Add to the scene instances or to a given model instance
        if (forThisModelInstance == null)
        {
            setDirty(true, true);
            asset.buildFlattenedHierarchy();
        
            // Add the asset instance to this PScene,    
            if (meshAsset.getDescriptor().getType() == SharedAssetType.COLLADA && !(asset instanceof SharedAssetPlaceHolder))
            {
                while(asset.getChildrenCount() > 0)
                    m_Instances.addChild(asset.getChild(0));
            }
            else
                m_Instances.addChild(asset);
        }
        else // This load process was done for a given model instance
        {
            // Add the processed shared asset (either a placeholder or the actual thing)
            if (meshAsset.getDescriptor().getType() == SharedAssetType.COLLADA && !(asset instanceof SharedAssetPlaceHolder))
            {
                while(asset.getChildrenCount() > 0)
                    forThisModelInstance.addChild(asset.getChild(0));
            }
            else
                forThisModelInstance.addChild(asset);
        }

        // Do this last... so recieveAsset() won't be called before this line
        if (load.get())
            ((Repository)m_WorldManager.getUserData(Repository.class)).loadSharedAsset(meshAsset, this);
        
        return asset;   
    }
    
    /**
     * Add an instance to this scene by supplying a SharedAsset that contains
     * a descriptor
     * @param modelAsset
     * @param origin
     * @return PPolygonModelInstance
     */
    public PPolygonModelInstance addModelInstance(SharedAsset modelAsset, PMatrix origin)
    {
        return addModelInstance("nameless model instance", modelAsset, origin);
    }
            
    /***
     * Add an instance to this scene by supplying a SharedAsset that contains
     * a descriptor
     * @param name
     * @param modelAsset
     * @param origin
     * @return PPolygonModelInstance
     */
    public PPolygonModelInstance addModelInstance(String name, SharedAsset modelAsset, PMatrix origin)
    {
        // Create the modelAsset Instance
        PPolygonModelInstance modelInstance    = new PPolygonModelInstance(name, origin);
     
        // Load the asset
        if (modelAsset != null)
            addMeshInstance(name, modelAsset, modelInstance);
        
        // Add the modelAsset instance to this PScene
        setDirty(true, true);
        m_Instances.addChild(modelInstance);
        modelInstance.buildFlattenedHierarchy();
        
        return modelInstance;
    }
    
    /**
     * Process a shared asset, if this scene holds a local reference to 
     * the required asset it will use it, otherwise it will either 
     * tag along to an existing pending request or start a new one.
     * 
     * @param asset
     * @param load
     * @return
     */
    private PNode processSharedAsset(SharedAsset asset, BooleanPointer load)
    {
        PNode result = null;
        
        // Check to see if we already have a local reference to this asset in the repository
        int index = m_SharedAssets.indexOf(asset);        
        if (-1 == index && asset.getAssetData() == null)
        { 
            // Create the placeholder instance for the modelAsset
            SharedAssetPlaceHolder assetInstance = new SharedAssetPlaceHolder("SharedAssetPlaceHolder instance", asset.getDescriptor(), null, asset.getInitializer());
            result = assetInstance;

            // Can this placeholder be a freeloader?
            // Check to see if there is someone in the waiting list with the same descriptor...
            // if there is we will jump aboard as a freeloader
            boolean freeloader = false;
            for (SharedAssetPlaceHolder holder : m_SharedAssetWaitingList)
            {
                if (holder.getDescriptor().equals(asset.getDescriptor()))
                {
                    // if there is someone in the waiting list with the same descriptor we are added as freeloaders
                    holder.addFreeloader(assetInstance);
                    freeloader = true;
                    break;
                }
            }

            // if we are not freeloaders we need to join the waiting list and request a load from the repository
            if (!freeloader)
            {
                // Add the placeholder to the waiting list
                m_SharedAssetWaitingList.add(assetInstance);

                // Ask the repository to load the asset
                load.set(true);
            }
        }
        else // the current asset is already loaded in this PScene so we have access to it - no need to create a placeholder and add it to the waiting list with a new load request
        {
            // If this asset was passed in loaded (e.g. from the collada loader)
            if (index == -1)
            {
                m_SharedAssets.add(asset);
                index = m_SharedAssets.indexOf(asset);
            }
            
            // Get the shared asset that we already have referene too with data and all
            SharedAsset loadedModel = m_SharedAssets.get(index);
            
            // Skinned meshAsset case
            // WE SHOULD NOT USE DESCRIPTOR TYPES, but rather the instance type
            if (loadedModel.getAssetData() instanceof PPolygonSkinnedMesh)
            {
                PPolygonSkinnedMeshInstance newInstance = buildSkinnedMeshInstance((PPolygonSkinnedMesh)loadedModel.getAssetData(), new PMatrix());
                newInstance.recalculateInverseBindPose();   // TODO is this needed?
                result = newInstance;
            }   
            // MS3D_Mesh case (very similar to skinned)
            else if (loadedModel.getAssetData() instanceof PPolygonMesh)
            {
                PPolygonMeshInstance newInstance = buildMeshInstance((PPolygonMesh)loadedModel.getAssetData(), new PMatrix(), null);
                result = newInstance;
            }   
            // ColldaScene case
            else if (loadedModel.getAssetData() instanceof PScene)
            {
                PScene otherScene = ((PScene)loadedModel.getAssetData());
                // The first model in the collada scene is the model we loaded from the collada file
                PNode newInstance = processNode(otherScene.getInstances());
                newInstance.setName(loadedModel.getDescriptor().getType() + " " + loadedModel.getDescriptor().getLocation().getFile());
                // copy over the shared assets from the other PScene
                for (SharedAsset sa : otherScene.getAssetList())
                {
                    m_SharedAssets.add(sa);
                }
                if (newInstance.getChildrenCount() > 0)
                    result = newInstance.getChild(0);
            }
            else if (loadedModel.getAssetData() instanceof SkeletonNode)
            {
                SkeletonNode newSkeleton = (SkeletonNode) processNode((SkeletonNode)loadedModel.getAssetData());
                result = newSkeleton;
            }
            getRepository().submitWork(new InitWork(asset.getInitializer(), result));
        }
        
        return result;
    }
    
    static class InitWork implements Runnable
    {
        AssetInitializer init = null;
        PNode node = null;

        public InitWork(AssetInitializer initializer, PNode result)
        {
            init = initializer;
            node = result;
        }

        public void run()
        {
            // Call initialization code
            if (init != null)
                init.initialize(node);
        }
    }

    private PPolygonSkinnedMeshInstance buildSkinnedMeshInstance(PPolygonSkinnedMeshInstance meshInstance, PMatrix parentWorldMatrix)
    {
        // Initialize the meshAsset instance
        PPolygonSkinnedMeshInstance meshInst  = new PPolygonSkinnedMeshInstance(meshInstance, this, false);

        meshInst.getTransform().buildWorldMatrix(parentWorldMatrix);
        //meshInst.setTransformHierarchy(mesh.getBindPoseTransformHierarchy());
        //meshInst.recalculateInverseBindPose();
        //meshInst.buildAnimationJointMapping();
        
        return meshInst;
    }
            
    /**
     * This helper function creates a skinned meshAsset instance
     * @param meshAsset
     * @param parentWorldMatrix
     * @return
     */
    private PPolygonSkinnedMeshInstance buildSkinnedMeshInstance(PPolygonSkinnedMesh mesh, PMatrix parentWorldMatrix) 
    {
        // Initialize the meshAsset instance
        PPolygonSkinnedMeshInstance meshInst  = new PPolygonSkinnedMeshInstance(mesh.getName(), mesh, mesh.getTransform().getLocalMatrix(false), this, false);
        
        meshInst.getTransform().buildWorldMatrix(parentWorldMatrix);
        //meshInst.setTransformHierarchy(mesh.getBindPoseTransformHierarchy());
        //meshInst.recalculateInverseBindPose();
        //meshInst.buildAnimationJointMapping();
        
        return meshInst;
    }
    
    /**
     * This helper function creates a meshAsset instance
     * @param meshAsset
     * @param parentWorldMatrix
     * @return
     */
    private PPolygonMeshInstance buildMeshInstance(PPolygonMesh mesh, PMatrix parentWorldMatrix, PMeshMaterial materialRef)
    {
        // Initialize the meshAsset instance
        PPolygonMeshInstance meshInst  = new PPolygonMeshInstance(mesh.getName(), mesh, mesh.getTransform().getLocalMatrix(false), this, false);
        if (materialRef != null)
            meshInst.setMaterial(new PMeshMaterial(materialRef));
        meshInst.getTransform().buildWorldMatrix(parentWorldMatrix);
        
        return meshInst;
    }
    
    /**
     * Removes geometry that is shared locally and has 0 reference counts
     * Note : if you hold an index of a geometry it might become invalid.
     */
    public synchronized void cleanUpGeometry()
    {
        FastTable<PNode> deathRow = new FastTable<PNode>();
        
        for (PPolygonMesh geometry : m_LocalGeometry)
        {
            if (geometry.getReferenceCount() == 0)
                   deathRow.add(geometry);
        } 
        
        for (int i = 0; i < deathRow.size(); i++)
            m_LocalGeometry.remove(deathRow.get(i));
    }

    /**
     * Shaders are not shared across threads at the moment,
     * there is an issue with binding the shaders if the call is 
     * made outside of the render thread.
     * @param shaderPair
     * @param shaderInstaller
     */
    @Deprecated
    public void loadShaders(SharedAsset shaderPair, PPolygonMeshInstance shaderInstaller)
    {
        //if (m_bUseRepository == true)
        {
            // add a placeholder to the waiting list
            m_SharedAssetWaitingList.add(new SharedAssetPlaceHolder(shaderPair.getDescriptor().getType().toString() + 
                    shaderPair.getDescriptor().getLocation().getPath().toString() + " - " +
                    shaderPair.getDescriptor().getLocation(1).getPath().toString(),
                    shaderPair.getDescriptor(), shaderInstaller, shaderPair.getInitializer()));
            // send request to the repository
            getRepository().loadSharedAsset(shaderPair, this);
        }
    }

    /**
     * Returns true if this scene is set to use the repository
     * @return m_bUseRepositry (boolean)
     */
    public boolean isUseRepository() 
    {
        return m_bUseRepository;
    }

    /**
     * Sets whenever this scene will use the repository to share
     * assets across threads.
     * @param bUseRepository
     */
    public void setUseRepository(boolean bUseRepository) 
    {
        m_bUseRepository = bUseRepository;
    }

    /**
     * Proccess a skinned mesh joint
     * @param skinnedMeshJoint
     * @return
     */
    private PNode processSkinnedMeshJoint(SkinnedMeshJoint skinnedMeshJoint)
    {
        SkinnedMeshJoint result = new SkinnedMeshJoint(skinnedMeshJoint.getName(),
                                    new PTransform(skinnedMeshJoint.getTransform()));
 
        for (PNode kid : skinnedMeshJoint.getChildren())
            result.addChild(processNode(kid));
        return result;
    }
    
    /**
     * Accessor
     * @return The JScene, may be null if unset
     */
    public JScene getJScene()
    {
        return m_JScene;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all relevant transient objects
        m_SharedAssetWaitingList = new FastList<SharedAssetPlaceHolder>();
        m_SharedAssets = new FastList<SharedAsset>();

    }

    /**
     * This method clears out all the locally cached geometry references.
     * Use with caution.
     */
    @Deprecated
    public synchronized void clearLocalCache()
    {
        m_SharedAssets.clear();
        m_LocalGeometry.clear();
    }
}
