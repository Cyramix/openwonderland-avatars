package imi.scene.polygonmodel.parts;

import com.jme.bounding.BoundingSphere;
import com.jme.renderer.Renderer;
import com.jme.scene.SharedMesh;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderManager;

/**
 * This class serves as a wrapper for the assorted render states that are 
 * needed in order to fully apply material properties. Currently this includes
 * a wireframe state, a material state, and a cull state.
 * @author Ronald E Dahlgren
 */
public class PMeshMaterialStates 
{
    private CullState       m_cullState         = null;
    //private TextureState    m_textureState      = null; <-- disabled until time for testing is available
    private MaterialState   m_materialState     = null;
    private WireframeState  m_wireframeState    = null;
    private BlendState      m_blendState        = null;
    private LightState      m_lightState        = null;
    private ZBufferState    m_bufferState       = null;
    
    /**
     * Construct a new instance with no states initialized.
     */
    public PMeshMaterialStates()
    {
        
    }
    
    /**
     * Construct a new instance and use the provided rendermanager to
     * create all the required render states.
     * @param rm
     */
    public PMeshMaterialStates(RenderManager rm)
    {
        createStates(rm);
    }
    
    public boolean createStates(RenderManager rm)
    {
        boolean retVal = true;
        
        m_cullState = (CullState)rm.createRendererState(RenderState.RS_CULL);
        //m_textureState = (TextureState)rm.createRendererState(RenderState.RS_TEXTURE);
        m_materialState = (MaterialState)rm.createRendererState(RenderState.RS_MATERIAL);
        m_wireframeState = (WireframeState)rm.createRendererState(RenderState.RS_WIREFRAME);
        m_blendState = (BlendState)rm.createRendererState(RenderState.RS_BLEND);
        m_lightState = (LightState)rm.createRendererState(RenderState.RS_LIGHT);
        m_bufferState = (ZBufferState)rm.createRendererState(RenderState.RS_ZBUFFER);
        
        if (m_cullState != null && m_blendState != null &&
            m_materialState != null && m_wireframeState != null &&
            m_lightState !=  null && m_bufferState != null)
            retVal = true;
        else
            retVal = false;
        
        return retVal;
    }
    
    /**
     * Apply all non-null states to the passed in node
     * @param jmeNode
     */
    public void applyToGeometry(SharedMesh jmeMesh)
    {
        if (m_cullState != null)
            jmeMesh.setRenderState(m_cullState);
        if (m_materialState != null)
            jmeMesh.setRenderState(m_materialState);
        if (m_wireframeState != null)
            jmeMesh.setRenderState(m_wireframeState);
        if (m_blendState != null)
        {
            jmeMesh.setRenderState(m_blendState);
            if (m_blendState.isEnabled() == true)
            {
                /**
                 * The following hacks are necessary because of this problem:
                 * The environments have baked light maps and only look correct
                 * with lighting disabled. Unfortunately, jME transparency only
                 * functions when the lighting is enabled AND some light is
                 * interacting with the geometry
                 */

                if (m_lightState != null)
                {
                    m_lightState.setTwoSidedLighting(true);
                    m_lightState.setEnabled(true);
                    
                    jmeMesh.setRenderState(m_lightState);
                }
                else
                    Logger.getLogger(this.getClass().toString()).log(Level.WARNING,
                            "Transparency Used but no light state available!");
                
                m_bufferState.setEnabled(true);
                m_bufferState.setWritable(false);
                m_bufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
                jmeMesh.setRenderState(m_bufferState);
                jmeMesh.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
            }
            else
            {
                m_bufferState.setEnabled(true);
                m_bufferState.setWritable(true);
                m_bufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
                jmeMesh.setRenderState(m_bufferState);
                BlendState bs = (BlendState) jmeMesh.getRenderState(RenderState.RS_BLEND);
                bs.setEnabled(false);
                bs.setBlendEnabled(false);
                bs.setTestEnabled(false);
                jmeMesh.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
            }
        }
        jmeMesh.updateRenderState();
    }
    
    /**
     * Use the provided material to configure the states
     * @param material
     */
    public void configureStates(PMeshMaterial material)
    {
        // Material state
        m_materialState.setEnabled(true);
        m_materialState.setDiffuse(material.getDiffuse());
        m_materialState.setAmbient(material.getAmbient()); 
        m_materialState.setSpecular(material.getSpecular());
        m_materialState.setEmissive(material.getEmissive());
        m_materialState.setShininess(material.getShininess());
        m_materialState.setColorMaterial(material.getColorMaterial());
        m_materialState.setMaterialFace(material.getMaterialFace());
        
        // Cull state
        m_cullState.setEnabled(true);
        m_cullState.setCullFace(material.getCullFace());
        
        // Wireframe state
        m_wireframeState.setEnabled(material.isWireframeEnabled());
        m_wireframeState.setAntialiased(material.isWireframeAntiAliased());
        m_wireframeState.setLineWidth(material.getWireframeLineWidth());
        m_wireframeState.setFace(material.getWireframeFace());
        
        // blend state
        if (material.getAlphaState() != PMeshMaterial.AlphaTransparencyType.NO_TRANSPARENCY)
        {
            m_blendState.setBlendEnabled(true);
            m_blendState.setEnabled(true);
        }
        else
        {
            m_blendState.setBlendEnabled(false);
            m_blendState.setEnabled(false);
        }
        
        if (material.getAlphaState() == PMeshMaterial.AlphaTransparencyType.A_ONE)
        {
            m_blendState.setEnabled(true);
            m_blendState.setBlendEnabled(true);
            m_blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            m_blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            m_blendState.setTestEnabled(true);
            m_blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
        }
        else if (material.getAlphaState() == PMeshMaterial.AlphaTransparencyType.RGB_ZERO)
        {
            m_blendState.setEnabled(true);
            m_blendState.setBlendEnabled(true);
            m_blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            m_blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            m_blendState.setTestEnabled(true);
            m_blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
        }
        
        
        
    }
}
