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

import com.jme.image.Texture.ApplyMode;
import com.jme.image.Texture.CombinerFunctionAlpha;
import com.jme.image.Texture.MinificationFilter;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import com.jme.scene.shape.Box;
import imi.character.objects.ObjectCollectionBase;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.TransitionObject;
import imi.scene.Updatable;
import imi.loaders.Instruction;
import imi.loaders.Instruction.InstructionType;
import imi.loaders.InstructionProcessor;
import imi.loaders.collada.Collada;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.ColladaLoadingException;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.Repository;
import imi.loaders.repository.RepositoryUser;
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
import imi.scene.animation.AnimationCycle;
import imi.scene.animation.AnimationGroup;
import imi.scene.animation.AnimationListener;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.animation.AnimationState;
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
import imi.scene.shader.programs.ClothingShaderSpecColor;
import imi.scene.shader.programs.EyeballShader;
import imi.scene.shader.programs.FleshShader;
import imi.scene.shader.programs.HairShader;
import imi.scene.shader.programs.PhongFleshShader;
import imi.scene.shader.programs.SimpleTNLWithAmbient;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.utils.PMeshUtils;
import imi.scene.utils.tree.SerializationHelper;
import imi.scene.utils.tree.TreeTraverser;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlJointModification;
import imi.serialization.xml.bindings.xmlMaterial;
import imi.utils.BinaryExporterImporter;
import imi.utils.instruments.Instrumentation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
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
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;



/**import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;

 * This class represents the high level avatar. It provides methods for performing
 * tasks that are character related.
 * @author Lou Hayt
 * @author Ronald E Dahlgren
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
    /** Context for the character's states**/
    protected GameContext                   m_context               = null;
    /** Various configuration attributes **/
    protected CharacterAttributes           m_attributes            = null;
    /** Mapping of different contexts **/
    protected HashMap<String, GameContext>  m_registry              = new HashMap<String, GameContext>();
    /** WM ref**/
    protected WorldManager                  m_wm                    = null;
    /** The root of the PNode's for the character's scene graph **/
    protected PScene                        m_pscene                = null;
    /** The bridge from PScenes to jME **/
    protected JScene                        m_jscene                = null;
    /** The model instance for the avatar **/
    protected PPolygonModelInstance         m_modelInst             = null;
    /** The model instance for the shadow quad **/
    protected PPolygonMeshInstance          m_shadowMesh           = null; // Quad!
    /** Skeleton that our skin is attached to **/
    protected SkeletonNode                  m_skeleton              = null;
    /** Collection of objects that this character is associated with **/
    protected ObjectCollectionBase          m_objectCollection      = null;
    /** Performs animation on the character **/
    protected CharacterAnimationProcessor   m_AnimationProcessor    = null;
    /** Provides update() calls to the character **/
    protected CharacterProcessor            m_characterProcessor    = null;
    /** Animation queue for facial animations. Used to chain expressions. **/
    protected FacialAnimationController     m_facialAnimations      = null;
    /** The eyes! **/
    protected CharacterEyes                 m_eyes                  = null;
    /** The arms! **/
    protected VerletArm                     m_rightArm              = null;
    protected VerletArm                     m_leftArm               = null;
    /** True once initialization has completely finished (including asset loading) **/
    private   boolean                       m_initialized           = false;
    /** Expansion slot for updatable things **/
    private   Updatable                     m_updateExtension       = null;
    /** Expansion slot for initialization **/
    private   CharacterInitializationInterface       m_initialization        = null;
    /** index of the 'default' facial animation **/
    private   int                           m_defaultFacePose       = 0; // No more 'playAll' cycle
    private   float                         m_defaultFacePoseTiming = 0.1f;
    private   VerletSkeletonFlatteningManipulator m_skeletonManipulator   = null;

    private Semaphore countingSemaphore = new Semaphore(0);
    protected BinaryExporterImporter                m_binaryExporter        = new BinaryExporterImporter();

    private static JAXBContext context;

    static {
        try {
            context = JAXBContext.newInstance("imi.serialization.xml.bindings", Character.class.getClassLoader());
        } catch (JAXBException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
    public Character(URL configurationFile, WorldManager wm) {
        this(configurationFile, wm, null, new PMatrix());
    }
    
    /**
     * Construct a new character using the specified configuration file and world
     * manager.
     * @param configurationFile
     * @param wm
     * @param transform
     */
    public Character(URL configurationFile, WorldManager wm, PMatrix transform) {
        this(configurationFile, wm, null, transform);
    }

    public Character(URL configurationFile, WorldManager wm, String baseURL, PMatrix transform) {
        this(configurationFile, wm, baseURL, transform, null);
    }

    public Character(URL configurationFile, WorldManager wm, String baseURL, PMatrix transform, CharacterInitializationInterface initializer)
    {
        super("InterimEntityName");
        xmlCharacter characterDOM = null;
        CharacterAttributes loadedAttributes = null;

        try {
            final Unmarshaller m = context.createUnmarshaller();

            InputStream is = configurationFile.openConnection().getInputStream();
            Object characterObj = m.unmarshal( is );

            if (characterObj instanceof xmlCharacter)
            {
                characterDOM = (xmlCharacter)characterObj;
                xmlCharacterAttributes xmlAttributes = characterDOM.getAttributes();
                xmlAttributes.setBaseURL(baseURL);
                loadedAttributes = new CharacterAttributes(xmlAttributes);
                loadedAttributes.setOrigin(transform);
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
        loadedAttributes.setInitializationObject(initializer);
        commonConstructionCode(wm, loadedAttributes, true, characterDOM);
    }

    public void setEnableShadow(boolean enable)
    {
        if (m_shadowMesh != null)
            m_shadowMesh.setRenderStop(!enable);
    }

    /**
     * Create a shadow for this character if one is not already present
     */
    public void addShadow() {
        if (m_shadowMesh == null) // Not parameterized, no sense in remaking shadows
        {
            // make shadow, minor offset to avoid Z-fighting with the y=0 plane
            Vector3f pointOne =     new Vector3f( 0.45f, 0.01f,  0.5f);
            Vector3f pointTwo =     new Vector3f(-0.45f, 0.01f,  0.5f);
            Vector3f pointThree =   new Vector3f(-0.45f, 0.01f, -0.5f);
            Vector3f pointFour =    new Vector3f( 0.45f, 0.01f, -0.5f);
            // UV sets, standard texturing
            Vector2f uvSetOne =     new Vector2f(0, 0);
            Vector2f uvSetTwo =     new Vector2f(1, 0);
            Vector2f uvSetThree =   new Vector2f(1, 1);
            Vector2f uvSetFour =    new Vector2f(0, 1);

            // Use a transparent material with a blob shadow texture
            PMeshMaterial shadowMaterial = new PMeshMaterial("ShadowMaterial");
            URL path = getClass().getClassLoader().getResource("imi/character/shadow.png");
            shadowMaterial.setTexture(path, 0);
            shadowMaterial.setAlphaState(PMeshMaterial.AlphaTransparencyType.A_ONE);
            shadowMaterial.setColorMaterial(ColorMaterial.None);

            TextureMaterialProperties textureProp = shadowMaterial.getTexture(0);
            textureProp.setAlphaCombineMode(CombinerFunctionAlpha.Modulate);
            textureProp.setApplyMode(ApplyMode.Replace);
            PPolygonMesh shadowMesh = PMeshUtils.createQuad("ShadowQuad",
                                                            pointOne, pointTwo, pointThree, pointFour,
                                                            ColorRGBA.cyan,
                                                            uvSetOne, uvSetTwo, uvSetThree, uvSetFour);
            shadowMesh.setMaterial(shadowMaterial);
            shadowMesh.setNumberOfTextures(1);
            // Add it to the scene
            m_shadowMesh = m_pscene.addMeshInstance(shadowMesh, new PMatrix());
            m_shadowMesh.setCollidable(false);
            // Attached to skeleton to get free dirtiness propagation
            m_skeleton.getSkeletonRoot().addChild(m_shadowMesh);
        }
    }

    /**
     * Finds every mesh associated with this avatar and applies its material.
     */
    public void applyMaterials()
    {
        // Skinned
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
            meshInst.applyMaterial();
        // Nonskinned
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
                meshInst.applyMaterial();
            }
            // add all the kids
            if (current instanceof PJoint ||
                current instanceof PPolygonMeshInstance)
                queue.addAll(current.getChildren());
        }
    }

    /**
     * Construction code that is common to all construction paths.
     * @param wm WorldManager
     * @param attributes attributes to use for initialization
     * @param addEntity True if the entity should be added to the WorldManager
     * @param characterDOM If non-null, use this to do additional setup
     */
    private void commonConstructionCode(WorldManager wm, CharacterAttributes attributes, boolean addEntity, xmlCharacter characterDOM)
    {
        m_wm = wm;
        // Initialize key bindings
        initKeyBindings();
        // The procedural scene graph
        m_pscene = new PScene(attributes.getName(), m_wm);
        // The glue between JME and pscene
        m_jscene = new JScene(m_pscene);
        // Don't render yet
        m_pscene.setRenderStop(true);
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        // Initialize the attributes
        m_attributes = attributes;
        // Apply the attributes file; this also initializes the skeleton
        applyAttributes();
        // Initialize the scene, this adds the skeleton to the scene graph
        initScene(processors);
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
     * @param characterDOM
     * @param character
     * @param attributes
     */
    protected void finalizeInitialization(xmlCharacter characterDOM)
    {
        while (isLoaded() == false) // Bind up skeleton reference
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                logger.warning("Interrupted while waiting on isLoaded()");
            }
        }

        // Nothing below is relevant in the simple test sphere case
        if (m_attributes.isUseSimpleStaticModel())
            return;

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
            facialAnimationState.setCycleMode(PlaybackMode.PlayOnce);
            facialAnimationState.setAnimationSpeed(0.1f); // <-- slow playback speed, anim duration is way short bro
        }
        if (m_skeleton.getAnimationComponent().getGroupCount() > 1)
        {
            m_facialAnimations = new FacialAnimationController(this, 1);
            // Go to default face pose
            m_skeleton.getAnimationState(1).setAnimationSpeed(0.24f);
            m_skeleton.getAnimationState(1).setCurrentCycle(m_defaultFacePose);
            m_skeleton.getAnimationState(1).setCurrentCycleTime(0);
            m_skeleton.getAnimationState(1).setCycleMode(PlaybackMode.PlayOnce);
        }

        // Hook up eyeballs, if eyeballs exist
        if (!(m_attributes instanceof UnimeshCharacterAttributes))
            m_eyes = new CharacterEyes(m_attributes.getEyeballTexture(), this, m_wm);

        // At this point, all meshes have been loaded. We should now ensure that
        // all of the child meshinstances have material states.
        initializeMeshInstanceMaterialStates();

        // Apply remaining customizations
        if (characterDOM != null)
        {
            // Materials
            applyMaterialProperties(characterDOM);
            // Skeletal modifications
            applySkeletalModifications(characterDOM);
        }
        else if (m_attributes instanceof UnimeshCharacterAttributes)
        {
            Repository repo = (Repository)m_wm.getUserData(Repository.class);
            PPolygonSkinnedMeshInstance smi = m_skeleton.getMeshesBySubGroup("FullBody")[0];
            smi.getMaterialRef().setShader(repo.newShader(FleshShader.class));
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

        // the shadow!
        addShadow();

        // Apply the material on everything that was just loaded.
        for (PPolygonSkinnedMeshInstance meshInstance : m_skeleton.getSkinnedMeshInstances())
            meshInstance.applyMaterial();


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
                meshInst.applyMaterial();
            }
            // add all the kids
            if (current instanceof PJoint ||
                current instanceof PPolygonMeshInstance ||
                current instanceof PPolygonModelInstance)
                queue.addAll(current.getChildren());
        }

        // New verlet skeleton manipulator
        EyeBall leftEye = null;
        EyeBall rightEye = null;
        if (m_eyes != null)
        {
            leftEye = m_eyes.leftEyeBall;
            rightEye = m_eyes.rightEyeBall;
        }
        m_skeletonManipulator = new VerletSkeletonFlatteningManipulator(m_leftArm, m_rightArm,
                                                                        leftEye, rightEye,
                                                                        m_skeleton, m_modelInst);
        m_rightArm.setSkeletonManipulator(m_skeletonManipulator);
        m_leftArm.setSkeletonManipulator(m_skeletonManipulator);
        //m_arm.setPointAtLocation(Vector3f.UNIT_Y.mult(2.0f)); // test pointing, set to null to stop pointing

        // Uncomment for verlet arm particle visualization
//        VisuManager vis = new VisuManager("Visualizations", m_wm);
//        vis.setWireframe(false);float handRadius    = 0.15f;
//        vis.addPositionObject(getLeftArm().getWristPosition(), ColorRGBA.magenta, handRadius);
//        vis.addPositionObject(getRightArm().getWristPosition(), ColorRGBA.magenta, handRadius);

        // Associate ourselves with our animation states
        for (AnimationState animState : m_skeleton.getAnimationStates())
            animState.addListener(this);
        
        m_AnimationProcessor.setAnimateFace(m_attributes.isAnimatingFace());
        
        // Initialization extension
        if (m_initialization != null)
            m_initialization.initialize(this);

        m_skeleton.setInstruments((Instrumentation)m_wm.getUserData(Instrumentation.class));
        // Turn on the animation
        if (m_attributes.isAnimateBody())
            m_AnimationProcessor.setEnabled(true);
        // Turn on updates
        m_characterProcessor.start();
        m_pscene.setRenderStop(false);
        m_modelInst.setRenderStop(false);

        // blink if you can hear me
        if (m_eyes != null)
            m_eyes.blink();
        // This is required to inherit the renderstates (light specifically) from the render manager
        m_wm.addToUpdateList(m_jscene);
        m_initialized = true;
    }

//    public void setClothesColors(ColorRGBA topColor, ColorRGBA topSpecColor, ColorRGBA bottomColor, ColorRGBA bottomSpecColor, ColorRGBA shoesColor, ColorRGBA shoesSpecColor)
//    {
//        Repository repo = (Repository)m_wm.getUserData(Repository.class);
//
//        // first the skinned meshes
//        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.getSkinnedMeshInstances();
//        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
//        {
//            PMeshMaterial meshMat = meshInst.getMaterialRef();
//            String tempName = meshInst.getName().toLowerCase();
//            if (tempName.contains("eyegeoshape") ||
//                tempName.contains("tongue")      ||
//                tempName.contains("teeth"))
//            {}
//            else if (tempName.contains("nude") ||
//                     tempName.contains("arms") ||
//                     tempName.contains("hand"))
//            {}
//            else if (tempName.contains("head"))
//            {}
//            else if (tempName.equals("hairashape1")) // HACK
//            {}
//            else // assume to be clothing
//            {
//                AbstractShaderProgram clothingShader = repo.newShader(ClothingShaderSpecColor.class);
//                if (meshInst.getParent().getName().equals("UpperBody"))
//                {
//                    try {
//                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, new float [] {topColor.r, topColor.g, topColor.b}));
//                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, new float [] {topSpecColor.r, topSpecColor.g, topSpecColor.b}));
//                    } catch (NoSuchPropertyException ex) {
//                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//                else if (meshInst.getParent().getName().equals("LowerBody"))
//                {
//                    try {
//                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, new float [] {bottomColor.r, bottomColor.g, bottomColor.b}));
//                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, new float [] {bottomSpecColor.r, bottomSpecColor.g, bottomSpecColor.b}));
//                    } catch (NoSuchPropertyException ex) {
//                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
//                }
//                else if (meshInst.getParent().getName().equals("Feet"))
//                {
//                    try {
//                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, new float [] {shoesColor.r, shoesColor.g, shoesColor.b}));
//                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, new float [] {shoesSpecColor.r, shoesSpecColor.g, shoesSpecColor.b}));
//                    } catch (NoSuchPropertyException ex) {
//                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
//                }
//                else    // White is the default
//                {
//                    float[] colorWhite = { 1, 1, 1 };
//                    try {
//                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, colorWhite));
//                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, colorWhite));
//                    } catch (NoSuchPropertyException ex) {
//                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
//                }
//                meshMat.setCullFace(CullState.Face.None);
//                meshMat.setShader(clothingShader);
//            }
//            // Apply it!
//            meshInst.applyShader();
//        }
//    }
    
    public void setSkinTone(ColorRGBA skinColor)
    {
        float[] skinColorFloats = new float[] { skinColor.r, skinColor.g, skinColor.b };
        // grab a reference to the head shader
        AbstractShaderProgram headShader = null;
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getMeshesBySubGroup("Head"))
        {
            String lowerCaseName = meshInst.getName().toLowerCase();
            if (lowerCaseName.contains("head"))
            {
                headShader = meshInst.getMaterialRef().getShader();
                break;
            }
        }

        // grab a reference to the flesh shaders
        AbstractShaderProgram fleshShader = null;
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
        {
            String lowerCaseName = meshInst.getName().toLowerCase();
            if ( lowerCaseName.contains("nude") ||
                 lowerCaseName.contains("arms") ||
                 lowerCaseName.contains("hand"))// is it flesh?
            {
                fleshShader = meshInst.getMaterialRef().getShader();
                break;
            }
        }

        // Set their material color to the one provided
        try {
            if (headShader != null)
                headShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColorFloats));
            if (fleshShader != null)
                fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColorFloats));
        } catch (NoSuchPropertyException ex) {
            logger.severe(ex.getMessage());
        }
        applyMaterials();
    }
    
    /**
     * Sets shaders on the parts according to the defaults.
     */
    public void setDefaultShaders()
    {
        Repository repo = (Repository)m_wm.getUserData(Repository.class);

        AbstractShaderProgram accessoryShader   = null;
        AbstractShaderProgram eyeballShader     = null;
        // HACK : Used for the one hair we have that is a skinned mesh.
        AbstractShaderProgram specialHairShader = null;

        float[] skinColor = m_attributes.getSkinTone();
        AbstractShaderProgram fleshShader       = null;
        AbstractShaderProgram headShader        = null;
        
        if (m_attributes.isUsingPhongLighting())
            headShader = repo.newShader(PhongFleshShader.class);
        else
            headShader = repo.newShader(FleshShader.class);
        // Set the skin color
        try {
            headShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            logger.severe(ex.getMessage());
        }

        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.getSkinnedMeshInstances();
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            
            specialHairShader = repo.newShader(VertDeformerWithSpecAndNormalMap.class);
            

            PMeshMaterial meshMat = meshInst.getMaterialRef();
            String tempName = meshInst.getName().toLowerCase();
            // is this an eyeball? (also used for tongue and teeth)
            if (tempName.contains("eyegeoshape") ||
                tempName.contains("tongue")      ||
                tempName.contains("teeth"))
            {
                if (meshMat.getTexture(0) != null)
                    meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                eyeballShader     = repo.newShader(EyeballShader.class);
                meshMat.setShader(eyeballShader);
            }
            else if (tempName.contains("nude") ||
                     tempName.contains("arms") ||
                     tempName.contains("hand"))// is it flesh?
            {
                fleshShader       = repo.newShader(FleshShader.class);
                // Set the skin color
                try {
                    fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
                } catch (NoSuchPropertyException ex) {
                    logger.severe(ex.getMessage());
                }
                meshMat.setShader(fleshShader);
            }
            else if (tempName.contains("head"))
                meshMat.setShader(headShader);
            else if (tempName.equals("hairashape1")) // HACK
            {
                try {
                    URL normalMapLocation = new URL("http://www.zeitgeistgames.com/assets/models/collada/Hair/FemaleHair/HairBrownHLBase_N.png");
                    meshMat.setTexture(meshMat.getTexture(0), 2);
                    meshMat.setTexture(normalMapLocation, 1);
                    meshMat.getTexture(1).loadTexture(m_pscene.getRepository());
                    // Change the textures, because we know they load incorrectly.
                    specialHairShader = repo.newShader(VertDeformerWithSpecAndNormalMap.class);
                    meshMat.setShader(specialHairShader);
                    meshMat.setCullFace(CullState.Face.None);
                }
                catch (MalformedURLException ex)
                {
                    // yeah yeah
                }
            }
            else // assume to be clothing
            {
                AbstractShaderProgram clothingShader = repo.newShader(ClothingShaderSpecColor.class);
                if (meshInst.getParent().getName().equals("UpperBody"))
                {
                    try {
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, m_attributes.getShirtColor()));
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, m_attributes.getShirtSpecColor()));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if (meshInst.getParent().getName().equals("LowerBody"))
                {
                    try {
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, m_attributes.getPantsColor()));
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, m_attributes.getPantsSpecColor()));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
                }
                else if (meshInst.getParent().getName().equals("Feet"))
                {
                    try {
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, m_attributes.getShoesColor()));
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, m_attributes.getShoesSpecColor()));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
                }
                else    // White is the default
                {
                    float[] colorWhite = { 1, 1, 1 };
                    try {
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, colorWhite));
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, colorWhite));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
                }
                meshMat.setCullFace(CullState.Face.None);
                meshMat.setShader(clothingShader);
            }
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
            accessoryShader   = repo.newShader(SimpleTNLWithAmbient.class);

            PNode current = queue.removeFirst();
            if (current instanceof PPolygonMeshInstance)
            {
                PPolygonMeshInstance meshInst = (PPolygonMeshInstance) current;
                PMeshMaterial meshMat = meshInst.getMaterialRef();
                if (meshInst.getParent().getName().equals("Hair"))
                {
                    AbstractShaderProgram hairShader = repo.newShader(HairShader.class);
                    try {
                        hairShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, m_attributes.getHairColor()));
                        hairShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, m_attributes.getHairColor()));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    meshMat.setShader(hairShader);
                    meshMat.setCullFace(CullState.Face.None); // Double sided hair
                }
                else
                    meshMat.setShader(accessoryShader);
                meshMat.setCullFace(CullState.Face.None);
                meshInst.applyShader();
            }
            // add all the kids
            if (current instanceof PJoint ||
                current instanceof PPolygonMeshInstance)
                queue.addAll(current.getChildren());
        }
    }

    /** Used for head loading **/
    private boolean m_bWaitingOnAsset = false;
    private SharedAsset asset = null;
    /** Used for internal requests for assets **/
    private final RepositoryUser            headInstaller = new RepositoryUser() {
        @Override
        public void receiveAsset(SharedAsset assetRecieved) {
            // Flip some switches
            asset = assetRecieved;
            countingSemaphore.release();
        }
    };

    public void initializeMeshInstanceMaterialStates() {
        if (m_pscene == null)
        {
            logger.severe("Cannot initialize material states with a null pscene!");
            return;
        }

        FastList<PNode> queue = new FastList<PNode>();
        PPolygonMeshInstance meshInst = null;
        PNode current = null;
        queue.add(m_pscene);
        // traverse!
        while (queue.isEmpty() == false)
        {
            current = queue.removeFirst();
            // Process
            if (current instanceof PPolygonMeshInstance)
            {
                meshInst = (PPolygonMeshInstance) current;
                meshInst.setPScene(m_pscene);
                meshInst.initializeStates(m_pscene);
            }
            // Add the kids
            if (current.getChildrenCount() > 0)
                for (PNode kid : current.getChildren())
                    queue.add(kid);
        }
    }

    /**
     * Load a head file and return the skeleton of the new head.
     * @param headLocation Location of the head to load
     * @param children Will be filled with the children of the new skeleton
     * @return New head skeleton with attachments
     */
    private SkeletonNode loadHeadFile(URL headLocation, List<PNode>children)
    {
        SkeletonNode result = null;
        // Ready a request for the repository
        AssetDescriptor descriptor = new AssetDescriptor(SharedAssetType.COLLADA, headLocation);
        asset = new SharedAsset(m_pscene.getRepository(), descriptor);
        m_bWaitingOnAsset = true;
        // Request it!
        m_pscene.getRepository().loadSharedAsset(asset, headInstaller);
        // Wait for it to load
        try {
            countingSemaphore.acquire();
        } catch (InterruptedException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (asset == null) // Timeout at the repository
            logger.severe("Timed out waiting on asset for new head.");
        else
        {
            // Locate the new skeleton
            PNode psceneInstances = ((PScene)asset.getAssetData()).getInstances();
            int numChildren = psceneInstances.getChildrenCount();
            // Known to be a top level child
            for (int i = 0; i < numChildren; ++i)
            {
                PNode current = psceneInstances.getChild(i);
                if (current instanceof SkeletonNode)
                {
                    result = ((SkeletonNode)current);
                    children.addAll(result.getChildren());
                    result = result.deepCopy();
                    break;
                }
            }
        }
        if (result == null)
            logger.severe("Unable to find skeleton for the new head!");
        return result;
    }

    /**
     * A subset of the functionality in setDefaultShaders
     */
    public void setDefaultHeadShaders()
    {
        Repository repo = (Repository)m_wm.getUserData(Repository.class);

        AbstractShaderProgram eyeballShader = repo.newShader(EyeballShader.class);

        AbstractShaderProgram fleshShader = null;
        if (m_attributes.isUsingPhongLighting())
            fleshShader = repo.newShader(PhongFleshShader.class);
        else
            fleshShader = repo.newShader(FleshShader.class);
        float[] skinColor = m_attributes.getSkinTone();
        if (skinColor == null)
        {
           skinColor = new float [3];
           skinColor[0] = (230.0f/255.0f);
           skinColor[1] = (197.0f/255.0f);
           skinColor[2] = (190.0f/255.0f);
        }
        try {
            fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.retrieveSkinnedMeshes("Head");
        if (smInstances == null) // no subgroup found
            logger.warning("No \"Head\" meshes found during head installation. I will now die with a NPE");
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            PMeshMaterial meshMat = meshInst.getMaterialRef();
            String tempName = meshInst.getName().toLowerCase();

            // is this an eyeball? (also used for tongue and teeth)
            if (tempName.contains("eyegeoshape"))
            {
                meshMat.setShader(eyeballShader.duplicate());
                if (meshMat.getTexture(0) != null)
                    meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
            }
            else if (tempName.contains("tongue") || tempName.contains("teeth"))
            {
                if (meshMat.getTexture(0) != null)
                    meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                meshMat.setShader(eyeballShader.duplicate());
            }
            else
                meshMat.setShader(fleshShader.duplicate());
            // Apply it!
            meshInst.applyShader();
        }
    }

    /**
     * This method performs the skeleton loading based on the attributes isMale
     * method. It also assigns the initialization object from the attributes if
     * one is present.
     */
    private void applyAttributes()
    {
        if (m_attributes == null)
        {
            logger.warning("No attributes, aborting applyAttributes.");
            return;
        }
        // Grab the initialization object if one is present
        m_initialization = m_attributes.getInitializationObject();

        if (m_attributes.isUseSimpleStaticModel() == true)
            return; // Nothing else to be done here

        // eat the skeleton ;)
        if (m_attributes.isMale()) 
           m_skeleton = m_pscene.getRepository().getSkeleton("MaleSkeleton");
        else 
           m_skeleton = m_pscene.getRepository().getSkeleton("FemaleSkeleton");
    }

    /**
     * This method applies all the commands of the CharacterAttributes object.
     * Things such as animation files to load, geometry to remove or add, etc.
     * @param attributes The attributes to process
     */
    private void executeAttributes(CharacterAttributes attributes)
    {
        String urlPrefix = attributes.getBaseURL();
        // If no base url was provided by the character attributes, then it is
        // assumed that the prefix should be the file protocol to the local machine
        // in the current folder.
        if (urlPrefix == null || urlPrefix.length() == 0)
            urlPrefix = new String("file:///" + System.getProperty("user.dir") + File.separatorChar);

        InstructionProcessor instructionProcessor = new InstructionProcessor(m_wm);
        Instruction attributeRoot = new Instruction();
        // Set the skeleton to our skeleton
        attributeRoot.addChildInstruction(InstructionType.setSkeleton, m_skeleton);

        // attach the appropriate head
        URL headLocation = null;
        try {
            if (attributes.getHeadAttachment() != null)
                headLocation = new URL(urlPrefix + attributes.getHeadAttachment());
        }
        catch (MalformedURLException ex)
        {
            logger.severe("Unable to create URL for head attachment, tried to combine \"" +
                   urlPrefix + "\" and \"" + attributes.getHeadAttachment() + "\"");
        }

        // TEMP
        if (headLocation != null) {
            installHeadConfigurationN(headLocation);
            //installHeadConfiguration(headLocation); // Should I parameterize this?
        }

        // Load up any geometry requested by the provided attributes object
        List<String[]> load = attributes.getLoadInstructions();
        if (load != null) {
            for (int i = 0; i < load.size(); i++) {
                String[] attr = load.get(i);
                if (attr.length>0) {
                    if (checkURLPath(urlPrefix + load.get(i)[0]))
                        attributeRoot.addChildInstruction(InstructionType.loadGeometry, urlPrefix + attr[0]);
                    else
                        attributeRoot.addChildInstruction(InstructionType.loadGeometry, checkResourcePath(attr[0]));
                }
            }

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
                    tempsolution = new PMatrix();
                else
                    tempsolution = attachments[i].getMatrix();
                attributeRoot.addAttachmentInstruction( attachments[i].getMeshName(), attachments[i].getParentJointName(), tempsolution, attachments[i].getAttachmentJointName());
            }
        }

        // Load up body animations
        String [] anims = attributes.getAnimations();
        if (anims != null && anims.length > 0) {
            for (int i = 0; i < anims.length; i++) {
                if (checkURLPath(urlPrefix + anims[i]))
                    attributeRoot.addChildInstruction(InstructionType.loadAnimation, urlPrefix + anims[i]);
                else
                    attributeRoot.addChildInstruction(InstructionType.loadAnimation, checkResourcePath(anims[i]));
            }
        }

        // Load up facial animations
        String [] facialAnims = attributes.getFacialAnimations();
        if (facialAnims != null && facialAnims.length > 0) {
            for (int i = 0; i < facialAnims.length; i++) {
                if (checkURLPath(urlPrefix + facialAnims[i]))
                    attributeRoot.addChildInstruction(InstructionType.loadFacialAnimation, urlPrefix + facialAnims[i]);
                else
                    attributeRoot.addChildInstruction(InstructionType.loadFacialAnimation, checkResourcePath(facialAnims[i]));
            }
        }

        // Execute the instruction tree
        instructionProcessor.execute(attributeRoot, false);
        if (attributes instanceof UnimeshCharacterAttributes)
        {
            UnimeshCharacterAttributes uniAttribs = (UnimeshCharacterAttributes)attributes;
            // find the skeleton node from the loading pscene
            FastList<PNode> queue = new FastList<PNode>();
            queue.add(instructionProcessor.getLoadingPScene().getInstances());

            PNode current = null;
            SkeletonNode skelly = null;
            while (!queue.isEmpty())
            {
                current = queue.removeFirst();
                if (current instanceof SkeletonNode) // found it!
                {
                    skelly = (SkeletonNode)current;
                    break;
                }
                // add all children
                if (current.getChildrenCount() > 0)
                    queue.addAll(current.getChildren());
            }
            uniAttribs.modifiedSkeleton = skelly;
        }
    }

    private boolean checkURLPath(String path) {
        try {
            URL urlPath     = new URL(path);
            InputStream is  = urlPath.openStream();
            is.close();
            return true;
        } catch (MalformedURLException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    private String checkResourcePath(String path) {
        String szResource   = null;
        URL resourcePath    = getClass().getResource(File.separatorChar + path);
        if (resourcePath != null) {
            return resourcePath.toString();
        }
        return szResource;
    }

    /**
     * Loads the model if using a simple static model and sets processors for the
     * character. This method also creates a <code>PPolygonModelInstance</code>
     * to place the skeleton and other child nodes under.
     * @param processors
     */
    protected void initScene(ArrayList<ProcessorComponent> processors)
    {
        if (m_attributes.isUseSimpleStaticModel())
        {
            if (m_attributes.getSimpleScene() == null)
            {
                m_modelInst = m_pscene.addModelInstance("Character", null, m_attributes.getOrigin());
                m_modelInst.addChild(new PNode("not a place holder"));
            } 
            else
            {
                SharedAsset modelAsset = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D_Mesh, ""));
                modelAsset.setAssetData(m_attributes.getSimpleScene());
                m_modelInst = m_pscene.addModelInstance("Character", modelAsset, m_attributes.getOrigin());
            }
        }
        else // Otherwise create the appropriate heirarchy
        {
            m_modelInst = new PPolygonModelInstance(m_attributes.getName());
            m_modelInst.setRenderStop(true); // don't start rendering until we finish
            m_modelInst.addChild(m_skeleton); // Skeleton is for our model instance
            m_pscene.addInstanceNode(m_modelInst);

            // Debugging / Diagnostic output
//            Logger.getLogger(Character.class.getName()).log(Level.INFO, "Model " + m_pscene + "  inst " + m_modelInst);
            m_AnimationProcessor = new CharacterAnimationProcessor(m_modelInst, m_wm);
            // Start the animation processor disabled until we finish loading
            m_AnimationProcessor.setEnabled(false);
            processors.add(m_AnimationProcessor);
        }
        // Used for updates, etc.
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
        GameContext contextT = m_registry.get(transition.getContextMessageName());
        if (contextT == null)
            return false;

        Class contextClass = contextT.getClass();
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
                bool = method.invoke(contextT, transition.getContextMessageArgs());
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
                    if (contextT.excecuteTransition(transition))
                    {
                        m_context.setCurrentState(null); // calls the stateExit()
                        m_context = contextT;
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
        matState.setColorMaterial(ColorMaterial.Diffuse);
        matState.setDiffuse(ColorRGBA.white);

        // Light state
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setEnabled(true);

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
//        if (!m_initialized)
//            setMeshAndSkeletonRefs();

        if (m_attributes.isUseSimpleStaticModel())
            m_modelInst.setDirty(true, true);
        if (m_facialAnimations != null)
            m_facialAnimations.update(deltaTime);
        if (m_context != null)
            m_context.update(deltaTime);
        if (m_eyes != null)
            m_eyes.update(deltaTime);
        if (m_rightArm != null)
            m_rightArm.update(deltaTime);
        if (m_leftArm != null)
            m_leftArm.update(deltaTime);

        if (m_updateExtension != null)
            m_updateExtension.update(deltaTime);
    }

    /**
     * If the model instance doesn't have kids then it is not loaded yet,
     * also checking if the first kid is a place holder.
     * If the first kid is not a SkeletonNode then this character
     * is using a simple scene for debugging visualization.
     */
    private boolean isLoaded()
    {
        // safety against place holders
        if (m_modelInst.getChildrenCount() <= 0 || m_modelInst.getChild(0) instanceof SharedAssetPlaceHolder)
            return false;

        // Simple sphere model case
        if (!(m_modelInst.getChild(0) instanceof SkeletonNode))
        {
            m_skeleton   = null;
            m_jscene.updateRenderState();
            
            // Initialization extension, special spot for the simple model case
            if (m_initialization != null)
                m_initialization.initialize(this);
            
            m_characterProcessor.start();
            m_pscene.setRenderStop(false);
            m_modelInst.setRenderStop(false);

            m_initialized = true;
        }

        return true;
    }

    /**
     * Use this convenience method to animate a facial expression for a short
     * amount of time.
     * @param cycleName The name of the facial animation cycle to play
     * @param fTransitionTime How long should the transition take
     * @param fExpressionDuration How long the pose should be held
     */
    public void initiateFacialAnimation(String cycleName, float fTransitionTime, float fExpressionDuration)
    {
        if (m_skeleton == null) // Not ready to handle facial animations yet
            return;
        if (m_facialAnimations == null)
        {
            AnimationComponent ac = m_skeleton.getAnimationComponent();
            if (ac.getGroupCount() > 1)
                m_facialAnimations = new FacialAnimationController(this, 1);
            else
                return;
        }
        int cycle = m_skeleton.getAnimationGroup(1).findAnimationCycleIndex(cycleName);
        if (cycle != -1)
            initiateFacialAnimation(cycle, fTransitionTime, fExpressionDuration);
    }

    /**
     * Convenience method for playing facial animations.
     * @param cycleIndex The index of the desired facial animation cycle
     * @param fTransitionTime How long should the transition take
     * @param fExpressionDuration How long the pose should be held
     */
    public void initiateFacialAnimation(int cycleIndex, float fTransitionTime, float fExpressionDuration)
    {
        if (m_facialAnimations == null)
        {
            if (m_skeleton.getAnimationComponent().getGroupCount() > 1)
                m_facialAnimations = new FacialAnimationController(this, 1);
            else
                return;
        }
        m_facialAnimations.queueFacialAnimation(fTransitionTime, // Time in
                                                fTransitionTime, // Time out
                                                fExpressionDuration, // Hold time
                                                cycleIndex, // Cycle to play
                                                PlaybackMode.PlayOnce); // Play mode
     }

    /**
     * Sets the camera on this character.
     */
    public void setCameraOnMe()
    {
        // TODO !
        //m_wm.getUserData(arg0);
    }

    /**
     * Adds this character to an object collection,
     * the collection will be used to receive obstacles to avoid
     * while using steering behaviors and to find objects such as chairs to
     * sit on for e.g.
     * @param objs
     */
    public void setObjectCollection(ObjectCollectionBase objs)
    {
        m_objectCollection = objs;
        objs.addObject(this);
    }

    /**
     * Retrieve the object collection this character is associated with.
     * @return
     */
    public ObjectCollectionBase getObjectCollection() {
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
        result.setCenter(m_modelInst.getTransform().getWorldMatrix(false).getTranslation().add(result.getCenterRef()));
        return result;
    }

    /**
     * Sets the sphere data that is built around the model instances bounding sphere.
     * @param output
     */
    public void getBoundingSphere(PSphere output)
    {
        if (m_modelInst.getBoundingSphere() == null)
            m_modelInst.calculateBoundingSphere();
        output.setRadius(m_modelInst.getBoundingSphere().getRadius());
        output.setCenter(m_modelInst.getTransform().getWorldMatrix(false).getTranslation().add(m_modelInst.getBoundingSphere().getCenterRef()));
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

    public Vector3f getPositionRef() {
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

    @Override
    public void receiveAnimationMessage(AnimationMessageType message, int stateID)
    {
        if (m_context != null)
            m_context.notifyAnimationMessage(message, stateID);

    }

    public FacialAnimationController getFacialAnimationQ()
    {
        if (m_facialAnimations == null && m_skeleton != null)
        {
            if (m_skeleton.getAnimationComponent().getGroupCount() > 1)
                m_facialAnimations = new FacialAnimationController(this, 1);
        }
        return m_facialAnimations;
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
    @Deprecated
    public void saveConfiguration(File location)
    {
        try {
            if (location.exists() == true && location.canWrite() == false)
                throw new IOException("Request file (" + location.toString() + ") is not writeable.");
            else if (location.exists() == false)
                location.createNewFile();

            saveConfiguration(new BufferedOutputStream(new FileOutputStream(location)));
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
     * Save the characters configuration to the supplied output stream
     *
     * @param out OutputStream to save data to
     */
    public void saveConfiguration(OutputStream out) throws JAXBException {
        final Marshaller m = context.createMarshaller();
        // Pretty files please
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);

        xmlCharacter characterDom = generateCharacterDOM();
        m.marshal( characterDom, out);
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
                SkinnedMeshJoint targetJoint = m_skeleton.getSkinnedMeshJoint(jMod.getTargetJointName());
                if (targetJoint != null)
                {
                    // Apply customizations
                    if (jMod.getLocalModifierMatrix() != null)
                        jMod.getLocalModifierMatrix().getPMatrix(targetJoint.getLocalModifierMatrix());
                    if (jMod.getBindPoseMatrix() != null)
                        jMod.getBindPoseMatrix().getPMatrix(targetJoint.getBindPose());
                }
                else
                    logger.log(Level.WARNING,
                            "Target joint not found for modifier: " + jMod.getTargetJointName());
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
            PMeshMaterial meshMat = new PMeshMaterial(xmlMat, m_wm, m_attributes.getBaseURL());
            String targetMeshName = xmlMat.getTargetMeshName();
            // find the mesh it belongs to
            PPolygonMeshInstance meshInst = (PPolygonMeshInstance)getSkeleton().findChild(targetMeshName);
            if (meshInst != null)
            {
                // Sweet! Apply the material
                meshInst.setMaterial(meshMat);
                meshInst.applyMaterial();
                meshInst.getGeometry().setSubmitGeometry(true);
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

    /**
     * Get the update extension object (not used by default)
     * @return
     */
    public Updatable getUpdateExtension() {
        return m_updateExtension;
    }

    /**
     * Set an updatable object to extened functionality
     * @param updateExtension
     */
    public void setUpdateExtension(Updatable updateExtension) {
        this.m_updateExtension = updateExtension;
    }

    /**
     * Release all resources
     */
    public void destroy() {
        if (m_wm == null)
            return;

        ProcessorCollectionComponent pcc = (ProcessorCollectionComponent) getComponent(ProcessorCollectionComponent.class);
        pcc.removeAllProcessors();
        // Something needs to wait until this is finished
        m_characterProcessor.setEnabled(false);
        m_characterProcessor.stop();
        if (m_AnimationProcessor!=null)
            m_AnimationProcessor.setEnabled(false);

        m_wm.removeEntity(this);

        m_keyBindings           = null;
        m_context               = null;
        m_attributes            = null;
        m_registry              = null;
        m_modelInst             = null;
        m_skeleton              = null;
        m_objectCollection      = null;
        m_AnimationProcessor    = null;
        m_characterProcessor    = null;
        m_facialAnimations      = null;
        m_eyes                  = null;
        m_rightArm              = null;
        m_leftArm               = null;
        m_skeletonManipulator   = null;
    }

    /**
     * Sets the characters head into bigHead mode.
     * @param fScale The new head scale, 1.0 for no weird scaling
     */
    public void setBigHeadMode(float fScale)
    {
        SkinnedMeshJoint joint = getSkeleton().getSkinnedMeshJoint("Head");
        getSkeleton().setJointPosition("Head", new Vector3f(0.0f, 0.11902f + 0.07f * (fScale - 1), 0.0f));

        joint.getBindPose().setScale(fScale);
        SkinnedMeshJoint rhand = getSkeleton().getSkinnedMeshJoint("rightHand");
        rhand.getBindPose().setScale(fScale);
        SkinnedMeshJoint lhand = getSkeleton().getSkinnedMeshJoint("leftHand");
        lhand.getBindPose().setScale(fScale);
        SkinnedMeshJoint rfeet = getSkeleton().getSkinnedMeshJoint("rightFoot");
        rfeet.getBindPose().setScale(fScale);
        SkinnedMeshJoint lfeet = getSkeleton().getSkinnedMeshJoint("leftFoot");
        lfeet.getBindPose().setScale(fScale);

        joint = getSkeleton().getSkinnedMeshJoint("Hips");
        getSkeleton().setJointPosition("Hips", new Vector3f(0.0f, 1.10598f + 0.09f * (fScale - 1), 0.0f));
    }


    /**
     * Make fists with the specified hands
     * @param rightHand True to have a fist with the right hand
     * @param leftHand True to have a fist with the left hand
     */
    public void makeFist(boolean rightHand, boolean leftHand)
    {
        if (rightHand)
        {

        }
        else // un-fist
        {
            FastList<PNode> queue = new FastList<PNode>();
            queue.addAll(getSkeleton().getSkinnedMeshJoint("leftHand").getChildren());
            while (queue.isEmpty() == false)
            {
                PNode current = queue.removeFirst();
                if (!(current instanceof SkinnedMeshJoint))
                    continue; // Not relevant
                ((SkinnedMeshJoint)current).resetBindPose();
                if (current.getChildrenCount() > 0)
                    queue.addAll(current.getChildren());
            }
        }
        if (leftHand)
        {
            PMatrix xRotMat = new PMatrix();
            Vector3f xRotation = new Vector3f((float)(Math.PI * -0.4), 0.01f, 0);
            xRotMat.setRotation(xRotation);
            FastList<PNode> queue = new FastList<PNode>();
            queue.addAll(getSkeleton().getSkinnedMeshJoint("leftPalm").getChildren());
            queue.add(getSkeleton().getSkinnedMeshJoint("leftHandThumb2"));
            while (queue.isEmpty() == false)
            {
                PNode current = queue.removeFirst();
                if (!(current instanceof SkinnedMeshJoint))
                    continue;

                SkinnedMeshJoint joint = (SkinnedMeshJoint)current;
                PMatrix bindPose = joint.getBindPose();
                bindPose.mul(xRotMat);
                if (current.getChildrenCount() > 0)
                    queue.addAll(current.getChildren());
            }

            // Set palm transform
            float[] matFloats =
            {
                0.927f,-0.375f, 0f,         -0.018f,
                0.366f, 0.907f, -0.208f,    0.024f,
                0.078f, 0.193f, 0.978f,		0.004f,
                0,		0,		0,          1
            };

            getSkeleton().getSkinnedMeshJoint("leftPalm").getBindPose().set(matFloats);
        }
        else // un-fist
        {
            FastList<PNode> queue = new FastList<PNode>();
            queue.addAll(getSkeleton().getSkinnedMeshJoint("leftHand").getChildren());
            while (queue.isEmpty() == false)
            {
                PNode current = queue.removeFirst();
                if (!(current instanceof SkinnedMeshJoint))
                    continue; // Not relevant
                ((SkinnedMeshJoint)current).resetBindPose();
                if (current.getChildrenCount() > 0)
                    queue.addAll(current.getChildren());
            }
        }

    }

    /**
     * Call this method to attach a light gray untextured jME cube to the avatar.
     * This is useful for debugging lighting errors, among other things.
     */
    private void attachDebuggingJMECube()
    {
        Box newBox = new Box("box", Vector3f.UNIT_XYZ.negate().mult(0.1f), Vector3f.UNIT_XYZ.mult(0.1f));
        Node boxNode = new Node();
        boxNode.attachChild(newBox);
        boxNode.setName("BoxNode");
        boxNode.setLocalTranslation(0, 2.0f, 0);
        RenderManager rm = m_wm.getRenderManager();
        // Z Buffer State
        ZBufferState buf = (ZBufferState) rm.createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) rm.createRendererState(RenderState.RS_MATERIAL);
        matState.setAmbient(ColorRGBA.lightGray);
        matState.setDiffuse(ColorRGBA.lightGray);
        matState.setEmissive(ColorRGBA.black);
        matState.setSpecular(ColorRGBA.black);
        matState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        matState.setColorMaterial(MaterialState.ColorMaterial.Diffuse);
        matState.setEnabled(true);

        LightState ls = (LightState) rm.createRendererState(RenderState.RS_LIGHT);
        ls.setTwoSidedLighting(false);
        ls.setEnabled(true);
        
        // Cull State
        CullState cs = (CullState) rm.createRendererState(RenderState.RS_CULL);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        boxNode.setRenderState(buf);
        boxNode.setRenderState(ls);
        boxNode.setRenderState(matState);
        boxNode.updateRenderState();
        
        m_jscene.getExternalKidsRoot().attachChild(boxNode);
    }

    public void installHead(URL headLocation)
    {
        installHeadConfiguration(headLocation);
        // hook the eyeballs and such back up
        m_eyes = new CharacterEyes(m_attributes.getEyeballTexture(), this, m_wm);
        m_skeletonManipulator.setLeftEyeBall(m_eyes.leftEyeBall);
        m_skeletonManipulator.setRightEyeBall(m_eyes.rightEyeBall);
    }

    /**
     * NEW HEAD INSTALL SETUP... Chops off head and does a straight replace w/o
     * any modifications.  Kills any facial animations in the binary body skeleton.
     * Adds all the SkinnedMeshInstances into the the headsubgroup onto the
     * complete skeleton.
     * @param headLocation
     */
    public void installHeadN(URL headLocation) {

        installHeadConfigurationN(headLocation);
        // hook the eyeballs and such back up
        m_eyes = new CharacterEyes(m_attributes.getEyeballTexture(), this, m_wm);
        m_skeletonManipulator.setLeftEyeBall(m_eyes.leftEyeBall);
        m_skeletonManipulator.setRightEyeBall(m_eyes.rightEyeBall);
    }

    protected void installHeadConfigurationN(URL headLocation) {

        if (checkBinaryFileExtension(headLocation)) {
            installBinaryHeadConfiguration(headLocation);
            return;
        }

        // Stop all of our processing.
        m_skeleton.setRenderStop(true);
        boolean animProcEnabled = m_AnimationProcessor.isEnabled();
        boolean charProcEnabled = m_characterProcessor.isEnabled();
        m_AnimationProcessor.setEnabled(false);
        m_characterProcessor.setEnabled(false);

                // Create parameters for the collada loader we will use
        ColladaLoaderParams params  = new ColladaLoaderParams(true,     true,   // load skeleton,   load geometry
                                                              false,    false,  // load animations, show debug info
                                                              4,                // max influences per-vertex
                                                              "HeadSkeleton",   // 'name'
                                                              null);            // existing skeleton (if applicable)

        String tmp = headLocation.getPath();
        int begin   = tmp.lastIndexOf('/');
        int end     = tmp.lastIndexOf(".");
        String name = tmp.substring(begin+1, end);


        // Load the skeleton
        Collada loader = new Collada(params);
        try {
            loader.load(new PScene(m_wm), headLocation);
        }
        catch (ColladaLoadingException ex)
        {
            logger.severe(ex.getMessage());
        }
        SkeletonNode newHeadSkeleton = loader.getSkeletonNode();
        newHeadSkeleton.setName(name);

        // Chop of the head and put it on our body with no modifications
        m_skeleton.clearSubGroup("Head");

        // Kills any facial animations that may be baked into the binary body skel
        AnimationGroup facial = m_skeleton.getAnimationGroup(1);
        if (facial != null)
            m_skeleton.getAnimationComponent().removeGroup(facial);

        SkinnedMeshJoint copyJoint  = newHeadSkeleton.getSkinnedMeshJoint("Neck");
        SkinnedMeshJoint origJoint  = m_skeleton.getSkinnedMeshJoint("Neck");
        origJoint.getParent().replaceChild(origJoint, copyJoint, false);
        m_skeleton.refresh();
        
        // Get all the SkinnedMeshInstances & place it in the head subgroup
        List<PPolygonSkinnedMeshInstance> skinnedMeshList = newHeadSkeleton.getSkinnedMeshInstances();
        if (skinnedMeshList.size() == 0) {
            logger.warning("No skinned mesh instances found in skeleton. Do you have meshes instead?");
            List<PPolygonSkinnedMesh> ppsmList = newHeadSkeleton.getAllSkinnedMeshes();
            if (ppsmList != null || ppsmList.size() > 0) {
                for (PPolygonSkinnedMesh pPolygonSkinnedMesh : ppsmList) {
                    PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance) m_pscene.addMeshInstance(pPolygonSkinnedMesh, new PMatrix());
                    meshInst.setAndLinkSkeletonNode(m_skeleton);
                    m_skeleton.addToSubGroup(meshInst, "Head");
                }
            }
        }

        for (PPolygonSkinnedMeshInstance meshInst : skinnedMeshList) {
            meshInst.setAndLinkSkeletonNode(m_skeleton);
            m_skeleton.addToSubGroup(meshInst, "Head");
        }

        // Finally, apply the default shaders
        setDefaultHeadShaders();

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnabled(animProcEnabled);
        m_characterProcessor.setEnabled(charProcEnabled);
        m_skeleton.setRenderStop(false);
    }

    /**
     * This method provides a way to install a head that is a derivative of the
     * base skeleton. Skeleton Deltas are generated and applied.
     * @param headLocation The location of a file with a head to load.
     */
    protected void installHeadConfiguration(URL headLocation)
    {
        if (checkBinaryFileExtension(headLocation)) {
            installBinaryHeadConfiguration(headLocation);
            return;
        }

        // Stop all of our processing.
        m_skeleton.setRenderStop(true);
        boolean animProcEnabled = m_AnimationProcessor.isEnabled();
        boolean charProcEnabled = m_characterProcessor.isEnabled();
        m_AnimationProcessor.setEnabled(false);
        m_characterProcessor.setEnabled(false);

        List<PNode> newSkeletonChildren = new ArrayList<PNode>();
        SkeletonNode newHeadSkeleton = loadHeadFile
                (headLocation, newSkeletonChildren);
        // Get rid of all the old stuff
        m_skeleton.clearSubGroup("Head");

        // Now fix the skeletal differences from the Neck through the heirarchy
        ArrayList<Integer> BFTIndices = new ArrayList<Integer>();
        m_skeleton.getSkinnedMeshJointIndices("Neck", BFTIndices);
        m_skeleton.applyConfiguration(newHeadSkeleton, BFTIndices);

        // Process the associated geometry and attach it to ourselves
        for (PNode node : newSkeletonChildren)
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

//        while (m_skeleton.getAnimationComponent().getGroupCount() < m_skeleton.getAnimationStateCount())
//            m_skeleton.addAnimationState(new AnimationState(m_skeleton.getAnimationStateCount()));
        
        // Finally, apply the default shaders
        setDefaultHeadShaders();

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnabled(animProcEnabled);
        m_characterProcessor.setEnabled(charProcEnabled);
        m_skeleton.setRenderStop(false);
    }

    protected void installBinaryHeadConfiguration(URL headLocation)
    {
        // Stop all of our processing.
        m_skeleton.setRenderStop(true);
        boolean animProcEnabled = m_AnimationProcessor.isEnabled();
        boolean charProcEnabled = m_characterProcessor.isEnabled();
        m_AnimationProcessor.setEnabled(false);
        m_characterProcessor.setEnabled(false);

        SkeletonNode newHeadSkeleton = m_binaryExporter.processBinaryData(headLocation);
        attatchHeadSkeleton(m_skeleton, newHeadSkeleton, m_pscene, m_wm);
        setDefaultHeadShaders();
        initializeMeshInstanceMaterialStates();

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnabled(animProcEnabled);
        m_characterProcessor.setEnabled(charProcEnabled);
        m_skeleton.setRenderStop(false);
    }

    private void attatchHeadSkeleton(SkeletonNode bodySkeleton, SkeletonNode headSkeleton, PScene pscene, WorldManager wm) {
        List<PPolygonSkinnedMeshInstance> skinnedMeshList           = headSkeleton.getSkinnedMeshInstances();
        SkinnedMeshJoint copyJoint                                  = headSkeleton.getSkinnedMeshJoint("Neck");

        SkinnedMeshJoint originalJoint                              = bodySkeleton.getSkinnedMeshJoint("Neck");
        bodySkeleton.clearSubGroup("Head");

        if (bodySkeleton.getAnimationGroup(1) != null)
            bodySkeleton.getAnimationGroup(1).clear();

        originalJoint.getParent().replaceChild(originalJoint, copyJoint, false);
        bodySkeleton.refresh();

        if (skinnedMeshList.size() == 0)
            logger.warning("No skinned mesh instances found in skeleton. Do you have meshes instead?");

        for (PPolygonSkinnedMeshInstance meshInst : skinnedMeshList)
            bodySkeleton.addToSubGroup(meshInst, "Head");

        if (bodySkeleton.getAnimationComponent().getGroupCount() > 1) {
            for (int i = 0; i < headSkeleton.getAnimationGroupCount(); i++) {
                for (AnimationCycle cycle : headSkeleton.getAnimationGroup(i).getCycles())
                    bodySkeleton.getAnimationGroup(1).addCycle(cycle);
            }
        } else {
            AnimationGroup facialAnims = new AnimationGroup("FacialAnimations");
            for (int i = 0; i < headSkeleton.getAnimationGroupCount(); i++) {
                for (AnimationCycle cycle : headSkeleton.getAnimationGroup(i).getCycles())
                    facialAnims.addCycle(cycle);
            }
            bodySkeleton.getAnimationComponent().addGroup(facialAnims);
        }

        // synch up animation states with groups
        while (bodySkeleton.getAnimationComponent().getGroupCount() < bodySkeleton.getAnimationStateCount())
            bodySkeleton.addAnimationState(new AnimationState(bodySkeleton.getAnimationStateCount()));
    }

    private boolean checkBinaryFileExtension(URL fileLocation) {

        int index  = fileLocation.toString().lastIndexOf(".");
        String ext = fileLocation.toString().substring(index);
        if (ext.toLowerCase().contains("bin") || ext.toLowerCase().contains("bhf"))
            return true;
        return false;
    }

    @Deprecated
    private void generateDeltas(SkeletonNode newSkeleton, String rootJointName)
    {
        if (newSkeleton == null || m_skeleton == null)
        {
            logger.severe(getName() + " can not install head, got a null skeleton! current one: " + m_skeleton + " new one: " + newSkeleton);
            return;
        }

        // Gather the joints and set the new local modifiers
        SkinnedMeshJoint attachmentJoint = m_skeleton.getSkinnedMeshJoint(rootJointName);
        LinkedList<PNode> list = new LinkedList<PNode>();
        list.add(attachmentJoint);
        PNode current = null;
        while(!list.isEmpty())
        {
            // Grab the next guy
            current = list.poll();
            // Process him! If not a skinned mesh joint skip and prune
            if (current instanceof SkinnedMeshJoint)
            {
                SkinnedMeshJoint currentHeadJoint = (SkinnedMeshJoint)current;
                SkinnedMeshJoint newHeadJoint     = newSkeleton.getSkinnedMeshJoint(currentHeadJoint.getName());

                if (newHeadJoint == null) // Not found in the new skeleton
                    logger.severe("Could not find associated joint in the new skeleton, joint name was " + currentHeadJoint.getName());
                currentHeadJoint.getBindPose().set(newHeadJoint.getBindPose());


            }
            else
                continue; // Prune (kids are not added to the list)
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
    }
}
