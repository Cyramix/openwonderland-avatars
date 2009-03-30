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
import com.jme.image.Texture;
import com.jme.image.Texture2D;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.TextureRenderer;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.character.avatar.Avatar;
import imi.character.avatar.FemaleAvatarAttributes;
import imi.environments.ColladaEnvironment;
import imi.loaders.repository.Repository;
import imi.scene.JScene;
import imi.scene.PScene;
import imi.scene.SkyBox;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.input.AvatarControlScheme;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.BufferUpdater;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderUpdater;
import org.jdesktop.mtgame.WorldManager;

/**
 * Base project that uses the BufferUpdater which makes sure the ogl window is
 * ready before continuing with the creation of the world.  This base creates an
 * FPS Camera, skybox and simple environment.
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class MTGameBase extends JFrame implements FrameRateListener {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    protected WorldManager              m_worldManager      = null;
    protected CameraNode                m_cameraNode        = null;
    protected CameraComponent           m_cameraComponent   = null;
    protected FlexibleCameraProcessor   m_cameraProcessor   = null;

    protected int                       m_desiredFrameRate  = 60;
    protected int                       m_width             = 600;
    protected int                       m_height            = 400;
    protected float                     m_aspect            = 600.0f/400.0f;

    protected Boolean                   m_loadSkybox        = Boolean.TRUE;
    protected final static Logger       m_logger            = Logger.getLogger(MTGameBase.class.getName());

    protected CustomDisplay             m_mainDisplay       = null;
    protected BufferedImage             bImage              = null;
    protected String[]                  m_ourArgs           = new String[] { "-env:/models/collada/Environments/Garden/Garden.dae" };
    protected URL                       m_urlToEnv          = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public MTGameBase(String[] args) {
        initWorldManager(100, 60, true, true);
        processArgs(m_ourArgs);

        System.out.println("Continuing with UI creation");
        createUI(m_worldManager);
    }

    /**
     * Run the test!
     * @param args
     */
    public static void main(String[] args) {
        MTGameBase test = new MTGameBase(args);
    }

    protected void createUI(WorldManager worldManager) {
        m_mainDisplay = new CustomDisplay(worldManager);
        m_mainDisplay.canvas.requestFocusInWindow();
        m_mainDisplay.setVisible(true);
        setLayout(new GridBagLayout());
        this.setContentPane(m_mainDisplay);
        this.setVisible(true);
        this.pack();
        worldManager.addUserData(JFrame.class, this);
    }

    protected void createTestSpace(WorldManager worldManager) {
        ColorRGBA color = new ColorRGBA();
        Vector3f center = new Vector3f();

        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        center.x = 0.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("Center", center, buf, color, worldManager);
        createSpace("Center", center, null, color, worldManager);
    }

    protected void createCameraEntity(WorldManager worldManager) {
        Node cameraSG = createCameraGraph(worldManager);

        Entity camera       = new Entity("DefaultCamera");
        m_cameraComponent   = worldManager.getRenderManager().createCameraComponent(cameraSG, m_cameraNode, m_width, m_height, 60.0f, m_aspect, 0.01f, 1000.0f, true);
        m_cameraComponent.setCameraSceneGraph(cameraSG);
        m_cameraComponent.setCameraNode(m_cameraNode);
        camera.addComponent(CameraComponent.class, m_cameraComponent);
        m_mainDisplay.renderBuffer.setCameraComponent(m_cameraComponent);

        SkyBox sky = createSkyBox(camera);

        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)worldManager.getInputManager().createInputComponent(m_mainDisplay.canvas, eventMask);
        m_cameraProcessor = new FlexibleCameraProcessor(cameraListener, m_cameraNode, worldManager, camera, sky);
        FirstPersonCamState state = new FirstPersonCamState();
        state.setCameraPosition(new Vector3f(0, 2.2f, -2));
        FirstPersonCamModel model = new FirstPersonCamModel();
        m_cameraProcessor.setCameraBehavior(model, state);

        worldManager.addUserData(FlexibleCameraProcessor.class, m_cameraProcessor);
        worldManager.addUserData(CameraState.class, m_cameraProcessor.getState());
        m_cameraProcessor.setRunInRenderer(true);

        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(m_cameraProcessor);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);

        worldManager.addEntity(camera);
    }

    protected void createInputEntity(WorldManager worldManager) {
        // Create input entity
        Entity InputEntity = new Entity("Input Entity");
        // Create event listener
        AWTInputComponent eventListener = (AWTInputComponent)worldManager.getInputManager().createInputComponent(m_mainDisplay.canvas, InputManager.KEY_EVENTS);
        // Create event processor
        JSceneAWTEventProcessor eventProcessor  = new JSceneAWTEventProcessor(eventListener, null, InputEntity);
        // Add the processor component to the entity
        InputEntity.addComponent(ProcessorComponent.class, eventProcessor);
        InputEntity.addComponent(AWTInputComponent.class, eventListener);
        // Add the entity to the world manager
        worldManager.addEntity(InputEntity);
        // Add the this input manager to the world manager for future access
        // (to asign a jscenes to drive)
        worldManager.addUserData(JSceneEventProcessor.class, eventProcessor);
    }

    protected void setGlobalLighting(WorldManager worldManager) {
        // Lighting Configuration
        LightNode lightNode = new LightNode("Dis is me light node man!");

        // Must be a PointLight to function
        PointLight pointLight = new PointLight();
        pointLight.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        pointLight.setAmbient(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        pointLight.setEnabled(true);

        // attach it to the LightNode
        lightNode.setLight(pointLight);
        lightNode.setLocalTranslation(10.0f, 15.0f, -5.0f);

        // add it to the render manager
        worldManager.getRenderManager().addLight(lightNode);
    }

    protected void createEnvironment(WorldManager worldManager, URL path) {
        if (path != null) {
            ColladaEnvironment environment = new ColladaEnvironment(worldManager, path, "TheWorld");
            worldManager.addUserData(ColladaEnvironment.class, environment);
        }
    }

    protected void createDemoEntities(WorldManager worldManager) {
        // The procedural scene graph
        PScene pscene = new PScene("PScene test", worldManager);

        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();

        // The glue between JME and pscene
        JScene jscene = new JScene(pscene);

        // Set this jscene to be the "selected" one for IMI input handling
        ((JSceneEventProcessor)worldManager.getUserData(JSceneEventProcessor.class)).setJScene(jscene);

        // Create entity
        Entity JSEntity = new Entity("Entity for a graph test");

        // Create a scene component and set the root to our jscene
        RenderComponent sc = worldManager.getRenderManager().createRenderComponent(jscene);

        // Add the scene component with our jscene to the entity
        JSEntity.addComponent(RenderComponent.class, sc);

        // Add our two processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));

        // Add the processor collection component to the entity
        JSEntity.addComponent(ProcessorCollectionComponent.class, processorCollection);

        // Add the entity to the world manager
        worldManager.addEntity(JSEntity);

        // Initialize the scene
        simpleSceneInit(jscene, worldManager, JSEntity, processors);
    }

    protected void simpleSceneInit(JScene jScene, WorldManager worldManager, Entity jsEntity, ArrayList<ProcessorComponent> processors) {
        // Add whatever you want to make here
        JSceneEventProcessor eventProcessor = (JSceneEventProcessor) worldManager.getUserData(JSceneEventProcessor.class);
        AvatarControlScheme control = (AvatarControlScheme)eventProcessor.setDefault(new AvatarControlScheme(null));

        FemaleAvatarAttributes  femaleAttribs   = new FemaleAvatarAttributes("RandomFemale", true);
        Avatar randomFemale = new Avatar(femaleAttribs, worldManager);

        randomFemale.selectForInput();
        control.getAvatarTeam().add(randomFemale);
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    private void initWorldManager(int freq, int frameRate, boolean loadSkeletons, boolean useCache) {
        m_worldManager = new WorldManager("TheManagerOfTheWorld");
        m_worldManager.getRenderManager().setFrameRateListener(this, freq);
        m_worldManager.getRenderManager().setDesiredFrameRate(frameRate);
        m_worldManager.addUserData(Repository.class, new Repository(m_worldManager, loadSkeletons, useCache));
    }

    private void createSpace(String name, Vector3f center, ZBufferState buf, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        Node node = new Node();

        // Add bounds and state for the whole space
        BoundingBox bbox = new BoundingBox(center, 500.0f, 500.0f, 500.0f);
        node.setModelBound(bbox);
        node.setRenderState(buf);
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);

        // Create a scene component for it
        RenderComponent sc = wm.getRenderManager().createRenderComponent(node);

        // Finally, create the space and add it.
        Entity e = new Entity(name + "Space");
        e.addComponent(ProcessorCollectionComponent.class, pcc);
        e.addComponent(RenderComponent.class, sc);
        wm.addEntity(e);
    }

    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");
        m_cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(m_cameraNode);

        return (cameraSG);
    }

    private SkyBox createSkyBox(Entity camera) {
        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, m_worldManager);
        sky.setTexture(SkyBox.NORTH, loadSkyboxTexture("/textures/skybox/Front.png"));  // +Z side
        sky.setTexture(SkyBox.EAST, loadSkyboxTexture("/textures/skybox/Right.png"));   // -X side
        sky.setTexture(SkyBox.SOUTH, loadSkyboxTexture("/textures/skybox/Back.png"));   // -Z side
        sky.setTexture(SkyBox.WEST, loadSkyboxTexture("/textures/skybox/Left.png"));    // +X side
        sky.setTexture(SkyBox.DOWN, loadSkyboxTexture("/textures/skybox/default.png")); // -Y Side
        sky.setTexture(SkyBox.UP, loadSkyboxTexture("/textures/skybox/Top.png"));       // +Y side

        RenderComponent sc2 = m_worldManager.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);

        return sky;
    }

    private Texture loadSkyboxTexture(String filePath) {
        Texture monkeyTexture = null;

        URL imageLocation   = getClass().getResource(filePath);
        monkeyTexture = TextureManager.loadTexture(imageLocation, Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);

        if (monkeyTexture != null) {
            monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            monkeyTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        }
        return monkeyTexture;
    }

    private void processArgs(String[] args) {

        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                m_desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + m_desiredFrameRate);
                i++;
            }
            else if (args[i].startsWith("-env:")) {
                String[] environmentArgs = args[i].split(":");
                if (environmentArgs.length < 2) {
                    m_urlToEnv  = null;
                }
                else if (environmentArgs[1].equals("none")) {
                    m_urlToEnv  = null;
                }
                else {
                    m_urlToEnv  = getClass().getResource(environmentArgs[1]);
                }
            }
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Implementations
////////////////////////////////////////////////////////////////////////////////

    public void currentFramerate(float arg0) {
        if (m_mainDisplay != null) {
            m_mainDisplay.fpsLabel.setText("FPS: " + arg0);
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Classes
////////////////////////////////////////////////////////////////////////////////

    public class CustomDisplay extends JPanel implements BufferUpdater {

        JPanel          canvasPanel     = new JPanel();
        JPanel          fpsPanel        = new JPanel();
        Canvas          canvas          = null;
        JLabel          fpsLabel        = new JLabel("FPS: ");
        RenderBuffer    renderBuffer    = null;
        boolean         first           = true;
        int             m_width         = 800;
        int             m_height        = 600;

        public CustomDisplay(WorldManager wm, int width, int height) {
            m_width = width;
            m_height = height;

            GridBagConstraints canvasConstraints = new GridBagConstraints();
            canvasConstraints.gridx = 0;
            canvasConstraints.gridy = 0;
            canvasConstraints.fill = java.awt.GridBagConstraints.BOTH;
            canvasConstraints.weightx = 1.0;
            canvasConstraints.weighty = 1.0;

            GridBagConstraints fpsConstraints = new GridBagConstraints();
            fpsConstraints.gridx = 0;
            fpsConstraints.gridy = 1;
            fpsConstraints.fill = java.awt.GridBagConstraints.BOTH;
            fpsConstraints.weightx = 1.0;
            fpsConstraints.weighty = 0.0;

            setLayout(new GridBagLayout());

            // The rendering canvas
            renderBuffer = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, m_width, m_height);
            wm.getRenderManager().addRenderBuffer(renderBuffer);
            canvas = renderBuffer.getCanvas();
            renderBuffer.setBufferUpdater(this);
            canvas.setVisible(true);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            add(canvasPanel, canvasConstraints);

            // The fps panel
            fpsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            fpsPanel.add(fpsLabel);
            add(fpsPanel, fpsConstraints);

        }

        public CustomDisplay(WorldManager wm) {

            GridBagConstraints canvasConstraints = new GridBagConstraints();
            canvasConstraints.gridx = 0;
            canvasConstraints.gridy = 0;
            canvasConstraints.fill = java.awt.GridBagConstraints.BOTH;
            canvasConstraints.weightx = 1.0;
            canvasConstraints.weighty = 1.0;

            GridBagConstraints buttonConstraints = new GridBagConstraints();
            buttonConstraints.gridx = 0;
            buttonConstraints.gridy = 1;
            buttonConstraints.fill = java.awt.GridBagConstraints.BOTH;
            buttonConstraints.weightx = 1.0;
            buttonConstraints.weighty = 0.0;

            setLayout(new GridBagLayout());

            // The rendering canvas
            renderBuffer = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, m_width, m_height);
            wm.getRenderManager().addRenderBuffer(renderBuffer);
            canvas = renderBuffer.getCanvas();
            renderBuffer.setBufferUpdater(this);
            canvas.setVisible(true);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            add(canvasPanel, canvasConstraints);

            // The fps panel
            fpsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            fpsPanel.add(fpsLabel);
            add(fpsPanel, buttonConstraints);

        }

        @Override
        public void init(RenderBuffer rb) {
            System.out.println("Continuing with test space creation");
            createTestSpace(m_worldManager);

            System.out.println("Continuing with camera entity creation");
            createCameraEntity(m_worldManager);

            System.out.println("Continuing with input entity creation");
            createInputEntity(m_worldManager);

            System.out.println("Continuing with global lighting setup");
            setGlobalLighting(m_worldManager);

            System.out.println("Continuing with environment creation");
            createEnvironment(m_worldManager, m_urlToEnv);

            System.out.println("Continuing with creation of avatar");
            createDemoEntities(m_worldManager);
        }
    }

    /**
     * Implementing from RenderUpdater makes this update in the ogl thread which
     * any sort of rendering must be done in (only one ogl window open at any time)
     */
    public class TextureRendererUpdater implements RenderUpdater {

        TextureRenderer m_tRender   = null;
        Node            m_fNode     = null;
        Texture2D       m_fTex      = null;

        public TextureRendererUpdater(TextureRenderer tRenderer, Node fakeScene, Texture2D t2D) {
            m_tRender   = tRenderer;
            m_fNode     = fakeScene;
            m_fTex      = t2D;
        }

        public void update(Object arg0) {
            // Update the texture
            m_tRender.render(m_fNode, m_fTex);

            // Add it in to be updated next cycle
            m_worldManager.addRenderUpdater(this, arg0);
        }

    }
}