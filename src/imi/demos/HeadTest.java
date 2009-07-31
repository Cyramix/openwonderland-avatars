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

import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import imi.camera.AbstractCameraState;
import imi.character.Character;
import imi.character.avatar.Avatar;
import imi.camera.CameraModel;
import imi.camera.CameraModels;
import imi.camera.FirstPersonCamModel;
import imi.camera.FirstPersonCamState;
import imi.camera.FlexibleCameraProcessor;
import imi.camera.TumbleObjectCamModel;
import imi.camera.TumbleObjectCamState;
import imi.character.CharacterInitializationInterface;
import imi.character.CharacterParams;
import imi.character.FemaleAvatarParams;
import imi.character.MaleAvatarParams;
import imi.character.Manipulator;
import imi.gui.CanvasDropTargetListener;
import imi.gui.JScreenShotButton;
import imi.gui.LoadAvatarDialogue;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputClient;
import imi.input.InputManagerEntity;
import imi.repository.CacheBehavior;
import imi.repository.Repository;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.animation.AnimationListener;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.utils.MaterialMeshUtils.ShaderType;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
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
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author ptruong
 */
public class HeadTest extends JFrame implements BufferUpdater {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private final static Logger     m_logger            = Logger.getLogger(HeadTest.class.getName());

    private static WorldManager     m_worldManager      = null;
    private PScene                  m_pscene            = null;
    private Repository              m_repository        = null;
    private CameraNode              m_cameraNode        = null;

    private int                     m_desiredFrameRate  = 60;
    private int                     m_width             = 300;
    private int                     m_height            = 300;
    private float                   m_aspect            = 300.0f/300.0f;
    private int                     m_height_gui        = 32;

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
    private LoadAvatarDialogue      m_avatarLoad        = null;

    private static final String[]   maleFGHeads         = new String[] { "FG_Obama_HeadMedPoly.bhf",
                                                                         "FG_MaleLowPoly_01.bhf",
                                                                         "FG_MaleHead02Medium.bhf",
                                                                         "FG_Male02LowPoly.bhf",
                                                                         /**"FaceGenMaleHi.bhf"**/ };
    private static final String[]   maleHeads           = new String[] { "midAgeGuy.bhf",
                                                                         "MaleCHead.bhf",
                                                                         "blackHead.bhf",
                                                                         "AsianHeadMale.bhf" };
    private static final String[]   femaleFGHeads       = new String[] { "FG_FemaleLowPoly_01.bhf",
                                                                         "FG_FemaleHead01.bhf",
                                                                         "FG_Female02HighPoly.bhf",
                                                                         "FG_Female01LowPoly.bhf",
                                                                         "FG_Female01HighPoly.bhf",
                                                                         "FG_Female_AF_Head02.bhf" };
    private static final String[]   femaleHeads         = new String[] { "FemaleHispanicHead.bhf",
                                                                         "FemaleCHead.bhf",
                                                                         "FemaleAAHead.bhf",
                                                                         "AsianFemaleHead.bhf" };
    private static final String     headPath            = "imi/character/data/";

    // Dahlgren: Mouse adapter
    private final AvatarMouseAdapter      mouse               = new AvatarMouseAdapter();

////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public HeadTest(String[] args) {
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
        HeadTest avatarHeadTest = new HeadTest(args);
        avatarHeadTest.initUI();
        avatarHeadTest.initViewerDefaults();
        avatarHeadTest.manualTest();    // .autoLoad();
    }

    public void loadAvatar(final CharacterParams attribute) {
        Runnable runHeadLoad = new Runnable() {

            public void run() {
                if (m_avatar != null) {
                    m_avatar.destroy();
                    m_avatar = null;
                }

                CharacterInitializationInterface initializer    = new CharacterInitializationInterface() {

                    @Override
                    public void initialize(Character character) {
                        m_avatar = (Avatar) character;
                        // Dahlgren: Added for new avatar behavior
                        m_avatar.getSkeleton().getAnimationState(1).addListener(mouse);
                        m_pscene = m_avatar.getPScene();
                        character.getSkeleton().resetAllJointsToBindPose();
                        updateCameraPosition();
                        setLoadingIndicator(false);
                    }
                };

                PMatrix mat = new PMatrix();
                mat.fromAngleAxis((float)Math.toRadians(180), Vector3f.UNIT_Y);
                m_avatar = new Avatar.AvatarBuilder(attribute, m_worldManager)
                                     .initializer(initializer)
                                     .transform(mat)
                                     .build();
            }
        };

        Thread threadHeadLoad = new Thread(runHeadLoad, "AvatarLoadingThread");
        threadHeadLoad.start();
        setLoadingIndicator(true);
    }

    public void autoLoad() {
        CharacterParams mparams = new MaleAvatarParams("JohnDoe").configureHead(0)
                                                                 .configureTorso(1)
                                                                 .buildSpecific();
        mparams.setShirtColorPreset(1, true);
        loadAvatar(mparams);
        
        while (m_loading)
            threadWait(100);

        cycleHeads(headPath, maleHeads, ShaderType.PhongFleshShader);
        cycleHeads(headPath, maleFGHeads, ShaderType.PhongFleshShader);

        CharacterParams fparams = new FemaleAvatarParams("JaneDoe").configureHead(0)
                                                                   .configureTorso(0)
                                                                   .buildSpecific();
        fparams.setShirtColorPreset(1, true);
        loadAvatar(fparams);

        while (m_loading)
            threadWait(100);

        cycleHeads(headPath, femaleHeads, ShaderType.FleshShader);
        cycleHeads(headPath, femaleFGHeads, ShaderType.PhongFleshShader);
    }

    public void manualTest() {
        CharacterParams mparams = new MaleAvatarParams("JohnDoe").configureHead(1)
                                                                 .configureTorso(1)
                                                                 .buildSpecific();
        mparams.setShirtColorPreset(1, true);
        loadAvatar(mparams);
    }

    private void cycleHeads(String path, String[] binaryheads, ShaderType shaderType) {
        for( int i = 0; i < binaryheads.length; i++ ) {
            URL url     = getClass().getClassLoader().getResource(path + binaryheads[i]);
            try {
                File file = new File(url.toURI());
                if (Manipulator.swapHeadMesh(m_avatar, true, file, shaderType)) {
                    threadWait(200);
                    Manipulator.playFacialAnimation(m_avatar, 1, 0.2f, 2.0f);
                    threadWait(3000);
                }
            } catch (URISyntaxException ex) {
                Logger.getLogger(HeadTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
        threadWait(100);
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
            threadWait(100);
        }
    }

    private void initialization() {
        m_logger.info("Creating InputEntity...\tSTART");
        createInputEntity(m_worldManager);
        m_logger.info("Creating InputEntity...\tDONE");

        threadWait(100);

        m_logger.info("Creating CameraEntity...\tSTART");
        createCameraEntity(m_worldManager);
        m_logger.info("Creating CameraEntity...\tDONE");

        threadWait(100);

        m_logger.info("Creating GlobalLighting...\tSTART");
        createGlobalLighting(m_worldManager, new Vector3f(10.0f, 15.0f, -5.0f));
        m_logger.info("Creating GlobalLighting...\tDONE");

        threadWait(100);

        m_logger.info("Creating the screenshotButton...\tSTART");
        setCameraButton();
        m_logger.info("Creating the screenshotButton...\tDONE");

//        new LightFixingDeity(m_worldManager);

        m_initDone = true;
    }

    private void createUI(WorldManager wm) {
        m_mainPanel = new SwingPanel(wm, this);
        m_mainPanel.m_renderBuffer.setBufferUpdater(this);
        setContentPane(m_mainPanel);
        pack();
        setSize(m_width, m_height + m_height_gui);
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
        // Dahlgren: Add one of the avatar mouse adapters
        ime.addInputClient(mouse);
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

    private void updateCameraPosition() {
        AbstractCameraState state   = m_cameraProcessor.getState();
        if (state instanceof TumbleObjectCamState) {

            PNode node  = m_pscene.getInstances();
            if (node == null || node.getChildrenCount() <= 0)
                throw new RuntimeException("SEVERE ERROR: pscene not valid");

            PPolygonModelInstance modInst   = (PPolygonModelInstance) node.getChild(0);

            if (modInst.getBoundingSphere() ==  null)
                modInst.calculateBoundingSphere();

            realCamPosUpdate(modInst, modInst.findChild("Head"));

            setLoadingIndicator(false);
        } else {
            throw new IllegalStateException("SEVERE ERROR: not a tumbleobject camera state");
        }
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
                state.setMaximumDistanceSquared(3.0f);
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
    }

    public void updateTumbleCameraTarget() {
        while (m_avatar == null)
            threadWait(100);
        while (m_avatar.isInitialized() == false)
            threadWait(100);
        while (m_avatar.getModelInst() == null)
            threadWait(100);

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

    public void setCameraButton() {
        m_mainPanel.camMenu.setCamProcessor(m_cameraProcessor);
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

    private void threadWait(long millis) {
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
        // Hot Bar
        JMenuBar            hotBar          = new JMenuBar();
        JScreenShotButton   camMenu         = new JScreenShotButton();
        // Camera Positions
        JComboBox       camPositions    = null;

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
                statusLabel.setText("READY");
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
                    JOptionPane.showMessageDialog(canvasPanel, "Head Verification Tool: \n" +
                            "(c) IMI; compliments of IR&D team.");
                }
            });
        }

        private void setHotBarActionListeners() {
            camPositions.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    updateCameraPosition();
                }
            });
        }
    }

    private class AvatarMouseAdapter implements InputClient, AnimationListener
    {
        // avatar reactions
        private int[] reactions = new int[] {
            0, // Smile
            1, // Smile again
            2, // Scorn
        };
        // How many clicks have occured?
        private int clickCount = 0;
        
        // Keep track of how many are left to play
        private int pendingAnimations = 0;
        // Don't let more than two to queue up
        private final int maxPendingAnimations = 2;

        public void processKeyEvent(KeyEvent keyEvent)
        {
            // Do nothing... yet
        }

        public void processMouseEvent(MouseEvent mouseEvent)
        {
            if (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED && pendingAnimations < maxPendingAnimations)
            {
                if (m_avatar == null)
                    m_logger.warning("Clicked when avatar was null.");
                else {
                    // make the avatar react
                    m_logger.info("Playing reaction[" + clickCount + "] : " + reactions[clickCount]);
                    synchronized(this) { // sychronize on using pendingAnimations
                        m_avatar.initiateFacialAnimation(reactions[clickCount], 0.1f, 1.0f);
                        pendingAnimations += 2;
                    }
                    // increment click count
                    clickCount++;
                    clickCount %= reactions.length;
            }
            }
        }

        public void focusChanged(boolean currentlyInFocus)
        {
            m_logger.info("focusChanged: " + currentlyInFocus);
        }

        public synchronized void receiveAnimationMessage(AnimationMessageType message, int stateID)
        {
            // ID 1 is known to be 'facial animations' within the avatar system
            if (stateID == 1 && message == AnimationMessageType.PlayOnceComplete) {
                m_logger.info("Received notice that playonce was complete. " +
                        "Pending animations: " + (pendingAnimations - 1));
                // ensure this doesnt fall below zero, because the initial loading
                // smile will finish while we are listening
                pendingAnimations = ((pendingAnimations -= 2) < 0) ? 0 : pendingAnimations;

            }
        }

    }
}