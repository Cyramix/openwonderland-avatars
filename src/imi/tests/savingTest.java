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
package imi.tests;

import com.jme.math.Vector3f;
import imi.gui.AvatarEditorGUI;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.scene.JScene;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.VertexDeformer;
import imi.utils.FileUtils;
import java.io.File;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;

/**
 *
 * @author IMI
 */
public class savingTest extends DemoBase
{
    private static AvatarEditorGUI GUI = new AvatarEditorGUI();
    
    public savingTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        savingTest worldTest = new savingTest(args);
    }
    
    private void initsavingTest(PScene pscene, JScene jscene, WorldManager wm, ArrayList<ProcessorComponent> processors, Entity JSEntity) 
    {
        //pscene.setUseRepository(false); // test
        final WorldManager fwm = wm;
        // Create a SharedAsset with the description of the ninja model file
        SharedAsset ninja = new SharedAsset(
                ((Repository)wm.getUserData(Repository.class)),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D_SkinnedMesh,
                "assets/models/ms3d/ninja.ms3d"));
        // ant
        SharedAsset ant = new SharedAsset(
                ((Repository)wm.getUserData(Repository.class)),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D_SkinnedMesh,
                "assets/models/ms3d/ant01.ms3d"));
        
        ant.setInitializer(
                new AssetInitializer() 
                {
                public boolean initialize(Object asset) 
                {
                    if (asset != null && asset instanceof PPolygonSkinnedMeshInstance)
                    {
                        PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)asset;
                       
                        // Create a material to use
                        PMeshMaterial material =  new PMeshMaterial("ant material", "assets/textures/ant1.jpg");
                        material.setShader(new VertexDeformer(fwm));
                        // make sure we have geometry before setting the material
                        target.getGeometry().submit(new PPolygonTriMeshAssembler());
                        // Set the material
                        target.getGeometry().setMaterial(material);
                        // We must disable the use of the geometry's material to see the texture we set for the instance
                        target.setUseGeometryMaterial(true);
                    }
                    return true;
                }
                });
        
        final PPolygonModelInstance antInst = pscene.addModelInstance(ant, PMatrix.IDENTITY);
                
        final ArrayList<ProcessorComponent> processorsFinal = processors; // for the loading test
        
        // Set up an initializer to excecute once the asset is loaded into the scene
        final PScene scene = pscene; // for the loading test
        ninja.setInitializer(
                new AssetInitializer() 
                {
                public boolean initialize(Object asset) 
                {
                    if (asset != null && asset instanceof PPolygonSkinnedMeshInstance)
                    {
                        PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)asset;
                       
                        // Create a material to use
                        PMeshMaterial material =  new PMeshMaterial("ninja material", "assets/textures/nskinwh.jpg");
                        
                        material.setShader(new VertexDeformer(fwm));
                        // make sure we have geometry before setting the material
                        target.getGeometry().submit(new PPolygonTriMeshAssembler());
                        // Set the material
                        target.setMaterial(material);
                        // We must disable the use of the geometry's material to see the texture we set for the instance
                        target.setUseGeometryMaterial(false);
                        // Select animation to play
                        //target.transitionTo("Walk");
                        
                        // Prepare for the saving test
                        ((PJoint)target.findChild("Joint15")).getLocalModifierMatrix().setRotation(Vector3f.UNIT_X);
                        //PJoint joint = new PJoint("my joint", new PTransform());
                        //joint.setLocalModifierMatrix(new PMatrix(Vector3f.UNIT_X, Vector3f.UNIT_XYZ, Vector3f.UNIT_X));
                        //target.getParent().addChild(joint);
                        target.findChild("Joint15").addChild(antInst);

                        // Switch to Character API when possible
                        // Test saving 
                        //((PPolygonModelInstance)target.getParent()).saveModel(new File(FileUtils.rootPath, "assets/configurations/ninjaDude.xml"));
                        
                        // Test loading
                        PPolygonModelInstance loadedNinja = new PPolygonModelInstance("Loading test ninja", new PMatrix());
                        //loadedNinja.loadModel(new File(FileUtils.rootPath, "assets/configurations/ninjaDude.xml"), scene);
                        loadedNinja.getTransform().setLocalMatrix(new PMatrix(Vector3f.UNIT_X.mult(10.0f)));
                        processorsFinal.add(new SkinnedAnimationProcessor((loadedNinja)));
                        scene.addInstanceNode(loadedNinja);
                    }
                    return true;
                }
                });
        
        // Add an instance to the scene
        PPolygonModelInstance modelInst = pscene.addModelInstance(ninja, new PMatrix(new Vector3f(0.0f, 0.0f, 5.0f)));
                
        // Add animation processor
        processors.add(new SkinnedAnimationProcessor(modelInst));
        processors.add(new SkinnedAnimationProcessor(antInst));
        
        // Pop up the GUI
//        imi.gui.AvatarOptionsUI.getInstance().setGUI(jscene, wm, processors, JSEntity);
//        imi.gui.AvatarOptionsUI.getInstance().setVisible(true);
        GUI.setGUI(jscene, wm, processors, JSEntity);
        GUI.setVisible(true);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {
        // The procedural scene graph
        PScene pscene = new PScene("PScene test", wm);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // The glue between JME and pscene
        JScene jscene = new JScene(pscene);
        
        // Use default render states
        setDefaultRenderStates(jscene, wm);
        
        // Set this jscene to be the "selected" one for IMI input handling
        ((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setJScene(jscene); 
       
        // Create entity
        Entity JSEntity = new Entity("Entity for a graph test");
        
        // Create a scene component and set the root to our jscene
        RenderComponent sc = wm.getRenderManager().createRenderComponent(jscene);
        
        // Add the scene component with our jscene to the entity
        JSEntity.addComponent(RenderComponent.class, sc);
        
        // Initialize
        initsavingTest(pscene, jscene, wm, processors, JSEntity);
        
        // Add our two processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));
        
        // Add the processor collection component to the entity
        JSEntity.addComponent(ProcessorCollectionComponent.class, processorCollection);
        
        // Add the entity to the world manager
        wm.addEntity(JSEntity);  
    }
}
