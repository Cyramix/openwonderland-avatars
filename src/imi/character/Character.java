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
package imi.character;

import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.shape.Sphere;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.character.objects.ObjectCollection;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.TransitionObject;
import imi.loaders.Instruction;
import imi.loaders.Instruction.InstructionType;
import imi.loaders.InstructionProcessor;
import imi.loaders.PPolygonTriMeshAssembler;
import imi.loaders.collada.Collada;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.Repository;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.loaders.repository.SharedAssetPlaceHolder;
import imi.scene.JScene;
import imi.scene.PJoint;
import imi.scene.PMatrix;
import imi.scene.PNode;
import imi.scene.PScene;
import imi.scene.animation.AnimationComponent;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationListener;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.animation.AnimationState;
import imi.scene.animation.TransitionCommand;
import imi.scene.animation.TransitionQueue;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.CharacterAnimationProcessor;
import imi.scene.processors.CharacterProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.shader.AbstractShaderProgram;
import imi.scene.shader.NoSuchPropertyException;
import imi.scene.shader.ShaderProperty;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.programs.ClothingShader;
import imi.scene.shader.programs.EyeballShader;
import imi.scene.shader.programs.FleshShader;
import imi.scene.shader.programs.SimpleTNLWithAmbient;
import imi.scene.utils.PMeshUtils;
import imi.scene.utils.tree.NodeProcessor;
import imi.scene.utils.tree.SerializationHelper;
import imi.scene.utils.tree.TreeTraverser;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlJointModification;
import imi.serialization.xml.bindings.xmlMaterial;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javolution.util.FastList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;



/**import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;

 * This class represents the high level avatar. It provides methods for performing
 * tasks that are character related.
 * @author Lou Hayt
 */
public abstract class Character extends Entity implements SpatialObject, AnimationListener
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(Character.class.getName());

    /**
     * Maps to game triggers from VK_ key IDs that are forwarded from the input
     * manager. This defines which triggers react to what keyboard input.
     * <KeyID, TriggerID>
     */
    protected Hashtable<Integer, Integer>   m_keyBindings           = new Hashtable<Integer, Integer>();
    protected GameContext                   m_context               = null;
    protected CharacterAttributes           m_attributes            = null;
    protected HashMap<String, GameContext>  m_registry              = new HashMap<String, GameContext>();
    protected WorldManager                  m_wm                    = null;
    protected PScene                        m_pscene                = null;
    protected JScene                        m_jscene                = null;
    protected PPolygonModelInstance         m_modelInst             = null;
    protected SkeletonNode                  m_skeleton              = null;
    protected PPolygonMeshInstance          m_mesh                  = null;
    protected ObjectCollection              m_objectCollection      = null;
    protected CharacterAnimationProcessor   m_AnimationProcessor    = null;
    protected CharacterProcessor            m_characterProcessor    = null;
    protected TransitionQueue               m_facialAnimationQ      = null;
    protected CharacterEyes                 m_eyes                  = null;
    protected VerletArm                     m_rightArm              = null;
    protected VerletArm                     m_leftArm               = null;
    private VerletSkeletonFlatteningManipulator m_skeletonManipulator   = null;
    private boolean                             m_initialized           = false;
    
    private int                             m_defaultFacePose       = 4;
    private float                           m_defaultFacePoseTiming = 0.1f;

    /**
     * Sets up the mtgame entity 
     * @param attributes
     * @param wm
     */
    public Character(CharacterAttributes attributes, WorldManager wm)
    {
        this(attributes, wm, true);
    }

    /**
     * Sets up the mtgame entity
     * @param attributes
     * @param wm
     * @param addEntity determines if the entity is added to the world manager in the constructor
     */
    public Character(CharacterAttributes attributes, WorldManager wm, boolean addEntity)
    {
        super(attributes.getName());
        commonConstructionCode(wm, attributes, addEntity, null);
    }

    /**
     * Construct a new character using the specified configuration file and world
     * manager.
     * @param configurationFile
     * @param wm
     */
    public Character(URL configurationFile, WorldManager wm)
    {
        super("InterimName");
        xmlCharacter characterDOM = null;
        CharacterAttributes loadedAttributes = null;

        try {
            final JAXBContext context = JAXBContext.newInstance("imi.serialization.xml.bindings");
            final Unmarshaller m = context.createUnmarshaller();

            InputStream is = configurationFile.openConnection().getInputStream();
            Object characterObj = m.unmarshal( is );

            if (characterObj instanceof xmlCharacter)
            {
                characterDOM = (xmlCharacter)characterObj;
                xmlCharacterAttributes xmlAttributes = characterDOM.getAttributes();
                loadedAttributes = new CharacterAttributes(xmlAttributes);
            }
            else
                logger.log(Level.SEVERE, "JAXB somehow parsed the file and made some other object: " + characterObj.toString());
        }
        catch (JAXBException ex) {
            logger.log(Level.SEVERE, "Failed to parse the file! " + ex.getMessage());
            logger.log(Level.SEVERE, ex.getErrorCode() + " : " + ex.getLocalizedMessage() + " : " + ex.toString());
            ex.printStackTrace();

        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to open InputStream to " +
                                    configurationFile.toString() + "! " + ex.getMessage());
        }
        commonConstructionCode(wm, loadedAttributes, true, characterDOM);
    }

    private void commonConstructionCode(WorldManager wm, CharacterAttributes attributes, boolean addEntity, xmlCharacter characterDOM)
    {
        m_wm = wm;
        // Initialize key bindings
        initKeyBindings();
        // The procedural scene graph
        m_pscene = new PScene(attributes.getName(), m_wm);
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        // Initialize the attributes
        m_attributes = attributes;
        // Apply the attributes file; this also initializes the skeleton
        applyAttributes();
        // Initialize the scene, this adds the skeleton to the scene graph
        initScene(processors);
        // The glue between JME and pscene
        m_jscene = new JScene(m_pscene);
        // Use default render states (unless that method is overriden)
        setRenderStates();
        // Create a scene component and set the root to our jscene
        RenderComponent rc = m_wm.getRenderManager().createRenderComponent(m_jscene);
        // Add the scene component with our jscene to the entity
        addComponent(RenderComponent.class, rc);
        // Add our processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));
        // Add the processor collection component to the entity
        addComponent(ProcessorCollectionComponent.class, processorCollection);

        // Start the rendering
        if (addEntity) // Add the entity to the world manager
            wm.addEntity(this);

        // Finish the initialization
        finalizeInitialization(characterDOM); // If not null, we are loading a configuration
    }
     
    /**
     * Instantiate the GameContext for this xmlCharacter
     * 
     * @return
     */
    protected abstract GameContext instantiateContext();

    /**
     * Override this method to initialize the game trigger actions mappings
     */
    protected abstract void initKeyBindings();

    /**
     * Wrap up the initialization process.
     * @param character
     * @param attributes
     */
    protected void finalizeInitialization(xmlCharacter characterDOM)
    {
        while (setMeshAndSkeletonRefs() == false) // Bind up skeleton reference, etc
            Thread.yield();

        // Set animations and custom meshes
        executeAttributes(m_attributes);

        // Set position
        if (m_attributes.getOrigin() != null)
            m_modelInst.getTransform().setLocalMatrix(m_attributes.getOrigin());

        // Facial animation state is designated to id (and index) 1
        if (m_skeleton.getAnimationStateCount() > 1)
        {
            AnimationState facialAnimationState = m_skeleton.getAnimationState(1);
            facialAnimationState.setCurrentCycle(-1);
            facialAnimationState.setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
            facialAnimationState.setAnimationSpeed(0.1f);
        }
        if (m_skeleton.getAnimationComponent().getGroups().size() > 1)
        {
            m_facialAnimationQ = new TransitionQueue(m_skeleton, 1);
            // Go to default face pose
            m_facialAnimationQ.addTransition(new TransitionCommand(m_defaultFacePose, m_defaultFacePoseTiming, PlaybackMode.PlayOnce, false));
            // Smile when comming in
            initiateFacialAnimation(1, 0.75f, 0.75f);
        }

        // Hook up eyeballs
        m_eyes = new CharacterEyes(this, m_wm);
        
        // Apply remaining customizations
        if (characterDOM != null)
        {
            // Materials
            applyMaterialProperties(characterDOM);
            // Skeletal modifications
            applySkeletalModifications(characterDOM);
        }
        else
            setDefaultShaders();


        // The verlet arm!
        SkinnedMeshJoint rightShoulderJoint = (SkinnedMeshJoint) m_skeleton.findChild("rightArm");
        SkinnedMeshJoint leftShoulderJoint  = (SkinnedMeshJoint) m_skeleton.findChild("leftArm");
        m_rightArm = new VerletArm(rightShoulderJoint, m_modelInst, true);
        m_leftArm  = new VerletArm(leftShoulderJoint,  m_modelInst, false);
        // Debugging visualization
        //                            VerletVisualManager visual = new VerletVisualManager("avatar arm visuals", m_wm);
        //                            visual.addVerletObject(m_arm);
        //                            visual.setWireframe(true);

        
        // Apply the material on everything that was just loaded.
        for (PPolygonSkinnedMeshInstance meshInstance : m_skeleton.getSkinnedMeshInstances())
            meshInstance.applyMaterial();

        // New verlet skeleton manipulator
        m_skeletonManipulator = new VerletSkeletonFlatteningManipulator(m_leftArm, m_rightArm, m_eyes.getLeftEyeBall(), m_eyes.getRightEyeBall(), m_skeleton, m_modelInst);
        m_rightArm.setSkeletonManipulator(m_skeletonManipulator);
        m_leftArm.setSkeletonManipulator(m_skeletonManipulator);
        //m_arm.setPointAtLocation(Vector3f.UNIT_Y.mult(2.0f)); // test pointing, set to null to stop pointing
        
        // Associate ourselves with our animation states
        for (AnimationState animState : m_skeleton.getAnimationStates())
            animState.addListener(this);
        // Turn on the animation
        m_AnimationProcessor.setEnable(true);
        // Turn on updates
        m_characterProcessor.start();
        m_modelInst.setRenderStop(false);
        m_initialized = true;
    }

    /**
     * Sets shaders on the parts according to the defaults.
     */
    public void setDefaultShaders()
    {
        AbstractShaderProgram clothingShader = new ClothingShader(m_wm);
        try {
            // Most clothes are not defaulting to use a pattern texture
            clothingShader.setProperty(new ShaderProperty("PatternDiffuseMapIndex", GLSLDataType.GLSL_INT, Integer.valueOf(0)));
        } catch (NoSuchPropertyException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        AbstractShaderProgram fleshShader = new FleshShader(m_wm);
        float[] skinColor = { (230.0f/255.0f), (197.0f/255.0f), (190.0f/255.0f) };
        try {
            fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }
        AbstractShaderProgram accessoryShader = new SimpleTNLWithAmbient(m_wm);
        AbstractShaderProgram eyeballShader = new EyeballShader(m_wm);
        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.getSkinnedMeshInstances();
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            PMeshMaterial meshMat = meshInst.getMaterialRef().getMaterial();
            // is this an eyeball? (also used for tongue and teeth)
            if (meshInst.getName().contains("EyeGeoShape") ||
                meshInst.getName().contains("Tongue")      ||
                meshInst.getName().contains("Teeth"))
                meshMat.setShader(eyeballShader);
            else if (meshInst.getName().contains("Head") ||
                     meshInst.getName().contains("Nude") ||
                     meshInst.getName().contains("Arms") ||
                     meshInst.getName().contains("Hand"))// is it flesh?
                meshMat.setShader(fleshShader);
            else // assume to be clothing
                meshMat.setShader(clothingShader);
            // Apply it!
            meshInst.applyShader();
        }
        // then the attachments
        PNode skeletonRoot = m_skeleton.getSkeletonRoot();
        FastList<PNode> queue = new FastList<PNode>();
        queue.addAll(skeletonRoot.getChildren());
        while (queue.isEmpty() == false)
        {
            // process
            PNode current = queue.removeFirst();
            if (current instanceof PPolygonMeshInstance)
            {
                PPolygonMeshInstance meshInst = (PPolygonMeshInstance) current;
                // Grab a copy of the material
                PMeshMaterial meshMat = meshInst.getMaterialCopy().getMaterial();
                meshMat.setShader(accessoryShader);
                meshInst.applyShader();
            }
            // add all the kids
            if (current instanceof PJoint ||
                current instanceof PPolygonMeshInstance)
                queue.addAll(current.getChildren());
        }
    }

    /**
     * A subset of the functionality in setDefaultShaders
     */
    private void setDefaultHeadShaders()
    {
        AbstractShaderProgram fleshShader = new FleshShader(m_wm);
        float[] skinColor = { (230.0f/255.0f), (197.0f/255.0f), (190.0f/255.0f) };
        try {
            fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

        AbstractShaderProgram eyeballShader = new EyeballShader(m_wm);

        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.retrieveSkinnedMeshes("Head");
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            PMeshMaterial meshMat = meshInst.getMaterialRef().getMaterial();
            // is this an eyeball? (also used for tongue and teeth)
            if (meshInst.getName().contains("EyeGeoShape") ||
                meshInst.getName().contains("Tongue")      ||
                meshInst.getName().contains("Teeth"))
                meshMat.setShader(eyeballShader);
            else
                meshMat.setShader(fleshShader);
            // Apply it!
            meshInst.applyShader();
        }
    }

    /**
     * This method applies the m_attributes member. It simply wraps calls to
     * loadSkeleton and executeAttributes
     */
    private void applyAttributes()
    {
        if (m_attributes == null)
        {
            logger.warning("No attributes, aborting applyAttributes.");
            return;
        }
        if (m_attributes.isUseSimpleStaticModel() == true)
            return; // Nothing else to be done here

        // eat the skeleton
        if (m_attributes.isMale())
           m_skeleton = m_pscene.getRepository().getSkeleton("MaleSkeleton");//loadSkeleton(maleSkeleton);
        else
           m_skeleton = m_pscene.getRepository().getSkeleton("FemaleSkeleton");//loadSkeleton(femaleSkeleton);
        
        if (m_skeleton == null) // problem
        {
            logger.severe("Unable to load skeleton. Aborting applyAttributes.");
            return;
        }
        else
        {
            // synch up animation states with groups
            while (m_skeleton.getAnimationComponent().getGroups().size() < m_skeleton.getAnimationStateCount())
                m_skeleton.addAnimationState(new AnimationState(m_skeleton.getAnimationStateCount()));
        }
    }
    
    /**
     * This method applies all the commands of the CharacterAttributes object.
     * Things such as animation files to load, geometry to remove or add, etc.
     * @param attributes The attributes to process
     */
    private void executeAttributes(CharacterAttributes attributes)
    {   
        String fileProtocol = attributes.getBaseURL();
        // If no base url was provided by the character attributes, then it is
        // assumed that the prefix should be the file protocol to the local machine
        // in the current folder.
        if (fileProtocol == null)
            fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");

        InstructionProcessor instructionProcessor = new InstructionProcessor(m_wm);
        Instruction attributeRoot = new Instruction();
        // Set the skeleton to our skeleton
        attributeRoot.addChildInstruction(InstructionType.setSkeleton, m_skeleton);
        // Load up any geometry requested by the provided attributes object
        List<String> load = attributes.getLoadInstructions();
        if (load != null) {
            for (int i = 0; i < load.size(); i++)
                attributeRoot.addChildInstruction(InstructionType.loadGeometry, fileProtocol + load.get(i));
        }
        // Skinned mesh attachments
        CharacterAttributes.SkinnedMeshParams [] add = attributes.getAddInstructions();
        if (add != null && add.length > 0) {
            for (int i = 0; i < add.length; i++) {
                attributeRoot.addSkinnedMeshInstruction(add[i].meshName, add[i].subGroupName);
            }
        }
        // Regular mesh attachments
        AttachmentParams [] attachments = attributes.getAttachmentsInstructions();
        if (attachments != null && attachments.length > 0) {
            for (int i = 0; i < attachments.length; i++) {
                // TODO: HACK for hair matrix... need to get this fixed its not outputing into config file
                PMatrix tempsolution = null;
                if (attachments[i].getMatrix() == null)
                    tempsolution = new PMatrix(new Vector3f(0.0f, (float) Math.toRadians(180), 0.0f), new Vector3f(1.0f, 1.0f, 1.0f), Vector3f.ZERO);
                else
                    tempsolution = attachments[i].getMatrix();
                attributeRoot.addAttachmentInstruction( attachments[i].getMeshName(), attachments[i].getJointName(), tempsolution);
            }
        }

        // Load up body animations
        String [] anims = attributes.getAnimations();
        if (anims != null && anims.length > 0) {
            for (int i = 0; i < anims.length; i++) {
                attributeRoot.addChildInstruction(InstructionType.loadAnimation, fileProtocol + anims[i]);
            }
        }

        // Load up facial animations
        String [] facialAnims = attributes.getFacialAnimations();
        if (facialAnims != null && facialAnims.length > 0) {
            for (int i = 0; i < facialAnims.length; i++) {
                attributeRoot.addChildInstruction(InstructionType.loadFacialAnimation, fileProtocol + facialAnims[i]);
            }
        }

        // Execute the instruction tree
        instructionProcessor.execute(attributeRoot);
    }


    /**
     * Interpret and apply the provided attributes and modify this character.
     * @param attributes
     */
    @Deprecated
    public void loadAttributes(CharacterAttributes attributes) {

    }

    /**
     * Loads the model and sets processors 
     * @param processors
     */
    protected void initScene(ArrayList<ProcessorComponent> processors)
    {

        if(m_attributes.isUseSimpleStaticModel()) // Using the sphere simplification
        {
            if (m_attributes.getSimpleScene() == null) {

                float radius = 1.0f;
                ColorRGBA color = new ColorRGBA(ColorRGBA.randomColor());
                SharedAsset modelAsset = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D_Mesh, ""));
                PMeshMaterial geometryMaterial = new PMeshMaterial();
                geometryMaterial.setColorMaterial(ColorMaterial.Diffuse); // Make the vert colors affect diffuse coloring
                geometryMaterial.setDiffuse(ColorRGBA.white);
                try {
                    geometryMaterial.setTexture(new TextureMaterialProperties(new File("assets/textures/SmileFace.jpg").toURI().toURL()), 0);
                } catch (MalformedURLException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                PPolygonMesh sphereMesh = PMeshUtils.createSphere("Character Sphere", Vector3f.ZERO, radius, 25, 25, color);
                Sphere s = new Sphere("Character Sphere", Vector3f.ZERO, 25, 25, radius);
                sphereMesh.getGeometry().reconstruct(s.getVertexBuffer(), s.getNormalBuffer(), s.getColorBuffer(), s.getTextureCoords(0), s.getIndexBuffer());
                sphereMesh.setMaterial(geometryMaterial);


                sphereMesh.setSubmitGeometry(false);
                modelAsset.setAssetData(sphereMesh);
                m_modelInst = m_pscene.addModelInstance("Character", modelAsset, m_attributes.getOrigin());
            } else {
                SharedAsset modelAsset = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D_Mesh, ""));
                modelAsset.setAssetData(m_attributes.getSimpleScene());
                m_modelInst = m_pscene.addModelInstance("Character", modelAsset, m_attributes.getOrigin());
            }
        }
        else // Otherwise use the specified collada model
        {
            m_modelInst = new PPolygonModelInstance(m_attributes.getName());
            m_modelInst.setRenderStop(true);
            m_modelInst.addChild(m_skeleton);
            m_pscene.addInstanceNode(m_modelInst);

            // Debugging / Diagnostic output
//            Logger.getLogger(Character.class.getName()).log(Level.INFO, "Model " + m_pscene + "  inst " + m_modelInst);
            m_AnimationProcessor = new CharacterAnimationProcessor(m_modelInst);
            // Start the animation processor disabled until we finish loading
            m_AnimationProcessor.setEnable(false);
            processors.add(m_AnimationProcessor);
        }

        m_characterProcessor = new CharacterProcessor(this);
        m_characterProcessor.stop();
        processors.add(m_characterProcessor);
    }
    
    /**
     * This method will atempt a context transition
     * @param transition
     * @return true if the transition is succesfully validated
     */
    public boolean  excecuteContextTransition(TransitionObject transition)
    {
        GameContext context = m_registry.get(transition.getContextMessageName());
        if (context == null)
            return false;
        
        Class contextClass = context.getClass();
        Method method = null;
        
        try {
            method = contextClass.getMethod(transition.getContextMessageName(), Object.class);
        } catch (NoSuchMethodException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        if (method != null)
        {
            Object bool = null;
            
            try {
                bool = method.invoke(context, transition.getContextMessageArgs());
            } catch (IllegalAccessException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            
            if (bool instanceof Boolean)
            {
                if ( ((Boolean)bool).booleanValue() )
                {
                    // Context transition validated! Switch context if the state
                    // transition is validated as well
                    if (context.excecuteTransition(transition))
                    {
                        m_context.setCurrentState(null); // calls the stateExit()
                        m_context = context;
                        return true; 
                    }               
                }
            }
        }
        
        return false;
    }
    
    /**
     * A context may register multiple entry points
     * @param context       -   the context to register
     * @param methodName    -   the entry point validation method of the context
     */
    public void  RegisterContext(GameContext context, String methodName)
    {
        m_registry.put(methodName, context);
    }
    
    /**
     * Called in the constructor, override this method to set your own
     * non-default render states.
     */
    public void setRenderStates() 
    {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) m_wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) m_wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(ColorRGBA.white);

        // SET lighting
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAmbient(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setLocation(new Vector3f(-1000, 0, 0)); // not affecting anything
        light.setEnabled(true);
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setEnabled(true);
        ls.attach(light);
        
        // Cull State
        CullState cs = (CullState) m_wm.getRenderManager().createRendererState(RenderState.RS_CULL);      
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        
        // Wireframe State
        WireframeState ws = (WireframeState) m_wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(false);
        
        // Push 'em down the pipe
        m_jscene.setRenderState(matState);
        m_jscene.setRenderState(buf);
        m_jscene.setRenderState(cs);
        m_jscene.setRenderState(ws);
        m_jscene.setRenderState(ls);
    }
    
    /**
     * This entity's jscene will be set in the IMI input manager
     * as the selected one.
     */
    public void selectForInput()
    {
        // Set this jscene to be the "selected" one for IMI input handling
        ((JSceneEventProcessor)m_wm.getUserData(JSceneEventProcessor.class)).setJScene(m_jscene); 
    }
    
    /**
     * Called each frame; used to drive the character's assorted time-based functionality.
     * @param deltaTime
     */
    public void update(float deltaTime)
    {
        if (!m_initialized)
            setMeshAndSkeletonRefs();
        
        if (m_attributes.isUseSimpleStaticModel())
            m_modelInst.setDirty(true, true);
        
        if (m_context != null)
            m_context.update(deltaTime);
        if (m_eyes != null)
            m_eyes.update(deltaTime);
        if (m_rightArm != null)
            m_rightArm.update(deltaTime);
        if (m_leftArm != null)
            m_leftArm.update(deltaTime);
    }

    /**
     * Sets the mesh and skeleton references after load time
     */
    private boolean setMeshAndSkeletonRefs()
    {
        // safety against place holders
        if (m_modelInst.getChildrenCount() <= 0 || m_modelInst.getChild(0) instanceof SharedAssetPlaceHolder)
            return false;
        
        if (m_modelInst.getChild(0).getChildrenCount() == 0 && m_modelInst.getChild(0) instanceof PPolygonMeshInstance)
        {
            // Simple sphere model case
            m_mesh       = (PPolygonMeshInstance)m_modelInst.getChild(0);
            m_skeleton   = null;
            m_initialized = true;
        }
        else if (m_modelInst.getChild(0) instanceof SkeletonNode
                && m_modelInst.getChild(0).getChildrenCount() >= 2
                && m_modelInst.getChild(0).getChild(1) instanceof PPolygonSkinnedMeshInstance)
        {
            m_mesh       = (PPolygonSkinnedMeshInstance)m_modelInst.getChild(0).getChild(1);
            m_skeleton   = (SkeletonNode)m_modelInst.getChild(0);
            m_skeleton.getAnimationState().addListener(this);
            
            m_initialized = true;
        }
        return true;
    }

    /**
     * Use this convenience method to animate a facial expression for a short
     * amount of time.
     * @param cycleName The name of the facial animation cycle to play
     * @param fTimeIn How long should to transition into the animation take
     * @param fTimeOut How long the transition out of the animation takes
     */
    public void initiateFacialAnimation(String cycleName, float fTimeIn, float fTimeOut) 
    {
        if (m_skeleton == null) // Not ready to handle facial animations yet
            return;
        if (m_facialAnimationQ == null)
        {
            AnimationComponent ac = m_skeleton.getAnimationComponent();
            List<AnimationGroup> groups = ac.getGroups();
            if (groups.size() > 1)
                m_facialAnimationQ = new TransitionQueue(m_skeleton, 1);
            else
                return;   
        }
        int cycle = m_skeleton.getAnimationGroup(1).findAnimationCycleIndex(cycleName);
        if (cycle != -1)
            initiateFacialAnimation(cycle, fTimeIn, fTimeOut);
    }

    /**
     * Convenience method for playing facial animations.
     * @param cycleIndex The index of the desired facial animation cycle
     * @param fTimeIn Transition-to time.
     * @param fTimeOut Transition-out time.
     */
    public void initiateFacialAnimation(int cycleIndex, float fTimeIn, float fTimeOut)
    {
        if (m_facialAnimationQ == null)
        {
            if (m_skeleton.getAnimationComponent().getGroups().size() > 1)   
                m_facialAnimationQ = new TransitionQueue(m_skeleton, 1);
            else
                return;   
        }
        
        // Return from default face pose if current
        m_facialAnimationQ.addTransition(new TransitionCommand(m_defaultFacePose, m_defaultFacePoseTiming, PlaybackMode.PlayOnce, true));
        
        m_facialAnimationQ.addTransition(new TransitionCommand(cycleIndex, fTimeIn, PlaybackMode.PlayOnce, false));
        m_facialAnimationQ.addTransition(new TransitionCommand(cycleIndex, fTimeOut, PlaybackMode.PlayOnce, true));
        
        // Go to default face pose
        m_facialAnimationQ.addTransition(new TransitionCommand(m_defaultFacePose, m_defaultFacePoseTiming, PlaybackMode.PlayOnce, false));
    }
    
    public void setCameraOnMe() 
    {
        //m_wm.getUserData(arg0);
    }
    
    /**
     * Adds this character to an object collection,
     * the collection will be used to receive obstacles to avoid
     * while using steering behaviors and to find objects such as chairs to
     * sit on for e.g.
     * @param objs
     */
    public void setObjectCollection(ObjectCollection objs) 
    {
        m_objectCollection = objs;
        objs.addObject(this);
    }

    public ObjectCollection getObjectCollection() {
        return m_objectCollection;
    }

    /**
     * Return a new PSphere that is built around the model instances bounding sphere.
     * @return The sphere
     */
    public PSphere getBoundingSphere() 
    {
        if (m_modelInst.getBoundingSphere() == null)
            m_modelInst.calculateBoundingSphere();
        PSphere result = new PSphere(m_modelInst.getBoundingSphere());
        result.setCenter(m_modelInst.getTransform().getWorldMatrix(false).getTranslation().add(result.getCenter()));
        return result;
    }
        
    /**
     * Returns true if currently transitioning between animation cycles
     * @return
     */
    public boolean isTransitioning()
    {
        if(m_skeleton != null)
            return m_skeleton.getAnimationState().isTransitioning();
        return false;
    }
    
    public PPolygonMeshInstance getMesh() {
        return m_mesh;
    }
    
    public SkeletonNode getSkeleton() {
        return m_skeleton;   
    }
    
    public void keyPressed(int key)
    {
        Integer trigger = m_keyBindings.get(key); 
        if (trigger == null)
            return;
        
        m_context.triggerPressed(trigger);
    }
        
    public void keyReleased(int key)
    {
        Integer trigger = m_keyBindings.get(key); 
        if (trigger == null)
            return;
        
        m_context.triggerReleased(trigger);
    }
    
    public CharacterAttributes getAttributes() {
        return m_attributes;
    }

    public void setAttributes(CharacterAttributes attributes) {
        m_attributes = attributes;
    }

    public GameContext getContext() {
        return m_context;
    }
    
    public CharacterController getController() {
        return m_context.getController();
    }

    public void setContext(GameContext context) {
        m_context = context;
    }

    public JScene getJScene() {
        return m_jscene;
    }

    public void setJScene(JScene jscene) {
        m_jscene = jscene;
    }

    public PScene getPScene() {
        return m_pscene;
    }

    public void setPScene(PScene pscene) {
        m_pscene = pscene;
    }

    public WorldManager getWorldManager() {
        return m_wm;
    }

    public void setWorldManager(WorldManager wm) {
        m_wm = wm;
    }

    public PPolygonModelInstance getModelInst() {
        return m_modelInst;
    }

    public void setModelInst(PPolygonModelInstance modelInst) {
        this.m_modelInst = modelInst;
    }
    
    public Hashtable<Integer, Integer> getKeyBindings() {
        return m_keyBindings;
    }
    
    public Vector3f getPosition() {
        return m_context.getController().getPosition();
    }
    
    public Quaternion getQuaternion() {
        return m_context.getController().getQuaternion();
    }

    public Vector3f getRightVector() {
        return m_context.getController().getRightVector();
    }
    
    public Vector3f getForwardVector() {
        return m_context.getController().getForwardVector();
    }
    
    public PSphere getNearestObstacleSphere(Vector3f myPosition)
    {
        return null;
    }
    
    public void receiveAnimationMessage(AnimationMessageType message, int stateID)
    {
        m_context.notifyAnimationMessage(message, stateID);
    }

    public TransitionQueue getFacialAnimationQ() 
    {
        if (m_facialAnimationQ == null)
        {
            if (m_skeleton.getAnimationComponent().getGroups().size() > 1)   
                m_facialAnimationQ = new TransitionQueue(m_skeleton, 1);
        }
        return m_facialAnimationQ;
    }

    public CharacterEyes getEyes() {
        return m_eyes;
    }

    public VerletArm getRightArm() {
        return m_rightArm;
    }
    
    public VerletArm getLeftArm() {
        return m_leftArm;
    }
    
    public VerletSkeletonFlatteningManipulator getSkeletonManipulator() {
        return m_skeletonManipulator;
    }
    
    /**
     * Will return true if the character is loaded and has a valid skeleton
     * and meshes
     * @return
     */
    public boolean isInitialized()
    {
        return m_initialized;
    }

    public void installHead(URL headLocation, String attachmentJointName)
    {
        m_skeleton.setRenderStop(true);
        m_AnimationProcessor.setEnable(false);
        m_characterProcessor.setEnabled(false);
        // Ready the collada loader!
        Collada colladaLoader = new Collada();
        colladaLoader.setLoadFlags(true, true, false);
        colladaLoader.load(new PScene(m_wm), headLocation);

        SkeletonNode newHeadSkeleton = colladaLoader.getSkeletonNode();

        // Cut off the old skeleton at the specified attach point
        SkinnedMeshJoint parent = (SkinnedMeshJoint)m_skeleton.findSkinnedMeshJoint(attachmentJointName).getParent();
        parent.removeChild(attachmentJointName);
        parent.addChild(newHeadSkeleton.findSkinnedMeshJoint(attachmentJointName));

        m_skeleton.refresh();
        m_skeleton.clearSubGroup("Head");

        Iterable<PNode> list = newHeadSkeleton.getChildren();
        for (PNode node : list)
        {
            if (node instanceof PPolygonSkinnedMesh)
            {
                PPolygonSkinnedMesh skinnedMesh = (PPolygonSkinnedMesh) node;
                // Make an instance
                PPolygonSkinnedMeshInstance skinnedMeshInstance = (PPolygonSkinnedMeshInstance) m_pscene.addMeshInstance(skinnedMesh, new PMatrix());
                // Add it to the skeleton
                m_skeleton.addToSubGroup(skinnedMeshInstance, "Head");
            }
        }
        setDefaultHeadShaders();
        // Relink all of the old meshes
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
            meshInst.setAndLinkSkeletonNode(m_skeleton);
        
        m_AnimationProcessor.setEnable(true);
        m_characterProcessor.setEnabled(true);
        m_skeleton.setRenderStop(false);
    }

    /**
     * This only remains in order to provide an example of the process.
     * @param headLocation
     * @deprecated
     */
    @Deprecated
    public void oldInstallHead(URL headLocation)
    {
        // Ready the collada loader!
        Collada colladaLoader = new Collada();
        colladaLoader.setLoadFlags(true, true, false);

        colladaLoader.load(new PScene(m_wm), headLocation);

        SkeletonNode newHeadSkeleton = colladaLoader.getSkeletonNode();

        // Remove all the old head stuff from our skeleton
        PNode pnode = m_skeleton.findChild("rightEyeGeoShape");
        int[] influences = null;
        if (pnode != null)
        {
            PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance)pnode;
            // cache le influences
            influences = meshInst.getInfluenceIndices(); // We should not need to do this, but it fixes the problem
        }
        m_skeleton.clearSubGroup("Head");

        Iterable<PNode> list = newHeadSkeleton.getChildren();
        for (PNode node : list)
        {
            if (node instanceof PPolygonSkinnedMesh)
            {
                PPolygonSkinnedMesh skinnedMesh = (PPolygonSkinnedMesh) node;
                // Make an instance
                PPolygonSkinnedMeshInstance skinnedMeshInstance = (PPolygonSkinnedMeshInstance) m_pscene.addMeshInstance(skinnedMesh, new PMatrix());

                //  Link the SkinnedMesh to the Skeleton.
                skinnedMeshInstance.setAndLinkSkeletonNode(m_skeleton);

                // Add it to the skeleton
                m_skeleton.addToSubGroup(skinnedMeshInstance, "Head");

            }
        }
        // Now fix the skeletal differences
        generateDeltas(newHeadSkeleton, "Head"); 
        // Now reattach the eyes
        m_eyes = new CharacterEyes(this, m_wm);
//        // reassociate this with the verlet thingy
//        while (m_skeletonManipulator == null)
//        {
//            Thread.yield();
//        }
        if (m_skeletonManipulator != null)
        {
            m_skeletonManipulator.setLeftEyeBall(m_eyes.getLeftEyeBall());
            m_skeletonManipulator.setRightEyeBall(m_eyes.getRightEyeBall());
        }
        // Finally, apply the default shaders
        setDefaultShaders();
        pnode = m_skeleton.findChild("rightEyeGeoShape");
        if (pnode != null)
        {
            PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance)pnode;
            // reset influence indices
            meshInst.setInfluenceIndices(influences);
        }
    }

    
    private void generateDeltas(SkeletonNode newSkeleton, String rootJointName)
    {
        if (newSkeleton == null || m_skeleton == null)
        {
            logger.severe(getName() + " can not install head, got a null skeleton! current one: " + m_skeleton + " new one: " + newSkeleton);
            return;
        }
        
        // Gather the joints and set the new local modifiers
        SkinnedMeshJoint head = m_skeleton.findSkinnedMeshJoint(rootJointName);
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(head);
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him! If not a skinned mesh joint skip and prune
            if (current instanceof SkinnedMeshJoint)
            {
                SkinnedMeshJoint currentHeadJoint = (SkinnedMeshJoint)current;
                SkinnedMeshJoint newHeadJoint     = newSkeleton.findSkinnedMeshJoint(currentHeadJoint.getName());

                if (newHeadJoint == null) // Not found in the new skeleton
                    logger.severe("Could not find associated joint in the new skeleton, joint name was " + currentHeadJoint.getName());
                PMatrix modifierDelta = new PMatrix();
                modifierDelta.mul( currentHeadJoint.getTransform().getLocalMatrix(false).inverse(),
                            newHeadJoint.getTransform().getLocalMatrix(false));

                currentHeadJoint.getBindPose().mul(modifierDelta);
//                currentHeadJoint.getBindPose().set(newHeadJoint.getBindPose());
            }
            else
                continue; // Prune (kids are not added to the list)
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
    }

    /**
     * Save the current avatar configuration to the specified location. This
     * method does not currently function, as the file protocol cannot open an
     * output stream to a particular file.
     * @param targetSaveLocation The location of the file that will be generated
     */
    public void saveConfiguration(URL targetSaveLocation)
    {
        // TODO : Find some workaround for this if one exists
        throw new UnsupportedOperationException("This method is currently unsupported " +
                "as the file URL protocol cannot open an OutputStream. A workaround is " +
                "being researched. In the interim, use the File overload of this method.");
    }

    /**
     * This method saves the character's current configuration to the specified
     * location.
     * @param location
     */
    public void saveConfiguration(File location)
    {
        try {
            final JAXBContext context = JAXBContext.newInstance("imi.serialization.xml.bindings");
            final Marshaller m = context.createMarshaller();
            // Pretty files please
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);

            if (location.exists() == true && location.canWrite() == false)
                throw new IOException("Request file (" + location.toString() + ") is not writeable.");
            else if (location.exists() == false)
                location.createNewFile();

            xmlCharacter characterDom = generateCharacterDOM();
            m.marshal( characterDom, location);
        }
        catch (JAXBException ex) {
            logger.log(Level.SEVERE, "Failed to write save file! " + ex.getMessage());
            logger.log(Level.SEVERE, ex.getErrorCode() + " : " + ex.getLocalizedMessage() + " : " + ex.toString());
            ex.printStackTrace();

        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to open OutputStream to " +
                                    location.toString() + "! " + ex.getMessage());
        }
    }
    
    /**
     * Load the configuration file at the specified location and apply it to this
     * character instance.
     * @param location
     */
    public void loadConfiguration(URL location)
    {
        // Turn off character processor and animation processor
        boolean oldState = m_AnimationProcessor.isEnable();
        m_AnimationProcessor.setEnable(false);
        m_characterProcessor.stop();

        try {
            final JAXBContext context = JAXBContext.newInstance("imi.serialization.xml.bindings");
            final Unmarshaller m = context.createUnmarshaller();

            InputStream is = location.openConnection().getInputStream();

            Object characterObj = m.unmarshal( is );
            if (characterObj instanceof xmlCharacter)
            {
                applyCharacterDOM((xmlCharacter)characterObj);
            }
            else
            {
                logger.log(Level.SEVERE,
                        "JAXB somehow parsed the file and made some other object: " + characterObj.toString());
            }

        }
        catch (JAXBException ex) {
            logger.log(Level.SEVERE, "Failed to parse the file! " + ex.getMessage());
            logger.log(Level.SEVERE, ex.getErrorCode() + " : " + ex.getLocalizedMessage() + " : " + ex.toString());
            ex.printStackTrace();

        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to open InputStream to " +
                                    location.toString() + "! " + ex.getMessage());
        }

        // restart processors
        m_AnimationProcessor.setEnable(oldState);
        m_characterProcessor.start();
    }

    /**
     * Apply the data with to this character instance
     * @param characterDOM
     */
    private void applyCharacterDOM(xmlCharacter characterDOM)
    {
        // The Attribute loading section is not functional currently,
        // skeleton and local modifiers can be saved and loaded.
        xmlCharacterAttributes xmlAttributes = characterDOM.getAttributes();

        if (m_attributes!=null) {
            m_attributes.applyAttributesDOM(xmlAttributes);
            loadAttributes(m_attributes);
        } else {
            CharacterAttributes attributes = new CharacterAttributes(xmlAttributes);

            // Apply the loaded attributes
            loadAttributes(attributes);
        }

        // Material properties
        applyMaterialProperties(characterDOM);

        // Skeletal modifications
        applySkeletalModifications(characterDOM);

        // assign to controller as well
        m_context.getController().setModelInstance(m_modelInst);
    }

    /**
     * This method parses through the SkeletonDetails list in the DOM and applies
     * the modifications.
     * @param characterDOM
     */
    private void applySkeletalModifications(xmlCharacter characterDOM)
    {
        List<xmlJointModification> jointMods = characterDOM.getSkeletonDetails();
        if (jointMods != null)
        {
            for (xmlJointModification jMod : jointMods)
            {
                SkinnedMeshJoint targetJoint = m_skeleton.findSkinnedMeshJoint(jMod.getTargetJointName());
                if (targetJoint != null)
                {
                    // Apply customizations
                    PMatrix mat = null;
                    if (jMod.getLocalModifierMatrix() != null)
                    {
                        mat = jMod.getLocalModifierMatrix().getPMatrix();
                        targetJoint.setLocalModifierMatrix(mat);
                    }

                    if (jMod.getBindPoseMatrix() != null)
                    {
                        mat = jMod.getBindPoseMatrix().getPMatrix();
                        targetJoint.getBindPose().set(mat);
                    }
                }
                else
                {
                    logger.log(Level.WARNING,
                            "Target joint not found for modifier: " + jMod.getTargetJointName());
                }
            }
        }
    }

    /**
     * Apply the material properties from the DOM to the corresponding meshes
     * @param characterDOM
     */
    private void applyMaterialProperties(xmlCharacter characterDOM) {
        for (xmlMaterial xmlMat : characterDOM.getMaterials())
        {
            PMeshMaterial meshMat = new PMeshMaterial(xmlMat, m_wm);
            String targetMeshName = xmlMat.getTargetMeshName();
            // find the mesh it belongs to
            PPolygonMeshInstance meshInst = getSkeleton().getSkinnedMeshInstance(targetMeshName);
            if (meshInst != null)
            {
                // Sweet! Apply the material
                meshInst.setMaterial(meshMat);
                meshInst.applyMaterial();
            }
            else
            {
                logger.log(Level.WARNING,
                        "xmlMaterial targetting nonexistant mesh; target was " +
                        targetMeshName);
            }
        }
    }

    /**
     * This method is used to serialize the character's configuration into a DOM object
     * @return
     */
    private xmlCharacter generateCharacterDOM()
    {
        xmlCharacter result = new xmlCharacter();
        // Attributes
        if (m_attributes != null)
            result.setAttributes(m_attributes.generateAttributesDOM());
        else
        {
            logger.log(Level.WARNING,
                    "Attemping to serialize a character with no attributes!");
        }
        // Store skeletal modifications
        SerializationHelper collector = new SerializationHelper();

        TreeTraverser.breadthFirst(m_skeleton, collector);
        for (xmlJointModification jMod : collector.getJointModifierList())
            result.addJointModification(jMod);
        // Store material information
        for (xmlMaterial mat : collector.getMaterials())
            result.addMaterial(mat);

        return result;
    }


    /**
     * The animation cycle index in the animation group (facial group is 1)
     * @return
     */
    public int getDefaultFacePose() {
        return m_defaultFacePose;
    }

    /**
     * The animation cycle index in the animation group (facial group is 1)
     * @param defaultFacePose
     */
    public void setDefaultFacePose(int defaultFacePose) {
        this.m_defaultFacePose = defaultFacePose;
    }

    /**
     * How fast to animate in and out of the default face pose
     * @return
     */
    public float getDefaultFacePoseTiming() {
        return m_defaultFacePoseTiming;
    }

    /**
     * How fast to animate in and out of the default face pose
     * @param defaultFacePoseTiming
     */
    public void setDefaultFacePoseTiming(float defaultFacePoseTiming) {
        this.m_defaultFacePoseTiming = defaultFacePoseTiming;
    }

    public void die() {
        if (m_wm == null)
            return;
        

        ProcessorCollectionComponent pcc = (ProcessorCollectionComponent) getComponent(ProcessorCollectionComponent.class);
        pcc.removeAllProcessors();
        // Something needs to wait until this is finished
        m_characterProcessor.setEnabled(false);
        m_characterProcessor.stop();
        m_AnimationProcessor.setEnable(false);

        m_wm.removeEntity(this);

        m_keyBindings           = null;
        m_context               = null;
        m_attributes            = null;
        m_registry              = null;
        m_modelInst             = null;
        m_skeleton              = null;
        m_mesh                  = null;
        m_objectCollection      = null;
        m_AnimationProcessor    = null;
        m_characterProcessor    = null;
        m_facialAnimationQ      = null;
        m_eyes                  = null;
        m_rightArm              = null;
        m_leftArm               = null;
        m_skeletonManipulator   = null;
    }
}
