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

import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.CullState.Face;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.MaterialState.MaterialFace;
import com.jme.scene.state.WireframeState;
import imi.shader.AbstractShaderProgram;
import imi.shader.NoSuchPropertyException;
import imi.shader.ShaderProperty;
import imi.shader.ShaderUtils;
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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastTable;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * This class provides state for our materials system.
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 */
public class PMeshMaterial implements Serializable
{
    /** Serialization version number **/
    private static final long serialVersionUID = 1l;

    private static final ColorRGBA defaultAmbient = new ColorRGBA(0.40f, 0.40f, 0.40f, 1.0f);

    /** Alpha transparency enumeration **/
    public enum AlphaTransparencyType
    {
        NO_TRANSPARENCY,
        RGB_ZERO,
        A_ONE
    }
    /** Diffuse material color **/
    private final ColorRGBA  diffuse   = new ColorRGBA(ColorRGBA.white);
    /** Ambient material color **/
    private final ColorRGBA  ambient   = new ColorRGBA(defaultAmbient);
    /** Emissive material color **/
    private final ColorRGBA  emissive  = new ColorRGBA(ColorRGBA.white);
    /** Specular material color **/
    private final ColorRGBA  specular  = new ColorRGBA(ColorRGBA.white);
    /** Transparency Color **/
    private final ColorRGBA  transparencyColor = new ColorRGBA(ColorRGBA.white);
    /** Shininess value **/
    private int      shininess = 0;   // 0 is none, around 5 is low, around 100 is high
    /** Alpha state **/
    private AlphaTransparencyType alphaState = AlphaTransparencyType.NO_TRANSPARENCY;
    /** Texture properties **/
    private final TextureMaterialProperties[] textures = new TextureMaterialProperties[8];
    /** Shader collection **/
    private final List<AbstractShaderProgram> shaderArray = new FastTable<AbstractShaderProgram>();
    /** Define the interaction between vertex colors and material properties **/
    private ColorMaterial   colorMaterial = ColorMaterial.None;
    /** Cull mode for this piece of geometry **/
    private CullState.Face  cullFace = CullState.Face.Back;
    /** Wireframe mode? **/
    private boolean         wireframeEnabled = false;
    /** Wireframe characteristics follow **/
    private boolean         wireframeAntiAliased = false;
    private float           wireframeLineWidth   = 1.0f;
    private WireframeState.Face wireFace = WireframeState.Face.Front;
    /** Which sides are affected **/
    private MaterialFace    materialFace = MaterialFace.FrontAndBack;

    private String          name = "Unnamed Material";

    /**
     * Create a new default instance.
     */
    public PMeshMaterial() {}

    /**
     * This constructor allocates memory for the member variables
     * (no member variable will be null)
     * @param name A non-null name
     */
    public PMeshMaterial(String name)
    {
        if (name == null)
            throw new IllegalArgumentException("Null name provided!");
        this.name = name;
    }

    /**
     * Copy the provided material.
     * @param other A non-null material to copy
     */
    public PMeshMaterial(PMeshMaterial other) {
        set(other);
    }

    /**
     * Set the state of this PMeshMaterial to match the specified other.
     * @param other A non-null material to copy
     * @return this
     * @throws NullPointerException If other == null
     */
    public PMeshMaterial set(PMeshMaterial other) {
        diffuse.set(other.diffuse);
        ambient.set(other.ambient);
        emissive.set(other.emissive);
        specular.set(other.specular);
        shininess = other.shininess;

        for (int i = 0; i < 8; i++)
            textures[i] = other.getTextureRef(i);

        shaderArray.clear();
        shaderArray.addAll(other.shaderArray);

        colorMaterial = other.colorMaterial;
        materialFace = other.materialFace;

        cullFace = other.cullFace;
        wireframeEnabled = other.wireframeEnabled;
        wireframeAntiAliased = other.wireframeAntiAliased;
        wireframeLineWidth = other.wireframeLineWidth;
        wireFace = other.wireFace;

        alphaState = other.alphaState;
        transparencyColor.set(other.transparencyColor);
        name = other.name;
        return this;
    }

    /**
     * Construct a new material from the provided DOM
     * @param xmlMat A non-null DOM to use
     * @param wm WorldManager ref
     * @param baseURL
     */
    @InternalAPI
    public PMeshMaterial(xmlMaterial xmlMat, WorldManager wm, String baseURL) {
        applyMaterialDOM(xmlMat, wm, baseURL);
    }

    /**
     * Set the diffuse color
     * @param diffuse
     * @return this
     */
    public PMeshMaterial setDiffuse(ColorRGBA diffuse)
    {
        this.diffuse.set(diffuse);
        return this;
    }

    /**
     * Set the ambient color
     * @param ambient
     * @return this
     */
    public PMeshMaterial setAmbient(ColorRGBA ambient)
    {
        this.ambient.set(ambient);
        return this;
    }

    /**
     * Set the emissive color
     * @param emissive
     * @return this
     */
    public PMeshMaterial setEmissive(ColorRGBA emissive)
    {
        this.emissive.set(emissive);
        return this;
    }


    /**
     * Set the specular color
     * @param specular
     * @return this
     */
    public PMeshMaterial setSpecular(ColorRGBA specular)
    {
        this.specular.set(specular);
        return this;
    }


    public PMeshMaterial setTextures(TextureMaterialProperties[] textures)
    {
        for (int i = 0; i < 8; ++i){
            if (i < textures.length && textures[i] != null)
                this.textures[i] = new TextureMaterialProperties(textures[i]);
            else
                this.textures[i] = null;
        }
        return this;
    }

    public PMeshMaterial setTextures(URL[] textureLocations)
    {
        for (int i = 0; i < 8; ++i){
            if (i < textures.length && textures[i] != null)
                this.textures[i] = new TextureMaterialProperties(textures[i]);
            else
                this.textures[i] = null;
        }
        return this;
    }

    /**
     * Set the texture at the specified index
     * @param texture
     * @param index
     * @return this
     */
    public PMeshMaterial setTexture(TextureMaterialProperties texture, int index)
    {
        textures[index] = new TextureMaterialProperties(texture); // Array index OOB if invalid
        return this;
    }

    /**
     * Set the file name of a texture in a specific index.
     * @param relativePath The file path to the texture file
     * @param index The unit to use
     * @param baseURL A prefix for the file path (may be null)
     * @return this
     */
    public PMeshMaterial setTexture(String relativePath, int index, String baseURL)
    {
        URL textureLocation = null;
        try {
            if (baseURL != null)
                relativePath = baseURL + relativePath;
            textureLocation = new File(relativePath).toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Could not create a URL from " + relativePath, ex);
        }

        textures[index] = new TextureMaterialProperties(textureLocation); // Array Index OOB if invalid
        textures[index].setTextureUnit(index);
        return this;
    }

    /**
     * Set the texture at the specified index to the provided file
     * @param file
     * @param index
     * @return this
     */
    public PMeshMaterial setTexture(File file, int index)
    {
        URL imageLocation = null;
        try {
            imageLocation = file.toURI().toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Could not create a URL from " + file, ex);
        }
        textures[index] = new TextureMaterialProperties(imageLocation);
        textures[index].setTextureUnit(index);
        return this;
    }

    /**
     * Set the texture at the specified unit
     * @param index
     * @param location
     * @return this
     */
    public PMeshMaterial setTexture(int index, URL location)
    {
        textures[index] = new TextureMaterialProperties(location); // throw array OOB if invalid
        textures[index].setTextureUnit(index);
        return this;
    }

    /**
     * Shininess should be between zero and 128
     * @param shininess
     * @return this
     */
    public PMeshMaterial setShininess(int shininess)
    {
        if (shininess < 0 || shininess > 128)
            throw new IllegalArgumentException("Invalid shininess value : " + shininess + ", must be 0-128");
            shininess = 0;
        this.shininess = shininess;
        return this;
    }

    public void getDiffuse(ColorRGBA out)
    {
        out.set(diffuse);
    }

    public void getAmbient(ColorRGBA out)
    {
        out.set(ambient);
    }

    public void getEmissive(ColorRGBA out)
    {
        out.set(emissive);
    }

    public void getSpecular(ColorRGBA out)
    {
        out.set(specular);
    }

    //// Convenience methds for package level access
    @InternalAPI
    ColorRGBA getDiffuseRef() {
        return diffuse;
    }

    @InternalAPI
    ColorRGBA getAmbientRef() {
        return ambient;
    }

    @InternalAPI
    ColorRGBA getEmissiveRef() {
        return emissive;
    }

    @InternalAPI
    ColorRGBA getSpecularRef() {
        return specular;
    }

    public int getShininess()
    {
        return shininess;
    }

    public void getTextureReferences(TextureMaterialProperties[] out) {
        for (int i = 0; i < 8; ++i)
            out[i] = textures[i];
    }

    public TextureMaterialProperties getTextureRef(int index)
    {
        return textures[index];
    }


    public MaterialFace getMaterialFace()
    {
        return materialFace;
    }


    public PMeshMaterial setMaterialFace(MaterialFace matFace)
    {
        materialFace = matFace;
        return this;
    }


    public ColorMaterial getColorMaterial()
    {
        return colorMaterial;
    }

    public PMeshMaterial setColorMaterial(ColorMaterial colorMat)
    {
        colorMaterial = colorMat;
        return this;
    }

    public Iterable<AbstractShaderProgram> getShaders()
    {
        return shaderArray;
    }

    public AbstractShaderProgram getShader(int index)
    {
        return shaderArray.get(index);
    }

    public AbstractShaderProgram getShader()
    {
        if (shaderArray.isEmpty())
            return null;
        return shaderArray.get(0);
    }
    
    public int getShaderCount() {
        return shaderArray.size();
    }

    public PMeshMaterial setShaders(AbstractShaderProgram[] shaders)
    {
        shaderArray.clear();
        for (AbstractShaderProgram shader : shaders)
            shaderArray.add(shader);
        return this;
    }


    public PMeshMaterial addShader(AbstractShaderProgram shader)
    {
        shaderArray.add(shader);
        return this;
    }

    public PMeshMaterial setDefaultShader(AbstractShaderProgram shader)
    {
        shaderArray.clear();
        shaderArray.add(shader);
        return this;
    }

    public CullState.Face getCullFace()
    {
        return cullFace;
    }

    public PMeshMaterial setCullFace(CullState.Face cullFace)
    {
        this.cullFace = cullFace;
        return this;
    }

    public boolean isWireframeEnabled()
    {
        return wireframeEnabled;
    }

    public PMeshMaterial setWireframeEnabled(boolean bWireframe)
    {
        wireframeEnabled = bWireframe;
        return this;
    }

    public boolean isWireframeAntiAliased()
    {
        return wireframeAntiAliased;
    }

    public float getWireframeLineWidth()
    {
        return wireframeLineWidth;
    }

    public WireframeState.Face getWireframeFace()
    {
        return wireFace;
    }
    /**
     * Determine how many texture units are needed.
     * @return Number used
     */
    public int getNumberOfRelevantTextures()
    {
        int result = 0;
        for (TextureMaterialProperties tex : textures)
        {
            if (tex != null)
                result++;
        }
        return result;
    }

    public AlphaTransparencyType getAlphaState()
    {
        return alphaState;
    }

    public PMeshMaterial setAlphaState(AlphaTransparencyType alphaState)
    {
        this.alphaState = alphaState;
        return this;
    }

    public void getTransparencyColor(ColorRGBA out)
    {
        out.set(transparencyColor);
    }

    public PMeshMaterial setTransparencyColor(ColorRGBA transparencyColor)
    {
        transparencyColor.set(transparencyColor);
        return this;
    }
    
    public String getName() {
        return name;
    }

    private void applyMaterialDOM(xmlMaterial xmlMat, WorldManager wm, String baseURL)
    {
        int counter = 0;
        // First, the texture materials!
        for (xmlTextureAttributes texAttr : xmlMat.getTextures())
        {
            textures[counter] = new TextureMaterialProperties(texAttr, baseURL);
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
        shaderArray.clear();
        for (xmlShader shaderDOM : xmlMat.getShaders())
        {
            // Parse the program
            AbstractShaderProgram shader = ShaderUtils.createShader(shaderDOM.getProgram(), wm);
            // Apply the properties
            applyProperties(shaderDOM.getProperties(), shader);
            addShader(shader);
            counter++;
        }
        // shininess
        setShininess(xmlMat.getShininess());
        // colorMaterial
        setColorMaterial(ColorMaterial.valueOf(xmlMat.getColorMaterial()));
        // cullFace
        setCullFace(Face.valueOf(xmlMat.getCullFace()));
        // name
        if (xmlMat.getName() != null)
            name = xmlMat.getName();


        if (xmlMat.getAlphaState() == 0)
            setAlphaState(AlphaTransparencyType.NO_TRANSPARENCY);
        else if (xmlMat.getAlphaState() == 1)
            setAlphaState(AlphaTransparencyType.A_ONE);
        else
            setAlphaState(AlphaTransparencyType.RGB_ZERO);
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



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PMeshMaterial other = (PMeshMaterial) obj;
        if (this.diffuse != other.diffuse && (this.diffuse == null || !this.diffuse.equals(other.diffuse))) {
            return false;
        }
        if (this.ambient != other.ambient && (this.ambient == null || !this.ambient.equals(other.ambient))) {
            return false;
        }
        if (this.emissive != other.emissive && (this.emissive == null || !this.emissive.equals(other.emissive))) {
            return false;
        }
        if (this.specular != other.specular && (this.specular == null || !this.specular.equals(other.specular))) {
            return false;
        }
        if (this.transparencyColor != other.transparencyColor && (this.transparencyColor == null || !this.transparencyColor.equals(other.transparencyColor))) {
            return false;
        }
        if (this.shininess != other.shininess) {
            return false;
        }
        if (this.alphaState != other.alphaState) {
            return false;
        }
        if (!Arrays.deepEquals(this.textures, other.textures)) {
            return false;
        }
        if (this.shaderArray != other.shaderArray && (this.shaderArray == null || !this.shaderArray.equals(other.shaderArray))) {
            return false;
        }
        if (this.colorMaterial != other.colorMaterial) {
            return false;
        }
        if (this.cullFace != other.cullFace) {
            return false;
        }
        if (this.wireframeEnabled != other.wireframeEnabled) {
            return false;
        }
        if (this.wireframeAntiAliased != other.wireframeAntiAliased) {
            return false;
        }
        if (this.wireframeLineWidth != other.wireframeLineWidth) {
            return false;
        }
        if (this.wireFace != other.wireFace) {
            return false;
        }
        if (this.materialFace != other.materialFace) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.diffuse != null ? this.diffuse.hashCode() : 0);
        hash = 79 * hash + (this.ambient != null ? this.ambient.hashCode() : 0);
        hash = 79 * hash + (this.emissive != null ? this.emissive.hashCode() : 0);
        hash = 79 * hash + (this.specular != null ? this.specular.hashCode() : 0);
        hash = 79 * hash + (this.transparencyColor != null ? this.transparencyColor.hashCode() : 0);
        hash = 79 * hash + this.shininess;
        hash = 79 * hash + this.alphaState.hashCode();
        hash = 79 * hash + Arrays.deepHashCode(this.textures);
        hash = 79 * hash + (this.shaderArray != null ? this.shaderArray.hashCode() : 0);
        hash = 79 * hash + this.colorMaterial.hashCode();
        hash = 79 * hash + this.cullFace.hashCode();
        hash = 79 * hash + (this.wireframeEnabled ? 1 : 0);
        hash = 79 * hash + (this.wireframeAntiAliased ? 1 : 0);
        hash = 79 * hash + Float.floatToIntBits(this.wireframeLineWidth);
        hash = 79 * hash + this.wireFace.hashCode();
        hash = 79 * hash + this.materialFace.hashCode();
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
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
