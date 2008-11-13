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
package org.collada.xml_walker;

import org.collada.colladaschema.Technique;
import java.util.List;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState.Face;
import org.collada.colladaschema.Extra;
import org.collada.colladaschema.ProfileCOMMON.Technique.Blinn;
import org.collada.colladaschema.ProfileCOMMON.Technique.Lambert;
import org.collada.colladaschema.ProfileCOMMON.Technique.Phong;
import org.collada.colladaschema.CommonColorOrTextureType;
import org.collada.colladaschema.CommonFloatOrParamType;
import org.collada.colladaschema.CommonTransparentType;
import org.collada.colladaschema.FxOpaqueEnum;
import org.collada.colladaschema.CommonColorOrTextureType.Texture;
import org.collada.colladaschema.CommonNewparamType;
import org.collada.colladaschema.FxSampler2DCommon;
import org.collada.colladaschema.FxSurfaceCommon;
import org.collada.colladaschema.FxSurfaceInitFromCommon;
import org.collada.colladaschema.Image;
import org.collada.colladaschema.ProfileCOMMON;

import imi.utils.FileUtils;

import imi.loaders.collada.Collada;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.NormalMapShader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 *
 * @author Chris Nagle
 */
public class PColladaMaterial
{
    private Collada         m_pCollada                  = null;
    private ProfileCOMMON   m_pProfileCommon            = null;

    private String          m_Name                      = "";

    private PColladaColor   m_EmissiveColor             = new PColladaColor();
    private PColladaColor   m_AmbientColor              = new PColladaColor();
    private PColladaColor   m_DiffuseColor              = new PColladaColor();
    private PColladaColor   m_SpecularColor             = new PColladaColor();

    private float           m_fShininess                = 0.0f;
    private PColladaColor   m_ReflectiveColor           = new PColladaColor();
    private float           m_fReflectivity             = 0.0f;

    /** Transparency information **/
    private PColladaColor   m_TransparentColor          = new PColladaColor();
    private float           m_fTransparency             = 0.0f;
    private int             m_nTransparencyMode         = -1; // -1 no transparency, 0 represents RGB_ZERO, 1 represents A_ONE
    private float           m_fIndexOfRefraction        = 0.0f;

    /** Emissive map     **/
    private String          m_EmissiveImageFilename     = "";
    /** Ambient map    **/
    private String          m_AmbientImageFilename      = "";
    /** Treated as the default diffuse map  **/
    private List<String>    m_DiffuseImageFilename      = new ArrayList<String>();
    /** Interpreted as a specular map       **/
    private String          m_SpecularImageFilename     = "";
    private String          m_ReflectiveImageFilename   = "";

    /** Bump map (currently unsupported)    **/
    private String          m_BumpMapImageFilename      = "";
    /** Normal map **/
    private String          m_NormalMapImageFilename    = "";



    //  Constructor.
    public PColladaMaterial(Collada pCollada, ProfileCOMMON pProfileCommon)
    {
        m_pCollada = pCollada;
        m_pProfileCommon = pProfileCommon;
    }



    //  Gets the Material's name.
    public String getName()
    {
        return(m_Name);
    }



    //  Initializes the ColladaMaterial based on a blinn effect.
    public void initialize(String name, Blinn pBlinn)
    {
        m_Name = name;

        m_EmissiveImageFilename = processColorOrTexture(pBlinn.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pBlinn.getAmbient(), m_AmbientColor);
        
        
        m_DiffuseImageFilename = processDiffuseColorsOrTextures(pBlinn.getDiffuse(), m_DiffuseColor);
        
        m_SpecularImageFilename = processColorOrTexture(pBlinn.getSpecular(), m_SpecularColor);
        
        //m_NormalMapImageFilename = processColorOrTexture(pBlinn., m_AmbientColor)
        
                
        m_fShininess = processFloatAttribute(pBlinn.getShininess());

        m_ReflectiveImageFilename = processColorOrTexture(pBlinn.getReflective(), m_ReflectiveColor);
        m_fReflectivity = processFloatAttribute(pBlinn.getReflectivity());
        processTransparent(pBlinn.getTransparent(), pBlinn.getTransparency());
        m_fTransparency = processFloatAttribute(pBlinn.getTransparency());
        m_fIndexOfRefraction = processFloatAttribute(pBlinn.getIndexOfRefraction());
    }

    //  Initializes the ColladaMaterial based on a phong effect.
    public void initialize(String name, Phong pPhong)
    {
        m_Name = name;
        
        m_EmissiveImageFilename = processColorOrTexture(pPhong.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pPhong.getAmbient(), m_AmbientColor);
        
        m_DiffuseImageFilename = processDiffuseColorsOrTextures(pPhong.getDiffuse(), m_DiffuseColor);
        
        m_SpecularImageFilename = processColorOrTexture(pPhong.getSpecular(), m_SpecularColor);
        m_fShininess = processFloatAttribute(pPhong.getShininess());

        m_ReflectiveImageFilename = processColorOrTexture(pPhong.getReflective(), m_ReflectiveColor);
        m_fReflectivity = processFloatAttribute(pPhong.getReflectivity());
        processTransparent(pPhong.getTransparent(), pPhong.getTransparency());
        m_fTransparency = processFloatAttribute(pPhong.getTransparency());
        m_fIndexOfRefraction = processFloatAttribute(pPhong.getIndexOfRefraction());
    }

    //  Initializes the ColladaMaterial based on a lambert effect.
    public void initialize(String name, Lambert pLambert)
    {
        m_Name = name;

        m_EmissiveImageFilename = processColorOrTexture(pLambert.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pLambert.getAmbient(), m_AmbientColor);
        
        m_DiffuseImageFilename = processDiffuseColorsOrTextures(pLambert.getDiffuse(), m_DiffuseColor);
        
        m_ReflectiveImageFilename = processColorOrTexture(pLambert.getReflective(), m_ReflectiveColor);
        m_fReflectivity = processFloatAttribute(pLambert.getReflectivity());
        processTransparent(pLambert.getTransparent(), pLambert.getTransparency());
        m_fTransparency = processFloatAttribute(pLambert.getTransparency());
        m_fIndexOfRefraction = processFloatAttribute(pLambert.getIndexOfRefraction());
    }
    
    
    public void applyBumpMappingData(Extra extra)
    {
        if (extra.getTechniques() == null) // no techniques to worry with
            return;
        
        for (Technique technique : extra.getTechniques()) // for each nested technique
        {
            if (technique.getAnies() != null)
            {
                for (Element someElement : technique.getAnies())
                {
                    if (someElement.getTagName().equals("bump")) // normal mapped
                    {
                        NodeList textureList = someElement.getElementsByTagName("texture");
                        // only grab texture zero and assign it as the normal map
                        if (textureList.getLength() > 0)
                        {
                            m_NormalMapImageFilename = getTextureFilename(textureList.item(0).getAttributes().getNamedItem("texture").getTextContent());
                            m_NormalMapImageFilename = FileUtils.getShortFilename(m_NormalMapImageFilename);
                        }
                    }
                }
            }
        }
    }




    /**
     * Generate a PMeshMaterial to match the state of this object.
     * @return
     */
    public PMeshMaterial createMeshMaterial()
    {
        //boolean bTransparency = (m_nTransparencyMode >= 0);
        
        PMeshMaterial result = new PMeshMaterial(m_Name);
        // Colors!
        result.setEmissive(buildColorRGBA(m_EmissiveColor));
        result.setAmbient(buildColorRGBA(m_AmbientColor));
        result.setSpecular(buildColorRGBA(m_SpecularColor));
        result.setDiffuse(buildColorRGBA(m_DiffuseColor));
        
        // Shininess
        result.setShininess(m_fShininess);
        
        // Textures
        int textureCount = 0;
        URL fileLocation = null;
        // lots of dereferencing, I know....
        String currentFolder = m_pCollada.getFileLocation().toString().substring(0, m_pCollada.getFileLocation().toString().lastIndexOf('/') + 1);
        try
        {
            if (m_DiffuseImageFilename != null && m_DiffuseImageFilename.size() > 0)
            {
                for (int i = 0; i < m_DiffuseImageFilename.size(); ++i)
                {
                    fileLocation = new URL(currentFolder + m_DiffuseImageFilename.get(i));
                    result.setTexture(fileLocation, textureCount);
                    textureCount++;
                }
            }

            boolean bNormalMapped = (m_NormalMapImageFilename != null && m_NormalMapImageFilename.length() > 0);
            boolean bSpecularMapped = (m_SpecularImageFilename != null && m_SpecularImageFilename.length() > 0);

            if (bNormalMapped)
            {
                fileLocation = new URL(currentFolder + m_NormalMapImageFilename);
                result.setTexture(fileLocation, 1);
                textureCount++;
            }
            if (bSpecularMapped)
            {
                fileLocation = new URL(currentFolder + m_SpecularImageFilename);
                result.setTexture(fileLocation, 2);
                textureCount++;
            }

            if (m_EmissiveImageFilename != null && m_EmissiveImageFilename.length() > 0)
            {
                fileLocation = new URL(currentFolder + m_EmissiveImageFilename);
                result.setTexture(fileLocation, textureCount);
                textureCount++;
            }

            if (m_AmbientImageFilename != null && m_AmbientImageFilename.length() > 0)
            {
                fileLocation = new URL(currentFolder + m_AmbientImageFilename);
                result.setTexture(fileLocation, textureCount);
                textureCount++;
            }

            // Shaders if necessary
            if (bNormalMapped && bSpecularMapped)
            {
                // WORLD MANAGER STRIKES AGAIN!
                if (m_pCollada != null && m_pCollada.getPScene() != null)
                    result.setShader(new NormalAndSpecularMapShader(m_pCollada.getPScene().getWorldManager(), m_pCollada.getFileLocation()));
                else
                    Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Unable to retrieve worldmanager, shaders unset. PColladaMaterial.java : 217");
            }
            else if (bNormalMapped)
            {
                // WORLD MANAGER STRIKES AGAIN!
                if (m_pCollada != null && m_pCollada.getPScene() != null) // BEWARE THE HARDCODED NUMBER BELOW!
                    result.setShader(new NormalMapShader(m_pCollada.getPScene().getWorldManager(), 0.2f));
                else
                    Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Unable to retrieve worldmanager, shaders unset. PColladaMaterial.java : 217");

            }
        }
        catch (MalformedURLException ex)
        {
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Malformed url! : " + ex.getMessage());
        }
        
        // transparency
        switch (m_nTransparencyMode)
        {
//            case 0:
//                result.setAlphaState(PMeshMaterial.AlphaTransparencyType.RGB_ZERO);
//                result.setTransparencyColor(m_TransparentColor.toColorRGBA());
//                break;
            case 1:
                result.setAlphaState(PMeshMaterial.AlphaTransparencyType.A_ONE);
                result.setTransparencyColor(m_TransparentColor.toColorRGBA());
                break;
            default:
                result.setAlphaState(PMeshMaterial.AlphaTransparencyType.NO_TRANSPARENCY);
                break;
        }

        // HACK : Default COLLADA to have no backface culling
        result.setCullFace(Face.None);

        return result;
    }

    //  Processes a color or Texture.
    //  Returns String containing the image name of the texture assigned to the attribute.
    //  If an empty string is returned, pColor is filled in with the color of the attribute.
    private String processColorOrTexture(CommonColorOrTextureType pAttribute, PColladaColor pColor)
    {
        if (pAttribute == null)
            return null;
        if (pAttribute.getTexture() != null && pAttribute.getTexture().size() > 0)
        {
            Texture pTexture = pAttribute.getTexture().get(0);

            String textureSamplerName = pTexture.getTexture();

            String textureFilename = getTextureFilename(textureSamplerName);
            //  Comment out the two lines below and uncomment the return statement
            // in order to use relative paths
            String shortTextureFilename = FileUtils.getShortFilename(textureFilename);
            return shortTextureFilename;
            //return textureFilename;
        }
        else if (pAttribute.getColor() != null)
        {
            List<Double> c = pAttribute.getColor().getValues();

            pColor.Red   = c.get(0).floatValue();
            pColor.Green = c.get(1).floatValue();
            pColor.Blue  = c.get(2).floatValue();
            pColor.Alpha = c.get(3).floatValue();

            return null;
        }

        return null;
    }

    private List<String> processDiffuseColorsOrTextures(CommonColorOrTextureType diffuse, PColladaColor color)
    {
        if (diffuse == null)
            return null; 
        
        ArrayList<String> result = new ArrayList<String>();
        
        if (diffuse.getTexture() != null)
        {
            // for each texture
            for (int i = 0; i < diffuse.getTexture().size(); ++i)
            {
                Texture pTexture = diffuse.getTexture().get(i);

                String textureSamplerName = pTexture.getTexture();

                String textureFilename = getTextureFilename(textureSamplerName);

                String shortTextureFilename = FileUtils.getShortFilename(textureFilename);
                
                //result.add(textureFilename); <-- uncomment this line and comment out the one below to use relative paths
                result.add(shortTextureFilename);
            }
            return result;
        }
        else if (diffuse.getColor() != null)
        {
            List<Double> c = diffuse.getColor().getValues();

            color.Red   = c.get(0).floatValue();
            color.Green = c.get(1).floatValue();
            color.Blue  = c.get(2).floatValue();
            color.Alpha = c.get(3).floatValue();

            return null;
        }

        return null;
    }

    //  Processes a float attribute.
    private float processFloatAttribute(CommonFloatOrParamType pAttribute)
    {
        if (pAttribute == null)
            return(0.0f);

        float fValue = (float)pAttribute.getFloat().getValue();

        return(fValue);
    }

    private void processTransparent(CommonTransparentType transparentType, CommonFloatOrParamType transparency)
    {
        // calculate color / texture if any
        m_TransparentColor = new PColladaColor();
        String alphaMap = processColorOrTexture(transparentType, m_TransparentColor);
        if (alphaMap != null) // this means we are not using the color.. currently not support
        {
            Logger.getLogger(this.getClass().toString()).log(Level.WARNING, "Transparency mapping is not currently supported by this loader.");
            m_TransparentColor.set(1.0f, 1.0f, 1.0f, 1.0f);
        }
        // determine transparency information
        if (transparentType != null && transparentType.getOpaque().equals(FxOpaqueEnum.A_ONE))
            m_nTransparencyMode = 1;
        else if (transparentType != null && transparentType.getOpaque().equals(FxOpaqueEnum.RGB_ZERO))
            m_nTransparencyMode = 0;
        else // could be no transparency or using default behavior
        {
            if (transparency != null)
            {
                m_nTransparencyMode = 1; // A_ONE
            }
            else // No transparency
            {
                m_nTransparencyMode = -1;
            }
        }
        
        if (m_nTransparencyMode >= 0) // some kind of transparency
        {
            m_fTransparency = (float) ((transparency != null) ? transparency.getFloat().getValue() : 1.0f);
        }
    }


    private String getTextureFilename(String samplerName)
    {
        CommonNewparamType pSamplerParamType = getImageNewParamType(samplerName);
        if (pSamplerParamType == null)
            return null;

        FxSampler2DCommon pSampler2D = pSamplerParamType.getSampler2D();
        if (pSampler2D == null)
            return null;

        String surfaceName = pSampler2D.getSource();

        CommonNewparamType pSurfaceParamType = getImageNewParamType(surfaceName);
        if (pSurfaceParamType == null)
            return null;

        FxSurfaceCommon pSurfaceCommon = pSurfaceParamType.getSurface();
        if (pSurfaceCommon == null)
            return null;

        List<FxSurfaceInitFromCommon> pInitFroms = pSurfaceCommon.getInitFroms();
        if (pInitFroms == null)
            return null;

        FxSurfaceInitFromCommon pSurface;

        for (int i=0; i<pInitFroms.size(); i++)
        {
            pSurface = (FxSurfaceInitFromCommon)pInitFroms.get(i);

            if (pSurface.getValue() instanceof Image)
            {
                Image pImage = (Image)pSurface.getValue();

                return(pImage.getInitFrom());
            }
        }

        return null;
    }

    private CommonNewparamType getImageNewParamType(String name)
    {
        List<Object> imagesAndNewParams = m_pProfileCommon.getImagesAndNewparams();
        CommonNewparamType pNewParamType;

        for (int i=0; i<imagesAndNewParams.size(); i++)
        {
            pNewParamType = (CommonNewparamType)imagesAndNewParams.get(i);

            if (pNewParamType.getSid().equals(name))
                return(pNewParamType);
        }

        return(null);
    }

    
    
    private ColorRGBA buildColorRGBA(PColladaColor pColor)
    {
        ColorRGBA pColorRGBA = null;
        if (pColor == null)
            pColorRGBA = new ColorRGBA();
        else
            pColorRGBA = new ColorRGBA(pColor.Red, pColor.Green, pColor.Blue, pColor.Alpha);

        return(pColorRGBA);
    }

    //  Gets the image filename for the Emissive channel.
    public String getEmissiveImageFilename()
    {
        return(m_EmissiveImageFilename);
    }

    //  Gets the image filename for the Ambient channel.
    public String getAmbientImageFilename()
    {
        return(m_AmbientImageFilename);
    }

    //  Gets the image filename for the Diffuse channel.
    public List<String> getDiffuseImageFilename()
    {
        return(m_DiffuseImageFilename);
    }
   
    //  Gets the image filename for the Specular channel.
    public String getSpecularImageFilename()
    {
        return(m_SpecularImageFilename);
    }

    //  Gets the image filename for the Reflective channel.
    public String getReflectiveImageFilename()
    {
        return(m_ReflectiveImageFilename);
    }

    //  Gets the image filename for the BumpMap channel.
    public String getBumpMapImageFilename()
    {
        return(m_BumpMapImageFilename);
    }

    //  Gets the image filename for the NormalMap channel.
    public String getNormalMapImageFilename()
    {
        return(m_NormalMapImageFilename);
    }

}




