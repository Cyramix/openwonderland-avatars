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
import imi.loaders.PPolygonTriMeshAssembler;
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
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;

/**
 *
 * @author Lou Hayt
 */
public class InstancingTest extends DemoBase
{
    public InstancingTest(String[] args){
        super(args);
    }
    
    public static void main(String[] args) {
        InstancingTest worldTest = new InstancingTest(args);
    }
    
    @Override
    protected void createDemoEntities(WorldManager wm) 
    {
        // On my iMac I can run 25 entities with 2x2 ninjas each
        // without a slow down (on XP)
        for(int i = 0; i < 5; i++)
            createNinjaArmy(wm, i);      
    }
    
    private void initNinjaArmy(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors, int yLevel) 
    {
        // Create a SharedAsset with the description of the ninja model file
        SharedAsset ninja = new SharedAsset(
                ((Repository)wm.getUserData(Repository.class)),
                new AssetDescriptor(SharedAsset.SharedAssetType.MS3D_SkinnedMesh,
                "assets/models/ms3d/ninja.ms3d"));
        final WorldManager fwm = wm;
        // Set up an initializer to excecute once the asset is loaded into the scene
        ninja.setInitializer(
                new AssetInitializer() 
                {
                public boolean initialize(Object asset) 
                {
                    if (asset != null && asset instanceof SkeletonNode)
                    {
                        SkeletonNode skeleton = (SkeletonNode)asset;
                        skeleton.transitionTo("Kick", false);
                        PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)skeleton.findChild("MS3DSkinnedMesh");
                        target.setName("NinjaInstance");
                        // Create a material to use
                        PMeshMaterial material =  new PMeshMaterial("ninja material", "assets/textures/checkerboard2.PNG");
                        
                        material.setShader(new VertexDeformer(fwm));
                        // make sure we have geometry before setting the material
                        target.getGeometry().submit(new PPolygonTriMeshAssembler());
                        // Set the material
                        target.setMaterial(material);
                        // This will apply the material
                        target.setUseGeometryMaterial(false);
                    }
                    return true;
                }
                });
        
        float yOffSet = yLevel * 1.0f;
        PPolygonModelInstance modelInst;
        for (int i = 0; i < 5; i++)
        {
            for (int j = 0; j < 4; j++)
            {
                // Add an instance to the scene
                modelInst = pscene.addModelInstance(ninja, new PMatrix(new Vector3f(i * 10.0f, yOffSet * 10.0f, j * 10.0f)));
                
                // Add animation processor
                processors.add(new SkinnedAnimationProcessor(modelInst));
            }
        }
    }
    
    private void createNinjaArmy(WorldManager wm, int yLevel) 
    {
        // The procedural scene graph
        PScene pscene = new PScene("PScene test", wm);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // Initialize the scene
        initNinjaArmy(pscene, wm, processors, yLevel);
        
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
