/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imi.gui;

import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.Instruction;
import imi.loaders.collada.InstructionProcessor;
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
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.shader.programs.VertexDeformer;
import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

        int iIndex = fileModel.getPath().indexOf("assets");
        int iIndex2 = fileModel.getParent().indexOf("./");
        String szModelPath = fileModel.getPath().substring(iIndex);
        String szSourcePath = null;
        if (iIndex2 > 0)
            szSourcePath = fileModel.getPath().substring(0, iIndex2);
        else
            szSourcePath = fileModel.getPath().substring(0, iIndex);
        String fullPath = szSourcePath + szModelPath;
        String szURL = new String("file://" + fullPath);
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
    
    public void loadDAEURL(boolean clear, Component arg0, String[] data) {
        currentPScene.setUseRepository(false);
        if(clear)
            currentPScene.getInstances().removeAllChildren();
        if(currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        if (data[2].equals("1")) {
            URL modelURL;
            try {
                modelURL = new URL(data[5]);
            } catch (MalformedURLException ex) {
                Logger.getLogger(SceneEssentials.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            SharedAsset colladaAsset = new SharedAsset(currentPScene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelURL));
            colladaAsset.setUserData(new ColladaLoaderParams(false, true, false, false, 3, data[0], null));
            modelInst = currentPScene.addModelInstance(data[0], colladaAsset, new PMatrix());
        } else {
            String[] anim = null;
            setShadersNProcesses(loadDAEURL(data, anim));
        }
    }
    
    public void loadDAECharacter(boolean clear, Component arg0) {
        if (clear)
            currentPScene.getInstances().removeAllChildren();
        
        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        setShadersNProcesses(loadDAEs());        
    }
    
    public void loadDAECharacterURL(boolean clear, Component arg0, String[] data, String[] anim) {
        if (clear)
            currentPScene.getInstances().removeAllChildren();
        
        if (currentHiProcessors == null)
            currentHiProcessors = new ArrayList<ProcessorComponent>();
        else
            currentHiProcessors.clear();
        
        setShadersNProcesses(loadDAEURL(data, anim));
    }    
    
    public SkeletonNode loadDAEs() {
        InstructionProcessor pProcessor = new InstructionProcessor();
        
        //String fileProtocol = new String("file://" + System.getProperty("user.dir") + "/");
        
        int iIndex = fileModel.getPath().indexOf("assets");
        int iIndex2 = fileModel.getParent().indexOf("./");
        String szModelPath = fileModel.getPath().substring(iIndex);
        String szSourcePath = null;
        if (iIndex2 > 0)
            szSourcePath = fileModel.getPath().substring(0, iIndex2);
        else
            szSourcePath = fileModel.getPath().substring(0, iIndex);
        
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File arg0, String arg1) {
                return (arg1.endsWith(".dae"));
            }
        };
        
        //File absPath = new File(szModelPath);
        String fullPath = szSourcePath + szModelPath;
        File absPath = new File(fullPath);
        String[] colladaList = absPath.list(filter);
        String anim = null; String bind = null;
        
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Bind") != -1)
                bind = absPath + "/" + colladaList[i];
        }
        
        String szURL = new String("file://" + bind);
        
        Instruction pRootInstruction = new Instruction("loadCharacter");
        Instruction pLoadBindPoseInstruction = pRootInstruction.addInstruction("loadBindPose", szURL);
        
        for (int i = 0; i < colladaList.length; i++) {
            if (colladaList[i].lastIndexOf("Anim") != -1) {
                anim = absPath + "/" + colladaList[i];
                String szAnim = new String("file://" + anim);
                pRootInstruction.addInstruction("loadAnimation", szAnim);
            }
        }
    
        pProcessor.execute(currentPScene, pRootInstruction);
        return (pProcessor.getSkeleton());
    }
    
    public SkeletonNode loadDAEURL(String[] data, String[] anim) {
        InstructionProcessor pProcessor = new InstructionProcessor();
        Instruction pRootInstruction = new Instruction("loadCharacter");
        Instruction pLoadBindPoseInstruction = pRootInstruction.addInstruction("loadBindPose", data[5]);
        
        if (anim != null) {
            for (int i = 0; i < anim.length; i++) {
                pRootInstruction.addInstruction("loadAnimation",anim[i]);
            }
        }
    
        pProcessor.execute(currentPScene, pRootInstruction);
        return (pProcessor.getSkeleton());
    }
    
    public void setShadersNProcesses(SkeletonNode sNode) {
        PPolygonModelInstance modInst = currentPScene.addModelInstance(sNode, new PMatrix());

        if (modInst.getChild(0) instanceof SkeletonNode)
        {
            SkeletonNode pSkeletonNode = (SkeletonNode) modInst.getChild(0);
            //  Assign the specified shader to all SkinnedMeshes.
            pSkeletonNode.setShader(new VertexDeformer(worldManager));
        }

        modInst.getTransform().getLocalMatrix(true).setScale(10.0f);
        ((ProcessorCollectionComponent)currentEntity.getComponent(ProcessorCollectionComponent.class)).addProcessor(new SkinnedAnimationProcessor(modInst));
        currentPScene.setDirty(true, true);
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
