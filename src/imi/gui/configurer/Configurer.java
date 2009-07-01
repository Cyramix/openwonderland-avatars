/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui.configurer;

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
import imi.gui.AttachmentControlFrame;
import imi.gui.CanvasDropTargetListener;
import imi.gui.JFrame_AdvOptions;
import imi.gui.JFrame_ColorSelector;
import imi.gui.JPanel_Animations;
import imi.gui.JScreenShotButton;
import imi.gui.LoadAvatarDialogue;
import imi.gui.MeshSwapDialogue;
import imi.gui.TextureCreator;
import imi.gui.TreeExplorer;
import imi.input.CharacterControls;
import imi.input.DefaultCharacterControls;
import imi.input.InputManagerEntity;
import imi.repository.CacheBehavior;
import imi.repository.Repository;
import imi.scene.JScene;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.utils.FileUtils;
import imi.utils.MaterialMeshUtils.ShaderType;
import imi.utils.MaterialMeshUtils.TextureType;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTarget;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
public class Configurer extends JFrame implements BufferUpdater {
////////////////////////////////////////////////////////////////////////////////
// Class Data Members
////////////////////////////////////////////////////////////////////////////////

    private final static Logger     m_logger            = Logger.getLogger(Configurer.class.getName());

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
    private TreeExplorer            m_explorer          = null;
    private AttachmentControlFrame  m_hairController    = null;
    private JFrame_ColorSelector    m_colorSelector     = null;
    private JFrame_AdvOptions       m_avatarEditor      = null;
    private LoadAvatarDialogue      m_avatarLoad        = null;
    private MeshSwapDialogue        m_meshSwap          = null;
    private TextureCreator          m_TexShadEditor     = null;
    private JFrame                  m_animationControl  = null;


////////////////////////////////////////////////////////////////////////////////
// Class Methods
////////////////////////////////////////////////////////////////////////////////

    public Configurer(String[] args) {
        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                // TODO: Real cleanup
                System.exit(0);
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
        Configurer avatarConfigurer = new Configurer(args);
        avatarConfigurer.initUI();
        avatarConfigurer.initViewerDefaults();
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
                        m_pscene = m_avatar.getPScene();
                        character.getSkeleton().resetAllJointsToBindPose();
                        m_avatarLoad.close();
                        m_avatarLoad = null;
                        updateCameraPosition();
                        enableUsableMenus(true);
                        setLoadingIndicator(false);
                    }
                };

                m_avatar = new Avatar.AvatarBuilder(attribute, m_worldManager)
                                     .initializer(initializer)
                                     .build();
            }
        };
        Thread threadHeadLoad = new Thread(runHeadLoad, "AvatarLoadingThread");
        threadHeadLoad.start();
    }

    public void swapMesh(final List<Object[]> SkinnedMeshes, final List<Object[]> Attatchments, final FileUtils.FileMetrics headMetrics, final boolean normalMap) {
        Runnable runHeadLoad = new Runnable() {

            public void run() {
                m_avatar.getSkeleton().setRenderStop(true);

                for (Object[] skinnedMesh : SkinnedMeshes) {
                    File smFile     = (File) skinnedMesh[0];
                    String subGroup = (String) skinnedMesh[1];

                    if (subGroup.equals("Hands")) {
                        Manipulator.swapHandsMesh(m_avatar, true, smFile);
                    } else if (subGroup.equals("UpperBody")) {
                        Manipulator.swapShirtMesh(m_avatar, true, smFile);
                    } else if (subGroup.equals("Jacket")) {
                        Manipulator.swapJacketMesh(m_avatar, true, smFile);
                    } else if (subGroup.equals("LowerBody")) {
                        Manipulator.swapPantsMesh(m_avatar, true, smFile);
                    } else if (subGroup.equals("Feet")) {
                        Manipulator.swapShoesMesh(m_avatar, true, smFile);
                    }
                }

                for (Object[] attatchments : Attatchments) {
                    File attatchFile    = (File) attatchments[0];
                    String meshName     = (String) attatchments[1];
                    String attatchJnt   = (String) attatchments[2];

                    if (attatchJnt.equals("HairAttach")) {
                        Manipulator.swapHairMesh(m_avatar, true, attatchFile, meshName);
                    } else if (attatchJnt.equals("Hats")) {
                        Manipulator.swapHatMesh(m_avatar, true, attatchFile, meshName);
                    } else if (attatchJnt.equals("Glasses")) {
                        Manipulator.swapGlassesMesh(m_avatar, true, attatchFile, meshName);
                    }
                }

                addAvatarHead(headMetrics, normalMap);
                Manipulator.setShaderOnSkin(m_avatar, ShaderType.FleshShader);

                m_meshSwap.close();
                m_meshSwap = null;

                m_avatar.getSkeleton().setRenderStop(false);
                setLoadingIndicator(false);
            }
        };
        Thread threadHeadLoad = new Thread(runHeadLoad, "MeshSwappingThread");
        threadHeadLoad.start();
    }

    public void viewMesh() {
        Runnable runHeadLoad = new Runnable() {

            public void run() {

            }
        };
        Thread threadHeadLoad = new Thread(runHeadLoad, "MeshLoadingThread");
        threadHeadLoad.start();
    }

    public void openExplorer() {
        if (m_explorer != null) {
            if (m_explorer.isVisible())
                return;
            else {
                m_explorer.dispose();
                m_explorer = null;
            }
        }

        m_explorer = new TreeExplorer();
        m_explorer.setExplorer(m_worldManager, m_avatar.getPScene());
        m_explorer.setVisible(true);
    }

    public void openHairExplorer() {
        if (m_hairController != null) {
            if (m_hairController.isVisible())
                return;
            else {
                m_hairController.dispose();
                m_hairController = null;
            }
        }

        m_hairController = new AttachmentControlFrame( m_worldManager,
                                                       m_avatar,
                                                       new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/data/folderHair.png")),
                                                       new ImageIcon(this.getClass().getClassLoader().getResource("imi/gui/data/folderHairDown.png")),
                                                       m_avatar.getSkeleton().getSkinnedMeshJoint("Head"),
                                                       "Hair", "Hair Selector");
        m_hairController.setVisible(true);
    }

    public void openColorSelector() {
        if (m_colorSelector != null) {
            if (m_colorSelector.isVisible())
                return;
            else {
                m_colorSelector.dispose();
                m_colorSelector = null;
            }
        }

        m_colorSelector = new JFrame_ColorSelector.Builder(m_worldManager)
                                                  .character(m_avatar)
                                                  .build();
        m_colorSelector.setVisible(true);
    }

    public void openAvatarEditor() {
        if (m_avatarEditor != null) {
            if (m_avatarEditor.isVisible())
                return;
            else {
                m_avatarEditor.dispose();
                m_avatarEditor = null;
            }
        }

        m_avatarEditor = new JFrame_AdvOptions.Builder(m_worldManager)
                                              .loadEyePanel(true)
                                              .character(m_avatar)
                                              .build();
        m_avatarEditor.setVisible(true);
    }

    public void openTexShadEditor() {
        if (m_TexShadEditor != null) {
            if (m_TexShadEditor.isVisible())
                return;
            else {
                m_TexShadEditor.dispose();
                m_TexShadEditor = null;
            }
        }

        m_TexShadEditor = new TextureCreator(m_pscene, m_worldManager);
        m_TexShadEditor.setVisible(true);
    }

    public void openSceneMonitor() {
//        SceneMonitor.getMonitor().registerNode(m_sceneEssentials.getJScene(), "Scene");
//        SceneMonitor.getMonitor().showViewer(true);
    }

    public void openAnimationControls() {

        if (m_animationControl != null) {
            if (m_animationControl.isVisible())
                return;
            else {
                m_animationControl.dispose();
                m_animationControl = null;
            }
        }

        m_animationControl = new JFrame();
        JPanel_Animations animPanel = new JPanel_Animations.Builder(m_worldManager, m_avatar)
                                                           .build();
        animPanel.startTimer();
        animPanel.setVisible(true);
        m_animationControl.add(animPanel);
        m_animationControl.pack();
        m_animationControl.setVisible(true);
    }

    public void loadConfiguration() {
        final Configurer me = this;
        Runnable loadConfig = new Runnable() {

            public void run() {
                CharacterInitializationInterface init   = new CharacterInitializationInterface() {

                    @Override
                    public void initialize(Character character) {
                        m_avatar    = (Avatar) character;
                        m_pscene    = character.getPScene();
                        updateCameraPosition();
                        AbstractCameraState state = m_cameraProcessor.getState();
                        if (state instanceof TumbleObjectCamState) {
                            ((TumbleObjectCamState) state).setRotationY(0);
                        }

                        enableUsableMenus(true);
                        setLoadingIndicator(false);
                    }
                };
                FileUtils.loadAvatarConfiguration(m_avatar, m_worldManager, me, init);
            }
        };
        Thread loadConfigThread = new Thread(loadConfig);
        loadConfigThread.start();
    }

    public void saveConfiguration() {
        final Configurer me = this;
        Runnable saveConfig = new Runnable() {

            public void run() {
                FileUtils.saveAvatarConfiguration(m_avatar, me);
                setLoadingIndicator(false);
            }
        };
        Thread saveConfigThread = new Thread(saveConfig);
        saveConfigThread.start();
    }

    public void loadHeadTexture(File textureFile) {
        if (m_avatar == null || m_avatar.getSkeleton() == null) {
            throw new IllegalArgumentException("SEVERE ERROR: avatar is null or contains no skeleton");
        }
        String relTexPath = FileUtils.getRelativePath(new File(System.getProperty("user.dir")), textureFile);
        Manipulator.setFaceTexture(m_avatar, relTexPath, TextureType.Color);
    }

    public void loadRandomAvatar(final int gender) {
        Runnable createRandomAvatar = new Runnable() {

            public void run() {
                CharacterInitializationInterface initializer    = new CharacterInitializationInterface() {

                    @Override
                    public void initialize(Character character) {
                        m_avatar = (Avatar) character;
                        m_pscene = m_avatar.getPScene();
                        character.getSkeleton().resetAllJointsToBindPose();
                        updateCameraPosition();
                        enableUsableMenus(true);
                        setLoadingIndicator(false);
                    }
                };

                CharacterParams attributes = null;
                if (gender == 1)
                    attributes  = new MaleAvatarParams("AvatarJohnDoe").build();
                else
                    attributes  = new FemaleAvatarParams("AvatarJaneDoe").build();

                //attributes.setBaseURL(m_urlBase);
                if (m_avatar != null) {
                    m_avatar.destroy();
                    m_avatar = null;
                }

                m_avatar    = new Avatar.AvatarBuilder(attributes, m_worldManager)
                                        .initializer(initializer)
                                        .build();
            }
        };
        Thread loadRandAvatarThread = new Thread(createRandomAvatar);
        loadRandAvatarThread.start();
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

    private void initAvatarLoader() {
        m_avatarLoad    = new LoadAvatarDialogue(this);
        m_avatarLoad.open();
    }

    private void initMeshSwapping() {
        if (m_avatar == null) {
            JOptionPane.showMessageDialog(new Frame(), "You have not loaded an avatar", "WARNING", JOptionPane.WARNING_MESSAGE);
            setLoadingIndicator(false);
        } else {
            m_meshSwap  = new MeshSwapDialogue(this, m_avatar, m_worldManager);
            m_meshSwap.open();
        }
    }

    private void initMeshViewing() {
        setLoadingIndicator(false); // replace with acutal init
    }
    
    private void addAvatarHead(FileUtils.FileMetrics headMetrics, boolean normalMap) {
        if (headMetrics == null) {
            return;
        }
        
        if (normalMap) {
            Manipulator.swapHeadMesh(m_avatar, true, headMetrics.file, ShaderType.FleshShader);
        }
        else {
            Manipulator.swapHeadMesh(m_avatar, true, headMetrics.file, ShaderType.PhongFleshShader);
        }
    }

    private void enableUsableMenus(final boolean onOff) {
        Runnable switchControlAvailability = new Runnable() {

            public void run() {
                m_mainPanel.toolMenu.setEnabled(onOff);
                m_mainPanel.swapMeshes.setEnabled(onOff);
            }
        };
        Thread ControlAvailabilityThread    = new Thread(switchControlAvailability);
        ControlAvailabilityThread.start();
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

            int view    = m_mainPanel.camPositions.getSelectedIndex();
            switch(view)
            {
                case 0: // TOP VIEW
                {
                    realCamPosUpdate(modInst, modInst.findChild("Head"));
                    break;
                }
                case 1: // MID VIEW
                {
                    realCamPosUpdate(modInst, modInst.findChild("Hips"));
                    break;
                }
                case 2: // BOTTOM VIEW
                {
                    realCamPosUpdate(modInst, modInst.findChild("rightLegRoll"));
                    break;
                }
            }
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
        enableUsableMenus(false);
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
        JMenu               toolMenu        = new JMenu("Tools");
        JMenu               helpMenu        = new JMenu("Help");
        JMenuItem           loadAvatar      = new JMenuItem("Load Avatar...");
        JMenuItem           swapMeshes      = new JMenuItem("Swap Meshes...");
        JMenuItem           viewMeshes      = new JMenuItem("View Meshes...");
        JMenuItem           loadConfig      = new JMenuItem("Load Configuration...");
        JMenuItem           saveConfig      = new JMenuItem("Save Configuration...");
        JMenuItem           exitMenu        = new JMenuItem("Exit");
        JMenuItem           explMenu        = new JMenuItem("Node Explorer...");
        JMenuItem           hairMenu        = new JMenuItem("Hair Explorer...");
        JMenuItem           colorMenu       = new JMenuItem("Color Selector...");
        JMenuItem           texshaMenu      = new JMenuItem("Texture & Shader Editor...");
        JMenuItem           sceneMenu       = new JMenuItem("Scene Monitor...");
        JMenuItem           editorMenu      = new JMenuItem("Avatar Editor...");
        JMenuItem           animMenu        = new JMenuItem("Animation Controls...");
        JMenuItem           loadInstMenu    = new JMenuItem("Usage Instructions...");
        JMenuItem           loadAboutMenu   = new JMenuItem("About...");
        // Hot Bar
        JMenuBar            hotBar          = new JMenuBar();
        JButton             randMale        = new JButton();
        JButton             randFemale      = new JButton();
        JScreenShotButton   camMenu         = new JScreenShotButton();
        JButton             lightMenu       = new JButton();
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

            // The Menu Bar
            fileMenu.add(loadAvatar);
            fileMenu.add(swapMeshes);
            fileMenu.add(viewMeshes);
            fileMenu.add(loadConfig);
            fileMenu.add(saveConfig);
            fileMenu.add(exitMenu);
            fileMenu.getPopupMenu().setLightWeightPopupEnabled(false);
            toolMenu.add(explMenu);
            toolMenu.add(hairMenu);
            toolMenu.add(colorMenu);
            toolMenu.add(texshaMenu);
            toolMenu.add(sceneMenu);
            toolMenu.add(editorMenu);
            toolMenu.add(animMenu);
            toolMenu.getPopupMenu().setLightWeightPopupEnabled(false);
            helpMenu.add(loadInstMenu);
            helpMenu.add(loadAboutMenu);
            helpMenu.getPopupMenu().setLightWeightPopupEnabled(false);
            menuBar.add(fileMenu);
            menuBar.add(toolMenu);
            menuBar.add(helpMenu);
            menuBar.setMinimumSize(new Dimension(200, 27));
            setMenuActionListeners();
            gbc.gridx = 0;  gbc.gridy = 0;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
            gbc.fill = GridBagConstraints.BOTH;
            this.add(menuBar, gbc);

            // The Hot Bar
            ImageIcon iconUp    = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/maleNew.png"));
            ImageIcon iconDown  = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/maleNewDown.png"));
            randMale            = new JButton(iconUp);
            randMale.setPressedIcon(iconDown);
            hotBar.add(randMale);
            iconUp              = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/femaleNew.png"));
            iconDown            = new ImageIcon(getClass().getClassLoader().getResource("imi/gui/data/femaleNewDown.png"));
            randFemale          = new JButton(iconUp);
            randFemale.setPressedIcon(iconDown);
            DefaultComboBoxModel model  = new DefaultComboBoxModel();
            model.addElement("Top");    model.addElement("Middle"); model.addElement("Bottom");
            camPositions        = new JComboBox(model);
            hotBar.add(randFemale);
            hotBar.add(camMenu);
            //hotBar.add(lightMenu);
            hotBar.add(camPositions);
            hotBar.setPreferredSize(new Dimension(200, 70));
            setHotBarActionListeners();
            gbc.gridx = 0;  gbc.gridy = 1;  gbc.weightx = 1.0f; gbc.weighty = 1.0f; gbc.gridwidth = 2;  gbc.gridheight = 1;
            this.add(hotBar, gbc);

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
            loadAvatar.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    initAvatarLoader();
                }
            });

            swapMeshes.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    initMeshSwapping();
                }
            });

            viewMeshes.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    initMeshViewing();
                }
            });

            loadConfig.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    loadConfiguration();
                }
            });
            
            saveConfig.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    saveConfiguration();
                }
            });

            exitMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    shutDown();
                }
            });

            explMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openExplorer();
                    setLoadingIndicator(false);
                }
            });

            hairMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openHairExplorer();
                    setLoadingIndicator(false);
                }
            });

            colorMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openColorSelector();
                    setLoadingIndicator(false);
                }
            });

            texshaMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openTexShadEditor();
                    setLoadingIndicator(false);
                }
            });

            sceneMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openSceneMonitor();
                    setLoadingIndicator(false);
                }
            });

            editorMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openAvatarEditor();
                    setLoadingIndicator(false);
                }
            });

            animMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    openAnimationControls();
                    setLoadingIndicator(false);
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
                    JOptionPane.showMessageDialog(canvasPanel, "Animation Configuration Tool: \n" +
                            "(c) IMI; compliments of IR&D team.");
                }
            });
        }

        private void setHotBarActionListeners() {
            randMale.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    loadRandomAvatar(1);    // 1 == male
                }
            });

            randFemale.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    loadRandomAvatar(2);    // 2 == female
                }
            });

            camPositions.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    setLoadingIndicator(true);
                    updateCameraPosition();
                }
            });
        }
    }
}