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

import com.jme.image.Texture;
import com.jme.util.TextureManager;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.ms3d.SkinnedMesh_MS3D_Importer;
import imi.scene.PScene;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
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
                        if (m_userData != null && m_userData instanceof ColladaLoaderParams)
                        {
                            ColladaLoaderParams loaderParams = (ColladaLoaderParams)m_userData;
                            colladaLoader.applyConfiguration(loaderParams);
                        }
                        PScene colladaScene = new PScene("COLLADA : " + 
                                m_descriptor.getLocation().getFile(), m_home.getWorldManager());
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
                {
                    Texture tex = null;
                    try {
                        URL loc = m_descriptor.getLocation();
                        tex = TextureManager.loadTexture(loc,
                                                        Texture.MinificationFilter.Trilinear,
                                                        Texture.MagnificationFilter.Bilinear);
                    } catch (Exception exception) {
                        if (exception.getMessage().equals("Connection refused")) {
                            System.out.println(this.toString() + " " + exception.getMessage() + "... Retrying from RepositoryAsset loadSelf");
                        }
                        m_data = null;
                        return;
                    }

                    if (tex != null)
                    {
                        tex.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                        tex.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
                    }
                    else // failure
                    {
                        System.out.println(this.toString() + " texture failed.");
                        m_data = null;
                        return;
                    }

                    m_data.add(tex);
                }
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
}
