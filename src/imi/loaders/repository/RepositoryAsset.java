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
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.ms3d.SkinnedMesh_MS3D_Importer;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorArmingCollection;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.NewFrameCondition;

/**
 * This class is the internal representation of a loaded piece of data and its
 * corresponding descriptor. The Repository manages things it has loaded through
 * RepositoryAsset objects.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class RepositoryAsset extends ProcessorComponent
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
    private LinkedList<Object>   m_data       = null;
    
    /** This object provides storage for storage of user data **/
    private Object               m_userData   = null;
    /** The home repository for this asset **/
    private Repository           m_home       = null;
    
    private final Boolean m_lock = Boolean.TRUE;

    /** True to enable the neew texture loading code **/
    private boolean bUseTextureImporter = false;

    /**
     * Construct a new instance
     * @param description 
     * @param userData
     * @param home
     */
    public RepositoryAsset(AssetDescriptor description, Object userData, Repository home) 
    {
        m_home          = home;
        m_descriptor    = description;
        m_userData      = userData;
    }

    /**
     * Load the described asset
     */
    private void loadSelf() 
    {
        synchronized (m_lock)
        {
            // First, has loading already occured?
            if (m_data != null && m_data.size() > 0)
                return;
            else if (m_data != null && m_data.isEmpty()) // weirdness
                m_data = null;

            // allocate data storage
            m_data = new LinkedList<Object>();
            switch (m_descriptor.getType())
            {
                case MS3D_Mesh: 
                    if (m_descriptor.getLocation().getPath().endsWith("ms3d"))
                        Logger.getLogger(this.getClass().toString()).log(Level.WARNING, "Non-skinned MS3D currently unsupported");
                    else if (m_descriptor.getLocation().getPath().endsWith("dae")) // collada
                        Logger.getLogger(this.getClass().toString()).log(Level.WARNING, "Collada asset requested as type Mesh, ignoring...");
                    break;
                case MS3D_SkinnedMesh:
                    {
                        if (m_descriptor.getLocation().getPath().endsWith("ms3d"))
                        {
                             SkeletonNode skeleton = new SkeletonNode(m_descriptor.getLocation().getFile() + " skeleton");
                             SkinnedMesh_MS3D_Importer importer = new SkinnedMesh_MS3D_Importer();
                             importer.load(skeleton , m_descriptor.getLocation());
                             m_data.add(skeleton);
                        }
                        else if (m_descriptor.getLocation().getPath().endsWith("dae")) // collada
                            Logger.getLogger(this.getClass().toString()).log(Level.WARNING, "Collada asset requested as type SkinnedMesh, ignoring...");
                    }
                    break;
                    // Intentional collada fall-throughs
                    // These three cases require special set up to function.
                    // Separate enumerations are 
                case COLLADA_SkinnedMesh:
                case COLLADA_Model:
                case COLLADA_Animation:
                    if (m_userData == null || !(m_userData instanceof ColladaLoaderParams))
                        break;
                case COLLADA_Mesh:
                    {
                        // Load the collada file to the PScene
                        Collada colladaLoader = new Collada();
                        boolean usingSkeleton = true;
                        if (m_userData != null && m_userData instanceof ColladaLoaderParams)
                        {
                            ColladaLoaderParams loaderParams = (ColladaLoaderParams)m_userData;
                            colladaLoader.applyConfiguration(loaderParams);
                            usingSkeleton = loaderParams.isUsingSkeleton();
                        }
                        PScene colladaScene = new PScene("COLLADA : " + 
                                m_descriptor.getLocation().getFile(), m_home.getWorldManager());
                        if (!usingSkeleton)
                            colladaLoader.setAddSkinnedMeshesToSkeleton(false);
                        colladaLoader.load(colladaScene, m_descriptor.getLocation());

                        m_data.add(colladaScene);
                    }
                    break;
                case Model:
                {
                    // This will load models that are a composit of meshes and skinned meshes (potentially in a hierarchy of joints)

                    // Load and parse a pscene
                }
                break;
                case Texture:
                    loadTexture();
                    break;
            }
            // first, if the size of data is still zero, there is a problem
            if (m_data != null && m_data.size() <= 0)
            {
                m_data = null;
                return;
            }
            // finished loading, remove ourselves from the update pool
            m_home.removeProcessor(this);
            setArmingCondition(new ProcessorArmingCollection(this));
        }
    }
    
    private Object getDataReference() 
    {
        // TODO: Implement reference sharing system
        m_referenceCount++;
        return m_data.get(0);
    }
    
    public boolean loadData(SharedAsset asset)
    {
        if (m_data != null && !m_data.isEmpty())
        {
            asset.setAssetData(getDataReference());
            return true;
        }
        
        return false; // didn't finish loading in loadSelf()
    }
    
    public int decrementReferenceCount()
    {
        // The repository should manage the case of references <= 0
        return --m_referenceCount;
    }

    /**
     * ProcessorComponent overload.
     * @param collection
     */
    @Override
    public void compute(ProcessorArmingCollection collection)
    {
        loadSelf();         
    }

    
    /**
     * ProcessorComponent overload.
     * @param collection
     */
    @Override
    public void commit(ProcessorArmingCollection collection) 
    {

    }

    
    /**
     * ProcessorComponent overload.
     */
    @Override
    public void initialize() 
    {
        ProcessorArmingCollection collection = new ProcessorArmingCollection(this);
        collection.addCondition(new NewFrameCondition(this));
        setArmingCondition(collection); 
    }

    /**
     * First searches for an existing binary texture form. Barring that, one is created
     * to speed up the next time it is loaded.
     * @return
     */
    private void loadTexture()
    {
        Texture result = null;
        URL loc = m_descriptor.getLocation();
        URL binaryLocation = null;
        if (bUseTextureImporter)
        {
            // First check for the existence of the binary form
            try
            {
                if (Repository.bafCacheURL == null)
                    binaryLocation = new URL(loc.toString().substring(0, loc.toString().length() - 3) + "baf");
                else
                    binaryLocation = new URL(Repository.bafCacheURL + loc.getFile().toString().substring(0, loc.getFile().toString().length() - 3) + "baf");
                result = loadBinaryTexture(binaryLocation);
            } catch (Exception ex)
            {
                logger.warning(ex.getMessage());
            }
        }

        if (result == null) // Load non-binary form
        {
            result = TextureManager.loadTexture(loc,
                                            Texture.MinificationFilter.Trilinear,
                                            Texture.MagnificationFilter.Bilinear);
            result.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
            result.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
            if (bUseTextureImporter)
                writeBinaryTexture(binaryLocation, result);
        }

        if (result != null)
            m_data.add(result);
        else // failure
            m_data = null;
    }

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


}
