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

import java.util.logging.Level;
import java.util.logging.Logger;

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
    private Repository          m_repository = null;
    
    // description
    private AssetDescriptor     m_descriptor = null;
    
    // data
    private Object              m_data       = null;
    
    // Any initialization code
    private AssetInitializer    m_initializer = null;
    
    static public enum SharedAssetType
    {
        Unknown,                        
        MS3D,        // unknown if skinned or not, determind by the existance of a *.anm file
        COLLADA,     // loads collada models
        Mesh,        // unknown if ms3d or collada
        SkinnedMesh, // unknown if ms3d or collada, in the case of ms3d will load the skeleton even without an *.anm file
        Model,       // complex model (composed of shared asset references in a graph) defined in an xml
        ShaderPair, // <-- DEPRECATED until shader loading issues are resolved
        Texture,
        AudioFX, AudioTrack, 
        AnimationProcessor, SkinnedAnimation, 
        AIProcessor, Code
    };

    public SharedAsset(Repository home, AssetDescriptor description) 
    {
        m_repository = home;
        setDescriptor(description);
    }

    public SharedAsset(Repository home, AssetDescriptor description, AssetInitializer initializer)
    {
        m_repository = home;
        setDescriptor(description);
        m_initializer = initializer;
    }
    
    public void setHome(Repository home)
    {
        m_repository = home;
    }
    
    public void returnHome()
    {
        m_repository.referenceSubtract(m_descriptor);
    }

    public Object getData() 
    {
        return m_data;
    }

    public void setData(Object data) 
    {
        m_data = data;
    }

    public AssetDescriptor getDescriptor() 
    {
        return m_descriptor;
    }

    public void setDescriptor(AssetDescriptor descriptor) 
    {
        if (descriptor.getType() == SharedAssetType.ShaderPair)
            Logger.getLogger(this.getClass().toString()).log(Level.WARNING, 
                    "ShaderPair types are currently unsupported.");
        m_descriptor = descriptor;
    }

    public Repository getRepository() 
    {
        return m_repository;
    }

    public void setRepository(Repository repository) 
    {
        m_repository = repository;
    }

    public AssetInitializer getInitializer()
    {
        return m_initializer;
    }

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
