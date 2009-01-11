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

import com.jme.math.Vector3f;
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
import imi.character.ninja.NinjaAvatar;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationState;
import imi.scene.animation.TransitionCommand;
import imi.scene.animation.TransitionQueue;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 *
 * @author ptruong
 */
public class SceneEssentials {
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
        private Vector3f                        m_camPos                        = new Vector3f(0.0f, 1.5f, -3.2f);
        private FlexibleCameraProcessor         m_curCameraProcessor            = null;
    // Model information
        private String                          m_modelName                     = null;
        private PPolygonModelInstance           m_modelInst                     = null;
        private SkeletonNode                    m_skeleton                      = null;
        private Character                       m_avatar                        = null;
        private int                             m_gender                        = -1;
        private float                           m_visualScale                   = 1.0f;
        private PMatrix                         m_origin                        = new PMatrix();
    // Misc
        private SQLInterface                    m_sql;
        private boolean                         m_bdefaultload                  = false;
        private URL                             m_poloShirt;
        private URL                             m_jeansPants;
        private URL                             m_tennisShoes;
        static final int                        m_buffer                        = 2048;
        private File                            m_zipFileLoc                    = null;
     // Enumerations

        public String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

    public SceneEssentials() {
        initFileChooser();

        try {
            m_poloShirt   = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/Shirts/PoloShirt_M/MalePolo.dae");
            m_jeansPants  = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/Pants/Jeans_M/Jeans.dae");
            m_tennisShoes = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/Shoes/TennisShoes_M/MaleTennisShoes.dae");
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    // Helper Functions
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
    
    // Initializes the JFileChooser GUI
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

                if (f.getName().toLowerCase().endsWith(".dae")) {
                    return true;
                }
                return false;
            }
            @Override
            public String getDescription() {
                String szDescription = new String("Collada (*.dae)");
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
    
    public void openServerBrowser(JFrame c) {
        m_serverFileChooserD = new ServerBrowserDialog(c, true);
        m_serverFileChooserD.initBrowser(0);
        m_serverFileChooserD.setSceneEssentials(this);
        m_serverFileChooserD.setVisible(true);
    }

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
                            material.setShader(new VertDeformerWithSpecAndNormalMap(m_worldManager));
                            // Set the material
                            target.setMaterial(material);
                            target.applyMaterial();

                            PPolygonModelInstance modInst = ((PPolygonModelInstance)skeleton.getParent());
                            ((ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst));
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
                            material.setShader(new VertDeformerWithSpecAndNormalMap(m_worldManager));
                            // Set the material
                            target.setMaterial(material);
                            target.applyMaterial();

                            PPolygonModelInstance modInst = ((PPolygonModelInstance)skeleton.getParent());
                            ((ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst));
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
    public boolean loadMeshDAEFile(boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            m_currentPScene.getInstances().removeAllChildren();
            if (m_currentHiProcessors != null)
                m_currentHiProcessors.clear();
            
            File path = getAbsPath(m_fileModel);
            String szURL;
            
            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            try {
                URL modelURL = new URL(szURL);
                SharedAsset colladaAsset = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
                colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, m_fileModel.getName(), null));
                m_modelInst = m_currentPScene.addModelInstance(m_fileModel.getName(), colladaAsset, new PMatrix());
                //pruneMeshes(m_fileModel.getName(), colladaAsset);
                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
            return false;
        }
        return false;
    }
    
    public boolean loadSMeshDAEFile(boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            m_currentPScene.getInstances().removeAllChildren();
            if (m_currentHiProcessors != null)
                m_currentHiProcessors.clear();

            File path = getAbsPath(m_fileModel);
            String szURL;

            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            try {
                URL modelURL = new URL(szURL);
                SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, modelURL));
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
    
    public boolean loadAvatarDAEFile(boolean clear, boolean useRepository, Component arg0) {
        int returnValue = m_jFileChooser_LoadAvatarDAE.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadAvatarDAE.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);
            if (clear)
                m_currentPScene.getInstances().removeAllChildren();

            if (m_currentHiProcessors == null)
                m_currentHiProcessors = new ArrayList<ProcessorComponent>();
            else
                m_currentHiProcessors.clear();

            URL bindPose = findBindPose(m_fileModel);
            if (bindPose.toString().contains("Female"))
                m_gender = 2;
            else
                m_gender = 1;


            final ArrayList<URL> animations = findAnims(m_fileModel);
            String[] anim = new String[animations.size()];
            for (int i = 0; i < animations.size(); i++) {
                anim[i] = animations.get(i).toString();
            }

            // Create avatar attribs
            CharacterAttributes attribs = new CharacterAttributes("Avatar");

            attribs = new CharacterAttributes("Avatar");
            attribs.setBaseURL("");
            attribs.setLoadInstructions(null);
            attribs.setAddInstructions(null);
            attribs.setAttachmentsInstructions(null);
            attribs.setGender(m_gender);

            if (m_avatar != null) {
                m_worldManager.removeEntity(m_avatar);
                setAvatar(null);
            }

            setAvatar(new NinjaAvatar(attribs, m_worldManager));
            while(!m_avatar.isInitialized()) {

            }
            m_avatar.selectForInput();
            m_currentPScene = m_avatar.getPScene();
        }
        return false;
    }

    public boolean loadAvatarHeadDAEFile(boolean clear, boolean useRepository, Component arg0) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return false;
        }

        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();

            m_currentPScene.setUseRepository(useRepository);
            
            if (clear)
                m_currentPScene.getInstances().removeAllChildren();
            if (m_currentHiProcessors == null)
                m_currentHiProcessors = new ArrayList<ProcessorComponent>();
            else
                m_currentHiProcessors.clear();
            
            File path = getAbsPath(m_fileModel);
            String szURL;

            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://localhost/" + path.getPath());

            URL modelURL = null;

            try {
                modelURL = new URL(szURL);
                m_avatar.installHead(modelURL, "Neck");

                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

    public boolean addSMeshDAEFile(boolean useRepository, Component arg0) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return false;
        }

        int returnValue = m_jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            m_fileModel = m_jFileChooser_LoadColladaModel.getSelectedFile();

            String subGroup     = null;
            String[] meshes     = null;

            Object[] subgroups = { m_regions[0], m_regions[1], m_regions[2], m_regions[3], m_regions[4] };
            subGroup = (String)JOptionPane.showInputDialog( new Frame(), "Please select the subgroup to which the meshes will be added",
                                                            "SPECIFY SUBGROUP TO ADD MESHES IN", JOptionPane.PLAIN_MESSAGE,
                                                            null, subgroups, m_regions[0]);

            if (subGroup == null || subGroup.length() <= 0)
                return false;

            m_currentPScene.setUseRepository(useRepository);

            File path = getAbsPath(m_fileModel);
            String szURL;

            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            try {
                URL modelURL = new URL(szURL);

                InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
                Instruction pRootInstruction = new Instruction();
                pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

                String[] meshestodelete = m_avatar.getSkeleton().getMeshNamesBySubGroup(subGroup);
                for (int i = 0; i < meshes.length; i++)
                    pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);

                pRootInstruction.addLoadGeometryToSubgroupInstruction(modelURL, subGroup);

                pProcessor.execute(pRootInstruction);

                m_avatar.getSkeleton().setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(m_worldManager));
                m_avatar.getSkeleton().setShaderOnMeshes(new NormalAndSpecularMapShader(m_worldManager));

                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public void loadDAEAnimationFile(int type, boolean useRepository, Component arg0) {
        if (m_avatar == null) {   // check to make sure you have a skinned meshed model loaded before adding animations
            System.out.println("Please have an avatar loaded before you continue loading animations");
            return;
        }

        int returnValue = m_jFileChooser_LoadAnim.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            java.io.File animation = m_jFileChooser_LoadAnim.getSelectedFile();
            m_currentPScene.setUseRepository(useRepository);

            File path       = getAbsPath(animation);
            String szURL;

            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            URL animURL    = null;
            try {
                animURL = new URL(szURL);

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
                if (m_avatar.getSkeleton().getAnimationComponent().getGroups().size() > 1)
                {
                    if (m_avatar.getSkeleton().getAnimationGroup(1).getCycleCount() > 1)
                        m_avatar.setDefaultFacePose(1);
                    else
                        m_avatar.setDefaultFacePose(0);

                    TransitionQueue facialAnimQ = m_avatar.getFacialAnimationQ();
                    // Go to default face pose
                    facialAnimQ.addTransition(new TransitionCommand(m_avatar.getDefaultFacePose(), m_avatar.getDefaultFacePoseTiming(), PlaybackMode.PlayOnce, false));
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // LOAD FROM SERVER
    ////////////////////////////////////////////////////////////////////////////
    public void loadMeshDAEURL(boolean useRepository, Component arg0, String[] data) {
        m_currentPScene.setUseRepository(useRepository);
        m_currentPScene.getInstances().removeAllChildren();
        if (m_currentHiProcessors != null)
            m_currentHiProcessors.clear();
        
        try {
            URL modelURL = new URL(data[3]);
            SharedAsset colladaAsset = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
            colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, data[0], null));
            pruneMeshes(data[0], colladaAsset);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadSMeshDAEURL(boolean useRepository, Component arg0, String[] data, String[] meshRef) {
        m_currentPScene.setUseRepository(useRepository);
        m_currentPScene.getInstances().removeAllChildren();
        if (m_currentHiProcessors != null)
            m_currentHiProcessors.clear();

        try {
            URL urlModel = new URL(data[3]);
            SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, urlModel));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, data[0], null));
            String[] anim = null;
            loadInitializer(data[0], character, anim);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadAvatarDAEURL(boolean useRepository, Component arg0, String[] anim, int gender) {
        m_currentPScene.setUseRepository(useRepository);

        m_currentPScene.getInstances().removeAllChildren();
        
        CharacterAttributes attribs = new CharacterAttributes("Avatar");
        attribs.setBaseURL("");
        attribs.setLoadInstructions(null);
        attribs.setAddInstructions(null);
        attribs.setAttachmentsInstructions(null);
        attribs.setGender(gender);

        if (m_avatar != null) {
            m_avatar.die();
            m_avatar = null;
        }

        m_avatar = new NinjaAvatar(attribs, m_worldManager);
        while(!m_avatar.isInitialized() || m_avatar.getModelInst() == null) {

        }
        m_avatar.selectForInput();
        m_currentPScene = m_avatar.getPScene();
    }

    public void loadAvatarHeadDAEURL(boolean useRepository, Component arg0, String[] data, String[] faceanim) {
        if (m_avatar == null) {
            System.out.println("You have not loaded an avatar yet... Please load one first");
            return;
        }

        try {
            URL urlHead = new URL(data[3]);
            m_avatar.installHead(urlHead, "Neck");
            if (faceanim != null) {     // TODO: load default facial anim base on gender

            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addSMeshDAEURLToModel(URL mesh, String subgroup) {
        if (m_avatar == null) {
            System.out.println("No avatar has been loaded... please load an avatar first");
            return;
        }

        InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());
        
        String[] meshes = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroup);
        for (int i = 0; i < meshes.length; i++)
            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshes[i]);

        pRootInstruction.addLoadGeometryToSubgroupInstruction(mesh, subgroup);
                    
        pProcessor.execute(pRootInstruction);

        m_avatar.getSkeleton().setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(m_worldManager));
        m_avatar.getSkeleton().setShaderOnMeshes(new NormalAndSpecularMapShader(m_worldManager));
    }

    public void addMeshDAEURLToModel(String[] data, String joint2addon, int region) {
        if (m_avatar == null) {
            System.out.println("No avatar has been loaded... please load an avatar first");
            return;
        }

        InstructionProcessor pProcessor = new InstructionProcessor(m_worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, m_avatar.getSkeleton());

        String subgroup = null;
        switch (region) {
            case 5:
            {
                subgroup = "Hair";
                break;
            }
            case 6:
            {
                subgroup = "FacialHair";
                break;
            }
            case 7:
            {
                subgroup = "Hats";
                break;
            }
            case 8:
            {
                subgroup = "Glasses";
                break;
            }
        }

        String[] meshes = m_avatar.getSkeleton().getMeshNamesBySubGroup(subgroup);
        for (int i = 0; i < meshes.length; i++)
            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshes[i]);

//        if (joint2addon.equals("skeletonRoot")) {
//            PNode node = null;
//            SkinnedMeshJoint rootJoint = null;
//
//            node = currentPScene.getInstances().findChild("testJoint");
//
//            if (node == null) {
//                rootJoint = new SkinnedMeshJoint("testJoint", new PTransform());
//                node = currentPScene.getInstances().getChild(0).getChild(0);
//                node.addChild(rootJoint);
//            }
//
//            joint2addon = "testJoint";
//        }

        String szName = joint2addon;

        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, data[3]);
        
        PMatrix tempSolution;
        if (data[3].indexOf("Female") != -1) {
            tempSolution = new PMatrix();
            tempSolution.setRotation(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f));
        } else
            tempSolution = new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), Vector3f.ZERO);

        pRootInstruction.addAttachmentInstruction( data[0], szName, tempSolution);
        pProcessor.execute(pRootInstruction);
    }

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
                setCameraOnModel();
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
            setCameraOnModel();
        }
    }

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

                    m_skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(m_worldManager));
                    m_skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(m_worldManager));
                    ((ProcessorCollectionComponent)m_currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skel));
                    m_currentPScene.setDirty(true, true);
                    setCameraOnModel();
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
    
    public void loadDAEAnimationURL(boolean useRepository, Component arg0) {
        // TODO: add in sql table for this to work...
    }

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
    public void loadUnZippedAvatar(boolean clear, boolean useRepository, Component arg0, File data, String[] meshRef, int region) {
        m_currentPScene.setUseRepository(useRepository);
        if (clear)
            m_currentPScene.getInstances().removeAllChildren();

        if (m_currentHiProcessors == null)
            m_currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            m_currentHiProcessors.clear();

        URL bindPose = findBindPose(data);
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
            SharedAsset character = new SharedAsset(m_currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, bindPose));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, name, null));
            loadInitializer(name, character, anim);
        } else {
            //addDAEMeshURLToModel(data, meshRef, region);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // HELPER FUNCTIONS
    ////////////////////////////////////////////////////////////////////////////
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
    
    public URL findBindPose(File file) {
        File abs = getAbsPath(file);
        String absPath = abs.getPath();
        String[] colladaList = getFileList(file);
        
        String bind = null;
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Bind") != -1) {
                if (isWindowsOS())
                    bind = absPath + '\\' + colladaList[i];
                else
                    bind = absPath + '/' + colladaList[i];
                break;
            }
        }
        
        String szURL;
        
        if (isWindowsOS())
            szURL = new String("file:\\\\" + bind);
        else
            szURL = new String("file://" + bind);

        URL modelURL = null;
        try {
            modelURL = new URL(szURL);
            return modelURL;
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modelURL;
    }
    
    public ArrayList<URL> findAnims(File file) {
        ArrayList<URL> animURLs = new ArrayList<URL>();
        File abs = getAbsPath(file);
        String absPath = abs.getPath();
        String[] colladaList = getFileList(file);
        
        String anim = null;
        String szAnim = null;
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Anim") != -1) {
                if (isWindowsOS()) {
                    anim = absPath + '\\' + colladaList[i];
                    szAnim = new String("file:\\\\" + anim);
                } else {
                    anim = absPath + "/" + colladaList[i];
                    szAnim = new String("file://" + anim);
                }

                try {
                    URL animURL = new URL(szAnim);
                    animURLs.add(animURL);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return animURLs;
    }
    
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

    public void setCameraOnModel() {
        while (m_currentPScene.getAssetWaitingList().size() > 0) {}
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
            camState.setTargetFocalPoint(pmInstance.getBoundingSphere().getCenter());
            camModel.turnTo(pmInstance.getBoundingSphere().getCenter(), camState);
            camState.setTargetNeedsUpdate(true);
        } else if (m_curCameraProcessor.getState() instanceof FirstPersonCamState) {
            FirstPersonCamState camState = ((FirstPersonCamState)m_curCameraProcessor.getState());
            if (pmInstance.getBoundingSphere() == null)
                pmInstance.calculateBoundingSphere();
            Vector3f pos = pmInstance.getBoundingSphere().getCenter();
            pos.z = -2.2f;
            camState.setCameraPosition(pos);
        }
    }

    public String getOS() {
        return System.getProperty("os.name");
    }
    
    public boolean isWindowsOS() {
        return getOS().contains("Windows");
    }

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
            File fullpath = getAbsPath(m_fileTexture);
            String szURL;
            
            if (isWindowsOS())
                szURL= new String("file:\\\\" + fullpath.toString());
            else
                szURL= new String("file://" + fullpath.toString());
            
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

    public void loadAvatarSaveFile(Component arg0) {
        int retVal = m_jFileChooser_LoadXML.showOpenDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                File configfile = m_jFileChooser_LoadXML.getSelectedFile();
                URL configURL   = configfile.toURI().toURL();
                if (m_avatar == null) {
                    m_avatar = new NinjaAvatar(configURL, m_worldManager);
                } else if (m_avatar != null) {
                    m_avatar.loadConfiguration(configURL);
                }                
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void saveAvatarSaveFile(Component arg0) {
        File saveFile = new File("saveme.xml");
        m_jFileChooser_SaveXML.setSelectedFile(saveFile);

        int retVal = m_jFileChooser_SaveXML.showSaveDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            saveFile = m_jFileChooser_SaveXML.getSelectedFile();
            if (m_avatar != null) {
                m_avatar.saveConfiguration(saveFile);
            }
        }
    }
}
