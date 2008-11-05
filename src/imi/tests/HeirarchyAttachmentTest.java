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
import com.jme.renderer.ColorRGBA;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.VertexDeformer;
import imi.scene.utils.PMeshUtils;
import java.io.File;
import java.util.ArrayList;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This test demonstrates functionality for dynamic heirarchy attachment
 * @author Ronald E Dahlgren
 */
public class HeirarchyAttachmentTest extends DemoBase
{

    public HeirarchyAttachmentTest(String[] args)
    {
        super(args);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String[] args) 
    {
        HeirarchyAttachmentTest worldTest = new HeirarchyAttachmentTest(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors)
    {
        final PScene pscenef = pscene;
        // load a ninja
        SharedAsset ninjaAsset = new SharedAsset(pscene.getRepository(),
                                    new AssetDescriptor(SharedAssetType.MS3D_SkinnedMesh, new File("assets/models/ms3d/ninja.ms3d")),
                                    new AssetInitializer() {
                                        public boolean initialize(Object asset)
                                        {
                                            if (asset instanceof SkeletonNode)
                                                initializeModel((SkeletonNode)asset, pscenef);
                                            else
                                                return false;
                                            
                                            return true;
                                        }
                                    });
       PPolygonModelInstance modelInst = pscene.addModelInstance("NinjaModelInstance", ninjaAsset, new PMatrix(new Vector3f(10,10,10)));
       processors.add(new SkinnedAnimationProcessor(modelInst));
    }
    
    private void initializeModel(SkeletonNode skeleton, PScene pscene)
    {
        // grab the ninja
        PPolygonSkinnedMeshInstance target = (PPolygonSkinnedMeshInstance)(skeleton).findChild("MS3DSkinnedMesh");
        target.setName("NinjaMeshInstance");
        // set vert deformer
        // Create a material to use
        PMeshMaterial material =  new PMeshMaterial("ninja material", "assets/textures/checkerboard2.PNG");
        material.setShader(new VertexDeformer(pscene.getWorldManager()));
        // Set the material
        target.setMaterial(material);
        // We must disable the use of the geometry's material to see the texture we set for the instance
        target.setUseGeometryMaterial(false);
        // generate animation to joint mapping
        //target.buildAnimationJointMapping(skeleton);
        // Select animation to play
        //target.getAnimationState().setCurrentCycle(target.getAnimationGroup().findAnimationCycle("Walk"));
        // attach a cube to the head
        SkinnedMeshJoint joint = skeleton.findSkinnedMeshJoint("Joint8");
        if (joint != null)
        {
            joint.addChild(createNormalMappedCube(Vector3f.ZERO, pscene));
            // adding a child makes you dirty
            joint.setDirty(true, true);
        }
        
        // add a pant leg
        PPolygonSkinnedMesh skinMesh = createSkinnedModel(3, 1.6f, 1.1f, new Vector3f(-0.55f,0,0.1f), "rightLeg");
        PMeshMaterial mat = new PMeshMaterial("MyName!", "assets/textures/rings.bmp");
        mat.setShader(new VertexDeformer(pscene.getWorldManager()));
        skinMesh.setMaterial(mat);
        int [] influences = new int[4];
        influences[0] = skeleton.getSkinnedMeshJointIndex("Joint26");
        influences[1] = skeleton.getSkinnedMeshJointIndex("Joint25");
        influences[2] = skeleton.getSkinnedMeshJointIndex("Joint24");
        influences[3] = skeleton.getSkinnedMeshJointIndex("Joint23");
        skinMesh.setInfluenceIndices(influences);
        PPolygonSkinnedMeshInstance skinnedMeshInst = (PPolygonSkinnedMeshInstance)pscene.processSkinnedMesh(skinMesh);
        skeleton.addChild(skinnedMeshInst);
        // add another pant leg
        skinMesh = createSkinnedModel(3, 1.6f, 1.1f, new Vector3f(0.55f,0,0.1f), "leftLeg");
        skinMesh.setMaterial(mat);
        influences = new int[4];
        influences[0] = skeleton.getSkinnedMeshJointIndex("Joint21");
        influences[1] = skeleton.getSkinnedMeshJointIndex("Joint20");
        influences[2] = skeleton.getSkinnedMeshJointIndex("Joint19");
        influences[3] = skeleton.getSkinnedMeshJointIndex("Joint18");
        skinMesh.setInfluenceIndices(influences);
        skinnedMeshInst = (PPolygonSkinnedMeshInstance)pscene.processSkinnedMesh(skinMesh);
        skeleton.addChild(skinnedMeshInst);
    }
    
    private PPolygonModelInstance createNormalMappedCube(Vector3f center, PScene pscene)
    {
        PPolygonModelInstance modelInst = pscene.addModelInstance(PMeshUtils.createBox("Box", center, 1.6f, 1.6f, 1.6f, ColorRGBA.blue), new PMatrix());
        PPolygonMeshInstance meshInst = (PPolygonMeshInstance)modelInst.getChild(0);
        // make sure the geometry has been built
        meshInst.getGeometry().submit(new PPolygonTriMeshAssembler());
        
        PMeshMaterial testmaterial = new PMeshMaterial("TestMaterial");
        testmaterial.setTexture("assets/textures/checkerboard.png", 0);
        testmaterial.setTexture("assets/textures/normalmap2.jpg", 1);
        testmaterial.setTexture("assets/textures/spec_beast.jpg", 2);
        testmaterial.setShader(new NormalAndSpecularMapShader(pscene.getWorldManager()));
        
        meshInst.getGeometry().setNumberOfTextures(3);
        meshInst.setMaterial(testmaterial);
        meshInst.setUseGeometryMaterial(false);
        
        return modelInst;
    }
    
}
