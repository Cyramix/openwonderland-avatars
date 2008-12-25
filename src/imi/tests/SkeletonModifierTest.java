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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.processors.TestHierarchyAnimationProcessor;
import imi.scene.shader.programs.VertexDeformer;
import imi.utils.input.DahlgrensInput;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * Unit test for skeleton remapping.
 * @author Ronald E Dahlgren
 */
public class SkeletonModifierTest extends DemoBase
{
    public SkeletonModifierTest(String[] args)
    {
        super(args);
    }


    public static void main(String[] args) {
        SkeletonModifierTest worldTest = new SkeletonModifierTest(args);
    }

    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors)
    {
        // create a generic skinned mesh material
        PMeshMaterial skinnedMeshMaterial = null;
        try {
            skinnedMeshMaterial = new PMeshMaterial("SkinMeshMat", new File("/work/avatars/assets/textures/checkerboard.png").toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(SkeletonModifierTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        skinnedMeshMaterial.setShader(new VertexDeformer(wm, 0.35f));
        // create a skeleton and
        // add it to the pscene
        PPolygonModelInstance modelInst = pscene.addModelInstance(generateSkeleton(), new PMatrix());
        SkeletonNode skeleton = (SkeletonNode)modelInst.getChild(0);

        // create the lower skinned mesh
        PPolygonSkinnedMesh skinMesh = createSkinnedModel(3, 4, 3, new Vector3f(0,0,0), "lowerHalf");
        int [] influences = new int[4];
        influences[0] = skeleton.getSkinnedMeshJointIndex("base");
        influences[1] = skeleton.getSkinnedMeshJointIndex("level1");
        influences[2] = skeleton.getSkinnedMeshJointIndex("level2");
        influences[3] = skeleton.getSkinnedMeshJointIndex("level3");
        skinMesh.setInfluenceIndices(influences);
        PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance) pscene.addMeshInstance(skinMesh, new PMatrix());
        skeleton.addToSubGroup(meshInst, "Tentacle");
        meshInst.setMaterial(skinnedMeshMaterial);
        meshInst.applyMaterial();
        // create the upper half skinned mesh
        skinMesh = createSkinnedModel(5, 4, 3, new Vector3f(0,12.0f,0), "upperHalf");
        influences = new int[6];
        influences[0] = skeleton.getSkinnedMeshJointIndex("level3");
        influences[1] = skeleton.getSkinnedMeshJointIndex("level4");
        influences[2] = skeleton.getSkinnedMeshJointIndex("level5");
        influences[3] = skeleton.getSkinnedMeshJointIndex("level6");
        influences[4] = skeleton.getSkinnedMeshJointIndex("level7");
        influences[5] = skeleton.getSkinnedMeshJointIndex("level8");
        skinMesh.setInfluenceIndices(influences);
        meshInst = (PPolygonSkinnedMeshInstance) pscene.addMeshInstance(skinMesh, new PMatrix());
        skeleton.addToSubGroup(meshInst, "Tentacle");
        meshInst.setMaterial(skinnedMeshMaterial);
        meshInst.applyMaterial();

        // Remap this skeleton onto the modified one
        skeleton.remapSkeleton(generateModifiedSkeleton());
        // now try another change
//        if (skeleton.displaceJoint("level3", new Vector3f(0, 100, 0)) == false)
//            System.out.println("Unable to modify the specified joint.");
//        if (skeleton.setJointPosition("level3", new Vector3f(10, 4, 0)) == false)
//            System.out.println("Unable to modify the specified joint.");
//        float[] fAngles = new float[3];
//        fAngles[0] = 0;
//        fAngles[1] = 0;
//        fAngles[2] = (float)(Math.PI * 0.5);
//        if (skeleton.setJointRotation("level3", new Quaternion(fAngles)) == false)
//            System.out.println("Unable to modify the specified joint.");
//        if (skeleton.rotateJoint("level3", Vector3f.UNIT_Z, (float)(Math.PI * 0.5)) == false)
//            System.out.println("Unable to modify the specified joint.");

        
        // Grab some joint references to animate
//        SkinnedMeshJoint joint1 = (SkinnedMeshJoint) modelInst.findChild("level1");
        SkinnedMeshJoint joint2 = (SkinnedMeshJoint) modelInst.findChild("level2");
        SkinnedMeshJoint joint3 = (SkinnedMeshJoint) modelInst.findChild("level5");

        // Attach processors to those joints
//        processors.add(new TestHierarchyAnimationProcessor(joint1,  0.003f, Vector3f.UNIT_Z));
        float[] fArray = new float[16];
        fArray[ 0] = 1;  fArray[ 1] =  0;  fArray[ 2] =   0;  fArray[ 3] = 0;
        fArray[ 4] = 0;  fArray[ 5] =  0;  fArray[ 6] =  -1;  fArray[ 7] = 4;
        fArray[ 8] = 0;  fArray[ 9] =  1;  fArray[10] =   0;  fArray[11] = 0;
        fArray[12] = 0;  fArray[13] =  0;  fArray[14] =   0;  fArray[15] = 1;
        processors.add(new TestHierarchyAnimationProcessor(joint2,  0.003f, Vector3f.UNIT_X, new PMatrix(fArray)));
        processors.add(new TestHierarchyAnimationProcessor(joint3,  0.003f, Vector3f.UNIT_X, new PMatrix(new Vector3f(0, 4, 0))));
//        processors.add(new TestHierarchyAnimationProcessor(joint3,  0.005f, Vector3f.UNIT_Z));

        // Show a reference skeleton of the form of the modified hierarchy
        SkeletonNode newSkeleton = generateModifiedSkeleton();
        newSkeleton.setName("AnotherSkeleton");
        // hook a sphere on the end
        skinMesh = createSkinnedModel(3, 1, 1, Vector3f.ZERO, "SomeMesh");
        influences = new int[6];
        influences[0] = newSkeleton.getSkinnedMeshJointIndex("level3");
        influences[1] = newSkeleton.getSkinnedMeshJointIndex("level4");
        influences[2] = newSkeleton.getSkinnedMeshJointIndex("level5");
        influences[3] = newSkeleton.getSkinnedMeshJointIndex("level6");
        influences[4] = newSkeleton.getSkinnedMeshJointIndex("level7");
        influences[5] = newSkeleton.getSkinnedMeshJointIndex("level8");
        skinMesh.setInfluenceIndices(influences);

        meshInst = (PPolygonSkinnedMeshInstance) pscene.addMeshInstance(skinMesh, new PMatrix());
        
        PPolygonModelInstance modelInstance = pscene.addModelInstance(newSkeleton, new PMatrix(new Vector3f(4, 0, 15)));
        newSkeleton = (SkeletonNode) modelInstance.getChild(0);
        newSkeleton.addToSubGroup(meshInst, "Head");
        

        joint2 = (SkinnedMeshJoint) modelInstance.findChild("level2");
        joint3 = (SkinnedMeshJoint) modelInstance.findChild("level5");
        processors.add(new TestHierarchyAnimationProcessor(joint2,  0.003f, Vector3f.UNIT_X, new PMatrix(fArray)));
        processors.add(new TestHierarchyAnimationProcessor(joint3,  0.003f, Vector3f.UNIT_X, new PMatrix(new Vector3f(0, 4, 0))));


        // hook up some control
        DahlgrensInput input = new DahlgrensInput();
        input.setTarget(modelInst);
        ((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setDefault(input);

        // Change the camera position
        FirstPersonCamState camState = (FirstPersonCamState) ((FlexibleCameraProcessor) wm.getUserData(FlexibleCameraProcessor.class)).getState();
        camState.setCameraPosition(new Vector3f(-15.0f, 3.0f, 0.0f));
        camState.setMovementRate(1.0f);

//        runPMatrixTest();

    }

    private void runPMatrixTest()
    {
        // We need to check the output here
        PMatrix testMatrix = new PMatrix(new Vector3f((float)(Math.PI * 0.5), 0, 0), new Vector3f(1, 1, 1), new Vector3f(10, 20, 30));
        System.out.println(testMatrix.toString());
    }

    public SkeletonNode generateSkeleton()
    {
        SkeletonNode result = new SkeletonNode("MyTestSkeleton");

        // build actual skeleton
        PNode skeletalRoot = new PNode(new PTransform());
        SkinnedMeshJoint base   = new SkinnedMeshJoint("base",   new PTransform());
        SkinnedMeshJoint level1 = new SkinnedMeshJoint("level1", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level2 = new SkinnedMeshJoint("level2", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level3 = new SkinnedMeshJoint("level3", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level4 = new SkinnedMeshJoint("level4", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level5 = new SkinnedMeshJoint("level5", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level6 = new SkinnedMeshJoint("level6", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level7 = new SkinnedMeshJoint("level7", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level8 = new SkinnedMeshJoint("level8", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));
        SkinnedMeshJoint level9 = new SkinnedMeshJoint("level9", new PTransform(new PMatrix(new Vector3f(0, 4.0f, 0.0f))));

        // hook them together
        skeletalRoot.addChild(base);
          base.addChild(level1);
        level1.addChild(level2);
        level2.addChild(level3);
        level3.addChild(level4);
        level4.addChild(level5);
        level5.addChild(level6);
        level6.addChild(level7);
        level7.addChild(level8);
        level8.addChild(level9);

        result.setSkeletonRoot(skeletalRoot);

        return result;
    }

    public SkeletonNode generateModifiedSkeleton()
    {
        SkeletonNode result = new SkeletonNode("MyTestSkeleton");

        // build actual skeleton
        PNode skeletalRoot = new PNode(new PTransform());
        SkinnedMeshJoint base   = new SkinnedMeshJoint("base",   new PTransform());
        SkinnedMeshJoint level1 = new SkinnedMeshJoint("level1", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));
        // TIE UP ANY CHANGES HERE WITH "bindMatrix" IN TestHierarchyAnimationProcessor!
        float[] fArray = new float[16];
        fArray[ 0] = 1;  fArray[ 1] =  0;  fArray[ 2] =  0;  fArray[ 3] = 0;
        fArray[ 4] = 0;  fArray[ 5] =  0;  fArray[ 6] = -1;  fArray[ 7] = 4;
        fArray[ 8] = 0;  fArray[ 9] =  1;  fArray[10] =  0;  fArray[11] = 0;
        fArray[12] = 0;  fArray[13] =  0;  fArray[14] =  0;  fArray[15] = 1;
        SkinnedMeshJoint level2 = new SkinnedMeshJoint("level2", new PTransform(new PMatrix(fArray)));
//        SkinnedMeshJoint level2 = new SkinnedMeshJoint("level2", new PTransform(new PMatrix(new Vector3f(0, 4, 0))));

        fArray = new float[16];
        fArray[ 0] = 1;  fArray[ 1] =  0;  fArray[ 2] =  0;  fArray[ 3] = 0;
        fArray[ 4] = 0;  fArray[ 5] =  0;  fArray[ 6] =  1;  fArray[ 7] = 4;
        fArray[ 8] = 0;  fArray[ 9] = -1;  fArray[10] =  0;  fArray[11] = 0;
        fArray[12] = 0;  fArray[13] =  0;  fArray[14] =  0;  fArray[15] = 1;
        // Set some rotational data
        SkinnedMeshJoint level3 = new SkinnedMeshJoint("level3", new PTransform(new PMatrix(fArray)));
//        SkinnedMeshJoint level3 = new SkinnedMeshJoint("level3", new PTransform(new PMatrix(new Vector3f(0, 4, 0))));


        SkinnedMeshJoint level4 = new SkinnedMeshJoint("level4", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));
        SkinnedMeshJoint level5 = new SkinnedMeshJoint("level5", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));
        SkinnedMeshJoint level6 = new SkinnedMeshJoint("level6", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));
        SkinnedMeshJoint level7 = new SkinnedMeshJoint("level7", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));
        SkinnedMeshJoint level8 = new SkinnedMeshJoint("level8", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));
        SkinnedMeshJoint level9 = new SkinnedMeshJoint("level9", new PTransform(new PMatrix(new Vector3f( 0.0f, 4.0f,  0.0f))));

        // hook them together
        skeletalRoot.addChild(base);
          base.addChild(level1);
        level1.addChild(level2);
        level2.addChild(level3);
        level3.addChild(level4);
        level4.addChild(level5);
        level5.addChild(level6);
        level6.addChild(level7);
        level7.addChild(level8);
        level8.addChild(level9);

        result.setSkeletonRoot(skeletalRoot);

        return result;
    }
}
