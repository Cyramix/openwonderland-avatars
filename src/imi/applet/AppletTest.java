/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.applet;

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
import imi.loaders.repository.Repository;
import imi.scene.SkyBox;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.BufferUpdater;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class AppletTest extends JApplet implements FrameRateListener {
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
    protected final static Logger       m_logger            = Logger.getLogger(AppletTest.class.getName());

    protected CustomDisplay             m_mainDisplay       = null;
    protected CustomCanvas              m_loadingCanvas     = null;

    protected BufferedImage             bImage              = null;
    
////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    private void createWindow() {
        m_worldManager = new WorldManager("TheManagerOfTheWorld");
        m_worldManager.getRenderManager().setFrameRateListener(this, 100);
        m_worldManager.getRenderManager().setDesiredFrameRate(m_desiredFrameRate);
        m_worldManager.addUserData(Repository.class, new Repository(m_worldManager));

        createUI(m_worldManager);
    }

    private void swapDisplay() {
        this.setContentPane(m_mainDisplay);
    }

////////////////////////////////////////////////////////////////////////////////
// JApplet Specific Code
////////////////////////////////////////////////////////////////////////////////
    /**
     * Initialization method that will be called after the applet is loaded
     * into the browser.
     */
    @Override
    public void init() {
        createLoadingScreen();
        createWindow();
        swapDisplay();
    }

    // TODO overwrite start(), stop() and destroy() methods
    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        //Execute a job on the event-dispatching thread:
        //destroying this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    remove(getContentPane());
                }
            });
        } catch (Exception e) { }
    }
    
////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public WorldManager getWorldManager() {
        return m_worldManager;
    }

    public CameraNode getCameraNode() {
        return m_cameraNode;
    }

    public FlexibleCameraProcessor getCameraProcessor() {
        return m_cameraProcessor;
    }

    public int getFrameRate() {
        return m_desiredFrameRate;
    }

    public int getDisplayWidth() {
        return m_width;
    }

    public int getDisplayHeight() {
        return m_height;
    }

    public float getAspectRatio() {
        return m_aspect;
    }

    public Boolean isLoadingSkybox() {
        return m_loadSkybox;
    }

    public CustomDisplay getDisplayPanel() {
        return m_mainDisplay;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setWorldManager(WorldManager worldManager) {
        m_worldManager = worldManager;
    }

    public void setCameraNode(CameraNode cameraNode) {
        m_cameraNode = cameraNode;
    }

    public void setCameraProcessor(FlexibleCameraProcessor cameraProcessor) {
        m_cameraProcessor = cameraProcessor;
    }

    public void setFrameRate(int desiredFrameRate) {
        m_desiredFrameRate = desiredFrameRate;
    }

    public void setDisplayWidth(int displayWidth) {
        m_width = displayWidth;
    }

    public void setDisplayHeight(int displayHeight) {
        m_height = displayHeight;
    }

    public void setAspectRatio(float aspectRatio) {
        m_aspect = aspectRatio;
    }

    public void calculateAspectRatio() {
        float aspectRatio = (float) m_width / (float) m_height;
        m_aspect = aspectRatio;
    }

    public void setLoadingSkybox(boolean onOroff) {
        m_loadSkybox = onOroff;
    }

    public void setDisplayPanel(CustomDisplay display) {
        m_mainDisplay = display;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void createUI(WorldManager worldManager) {
        m_mainDisplay = new CustomDisplay(worldManager);
        m_mainDisplay.canvas.requestFocusInWindow();
        worldManager.addUserData(AppletTest.class, this);
    }

    public void createTestSpace(WorldManager worldManager) {
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

    public void createCameraEntity(WorldManager worldManager) {
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

    public void createInputEntity(WorldManager worldManager) {
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

    public void setGlobalLighting(WorldManager worldManager) {
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

    public void createDemoEntities(WorldManager wm) {

    }

    private void createLoadingScreen() {
        
        // Set contentpane
        JPanel loadingPanel = (JPanel) this.getContentPane();
        loadingPanel.setLayout(new BorderLayout());

        // Create canvas
        m_loadingCanvas             = new CustomCanvas();
        m_loadingCanvas.setSize(m_loadingCanvas.iWidth, m_loadingCanvas.iHeight);
        m_loadingCanvas.setVisible(true);

        // Add canvas to display panel
        JPanel splashPanel = new JPanel();
        splashPanel.setLayout(new GridBagLayout());
        splashPanel.add(m_loadingCanvas);
        splashPanel.setVisible(true);
        
        // Add display panel to the contentpane
        loadingPanel.add(splashPanel, BorderLayout.CENTER);
        loadingPanel.setVisible(true);
        this.setSize(m_loadingCanvas.iWidth, m_loadingCanvas.iHeight);
        
        // Force a repaint
        m_loadingCanvas.repaint();
        repaint();
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public void createSpace(String name, Vector3f center, ZBufferState buf, ColorRGBA color, WorldManager wm) {
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
        sky.setTexture(SkyBox.NORTH, loadSkyboxTexture("assets/textures/skybox/pos_z.bmp"));  // north
        sky.setTexture(SkyBox.EAST, loadSkyboxTexture("assets/textures/skybox/pos_x.bmp"));  // south
        sky.setTexture(SkyBox.SOUTH, loadSkyboxTexture("assets/textures/skybox/neg_z.bmp"));   // east
        sky.setTexture(SkyBox.WEST, loadSkyboxTexture("assets/textures/skybox/neg_x.bmp"));   // west
        sky.setTexture(SkyBox.DOWN, loadSkyboxTexture("assets/textures/skybox/neg_y.bmp"));    // up
        sky.setTexture(SkyBox.UP, loadSkyboxTexture("assets/textures/skybox/pos_y.bmp"));    // down

        RenderComponent sc2 = m_worldManager.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);

        return sky;
    }

    private Texture loadSkyboxTexture(String filePath) {
        Texture monkeyTexture = null;

//        ImageIcon icon  = new ImageIcon(AppletTest.class.getResource("imilogo.png"));
//        Image image     = icon.getImage();
        Image image = getImage(getDocumentBase(), filePath);
        System.out.println(getDocumentBase().toString() + filePath);
        monkeyTexture = TextureManager.loadTexture(image, Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor, false);

        if (monkeyTexture != null) {
            monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            monkeyTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        }
        return monkeyTexture;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Classes
////////////////////////////////////////////////////////////////////////////////

    public class CustomCanvas extends Canvas {

        BufferedImage   bImage;
        Image           image;
        int             iWidth;
        int             iHeight;
        
        public CustomCanvas() {
            ImageIcon icon  = new ImageIcon(AppletTest.class.getResource("imilogo.png"));
            image           = icon.getImage();
            System.out.println("Image Width: " + image.getWidth(this) + "\tImage Height: " + image.getHeight(this));

            if (image.getWidth(this) < 0) {
                iWidth  = 600;
                iHeight = 400;
            } else {
                iWidth  = image.getWidth(this);
                iHeight = image.getHeight(this);
            }

            bImage = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = bImage.createGraphics();
            graphics.drawImage(image, 0, 0, this);
            graphics.dispose();
        }
        
        @Override
        public void paint(Graphics g) {
            Graphics graphics = bImage.getGraphics();
            graphics.drawImage(image, 0, 0, this);
            graphics.dispose();

            Graphics2D g2D = (Graphics2D) g;
            g2D.drawImage(bImage, 0, 0, this);
            g2D.dispose();
        }

        public int getImageWidth() {
            return iWidth;
        }

        public int getImageHeight() {
            return iHeight;
        }
    }

    public class CustomDisplay extends JPanel implements BufferUpdater {

        JPanel canvasPanel          = new JPanel();
        JPanel fpsPanel             = new JPanel();
        Canvas canvas               = null;
        JLabel fpsLabel             = new JLabel("FPS: ");
        RenderBuffer renderBuffer   = null;
        boolean first               = true;

        public CustomDisplay(WorldManager wm) {

            setLayout(new BorderLayout());

            // The rendering canvas
            renderBuffer = wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, m_width, m_height);
            wm.getRenderManager().addRenderBuffer(renderBuffer);
            canvas = renderBuffer.getCanvas();
            renderBuffer.setBufferUpdater(this);
            canvas.setVisible(true);
            canvasPanel.setLayout(new GridBagLayout());
            canvasPanel.add(canvas);
            add(canvasPanel, BorderLayout.CENTER);

            // The fps panel
            fpsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            fpsPanel.add(fpsLabel);
            add(fpsPanel, BorderLayout.SOUTH);
        }

        public void init(RenderBuffer rb) {
            System.out.println("Continuing");
            createTestSpace(m_worldManager);
            System.out.println("Continuing 2");
            createCameraEntity(m_worldManager);
            createInputEntity(m_worldManager);
            setGlobalLighting(m_worldManager);
            createDemoEntities(m_worldManager);
        }
    }

////////////////////////////////////////////////////////////////////////////////
// Implementations
////////////////////////////////////////////////////////////////////////////////
    public void currentFramerate(float arg0) {

    }
}
