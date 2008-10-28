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

import java.util.ArrayList;

import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.PNode;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.Entity;

import imi.loaders.collada.Collada;
import imi.loaders.collada.CharacterLoader;
import imi.scene.JScene;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.shader.programs.VertexDeformer;
import java.io.File;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.RenderComponent;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.utils.FileUtils;

import imi.loaders.collada.Instruction;
import imi.loaders.collada.InstructionProcessor;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;



/**
 *
 * @author Chris Nagle
 */
public class COLLADA_CharacterTest extends DemoBase2
{
    //  Constructor.
    public COLLADA_CharacterTest(String[] args)
    {
        super(args);
    }

    public static void main(String[] args)
    {
        COLLADA_CharacterTest worldTest = new COLLADA_CharacterTest(args);
    }


    private void initGUITest(PScene pscene, JScene jscene, WorldManager wm, ArrayList<ProcessorComponent> processors, Entity JSEntity)
    {
        simpleSceneInit(pscene, wm, processors);
        setGUI(jscene, wm, processors, JSEntity);
        setVisible(true);
    }

    
    private SkeletonNode loadCharacter(PScene pscene)
    {
        InstructionProcessor pProcessor = new InstructionProcessor();
        
        String fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");
        
        Instruction pRootInstruction = new Instruction("loadCharacter");
        Instruction pLoadBindPoseInstruction = pRootInstruction.addInstruction("loadBindPose", fileProtocol + "assets/models/collada/Avatars/MaleZip/MaleBind.dae");
        //Instruction loadGeometry = pRootInstruction.addInstruction( "loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressPants1.dae");
        Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
        pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
        pReplaceGeometryInstruction.addInstruction("loadGeometry", fileProtocol + "assets/models/collada/Clothing/MaleDressPants1.dae");
        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "Legs_LegsNudeShape");
        pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
        pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
        pReplaceGeometryInstruction.addInstruction("loadGeometry", fileProtocol + "assets/models/collada/Clothing/FlipFlopsFeet.dae");
        //pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurfaceShape3");
        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
        pRootInstruction.addInstruction("loadAnimation", fileProtocol + "assets/models/collada/Avatars/Male2/Male_Anim_Idle.dae");
        pRootInstruction.addInstruction("loadAnimation", fileProtocol + "assets/models/collada/Avatars/Male2/Male_Anim_Walk.dae");

        pProcessor.execute(pscene, pRootInstruction);
        
        return(pProcessor.getSkeleton());
    }
    
    

    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors)
    {
        SkeletonNode pTheSkeletonNode = loadCharacter(pscene);


        PPolygonModelInstance modelInst = pscene.addModelInstance(pTheSkeletonNode, new PMatrix());

        if (modelInst.getChild(0) instanceof SkeletonNode)
        {
            SkeletonNode pSkeletonNode = (SkeletonNode) modelInst.getChild(0);
            //  Assign the specified shader to all SkinnedMeshes.
            pSkeletonNode.setShader(new VertDeformerWithSpecAndNormalMap(wm));
        }

        modelInst.getTransform().getLocalMatrix(true).setScale(10.0f);
        processors.add(new SkinnedAnimationProcessor(modelInst)); 
        pscene.setDirty(true, true);
        
        

//        createSkinnedAnimationProcessors(pscene, processors);
    }


    private void assignMaterial(PPolygonSkinnedMeshInstance pSkinnedMeshInstance, WorldManager wm) {
        /**
         * Incredible hacks follow.
         */
        String meshName = pSkinnedMeshInstance.getName();
        if (meshName.equals("HeadGeoShape")) {
            pSkinnedMeshInstance.getGeometry().setNumberOfTextures(3);
            PMeshMaterial newMaterial = new PMeshMaterial();
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCHeadCLR.png"), 0);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleHeadNRM.png"), 1);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCHeadSPC.png"), 2);
            newMaterial.setShader(new VertDeformerWithSpecAndNormalMap(wm));

            pSkinnedMeshInstance.setMaterial(newMaterial);
            pSkinnedMeshInstance.setUseGeometryMaterial(false);
        } else if (meshName.equals("LHandShape") || meshName.equals("RHandShape")) {
            pSkinnedMeshInstance.getGeometry().setNumberOfTextures(3);
            PMeshMaterial newMaterial = new PMeshMaterial();
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCHandsCLR.png"), 0);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleHandNRM.png"), 1);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCHandsSPC.png"), 2);
            newMaterial.setShader(new VertDeformerWithSpecAndNormalMap(wm));

            pSkinnedMeshInstance.setMaterial(newMaterial);
            pSkinnedMeshInstance.setUseGeometryMaterial(false);
        } else if (meshName.equals("LFootNudeShape") || meshName.equals("RFootNudeShape")) {
            pSkinnedMeshInstance.getGeometry().setNumberOfTextures(3);
            PMeshMaterial newMaterial = new PMeshMaterial();
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCFeetCLR.png"), 0);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleFeetNRM.png"), 1);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCFeetSPC.png"), 2);
            newMaterial.setShader(new VertDeformerWithSpecAndNormalMap(wm));

            pSkinnedMeshInstance.setMaterial(newMaterial);
            pSkinnedMeshInstance.setUseGeometryMaterial(false);
        } else if (meshName.equals("TorsoNudeShape") || meshName.equals("LegsNudeShape")) {
            pSkinnedMeshInstance.getGeometry().setNumberOfTextures(3);
            PMeshMaterial newMaterial = new PMeshMaterial();
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCBodyCLRUndies.png"), 0);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleBodyNRM.png"), 1);
            newMaterial.setTexture(new File("assets/models/collada/Avatars/Male/MaleCBodySPC.png"), 2);
            newMaterial.setShader(new VertDeformerWithSpecAndNormalMap(wm));

            pSkinnedMeshInstance.setMaterial(newMaterial);
            pSkinnedMeshInstance.setUseGeometryMaterial(false);
        }
    }


    @Override
    protected void createDemoEntities(WorldManager wm) {
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
        for (int i = 0; i < processors.size(); i++) {
            processorCollection.addProcessor(processors.get(i));
        // Add the processor collection component to the entity
        }
        JSEntity.addComponent(ProcessorCollectionComponent.class, processorCollection);

        // Add the entity to the world manager
        wm.addEntity(JSEntity);
    }
}
