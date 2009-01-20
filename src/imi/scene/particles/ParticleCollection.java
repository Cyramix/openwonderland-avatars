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
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import com.jmex.effects.particles.ParticleController;
import com.jmex.effects.particles.ParticleFactory;
import com.jmex.effects.particles.ParticleMesh;
import imi.scene.Updatable;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.processors.UpdateProcessor;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class maintains information about a collection of particles.
 * @author Ronald E Dahlgren
 */
public class ParticleCollection implements Updatable
{
    /** The particles being managed **/
    public ParticleMesh particles = null; // TODO : Encapsulate
    /** Their controller **/
    private ParticleController controller = null;
    /** The Entity with particles attached as children **/
    private Entity entity = null;
    /** Render component we attach to **/
    private RenderComponent renderComponent = null;
    /** Updater **/
    private UpdateProcessor updater     = null;
    /** Used to determine wether updates should happen or not **/
    private boolean enabled             = true;
    /** Position of the emitter **/
    private PPolygonModelInstance targetModel = null;

    /**
     * Construct a new instance with the specified number of particles.
     * @param numberOfParticles
     */
    public ParticleCollection(int numberOfParticles, WorldManager wm, Node parent)
    {
        particles = ParticleFactory.buildParticles("ParticleTest", numberOfParticles);
        controller = particles.getParticleController();
        updater = new UpdateProcessor(this);
        particleSetUp();
        createEntity(wm);
        setDefaultRenderStates(wm);
        parent.attachChild(particles);
    }

    public Spatial getJMENode() {
        return particles;
    }


    private void particleSetUp()
    {
        // TODO : Parameterize
        particles.setEmissionDirection(new Vector3f(0, 1, 0));
        particles.setOriginOffset(new Vector3f(0, 0, 0));
        particles.setInitialVelocity(.005f);
        particles.setStartSize(0.01f);
        particles.setEndSize(1.4f);
        particles.setMinimumLifeTime(100f);
        particles.setMaximumLifeTime(1400f);
        particles.setStartColor(new ColorRGBA(1, 0, 0, 1));
        particles.setEndColor(new ColorRGBA(0, 1, 0, 0));
        particles.setMaximumAngle(90f * FastMath.DEG_TO_RAD);
        particles.warmUp(120);
        controller.setControlFlow(false);
    }

    private void createEntity(WorldManager wm)
    {
        entity = new Entity("ParticleEntity");
        entity.addComponent(ProcessorComponent.class, updater);
        wm.addEntity(entity);
    }

    private void setDefaultRenderStates(WorldManager wm)
    {
        BlendState blendState = (BlendState)wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(BlendState.DestinationFunction.One);
        blendState.setTestEnabled(true);
        blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
        blendState.setEnabled(true);
        particles.setRenderState(blendState);


        ZBufferState zstate = (ZBufferState)wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zstate.setEnabled(true);
        zstate.setWritable(false);
        zstate.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        particles.setRenderState(zstate);

        particles.setModelBound(new BoundingSphere());
        particles.updateModelBound();

        // point sprites are useless without a texture
        TextureState ts = (TextureState)wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        ts.setTexture(TextureManager.loadTexture(ParticleCollection.class.getClassLoader().getResource("jmetest/data/texture/flaresmall.jpg"), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.NearestNeighbor));
        ts.setEnabled(true);
        particles.setRenderState(ts);
        particles.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        particles.updateRenderState();

    }

    private final Vector3f vecBuffer = new Vector3f();
    public void update(float deltaTime) {
        if (true)
        {
            if (targetModel != null)
            {
                targetModel.getTransform().getLocalMatrix(false).getTranslation(vecBuffer);
                particles.setOriginOffset(vecBuffer);
            }
            controller.update(deltaTime);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnable(boolean state) {
        enabled = state;
    }

    public PPolygonModelInstance getTargetModel() {
        return targetModel;
    }

    public void setTargetModel(PPolygonModelInstance targetModel) {
        this.targetModel = targetModel;
    }

}
