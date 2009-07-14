/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.demos;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import imi.camera.AbstractCameraState;
import imi.character.avatar.Avatar;
import imi.camera.CameraModel;
import imi.camera.CameraModels;
import imi.camera.FirstPersonCamModel;
import imi.camera.FirstPersonCamState;
import imi.camera.FlexibleCameraProcessor;
import imi.camera.TumbleObjectCamModel;
import imi.camera.TumbleObjectCamState;
import imi.gui.CanvasDropTargetListener;
import imi.gui.LoadAvatarDialogue;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.repository.CacheBehavior;
import imi.repository.Repository;
import imi.scene.JScene;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import org.jdesktop.mtgame.BufferUpdater;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.OnscreenRenderBuffer;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.RotationProcessor;

/**
 *
 * @author ptruong
 */
public class JavaTest extends JFrame implements BufferUpdater {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private final static Logger     m_logger            = Logger.getLogger(JavaTest.class.getName());

    private static WorldManager     m_worldManager      = null;
    private PScene                  m_pscene            = null;
    private Repository              m_repository        = null;
    private CameraNode              m_cameraNode        = null;

    private int                     m_desiredFrameRate  = 60;
    private int                     m_width             = 800;
    private int                     m_height            = 600;
    private float                   m_aspect            = 800.0f/600.0f;

    private int                     m_screenWidth       = 800;
    private int                     m_screenHeight      = 732;

    private String[]                m_args              = null;

    private FlexibleCameraProcessor m_cameraProcessor   = null;

    private ColorRGBA               m_clearColor        = new ColorRGBA(173.0f/255.0f, 195.0f/255.0f, 205.0f/255.0f, 1.0f);
    private Color                   m_colorRed          = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    private Color                   m_colorBlue         = new Color(0.0f, 0.0f, 1.0f, 1.0f);

    private boolean                 m_initDone          = false;
    private boolean                 m_loading           = false;
    private Avatar                  m_avatar            = null;

    public enum CameraType  { FPSCamera, TumbleCamera }
    public enum Gender      { Null, Male, Female }

    private SwingPanel              m_mainPanel         = null;

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public JavaTest(String[] args) {
        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                shutDown();
            }
        });

        m_logger.info("Current Directory: " + System.getProperty("user.dir"));

        m_args  = new String[args.length];
        for (int i = 0; i < m_args.length; i++) {
            m_args[i]   = args[i];
        }

        initWorldManager("WorldManagerPrimero", true, null);
    }

    public static void main(String[] args) {
        JavaTest avatarJavaTest = new JavaTest(args);
        avatarJavaTest.initUI();
        avatarJavaTest.initViewerDefaults();
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Functions
////////////////////////////////////////////////////////////////////////////////

    public static WorldManager getWM() {
        return m_worldManager;
    }

    private void initWorldManager(String name, boolean bUseSkeleton, CacheBehavior cache) {
        m_worldManager = new WorldManager(name);
        m_worldManager.addUserData(JFrame.class, this);
        processArgs(m_args);

        m_worldManager.getRenderManager().setDesiredFrameRate(m_desiredFrameRate);
        setLogerLevels();

        m_logger.info("Creating Repository...\tSTART");
        m_repository = new Repository(m_worldManager, bUseSkeleton, cache);
        ThreadWait(100);
        m_logger.info("Creating Repository...\tDONE");

        m_worldManager.addUserData(Repository.class, m_repository);
    }

    private void setLogerLevels() {
        // shutup jmonkey!
        Logger.getLogger("com.jme").setLevel(Level.SEVERE);
        Logger.getLogger("com.jmex").setLevel(Level.SEVERE);
        Logger.getLogger("org.collada").setLevel(Level.SEVERE);
        Logger.getLogger("imi.loaders.collada").setLevel(Level.SEVERE);
        Logger.getLogger("com.jme.scene.state.jogl.shader").setLevel(Level.OFF);
    }

    private void initUI () {
        m_logger.info("Creating UI...\t\tSTART");
        createUI(m_worldManager);
        m_logger.info("Creating UI...\t\tDONE");
        while(m_initDone == false) {
            ThreadWait(100);
        }
    }

    private void initialization() {
        m_logger.info("Creating InputEntity...\tSTART");
        createInputEntity(m_worldManager);
        m_logger.info("Creating InputEntity...\tDONE");

        ThreadWait(100);

        m_logger.info("Creating CameraEntity...\tSTART");
        createCameraEntity(m_worldManager);
        m_logger.info("Creating CameraEntity...\tDONE");

        ThreadWait(100);

        m_logger.info("Creating GlobalLighting...\tSTART");
        createGlobalLighting(m_worldManager, new Vector3f(10.0f, 15.0f, -5.0f));
        m_logger.info("Creating GlobalLighting...\tDONE");

        ThreadWait(100);

        m_initDone = true;
    }

    private void createUI(WorldManager wm) {
        m_mainPanel = new SwingPanel(wm, this);
        m_mainPanel.m_renderBuffer.setBufferUpdater(this);
        setContentPane(m_mainPanel);
        pack();
        setSize(m_screenWidth, m_screenHeight);
        setLocationRelativeTo(null);
        m_mainPanel.canvas.requestFocusInWindow();
        setVisible(true);
        wm.addUserData(OnscreenRenderBuffer.class, m_mainPanel.m_renderBuffer);
    }

    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);

        // Add the camera
        Entity camera = new Entity("DefaultCamera");

        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG,
                m_cameraNode,   // Camera node
                m_width,        // Screen width
                m_height,       // Screen height
                35.0f,          // Field of view
                m_aspect,       // Aspect ratio
                0.001f,         // Near clip
                15.0f,          // far clip
                true);          // Primary camera component boolean

        OnscreenRenderBuffer renderBuffer = (OnscreenRenderBuffer) wm.getUserData(OnscreenRenderBuffer.class);
        renderBuffer.setBackgroundColor(m_clearColor);
        camera.addComponent(CameraComponent.class, cc);
        renderBuffer.setCameraComponent(cc);

        m_cameraProcessor = new FlexibleCameraProcessor(m_cameraNode, wm, camera, null, m_width, m_height);

        assignCameraType(wm, CameraType.TumbleCamera, new Vector3f(0, 2.2f, -1), Vector3f.ZERO);

        wm.addUserData(FlexibleCameraProcessor.class, m_cameraProcessor);
        wm.addUserData(AbstractCameraState.class, m_cameraProcessor.getState());

        m_cameraProcessor.setRunInRenderer(true);

        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(m_cameraProcessor);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);

        wm.addEntity(camera);
    }

    private void createInputEntity(WorldManager wm) {
        new InputManagerEntity(wm);
        // Create avatar input scheme
        InputManagerEntity ime = (InputManagerEntity)wm.getUserData(InputManagerEntity.class);
        CharacterControls control = new DefaultCharacterControls(wm);
        ime.addInputClient(control);
    }

    private void createGlobalLighting(WorldManager wm, Vector3f lightPos) {
        // Lighting Configuration
        LightNode lightNode = new LightNode("Dis is me light node man!");
        // Must be a PointLight to function
        PointLight pointLight = new PointLight();
        pointLight.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        pointLight.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f));
        pointLight.setEnabled(true);
        // attach it to the LightNode
        lightNode.setLight(pointLight);

        lightNode.setLocalTranslation(lightPos);
        // add it to the render manager
        wm.getRenderManager().addLight(lightNode);
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Helper Functions
////////////////////////////////////////////////////////////////////////////////

    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");
        m_cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(m_cameraNode);

        return (cameraSG);
    }

    private void assignCameraType(WorldManager wm, CameraType camType, Vector3f camPos, Vector3f focalPoint) {
        CameraModel model               = null;
        AbstractCameraState resultState = null;
        switch(camType)
        {
            case FPSCamera:
            {
                FirstPersonCamState state = new FirstPersonCamState();
                state.setCameraPosition(camPos);
                model = (FirstPersonCamModel) CameraModels.getCameraModel(FirstPersonCamModel.class);
                resultState = state;
                break;
            }
            case TumbleCamera:
            {
                TumbleObjectCamState state   = new TumbleObjectCamState(null);
                state.setCameraPosition(camPos);
                state.setTargetFocalPoint(focalPoint);
                state.setMinimumDistanceSquared(0.1f);
                state.setMaximumDistanceSquared(20.0f);
                state.setRotationY(180.0f);
                model   = CameraModels.getCameraModel(TumbleObjectCamModel.class);
                resultState = state;
                break;
            }
        }

        m_cameraProcessor.setCameraBehavior(model, resultState);
    }

    public void initViewerDefaults() {
        m_pscene        = new PScene("PScenePrimero", m_worldManager);
        JScene jscene   = new JScene(m_pscene);
        jscene.setName("JScenePrimero");

        Node objects = new Node("objects");
        TextureState ts = (TextureState) m_worldManager.getRenderManager().createRendererState(RenderState.StateType.Texture);
        URL url = getClass().getClassLoader().getResource("imi/gui/data/imilogo.png");
        Texture t0 = TextureManager.loadTexture(url,
                Texture.MinificationFilter.Trilinear,
                Texture.MagnificationFilter.Bilinear);
        t0.setWrap(Texture.WrapMode.Repeat);
        ts.setTexture(t0);

        CullState cs = (CullState) m_worldManager.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);

        Box box = new Box("box", Vector3f.ZERO, 0.5f, 0.5f, 0.5f);
        box.setLocalTranslation(new Vector3f(0, 0, 0));
        box.setRenderState(ts);
        box.setRenderState(cs);
        box.setModelBound(new BoundingBox());
        box.updateModelBound();
        objects.attachChild(box);

        RotationProcessor rp = new RotationProcessor("BoxSpinner", m_worldManager,
                objects, (float) (1.0f * Math.PI / 180.0f));


        Entity e = new Entity("RotatingBox");
        RenderComponent rc = m_worldManager.getRenderManager().createRenderComponent(objects);
        e.addComponent(ProcessorComponent.class, rp);
        e.addComponent(RenderComponent.class, rc);
        m_worldManager.addEntity(e);
    }

    public void updateTumbleCameraTarget() {
        while (m_avatar == null)
            ThreadWait(100);
        while (m_avatar.isInitialized() == false)
            ThreadWait(100);
        while (m_avatar.getModelInst() == null)
            ThreadWait(100);

        if (m_cameraProcessor.getState() instanceof TumbleObjectCamState) {
            m_avatar.getSkeleton().buildFlattenedHierarchy();  // gotta flatten heirarchy to get the correct numbers
            TumbleObjectCamState state = (TumbleObjectCamState) m_cameraProcessor.getState();

            PPolygonModelInstance modelInst = m_avatar.getModelInst();
            state.setTargetModelInstance(modelInst);
            PNode node      = modelInst.findChild("Head");
            Vector3f pos    = node.getTransform().getWorldMatrix(false).getTranslation();
            Vector3f camPos = state.getCameraPosition();
            camPos.y        = pos.y;
            state.setCameraPosition(camPos);
            state.setTargetFocalPoint(pos);
        }
    }

    public void realCamPosUpdate(PPolygonModelInstance modInst, PNode targetNode) {
        if (m_cameraProcessor.getState() instanceof TumbleObjectCamState) {
            TumbleObjectCamState state = (TumbleObjectCamState) m_cameraProcessor.getState();

            modInst.buildFlattenedHierarchy();
            state.setTargetModelInstance(modInst);

            Vector3f pos    = null;
            if (targetNode != null)
                pos = targetNode.getTransform().getWorldMatrix(false).getTranslation();
            else
                pos = modInst.getBoundingSphere().getCenterRef();

            Vector3f camPos = state.getCameraPosition();
            camPos.y        = pos.y;
            state.setCameraPosition(camPos);
            state.setTargetFocalPoint(pos);
        }
    }

    public void setLoadingIndicator(boolean onOff) {
        m_loading = onOff;
    }

    public boolean isColladaFile(File file) {
        int index   = file.toString().lastIndexOf(".");
        String ext  = file.toString().substring(index);

        if (ext.toLowerCase().contains("dae"))
            return true;
        else
            return false;
    }

    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                m_desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + m_desiredFrameRate);
                i++;
            }
        }
    }

    private void ThreadWait(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            m_logger.log(Level.SEVERE, null, ex);
        }
    }

    public void init(RenderBuffer arg0) {
        m_logger.info("Buffer///////////////////////////////////Updater START");
        initialization();
        m_logger.info("Buffer////////////////////////////////////Updater DONE");
    }

    private void shutDown() {
        dispose();
        System.exit(0);
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Classes
////////////////////////////////////////////////////////////////////////////////

    private static class HelpDocumentation extends JFrame {
        // Put help in HTMl file in the package
        private final URL       HelpContentsURL = getClass().getResource("help.html");

        public HelpDocumentation() {
            try {
                JEditorPane htmlPane = new JEditorPane(HelpContentsURL);
                htmlPane.setEditable(false);
                this.getContentPane().add(new JScrollPane(htmlPane));
                this.setSize(650, 600);
            } catch (IOException ex) {
                // Do nothing
            }
        }
    }

    private class SwingPanel extends JPanel implements FrameRateListener {

        // JPanel containers
        JPanel          canvasPanel     = new JPanel();
        JPanel          statusPanel     = new JPanel();
        // Render JCanvas
        Canvas          canvas          = null;
        // Status Bar
        JLabel          statusLabel     = new JLabel("");
        JProgressBar    progressBar     = new JProgressBar();
        // Render buffer
        OnscreenRenderBuffer    m_renderBuffer  = null;
        // Menu items
        JMenuBar            menuBar         = new JMenuBar();
        JMenu               fileMenu        = new JMenu("File");
        JMenu               helpMenu        = new JMenu("Help");
        JMenuItem           exitMenu        = new JMenuItem("Exit");
        JMenuItem           loadInstMenu    = new JMenuItem("Usage Instructions...");
        JMenuItem           loadAboutMenu   = new JMenuItem("About...");

        transient Runnable progressStart = new Runnable() {

            public void run() {
                progressBar.setIndeterminate(true);
                progressBar.setStringPainted(true);
            }
        };

        transient Runnable progressStop = new Runnable() {

            public void run() {
                progressBar.setIndeterminate(false);
                progressBar.setStringPainted(false);
            }
        };

        // Construct the panel
        public SwingPanel(WorldManager wm, Component parent) {
            super();
            this.setLayout(new GridBagLayout());

            // Constraints
            GridBagConstraints gbc = new GridBagConstraints();

            // The Menu Bar
            fileMenu.add(exitMenu);
            fileMenu.getPopupMenu().setLightWeightPopupEnabled(false);
            helpMenu.add(loadAboutMenu);
            helpMenu.getPopupMenu().setLightWeightPopupEnabled(false);
            menuBar.add(fileMenu);
            menuBar.add(helpMenu);
            menuBar.setMinimumSize(new Dimension(200, 27));
            setMenuActionListeners();
            gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.BOTH;
            this.add(menuBar, gbc);

            // The Rendering Canvas
            m_renderBuffer  = (OnscreenRenderBuffer) wm.getRenderManager().createRenderBuffer(RenderBuffer.Target.ONSCREEN, m_width, m_height);
            wm.getRenderManager().addRenderBuffer(m_renderBuffer);
            canvas          = m_renderBuffer.getCanvas();
            canvas.setDropTarget(new DropTarget(canvas, new CanvasDropTargetListener(parent)));
            canvas.setVisible(true);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());
            gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f;
            canvasPanel.add(canvas, gbc);
            canvasPanel.setPreferredSize(new Dimension(400, 600));

            gbc.gridx = 0;  gbc.gridy = 2;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
            this.add(canvasPanel, gbc);

            // The Status Panel
            statusPanel.setLayout(new GridBagLayout());

            gbc.gridx = 1;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 1;  gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.WEST;
            statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            statusPanel.add(statusLabel, gbc);

            gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 0.0f; gbc.weighty = 0.0f;
            gbc.fill = GridBagConstraints.BOTH; gbc.anchor = GridBagConstraints.CENTER;
            progressBar.setString("Loading...");
            progressBar.setStringPainted(false);
            statusPanel.add(progressBar, gbc);
            statusPanel.setMinimumSize(new Dimension(200, 27));

            gbc.gridx = 0;  gbc.gridy = 3;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
            this.add(statusPanel, gbc);
        }

        public void currentFramerate(float framerate) {

            Thread progress = null;
            if (m_loading) {
                progress = new Thread(progressStart, "ProgressBarIndicator");
                statusLabel.setText("Loading...");
                statusLabel.setForeground(m_colorRed);
                setCursor(new Cursor(Cursor.WAIT_CURSOR));
            } else {
                progress = new Thread(progressStop, "ProgressBarIndicator");
                statusLabel.setText("READY... If you see the spinning cube, you have the latest java & jogl");
                statusLabel.setForeground(m_colorBlue);
                setCursor(null);
            }
            progress.start();
        }

        private void setMenuActionListeners() {
            exitMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    shutDown();
                }
            });

            loadInstMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    HelpDocumentation   helpme = new HelpDocumentation();
                    helpme.setLocationRelativeTo(null);
                    helpme.setVisible(true);
                }
            });

            loadAboutMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    JOptionPane.showMessageDialog(canvasPanel, "Java & Jogl verification tool: \n" +
                            "(c) IMI; compliments of IR&D team.");
                }
            });
        }
    }
}