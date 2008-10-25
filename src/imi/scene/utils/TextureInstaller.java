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
package imi.scene.utils;

import com.jme.image.Texture;
import com.jme.scene.state.TextureState;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;

/**
 * This class receives Textures and accumulates them until all have been loaded.
 * At this point, a TextureState (extends RenderState) will be returned with the
 * Textures already set on it.
 * 
 * @author Ronald E Dahlgren
 */
public class TextureInstaller 
{
    private boolean             m_bComplete = false; // True when all textures have been loaded
    
    private Texture []          m_Textures = null; // Our list of textures that have been received
    
    private TextureMaterialProperties[] m_textureProperties = null;
    
    private TextureState        m_TextureState = null; // The texture state to load up, provided by the constructor
    
    /**
     * Construct a new texture installer. The provided array of properties will be
     * used to configure the textures as they are loaded. The URL location of the
     * images is not required (as the retrieval of the data is handled elsewhere)
     * but the texture unit and other properties should be set correctly.
     * @param textureProperties 
     * @param textureState This is the state that will be configured on completion
     */
    public TextureInstaller(TextureMaterialProperties[] textureProperties, TextureState textureState)
    {
        m_TextureState = textureState;
        m_textureProperties = textureProperties;
        if (m_textureProperties != null)
            m_Textures = new Texture[m_textureProperties.length];
    }
    
    
    /**
     * This method determines whether or not all requisite textures have been loaded
     * @return False if any texture is null
     */
    public boolean isComplete()
    {
        if (m_bComplete == true) // Already know we are done
            return true;
        if (m_Textures == null) // Not waiting on anything
            return true;
        
        // Check for missing textures
        for (int i = 0; i < m_Textures.length; ++i)
        {
            if (m_Textures[i] == null)
                return false;
        }
        // Now we are finished!
        m_bComplete = true;
        return m_bComplete;
    }
    
    /**
     * This method installs the Texture to the specified TextureUnit spot
     * @param tex The jME Texture
     * @param nTextureUnit Which texture unit should this occupy
     * @return the TextureState if complete, null otherwise
     */
    public TextureState installTexture(Texture tex, int nTextureUnit)
    {
        
        // Install it
        m_Textures[nTextureUnit] = tex;
        // Find the corresponding properties objectand configure it
        assert(m_textureProperties[nTextureUnit].getTextureUnit() == nTextureUnit) : "Incorrect texture unit provided";
        m_textureProperties[nTextureUnit].apply(m_Textures[nTextureUnit]);
        
        if (isComplete() == true)
        {
            // Load up the texture state and return it
            loadTextureState();
            return m_TextureState;
        }
        else
            return null;
    }

    /**
     * Load up the m_TextureState member with accumulated Textures
     */
    private void loadTextureState()
    {
        for (int i = 0; i < m_Textures.length; ++i)
            m_TextureState.setTexture(m_Textures[i], i);
    }
}
