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
    long                m_maxQueryTime  = 0l;   // How many nano-seconds to wait before giving up
    long                m_startTime     = 0l;
   
    ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_collection = null;
    
    boolean             m_bLive         = true;
    
    public RepositoryWorker(Repository home, SharedAsset asset, RepositoryUser user,
            RepositoryAsset repoAsset, ConcurrentHashMap collection,
            long maxQueryTime)
    {
        m_startTime     = System.nanoTime();
        
        m_home          = home;
        m_repoAsset     = repoAsset;
        m_asset         = asset;
        m_user          = user;
        m_maxQueryTime  = maxQueryTime;
        m_collection    = collection; 
        
    }
    
    private void reset(WorkOrder statementOfWork)
    {
        m_startTime     = System.nanoTime();
        
        m_repoAsset     = statementOfWork.m_repoAsset;
        m_asset         = statementOfWork.m_asset;
        m_user          = statementOfWork.m_user;
        m_maxQueryTime  = statementOfWork.m_maxQueryTime;
        m_collection    = statementOfWork.m_collection;   
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
        
        synchronized (m_home.getWorkOrders())
        {
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
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) 
    {
        // the worker class will call loadData() on repoAsset until it returns true and then call receiveAsset() and shutdown()
        // if loadData() returns false continuesly for a maxQueryTime amount of time then repoAsset will be removed from collection and receiveAsset() will return null.. and then shutdown.
        if (m_bLive) // this boolean might not be needed at all TODO
        {
            if (m_repoAsset.loadData(m_asset)) // Success!
            {
                // If this asset is a geometry we will set the shared asset for it so it can save to a configuration file later
                if (m_asset.getData() instanceof PPolygonMesh)
                    ((PPolygonMesh)m_asset.getData()).setSharedAsset(m_asset);
                    
                m_user.receiveAsset(m_asset);
                ShutDown();
            }
            else
            {
                // Not loaded, has the timeout expired?
                if ((System.nanoTime() - m_startTime) > m_maxQueryTime)
                {
                    // remove this RepositoryAsset from the collection.
                    m_collection.remove(m_asset.getDescriptor());
                    assert(m_asset.getData() == null);
                    m_user.receiveAsset(m_asset); // the asset is returned with null datda
                    ShutDown();
                }
                try 
                {
                    Thread.sleep(10); // suspend execution until a notify all happens
                } 
                catch (InterruptedException ex) 
                {
                    Logger.getLogger(RepositoryWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
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


}
