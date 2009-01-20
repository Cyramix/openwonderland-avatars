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
package imi.scene.particles;

import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticlePoints;
import imi.scene.Updatable;
import imi.scene.processors.UpdateProcessor;
import java.nio.FloatBuffer;
import javax.media.opengl.GL;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class maintains information about a collection of particles.
 * @author Ronald E Dahlgren
 */
public class ParticleCollection implements RenderUpdater, Updatable
{
    /** The particles being managed **/
    private ParticlePoints particles = null;
    /** The Entity with particles attached as children **/
    private Entity entity = null;
    /** Render component we attach to **/
    private RenderComponent renderComponent = null;
    /** Updater **/
    private UpdateProcessor updater     = null;
    /** Used to determine wether updates should happen or not **/
    private boolean enabled             = true;

    /**
     * Construct a new instance with the specified number of particles.
     * @param numberOfParticles
     */
    public ParticleCollection(int numberOfParticles, WorldManager wm)
    {
        particles = ParticleFactory.buildPointParticles("particles", numberOfParticles);
        updater = new UpdateProcessor(this);
        particleSetUp();
        createEntity(wm);
        createRenderComponent(wm);
        setDefaultRenderStates(wm);

    }


    private void particleSetUp()
    {
        // TODO : Parameterize
        particles.setPointSize(5);
        particles.setAntialiased(true);
        particles.setEmissionDirection(new Vector3f(0, 1, 0));
        particles.setOriginOffset(new Vector3f(0, 0, 0));
        particles.setInitialVelocity(.006f);
        particles.setStartSize(2.5f);
        particles.setEndSize(.5f);
        particles.setMinimumLifeTime(1200f);
        particles.setMaximumLifeTime(1400f);
        particles.setStartColor(new ColorRGBA(1, 0, 0, 1));
        particles.setEndColor(new ColorRGBA(0, 1, 0, 0));
        particles.setMaximumAngle(360f * FastMath.DEG_TO_RAD);
        particles.getParticleController().setControlFlow(true);
        particles.warmUp(120);
    }

    private void createEntity(WorldManager wm)
    {
        entity = new Entity("ParticleEntity");
        wm.addEntity(entity);
    }

    private void createRenderComponent(WorldManager wm)
    {
        renderComponent = wm.getRenderManager().createRenderComponent(particles);
        entity.addComponent(RenderComponent.class, renderComponent);
        entity.addComponent(ProcessorComponent.class, updater);
    }

    private void setDefaultRenderStates(WorldManager wm)
    {
        BlendState as1 = (BlendState)wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        as1.setBlendEnabled(true);
        as1.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        as1.setDestinationFunction(BlendState.DestinationFunction.One);
        as1.setEnabled(true);
        particles.setRenderState(as1);

        ZBufferState zstate = (ZBufferState)wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zstate.setEnabled(false);
        particles .setRenderState(zstate);

        particles.setModelBound(new BoundingSphere());
        particles.updateModelBound();
        // point sprites are useless without a texture
        TextureState ts = (TextureState)wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        ts.setTexture(TextureManager.loadTexture(ParticleCollection.class.getClassLoader().getResource("jmetest/data/texture/flaresmall.jpg"), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.NearestNeighbor));
        ts.setEnabled(true);
        particles.setRenderState(ts);

        FloatBuffer ceoff = BufferUtils.createFloatBuffer(0.0f, 0.00001f, 0.0f, 0.0f);
        ceoff.rewind();
        wm.addRenderUpdater(this, ceoff);


    }

    public void update(Object arg0) {
        FloatBuffer ceoff = (FloatBuffer)arg0;
        GL glDevice = javax.media.opengl.GLContext.getCurrent().getGL();
        glDevice.glEnable(GL.GL_POINT_SPRITE_ARB);
        glDevice.glTexEnvi(GL.GL_POINT_SPRITE_ARB, GL.GL_COORD_REPLACE_ARB, 1);
        glDevice.glPointParameterfvARB(GL.GL_POINT_DISTANCE_ATTENUATION_ARB, ceoff);
    }

    public void update(float deltaTime) {
        if (false)
        {
            float ourTime = deltaTime * 0.00000001f;
            System.out.println("ourTime : " + ourTime);
            particles.getController(0).update(ourTime);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnable(boolean state) {
        enabled = state;
    }
}
