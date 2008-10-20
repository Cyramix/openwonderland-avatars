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

    
    private SkeletonNode loadCharacter(PScene pScene)
    {
        InstructionProcessor pProcessor = new InstructionProcessor();
        
        String fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");
        
        Instruction pRootInstruction = new Instruction("loadCharacter");
        Instruction pLoadBindPoseInstruction = pRootInstruction.addInstruction("loadBindPose", fileProtocol + "assets/models/collada/Avatars/Male2/Male_Bind.dae");
//        //Instruction loadGeometry = pRootInstruction.addInstruction( "loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressPants1.dae");
//        Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
//        pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
//        pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressPants1.dae");
//        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "Legs_LegsNudeShape");
//        pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
//        pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
//        pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/FlipFlopsFeet.dae");
//        //pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurfaceShape3");
//        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
//        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
//        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
//        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
        pRootInstruction.addInstruction("loadAnimation", fileProtocol + "assets/models/collada/Avatars/Male2/Male_Idle.dae");
        pRootInstruction.addInstruction("loadAnimation", fileProtocol + "assets/models/collada/Avatars/Male2/Male_Walk.dae");

        pProcessor.execute(pScene, pRootInstruction);
        
        return(pProcessor.getSkeleton());
    }
    
    

    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors)
    {
        SkeletonNode pTheSkeletonNode = loadCharacter(pscene);

/*
        String modelFilename;
        modelFilename = "assets/models/collada/Female01/Female01.dae";
        modelFilename = "assets/models/collada/Female02/Female02.dae";
        modelFilename = "assets/models/collada/grievous/grievous.dae";
        modelFilename = "assets/models/collada/Seymour/Seymour.dae";
        modelFilename = "assets/models/collada/Tifa/Tifa.dae";
        modelFilename = "assets/models/collada/trooper/trooper.dae";
        modelFilename = "assets/models/collada/Duck/Duck.dae";
//        modelFilename = "assets/models/collada/Alien/alienAnim02.dae";
//        modelFilename = "assets/models/collada/r2d2/r2d2.dae";
//        modelFilename = "assets/models/collada/Accessories/accessories.dae";
        modelFilename = "assets/models/collada/BodyMaleIdleTest.dae";
//        modelFilename = "assets/models/collada/Head/HeadRig1.dae";
//        modelFilename = "assets/models/collada/ArmTest.dae";
        modelFilename = "assets/models/collada/Avatars/Male/MaleBind.dae";
        modelFilename = "assets/models/collada/Avatars/Male/MaleAvatarPolo.dae";

        String geometryFilename = "assets/models/collada/Avatars/Male/MaleAvatarPolo.dae";//Male_Sweater2.dae";

        String animationFilename = "assets/models/collada/Avatars/Male/Male_Walk.dae";

        
        modelFilename = "assets/models/collada/Avatars/Male2/Male_Bind.dae";//Male_Sweater2.dae";

        animationFilename = "assets/models/collada/Avatars/Male2/Male_Walk.dae";
        String animation2Filename = "assets/models/collada/Avatars/Male2/Male_Idle.dae";

//        pscene.setUseRepository(false);

        CharacterLoader characterLoader = new CharacterLoader();

        //  Load the rig.
        SkeletonNode pTheSkeletonNode = characterLoader.loadSkeletonRig(pscene, modelFilename);
        characterLoader.loadGeometry(pscene, pTheSkeletonNode, "assets/models/collada/Avatars/Male/MaleBind.dae");//geometryFilename);
        characterLoader.loadAnimation(pscene, pTheSkeletonNode, animationFilename);
//        characterLoader.loadAnimation(pscene, pTheSkeletonNode, animation2Filename);

//        pscene.setUseRepository(true);
*/


        //  Assign the specified shader to all SkinnedMeshes.
        pTheSkeletonNode.setShader(new VertexDeformer(wm));

        PPolygonModelInstance modelInst = pscene.addModelInstance(pTheSkeletonNode, new PMatrix());

        if (modelInst.getChild(0) instanceof SkeletonNode)
        {
            SkeletonNode pSkeletonNode = (SkeletonNode) modelInst.getChild(0);

            ArrayList<PPolygonSkinnedMeshInstance> skinnedMeshInstances = pSkeletonNode.getSkinnedMeshInstances();
            PPolygonSkinnedMeshInstance pSkinnedMeshInstance;


            for (int i = 0; i < skinnedMeshInstances.size(); i++)
            {
                pSkinnedMeshInstance = skinnedMeshInstances.get(i);
                assignMaterial(pSkinnedMeshInstance, wm);

                //pSkinnedMeshInstance.buildAnimationJointMapping(pSkeletonNode); <-- no longer needed
            }

            //  Assign the specified shader to all SkinnedMeshes.
            pSkeletonNode.setShader(new VertexDeformer(wm));
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
