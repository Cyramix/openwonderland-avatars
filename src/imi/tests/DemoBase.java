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
import com.jme.light.DirectionalLight;
import com.jme.light.LightNode;
import com.jme.light.PointLight;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import com.jme.scene.shape.Box;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.MaterialState.MaterialFace;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.util.TextureManager;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.scene.JScene;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModel;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.polygon.PPolygon;
import imi.scene.polygonmodel.parts.skinned.PBoneIndices;
import imi.scene.polygonmodel.parts.skinned.PPolygonSkinnedVertexIndices;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.SkinnedMeshJoint;
import imi.scene.processors.JSceneAWTEventProcessor;
import imi.scene.utils.PMeshUtils;
import imi.utils.FileUtils;
import imi.utils.PMathUtils;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
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
import org.jdesktop.mtgame.processor.RotationProcessor;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PScene;
import imi.scene.processors.CameraProcessor;
import imi.scene.processors.JSceneEventProcessor;
import org.jdesktop.mtgame.RenderBuffer;


/**
 * A World test application
 * 
 * Demo Base - Do not use this file! Copy Demo.java for your own test file...
 */
public class DemoBase 
{
    private WorldManager worldManager       = null;
    protected CameraNode   cameraNode         = null;
    private int          desiredFrameRate   = 60;
    private int          width              = 800;
    private int          height             = 600;
    private float        aspect             = 800.0f/600.0f;
    
    private Entity m_jsceneEntity = null; // Maintained for lighting operations
    
    protected CameraProcessor m_cameraProcessor = null;
    
    
    public DemoBase(String[] args) 
    {
        System.out.println("Current Directory: " + System.getProperty("user.dir"));
        
        worldManager = new WorldManager("DemoWorld");
        
        processArgs(args);
        worldManager.getRenderManager().setDesiredFrameRate(desiredFrameRate);
        
        // shutup jmonkey!
        Logger.getLogger("com.jme").setLevel(Level.SEVERE);
        Logger.getLogger("com.jmex").setLevel(Level.SEVERE);
        Logger.getLogger("org.collada").setLevel(Level.SEVERE);
        Logger.getLogger("imi.loaders.collada").setLevel(Level.SEVERE);
        Logger.getLogger("com.jme.scene.state.jogl.shader").setLevel(Level.OFF);
        
        // add the repository
        worldManager.addUserData(Repository.class, new Repository(worldManager));
        createUI(worldManager);  
        createTestSpace(worldManager);
        createCameraEntity(worldManager);  
        createInputEntity(worldManager); 
        createDemoEntities(worldManager);
        setGlobalLighting(worldManager);
    }
    
    // Override this for simple tests that only require a single scene
    // that do not need to set up the enity for fancy shmancy stuff
    protected void simpleSceneInit(PScene pscene, WorldManager wm, ArrayList<ProcessorComponent> processors) 
    {
        //PPolygonModelInstance modelInst = pscene.addModelInstance(createArticulatedModel(1.3f, 1.0f, 2.0f, 10.0f, 3.0f, new PMatrix()), new PMatrix());
    }
    
    
    // Override this if you wish to have multiple entities or if you wish
    // to setup your entity for fancy shmanciness
    protected void createDemoEntities(WorldManager wm) 
    {
        // The procedural scene graph
        PScene pscene = new PScene("PScene test", wm);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // Initialize the scene
        simpleSceneInit(pscene, wm, processors);
        
        // The glue between JME and pscene
        JScene jscene = new JScene(pscene);
        
        // Set this jscene to be the "selected" one for IMI input handling
        ((JSceneEventProcessor)wm.getUserData(JSceneEventProcessor.class)).setJScene(jscene); 
       
        // Create entity
        m_jsceneEntity = new Entity("Entity for a graph test");
        
        // Create a scene component and set the root to our jscene
        RenderComponent sc = wm.getRenderManager().createRenderComponent(jscene);
        sc.setLightingEnabled(true);
        // Add the scene component with our jscene to the entity
        m_jsceneEntity.addComponent(RenderComponent.class, sc);
   
        // Add our two processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));
        
        // Add the processor collection component to the entity
        m_jsceneEntity.addComponent(ProcessorCollectionComponent.class, processorCollection);
        
        // Add the entity to the world manager
        wm.addEntity(m_jsceneEntity);    
        
        
        // Use default render states
        setDefaultRenderStates(jscene, wm);
    }
    
    /**
     * Sets up several default render states including the lighting
     * @param jscene
     * @param wm
     */
    public void setDefaultRenderStates(JScene jscene, WorldManager wm) 
    {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setAmbient(new ColorRGBA(0.1f, 0.1f, 0.1f, 1.0f));
        matState.setDiffuse(ColorRGBA.white);
        matState.setEmissive(ColorRGBA.black);
        matState.setMaterialFace(MaterialFace.FrontAndBack);
        
        // Cull State
        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);      
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        
        // Wireframe State
        WireframeState ws = (WireframeState) wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(true);
        
        // Push 'em down the pipe
        jscene.setRenderState(matState);
        jscene.setRenderState(buf);
        jscene.setRenderState(cs);
        jscene.setRenderState(ws);
        jscene.updateRenderState();
    }
    
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
    
   public void createSpace(String name, Vector3f center, ZBufferState buf,
            ColorRGBA color, WorldManager wm) {
        MaterialState matState = null;

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
    
    public PPolygonModel createSphereModel(float radius, ColorRGBA color, PMatrix origin) 
    {   
        PPolygonModel resultModel = new PPolygonModel("Sphere Model");
        resultModel.getTransform().setLocalMatrix(origin);
                
        PMeshMaterial geometryMaterial = new PMeshMaterial();
        geometryMaterial.setColorMaterial(ColorMaterial.Diffuse); // Make the vert colors affect diffuse coloring
        geometryMaterial.setDiffuse(color);
        
        PPolygonMesh sphereMesh = PMeshUtils.createSphere("Sphere", Vector3f.ZERO, radius, 6, 6, color);
        sphereMesh.setMaterial(geometryMaterial);
        resultModel.addChild(sphereMesh);
                
        resultModel.buildFlattenedHierarchy(); // This method updates all the world transforms for the hierarchy.
        resultModel.submitGeometry(new PPolygonTriMeshAssembler());
                
        return resultModel;
    }
    
    public PPolygonModelInstance createSphereEntity(float radius, ColorRGBA color, PMatrix origin, WorldManager wm)
    {
        // The procedural scene graph
        PScene pscene = new PScene("Sphere PScene", wm);
        
        // The collection of processors for this entity
        //ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // Initialize the scene
        SharedAsset modelAsset = new SharedAsset(pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D_Mesh, ""));
        PMeshMaterial geometryMaterial = new PMeshMaterial();
        geometryMaterial.setColorMaterial(ColorMaterial.Diffuse); // Make the vert colors affect diffuse coloring
        geometryMaterial.setDiffuse(ColorRGBA.white);
        PPolygonMesh sphereMesh = PMeshUtils.createSphere("Sphere", Vector3f.ZERO, radius, 6, 6, color);
        sphereMesh.setMaterial(geometryMaterial);
        sphereMesh.submit(new PPolygonTriMeshAssembler());
        modelAsset.setAssetData(sphereMesh);
        PPolygonModelInstance modelInst = pscene.addModelInstance(modelAsset, origin);
        
        // The glue between JME and pscene
        JScene jscene = new JScene(pscene);
        
        // Use default render states
        setDefaultRenderStates(jscene, wm);
               
        // Create entity
        Entity JSEntity = new Entity("Sphere Entity");
        
        // Create a scene component and set the root to our jscene
        RenderComponent sc = wm.getRenderManager().createRenderComponent(jscene);
        
        // Add the scene component with our jscene to the entity
        JSEntity.addComponent(RenderComponent.class, sc);
        
        // Add our two processors to a collection component
//        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
//        for (int i = 0; i < processors.size(); i++)
//            processorCollection.addProcessor(processors.get(i));
        
        // Add the processor collection component to the entity
        //JSEntity.addComponent(ProcessorCollectionComponent.class, processorCollection);
        
        // Add the entity to the world manager
        wm.addEntity(JSEntity); 
        
        return modelInst;
    }
    
    /**
     * This method procedurally generates a skinned model of a "tower".
     * Bones are generated per floor.
     * @param numberOfLevels How many floors (must be three or more)
     * @param floorHeight The space between floors
     * @param floorWidth The width (and depth) of floors
     * @return The completed skeleton!
     */
    public PPolygonSkinnedMesh createSkinnedModel(int numberOfLevels, float floorHeight, float floorWidth, Vector3f modelSpaceOffset, String name)
    {
        // increment to account for the roof
        numberOfLevels++;
        
        if (numberOfLevels < 3) // If less than 3 floors was requested, we cannot properly generate the model
            return null;
                       
        PPolygonSkinnedMesh mesh = new PPolygonSkinnedMesh(name);
        //mesh.setSmoothNormals(true); // Just to be pretty 
        
        mesh.beginBatch(); // Always call beginBatch when messing with a mesh's geometry. The accompanying endBatch call comes later
        
        // Grab a reference to flesh out the transform hierarchy,
        // the model (not instance) contains the transform hierarchy for the bind pose
        // which will be referenced by all instances that use this geometry as a base.
        PNode bindPose = new PNode(new PTransform());
        
        SkinnedMeshJoint [] bones = new SkinnedMeshJoint[numberOfLevels]; // Allocate the array for our bones (made of PJoints)
        // the base!                       Name,Parent,Child(ren),Transform
        bones[0]   = new SkinnedMeshJoint("base", new PTransform());
        // Attach this newly created bone to the base of the skeleton
        bindPose.addChild(bones[0]);
        
        for (int i = 1; i < numberOfLevels; i++) // For each level (start at one so the following logic works)
        {
            // Generate a bone at each floor
            bones[i]   = new SkinnedMeshJoint("middle " + i, new PTransform(new PMatrix(new Vector3f(0,floorHeight,0))));
            // Add this to the previously created floor
            bones[i-1].addChild(bones[i]);
        }
        
        // build world matrices
        bindPose.buildFlattenedHierarchy();
        
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
                // tack on offset
                vertPosition.addLocal(modelSpaceOffset);

                skinnedVertArray[boneIndex][i].m_PositionIndex    = mesh.getPosition(vertPosition); // Add our position to the mesh and retrieve the index
                skinnedVertArray[boneIndex][i].m_NormalIndex      = mesh.getNormal(Vector3f.UNIT_Z); // These normals are generated later, so give them some default; obviously wrong, not unit length ;)
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
        
        return mesh;
    }
    
    public void createInputEntity(WorldManager wm)
    {
        // Create input entity
        Entity InputEntity = new Entity("Input Entity");
        // Create event listener
        Canvas canvas = ((SwingFrame)wm.getUserData(JFrame.class)).getRenderBuffer().getCanvas();
        AWTInputComponent eventListener = (AWTInputComponent)worldManager.getInputManager().createInputComponent(canvas, InputManager.KEY_EVENTS);
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

    private Texture loadSkyboxTexture(String filePath)
    {
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
        
        RenderBuffer renderBuffer = ((SwingFrame)wm.getUserData(JFrame.class)).getRenderBuffer();
        
        camera.addComponent(CameraComponent.class, cc);
        renderBuffer.setCameraComponent(cc);
        //////////////////////////////////////////////////////////////////////
        // Skybox
        SkyBox sky = new SkyBox("skybox", 10.0f, 10.0f, 10.0f, wm);
        
        sky.setTexture(SkyBox.NORTH,   loadSkyboxTexture("assets/textures/skybox/pos_z.bmp")); // north
        sky.setTexture(SkyBox.EAST,    loadSkyboxTexture("assets/textures/skybox/pos_x.bmp")); // south
        sky.setTexture(SkyBox.SOUTH,   loadSkyboxTexture("assets/textures/skybox/neg_z.bmp")); // east
        sky.setTexture(SkyBox.WEST,    loadSkyboxTexture("assets/textures/skybox/neg_x.bmp")); // west
        sky.setTexture(SkyBox.DOWN,    loadSkyboxTexture("assets/textures/skybox/neg_y.bmp")); // up
        sky.setTexture(SkyBox.UP,      loadSkyboxTexture("assets/textures/skybox/pos_y.bmp")); // down
        //
        
        RenderComponent sc2 = wm.getRenderManager().createRenderComponent(sky);
        camera.addComponent(RenderComponent.class, sc2);
        //////////////////////////////////////////////////////////////////////

        // Create the input listener and process for the camera
        int eventMask = InputManager.KEY_EVENTS | InputManager.MOUSE_EVENTS;
        Canvas canvas = renderBuffer.getCanvas();
        AWTInputComponent cameraListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);
        m_cameraProcessor = new CameraProcessor(cameraListener, cameraNode, wm, camera, sky);
        //OrbitCameraProcessor eventProcessor = new OrbitCameraProcessor(cameraListener, cameraNode, wm, camera);
        m_cameraProcessor.setRunInRenderer(true);
        
        AWTInputComponent selectionListener = (AWTInputComponent)wm.getInputManager().createInputComponent(canvas, eventMask);        
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
    
    private void createTestSpace(WorldManager wm) 
    {
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
    public static void main(String[] args) 
    {
        DemoBase worldTest = new DemoBase(args);
    }
    
    /**
     * Process any command line args
     */
    private void processArgs(String[] args) 
    {
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
        SwingFrame frame = new SwingFrame(wm);
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame with focus
        frame.canvas.requestFocusInWindow();
        
        frame.setVisible(true);
        
        // Add to the wm to set title string later during debugging
        wm.addUserData(JFrame.class, frame);
    }
    
    public class SwingFrame extends JFrame implements FrameRateListener, ActionListener {

        JPanel contentPane;
        JPanel menuPanel = new JPanel();
        JPanel canvasPanel = new JPanel();
        JPanel optionsPanel = new JPanel();
        JPanel statusPanel = new JPanel();
        Canvas canvas = null;
        JLabel fpsLabel = new JLabel("FPS: ");
        RenderBuffer m_renderBuffer = null;
        JToggleButton coordButton = new JToggleButton("Coords", true);
        JToggleButton gridButton = new JToggleButton("Grid", true);
        JMenuItem loadItem = null;
        JMenuItem exitItem = null;
        JMenuItem createTeapotItem = null;


        // Construct the frame
        public SwingFrame(WorldManager wm) {
            addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO: Real cleanup
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            
            // The Menu Bar
            menuPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            JMenuBar menuBar = new JMenuBar();
            
            // File Menu
            JMenu fileMenu = new JMenu("File");
            exitItem = new JMenuItem("Exit");
            exitItem.addActionListener(this);
            loadItem = new JMenuItem("Load");
            loadItem.addActionListener(this);
            fileMenu.add(loadItem);
            fileMenu.add(exitItem);
            menuBar.add(fileMenu);
            
            // Create Menu
            JMenu createMenu = new JMenu("Create");
            createTeapotItem = new JMenuItem("Teapot");
            createTeapotItem.addActionListener(this);
            createMenu.add(createTeapotItem);
            menuBar.add(createMenu);
            
            menuPanel.add(menuBar);
            contentPane.add(menuPanel, BorderLayout.NORTH);
            
            // The Rendering Canvas
            m_renderBuffer = new RenderBuffer(RenderBuffer.Target.ONSCREEN, width, height);
            wm.getRenderManager().addRenderBuffer(m_renderBuffer);
            canvas = m_renderBuffer.getCanvas();
            //canvas = wm.getRenderManager().createCanvas(width, height);
            //wm.getRenderManager().setCurrentCanvas(canvas);
            canvas.setVisible(true);
            //canvas.setBounds(0, 0, width, height);
            wm.getRenderManager().setFrameRateListener(this, 100);
            canvasPanel.setLayout(new GridBagLayout());           
            canvasPanel.add(canvas);
            contentPane.add(canvasPanel, BorderLayout.CENTER);
            
            // The options panel
            optionsPanel.setLayout(new GridBagLayout());
            
            coordButton.addActionListener(this);
            optionsPanel.add(coordButton);
          
            gridButton.addActionListener(this);
            optionsPanel.add(gridButton);
            
            contentPane.add(optionsPanel, BorderLayout.WEST);
            
            // The status panel
            statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(fpsLabel);
            contentPane.add(statusPanel, BorderLayout.SOUTH);

            pack();
        }
        
        /**
         * Listen for frame rate updates
         */
        public void currentFramerate(float framerate) {
            fpsLabel.setText("FPS: " + framerate);
        }

        public void actionPerformed(ActionEvent e)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public RenderBuffer getRenderBuffer()
        {
            return m_renderBuffer;
        }
    }
}
