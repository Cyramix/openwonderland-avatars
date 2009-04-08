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
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.character.objects.ObjectCollectionBase;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.TransitionObject;
import imi.scene.Updatable;
import imi.loaders.Instruction;
import imi.loaders.Instruction.InstructionType;
import imi.loaders.InstructionProcessor;
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
import imi.utils.BinaryExporter;
import imi.utils.instruments.Instrumentation;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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
    protected PPolygonModelInstance         m_shadowModel           = null; // Quad!
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
    private   InitializationInterface       m_initialization        = null;
    /** index of the 'default' facial animation **/
    private   int                           m_defaultFacePose       = 0; // No more 'playAll' cycle
    private   float                         m_defaultFacePoseTiming = 0.1f;
    private   VerletSkeletonFlatteningManipulator m_skeletonManipulator   = null;

    protected BinaryExporter                m_binaryExporter        = new BinaryExporter();

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
        this(configurationFile, wm, null);
    }

    public Character(URL configurationFile, WorldManager wm, String baseURL)
    {
        super("InterimEntityName");
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
                xmlAttributes.setBaseURL(baseURL);
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


    /**
     * Create a shadow for this character if one is not already present
     */
    public void addShadow() {
        if (m_shadowModel == null) // Not parameterized, no sense in remaking shadows
        {
            // make shadow, minor offset to avoid Z-fighting with the y=0 plane
            Vector3f pointOne =     new Vector3f( 0.45f, 0.001f,  0.5f);
            Vector3f pointTwo =     new Vector3f(-0.45f, 0.001f,  0.5f);
            Vector3f pointThree =   new Vector3f(-0.45f, 0.001f, -0.5f);
            Vector3f pointFour =    new Vector3f( 0.45f, 0.001f, -0.5f);
            // UV sets, standard texturing
            Vector2f uvSetOne =     new Vector2f(0, 0);
            Vector2f uvSetTwo =     new Vector2f(1, 0);
            Vector2f uvSetThree =   new Vector2f(1, 1);
            Vector2f uvSetFour =    new Vector2f(0, 1);

            // Use a transparent material with a blob shadow texture
            PMeshMaterial shadowMaterial = new PMeshMaterial("ShadowMaterial");
            URL path = getClass().getResource("/textures/shadow.png");
            shadowMaterial.setTexture(path, 0);
//            shadowMaterial.setTexture("assets/textures/shadow.png", 0, m_attributes.getBaseURL());
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
            m_shadowModel = m_pscene.addModelInstance(shadowMesh, new PMatrix());
            // Attached to skeleton to get free dirtiness propagation
            m_skeleton.getSkeletonRoot().addChild(m_shadowModel);
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
     * @param characterDOM
     * @param character
     * @param attributes
     */
    protected void finalizeInitialization(xmlCharacter characterDOM)
    {
        while (isLoaded() == false) // Bind up skeleton reference, etc
        {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
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

            // Smile when comming in
            //initiateFacialAnimation(1, 0.4f, 2.75f);
        }

        // Hook up eyeballs
        m_eyes = new CharacterEyes(m_attributes.getEyeballTexture(), this, m_wm);

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
        m_skeletonManipulator = new VerletSkeletonFlatteningManipulator(m_leftArm, m_rightArm, m_eyes.getLeftEyeBall(), m_eyes.getRightEyeBall(), m_skeleton, m_modelInst);
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
        m_AnimationProcessor.setEnable(true);
        // Turn on updates
        m_characterProcessor.start();
        m_pscene.setRenderStop(false);
        m_modelInst.setRenderStop(false);

        m_initialized = true;
    }
    
    /**
     * Sets shaders on the parts according to the defaults.
     */
    public void setDefaultShaders()
    {
        Repository repo = (Repository)m_wm.getUserData(Repository.class);

        AbstractShaderProgram accessoryShader = repo.newShader(SimpleTNLWithAmbient.class);
        AbstractShaderProgram eyeballShader = repo.newShader(EyeballShader.class);
        // HACK
        AbstractShaderProgram specialHairShader = repo.newShader(VertDeformerWithSpecAndNormalMap.class);

        float[] skinColor = m_attributes.getSkinTone();
        AbstractShaderProgram fleshShader = repo.newShader(FleshShader.class);
        AbstractShaderProgram headShader = null;
        
        if (m_attributes.isUsingPhongLighting())
            headShader = repo.newShader(PhongFleshShader.class);
        else
            headShader = repo.newShader(FleshShader.class);
        try {
            headShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.getSkinnedMeshInstances();
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            PMeshMaterial meshMat = meshInst.getMaterialRef();
            String tempName = meshInst.getName().toLowerCase();
            // is this an eyeball? (also used for tongue and teeth)
            if (tempName.contains("eyegeoshape") ||
                tempName.contains("tongue")      ||
                tempName.contains("teeth"))
            {
                if (meshMat.getTexture(0) != null)
                    meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                meshMat.setShader(eyeballShader);
            }
            else if (tempName.contains("nude") ||
                     tempName.contains("arms") ||
                     tempName.contains("hand"))// is it flesh?
                meshMat.setShader(fleshShader);
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
            m_bWaitingOnAsset = false;
        }
    };

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
        while (m_bWaitingOnAsset == true) // wait until loaded
        {
            Thread.yield();
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
    private void setDefaultHeadShaders()
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
            logger.warning("No subgroups found during head installation");
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            PMeshMaterial meshMat = meshInst.getMaterialRef();
            String tempName = meshInst.getName().toLowerCase();

            // is this an eyeball? (also used for tongue and teeth)
            if (tempName.contains("eyegeoshape"))
            {
                meshMat.setShader(eyeballShader);
                if (meshMat.getTexture(0) != null)
                    meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
            }
            else if (tempName.contains("tongue") || tempName.contains("teeth"))
            {
                if (meshMat.getTexture(0) != null)
                    meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                meshMat.setShader(eyeballShader);
            }
            else
                meshMat.setShader(fleshShader);
            // Apply it!
            meshInst.applyShader();
        }
    }

    /**
     * This method applies the m_attributes member. It simply wraps
     * loadSkeleton 
     */
    private void applyAttributes()
    {
        if (m_attributes == null)
        {
            logger.warning("No attributes, aborting applyAttributes.");
            return;
        }
        m_initialization = m_attributes.getInitializationObject();
        if (m_attributes.isUseSimpleStaticModel() == true)
            return; // Nothing else to be done here

        // eat the skeleton ;)
        if (m_attributes.isMale()) {
           m_skeleton = m_pscene.getRepository().getSkeleton("MaleSkeleton");
//           loadSkeleton(maleSkeleton);
        }
        else {
           m_skeleton = m_pscene.getRepository().getSkeleton("FemaleSkeleton");
//           loadSkeleton(femaleSkeleton);
        }

//        if (m_skeleton == null) // problem
//        {
//            logger.severe("Unable to load skeleton. Aborting applyAttributes.");
//            return;
//        }
//        else
//        {
//            // synch up animation states with groups
//            while (m_skeleton.getAnimationComponent().getGroupCount() < m_skeleton.getAnimationStateCount())
//                m_skeleton.addAnimationState(new AnimationState(m_skeleton.getAnimationStateCount()));
//        }
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
            urlPrefix = new String("file:///" + System.getProperty("user.dir") + "/");
        // attach the appropriate head
        URL headLocation = null;
        try {
            headLocation = new URL(urlPrefix + attributes.getHeadAttachment());
        }
        catch (MalformedURLException ex)
        {
            logger.severe("Unable to create URL for head attachment, tried to combine \"" +
                   urlPrefix + "\" and \"" + attributes.getHeadAttachment() + "\"");
        }
        if (headLocation != null)
            installHeadConfiguration(headLocation); // Should I parameterize this?

        InstructionProcessor instructionProcessor = new InstructionProcessor(m_wm);
        Instruction attributeRoot = new Instruction();
        // Set the skeleton to our skeleton
        attributeRoot.addChildInstruction(InstructionType.setSkeleton, m_skeleton);
        // Load up any geometry requested by the provided attributes object
        List<String[]> load = attributes.getLoadInstructions();
        if (load != null) {
            for (int i = 0; i < load.size(); i++)
                attributeRoot.addChildInstruction(InstructionType.loadGeometry, urlPrefix + load.get(i)[0]);
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
                attributeRoot.addChildInstruction(InstructionType.loadAnimation, urlPrefix + anims[i]);
            }
        }

        // Load up facial animations
        String [] facialAnims = attributes.getFacialAnimations();
        if (facialAnims != null && facialAnims.length > 0) {
            for (int i = 0; i < facialAnims.length; i++) {
                attributeRoot.addChildInstruction(InstructionType.loadFacialAnimation, urlPrefix + facialAnims[i]);
            }
        }

        // Execute the instruction tree
        instructionProcessor.execute(attributeRoot);
    }

    /**
     * Loads the model and sets processors
     * @param processors
     */
    protected void initScene(ArrayList<ProcessorComponent> processors)
    {
        if(m_attributes.isUseSimpleStaticModel())
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
        else // Otherwise use the specified collada model
        {
            m_modelInst = new PPolygonModelInstance(m_attributes.getName());
            m_modelInst.setRenderStop(true);
            m_modelInst.addChild(m_skeleton);
            m_pscene.addInstanceNode(m_modelInst);

            // Debugging / Diagnostic output
//            Logger.getLogger(Character.class.getName()).log(Level.INFO, "Model " + m_pscene + "  inst " + m_modelInst);
            m_AnimationProcessor = new CharacterAnimationProcessor(m_modelInst, m_wm);
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
//        PointLight light = new PointLight();
//        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
//        light.setAmbient(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
//        light.setLocation(new Vector3f(-1000, 0, 0)); // not affecting anything
//        light.setEnabled(true);
        LightState ls = (LightState) m_wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setEnabled(true);
       // ls.attach(light);

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
            
            // Initialization extension
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
        if (m_facialAnimations == null)
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
        final JAXBContext context = JAXBContext.newInstance("imi.serialization.xml.bindings");
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
            PMeshMaterial meshMat = new PMeshMaterial(xmlMat, m_wm, m_attributes.getBaseURL());
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
        m_AnimationProcessor.setEnable(false);

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
        getSkeleton().displaceJoint("Head", new Vector3f(0, 0.07f * (fScale - 1), 0));
        joint.getBindPose().setScale(fScale);

        SkinnedMeshJoint rhand = getSkeleton().getSkinnedMeshJoint("rightHand");
        rhand.getBindPose().setScale(fScale);
        SkinnedMeshJoint lhand = getSkeleton().getSkinnedMeshJoint("leftHand");
        lhand.getBindPose().setScale(fScale);
        SkinnedMeshJoint rfeet = getSkeleton().getSkinnedMeshJoint("rightFoot");
        rfeet.getBindPose().setScale(1.5f);
        SkinnedMeshJoint lfeet = getSkeleton().getSkinnedMeshJoint("leftFoot");
        lfeet.getBindPose().setScale(1.5f);
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

    public void installHead(URL headLocation)
    {
        installHeadConfiguration(headLocation);
        // hook the eyeballs and such back up
        m_eyes = new CharacterEyes(m_attributes.getEyeballTexture(), this, m_wm);
        m_skeletonManipulator.setLeftEyeBall(m_eyes.leftEyeBall);
        m_skeletonManipulator.setRightEyeBall(m_eyes.rightEyeBall);
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
        m_AnimationProcessor.setEnable(false);
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

        while (m_skeleton.getAnimationComponent().getGroupCount() < m_skeleton.getAnimationStateCount())
            m_skeleton.addAnimationState(new AnimationState(m_skeleton.getAnimationStateCount()));
        
        // Finally, apply the default shaders
        setDefaultHeadShaders();

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnable(true);
        m_characterProcessor.setEnabled(true);
        m_skeleton.setRenderStop(false);
    }

    protected void installBinaryHeadConfiguration(URL headLocation)
    {
        // Stop all of our processing.
        m_skeleton.setRenderStop(true);
        m_AnimationProcessor.setEnable(false);
        m_characterProcessor.setEnabled(false);

        SkeletonNode newHeadSkeleton = m_binaryExporter.processBinaryData(headLocation);
        attatchHeadSkeleton(m_skeleton, newHeadSkeleton, m_pscene, m_wm);
        setDefaultHeadShaders();

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnable(true);
        m_characterProcessor.setEnabled(true);
        m_skeleton.setRenderStop(false);
    }

    private void attatchHeadSkeleton(SkeletonNode bodySkeleton, SkeletonNode headSkeleton, PScene pscene, WorldManager wm) {
        List<PPolygonSkinnedMesh> skinnedMeshList                   = headSkeleton.getAllSkinnedMeshes();
        SkinnedMeshJoint copyJoint                                  = headSkeleton.getSkinnedMeshJoint("Neck");

        SkinnedMeshJoint originalJoint                              = bodySkeleton.getSkinnedMeshJoint("Neck");
        bodySkeleton.clearSubGroup("Head");
        bodySkeleton.getAnimationGroup(1).clear();


        originalJoint.getParent().replaceChild(originalJoint, copyJoint, false);
        bodySkeleton.refresh();

        PPolygonSkinnedMeshInstance skinnedMeshInstance = null;

        for (int i = 0; i < skinnedMeshList.size(); i++) {
            skinnedMeshInstance = (PPolygonSkinnedMeshInstance) pscene.addMeshInstance(skinnedMeshList.get(i), new PMatrix());
            bodySkeleton.addToSubGroup(skinnedMeshInstance, "Head");
        }

        for (int i = 0; i < headSkeleton.getAnimationGroupCount(); i++) {
            for (AnimationCycle cycle : headSkeleton.getAnimationGroup(i).getCycles())
                bodySkeleton.getAnimationGroup(1).addCycle(cycle);
        }

        // synch up animation states with groups
        while (bodySkeleton.getAnimationComponent().getGroupCount() < bodySkeleton.getAnimationStateCount())
            bodySkeleton.addAnimationState(new AnimationState(bodySkeleton.getAnimationStateCount()));
    }

    private boolean checkBinaryFileExtension(URL fileLocation) {

        int index  = fileLocation.toString().lastIndexOf(".");
        String ext = fileLocation.toString().substring(index);
        if (ext.toLowerCase().contains("bin"))
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
