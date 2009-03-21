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

import imi.loaders.repository.Repository.WorkOrder;
import imi.scene.polygonmodel.PPolygonMesh;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.NewFrameCondition;

/**
 * The repository worker is used to asynchronously query a repository asset for
 * its data and send this data along to the requestor once it has finished loading.
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
class RepositoryWorker extends ProcessorComponent
{
    Repository          m_home          = null;
    RepositoryAsset     m_repoAsset     = null; // Source
    SharedAsset         m_asset         = null; // Destination
    RepositoryUser      m_user          = null; // Caller
    long                m_maxQueryTime  = 0l;   // How many milliseconds to wait before giving up
    long                m_startTime     = 0l;
   
    ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_collection = null;
    
    boolean             m_bLive         = true;
    boolean             m_bAssetSent    = false;
    boolean             m_bWorking      = false;
    
    public RepositoryWorker(Repository home, SharedAsset asset, RepositoryUser user,
            RepositoryAsset repoAsset, ConcurrentHashMap collection,
            long maxQueryTime)
    {
        super();
        setEntity(home);
        m_startTime     = System.currentTimeMillis();
        
        m_home          = home;
        m_repoAsset     = repoAsset;
        m_asset         = asset;
        m_user          = user;
        m_maxQueryTime  = maxQueryTime;
        m_collection    = collection; 
        
    }
    
    private void reset(WorkOrder statementOfWork)
    {
        m_startTime     = System.currentTimeMillis();
        
        m_repoAsset     = statementOfWork.m_repoAsset;
        m_asset         = statementOfWork.m_asset;
        m_user          = statementOfWork.m_user;
        m_maxQueryTime  = statementOfWork.m_maxQueryTime;
        m_collection    = statementOfWork.m_collection;   
        
        m_bLive         = true;
        m_bAssetSent    = false;
    }
    
    private void ShutDown()
    {
        // A request has been completed
        m_home.adjustNumberOfLoadRequests(-1);
        
        // Check if this worker should die off or look for more work
        if (m_home.getNumberOfLoadRequests() >= m_home.getMaxConcurrentLoadRequests())
        {
            // Rest in peace
            m_bLive = false;
            m_home.removeProcessor(this);
            return; // Done
        }
        

        // Look for more work
        WorkOrder statementOfWork = m_home.popWorkOrder();

        // Check if we have work to do
        if (statementOfWork != null)
        {
            // Create the repository asset to load itself
            m_home.createRepositoryAsset(statementOfWork);

            // Wait for it to load
            reset(statementOfWork);
        }
        else
        {
            // Rest in peace
            m_bLive = false;
            m_home.removeProcessor(this);
        }
        
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) 
    {
        if (m_bWorking)
            return;
        m_bWorking = true;

        // the worker class will call loadData() on repoAsset until it returns true and then call receiveAsset() and shutdown()
        // if loadData() returns false continuesly for a maxQueryTime amount of time then repoAsset will be removed from collection and receiveAsset() will return null.. and then shutdown.
        if (m_bLive) // this boolean might not be needed
        {
            if (m_repoAsset.loadData(m_asset)) // Success!
            {
                // If this asset is a geometry we will set the shared asset for it so it can save to a configuration file later
                if (m_asset.getAssetData() instanceof PPolygonMesh)
                    ((PPolygonMesh)m_asset.getAssetData()).setSharedAsset(m_asset);
                assert(m_asset.getAssetData() != null);
                if (!m_bAssetSent)
                {
                    m_bAssetSent = true;
                    m_user.receiveAsset(m_asset);
                    ShutDown();
                }
            }
            else
            {
                // Not loaded, has the timeout expired?
                if ((System.currentTimeMillis() - m_startTime) > m_maxQueryTime)
                {
                    // remove this RepositoryAsset from the collection.
                    m_collection.remove(m_asset.getDescriptor());
                    assert(m_asset.getAssetData() == null);
                    if (!m_bAssetSent)
                    {
                        m_bAssetSent = true;
                        m_user.receiveAsset(m_asset); // the asset is returned with null data
                        ShutDown();
                    }
                }
                try 
                {
                    Thread.sleep(100); // wait a second
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(RepositoryWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else
            ShutDown();
        m_bWorking = false;
    }

    public void compute() {
        if (m_bWorking)
            return;
        m_bWorking = true;

        // the worker class will call loadData() on repoAsset until it returns true and then call receiveAsset() and shutdown()
        // if loadData() returns false continuesly for a maxQueryTime amount of time then repoAsset will be removed from collection and receiveAsset() will return null.. and then shutdown.
        if (m_bLive) // this boolean might not be needed
        {
            if (m_repoAsset.loadData(m_asset)) // Success!
            {
                // If this asset is a geometry we will set the shared asset for it so it can save to a configuration file later
                if (m_asset.getAssetData() instanceof PPolygonMesh)
                    ((PPolygonMesh)m_asset.getAssetData()).setSharedAsset(m_asset);
                assert(m_asset.getAssetData() != null);
                if (!m_bAssetSent)
                {
                    m_bAssetSent = true;
                    m_user.receiveAsset(m_asset);
                    ShutDown();
                }
            }
            else
            {
                // Not loaded, has the timeout expired?
                if ((System.currentTimeMillis() - m_startTime) > m_maxQueryTime)
                {
                    // remove this RepositoryAsset from the collection.
                    m_collection.remove(m_asset.getDescriptor());
                    assert(m_asset.getAssetData() == null);
                    if (!m_bAssetSent)
                    {
                        m_bAssetSent = true;
                        m_user.receiveAsset(m_asset); // the asset is returned with null data
                        ShutDown();
                    }
                }
                try
                {
                    Thread.sleep(100); // wait a second
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(RepositoryWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else
            ShutDown();
        m_bWorking = false;
    }

    @Override
    public void commit(ProcessorArmingCollection collection) {
        // Nothing to commit
    }

    @Override
    public void initialize() 
    { 
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);  
        collection.addCondition(new NewFrameCondition(this)); 
        setArmingCondition(collection); 
    }

    @Override
    public void commit() {

    }


}
