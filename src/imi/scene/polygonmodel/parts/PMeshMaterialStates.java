package imi.scene.polygonmodel.parts;

import com.jme.scene.SharedMesh;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
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
        
        if (m_cullState != null /*&& m_textureState != null*/ &&
                m_materialState != null && m_wireframeState != null)
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
    }
}
