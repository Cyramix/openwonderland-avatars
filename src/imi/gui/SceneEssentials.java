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
package imi.gui;

////////////////////////////////////////////////////////////////////////////////
// IMPORTS
////////////////////////////////////////////////////////////////////////////////
import com.jme.math.Vector3f;
import imi.character.AttachmentParams;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.Instruction;
import imi.loaders.Instruction.InstructionType;
import imi.loaders.InstructionProcessor;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.character.Character;
import imi.character.CharacterAttributes;
import imi.character.CharacterAttributes.SkinnedMeshParams;
import imi.character.avatar.Avatar;
import imi.loaders.repository.Repository;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationState;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.behaviors.TumbleObjectCamModel;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.camera.state.TumbleObjectCamState;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.utils.tree.MeshInstanceSearchProcessor;
import imi.scene.utils.tree.TreeTraverser;
import imi.sql.SQLInterface;
import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;


/**
 *
 * @author Paul Viet Nguyen Truong (ptruong)
 */
public class SceneEssentials {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS
////////////////////////////////////////////////////////////////////////////////
// Scene information
    private JScene                          m_currentJScene                 = null;
    private PScene                          m_currentPScene                 = null;
    private Entity                          m_currentEntity                 = null;
    private WorldManager                    m_worldManager                  = null;
    private ArrayList<ProcessorComponent>   m_currentHiProcessors           = null;
// File IO
    private JFileChooser                    m_jFileChooser_LoadAssets       = null;
    private JFileChooser                    m_jFileChooser_LoadColladaModel = null;
    private JFileChooser                    m_jFileChooser_LoadModel        = null;
    private JFileChooser                    m_jFileChooser_LoadXML          = null;
    private JFileChooser                    m_jFileChooser_LoadAvatarDAE    = null;
    private JFileChooser                    m_jFileChooser_SaveXML          = null;
    private JFileChooser                    m_jFileChooser_LoadAnim         = null;
    private ServerBrowserDialog             m_serverFileChooserD            = null;
    private JPanel_ServerBrowser            m_serverBrowserPanel            = null;
    private File                            m_fileXML                       = null;
    private File                            m_fileModel                     = null;
    private File                            m_fileTexture                   = null;
// Camera information
    private Vector3f                        m_camPos                        = new Vector3f(0.0f, 1.5f, 3.2f);
    private FlexibleCameraProcessor         m_curCameraProcessor            = null;
// Model information
    private String                          m_modelName                     = null;
    private PPolygonModelInstance           m_modelInst                     = null;
    private SkeletonNode                    m_skeleton                      = null;
    private Character                       m_avatar                        = null;
    private int                             m_gender                        = -1;
    private float                           m_visualScale                   = 1.0f;
    private PMatrix                         m_origin                        = new PMatrix();
    private String[]                        m_prevAttches                   = new String[] { null, null, null, null };
// Misc
    private SQLInterface                    m_sql;
    private boolean                         m_bdefaultload                  = false;
    static final int                        m_buffer                        = 2048;
    private File                            m_zipFileLoc                    = null;
 // Enumerations
    public String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS
////////////////////////////////////////////////////////////////////////////////
    public SceneEssentials() {
        initFileChooser();
    }    
    
    // Accessors
    public JScene getJScene() { return m_currentJScene; }

    public PScene getPScene() { return m_currentPScene; }

    public Entity getEntity() { return m_currentEntity; }

    public WorldManager getWM() { return m_worldManager; }

    public ArrayList<ProcessorComponent> getProcessors() { return m_currentHiProcessors; }

    public File getFileXML() { return m_fileXML; }

    public File getFileModel() { return m_fileModel; }

    public File getFileTexture() { return m_fileTexture; }

    public PPolygonModelInstance getModelInstance() { return m_modelInst; }

    public SkeletonNode getCurrentSkeleton() { return m_skeleton; }

    public FlexibleCameraProcessor getCurCamProcessor() { return m_curCameraProcessor; }

    public boolean isDefaultLoad() { return m_bdefaultload; }

    public Character getAvatar() { return m_avatar; }

    public int getGender() { return m_gender; }

    // Mutators
    public void setJScene(JScene jscene) { m_currentJScene = jscene; }

    public void setPScene(PScene pscene) { m_currentPScene = pscene; }

    public void setEntity(Entity entity) { m_currentEntity = entity; }

    public void setWM(WorldManager wm) { m_worldManager = wm; }

    public void setProcessors(ArrayList<ProcessorComponent> processors) { m_currentHiProcessors = processors; }

    public void setfileXML(File file) { m_fileXML = file; }

    public void setfileModel(File file) { m_fileModel = file; }

    public void setfileTexture(File file) { m_fileTexture = file; }

    public void setModelInstance(PPolygonModelInstance modinstance) { m_modelInst = modinstance; }

    public void setModelName(String name) { m_modelName = name; }

    public void setCurrentSkeleton(SkeletonNode s) { m_skeleton = s; }

    public void searchnSetSkeleton(PScene p) {
        if (p.getInstances() == null || p.getInstances().getChild(0) == null) {
            m_skeleton = null;
            return;
        }
        
        m_skeleton = ((SkeletonNode)p.getInstances().getChild(0).getChild(0));
    }
    public void setCurCamProcessor(FlexibleCameraProcessor camProc) { m_curCameraProcessor = camProc; }

    public void setDefaultLoad(boolean load) { m_bdefaultload = load; }

    public void setAvatar(Character c) { m_avatar = c; }

    public void setGender(int sex) { m_gender = sex; }

////////////////////////////////////////////////////////////////////////////////
// HELPER FUNCTIONS
////////////////////////////////////////////////////////////////////////////////
    /**
     * Initializer for the scene essentials class.  Null safe s0 it won't crash
     * if you decide you don't need something, but if you choose to do so some
     * functions and classes that use a scene essential class may not work.
     * @param jscene
     * @param pscene
     * @param entity
     * @param wm
     * @param processors
     */
    public void setSceneData(JScene jscene, PScene pscene, Entity entity, WorldManager wm, ArrayList<ProcessorComponent> processors) {
        m_currentJScene = jscene;
        
        if (pscene == null)
            m_currentPScene = jscene.getPScene();
        else
            m_currentPScene = pscene;
        
        if (entity != null)
            m_currentEntity = entity;
        
        if (wm != null)
            m_worldManager = wm;
        
        if (processors != null)
            m_currentHiProcessors = processors;
    }
    
    /**
     * Initializes the JFileChoosers that the scene essentials uses for file I/O
     */
    public void initFileChooser() {
        FileFilter assetFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".jpg") ||
                    f.getName().toLowerCase().endsWith(".png") ||
                    f.getName().toLowerCase().endsWith(".gif") ||
                    f.getName().toLowerCase().endsWith(".tga")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Images (*.jpg, *.png, *.gif, *.tga)");
                return szDescription;
            }
        };
        m_jFileChooser_LoadAssets = new javax.swing.JFileChooser();
        m_jFileChooser_LoadAssets.setDialogTitle("Load Texture File");
        java.io.File assetDirectory;

        if (isWindowsOS())
            assetDirectory = new java.io.File(".\\assets\\textures");
        else
            assetDirectory = new java.io.File("./assets/textures");

        m_jFileChooser_LoadAssets.setCurrentDirectory(assetDirectory);
        m_jFileChooser_LoadAssets.setDoubleBuffered(true);
        m_jFileChooser_LoadAssets.setDragEnabled(true);
        m_jFileChooser_LoadAssets.addChoosableFileFilter((FileFilter)assetFilter);
////////////////////////////////////////////////////////////////////////////////
        FileFilter colladaFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".dae") ||
                    f.getName().toLowerCase().endsWith(".bin")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Collada (*.dae) or Binary (*.bin)");
                return szDescription;
            }
        };

        m_jFileChooser_LoadColladaModel = new javax.swing.JFileChooser();
        m_jFileChooser_LoadColladaModel.setDialogTitle("Load Collada File");
        java.io.File colladaDirectory;

        if (isWindowsOS())
            colladaDirectory = new java.io.File(".\\assets\\models\\collada");
        else
            colladaDirectory = new java.io.File("./assets/models/collada");

        m_jFileChooser_LoadColladaModel.setCurrentDirectory(colladaDirectory);
        m_jFileChooser_LoadColladaModel.setDoubleBuffered(true);
        m_jFileChooser_LoadColladaModel.setDragEnabled(true);
        m_jFileChooser_LoadColladaModel.addChoosableFileFilter((FileFilter)colladaFilter);
////////////////////////////////////////////////////////////////////////////////
        FileFilter modelFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".dae") ||
                    f.getName().toLowerCase().endsWith(".ms3d")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Models (*.dae, *.ms3d)");
                return szDescription;
            }
        };
        m_jFileChooser_LoadModel = new javax.swing.JFileChooser();
        m_jFileChooser_LoadModel.setDialogTitle("Load Model File");
        java.io.File modelDirectory;
        
        if (isWindowsOS())
            modelDirectory = new java.io.File(".\\assets\\models");
        else
            modelDirectory = new java.io.File("./assets/models");

        m_jFileChooser_LoadModel.setCurrentDirectory(modelDirectory);
        m_jFileChooser_LoadModel.setDoubleBuffered(true);
        m_jFileChooser_LoadModel.setDragEnabled(true);
        m_jFileChooser_LoadModel.addChoosableFileFilter((FileFilter)modelFilter);
////////////////////////////////////////////////////////////////////////////////
        FileFilter xmlFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Extensible Markup Language (*.xml)");
                return szDescription;
            }
        };
        m_jFileChooser_LoadXML = new javax.swing.JFileChooser();
        m_jFileChooser_LoadXML.setDialogTitle("Load Configuration File");
        java.io.File xmlDirectory;

        if (isWindowsOS())
            xmlDirectory = new java.io.File(".\\assets\\");
        else
            xmlDirectory = new java.io.File("./assets/");

        m_jFileChooser_LoadXML.setCurrentDirectory(xmlDirectory);
        m_jFileChooser_LoadXML.setDoubleBuffered(true);
        m_jFileChooser_LoadXML.setDragEnabled(true);
        m_jFileChooser_LoadXML.addChoosableFileFilter((FileFilter)xmlFilter);
////////////////////////////////////////////////////////////////////////////////
        m_jFileChooser_LoadAvatarDAE = new javax.swing.JFileChooser();
        m_jFileChooser_LoadAvatarDAE.setDialogTitle("Load Avatar Model");
        java.io.File avatarDirectory;

        if (isWindowsOS())
            avatarDirectory = new java.io.File(".\\assets\\models\\collada");
        else
            avatarDirectory = new java.io.File("./assets/models/collada");

        m_jFileChooser_LoadAvatarDAE.setCurrentDirectory(avatarDirectory);
        m_jFileChooser_LoadAvatarDAE.setDoubleBuffered(true);
        m_jFileChooser_LoadAvatarDAE.setDragEnabled(true);
        m_jFileChooser_LoadAvatarDAE.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
////////////////////////////////////////////////////////////////////////////////
        FileFilter configFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Extensible Markup Language (*.xml)");
                return szDescription;
            }
        };
        m_jFileChooser_SaveXML = new javax.swing.JFileChooser();
        m_jFileChooser_SaveXML.setDialogTitle("Save Extensible Markup Language File");
        java.io.File source;

        if (isWindowsOS())
            source = new java.io.File(".\\assets");
        else
            source = new java.io.File("./assets");

        m_jFileChooser_SaveXML.setCurrentDirectory(xmlDirectory);
        m_jFileChooser_SaveXML.setDoubleBuffered(true);
        m_jFileChooser_SaveXML.setDragEnabled(true);
        m_jFileChooser_SaveXML.addChoosableFileFilter((FileFilter)configFilter);
////////////////////////////////////////////////////////////////////////////////
        FileFilter animFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }

                if (f.getName().toLowerCase().endsWith(".dae")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Collada File (*.dae)");
                return szDescription;
            }
        };
        m_jFileChooser_LoadAnim = new javax.swing.JFileChooser();
        m_jFileChooser_LoadAnim.setDialogTitle("Load Animation File");
        java.io.File animDirectory;

        if (isWindowsOS())
            animDirectory = new java.io.File(".\\assets\\");
        else
            animDirectory = new java.io.File("./assets/");

        m_jFileChooser_LoadAnim.setCurrentDirectory(animDirectory);
        m_jFileChooser_LoadAnim.setDoubleBuffered(true);
        m_jFileChooser_LoadAnim.setDragEnabled(true);
        m_jFileChooser_LoadAnim.addChoosableFileFilter((FileFilter)animFilter);
    }

    /**
     * Launches a custom browser window that lists and organizes the differnt
     * files listed on the mySQL server
     * @param c - The parent frame of the this window
     */
    @Deprecated
    public void openServerBrowser(JFrame c) {
        m_serverFileChooserD = new ServerBrowserDialog(c, true);
        m_serverFileChooserD.initBrowser(0);
        m_serverFileChooserD.setSceneEssentials(this);
        m_serverFileChooserD.setVisible(true);
    }

    /**
     * Launces a custom panel that lists and organizes the differnt files listed
     * on the mySQL server
     * @return JPanel_ServerBrowser - custom panel
     */
    @Deprecated
    public JPanel_ServerBrowser openServerBrowserPanel() {
        m_serverBrowserPanel = new JPanel_ServerBrowser();
        m_serverBrowserPanel.initBrowser(0);
        m_serverBrowserPanel.setSceneEssentials(this);
        m_serverBrowserPanel.setVisible(true);
        return m_serverBrowserPanel;
    }
    
    /**
     * Removes old model data from the pscene and replaces it with the user 
     * selected milkshake data
     */
    @Deprecated
    public void loadMS3DFile(int condition, boolean clear, Component arg0) {
        // 1- Create a shared asset for the repository
        SharedAsset newAsset = new SharedAsset(
                m_currentPScene.getRepository(),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D_SkinnedMesh, m_fileModel));  // Type set as MS3D_SkinnedMesh to load both skinned and regular
        if(condition == 0) {
            // 2 - Open dialog to load associated texture for model
            int retValAsset = m_jFileChooser_LoadAssets.showOpenDialog(arg0);
            // 3- Get the name and path to the asset
            if (retValAsset == JFileChooser.APPROVE_OPTION) {
                // 4- Add new model to the current PScene
                if(clear)
                    m_currentPScene.getInstances().removeAllChildren();
                if(m_currentHiProcessors == null)
                    m_currentHiProcessors = new ArrayList<ProcessorComponent>();
                else {
                    ProcessorCollectionComponent comp = (ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class);
                    for(int i = 0; i < m_currentHiProcessors.size(); i++)
                        comp.removeProcessor(m_currentHiProcessors.get(i));
                    m_currentHiProcessors.clear();
                }
                // 5- Set up an initializer to excecute once the asset is loaded into the scene
                m_fileTexture = m_jFileChooser_LoadAssets.getSelectedFile();
                
                newAsset.setInitializer(
                new AssetInitializer() {                                        // WARNING: problem with not being ready when the model is ready
                    public boolean initialize(Object asset) {
                        if (asset != null && asset instanceof SkeletonNode) {
                            // RED - 01/14/09
                            Repository repo = (Repository)m_worldManager.getUserData(Repository.class);
                            SkeletonNode skeleton = (SkeletonNode)asset;
                            skeleton.getAnimationState().setCurrentCycle(0);
                            skeleton.getAnimationState().setPauseAnimation(false);
                            PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)((SkeletonNode)asset).findChild("MS3DSkinnedMesh");

                            // Create a material to use
                            int iIndex = m_fileTexture.getName().indexOf(".");
                            String szName = m_fileTexture.getName().substring(0, iIndex);
                            int index = m_fileTexture.getPath().indexOf("assets");
                            String szTemp = m_fileTexture.getPath().substring(index, m_fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            material.setShader(repo.newShader(VertDeformerWithSpecAndNormalMap.class));
                            // Set the material
                            target.setMaterial(material);
                            target.applyMaterial();

                            PPolygonModelInstance modInst = ((PPolygonModelInstance)skeleton.getParent());
                            ((ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst, m_worldManager));
                        } else {
                            PPolygonMeshInstance target = (PPolygonMeshInstance) asset;
                            // Create a material to use
                            int iIndex = m_fileTexture.getName().indexOf(".");
                            String szName = m_fileTexture.getName().substring(0, iIndex);
                            int index = m_fileTexture.getPath().indexOf("assets");
                            String szTemp = m_fileTexture.getPath().substring(index, m_fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            // Set the material
                            target.setMaterial(material);
                            target.applyMaterial();
                        }
                        m_currentPScene.setDirty(true, true);
                        return true;
                    }
                });                
                m_modelInst = m_currentPScene.addModelInstance(m_modelName, newAsset, new PMatrix());
            }
        }
        else {
            if(clear)
                m_currentPScene.getInstances().removeAllChildren();
            if(m_currentHiProcessors == null)
                m_currentHiProcessors = new ArrayList<ProcessorComponent>();
            else {
                ProcessorCollectionComponent comp = (ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class);
                for(int i = 0; i < m_currentHiProcessors.size(); i++)
                    comp.removeProcessor(m_currentHiProcessors.get(i));
                m_currentHiProcessors.clear();
            }

                newAsset.setInitializer(
                new AssetInitializer() {                                        // WARNING: problem with not being ready when the model is ready
                    public boolean initialize(Object asset) {
                        if (asset != null && asset instanceof SkeletonNode) {
                            // RED - 01/14/09
                            Repository repo = (Repository) m_worldManager.getUserData(Repository.class);
                            SkeletonNode skeleton = (SkeletonNode)asset;
                            skeleton.getAnimationState().setCurrentCycle(0);
                            skeleton.getAnimationState().setPauseAnimation(false);
                            PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)((SkeletonNode)asset).findChild("MS3DSkinnedMesh");

                            // Create a material to use
                            int iIndex = m_fileTexture.getName().indexOf(".");
                            String szName = m_fileTexture.getName().substring(0, iIndex);
                            int index = m_fileTexture.getPath().indexOf("assets");
                            String szTemp = m_fileTexture.getPath().substring(index, m_fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            material.setShader(repo.newShader(VertDeformerWithSpecAndNormalMap.class));
                            // Set the material
                            target.setMaterial(material);
                            target.applyMaterial();

                            PPolygonModelInstance modInst = ((PPolygonModelInstance)skeleton.getParent());
                            ((ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst, m_worldManager));
                        } else {
                            PPolygonMeshInstance target = (PPolygonMeshInstance) asset;
                            // Create a material to use
                            int iIndex = m_fileTexture.getName().indexOf(".");
                            String szName = m_fileTexture.getName().substring(0, iIndex);
                            int index = m_fileTexture.getPath().indexOf("assets");
                            String szTemp = m_fileTexture.getPath().substring(index, m_fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            // Set the material
                            target.setMaterial(material);
                            target.applyMaterial();
                        }
                        m_currentPScene.setDirty(true, true);
                        return true;
                    }
                });                
                m_modelInst = m_currentPScene.addModelInstance(m_modelName, newAsset, new PMatrix());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // LOAD FROM USER HD - Collada
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of a non-skinned model and then subsequently creates a shared asset to
     * to load the model for viewing.
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called this function
     * @return true if succcessful
     */
    public boolean loadMeshDAEFile(boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            m_currentPScene.getInstances().removeAllChildren();
            if (m_currentHiProcessors != null)
                m_currentHiProcessors.clear();
            
            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path = getRelativePath(m_fileModel);
            String szURL = protocal + path;

            try {
                URL modelURL = new URL(szURL);
                SharedAsset colladaAsset = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, modelURL));
                colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, m_fileModel.getName(), null));
                m_modelInst = m_currentPScene.addModelInstance(m_fileModel.getName(), colladaAsset, new PMatrix());
//                pruneMeshes(m_fileModel.getName(), colladaAsset);
                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        return false;
    }

    public boolean loadMeshDAEFile(boolean useRepository, Component arg0, PNode attatchNode) {
        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path = getRelativePath(m_fileModel);
            String szURL = protocal + path;

            try {
                URL modelURL = new URL(szURL);
                SharedAsset colladaAsset = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, modelURL));
                colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, m_fileModel.getName(), null));
                attatchNode.addChild(m_currentPScene.addModelInstance(m_fileModel.getName(), colladaAsset, new PMatrix()));
                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of a skinned model and then subsequently creates a shared asset to
     * to load the model for viewing.
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called this function
     * @return true if succcessful
     */
    public boolean loadSMeshDAEFile(boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            m_currentPScene.getInstances().removeAllChildren();
            if (m_currentHiProcessors != null)
                m_currentHiProcessors.clear();

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path = getRelativePath(m_fileModel);
            String szURL = protocal + path;

            try {
                URL modelURL = new URL(szURL);
                SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, modelURL));
                character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, m_fileModel.getName(), null));
                String[] anim = null;
                loadInitializer(m_fileModel.getName(), character, anim);
                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of a avatar head (skinned model) and then subsequently creates a shared asset
     * to load the model for viewing.
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called this function
     * @return true if succcessful
     */
    public boolean loadAvatarHeadDAEFile(boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            m_currentPScene.getInstances().removeAllChildren();
            if (m_currentHiProcessors != null)
                m_currentHiProcessors.clear();

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path = getRelativePath(m_fileModel);
            String szURL = protocal + path;

            try {
                URL urlModel = new URL(szURL);
                SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, urlModel));
                character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, m_fileModel.getName(), null));
                String[] anim = null;
                loadInitializer(m_fileModel.getName(), character, anim);
                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of an avatar (skinned model) and then subsequently loads the avatar for
     * viewing by creating a new characer class object and default attributes
     * @param clear - boolean true to clear the pscene of all children
     * @param useRepository - boolean true to use the repository
     * @param arg0 - parent component that called this function
     * @return true if successful
     */
    public boolean loadAvatarDAEFile(boolean clear, boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadAvatarDAE.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadAvatarDAE.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            removeallMeshReferencesOnSkeleton();
            if (clear)
                m_currentPScene.getInstances().removeAllChildren();

            String bindPose = findBindPose(m_fileModel);
            if (bindPose.toString().contains("Female"))
                m_gender = 2;
            else
                m_gender = 1;

            // Create avatar attribs
            CharacterAttributes attribs = createDefaultAttributes(m_gender, bindPose, null, null);

            if (m_avatar != null) {
                m_avatar.destroy();
                setAvatar(null);
            }

            setAvatar(new Avatar(attribs, m_worldManager));
            while(!m_avatar.isInitialized() || m_avatar.getModelInst() == null) {

            }

            m_avatar.selectForInput();
            m_currentPScene = m_avatar.getPScene();
        }
        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of an avatar head (skinned model) and then subsequently uses a load instruction
     * to remove the current avatar's head and swap it with the selected head.
     * @param useRepository - boolean true to use the repository
     * @param arg0 - parent component that called this function
     * @return true if successful
     */
    public boolean addAvatarHeadDAEFile(boolean useRepository, Component arg0) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return false;
        }

        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();

            m_currentPScene.setUseRepository(useRepository);

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path = getRelativePath(m_fileModel);
            String szURL = protocal + path;

            try {

                URL modelURL = m_fileModel.toURI().toURL();
                m_avatar.installHead(modelURL);
                m_avatar.getAttributes().setHeadAttachment(path);
                m_avatar.setDefaultShaders();
                m_avatar.applyMaterials();
                return true;

            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    public boolean addAvatarHeadDAEFileN(boolean useRepository, Component arg0) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return false;
        }

        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();

            m_currentPScene.setUseRepository(useRepository);

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path = getRelativePath(m_fileModel);
            String szURL = protocal + path;

            try {

                URL modelURL = m_fileModel.toURI().toURL();
                m_avatar.installHeadN(modelURL);
                m_avatar.getAttributes().setHeadAttachment(path);
                m_avatar.setDefaultShaders();
                m_avatar.applyMaterials();
                return true;

            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of an clothing (skinned model) and then subsequently uses a load instruction
     * to remove the current clothes at the specified subgroup and loads the new
     * clothes into that subgroup
     * @param useRepository - boolean true to use the repository
     * @param arg0 - parent component that called this function
     * @return true if successful
     */
    public boolean addSMeshDAEFile(boolean useRepository, Component arg0) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return false;
        }

        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();

            String subGroup     = null;

            Object[] subgroups = { m_regions[0], m_regions[1], m_regions[2], m_regions[3], m_regions[4] };
            subGroup = (String)JOptionPane.showInputDialog( new Frame(), "Please select the subgroup to which the meshes will be added",
                                                            "SPECIFY SUBGROUP TO ADD MESHES IN", JOptionPane.PLAIN_MESSAGE,
                                                            null, subgroups, m_regions[0]);

            if (subGroup == null || subGroup.length() <= 0)
                return false;

            m_currentPScene.getInstances().setRenderStop(true);
            m_currentPScene.setUseRepository(useRepository);

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path     = getRelativePath(m_fileModel);
            String szURL    = protocal + path;

            try {
                URL modelURL = m_fileModel.toURI().toURL();

                InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
                Instruction pRootInstruction = new Instruction();
                pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

                m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subGroup);
                m_avatar.getAttributes().deleteAddInstructionsBySubGroup(subGroup);

                String[] meshestodelete = m_avatar.getSkeleton().getMeshNamesBySubGroup(subGroup);
                for (int i = 0; i < meshestodelete.length; i++)
                    pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);

                pRootInstruction.addLoadGeometryToSubgroupInstruction(modelURL, subGroup);

                pProcessor.execute(pRootInstruction);

                while (m_avatar.getSkeleton().getMeshNamesBySubGroup(subGroup).length <= 0) {
                    // Wait till the mesh is loaded and in group
                    try {
                        Thread.sleep(3000);
                        Thread.yield();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                m_avatar.setDefaultShaders();
                m_avatar.applyMaterials();
                m_currentPScene.getInstances().setRenderStop(false);
                
                // TEST CODE TO UPDATE ATTRIBUTES FOR SKINNED MESHES
                String[] meshesToAdd = m_avatar.getSkeleton().getMeshNamesBySubGroup(subGroup);
                SkinnedMeshParams[] smparams    = new SkinnedMeshParams[meshesToAdd.length];
                SkinnedMeshParams[] current     = m_avatar.getAttributes().getAddInstructions();
                for (int i = 0; i < meshesToAdd.length; i++) {
                    smparams[i] = m_avatar.getAttributes().createSkinnedMeshParams(meshesToAdd[i], subGroup);
                }
                List<SkinnedMeshParams> smParams  = new ArrayList<SkinnedMeshParams>();
                for (int i = 0; i < current.length; i++) {
                    smParams.add(current[i]);
                }

                for (int i = 0; i < smparams.length; i++) {
                    smParams.add(smparams[i]);
                }

                SkinnedMeshParams[] newparams   = new SkinnedMeshParams[smParams.size()];
                smParams.toArray(newparams);
                m_avatar.getAttributes().setAddInstructions(newparams);
//                for (int i = 0; i < meshesToAdd.length; i++) {
//                    for (int j = 1; j < meshesToAdd.length; j++) {
//                        if (meshesToAdd[i] == null)
//                            continue;
//
//                        if (meshesToAdd[i].equals(meshesToAdd[j]))
//                            meshesToAdd[j] = null;
//                    }
//                }

                List<String[]> loadinstructs = m_avatar.getAttributes().getLoadInstructions();
//                SkinnedMeshParams[] params = m_avatar.getAttributes().getAddInstructions();
//                ArrayList<SkinnedMeshParams> newParams = new ArrayList<SkinnedMeshParams>();
//
//                for (int i = 0; i < params.length; i++) {
//                    for (int j = 0; j < meshestodelete.length; j++) {
//                        if (params[i].meshName.equals(meshestodelete[j]))
//                            continue;
//                        newParams.add(params[i]);
//                    }
//                }
//
//                for (int i = 0; i < meshesToAdd.length; i++) {
//                    if (meshesToAdd[i] == null)
//                        continue;
//
//                    newParams.add(m_avatar.getAttributes().createSkinnedMeshParams(meshesToAdd[i], subGroup));
//                }
//
//                m_avatar.getAttributes().setAddInstructions(newParams.toArray(new SkinnedMeshParams[newParams.size()]));

                String[] szload   = new String[2];
                szload[0]   = path;
                szload[1]   = subGroup;
                loadinstructs.add(szload);

                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * of an accessory (non-skinned model) and then subsequently uses a load 
     * instruction to remove the current clothes at the specified subgroup and 
     * loads the new accessory
     * @param useRepository - boolean true to use the repository
     * @param arg0 - parent component that called this function
     * @return true if successful
     */
    public boolean addMeshDAEFile(boolean useRepository, Component arg0) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return false;
        }

        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();

            String  subGroup    = null;
            String  parentJoint = null;
            String  meshName    = null;
            int     selection   = -1;

            Object[] subgroups = { "Hair", "FacialHair", "Hats", "Glasses" };
            subGroup = (String)JOptionPane.showInputDialog( new Frame(), "Please select the subgroup to which the meshes will be added",
                                                            "SPECIFY SUBGROUP TO ADD MESHES IN", JOptionPane.PLAIN_MESSAGE,
                                                            null, subgroups, "Hair");

            Object[] joints = { "Head", "Neck" };
            parentJoint = (String)JOptionPane.showInputDialog( new Frame(), "Please select the joint to which the meshes will be added",
                                                            "SPECIFY JOINT TO ADD MESH ON", JOptionPane.PLAIN_MESSAGE,
                                                            null, joints, "Head");

            meshName = (String)JOptionPane.showInputDialog( new Frame(), "Please specify the name of the mesh to be added",
                                                            "SPECIFY THE NAME OF THE MESH", JOptionPane.PLAIN_MESSAGE,
                                                            null, null, "Hair");

            m_currentPScene.getInstances().setRenderStop(true);
            m_currentPScene.setUseRepository(useRepository);

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path     = getRelativePath(m_fileModel);
            String szURL    = protocal + path;

            InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
            Instruction pRootInstruction = new Instruction();
            pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

            m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subGroup);
            m_avatar.getAttributes().deleteAttachmentInstructionsBySubGroup(subGroup);

            PNode mesh = m_avatar.getSkeleton().findChild(parentJoint);
            ArrayList<PNode> meshesToDelete = new ArrayList<PNode>();

            if (mesh.getChildrenCount() > 0) {
                for (int i = 0; i < mesh.getChildrenCount(); i++)
                    meshesToDelete.add(mesh.getChild(i));
            }

            if (mesh != null)
                m_avatar.getSkeleton().findAndRemoveChild(subGroup);
            try {
                pRootInstruction.addChildInstruction(InstructionType.loadGeometry, m_fileModel.toURI().toURL());
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }

            PMatrix tempSolution = new PMatrix();

            pRootInstruction.addAttachmentInstruction( meshName, parentJoint, tempSolution, subGroup );
            pProcessor.execute(pRootInstruction);

            // TEST CODE TO UPDATE ATTRIBUTES FOR MESHES
            List<String[]> loadinstructs = m_avatar.getAttributes().getLoadInstructions();
            AttachmentParams[] attatchments = m_avatar.getAttributes().getAttachmentsInstructions();
            ArrayList<AttachmentParams> newAttatchments = new ArrayList<AttachmentParams>();


            if (attatchments != null) {
                for (int i = 0; i < attatchments.length; i++) {
                    if (meshesToDelete.size() <= 0) {
                        newAttatchments.add(attatchments[i]);
                        continue;
                    }

                    for (int j = 0; j < meshesToDelete.size(); j++) {
                        if (attatchments[i].getMeshName().equals(meshesToDelete.get(j).getName()))
                            continue;
                        newAttatchments.add(attatchments[i]);
                    }
                }
            }

            newAttatchments.add(new AttachmentParams(meshName, parentJoint, tempSolution, subGroup));

            m_avatar.getAttributes().setAttachmentsInstructions(newAttatchments.toArray(new AttachmentParams[newAttatchments.size()]));
            String[] szload   = new String[2];
            szload[0]   = path;
            szload[1]   = subGroup;
            loadinstructs.add(szload);

            while (m_avatar.getSkeleton().findChild(meshName) == null) {
                // Wait till the mesh is loaded
                try {
                    Thread.sleep(3000);
                    Thread.yield();
                } catch (InterruptedException ex) {
                    Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            m_avatar.setDefaultShaders();
            m_avatar.applyMaterials();
            m_currentPScene.getInstances().setRenderStop(false);

            return true;
        }
        return false;
    }

    /**
     * Opens a JFileChooser window for the user to select a collada file (*.dae)
     * that contains animation data and then subsequently uses a load instruction
     * to add the animation to either a body animation group or facial animation
     * group.  The group depends on type specified on load (gui controled)
     * @param type - 1 for body animation and 2 for facial animation (set by gui)
     * @param useRepository - boolean true to use the repository
     * @param arg0 - parent component that called this function
     */
    public void loadDAEAnimationFile(int type, boolean useRepository, Component arg0) {
        if (m_avatar == null) {   // check to make sure you have a skinned meshed model loaded before adding animations
            System.out.println("Please have an avatar loaded before you continue loading animations");
            return;
        }

        int returnValue = m_jFileChooser_LoadAnim.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            java.io.File animation = m_jFileChooser_LoadAnim.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);

            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path     = getRelativePath(animation);
            String szURL    = protocal + path;

            try {
                URL animURL = animation.toURI().toURL();

                InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
                Instruction pRootInstruction = new Instruction();
                pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

                if (type == 0)
                    pRootInstruction.addChildInstruction(InstructionType.loadAnimation, animURL);
                else
                    pRootInstruction.addChildInstruction(InstructionType.loadFacialAnimation, animURL);

                pProcessor.execute(pRootInstruction);
                
                // Facial animation state is designated to id (and index) 1
                AnimationState facialAnimationState = new AnimationState(1);
                facialAnimationState.setCurrentCycle(-1);
                facialAnimationState.setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
                facialAnimationState.setAnimationSpeed(0.1f);
                m_avatar.getSkeleton().addAnimationState(facialAnimationState);
                m_avatar.initiateFacialAnimation(0, 0.2f, 1.0f);
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Sets up default attributes based on the current avatar standard being used.
     * This will be obsolete if the avatar standards change.
     * @param iGender - 1 for male and 2 for female
     * @param szAvatarModelFile - the collada file containing the animations
     * @return CharacterAttributes
     */
    public CharacterAttributes createDefaultAttributes(int iGender, String szAvatarModelFile, String szAvatarHeadModelFile, String szAvatarHandsModelFile) {

        // Create avatar attribs
        CharacterAttributes             attribs     = new CharacterAttributes("Avatar");
        ArrayList<String[]>             load        = new ArrayList<String[]>();
        ArrayList<SkinnedMeshParams>    add         = new ArrayList<SkinnedMeshParams>();

        String baseFilePath = "file:///" + System.getProperty("user.dir") + "/";
        String[] szBind     = new String[2];
        String[] szHands    = new String[2];

        switch (iGender)
        {
            case 1:
            {
                if (szAvatarModelFile == null)
                    szAvatarModelFile = "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae";
                if (szAvatarHandsModelFile == null)
                    szAvatarHandsModelFile = "assets/models/collada/Avatars/MaleAvatar/Male_Hands.dae";
                if (szAvatarHeadModelFile == null)
                    szAvatarHeadModelFile = "assets/models/collada/Heads/MaleHead/MaleCHead.dae";

                szBind[0]   = szAvatarModelFile;
                szBind[1]   = new String("Bind");
                szHands[0]  = szAvatarHandsModelFile;
                szHands[1]  = new String("Hands");

                load.add(szBind);    // Load selected male body meshes
                add.add(attribs.createSkinnedMeshParams("RFootNudeShape",   "Feet"));
                add.add(attribs.createSkinnedMeshParams("LFootNudeShape",   "Feet"));
                add.add(attribs.createSkinnedMeshParams("TorsoNudeShape",   "UpperBody"));
                add.add(attribs.createSkinnedMeshParams("LegsNudeShape",    "LowerBody"));
                load.add(szHands);   // Load selected hand meshes
                add.add(attribs.createSkinnedMeshParams("RHandShape",       "Hands"));
                add.add(attribs.createSkinnedMeshParams("LHandShape",       "Hands"));
                attribs.setHeadAttachment(szAvatarHeadModelFile);
                break;
            }
            case 2:
            {
                if (szAvatarModelFile == null)
                    szAvatarModelFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
                if (szAvatarHandsModelFile == null)
                    szAvatarHandsModelFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae";
                if (szAvatarHeadModelFile == null)
                    szAvatarHeadModelFile = "assets/models/collada/Heads/FemaleHead/FemaleCHead.dae";

                szBind[0]   = szAvatarModelFile;
                szBind[1]   = new String("Bind");
                szHands[0]  = szAvatarHandsModelFile;
                szHands[1]  = new String("Hands");

                load.add(szBind);    // Load selected female skeleton
                add.add(attribs.createSkinnedMeshParams("Torso_NudeShape",      "UpperBody"));
                add.add(attribs.createSkinnedMeshParams("Legs_NudeShape",       "LowerBody"));
                add.add(attribs.createSkinnedMeshParams("FemaleFeet_NudeShape", "Feet"));
                load.add(szHands);   // Load selected female hand meshes
                add.add(attribs.createSkinnedMeshParams("Hands_NudeShape",  "Hands"));
                attribs.setHeadAttachment(szAvatarHeadModelFile);
                break;
            }
        }

        attribs.setBaseURL(null);
        attribs.setLoadInstructions(load);
        attribs.setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
        attribs.setAttachmentsInstructions(null);
        attribs.setGender(iGender);

        return attribs;
    }

    /**
     * Sets up default attributes based on the current avatar standard being used.
     * This will be obsolete if the avatar standards change.
     * @param iGender - 1 for male and 2 for female
     * @param szAvatarModelFile - the collada file containing the animations
     * @return CharacterAttributes
     */
    public CharacterAttributes createDefaultAttributes(int iGender, String szAvatarModelFile, String szAvatarHeadModelFile, String szAvatarHandsModelFile, String protocol) {

        // Create avatar attribs
        CharacterAttributes             attribs     = new CharacterAttributes("Avatar");
        ArrayList<String[]>             load        = new ArrayList<String[]>();
        ArrayList<SkinnedMeshParams>    add         = new ArrayList<SkinnedMeshParams>();

        String baseFilePath = "file:///" + System.getProperty("user.dir") + "/";
        String[] szBind     = new String[2];
        String[] szHands    = new String[2];

        switch (iGender)
        {
            case 1:
            {
                if (szAvatarModelFile == null)
                    szAvatarModelFile = "assets/models/collada/Avatars/MaleAvatar/Male_Bind.dae";
                if (szAvatarHandsModelFile == null)
                    szAvatarHandsModelFile = "assets/models/collada/Avatars/MaleAvatar/Male_Hands.dae";
                if (szAvatarHeadModelFile == null)
                    szAvatarHeadModelFile = "assets/models/collada/Heads/MaleHead/MaleCHead.dae";

                szBind[0]   = szAvatarModelFile;
                szBind[1]   = new String("Bind");
                szHands[0]  = szAvatarHandsModelFile;
                szHands[1]  = new String("Hands");

                load.add(szBind);    // Load selected male body meshes
                add.add(attribs.createSkinnedMeshParams("RFootNudeShape",   "Feet"));
                add.add(attribs.createSkinnedMeshParams("LFootNudeShape",   "Feet"));
                add.add(attribs.createSkinnedMeshParams("TorsoNudeShape",   "UpperBody"));
                add.add(attribs.createSkinnedMeshParams("LegsNudeShape",    "LowerBody"));
                load.add(szHands);   // Load selected hand meshes
                add.add(attribs.createSkinnedMeshParams("RHandShape",       "Hands"));
                add.add(attribs.createSkinnedMeshParams("LHandShape",       "Hands"));
                attribs.setHeadAttachment(szAvatarHeadModelFile);
                break;
            }
            case 2:
            {
                if (szAvatarModelFile == null)
                    szAvatarModelFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Bind.dae";
                if (szAvatarHandsModelFile == null)
                    szAvatarHandsModelFile = "assets/models/collada/Avatars/FemaleAvatar/Female_Hands.dae";
                if (szAvatarHeadModelFile == null)
                    szAvatarHeadModelFile = "assets/models/collada/Heads/FemaleHead/FemaleCHead.dae";

                szBind[0]   = szAvatarModelFile;
                szBind[1]   = new String("Bind");
                szHands[0]  = szAvatarHandsModelFile;
                szHands[1]  = new String("Hands");

                load.add(szBind);    // Load selected female skeleton
                add.add(attribs.createSkinnedMeshParams("Torso_NudeShape",      "UpperBody"));
                add.add(attribs.createSkinnedMeshParams("Legs_NudeShape",       "LowerBody"));
                add.add(attribs.createSkinnedMeshParams("FemaleFeet_NudeShape", "Feet"));
                load.add(szHands);   // Load selected female hand meshes
                add.add(attribs.createSkinnedMeshParams("Hands_NudeShape",  "Hands"));
                attribs.setHeadAttachment(szAvatarHeadModelFile);
                break;
            }
        }

        attribs.setBaseURL(protocol);
        attribs.setLoadInstructions(load);
        attribs.setAddInstructions(add.toArray(new SkinnedMeshParams[add.size()]));
        attribs.setAttachmentsInstructions(null);
        attribs.setGender(iGender);

        return attribs;
    }

    ////////////////////////////////////////////////////////////////////////////
    // LOAD FROM SERVER
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Loads specified collada file (*.dae) which contains information of a
     * non-skinned mesh for viewing by creating a shared asset and loading
     * parmaters. Clears the scene before loading
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called the function
     * @param data - string array containing information about the model
     *        { 0= meshname, 1= description, 2= male/female, 3= file location, 4= meshtype, 5= table id }
     */
    public void loadMeshDAEURL(boolean useRepository, Component arg0, String[] data) {
        m_currentPScene.setUseRepository(useRepository);
        m_currentPScene.getInstances().removeAllChildren();
        if (m_currentHiProcessors != null)
            m_currentHiProcessors.clear();
        
        try {
            URL modelURL = new URL(data[3]);
            SharedAsset colladaAsset = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, modelURL));
            colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, data[0], null));
            pruneMeshes(data[0], colladaAsset);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads specified collada file (*.dae) which contains information of a
     * skinned mesh for viewing by creating a shared asset and loading
     * parmaters.  Clears the scene before loading
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called the function
     * @param data - string array containing information about the model
     *        { 0= meshname, 1= description, 2= male/female, 3= file location, 4= meshtype, 5= table id }
     * @param meshRef - name of meshes contained in the collada file
     */
    public void loadSMeshDAEURL(boolean useRepository, Component arg0, String[] data, String[] meshRef) {
        m_currentPScene.setUseRepository(useRepository);
        m_currentPScene.getInstances().removeAllChildren();
        if (m_currentHiProcessors != null)
            m_currentHiProcessors.clear();

        try {
            URL urlModel = new URL(data[3]);
            SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, urlModel));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, data[0], null));
            String[] anim = null;
            loadInitializer(data[0], character, anim);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads specified collada file (*.dae) which contains information on an avatar
     * for viewing by creating a new character object and attributes.  If no
     * attributes is specified (ie null) default attributes are used.  Clears the
     * scene of all children before loading
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called the function
     * @param attributes - characterattributes for the avatar containing load paramaters
     * @param gender - 1= male 2= female; specifies which defaults to load.
     */
    public void loadAvatarDAEURL(boolean useRepository, Component arg0, String modelLocation, String headLocation, String handLocation, CharacterAttributes attributes, int gender) {
        m_currentPScene.setUseRepository(useRepository);
        removeallMeshReferencesOnSkeleton();
        m_currentPScene.getInstances().removeAllChildren();

        CharacterAttributes attribs = null;
        
        if (attributes != null) {   // assumes bindpose information with the mesh info was already added to the attributes
            attribs = attributes;
        } else {    // requires the bind pose information for mesh information on basic body parts
            if (modelLocation != null)
                attribs = createDefaultAttributes(gender, modelLocation, headLocation, handLocation);
        }

        if (m_avatar != null) {
            m_avatar.destroy();
            m_avatar = null;
        }

        m_avatar = new Avatar(attribs, m_worldManager);
        while(!m_avatar.isInitialized() || m_avatar.getModelInst() == null) {

        }
        m_avatar.selectForInput();
        m_currentPScene = m_avatar.getPScene();
    }

    /**
     * Loads specified collada file (*.dae) that contains information on an avatar
     * head for viewing by creating an asseet and loading paramaters.  Clears out
     * the scene's children before loading.
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called the function
     * @param data - string array containing information about the model
     *        { 0= meshname, 1= description, 2= male/female, 3= file location, 4= meshtype, 5= table id }
     * @param meshRef - name of meshes contained in the collada file
     */
    public void loadAvatarHeadDAEURL(boolean useRepository, Component arg0, String[] data, String[] meshRef) {
        m_currentPScene.setUseRepository(useRepository);
        m_currentPScene.getInstances().removeAllChildren();
        if (m_currentHiProcessors != null)
            m_currentHiProcessors.clear();

        try {
            URL urlModel = new URL(data[3]);
            SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, urlModel));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, data[0], null));
            String[] anim = null;
            loadInitializer(data[0], character, anim);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes the current head installed on the avatar and swaps it out with the
     * selected head model collada file (*.dae).  Method will return out if there
     * is no avatar loaded in the current scene.
     * @param useRepository - boolean true to use the repository
     * @param arg0 - component parent containing information about the model
     * @param data - string array containing information about the model
     *        { 0= meshname, 1= description, 2= male/female, 3= file location, 4= meshtype, 5= table id }
     */
    public void addAvatarHeadDAEURL(boolean useRepository, Component arg0, String data) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return;
        }

        try {
            URL urlHead = new URL(data);
            m_avatar.installHead(urlHead);
            m_avatar.getAttributes().setHeadAttachment(data);
            m_avatar.setDefaultShaders();
            m_avatar.applyMaterials();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addAvatarHeadDAEURLN(boolean useRepository, Component arg0, String data) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return;
        }

        try {
            URL urlHead = new URL(data);
            m_avatar.installHeadN(urlHead);
            m_avatar.getAttributes().setHeadAttachment(data);
            m_avatar.setDefaultShaders();
            m_avatar.applyMaterials();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes the current head installed on the avatar and swaps it out with the
     * selected head model collada file (*.dae).  Method will return out if there
     * is no avatar loaded in the current scene.
     * @param useRepository - boolean true to use the repository
     * @param arg0 - component parent containing information about the model
     * @param data - string array containing information about the model
     *        { 0= meshname, 1= description, 2= male/female, 3= file location, 4= meshtype, 5= table id }
     */
    public void addAvatarHeadDAEURL(boolean useRepository, Component arg0, String url, String relativePath) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return;
        }

        try {
            URL urlHead = new URL(url);
            m_avatar.installHead(urlHead);
            m_avatar.getAttributes().setHeadAttachment(relativePath);
            m_avatar.setDefaultShaders();
            m_avatar.applyMaterials();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addAvatarHeadDAEURLN(boolean useRepository, Component arg0, String url, String relativePath) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return;
        }

        try {
            URL urlHead = new URL(url);
            m_avatar.installHeadN(urlHead);
            m_avatar.getAttributes().setHeadAttachment(relativePath);
            m_avatar.setDefaultShaders();
            m_avatar.applyMaterials();
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes the meshes loaded in the specified subgroup and loads the new collada
     * file (*.dae) mesh data into that subgroup.  Method will return out if there
     * is no avatar loaded in the current scene.
     * @param mesh - URL location of the file containing mesh data
     * @param subgroup - the selected subgroup to load the mesh into
     */
    public void addSMeshDAEURLToModel(URL mesh, String subgroup) {
        if (m_avatar == null) {
            System.out.println("No avatar has been loaded... please load an avatar first");
            return;
        }

        InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

        m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subgroup);
        m_avatar.getAttributes().deleteAddInstructionsBySubGroup(subgroup);

        String[] meshes = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroup);
        for (int i = 0; i < meshes.length; i++)
            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshes[i]);

        pRootInstruction.addLoadGeometryToSubgroupInstruction(mesh, subgroup);
                    
        pProcessor.execute(pRootInstruction);

        while (m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroup).length <= 0) {
            // Wait till the mesh is loaded and in group
            try {
                Thread.sleep(3000);
                Thread.yield();
            } catch (InterruptedException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        m_avatar.setDefaultShaders();
        m_avatar.applyMaterials();

        // TEST CODE TO UPDATE ATTRIBUTES FOR SKINNED MESHES
        String[] meshesToAdd = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroup);
        SkinnedMeshParams[] smparams    = new SkinnedMeshParams[meshesToAdd.length];
        SkinnedMeshParams[] current     = m_avatar.getAttributes().getAddInstructions();
        for (int i = 0; i < meshesToAdd.length; i++) {
            smparams[i] = m_avatar.getAttributes().createSkinnedMeshParams(meshesToAdd[i], subgroup);
        }
        List<SkinnedMeshParams> smParams  = new ArrayList<SkinnedMeshParams>();
        for (int i = 0; i < current.length; i++) {
            smParams.add(current[i]);
        }

        for (int i = 0; i < smparams.length; i++) {
            smParams.add(smparams[i]);
        }

        SkinnedMeshParams[] newparams   = new SkinnedMeshParams[smParams.size()];
        smParams.toArray(newparams);
        m_avatar.getAttributes().setAddInstructions(newparams);

        List<String[]> loadinstructs = m_avatar.getAttributes().getLoadInstructions();
//        SkinnedMeshParams[] params = m_avatar.getAttributes().getAddInstructions();
//        ArrayList<SkinnedMeshParams> newParams = new ArrayList<SkinnedMeshParams>();
//
//        for (int i = 0; i < params.length; i++) {
//            for (int j = 0; j < meshes.length; j++) {
//                if (params[i].meshName.equals(meshes[j]))
//                    continue;
//                newParams.add(params[i]);
//            }
//        }
//
//        for (int i = 0; i < meshesToAdd.length; i++)
//            newParams.add(m_avatar.getAttributes().createSkinnedMeshParams(meshesToAdd[i], subgroup));
//
//        m_avatar.getAttributes().setAddInstructions(newParams.toArray(new SkinnedMeshParams[newParams.size()]));

        String[] szload = new String[2];
        szload[0]   = mesh.toString();
        szload[1]   = subgroup;
        loadinstructs.add(szload);
    }

    /**
     * Removes the previous non-skinned mesh (if one was present) and replaces
     * it with the new mesh(s) specified in the chosen collada file (*.dae).
     * A joint is created and used to attach the mesh onto the model.
     * @param data - string array containg information about the model
     * @param joint2addon - the joint to the new mesh and joint needs to attach on
     * @param prevAttchName - the name of the currently installed mesh to get rid of
     */
    public void addMeshDAEURLToModel(String[] data, String joint2addon, String subGroup) {
        if (m_avatar == null) {
            System.out.println("No avatar has been loaded... please load an avatar first");
            return;
        }

        InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

        m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subGroup);
        m_avatar.getAttributes().deleteAttachmentInstructionsBySubGroup(subGroup);

        PNode joint = m_avatar.getSkeleton().findChild(subGroup);
        ArrayList<PNode> meshesToDelete = new ArrayList<PNode>();
        if (joint != null) {
            for (int i = 0; i < joint.getChildrenCount(); i++)
                meshesToDelete.add(joint.getChild(i));
            m_avatar.getSkeleton().findAndRemoveChild(joint);
        }

        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, data[3]);
        
        PMatrix tempSolution = new PMatrix();

//        if (data[3].toLowerCase().contains("female")) {
//            tempSolution = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0), Vector3f.UNIT_XYZ, new Vector3f(0.0f, 0.0f, 0.03f));
//        } else if (data[3].toLowerCase().contains("male")) {
//            tempSolution = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0), Vector3f.UNIT_XYZ, Vector3f.ZERO);
//        }

        pRootInstruction.addAttachmentInstruction( data[0], joint2addon, tempSolution, subGroup );
        pProcessor.execute(pRootInstruction);

        // TEST CODE TO UPDATE ATTRIBUTES FOR MESHES
        List<String[]> loadinstructs = m_avatar.getAttributes().getLoadInstructions();
        AttachmentParams[] attatchments = m_avatar.getAttributes().getAttachmentsInstructions();
        ArrayList<AttachmentParams> newAttatchments = new ArrayList<AttachmentParams>();

        if (attatchments != null) {

            for (int i = 0; i < attatchments.length; i++) {
                if (meshesToDelete.size() <= 0) {
                    newAttatchments.add(attatchments[i]);
                    continue;
                }

                for (int j = 0; j < meshesToDelete.size(); j++) {
                    if (attatchments[i].getMeshName().equals(meshesToDelete.get(j).getName()))
                        continue;
                    newAttatchments.add(attatchments[i]);
                }
            }
        }

        newAttatchments.add(new AttachmentParams(data[0], joint2addon, tempSolution, subGroup));

        m_avatar.getAttributes().setAttachmentsInstructions(newAttatchments.toArray(new AttachmentParams[newAttatchments.size()]));
        String[] szload = new String[2];
        szload[0]   = data[3];
        szload[1]   = subGroup;
        loadinstructs.add(szload);

        while (m_avatar.getSkeleton().findChild(data[0]) == null) {
            // Wait till the mesh is loaded
            try {
                Thread.sleep(3000);
                Thread.yield();
            } catch (InterruptedException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        m_avatar.setDefaultShaders();
        m_avatar.applyMaterials();
    }

    /**
     * Removes the previous non-skinned mesh (if one was present) and replaces
     * it with the new mesh(s) specified in the chosen collada file (*.dae).
     * A joint is created and used to attach the mesh onto the model.
     * @param meshName - geometry id (name) of mesh in the collada file
     * @param meshLocation - string location of collada file to load
     * @param joint2addon - string name of joint to add mesh to
     * @param subGroup
     */
    public void addMeshDAEURLToModel(String meshName, String meshLocation, String joint2addon, String subGroup) {
        if (m_avatar == null) {
            System.out.println("No avatar has been loaded... please load an avatar first");
            return;
        }

        m_currentPScene.getInstances().setRenderStop(true);
        InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

        m_avatar.getAttributes().deleteLoadInstructionsBySubGroup(subGroup);
        m_avatar.getAttributes().deleteAttachmentInstructionsBySubGroup(subGroup);

        PNode mesh = m_avatar.getSkeleton().findChild(subGroup);
        ArrayList<PNode> meshesToDelete = new ArrayList<PNode>();

        if (mesh.getChildrenCount() > 0) {
            for (int i = 0; i < mesh.getChildrenCount(); i++)
                meshesToDelete.add(mesh.getChild(i));
            m_avatar.getSkeleton().findAndRemoveChild(subGroup);
        }

        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, meshLocation);

        PMatrix tempSolution = new PMatrix();

//        if (meshLocation.toLowerCase().contains("female")) {
//            tempSolution = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0), Vector3f.UNIT_XYZ, new Vector3f(0.0f, 0.0f, 0.03f));
//        } else if (meshLocation.toLowerCase().contains("male")) {
//            tempSolution = new PMatrix(new Vector3f((float)Math.toRadians(10),0,0), Vector3f.UNIT_XYZ, Vector3f.ZERO);
//        }

        pRootInstruction.addAttachmentInstruction( meshName, joint2addon, tempSolution, subGroup );
        pProcessor.execute(pRootInstruction);

        // TEST CODE TO UPDATE ATTRIBUTES FOR MESHES
        List<String[]> loadinstructs = m_avatar.getAttributes().getLoadInstructions();
        AttachmentParams[] attatchments = m_avatar.getAttributes().getAttachmentsInstructions();
        ArrayList<AttachmentParams> newAttatchments = new ArrayList<AttachmentParams>();


        for (int i = 0; i < attatchments.length; i++) {
            if (meshesToDelete.size() <= 0) {
                newAttatchments.add(attatchments[i]);
                continue;
            }

            for (int j = 0; j < meshesToDelete.size(); j++) {
                if (attatchments[i].getMeshName().equals(meshesToDelete.get(j).getName()))
                    continue;
                newAttatchments.add(attatchments[i]);
            }
        }

        newAttatchments.add(new AttachmentParams(meshName, joint2addon, tempSolution, subGroup));
        m_avatar.getAttributes().setAttachmentsInstructions(newAttatchments.toArray(new AttachmentParams[newAttatchments.size()]));
        String[] szload = new String[2];
        szload[0]   = meshLocation;
        szload[1]   = subGroup;
        loadinstructs.add(szload);

        while (m_avatar.getSkeleton().findChild(meshName) == null) {
            // Wait till the mesh is loaded
            try {
                Thread.sleep(3000);
                Thread.yield();
            } catch (InterruptedException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        m_avatar.setDefaultShaders();
        m_avatar.applyMaterials();
        m_currentPScene.getInstances().setRenderStop(false);
    }

    /**
     * Removes meshes from a file that was not meant to be loaded.  Some collada
     * files (*.dae) contain multiple mesh information (ie hair and accessories)
     * and when loaded will load all meshes; this method removes the meshes that
     * are not needed to be loaded.
     * @param meshname - the mesh we want to load
     * @param s - the shared asset used to load the model(s)
     */
    public void pruneMeshes(final String meshname, SharedAsset s) {
        AssetInitializer init = new AssetInitializer() {

            public boolean initialize(Object asset) {

                MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
                proc.setProcessor();
                TreeTraverser.breadthFirst(m_currentPScene, proc);
                Vector<PPolygonMeshInstance> meshes = proc.getMeshInstances();
                for (int i = 0; i < meshes.size(); i++) {
                    if (!meshes.get(i).getName().equals(meshname))
                        m_currentPScene.getInstances().findAndRemoveChild(meshes.get(i));
                    else {
                        meshes.get(i).getTransform().setLocalMatrix(new PMatrix());
                    }

                }
//                setCameraOnModel();
                return true;
            }
        };
        s.setInitializer(init);

        m_modelInst = m_currentPScene.addModelInstance(meshname, s, new PMatrix());

        if (m_currentPScene.getAssetWaitingList().size() <= 0) {
            MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
            proc.setProcessor();
            TreeTraverser.breadthFirst(m_currentPScene, proc);
            Vector<PPolygonMeshInstance> meshes = proc.getMeshInstances();
            for (int i = 0; i < meshes.size(); i++) {
                if (!meshes.get(i).getName().equals(meshname))
                    m_currentPScene.getInstances().findAndRemoveChild(meshes.get(i));
                else {
                    meshes.get(i).getTransform().setLocalMatrix(new PMatrix());
                }

            }
//            setCameraOnModel();
        }
    }

    /**
     * Initializer used to load asset attributes post mesh loading.  Instruction
     * processors can not run until after the model has been loaded, so all post
     * processing is done after the model is initialized.
     * @param n - name of the model
     * @param s - shared asset used to load the model
     * @param a - string array of animations to load
     */
    public void loadInitializer(String n, SharedAsset s, final String[] a) {
        AssetInitializer init = new AssetInitializer() {

            public boolean initialize(Object asset) {

                while (((PNode)asset).getChildrenCount() < 1) {
                    try {
                        Thread.sleep(3000);
                        Thread.yield();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (((PNode) asset).getChild(0) instanceof SkeletonNode) {
                    final SkeletonNode skel = (SkeletonNode) ((PNode) asset).getChild(0);
                    m_skeleton = skel;
                    
                    // Visual Scale
                    if (m_visualScale != 1.0f) {
                        ArrayList<PPolygonSkinnedMeshInstance> meshes = skel.getSkinnedMeshInstances();
                        for (PPolygonSkinnedMeshInstance mesh : meshes) {
                            mesh.getTransform().getLocalMatrix(true).setScale(m_visualScale);
                        }
                    }
                    
                    // Set animations
                    if (a != null) {
                        InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
                        Instruction pRootInstruction = new Instruction();

                        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skel);
                        for (int i = 0; i < a.length; i++) {
                            pRootInstruction.addChildInstruction(InstructionType.loadAnimation, a[i]);
                        }
                        pProcessor.execute(pRootInstruction);
                    }
                    // RED - 01/14/09
                    Repository repo = (Repository)m_worldManager.getUserData(Repository.class);
                    m_skeleton.setShaderOnSkinnedMeshes(repo.newShader(VertDeformerWithSpecAndNormalMap.class));
                    m_skeleton.setShaderOnMeshes(repo.newShader(NormalAndSpecularMapShader.class));
                    if (a != null)
                        ((ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skel, m_worldManager));
                    m_currentPScene.setDirty(true, true);
//                    setCameraOnModel();
                }
                return true;
            }
        };
        s.setInitializer(init);

        m_modelInst = m_currentPScene.addModelInstance(n, s, new PMatrix());
        // Set position
        if (m_origin != null) {
            m_modelInst.getTransform().setLocalMatrix(m_origin);
        }
    }

    /**
     * Adds selected facial or body animations from the server into the the
     * animation subgroups of the avatar.
     * @param useRepository - boolean true to use repository
     * @param arg0 - parent component that called this function
     */
    // TODO: add in sql table for this to work...
    public void loadDAEAnimationURL(boolean useRepository, Component arg0) {
        
    }

    /**
     * Downloads a zipped file to the current user directory and uncompresses the
     * file.
     * @param link - String representation of the file location to download
     * @param destination - destination to save the file to
     */
    public void downloadZipStream(String link, File destination) {
        int a, b;
        String fold;
        
        if (isWindowsOS())
            a = destination.toString().lastIndexOf('\\');
        else
            a = destination.toString().lastIndexOf('/');
        
        b = destination.toString().lastIndexOf(".");
        
        if (isWindowsOS())
            fold = destination.toString().substring(a, b) + '\\';
        else
            fold = destination.toString().substring(a, b) + '/';
        
        File desti = new File(destination.getParent(), fold);
        desti.mkdirs();
        
        downloadURLFile(link, destination);

        try {
            BufferedOutputStream dest = null;
            BufferedInputStream is = null;
            ZipEntry entry;
            ZipFile zipfile = new ZipFile(m_zipFileLoc);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                System.out.println("Extracting: " + entry);
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[m_buffer];
                FileOutputStream fos;
                if (isWindowsOS())
                    fos = new FileOutputStream(desti.toString() + '\\' + entry.getName());
                else
                    fos = new FileOutputStream(desti.toString() + '/' + entry.getName());
                dest = new BufferedOutputStream(fos, m_buffer);
                while ((count = is.read(data, 0, m_buffer)) != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opens  up a file stream to a specified file and downloads it to the
     * specified location.  Called by the ZipStream function to retrieve the file
     * so it can be unpacked.
     * @param address - location of the file to download
     * @param destinationFile - location to save the file.
     */
    public void downloadURLFile(String address, File destinationFile) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        try {
            URL url = new URL(address);
            out = new BufferedOutputStream(new FileOutputStream(destinationFile));
            conn = url.openConnection();
            in = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
            System.out.println(destinationFile + "\t" + numWritten);
        } catch (Exception exception) {
            System.out.println(exception.getMessage() + "... Retrying");
            downloadURLFile(address, destinationFile);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }
        m_zipFileLoc = destinationFile;
    }

    ////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Test method that loads an avatar from a zip file.
     * @param clear - boolean true to clear the current scene before loading
     * @param useRepository - boolean true to use the repository
     * @param arg0 - parent component that called this function
     * @param data - directory location containing the downloaded zip file
     * @param meshRef - meshes to load
     * @param region - location to load meshes
     */
    @Deprecated
    public void loadUnZippedAvatar(boolean clear, boolean useRepository, Component arg0, File data, String[] meshRef, int region) {
        m_currentPScene.setUseRepository(useRepository);
        if (clear)
            m_currentPScene.getInstances().removeAllChildren();

        if (m_currentHiProcessors == null)
            m_currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            m_currentHiProcessors.clear();

        String bindPose = findBindPose(data);
        final ArrayList<URL> animations = findAnims(data);
        String[] anim = new String[animations.size()];
        for (int i = 0; i < animations.size(); i++) {
            anim[i] = animations.get(i).toString();
        }

        int a;
        if (isWindowsOS())
            a = data.toString().lastIndexOf('\\');
        else
            a = data.toString().lastIndexOf('/');
        String name = data.toString().substring(a+1);

        if (clear) {
            SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, bindPose));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, name, null));
            loadInitializer(name, character, anim);
        } else {
            //addDAEMeshURLToModel(data, meshRef, region);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // HELPER FUNCTIONS
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Creates a file that contains the absolute path of a file located on the HD
     * @param file - file located on the local HD
     * @return File containg the absolute path
     */
    public File getAbsPath(File file) {
        String fullpath = null;
        int index;
        if (isWindowsOS())
            index = file.getParent().indexOf(".\\");
        else
            index = file.getParent().indexOf("./");
        
        if (index > 0) {
            fullpath = file.getPath().substring(0, index);
            fullpath += file.getPath().substring(index+2);
        } else
            fullpath = file.getPath();        
        
        File absPath = new File(fullpath);
        return absPath;
    }

    public String getRelativePath(File file) {
        String userDir = System.getProperty("user.dir");
        String szfile = file.toString().substring(userDir.length());

        int index = szfile.indexOf("/");
        szfile = szfile.substring(index + 1);
        int indez = szfile.indexOf(".");

        if (indez == 0)
            szfile = szfile.substring(2);

        return szfile;
    }

    /**
     * Creates and returns an array containing the collada files (*.dae) located
     * in directory specified by the file
     * @param file - direcotry containing the collada files
     * @return string[] - array of collada files located in the directory
     */
    public String[] getFileList(File file) {
        File abs = getAbsPath(file);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File arg0, String arg1) {
                return (arg1.endsWith(".dae"));
            }
        };
        
        String[] colladaList = abs.list(filter);
        return colladaList;
    }

    /**
     * Locates and then returns a URL to the avatar bindpose that is located in
     * the directory specified by the file.  Files must follow a strict naming
     * convention to be found (ie contain BIND in the name);
     * @param file direcotry containing the collada files
     * @return URL to bindpose or null if not found
     */
    public String findBindPose(File file) {
        int index = file.toString().indexOf(".");
        String szfile = file.toString().substring(index +2);
        szfile += "/";
        String[] colladaList = getFileList(file);
        String szURL = null;
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Bind") != -1) {
                return szURL = szfile +colladaList[i];
            }
        }
        return szURL;
    }

    /**
     * Locates and then returns and arraylist of all collada animation files that
     * are located in the directy specified by the file.  Files must follow a
     * strict naming convention to be found (ie contain ANIM in the name);
     * @param file directory containing the collada files
     * @return ArrayList containing all the animation files or null if nothing found
     */
    public ArrayList<URL> findAnims(File file) {
        ArrayList<URL> animURLs = new ArrayList<URL>();
        File abs = getAbsPath(file);
        String absPath = abs.getPath();
        String[] colladaList = getFileList(file);
        
        String szURL = null;
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Anim") != -1) {
                szURL = "file:///" + absPath + "/" + colladaList[i];

                try {
                    URL animURL = new URL(szURL);
                    animURLs.add(animURL);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return animURLs;
    }

    /**
     * Opens a connection to the mySQL database and retrieves data asked for in
     * the string query in the form of an ArrayList of String arrays.  When query
     * is complete the connection is closed.
     * @param query - string containing a syntax correct query for the database
     * @return ArrayList of string arrays containing the data requested
     */
    public ArrayList<String[]> loadSQLData(String query) {
        m_sql = new SQLInterface();
        boolean connected = m_sql.Connect(null, "jdbc:mysql://zeitgeistgames.com:3306/ColladaShop", "ColladaShopper", "ColladaShopperPassword");
        ArrayList<String[]> data = new ArrayList<String[]>();
        
        data = m_sql.Retrieve(query);
        int iNumData = m_sql.getNumColumns();
        ArrayList<String> temp = new ArrayList<String>();
        //int counter = 0;
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < iNumData; j++) {
                temp.add(data.get(i)[j].toString());
                //System.out.println("retrieved " + temp.get(counter));
                //counter++;
            }
        }
        System.out.println("sql query complete");
        m_sql.Disconnect();
        return data;
    }

    /**
     * Sets the camera on the current scene's selected model.  Setting depends on
     * the type of camnera used in the scene.
     */
    public void setCameraOnModel() {
//        while (m_currentPScene.getAssetWaitingList().size() > 0) {}
        PNode node = m_currentPScene.getInstances();
        if (node.getChildrenCount() <= 0)
            return;

        PPolygonModelInstance pmInstance = ((PPolygonModelInstance) node.getChild(0));
        if (m_curCameraProcessor.getState() instanceof TumbleObjectCamState) {

            TumbleObjectCamState camState = ((TumbleObjectCamState)m_curCameraProcessor.getState());
            TumbleObjectCamModel camModel = ((TumbleObjectCamModel)m_curCameraProcessor.getModel());
            camState.setTargetModelInstance(pmInstance);
            camState.setCameraPosition(m_camPos);

            if (pmInstance.getBoundingSphere() == null)
                pmInstance.calculateBoundingSphere();
            camState.setTargetFocalPoint(pmInstance.getBoundingSphere().getCenterRef());
            camModel.turnTo(pmInstance.getBoundingSphere().getCenterRef(), camState);
//            camState.setTargetNeedsUpdate(true);

        } else if (m_curCameraProcessor.getState() instanceof FirstPersonCamState) {

            FirstPersonCamState camState = ((FirstPersonCamState)m_curCameraProcessor.getState());
            FirstPersonCamModel camModel = ((FirstPersonCamModel)m_curCameraProcessor.getModel());
            if (pmInstance.getBoundingSphere() == null)
                pmInstance.calculateBoundingSphere();
            Vector3f pos = pmInstance.getBoundingSphere().getCenterRef();
            pos.z = 3.2f;
            camState.setCameraPosition(pos);

        }
    }

    /**
     * Go through the list of meshes attatched to the avatar and remove them all
     */
    public void removeallMeshReferencesOnSkeleton() {
        for (int i = 0; i < m_prevAttches.length; i++) {
            if (m_prevAttches[i] != null) {
                PNode mesh = m_avatar.getSkeleton().findChild(m_prevAttches[i]);
                if (mesh != null)
                    m_avatar.getSkeleton().findAndRemoveChild(mesh.getParent());
            }
        }
    }

    /**
     * Retrieves the name of the operating system that is running the application
     * @return String containing the name of the operating system
     */
    public String getOS() {
        return System.getProperty("os.name");
    }

    /**
     * Determines if the operating system the application is running is Windows
     * @return boolean true if it's winodows and false if it is not
     */
    public boolean isWindowsOS() {
        return getOS().contains("Windows");
    }

    /**
     * Checks the subgroup for duplicates meshes to add and remove the geometry.
     * @param subgroup
     */
    public void removeDuplicateMeshesBySubgroup(String subgroup) {
        if (m_avatar == null)
            return;

//        PPolygonSkinnedMeshInstance[] sMeshes = m_avatar.getSkeleton().getMeshesBySubGroup(subgroup);
//        for (int i = 0; i < sMeshes.length; i++) {
//            for (int j = i+1; j < sMeshes.length; j++) {
//                if (sMeshes[i].equals(sMeshes[j])) {
//                    m_currentPScene.findAndRemoveChild(sMeshes[j]);
//                    break;
//                }
//            }
//        }

        String[] meshes = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroup);
        for (int i = 0; i < meshes.length; i++) {
            for (int j = i+1; j < meshes.length; j++) {
                if (meshes[i].equals(meshes[j])) {
                    m_currentPScene.findAndRemoveChild(meshes[j]);
                    break;
                }
            }
        }
    }

    /**
     * Creates and returns a CharacterAttribute containng only animation information
     * to be used in adding animations to an existing avatar
     * @param animType
     * @param animFile
     * @return
     */
    public CharacterAttributes makeAnimAttrib(int animType, URL animFile) {
        // Create avatar animation attribs
        CharacterAttributes newAttribs = new CharacterAttributes(m_avatar.getAttributes().getName());
        String[] animation = new String[] { animFile.toString() };

        newAttribs.setBaseURL("");
        if (animType == 0)
            newAttribs.setAnimations(animation);
        else
            newAttribs.setFacialAnimations(animation);

        return newAttribs;
    }

    /**
     * Replaces the texture of the currently selected model
     * TODO: Allow support for textures on multiple meshes.
     * @param modInst (PPolygonModelInstance)
     * @param arg0 (Component)
     */
    public void loadTexture(imi.scene.PNode meshInst, Component arg0) {
        int retValAsset = m_jFileChooser_LoadAssets.showOpenDialog(arg0);
        if (retValAsset == JFileChooser.APPROVE_OPTION) {
            m_fileTexture = m_jFileChooser_LoadAssets.getSelectedFile();

            // Create a material to use
            int iIndex = m_fileTexture.getName().indexOf(".");
            String szName = m_fileTexture.getName().substring(0, iIndex);
            String protocal = "file:///" + System.getProperty("user.dir") + "/";
            String path     = getRelativePath(m_fileModel);
            String szURL    = protocal + path;
            
            try {
                URL urlFile = new URL(szURL);
                PMeshMaterial material = new PMeshMaterial(szName + "Material", urlFile);
                if (meshInst instanceof PPolygonSkinnedMeshInstance) {
                    ((PPolygonSkinnedMeshInstance) meshInst).setMaterial(material);
                } else if (meshInst instanceof PPolygonMeshInstance) {
                    ((PPolygonMeshInstance) meshInst).setMaterial(material);
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Load a previously saved configuration file containing avatar metrics
     * @param arg0 - parent component that called this function
     */
    public void loadAvatarSaveFile(Component arg0) {
        int retVal = m_jFileChooser_LoadXML.showOpenDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                File configfile = m_jFileChooser_LoadXML.getSelectedFile();
                URL configURL   = configfile.toURI().toURL();
                if (m_avatar != null) {
                    m_avatar.destroy();
                    setAvatar(null);
                }
//                if (m_avatar == null) {
                    m_avatar = new Avatar(configURL, m_worldManager);
                    m_currentPScene = m_avatar.getPScene();
//                } else if (m_avatar != null) {
//                    m_avatar.loadConfiguration(configURL);
//                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Save out the current configuration of the avatar into a xml file
     * @param arg0 - parent component that called this function
     */
    public void saveAvatarSaveFile(Component arg0) {
        File saveFile = new File("saveme.xml");
        m_jFileChooser_SaveXML.setSelectedFile(saveFile);

        int retVal = m_jFileChooser_SaveXML.showSaveDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            saveFile = m_jFileChooser_SaveXML.getSelectedFile();
            if (m_avatar != null) {
                m_avatar.getAttributes().deleteLoadInstructionsBySubGroup("Bind");
                m_avatar.saveConfiguration(saveFile);
            }
        }
    }
}
