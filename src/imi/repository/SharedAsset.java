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

import java.io.Serializable;

/**
 * This class is used to represent an asset that is shared across threads via
 * the repository. It consists of a descriptor with details about its type and
 * data location, as well as a reference to the actual data.
 * @author Ronald E Dahlgren
 * @author Lou Hayt
 */
public class SharedAsset
{
    // home
    private transient Repository          m_repository = null;
    
    // description
    private AssetDescriptor     m_descriptor = null;
    
    // data
    private Object              m_assetData  = null;
    
    // user provided data
    private Object              m_userData   = null;
    
    // Any initialization code
    private transient AssetInitializer    m_initializer = null;
    
    static public enum SharedAssetType
    {
        Unknown,                        
        MS3D_SkinnedMesh,        // unknown if skinned or not, determind by the existance of a *.anm file
        COLLADA, // Load a COLLADA asset.
        MS3D_Mesh,        // unknown if ms3d or collada
        MS3D_SkinnedMesh1, // unknown if ms3d or collada, in the case of ms3d will load the skeleton even without an *.anm file
        Model,       // complex model (composed of shared asset references in a graph) defined in an xml
        Texture,
        AudioFX, AudioTrack, 
        AnimationProcessor, SkinnedAnimation, 
        AIProcessor, Code
    };

    /**
     * Construct a new instance
     * @param home The repository owning this shared asset
     * @param description
     */
    public SharedAsset(Repository home, AssetDescriptor description) 
    {
        m_repository = home;
        setDescriptor(description);
    }

    /**
     * Construct a new instance
     * @param home The repository owning this asset
     * @param description
     * @param initializer An initializer class!
     */
    public SharedAsset(Repository home, AssetDescriptor description, AssetInitializer initializer)
    {
        m_repository = home;
        setDescriptor(description);
        m_initializer = initializer;
    }
    
    public SharedAsset(Repository home, AssetDescriptor description, AssetInitializer initializer, Object userData)
    {
        this(home, description, initializer);
        setUserData(userData);
    }
    
    /**
     * Set the home repository for this asset
     * @param home
     */
    public void setHome(Repository home)
    {
        m_repository = home;
    }
    
    /**
     * This method should be called whenever a shared asset is no longer being
     * used.
     */
    public void returnHome()
    {
        m_repository.referenceSubtract(m_descriptor);
    }

    /**
     * Retrieve the data this asset was responsible for loading
     * @return Th edata
     */
    public Object getAssetData() 
    {
        return m_assetData;
    }

    /**
     * Set the data this asset is responsible for loading
     * @param data The data
     */
    public void setAssetData(Object data) 
    {
        m_assetData = data;
    }
    
    /**
     * Retrieve any user defined data associated with this shared asset
     * @return
     */
    public Object getUserData()
    {
        return m_userData;
    }
    
    /**
     * Sets any user specified data for this shared asset
     * @param data
     */
    public void setUserData(Object data)
    {
        m_userData = data;
    }

    /**
     * Retrieve the asset descriptor for this shared asset
     * @return The descriptor
     */
    public AssetDescriptor getDescriptor() 
    {
        return m_descriptor;
    }

    /**
     * Set the descriptor
     * @param descriptor
     */
    public void setDescriptor(AssetDescriptor descriptor) 
    {
        m_descriptor = descriptor;
    }

    /**
     * Retrieve a reference to the home repository
     * @return the repository
     */
    public Repository getRepository() 
    {
        return m_repository;
    }

    /**
     * Set the home repository for this asset
     * @param repository
     */
    public void setRepository(Repository repository) 
    {
        m_repository = repository;
    }

    /**
     * Set an initializer to be used with this shared asset
     * @return
     */
    public AssetInitializer getInitializer()
    {
        return m_initializer;
    }

    /**
     * Set the initializer for this shared asset
     * @param initializer
     */
    public void setInitializer(AssetInitializer initializer)
    {
        m_initializer = initializer;
    }
    
    @Override
    public boolean equals(Object obj) 
    {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SharedAsset other = (SharedAsset) obj;
        if (this.m_descriptor != other.m_descriptor && (this.m_descriptor == null || !this.m_descriptor.equals(other.m_descriptor))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() 
    {
        int hash = 7;
        hash = 31 * hash + (this.m_descriptor != null ? this.m_descriptor.hashCode() : 0);
        return hash;
    }
    
    
    
}
