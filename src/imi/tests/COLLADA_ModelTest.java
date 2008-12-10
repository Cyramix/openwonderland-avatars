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
package imi.tests;


import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import imi.gui.TreeExplorer;
import imi.scene.shader.NoSuchPropertyException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;

import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.Instruction;
import imi.loaders.collada.InstructionProcessor;
import imi.loaders.repository.AssetInitializer;
import imi.scene.PNode;
import imi.scene.animation.AnimationState;
import imi.scene.camera.behaviors.TumbleObjectCamModel;
import imi.scene.camera.state.TumbleObjectCamState;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.programs.SimpleTNLWithAmbient;
import imi.scene.utils.PMeshUtils;
import java.io.File;
import java.net.URL;
import imi.scene.shader.dynamic.*;
import imi.scene.shader.effects.*;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;


/**
 *
 * @author Chris Nagle
 */
public class COLLADA_ModelTest extends DemoBase
{
   
    
    public COLLADA_ModelTest(String[] args)
    {
        super(args);
    }
    
    public static void main(String[] args)
    {
        COLLADA_ModelTest modelTest = new COLLADA_ModelTest(args);
    }
    TreeExplorer te = null;
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        // load a model from one of the listed locations below
        URL modelLocation = null;
        try
        {
            //modelLocation = new URL("http://www.zeitgeistgames.com/assets/collada/Clothing/FlipFlopsFeet.dae");
            //modelLocation = new File("assets/models/collada/Environments/Milan/DSI.dae").toURI().toURL();
            modelLocation = new File("assets/models/collada/Avatars/Male/Male_Bind.dae").toURI().toURL();
            //modelLocation = new File("assets/models/pack/headOne.dae").toURI().toURL();
            //modelLocation = new File("assets/models/collada/Heads/MaleAfricanHead/AfricanAmericanMaleHead1_Bind.dae").toURI().toURL();
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        final WorldManager fwm = wm;
        final PScene fpscene = pscene;

        SharedAsset colladaAsset = new SharedAsset(pscene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Mesh, modelLocation), new AssetInitializer() {

            public boolean initialize(Object asset) {
                SkeletonNode skeleton = (SkeletonNode)((PNode)asset).getChild(0);
                Instruction configurationInstruction = new Instruction(Instruction.InstructionNames.instructions, new String("Configurating My Avatar!"));
                configurationInstruction.addInstruction(Instruction.InstructionNames.setSkeleton, skeleton);
                try {
                    configurationInstruction.addInstruction(Instruction.InstructionNames.loadAnimation, new URL("file://localhost/work/avatars/assets/models/collada/Avatars/Male/Male_Walk.dae"));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                InstructionProcessor executor = new InstructionProcessor(fwm);
                executor.execute(configurationInstruction);
                AnimationState newState = skeleton.getAnimationState(0);

                skeleton.addAnimationState(newState);
                newState.setCurrentCycle(0); // skeleton.getAnimationGroup().getCycleCount() - 1
                skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(fwm));
                Instruction maleBind = new Instruction(Instruction.InstructionNames.instructions, new String("Loading the male bind pose"));
                maleBind.addInstruction(Instruction.InstructionNames.setSkeleton, null);
                try {
                    maleBind.addInstruction(Instruction.InstructionNames.loadHumanoidAvatarBindPose, new URL("file://localhost/work/avatars/assets/models/collada/Avatars/Male/Male_Bind.dae"));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(COLLADA_ModelTest.class.getName()).log(Level.SEVERE, null, ex);
                }
                executor.execute(maleBind, false);
                SkeletonNode originalBind = executor.getSkeleton();
                skeleton.remapSkeleton(originalBind);

//                te = new TreeExplorer();
//                SceneEssentials se = new SceneEssentials();
//                se.setPScene(fpscene);
//                se.setWM(fwm);
//                te.setExplorer(se);
//                te.setVisible(true);
//                SkeletonNode skeleton = (SkeletonNode)((PNode)asset).getChild(0);
//                skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(fwm));
                return true;
            }
        });
        //colladaAsset.setUserData(new ColladaLoaderParams(true, true, false, false, 3, "FlipFlops", null));
        colladaAsset.setUserData(new ColladaLoaderParams(true, true, false, false, 3, "Milan", null));
        PPolygonModelInstance modelInst = pscene.addModelInstance("Collada Model", colladaAsset, new PMatrix());
        processors.add(new SkinnedAnimationProcessor(modelInst));
        // create and add a standard cube to see the default lighting
        PPolygonMesh cube = PMeshUtils.createBox("Cube!", Vector3f.ZERO, 2, 2, 2, ColorRGBA.cyan);
        // add it to the pscene
        PPolygonModelInstance modelInst2 = pscene.addModelInstance(cube, new PMatrix(Vector3f.UNIT_Z.mult(5.0f)));
        // grab the mesh instance
        PPolygonMeshInstance meshInst = (PPolygonMeshInstance) modelInst2.getChild(0);
        // assign a texture to the mesh instance
        PMeshMaterial material = new PMeshMaterial("cubeTex", "assets/textures/checkerboard.png");
        GLSLShaderProgram shader = new SimpleTNLWithAmbient(wm, 0.35f);
        material.setShader(shader);
        meshInst.setMaterial(material);
        meshInst.setUseGeometryMaterial(false);



        // NEW CAMERA MODEL TEST CODE
        TumbleObjectCamModel tumbleModel = new TumbleObjectCamModel();
        TumbleObjectCamState tumbleState = new TumbleObjectCamState(modelInst);
        tumbleState.setCameraPosition(new Vector3f(1,2,-6));
        tumbleState.setMinimumDistanceSquared(0.05f);
        m_cameraProcessor.setCameraBehavior(tumbleModel, tumbleState);
        
        // test setting a different focal point, say at the sphere
        tumbleState.setTargetFocalPoint(modelInst.getTransform().getWorldMatrix(false).getTranslation().add(0.0f, 1.7f, 0.0f));
        
        pscene.setDirty(true, true);


    }
}

