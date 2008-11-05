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
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.VertexDeformer;
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
public class GUITest extends DemoBase2
{
    //private static AvatarEditorGUI GUI = new AvatarEditorGUI();
    
    public GUITest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        GUITest worldTest = new GUITest(args);
    }
    
    private void initGUITest(PScene pscene, JScene jscene, WorldManager wm, ArrayList<ProcessorComponent> processors, Entity JSEntity) 
    {
        pscene.setUseRepository(true); // test
        
        final PScene fpscene = pscene;
        
        // Create a SharedAsset with the description of the ninja model file
        SharedAsset ninja = new SharedAsset(
                ((Repository)wm.getUserData(Repository.class)),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D_SkinnedMesh,
                new File("assets/models/ms3d/ninja.ms3d")));
        
        // Set up an initializer to excecute once the asset is loaded into the scene
        ninja.setInitializer(
                new AssetInitializer() 
                {
                public boolean initialize(Object asset) 
                {
                    if (asset != null && asset instanceof SkeletonNode)
                    {
                        SkeletonNode skeleton = (SkeletonNode)asset;
                        skeleton.getAnimationState().setCurrentCycle(0);
                        skeleton.getAnimationState().setPauseAnimation(false);
                        PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)skeleton.findChild("MS3DSkinnedMesh");
                        target.setName("NinjaInstance");
                         
                        // Create a material to use
                        PMeshMaterial material =  new PMeshMaterial("ninja material", "assets/textures/checkerboard2.PNG");
                        
                        material.setShader(new VertexDeformer(fpscene.getWorldManager()));
                        
                        // Set the material
                        target.setMaterial(material);
                        // We must disable the use of the geometry's material to see the texture we set for the instance
                        target.setUseGeometryMaterial(false);
                    }
                    // To refresh the skeleton visualization
                    fpscene.setDirty(true, true);
                    return true;
                }
                });
        
        // Add an instance to the scene
        PPolygonModelInstance modelInst = pscene.addModelInstance(ninja, new PMatrix(new Vector3f(0.0f, 0.0f, 5.0f)));
                
        // Add animation processor
        processors.add(new SkinnedAnimationProcessor(modelInst)); 
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
        
        RenderComponent sc = wm.getRenderManager().createRenderComponent(jscene);
        
        // Add the scene component with our jscene to the entity
        JSEntity.addComponent(RenderComponent.class, sc);
        
        // Initialize
        initGUITest(pscene, jscene, wm, processors, JSEntity);
        
        // Add our two processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));
        
        // Add the processor collection component to the entity
        JSEntity.addComponent(ProcessorCollectionComponent.class, processorCollection);
        
        // Add the entity to the world manager
        wm.addEntity(JSEntity);
        
        setGUI(jscene, wm, processors, JSEntity);
        setVisible(true);
    }
}
