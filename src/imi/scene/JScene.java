/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package imi.scene;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.LightState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import imi.character.CharacterMotionListener;
import imi.scene.utils.visualizations.DebugRenderer;

/**
 * JScenes are the glue between the dynamic PScene nodes and the render-only JMonkey scene nodes.
 * Set a JScene as the root of a SceneComponent and add it to an Entity to have
 * the "live" PScene flattend into the JScene graph every frame.
 * The "external kids root" is used to attach "normal" jme nodes to the scene,
 * attaching a node directly under the JScene will not work.
 *
 * @author Lou Hayt
 * @author Chris Nagle
 */
public class JScene extends Node implements CharacterMotionListener {
    public enum ExternalKidsType { TRANSFORMED, UNTRANSFORMED };

    private final PScene    m_PScene;
    private boolean         m_bRender           = true;

    private boolean         m_bDebugRender      = false;
    private boolean         m_bDebugRenderOnly  = false;
    private DebugRenderer   m_debugRenderer     = null;

    /** The external jme kids root is used to add aditional jme nodes to this render component **/
    private Node        m_externalJmeKidsRoot = new Node("external Kids");
    private Vector3f    m_ExternalKidsRootPosition = new Vector3f(); // applied by the PScene
    private Quaternion  m_ExternalKidsRootRotation = new Quaternion();

    /** An untransformed jme kids root */
    private Node m_externalJmeKidsRootUntransformed = new Node("external Kids untransformed");

    private Vector3f worldPos = new Vector3f();

    private boolean kidsChanged = false;

    private boolean printCullInfo = false;

    /**
     * Constructor, takes in the PScene for this Node (JScene is intended to be used as a render component that supports a PScene)
     * @param scene
     */
    public JScene(PScene scene) {
        m_PScene = scene;
        m_PScene.setJScene(this);
    }

    /**
     * Get a jme node to attach external (none PScene related) nodes
     * @return
     */
    public Node getExternalKidsRoot() {
        return getExternalKidsRoot(ExternalKidsType.TRANSFORMED);
    }

    /**
     * Get a jme node to attach external (not PScene related) nodes. There
     * are two nodes available: a transformed node moves with the avatar,
     * while the untransformed node does not.
     */
    public Node getExternalKidsRoot(ExternalKidsType kidsType) {
        switch (kidsType) {
            case TRANSFORMED:
                return m_externalJmeKidsRoot;
            case UNTRANSFORMED:
                return m_externalJmeKidsRootUntransformed;

            default:
                throw new IllegalArgumentException("Unknown type: " +
                            kidsType);
        }
    }

    /**
     * Set a flag indicating that the external kids graph has changed
     */
    public void setExternalKidsChanged(boolean flag) {
        kidsChanged = flag;
    }

    /**
     * Get the position of the external (none PScene related) jme root
     * @return
     */
    public Vector3f getExternalKidsRootPosition() {
        return m_ExternalKidsRootPosition;
    }

    /**
     * Get the rotation of the external (none PScene related) jme root
     * @return
     */
    public Quaternion getExternalKidsRootRotation() {
        return m_ExternalKidsRootRotation;
    }

    /**
     * Set the position (local to the JScene's position) of the external (none PScene related) jme root
     * @param externalKidsRootPosition
     * @param externalKidsRootRotation
     */
    public void setExternalKidsRootPosition(Vector3f externalKidsRootPosition, Quaternion externalKidsRootRotation) {
        this.m_ExternalKidsRootPosition.set(externalKidsRootPosition);
        this.m_ExternalKidsRootRotation.set(externalKidsRootRotation);
//        m_externalJmeKidsRoot.setLocalTranslation(externalKidsRootPosition); NO! must be on render thread - let the pscene do it
//        m_externalJmeKidsRoot.setLocalRotation(externalKidsRootRotation);    NO! must be on render thread - let the pscene do it
    }

    /**
     * Get the PScene of this JScene
     * @return returns the pscene that is used by this jscene
     */
    public PScene getPScene() {
        return m_PScene;
    }

    /**
     * Sets rendering on/off
     * @param renderingOn
     */
    public synchronized void setRenderBool(boolean renderingOn) {
        m_bRender = renderingOn;
    }

    /**
     * Get the render flag
     * @return true if this jscene is set to render
     */
    public synchronized boolean getRenderBool() {
        return m_bRender;
    }

    /**
     * Set wireframe renderstate
     * @param on
     */
    public void setWireframe(boolean on)
    {
        WireframeState wireframeState = (WireframeState) getRenderState(RenderState.RS_WIREFRAME);
        if (wireframeState != null)
        {
            wireframeState.setEnabled(on);
            updateRenderState();
        }
        else
        {
            wireframeState = (WireframeState) m_PScene.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
            wireframeState.setEnabled(on);
            setRenderState(wireframeState);
            updateRenderState();
        }
    }

    /**
     * Check wireframe state
     * @return
     */
    public boolean isWireframeOn() {
        WireframeState wireframeState = (WireframeState) getRenderState(RenderState.RS_WIREFRAME);
        if (wireframeState != null)
            return wireframeState.isEnabled();
        return false;
    }

    /**
     * Will toggle light renderstate enabled on\off
     */
    public void toggleLights() {
        LightState lightState = (LightState) getRenderState(RenderState.RS_LIGHT);
        if (lightState != null) {
            lightState.setEnabled(!lightState.isEnabled());
            updateRenderState();
        }
    }

    /**
     * Will toggle wireframe renderstate
     */
    public void toggleWireframe() {
        WireframeState wireframeState = (WireframeState) getRenderState(RenderState.RS_WIREFRAME);
        if (wireframeState != null)
        {
            wireframeState.setEnabled(!wireframeState.isEnabled());
            updateRenderState();
        }
        else
        {
            wireframeState = (WireframeState) m_PScene.getWorldManager().getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
            wireframeState.setEnabled(true);
            setRenderState(wireframeState);
            updateRenderState();
        }
    }

    /**
     * Check if the bounding spheres draw is enabled in the debug renderer
     * @return
     */
    public boolean isBoundingSphereDrawEnabled() {
        if (m_debugRenderer == null)
            m_debugRenderer = new DebugRenderer();
        return m_debugRenderer.isBoundingSphereDrawEnabled();
    }

    /**
     * Set rendering of the bounding sphere draw in the debug renderer
     * @param boundingSphereDrawEnabled
     */
    public void setBoundingSphereDrawEnabled(boolean boundingSphereDrawEnabled) {
        if (m_debugRenderer == null)
            m_debugRenderer = new DebugRenderer();
        m_debugRenderer.setBoundingSphereDrawEnabled(boundingSphereDrawEnabled);
    }

    /**
     * Check if debug rendering is on
     * @return
     */
    public boolean isDebugRendererOn() {
        return m_bDebugRender;
    }

    /**
     * Set the debug renderer flag
     * @param debugRendererOn
     */
    public void setDebugRendererOn(boolean debugRendererOn) {
        this.m_bDebugRender = debugRendererOn;
    }

    /**
     * Check if debug rendering is turned on exclusivly
     * @return
     */
    public boolean isDebugRenderOnly() {
        return m_bDebugRenderOnly;
    }

    /**
     * Set if jme rendering will be used in addition to the debug rendering
     * @param debugRenderOnly
     */
    public void setDebugRenderOnly(boolean debugRenderOnly) {
        this.m_bDebugRenderOnly = debugRenderOnly;
    }

    /**
     * Toggle smooth normals on local geometry
     */
    public void toggleSmoothNormals() {
        if (m_PScene != null) {
            m_PScene.toggleSmoothNormals();
            m_PScene.setDirty(true, false);
        }
    }

    /**
     * Flip normals on local geometry
     */
    public void flipNormals() {
        if (m_PScene != null) {
            m_PScene.flipNormals();
            m_PScene.setDirty(true, false);
        }
    }

    /**
     * Toggles between JMonkey rendering \ Debug rendering enabled \ Only debug rendering
     */
    public void debugRenderToggle() {
        if (!m_bDebugRender)
            m_bDebugRender = true;
        else if (m_bDebugRenderOnly)
        {
            m_bDebugRender = false;
            m_bDebugRenderOnly = false;
        }
        else
            m_bDebugRenderOnly = true;
    }

    @Override
    public synchronized void updateRenderState() {
        if(m_bRender)
            super.updateRenderState();
    }

    /**
     * Calls the onDraw method for each JMonkey child maintained by this node.
     * onDraw() will check if the child should be culled and if not it will call
     * the child's draw method.
     *
     * This method is overriden to enable PNode based debug rendering and let
     * the pscene submit itself to this jscene which means that the transform
     * hierarchy will be "flattened" (world matrices will be calculated) and
     * all jscene's kids will be removed(the references are kept by the
     * relevant objects in the pscene) and replaced with the appropriate
     * references to draw this frame.
     *
     * @see com.jme.scene.Spatial#draw(com.jme.renderer.Renderer)
     * @param r - the renderer to draw to.
     */
    @Override
    public synchronized void draw(Renderer r)
    {
        if (m_bRender)
        {
            if (m_bDebugRender)
            {
                if (m_bDebugRenderOnly)
                    m_PScene.submitTransforms();
                else
                    drawScene(r);

                if (m_debugRenderer == null)
                    m_debugRenderer = new DebugRenderer();
                m_debugRenderer.resetRenderer(r);
                m_PScene.debugDraw(m_debugRenderer);
                m_debugRenderer.present();
            }
            else
                drawScene(r);
        }

        if (printCullInfo) {
            System.err.print("Draw "+System.nanoTime()+"  ");
            printCullHint(this);
            System.err.println();
        }
    }

    private void printCullHint(Spatial n) {
        if (n==null)
            return;
        System.err.println("   "+n.getCullHint()+" "+n.getWorldBound());
        if (n instanceof Node) {
            for(Spatial c : ((Node)n).getChildren()) {
                printCullHint(c);
            }
        }
    }

    public void setPrintCullInfo(boolean print) {
        System.err.println("PRINTING AVATAR PARENTS");
        Node n = getParent();
        while(n!=null) {
            System.err.println(n.getClass().getName()+"  "+n.getName()+"  "+n.getWorldBound()+"  "+n.getCullHint());
        }
        this.printCullInfo = print;
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
     * @param r - the renderer to draw to.
     */
    private void drawScene(Renderer r) {
        m_PScene.submitTransformsAndGeometry(kidsChanged);
        kidsChanged = false;
        updateRenderState();
        super.draw(r);
    }

    /**
     *
     * @param translation
     * @param rotation
     */
    public void transformUpdate(Vector3f translation, PMatrix rotation) {
        worldPos.set(translation);
        m_PScene.getWorldManager().addToUpdateList(this);
    }

    @Override
    public void updateWorldBound() {
        // TOOD, this is a temporary work around for incorrect bounds when
        // an avatar jumps out of the view frustum. Before this fix the bounds
        // did not update once the avatar was outside the view frustum, they
        // are updated in the draw call, which is not called if this node is
        // frustum culled. Hence this workaround to compute the bounds in
        // the normal JME style.
        // This fix does not take into account the bounds of external kids etc.
        worldBound = new BoundingSphere(1.5f, Vector3f.ZERO).transform(getWorldRotation(),
            worldPos, getWorldScale(), worldBound);
   } 
}
