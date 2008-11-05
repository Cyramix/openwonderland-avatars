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
 * $Revision$
 * $Date$
 * $State$
 */

package imi.gui;

import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.Instruction;
import imi.loaders.collada.Instruction.InstructionNames;
import imi.loaders.collada.InstructionProcessor;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.VertexDeformer;
import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
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
        
        
    public SceneEssentials() {
        initFileChooser();
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

        jFileChooser_LoadAssets.setDialogTitle("Load Asset File");
        java.io.File assetDirectory = new java.io.File("./assets/textures");
        jFileChooser_LoadAssets.setCurrentDirectory(assetDirectory);
        jFileChooser_LoadAssets.setDoubleBuffered(true);

        jFileChooser_LoadAssets.setDragEnabled(true);
        jFileChooser_LoadAssets.addChoosableFileFilter((FileFilter)assetFilter);
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
                            // Set the shader (if no vertex deformer then there is no animations)
                            material.setShader(new VertexDeformer(worldManager));                            
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
                            // Set the shader (if no vertex deformer then there is no animations)
                            material.setShader(new VertexDeformer(worldManager));                            
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

    /**
     * Removes old model data from the pscene and replaces it with the user
     * selected collada data
     */
    public void loadDAEFile(boolean clear, Component arg0) {
        currentPScene.setUseRepository(false);
        if(clear)
            currentPScene.getInstances().removeAllChildren();
        if(currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();

        File path = getAbsPath(fileModel);        
        String szURL = new String("file://" + path.getPath());
        URL modelURL = null;
        
        try {
            modelURL = new URL(szURL);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SharedAsset colladaAsset = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
        colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, fileModel.getName(), null));
        modelInst = currentPScene.addModelInstance(fileModel.getName(), colladaAsset, new PMatrix());
    }
    
    public void loadDAESMeshFile(boolean clear, Component arg0) {
        currentPScene.setUseRepository(false);
        if(clear)
            currentPScene.getInstances().removeAllChildren();
        if(currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        File path = getAbsPath(fileModel);        
        String szURL = new String("file://" + path.getPath());
        try {
            if (clear) {
                URL URLfile = new URL(szURL);
                SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, URLfile));
                character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, fileModel.getName(), null));
                String[] anim = null;
                loadInitializer(fileModel.getName(), character, anim);
            } else {
                
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadDAEURL(boolean clear, Component arg0, String[] data, String[] meshRef, int region) {
        currentPScene.setUseRepository(false);
        if(clear)
            currentPScene.getInstances().removeAllChildren();
        if(currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        if (data[4].equals("1")) {
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
                modelInst = currentPScene.addModelInstance(data[0], colladaAsset, new PMatrix());
            } else {
                // Add MS3D_Mesh Onto Model
                
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
    
    public void addDAEMeshURLToModel(String[] data, String[] meshRef, int region) {
        currentPScene.setUseRepository(false);
        InstructionProcessor pProcessor = new InstructionProcessor(worldManager);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addInstruction(InstructionNames.setSkeleton, skeleton);
        
        String[] meshestodelete = meshsetup.get(region);
        for (int i = 0; i < meshestodelete.length; i++)
            pRootInstruction.addInstruction(InstructionNames.deleteSkinnedMesh, meshestodelete[i]);
        
        pRootInstruction.addInstruction(InstructionNames.loadGeometry, data[3]);
        
        for (int i = 0; i < meshRef.length; i++)
            pRootInstruction.addInstruction(InstructionNames.addSkinnedMesh, meshRef[i]);
            
        pProcessor.execute(pRootInstruction);
        
        skeleton.setShader(new VertexDeformer(worldManager));
        ((ProcessorCollectionComponent) currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skeleton));
        currentPScene.setDirty(true, true);
        
        meshsetup.put(region, meshRef);
    }
    
    public void loadDAECharacter(boolean clear, Component arg0) {
        if (clear)
            currentPScene.getInstances().removeAllChildren();
        
        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        loadDAEChar();
    }
    
    public void loadDAECharacterURL(boolean clear, Component arg0, String[] data, String[] anim) {
        if (clear)
            currentPScene.getInstances().removeAllChildren();
        
        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        try {
            URL urlModel = new URL(data[3]);
            SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, urlModel));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, data[0], null));
            loadInitializer(data[0], character, anim);
        } catch (MalformedURLException ex) {
            Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }    
    
    public void loadDAEChar() {
        URL bindPose = findBindPose(fileModel);
        final ArrayList<URL> animations = findAnims(fileModel);
        String[] anim = new String[animations.size()];
        for (int i = 0; i < animations.size(); i++) {
            anim[i] = animations.get(i).toString();
        }
        
        SharedAsset character = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, bindPose));
        character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, fileModel.getName(), null));
        loadInitializer(fileModel.getName(), character, anim);
    }
    
    public File getAbsPath(File file) {
        String fullpath = null;
        int index = file.getParent().indexOf("./");
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
                bind = absPath + "/" + colladaList[i];
                break;
            }
        }
        
        String szURL = new String("file://" + bind);
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
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Anim") != -1) {
                anim = absPath + "/" + colladaList[i];
                String szAnim = new String("file://" + anim);
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
       
    public void loadInitializer(String n, SharedAsset s, final String[] a) {
        AssetInitializer init = new AssetInitializer() {

            public boolean initialize(Object asset) {

                if (((PNode) asset).getChild(0) instanceof SkeletonNode) {
                    final SkeletonNode skel = (SkeletonNode) ((PNode) asset).getChild(0);
                    
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

                        pRootInstruction.addInstruction(InstructionNames.setSkeleton, skel);
                        for (int i = 0; i < a.length; i++) {
                            pRootInstruction.addInstruction(InstructionNames.loadAnimation, a[i]);
                        }
                        pProcessor.execute(pRootInstruction);
                    }
                    
                    skel.setShader(new VertexDeformer(worldManager));
                    ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(skel));
                    currentPScene.setDirty(true, true);
                }
                return true;
            }
        };
        s.setInitializer(init);
        currentPScene.setUseRepository(false);
        modelInst = currentPScene.addModelInstance(n, s, new PMatrix());
        // Set position
        if (origin != null) {
            modelInst.getTransform().setLocalMatrix(origin);                    // Set animations
        }
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
            String szURL = new String("file://" + fullpath.toString());
            
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
}
