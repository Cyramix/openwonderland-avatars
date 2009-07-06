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
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import imi.utils.AvatarObjectOutputStream;
import imi.loaders.Collada;
import imi.scene.PScene;
import imi.utils.AvatarObjectInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is the internal representation of a loaded piece of data and its
 * corresponding descriptor. The Repository manages things it has loaded through
 * RepositoryAsset objects.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class RepositoryAsset
{
    /** Logger ref **/
    static final Logger logger = Logger.getLogger(RepositoryAsset.class.getName());
    /** Exporter **/
    static final BinaryExporter m_binaryExporter = new BinaryExporter();
    /** Importer **/
    static final BinaryImporter m_binaryImporter = new BinaryImporter();

    /** the maximum number of references per (deep) copy **/
    static final int m_referenceThreshHold   = 100; //  TODO ? how many
    /** The current number of references **/
    private int      m_referenceCount        = 0;
    
    /** description **/
    private AssetDescriptor     m_descriptor = null;
   
   /** 
    * Data (doesn't need to be sequential in memory when multiple threads access the members)
    * in the future additional members will be added with (deep) copies of the original 
    * if the threshhold has been reached, to relieve frequent hits on the same memory spot. 
    * see : getDataReference() 
    */
    private final LinkedList<Object>   m_data = new LinkedList<Object>();
    /** The home repository for this asset **/
    private Repository           m_home       = null;
    
    /** True to enable the neew texture loading code **/
    private boolean bUseTextureImporter = false;

    private final LinkedList<PendingUsers> pendingUserShares = new LinkedList<PendingUsers>();
    private boolean loadComplete = false;

    /**
     * Construct a new instance
     * @param description 
     * @param userData
     * @param home
     */
    public RepositoryAsset(AssetDescriptor description, Repository home) 
    {
        super();
        m_home          = home;
        m_descriptor    = description;
    }

    /**
     * Load the described asset
     */
    void loadSelf()
    {
        if (m_data.size() > 0) // already loaded.
            throw new IllegalStateException("Data has already been loaded before!");

        // allocate data storage
        switch (m_descriptor.getType())
        {
            case MS3D_Mesh:
                if (m_descriptor.getLocation().getPath().endsWith("ms3d"))
                    logger.log(Level.WARNING, "Non-skinned MS3D currently unsupported");
                else if (m_descriptor.getLocation().getPath().endsWith("dae")) // collada
                    logger.log(Level.WARNING, "Collada asset requested as type Mesh, ignoring...");
                break;
            case MS3D_SkinnedMesh:
                {
//                    if (m_descriptor.getLocation().getPath().endsWith("ms3d"))
//                    {
//                         SkeletonNode skeleton = new SkeletonNode(m_descriptor.getLocation().getFile() + " skeleton");
//                         MS3DSkinnedMeshImporter importer = new MS3DSkinnedMeshImporter();
//                         importer.load(skeleton , m_descriptor.getLocation());
//                         m_data.add(skeleton);
//                    }
//                    else if (m_descriptor.getLocation().getPath().endsWith("dae")) // collada
//                        logger.log(Level.WARNING, "Collada asset requested as type SkinnedMesh, ignoring...");
                }
                break;
                // Intentional collada fall-throughs
                // These three cases require special set up to function.
                // Separate enumerations are
            case COLLADA:
                loadCOLLADA();
                break;
            case Model:
            {
                // This will load models that are a composit of meshes and skinned meshes (potentially in a hierarchy of joints)

                // Load and parse a pscene
            }
            break;
        }
        // if the size of data is still zero or the collection is null, there is a problem
        if (m_data.size() <= 0)
            throw new RuntimeException("Data failed to load!");
        else
        {
            // finished loading, remove ourselves from the update pool
            loadComplete = true;
            // Now notify any users on the pendingShares list
            synchronized (pendingUserShares) {
                for(PendingUsers u : pendingUserShares) {
                    u.asset.setAssetData(getDataReference());
                    u.user.receiveAsset(u.asset);
                }
            }
        }
    }

    /**
     * Share the contents of this asset with the specified asset and notify
     * the user that the asset is ready
     * @param asset
     */
    void shareAsset(RepositoryUser user, SharedAsset asset) {
        synchronized(pendingUserShares) {
            if (!loadComplete) {
                pendingUserShares.add(new PendingUsers(user, asset));
            } else {
                asset.setAssetData(getDataReference());
                user.receiveAsset(asset);
            }
        }
    }

    /**
     * Check to see if the asset has been loaded.
     * @return
     */
    public boolean isComplete()
    {
        return loadComplete;
    }

    private Object getDataReference() 
    {
        // TODO: Implement reference sharing system
        m_referenceCount++;
        return m_data.get(0);
    }
    
    public int decrementReferenceCount()
    {
        // The repository should manage the case of references <= 0
        return --m_referenceCount;
    }

    private void loadCOLLADA()
    {
        // Check the cache for this file
        PScene loadedScene = null;
        if (m_home.m_cache != null && m_home.m_cache.isFileCached(m_descriptor.getLocation()))
            loadedScene = loadBinaryPScene(m_home.m_cache.getStreamToResource(m_descriptor.getLocation()));

        if (loadedScene != null) // Did we succeed?
        {
            loadedScene.setWorldManager(m_home.getWorldManager());
            loadedScene.finalizeDeserialization();
            m_data.add(loadedScene);
        }
        else // create it
        {
            Collada loader = new Collada();
            if (m_home.isLoadingGeometry())
                loader.setLoadFlags(true, true, true); // load everything
            else
                loader.setLoadFlags(true, false, true);

            loader.setMaxNumberOfWeights(4);
            loader.setAddSkinnedMeshesToSkeleton(true);

            PScene loadingScene = new PScene(m_home.getWorldManager());
            try {
                loader.load(loadingScene, m_descriptor.getLocation());
            }
            catch (Exception ex)
            {
                logger.severe("Loading " + m_descriptor.getLocation().getFile() + " threw " + ex.getClass().getSimpleName());
                logger.severe(ex.getMessage());
                ex.printStackTrace();
            }
            
            // now we have the pscene prepared, write it to the cache location
            // if the cache is being used.
            if (m_home.m_cache != null)
                serializePScene(m_home.m_cache.getStreamForWriting(m_descriptor.getLocation()), loadingScene);
            m_data.add(loadingScene);
        }
    }

    /**
     * Attempt to load a serialized PScene from the specified location.
     * @param binaryLocation Location to load
     * @return The reconstituted PScene, or null on failure.
     */
    private PScene loadBinaryPScene(InputStream stream) {
        PScene result = null;
        AvatarObjectInputStream in = null;
        try
        {
            in = new AvatarObjectInputStream(stream);
            result = (PScene)in.readObject();
            in.close();
        }
        catch(Exception ex)
        {
            if (!(ex instanceof FileNotFoundException))
            {
                logger.severe("Caught a " + ex.getClass().getSimpleName() + ": " +
                        ex.getMessage() + " : " + ex.getCause());
                ex.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Serialize the provided pscene to the specified location.
     * @param destination
     * @param sceneToWrite
     */
    private void serializePScene(OutputStream destination, PScene sceneToWrite)
    {
        AvatarObjectOutputStream out = null;
        try
        {
          out = new AvatarObjectOutputStream(destination);
          out.writeObject(sceneToWrite);
          out.close();
        }
        catch(IOException ex)
        {
          ex.printStackTrace();
        }
    }

    @Deprecated
    private Texture loadBinaryTexture(URL binaryLocation)
    {
        System.out.println("Attempting to load binary texture from " + binaryLocation.toString());
        Texture result = null;
        synchronized (m_binaryImporter)
        {
            try
            {
                InputStream is = binaryLocation.openStream();
                result = (Texture)m_binaryImporter.load(is);
            }
            catch (Exception ex)
            {
                if (!(ex instanceof FileNotFoundException)) // Not a problem if it doesnt exist, we are just checking.
                    logger.warning(ex.getMessage());
            }
        }
        return result;
    }

    @Deprecated
    private void writeBinaryTexture(URL outputFileLocation, Texture tex)
    {
        System.out.println("Attempting to load binary texture from " + outputFileLocation.toString());
        tex.setStoreTexture(true);
        File destination = new File(outputFileLocation.getFile());
        synchronized(m_binaryExporter)
        {
            try
            {
                m_binaryExporter.save(tex, destination);
            }
            catch (Exception ex)
            {
                logger.warning(ex.getMessage());
            }
        }
    }

    
    static class PendingUsers {
        RepositoryUser user;
        SharedAsset asset;

        public PendingUsers(RepositoryUser user, SharedAsset asset) {
            this.user = user;
            this.asset = asset;
        }
    }
}
