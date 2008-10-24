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
    private PColladaColor   m_TransparentColor          = new PColladaColor();
    private float           m_fTransparency             = 0.0f;
    private float           m_fIndexOfRefraction        = 0.0f;

    /** Treated as tertiary diffuse map     **/
    private String          m_EmissiveImageFilename     = "";
    /** Treated as secondary diffuse map    **/
    private String          m_AmbientImageFilename      = "";
    /** Treated as the default diffuse map  **/
    private String          m_DiffuseImageFilename      = "";   
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
        m_DiffuseImageFilename = processColorOrTexture(pBlinn.getDiffuse(), m_DiffuseColor);
        m_SpecularImageFilename = processColorOrTexture(pBlinn.getSpecular(), m_SpecularColor);
        
        //m_NormalMapImageFilename = processColorOrTexture(pBlinn., m_AmbientColor)
                
        m_fShininess = processFloatAttribute(pBlinn.getShininess());

        m_ReflectiveImageFilename = processColorOrTexture(pBlinn.getReflective(), m_ReflectiveColor);
        m_fReflectivity = processFloatAttribute(pBlinn.getReflectivity());
        processTransparent(pBlinn.getTransparent(), m_TransparentColor);
        m_fTransparency = processFloatAttribute(pBlinn.getTransparency());
        m_fIndexOfRefraction = processFloatAttribute(pBlinn.getIndexOfRefraction());
    }

    //  Initializes the ColladaMaterial based on a phong effect.
    public void initialize(String name, Phong pPhong)
    {
        m_Name = name;
        
        m_EmissiveImageFilename = processColorOrTexture(pPhong.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pPhong.getAmbient(), m_AmbientColor);
        m_DiffuseImageFilename = processColorOrTexture(pPhong.getDiffuse(), m_DiffuseColor);
        m_SpecularImageFilename = processColorOrTexture(pPhong.getSpecular(), m_SpecularColor);
        m_fShininess = processFloatAttribute(pPhong.getShininess());

        m_ReflectiveImageFilename = processColorOrTexture(pPhong.getReflective(), m_ReflectiveColor);
        m_fReflectivity = processFloatAttribute(pPhong.getReflectivity());
        processTransparent(pPhong.getTransparent(), m_TransparentColor);
        m_fTransparency = processFloatAttribute(pPhong.getTransparency());
        m_fIndexOfRefraction = processFloatAttribute(pPhong.getIndexOfRefraction());
    }

    //  Initializes the ColladaMaterial based on a lambert effect.
    public void initialize(String name, Lambert pLambert)
    {
        m_Name = name;

        m_EmissiveImageFilename = processColorOrTexture(pLambert.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pLambert.getAmbient(), m_AmbientColor);
        m_DiffuseImageFilename = processColorOrTexture(pLambert.getDiffuse(), m_DiffuseColor);

        m_ReflectiveImageFilename = processColorOrTexture(pLambert.getReflective(), m_ReflectiveColor);
        m_fReflectivity = processFloatAttribute(pLambert.getReflectivity());
        processTransparent(pLambert.getTransparent(), m_TransparentColor);
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
        
        if (m_DiffuseImageFilename != null && m_DiffuseImageFilename.length() > 0)
        {
            result.setTexture(FileUtils.findTextureFile(m_DiffuseImageFilename), 0);
            textureCount++;
        }
        
        boolean bNormalMapped = (m_NormalMapImageFilename != null && m_NormalMapImageFilename.length() > 0);
        boolean bSpecularMapped = (m_SpecularImageFilename != null && m_SpecularImageFilename.length() > 0);
        
        if (bNormalMapped)
        {
            result.setTexture(FileUtils.findTextureFile(m_NormalMapImageFilename), 1);
            textureCount++;
        }
        if (bSpecularMapped)
        {
            result.setTexture(FileUtils.findTextureFile(m_SpecularImageFilename), 2);
            textureCount++;
        }
        
        if (m_EmissiveImageFilename != null && m_EmissiveImageFilename.length() > 0)
        {
            result.setTexture(FileUtils.findTextureFile(m_EmissiveImageFilename), textureCount);
            textureCount++;
        }
        
        if (m_AmbientImageFilename != null && m_AmbientImageFilename.length() > 0)
        {
            result.setTexture(FileUtils.findTextureFile(m_AmbientImageFilename), textureCount);
            textureCount++;
        }
        
        // Shaders if necessary
        if (bNormalMapped && bSpecularMapped)
        {
            // WORLD MANAGER STRIKES AGAIN!
            if (m_pCollada != null && m_pCollada.getPScene() != null)
                result.setShader(new NormalAndSpecularMapShader(m_pCollada.getPScene().getWorldManager()));
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
        // We don't really support spec mapping without normal mapping

        return result;
    }

    //  Processes a color or Texture.
    //  Returns String containing the image name of the texture assigned to the attribute.
    //  If an empty string is returned, pColor is filled in with the color of the attribute.
    private String processColorOrTexture(CommonColorOrTextureType pAttribute, PColladaColor pColor)
    {
        if (pAttribute == null)
            return("");

        if (pAttribute.getTexture() != null)
        {
            Texture pTexture = pAttribute.getTexture();

            String textureSamplerName = pTexture.getTexture();
            String textureTexCoord = pTexture.getTexcoord();

            String textureFilename = getTextureFilename(textureSamplerName);
//                    m_pCollada.getTextureFilename(textureSamplerName);
            //  Should lookup the Texture to determine the filename.

            //  Returns a string containing just the short filename.
            String shortTextureFilename = FileUtils.getShortFilename(textureFilename);

            return(shortTextureFilename);
        }
        else if (pAttribute.getColor() != null)
        {
            List<Double> c = pAttribute.getColor().getValues();

            pColor.Red   = c.get(0).floatValue();
            pColor.Green = c.get(1).floatValue();
            pColor.Blue  = c.get(2).floatValue();
            pColor.Alpha = c.get(3).floatValue();

            return("");
        }

        return("");
    }

    //  Processes a float attribute.
    private float processFloatAttribute(CommonFloatOrParamType pAttribute)
    {
        if (pAttribute == null)
            return(0.0f);

        float fValue = (float)pAttribute.getFloat().getValue();

        return(fValue);
    }

    private void processTransparent(CommonTransparentType transparentType, PColladaColor pColor)
    {
        if (transparentType.getOpaque() == FxOpaqueEnum.A_ONE)
            pColor.set(0.0f, 0.0f, 0.0f, 1.0f);
        else if (transparentType.getOpaque() == FxOpaqueEnum.RGB_ZERO)
            pColor.set(0.0f, 0.0f, 0.0f, 0.0f);
    }


 public String getTextureFilename(String samplerName)
    {
        CommonNewparamType pSamplerParamType = getImageNewParamType(samplerName);
        if (pSamplerParamType == null)
            return("");

        FxSampler2DCommon pSampler2D = pSamplerParamType.getSampler2D();
        if (pSampler2D == null)
            return("");

        String surfaceName = pSampler2D.getSource();

        CommonNewparamType pSurfaceParamType = getImageNewParamType(surfaceName);
        if (pSurfaceParamType == null)
            return("");

        FxSurfaceCommon pSurfaceCommon = pSurfaceParamType.getSurface();
        if (pSurfaceCommon == null)
            return("");

        List<FxSurfaceInitFromCommon> pInitFroms = pSurfaceCommon.getInitFroms();
        if (pInitFroms == null)
            return("");

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

        return("");
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

    private String[] buildTextures()
    {
        int textureCount = calculateTextureCount();
        if (textureCount == 0)
            return(null);
        
        String []textures = new String[textureCount];
        int textureIndex = 0;
        
        if (m_DiffuseImageFilename.length() > 0)
        {
            textures[textureIndex] = FileUtils.findTextureFile(m_DiffuseImageFilename);
            textureIndex++;
        }
        if (m_SpecularImageFilename.length() > 0)
        {
            textures[textureIndex] = FileUtils.findTextureFile(m_SpecularImageFilename);
            textureIndex++;
        }
        if (m_AmbientImageFilename.length() > 0)
        {
            textures[textureIndex] = FileUtils.findTextureFile(m_AmbientImageFilename);
            textureIndex++;
        }
        if (m_EmissiveImageFilename.length() > 0)
        {
            textures[textureIndex] = FileUtils.findTextureFile(m_EmissiveImageFilename);
            textureIndex++;
        }

        if (m_ReflectiveImageFilename.length() > 0)
        {
            textures[textureIndex] = FileUtils.findTextureFile(m_ReflectiveImageFilename);
            textureIndex++;
        }
       
        return(textures);
    }

    private int calculateTextureCount()
    {
        int textureCount = 0;

        if (m_EmissiveImageFilename.length() > 0)
            textureCount++;
        if (m_AmbientImageFilename.length() > 0)
            textureCount++;
        if (m_DiffuseImageFilename.length() > 0)
            textureCount++;
        if (m_SpecularImageFilename.length() > 0)
            textureCount++;

        if (m_ReflectiveImageFilename.length() > 0)
            textureCount++;

        return(textureCount);
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
    public String getDiffuseImageFilename()
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




