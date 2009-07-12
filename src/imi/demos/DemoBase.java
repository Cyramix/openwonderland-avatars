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
package imi.demos;

import com.jme.bounding.BoundingSphere;
import imi.scene.SkyBox;
import com.jme.image.Texture;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.SharedMesh;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.shape.Quad;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.RenderState.StateType;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.scene.JScene;
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
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.RenderComponent;
import imi.repository.Repository;
import imi.camera.FirstPersonCamModel;
import imi.camera.AbstractCameraState;
import imi.camera.CameraModels;
import imi.camera.FirstPersonCamState;
import imi.camera.FlexibleCameraProcessor;
import imi.input.InputManagerEntity;
import imi.utils.instruments.DefaultInstrumentation;
import java.net.URL;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.RenderBuffer;


/**
 * Base starting point for demos, all demos are derived and override
 * createApplicationEntities(). Running this file directly will spawn a camera
 * in an empty environment and a skybox.
 */
public class DemoBase {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    /** Logger reference **/
    protected final static Logger logger = Logger.getLogger(DemoBase.class.getName());

    /** Convenience references for derived classes **/
    /** The WorldManager, only one may exist at a time **/
    static WorldManager  worldManager    = null;
    /** Asset repository **/
    protected Repository    repository      = null;
    /** The jMonkey Engine camera node we use in demos **/
    protected CameraNode    cameraNode      = null;

    /** View port options **/
    protected int          desiredFrameRate   = 60;
    protected int          width              = 800;
    protected int          height             = 600;
    protected float        aspect             = 800.0f/600.0f;
    
    /** Caches command line parameters for subclass usage **/
    private String[]    args = null;
    /** The camera processor **/
    protected FlexibleCameraProcessor m_cameraProcessor = null;

    protected Node   m_skyBox = null;

    protected String[] m_skyboxAssets = new String[] { "assets/textures/skybox/Front.png",
                                                       "assets/textures/skybox/Right.png",
                                                       "assets/textures/skybox/Back.png",
                                                       "assets/textures/skybox/Left.png",
                                                       "assets/textures/skybox/default.png",
                                                       "assets/textures/skybox/Top.png" };

    private ColorRGBA m_clearColor = new ColorRGBA(173.0f/255.0f, 195.0f/255.0f, 205.0f/255.0f, 1.0f);

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public static WorldManager getWM() {
        return worldManager;
    }
    /**
     * Construct a brand new instance!
     * @param args
     */
    protected DemoBase(String[] args) {
        logger.info("Current Directory: " + System.getProperty("user.dir"));
        this.args = args;
        worldManager = new WorldManager("DemoWorld");
        // Handle any arguments passed in
        processArgs(args);
        worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        // shutup jmonkey!
        Logger.getLogger("com.jme").setLevel(Level.SEVERE);
        Logger.getLogger("com.jmex").setLevel(Level.SEVERE);
        Logger.getLogger("org.collada").setLevel(Level.SEVERE);
        Logger.getLogger("imi.loaders.collada").setLevel(Level.SEVERE);
        Logger.getLogger("com.jme.scene.state.jogl.shader").setLevel(Level.OFF);

        // add the repository
        System.out.print("Building Repository...");
        repository = new Repository(worldManager);
        Thread.yield();
        System.out.println("done.");
        worldManager.addUserData(Repository.class, repository);

        System.out.print("Creating UI...");
        createUI(worldManager);
        System.out.println("done.");
        Thread.yield();
        System.out.print("Creating InputEntity...");
        createInputEntity(worldManager);
        System.out.println("done.");
        Thread.yield();
        System.out.print("Creating CameraEntity...");
        createCameraEntity(worldManager);
        System.out.println("done.");
        Thread.yield();
        System.out.print("Creating GlobalLighting...");
        setGlobalLighting(worldManager);
        System.out.println("done.");
        System.out.print("Creating Instrumentation...");
        createInstrumentation(worldManager);
        System.out.println("done.");
        System.out.print("Creating Application Entities...");
        createApplicationEntities(worldManager);
        System.out.println("done.");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new DemoBase(args);
    }

    /**
     * Create all of the Swing windows - and the 3D window
     */
    protected void createUI(WorldManager wm) {
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame with focus
        frame.canvas.requestFocusInWindow();

        frame.setVisible(true);

        // Add to the wm to set title string later during debugging
        wm.addUserData(OnscreenRenderBuffer.class, frame.m_renderBuffer);
    }

    public void createInputEntity(WorldManager wm)
    {
        new InputManagerEntity(wm);
    }

    protected void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);

        // Add the camera
        Entity camera = new Entity("DefaultCamera");

        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG,
                cameraNode,
                width, height,
                35.0f, // Field of view
                aspect, // Aspect ratio
                0.01f, // Near clip
                1000.0f, // far clip
                true);
        OnscreenRenderBuffer renderBuffer = (OnscreenRenderBuffer)wm.getUserData(OnscreenRenderBuffer.class);
        renderBuffer.setBackgroundColor(m_clearColor);
        
        camera.addComponent(CameraComponent.class, cc);
        renderBuffer.setCameraComponent(cc);

        // Skybox
        m_skyBox = createSkyBox(camera);

        m_cameraProcessor = new FlexibleCameraProcessor(cameraNode, wm, camera, m_skyBox, width, height);

        assignCameraType(wm);
        wm.addUserData(FlexibleCameraProcessor.class, m_cameraProcessor);
        wm.addUserData(AbstractCameraState.class, m_cameraProcessor.getState());

        m_cameraProcessor.setRunInRenderer(true);


        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(m_cameraProcessor);
        //pcc.addProcessor(selector);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);

        wm.addEntity(camera);
    }

    protected void setGlobalLighting(WorldManager wm) {
        // Lighting Configuration
        LightNode lightNode = new LightNode("Dis is me light node man!");
        // Must be a PointLight to function
        PointLight pointLight = new PointLight();
        pointLight.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        pointLight.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        pointLight.setEnabled(true);
        // attach it to the LightNode
        lightNode.setLight(pointLight);

        lightNode.setLocalTranslation(10.0f, 15.0f, -5.0f);
        // add it to the render manager
        wm.getRenderManager().addLight(lightNode);
    }

    protected void createInstrumentation(WorldManager worldManager) {
        new DefaultInstrumentation(worldManager);
    }

    protected void createApplicationEntities(WorldManager wm) {
        System.out.println("No application entities are created by default!");
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets up several default render states including the lighting
     * @param jscene
     * @param wm
     */
    public void setDefaultRenderStates(JScene jscene, WorldManager wm) {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // LightState
        LightState ls = (LightState)wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setEnabled(true);
        ls.setTwoSidedLighting(false);
        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setAmbient(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        matState.setDiffuse(ColorRGBA.white);

        // Cull State
        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        // Wireframe State
        WireframeState ws = (WireframeState) wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(false);

        // Push 'em down the pipe
        jscene.setRenderState(matState);
        jscene.setRenderState(buf);
        jscene.setRenderState(cs);
        jscene.setRenderState(ws);
        jscene.setRenderState(ls);
        jscene.updateRenderState();
    }

    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);

        return (cameraSG);
    }

    protected Node createSkyBox(Entity camera) {
        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, worldManager);
        sky.setTexture(SkyBox.NORTH,    loadSkyboxTexture(m_skyboxAssets[0]));  // +Z side
        sky.setTexture(SkyBox.EAST,     loadSkyboxTexture(m_skyboxAssets[1]));  // -X side
        sky.setTexture(SkyBox.SOUTH,    loadSkyboxTexture(m_skyboxAssets[2]));  // -Z side
        sky.setTexture(SkyBox.WEST,     loadSkyboxTexture(m_skyboxAssets[3]));  // +X side
        sky.setTexture(SkyBox.DOWN,     loadSkyboxTexture(m_skyboxAssets[4]));  // -Y Side
        sky.setTexture(SkyBox.UP,       loadSkyboxTexture(m_skyboxAssets[5]));  // +Y side

        RenderComponent sc2 = worldManager.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);

        return sky;
    }

    protected Texture loadSkyboxTexture(String filePath) {
        Texture monkeyTexture = null;
        if (filePath.contains("assets")) {
            try
            {
                monkeyTexture = TextureManager.loadTexture(new File(FileUtils.rootPath, filePath).toURI().toURL(), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
            } catch (MalformedURLException ex)
            {
                System.out.println(ex.getMessage());
            }
        } else {
            URL imageLocation   = getClass().getClassLoader().getResource(filePath);
            monkeyTexture = TextureManager.loadTexture(imageLocation, Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
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

    protected void assignCameraType(WorldManager wm) {
        FirstPersonCamState state = new FirstPersonCamState();
        state.setCameraPosition(new Vector3f(0, 2.2f, -6));
        FirstPersonCamModel model = (FirstPersonCamModel) CameraModels.getCameraModel(FirstPersonCamModel.class);
        m_cameraProcessor.setCameraBehavior(model, state);
    }

    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
                i++;
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

    public static Entity createSimpleFloor(WorldManager wm, float length1, float length2, float UVscale, Vector3f origin) {
        Entity floorEntity = new Entity("Floor Entity");
        Quad floorQuad = new Quad("Floor Quad", length1, length2);
        floorQuad.scaleTextureCoordinates(0, UVscale);
        textureMesh(floorQuad, "assets/textures/floor_tiles_karystoy.png", wm, 0);
        Node root = new Node("Floor Entity RC Root");
        root.setLocalTranslation(origin);
        setDefaultRenderStates(root, wm);
        root.attachChild(floorQuad);
        Matrix3f rotation = new Matrix3f();
        rotation.fromAngleAxis((float) Math.toRadians(-90), Vector3f.UNIT_X);
        floorQuad.setModelBound(new BoundingSphere());
        floorQuad.updateModelBound();
        floorQuad.setLocalRotation(rotation);
        root.updateWorldData(0.0f);
        root.updateGeometricState(0.0f, true);
        RenderComponent rc = wm.getRenderManager().createRenderComponent(root);
        floorEntity.addComponent(RenderComponent.class, rc);
        wm.addEntity(floorEntity);
        return floorEntity;
    }

    public static void textureMesh(Spatial mesh, String localPath, WorldManager wm, int textureUnit) {
        if (mesh instanceof SharedMesh || mesh instanceof TriMesh)
        {
            MaterialState matState = (MaterialState) (mesh).getRenderState(StateType.Material);
            if (matState == null)
                matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.StateType.Material);
            matState.setColorMaterial(MaterialState.ColorMaterial.None);
            matState.setMaterialFace(MaterialState.MaterialFace.Front);

            TextureState texState = (TextureState) mesh.getRenderState(StateType.Texture);
            if (texState == null)
            {
                texState = (TextureState) wm.getRenderManager().createRendererState(StateType.Texture);
                Texture base   = null;
                URL path       = null;
                String texLoc  = null;
                try {
                    texLoc  = localPath;
                    path    = DemoBase.class.getResource(texLoc);
                    if (path != null)
                        base = TextureManager.loadTexture(path);
                    else
                        base   = TextureManager.loadTexture(new File(texLoc).toURI().toURL());
                } catch (MalformedURLException ex) {
                    Logger.getLogger(DemoBase.class.getName()).log(Level.SEVERE, null, ex);
                }
                base.setApply(Texture.ApplyMode.Modulate);
                base.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                base.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
                base.setMinificationFilter(Texture.MinificationFilter.Trilinear);
                texState.setTexture(base, textureUnit);

                mesh.setRenderState(texState);
            } else {
                Texture base   = null;
                URL path       = null;
                String texLoc  = null;
                try {
                    texLoc  = localPath;
                    path    = DemoBase.class.getResource(texLoc);
                    if (path != null)
                        base = TextureManager.loadTexture(path);
                    else
                        base = TextureManager.loadTexture(new File(texLoc).toURI().toURL());

                } catch (MalformedURLException ex) {
                    Logger.getLogger(DemoBase.class.getName()).log(Level.SEVERE, null, ex);
                }
                base.setApply(Texture.ApplyMode.Modulate);
                base.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
                base.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
                base.setMinificationFilter(Texture.MinificationFilter.Trilinear);
                texState.setTexture(base, textureUnit);
                mesh.setRenderState(texState);
            }
        }
        mesh.updateRenderState();
    }

    public static void setDefaultRenderStates(Spatial spat, WorldManager worldManager) {

        // Z Buffer State
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) worldManager.getRenderManager().createRendererState(StateType.Material);
        matState.setAmbient(ColorRGBA.white);
        matState.setDiffuse(ColorRGBA.white);
        matState.setMaterialFace(MaterialState.MaterialFace.Front);
        matState.setColorMaterial(MaterialState.ColorMaterial.None);
        matState.setEnabled(true);

        // Cull State
        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        // Bounding volume
        spat.setModelBound(new BoundingSphere());
        spat.updateModelBound();

        spat.setRenderState(buf);
        spat.setRenderState(matState);
        spat.setRenderState(cs);
        spat.updateRenderState();
    }
}
