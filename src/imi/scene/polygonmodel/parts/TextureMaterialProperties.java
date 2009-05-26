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
package imi.scene.polygonmodel.parts;

import com.jme.image.Texture;
import com.jme.image.Texture.ApplyMode;
import com.jme.image.Texture.CombinerFunctionAlpha;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.image.Texture.WrapMode;
import imi.loaders.repository.Repository;
import imi.serialization.xml.bindings.xmlTextureAttributes;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to maintain information about the properties of textures
 * used by a given material. It tracks things such as the desired apply mode, the
 * URL of the image, and UV coordinate wrap modes.
 * @author Ronald E Dahlgren
 */
public class TextureMaterialProperties implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

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
//    private Texture.MinificationFilter      m_minFilter = null;
    // HACK - Dahlgren: added to cope with mip mapping bug (black textures)
    private Texture.MinificationFilter      m_minFilter = Texture.MinificationFilter.Trilinear;
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

    public TextureMaterialProperties(xmlTextureAttributes texAttr, String baseURL)
    {
        applyTextureAttributesDOM(texAttr, baseURL);
    }

    /**
     * Load the texture via the specified repository
     * @param repository
     * @return
     */
    public Texture loadTexture(Repository repository) {
        try {
            Texture result = repository.loadTexture(m_imageLocation);
            apply(result);
            return result;
        } catch(Exception e) {
            Logger.getAnonymousLogger().warning("Error Loading Texture"+m_imageLocation);
        }
        return null;
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
        // HACK - Dahlgren: added to cope with mip mapping bug (black textures)
//        tex.setMinificationFilter(m_minFilter);
        tex.setMagnificationFilter(m_magFilter);
        tex.setAnisotropicFilterPercent(m_anistotropicValue);
        tex.setApply(m_applyMode);
    }


    public xmlTextureAttributes generateTexturePropertiesDOM()
    {
        xmlTextureAttributes result = new xmlTextureAttributes();
        // Location
        if (m_imageLocation != null)
        {
            int lastIndexOfAssets = m_imageLocation.toString().lastIndexOf("assets");
            if (lastIndexOfAssets != -1)
                result.setRelativePath(m_imageLocation.toString().substring(lastIndexOfAssets));
            else
                result.setRelativePath(m_imageLocation.getFile());
        }
        else
            result.setRelativePath(null);
        // Texture Unit
        result.setTextureUnit(m_textureUnit);
        // Wrap S
        result.setWrapS(m_wrapS.toString());
        // Wrap T
        result.setWrapT(m_wrapT.toString());
        // Alpha Combiner
        result.setAlphaCombiner(m_alphaCombineMode.toString());
        // Minification Filter
        result.setMinificationFilter(m_minFilter.toString());
        // Magnification Filter
        result.setMagnificationFilter(m_magFilter.toString());
        // Anisotropic Value
        result.setAnisotropicValue(m_anistotropicValue);
        // Texture Apply Mode
        result.setTextureApplyMode(m_applyMode.toString());
        // all done!
        return result;
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


    private void applyTextureAttributesDOM(xmlTextureAttributes texAttr, String baseURL) {
        try
        {

            String fileProtocol = baseURL;
            if (baseURL==null)
                fileProtocol = "file:///" + System.getProperty("user.dir") + "/";
            
            // url
            setImageLocation(new URL(fileProtocol + texAttr.getRelativePath()));
            // textureUnit
            setTextureUnit(texAttr.getTextureUnit());
            // wrapS
            setWrapS(WrapMode.valueOf(texAttr.getWrapS()));
            // wrapT
            setWrapT(WrapMode.valueOf(texAttr.getWrapT()));
            // alphaCombiner
            setAlphaCombineMode(CombinerFunctionAlpha.valueOf(texAttr.getAlphaCombiner()));
            // minificationFilter
            setMinFilter(MinificationFilter.valueOf(texAttr.getMinificationFilter()));
            // magnificationFilter
            setMagFilter(MagnificationFilter.valueOf(texAttr.getMagnificationFilter()));
            // anisotropicValue
            setAnistotropicValue(texAttr.getAnisotropicValue());
            // textureApplyMode
            setApplyMode(ApplyMode.valueOf(texAttr.getTextureApplyMode()));
        }
        catch (Exception ex)
        {
            Logger.getLogger(TextureMaterialProperties.class.getName()).log(Level.SEVERE,
                    "Error applying DOM! - " + ex.getMessage());
        }
    }

    // Serialization helpers
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // HACK
        // May need to remap file URLs to point locally
        if (m_imageLocation.getProtocol().equalsIgnoreCase("file"))
        {
            String fileProtocol = "file:///" + System.getProperty("user.dir") + "/";
            String relativePath = m_imageLocation.toString();
            int assetsIndex = relativePath.indexOf("assets/");
            if (assetsIndex != -1)
                relativePath = relativePath.substring(assetsIndex);

            boolean verified = false;
            URL localURL = null;
            try {
                localURL            = new URL(fileProtocol + relativePath);
                InputStream stream  = localURL.openStream();
                stream.close();
                verified            = true;
            } catch (MalformedURLException ex) {
                verified = false;
            } catch (IOException ex) {
                verified = false;
            }

            if (!verified) {
                try {
                    localURL            = new URL("http://zeitgeistgames.com/" + relativePath);
                    InputStream stream  = localURL.openStream();
                    stream.close();
                    verified            = true;
                } catch (MalformedURLException ex) {
                    verified = false;
                }
            }
            
            m_imageLocation = localURL;
        }
    }
}
