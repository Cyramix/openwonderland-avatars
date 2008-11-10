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
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModel;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.processors.TestHierarchyAnimationProcessor;
import imi.scene.shader.programs.VertexDeformer;
import java.io.File;
import java.util.ArrayList;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.ProcessorComponent;

/**
 * This test demonstrates the construction of a hierarchy based model and shows dynamic 
 * attaching of parts during run-time.
 * @author Ronald E Dahlgren
 */
public class RobertTest extends DemoBase 
{
    public RobertTest(String[] args)
    {
        super(args);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String[] args) 
    {
        RobertTest worldTest = new RobertTest(args);
    }
    /**
     * This method initializes the scene for demonstration purposes
     * @param pscene 
     * @param wm
     * @param processors A collection of processor components that are utilized by the default entity.
     */
    @Override
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        pscene.setUseRepository(true);
         // position and orient the camera
         //m_cameraProcessor.setPosition(new Vector3f(20.0f, 0.0f, -30.0f));
        
         // create the base tower for robert to stand on
         PPolygonModelInstance towerModel = initializeAndAnimateSkinnedMesh(pscene, processors);
         // create Robert
         PPolygonModel robertModel = createArticulatedModel(0.5f, 0.8f, 1.6f, 1.6f, 1.9f, new PMatrix());
         // add the model to the PScene, give him a transform translating on the positive Y axis.
         // This allows robert to stand on the tower (rather than aligning origins)sss
         PPolygonModelInstance robertInstance = pscene.addModelInstance(robertModel, new PMatrix(new Vector3f(0.0f, 33.5f, 0.0f)));
         // make this instance a child of the tower's top
         towerModel.findChild("middle 8").addChild(robertInstance); 
         // Now let's create two more skinned meshes and attach them to robert as tentacle arms (that makes sense, right?)
         PPolygonModelInstance leftTentacle = createAndAnimateTentacleArm(pscene, 
                                                                          processors,
                                                                          new File("assets/textures/largecheckerboard.PNG"), 
                                                                          10, 4, 6); // divisions, width, length
         PPolygonModelInstance rightTentacle = createAndAnimateTentacleArm(pscene, 
                                                                          processors,
                                                                          new File("assets/textures/grass.jpg"), 
                                                                          6, 6, 6); // divisions, width, length
         // Let's orient the tentacles more like arms
         leftTentacle.getTransform().getLocalMatrix(true).setRotation(new Vector3f(3.14159f,1.56f,0.0f));
         rightTentacle.getTransform().getLocalMatrix(true).setRotation(new Vector3f(-3.14159f,-1.56f,0.0f));
         // Now attach these to the appropriate attaching points on Robert
         robertInstance.findChild("Robert's Left Arm").addChild(leftTentacle);
         robertInstance.findChild("Robert's Right Arm").addChild(rightTentacle);
         // That's it! Behold a mighty tentacle-armed Robert!
    }

    /**
     * This method generates the base tower for robert
     * @param pscene
     * @param processors
     * @return The completed model instance referencing the skinned mesh
     */
    private PPolygonModelInstance initializeAndAnimateSkinnedMesh(PScene pscene, ArrayList<ProcessorComponent> processors)
    {
        //PPolygonModel skinnedModel = createSkinnedModel(8, 8.0f, 9.0f);
        PPolygonSkinnedMesh skinnedMesh = createSkinnedModel(8, 8.0f, 9.0f, Vector3f.ZERO, "Name!");//(PPolygonSkinnedMesh) skinnedModel.getChild(0); // grab the mesh so we can tweak the material
        // set up the material to use the skinning shader
        PMeshMaterial pMaterial = new PMeshMaterial("Stucco Skinning", "assets/textures/nskinwh.jpg"); 
        // TODO: Convert to the new system
        pMaterial.setShader(new VertexDeformer(pscene.getWorldManager()));
        // make sure we have geometry before setting the material
        skinnedMesh.submit(new PPolygonTriMeshAssembler());
        skinnedMesh.setMaterial(pMaterial); // setting the material implicitely "applies" it
        // add this to the PScene, we get back the instance
        PPolygonModelInstance modelInst = pscene.addModelInstance(skinnedMesh, new PMatrix(new Vector3f(20.0f, -64.0f, 0.0f)));
        // Child zero of the transform heirarchy is the root bone, animate some children
//        SkinnedMeshJoint pBone  = (SkinnedMeshJoint) ((PPolygonSkinnedMeshInstance)(modelInst.getChild(0))).getTransformHierarchy().getChild(0).getChild(0);
//        SkinnedMeshJoint pBone2 = (SkinnedMeshJoint) ((PPolygonSkinnedMeshInstance)(modelInst.getChild(0))).getTransformHierarchy().getChild(0).getChild(0).getChild(0).getChild(0);
//        SkinnedMeshJoint pBone3 = (SkinnedMeshJoint) ((PPolygonSkinnedMeshInstance)(modelInst.getChild(0))).getTransformHierarchy().getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0);
//        SkinnedMeshJoint pBone4 = (SkinnedMeshJoint) ((PPolygonSkinnedMeshInstance)(modelInst.getChild(0))).getTransformHierarchy().getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0).getChild(0);
        // Animate some joints
        // *NOTE* - Our animation processor kind of sucks, so it usually gets hung up in some sort of twitching loop
//        processors.add(new TestHierarchyAnimationProcessor(pBone, 0.003f, Vector3f.UNIT_X));
//        processors.add(new TestHierarchyAnimationProcessor(pBone2, -0.004f, Vector3f.UNIT_X));
//        processors.add(new TestHierarchyAnimationProcessor(pBone3, 0.0015f, Vector3f.UNIT_X));
//        processors.add(new TestHierarchyAnimationProcessor(pBone4, -0.0015f, Vector3f.UNIT_X));

        // return the model for attachment purposes
        PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance)modelInst.getChild(0);
        meshInst.setUseGeometryMaterial(false);
        return modelInst;
    }
    
    private PPolygonModelInstance createAndAnimateTentacleArm(
                                    PScene pscene, 
                                    ArrayList<ProcessorComponent> processors,
                                    File texture,
                                    int tentacleDivisions,
                                    float fTentacleWidth,
                                    float fTentacleSectionLength)
    {
        // Make the model with the requested dimensions
        PPolygonSkinnedMesh skinnedMesh = createSkinnedModel(tentacleDivisions, fTentacleWidth, fTentacleSectionLength, Vector3f.ZERO, "Name again!");
        // set up the material to use the skinning shader and the requested texture file
        PMeshMaterial pMaterial = new PMeshMaterial("Skinning Material", texture.getPath()); 
        skinnedMesh.setMaterial(pMaterial); // setting the material implicitely "applies" it
        // add this to the PScene, we get back the instance
        PPolygonModelInstance modelInst = pscene.addModelInstance(skinnedMesh, new PMatrix(new Vector3f(0.0f, 0.0f, 0.0f)));
        // Animate some joints
        SkinnedMeshJoint pBone = null;//(SkinnedMeshJoint) ((PPolygonSkinnedMeshInstance)(modelInst.getChild(0))).getTransformHierarchy().getChild(0);
        for (int i = 0; i < tentacleDivisions; i++)
        {
            // Use a 60% chance of animating
            if (Math.random() <= 0.60)
            {
                // animate this bone
                processors.add(new TestHierarchyAnimationProcessor(pBone, (float)(Math.random() * 0.01), Vector3f.UNIT_X));
            }
            // walk to the next joint
            pBone = (SkinnedMeshJoint) pBone.getChild(0);
        }
        
        PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance)modelInst.getChild(0);
        meshInst.setUseGeometryMaterial(false);
        return modelInst;
    }
    
    

    
    
}
