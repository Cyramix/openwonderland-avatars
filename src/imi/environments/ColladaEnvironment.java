package imi.environments;

import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
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
         
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setTwoSidedLighting(true);
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
}
