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

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class allows for the use of a cylinder as the "sky geometry", rather than
 * a cube.
 * @author Ronald E Dahlgren
 */
public class SkyCylinder extends Node
{
    /** The World Manager for renderstate creation. */
    private WorldManager m_wm = null;

    private Cylinder skyCylinder = null;

     /**
     * Creates a new skybox. The size of the skybox and name is specified here.
     * By default, no textures are set.
     *
     * @param name
     *            The name of the skybox.
     * @param xExtent
     *            The x size of the skybox in both directions from the center.
     * @param yExtent
     *            The y size of the skybox in both directions from the center.
     * @param zExtent
     *            The z size of the skybox in both directions from the center.
     */
    public SkyCylinder(String name, WorldManager wm) {
        super(name);
        m_wm = wm;
        initialize();
    }

    /**
     * Set the texture for the sky cylinder. Should be a panoramic projection
     * @param texture The texture to use
     */
    public void setTexture(Texture texture) {
        TextureState ts = (TextureState) skyCylinder.getRenderState(StateType.Texture);
        if (ts == null)
            ts = (TextureState)m_wm.getRenderManager().createRendererState(StateType.Texture);

        // Initialize the texture state
        ts.setTexture(texture, 0);
        ts.setEnabled(true);
        skyCylinder.setRenderState(ts);
        // get an update
        m_wm.addToUpdateList(skyCylinder);
    }


    private void initialize() {

        // make the sky geometry
        skyCylinder = new Cylinder("Sky", 32, 16, 10, 30, true, true);
        skyCylinder.setLocalRotation(new Quaternion(new float[] {3.14159f * -0.5f, 0, 0}));

        // We don't want the light to effect our skybox
        skyCylinder.setLightCombineMode(LightCombineMode.Off);
        skyCylinder.setTextureCombineMode(TextureCombineMode.Replace);

        ZBufferState zbuff = (ZBufferState)m_wm.getRenderManager().createRendererState(StateType.ZBuffer);
        zbuff.setWritable(false);
        zbuff.setEnabled(true);
        zbuff.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        skyCylinder.setRenderState(zbuff);

        // We don't want it making our skybox disapear, so force view
        skyCylinder.setCullHint(CullHint.Never);

        // Make sure texture is only what is set.
        skyCylinder.setTextureCombineMode(TextureCombineMode.Replace);

        // Make sure no lighting on the skybox
        skyCylinder.setLightCombineMode(LightCombineMode.Off);

        // Make sure the quad is viewable
        skyCylinder.setCullHint(CullHint.Never);

        // Set a bounding volume
        skyCylinder.setModelBound(new BoundingBox());
        skyCylinder.updateModelBound();

        skyCylinder.setRenderQueueMode(Renderer.QUEUE_SKIP);
        skyCylinder.setVBOInfo(null);

        // And attach the skybox as a child
        attachChild(skyCylinder);
        // get an update
        m_wm.addToUpdateList(skyCylinder);
        m_wm.addToUpdateList(this);
    }
}
