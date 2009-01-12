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
package imi.loaders.repository;

import imi.annotations.Debug;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.animation.AnimationState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.*;
import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;

/**
 * The Repository is used as a mechanism for sharing data across threads and
 * specifically between PScene instances. Data is shared as much as possible.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class Repository extends Entity
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(Repository.class.getName());

    /** The manager OF THE WORLD **/
    private WorldManager m_worldManager = null;

    /** All our processors **/
    private final ProcessorCollectionComponent m_processorCollection = new ProcessorCollectionComponent();
    
    // The maximum number of load requests that can handled at a time
    private long m_numberOfLoadRequests      = 0l;
    private long m_maxConcurrentLoadRequests = 35l;
    private static long m_maxQueryTime  = 20000000l; // Lengthy timeout for testing purposes
    /** Collection of work requests for RepositoryWorkers to process **/
    private final FastList<WorkOrder> m_workOrders = new FastList<WorkOrder>();
    
    /** Geometry Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Geometry =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    /** Texture Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Textures =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    /** Animation Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Animations =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    // And potentially...
    // processors (AI, animations, etc)
    // code

    public final FastList<SkeletonNode> m_Skeletons = new FastList<SkeletonNode>();
    
    /**
     * Construct a BRAND NEW REPOSITORY!
     * @param wm
     */
    public Repository(WorldManager wm)
    {
        super("Asset Repository");
        
        m_worldManager = wm;
        
        wm.addEntity(this);

        // Add our collection of processors to the entity
        addComponent(ProcessorCollectionComponent.class, m_processorCollection);

        loadSkeletons();
    }
    
    public synchronized void loadSharedAsset(SharedAsset asset, RepositoryUser user)
    {
        RepositoryAsset repoAsset = null;
        ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection = getCollection(asset.getDescriptor().getType());

        // Do some robust error checking
        boolean failure = true;
        if (collection == null) // Collection not found?!
            logger.severe("Unable to get correct collection for " + asset.getDescriptor().toString());
        else if (asset == null)
            logger.severe("Asset requested was null!");
        else if (asset.getDescriptor() == null)
            logger.severe("Asset descriptor was null!");
        else
            failure = false;
        // To proceed, or not?
        if (failure)
        {
            logger.severe("Failed to load the requested asset...");
            return;
        }

        // If we already have it in the collection we will get it now
        repoAsset = collection.get(asset.getDescriptor());
        if (repoAsset == null)
        {
            if (m_numberOfLoadRequests <= m_maxConcurrentLoadRequests)
            {
                // We did not exceed the maxium number of workers so we can process this request now
                // If we don't already have it in the collection we will add it now
                repoAsset = new RepositoryAsset(asset.getDescriptor(), asset.getUserData(), this);
                
                // The new repository asset will loaditself, incerement the counter
                m_numberOfLoadRequests++;

                collection.put(asset.getDescriptor(), repoAsset);

                // Add the repository asset as a processor so it will load itself
                m_processorCollection.addProcessor(repoAsset); 
                repoAsset.initialize(); // now safe  to initialize
            }
            else
            {
                // We exceeded the maximum number of workers, 
                // we will delay this request and issue a pending work order.
                // This work order will be picked up by one of the currently 
                // bussy workers on its shutdown()
                m_workOrders.add(new WorkOrder(asset, user, null, collection, m_maxQueryTime));
                return; // Get out of here!
            }
        }   
        
        if (repoAsset.loadData(asset)) // success?
        {
            assert(asset.getAssetData() != null);
            user.receiveAsset(asset); // we call back the user after loading the data into the asset
        }
        else
        {   
            // create a worker that will setup the SharedAsset and notify the user when the repo asset finished loading itself
            // Add a new processor (worker) to process this request
            RepositoryWorker slave = new RepositoryWorker(this, asset, user, repoAsset, collection, m_maxQueryTime); // TODO maxQueryTime according to source location and type
            m_processorCollection.addProcessor(slave);
            slave.initialize();
        }
    }
    
    public void removeProcessor(ProcessorComponent pc)
    {
        // remove the processor from the entity process controller
        m_processorCollection.removeProcessor(pc);
    }

    // one reference per thread\PScene
    public void referenceSubtract(AssetDescriptor descriptor) 
    {
        ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection = getCollection(descriptor.getType());
        
        RepositoryAsset repoAsset = collection.get(descriptor);
        
        if (repoAsset != null)
            repoAsset.decrementReferenceCount();
    }
    
    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getCollection(SharedAssetType collectionType)
    {
        switch (collectionType)
        {
            // Fall-throughs are intentional
            case MS3D_Mesh:
            case MS3D_SkinnedMesh:
            case COLLADA_Model:
            case COLLADA_Mesh:
                return m_Geometry;
            case COLLADA_Animation:
                return m_Animations;
            case Texture:
                return m_Textures;
        }
        
        return null;
    }

    public long getMaxConcurrentLoadRequests() {
        return m_maxConcurrentLoadRequests;
    }

    long getNumberOfLoadRequests() {
        return m_numberOfLoadRequests;
    }

    void adjustNumberOfLoadRequests(long workersModifierNumber) {
        m_numberOfLoadRequests += workersModifierNumber;
    }

    // Used to throtel the repository
    void setMaxConcurrentLoadRequests(long maxConcurrentWorkers) {
        m_maxConcurrentLoadRequests = maxConcurrentWorkers;
    }
    
    FastList<WorkOrder> getWorkOrders()
    {
        return m_workOrders;
    }
    
    WorkOrder popWorkOrder()
    {
        WorkOrder statementOfWork = null;
        
        synchronized (m_workOrders)
        {
            if (!m_workOrders.isEmpty())
                statementOfWork = m_workOrders.removeFirst();
        }
        
        return statementOfWork;
    }
    
   

    void createRepositoryAsset(WorkOrder statementOfWork) 
    {
        // We did not exceed the maxium number of workers so we can process this request now
        // If we don't already have it in the collection we will add it now
        RepositoryAsset repoAsset = new RepositoryAsset(statementOfWork.m_asset.getDescriptor(), statementOfWork.m_asset.getUserData(), this);

        // The new repository asset will loaditself, inceremnt the counter
        m_numberOfLoadRequests++;

        // we are not sharing shaders yet...
        getCollection(statementOfWork.m_asset.getDescriptor().getType()).put(statementOfWork.m_asset.getDescriptor(), repoAsset);

        // Add the repository asset as a processor so it will load itself
        m_processorCollection.addProcessor(repoAsset);
        repoAsset.initialize();
        
        // Update the work order
        statementOfWork.m_repoAsset = repoAsset;
    }

    private void loadSkeletons() {

        FileInputStream fis = null;
        WonderlandObjectInputStream in = null;
        SkeletonNode MaleSkeleton = null;
        SkeletonNode FemaleSkeleton = null;
        try
        {
            fis = new FileInputStream(new File("assets/skeletons/Male.bs"));
            in = new WonderlandObjectInputStream(fis);
            MaleSkeleton = (SkeletonNode)in.readObject();
            in.close();
            fis.close();
            fis = new FileInputStream(new File("assets/skeletons/Female.bs"));
            in = new WonderlandObjectInputStream(fis);
            FemaleSkeleton = (SkeletonNode)in.readObject();
        }
        catch(Exception ex)
        {
            logger.severe("Uh oh! Error loading skeleton for character: " + ex.getMessage());
            ex.printStackTrace();
        }
        // Add these into our collection
        MaleSkeleton.setName("MaleSkeleton");
        FemaleSkeleton.setName("FemaleSkeleton");

        m_Skeletons.add(MaleSkeleton);
        m_Skeletons.add(FemaleSkeleton);
    }

    public SkeletonNode getSkeleton(String name)
    {
        for (SkeletonNode skeleton : m_Skeletons)
            if (name.equals(skeleton.getName()))
                return skeleton.deepCopy();
        return null;
    }

    public void addSkeleton(SkeletonNode skeleton)
    {
        m_Skeletons.add(skeleton.deepCopy());
    }
    
    protected class WorkOrder
    {
        private long    m_timeStamp    = System.currentTimeMillis();
        
        SharedAsset     m_asset        = null;
        RepositoryUser  m_user         = null;
        RepositoryAsset m_repoAsset    = null;
        ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_collection = null;
        long            m_maxQueryTime = Repository.m_maxQueryTime;

        public WorkOrder(SharedAsset asset, RepositoryUser user, RepositoryAsset repoAsset, ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection, long maxQueryTime) 
        {
            m_asset         = asset;
            m_user          = user;
            m_repoAsset     = repoAsset;
            m_collection    = collection;
            m_maxQueryTime  = maxQueryTime;
        }
        
        public long getTimeStamp()
        {
            return m_timeStamp;
        }
    }
    
    @Debug
    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getGeometryCollection()
    {
        return m_Geometry;
    }
    
    @Debug
    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getTexturesCollection()
    {
        return m_Textures;
    }
    
    @Debug
    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getAnimationCollection()
    {
        return m_Animations;
    }
    
    /**
     * This method is only exposed for PScene construction. It is liable to change
     * so DO NOT rely on this method!
     * @return The world manager
     */
    @Deprecated
    public WorldManager getWorldManager() 
    {
        return m_worldManager;
    }
}
