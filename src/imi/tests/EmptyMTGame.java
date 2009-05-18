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
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.scene.SkyBox;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.utils.FileUtils;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class provides a test that only gets MTGame up and running.
 * @author Ronald E Dahlgren
 */
public class EmptyMTGame
{
    /** Logger reference **/
    protected final static Logger logger = Logger.getLogger(EmptyMTGame.class.getName());

    /** The jMonkey Engine camera node we use in demos **/
    protected CameraNode    cameraNode      = null;

    /** The world manager **/
    protected WorldManager  worldManager    = null;

    /** View port options **/
    private int          desiredFrameRate   = 60;
    private int          width              = 800;
    private int          height             = 600;
    private float        aspect             = 800.0f/600.0f;

    /** The camera processor **/
    protected FlexibleCameraProcessor m_cameraProcessor = null;

    /** Control the loading of the skybox **/
    protected Boolean loadSkybox = Boolean.TRUE;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
//        EmptyMTGame worldTest = new EmptyMTGame(new String[] {"-nosky"}); // <-- no skybox
        EmptyMTGame worldTest = new EmptyMTGame(args);
    }

    
    /**
     * Construct a brand new instance!
     * @param args
     */
    protected EmptyMTGame(String[] args)
    {
        // Handle any arguments passed in
        processArgs(args);
        // Allocate the world manager
        worldManager = new WorldManager("TheManagerOfTheWorld");

        createUI(worldManager);
        createTestSpace(worldManager);
        createCameraEntity(worldManager);
        createInputEntity(worldManager);
        setGlobalLighting(worldManager);
    }

    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm)
    {
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame with focus
        frame.canvas.requestFocusInWindow();

        frame.setVisible(true);

        // Add to the wm to set title string later during debugging
        wm.addUserData(JFrame.class, frame);
    }

    /**
     * Creates the space that the test runs in
     * @param wm
     */
    private void createTestSpace(WorldManager wm)
    {
        ColorRGBA color = new ColorRGBA();
        Vector3f center = new Vector3f();

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // First create the geometry
        center.x = 0.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("Center", center, buf, color, wm);
    }

    public void createInputEntity(WorldManager wm)
    {
        // Create input entity
        Entity InputEntity = new Entity("Input Entity");
        // Create event listener
        Canvas canvas = ((SwingFrame)wm.getUserData(JFrame.class)).getRenderBuffer().getCanvas();
        AWTInputComponent eventListener = (AWTInputComponent)worldManager.getInputManager().createInputComponent(canvas, InputManager.KEY_EVENTS);
        // Create event processor
        JSceneAWTEventProcessor eventProcessor  = new JSceneAWTEventProcessor(eventListener, null, InputEntity);
        // Add the processor component to the entity
        InputEntity.addComponent(ProcessorComponent.class, eventProcessor);
        InputEntity.addComponent(AWTInputComponent.class, eventListener);
        // Add the entity to the world manager
        wm.addEntity(InputEntity);
        // Add the this input manager to the world manager for future access
        // (to asign a jscenes to drive)
        wm.addUserData(JSceneEventProcessor.class, eventProcessor);
    }
    
    protected void setGlobalLighting(WorldManager wm)
    {
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
        wm.getRenderManager().addLight(lightNode);
    }

   public void createSpace(String name, Vector3f center, ZBufferState buf,
            ColorRGBA color, WorldManager wm)
   {
        MaterialState matState = null;

        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();

        // Create the root for the space
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

    

    private Texture loadSkyboxTexture(String filePath)
    {
        Texture monkeyTexture = null;
        try
        {
            monkeyTexture = TextureManager.loadTexture(new File(FileUtils.rootPath, filePath).toURI().toURL(), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(DemoBase.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (monkeyTexture != null)
        {
            monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            monkeyTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        }
        return monkeyTexture;
    }


    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);

        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode,
                width, height, 60.0f, aspect, 0.01f, 1000.0f, true);

        OnscreenRenderBuffer renderBuffer = ((SwingFrame)wm.getUserData(JFrame.class)).getRenderBuffer();

        camera.addComponent(CameraComponent.class, cc);
        renderBuffer.setCameraComponent(cc);
        
        SkyBox sky = null;
        if (loadSkybox)
        {
            // Skybox
            sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, wm);
            sky.setTexture(SkyBox.NORTH,   loadSkyboxTexture("assets/textures/skybox/Front.png")); // north
            sky.setTexture(SkyBox.EAST,    loadSkyboxTexture("assets/textures/skybox/Right.png")); // south
            sky.setTexture(SkyBox.SOUTH,   loadSkyboxTexture("assets/textures/skybox/Back.png")); // east
            sky.setTexture(SkyBox.WEST,    loadSkyboxTexture("assets/textures/skybox/Left.png")); // west
            sky.setTexture(SkyBox.DOWN,    loadSkyboxTexture("assets/textures/skybox/Top.png")); // up
            sky.setTexture(SkyBox.UP,      loadSkyboxTexture("assets/textures/skybox/Top.png")); // down
            //
            RenderComponent sc2 = wm.getRenderManager().createRenderComponent(sky);
            camera.addComponent(RenderComponent.class, sc2);
        }

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        Canvas canvas = renderBuffer.getCanvas();
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);

        m_cameraProcessor = new FlexibleCameraProcessor(cameraListener, cameraNode, wm, camera, sky, width, height);

        assignCameraType(wm);
        wm.addUserData(FlexibleCameraProcessor.class, m_cameraProcessor);
        wm.addUserData(CameraState.class, m_cameraProcessor.getState());

        m_cameraProcessor.setRunInRenderer(true);


        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(m_cameraProcessor);
        //pcc.addProcessor(selector);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);

        wm.addEntity(camera);
    }

    /**
     * This method should be overridden in order to change the camera used in a
     * demo / test file. Just call m_cameraProcessor.setCameraBehavior
     * @return
     */
    protected void assignCameraType(WorldManager wm)
    {
        FirstPersonCamState state = new FirstPersonCamState();
        state.setCameraPosition(new Vector3f(0, 2.2f, -2));
        FirstPersonCamModel model = new FirstPersonCamModel();
        m_cameraProcessor.setCameraBehavior(model, state);
    }

    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);

        return (cameraSG);
    }

    /**
     * Process any command line args
     */
    private void processArgs(String[] args)
    {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
            }
            if (args[i].equalsIgnoreCase("-NoSky")) {
                loadSkybox = false;
            }
        }
    }

    public class SwingFrame extends JFrame implements FrameRateListener, ActionListener {

        JPanel contentPane;
        JPanel canvasPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        Canvas canvas = null;
        JLabel fpsLabel = new JLabel("FPS: ");
        OnscreenRenderBuffer m_renderBuffer = null;


        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());

            // The Menu Bar
            JMenuBar menuBar = new JMenuBar();

            // File Menu
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            // Create Menu
            JMenu createMenu = new JMenu("Create");
            menuBar.add(createMenu);

            // The Rendering Canvas
            m_renderBuffer = (OnscreenRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, width, height);
            wm.getRenderManager().addRenderBuffer(m_renderBuffer);
            canvas = m_renderBuffer.getCanvas();
            canvas.setVisible(true);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);

            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
        }

        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }

        public void actionPerformed(ActionEvent e)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public OnscreenRenderBuffer getRenderBuffer()
        {
            return m_renderBuffer;
        }
    }
}
