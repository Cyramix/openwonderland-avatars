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
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.MaterialState.MaterialFace;
import com.jme.scene.state.WireframeState;
import imi.scene.PNode;
import imi.scene.shader.AbstractShaderProgram;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides state for our materials system.
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class PMeshMaterial extends PNode
{
    /** Diffuse material color **/
    private ColorRGBA  m_Diffuse   = null;
    /** Ambient material color **/
    private ColorRGBA  m_Ambient   = null;
    /** Emissive material color **/
    private ColorRGBA  m_Emissive  = null;
    /** Specular material color **/
    private ColorRGBA  m_Specular  = null;
    /** Shininess value **/
    private float      m_Shininess = 0.0f;   // 0 is none, around 5 is low, around 100 is high
    /** Texture map locations **/
    private URL[]  m_Textures      = new URL[8];  // multi texturing supported
    /** Desired combination mode for textures **/
    private Texture.ApplyMode []  m_TextureModes = new Texture.ApplyMode [8];  // texture combine mode for each texture  
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
        m_Ambient  = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Emissive = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Specular = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        for (int i = 0; i < 8; i++)
            m_TextureModes[i] = Texture.ApplyMode.Modulate;
    }

    /**
     * Construct a new instance, setting the diffuse color to the one specified
     * and all other data to default values.
     * @param diffuseColor
     */
    public PMeshMaterial(ColorRGBA diffuseColor) 
    {
        m_Diffuse  = new ColorRGBA(diffuseColor);
        m_Ambient  = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Emissive = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Specular = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        for (int i = 0; i < 8; i++)
            m_TextureModes[i] = Texture.ApplyMode.Modulate;
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
        
        if (texture0 != null)
        {
            try
            {
                m_Textures[0] = new File(texture0).toURI().toURL();
            } catch (MalformedURLException ex)
            {
                Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            m_Textures[0] = texture0;
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
            float shininess, String [] textures, Texture.ApplyMode textureMode)
    {
        setName(name);
        
        m_Diffuse  = new ColorRGBA(1.0f, 0.0f, 1.0f, 0.0f);
        m_Ambient  = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Emissive = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        m_Specular = new ColorRGBA(0.0f, 0.0f, 0.0f, 0.0f);
        
        setDiffuse(diffuse);
        setAmbient(ambient);
        setEmissive(emissive);
        setSpecular(specular);
        
        setShininess(shininess);
        setTextures(textures);
        for (int i = 0; i < 8; i++)
            m_TextureModes[i] = textureMode;
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
        
        int numTextures = bUseOriginalMaterialNumberOfTextures ? m_Textures.length : other.m_Textures.length;
        for (int i = 0; i < numTextures; i++)
        {
            if (other.getTexture(i) != null)
                m_Textures[i] = other.getTexture(i);
        }
        
        m_ShaderArray = new AbstractShaderProgram[other.getShaders().length];
        for (int i = 0; i < m_ShaderArray.length; ++i)
        {
            if (other.getShader(i) != null)
                m_ShaderArray[i] = other.getShader(i);
        }
        
        for (int i = 0; i < m_TextureModes.length; i++)
            m_TextureModes[i]    = other.getTextureMode(i);
        
        m_ColorMaterial = other.getColorMaterial();
        m_MaterialFace = other.getMaterialFace();
        
        m_cullFace = other.getCullFace();
        m_bWireframeEnabled = other.isWireframeEnabled();
        m_bWireframeAntiAliased = other.isWireframeAntiAliased();
        m_fWireframeLineWidth = other.getWireframeLineWidth();
        m_wireFace = other.getWireframeFace();
    }
    
    public PMeshMaterial(PMeshMaterial other)
    {
        this(other, true);
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
        
        boolean modes    = false;
        boolean textures = false;
        boolean shaders  = false;
        for (int i = 0; i < 8; i++)
        {
            if (m_Textures[i] != null)
            {
                if (m_Textures[i].equals(other.getTexture(i)))
                    textures = true;
                else
                    textures = false;
            }
            else if (other.getTexture(i) == null)
                textures = true;
            
            if (m_TextureModes[i] == other.getTextureMode(i))
                modes    = true;
            else
                modes    = false;
        }
        
        for (int i = 0; i < m_ShaderArray.length; ++i)
        {
            if (m_ShaderArray[i] != null)
            {
                if (m_ShaderArray[i].equals(other.getShader(i)))
                    shaders = true;
                else
                    shaders = false;
            }
            else if (other.getShader(i) == null)
                shaders = true;
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
        
        for (int i = 0; i < m_Textures.length; i++)
            hash = 37 * hash + (this.m_Textures[i] != null ? this.m_Textures[i].hashCode() : 0);
//        hash = 37 * hash + (this.m_Textures != null ? this.m_Textures.hashCode() : 0);
  
//        for (int i = 0; i < m_VertShader.length; i++)
//            hash = 37 * hash + (this.m_VertShader[i] != null ? this.m_VertShader[i].hashCode() : 0);
        for (int i = 0; i < m_ShaderArray.length; i++)
            hash = 37 * hash + (this.m_ShaderArray[i] != null ? this.m_ShaderArray[i].hashCode() : 0);
        
        for (int i = 0; i < m_TextureModes.length; i++)
            hash = 37 * hash + this.m_TextureModes[i].ordinal();
        //hash = 37 * hash + (this.m_TextureModes != null ? this.m_TextureModes.hashCode() : 0);
        
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
        setTextureModes(other.getTextureModes());
        setShaders(other.getShaders());
        setColorMaterial(other.getColorMaterial());
        setMaterialFace(other.getMaterialFace());
        setCullFace(other.getCullFace());
        setWireframeCharacteristics(other.isWireframeEnabled(),
                                    other.isWireframeAntiAliased(), 
                                    other.getWireframeLineWidth(), 
                                    other.getWireframeFace());
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

    public void setTextures(URL[] textures) 
    {
        int i = 0;
        if (textures != null)
        {
            for (i = 0; i < textures.length; i++)
            {
                if (textures[i] != null)
                    m_Textures[i] = textures[i];
            }
        }
        // initialize the rest to null
        for ( ; i < 8; i++)
        {
            m_Textures[i] = null;
        }
    }
    
    public void setTextures(String[] textures) 
    {
        int i = 0;
        if (textures != null)
        {
            for (i = 0; i < textures.length; i++)
            {
                if (textures[i] != null)
                {
                    try
                    {
                        m_Textures[i] = new File(textures[i]).toURI().toURL();
                    } catch (MalformedURLException ex)
                    {
                        Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        // initialize the rest to null
        for ( ; i < 8; i++)
        {
            m_Textures[i] = null;
        }
    }
    
    /**
     * Set the file name of a texture in a specific index.
     * Note : remember to set the number of texture for the mesh!
     * @param fileName  -   the file name of the texture file
     * @param index     -   if you don't use multitexturing simply use 0 (can not be higher than 7)
     */
    public void setTexture(String fileName, int index)
    {
        if (index < 0 || index > 7 || fileName == null)
            return;
        try
        {

            m_Textures[index] = new File(fileName).toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setTexture(File fileName, int index)
    {
        if (index < 0 || index > 7 || fileName == null)
            return;
        try
        {

            m_Textures[index] = fileName.toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(PMeshMaterial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setTexture(URL location, int index)
    {
        if (index < 0 || index > 7)
            return;
        m_Textures[index] = location;
    }
    
    public void setShininess(float shininess) 
    {
        if (shininess >= 0.0f)
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
    
    public float getShininess() 
    {
        return m_Shininess;
    }

    public URL[] getTextures() 
    {
        return m_Textures;
    }
    
    public URL getTexture(int index)
    {
        return m_Textures[index];
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

    public Texture.ApplyMode[] getTextureModes() 
    {
        return m_TextureModes;
    }
    
    public Texture.ApplyMode getTextureMode(int textureUnit) 
    {
        return m_TextureModes[textureUnit];
    }

    public void setTextureModes(Texture.ApplyMode[] TextureModes) 
    {
        for (int i = 0; i < TextureModes.length; i++)
            m_TextureModes[i] = TextureModes[i];
    }
    
    public void setTextureMode(int textureUnit, Texture.ApplyMode TextureMode) 
    {
        m_TextureModes[textureUnit] = TextureMode;
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
        m_ShaderArray = new AbstractShaderProgram[shaders.length];
        for (int i = 0; i < shaders.length; ++i)
            m_ShaderArray[i] = shaders[i];
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
}
