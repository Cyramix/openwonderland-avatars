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
import imi.scene.camera.state.FirstPersonCamState;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModel;
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
import imi.scene.utils.tree.KeyProcessor;
import imi.scene.utils.tree.ScaleResetProcessor;
import imi.scene.utils.tree.TreeTraverser;
import imi.utils.FileUtils;
import imi.utils.PMathUtils;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
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
////////////////////////////////////////////////////////////////////////////////

/**
 * New Demo Base that incorporates the main Avatar Creator as part of the GUI
 * @author  Lou Hayat, Ronald E Dahlgren, Paul Viet Nguyen Truong
 */
public class DemoBase2 extends javax.swing.JFrame implements FrameRateListener, java.awt.event.ActionListener {
    
    // Class Data Members (DEMOBASE2)
    ////////////////////////////////////////////////////////////////////////////
    
    private WorldManager worldManager       = null;
    protected CameraNode cameraNode         = null;
    private int          desiredFrameRate   = 60;
    private int          width              = 800;
    private int          height             = 600;
    private float        aspect             = 800.0f/600.0f;    
    protected FlexibleCameraProcessor m_cameraProcessor = null;
    
    private RenderBuffer renderBuffer       = null;
    // Class Data Members (GUI TOOLS)
    ////////////////////////////////////////////////////////////////////////////
    
    private SceneEssentials sceneData = new SceneEssentials();
    // Scaling Data
    private ArrayList<String> keys = new ArrayList<String>();
    private ArrayList<Vector3f> values = new ArrayList<Vector3f>();
    private HashMap<String, Vector3f> scales = new HashMap<String, Vector3f>();
    // Save Data
    private String avatarName = new String("John Doe");
    private String avatarGender = new String("male");
    // Tools
    private TreeExplorer explorer = null;
    private OptionsGUI options = null;
    // Options
    private boolean bOptions = false;
    private boolean bExplorer = false;
    
    // TEST //
    private HashMap<String, ArrayList<PNode>> groupings = new HashMap<String, ArrayList<PNode>>();
    
    // DemoBase2 Methods
    ////////////////////////////////////////////////////////////////////////////
    
    public DemoBase2(String[] args) {
        //setLookFeel();
        
        addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                dispose();
                
                // TODO: Real cleanup
                System.exit(0);
            }
        });
        
        addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent arg0) {
                if(arg0.getPropertyName().equals("NO OPTIONS")) {
                    java.awt.Frame[] frames = getFrames();
                    frames[0].setSize(((Integer)arg0.getOldValue()).intValue(), ((Integer)arg0.getNewValue()).intValue());
                }
            }
        });
        
        Logger.getLogger("com.jme.scene.state.jogl.shader").setLevel(Level.OFF);
        
        System.out.println("Current Directory: " + System.getProperty("user.dir"));
        
        worldManager = new WorldManager("DemoWorld");
        
        processArgs(args);
        worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);

        
        // add the repository
        worldManager.addUserData(Repository.class, new Repository(worldManager));
        createUI(worldManager);  
        createTestSpace(worldManager);
        createCameraEntity(worldManager);  
        createInputEntity(worldManager); 
        createDemoEntities(worldManager);
        setGlobalLighting(worldManager);
        // Add my shader property panel
//        JFrame testFrame = new JFrame("Shader Test");
//        testFrame.add(new JPanel_ShaderProperties(new VertexDeformer(worldManager), worldManager));
//        testFrame.setSize(300, 600);
//        testFrame.setVisible(true);
    }
    
    /**
     * Override this for simple tests that only require a single scene that do
     * not need to set up the enity for fancy shmancy stuff
     * @param pscene (PScene)
     * @param wm (WorldManager)
     * @param processors (ArrayList<ProcessorComponent>)
     */
    protected void simpleSceneInit(JScene jscene, WorldManager wm, Entity jsentity, ArrayList<ProcessorComponent> processors) {
        //PPolygonModelInstance modelInst = pscene.addModelInstance(createArticulatedModel(1.3f, 1.0f, 2.0f, 10.0f, 3.0f, new PMatrix()), new PMatrix());
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
        
        // called to set up the GUI Stuff
        setGUI(jscene, wm, processors, JSEntity);
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
    
    private void createCube(Vector3f center, float xoff, float yoff, float zoff, 
            ProcessorCollectionComponent pcc, Node parent, WorldManager wm) {
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
        AWTInputComponent eventListener = (AWTInputComponent)worldManager.getInputManager().createInputComponent(canvas_SceneRenderWindow, InputManager.KEY_EVENTS);
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
        CameraComponent cc = wm.getRenderManager().createCameraComponent(cameraSG, cameraNode, 
                width, height, 45.0f, aspect, 0.1f, 1000.0f, true);
        renderBuffer.setCameraComponent(cc);
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
       m_cameraProcessor = new FlexibleCameraProcessor(cameraListener, cameraNode, wm, camera, sky);
        
        FirstPersonCamState state = new FirstPersonCamState();
        FirstPersonCamModel model = new FirstPersonCamModel();
        m_cameraProcessor.setCameraBehavior(model, state);
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
        cameraNode = new CameraNode("MyCamera", null);
        cameraSG.attachChild(cameraNode);
        
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
                desiredFrameRate = Integer.parseInt(args[i+1]);
                System.out.println("DesiredFrameRate: " + desiredFrameRate);
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
        firePropertyChange("NO OPTIONS", 1062, this.getHeight());
        // center the frame
        setLocationRelativeTo(null);
        // show frame with focus
        canvas_SceneRenderWindow.requestFocusInWindow();
        // make it visible
        setVisible(true);
        
        // Add to the wm to set title string later during debugging
        wm.addUserData(JFrame.class, this);
    }
    
    private void setFrame(WorldManager wm) {
        // The Rendering Canvas
        renderBuffer = new RenderBuffer(RenderBuffer.Target.ONSCREEN, width, height);
        wm.getRenderManager().addRenderBuffer(renderBuffer);
        canvas_SceneRenderWindow = renderBuffer.getCanvas();
//        canvas_SceneRenderWindow = wm.getRenderManager().createCanvas(width, height);
//        wm.getRenderManager().setCurrentCanvas(canvas_SceneRenderWindow);
        //canvas_SceneRenderWindow.setVisible(true);
        wm.getRenderManager().setFrameRateListener(this, 100);
        jPanel_DisplayWindow.setLayout(new java.awt.GridBagLayout());
    }

    public void currentFramerate(float framerate) {
        jLabel_FPSCounter.setText("Avatar Display Window -- FPS: " + framerate);
    }

    public void actionPerformed(ActionEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Options GUI Methods
    ////////////////////////////////////////////////////////////////////////////

    // Accessors
    public HashMap<String, Vector3f> getScales() {
        return scales;
    }
    // Mutators
    public void setAvatarName(String name) {
        avatarName = name;
    }

    public void setAvatarGender(String gender) {
        avatarGender = gender;
    }

    // Helper Functions
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
    
    /**
     * Method called by other frames to set the set the scene information that
     * the UI uses for manipulation
     * @param jscene (JScene), worldmanager WorldManager,
     *        hiprocessors (ArrayList<ProcessorComponent>), jentity (Entity)
     */
    public void setGUI(JScene jscene, WorldManager worldmanager, ArrayList<ProcessorComponent> hiprocessors, Entity jentity) {
        sceneData.setSceneData(jscene, jscene.getPScene(), jentity, worldmanager, hiprocessors);

        // Wait until the assets are available for use
        while (sceneData.getPScene().getAssetWaitingList().size() > 0) {
            Thread.yield();
            //System.out.println("Waiting to get assets...");
        }

        setDefault();
        jPanel_Animations1.startTimer();
    }

    /**
     * Resets the AvatarEditor to default values and component positions
     */
    public void setDefault() {
        jPanel_Animations1.setPScene(sceneData.getPScene());
        resetDataMembers();
        resetGUI(0);
        //jPanel_ShaderLoader1.setPanel(sceneData.getPScene());
        jPanel_ModelRotation1.setPanel(sceneData.getPScene(), jPanel_Animations1.getSelectedModelInstanceNode());
        fileIOPanel1.setPanel(sceneData, jPanel_ModelRotation1, jPanel_Animations1);
    }

    /**
     * Collect scaling data from the model
     */
    public void setLocalScales() {
        scales.clear();
        keys.clear();
        values.clear();
        // Retrieve new scale keys
        if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") != null) {
            KeyProcessor keyProc = new KeyProcessor();
            TreeTraverser.breadthFirst(sceneData.getPScene().getInstances().getChild(0).getChild(0).getChild(0).getChild(0), keyProc);
            keys = keyProc.getKeys();
            values = keyProc.getValues();
            // Set the scale data to default values
            for (int i = 0; i < keys.size(); i++) {
                scales.put(keys.get(i), values.get(i));
            }
        }
    }
    
    /**
     * Resets the UI class data members to default values
     */
    public void resetDataMembers() {
        if (sceneData.getPScene() == null) {
            System.out.println("==================================================================");
            System.out.println("UI has not been initialized... please call setGUI from main window");
            System.out.println("==================================================================");
            return;
        }
        // Clear out old scale data
        resetLocalScales();
    }

    /**
     * Resets the local scale variables to default 1:1:1 values
     */
    public void resetLocalScales() {
        if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") == null) {
            System.out.println("No joints loaded yet");
            return;
        }
        Vector3f normalVector = new Vector3f(1.0f, 1.0f, 1.0f);
        scales.clear();
        keys.clear();
        values.clear();
        // Retrieve new scale keys
        KeyProcessor keyProc = new KeyProcessor();
        if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") != null) {
            TreeTraverser.breadthFirst(sceneData.getPScene().getInstances().findChild("m_TransformHierarchy").getChild(0), keyProc);
            keys = keyProc.getKeys();
            // Set the scale data to default values
            for (int i = 0; i < keys.size(); i++) {
                scales.put(keys.get(i), normalVector);
                values.add(normalVector);
            }
        }
    }

    /**
     * Resets the UI to default positions
     */
    public void resetGUI(int type) {
        if (type == 0) {
            // Closes all tools options
            if(options != null) {
                options.dispose();
                bOptions = false;
            }
            if(explorer != null) {
                explorer.dispose();
                bExplorer = false;
            }
        }
        jPanel_Animations1.resetPanel();
        jPanel_ModelRotation1.resetPanel();
    }

    /**
     * Resets the model scale to default 1:1:1 scaling
     */
    public void resetModelScales() {
        if (sceneData.getPScene().getInstances().getChildrenCount() > 0) {
            sceneData.getPScene().getInstances().getChild(0).getTransform().getLocalMatrix(true).setScale(new Vector3f(1.0f, 1.0f, 1.0f));
            if (sceneData.getPScene().getInstances().findChild("m_TransformHierarchy") != null) {
                TreeTraverser.depthFirstPre(sceneData.getPScene().getInstances().getChild(0).getChild(0).getChild(0).getChild(0), new ScaleResetProcessor());
            }
        }
    }
    
    public void OpenOptions() {
        if (bOptions == false) {
            options = new OptionsGUI();
            options.setPScene(sceneData.getPScene());
            options.setSelectedInstance(jPanel_Animations1.getSelectedModelInstance());
            options.initValues();
            options.setAvatarName(avatarName);
            options.setAvatarGender(avatarGender);
            options.setVisible(true);
            bOptions = true;
        } else {
            options.dispose();
            bOptions = false;
        }
    }

    public void OpenExplorer() {
        if (bExplorer == false) {
            explorer = new TreeExplorer();
            explorer.setExplorer(sceneData);
            explorer.setVisible(true);
            explorer.expandTree();
            bExplorer = true;
        } else {
            explorer.nodeUnselect();
            explorer.dispose();
            bExplorer = false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_DisplayWindow = new javax.swing.JPanel();
        canvas_SceneRenderWindow = new java.awt.Canvas();
        jLabel_FPSCounter = new javax.swing.JLabel();
        jToolBar_AvatarTools = new javax.swing.JToolBar();
        jToolBar_AvatarOptions = new javax.swing.JToolBar();
        jButton_AvatarOptions = new javax.swing.JButton();
        jToolBar11 = new javax.swing.JToolBar();
        jButton_ExplorerOptions = new javax.swing.JButton();
        jPanel_ModelRotation1 = new imi.gui.JPanel_ModelRotation();
        jPanel_Animations1 = new imi.gui.JPanel_Animations();
        fileIOPanel1 = new imi.gui.FileIOPanel();
        jMenuBar_MainMenu = new javax.swing.JMenuBar();
        jMenu_File = new javax.swing.JMenu();
        jMenuItem_LoadModel = new javax.swing.JMenuItem();
        jMenuItem_LoadTexture = new javax.swing.JMenuItem();
        jMenuItem_LoadXML = new javax.swing.JMenuItem();
        jMenuItem_SaveXML = new javax.swing.JMenuItem();
        jMenu_Help = new javax.swing.JMenu();
        jMenuItem_About = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("frame_Main"); // NOI18N
        setResizable(false);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        jPanel_DisplayWindow.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        setFrame(worldManager);

        jLabel_FPSCounter.setText("Avatar Display Window -- FPS: ");

        org.jdesktop.layout.GroupLayout jPanel_DisplayWindowLayout = new org.jdesktop.layout.GroupLayout(jPanel_DisplayWindow);
        jPanel_DisplayWindow.setLayout(jPanel_DisplayWindowLayout);
        jPanel_DisplayWindowLayout.setHorizontalGroup(
            jPanel_DisplayWindowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_DisplayWindowLayout.createSequentialGroup()
                .add(295, 295, 295)
                .add(jLabel_FPSCounter))
            .add(canvas_SceneRenderWindow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 800, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );
        jPanel_DisplayWindowLayout.setVerticalGroup(
            jPanel_DisplayWindowLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel_DisplayWindowLayout.createSequentialGroup()
                .add(jLabel_FPSCounter)
                .add(5, 5, 5)
                .add(canvas_SceneRenderWindow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 600, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jToolBar_AvatarTools.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED), "Avatar Tools", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(0, 0, 255))); // NOI18N
        jToolBar_AvatarTools.setOrientation(1);
        jToolBar_AvatarTools.setRollover(true);

        jToolBar_AvatarOptions.setFloatable(false);
        jToolBar_AvatarOptions.setRollover(true);
        jToolBar_AvatarOptions.setMaximumSize(new java.awt.Dimension(230, 26));
        jToolBar_AvatarOptions.setPreferredSize(new java.awt.Dimension(230, 25));

        jButton_AvatarOptions.setText("Avatar Options");
        jButton_AvatarOptions.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_AvatarOptions.setFocusable(false);
        jButton_AvatarOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_AvatarOptions.setMaximumSize(new java.awt.Dimension(230, 25));
        jButton_AvatarOptions.setMinimumSize(new java.awt.Dimension(100, 25));
        jButton_AvatarOptions.setPreferredSize(new java.awt.Dimension(100, 25));
        jButton_AvatarOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_AvatarOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenOptions();
            }
        });
        jToolBar_AvatarOptions.add(jButton_AvatarOptions);

        jToolBar_AvatarTools.add(jToolBar_AvatarOptions);

        jToolBar11.setFloatable(false);
        jToolBar11.setRollover(true);
        jToolBar11.setMaximumSize(new java.awt.Dimension(230, 26));
        jToolBar11.setPreferredSize(new java.awt.Dimension(230, 25));

        jButton_ExplorerOptions.setText("Explorer Options");
        jButton_ExplorerOptions.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton_ExplorerOptions.setFocusable(false);
        jButton_ExplorerOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton_ExplorerOptions.setMaximumSize(new java.awt.Dimension(230, 25));
        jButton_ExplorerOptions.setMinimumSize(new java.awt.Dimension(100, 25));
        jButton_ExplorerOptions.setPreferredSize(new java.awt.Dimension(100, 25));
        jButton_ExplorerOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton_ExplorerOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenExplorer();
            }
        });
        jToolBar11.add(jButton_ExplorerOptions);

        jToolBar_AvatarTools.add(jToolBar11);

        jMenuBar_MainMenu.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jMenuBar_MainMenu.setMaximumSize(new java.awt.Dimension(999999, 25));
        jMenuBar_MainMenu.setMinimumSize(new java.awt.Dimension(1024, 25));
        jMenuBar_MainMenu.setPreferredSize(new java.awt.Dimension(1024, 25));

        jMenu_File.setText("File");

        jMenuItem_LoadModel.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadModel.setText("Load Model");
        jMenuItem_LoadModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileIOPanel1.loadModelFile();
            }
        });
        jMenu_File.add(jMenuItem_LoadModel);

        jMenuItem_LoadTexture.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadTexture.setText("Load Texture");
        jMenuItem_LoadTexture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileIOPanel1.loadTexFile();
            }
        });
        jMenu_File.add(jMenuItem_LoadTexture);

        jMenuItem_LoadXML.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_LoadXML.setText("Load Configuration");
        jMenuItem_LoadXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileIOPanel1.loadConfigFile();
            }
        });
        jMenu_File.add(jMenuItem_LoadXML);

        jMenuItem_SaveXML.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem_SaveXML.setText("Save Configuration");
        jMenuItem_SaveXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileIOPanel1.saveConfigFile();
            }
        });
        jMenu_File.add(jMenuItem_SaveXML);

        jMenuBar_MainMenu.add(jMenu_File);

        jMenu_Help.setText("Help");

        jMenuItem_About.setText("About");
        jMenu_Help.add(jMenuItem_About);

        jMenuBar_MainMenu.add(jMenu_Help);

        setJMenuBar(jMenuBar_MainMenu);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(0, 0, 0)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(fileIOPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jToolBar_AvatarTools, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 230, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jPanel_ModelRotation1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 230, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jPanel_Animations1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(0, 0, 0)
                .add(jPanel_DisplayWindow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(fileIOPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jToolBar_AvatarTools, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, 0)
                .add(jPanel_ModelRotation1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 175, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel_Animations1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 223, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(jPanel_DisplayWindow, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

/**
 * If the checkbox is selected it opens up the Options window for editing the
 * avatar otherwise it will dispose of the options window if open
 * @param evt (ItemEvent)
 */
/**
 * If the checkbox is selected it opens up the PScene Explorer window otherwise
 * it will dispose of the explorer window if open
 * @param evt (ItemEvent)
 */
/**
 * This should be a quick keyshortuct for the GUI but no events are being captured ???
 * TODO: Make this work.
 * @param evt
 */
private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
    if(evt.getKeyCode() == KeyEvent.VK_I)
        jPanel_Animations1.mediaFunction(0);
    if(evt.getKeyCode() == KeyEvent.VK_O)
        jPanel_Animations1.mediaFunction(1);
    if(evt.getKeyCode() == KeyEvent.VK_P)
        jPanel_Animations1.mediaFunction(2);
}//GEN-LAST:event_formKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas canvas_SceneRenderWindow;
    private imi.gui.FileIOPanel fileIOPanel1;
    private javax.swing.JButton jButton_AvatarOptions;
    private javax.swing.JButton jButton_ExplorerOptions;
    private javax.swing.JLabel jLabel_FPSCounter;
    private javax.swing.JMenuBar jMenuBar_MainMenu;
    private javax.swing.JMenuItem jMenuItem_About;
    private javax.swing.JMenuItem jMenuItem_LoadModel;
    private javax.swing.JMenuItem jMenuItem_LoadTexture;
    private javax.swing.JMenuItem jMenuItem_LoadXML;
    private javax.swing.JMenuItem jMenuItem_SaveXML;
    private javax.swing.JMenu jMenu_File;
    private javax.swing.JMenu jMenu_Help;
    private imi.gui.JPanel_Animations jPanel_Animations1;
    private javax.swing.JPanel jPanel_DisplayWindow;
    private imi.gui.JPanel_ModelRotation jPanel_ModelRotation1;
    private javax.swing.JToolBar jToolBar11;
    private javax.swing.JToolBar jToolBar_AvatarOptions;
    private javax.swing.JToolBar jToolBar_AvatarTools;
    // End of variables declaration//GEN-END:variables

}
