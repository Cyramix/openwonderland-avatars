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

import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.JScene;
import imi.scene.PMatrix;
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
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.NewFrameCondition;
import org.jdesktop.mtgame.ProcessorArmingCollection;
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
    
    // Helper Functions
    public void setSceneData(JScene jscene, PScene pscene, Entity entity, WorldManager wm, ArrayList<ProcessorComponent> processors) {
        currentJScene = jscene;
        currentPScene = pscene;
        currentEntity = entity;
        worldManager = wm;
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
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D, fileModel));  // Type set as MS3D to load both skinned and regular
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
                    currentEntity.removeComponent(ProcessorCollectionComponent.class);
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

                            ProcessorCollectionComponent proc = (ProcessorCollectionComponent) currentEntity.getComponent(ProcessorCollectionComponent.class);
                            SkinnedAnimationProcessor newProcessor = new SkinnedAnimationProcessor(modelInst);
                            currentHiProcessors.add(newProcessor);
                            proc.addProcessor(newProcessor);
                            
                            for (int i = 0; i < currentHiProcessors.size(); i++) {
                                //currentHiProcessors.get(i).setEntityProcessController(worldManager.getProcessorManager());
                                proc.addProcessor(currentHiProcessors.get(i));
                                ProcessorArmingCollection collection = new ProcessorArmingCollection(currentHiProcessors.get(i));
                                collection.addCondition(new NewFrameCondition(currentHiProcessors.get(i)));
                                currentHiProcessors.get(i).setArmingCondition(collection);
                            }
                            currentEntity.addComponent(ProcessorCollectionComponent.class, proc);
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

                            ProcessorCollectionComponent proc = new ProcessorCollectionComponent();
                            
                            currentHiProcessors.add(new SkinnedAnimationProcessor(modelInst));
                            
                            for (int i = 0; i < currentHiProcessors.size(); i++) {
                                //currentHiProcessors.get(i).setEntityProcessController(worldManager.getProcessorManager());
                                proc.addProcessor(currentHiProcessors.get(i));
                                ProcessorArmingCollection collection = new ProcessorArmingCollection(currentHiProcessors.get(i));
                                collection.addCondition(new NewFrameCondition(currentHiProcessors.get(i)));
                                currentHiProcessors.get(i).setArmingCondition(collection);
                            }
                            currentEntity.addComponent(ProcessorCollectionComponent.class, proc);
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
        if(clear)
            currentPScene.getInstances().removeAllChildren();
        if(currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();

        int iIndex = fileModel.getPath().indexOf("assets");
        String szModelPath = fileModel.getPath().substring(iIndex, fileModel.getPath().length());
        
        currentPScene.setUseRepository(false);
                
        SharedAsset colladaAsset = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA, szModelPath));
        modelInst = currentPScene.addModelInstance(modelName, colladaAsset, new PMatrix());
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
            int index = fileTexture.getPath().indexOf("assets");
            String szTemp = fileTexture.getPath().substring(index, fileTexture.getPath().length());

            PMeshMaterial material = new PMeshMaterial(szName + "material", szTemp);
            if(meshInst instanceof PPolygonSkinnedMeshInstance)
                ((PPolygonSkinnedMeshInstance)meshInst).setMaterial(material);
            else if(meshInst instanceof PPolygonMeshInstance)
                ((PPolygonMeshInstance)meshInst).setMaterial(material);
        }
    }
}
