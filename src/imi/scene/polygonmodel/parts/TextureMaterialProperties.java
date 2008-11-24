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
package imi.scene.polygonmodel.parts;

import com.jme.image.Texture;
import com.jme.image.Texture.ApplyMode;
import com.jme.image.Texture.CombinerFunctionAlpha;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.image.Texture.WrapMode;
import java.net.URL;

/**
 * This class is used to maintain information about the properties of textures
 * used by a given material. It tracks things such as the desired apply mode, the
 * URL of the image, and UV coordinate wrap modes.
 * @author Ronald E Dahlgren
 */
public class TextureMaterialProperties 
{
    /** The location of the image **/
    private URL     m_imageLocation = null;
    /** Which texture unit is this texture destined for? **/
    private int     m_textureUnit = -1;
    /** wrap modes **/
    private Texture.WrapMode    m_wrapS = null;
    private Texture.WrapMode    m_wrapT = null;
    /** Alpha combine mode **/
    private Texture.CombinerFunctionAlpha   m_alphaCombineMode = null;
    /** Mip mapping **/
    private Texture.MinificationFilter      m_minFilter = null;
    /** Mag mapping **/
    private Texture.MagnificationFilter     m_magFilter = null;
    /** Anisotropic filtering **/
    private float   m_anistotropicValue = 0.0f;
    /** Apply mode **/
    private Texture.ApplyMode   m_applyMode = null;
    
    /**
     * Default constructor, initializes all values to conservative defaults.
     * The image location is left unset, and the texture unit is set to zero.
     */
    public TextureMaterialProperties()
    {
        setDefaultValues();
    }
    
    public TextureMaterialProperties(URL imageLocation)
    {
        setDefaultValues();
        setImageLocation(imageLocation);
    }
    
    private void setDefaultValues()
    {
        m_textureUnit = 0;
        m_wrapS = WrapMode.Repeat;
        m_wrapT = WrapMode.Repeat;
        m_alphaCombineMode = CombinerFunctionAlpha.Add;
        m_minFilter = MinificationFilter.Trilinear;
        m_magFilter = MagnificationFilter.Bilinear;
        m_applyMode = Texture.ApplyMode.Modulate;
    }
    
    /**
     * Applies all the configuration to a texture object. The Texture is 
     * assumed to have been loaded from the image location specified in
     * this class.
     * @param tex
     */
    public void apply(Texture tex)
    {
        tex.setWrap(Texture.WrapAxis.S, m_wrapS);
        tex.setWrap(Texture.WrapAxis.T, m_wrapT);
        tex.setCombineFuncAlpha(m_alphaCombineMode);
        tex.setMinificationFilter(m_minFilter);
        tex.setMagnificationFilter(m_magFilter);
        tex.setAnisotropicFilterPercent(m_anistotropicValue);
        tex.setApply(m_applyMode);
    }

    ///////////////////////////////////////////////////////////
    //////////////////// Getters and Setters //////////////////
    ///////////////////////////////////////////////////////////
    public CombinerFunctionAlpha getAlphaCombineMode()
    {
        return m_alphaCombineMode;
    }

    public void setAlphaCombineMode(CombinerFunctionAlpha alphaCombineMode)
    {
        m_alphaCombineMode = alphaCombineMode;
    }

    public float getAnistotropicValue()
    {
        return m_anistotropicValue;
    }

    public void setAnistotropicValue(float anistotropicValue)
    {
        m_anistotropicValue = anistotropicValue;
    }

    public URL getImageLocation()
    {
        return m_imageLocation;
    }

    public void setImageLocation(URL imageLocation)
    {
        m_imageLocation = imageLocation;
    }

    public MagnificationFilter getMagFilter()
    {
        return m_magFilter;
    }

    public void setMagFilter(MagnificationFilter magFilter)
    {
        m_magFilter = magFilter;
    }

    public MinificationFilter getMinFilter()
    {
        return m_minFilter;
    }

    public void setMinFilter(MinificationFilter minFilter)
    {
        m_minFilter = minFilter;
    }

    public int getTextureUnit()
    {
        return m_textureUnit;
    }

    public void setTextureUnit(int textureUnit)
    {
        m_textureUnit = textureUnit;
    }

    public WrapMode getWrapS()
    {
        return m_wrapS;
    }

    public void setWrapS(WrapMode wrapS)
    {
        m_wrapS = wrapS;
    }

    public WrapMode getWrapT()
    {
        return m_wrapT;
    }

    public void setWrapT(WrapMode wrapT)
    {
        m_wrapT = wrapT;
    }
    
    public ApplyMode getApplyMode()
    {
        return m_applyMode;
    }
    
    public void setApplyMode(ApplyMode mode)
    {
        m_applyMode = mode;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final TextureMaterialProperties other = (TextureMaterialProperties) obj;
        if (this.m_imageLocation != other.m_imageLocation && (this.m_imageLocation == null || !this.m_imageLocation.equals(other.m_imageLocation)))
        {
            return false;
        }
        if (this.m_textureUnit != other.m_textureUnit)
        {
            return false;
        }
        if (this.m_wrapS != other.m_wrapS)
        {
            return false;
        }
        if (this.m_wrapT != other.m_wrapT)
        {
            return false;
        }
        if (this.m_alphaCombineMode != other.m_alphaCombineMode)
        {
            return false;
        }
        if (this.m_minFilter != other.m_minFilter)
        {
            return false;
        }
        if (this.m_magFilter != other.m_magFilter)
        {
            return false;
        }
        if (this.m_anistotropicValue != other.m_anistotropicValue)
        {
            return false;
        }
        if (this.m_applyMode != other.m_applyMode)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + (this.m_imageLocation != null ? this.m_imageLocation.hashCode() : 0);
        hash = 17 * hash + this.m_textureUnit;
        hash = 17 * hash + (this.m_wrapS != null ? this.m_wrapS.hashCode() : 0);
        hash = 17 * hash + (this.m_wrapT != null ? this.m_wrapT.hashCode() : 0);
        hash = 17 * hash + (this.m_alphaCombineMode != null ? this.m_alphaCombineMode.hashCode() : 0);
        hash = 17 * hash + (this.m_minFilter != null ? this.m_minFilter.hashCode() : 0);
        hash = 17 * hash + (this.m_magFilter != null ? this.m_magFilter.hashCode() : 0);
        hash = 17 * hash + Float.floatToIntBits(this.m_anistotropicValue);
        hash = 17 * hash + (this.m_applyMode != null ? this.m_applyMode.hashCode() : 0);
        return hash;
    }
}
