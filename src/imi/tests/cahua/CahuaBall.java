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
package imi.tests.cahua;

import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.environments.ColladaEnvironment;
import imi.scene.particles.ParticleCollection;
import imi.scene.polygonmodel.PPolygonModelInstance;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class handles ball behavior
 * @author Ronald E Dahlgren
 */
public class CahuaBall
{
    /** The model instance for this ball **/
    private PPolygonModelInstance   ballModel = null;
    /** The jME node for the environment **/
    private Node                    environmentNode = null;
    /** Our jme node **/
    private Sphere                    jmeNode = null;
    /** Active if particles are flying out **/
    private ParticleCollection      particles = null;
    /** Used for creating stuff **/
    private WorldManager            m_wm = null;
   
    public CahuaBall(PPolygonModelInstance ballModelInstance, WorldManager wm)
    {
        m_wm = wm;
        ColladaEnvironment environment = (ColladaEnvironment)wm.getUserData(ColladaEnvironment.class);
        environmentNode = environment.getJMENode();
        jmeNode = new Sphere("CahuaBallSphere", 16, 32, 3);
        jmeNode.setDefaultColor(ColorRGBA.magenta);
        jmeNode.setLocalTranslation(ballModelInstance.getTransform().getLocalMatrix(false).getTranslation());
        setDefaultRenderStates(jmeNode);
        environmentNode.attachChild(jmeNode);

    }

    public void applyParticles(int style)
    {
        particles = new ParticleCollection(30, m_wm, environmentNode);
        particles.setTargetModel(ballModel);
        switch(style)
        {
            default:
                setDefault(particles);
        }
    }

    private void setDefault(ParticleCollection particles)
    {
        // Do something!
    }

    private void setDefaultRenderStates(Sphere jmeNode)
    {
        BlendState blendState = (BlendState)m_wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        blendState.setBlendEnabled(true);
        blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        blendState.setDestinationFunction(BlendState.DestinationFunction.One);
        blendState.setTestEnabled(true);
        blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
        blendState.setEnabled(true);
        jmeNode.setRenderState(blendState);


        ZBufferState zstate = (ZBufferState)m_wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zstate.setEnabled(true);
        zstate.setWritable(false);
        zstate.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        jmeNode.setRenderState(zstate);

        jmeNode.setModelBound(new BoundingSphere());
        jmeNode.updateModelBound();

        
        TextureState ts = (TextureState)m_wm.getRenderManager().createRendererState(RenderState.RS_TEXTURE);
        ts.setTexture(TextureManager.loadTexture(ParticleCollection.class.getClassLoader().getResource("jmetest/data/texture/flaresmall.jpg"), Texture.MinificationFilter.BilinearNearestMipMap, Texture.MagnificationFilter.NearestNeighbor));
        ts.setEnabled(true);
        jmeNode.setRenderState(ts);
        jmeNode.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        jmeNode.updateRenderState();
    }
}
