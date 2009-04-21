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

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import imi.annotations.Debug;
import imi.cache.CacheBehavior;
import imi.cache.DefaultAvatarCache;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.ShaderFactory;
import imi.utils.AvatarObjectInputStream;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javolution.util.FastList;
import javolution.util.FastTable;
import org.jdesktop.mtgame.*;

/**
 * The Repository is used as a mechanism for sharing data across threads and
 * specifically between PScene instances. Data is shared as much as possible.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class Repository extends Entity
{
    /** Some package private references for convenience and centricity **/
    static String bafCacheURL = System.getProperty("BafCacheDir", null);

    /** Path to the cache folder **/
    static File cacheFolder = new File(System.getProperty("user.home") + "/WonderlandAvatarCache/");

    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(Repository.class.getName());

    /** The manager OF THE WORLD **/
    private WorldManager m_worldManager = null;

    /** Shader producing entity **/
    private ShaderFactory m_shaderFactory = null;

    /** Caching object **/
    CacheBehavior m_cache = null;

    /** All our processors **/
    private final ProcessorCollectionComponent m_processorCollection = new ProcessorCollectionComponent();
    
    // The maximum number of load requests that can handled at a time
    private long m_numberOfLoadRequests      = 0l;
    private long m_maxConcurrentLoadRequests = 35l;
    private static long m_maxQueryTime  = 2000000000l; // Lengthy timeout for testing purposes

    /** Collection of work requests for RepositoryWorkers to process **/
    private final FastList<WorkOrder> m_workOrders = new FastList<WorkOrder>();

    /************************
     *  Asset Collections   *
     ************************/
    /** Geometry Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Geometry =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    /** Texture Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Textures =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();
    
    /** Animation Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_Animations =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();

    /** PScene Collection **/
    private final ConcurrentHashMap<AssetDescriptor, RepositoryAsset> m_PScenes =
            new ConcurrentHashMap<AssetDescriptor, RepositoryAsset>();

    /** Skeleton Collection **/
    public final FastTable<SkeletonNode> m_Skeletons = new FastTable<SkeletonNode>();

    
    private boolean m_bLoadGeometry = true;

    
    /**
     * Construct a BRAND NEW REPOSITORY!
     * @param wm
     */
    public Repository(WorldManager wm)
    {
        this(wm, true, new DefaultAvatarCache(cacheFolder));
    }

    /**
     * Construct a new repository.
     * @param wm The world manager
     * @param bLoadSkeletons True to load prototype skeletons
     * @param bUseCache True to use caching (project/cache dir)
     */
    public Repository(WorldManager wm, boolean bLoadSkeletons, CacheBehavior cache)
    {
        super("Asset Repository");
        // Catch wm ref
        m_worldManager = wm;
        // Add ourselves as an entity to be managed
        wm.addEntity(this);

        // Add our collection of processors to the entity
        addComponent(ProcessorCollectionComponent.class, m_processorCollection);

        // Load up the default skeletons
        if (bLoadSkeletons)
            loadSkeletons();

        // Boot up the cache
        m_cache = cache;
        if (m_cache != null)
            initCache();

        // create the shader factory
        m_shaderFactory = new ShaderFactory(wm);

        // Load the texture cache
        loadTextureCache();
    }

    public File getCacheFolder() {
        return cacheFolder;
    }

    private void loadTextureCache()
    {
        // Texture caching is disabled until some other things are changed.
        if (true)
            return;
        if (m_cache != null)
        {
            final File textureCacheFile = new File(cacheFolder, "textures.bin");
            // prime the texture manager
            if (textureCacheFile.exists() == false) // no? then grab it from the net
                grabTextureCacheFileFromInternet(textureCacheFile);
            try {
                TextureManager.readCache(textureCacheFile);
            } catch (IOException ex) {
                Logger.getLogger(Repository.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Create a new shader of the specified type.
     * @param shaderType
     * @return
     */
    public AbstractShaderProgram newShader(Class shaderType)
    {
        return m_shaderFactory.newShader(shaderType);
    }

    /**
     * Request to load the specified asset and deliver it to the user on completion.
     * @param asset
     * @param user
     */
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
        if (repoAsset == null) // Not there, need to make an asset for it
        {
            if (m_numberOfLoadRequests <= m_maxConcurrentLoadRequests)
            {
                // We did not exceed the maxium number of workers so we can process this request now
                // If we don't already have it in the collection we will add it now
                repoAsset = new RepositoryAsset(asset.getDescriptor(), asset.getUserData(), this);
                
                // The new repository asset will loaditself, increment the counter
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

    /**
     * Load the specified texture.
     * @param location
     * @return
     */
    public Texture loadTexture(URL location)
    {
        if (m_cache != null)
            return m_cache.loadTexture(location);
        else
            return TextureManager.loadTexture(location);
    }

    public void setLoadGeometry(boolean bLoadGeometry) {
        m_bLoadGeometry = bLoadGeometry;
    }

    public boolean isLoadingGeometry() {
        return m_bLoadGeometry;
    }

    /**
     * Remove the provided processor component from this Entity's processor collection.
     * @param pc
     */
    void removeProcessor(ProcessorComponent pc)
    {
        // remove the processor from the entity process controller
        m_processorCollection.removeProcessor(pc);
    }

    /**
     * Decrement the number of references maintained by the RepositoryAsset with
     * the provided descriptor.
     * @param descriptor
     */
    void referenceSubtract(AssetDescriptor descriptor) 
    {
        ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection = getCollection(descriptor.getType());
        
        RepositoryAsset repoAsset = collection.get(descriptor);
        
        if (repoAsset != null)
            repoAsset.decrementReferenceCount();
    }

    /**
     * Return the appropriate collection for the specified asset type.
     * @param collectionType
     * @return
     */
    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getCollection(SharedAssetType collectionType)
    {
        switch (collectionType)
        {
            // Fall-throughs are intentional
            case MS3D_Mesh:
            case MS3D_SkinnedMesh:
                return m_Geometry;
            case COLLADA:
                return m_PScenes;
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

    /**
     * Create and bind a repository asset from the provided statement of work.
     * @param statementOfWork
     */
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

    private void grabTextureCacheFileFromInternet(File textureCacheFile) {
        URL textureCacheLocation = null;
        JFrame frame = new JFrame("Downloading Texture Cache");
        JProgressBar progressBar = new JProgressBar(0, 36700160);
        progressBar.setPreferredSize(new Dimension(300, 50));
        frame.add(progressBar);
        frame.pack();
        frame.setVisible(true);
        try {
            textureCacheLocation = new URL("http://www.zeitgeistgames.com/assets/textures/textures.bin");
            FileOutputStream fos = new FileOutputStream(textureCacheFile);
            BufferedInputStream bis = new BufferedInputStream(textureCacheLocation.openStream());
            byte[] byteBuffer = new byte[65536];
            int bytesRead = 0;
            int totalRead = 0;
            while ((bytesRead = bis.read(byteBuffer)) != -1)
            {
                totalRead += bytesRead;
                fos.write(byteBuffer, 0, bytesRead);
                progressBar.setValue(totalRead);
            }
            // Now the file is created! Make sure it exists
            if (textureCacheFile.exists() == false)
                logger.severe("Downloaded the file, but still couldn't create the cache version!");
        }
        catch (MalformedURLException ex)
        {
            logger.severe("The URL to the binary texture cache was WRONG! " + ex.getMessage());
        }
        catch (FileNotFoundException ex)
        {
            logger.severe("The file to be written wasn't found or something. " + ex.getMessage());
        }
        catch (IOException ex)
        {
            logger.severe("An IOException! OH NOOOOOO. " + ex.getMessage());
        }
        finally
        {
            frame.setVisible(false);
        }
    }


    /**
     * Load the default skeletons.
     */
    private void loadSkeletons() {
        AvatarObjectInputStream in = null;
        SkeletonNode MaleSkeleton = null;
        SkeletonNode FemaleSkeleton = null;

        try
        {
            URL maleSkeleton = getClass().getResource("/imi/character/skeleton/Male.bs");
            URL femaleSkeleton = getClass().getResource("/imi/character/skeleton/Female.bs");
            // Debug Skeletons
//            URL maleSkeleton = getClass().getResource("/imi/character/skeleton/MaleDebug.bs");
//            URL femaleSkeleton = getClass().getResource("/imi/character/skeleton/FemaleDebug.bs");

            in = new AvatarObjectInputStream(maleSkeleton.openStream());
            MaleSkeleton = (SkeletonNode)in.readObject();
            in.close();

            in = new AvatarObjectInputStream(femaleSkeleton.openStream());
            FemaleSkeleton = (SkeletonNode)in.readObject();
            in.close();
        }
        catch(Exception ex)
        {
            logger.severe("Uh oh! Error loading skeleton for character: " + ex.getMessage());
            ex.printStackTrace();
            // do some back up stuff
            MaleSkeleton = null;
            FemaleSkeleton = null;
        }

        // Add these into our collection if they loaded successfuly
        if (MaleSkeleton != null && FemaleSkeleton != null) // These will either both be null or both be valid
        {
            MaleSkeleton.setName("MaleSkeleton");
            FemaleSkeleton.setName("FemaleSkeleton");
            m_Skeletons.add(MaleSkeleton);
            m_Skeletons.add(FemaleSkeleton);
        }
    }

    /**
     * Get the cache directory ready.
     */
    public void initCache()
    {
        // Determine if the directory exists. If not, create it.
        if (cacheFolder.exists() == false)
            if (cacheFolder.mkdir() == false) // error
                logger.severe("Cache is unavailable!");
        // Now boot up the cache object
        if (m_cache != null)
            m_cache.initialize(null);

    }

    /**
     * Clears the cache folder
     */
    public void clearCache()
    {
        if (m_cache != null)
            m_cache.clearCache();
    }

    CacheUser user = new CacheUser();
    public synchronized void cacheAsset(SharedAsset asset)
    {
        user.loaded = false;
        loadSharedAsset(asset, user);
        while (!user.loaded)
            Thread.yield();
    }

    /**
     * Retrieve the skeleton with the specified name.
     * @param name
     * @return
     */
    public SkeletonNode getSkeleton(String name)
    {
        for (SkeletonNode skeleton : m_Skeletons)
            if (name.equals(skeleton.getName()))
                return skeleton.deepCopy(new PScene(m_worldManager));
        return null;
    }

    /**
     * Add a <b>copy</b> of the provided skeleton to the repository's internal
     * collection.
     * @param skeleton
     */
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
    WorldManager getWorldManager() 
    {
        return m_worldManager;
    }

    class CacheUser implements RepositoryUser
    {
        public boolean loaded = false;
        @Override
        public void receiveAsset(SharedAsset asset) {
            logger.info("Cached asset " + asset.getDescriptor().getLocation().getFile());
            loaded = true;
        }

    }
}
