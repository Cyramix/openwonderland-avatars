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
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Cylinder;
import com.jme.scene.shape.Disk;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This class allows for the use of a cylinder as the "sky geometry", rather than
 * a cube.
 * @author Ronald E Dahlgren
 */
@ExperimentalAPI
public class SkyCylinder extends Node
{
    public enum SkyParts {
        Cylinder,
        TopDisk,
        BottomDisk,
    }
    /** The World Manager for renderstate creation. */
    private WorldManager m_wm = null;

    /** Geometry for the sky **/
    private Cylinder skyCylinder    = null;
    private Disk     skyTop         = null;
    private Disk     skyBottom      = null;

    /** Magic numbers needed until the debug renderer can play nice with the skybox **/
    private float cylinderRadius = 50;
    private float cylinderHeight = 50;


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
     * @param destinationSection Where does this texture belong?
     */
    public void setTexture(Texture texture, SkyParts destinationSection) {
        TextureState ts = null;
        TriMesh target = null;
        switch (destinationSection)
        {
            case BottomDisk:
                ts = (TextureState) skyBottom.getRenderState(StateType.Texture);
                target = skyBottom;
                break;
            case Cylinder:
                ts = (TextureState) skyCylinder.getRenderState(StateType.Texture);
                target = skyCylinder;
                break;
            case TopDisk:
                ts = (TextureState) skyTop.getRenderState(StateType.Texture);
                target = skyTop;
                break;
        }

        if (ts == null)
            ts = (TextureState)m_wm.getRenderManager().createRendererState(StateType.Texture);

        // Initialize the texture state
        ts.setTexture(texture, 0);
        ts.setEnabled(true);
        target.setRenderState(ts);
        // get an update
        m_wm.addToUpdateList(target);
    }


    private void initialize() {

        // make the sky geometry
        // body
        skyCylinder = new Cylinder("Sky", 32, 16, cylinderRadius, cylinderHeight, false, true);
        skyCylinder.setLocalRotation(new Quaternion(new float[] {3.14159f * -0.5f, 0, 0}));
        // top
        skyTop = new Disk("SkyTop", 8, 16, cylinderRadius);
        skyTop.setLocalRotation(new Quaternion(new float[] {3.14159f * 0.5f, 0, 0}));
        skyTop.setLocalTranslation(0, cylinderHeight * 0.5f, 0);
        // bottom
        skyBottom = new Disk("SkyTop", 8, 16, cylinderRadius);
        skyBottom.setLocalRotation(new Quaternion(new float[] {3.14159f * -0.5f, 0, 0}));
        skyBottom.setLocalTranslation(0, cylinderHeight * -0.5f, 0);

        setRenderStates(skyTop);
        setRenderStates(skyBottom);
        setRenderStates(skyCylinder);

        // And attach the skybox as a child
        attachChild(skyCylinder);
        attachChild(skyTop);
        attachChild(skyBottom);
        // get an update
        m_wm.addToUpdateList(skyCylinder);
        m_wm.addToUpdateList(skyBottom);
        m_wm.addToUpdateList(skyTop);
        m_wm.addToUpdateList(this);
    }

    private void setRenderStates(TriMesh target)
    {
        // We don't want the light to effect our skybox
        target.setLightCombineMode(LightCombineMode.Off);
        target.setTextureCombineMode(TextureCombineMode.Replace);

        ZBufferState zbuff = (ZBufferState)m_wm.getRenderManager().createRendererState(StateType.ZBuffer);
        zbuff.setWritable(false);
        zbuff.setEnabled(true);
        zbuff.setFunction(ZBufferState.TestFunction.LessThan);
        target.setRenderState(zbuff);


        // Make sure texture is only what is set.
        target.setTextureCombineMode(TextureCombineMode.Replace);

        // Make sure no lighting on the skybox
        target.setLightCombineMode(LightCombineMode.Off);

        // Make sure the quad is viewable
        target.setCullHint(CullHint.Never);

        // Set a bounding volume
        target.setModelBound(new BoundingBox());
        target.updateModelBound();

        target.setRenderQueueMode(Renderer.QUEUE_SKIP);
        target.setVBOInfo(null);
    }
}
