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
package imi.scene;

import com.jme.image.Texture;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.util.TextureManager;
import imi.scene.PJoint;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.ms3d.SkinnedMesh_MS3D_Importer;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.Repository;
import imi.loaders.repository.RepositoryUser;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.loaders.repository.SharedAssetPlaceHolder;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.utils.PRenderer;
import imi.scene.utils.tree.PSceneSubmitHelper;
import imi.scene.utils.tree.TreeTraverser;
import imi.utils.BooleanPointer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javolution.util.FastList;
import org.jdesktop.mtgame.WorldManager;

/**
 * This scene is capable of procedural manipulation and editing of geometry. 
 * The data is shared localy or globaly via the repository. 
 * A PScene needs to be submited to its JScene for rendering.
 * 
 * @author Chris Nagle
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PScene extends PNode implements RepositoryUser
{
    // Procedural Geometry
    private FastList<PPolygonMesh> m_ProceduralGeometry = new FastList<PPolygonMesh>();
    
    // Shared Assets
    private boolean m_bUseRepository                = true;
    private FastList<SharedAsset> m_SharedAssets    = new FastList<SharedAsset>();
    private ArrayList<SharedAssetPlaceHolder> m_SharedAssetWaitingList = new ArrayList<SharedAssetPlaceHolder>();

    // Instances
    private PNode m_Instances                       = null;
    
    // Utility
    private JScene                      m_JScene            = null;
    private WorldManager                m_WorldManager      = null;
    private PPolygonTriMeshAssembler    m_TriMeshAssembler  = null;

    /**
     * Constructor
     * @param wm
     */
    public PScene(WorldManager wm)
    {
        setName("PScene");
        m_WorldManager          = wm;
        m_Instances = new PNode("m_Instances", new PTransform());
        this.addChild(m_Instances);
        m_TriMeshAssembler      = new PPolygonTriMeshAssembler();
    }

    /**
     * Constructor
     * @param name
     * @param wm   
     */
    public PScene(String name, WorldManager wm)
    {
        this(wm);
        setName(name);
    }

    /**
     * Set the jscene this pscene belongs to
     * @param j
     */
    public void setJScene(JScene j)
    {
        if (j != null)
            m_JScene = j;
    }

    /**
     * The PScene will flatten its hierarchy (build the transform matrices)
     * and reconstruct the JScene it belongs to (reconstruct dirty triMeshes)
     * this will clean the dirty JScene.
     */
    public void submitTransformsAndGeometry()
    {
        // we will do this in one pass... potentially this could run in parralel?

        // potentially this could run in parralel
        submitGeometry();
        submitTransforms();
    }

//    @Override
//    public void setDirty(boolean bDirty, boolean bAffectKids)
//    {
//        super.setDirty(bDirty, bAffectKids); // used for a break point location for testing
//    }

    /**
     * The PScene will flatten its hierarchy (build the transform world matrices)
     * This method will clear all the children from m_JScene and resubmit thier
     * references according to the current PScene structure.
     */
    public void submitTransforms()
    {
        List<Spatial> kids = m_JScene.getChildren();
        
        // Nuke the m_JScene
        if (kids == null)
        {
            Node dead = new Node("kill me"); // kids = new... will not work :(
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

        m_Instances.setDirty(false, false);
        
        // Update the m_JScene
        m_JScene.updateRenderState();
    }

    /**
     * Any geometry data that is shared localy within this scene will check 
     * its dirty boolean and if true it will reconstruct its TriMesh.
     */
    public void submitGeometry()
    {
        for (PPolygonMesh geometry : m_ProceduralGeometry)
        {
            geometry.submit(m_TriMeshAssembler);
        }
    }
    
    /**
     * Draw this scene's debugging visualizations
     * @param renderer
     */
    public void internalRender(PRenderer renderer)
    {
        drawAll(renderer);
    }
    
    /**
     * Made for functionality needed in the sigraph demo
     */
    void hackOnDraw() 
    {
        //ArrayList<PNode> instances = m_Instances.getChildren();
        //for (PNode modelInst : instances)
        {
//            if (modelInst instanceof PPolygonModelInstance)
//                ((PPolygonModelInstance)modelInst).GameObjUpdate();
        }
    }
    
    /**
     * This is a temp function, in the future we will select an instance and 
     * create a new geometry for it with flipped normals.
     * (if that has been done before we need to find it and reuse that geometry)
     * Fliping normals will make the scene dirty
     */
    public void flipNormals()
    {
        // flip the normals for all the geometry in this scene
        for (PPolygonMesh geometry : m_ProceduralGeometry)
            geometry.flipNormals();
        
        setDirty(true, false);
    }
    
    /**
     * This is a temp function, in the future we will select an instance and 
     * create a new geometry for it with smooth normals.
     * (if that has been done before we need to find it and reuse that geometry)
     * Toggling smooth normals will make the scene dirty
     */
    public void toggleSmoothNormals()
    {
        for (PPolygonMesh geometry : m_ProceduralGeometry)
            geometry.setSmoothNormals(!geometry.getSmoothNormals());
        
        setDirty(true, false);
    }

    /**
     * Add geometry to this scene to be shared locally
     * @param   meshAsset
     * @return  index of the meshAsset geometry
     */
    private int addMeshGeometry(PPolygonMesh mesh)
    {
        // Duplicate checking
        int index = m_ProceduralGeometry.indexOf(mesh);
        
        if (index == -1)
        {
            mesh.setDirty(true, true);
            m_ProceduralGeometry.add(mesh);
            index = m_ProceduralGeometry.size()-1;
        }
        
        return index;
    }

    /**
     * Add an instance node
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
        ArrayList<PNode> children = node.getChildren();
        while (children.isEmpty() == false) 
            model.addChild(children.get(0)); 
        
        m_Instances.addChild(model);
        setDirty(true, true);
        model.buildFlattenedHierarchy();
        
        return model;
    }
    
    /**
     * Add an instance to this scene by supplying a PNode
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

    /** Returns the list of shared assets in the repository
     * @return m_SharedAssets (FastList<SharedAssets>)
     */
    public FastList<SharedAsset> getAssetList() {
        return m_SharedAssets;
    }
    
    /**
     * Returns the list of placeholder assets
     * @return m_SharedAssetWaitingList (ArrayList<SharedAssetPlaceHolder>)
     */
    public ArrayList<SharedAssetPlaceHolder> getAssetWaitingList() {
        return m_SharedAssetWaitingList;
    }

    private boolean isColladaType(SharedAssetType type)
    {
        if (type == SharedAssetType.COLLADA_Animation ||
            type == SharedAssetType.COLLADA_Mesh ||
            type == SharedAssetType.COLLADA_Model ||
            type == SharedAssetType.COLLADA_SkinnedMesh)
            return true;
        else
            return false;
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
            // skip the transform heirarchy nodes, they are process by the 
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
    public PPolygonMeshInstance processMesh(PPolygonMesh mesh)
    {
        // Put the kids in a sack
        ArrayList<PNode> kids = new ArrayList<PNode>(mesh.getChildren());

        // Check the geometry for duplicates
        int index = addMeshGeometry(mesh);
        PPolygonMesh geometry = m_ProceduralGeometry.get(index);
        
        // Initialize the meshAsset instance
        PPolygonMeshInstance meshInst  = buildMeshInstance(geometry, mesh.getTransform().getLocalMatrix(false));
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
    public PPolygonMeshInstance processMeshInstance(PPolygonMeshInstance originalMeshInstance)
    {
        // Put the kids in a sack
        ArrayList<PNode> kids = new ArrayList<PNode>(originalMeshInstance.getChildren());

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
        
        //geometry = originalMeshInstance.getGeometry();
        
        if (geometry == null)
        {
//            SharedAssetType type = null;
//            
//            if (originalMeshInstance.getGeometry() instanceof PPolygonMesh)
//                type = SharedAsset.SharedAssetType.Mesh;
//            else if (originalMeshInstance.getGeometry() instanceof PPolygonSkinnedMesh)
//                type = SharedAsset.SharedAssetType.SkinnedMesh;
            
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
            newMeshInst = buildMeshInstance(geometry, parentWorldMat);

        newMeshInst.setName(originalMeshInstance.getName());
        // Must use the transform of the passed in geometry, not the (potentially) found duplicate
        newMeshInst.getTransform().getLocalMatrix(true).set(originalMeshInstance.getTransform().getLocalMatrix(false));

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
    public PNode processSkinnedMesh(PPolygonSkinnedMesh mesh) 
    {
        // Put the kids in a sack
        ArrayList<PNode> kids = new ArrayList<PNode>(mesh.getChildren());

        // Check the geometry for duplicates
        int index = addMeshGeometry(mesh);
        PPolygonSkinnedMesh geometry = (PPolygonSkinnedMesh)m_ProceduralGeometry.get(index);
        
        // Initialize the meshAsset instance
        PPolygonSkinnedMeshInstance meshInst  = buildSkinnedMeshInstance(geometry, mesh.getTransform().getLocalMatrix(false));
        meshInst.setName(mesh.getName());
        
        // Take the kids out of the sack
        for (int i = 0; i < kids.size(); i++)
        {
            PNode kid = kids.get(i);
            if (kid.getName().equals("m_TransformHierarchy"))
                continue;
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
    public PNode processJoint(PJoint joint)
    {
        PJoint jointInst = new PJoint(joint.getName(), joint.getTransform());

        ArrayList<PNode> kids = new ArrayList<PNode>(joint.getChildren());
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
//        if (instance != null)
//            instance.removeCleanUp();
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
        if (parent == null) // The placeholder should have a target
        {
            // Switch by type
            switch(asset.getDescriptor().getType())
            {
                case Texture:
                {
                    // Use texture installer
                    PPolygonMeshInstance target = placeHolder.getTarget();
                    target.installTexture((Texture)asset.getAssetData(), asset.getDescriptor().getLocation());
                }
                break;
            }
        }
        else if (asset.getAssetData() instanceof PScene)
        {
            processSharedAsset(asset, new BooleanPointer(true));
//            PScene otherScene = ((PScene)asset.getAssetData());
//            // The first model in the collada scene is the model we loaded from the collada file
//            PNode newInstance = processNode(otherScene.getInstances());
//            newInstance.setName(asset.getDescriptor().getType() + " " + asset.getDescriptor().getLocation().getFile());
//            // copy over the shared assets from the other PScene
//            for (SharedAsset sa : otherScene.getAssetList())
//                m_SharedAssets.add(sa);
//            // Initialize this asset now that it is loaded
//            if (asset.getInitializer() != null)
//                asset.getInitializer().initialize(newInstance); 
//            m_Instances.addChild(newInstance);
//            PScene otherScene = ((PScene)asset.getAssetData());
//            PNode root = processNode(otherScene.getInstances());
//            //root.setName(asset.getDescriptor().getType() + " " + asset.getDescriptor().getFile().getName());
//            root.setName(placeHolder.getName());
//            
//            for (SharedAsset sa : otherScene.getAssetList())
//            {
//                m_SharedAssets.add(sa);
//            }
//            
//            parent.removeChild(placeHolder);
//            while(root.getChildrenCount() > 0)
//                parent.addChild(root.getChild(0));
//                        
//            // Initialize this asset now that it is loaded
//            if (asset.getInitializer() != null)
//                asset.getInitializer().initialize(parent);  
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
                if (asset.getInitializer() != null)
                    asset.getInitializer().initialize(skeleton);   
            }
            else if (asset.getAssetData() instanceof PPolygonMesh)
            {
                PPolygonMeshInstance newInstance = buildMeshInstance((PPolygonMesh)asset.getAssetData(), parent.getTransform().getWorldMatrix(false));
                newInstance.setName(placeHolder.getName());
                parent.replaceChild(placeHolder, newInstance, true);
                parent.buildFlattenedHierarchy();
                
                // Initialize this asset now that it is loaded
                if (asset.getInitializer() != null)
                    asset.getInitializer().initialize(newInstance);   
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
        {
            // Worker timed out - unable to reach source?
            System.out.println("PScene - receiveAsset - timed out (data is null) - " + asset.getDescriptor().getLocation().getFile());
        }
        
       boolean success = false;
        
       synchronized(m_SharedAssetWaitingList)
       {
           for (int i = 0; i < m_SharedAssetWaitingList.size(); i++)
           {
                SharedAssetPlaceHolder placeHolder = m_SharedAssetWaitingList.get(i);

                if (asset.getDescriptor().equals(placeHolder.getDescriptor()))
                {
                    // install the asset into the scene graph,
                    // this will swap the placeHolder with the asset while maintaing the graph structure (kids from both will remain)
                    synchronized (placeHolder)
                    {
                        // If the asset was able to load
                        if (asset.getAssetData() != null)
                            installAsset(placeHolder, asset);
                        else
                        {
                            placeHolder.setName(placeHolder.getName() + " ERROR : Asset was unable to load");
                            System.err.println(placeHolder.getName());
                        }
                    }

                    // remove from the waiting list
                    m_SharedAssetWaitingList.remove(i);
                    
                    System.out.println(asset.getDescriptor().getType().toString() + " receiveAsset - asset removed from m_SharedAssetWaitingList: " + asset.getDescriptor().getLocation().getPath());
                    
                    // take care of all the freeloaders
                    for (int j = 0; j < placeHolder.getFreeloaderCount(); j++)
                    {
                        SharedAssetPlaceHolder freeloader = placeHolder.getFreeloader(j);
                        installAsset(freeloader, asset);
                    }
                    placeHolder.clearFreeloaders();

                    success = true;
                    break;
                }
            }
       }
       
       if (!success)
           System.out.println(asset.getDescriptor().getType().toString() + " : " + getName() + "'s SharedAsset received but no one came to pick it up! " + asset.getDescriptor().getLocation().getPath());            
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
        return addMeshInstance(name, meshAsset, false);
    }
    
    public PNode processSkeletonNode(SkeletonNode skeletonNode)
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
    private PNode addMeshInstance(String name, SharedAsset meshAsset, boolean forAModel)
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
                    meshAsset = asset;
                    foundSharedAsset = true;
                    break;
                }
            }
            
            // If we didn't found an asset to share
            if (!foundSharedAsset)
            {
                if (meshAsset.getDescriptor().getType() == SharedAssetType.SkinnedMesh)
                {
                    if (meshAsset.getDescriptor().getLocation().getPath().endsWith("ms3d"))
                    {
                        SkeletonNode skeleton = new SkeletonNode(meshAsset.getDescriptor().getLocation().getFile() + " skeleton");
                        try
                        {
                            new SkinnedMesh_MS3D_Importer().load(skeleton, meshAsset.getDescriptor().getLocation());
                        }
                        catch (Exception e){e.printStackTrace();}

                        // Set the data for the shared asset
                        meshAsset.setAssetData(skeleton);
                    }
                    else if (meshAsset.getDescriptor().getLocation().getPath().endsWith("dae")) // COLLADA
                    {
                        
                    }
                }
                else if(meshAsset.getDescriptor().getType() == SharedAssetType.Mesh)
                {
                    if (meshAsset.getDescriptor().getLocation().getPath().endsWith("ms3d"))
                    { 
                    }
                }
                else if (isColladaType(meshAsset.getDescriptor().getType()))
                {
                    // Load the collada file to the PScene
                    Collada colladaLoader = new Collada();
                    if (meshAsset.getUserData() != null)
                    {
                        ColladaLoaderParams loaderParams = (ColladaLoaderParams)meshAsset.getUserData();
                        colladaLoader.applyConfiguration(loaderParams);
                    }
                    PScene colladaScene = new PScene("COLLADA : " + meshAsset.getDescriptor().getLocation().getFile(), m_WorldManager);
                    colladaScene.setUseRepository(true);
                    colladaLoader.load(colladaScene, meshAsset.getDescriptor().getLocation());
                        
                    meshAsset.setAssetData(colladaScene);
                }
                else if (meshAsset.getDescriptor().getType() == SharedAssetType.MS3D) // for the GUI (unknown if skinned or not)
                {
                    SkinnedMesh_MS3D_Importer importer = new SkinnedMesh_MS3D_Importer();
                    SkeletonNode skeleton = importer.loadMS3D(meshAsset.getDescriptor().getLocation());
                    
                    // Set the data for the shared asset    
                    meshAsset.setAssetData(skeleton);   
                }
                
                // Add the loaded asset to the shared assets list
                m_SharedAssets.add(meshAsset);
            }
        }
        
        BooleanPointer load = new BooleanPointer(false);
        
        // Add the processed shared asset (either a placeholder or the actual thing)
        PNode asset = processSharedAsset(meshAsset, load);
        asset.setName(name);
        
        if (!forAModel)
        {
            setDirty(true, true);
            asset.buildFlattenedHierarchy();
        
            // Add the asset instance to this PScene,    
            if (isColladaType(meshAsset.getDescriptor().getType()) && !(asset instanceof SharedAssetPlaceHolder))
            {
                while(asset.getChildrenCount() > 0)
                    m_Instances.addChild(asset.getChild(0));
            }
            else
                m_Instances.addChild(asset);
        }

        // Do this last... so recieveAsset() won't be called before this line
        if (load.get())
            ((Repository)m_WorldManager.getUserData(Repository.class)).loadSharedAsset(meshAsset, this);
        
        return asset;   
    }
    
    // modelAsset represents a single meshAsset TODO complex modelAsset loading from the repository
    
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
        PNode node = addMeshInstance(name, modelAsset, true);
        
        // Create the modelAsset Instance
        PPolygonModelInstance modelInstance    = new PPolygonModelInstance(name, origin);
       
        // Add the processed shared asset (either a placeholder or the actual thing)
        if (isColladaType(modelAsset.getDescriptor().getType()) && !(node instanceof SharedAssetPlaceHolder))
        {
            while(node.getChildrenCount() > 0)
                modelInstance.addChild(node.getChild(0));
        }
        else
            modelInstance.addChild(node);
        
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
            SharedAssetPlaceHolder assetInstance = new SharedAssetPlaceHolder("SharedAssetPlaceHolder instance", asset.getDescriptor());
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
            // Mesh case (very similar to skinned)
            else if (loadedModel.getAssetData() instanceof PPolygonMesh)
            {
                PPolygonMeshInstance newInstance = buildMeshInstance((PPolygonMesh)loadedModel.getAssetData(), new PMatrix());                
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
                result = newInstance;
            }
            else if (loadedModel.getAssetData() instanceof SkeletonNode)
            {
                SkeletonNode newSkeleton = (SkeletonNode) processNode((SkeletonNode)loadedModel.getAssetData());
                result = newSkeleton;
            }
            // Call initialization code
            AssetInitializer init = asset.getInitializer();
            // This will cause problems because the asset has not been "installed" yet
            if (init != null)
                init.initialize(result);
        }
        
        return result;
    }

    private PPolygonSkinnedMeshInstance buildSkinnedMeshInstance(PPolygonSkinnedMeshInstance meshInstance, PMatrix parentWorldMatrix)
    {
        // Initialize the meshAsset instance
        PPolygonSkinnedMeshInstance meshInst  = new PPolygonSkinnedMeshInstance(meshInstance, this);

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
        PPolygonSkinnedMeshInstance meshInst  = new PPolygonSkinnedMeshInstance(mesh.getName(), mesh, mesh.getTransform().getLocalMatrix(false), this);
        
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
    private PPolygonMeshInstance buildMeshInstance(PPolygonMesh mesh, PMatrix parentWorldMatrix)
    {
        // Initialize the meshAsset instance
        PPolygonMeshInstance meshInst  = new PPolygonMeshInstance(mesh.getName(), mesh, mesh.getTransform().getLocalMatrix(false), this);
        meshInst.getTransform().buildWorldMatrix(parentWorldMatrix);
        
        return meshInst;
    }
    
    /**
     * Removes geometry that is shared locally and has 0 reference counts
     * Note : if you hold an index of a geometry it might become invalid.
     */
    public void cleanUpGeometry()
    {
        ArrayList<PNode> deathRow = new ArrayList<PNode>();
        
        for (PPolygonMesh geometry : m_ProceduralGeometry)
        {
            if (geometry.getReferenceCount() == 0)
                   deathRow.add(geometry);
        } 
        
        for (int i = 0; i < deathRow.size(); i++)
            m_ProceduralGeometry.remove(deathRow.get(i));
    }

    /***
     * This method is bypassing the repository.
     * It loads textures locally and share them (in m_SharedAssets) with
     * other members of this PScene, this texture will NOT be shared across threads.
     * 
     * Note : this method is called implicitly as a result of setting a material
     * with textures on a meshAsset. (when m_bUseRepository is false)
     * 
     * @param texturePath - texture path
     * @return monkeyTexture (Texture)
     */
    public Texture loadTexture(URL textureLocation)
    {
        System.out.println("Thread: " + Thread.currentThread().getName() + " -Loading a texture in PScene: " + textureLocation);
        
        // Create a suitable asset
        SharedAsset texture = new SharedAsset(getRepository(), new AssetDescriptor(SharedAssetType.Texture, textureLocation));
        
        // Check localy in the the SharedAsset list  
        int index = m_SharedAssets.indexOf(texture);        
        if (-1 == index) 
        {
            // If not found load and add it
            Texture monkeyTexture = TextureManager.loadTexture(textureLocation,
                    Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
            
            if (monkeyTexture != null)
            {
                monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
            }
            
            texture.setAssetData(monkeyTexture);
            
            m_SharedAssets.add(texture);
            
            return monkeyTexture;
        }
        else
            return (Texture)m_SharedAssets.get(index).getAssetData();
    }
    
    /**
     * This method loads the specified texture into it's corresponding texture 
     * unit in textureInstaller. The texture is an asset that is shared
     * localy and across threads via the repository.
     * 
     * Note : this method is called implicitly as a result of setting a material
     * with textures on a meshAsset. (when m_bUseRepository is true)
     * 
     * @param texture
     * @param textureInstaller
     */
    public void loadTexture(SharedAsset texture, PPolygonMeshInstance textureInstaller)
    {
        // Check localy in the the SharedAsset list
        int index = m_SharedAssets.indexOf(texture);        
        if (-1 == index)
        {
            // we do NOT already have a local reference
            SharedAssetPlaceHolder placeHolder =  new SharedAssetPlaceHolder(texture.getDescriptor().getType().toString() +
                    texture.getDescriptor().getLocation().getPath().toString(),
                    texture.getDescriptor(), textureInstaller);
            
            // Can this placeholder be a freeloader?
            // Check to see if there is someone in the waiting list with the same descriptor...
            // if there is we will jump aboard as a freeloader
            boolean freeloader = false;
            for (SharedAssetPlaceHolder holder : m_SharedAssetWaitingList)
            {
                if (holder.getDescriptor().equals(placeHolder.getDescriptor()))
                {
                    // if there is someone in the waiting list with the same descriptor we are added as freeloaders
                    holder.addFreeloader(placeHolder);
                    freeloader = true;
                    break;
                }
            }

            if (!freeloader)
            {
                // add a placeholder to the waiting list
                m_SharedAssetWaitingList.add(placeHolder);
                // send request to the repository
                getRepository().loadSharedAsset(texture, this);
            }
        }
        else
        {
            // we alraedy have a local reference
            SharedAsset sharedTexture = m_SharedAssets.get(index);
            textureInstaller.installTexture((Texture)sharedTexture.getAssetData(), sharedTexture.getDescriptor().getLocation());
        }
    }
    
    /**
     * Shaders are not shared across threads at the moment,
     * there is an issue with binding the shaders if the call is 
     * made outside of the render thread.
     * @param shaderPair
     * @param shaderInstaller
     */
    public void loadShaders(SharedAsset shaderPair, PPolygonMeshInstance shaderInstaller)
    {
        //if (m_bUseRepository == true)
        {
            // add a placeholder to the waiting list
            m_SharedAssetWaitingList.add(new SharedAssetPlaceHolder(shaderPair.getDescriptor().getType().toString() + 
                    shaderPair.getDescriptor().getLocation().getPath().toString() + " - " +
                    shaderPair.getDescriptor().getLocation(1).getPath().toString(),
                    shaderPair.getDescriptor(), shaderInstaller));
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

    private PNode processSkinnedMeshJoint(SkinnedMeshJoint skinnedMeshJoint)
    {
        SkinnedMeshJoint result = new SkinnedMeshJoint();
        
        result.setName(skinnedMeshJoint.getName());
        result.setTransform(new PTransform(skinnedMeshJoint.getTransform()));
        
        for (PNode kid : skinnedMeshJoint.getChildren())
            result.addChild(processNode(kid));
        return result;
    }
}
