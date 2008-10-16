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
import javolution.util.FastList;

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
    
    private TextureState        m_TextureState = null; // The texture state to load up, provided by the constructor
    
    public TextureInstaller(int nNumberOfTextures, TextureState textureState)
    {
        m_TextureState = textureState;
        m_Textures = new Texture[nNumberOfTextures]; // Sized perfectly
    }
    
    /**
     * This method determines whether or not all requisite textures have been loaded
     * @return False if any texture is null
     */
    public boolean isComplete()
    {
        if (m_bComplete == true) // Already know we are done
            return true;
        // Are there any null Textures left?
        for (Texture tex : m_Textures)
            if (tex == null)
                return false;
        // Now we are finished!
        m_bComplete = true;
        return true;
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
