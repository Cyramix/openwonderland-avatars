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

import com.jme.image.Texture;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TexCoords;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
import java.net.URL;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides the basis for collada environments
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

    private PScene scene = null;

    /**
     * Primary constructor. This initializes and loads the environment. The provided
     * pscene is used to request the loading action, and once the loading has finished
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
        AssetDescriptor descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA_Model, relativePath);
        SharedAsset worldAsset = new SharedAsset(repo, descriptor, null);
        worldAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 0, name, null));

        PScene scene = new PScene(m_wm);
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
    
    public ColladaEnvironment(WorldManager wm, URL relativePath, String name)
    {
        super(name);
        m_wm = wm;
        // create and load the environment
        Repository repo = (Repository)wm.getUserData(Repository.class);
        AssetDescriptor descriptor = new AssetDescriptor(SharedAsset.SharedAssetType.COLLADA_Model, relativePath);
        SharedAsset worldAsset = new SharedAsset(repo, descriptor, null);
        worldAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 0, name, null));

        PScene scene = new PScene(m_wm);
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

    public Node getSceneRoot()
    {
        return m_jmeRoot;
    }

    public PScene getPScene() {
        return scene;
    }
    
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
     * This method traverses the heirarchy and applies the specified renderstates to
     * 
     * @param rs
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
                    System.out.println("Weirdness has occured on line 139, ColladaEnvironment.java - BUG FIX TEAM, ATTACK!");
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
    
    private void sphereTestCode()
    {
        // test code
        // our sphere
        final Sphere sphere = new Sphere("sphere", Vector3f.ZERO, 96, 96, 7f);

        // the sphere material taht will be modified to make the sphere
        // look opaque then transparent then opaque and so on
        MaterialState materialState = (MaterialState) m_wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        materialState.setAmbient(ColorRGBA.white);
        materialState.setDiffuse(ColorRGBA.white);
        materialState.setSpecular(ColorRGBA.white);
        materialState.setShininess(64.0f);
        materialState.setEnabled(true);

        TextureState textures = (TextureState)m_wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        
        Texture diffuseMap = TextureManager.loadTexture("assets/textures/checkerboard.png", Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
        Texture secondDiffuseMap = TextureManager.loadTexture("assets/textures/checkerboard.png", Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
        secondDiffuseMap.setApply(Texture.ApplyMode.Modulate);
        secondDiffuseMap.setWrap(Texture.WrapMode.Repeat);
        
        textures.setTexture(diffuseMap, 0);
        textures.setTexture(secondDiffuseMap, 1);
        
        sphere.copyTextureCoordinates(0, 1, 1.0f);
        
        TexCoords uv = sphere.getTextureCoords(0);
        
        float[] uvArray = new float[uv.coords.limit()];
        
        try
        {
            uv.coords.rewind();
            uv.coords.get(uvArray);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage() + " and I don't care.");
        }
        
        for (int i = 0; i < uvArray.length; ++i)
            uvArray[i] = uvArray[i];// * 5.8f;
        
        TexCoords newUV = TexCoords.makeNew(uvArray);
        sphere.setTextureCoords(newUV, 1);
        textures.setEnabled(true);
        
        sphere.setRenderState(textures);
        sphere.updateRenderState();
        
        materialState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);

        sphere.setRenderState(materialState);
        sphere.updateRenderState();

        m_jmeRoot.attachChild(sphere);

        // to handle transparency: a BlendState
        // an other tutorial will be made to deal with the possibilities of this
        // RenderState
//        BlendState alphaState = (BlendState) m_wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
//        alphaState.setEnabled(true);
//        alphaState.setBlendEnabled(true);
//        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
//        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
//        alphaState.setTestEnabled(true);
//        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
        
        
                 
        final PointLight light = new PointLight();
        light.setAmbient(ColorRGBA.white);
        light.setDiffuse(ColorRGBA.white);
        light.setSpecular(ColorRGBA.white);
        light.setLocation(new Vector3f(100.0f, 100.0f, 100.0f));
        light.setEnabled(true);
        
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setTwoSidedLighting(false);
        ls.attach(light);
        ls.setEnabled(true);
        
        //sphere.setRenderState(alphaState);
        sphere.setRenderState(ls);
        sphere.updateRenderState();

        m_jmeRoot.updateRenderState();
        // IMPORTANT: since the sphere will be transparent, place it
        // in the transparent render queue!
        sphere.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
    }
}
