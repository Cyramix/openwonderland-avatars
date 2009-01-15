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
package imi.environments;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides the basis for collada environments. It allows for the use
 * of specialized loading and initialization code with environment loading.
 * @author Ronald E Dahlgren
 */
public class ColladaEnvironment extends Entity
{
    /** The root of the jME graph **/
    protected Node      m_jmeRoot = null;
    /** Used to determine whether loading has completed **/
    protected Boolean   m_finishedLoading = Boolean.FALSE;
    /** WorldManager HOOOOOOOO! **/
    protected WorldManager  m_wm = null;
    /** **/
    private PScene scene = null;

    /**
     * Primary constructor. This initializes and loads the environment. The provided
     * world manager is used to create a pscene, and once the loading has finished
     * the scene is then initialized.
     * @param wm The world manager; used in PScene construction and referencing the repository
     * @param relativePath The relative path to the collada file containing the environment
     * @param name The name of the land!
     */
    public ColladaEnvironment(WorldManager wm, String relativePath, String name)
    {
        super(name);
        m_wm = wm;
        // create and load the environment
        Repository repo = (Repository)wm.getUserData(Repository.class);
        // TODO !!
        AssetDescriptor descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, relativePath);
        SharedAsset worldAsset = new SharedAsset(repo, descriptor, null);
        worldAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 0, name, null));

        scene = new PScene(m_wm);
        scene.setUseRepository(false); // Synchronous loading requested
        scene.addModelInstance(worldAsset, new PMatrix()); // Add it to the scene
        
        SceneGraphConvertor convertor = new SceneGraphConvertor();
        m_jmeRoot = convertor.convert(scene);
        //m_jmeRoot = new Node("NodeRoot");
        // Now assign the rendering component
        RenderComponent rc = m_wm.getRenderManager().createRenderComponent(m_jmeRoot);
        this.addComponent(RenderComponent.class, rc);
        // set some default rendering behavior
        setDefaultRenderStates();
        // add ourselves to the world manager
        m_wm.addEntity(this);
 
    }

    /**
     * Construct a new instance! This initializes and loads the environment. The provided
     * world manager is used to create the pscene, and once the loading has finished
     * the scene is then initialized.
     * @param wm The world manager; used in PScene construction and referencing the repository
     * @param relativePath The relative path to the collada file containing the environment
     * @param name The name of the land!
     */
    public ColladaEnvironment(WorldManager wm, URL relativePath, String name)
    {
        super(name);
        m_wm = wm;
        // create and load the environment
        Repository repo = (Repository)wm.getUserData(Repository.class);
        AssetDescriptor descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA, relativePath);
        SharedAsset worldAsset = new SharedAsset(repo, descriptor, null);
        worldAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 0, name, null));

        scene = new PScene(m_wm);
        scene.setUseRepository(false); // Synchronous loading requested
        scene.addModelInstance(worldAsset, new PMatrix());

        SceneGraphConvertor convertor = new SceneGraphConvertor();
        m_jmeRoot = convertor.convert(scene);
        //m_jmeRoot = new Node("NodeRoot");
        // Now assign the rendering component
        RenderComponent rc = m_wm.getRenderManager().createRenderComponent(m_jmeRoot);
        this.addComponent(RenderComponent.class, rc);
        // set some default rendering behavior
        setDefaultRenderStates();
        // add ourselves to the world manager
        m_wm.addEntity(this);

    }

    /**
     * Set up the default render states for the environment.
     */
    public void setDefaultRenderStates()
    {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) m_wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) m_wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setAmbient(ColorRGBA.magenta);
        matState.setDiffuse(ColorRGBA.magenta);
        matState.setEmissive(ColorRGBA.magenta);
        matState.setSpecular(ColorRGBA.magenta);
        matState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        matState.setColorMaterial(MaterialState.ColorMaterial.None);
        matState.setEnabled(true);
  
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setTwoSidedLighting(false);
        ls.setEnabled(true);
        
        // Cull State
        CullState cs = (CullState) m_wm.getRenderManager().createRendererState(RenderState.RS_CULL);      
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        
        // Push 'em down the pipe
        m_jmeRoot.setRenderState(matState);
        m_jmeRoot.setRenderState(buf);
        m_jmeRoot.setRenderState(cs);
        m_jmeRoot.setRenderState(ls);
        nullifyColorBuffers();
        visitNodes();
        m_jmeRoot.updateRenderState();
    }
    
    /**
     * This method traverses the heirarchy and applies some render states.
     */
    private void visitNodes()
    {
        FastList<Spatial> queue = new FastList<Spatial>();
        queue.addAll(m_jmeRoot.getChildren());
        
        while (queue.isEmpty() == false)
        {
            Spatial current = queue.removeFirst();
            //current.setRenderState(rs);
            
            if (current instanceof SharedMesh)
            {
                MaterialState matState = (MaterialState) (current).getRenderState(RenderState.RS_MATERIAL);
                matState.setColorMaterial(MaterialState.ColorMaterial.Emissive);
                
                TextureState texState = (TextureState) current.getRenderState(RenderState.RS_TEXTURE);
                if (texState == null) // weirdness
                {
                    // Debugging / Diagnostic output
                    Logger.getLogger(ColladaEnvironment.class.getName()).log(Level.SEVERE,
                            "Weirdness has occured on line 139, ColladaEnvironment.java - BUG FIX TEAM, ATTACK!");
                }
            }
            
            if (current instanceof Node)
            {
                // add all children
                if (((Node)current).getChildren() == null)
                    continue;
                for (int i = 0; i < ((Node)current).getChildren().size(); ++i)
                    queue.add(((Node)current).getChild(i));
            }
        }
        
    }

    /**
     * Visit each node in the graph and nullify any trimesh color buffers.
     */
    private void nullifyColorBuffers()
    {
        FastList<Spatial> queue = new FastList<Spatial>();
        queue.addAll(m_jmeRoot.getChildren());
        
        while (queue.isEmpty() == false)
        {
            Spatial current = queue.removeFirst();
            
            if (current instanceof SharedMesh)
            {
                TriMesh geometry = ((SharedMesh)current).getTarget();
                geometry.setColorBuffer(null);
            }
            
            if (current instanceof Node)
            {
                // add all children
                if (((Node)current).getChildren() == null)
                    continue;
                for (int i = 0; i < ((Node)current).getChildren().size(); ++i)
                    queue.add(((Node)current).getChild(i));
            }
        }
        
    }
}
