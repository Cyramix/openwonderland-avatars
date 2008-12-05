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
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.processors.TestHierarchyAnimationProcessor;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLCompileException;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLDefaultVariables;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.AmbientNdotL_Lighting;
import imi.scene.shader.effects.CalculateToLight_Lighting;
import imi.scene.shader.effects.DecalTexture;
import imi.scene.shader.effects.GenerateFragLocalNormal;
import imi.scene.shader.effects.MeshColorModulation;
import imi.scene.shader.effects.UnlitTexturing_Lighting;
import imi.scene.shader.effects.VertexToPosition_Transform;
import imi.scene.shader.effects.SimpleNdotL_Lighting;
import imi.scene.shader.effects.VertexDeformer_Transform;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.utils.PMeshUtils;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.*;
import imi.utils.input.DahlgrensInput;

/**
 * This class provides a simple test ground for relief mapping techniques
 * @author Ronald E Dahlgren
 */
public class DynamicShaderOnMeshTest extends DemoBase
{
    
    public DynamicShaderOnMeshTest(String[] args) 
    {
        super(args);
    }
  
    
    public static void main(String[] args) {
        DynamicShaderOnMeshTest worldTest = new DynamicShaderOnMeshTest(args);
    }
    
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        
        // just to prove it works...
        pscene.setUseRepository(true); 
        
        // Now make a materal with the new shaders to use
        PMeshMaterial mat = new PMeshMaterial("MyName!");
        GLSLShaderProgram shader = new VertDeformerWithSpecAndNormalMap(wm, 0.35f);
        System.out.print(shader.getVertexProgramSource());
        System.out.println("\n\n----FRAG LOGIC---\n\n");
        System.out.println(shader.getFragmentProgramSource());
        mat.setShader(shader);
        
        // Create the skeleton to attach our meshes to
        SkeletonNode skeleton = generateSkeleton();
        
        // create the lower half skinned mesh
        PPolygonSkinnedMesh skinMesh = createSkinnedModel(3, 4, 3, new Vector3f(0,0,0), "lowerHalf");
        int [] influences = new int[4];
        influences[0] = skeleton.getSkinnedMeshJointIndex("base");
        influences[1] = skeleton.getSkinnedMeshJointIndex("level1");
        influences[2] = skeleton.getSkinnedMeshJointIndex("level2");
        influences[3] = skeleton.getSkinnedMeshJointIndex("level3");
        skinMesh.setInfluenceIndices(influences);
        skeleton.addChild(skinMesh);
        // assign our material
        skinMesh.setMaterial(mat);
        
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
        skeleton.addChild(skinMesh);
        // Assign the material
        skinMesh.setMaterial(mat);
        
        // Add it to the pscene and grab a reference
        PPolygonModelInstance modelInst = pscene.addModelInstance(skeleton, new PMatrix(new Vector3f(10,5,8)));
        // refresh the new skeleton
        ((SkeletonNode)modelInst.findChild("MyTestSkeleton")).refresh();
        // make a new texture to slap on one of the instances
        mat = new PMeshMaterial("NewTexture");
        mat.setTexture("assets/textures/checkerboard2.PNG", 0);
        //mat.setTexture("assets/textures/tgatest.tga", 0);
        mat.setShader(shader); // use the second shader we generated
        
        // Assign the material with the second shader to the upper half mesh
        PPolygonSkinnedMeshInstance meshInst = ((SkeletonNode)modelInst.findChild("MyTestSkeleton")).getSkinnedMeshInstance("upperHalf");
        meshInst.getSharedMesh().setSolidColor(ColorRGBA.white);
        ((SkeletonNode)modelInst.findChild("MyTestSkeleton")).getSkinnedMeshInstance("upperHalf").setMaterial(mat);
        ((SkeletonNode)modelInst.findChild("MyTestSkeleton")).getSkinnedMeshInstance("upperHalf").setUseGeometryMaterial(false);
                
        // Grab some joint references to animate
        SkinnedMeshJoint joint1 = (SkinnedMeshJoint) modelInst.findChild("level1");
        SkinnedMeshJoint joint2 = (SkinnedMeshJoint) modelInst.findChild("level3");
        SkinnedMeshJoint joint3 = (SkinnedMeshJoint) modelInst.findChild("level7");
        
        // Attach processors to those joints
        processors.add(new TestHierarchyAnimationProcessor(joint1,  0.003f, Vector3f.UNIT_Z));
        processors.add(new TestHierarchyAnimationProcessor(joint2,  0.003f, Vector3f.UNIT_X));
        processors.add(new TestHierarchyAnimationProcessor(joint3,  0.005f, Vector3f.UNIT_Z));
        
        // hook up some control
        DahlgrensInput input = new DahlgrensInput();
        input.setTarget(modelInst);
        ((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).addScheme(input);
        
        // add a reference cube lit with the fixed function pipeline
        pscene.addModelInstance(PMeshUtils.createCubeBox("cube", 
                Vector3f.ZERO, 3.0f, 3.0f, 3.0f, ColorRGBA.blue), new PMatrix());
        
    }
    
    public SkeletonNode generateSkeleton()
    {
        SkeletonNode result = new SkeletonNode("MyTestSkeleton");
        
        // build actual skeleton
        PNode skeletalRoot = new PNode(new PTransform());
        SkinnedMeshJoint base   = new SkinnedMeshJoint("base",   new PTransform());
        SkinnedMeshJoint level1 = new SkinnedMeshJoint("level1", new PTransform(new PMatrix(new Vector3f(0, 4.0f,    0 ))));
        SkinnedMeshJoint level2 = new SkinnedMeshJoint("level2", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        SkinnedMeshJoint level3 = new SkinnedMeshJoint("level3", new PTransform(new PMatrix(new Vector3f(0, 4.0f,    0 ))));
        SkinnedMeshJoint level4 = new SkinnedMeshJoint("level4", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        SkinnedMeshJoint level5 = new SkinnedMeshJoint("level5", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        SkinnedMeshJoint level6 = new SkinnedMeshJoint("level6", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        SkinnedMeshJoint level7 = new SkinnedMeshJoint("level7", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        SkinnedMeshJoint level8 = new SkinnedMeshJoint("level8", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        SkinnedMeshJoint level9 = new SkinnedMeshJoint("level9", new PTransform(new PMatrix(new Vector3f(0, 4.0f,  0.0f))));
        
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
