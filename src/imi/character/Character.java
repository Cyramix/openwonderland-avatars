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
import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector2f;
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
import imi.scene.shader.dynamic.GLSLCompileException;
import imi.scene.shader.dynamic.GLSLDataType;
import imi.scene.shader.dynamic.GLSLShaderProgram;
import imi.scene.shader.effects.MeshColorModulation;
import imi.scene.shader.programs.ClothingShaderSpecColor;
import imi.scene.shader.programs.EyeballShader;
import imi.scene.shader.programs.FleshShader;
import imi.scene.shader.programs.NormalMapShader;
import imi.scene.shader.programs.SimpleTNLWithAmbient;
import imi.scene.utils.PMeshUtils;
import imi.scene.utils.tree.SerializationHelper;
import imi.scene.utils.tree.TreeTraverser;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlJointModification;
import imi.serialization.xml.bindings.xmlMaterial;
import imi.utils.instruments.Instrumentation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
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
    protected PPolygonModelInstance         m_shadowModel           = null; // Quad!
    protected SkeletonNode                  m_skeleton              = null;
    protected PPolygonMeshInstance          m_mesh                  = null;
    protected ObjectCollection              m_objectCollection      = null;
    protected CharacterAnimationProcessor   m_AnimationProcessor    = null;
    protected CharacterProcessor            m_characterProcessor    = null;
    protected TransitionQueue               m_facialAnimationQ      = null;
    protected CharacterEyes                 m_eyes                  = null;
    protected VerletArm                     m_rightArm              = null;
    protected VerletArm                     m_leftArm               = null;
    private   boolean                       m_initialized           = false;
    private   Updatable                     m_updateExtension       = null;
    private   int                           m_defaultFacePose       = 4;
    private   float                         m_defaultFacePoseTiming = 0.1f;
    private   VerletSkeletonFlatteningManipulator m_skeletonManipulator   = null;

    /** Used for internal requests for assets **/
    private final RepositoryUser            headInstaller = new RepositoryUser() {
            
            public void receiveAsset(SharedAsset assetRecieved) {
                // Flip some switches
                asset = assetRecieved;
                m_bWaitingOnAsset = false;
            }
    };

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
            shadowMaterial.setTexture("assets/textures/shadow.png", 0);
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

    private void commonConstructionCode(WorldManager wm, CharacterAttributes attributes, boolean addEntity, xmlCharacter characterDOM)
    {
        m_wm = wm;
        // Initialize key bindings
        initKeyBindings();
        // The procedural scene graph
        m_pscene = new PScene(attributes.getName(), m_wm);
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
        // Nothing below is relevant in the simple test sphere case
        if (m_attributes.isUseSimpleStaticModel())
            return;

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

        
        // Apply the material on everything that was just loaded.
        for (PPolygonSkinnedMeshInstance meshInstance : m_skeleton.getSkinnedMeshInstances())
            meshInstance.applyMaterial();

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

        // the shadow!
        addShadow();
        m_skeleton.setInstruments((Instrumentation)m_wm.getUserData(Instrumentation.class));
        // Turn on the animation
        m_AnimationProcessor.setEnable(true);
        // Turn on updates
        m_characterProcessor.start();
        m_pscene.setRenderStop(false);
        m_pscene.updateJSceneRenderState();
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
        
        float[] skinColor = m_attributes.getSkinTone();
        AbstractShaderProgram fleshShader = repo.newShader(FleshShader.class);
        try {
            fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

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
                // Grab a copy of the material
                PMeshMaterial meshMat = meshInst.getMaterialCopy().getMaterial();
                if (meshInst.getParent().getName().equals("Hair"))
                {
                    GLSLShaderProgram hairShader = (GLSLShaderProgram)repo.newShader(NormalMapShader.class);
                    hairShader.addEffect(new MeshColorModulation());
                    try {
                        hairShader.compile();
                        hairShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, m_attributes.getHairColor()));
                    } catch (Exception ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
                    meshMat.setShader(hairShader);
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

    /**
     * A subset of the functionality in setDefaultShaders
     */
    private void setDefaultHeadShaders()
    {
        Repository repo = (Repository)m_wm.getUserData(Repository.class);

        AbstractShaderProgram eyeballShader = repo.newShader(EyeballShader.class);
        AbstractShaderProgram fleshShader = repo.newShader(FleshShader.class);
        float[] skinColor = { (230.0f/255.0f), (197.0f/255.0f), (190.0f/255.0f) };
        try {
            fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.retrieveSkinnedMeshes("Head");
        for (PPolygonSkinnedMeshInstance meshInst : smInstances)
        {
            PMeshMaterial meshMat = meshInst.getMaterialRef().getMaterial();
            // is this an eyeball? (also used for tongue and teeth)
            if (meshInst.getName().contains("EyeGeoShape"))
            {
                meshMat.setShader(eyeballShader);
                meshMat.getTexture(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
            }
            else if (meshInst.getName().contains("Tongue") || meshInst.getName().contains("Teeth"))
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
            installInitialHead(headLocation, "Neck"); // Should I parameterize this?

        InstructionProcessor instructionProcessor = new InstructionProcessor(m_wm);
        Instruction attributeRoot = new Instruction();
        // Set the skeleton to our skeleton
        attributeRoot.addChildInstruction(InstructionType.setSkeleton, m_skeleton);
        // Load up any geometry requested by the provided attributes object
        List<String> load = attributes.getLoadInstructions();
        if (load != null) {
            for (int i = 0; i < load.size(); i++)
                attributeRoot.addChildInstruction(InstructionType.loadGeometry, urlPrefix + load.get(i));
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
        
        if (m_updateExtension != null)
            m_updateExtension.update(deltaTime);
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
        if (m_context != null)
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

    private boolean m_bWaitingOnAsset = false;
    private SharedAsset asset = null;
    /**
     * Change out the head (including head skeleton) that the avatar is using.
     * @param headLocation Where the collada file is located.
     * @param attachmentJointName The joint to attach on.
     */
    public void installHead(URL headLocation, String attachmentJointName)
    {
        // Stop all of our processing.
        m_skeleton.setRenderStop(true);
        m_AnimationProcessor.setEnable(false);
        m_characterProcessor.setEnabled(false);

        AssetDescriptor descriptor = new AssetDescriptor(SharedAssetType.COLLADA, headLocation);
        asset = new SharedAsset(m_pscene.getRepository(), descriptor);
        m_bWaitingOnAsset = true;
        m_pscene.getRepository().loadSharedAsset(asset, headInstaller);
        while (m_bWaitingOnAsset == true)
        {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                logger.severe(ex.getMessage());
            }
        }
        PScene newHeadPScene = (PScene)asset.getAssetData();
        SkeletonNode newHeadSkeleton = (SkeletonNode)newHeadPScene.findChild("skeletonRoot").getParent();

        // Cut off the old skeleton at the specified attach point
        SkinnedMeshJoint parent = (SkinnedMeshJoint)m_skeleton.getSkinnedMeshJoint(attachmentJointName).getParent();
        parent.removeChild(attachmentJointName);
        parent.addChild(newHeadSkeleton.getSkinnedMeshJoint(attachmentJointName));

        m_skeleton.refresh();
        m_skeleton.clearSubGroup("Head");
        m_eyes = null;

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
        
        m_eyes = new CharacterEyes(m_attributes.getEyeballTexture(), this, m_wm);
        m_skeletonManipulator.setLeftEyeBall(m_eyes.leftEyeBall);
        m_skeletonManipulator.setRightEyeBall(m_eyes.rightEyeBall);
        // Apply the correct shaders to them
        setDefaultHeadShaders();
        // Relink all of the old meshes and apply their materials
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
        {
            meshInst.setAndLinkSkeletonNode(m_skeleton);
            meshInst.applyMaterial();
        }

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnable(true);
        m_characterProcessor.setEnabled(true);
        m_skeleton.setRenderStop(false);
    }

    /**
     * m_skeleton and m_pscene must already be initialized.
     * @param headLocation
     * @param attachmentJointName
     * @return true on success, false on failure.
     */
    private boolean installInitialHead(URL headLocation, String attachmentJointName)
    {
        boolean result = true;
        // Ready a request for the repository
        AssetDescriptor descriptor = new AssetDescriptor(SharedAssetType.COLLADA, headLocation);
        asset = new SharedAsset(m_pscene.getRepository(), descriptor);
        m_bWaitingOnAsset = true;
        // Request it!
        m_pscene.getRepository().loadSharedAsset(asset, headInstaller);
        while (m_bWaitingOnAsset == true) // wait until loaded
        {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException ex)
            {
                logger.severe(ex.getMessage());
            }
        }
        if (asset == null) // Timeout at the repository
        {
            logger.severe("Timed out waiting on asset for new head.");
            result = false;
            return result; // Abort!
        }
        // Locate the new skeleton
        SkeletonNode newHeadSkeleton = null;
        PNode psceneInstances = ((PScene)asset.getAssetData()).getInstances();
        int numChildren = psceneInstances.getChildrenCount();
        Iterable<PNode> newSkeletonChildren = null;
        // Known to be a top level child
        for (int i = 0; i < numChildren; ++i)
        {
            PNode current = psceneInstances.getChild(i);
            if (current instanceof SkeletonNode)
            {
                newHeadSkeleton = ((SkeletonNode)current);
                newSkeletonChildren = newHeadSkeleton.getChildren();
                newHeadSkeleton = newHeadSkeleton.deepCopy();
            }
        }
        if (newHeadSkeleton == null)
        {
            logger.severe("Unable to find skeleton for the new head!");
            result = false;
            return result; // abort!
        }
        // Cut off the old skeleton at the specified attach point
        SkinnedMeshJoint parent = (SkinnedMeshJoint)m_skeleton.getSkinnedMeshJoint(attachmentJointName).getParent();
        parent.removeChild(attachmentJointName);
        parent.addChild(newHeadSkeleton.getSkinnedMeshJoint(attachmentJointName));

        m_skeleton.refresh();
        m_skeleton.clearSubGroup("Head");

        
        for (PNode node : newSkeletonChildren)
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

        // Relink all of the old meshes and apply their materials
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
            meshInst.setAndLinkSkeletonNode(m_skeleton);

        return result;
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


    public void setBeerBelly(float fSize)
    {
//        SkinnedMeshJoint bellyJoint = getSkeleton().getSkinnedMeshJoint("Spine1");
//        bellyJoint.getLocalModifierMatrix().setScale(fSize);
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

}
