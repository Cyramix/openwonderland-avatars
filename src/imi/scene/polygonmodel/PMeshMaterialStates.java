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
import com.jme.renderer.Renderer;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial.CullHint;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.GLSLShaderObjectsState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.wonderland.common.InternalAPI;

/**
 * This class serves as a wrapper for the assorted render states that are 
 * needed in order to fully apply material properties. Currently this includes
 * a wireframe state, a material state, and a cull state.
 * @author Ronald E Dahlgren
 */
public class PMeshMaterialStates 
{
    private final CullState       cullState;
    private final TextureState    textureState;
    private final MaterialState   materialState;
    private final WireframeState  wireframeState;
    private final BlendState      blendState;
    private final LightState      lightState;
    private final ZBufferState    bufferState;
    private final GLSLShaderObjectsState shaderState;

    /**
     * Construct a new instance and use the provided rendermanager to
     * create all the required render states.
     * @param rm
     */
    PMeshMaterialStates(RenderManager rm)
    {
        cullState = (CullState)rm.createRendererState(RenderState.StateType.Cull);
        textureState = (TextureState)rm.createRendererState(RenderState.StateType.Texture);
        materialState = (MaterialState)rm.createRendererState(RenderState.StateType.Material);
        wireframeState = (WireframeState)rm.createRendererState(RenderState.StateType.Wireframe);
        blendState = (BlendState)rm.createRendererState(RenderState.StateType.Blend);
        lightState = (LightState)rm.createRendererState(RenderState.StateType.Light);
        bufferState = (ZBufferState)rm.createRendererState(RenderState.StateType.ZBuffer);
        shaderState = (GLSLShaderObjectsState)rm.createRendererState(RenderState.StateType.GLSLShaderObjects);
    }
    
    /**
     * Construct a new material states object with null states. This is good
     * for headless tools to use.
     */
    PMeshMaterialStates()
    {
        cullState = null;
        textureState = null;
        materialState = null;
        wireframeState = null;
        blendState = null;
        lightState = null;
        bufferState = null;
        shaderState = null;
    
    }


    @InternalAPI
    public GLSLShaderObjectsState getShaderState() {
        return shaderState;
    }
    
    /**
     * Apply all non-null states to the passed in node
     * @param jmeNode
     */
    public void applyToGeometry(SharedMesh jmeMesh)
    {
        // XXX Temporary workaround for hand bounding volumes.
        jmeMesh.setCullHint(CullHint.Never);
        if (cullState != null)
            jmeMesh.setRenderState(cullState);
        if (materialState != null)
            jmeMesh.setRenderState(materialState);
        if (blendState != null)
        {
            jmeMesh.setRenderState(blendState);
            if (blendState.isEnabled() == true)
            {
                /**
                 * The following hacks are necessary because of this problem:
                 * The environments have baked light maps and only look correct
                 * with lighting disabled. Unfortunately, jME transparency only
                 * functions when the lighting is enabled AND some light is
                 * interacting with the geometry
                 */

                if (lightState != null)
                {
                    lightState.setTwoSidedLighting(true);
                    lightState.setEnabled(true);
                    
                    jmeMesh.setRenderState(lightState);
                }
                else
                    Logger.getLogger(this.getClass().toString()).log(Level.WARNING,
                            "Transparency Used but no light state available!");
                
                bufferState.setEnabled(true);
                bufferState.setWritable(true);
                bufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
                jmeMesh.setRenderState(bufferState);
                jmeMesh.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
            }
            else
            {
                bufferState.setEnabled(true);
                bufferState.setWritable(true);
                bufferState.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
                jmeMesh.setRenderState(bufferState);
                BlendState bs = (BlendState) jmeMesh.getRenderState(RenderState.RS_BLEND);
                bs.setEnabled(false);
                bs.setBlendEnabled(false);
                bs.setTestEnabled(false);
                jmeMesh.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
            }
        }
        // texture
        textureState.setEnabled(true);
        jmeMesh.setRenderState(textureState);
        // Shader
        jmeMesh.updateRenderState();
    }

    /**
     * Set the texture for the specified texture unit.
     * @param texture A texture
     * @param unit A positive integer less than the max supported texture units
     */
    public void setTexture(Texture texture, int unit)
    {
        if (unit < 0)
            throw new IllegalArgumentException("Unit was negative: " + unit);
        textureState.setTexture(texture, unit);
    }
    /**
     * Use the provided material to configure the states
     * @param material
     */
    public void configureStates(PMeshMaterial material)
    {
        // Material state
        materialState.setEnabled(true);
        materialState.setDiffuse(material.getDiffuseRef());
        materialState.setAmbient(material.getAmbientRef());
        materialState.setSpecular(material.getSpecularRef());
        materialState.setEmissive(material.getEmissiveRef());
        materialState.setShininess(material.getShininess());
        materialState.setColorMaterial(material.getColorMaterial());
        materialState.setMaterialFace(material.getMaterialFace());
        
        // Cull state
        cullState.setEnabled(true);
        cullState.setCullFace(material.getCullFace());
        
        // Wireframe state
        wireframeState.setEnabled(material.isWireframeEnabled());
        wireframeState.setAntialiased(material.isWireframeAntiAliased());
        wireframeState.setLineWidth(material.getWireframeLineWidth());
        wireframeState.setFace(material.getWireframeFace());
        
        // blend state
        if (material.getAlphaState() != PMeshMaterial.AlphaTransparencyType.NO_TRANSPARENCY)
        {
            blendState.setBlendEnabled(true);
            blendState.setEnabled(true);
        }
        else
        {
            blendState.setBlendEnabled(false);
            blendState.setEnabled(false);
        }
        
        if (material.getAlphaState() == PMeshMaterial.AlphaTransparencyType.A_ONE)
        {
            blendState.setEnabled(true);
            blendState.setBlendEnabled(true);
            blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            blendState.setTestEnabled(true);
            blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
        }
        else if (material.getAlphaState() == PMeshMaterial.AlphaTransparencyType.RGB_ZERO)
        {
            blendState.setEnabled(true);
            blendState.setBlendEnabled(true);
            blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
            blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
            blendState.setTestEnabled(true);
            blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
        }
    }
}
