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
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.CullState.Face;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.MaterialState.MaterialFace;
import com.jme.scene.state.WireframeState;
import imi.scene.PNode;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.ShaderUtils;
import imi.serialization.xml.bindings.xmlMaterial;
import imi.serialization.xml.bindings.xmlShader;
import imi.serialization.xml.bindings.xmlShaderProperty;
import imi.serialization.xml.bindings.xmlTextureAttributes;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides state for our materials system.
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class PMeshMaterial extends PNode implements Serializable
{
    private static final ColorRGBA defaultAmbient = new ColorRGBA(0.30f, 0.30f, 0.30f, 1.0f);

    /** Alpha transparency enumeration **/
    public enum AlphaTransparencyType
    {
        NO_TRANSPARENCY,
        RGB_ZERO,
        A_ONE
    }
    /** Diffuse material color **/
    private ColorRGBA  m_Diffuse   = ColorRGBA.white;
    /** Ambient material color **/
    private ColorRGBA  m_Ambient   = defaultAmbient;
    /** Emissive material color **/
    private ColorRGBA  m_Emissive  = ColorRGBA.black;
    /** Specular material color **/
    private ColorRGBA  m_Specular  = ColorRGBA.white;
    /** Transparency Color **/
    private ColorRGBA  m_TransparencyColor = null;
    /** Shininess value **/
    private int      m_Shininess = 0;   // 0 is none, around 5 is low, around 100 is high
    /** Alpha state **/
    private AlphaTransparencyType m_alphaState = AlphaTransparencyType.NO_TRANSPARENCY;
    /** Texture properties **/
    private TextureMaterialProperties[] m_textures = new TextureMaterialProperties[8];

    /** Shader collection **/
    private AbstractShaderProgram [] m_ShaderArray = new AbstractShaderProgram[1];
    /** Define the interaction between vertex colors and material properties **/
    private ColorMaterial   m_ColorMaterial = ColorMaterial.None;
    /** Cull mode for this piece of geometry **/
    private CullState.Face  m_cullFace = CullState.Face.Back;
    /** Wireframe mode? **/
    private boolean         m_bWireframeEnabled = false;
    /** Wireframe characteristics follow **/
    private boolean         m_bWireframeAntiAliased = false;
    private float           m_fWireframeLineWidth   = 1.0f;
    private WireframeState.Face m_wireFace = WireframeState.Face.Front;
    /** Which sides are affected **/
    private MaterialFace    m_MaterialFace = MaterialFace.FrontAndBack;
    /** The default**/
    public static final PMeshMaterial DEFAULT_MATERIAL = new PMeshMaterial("Default Material");

    /**
     * This default constructor allocates memory for the member variables
     * (no member variable will be null)
     */
    public PMeshMaterial()
    {
        m_Diffuse  = new ColorRGBA(ColorRGBA.white);
        m_Emissive = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Specular = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Construct a new instance, setting the diffuse color to the one specified
     * and all other data to default values.
     * @param diffuseColor
     */
    public PMeshMaterial(ColorRGBA diffuseColor)
    {
        m_Diffuse  = new ColorRGBA(diffuseColor);
        m_Emissive = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Specular = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
    }

    /**
     * This constructor allocates memory for the member variables
     * (no member variable will be null)
     * @param name
     */
    public PMeshMaterial(String name)
    {
        this();
        setName(name);
    }

    /**
     * Construct a new material instance with the specified name and texture.
     * The string provided for the texture will be converted to a URL internally.
     * This is a minor performance hit, and may throw an exception. This conversion
     * should be avoided if possible and the URL version used instead.
     * @param name
     * @param texture0
     */
    public PMeshMaterial(String name, String texture0)
    {
        this();
        setName(name);

        URL textureLocation = null;
        if (texture0 != null)
        {
            try
            {
                textureLocation = new File(texture0).toURI().toURL();
            } catch (MalformedURLException ex)
            {
                Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (textureLocation != null)
        {
            m_textures[0] = new TextureMaterialProperties(textureLocation);
        }
        m_Diffuse = new ColorRGBA(ColorRGBA.white);
    }

    /**
     * Construct a new material with the specified name and texture for texture
     * unit zero.
     * @param name
     * @param texture0
     */
    public PMeshMaterial(String name, URL texture0)
    {
        this();
        setName(name);
        if (texture0 != null)
            m_textures[0] = new TextureMaterialProperties(texture0);
        m_Diffuse = new ColorRGBA(ColorRGBA.white);
    }

    /**
     * @param name
     * @param diffuse
     * @param ambient
     * @param emissive
     * @param specular
     * @param shininess
     * @param textures      - must be an array of size [8]
     */
    public PMeshMaterial(String name, ColorRGBA diffuse, ColorRGBA ambient, ColorRGBA emissive, ColorRGBA specular,
            int shininess, String [] textures, Texture.ApplyMode textureMode)
    {
        setName(name);

        m_Diffuse  = new ColorRGBA(1.0f, 0.0f, 1.0f, 0.0f);
        m_Emissive = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Specular = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);

        setDiffuse(diffuse);
        setAmbient(ambient);
        setEmissive(emissive);
        setSpecular(specular);

        setShininess(shininess);
        setTextures(textures);

        for (int i = 0; i < 8; i++)
        {
            if (m_textures[i] != null) // set the mode if relevant
                m_textures[i].setApplyMode(textureMode);
        }
    }

    public PMeshMaterial(PMeshMaterial other, boolean bUseOriginalMaterialNumberOfTextures)
    {
        if (other == null)
        {
            set(DEFAULT_MATERIAL);
            return;
        }

        m_Diffuse   = new ColorRGBA(other.getDiffuse());
        m_Ambient   = new ColorRGBA(other.getAmbient());
        m_Emissive  = new ColorRGBA(other.getEmissive());
        m_Specular  = new ColorRGBA(other.getSpecular());
        m_Shininess = other.getShininess();

        for (int i = 0; i < 8; i++)
        {
            if (other.getTexture(i) != null)
                m_textures[i] = other.getTexture(i);
        }

        m_ShaderArray = new AbstractShaderProgram[other.getShaders().length];
        for (int i = 0; i < m_ShaderArray.length; ++i)
        {
            if (other.getShader(i) != null)
                m_ShaderArray[i] = other.getShader(i);
        }

        m_ColorMaterial = other.getColorMaterial();
        m_MaterialFace = other.getMaterialFace();

        m_cullFace = other.getCullFace();
        m_bWireframeEnabled = other.isWireframeEnabled();
        m_bWireframeAntiAliased = other.isWireframeAntiAliased();
        m_fWireframeLineWidth = other.getWireframeLineWidth();
        m_wireFace = other.getWireframeFace();

        m_alphaState = other.getAlphaState();
        m_TransparencyColor = other.getTransparencyColor();
    }

    public PMeshMaterial(PMeshMaterial other)
    {
        this(other, true);
    }


    public PMeshMaterial(xmlMaterial xmlMat, WorldManager wm, String baseURL) {
        this();
        applyMaterialDOM(xmlMat, wm, baseURL);
    }
    /**
     * Don't use this function, use the hashcode comparison ;)
     * @param otherObject
     * @return true if the material properties and the texture file names are equal
     */
    @Override
    public boolean equals(Object otherObject)
    {
        if (otherObject == null)
            return false;

        if (this == otherObject)
            return true;

        if (otherObject.getClass() != this.getClass())
            return false;

        PMeshMaterial other = (PMeshMaterial)otherObject;

        boolean material = (

                m_Diffuse.equals(other.getDiffuse())   &&
                m_Ambient.equals(other.getAmbient())   &&
                m_Emissive.equals(other.getEmissive()) &&
                m_Specular.equals(other.getSpecular()) &&
                m_Shininess == other.getShininess()

                );

        boolean modes    = true;
        boolean textures = true;
        boolean shaders  = true;

        for (int i = 0; i < 8; i++)
        {
            if (m_textures[i] != null)
            {
                if (!m_textures[i].equals(other.getTexture(i)))
                    textures = false;
            }
            else if (other.getTexture(i) != null)
                textures = false;
        }

        for (int i = 0; i < m_ShaderArray.length; ++i)
        {
            if (m_ShaderArray[i] != null)
            {
                if (!m_ShaderArray[i].equals(other.getShader(i)))
                    shaders = false;
            }
            else if (other.getShader(i) != null)
                shaders = false;
        }

        if (m_ColorMaterial == other.getColorMaterial())
                modes    = true;
            else
                modes    = false;

        if (m_MaterialFace == other.getMaterialFace())
                modes    = true;
            else
                modes    = false;
        if (m_cullFace.equals(other.getCullFace()))
            modes = true;
        else
            modes = false;

        boolean wirestates = false;

        if (m_wireFace.equals(other.getWireframeFace()))
            wirestates = true;
        else
            wirestates = false;

        if (m_fWireframeLineWidth == other.getWireframeLineWidth() && wirestates == true)
            wirestates = true;
        else
            wirestates = false;
        wirestates = wirestates && (m_bWireframeEnabled == other.isWireframeEnabled()) &&
                     (m_bWireframeAntiAliased == other.isWireframeAntiAliased());

        return material && textures && modes && shaders && wirestates;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 37 * hash + (this.m_Diffuse  != null ? this.m_Diffuse.hashCode()  : 0);
        hash = 37 * hash + (this.m_Ambient  != null ? this.m_Ambient.hashCode()  : 0);
        hash = 37 * hash + (this.m_Emissive != null ? this.m_Emissive.hashCode() : 0);
        hash = 37 * hash + (this.m_Specular != null ? this.m_Specular.hashCode() : 0);
        hash = 37 * hash + Float.floatToIntBits(this.m_Shininess);

        for (int i = 0; i < m_textures.length; i++)
            hash = 37 * hash + (this.m_textures[i] != null ? this.m_textures[i].hashCode() : 0);

        for (int i = 0; i < m_ShaderArray.length; i++)
            hash = 37 * hash + (this.m_ShaderArray[i] != null ? this.m_ShaderArray[i].hashCode() : 0);

        hash = 37 * hash + this.m_MaterialFace.ordinal();
        hash = 37 * hash + this.m_ColorMaterial.ordinal();
        return hash;
    }

    public void set(PMeshMaterial other)
    {
        setDiffuse(other.getDiffuse());
        setAmbient(other.getAmbient());
        setEmissive(other.getEmissive());
        setSpecular(other.getSpecular());
        setShininess(other.getShininess());
        setTextures(other.getTextures());
        setShaders(other.getShaders());
        setColorMaterial(other.getColorMaterial());
        setMaterialFace(other.getMaterialFace());
        setCullFace(other.getCullFace());
        setWireframeCharacteristics(other.isWireframeEnabled(),
                                    other.isWireframeAntiAliased(),
                                    other.getWireframeLineWidth(),
                                    other.getWireframeFace());
        setAlphaState(other.getAlphaState());
        setTransparencyColor(other.getTransparencyColor());
    }

    public void setDiffuse(ColorRGBA diffuse)
    {
        if (diffuse != null)
            m_Diffuse.set(diffuse);
    }

    public void setAmbient(ColorRGBA ambient)
    {
        if (ambient != null)
            m_Ambient.set(ambient);
    }

    public void setEmissive(ColorRGBA emissive)
    {
        if (emissive != null)
            m_Emissive.set(emissive);
    }

    public void setSpecular(ColorRGBA specular)
    {
        if (specular != null)
            m_Specular.set(specular);
    }

    public void setTextures(TextureMaterialProperties[] textures)
    {
        int i = 0;
        if (textures != null)
        {
            for (i = 0; i < textures.length; i++)
            {
                if (textures[i] != null)
                    m_textures[i] = textures[i];
            }
        }
        // set the rest to null
        for ( ; i < 8; i++)
        {
            m_textures[i] = null;
        }
    }

    public void setTextures(URL[] textureLocations)
    {
        int i = 0;
        if (textureLocations != null)
        {
            for (i = 0; i < textureLocations.length; i++)
            {
                if (textureLocations[i] != null)
                {
                    m_textures[i] = new TextureMaterialProperties(textureLocations[i]);
                    m_textures[i].setTextureUnit(i);
                }
            }
        }
        // set the rest to null
        for ( ; i < 8; i++)
        {
            m_textures[i] = null;
        }
    }

    /**
     * Intrinsicly turn the provided non-null strings into URLs, and barring
     * failure of that create a new TextureMaterialProperties object representing
     * that texture.
     * @param textures
     */
    public void setTextures(String[] textures)
    {
        int i = 0;
        URL textureLocation = null;

        if (textures != null)
        {
            for (i = 0; i < textures.length; i++)
            {
                if (textures[i] != null)
                {
                    // out with the old
                    textureLocation = null;
                    try
                    {
                        textureLocation = new File(textures[i]).toURI().toURL();
                    } catch (MalformedURLException ex)
                    {
                        Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (textureLocation != null)
                    {
                        m_textures[i] = new TextureMaterialProperties(textureLocation);
                        m_textures[i].setTextureUnit(i);
                    }
                }
            }
        }
        // initialize the rest to null
        for ( ; i < 8; i++)
        {
            m_textures[i] = null;
        }
    }

    public void setTexture(TextureMaterialProperties texture, int index)
    {
        if (index < 0 || index > 7)
            return;
        m_textures[index] = texture;
    }

    /**
     * Set the file name of a texture in a specific index.
     * Note : remember to set the number of texture for the mesh!
     * @param fileName  -   the file name of the texture file
     * @param index     -   if you don't use multitexturing simply use 0 (can not be higher than 7)
     */
    public void setTexture(String fileName, int index, String baseURL)
    {
        if (index < 0 || index > 7 || fileName == null)
            return;
        URL textureLocation = null;
        try
        {
            if (baseURL==null) {
                textureLocation = new File(fileName).toURI().toURL();
            } else {
                textureLocation = new URL(baseURL+fileName);
            }
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (textureLocation != null)
        {
            m_textures[index] = new TextureMaterialProperties(textureLocation);
            m_textures[index].setTextureUnit(index);
        }
    }

    public void setTexture(File fileName, int index)
    {
        if (index < 0 || index > 7 || fileName == null)
            return;
        URL imageLocation = null;
        try
        {
            imageLocation = fileName.toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (imageLocation != null)
        {
            m_textures[index] = new TextureMaterialProperties(imageLocation);
            m_textures[index].setTextureUnit(index);
        }
    }

    public void setTexture(URL location, int index)
    {
        if (index < 0 || index > 7)
            return;
        m_textures[index] = new TextureMaterialProperties(location);
        m_textures[index].setTextureUnit(index);
    }

    /**
     * Shininess should be between zero and 128
     * @param shininess
     */
    public void setShininess(int shininess)
    {
        if (shininess < 0)
            m_Shininess = 0;
        else if (shininess > 128)
            m_Shininess = 128;
        else
            m_Shininess = shininess;
    }

    public ColorRGBA getDiffuse()
    {
        return m_Diffuse;
    }

    public ColorRGBA getAmbient()
    {
        return m_Ambient;
    }

    public ColorRGBA getEmissive()
    {
        return m_Emissive;
    }

    public ColorRGBA getSpecular()
    {
        return m_Specular;
    }

    public int getShininess()
    {
        return m_Shininess;
    }

    public TextureMaterialProperties[] getTextures()
    {
        return m_textures;
    }

    public TextureMaterialProperties getTexture(int index)
    {
        return m_textures[index];
    }


    public MaterialFace getMaterialFace()
    {
        return m_MaterialFace;
    }


    public void setMaterialFace(MaterialFace matFace)
    {
        m_MaterialFace = matFace;
    }


    public ColorMaterial getColorMaterial()
    {
        return m_ColorMaterial;
    }


    public void setColorMaterial(ColorMaterial colorMat)
    {
        m_ColorMaterial = colorMat;
    }

    public AbstractShaderProgram[] getShaders()
    {
        return m_ShaderArray;
    }

    public AbstractShaderProgram getShader(int index)
    {
        return m_ShaderArray[index];
    }

    public AbstractShaderProgram getShader()
    {
        return m_ShaderArray[0];
    }

    public void setShaders(AbstractShaderProgram[] shaders)
    {
        if (shaders == null)
            m_ShaderArray = new AbstractShaderProgram[1];
        else
        {
            m_ShaderArray = new AbstractShaderProgram[shaders.length];
            for (int i = 0; i < shaders.length; ++i)
                m_ShaderArray[i] = shaders[i];
        }
    }

    public void setShader(AbstractShaderProgram shader, int index)
    {
        m_ShaderArray[index] = shader;
    }

    public void setShader(AbstractShaderProgram shader)
    {
        m_ShaderArray[0] = shader;
    }

    public CullState.Face getCullFace()
    {
        return m_cullFace;
    }

    public void setCullFace(CullState.Face cullFace)
    {
        m_cullFace = cullFace;
    }

    public boolean isWireframeEnabled()
    {
        return m_bWireframeEnabled;
    }

    public void setWireframeEnabled(boolean bWireframe)
    {
        m_bWireframeEnabled = bWireframe;
    }

    public void setWireframeCharacteristics(boolean enabled,
            boolean antiAliased, float fWireWidth,
            WireframeState.Face face)
    {
        m_bWireframeEnabled = enabled;
        m_bWireframeAntiAliased = antiAliased;
        m_fWireframeLineWidth = fWireWidth;
        m_wireFace = face;
    }

    public boolean isWireframeAntiAliased()
    {
        return m_bWireframeAntiAliased;
    }

    public float getWireframeLineWidth()
    {
        return m_fWireframeLineWidth;
    }

    public WireframeState.Face getWireframeFace()
    {
        return m_wireFace;
    }
    /**
     * Determine how many texture units are needed.
     * @return Number used
     */
    public int getNumberOfRelevantTextures()
    {
        int result = 0;
        for (TextureMaterialProperties tex : m_textures)
        {
            if (tex != null)
                result++;
        }
        return result;
    }

    public AlphaTransparencyType getAlphaState()
    {
        return m_alphaState;
    }

    public void setAlphaState(AlphaTransparencyType alphaState)
    {
        m_alphaState = alphaState;
    }

    public ColorRGBA getTransparencyColor()
    {
        return m_TransparencyColor;
    }

    public void setTransparencyColor(ColorRGBA transparencyColor)
    {
        m_TransparencyColor = transparencyColor;
    }

    private void applyMaterialDOM(xmlMaterial xmlMat, WorldManager wm, String baseURL)
    {
        int counter = 0;
        // First, the texture materials!
        for (xmlTextureAttributes texAttr : xmlMat.getTextures())
        {
            m_textures[counter] = new TextureMaterialProperties(texAttr, baseURL);
            counter++;
        }
        // diffuseColor
        if (xmlMat.getDiffuseColor() != null)
            setDiffuse(xmlMat.getDiffuseColor().getColorRGBA());
        // ambientColor
        if (xmlMat.getAmbientColor() != null)
            setAmbient(xmlMat.getAmbientColor().getColorRGBA());
        // emissiveColor
        if (xmlMat.getEmissiveColor() != null)
            setEmissive(xmlMat.getEmissiveColor().getColorRGBA());
        // specularColor
        if (xmlMat.getSpecularColor() != null)
            setSpecular(xmlMat.getSpecularColor().getColorRGBA());
        // transparencyColor
        if (xmlMat.getTransparencyColor() != null)
            setTransparencyColor(xmlMat.getTransparencyColor().getColorRGBA());
        // shaders
        counter = 0;
        for (xmlShader shaderDOM : xmlMat.getShaders())
        {
            // Parse the program
            AbstractShaderProgram shader = ShaderUtils.createShader(shaderDOM.getProgram(), wm);
            // Apply the properties
            applyProperties(shaderDOM.getProperties(), shader);
            setShader(shader, counter);
            counter++;
        }
        // shininess
        setShininess(xmlMat.getShininess());
        // colorMaterial
        setColorMaterial(ColorMaterial.valueOf(xmlMat.getColorMaterial()));
        // cullFace
        setCullFace(Face.valueOf(xmlMat.getCullFace()));
        // name
        setName(xmlMat.getName());
    }

    /**
     * Apply the provided property list to the provided shader
     * @param properties
     * @param shader
     */
    private void applyProperties(List<xmlShaderProperty> properties,
                                        AbstractShaderProgram shader)
    {
        for (xmlShaderProperty xmlProp : properties)
        {
            ShaderProperty prop = new ShaderProperty(xmlProp);
            try {
                shader.setProperty(prop);
            }
            catch (NoSuchPropertyException ex) {
                Logger.getLogger(PMeshMaterial.class.getName()).log(Level.WARNING,
                        "Unknown property read from XML file! : " + ex.getMessage());
            }
        }
    }

    /****************************
     * SERIALIZATION ASSISTANCE *
     ****************************/
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        // Re-allocate all transient objects
    }
}
