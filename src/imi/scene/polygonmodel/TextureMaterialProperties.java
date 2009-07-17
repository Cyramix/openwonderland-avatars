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
package imi.scene.polygonmodel;

import com.jme.image.Texture;
import com.jme.image.Texture.ApplyMode;
import com.jme.image.Texture.CombinerFunctionAlpha;
import com.jme.image.Texture.MagnificationFilter;
import com.jme.image.Texture.MinificationFilter;
import com.jme.image.Texture.WrapMode;
import imi.repository.Repository;
import imi.serialization.xml.bindings.xmlTextureAttributes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.InternalAPI;

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

//    private static final Logger logger = Logger.getLogger(TextureMaterialProperties.class.getName());
    /** The location of the image **/
    private URL     imageLocation = null;
    /** Which texture unit is this texture destined for? **/
    private int     textureUnit = 0;
    /** wrap modes **/
    private Texture.WrapMode    wrapS = WrapMode.Repeat;
    private Texture.WrapMode    wrapT = WrapMode.Repeat;
    /** Alpha combine mode **/
    private Texture.CombinerFunctionAlpha   alphaCombineMode = CombinerFunctionAlpha.Add;
    /** Mip mapping **/
//    private Texture.MinificationFilter      m_minFilter = null;
    // HACK - Dahlgren: added to cope with mip mapping bug (black textures)
    private Texture.MinificationFilter      minFilter = Texture.MinificationFilter.Trilinear;
    /** Mag mapping **/
    private Texture.MagnificationFilter     magFilter = MagnificationFilter.Bilinear;
    /** Anisotropic filtering **/
    private float   anistotropicValue = 0.0f;
    /** Apply mode **/
    private Texture.ApplyMode   applyMode = ApplyMode.Modulate;
    /**
     * Default constructor, initializes all values to conservative defaults.
     * The image location is left unset, and the texture unit is set to zero.
     */
    public TextureMaterialProperties()
    {
    }

    /**
     * Create a new instance with internal state to match the specified other.
     * @param other
     */
    public TextureMaterialProperties(TextureMaterialProperties other) {
        this.set(other);
    }

    /**
     * Construct a new instance pointing to the specified URL
     * @param imageLocation A non-null URL
     * @throws IllegalArgumentException If imageLocation == null
     */
    public TextureMaterialProperties(URL imageLocation)
    {
        if (imageLocation == null)
            throw new IllegalArgumentException("Null URL specified!");
        setImageLocation(imageLocation);
    }

    /**
     *
     * @param texAttr
     * @param baseURL
     */
    TextureMaterialProperties(xmlTextureAttributes texAttr, String baseURL)
    {
        applyTextureAttributesDOM(texAttr, baseURL);
    }

    /**
     * Load the texture via the specified repository
     * @param repository
     * @return The loaded texture
     */
    public Texture loadTexture(Repository repository) {
        try {
            Texture result = repository.loadTexture(imageLocation);
            apply(result);
            return result;
        } catch(Exception e) {
            Logger.getAnonymousLogger().warning("Error Loading Texture"+imageLocation);
        }
        return null;
    }

    /**
     * Applies all the configuration to a texture object. The Texture is
     * assumed to have been loaded from the image location specified in
     * this class.
     * @param tex A non-null texture
     * @throws NullPointerException If tex == null
     */
    public void apply(Texture tex)
    {
        tex.setWrap(Texture.WrapAxis.S, wrapS);
        tex.setWrap(Texture.WrapAxis.T, wrapT);
        tex.setCombineFuncAlpha(alphaCombineMode);
        tex.setMagnificationFilter(magFilter);
        tex.setAnisotropicFilterPercent(anistotropicValue);
        tex.setApply(applyMode);
    }


    @InternalAPI
    public xmlTextureAttributes generateTexturePropertiesDOM()
    {
        xmlTextureAttributes result = new xmlTextureAttributes();
        // Location
        if (imageLocation != null)
        {
            int lastIndexOfAssets = imageLocation.toString().lastIndexOf("assets");
            if (lastIndexOfAssets != -1)
                result.setRelativePath(imageLocation.toString().substring(lastIndexOfAssets));
            else
                result.setRelativePath(imageLocation.getFile());
        }
        else
            result.setRelativePath(null);
        // Texture Unit
        result.setTextureUnit(textureUnit);
        // Wrap S
        result.setWrapS(wrapS.toString());
        // Wrap T
        result.setWrapT(wrapT.toString());
        // Alpha Combiner
        result.setAlphaCombiner(alphaCombineMode.toString());
        // Minification Filter
        result.setMinificationFilter(minFilter.toString());
        // Magnification Filter
        result.setMagnificationFilter(magFilter.toString());
        // Anisotropic Value
        result.setAnisotropicValue(anistotropicValue);
        // Texture Apply Mode
        result.setTextureApplyMode(applyMode.toString());
        // all done!
        return result;
    }

    ///////////////////////////////////////////////////////////
    //////////////////// Getters and Setters //////////////////
    ///////////////////////////////////////////////////////////
    public CombinerFunctionAlpha getAlphaCombineMode()
    {
        return alphaCombineMode;
    }

    public void setAlphaCombineMode(CombinerFunctionAlpha alphaCombineMode)
    {
        this.alphaCombineMode = alphaCombineMode;
    }

    public float getAnistotropicValue()
    {
        return anistotropicValue;
    }

    public void setAnistotropicValue(float anistotropicValue)
    {
        this.anistotropicValue = anistotropicValue;
    }

    public URL getImageLocation()
    {
        return imageLocation;
    }

    public void setImageLocation(URL imageLocation)
    {
        this.imageLocation = imageLocation;
    }

    public MagnificationFilter getMagFilter()
    {
        return magFilter;
    }

    public void setMagFilter(MagnificationFilter magFilter)
    {
        this.magFilter = magFilter;
    }

    public MinificationFilter getMinFilter()
    {
        return minFilter;
    }

    public void setMinFilter(MinificationFilter minFilter)
    {
        this.minFilter = minFilter;
    }

    public int getTextureUnit()
    {
        return textureUnit;
    }

    public void setTextureUnit(int textureUnit)
    {
        this.textureUnit = textureUnit;
    }

    public WrapMode getWrapS()
    {
        return wrapS;
    }

    public void setWrapS(WrapMode wrapS)
    {
        this.wrapS = wrapS;
    }

    public WrapMode getWrapT()
    {
        return wrapT;
    }

    public void setWrapT(WrapMode wrapT)
    {
        this.wrapT = wrapT;
    }

    public ApplyMode getApplyMode()
    {
        return applyMode;
    }

    public void setApplyMode(ApplyMode mode)
    {
        applyMode = mode;
    }

    /**
     * Set the state of this instance to match that of the specified instance.
     * @param other A non-null instance to copy
     * @return this
     * @throws NullPointerException If other == null
     */
    public TextureMaterialProperties set(TextureMaterialProperties other) {
        this.imageLocation = other.imageLocation; // NPE if null
        this.textureUnit = other.textureUnit;
        this.wrapS = other.wrapS;
        this.wrapT = other.wrapT;
        this.alphaCombineMode = other.alphaCombineMode;
        this.minFilter = other.minFilter;
        this.magFilter = other.magFilter;
        this.anistotropicValue = other.anistotropicValue;
        this.applyMode = other.applyMode;
        return this;
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

        if (imageLocation.toString().equals(other.imageLocation.toString()))
        {
            return false;
        }
        if (this.textureUnit != other.textureUnit)
        {
            return false;
        }
        if (this.wrapS != other.wrapS)
        {
            return false;
        }
        if (this.wrapT != other.wrapT)
        {
            return false;
        }
        if (this.alphaCombineMode != other.alphaCombineMode)
        {
            return false;
        }
        if (this.minFilter != other.minFilter)
        {
            return false;
        }
        if (this.magFilter != other.magFilter)
        {
            return false;
        }
        if (this.anistotropicValue != other.anistotropicValue)
        {
            return false;
        }
        if (this.applyMode != other.applyMode)
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + (this.imageLocation != null ? this.imageLocation.toString().hashCode() : 0);
        hash = 17 * hash + this.textureUnit;
        hash = 17 * hash + (this.wrapS != null ? this.wrapS.hashCode() : 0);
        hash = 17 * hash + (this.wrapT != null ? this.wrapT.hashCode() : 0);
        hash = 17 * hash + (this.alphaCombineMode != null ? this.alphaCombineMode.hashCode() : 0);
        hash = 17 * hash + (this.minFilter != null ? this.minFilter.hashCode() : 0);
        hash = 17 * hash + (this.magFilter != null ? this.magFilter.hashCode() : 0);
        hash = 17 * hash + Float.floatToIntBits(this.anistotropicValue);
        hash = 17 * hash + (this.applyMode != null ? this.applyMode.hashCode() : 0);
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("ImageLocation: " + imageLocation);
        result.append("\nTexture Unit: " + textureUnit);
        result.append("\nWrapS: " + wrapS);
        result.append("\nWrapT: " + wrapT);
        result.append("\nAlphaCombineMode: " + alphaCombineMode);
        result.append("\nminFilter: " + minFilter);
        result.append("\nmagFilter: " + magFilter);
        result.append("\nAnisotropicValue: " + anistotropicValue);
        result.append("\nApplymode: " + applyMode);
        return result.toString();
    }



    // Serialization helpers
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // HACK
        // May need to remap file URLs to point locally
        if (imageLocation.getProtocol().equalsIgnoreCase("file"))
        {
            String fileProtocol = "file:///" + System.getProperty("user.dir") + "/";
            String relativePath = imageLocation.toString();
            int assetsIndex = relativePath.indexOf("assets/");
            if (assetsIndex != -1)
                relativePath = relativePath.substring(assetsIndex);


//            boolean verified = false;
            URL localURL = null;
            try {
                localURL            = new URL(fileProtocol + relativePath);
                // Don't verify here! TOO EXPENSIVE!
//                InputStream stream  = localURL.openStream();
//                stream.close();
//                verified            = true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(TextureMaterialProperties.class.getName()).log(Level.SEVERE, "File does not exist... " + ex.getMessage());
//                verified = false;
            } catch (IOException ex) {
                Logger.getLogger(TextureMaterialProperties.class.getName()).log(Level.SEVERE, "File does not exist... " + ex.getMessage());
//                verified = false;
            }

//            if (!verified) {
//                try {
//                    localURL            = new URL("http://zeitgeistgames.com/" + relativePath);
//                    InputStream stream  = localURL.openStream();
//                    stream.close();
//                    verified            = true;
//                } catch (MalformedURLException ex) {
//                    verified = false;
//                }
//            }
            
            imageLocation = localURL;
        }
    }
}
