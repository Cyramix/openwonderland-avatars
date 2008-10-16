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
package imi.scene.polygonmodel;

import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.SharedMesh;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.loaders.scenebindings.sbConfigurationData;
import imi.loaders.scenebindings.sbScale;
import imi.loaders.scenebindings.sbTexture;
import imi.loaders.scenebindings.sbMaterial;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.PMeshMaterialCombo;
import imi.scene.utils.PRenderer;
import imi.scene.utils.TextureInstaller;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains a jME SharedMesh with a target TriMesh belonging to
 * a PPolygonMesh elsewhere in the system. This class is typically instantiated
 * by a PScene for a given PPolygonMesh. The PScene ties up all the appropriate
 * connections under the hood.
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PPolygonMeshInstance extends PNode
{
    // The owning PScene
    protected PScene              m_PScene    = null;
    // JMonkey instance object
    protected SharedMesh          m_instance  = null;
    
    protected boolean             m_bUniformTexCoords = false;
    
    // Geometry reference
    protected PPolygonMesh        m_geometry  = null; // if this reference will be assigned again outside of the constructor then m_instance will need an update
    
    // Shader state. This must be allocated carefully (on render thread presumably)
    protected GLSLShaderObjectsState    m_shaderState       = null;//new LWJGLShaderObjectsState();
    
    // Textures
    protected TextureInstaller          m_textureInstaller  = null; // Needs to know how many texture units will be used
    protected TextureState              m_textureState      = null;
    
    // JMonkey\LWJGL MaterialState
    protected MaterialState             m_matState          = null;
    
    // MaterialCombo reference
    protected PMeshMaterialCombo  m_material  = null;
    protected boolean  m_bUseGeometryMaterial = true; // by default the instance will use the geometry's material
    
    /**
     * This constructor copies all the data of the other instance and inserts
     * this instance into the scene graph as a child of the provided parent.
     * @param parent
     * @param pPolygonMeshInstance
     */
    public PPolygonMeshInstance(PNode parent, PPolygonMeshInstance pPolygonMeshInstance)
    {
        super(pPolygonMeshInstance.getName(), parent, null, new PTransform(pPolygonMeshInstance.getTransform()));

        m_PScene = pPolygonMeshInstance.m_PScene;
        initializeStates(m_PScene);
        
        m_geometry = pPolygonMeshInstance.getGeometry();
        m_geometry.adjustReferenceCount(1);

        m_instance = new SharedMesh(getName(), m_geometry.getGeometry());

        m_material = new PMeshMaterialCombo(m_geometry.getMaterialCopy(), null);
        applyMaterial();
    }

    /**
     * This constructor sets the name to the one provided, uses the provided
     * PPolygonMesh as reference geometry, has a local transform of <code>origin</code>,
     * and belongs to the provided PScene.
     * @param name
     * @param geometry
     * @param origin
     * @param pscene
     */
    public PPolygonMeshInstance(String name, PPolygonMesh geometry, PMatrix origin, PScene pscene) 
    {
        super(name, null, null, new PTransform(origin));
        
        if (geometry != null)
        {
            m_PScene = pscene;
            initializeStates(pscene);
            
            m_geometry = geometry;
            m_geometry.adjustReferenceCount(1);

            m_instance = new SharedMesh(getName(), m_geometry.getGeometry());

            m_material = new PMeshMaterialCombo(m_geometry.getMaterialCopy(), null);
            m_instance.setTextureCoords(m_geometry.getGeometry().getTextureCoords(0));
            setUniformTexCoords(m_geometry.isUniformTexCoords());
                
            applyMaterial();
        }
    }
   
    public sbConfigurationData load_createConfigurationData()
    {
        boolean bNeeded = false; // true if this configuration is needed
        
        // Allocate memory
        sbConfigurationData result = new sbConfigurationData();
        
        // Scale
        Vector3f scale = getTransform().getLocalMatrix(false).getScaleVector();
        if (!scale.equals(Vector3f.UNIT_XYZ))
        {
            bNeeded = true;
            result.setScale(new sbScale());
            result.getScale().set(scale.x, scale.y, scale.z);
        }
        
        if(!isUseGeometryMaterial())
        {
            sbMaterial configMat = new sbMaterial();
            
            // Shaders
            // TODO: Convert to the new system
//            if (getMaterialRef().getMaterial().getFragShader() != null && getMaterialRef().getMaterial().getVertShader() != null)
//            {
//                bNeeded = true;
//                sbShaderPair pair = new sbShaderPair();
//                pair.setFragmentShaderPath(getMaterialRef().getMaterial().getFragShader().getPath());
//                pair.setVertexShaderPath(getMaterialRef().getMaterial().getVertShader().getPath());
//                configMat.setShaderPair(pair);
//            }

            // Textures
            List<sbTexture> textureList = new ArrayList<sbTexture>();
            URL[] textures = getMaterialRef().getMaterial().getTextures();
            for (int i = 0; i < 8; i++)
            {
                if (textures[i] != null)
                {
                    bNeeded = true;
                    sbTexture texture = new sbTexture();
                    texture.setTextureUnit(i);
                    texture.setPath(textures[i].getPath());
                    textureList.add(texture);
                }
                else
                    break;
            }
            if (!textureList.isEmpty())
                configMat.setTextureFiles(textureList);
            
            result.setMaterial(configMat);
        }
        
        if (bNeeded)
            return result;
        else
            return null;
    }

    /**
     * Initialize the material and texture state objects for this mesh.
     * This is relies on grabbing the render manager from the world manager.
     * @param pscene A pscene with a world manager reference
     */
    private void initializeStates(PScene pscene)
    {
        m_textureState = (TextureState) pscene.getWorldManager().getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        m_matState = (MaterialState) pscene.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
    }
    
    public SharedMesh getSharedMesh()
    {
        return m_instance;
    }
    
    // called when we faltten the hierarchy on submitTransform in PSCene
    public SharedMesh updateSharedMesh()
    {
        // TODO push shader data 
        
        // can we optimize this with dirty booleans?
        PMatrix world = getTransform().getWorldMatrix(false);
        m_instance.setLocalRotation(world.getRotation());
        m_instance.setLocalTranslation(world.getTranslation());
        m_instance.setLocalScale(world.getScaleVector());
        
        return m_instance;
    }
    
    /**
     * Use with caution!
     * @return m_textureState
     */
    public TextureState getTextureState()
    {
        return m_textureState;
    }
    
    @Override
    public void draw(PRenderer renderer)
    {
        if (m_geometry != null)
        {
            // Set world origin
            PMatrix origin       = getTransform().getWorldMatrix(false);
            renderer.setOrigin(origin);
            
            // Draw geometry
            m_geometry.draw(renderer);
        }
                
        // TODO is this needed? .... draw mesh kids - mesh that belongs to a model...
        for (int i = 0; i < getChildrenCount(); i++) 
            getChild(i).drawAll(renderer);
    }
    
    public GLSLShaderObjectsState getShaderState()
    {
        return m_shaderState;
    }
    
    public void setShaderState(GLSLShaderObjectsState shader) 
    {
        m_shaderState = shader;
        
        if (m_instance == null)
            m_instance = new SharedMesh(getName(), m_geometry.getGeometry());
        
        m_instance.setRenderState(m_shaderState);
        m_shaderState.setEnabled(true);
        
        // TODO are we missing more stuff???
        
        setDirty(true, true); // TODO is this needed?
    }
    
    public synchronized void installTexture(Texture tex, URL path)
    {
        //System.out.println("Received " + path.toString());
        if (tex == null)
        {
            System.out.println("Why is the texture null? Location is " + path.toString());
            return;
        }
        
        if (m_textureInstaller == null)
        {
            System.out.println("Why is the texture installer null?");
            return;   
        }
        
        // find the texture unit (multi texture support)
        int texUnit = -1;
        PMeshMaterial meshMat = m_material.getMaterial();
        for (int i = 0; i < m_geometry.getNumberOfTextures(); i++)
        {
            if (meshMat.getTexture(i) != null)
            {
                if (meshMat.getTexture(i).equals(path))
                    texUnit = i;
            }
        }
        // did we find the texture?
        if (texUnit != -1)
        {
            TextureState ts = m_textureInstaller.installTexture(tex, texUnit);
            if (ts != null)
            {
                getSharedMesh().setRenderState(ts);
                m_textureInstaller = null;
            }
        }
    }

    public boolean isUseGeometryMaterial() 
    {
        return m_bUseGeometryMaterial;
    }

    /***
     * Warning: by setting the material to the geometry's material you
     * loose the previous material's reference.
     * @param bUseGeometryMaterial
     */
    public void setUseGeometryMaterial(boolean bUseGeometryMaterial) 
    {
        m_bUseGeometryMaterial = bUseGeometryMaterial;
        
        if (m_bUseGeometryMaterial)
        {
            m_material.setMaterial(m_geometry.getMaterialCopy());
            applyMaterial();
        }
        else 
        {
            if (m_material != null)
                applyMaterial();
            else
            {
                setMaterial(m_geometry.getMaterialRef());
            }
        }
    }

    public void setMaterial(PMeshMaterialCombo material) 
    {
        m_material = material;
        if (!m_bUseGeometryMaterial)
            applyMaterial();
    }
    
    public void setMaterial(PMeshMaterial material) 
    {
        m_material = new PMeshMaterialCombo(material, null);
        if (!m_bUseGeometryMaterial)
            applyMaterial();
    }
    
    protected void applyMaterial()
    {
        PMeshMaterial meshMat = m_material.getMaterial();
        // Material
        m_matState.setEnabled(true);
        m_matState.setDiffuse(meshMat.getDiffuse());
        m_matState.setAmbient(meshMat.getAmbient());
        m_matState.setSpecular(meshMat.getSpecular());
        m_matState.setEmissive(meshMat.getEmissive());
        m_matState.setShininess(meshMat.getShininess());
        m_matState.setColorMaterial(meshMat.getColorMaterial());
        m_matState.setMaterialFace(meshMat.getMaterialFace());
        m_instance.setRenderState(m_matState);
        
        // Textures
        m_textureState.setEnabled(true);
        m_textureInstaller = new TextureInstaller(m_geometry.getNumberOfTextures(), m_textureState);
        // TODO add functionality and data to this instance if we want 
        // to handle textures differently than the geometry
        boolean bNeedToUseTextureInstaller = false;
        for (int i = 0; i < m_geometry.getNumberOfTextures(); i++)
        {
            if (meshMat.getTexture(i) != null && meshMat.getTexture(i).getPath().length() > 0)
            {
                bNeedToUseTextureInstaller = true;
                if (m_PScene.isUseRepository())
                {
                    // Send SharedAsset request to the PScene
                    SharedAsset texture = new SharedAsset(m_PScene.getRepository(),
                            new AssetDescriptor(SharedAssetType.Texture, new File(meshMat.getTexture(i).getPath())));
                    m_PScene.loadTexture(texture, this);
                }
                else
                    m_textureState.setTexture(  m_PScene.loadTexture(meshMat.getTexture(i))  ,   i   );
            }
        }
        if (!bNeedToUseTextureInstaller)
            m_textureInstaller = null;
        m_instance.setRenderState(m_textureState);
        if (meshMat.getShader() != null)
        {
            meshMat.getShader().applyToMesh(this);
        }
        // Shaders
        // Send SharedAsset request to the PScene
        // This shader state will not be shared (can we fix that?)
        // The shader state will be created and loaded on the render thread (can we get this to work another way?)
        //File [] shaderPairPath = new File[2];
        //shaderPairPath[0] = meshMat.getVertShader();
        //shaderPairPath[1] = meshMat.getFragShader();
        //if (shaderPairPath[0] != null && shaderPairPath[1] != null)
        //{
        //    SharedAsset shaders = new SharedAsset(m_PScene.getRepository(),
        //            new AssetDescriptor(SharedAssetType.ShaderPair, shaderPairPath));
        //    shaders.setInitializer(m_shaderInitializer);
        //    m_PScene.loadShaders(shaders, this);
        //}
        
        ////////////////////////////////////////////////////////////////////
//        m_shaderState       = new LWJGLShaderObjectsState();
//        m_shaderState.load( PPolygonMeshInstanceFileUtils.rootPath + "assets/shaders/shaderDeform_3.vert"),
//                            PPolygonMeshInstanceFileUtils.rootPath + "assets/shaders/wood.frag"));
//        setShaderState(m_shaderState);
    }
    
    protected void load_processConfiguration(sbConfigurationData config) 
    {
        PMatrix local = getTransform().getLocalMatrix(true);
        local.setIdentity();
        
        // Scale
        if (config.getScale() != null);
            local.setScale(config.getScale().getVec3());
            
        if (config.getMaterial() != null)
        {
            PMeshMaterial mat = new PMeshMaterial();
            sbMaterial configMat = config.getMaterial();
            
            // Textures
            if (configMat.getTextureFiles() != null)
            {
                List<sbTexture> textures = configMat.getTextureFiles();
                for (sbTexture tex : textures)
                    mat.setTexture(tex.getPath(), tex.getTextureUnit());
            }

            // Shaders
            // TODO: Convert to the new system
//            if (configMat.getShaderPair() != null)
//            {
//                sbShaderPair shades = configMat.getShaderPair();
//                mat.setVertShader(new File(shades.getVertexShaderPath()));
//                mat.setFragShader(new File(shades.getFragmentShaderPath()));
//            }
            
            // Set the material (applies it)
            setMaterial(mat);
            setUseGeometryMaterial(false);
        }
    }
    
    public PMeshMaterialCombo getMaterialRef() 
    {
        return m_material;
    }
    
    public PMeshMaterialCombo getMaterialCopy() 
    {
        return new PMeshMaterialCombo(m_material);
    }
    
    public PPolygonMesh getGeometry()
    {
        return m_geometry;
    }
    
    /**
     * Decrements the associated geometry reference count
     */
    public void decrementGeometryReference()
    {
        m_geometry.adjustReferenceCount(-1);
    }
    
    @Override
    public boolean isDirty()
    {
        return super.isDirty() || m_geometry.isDirty();
    }

    private void setUniformTexCoords(boolean uniformTexCoords)
    {
        
        m_bUniformTexCoords = uniformTexCoords;
        if (m_bUniformTexCoords == true)
        {
            // Copy texture coordinates to the set number of units
            for (int i = 0; i < m_geometry.getNumberOfTextures(); i++)
            {
                m_instance.copyTextureCoordinates(0, i, 1.0f);
            }
        }
    
    }
    

    
}
