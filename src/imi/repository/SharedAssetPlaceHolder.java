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
package imi.repository;

import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import javolution.util.FastList;

/**
 * The SharedAssetPlaceHolder class is used to "hold a spot" in the scene graph
 * of a PScene until the actual asset it is proxying for has completed loading.
 * @author Lou Hayt
 * @author Ronald Dahlgren
 */
public class SharedAssetPlaceHolder extends PNode
{
    private AssetDescriptor                     m_assetDescriptor   = null;
    
    private FastList<SharedAssetPlaceHolder>    m_freeloaders       = new FastList<SharedAssetPlaceHolder>();
    // A placeholder without a parent will use this target to install the received asset.
    // For instance, textures are not nodes but node affectors, so they need a target
    private PPolygonMeshInstance                m_target        = null;
    private AssetInitializer                    m_assetInitializer = null;
    
    /**
     * Construct a new instance
     * @param name Name for this node
     * @param description
     * @param target The mesh instance that will be modified by this asset
     */
    public SharedAssetPlaceHolder(String name, AssetDescriptor description, PPolygonMeshInstance target, AssetInitializer init)
    {
        super(name);
        m_assetDescriptor = description;
        m_target = target;
        m_assetInitializer = init;
    }

    /**
     * Add a free loader to this placeholder.
     * @param assetInstance The other placeholder with interest in this asset
     */
    public synchronized void addFreeloader(SharedAssetPlaceHolder assetInstance) 
    {
        m_freeloaders.add(assetInstance);
    }
    
    /**
     * Clean out the list of free loaders waiting on this asset
     */
    public synchronized void clearFreeloaders()
    {
        m_freeloaders.clear();
    }

    public Iterable<SharedAssetPlaceHolder> getFreeLoaders() {
        return m_freeloaders;
    }
    
    public int getFreeloaderCount()
    {
        return m_freeloaders.size();   
    }
    
    public SharedAssetPlaceHolder getFreeloader(int index)
    {
        return m_freeloaders.get(index);
    }
    
    public AssetDescriptor getDescriptor()
    {
        return m_assetDescriptor;
    }

    public AssetInitializer getInitializer() {
        return m_assetInitializer;
    }
    
    public PPolygonMeshInstance getTarget()
    {
        return m_target;
    }
}
