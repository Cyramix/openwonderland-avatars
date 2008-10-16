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
package imi.scene;

import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import imi.scene.utils.PRenderer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JScenes are the glue between the dynamic PScene nodes and the render-only JMonkey scene nodes.
 * Set a JScene as the root of a SceneComponent and add it to an entity.
 *
 * @author Lou Hayt
 * @author Chris Nagle
 */
public class JScene extends Node {

    private PScene      m_PScene            = null;
    private boolean     m_bRender           = true;
    private boolean     m_bRenderInternally = false;    //  if false JMonkey's rendering will be used
    private boolean     m_bRenderBoth       = false;    //  true when both JMonkey and PRenderer are used
    private PRenderer   m_PRenderer         = null;     //  for diagnostic internal rendering

    /**
     * Empty constructor, does nothing.
     */
    public JScene() {
    }

    /**
     * Constructor
     * @param scene
     */
    public JScene(PScene scene) {
        setPScene(scene);
    }

    /**
     * Constructor
     * @param scene
     * @param internalRenderer
     */
    public JScene(PScene scene, PRenderer internalRenderer)
    {
        setPScene(scene);
        m_PRenderer = internalRenderer;
    }


    /**
     * Set the scene, a scene may contain model instances of
     * geometry that is shared internally or across threads (if using 
     * the repository).
     * @param scene
     */
    public void setPScene(PScene scene) {
        if (scene != null) {
            m_PScene = scene;
            m_PScene.setJScene(this);
        }
    }

    /**
     * 
     * @return returns the pscene that is used by this jscene
     */
    public PScene getPScene() {
        return m_PScene;
    }

    /**
     * Sets rendering on/off
     * @param renderingOn
     */
    public void setRenderBool(boolean renderingOn) {
        m_bRender = renderingOn;
    }

    /**
     * Sets internal rendering on\off
     * Internal rendering is used to visualize pscene elements,
     * it is using JME lines and points to do so.
     * @param internalRenderingOn
     */
    public void setRenderInternallyBool(boolean internalRenderingOn) {
        m_bRenderInternally = internalRenderingOn;
    }

    /**
     * This method sets both renders to active.
     * @param renderBoth
     */
    public void setRenderBothBool(boolean renderBoth)
    {
        m_bRenderBoth = renderBoth;
    }

    /**
     * 
     * @return true if this jscene is set to render
     */
    public boolean getRenderBool() {
        return m_bRender;
    }

    /**
     * 
     * @return true if internal rendering is on (PRenderer)
     * Internal rendering is used to visualize pscene elements,
     * it is using JME lines and points to do so.
     */
    public boolean getRenderInternallyBool() {
        return m_bRenderInternally;
    }

    public void setRenderPRendererMesh(boolean on) {
        if (m_PRenderer != null)
            m_PRenderer.setRenderPRendererMesh(on);
        else
        {
            m_PRenderer = new PRenderer();
            m_PRenderer.setRenderPRendererMesh(on);
        }
    }

    public void setWireframe(boolean on) 
    {
        WireframeState wireframeState = (WireframeState) getRenderState(RenderState.RS_WIREFRAME);
        if (wireframeState != null) 
        {
            wireframeState.setEnabled(on);
            updateRenderState();
        }
    }

    /**
     * Will toggle lights in this jscene on\off
     */
    public void toggleLights() {
        LightState lightState = (LightState) getRenderState(RenderState.RS_LIGHT);
        if (lightState != null) {
            lightState.setEnabled(!lightState.isEnabled());
            updateRenderState();
        }
    }

    /**
     * Will toggle rendering of the green wireframes of meshes
     * in this jscene on\off
     */
    public void toggleRenderPRendererMesh() 
    {
        if (m_PRenderer != null)
            m_PRenderer.toggleRenderPRendererMesh();
    }

    /**
     * Will toggle wireframe for the JME renderer on elements of this scene
     */
    public void toggleWireframe() {
        WireframeState wireframeState = (WireframeState) getRenderState(RenderState.RS_WIREFRAME);
        if (wireframeState != null) 
        {
            wireframeState.setEnabled(!wireframeState.isEnabled());
            updateRenderState();
        }
    }

    /**
     * Toggle rendering for polygon normals (in the center of every polygon)
     * for the internal rendering of this scene.
     * Internal rendering is used to visualize pscene elements,
     * it is using JME lines and points to do so.
     */
    public void toggleRenderPolygonNormals() {
        if (m_PRenderer != null)
            m_PRenderer.renderPolygonNormals(!m_PRenderer.getRenderPolygonNormals());
    }

    /**
     * Toggle rendering for vertex normals (on evert vertex)
     * for the internal rendering of this scene.
     * Internal rendering is used to visualize pscene elements,
     * it is using JME lines and points to do so.
     */
    public void toggleRenderVertexNormals() {
        if (m_PRenderer != null)
            m_PRenderer.renderVertexNormals(!m_PRenderer.getRenderVertexNormals());
    }

    /**
     * Toggle rendering for dots in the center of every polygon.
     * for the internal rendering of this scene.
     * Internal rendering is used to visualize pscene elements,
     * it is using JME lines and points to do so.
     */
    public void toggleRenderPolygonCenters() {
        if (m_PRenderer != null)
            m_PRenderer.renderPolygonCenters(!m_PRenderer.getRenderPolygonCenters());
    }

    /**
     * Toggle rendering of bounding volumes 
     * for the internal rendering of this scene.
     * The order is Off - Box - Sphere
     * Internal rendering is used to visualize pscene elements,
     * it is using JME lines and points to do so.
     */    
    public void toggleRenderBoundingVolume() {
        if (m_PRenderer != null) {
            m_PRenderer.renderBoundingVolumeToggle();
        }
    }

    /**
     * Switches rendering of this scene between:
     * - JME only rendering
     * - JME and internal (PRenderer) rendering
     * - Only internal rendering (PRenderer)
     */
    public void renderToggle() {
        //  (JMonkey, JMonkey and PRenderer, PRenderer)
        if (m_bRenderBoth) 
        {
            m_bRenderBoth = false;
        } 
        else if (m_bRenderInternally) 
        {
            m_bRenderInternally = false;
        } 
        else 
        {
            m_bRenderInternally = true;
            m_bRenderBoth = true;
        }

    }

    /**
     * This functionality exists but selection is not implemented yet.
     * In the future selected models could switch between smooth and flat normals.
     */
    public void toggleSmoothNormals() {
        if (m_PScene != null) {
            m_PScene.toggleSmoothNormals();
            m_PScene.setDirty(true, false);
        }
    }

    /**
     * This functionality exists but selection is not implemented yet.
     * In the future selected models could flip thier normals.
     */
    public void flipNormals() {
        if (m_PScene != null) {
            m_PScene.flipNormals();
            m_PScene.setDirty(true, false);
        }
    }

    /**
     * draw() calls the onDraw method for each JMonkey child maintained by this node.
     * onDraw() will check if the child should be culled and if not it will call
     * the child's draw method.
     * 
     * This method is overriden to enable internal rendering if nessecary 
     * and also (more importantly) let pscene submit itself to this jscene
     * which means that the transform hierarchy will be "flattened" (world
     * matrices will be calculated) and all jscene's kids will be removed
     * (the references are kept by the relevant objects in pscene) and replaced
     * with the appropriate references to draw this frame.
     * 
     * @see com.jme.scene.Spatial#draw(com.jme.renderer.Renderer)
     * @param r
     *            the renderer to draw to.
     */
    @Override
    public void draw(Renderer r) 
    {
        m_PScene.hackOnDraw();
        if (m_bRender) 
        {
            if (m_bRenderInternally && m_PScene != null) 
            {
                if (m_PRenderer == null) 
                {
                    m_PRenderer = new PRenderer();
                }
                m_PRenderer.resetRenderer(r);

                if (m_bRenderBoth) 
                {
                    drawScene(r);
                } 
                else if (m_PScene.isDirty()) 
                {
                    m_PScene.submitTransforms();
                    //m_PScene.setDirty(false, false);
                }

                m_PScene.internalRender(m_PRenderer);
                
                m_PRenderer.present();
            } 
            else 
            {
                drawScene(r);
            }
        }
    }

    /**
     * This method lets pscene submit itself to this jscene
     * which means that the transform hierarchy will be "flattened" (world
     * matrices will be calculated) and all jscene's kids will be removed
     * (the references are kept by the relevant objects in pscene) and replaced
     * with the appropriate references to draw this frame.
     * 
     * Also any geometry in pscene that changed will reconstruct its
     * relevant JME object.
     * 
     * @param r
     *            the renderer to draw to.
     */
    private void drawScene(Renderer r) {
        m_PScene.submitTransformsAndGeometry();

        super.draw(r);
    }
}
