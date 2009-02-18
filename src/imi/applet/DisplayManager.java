/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.applet;
////////////////////////////////////////////////////////////////////////////////
// Imports
////////////////////////////////////////////////////////////////////////////////
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.util.TextureManager;
import imi.scene.SkyBox;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.state.CameraState;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.utils.FileUtils;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Container;
import java.io.File;
import java.net.MalformedURLException;
import javax.swing.JPanel;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class DisplayManager {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////
    
    // Display resolution
    private int                         m_width             = 600;
    private int                         m_height            = 400;
    private float                       m_aspect            = 600.0f/400.0f;

    // Camera Objects
    private CameraNode                  m_cameraNode        = null;
    private CameraComponent             m_cameraComponent   = null;
    private FlexibleCameraProcessor     m_cameraProcessor   = null;

    // GUI Objects
    private Canvas                      m_renderCanvas      = null;
    private RenderBuffer                m_renderBuffer      = null;

    // Managers
    private WorldManager                m_worldManager      = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public DisplayManager() {
        // Creates a display manager with default settings
        // Must still seet WorldManager
    }

    public DisplayManager(int displayWidth, int displayHeight, WorldManager wm) {
        if (displayWidth > 0)
            m_width = displayWidth;

        if (displayHeight > 0)
            m_height = displayHeight;

        m_aspect = (float)m_width / (float)m_height;

        m_worldManager = wm;
    }

    public DisplayManager(int displayWidth, int displayHeight, float displayAspectRatio, WorldManager wm) {
        if (displayWidth > 0)
            m_width = displayWidth;

        if (displayHeight > 0)
            m_height = displayHeight;

        if (displayAspectRatio > 0)
            m_aspect = displayAspectRatio;
        else
            m_aspect = (float)m_width / (float)m_height;

        m_worldManager = wm;
    }

    public void attatchRenderCanvas(Container container) {
        JPanel contentPanel = (JPanel) container;

        m_renderBuffer = m_worldManager.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, m_width, m_height);
        m_worldManager.getRenderManager().addRenderBuffer(m_renderBuffer);

        m_renderCanvas = m_renderBuffer.getCanvas();
        m_renderCanvas.setVisible(true);
        m_renderCanvas.setBounds(0, 0, m_width, m_height);

        contentPanel.add(m_renderCanvas, BorderLayout.CENTER);
        contentPanel.setSize(m_width, m_height);

//        createCameraEntity();
    }

////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    public int getWidth() {
        return m_width;
    }

    public int getHeight() {
        return m_height;
    }

    public float getAspectRatio() {
        return m_aspect;
    }

    public CameraNode getCameraNode() {
        return m_cameraNode;
    }

    public FlexibleCameraProcessor getCameraProcessor() {
        return m_cameraProcessor;
    }

    public Canvas getRenderCanvas() {
        return m_renderCanvas;
    }

    public RenderBuffer getRenderBuffer() {
        return m_renderBuffer;
    }

    public WorldManager getWorldManager() {
        return m_worldManager;
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    public void setDisplayWidth(int width) {
        m_width = width;
    }

    public void setDisplayHeight(int height) {
        m_height = height;
    }

    public void setAspectRatio(float width, float height) {
        m_aspect = width/height;
    }

    public void setAspectRactio(float aspect) {
        m_aspect = aspect;
    }

    public void setCameraNode(CameraNode cameraNode) {
        m_cameraNode = cameraNode;
    }

    public void setCameraProcessor(FlexibleCameraProcessor cameraProcessor) {
        m_cameraProcessor = cameraProcessor;
    }

    public void setRenderCanvas(Canvas canvas) {
        m_renderCanvas = canvas;
    }

    public void setRenderBuffer(RenderBuffer renderBuffer) {
        m_renderBuffer = renderBuffer;
    }

    public void setWorldManager(WorldManager worldManager) {
        m_worldManager = worldManager;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    private void createCameraEntity() {
        Node cameraSG = createCameraGraph();

        Entity camera = new Entity("DefaultCamera");
        m_cameraComponent = m_worldManager.getRenderManager().createCameraComponent(cameraSG, m_cameraNode, m_width, m_height, 45.0f, m_aspect, 1.0f, 2000.0f, true);
        m_cameraComponent.setCameraSceneGraph(cameraSG);
        m_cameraComponent.setCameraNode(m_cameraNode);
        camera.addComponent(CameraComponent.class, m_cameraComponent);
        m_renderBuffer.setCameraComponent(m_cameraComponent);

        SkyBox sky = createSkyBox(camera);

        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)m_worldManager.getInputManager().createInputComponent(m_renderCanvas, eventMask);
        m_cameraProcessor = new FlexibleCameraProcessor(cameraListener, m_cameraNode, m_worldManager, camera, sky);
        FirstPersonCamState state = new FirstPersonCamState();
        state.setCameraPosition(new Vector3f(0, 2.2f, -2));
        FirstPersonCamModel model = new FirstPersonCamModel();
        m_cameraProcessor.setCameraBehavior(model, state);

        m_worldManager.addUserData(FlexibleCameraProcessor.class, m_cameraProcessor);
        m_worldManager.addUserData(CameraState.class, m_cameraProcessor.getState());
        m_cameraProcessor.setRunInRenderer(true);

        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(m_cameraProcessor);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);

        m_worldManager.addEntity(camera);
    }

    private Node createCameraGraph() {
        Node cameraSG = new Node("MyCamera SG");
        m_cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(m_cameraNode);

        return (cameraSG);
    }

    private SkyBox createSkyBox(Entity camera) {
        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, m_worldManager);
        sky.setTexture(SkyBox.NORTH,   loadSkyboxTexture("assets/textures/skybox/Front.png"));  // north
        sky.setTexture(SkyBox.EAST,    loadSkyboxTexture("assets/textures/skybox/Right.png"));  // south
        sky.setTexture(SkyBox.SOUTH,   loadSkyboxTexture("assets/textures/skybox/Back.png"));   // east
        sky.setTexture(SkyBox.WEST,    loadSkyboxTexture("assets/textures/skybox/Left.png"));   // west
        sky.setTexture(SkyBox.DOWN,    loadSkyboxTexture("assets/textures/skybox/Top.png"));    // up
        sky.setTexture(SkyBox.UP,      loadSkyboxTexture("assets/textures/skybox/Top.png"));    // down

        RenderComponent sc2 = m_worldManager.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);

        return sky;
    }

    private Texture loadSkyboxTexture(String filePath) {
        Texture monkeyTexture = null;
        try {
            monkeyTexture = TextureManager.loadTexture(new File(FileUtils.rootPath, filePath).toURI().toURL(), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
        } catch (MalformedURLException ex) {
            System.err.println(ex.getMessage());
        }

        if (monkeyTexture != null) {
            monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.EdgeClamp);
            monkeyTexture.setMinificationFilter(Texture.MinificationFilter.Trilinear);
            monkeyTexture.setMagnificationFilter(Texture.MagnificationFilter.Bilinear);
        }
        return monkeyTexture;
    }
}
