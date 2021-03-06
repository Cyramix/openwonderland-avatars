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
package org.collada.xml_walker;

import org.collada.colladaschema.Technique;
import java.util.List;
import imi.scene.polygonmodel.PMeshMaterial;
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

import imi.loaders.Collada;
import imi.repository.Repository;
import imi.shader.programs.NormalAndSpecularMapShader;
import imi.shader.programs.NormalMapShader;
import java.net.MalformedURLException;
import java.net.URL;
import javolution.util.FastTable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 *
 * @author Chris Nagle
 */
public class PColladaEffect
{
    private Collada         m_pCollada                  = null;
    private ProfileCOMMON   m_pProfileCommon            = null;

    private String          m_effectIdentifier          = null;

    private PColladaColor   m_EmissiveColor             = new PColladaColor();
    private PColladaColor   m_AmbientColor              = new PColladaColor();
    private PColladaColor   m_DiffuseColor              = new PColladaColor();
    private PColladaColor   m_SpecularColor             = new PColladaColor();

    private int             m_shininess                 = 0;
    private PColladaColor   m_ReflectiveColor           = new PColladaColor();

    /** Transparency information **/
    private PColladaColor   m_TransparentColor          = new PColladaColor();
    private int             m_nTransparencyMode         = -1; // -1 no transparency, 0 represents RGB_ZERO, 1 represents A_ONE

    /** Emissive map     **/
    private String          m_EmissiveImageFilename     = null;
    /** Ambient map    **/
    private String          m_AmbientImageFilename      = null;
    /** Treated as the default diffuse map  **/
    private List<String>    m_DiffuseImageFilename      = new FastTable<String>();
    /** Interpreted as a specular map       **/
    private String          m_SpecularImageFilename     = null;
    private String          m_ReflectiveImageFilename   = null;

    /** Bump map (currently unsupported)    **/
    private String          m_BumpMapImageFilename      = null;
    /** Normal map **/
    private String          m_NormalMapImageFilename    = null;



    //  Constructor.
    public PColladaEffect(Collada pCollada, ProfileCOMMON pProfileCommon)
    {
        m_pCollada = pCollada;
        m_pProfileCommon = pProfileCommon;
    }



    //  Gets the Material's ID
    public String getEffectIdentifier()
    {
        return m_effectIdentifier;
    }



    //  Initializes the ColladaMaterial based on a blinn effect.
    public void initialize(String effectIdentifier, Blinn pBlinn)
    {
        m_effectIdentifier = effectIdentifier;

        m_EmissiveImageFilename = processColorOrTexture(pBlinn.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pBlinn.getAmbient(), m_AmbientColor);
        
        
        m_DiffuseImageFilename = processDiffuseColorsOrTextures(pBlinn.getDiffuse(), m_DiffuseColor);
        
        m_SpecularImageFilename = processColorOrTexture(pBlinn.getSpecular(), m_SpecularColor);
        
        //m_NormalMapImageFilename = processColorOrTexture(pBlinn., m_AmbientColor)
        
                
        m_shininess = (int)processFloatAttribute(pBlinn.getShininess());

        m_ReflectiveImageFilename = processColorOrTexture(pBlinn.getReflective(), m_ReflectiveColor);
        
        processTransparent(pBlinn.getTransparent(), pBlinn.getTransparency());
        
        
    }

    //  Initializes the ColladaMaterial based on a phong effect.
    public void initialize(String effectIdentifier, Phong pPhong)
    {
        m_effectIdentifier = effectIdentifier;
        
        m_EmissiveImageFilename = processColorOrTexture(pPhong.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pPhong.getAmbient(), m_AmbientColor);
        
        m_DiffuseImageFilename = processDiffuseColorsOrTextures(pPhong.getDiffuse(), m_DiffuseColor);
        
        m_SpecularImageFilename = processColorOrTexture(pPhong.getSpecular(), m_SpecularColor);
        m_shininess = (int)processFloatAttribute(pPhong.getShininess());

        m_ReflectiveImageFilename = processColorOrTexture(pPhong.getReflective(), m_ReflectiveColor);

        processTransparent(pPhong.getTransparent(), pPhong.getTransparency());
        
    }

    //  Initializes the ColladaMaterial based on a lambert effect.
    public void initialize(String effectIdentifier, Lambert pLambert)
    {
        m_effectIdentifier = effectIdentifier;

        m_EmissiveImageFilename = processColorOrTexture(pLambert.getEmission(), m_EmissiveColor);
        m_AmbientImageFilename = processColorOrTexture(pLambert.getAmbient(), m_AmbientColor);
        
        m_DiffuseImageFilename = processDiffuseColorsOrTextures(pLambert.getDiffuse(), m_DiffuseColor);
        
        m_ReflectiveImageFilename = processColorOrTexture(pLambert.getReflective(), m_ReflectiveColor);
        
        processTransparent(pLambert.getTransparent(), pLambert.getTransparency());
        
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
                        
                        // OWL issue 41: make sure to check for elements in a different
                        // namespace
                        Node textureNode = null;
                        if (textureList.getLength() == 0) {
                            for (Node child = someElement.getFirstChild(); child != null; child = child.getNextSibling()) {
                                if (child instanceof Element && "texture".equals(child.getLocalName())) {
                                    textureNode = child;
                                    break;
                                }
                            }
                        } else {
                            // only grab texture zero and assign it as the normal map
                            textureNode = textureList.item(0);
                        }

                        // only grab texture zero and assign it as the normal map
                        if (textureNode != null)
                        {
                            String stringID = textureNode.getAttributes().getNamedItem("texture").getTextContent();
                            m_NormalMapImageFilename = getTextureFilename(stringID);
                            if (m_NormalMapImageFilename == null) // oh no! try something!
                            {
                                // HACK trying to save the normal map
                                //System.out.println("  HACK  HACK  HACK   original string:   " + stringID);
                                stringID = stringID.substring(stringID.indexOf("file"), stringID.lastIndexOf("-"));
                                //System.out.println("  HACK  HACK  HACK   new string:   " + stringID);
                                List<Image> list = m_pCollada.getLibraryImages().getImages();
                                for (Image image : list)
                                {
                                    if (image.getId().equals(stringID))
                                    {
                                        m_NormalMapImageFilename = image.getInitFrom();
                                        break;
                                    }
                                }
                            }
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
        PMeshMaterial result = null;
        if (m_effectIdentifier == null)
            result = new PMeshMaterial("Untitled Material : PColladaMaterial.java :: createMeshMaterial()");
        else
            result = new PMeshMaterial(m_effectIdentifier);
        // Colors!
        result.setEmissive(buildColorRGBA(m_EmissiveColor, 0));
        result.setAmbient(buildColorRGBA(m_AmbientColor, 1));
        result.setSpecular(buildColorRGBA(m_SpecularColor, 2));
        result.setDiffuse(buildColorRGBA(m_DiffuseColor, 3));
        
        // Shininess
        result.setShininess(m_shininess);


        boolean bNormalMapped = (m_NormalMapImageFilename != null);
        boolean bSpecularMapped = (m_SpecularImageFilename != null);
        // Textures
        if (m_pCollada.isLoadingTextures())
        {
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
                        fileLocation = new URL(currentFolder + locateSizedImage(m_DiffuseImageFilename.get(i), "512x512"));
                        if (!FileUtils.checkURL(fileLocation))
                            throw new RuntimeException("URL failed to open stream " + fileLocation);
                        result.setTexture(textureCount, fileLocation);
                        textureCount++;
                    }
                }

                if (bNormalMapped)
                {
                    fileLocation = new URL(currentFolder + locateSizedImage(m_NormalMapImageFilename, "512x512"));
                    if (!FileUtils.checkURL(fileLocation))
                        throw new RuntimeException("URL failed to open stream " + fileLocation);
                    result.setTexture(1,fileLocation);
                    textureCount++;
                }
                if (bSpecularMapped)
                {
                    fileLocation = new URL(currentFolder + locateSizedImage(m_SpecularImageFilename, "512x512"));
                    if (!FileUtils.checkURL(fileLocation))
                        throw new RuntimeException("URL failed to open stream " + fileLocation);
                    result.setTexture(2,fileLocation);
                    textureCount++;
                }

                if (m_EmissiveImageFilename != null && m_EmissiveImageFilename.length() > 0)
                {
                    fileLocation = new URL(currentFolder + locateSizedImage(m_EmissiveImageFilename, "512x512"));
                    if (!FileUtils.checkURL(fileLocation))
                        throw new RuntimeException("URL failed to open stream " + fileLocation);
                    result.setTexture(textureCount, fileLocation);
                    textureCount++;
                }

                if (m_AmbientImageFilename != null && m_AmbientImageFilename.length() > 0)
                {
                    fileLocation = new URL(currentFolder + locateSizedImage(m_AmbientImageFilename, "512x512"));
                    if (!FileUtils.checkURL(fileLocation))
                        throw new RuntimeException("URL failed to open stream " + fileLocation);
                    result.setTexture(textureCount, fileLocation);
                    textureCount++;
                }
            }
            catch (MalformedURLException ex)
            {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Malformed url! : " + ex.getMessage());
            }
        }

        // Shaders if necessary
        Repository repo = (Repository) m_pCollada.getPScene().getWorldManager().getUserData(Repository.class);
        if (bNormalMapped && bSpecularMapped)
        {
            // WORLD MANAGER STRIKES AGAIN!
            if (m_pCollada.getPScene() != null)
                result.setDefaultShader(repo.newShader(NormalAndSpecularMapShader.class));
            else
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Unable to retrieve worldmanager, shaders unset. PColladaMaterial.java : 217");
        }
        else if (bNormalMapped)
        {
            // WORLD MANAGER STRIKES AGAIN!
            if (m_pCollada.getPScene() != null) // BEWARE THE HARDCODED NUMBER BELOW!
                result.setDefaultShader(repo.newShader(NormalMapShader.class));
            else
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Unable to retrieve worldmanager, shaders unset. PColladaMaterial.java : 217");

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

            pColor.red   = c.get(0).floatValue();
            pColor.green = c.get(1).floatValue();
            pColor.blue  = c.get(2).floatValue();
            pColor.alpha = c.get(3).floatValue();

            return null;
        }

        return null;
    }

    private List<String> processDiffuseColorsOrTextures(CommonColorOrTextureType diffuse, PColladaColor color)
    {
        if (diffuse == null)
            return null; 
        
        FastTable<String> result = new FastTable<String>();
        
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

            color.red   = c.get(0).floatValue();
            color.green = c.get(1).floatValue();
            color.blue  = c.get(2).floatValue();
            color.alpha = c.get(3).floatValue();

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
                m_nTransparencyMode = 1; // A_ONE
            else // No transparency
                m_nTransparencyMode = -1;
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

        return null;
    }

    
    
    private ColorRGBA buildColorRGBA(PColladaColor pColor, int element)
    {
        ColorRGBA result = null;
        if (pColor == null)
        {
            switch (element)
            {
                case 0:
                    result = ColorRGBA.black;
                    break;
                case 1:
                    result = ColorRGBA.white;
                    break;
                case 2:
                    result = ColorRGBA.black;
                    break;
                case 3:
                    result = ColorRGBA.white;
                    break;
            }
        }
        else
        {
            if ((pColor.red == 1.0f && pColor.green == 1.0f && pColor.blue == 1.0f) &&
                    (element == 0 || element == 2))
                result = ColorRGBA.black;
            else
                result = new ColorRGBA(pColor.red, pColor.green, pColor.blue, pColor.alpha);
        }

        return result;
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

    private String locateSizedImage(String fullFileName, String sizeSuffix)
    {
        return fullFileName;

        // WIP
//        String result = null;
//        // first, grab the file extension
//        String fileExtension = fullFileName.substring(fullFileName.lastIndexOf("."));
//        result = fullFileName.substring(0, fullFileName.lastIndexOf(".")) + "-" + sizeSuffix + fileExtension;
//        return result;
    }

}




