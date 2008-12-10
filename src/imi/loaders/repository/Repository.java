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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.*;

/**
 * The Repository is used as a mechanism for sharing data across threads and
 * specifically between PScene instances. Data is shared as much as possible.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class Repository extends Entity
{   
    // Needed for processor operations
    private WorldManager m_worldManager = null;
    
    ProcessorCollectionComponent m_processorCollection = new ProcessorCollectionComponent();
    
    // The maximum number of load requests that can handled at a time
    private long m_numberOfLoadRequests      = 0l;
    private long m_maxConcurrentLoadRequests = 35l;
    private static long m_maxQueryTime  = 20000000l; // Lengthy timeout for testing purposes
    private final FastList<WorkOrder> m_workOrders = new FastList<WorkOrder>();
    
    // geometry
    private ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Geometry = new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    // textures
    private ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Textures = new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    // Animations
    private ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Animations = new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    // processors (AI, animations, etc)
    
    // code
    
    /**
     * Construct a BRAND NEW REPOSITORY!
     * @param wm
     */
    public Repository(WorldManager wm)
    {
        super("Asset Repository");
        
        m_worldManager = wm;
        
        wm.addEntity(this);
        
        addComponent(ProcessorCollectionComponent.class, m_processorCollection);
    }
    
    public synchronized void loadSharedAsset(SharedAsset asset, RepositoryUser user)
    {
        RepositoryAsset repoAsset = null;
        ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection = getCollection(asset.getDescriptor().getType());
        // Do some robust error checking
        if (collection == null) // Collection not found?!
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE,
                    "Unable to get correct collection for " + asset.getDescriptor().toString());
        }
        else if (asset == null)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE,
                    "Asset requested was null!");
        }
        else if (asset.getDescriptor() == null)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE,
                    "Asset descriptor was null!");
        }
        // If we already have it in the collection we will get it now
        repoAsset = collection.get(asset.getDescriptor());
        if (repoAsset == null)
        {
            if (m_numberOfLoadRequests < m_maxConcurrentLoadRequests)
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
                return;
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

    public long getNumberOfLoadRequests() {
        return m_numberOfLoadRequests;
    }

    public void adjustNumberOfLoadRequests(long workersModifierNumber) {
        m_numberOfLoadRequests += workersModifierNumber;
    }

    // Used to throtel the repository
    public void setMaxConcurrentLoadRequests(long maxConcurrentWorkers) {
        m_maxConcurrentLoadRequests = maxConcurrentWorkers;
    }
    
    public FastList<WorkOrder> getWorkOrders()
    {
        return m_workOrders;
    }
    
    public WorkOrder popWorkOrder()
    {
        WorkOrder statementOfWork = null;
        
        synchronized (m_workOrders)
        {
            if (!m_workOrders.isEmpty())
                statementOfWork = m_workOrders.removeFirst();
        }
        
        return statementOfWork;
    }
    
   

    public void createRepositoryAsset(WorkOrder statementOfWork) 
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
