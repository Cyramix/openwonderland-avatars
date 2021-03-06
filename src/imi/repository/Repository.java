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

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import imi.repository.SharedAsset.SharedAssetType;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.PScene;
import imi.scene.SkeletonNode;
import imi.shader.AbstractShaderProgram;
import imi.shader.ShaderFactory;
import imi.utils.AvatarObjectInputStream;
import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.FastTable;
import org.jdesktop.mtgame.*;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * The Repository is used as a mechanism for sharing data across threads and
 * specifically between PScene instances. Data is shared as much as possible.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class Repository extends Entity
{
    /** Keeps track of what files have been loaded! **/
    private Set<String> loadedFiles = new FastSet<String>();

    /** Some package private references for convenience and centricity **/
    static String bafCacheURL = System.getProperty("BafCacheDir", null);

    /** Path to the cache folder **/
    private static File cacheFolder = new File(System.getProperty("user.home") + "/WonderlandAvatarCache/");

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

    /** Collection of mix-in components **/
    private final Map<Class<? extends RepositoryComponent>, RepositoryComponent> components = new FastMap();
    
    // The maximum number of load requests that can handled at a time
    private long m_numberOfLoadRequests      = 0l;
    private int m_maxConcurrentLoadRequests = 35;
    private static long m_maxQueryTime  = 2000000000l; // Lengthy timeout for testing purposes

    /********************************
     * Generic Thread Pool!
     *******************************/
    private int genericThreadPool = 8;
    private final ExecutorService genericThreadService = Executors.newFixedThreadPool(genericThreadPool);

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

    /** Indicates if the repository cache should be used. **/
//    private boolean m_bUseCache = false;
    private boolean m_bLoadGeometry = true;
    /** Indicates the repository is in 'tool' mode **/
    private boolean toolMode = false;
    /** Load textures, or not **/
    private boolean bLoadTextures = true;

    // Executor service for loading assets
    private ExecutorService loaderService = Executors.newFixedThreadPool(m_maxConcurrentLoadRequests);

    /** Instrumentation **/
    private final RepositoryInstruments instruments;
    /**
     * Construct a BRAND NEW REPOSITORY!
     * @param wm
     */
    public Repository(WorldManager wm, CacheBehavior cache) {
        this(wm, true, cache);
    }

    public Repository(WorldManager wm)
    {
        this(wm, true, new DefaultAvatarCache(cacheFolder));
    }

    /**
     * Set whether or not textures should be persued when loading geometry files
     * that reference them.
     * @param loadTextures
     */
    public void setLoadTextures(boolean loadTextures) {
        bLoadTextures = loadTextures;
    }

    boolean isLoadingTextures() {
        return bLoadTextures;
    }

    public boolean isInToolMode() {
        return toolMode;
    }
    /**
     * Construct a new repository.
     * @param wm The world manager
     * @param bLoadSkeletons True to load prototype skeletons
     * @param cache If non-null, the provided caching behavior will be used
     */
    public Repository(WorldManager wm, boolean bLoadSkeletons, CacheBehavior cache)
    {
        this(wm, bLoadSkeletons, cache, false);
    }

    /**
     * Construct a new repository.
     * @param wm The world manager
     * @param bLoadSkeletons True to load prototype skeletons
     * @param cache If non-null, the provided cache behavior will be used
     * @param instrument True to install instrumentation
     */
    public Repository(WorldManager wm, boolean bLoadSkeletons, CacheBehavior cache, boolean instrument)
    {
        super("Asset Repository");
        if (instrument)
            instruments = new RepositoryInstruments(cache != null);
        else
            instruments = null;
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
            m_cache.initialize(null);

        // create the shader factory
        m_shaderFactory = new ShaderFactory(wm);

        // Load the texture cache
        loadTextureCache();
    }

    public File getCacheFolder() {
        return cacheFolder;
    }

    /**
     * Retrieve the repository component of the specified type
     * @param classz
     * @return The component, or null if none of that type was found
     */
    public RepositoryComponent getRepositoryComponent(Class<? extends RepositoryComponent> classz)
    {
        return components.get(classz);
    }

    /**
     * This method adds the provided component to the internal collection, removing
     * any old component previously set.
     * @param classz
     * @param component
     */
    public void addRepositoryComponent(Class<? extends RepositoryComponent> classz,
                                        RepositoryComponent component)
    {
        removeRepositoryComponent(classz);
        component.initialize();
        components.put(classz, component);
    }

    /**
     * Remove the component of the specified type.
     * @param classz
     */
    public void removeRepositoryComponent(Class<? extends RepositoryComponent> classz)
    {
        RepositoryComponent old = components.remove(classz);
        if (old != null)
            old.shutdown();
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
        AbstractShaderProgram result = null;
        if (toolMode)
            result = m_shaderFactory.newShader(shaderType, ShaderFactory.CreationalBehavior.NoRenderStates);
        else 
            result = m_shaderFactory.newShader(shaderType);
        return result;
    }

    /**
     * Request to load the specified asset and deliver it to the user on completion.
     * @param asset
     * @param user
     */
    public synchronized void loadSharedAsset(SharedAsset asset, RepositoryUser user)
    {
        if (asset == null) {
            throw new IllegalArgumentException("Null asset requested!");
        }

        // Do some robust error checking
        boolean failure                 = true;
        RepositoryAsset repoAsset       = null;
        AssetDescriptor assetDescriptor = asset.getDescriptor();
        SharedAssetType collectionType  = null;
        ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection  = null;

        if (assetDescriptor == null) {
            logger.severe("Asset descriptor was null!");
        } else {
            collectionType  = asset.getDescriptor().getType();
        }

        if (collectionType == null) {
            logger.severe("SharedAssetType was null");
        } else {
            collection = getCollection(collectionType);
        }

        if (collection == null) // Collection not found?!
            logger.severe("Unable to get correct collection for " + asset.getDescriptor().toString());
        else
            failure = false;

        // To proceed, or not?
        if (failure) {
            logger.severe("Failed to load the requested asset...");
            return;
        }

        // If we already have it in the collection we will get it now
        repoAsset = collection.get(asset.getDescriptor());
        loadedFiles.add(asset.getDescriptor().getLocation().toString());
        if (repoAsset == null) // Not there, need to make an asset for it
        {
            if (instruments != null)
                instruments.requestStarted(asset);
            loaderService.execute(new WorkOrder(asset, user, collection, m_maxQueryTime));
        } else {
            repoAsset.shareAsset(user, asset);
        }
    }

    @InternalAPI
    public void setToolMode(boolean toolMode) {
        this.toolMode = toolMode;
    }
    /**
     * Load the specified texture.
     * @param location
     * @return
     */
    public Texture loadTexture(URL location)
    {
//        int index   = location.toString().lastIndexOf("assets");
//        String path = location.toString().substring(index);
        URL url = null;//HeadAssets.class.getClassLoader().getResource(path);

        if (url == null)
            url = location;

        loadedFiles.add(url.toString());

//        System.out.println(location.toString());
        if (m_cache != null)
            return m_cache.loadTexture(url);
        else
            return TextureManager.loadTexture(url);
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
    ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getCollection(SharedAssetType collectionType)
    {
        switch (collectionType)
        {
            // Fall-throughs are intentional
            case MS3D_Mesh:
            case MS3D_SkinnedMesh:
                return m_Geometry;
            case COLLADA:
                return m_PScenes;
        }
        
        return null;
    }

    public long getMaxConcurrentLoadRequests() {
        return m_maxConcurrentLoadRequests;
    }

    long getNumberOfLoadRequests() {
        return m_numberOfLoadRequests;
    }

    private void grabTextureCacheFileFromInternet(File textureCacheFile) {
        URL textureCacheLocation = null;
        JFrame frame = new JFrame("Downloading Texture Cache");
        JProgressBar progressBar = new JProgressBar(0, 36700160);
        progressBar.setPreferredSize(new Dimension(300, 50));
        frame.add(progressBar);
        frame.pack();
        frame.setVisible(true);
        FileOutputStream fos    = null;
        BufferedInputStream bis = null;

        try {
            textureCacheLocation = new URL("http://www.zeitgeistgames.com/assets/textures/textures.bin");
            fos = new FileOutputStream(textureCacheFile);
            bis = new BufferedInputStream(textureCacheLocation.openStream());
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
            try {
                if (fos != null)
                    fos.close();
                if (bis != null)
                    bis.close();
            } catch (IOException ex2) {
                logger.severe("IOstream close failure: " + ex2.getMessage());
            }
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
            URL maleSkeleton = getClass().getResource("/imi/character/data/Male.bs");
            URL femaleSkeleton = getClass().getResource("/imi/character/data/Female.bs");
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
     * Clears the cache folder
     */
    public void clearCache()
    {
        if (m_cache != null)
            m_cache.clearCache();
    }

    /**
     * Save the cache state to the specified output stream
     * @param out
     */
    public void saveCache(OutputStream out) {
        if (m_cache != null)
            m_cache.createCachePackage(out);
    }

    /**
     * Load a cache pack from the provided InputStream
     * @param in The input stream
     */
    public void loadCacheState(InputStream in) {
        if (m_cache != null)
            m_cache.loadCachePackage(in);
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

    // HACK XXX UNSAFE
    public void dumpStats() {
        if (instruments != null)
            instruments.dumpStats();
        if (m_cache != null)
            ((DefaultAvatarCache)m_cache).dumpStats();
    }

    /**
     * Submit a runnable to execute asynchronously.
     * @param work
     */
    public void submitWork(Runnable work)
    {
        genericThreadService.execute(work);
    }

    /**
     * Write the list of loaded files out to the specified destinations
     * @param outfile
     * @param true to alphabetize
     * @throws IOException
     */
    @InternalAPI
    public void writeLoadedFileList(File outfile, boolean sort) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
        List<String> sortedList = new ArrayList<String>();
        // Add our URLs to the list we will sort
        sortedList.addAll(loadedFiles);
        Collections.sort(sortedList);
        for (String url : sortedList) {
            writer.write(url);
            writer.newLine();
        }
        writer.close();
    }
    
    protected class WorkOrder implements Runnable
    {
        SharedAsset     m_asset        = null;
        RepositoryUser  m_user         = null;
        RepositoryAsset m_repoAsset    = null;

        public WorkOrder(SharedAsset asset, RepositoryUser user, ConcurrentHashMap<AssetDescriptor, RepositoryAsset> collection, long maxQueryTime)
        {
            m_asset         = asset;
            m_user          = user;
            m_maxQueryTime  = maxQueryTime;

            m_repoAsset = new RepositoryAsset(m_asset.getDescriptor(), Repository.this);
            collection.put(m_asset.getDescriptor(), m_repoAsset);
        }

        public void run() {
            // TODO watchdog timer
            m_repoAsset.loadSelf();
            while (!m_repoAsset.isComplete()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    logger.warning("Interrupted whilst napping! " + ex.getMessage());
                }
            }
                

            m_repoAsset.shareAsset(m_user, m_asset);
            
            if (m_asset.getAssetData() instanceof PPolygonMesh)
                ((PPolygonMesh)m_asset.getAssetData()).setSharedAsset(m_asset);
            assert(m_asset.getAssetData() != null);
        }
    }

    
//    @Debug
//    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getGeometryCollection()
//    {
//        return m_Geometry;
//    }
//
//    @Debug
//    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getTexturesCollection()
//    {
//        return m_Textures;
//    }
//
//    @Debug
//    public ConcurrentHashMap<AssetDescriptor, RepositoryAsset> getAnimationCollection()
//    {
//        return m_Animations;
//    }
    
    /**
     * This method is only exposed for PScene construction. It is liable to change
     * so DO NOT rely on this method!
     * @return The world manager
     */
    WorldManager getWorldManager() 
    {
        return m_worldManager;
    }

    static class CacheUser implements RepositoryUser
    {
        public boolean loaded = false;
        @Override
        public void receiveAsset(SharedAsset asset) {
            logger.info("Cached asset " + asset.getDescriptor().getLocation().getFile());
            loaded = true;
        }

    }
}
