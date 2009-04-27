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

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.SharedMesh;
import com.jme.scene.state.GLSLShaderObjectsState;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.PMeshMaterialStates;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;
import imi.scene.utils.PRenderer;
import imi.scene.utils.TextureInstaller;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This class contains a jME SharedMesh with a target TriMesh belonging to
 * a PPolygonMesh elsewhere in the system. This class is typically instantiated
 * by a PScene for a given PPolygonMesh. The PScene ties up all the appropriate
 * connections under the hood.
 * @author Lou Hayt
 * @author Ron Dahlgren
 */
public class PPolygonMeshInstance extends PNode implements Serializable
{
    // The owning PScene
    protected transient PScene              m_PScene    = null;
    // JMonkey instance object
    protected transient SharedMesh          m_instance  = null;
    
    protected boolean             m_bUniformTexCoords = false;
    
    // Geometry reference
    protected PPolygonMesh        m_geometry  = null; // if this reference will be assigned again outside of the constructor then m_instance will need an update
    
    // Shader state. This must be allocated carefully (on render thread presumably)
    protected transient GLSLShaderObjectsState    m_shaderState       = null;

    /** Material **/
    protected PMeshMaterial  m_material             = null;
    /** Use geometry material or not **/
    protected boolean  m_bUseGeometryMaterial       = true; 
    /** convenient state wrapper **/
    protected transient PMeshMaterialStates m_materialStates = null;

    /** Used in calculations **/
    private transient Vector3f m_translationBufferVector    = new Vector3f();
    private transient Vector3f m_scaleBufferVector          = new Vector3f();
    private transient Matrix3f m_rotationBuffer             = new Matrix3f();

    /** Is it a collidable mesh? **/
    protected boolean collidable = true;

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
        
        if (pPolygonMeshInstance.isUseGeometryMaterial() == false)
            m_material = pPolygonMeshInstance.getMaterialCopy();
        else
            m_material = pPolygonMeshInstance.getGeometry().getMaterialCopy();
//        applyMaterial();
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

            m_material = new PMeshMaterial(m_geometry.getMaterialCopy());
            
            if (bApplyMaterial == true)
                applyMaterial();
        }
    }
    public PPolygonMeshInstance(PPolygonMeshInstance other, PScene pscene, boolean bApplyMaterial)
    {
        super(other.getName(), null, null, new PTransform(other.getTransform()));
        m_PScene = pscene;
        initializeStates(m_PScene);
        m_geometry = other.m_geometry;
        m_instance = new SharedMesh(other.getName(), m_geometry.getGeometry());
        m_material = new PMeshMaterial(other.m_material);

        if (bApplyMaterial)
            applyMaterial();
    }
   
    
    /**
     * Initialize the material and texture state objects for this mesh if it has
     * not already been done. This is relies on grabbing the render manager
     * from the world manager via the provided PScene.
     * @param pscene A pscene with a world manager reference
     */
    public void initializeStates(PScene pscene)
    {
        if (m_materialStates == null)
            m_materialStates = new PMeshMaterialStates(pscene.getWorldManager().getRenderManager());
    }
    
    public SharedMesh getSharedMesh()
    {
        return m_instance;
    }

    
    /**
     * Return the instance with its transform information updated to reflect the
     * current position.
     * @return
     */
    public SharedMesh updateSharedMesh()
    {
        // TODO push shader data
        // can we optimize this with dirty booleans?
        PMatrix world = getTransform().getWorldMatrix(false);
        world.normalizeCP(); // <-- do not change. See addendum for details.
        world.getTranslation(m_translationBufferVector);
        world.getScale(m_scaleBufferVector);
        world.getRotation(m_rotationBuffer);

        m_instance.setLocalRotation(m_rotationBuffer);
        m_instance.setLocalScale(m_scaleBufferVector);
        m_instance.setLocalTranslation(m_translationBufferVector);
        

        if (m_instance.getTarget().getIndexBuffer() == null)
            return null;
        return m_instance;
    }
    
    @Override
    public void draw(PRenderer renderer)
    {
        if (m_geometry != null && collidable)
        {
            // Set world origin
            PMatrix origin       = getTransform().getWorldMatrix(false);
            renderer.setOrigin(origin);
            
            // Draw bounding volume
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
        if (shader == null)
        {
            if (m_shaderState != null)
                m_shaderState.setEnabled(false);
            m_shaderState = null;
            m_instance.updateRenderState();
        }
        else
        {
            m_shaderState = shader;
        
            if (m_instance == null)
                m_instance = new SharedMesh(getName(), m_geometry.getGeometry());
        
            m_instance.setRenderState(m_shaderState);
            m_shaderState.setEnabled(true);
        }
        
        setDirty(true, true); // TODO is this needed?
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

    public void setMaterial(PMeshMaterial material) 
    {
        m_material = material;
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
        
        if (m_material.getShader(index) != null)
            m_material.getShader(index).applyToMesh(this);
        else
            logger.severe("Requested shader was null! (index was " + index + ", shader array length was " + m_material.getShaders().length);
    }

    /**
     * This method applies the material the this mesh instance. It requires that a
     * material exist (or it's geometry have a valid material) as well as a material
     * states collection.
     */
    public void applyMaterial()
    {
        // Debugging / Diagnostic output
//        logger.info("applying material, I am " + getName() + " here is the call stack:");
//        Thread.dumpStack();
        if (m_material == null) // We better be using the geometry's material
        {
            if (m_bUseGeometryMaterial == true)
                m_material = new PMeshMaterial(m_geometry.getMaterialRef());
            else // cant do too much now
                return;
        }
        
        m_materialStates.configureStates(m_material);

        int numNeeded = m_material.getNumberOfRelevantTextures();

        if (m_PScene != null)
        {
            for (int i = 0; i < numNeeded; ++i)
            {
                TextureMaterialProperties texProps = m_material.getTexture(i);
                if (texProps != null)
                    m_materialStates.setTexture(texProps.loadTexture(m_PScene.getRepository()), i);
                else
                    logger.warning("Null texture property found in material for index " + i + " mesh name is " + getName());
            }
        }
        else
            logger.warning("Unable to load textures due to a null PScene reference.");


        m_materialStates.applyToGeometry(m_instance);
        
        if (m_material.getShader() != null)
            m_material.getShader().applyToMesh(this);
        else
            setShaderState(null);

        m_instance.updateRenderState();
    }
    
    public PMeshMaterial getMaterialRef() 
    {
        return m_material;
    }
    
    public PMeshMaterial getMaterialCopy() 
    {
        return new PMeshMaterial(m_material);
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
    
    /**
     * Set the target pscene used for loading textures, etc
     * @param pscene
     */
    public void setPScene(PScene pscene)
    {
        m_PScene = pscene;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public void setCollidable(boolean collidable) {
        this.collidable = collidable;
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
        m_instance = new SharedMesh(getName(), getGeometry().getGeometry());
        m_translationBufferVector = new Vector3f();
        m_scaleBufferVector = new Vector3f();
        m_rotationBuffer = new Matrix3f();
    }
}
