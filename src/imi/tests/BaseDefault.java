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

////////////////////////////////////////////////////////////////////////////////
// IMPORTS - BEGIN
////////////////////////////////////////////////////////////////////////////////
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.CullState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.gui.JPanel_Animations;
import imi.gui.JPanel_BasicOptions;
import imi.gui.JPanel_EZOptions;
import imi.gui.JPanel_ServerBrowser;
import imi.gui.OptionsGUI;
import imi.gui.SceneEssentials;
import imi.gui.TreeExplorer;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.repository.Repository;
import imi.scene.JScene;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.camera.behaviors.FirstPersonCamModel;
import imi.scene.camera.behaviors.TumbleObjectCamModel;
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.camera.state.TumbleObjectCamState;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModel;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.processors.FlexibleCameraProcessor;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.utils.PMeshUtils;
import imi.utils.FileUtils;
import imi.utils.PMathUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

import org.jdesktop.mtgame.FrameRateListener;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.CameraComponent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;

//import mtgame.tests.sigraph.SkyBox;
import org.jdesktop.mtgame.AWTInputComponent;
import org.jdesktop.mtgame.InputManager;
import org.jdesktop.mtgame.RenderBuffer;
import org.jdesktop.mtgame.processor.RotationProcessor;
////////////////////////////////////////////////////////////////////////////////
// IMPORTS - END
////////////////////////////////////////////////////////////////////////////////

/**
 * New Demo Base that incorporates the main Avatar Creator as part of the GUI
 * @author  Lou Hayat, Ronald E Dahlgren, Paul Viet Nguyen Truong
 */
public class BaseDefault extends javax.swing.JFrame implements FrameRateListener, java.awt.event.ActionListener {
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - BEGIN
////////////////////////////////////////////////////////////////////////////////    
    private WorldManager                m_worldManager      = null;
    protected CameraNode                m_cameraNode        = null;
    protected int                       m_desiredFrameRate  = 60;
    protected int                       m_width             = 800;
    protected int                       m_height            = 600;
    protected float                     m_aspect            = 800.0f/600.0f;
    protected SceneEssentials           m_sceneData         = null;
    protected boolean                   m_bLoading          = false;
    protected RenderBuffer              m_renderBuffer      = null;
    protected Component                 m_base              = this;
    protected OptionsGUI                m_AvatarOptions     = null;
    protected TreeExplorer              m_NodeExplorer      = null;
    protected JFrame                    m_AnimationViewer   = null;
    protected JPanel_ServerBrowser      m_ServerBrowser  = null;
    protected FlexibleCameraProcessor   m_cameraProcessor   = null;
////////////////////////////////////////////////////////////////////////////////
// CLASS DATA MEMBERS - END
////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - BEGIN
////////////////////////////////////////////////////////////////////////////////
    public BaseDefault(String[] args) {
        //setLookFeel();
        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                
                // TODO: Real cleanup
                System.exit(0);
            }
        });
        
        System.out.println("Current Directory: " + System.getProperty("user.dir"));
        
        m_worldManager = new WorldManager("DemoWorld");
        
        processArgs(args);
        m_worldManager.getRenderManager().setDesiredFrameRate(m_desiredFrameRate);
        
        // add the repository
        m_worldManager.addUserData(Repository.class, new Repository(m_worldManager));

        createUI(m_worldManager);  
        createTestSpace(m_worldManager);
        createCameraEntity(m_worldManager);  
        createInputEntity(m_worldManager); 
        createDemoEntities(m_worldManager);
        setGlobalLighting(m_worldManager);
        runProgressBar(false);
    }
    
    /**
     * Set up the global lighting
     */
    protected void setGlobalLighting(WorldManager wm)
    {
        // Lighting Configuration
        LightNode lightNode = new LightNode("Dis is me light node man!");
        // Must be a PointLight to function
        PointLight pointLight = new PointLight();
        pointLight.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        pointLight.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.2f));
        pointLight.setEnabled(true);
        // attach it to the LightNode
        lightNode.setLight(pointLight);
        lightNode.setLocalTranslation(0.0f, 50.0f, 50.0f);
        // add it to the render manager
        wm.getRenderManager().addLight(lightNode);
    }
    /**
     * Override this for simple tests that only require a single scene that do
     * not need to set up the enity for fancy shmancy stuff
     * @param pscene (PScene)
     * @param wm (WorldManager)
     * @param processors (ArrayList<ProcessorComponent>)
     */
    protected void simpleSceneInit(JScene jscene, WorldManager wm, Entity jsentity, ArrayList<ProcessorComponent> processors) {
        m_sceneData = new SceneEssentials();
        m_sceneData.setSceneData(jscene, jscene.getPScene(), jsentity, wm, processors);
    }
    
    /**
     * Override this if you wish to have multiple entities or if you wish to 
     * setup your entity for fancy shmanciness
     * @param wm (WorldManager)
     */
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
        
        // Initialize the scene
        simpleSceneInit(jscene, wm, JSEntity, processors);
    }
    
    public void setDefaultRenderStates(JScene jscene, WorldManager wm) {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(ColorRGBA.white);
        
        // Lighting Configuration
        LightNode lightNode = new LightNode("Dis is me light node man!");
        // Must be a PointLight to function
        PointLight pointLight = new PointLight();
        pointLight.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        pointLight.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 0.2f));
        pointLight.setEnabled(true);
        // attach it to the LightNode
        lightNode.setLight(pointLight);
        lightNode.setLocalTranslation(0.0f, 50.0f, 50.0f);
        // add it to the render manager
        wm.getRenderManager().addLight(lightNode);
        
        // Cull State
        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);      
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        
        // Wireframe State
        WireframeState ws = (WireframeState) wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(false);
        
        // Push 'em down the pipe
        jscene.setRenderState(matState);
        jscene.setRenderState(buf);
        jscene.setRenderState(cs);
        jscene.setRenderState(ws);
        jscene.updateRenderState();
    }
    
    public void createSpace(String name, Vector3f center, ZBufferState buf, ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;

        Box cube = null;
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        
        // Create the root for the space
        Node node = new Node();
        
        // Now the walls
        Box box = new Box(name + "Box", center, 500.0f, 500.0f, 500.0f);
        node.attachChild(box);
       
        // Now some rotating cubes - all confined within the space (not entities)
        createCube(center, -250.0f, 150.0f,  250.0f, pcc, node, wm);
        createCube(center,  250.0f, 150.0f,  250.0f, pcc, node, wm);
        createCube(center,  250.0f, 150.0f, -250.0f, pcc, node, wm);
        createCube(center, -250.0f, 150.0f, -250.0f, pcc, node, wm);
     
        // Add bounds and state for the whole space
        BoundingBox bbox = new BoundingBox(center, 500.0f, 500.0f, 500.0f);
        node.setModelBound(bbox);
        node.setRenderState(buf);
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(color);
        node.setRenderState(matState);
        
        // Create a scene component for it
        RenderComponent sc = wm.getRenderManager().createRenderComponent(node);
        
        // Finally, create the space and add it.
        Entity e = new Entity(name + "Space");
        e.addComponent(ProcessorCollectionComponent.class, pcc);
        e.addComponent(RenderComponent.class, sc);
        wm.addEntity(e);        
    }
    
    private void createCube(Vector3f center, float xoff, float yoff, float zoff, ProcessorCollectionComponent pcc, Node parent, WorldManager wm) {
        Vector3f cubeCenter = new Vector3f();
        Vector3f c = new Vector3f();
        
        cubeCenter.x = center.x + xoff;
        cubeCenter.y = center.y + yoff;
        cubeCenter.z = center.z + zoff;
        Box cube = new Box("Space Cube", c, 5.0f, 5.0f, 5.0f);
        Node cubeNode = new Node();
        cubeNode.setLocalTranslation(cubeCenter);
        cubeNode.attachChild(cube);  
        parent.attachChild(cubeNode);
        
        RotationProcessor rp = new RotationProcessor("Cube Rotator", wm, cubeNode, 
                (float) (6.0f * Math.PI / 180.0f));
        //rp.setRunInRenderer(true);
        pcc.addProcessor(rp);
    }
    
     /**
     * This method procedurally generates a skinned model of a "tower".
     * Bones are generated per floor.
     * @param numberOfLevels How many floors (must be three or more)
     * @param floorHeight The space between floors
     * @param floorWidth The width (and depth) of floors
     * @return The completed model!
     */
    public PPolygonSkinnedMesh createSkinnedModel(int numberOfLevels, float floorHeight, float floorWidth) {
        // increment to account for the roof
        numberOfLevels++;
        
        if (numberOfLevels < 3) // If less than 3 floors was requested, we cannot properly generate the model
            return null;
                       
        PPolygonSkinnedMesh mesh = new PPolygonSkinnedMesh("ProceduralSkinnedTower");
        mesh.setSmoothNormals(true); // Just to be pretty 
        
        mesh.beginBatch(); // Always call beginBatch when messing with a mesh's geometry. The accompanying endBatch call comes later
        
        // Grab a reference to flesh out the transform hierarchy,
        // the model (not instance) contains the transform hierarchy for the bind pose
        // which will be referenced by all instances that use this geometry as a base.
        PNode bindPose = null;//mesh.getBindPoseTransformHierarchy(); 
        
        SkinnedMeshJoint [] bones = new SkinnedMeshJoint[numberOfLevels]; // Allocate the array for our bones (made of PJoints)
        // the base!                       Name,Parent,Child(ren),Transform
        bones[0]   = new SkinnedMeshJoint("base", null, null,   new PTransform());
        // Attach this newly created bone to the base of the skeleton, aka transform hierarchy.
        bindPose.addChild(bones[0]);
        
        for (int i = 1; i < numberOfLevels; i++) // For each level (start at one so the following logic works)
        {
            // Generate a bone at each floor
            bones[i]   = new SkinnedMeshJoint("middle " + i, null, null, new PTransform(Vector3f.ZERO, new Vector3f(0.0f, floorHeight, 0.0f), Vector3f.UNIT_XYZ));
            // Add this to the previously created floor
            bones[i-1].addChild(bones[i]);
        }
        
        // build world matrices
        bones[0].buildFlattenedHierarchy();
        
        // build the vertices needed to construct the skin, each floor is composed of 8 verts split into two layers
        PPolygonSkinnedVertexIndices [][] skinnedVertArray   = new PPolygonSkinnedVertexIndices[numberOfLevels][8];
        
        // This array is generated in order to make the loop ahead more readable
        // Two tiers of verts are allocated per floor: one at the base (bottom), and
        // another half-way up the floor. These are local space coordinates.
        Vector3f [] floorWidthOffsets = {   new Vector3f(0.0f, 0.0f, -floorWidth), // Bottom Layer
                                            new Vector3f(-floorWidth, 0.0f, 0.0f), 
                                            new Vector3f(0.0f, 0.0f, floorWidth), 
                                            new Vector3f(floorWidth, 0.0f, 0.0f),
                                            new Vector3f(0.0f, floorHeight * 0.5f, -floorWidth), // Top layer
                                            new Vector3f(-floorWidth, floorHeight * 0.5f, 0.0f), 
                                            new Vector3f(0.0f, floorHeight * 0.5f, floorWidth), 
                                            new Vector3f(floorWidth, floorHeight * 0.5f, 0.0f) };
        
        Vector2f [] textureCoordinates = {  new Vector2f(0,0), // Bottom Row
                                            new Vector2f(0.33f, 0),
                                            new Vector2f(0.66f, 0),
                                            new Vector2f(1.0f, 0),
                                            new Vector2f(0,1), // Top Row
                                            new Vector2f(0.33f, 1),
                                            new Vector2f(0.66f, 1),
                                            new Vector2f(1.0f, 1) };
        
        for (int boneIndex = 0; boneIndex < bones.length; boneIndex++) // For each bone that we generated
        {
            skinnedVertArray[boneIndex][0]    = new PPolygonSkinnedVertexIndices(); 
            
            for (int i = 0; i < 8; i++) // For each vert on this layer
            {
                Vector3f vertPosition = new Vector3f(floorWidthOffsets[i]); // grab our position
                
                skinnedVertArray[boneIndex][i]    = new PPolygonSkinnedVertexIndices(); // Start a new vert
                
                bones[boneIndex].getTransform().getWorldMatrix(false).transformPoint(vertPosition); // Move the position into bone space

                skinnedVertArray[boneIndex][i].m_PositionIndex    = mesh.getPosition(vertPosition); // Add our position to the mesh and retrieve the index
                skinnedVertArray[boneIndex][i].m_NormalIndex      = mesh.getNormal(Vector3f.UNIT_XYZ); // These normals are generated later, so give them some default; obviously wrong, not unit length ;)
                skinnedVertArray[boneIndex][i].m_ColorIndex       = mesh.getColor(ColorRGBA.red); // Change at will =)
                skinnedVertArray[boneIndex][i].m_TexCoordIndex[0] = mesh.getTexCoord(textureCoordinates[i]); // Generate proper texture coordinates
                // Which bones influence this vert, these are sorted by precedence (also the second two weights are zero, so the last two indices are irrelevant
                skinnedVertArray[boneIndex][i].m_BoneIndicesIndex = mesh.getBoneIndices( new PBoneIndices(boneIndex, PMathUtils.clamp(boneIndex+1, bones.length - 1), 0, 0) );
                // allocate weighting appropriately
                if (i < 4) // bottom layer
                    skinnedVertArray[boneIndex][i].m_BoneWeightIndex  = mesh.getBoneWeights(new Vector3f(1.0f, 0.0f, 0.0f)); // all influence goes on this bone
                else // upper layer
                    skinnedVertArray[boneIndex][i].m_BoneWeightIndex  = mesh.getBoneWeights(new Vector3f(0.5f, 0.5f, 0.0f)); // fifty fifty
            } // End for each vert loop
        } /// End for each bone loop
       
        // use the vertices and build the skin
        for (int boneIndex = 0; boneIndex < bones.length - 1; boneIndex++) // for each bone
        {   
            // create a quad
            for (int i = 0; i < 4; i++)
            {
                PPolygon poly1  = new PPolygon(mesh);
                PPolygon poly2  = new PPolygon(mesh);
                // Magical math hacks follow... if you've ever worked on a tile engine you should recognize this stuff ;)
                poly1.addVertex(skinnedVertArray[boneIndex]  [ (i+1)  % 4      ]); // 1
                poly1.addVertex(skinnedVertArray[boneIndex]  [ ((i+1) % 4) + 4 ]); // 5
                poly1.addVertex(skinnedVertArray[boneIndex]  [ (i % 4) + 4     ]); // 4
                poly1.addVertex(skinnedVertArray[boneIndex]  [ i               ]); // 0
                
                poly2.addVertex(skinnedVertArray[boneIndex]  [ ((i+1) % 4) + 4 ]); // 5
                poly2.addVertex(skinnedVertArray[boneIndex+1][ (i+1)  % 4      ]); // Next 1
                poly2.addVertex(skinnedVertArray[boneIndex+1][ i               ]); // Next 0
                poly2.addVertex(skinnedVertArray[boneIndex]  [ (i % 4) + 4     ]); // 4
                // Generate proper normals and add these quads to the mesh
                poly1.calculateNormal();
                poly2.calculateNormal();
                mesh.addPolygon(poly1);
                mesh.addPolygon(poly2);
            }
        }
        
        // Add a polygon for the roof 
        PPolygon roofPoly  = new PPolygon(mesh);
        roofPoly.addVertex(skinnedVertArray[numberOfLevels - 1][3]);
        roofPoly.addVertex(skinnedVertArray[numberOfLevels - 1][0]);
        roofPoly.addVertex(skinnedVertArray[numberOfLevels - 1][1]);
        roofPoly.addVertex(skinnedVertArray[numberOfLevels - 1][2]);
        roofPoly.calculateNormal();
        mesh.addPolygon(roofPoly);
        
        // Add a polygon for the floor
        PPolygon floorPoly  = new PPolygon(mesh);
        floorPoly.addVertex(skinnedVertArray[0][3]);
        floorPoly.addVertex(skinnedVertArray[0][2]);
        floorPoly.addVertex(skinnedVertArray[0][1]);
        floorPoly.addVertex(skinnedVertArray[0][0]);
        floorPoly.calculateNormal();
        mesh.addPolygon(floorPoly);
                
        mesh.endBatch(); // finished editing geometry; endBatch calls some methods under the hood that are important for proper rendering
        mesh.setMaterial(new PMeshMaterial()); // Default material

        mesh.submit(new PPolygonTriMeshAssembler());
        return mesh; // finished!
    }
    
    /**
     * Generates a procedurally created articulated model. This model is made of geometric primitives, so don't
     * expect anything too cool (except the shoes)
     * @param headRadius
     * @param bodyWidth
     * @param bodyHeight
     * @param armLength
     * @param legLength
     * @param origin The center of the model in local space
     * @return The completed model
     */
    public PPolygonModel createArticulatedModel(float headRadius, float bodyWidth, float bodyHeight, float armLength, float legLength, PMatrix origin) {
        // This scale is used to make the numbers passed in smaller while maintaining some
        // semblance of proportion.
        float scale = 10.0f;
        headRadius *= scale; // Scale everything accordingly.
        bodyWidth  *= scale;
        bodyHeight *= scale;
        armLength  *= scale;
        legLength  *= scale;
        
        PPolygonModel resultModel = new PPolygonModel("Robert");
        
        // Generate the heirarchy. We will basically be making a tree of PJoint's and attaching
        // meshes to the appropriate attaching points once those are complete. The tree begins at the model level
        // and continues "down" the heirarchy to the feet. The individual joints are given transforms in order to 
        // properly place them in relation to their parents. This allows the attached meshes to exist at the origin
        // in their local space and be placed (mostly) correctly automatically.
        PJoint torso = new PJoint("Robert's Torso", new PTransform(origin)); // The center of mass is the torso
        resultModel.addChild(torso);
        
        PJoint head  = new PJoint("Robert's Head", new PTransform(new PMatrix(Vector3f.UNIT_Y.mult(headRadius * 0.5f)))); // Offset upwards
        torso.addChild(head);
        
        PJoint rightArm  = new PJoint("Robert's Right Arm", new PTransform(new PMatrix(Vector3f.UNIT_X.mult(bodyWidth * 0.7f)))); // offset viewer right
        torso.addChild(rightArm);
        
        PJoint leftArm  = new PJoint("Robert's Left Arm", new PTransform(new PMatrix(Vector3f.UNIT_X.mult(bodyWidth * (-0.7f))))); // Offset viewer left
        torso.addChild(leftArm);
        
        PJoint rightLeg  = new PJoint("Robert's Right Leg", new PTransform(new PMatrix(new Vector3f(bodyWidth * 0.5f, bodyHeight * (-0.8f), 0.0f) ))); // '' right and down
        torso.addChild(rightLeg);
        
        PJoint leftLeg  = new PJoint("Robert's Left Leg", new PTransform(new PMatrix(new Vector3f(bodyWidth * (-0.5f), bodyHeight * (-0.8f), 0.0f) ))); // '' left and down
        torso.addChild(leftLeg);
        
        PJoint rightFoot  = new PJoint("Robert's Right Foot", new PTransform(new PMatrix(new Vector3f(0.0f, -legLength, 0.3f) ))); // '' right and down
        rightLeg.addChild(rightFoot);
        
        PJoint leftFoot  = new PJoint("Robert's Left Foot", new PTransform(new PMatrix(new Vector3f(0.0f, -legLength, 0.3f) ))); // '' left and down
        leftLeg.addChild(leftFoot);
        
        // At this point the PJoint hierarchy is in place. Now let's create some meshes and attach them to the
        // appropriate PJoints.
        
        // This material will be used on all of the procedurally generated geometry.
        // Anytime our PMeshUtils or PModelUtils methods are used, the resulting geometry
        // should have a PMeshMaterial explicitely assigned to it in order to avoid the
        // default material. 
        PMeshMaterial geometryMaterial = new PMeshMaterial();
        geometryMaterial.setColorMaterial(ColorMaterial.Diffuse); // Make the vert colors affect diffuse coloring
        // NOTE - On skinned meshes, the vertex colors are currently used to send weighting data to the deforming shader.
        //        Use caution with skinned meshes and vertex coloration =)
        geometryMaterial.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.0f));
        
        PPolygonMesh rootCylinder = PMeshUtils.createCylinder("Torso", Vector3f.ZERO, -bodyHeight, 8, 8, bodyWidth, bodyWidth, true, true, ColorRGBA.green); // Green vert coloration
        rootCylinder.flipNormals(); // Cylinder defaults to having normals facing in.
        rootCylinder.setMaterial(geometryMaterial); // Assign the material that we are using.
        torso.addChild(rootCylinder); // This cylinder is now a child of the torso representing the torso's geometry
        // The other bits of geometry follow the same basic pattern
        PPolygonMesh sphereMesh = PMeshUtils.createSphere("Head", Vector3f.ZERO, headRadius, 6, 6, ColorRGBA.randomColor()); // sphere changes color every time ;)
        sphereMesh.setMaterial(geometryMaterial); // No normal flipping needed by default on our spheres.
        head.addChild(sphereMesh);
        
        PPolygonMesh rightLegMesh = PMeshUtils.createCylinder("Leg", Vector3f.ZERO, -legLength, 8, 8, 0.3f * scale, 0.4f * scale, false, true, ColorRGBA.blue);
        rightLegMesh.flipNormals();
        rightLegMesh.setMaterial(geometryMaterial);
        rightLeg.addChild(rightLegMesh);
        
        PPolygonMesh leftLegMesh = PMeshUtils.createCylinder("Leg", Vector3f.ZERO, -legLength, 8, 8, 0.3f * scale, 0.4f * scale, false, true, ColorRGBA.blue);
        leftLegMesh.flipNormals();
        leftLegMesh.setMaterial(geometryMaterial);
        leftLeg.addChild(leftLegMesh);
        // Now we make a new material that features some multi-texturing action
        geometryMaterial = new PMeshMaterial();
        geometryMaterial.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 0.0f));
        geometryMaterial.setTexture("assets/textures/checkerboard.png", 0);
        geometryMaterial.setTexture("assets/textures/largecheckerboard.PNG", 1);
        geometryMaterial.setTexture("assets/textures/dwarf2.jpg", 2);
        
        // Make the feet meshes - Now with sweet multitextured shoes!
        PPolygonMesh rightFootMesh = PMeshUtils.createBox("Foot", Vector3f.ZERO, 0.33f * scale, 0.35f * scale, 0.8f * scale, ColorRGBA.red); // vert color = red
        rightFootMesh.setUniformTexCoords(true); // This call copies the UV data for texture unit 0 to all other texture units
        rightFootMesh.setNumberOfTextures(3); // Specify how many textures we intent to use
        rightFootMesh.setMaterial(geometryMaterial);
        rightFoot.addChild(rightFootMesh);
        
        PPolygonMesh leftFootMesh = PMeshUtils.createBox("Foot", Vector3f.ZERO, 0.33f * scale, 0.35f * scale, 0.8f * scale, ColorRGBA.red);
        leftFootMesh.setUniformTexCoords(true);
        leftFootMesh.setNumberOfTextures(3);
        leftFootMesh.setMaterial(geometryMaterial);
        leftFoot.addChild(leftFootMesh);
        
        torso.buildFlattenedHierarchy(); // This method updates all the world transforms for the hierarchy.
                
        resultModel.submitGeometry(new PPolygonTriMeshAssembler());
                
        return resultModel;
    }
    
    public void createInputEntity(WorldManager wm) {
        // Create input entity
        Entity InputEntity = new Entity("Input Entity");
        // Create event listener
        AWTInputComponent eventListener = (AWTInputComponent)m_worldManager.getInputManager().createInputComponent(canvas_SceneRenderWindow, InputManager.KEY_EVENTS);
        // Create event processor
        JSceneAWTEventProcessor eventProcessor  = new JSceneAWTEventProcessor(eventListener, null, InputEntity);
        // Add the processor component to the entity
        InputEntity.addComponent(ProcessorComponent.class, eventProcessor);
        InputEntity.addComponent(AWTInputComponent.class, eventListener);
        // Add the entity to the world manager
        wm.addEntity(InputEntity);  
        // Add the this input manager to the world manager for future access
        // (to asign a jscenes to drive)
        wm.addUserData(JSceneEventProcessor.class, eventProcessor);
    }

    private Texture loadSkyboxTexture(String filePath) {
        Texture monkeyTexture = null;
        try
        {
            monkeyTexture = TextureManager.loadTexture(new File(FileUtils.rootPath, filePath).toURI().toURL(), Texture.MinificationFilter.NearestNeighborNoMipMaps, Texture.MagnificationFilter.NearestNeighbor);
        } catch (MalformedURLException ex)
        {
            Logger.getLogger(DemoBase.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (monkeyTexture != null)
        {
            monkeyTexture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Clamp);
            monkeyTexture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Clamp);
        }
        return monkeyTexture;
    }
        
    private void createCameraEntity(WorldManager wm) {
        Node cameraSG = createCameraGraph(wm);
        
        // Add the camera
        Entity camera = new Entity("DefaultCamera");
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, m_cameraNode, 
                m_width, m_height, 45.0f, m_aspect, 0.1f, 1000.0f, true);
        m_renderBuffer.setCameraComponent(cc);
        camera.addComponent(CameraComponent.class, cc);
        
        //////////////////////////////////////////////////////////////////////
        // Skybox
        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, wm);
        
        sky.setTexture(SkyBox.NORTH,   loadSkyboxTexture("assets/textures/skybox/default.png")); // north
        sky.setTexture(SkyBox.EAST,    loadSkyboxTexture("assets/textures/skybox/default.png")); // south
        sky.setTexture(SkyBox.SOUTH,   loadSkyboxTexture("assets/textures/skybox/default.png")); // east
        sky.setTexture(SkyBox.WEST,    loadSkyboxTexture("assets/textures/skybox/default.png")); // west
        sky.setTexture(SkyBox.DOWN,    loadSkyboxTexture("assets/textures/skybox/default.png")); // up
        sky.setTexture(SkyBox.UP,      loadSkyboxTexture("assets/textures/skybox/default.png")); // down
        //
        
        RenderComponent sc2 = wm.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);
        //////////////////////////////////////////////////////////////////////

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas_SceneRenderWindow, eventMask);
        m_cameraProcessor = new FlexibleCameraProcessor(cameraListener, cameraSG, wm, camera, sky);
        
        FirstPersonCamState state = new FirstPersonCamState();
        FirstPersonCamModel model = new FirstPersonCamModel();
        m_cameraProcessor.setCameraBehavior(model, state);

//        TumbleObjectCamState tobj = new TumbleObjectCamState(null);
//        tobj.setTargetFocalPoint(new Vector3f(0.0f, 0.0f, 0.0f));
//        tobj.setTargetNeedsUpdate(true);
//        m_cameraProcessor.setCameraBehavior(new TumbleObjectCamModel(), tobj);
        //OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        m_cameraProcessor.setRunInRenderer(true);
        
        AWTInputComponent selectionListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas_SceneRenderWindow, eventMask);
        //SelectionProcessor selector = new SelectionProcessor(selectionListener, wm, camera, camera, width, height, m_cameraProcessor);
        //selector.setRunInRenderer(true);
        
        ProcessorCollectionComponent pcc = new ProcessorCollectionComponent();
        pcc.addProcessor(m_cameraProcessor);
        //pcc.addProcessor(selector);
        camera.addComponent(ProcessorCollectionComponent.class, pcc);
        
        wm.addEntity(camera);
    }
    
    private Node createCameraGraph(WorldManager wm) {
        Node cameraSG = new Node("MyCamera SG");        
        m_cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(m_cameraNode);
        
        return (cameraSG);
    }
    
    private void createTestSpace(WorldManager wm) {
        ColorRGBA color = new ColorRGBA();
        Vector3f center = new Vector3f();

        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // First create the geometry
        center.x = 0.0f; center.y = 25.0f; center.z = 0.0f;
        color.r = 0.0f; color.g = 0.0f; color.b = 1.0f; color.a = 1.0f;
        createSpace("Center", center, buf, color, wm);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DemoBase2 worldTest = new DemoBase2(args);
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) {
        for (int i=0; i<args.length;i++) {
            if (args[i].equals("-fps")) {
                m_desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + m_desiredFrameRate);
                i++;
            }
        }
    }
    
    /**
     * Create all of the Swing windows - and the 3D window
     */
    private void createUI(WorldManager wm) {
        // init GUI components
        initComponents();
        
        // center the frame
        setLocationRelativeTo(null);
        
        // show frame with focus
        canvas_SceneRenderWindow.requestFocusInWindow();
        
        // make it visible
        setVisible(true);
        
        // init progress bar
        runProgressBar(true);
        
        // Add to the wm to set title string later during debugging
        wm.addUserData(JFrame.class, this);
    }
    
    private void setFrame(WorldManager wm) {
        // The Rendering Canvas
        m_renderBuffer = new RenderBuffer(RenderBuffer.Target.ONSCREEN, m_width, m_height);
        wm.getRenderManager().addRenderBuffer(m_renderBuffer);
        canvas_SceneRenderWindow = m_renderBuffer.getCanvas();
        wm.getRenderManager().setFrameRateListener(this, 100);
        jPanel_DisplayWindow.setLayout(new java.awt.GridBagLayout());
    }

    public void currentFramerate(float framerate) {
        jLabel_FPSCounter.setText("FPS: " + framerate);
    }

    public void actionPerformed(ActionEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    /**
     * Sets the GUI to the default Java Look & Feel (Metal)
     */
    public void setLookFeel() {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (javax.swing.UnsupportedLookAndFeelException e) {
            // handle exception
            System.out.println("Unsupported Look & Feel Exception...");
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            // handle exception
            System.out.println("Class Not Found Exception...");
            System.out.println(e.getMessage());
        } catch (InstantiationException e) {
            // handle exception
            System.out.println("Instantiation Exception...");
            System.out.println(e.getMessage());
        } catch (IllegalAccessException e) {
            // handle exception
            System.out.println("Illegal Access Exception...");
            System.out.println(e.getMessage());
        }        
    }
    
    public void runProgressBar(boolean b) {
        if (b) {
            m_bLoading = true;
            jProgressBar_Progress.setIndeterminate(true);
            jLabel_LoadingText.setText("Loading...");
        } else {
            jProgressBar_Progress.setIndeterminate(false);
            jLabel_LoadingText.setText("");
            m_bLoading = false;
        }
    }

    public void openAvatarEditor() {
        m_AvatarOptions = new OptionsGUI();
        m_AvatarOptions.setPScene(m_sceneData.getPScene());
        PNode node = m_sceneData.getPScene().getInstances();
        if (node.getChildrenCount() > 0)
            m_AvatarOptions.setSelectedInstance((PPolygonModelInstance) node.getChild(0));
        m_AvatarOptions.setVisible(true);
    }
    
    public void openNodeExplorer() {
        m_NodeExplorer = new TreeExplorer();
        m_NodeExplorer.setExplorer(m_sceneData);
        m_NodeExplorer.setVisible(true);
    }

    public void openAnimationViewer() {
        m_AnimationViewer = new JFrame();
        JPanel_Animations animPanel = new JPanel_Animations();
        animPanel.setPanel(m_sceneData);
        animPanel.startTimer();
        animPanel.setVisible(true);
        m_AnimationViewer.add(animPanel);
        m_AnimationViewer.pack();
        m_AnimationViewer.setVisible(true);
    }

    public void openServerBrowser() {
        m_ServerBrowser = m_sceneData.openServerBrowserPanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel_MainPanel.add(m_ServerBrowser, gridBagConstraints);
    }

    public void openBasicOptions() {
        JPanel_BasicOptions BasicOptions = new JPanel_BasicOptions();
        BasicOptions.setSceneData(m_sceneData);
        m_sceneData.setCurCamProcessor(m_cameraProcessor);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel_MainPanel.add(BasicOptions, gridBagConstraints);
    }

    public void openEZOptions() {
        JPanel_EZOptions EZOptions = new JPanel_EZOptions();
        EZOptions.setSceneData(m_sceneData);
        m_sceneData.setCurCamProcessor(m_cameraProcessor);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        jPanel_MainPanel.add(EZOptions, gridBagConstraints);

        String url = new String(System.getProperty("user.dir") + "/assets/file/avatars_cau.xml");
        File xml = new File(url);
        EZOptions.readPresetList(xml);
        EZOptions.setTable();
    }

    public void resetOpenTools() {
        if (m_AvatarOptions != null) {
            if (m_AvatarOptions.isVisible()) {
                m_AvatarOptions.dispose();
                openAvatarEditor();
            }
        }

        if (m_NodeExplorer != null) {
            if (m_AvatarOptions.isVisible()) {
                m_NodeExplorer.dispose();
                openNodeExplorer();
            }
        }

        if (m_AnimationViewer != null) {
            if (m_AnimationViewer.isVisible()) {
                m_AnimationViewer.dispose();
                openAnimationViewer();
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////
// CLASS METHODS - END
////////////////////////////////////////////////////////////////////////////////
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel_MainPanel = new javax.swing.JPanel();
        jToolBar_Hotkeys = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jButton2 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jButton3 = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jButton4 = new javax.swing.JButton();
        jPanel_DisplayWindow = new javax.swing.JPanel();
        canvas_SceneRenderWindow = new java.awt.Canvas();
        jToolBar_ProgressBar = new javax.swing.JToolBar();
        jProgressBar_Progress = new javax.swing.JProgressBar();
        jSeparator_VisualText = new javax.swing.JToolBar.Separator();
        jLabel_LoadingText = new javax.swing.JLabel();
        jSeparator_LoadingFPS = new javax.swing.JToolBar.Separator();
        jLabel_FPSCounter = new javax.swing.JLabel();
        jMenuBar_MainMenu = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenu_LoadModels = new javax.swing.JMenu();
        jMenuItem_Avatar = new javax.swing.JMenuItem();
        jMenuItem_Clothes = new javax.swing.JMenuItem();
        jMenuItem_Accessories = new javax.swing.JMenuItem();
        jMenuItem_LoadTextureFile = new javax.swing.JMenuItem();
        jMenuItem_LoadXMLFile = new javax.swing.JMenuItem();
        jMenuItem_SaveXMLFile = new javax.swing.JMenuItem();
        jMenu_URL = new javax.swing.JMenu();
        jMenuItem_LoadModelURL = new javax.swing.JMenuItem();
        jMenuItem_LoadTextureURL = new javax.swing.JMenuItem();
        jMenu_Tools = new javax.swing.JMenu();
        jMenuItem_AvatarEditor = new javax.swing.JMenuItem();
        jMenuItem_NodeExplorer = new javax.swing.JMenuItem();
        jMenuItem_AnimationViewer = new javax.swing.JMenuItem();
        jMenuItem_ShaderCreator = new javax.swing.JMenuItem();
        jMenu_Help = new javax.swing.JMenu();
        jMenuItem_About = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel_MainPanel.setMinimumSize(new java.awt.Dimension(800, 600));
        jPanel_MainPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        jPanel_MainPanel.setLayout(new java.awt.GridBagLayout());

        jToolBar_Hotkeys.setRollover(true);
        jToolBar_Hotkeys.setMaximumSize(new java.awt.Dimension(800, 22));
        jToolBar_Hotkeys.setMinimumSize(new java.awt.Dimension(400, 22));
        jToolBar_Hotkeys.setPreferredSize(new java.awt.Dimension(800, 22));

        jButton1.setText("jButton1");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar_Hotkeys.add(jButton1);
        jToolBar_Hotkeys.add(jSeparator1);

        jButton2.setText("jButton2");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar_Hotkeys.add(jButton2);
        jToolBar_Hotkeys.add(jSeparator2);

        jButton3.setText("jButton3");
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar_Hotkeys.add(jButton3);
        jToolBar_Hotkeys.add(jSeparator3);

        jButton4.setText("jButton4");
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar_Hotkeys.add(jButton4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 520;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel_MainPanel.add(jToolBar_Hotkeys, gridBagConstraints);

        jPanel_DisplayWindow.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel_DisplayWindow.setMaximumSize(new java.awt.Dimension(800, 600));
        jPanel_DisplayWindow.setMinimumSize(new java.awt.Dimension(400, 300));
        jPanel_DisplayWindow.setPreferredSize(new java.awt.Dimension(800, 600));
        jPanel_DisplayWindow.setLayout(new java.awt.GridBagLayout());

        setFrame(m_worldManager);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_DisplayWindow.add(canvas_SceneRenderWindow, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel_MainPanel.add(jPanel_DisplayWindow, gridBagConstraints);

        jToolBar_ProgressBar.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jToolBar_ProgressBar.setFloatable(false);
        jToolBar_ProgressBar.setRollover(true);
        jToolBar_ProgressBar.setMaximumSize(new java.awt.Dimension(800, 24));
        jToolBar_ProgressBar.setPreferredSize(new java.awt.Dimension(800, 24));

        jProgressBar_Progress.setMaximumSize(new java.awt.Dimension(146, 20));
        jProgressBar_Progress.setMinimumSize(new java.awt.Dimension(146, 20));
        jToolBar_ProgressBar.add(jProgressBar_Progress);

        jSeparator_VisualText.setSeparatorSize(new java.awt.Dimension(15, 0));
        jToolBar_ProgressBar.add(jSeparator_VisualText);

        jLabel_LoadingText.setText("jLabel1");
        jToolBar_ProgressBar.add(jLabel_LoadingText);

        jSeparator_LoadingFPS.setSeparatorSize(new java.awt.Dimension(480, 0));
        jToolBar_ProgressBar.add(jSeparator_LoadingFPS);

        jLabel_FPSCounter.setText("FPS: 00.00 ");
        jToolBar_ProgressBar.add(jLabel_FPSCounter);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        jPanel_MainPanel.add(jToolBar_ProgressBar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel_MainPanel, gridBagConstraints);

        jMenuBar_MainMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jMenuBar_MainMenu.setMaximumSize(new java.awt.Dimension(999999, 25));
        jMenuBar_MainMenu.setMinimumSize(new java.awt.Dimension(1024, 25));

        jMenu_File.setText("File");

        jMenu_LoadModels.setText("Load Model");

        jMenuItem_Avatar.setText("Avatar");
        jMenuItem_Avatar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                m_sceneData.loadAvatarDAEFile(true, false, m_base);
                resetOpenTools();
                runProgressBar(false);
            }
        });
        jMenu_LoadModels.add(jMenuItem_Avatar);

        jMenuItem_Clothes.setText("Clothes");
        jMenuItem_Clothes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                m_sceneData.loadSMeshDAEFile(true, false, m_base);
                resetOpenTools();
                runProgressBar(false);
            }
        });
        jMenu_LoadModels.add(jMenuItem_Clothes);

        jMenuItem_Accessories.setText("Accessories");
        jMenuItem_Accessories.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                m_sceneData.loadMeshDAEFile(true, false, m_base);
                resetOpenTools();
                runProgressBar(false);
            }
        });
        jMenu_LoadModels.add(jMenuItem_Accessories);

        jMenu_File.add(jMenu_LoadModels);

        jMenuItem_LoadTextureFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadTextureFile.setText("Load Texture");
        jMenuItem_LoadTextureFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //fileIOPanel1.loadTexFile();
            }
        });
        jMenu_File.add(jMenuItem_LoadTextureFile);

        jMenuItem_LoadXMLFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadXMLFile.setText("Load Configuration");
        jMenuItem_LoadXMLFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //fileIOPanel1.loadConfigFile();
            }
        });
        jMenu_File.add(jMenuItem_LoadXMLFile);

        jMenuItem_SaveXMLFile.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_SaveXMLFile.setText("Save Configuration");
        jMenuItem_SaveXMLFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //fileIOPanel1.saveConfigFile();
            }
        });
        jMenu_File.add(jMenuItem_SaveXMLFile);

        jMenuBar_MainMenu.add(jMenu_File);

        jMenu_URL.setText("URL");

        jMenuItem_LoadModelURL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                //openServerBrowser();
                //m_sceneData.openServerBrowser((JFrame) m_base);
                //openBasicOptions();
                openEZOptions();
                resetOpenTools();
                runProgressBar(false);
            }
        });
        jMenuItem_LoadModelURL.setText("Load Model");
        jMenu_URL.add(jMenuItem_LoadModelURL);

        jMenuItem_LoadTextureURL.setText("Load Texture");
        jMenu_URL.add(jMenuItem_LoadTextureURL);

        jMenuBar_MainMenu.add(jMenu_URL);

        jMenu_Tools.setText("Tools");

        jMenuItem_AvatarEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                openAvatarEditor();
                runProgressBar(false);
            }
        });
        jMenuItem_AvatarEditor.setText("Avatar Editor");
        jMenu_Tools.add(jMenuItem_AvatarEditor);

        jMenuItem_NodeExplorer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                openNodeExplorer();
                runProgressBar(false);
            }
        });
        jMenuItem_NodeExplorer.setText("Node Explorer");
        jMenu_Tools.add(jMenuItem_NodeExplorer);

        jMenuItem_AnimationViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runProgressBar(true);
                openAnimationViewer();
                runProgressBar(false);
            }
        });
        jMenuItem_AnimationViewer.setText("Animation Viewer");
        jMenu_Tools.add(jMenuItem_AnimationViewer);

        jMenuItem_ShaderCreator.setText("Shader Creator");
        jMenu_Tools.add(jMenuItem_ShaderCreator);

        jMenuBar_MainMenu.add(jMenu_Tools);

        jMenu_Help.setText("Help");

        jMenuItem_About.setText("About");
        jMenu_Help.add(jMenuItem_About);

        jMenuBar_MainMenu.add(jMenu_Help);

        setJMenuBar(jMenuBar_MainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas canvas_SceneRenderWindow;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel_FPSCounter;
    private javax.swing.JLabel jLabel_LoadingText;
    private javax.swing.JMenuBar jMenuBar_MainMenu;
    private javax.swing.JMenuItem jMenuItem_About;
    private javax.swing.JMenuItem jMenuItem_Accessories;
    private javax.swing.JMenuItem jMenuItem_AnimationViewer;
    private javax.swing.JMenuItem jMenuItem_Avatar;
    private javax.swing.JMenuItem jMenuItem_AvatarEditor;
    private javax.swing.JMenuItem jMenuItem_Clothes;
    private javax.swing.JMenuItem jMenuItem_LoadModelURL;
    private javax.swing.JMenuItem jMenuItem_LoadTextureFile;
    private javax.swing.JMenuItem jMenuItem_LoadTextureURL;
    private javax.swing.JMenuItem jMenuItem_LoadXMLFile;
    private javax.swing.JMenuItem jMenuItem_NodeExplorer;
    private javax.swing.JMenuItem jMenuItem_SaveXMLFile;
    private javax.swing.JMenuItem jMenuItem_ShaderCreator;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_Help;
    private javax.swing.JMenu jMenu_LoadModels;
    private javax.swing.JMenu jMenu_Tools;
    private javax.swing.JMenu jMenu_URL;
    private javax.swing.JPanel jPanel_DisplayWindow;
    private javax.swing.JPanel jPanel_MainPanel;
    private javax.swing.JProgressBar jProgressBar_Progress;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator_LoadingFPS;
    private javax.swing.JToolBar.Separator jSeparator_VisualText;
    private javax.swing.JToolBar jToolBar_Hotkeys;
    private javax.swing.JToolBar jToolBar_ProgressBar;
    // End of variables declaration//GEN-END:variables

    public String getOS() {
        return System.getProperty("os.name");
    }
    
    public boolean isWindowsOS() {
        return getOS().contains("Windows");
    }
}
