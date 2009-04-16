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
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.JmeException;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt (used the JME e.g.)
 */
public class SkyBox extends Node
{
    /** The World Manager for renderstate creation. */
    private WorldManager m_wm = null;
    
    /** The +Z side of the skybox. */
    public final static int NORTH = 0;

    /** The -Z side of the skybox. */
    public final static int SOUTH = 1;

    /** The -X side of the skybox. */
    public final static int EAST = 2;

    /** The +X side of the skybox. */
    public final static int WEST = 3;

    /** The +Y side of the skybox. */
    public final static int UP = 4;

    /** The -Y side of the skybox. */
    public final static int DOWN = 5;

    private float xExtent;

    private float yExtent;

    private float zExtent;

    private Quad[] skyboxQuads;
    
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
    public SkyBox(String name, float xExtent, float yExtent, float zExtent, WorldManager wm) {
        super(name);

        this.xExtent = xExtent;
        this.yExtent = yExtent;
        this.zExtent = zExtent;
        
        m_wm = wm;

        initialize();
    }
    
    /**
     * Set the texture to be displayed on the given side of the skybox. Replaces
     * any existing texture on that side.
     * 
     * @param direction
     *            One of Skybox.NORTH, Skybox.SOUTH, and so on...
     * @param texture
     *            The texture for that side to assume.
     */
    public void setTexture(int direction, Texture texture) {
        if (direction < 0 || direction > 5) {
            throw new JmeException("Direction " + direction
                    + " is not a valid side for the skybox");
        }

        skyboxQuads[direction].clearRenderState(RenderState.RS_TEXTURE);
        setTexture(direction, texture, 0);
    }

    /**
     * Set the texture to be displayed on the given side of the skybox. Only
     * replaces the texture at the index specified by textureUnit.
     * 
     * @param direction
     *            One of Skybox.NORTH, Skybox.SOUTH, and so on...
     * @param texture
     *            The texture for that side to assume.
     * @param textureUnit
     *            The texture unite of the given side's TextureState the texture
     *            will assume.
     */
    public void setTexture(int direction, Texture texture, int textureUnit) {
        // Validate
        if (direction < 0 || direction > 5) {
            throw new JmeException("Direction " + direction
                    + " is not a valid side for the skybox");
        }

        TextureState ts = (TextureState) skyboxQuads[direction]
                .getRenderState(RenderState.RS_TEXTURE);
        if (ts == null) {
            ts = (TextureState)m_wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        }

        // Initialize the texture state
        ts.setTexture(texture, textureUnit);
        ts.setEnabled(true);

        // Set the texture to the quad
        skyboxQuads[direction].setRenderState(ts);

        return;
    }
    
    private void initialize() {

        // Skybox consists of 6 sides
        skyboxQuads = new Quad[6];

        // Create each of the quads
        skyboxQuads[NORTH] = new Quad("north", xExtent * 2, yExtent * 2);
        skyboxQuads[NORTH].setLocalRotation(new Quaternion(new float[] { 0,
                (float) Math.toRadians(180), 0 }));
        skyboxQuads[NORTH].setLocalTranslation(new Vector3f(0, 0, zExtent));
        skyboxQuads[SOUTH] = new Quad("south", xExtent * 2, yExtent * 2);
        skyboxQuads[SOUTH].setLocalTranslation(new Vector3f(0, 0, -zExtent));
        skyboxQuads[EAST] = new Quad("east", zExtent * 2, yExtent * 2);
        skyboxQuads[EAST].setLocalRotation(new Quaternion(new float[] { 0,
                (float) Math.toRadians(90), 0 }));
        skyboxQuads[EAST].setLocalTranslation(new Vector3f(-xExtent, 0, 0));
        skyboxQuads[WEST] = new Quad("west", zExtent * 2, yExtent * 2);
        skyboxQuads[WEST].setLocalRotation(new Quaternion(new float[] { 0,
                (float) Math.toRadians(270), 0 }));
        skyboxQuads[WEST].setLocalTranslation(new Vector3f(xExtent, 0, 0));
        skyboxQuads[UP] = new Quad("up", xExtent * 2, zExtent * 2);
        skyboxQuads[UP].setLocalRotation(new Quaternion(new float[] {
                (float) Math.toRadians(90), (float) Math.toRadians(270), 0 }));
        skyboxQuads[UP].setLocalTranslation(new Vector3f(0, yExtent, 0));
        skyboxQuads[DOWN] = new Quad("down", xExtent * 2, zExtent * 2);
        skyboxQuads[DOWN].setLocalRotation(new Quaternion(new float[] {
                (float) Math.toRadians(270), (float) Math.toRadians(270), 0 }));
        skyboxQuads[DOWN].setLocalTranslation(new Vector3f(0, -yExtent, 0));

        // We don't want the light to effect our skybox
        setLightCombineMode(LightCombineMode.Off);
        
        setTextureCombineMode(TextureCombineMode.Replace);

        ZBufferState zbuff = (ZBufferState)m_wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuff.setWritable(false);
        zbuff.setEnabled(true);
        zbuff.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        setRenderState(zbuff);

        // We don't want it making our skybox disapear, so force view
        setCullHint(CullHint.Never);

        for (int i = 0; i < 6; i++) {
            // Make sure texture is only what is set.
            skyboxQuads[i].setTextureCombineMode(TextureCombineMode.Replace);

            // Make sure no lighting on the skybox
            skyboxQuads[i].setLightCombineMode(LightCombineMode.Off);

            // Make sure the quad is viewable
            skyboxQuads[i].setCullHint(CullHint.Never);

            // Set a bounding volume
            skyboxQuads[i].setModelBound(new BoundingBox());
            skyboxQuads[i].updateModelBound();

            skyboxQuads[i].setRenderQueueMode(Renderer.QUEUE_SKIP);
            skyboxQuads[i].setVBOInfo(null);

            // And attach the skybox as a child
            attachChild(skyboxQuads[i]);
        }
    }
    
    /**
     * Force all of the textures to load. This prevents pauses later during the
     * application as you pan around the world.
     */
    public void preloadTextures() {
        for (int x = 0; x < 6; x++) {
            TextureState ts = (TextureState) skyboxQuads[x]
                    .getRenderState(RenderState.RS_TEXTURE);
            if (ts != null)
                ts.apply();
        }

    }
    
}
