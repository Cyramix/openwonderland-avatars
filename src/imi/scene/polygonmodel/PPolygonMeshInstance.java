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
import com.jme.scene.SharedMesh;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.PMeshMaterialCombo;
import imi.scene.polygonmodel.parts.PMeshMaterialStates;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;
import imi.scene.utils.PRenderer;
import imi.scene.utils.TextureInstaller;
import java.net.URL;

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
    
//    // JMonkey\LWJGL MaterialState
//    protected MaterialState             m_matState          = null;
    
    /** Material **/
    protected PMeshMaterialCombo  m_material        = null;
    /** Use geometry material or not **/
    protected boolean  m_bUseGeometryMaterial       = true; 
    /** convenient state wrapper **/
    protected PMeshMaterialStates m_pmaterialStates = null;
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
     * @param bApplyMaterial
     */
    public PPolygonMeshInstance(String name, PPolygonMesh geometry, PMatrix origin, PScene pscene, boolean bApplyMaterial)
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
            if (bApplyMaterial == true)
                applyMaterial();
        }
    }
   
    
    /**
     * Initialize the material and texture state objects for this mesh.
     * This is relies on grabbing the render manager from the world manager.
     * @param pscene A pscene with a world manager reference
     */
    private void initializeStates(PScene pscene)
    {
        m_textureState = (TextureState) pscene.getWorldManager().getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        m_pmaterialStates = new PMeshMaterialStates(pscene.getWorldManager().getRenderManager());
        //m_matState = (MaterialState) pscene.getWorldManager().getRenderManager().createRendererState(RenderState.RS_MATERIAL);
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

        if (m_instance.getTarget().getIndexBuffer() == null)
            return null;
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
            logger.severe("Why is the texture null? Location is " + path.toString());
            return;
        }
        
        if (getTextureInstaller() == null)
        {
            logger.severe("Why is the texture installer null? "+path.toString()+"  "+this);
            return;   
        }
        
        // find the texture unit (multi texture support)
        int texUnit = -1;
        PMeshMaterial meshMat = m_material.getMaterial();
        for (int i = 0; i < m_geometry.getNumberOfTextures(); i++)
        {
            if (meshMat.getTexture(i) != null)
            {
                if (meshMat.getTexture(i).getImageLocation().equals(path))
                {
                    texUnit = i;
                    break;
                }
            }
        }
        // did we find the texture?
        if (texUnit != -1)
        {
            TextureState ts = getTextureInstaller().installTexture(tex, texUnit);
            if (ts != null)
            {
                getSharedMesh().setRenderState(ts);
                setTextureInstaller(null);
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
    }

    public void setMaterial(PMeshMaterialCombo material) 
    {
        m_material = material;
        // No longer using the geometry's material
        m_bUseGeometryMaterial = false;
    }
    
    public void setMaterial(PMeshMaterial material) 
    {
        m_material = new PMeshMaterialCombo(material, null);
        // No longer using the geometry's material
        m_bUseGeometryMaterial = false;
    }

    public void applyShader()
    {
        applyShader(0);
    }

    public void applyShader(int index)
    {
        if (m_material == null) // no use in trying
        {
            logger.severe("Cannot apply shaders without a material!");
            return; // Abort
        }
        PMeshMaterial meshMat = m_material.getMaterial();
        if (meshMat.getShader(index) != null)
            meshMat.getShader(index).applyToMesh(this);
        else
            logger.severe("Requested shader was null! (index was " + index + ", shader array length was " + meshMat.getShaders().length);
    }
    
    public void applyMaterial()
    {
        // Debugging / Diagnostic output
//        logger.info("applying material, I am " + getName() + " here is the call stack:");
//        Thread.dumpStack();
        if (m_material == null) // We better be using the geometry's material
        {
            if (m_bUseGeometryMaterial == true)
                m_material = new PMeshMaterialCombo(m_geometry.getMaterialRef(), null);
            else // cant do too much now
                return;
        }
        PMeshMaterial meshMat = m_material.getMaterial();
        m_pmaterialStates.configureStates(meshMat);
        m_pmaterialStates.applyToGeometry(m_instance);
        
        // Textures
        m_textureState.setEnabled(true);
        // determine number of needed textures
        int numNeeded = meshMat.getNumberOfRelevantTextures();
        TextureMaterialProperties[] texProps = new TextureMaterialProperties[numNeeded];
        for (int i = 0; i < numNeeded; ++i)
            texProps[i] = meshMat.getTexture(i);
        
        setTextureInstaller(new TextureInstaller(texProps, m_textureState));
        // Debugging / Diagnostic outpout
//        logger.info("[-----------------START------------------]");
//        Thread.dumpStack();
//        logger.info("[" + this + " - " + getName() + "]");
//        logger.info("[Textures being loaded are: ]");
//        for (int i = 0; i < numNeeded; ++i)
//            logger.info(texProps[i].getImageLocation().toString());
//        logger.info("[-----------------END------------------]");

        // TODO add functionality and data to this instance if we want 
        // to handle textures differently than the geometry
        boolean bNeedToUseTextureInstaller = false;
        for (int i = 0; i < m_geometry.getNumberOfTextures(); i++)
        {
            if (meshMat.getTexture(i) != null)
            {
                bNeedToUseTextureInstaller = true;
                if (m_PScene.isUseRepository() == true)
                {
                    // Send SharedAsset request to the PScene
                    SharedAsset texture = new SharedAsset(m_PScene.getRepository(),
                            new AssetDescriptor(SharedAssetType.Texture, meshMat.getTexture(i).getImageLocation()));
                    m_PScene.loadTexture(texture, this);
                }
                else
                    m_textureState.setTexture(  m_PScene.loadTexture(meshMat.getTexture(i).getImageLocation()), i);
            }
        }
        if (!bNeedToUseTextureInstaller || m_PScene.isUseRepository() == false)
            setTextureInstaller(null);
        m_instance.setRenderState(m_textureState);
        if (meshMat.getShader() != null)
            meshMat.getShader().applyToMesh(this);
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

    // Used for instrumentation
    private void setTextureInstaller(TextureInstaller textureInstaller) {
        m_textureInstaller = textureInstaller;
//        if (textureInstaller == null)
//        {
//            System.err.println("NULLIFYING the texture installer");
//            System.err.println("My name is " + getName());
//            Thread.dumpStack();
//
//        }
        // Debugging / Diagnostic output
//        logger.severe("-----------Setting texture installer to " + textureInstaller);
//        logger.severe("-----------this is " + this);
//        Thread.dumpStack();
    }

    private TextureInstaller getTextureInstaller() {
        // Debugging / Diagnostic output
//        logger.severe("-----------Retrieving texture installer...");
//        logger.severe("-----------this is " + this);
//        Thread.dumpStack();
        return m_textureInstaller;
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
    
    public boolean isWaitingOnTextures()
    {
        if (getTextureInstaller() != null)
            return getTextureInstaller().isComplete();
        return false;
    }
    
    /**
     * Set the target pscene used for loading textures, etc
     * @param pscene
     */
    public void setPScene(PScene pscene)
    {
        m_PScene = pscene;
    }
    
}
