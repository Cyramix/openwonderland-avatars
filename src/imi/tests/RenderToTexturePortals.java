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

package imi.tests;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TexCoords;
import com.jme.scene.shape.Box;
import com.jme.scene.shape.Torus;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.character.avatar.Avatar;
import imi.character.avatar.MaleAvatarAttributes;
import imi.environments.ColladaEnvironment;
import imi.portals.IMI_PortalsManager;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.SkyBox;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.AvatarControlScheme;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Vector;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.RotationProcessor;

/**
 *
 * @author ptruong
 */
public class RenderToTexturePortals extends DemoBase {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private Vector<Vector3f>    m_portalPositions   = new Vector<Vector3f>();
    private Vector<String>      m_cubeTextures      = new Vector<String>();

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public RenderToTexturePortals(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        RenderToTexturePortals test = new RenderToTexturePortals(args);
    }

    protected void createEnvironment(WorldManager worldManager, Vector3f position) {
        URL path = getClass().getResource("/models/collada/Environments/Arena/Arena.dae");
        ColladaEnvironment environment = new ColladaEnvironment(worldManager, path, "TheWorld");
        environment.getJMENode().setLocalTranslation(position);
        worldManager.addUserData(ColladaEnvironment.class, environment);
    }

    protected SkyBox createSkyBox(Entity camera) {
//        m_skyboxAssets = new String[] { "/textures/skybox/Front.png",
//                                        "/textures/skybox/Right.png",
//                                        "/textures/skybox/Back.png",
//                                        "/textures/skybox/Left.png",
//                                        "/textures/skybox/default.png",
//                                        "/textures/skybox/Top.png" };
//
//        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, worldManager);
//        sky.setTexture(SkyBox.NORTH,    loadSkyboxTexture(m_skyboxAssets[0]));  // +Z side
//        sky.setTexture(SkyBox.EAST,     loadSkyboxTexture(m_skyboxAssets[1]));  // -X side
//        sky.setTexture(SkyBox.SOUTH,    loadSkyboxTexture(m_skyboxAssets[2]));  // -Z side
//        sky.setTexture(SkyBox.WEST,     loadSkyboxTexture(m_skyboxAssets[3]));  // +X side
//        sky.setTexture(SkyBox.DOWN,     loadSkyboxTexture(m_skyboxAssets[4]));  // -Y Side
//        sky.setTexture(SkyBox.UP,       loadSkyboxTexture(m_skyboxAssets[5]));  // +Y side
//
//        RenderComponent sc2 = worldManager.getRenderManager().createRenderComponent(sky);
//        camera.addComponent(RenderComponent.class, sc2);

        return null;
    }

    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) {
        m_portalPositions   = new Vector<Vector3f>();
        m_cubeTextures      = new Vector<String>();

        m_portalPositions.add(new Vector3f(-10.0f, 2.0f,  0.0f));
        m_portalPositions.add(new Vector3f(  5.0f, 2.0f, 10.0f));
        m_portalPositions.add(new Vector3f(  0.0f, 2.0f, 15.0f));
        m_portalPositions.add(new Vector3f(  0.0f, 2.0f, 35.0f));

        m_cubeTextures.add("/textures/Symbol.png");
        m_cubeTextures.add("/textures/imilogo.png");
        m_cubeTextures.add("/textures/default.png");
        m_cubeTextures.add("/jmetest/data/images/Monkey.jpg");

        ////////////////////////////////////////////////////////////////////////
        // Create 1st environment
        createEnvironment(worldManager, new Vector3f());
        createCubes();
        
        ////////////////////////////////////////////////////////////////////////
        // Create 2nd environment;
        createEnvironment(worldManager, new Vector3f(0.0f, 0.0f, 50.0f));
        createObjects(new Vector3f(0.0f, 0.0f, 50.0f));

        ////////////////////////////////////////////////////////////////////////
        // Create avatar
        createAvatarForInput(worldManager, pscene);

        ////////////////////////////////////////////////////////////////////////
        // Create Zbuffer state for portals
        ZBufferState buf = wm.getRenderManager().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        ////////////////////////////////////////////////////////////////////////
        // Create portals

        PMatrix rotY    = new PMatrix();
        rotY.buildRotationY((float) (Math.toRadians(180)));
//        PMatrix portal1 = new PMatrix(m_portalPositions.get(0));
//        PMatrix portal2 = new PMatrix(m_portalPositions.get(1));
        PMatrix portal3 = new PMatrix(m_portalPositions.get(2));
        portal3.mul(rotY);
        PMatrix portal4 = new PMatrix(m_portalPositions.get(3));

        IMI_PortalsManager pm   = new IMI_PortalsManager("PortalsMaster", width, height);
        pm.setCollisionSystem(worldManager);
        pm.setSkyBox(m_skyBox);

//        pm.createPortal("Portal1", portal1, new Vector3f(3*0.8f, 3*0.6f, 0.1f), buf, m_portalPositions.get(1), worldManager);
//        pm.createPortal("Portal2", portal2, new Vector3f(3*0.8f, 3*0.6f, 0.1f), buf, m_portalPositions.get(0), worldManager);

        pm.createPortal("Portal3", portal3, new Vector3f(6*0.8f, 6*0.6f, 0.1f), buf, m_portalPositions.get(3), worldManager, null);
        pm.createPortal("Portal4", portal4, new Vector3f(6*0.8f, 6*0.6f, 0.1f), buf, m_portalPositions.get(2), worldManager, rotY.getRotation());
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    private void createCube(String cubeName, Vector3f cubeSize, Vector3f cubePos, String cubeTexture) {

        Entity boxEntity    = new Entity(cubeName);
        ZBufferState buf = worldManager.getRenderManager().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        Box realBox = new Box(cubeName, new Vector3f(0.0f, 0.0f, 0.0f), cubeSize.x, cubeSize.y, cubeSize.z);
        realBox.setModelBound(new BoundingSphere());
        realBox.updateModelBound();

        Node boxNode = new Node(cubeName);
        boxNode.attachChild(realBox);
        boxNode.setLocalTranslation(cubePos);
        boxNode.setRenderState(buf);

        TextureState ts = worldManager.getRenderManager().createTextureState();
        ts.setEnabled(true);
        Texture tex = TextureManager.loadTexture(
                getClass().getResource(cubeTexture),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        ts.setTexture(tex, 0);
        boxNode.setRenderState(ts);

        RotationProcessor rp = new RotationProcessor("Cube Rotator", worldManager, boxNode, (float) (0.5f * Math.PI / 180.0f));
        rp.setRunInRenderer(true);
        
        RenderComponent rc      = worldManager.getRenderManager().createRenderComponent(boxNode);
        rc.setOrtho(false);
        rc.setLightingEnabled(true);
        boxEntity.addComponent(RenderComponent.class, rc);
        boxEntity.addComponent(ProcessorComponent.class, rp);
        worldManager.addEntity(boxEntity);
    }

    private void createObjects(Vector3f position) {
        Node                cubesN  = new Node("Cubes");
        Node                torusN  = new Node("Torus");
        Torus               torus   = null;
        Box                 box     = null;
        TextureState        ts      = null;
        Texture             t0      = null;
        Texture             t1      = null;
        Vector3f            min     = new Vector3f(-0.5f, -0.5f, -0.5f);
        Vector3f            max     = new Vector3f( 0.5f,  0.5f,  0.5f);
        RotationProcessor   rp      = null;

        ZBufferState buf = worldManager.getRenderManager().createZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        ////////////////////////////////////////////////////////////////////////
        // TORUS OBJECT
        torus = new Torus("Torus", 25, 25, 1, 3);
        torus.setLocalTranslation(new Vector3f(0, 5.0f, 0));
        ts = worldManager.getRenderManager().createTextureState();
        t0 = TextureManager.loadTexture(
                getClass().getResource("/jmetest/data/images/Monkey.jpg"),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t1 = TextureManager.loadTexture(
                getClass().getResource("/jmetest/data/texture/north.jpg"),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t1.setEnvironmentalMapMode(Texture.EnvironmentalMapMode.SphereMap);
        ts.setTexture(t0, 0);
        ts.setTexture(t1, 1);
        ts.setEnabled(true);
        torus.setRenderState(ts);
        
        torusN.attachChild(torus);
        torusN.setRenderState(buf);
        torusN.setLocalTranslation(position);
        rp = new RotationProcessor("Moving Objects", worldManager, torusN, (float) (0.7f * Math.PI / 180.0f));

        RenderComponent rt = worldManager.getRenderManager().createRenderComponent(torusN);
        Entity toruz = new Entity("Cubes");
        toruz.addComponent(RotationProcessor.class, rp);
        toruz.addComponent(RenderComponent.class, rt);
        worldManager.addEntity(toruz);

        ////////////////////////////////////////////////////////////////////////
        // CUBE OBJECTS
        ts = worldManager.getRenderManager().createTextureState();
        t0 = TextureManager.loadTexture(
                getClass().getResource("/jmetest/data/texture/wall.jpg"),
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t0);

        box = new Box("box1", min, max);
        box.setLocalTranslation(new Vector3f(-7, 0.5f, 0));
        box.setRenderState(ts);
        cubesN.attachChild(box);

        box = new Box("box2", min, max);
        box.setLocalTranslation(new Vector3f(-9, 0.5f, 0));
        box.setRenderState(ts);
        cubesN.attachChild(box);

        box = new Box("box3", min, max);
        box.setLocalTranslation(new Vector3f(-8, 1.5f, 0));
        box.setRenderState(ts);
        cubesN.attachChild(box);

        box = new Box("box4", min, max);
        box.setLocalTranslation(new Vector3f(-8, 0.5f, -1));
        box.setRenderState(ts);
        cubesN.attachChild(box);

        box = new Box("box5", min, max);
        box.setLocalTranslation(new Vector3f(-8, 0.5f, 1));
        box.setRenderState(ts);
        cubesN.attachChild(box);

        cubesN.setRenderState(buf);
        cubesN.setLocalTranslation(position);

        RenderComponent rc  = worldManager.getRenderManager().createRenderComponent(cubesN);
        Entity cubez = new Entity("Cubes");
        cubez.addComponent(RenderComponent.class, rc);
        worldManager.addEntity(cubez);
    }

    private void createCubes() {
        // Create cubes for testing
        Vector3f cubPos = new Vector3f(m_portalPositions.get(0));
        cubPos.x += 2;  cubPos.y -= 0.2f;  cubPos.z += 5;
        createCube("Cube1", new Vector3f(1.5f, 1.5f, 1.5f), cubPos, m_cubeTextures.get(0));

        cubPos = new Vector3f(m_portalPositions.get(0));
        cubPos.x -= 1.5f;  cubPos.y -= 1.1f;  cubPos.z += 7.0f;
        createCube("Cube2", new Vector3f(0.5f, 0.5f, 0.5f), cubPos, m_cubeTextures.get(3));

        cubPos = new Vector3f(m_portalPositions.get(1));
        cubPos.x += 1.3f;  cubPos.y -= 1.5f;  cubPos.z -= 3.75f;
        createCube("Cube3", new Vector3f(0.4f, 0.4f, 0.4f), cubPos, m_cubeTextures.get(2));
    }

    private void createAvatarForInput(WorldManager worldManager, PScene pscene) {

        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) worldManager.getUserData(JSceneEventProcessor.class);
        AvatarControlScheme control = (AvatarControlScheme)eventProcessor.setDefault(new AvatarControlScheme(null));

        MaleAvatarAttributes maleAttrib = new MaleAvatarAttributes("TestGuy",0, 0, 0, 8, 0, 0, 0);
        maleAttrib.setBaseURL("http://www.zeitgeistgames.com/");
        maleAttrib.setOrigin(new PMatrix(new Vector3f(0.0f, 0.0f, -5.0f)));
        Avatar  TestGuy  = new Avatar(maleAttrib, worldManager);
        
        TestGuy.selectForInput();
        control.getAvatarTeam().add(TestGuy);
    }
}
