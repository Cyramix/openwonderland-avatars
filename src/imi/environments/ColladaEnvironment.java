package imi.environments;

import com.jme.image.Texture;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.loaders.repository.SharedAsset;
import imi.scene.PMatrix;
import imi.scene.PScene;
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
    
    /**
     * Primary constructor. This initializes and loads the environment. The provided
     * pscene is used to request the loading action, and once the loading has finished
     * the scene is then initialized.
     * @param freshPScene This should be a newly constructed PScene.
     * @param environment 
     * @param name The name to refer to the environment by
     */
    public ColladaEnvironment(WorldManager wm, SharedAsset environment, String name)
    {
        super(name);
        m_wm = wm;
        PScene scene = new PScene(m_wm);
        scene.setUseRepository(false);
        scene.addModelInstance(environment, new PMatrix());
        
        SceneGraphConvertor convertor = new SceneGraphConvertor();
        m_jmeRoot = convertor.convert(scene);
        //m_jmeRoot = new Node("DasRoot");
        // Now assign the rendering component
        RenderComponent rc = m_wm.getRenderManager().createRenderComponent(m_jmeRoot);
        this.addComponent(RenderComponent.class, rc);
        // set some default rendering behavior
        setDefaultRenderStates();
        // add ourselves to the world manager
        m_wm.addEntity(this);
 
        //sphereTestCode();
    }
    
    public Node getSceneRoot()
    {
        return m_jmeRoot;
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
        ls.setEnabled(false);
        
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
        m_jmeRoot.updateRenderState();
        
    }
    
    /**
     * This method traverses the heirarchy and applies the specified renderstates to
     * 
     * @param rs
     */
    private void forceRenderState(RenderState rs)
    {
        FastList<Spatial> queue = new FastList<Spatial>();
        queue.addAll(m_jmeRoot.getChildren());
        
        while (queue.isEmpty() == false)
        {
            Spatial current = queue.removeFirst();
            current.setRenderState(rs);
            
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
        materialState.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        materialState.setDiffuse(new ColorRGBA(0.1f, 0.5f, 0.8f, 1.0f));
        materialState.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        materialState.setShininess(64.0f);
        materialState.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        materialState.setEnabled(true);

        TextureState textures = (TextureState)m_wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        Texture diffuseMap = TextureManager.loadTexture("assets/models/collada/environments/MPK20/Fern.png", Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.Bilinear);
        
        textures.setTexture(diffuseMap);
        
        sphere.setRenderState(textures);
        sphere.updateRenderState();
        // IMPORTANT: this is used to handle the internal sphere faces when
        // setting them to transparent, try commenting this line to see what
        // happens
        materialState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);

        sphere.setRenderState(materialState);
        sphere.updateRenderState();

        m_jmeRoot.attachChild(sphere);

        // to handle transparency: a BlendState
        // an other tutorial will be made to deal with the possibilities of this
        // RenderState
        BlendState alphaState = (BlendState) m_wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        alphaState.setEnabled(true);
        alphaState.setBlendEnabled(true);
        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alphaState.setTestEnabled(true);
        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
        
        
                 
        final PointLight light = new PointLight();
        light.setAmbient(ColorRGBA.white);
        light.setDiffuse(ColorRGBA.white);
        light.setSpecular(ColorRGBA.white);
        light.setLocation(new Vector3f(100.0f, 100.0f, 100.0f));
        light.setEnabled(true);
        
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setTwoSidedLighting(true);
        ls.attach(light);
        ls.setEnabled(true);
        
        sphere.setRenderState(alphaState);
        sphere.setRenderState(ls);
        sphere.updateRenderState();

        m_jmeRoot.updateRenderState();
        // IMPORTANT: since the sphere will be transparent, place it
        // in the transparent render queue!
        sphere.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
    }
}
