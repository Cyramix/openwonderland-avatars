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
import imi.loaders.collada.Instruction;
import imi.loaders.collada.Instruction.InstructionType;
import imi.loaders.collada.InstructionProcessor;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.character.Character;
import imi.character.ninja.NinjaAvatar;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.behaviors.TumbleObjectCamModel;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.camera.state.TumbleObjectCamState;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.VertDeformerWithNormalMapping;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.shader.programs.VertexDeformer;
import imi.scene.utils.tree.MeshInstanceSearchProcessor;
import imi.scene.utils.tree.TreeTraverser;
import imi.sql.SQLInterface;
import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.print.DocFlavor.CHAR_ARRAY;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
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
        private JScene currentJScene = null;
        private PScene currentPScene = null;
        private Entity currentEntity = null;
        private WorldManager worldManager = null;
        private ArrayList<ProcessorComponent> currentHiProcessors = null;
        private PPolygonModelInstance modelInst = null;
    // File IO GUI
        private JFileChooser jFileChooser_LoadAssets = null;
        private JFileChooser jFileChooser_LoadColladaModel = null;
        private JFileChooser jFileChooser_LoadModel = null;
        private JFileChooser jFileChooser_LoadXML = null;
        private JFileChooser jFileChooser_LoadAvatarDAE = null;
        private JFileChooser jFileChooser_SaveXML = null;
        private ServerBrowserDialog serverFileChooserD = null;
        private JPanel_ServerBrowser serverBrowserPanel = null;
    // File Containers
        private File fileXML     = null;
        private File fileModel   = null;
        private File fileTexture = null;
    // Names
        private String modelName = null;
    // OTHER
        private float visualScale = 1.0f;
        private PMatrix origin = new PMatrix();
        private SkeletonNode skeleton = null;
        private Map<Integer, String[]> meshsetup = null;
        private SQLInterface m_sql;
        private Vector3f camPos = new Vector3f(0.0f, 1.5f, -3.2f);
        private FlexibleCameraProcessor curCameraProcessor = null;
        private boolean bhairLoaded = false;
        private boolean bdefaultload = false;
     // URLS
        private URL poloShirt;
        private URL jeansPants;
        private URL tennisShoes;
        static final int BUFFER = 2048;
        private File zipFileLoc = null;
     // Avatar
        private Character avatar;
        private int gender;
        public String[] m_regions = new String[] { "Head", "Hands", "UpperBody", "LowerBody", "Feet", "Hair", "FacialHair", "Hats", "Glasses", "Jackets" };

    public SceneEssentials() {
        initFileChooser();

        try {
            poloShirt   = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/Shirts/PoloShirt_M/MalePolo.dae");
            jeansPants  = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/Pants/Jeans_M/Jeans.dae");
            tennisShoes = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/Shoes/TennisShoes_M/MaleTennisShoes.dae");
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    // Accessors
    public JScene getJScene() { return currentJScene; }
    public PScene getPScene() { return currentPScene; }
    public Entity getEntity() { return currentEntity; }
    public WorldManager getWM() { return worldManager; }
    public ArrayList<ProcessorComponent> getProcessors() { return currentHiProcessors; }
    public File getFileXML() { return fileXML; }
    public File getFileModel() { return fileModel; }
    public File getFileTexture() { return fileTexture; }
    public PPolygonModelInstance getModelInstance() { return modelInst; }
    public SkeletonNode getCurrentSkeleton() { return skeleton; }
    public Map<Integer, String[]> getMeshSetup() { return meshsetup; }
    public FlexibleCameraProcessor getCurCamProcessor() {
        return curCameraProcessor;
    }
    public boolean isDefaultLoad() {
        return bdefaultload;
    }
    public Character getAvatar() {
        return avatar;
    }
    public int getGender() {
        return gender;
    }

    // Mutators
    public void setJScene(JScene jscene) { currentJScene = jscene; }
    public void setPScene(PScene pscene) { currentPScene = pscene; }
    public void setEntity(Entity entity) { currentEntity = entity; }
    public void setWM(WorldManager wm) { worldManager = wm; }
    public void setProcessors(ArrayList<ProcessorComponent> processors) { currentHiProcessors = processors; }
    public void setfileXML(File file) { fileXML = file; }
    public void setfileModel(File file) { fileModel = file; }
    public void setfileTexture(File file) { fileTexture = file; }
    public void setModelInstance(PPolygonModelInstance modinstance) { modelInst = modinstance; }
    public void setModelName(String name) { modelName = name; }
    public void setCurrentSkeleton(SkeletonNode s) { skeleton = s; }
    public void searchnSetSkeleton(PScene p) {
        if (p.getInstances() == null || p.getInstances().getChild(0) == null) {
            skeleton = null;
            return;
        }
        
        skeleton = ((SkeletonNode)p.getInstances().getChild(0).getChild(0));
    }
    public void setMeshSetup(Map<Integer, String[]> m) { meshsetup = m; }
    public void setCurCamProcessor(FlexibleCameraProcessor camProc) {
        curCameraProcessor = camProc;
    }
    public void setDefaultLoad(boolean load) {
        bdefaultload = load;
    }
    public void setAvatar(Character c) {
        avatar = c;
    }
    public void setGender(int sex) {
        gender = sex;
    }
    // Helper Functions
    public void setSceneData(JScene jscene, PScene pscene, Entity entity, WorldManager wm, ArrayList<ProcessorComponent> processors) {
        currentJScene = jscene;
        
        if (pscene == null)
            currentPScene = jscene.getPScene();
        else
            currentPScene = pscene;
        
        if (entity != null)
            currentEntity = entity;
        
        if (wm != null)
            worldManager = wm;
        
        if (processors != null)
            currentHiProcessors = processors;
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
        jFileChooser_LoadAssets = new javax.swing.JFileChooser();
        jFileChooser_LoadAssets.setDialogTitle("Load Texture File");
        java.io.File assetDirectory;

        if (isWindowsOS())
            assetDirectory = new java.io.File(".\\assets\\textures");
        else
            assetDirectory = new java.io.File("./assets/textures");

        jFileChooser_LoadAssets.setCurrentDirectory(assetDirectory);
        jFileChooser_LoadAssets.setDoubleBuffered(true);
        jFileChooser_LoadAssets.setDragEnabled(true);
        jFileChooser_LoadAssets.addChoosableFileFilter((FileFilter)assetFilter);
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

        jFileChooser_LoadColladaModel = new javax.swing.JFileChooser();
        jFileChooser_LoadColladaModel.setDialogTitle("Load Collada File");
        java.io.File colladaDirectory;

        if (isWindowsOS())
            colladaDirectory = new java.io.File(".\\assets\\models\\collada");
        else
            colladaDirectory = new java.io.File("./assets/models/collada");

        jFileChooser_LoadColladaModel.setCurrentDirectory(colladaDirectory);
        jFileChooser_LoadColladaModel.setDoubleBuffered(true);
        jFileChooser_LoadColladaModel.setDragEnabled(true);
        jFileChooser_LoadColladaModel.addChoosableFileFilter((FileFilter)colladaFilter);
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
        jFileChooser_LoadModel = new javax.swing.JFileChooser();
        jFileChooser_LoadModel.setDialogTitle("Load Model File");
        java.io.File modelDirectory;
        
        if (isWindowsOS())
            modelDirectory = new java.io.File(".\\assets\\models");
        else
            modelDirectory = new java.io.File("./assets/models");

        jFileChooser_LoadModel.setCurrentDirectory(modelDirectory);
        jFileChooser_LoadModel.setDoubleBuffered(true);
        jFileChooser_LoadModel.setDragEnabled(true);
        jFileChooser_LoadModel.addChoosableFileFilter((FileFilter)modelFilter);
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
        jFileChooser_LoadXML = new javax.swing.JFileChooser();
        jFileChooser_LoadXML.setDialogTitle("Load Configuration File");
        java.io.File xmlDirectory;

        if (isWindowsOS())
            xmlDirectory = new java.io.File(".\\assets\\");
        else
            xmlDirectory = new java.io.File("./assets/");

        jFileChooser_LoadXML.setCurrentDirectory(xmlDirectory);
        jFileChooser_LoadXML.setDoubleBuffered(true);
        jFileChooser_LoadXML.setDragEnabled(true);
        jFileChooser_LoadXML.addChoosableFileFilter((FileFilter)xmlFilter);
////////////////////////////////////////////////////////////////////////////////
        jFileChooser_LoadAvatarDAE = new javax.swing.JFileChooser();
        jFileChooser_LoadAvatarDAE.setDialogTitle("Load Avatar Model");
        java.io.File avatarDirectory;

        if (isWindowsOS())
            avatarDirectory = new java.io.File(".\\assets\\models\\collada");
        else
            avatarDirectory = new java.io.File("./assets/models/collada");

        jFileChooser_LoadAvatarDAE.setCurrentDirectory(avatarDirectory);
        jFileChooser_LoadAvatarDAE.setDoubleBuffered(true);
        jFileChooser_LoadAvatarDAE.setDragEnabled(true);
        jFileChooser_LoadAvatarDAE.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
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
        jFileChooser_SaveXML = new javax.swing.JFileChooser();
        jFileChooser_SaveXML.setDialogTitle("Save Extensible Markup Language File");
        java.io.File source = new java.io.File("./assets/");
        jFileChooser_SaveXML.setCurrentDirectory(xmlDirectory);

        jFileChooser_SaveXML.setDragEnabled(true);
        jFileChooser_SaveXML.addChoosableFileFilter((FileFilter)configFilter);

    }
    
    public void openServerBrowser(JFrame c) {
        serverFileChooserD = new ServerBrowserDialog(c, true);
        serverFileChooserD.initBrowser(0);
        serverFileChooserD.setSceneEssentials(this);
        serverFileChooserD.setVisible(true);
    }

    public JPanel_ServerBrowser openServerBrowserPanel() {
        serverBrowserPanel = new JPanel_ServerBrowser();
        serverBrowserPanel.initBrowser(0);
        serverBrowserPanel.setSceneEssentials(this);
        serverBrowserPanel.setVisible(true);
        return serverBrowserPanel;
    }
    
    /**
     * Removes old model data from the pscene and replaces it with the user 
     * selected milkshake data
     */
    public void loadMS3DFile(int condition, boolean clear, Component arg0) {
        // 1- Create a shared asset for the repository
        SharedAsset newAsset = new SharedAsset(
                currentPScene.getRepository(),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D_SkinnedMesh, fileModel));  // Type set as MS3D_SkinnedMesh to load both skinned and regular
        if(condition == 0) {
            // 2 - Open dialog to load associated texture for model
            int retValAsset = jFileChooser_LoadAssets.showOpenDialog(arg0);
            // 3- Get the name and path to the asset
            if (retValAsset == JFileChooser.APPROVE_OPTION) {
                // 4- Add new model to the current PScene
                if(clear)
                    currentPScene.getInstances().removeAllChildren();
                if(currentHiProcessors == null)
                    currentHiProcessors = new ArrayList<ProcessorComponent>();
                else {
                    ProcessorCollectionComponent comp = (ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class);
                    for(int i = 0; i < currentHiProcessors.size(); i++)
                        comp.removeProcessor(currentHiProcessors.get(i));
                    currentHiProcessors.clear();
                }
                // 5- Set up an initializer to excecute once the asset is loaded into the scene
                fileTexture = jFileChooser_LoadAssets.getSelectedFile();
                
                newAsset.setInitializer(
                new AssetInitializer() {                                        // WARNING: problem with not being ready when the model is ready
                    public boolean initialize(Object asset) {
                        if (asset != null && asset instanceof SkeletonNode) {
                            
                            SkeletonNode skeleton = (SkeletonNode)asset;
                            skeleton.getAnimationState().setCurrentCycle(0);
                            skeleton.getAnimationState().setPauseAnimation(false);
                            PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)((SkeletonNode)asset).findChild("MS3DSkinnedMesh");

                            // Create a material to use
                            int iIndex = fileTexture.getName().indexOf(".");
                            String szName = fileTexture.getName().substring(0, iIndex);
                            int index = fileTexture.getPath().indexOf("assets");
                            String szTemp = fileTexture.getPath().substring(index, fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            material.setShader(new VertDeformerWithSpecAndNormalMap(worldManager));
                            // Set the material
                            target.setMaterial(material);
                            // We must disable the use of the geometry's material to see the texture we set for the instance
                            target.setUseGeometryMaterial(false);

                            PPolygonModelInstance modInst = ((PPolygonModelInstance)skeleton.getParent());
                            ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst));
                        } else {
                            PPolygonMeshInstance target = (PPolygonMeshInstance) asset;
                            // Create a material to use
                            int iIndex = fileTexture.getName().indexOf(".");
                            String szName = fileTexture.getName().substring(0, iIndex);
                            int index = fileTexture.getPath().indexOf("assets");
                            String szTemp = fileTexture.getPath().substring(index, fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            // Set the material
                            target.setMaterial(material);
                            // We must disable the use of the geometry's material to see the texture we set for the instance
                            target.setUseGeometryMaterial(false);
                        }
                        currentPScene.setDirty(true, true);
                        return true;
                    }
                });                
                modelInst = currentPScene.addModelInstance(modelName, newAsset, new PMatrix());
            }
        }
        else {
            if(clear)
                currentPScene.getInstances().removeAllChildren();
            if(currentHiProcessors == null)
                currentHiProcessors = new ArrayList<ProcessorComponent>();
            else {
                ProcessorCollectionComponent comp = (ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class);
                for(int i = 0; i < currentHiProcessors.size(); i++)
                    comp.removeProcessor(currentHiProcessors.get(i));
                currentHiProcessors.clear();
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
                            int iIndex = fileTexture.getName().indexOf(".");
                            String szName = fileTexture.getName().substring(0, iIndex);
                            int index = fileTexture.getPath().indexOf("assets");
                            String szTemp = fileTexture.getPath().substring(index, fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            material.setShader(new VertDeformerWithSpecAndNormalMap(worldManager));
                            // Set the material
                            target.setMaterial(material);
                            // We must disable the use of the geometry's material to see the texture we set for the instance
                            target.setUseGeometryMaterial(false);

                            PPolygonModelInstance modInst = ((PPolygonModelInstance)skeleton.getParent());
                            ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst));
                        } else {
                            PPolygonMeshInstance target = (PPolygonMeshInstance) asset;
                            // Create a material to use
                            int iIndex = fileTexture.getName().indexOf(".");
                            String szName = fileTexture.getName().substring(0, iIndex);
                            int index = fileTexture.getPath().indexOf("assets");
                            String szTemp = fileTexture.getPath().substring(index, fileTexture.getPath().length());

                            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
                            // Set the material
                            target.setMaterial(material);
                            // We must disable the use of the geometry's material to see the texture we set for the instance
                            target.setUseGeometryMaterial(false);
                        }
                        currentPScene.setDirty(true, true);
                        return true;
                    }
                });                
                modelInst = currentPScene.addModelInstance(modelName, newAsset, new PMatrix());
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // LOAD FROM USER HD - Collada
    ////////////////////////////////////////////////////////////////////////////
    public boolean loadMeshDAEFile(boolean clear, boolean useRepository, Component arg0) {
        int returnValue = jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            fileModel = jFileChooser_LoadColladaModel.getSelectedFile();
            currentPScene.setUseRepository(useRepository);
            if (clear)
                currentPScene.getInstances().removeAllChildren();
            if (currentHiProcessors == null)
                currentHiProcessors = new ArrayList<ProcessorComponent>();
            else
                currentHiProcessors.clear();
            
            File path = getAbsPath(fileModel);
            String szURL;
            
            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            URL modelURL = null;

            try {
                modelURL = new URL(szURL);
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }

            SharedAsset colladaAsset = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
            colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, fileModel.getName(), null));
            modelInst = currentPScene.addModelInstance(fileModel.getName(), colladaAsset, new PMatrix());
            return true;
        }
        return false;
    }
    
    public boolean loadSMeshDAEFile(boolean clear, boolean useRepository, Component arg0) {
        int returnValue = jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            fileModel = jFileChooser_LoadColladaModel.getSelectedFile();
            currentPScene.setUseRepository(useRepository);
            if (clear)
                currentPScene.getInstances().removeAllChildren();
            if (currentHiProcessors == null)
                currentHiProcessors = new ArrayList<ProcessorComponent>();
            else
                currentHiProcessors.clear();
            
            File path = getAbsPath(fileModel);
            String szURL;

            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            URL modelURL = null;

            try {
                modelURL = new URL(szURL);
                SharedAsset colladaAsset = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
                colladaAsset.setUserData(new ColladaLoaderParams(true, true, false, false, 4, fileModel.getName(), null));
                String[] anim = null;
                loadInitializer(fileModel.getName(), colladaAsset, anim);
                return true;
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    public boolean loadAvatarDAEFile(boolean clear, boolean useRepository, Component arg0) {
        int returnValue = jFileChooser_LoadAvatarDAE.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            fileModel = jFileChooser_LoadAvatarDAE.getSelectedFile();
            currentPScene.setUseRepository(useRepository);
            if (clear)
                currentPScene.getInstances().removeAllChildren();

            if (currentHiProcessors == null)
                currentHiProcessors = new ArrayList<ProcessorComponent>();
            else
                currentHiProcessors.clear();

            loadDAEChar();
        }
        return false;
    }
    
    public void loadDAEChar() {
        URL bindPose = findBindPose(fileModel);
        final ArrayList<URL> animations = findAnims(fileModel);
        String[] anim = new String[animations.size()];
        for (int i = 0; i < animations.size(); i++) {
            anim[i] = animations.get(i).toString();
        }
        setDefaultMeshSwapList();
        SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, bindPose));
        character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, fileModel.getName(), null));
        loadInitializer(fileModel.getName(), character, anim);
    }

    public void loadDAEAnimationFile(boolean useRepository, Component arg0) {
        if (skeleton == null)   // check to make sure you have a skinned meshed model loaded before adding animations
            return;

        int returnValue = jFileChooser_LoadColladaModel.showOpenDialog(arg0);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            fileModel = jFileChooser_LoadColladaModel.getSelectedFile();
            currentPScene.setUseRepository(useRepository);

            File path       = getAbsPath(fileModel);
            String szURL;

            if (isWindowsOS())
                szURL = new String("file:\\" + path.getPath());
            else
                szURL = new String("file://" + path.getPath());

            URL modelURL    = null;
            try {
                modelURL = new URL(szURL);
                InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
                Instruction pRootInstruction = new Instruction();
                pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skeleton);
                pRootInstruction.addChildInstruction(InstructionType.loadAnimation, modelURL);
                pProcessor.execute(pRootInstruction);
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // LOAD FROM SERVER
    ////////////////////////////////////////////////////////////////////////////
    public void loadMeshDAEURL(boolean clear, boolean useRepository, Component arg0, String[] data, String[] meshRef, int region) {
        currentPScene.setUseRepository(useRepository);
        if(clear)
            currentPScene.getInstances().removeAllChildren();
        if(currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        if (data[4].equals("0") || data[4].equals("1") || data[4].equals("2")) {
            URL modelURL;
            try {
                modelURL = new URL(data[3]);
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            if (clear) {
                SharedAsset colladaAsset = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
                colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, data[0], null));
                pruneMeshes(data[0], colladaAsset, data);
            } else {
                addDAEMeshURLToModelA(data, "Head", region);
//                if (data[4].equals("0"))
//                    addDAEMeshURLToModelA(data, "Head", region);
//                else
//                    addDAEMeshURLToModelA(data, "skeletonRoot", region);
            }
        } else {
            try {
                if (clear) {
                    URL urlModel = new URL(data[3]);
                    SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, urlModel));
                    character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, data[0], null));
                    String[] anim = null;
                    loadInitializer(data[0], character, anim);
                } else {
                    addDAEMeshURLToModel(data, meshRef, region);
                }
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void loadAvatarDAEURL(boolean clear, boolean useRepository, Component arg0, String[] data, String[] anim, String[] meshRef, int region) {
        currentPScene.setUseRepository(useRepository);
        if (clear)
            currentPScene.getInstances().removeAllChildren();
        
        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        try {
            if (clear) {
                URL urlModel = new URL(data[3]);
                SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, urlModel));
                character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, data[0], null));
                if (bdefaultload)
                    specialloadInitializer(data[0], character, anim);
                else
                    loadInitializer(data[0], character, anim);
            } else {
                addDAEMeshURLToModel(data, meshRef, region);
            }                
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    public void loadAvatarDAEURL(boolean clear, boolean useRepository, String[] data, Map<Integer, String[]> swapdata) {
        currentPScene.setUseRepository(useRepository);
        if (clear)
            currentPScene.getInstances().removeAllChildren();

        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();

        try {
            if (clear) {
                URL urlModel = new URL(data[2]);
                SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, urlModel));
                character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, "Avatar", null));
                loadInitializer("Avatar", character, swapdata, data[3]);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addDAEMeshURLToModel(String[] data, String[] meshRef, int region) {
        InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skeleton);
        
        String[] meshestodelete = meshsetup.get(region);
        for (int i = 0; i < meshestodelete.length; i++)
            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshestodelete[i]);
        
        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, data[3]);
        
        for (int i = 0; i < meshRef.length; i++)
            pRootInstruction.addChildInstruction(InstructionType.addSkinnedMesh, meshRef[i]);
            
        pProcessor.execute(pRootInstruction);

        skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(worldManager));
        skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(worldManager));
        meshsetup.put(region, meshRef);
    }

    public void addDAEMeshURLToModelA(String[] data, String joint2addon, int region) {
        InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skeleton);

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

        if (meshsetup.get(region) != null) {
            // TODO: DELETE STATIC MESHES LIKE HAIR AND STUFF IF IT IS LOADED...
        }

        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, data[3]);
        
        PMatrix tempSolution;
        if (data[3].indexOf("Female") != -1) {
            tempSolution = new PMatrix();
            tempSolution.setRotation(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f));
        } else
            tempSolution = new PMatrix(new Vector3f(0.0f,(float) Math.toRadians(180), 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), Vector3f.ZERO);

        pRootInstruction.addAttachmentInstruction( data[0], szName, tempSolution);
        pProcessor.execute(pRootInstruction);
        
        int hairCheck = data[3].indexOf("Hair");
        if (hairCheck != -1) {
            bhairLoaded = true;
        }
    }

    public void pruneMeshes(String n, SharedAsset s, final String[] data) {
        AssetInitializer init = new AssetInitializer() {

            public boolean initialize(Object asset) {

                MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
                proc.setProcessor();
                TreeTraverser.breadthFirst(currentPScene, proc);
                Vector<PPolygonMeshInstance> meshes = proc.getMeshInstances();
                for (int i = 0; i < meshes.size(); i++) {
                    if (!meshes.get(i).getName().equals(data[0]))
                        currentPScene.getInstances().findAndRemoveChild(meshes.get(i));
                    else {
                        meshes.get(i).getTransform().setLocalMatrix(new PMatrix());
                    }

                }
                setCameraOnModel();
                return true;
            }
        };
        s.setInitializer(init);

        modelInst = currentPScene.addModelInstance(n, s, new PMatrix());

        if (currentPScene.getAssetWaitingList().size() <= 0) {
            MeshInstanceSearchProcessor proc = new MeshInstanceSearchProcessor();
            proc.setProcessor();
            TreeTraverser.breadthFirst(currentPScene, proc);
            Vector<PPolygonMeshInstance> meshes = proc.getMeshInstances();
            for (int i = 0; i < meshes.size(); i++) {
                if (!meshes.get(i).getName().equals(data[0]))
                    currentPScene.getInstances().findAndRemoveChild(meshes.get(i));
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
                    skeleton = skel;
                    
                    // Visual Scale
                    if (visualScale != 1.0f) {
                        ArrayList<PPolygonSkinnedMeshInstance> meshes = skel.getSkinnedMeshInstances();
                        for (PPolygonSkinnedMeshInstance mesh : meshes) {
                            mesh.getTransform().getLocalMatrix(true).setScale(visualScale);
                        }
                    }
                    
                    // Set animations
                    if (a != null) {
                        InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
                        Instruction pRootInstruction = new Instruction();

                        pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skel);
                        for (int i = 0; i < a.length; i++) {
                            pRootInstruction.addChildInstruction(InstructionType.loadAnimation, a[i]);
                        }
                        pProcessor.execute(pRootInstruction);
                    }

                    skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(worldManager));
                    skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(worldManager));
                    ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skel));
                    currentPScene.setDirty(true, true);
                    setCameraOnModel();
                }
                return true;
            }
        };
        s.setInitializer(init);

        modelInst = currentPScene.addModelInstance(n, s, new PMatrix());
        // Set position
        if (origin != null) {
            modelInst.getTransform().setLocalMatrix(origin);                    // Set animations
        }
    }

    public void loadInitializer(String n, SharedAsset s, final Map<Integer, String[]> d, final String a) {
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
                    skeleton = skel;
                    
                    // Visual Scale
                    if (visualScale != 1.0f) {
                        ArrayList<PPolygonSkinnedMeshInstance> meshes = skel.getSkinnedMeshInstances();
                        for (PPolygonSkinnedMeshInstance mesh : meshes) {
                            mesh.getTransform().getLocalMatrix(true).setScale(visualScale);
                        }
                    }

                    InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
                    Instruction pRootInstruction = new Instruction();
                    pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skel);

                    // Set animations clothes heads and hair
                    for (int i = 0; i < 9; i++)
                        deleteNLoad(pRootInstruction, d.get(i), i);
                    
                    pRootInstruction.addChildInstruction(InstructionType.loadAnimation, a);
                    pProcessor.execute(pRootInstruction);

                    skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(worldManager));
                    skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(worldManager));
                    ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skel));
                    currentPScene.setDirty(true, true);
                    setCameraOnModel();
                }
                return true;
            }
        };
        s.setInitializer(init);

        modelInst = currentPScene.addModelInstance(n, s, new PMatrix());
        // Set position
        if (origin != null) {
            modelInst.getTransform().setLocalMatrix(origin);                    // Set animations
        }
    }

    public void specialloadInitializer(String n, SharedAsset s, final String[] a) {
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
                    skeleton = skel;
                    
                    // Visual Scale
                    if (visualScale != 1.0f) {
                        ArrayList<PPolygonSkinnedMeshInstance> meshes = skel.getSkinnedMeshInstances();
                        for (PPolygonSkinnedMeshInstance mesh : meshes) {
                            mesh.getTransform().getLocalMatrix(true).setScale(visualScale);
                        }
                    }
                    
                    String[] meshes = null;
                    InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
                    Instruction pRootInstruction = new Instruction();
                    pRootInstruction.addChildInstruction(InstructionType.setSkeleton, skel);

                    // Set animations and clothes
                    if (a != null) {
                        for (int i = 0; i < meshsetup.get(2).length; i++)
                            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshsetup.get(2)[i]);
                        
                        for (int i = 0; i < meshsetup.get(3).length; i++)
                            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshsetup.get(3)[i]);
                        
                        for (int i = 0; i < meshsetup.get(4).length; i++)
                            pRootInstruction.addChildInstruction(InstructionType.deleteSkinnedMesh, meshsetup.get(4)[i]);
                        
                        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, poloShirt);
                        // This is the old way
//                        pRootInstruction.addChildInstructionOfType(InstructionType.addSkinnedMesh, "TorsoNudeShape");
//                        pRootInstruction.addChildInstructionOfType(InstructionType.addSkinnedMesh, "PoloShape");
                        // Here is the new way
                        pRootInstruction.addSkinnedMeshInstruction("TorsoNudeShape", "UpperBody");
                        pRootInstruction.addSkinnedMeshInstruction("PoloShape", "UpperBody");
                        meshes = new String[] {"TorsoNudeShape", "PoloShape" };
                        meshsetup.put(2, meshes);
                        
                        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, jeansPants);
                        //pRootInstruction.addChildInstructionOfType(InstructionType.addSkinnedMesh, "polySurface3Shape");
                        pRootInstruction.addSkinnedMeshInstruction("polySurface3Shape", "LowerBody");
                        meshes = new String[] {"polySurface3Shape"};
                        meshsetup.put(3, meshes);
                        
                        pRootInstruction.addChildInstruction(InstructionType.loadGeometry, tennisShoes);
                        //pRootInstruction.addChildInstructionOfType(InstructionType.addSkinnedMesh, "TennisShoesShape");
                        pRootInstruction.addSkinnedMeshInstruction("TennisShoesShape", "Feet");
                        meshes = new String[] {"TennisShoesShape"};
                        meshsetup.put(4, meshes);

                        for (int i = 0; i < a.length; i++) {
                            pRootInstruction.addChildInstruction(InstructionType.loadAnimation, a[i]);
                        }
                        pProcessor.execute(pRootInstruction);
                    }

                    skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(worldManager));
                    skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(worldManager));
                    ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skel));
                    currentPScene.setDirty(true, true);
                    setCameraOnModel();
                }
                return true;
            }
        };
        s.setInitializer(init);

        modelInst = currentPScene.addModelInstance(n, s, new PMatrix());
        // Set position
        if (origin != null) {
            modelInst.getTransform().setLocalMatrix(origin);                    // Set animations
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
            ZipFile zipfile = new ZipFile(zipFileLoc);
            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                System.out.println("Extracting: " + entry);
                is = new BufferedInputStream(zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos;
                if (isWindowsOS())
                    fos = new FileOutputStream(desti.toString() + '\\' + entry.getName());
                else
                    fos = new FileOutputStream(desti.toString() + '/' + entry.getName());
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER)) != -1) {
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
        zipFileLoc = destinationFile;
    }

    public void deleteNLoad(final Instruction instruct, final String[] d, int iRegion) {
        if (d != null) {
            if (meshsetup.get(iRegion) != null) {
                for (int i = 0; i < meshsetup.get(iRegion).length; i++)
                    instruct.addChildInstruction(InstructionType.deleteSkinnedMesh, meshsetup.get(iRegion)[i]);
            }

            String[] meshes = new String[d.length -1];

            instruct.addChildInstruction(InstructionType.loadGeometry, d[0]);
            if (iRegion > 4 && iRegion < 9) {
                for (int i = 1; i < d.length; i++) {
                    PMatrix tempSolution;
                    tempSolution = new PMatrix(new Vector3f(0.0f, (float) Math.toRadians(180), 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), Vector3f.ZERO);
                    instruct.addAttachmentInstruction( d[i], "Head", tempSolution);
                    meshes[i-1] = d[i];
                }
            } else {
                for (int i = 1; i < d.length; i++) {
                    instruct.addSkinnedMeshInstruction(d[i], m_regions[iRegion]);
                    meshes[i-1] = d[i];
                }
            }
            meshsetup.put(iRegion, meshes);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // TESTS
    ////////////////////////////////////////////////////////////////////////////
    public void loadUnZippedAvatar(boolean clear, boolean useRepository, Component arg0, File data, String[] meshRef, int region) {
        currentPScene.setUseRepository(useRepository);
        if (clear)
            currentPScene.getInstances().removeAllChildren();

        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();

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
            SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, bindPose));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, name, null));
            if (bdefaultload)
                specialloadInitializer(name, character, anim);
            else
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

    public void setDefaultMeshSwapList() {
        int gender = fileModel.getName().lastIndexOf("Male");
        String szGender = null;
        
        if (gender != -1)
            szGender = "'Male'";
        else
            szGender = "'Female'";

        String query = "SELECT name, grouping FROM GeometryReferences WHERE tableref = ";
        query += gender;
        if (meshsetup != null)
            meshsetup.clear();
        meshsetup = new HashMap<Integer, String[]>();
        ArrayList<String[]> meshes = loadSQLData(query);
        
        createMeshSwapList("0", meshes);
        createMeshSwapList("1", meshes);
        createMeshSwapList("2", meshes);
        createMeshSwapList("3", meshes);
        createMeshSwapList("4", meshes);
    }
    
    public void createMeshSwapList(String region, ArrayList<String[]> meshes) {
        String[] geometry = null;
        ArrayList<String> temp = new ArrayList<String>();
        
        for (int i = 0; i < meshes.size(); i++) {
            if (meshes.get(i)[1].equals(region)) {
                temp.add(meshes.get(i)[0].toString());
            }
        }
        geometry = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            geometry[i] = temp.get(i);
        }
        
        int iregion = 0;
        if (region.equals("0"))
            iregion = 0;          // Head
        else if (region.equals("1"))
            iregion = 1;          // Hands
        else if (region.equals("2"))
            iregion = 2;          // Torso
        else if (region.equals("3"))
            iregion = 3;          // Legs
        else if (region.equals("4"))
            iregion = 4;          // Feet
        
        meshsetup.put(iregion, geometry);
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
        PNode node = currentPScene.getInstances();
        if (node != null && node.getChildrenCount() > 0) {
            PPolygonModelInstance pmInstance = ((PPolygonModelInstance) node.getChild(0));
            if (curCameraProcessor.getState() instanceof TumbleObjectCamState) {
                TumbleObjectCamState camState = ((TumbleObjectCamState)curCameraProcessor.getState());
                TumbleObjectCamModel camModel = ((TumbleObjectCamModel)curCameraProcessor.getModel());
                camState.setTargetModelInstance(pmInstance);
                camState.setCameraPosition(camPos);

                if (pmInstance.getBoundingSphere() == null)
                    pmInstance.calculateBoundingSphere();
                camState.setTargetFocalPoint(pmInstance.getBoundingSphere().getCenter());
                camModel.turnTo(pmInstance.getBoundingSphere().getCenter(), camState);
                camState.setTargetNeedsUpdate(true);
            } else if (curCameraProcessor.getState() instanceof FirstPersonCamState) {
                FirstPersonCamState camState = ((FirstPersonCamState)curCameraProcessor.getState());
                if (pmInstance.getBoundingSphere() == null)
                    pmInstance.calculateBoundingSphere();
                Vector3f pos = pmInstance.getBoundingSphere().getCenter();
                pos.z = -3.2f;
                camState.setCameraPosition(pos);
            }
        }
    }

    public String getOS() {
        return System.getProperty("os.name");
    }
    
    public boolean isWindowsOS() {
        return getOS().contains("Windows");
    }
    
    /**
     * Replaces the texture of the currently selected model
     * TODO: Allow support for textures on multiple meshes.
     * @param modInst (PPolygonModelInstance)
     * @param arg0 (Component)
     */
    public void loadTexture(imi.scene.PNode meshInst, Component arg0) {
        int retValAsset = jFileChooser_LoadAssets.showOpenDialog(arg0);
        if (retValAsset == JFileChooser.APPROVE_OPTION) {
            fileTexture = jFileChooser_LoadAssets.getSelectedFile();

            // Create a material to use
            int iIndex = fileTexture.getName().indexOf(".");
            String szName = fileTexture.getName().substring(0, iIndex);
            File fullpath = getAbsPath(fileTexture);
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
        int retVal = jFileChooser_LoadXML.showOpenDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            try {
                File configfile = jFileChooser_LoadXML.getSelectedFile();
                URL configURL   = configfile.toURI().toURL();
                if (avatar == null) {
                    avatar = new NinjaAvatar(configURL, worldManager);
                } else if (avatar != null) {
                    avatar.loadConfiguration(configURL);
                }                
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void saveAvatarSaveFile(Component arg0) {
        File saveFile = new File("saveme.xml");
        jFileChooser_SaveXML.setSelectedFile(saveFile);

        int retVal = jFileChooser_SaveXML.showSaveDialog(arg0);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            saveFile = jFileChooser_SaveXML.getSelectedFile();
            if (avatar != null) {
                avatar.saveConfiguration(saveFile);
            }
        }
    }
}
