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

import com.jme.bounding.BoundingSphere;
import com.jme.image.Texture.ApplyMode;
import com.jme.image.Texture.CombinerFunctionAlpha;
import com.jme.image.Texture.MinificationFilter;
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
import imi.character.avatar.AvatarContext;
import imi.input.CharacterControls;
import imi.objects.ObjectCollectionBase;
import imi.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.TransitionObject;
import imi.loaders.BinaryHeadFileImporter;
import imi.utils.Updatable;
import imi.loaders.Instruction;
import imi.loaders.Instruction.InstructionType;
import imi.loaders.InstructionProcessor;
import imi.loaders.Collada;
import imi.loaders.ColladaLoaderParams;
import imi.loaders.ColladaLoadingException;
import imi.repository.AssetDescriptor;
import imi.repository.Repository;
import imi.repository.SharedAsset;
import imi.repository.SharedAsset.SharedAssetType;
import imi.repository.SharedAssetPlaceHolder;
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
import imi.scene.PSphere;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.polygonmodel.TextureMaterialProperties;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import imi.scene.polygonmodel.PPolygonSkinnedMesh;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import imi.shader.AbstractShaderProgram;
import imi.shader.NoSuchPropertyException;
import imi.shader.ShaderProperty;
import imi.shader.dynamic.GLSLDataType;
import imi.shader.programs.ClothingShaderSpecColor;
import imi.shader.programs.EyeballShader;
import imi.shader.programs.FleshShader;
import imi.shader.programs.HairShader;
import imi.shader.programs.PhongFleshShader;
import imi.shader.programs.SimpleTNLWithAmbient;
import imi.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.utils.PMeshUtils;
import imi.scene.utils.traverser.SerializationHelper;
import imi.scene.utils.traverser.TreeTraverser;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlJointModification;
import imi.serialization.xml.bindings.xmlMaterial;
import imi.shader.programs.VertexDeformer;
import imi.utils.FileUtils;
import imi.utils.instruments.Instrumentation;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javolution.util.FastList;
import javolution.util.FastTable;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.EntityComponent;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.RenderManager;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.InternalAPI;


/**
 * This class represents the high level character. It provides methods for
 * performing tasks at the character level. This wraps SkeletonNode functionality,
 * meshy loading and swapping, and some material conveniences.
 *
 * @author Lou Hayt
 * @author Ronald E Dahlgren
 * @author Paul Viet Truong
 */
public abstract class Character extends Entity implements SpatialObject, AnimationListener {

    /** Map of meshname to skeleton subgroup**/
    private static final HashMap<String, String> MeshToSubGroupMap = new HashMap<String, String>();
    static {
        // Head
        MeshToSubGroupMap.put("rightEyeGeoShape", "Head");
        MeshToSubGroupMap.put("leftEyeGeoShape", "Head");
        MeshToSubGroupMap.put("UpperTeethShape", "Head");
        MeshToSubGroupMap.put("TongueGeoShape", "Head");
        MeshToSubGroupMap.put("LowerTeethShape", "Head");
        MeshToSubGroupMap.put("HeadGeoShape", "Head");
        MeshToSubGroupMap.put("Head_NudeShape", "Head");
        // UpperBody
        MeshToSubGroupMap.put("TorsoNudeShape", "UpperBody");
        // LowerBody
        MeshToSubGroupMap.put("LegsNudeShape", "LowerBody");
        // Feet
        MeshToSubGroupMap.put("RFootNudeShape", "Feet");
        MeshToSubGroupMap.put("LFootNudeShape", "Feet");
        // Hands
        MeshToSubGroupMap.put("RHandShape", "Hands");
        MeshToSubGroupMap.put("LHandShape", "Hands");
    }


    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(Character.class.getName());
    /** Jar path to the blob shadow. This is temorary (hopefully) **/
    private static final String shadowPath              = "imi/character/data/shadow.png";

    /**
     * Retrieve the name of the {@code SkeletonNode} subgroup that should be used for the
     * mesh with the provided string name.
     *
     * <p>
     * If there is no key matching {@code meshName}, then {@code null }will be returned
     * </p>
     * @param meshName A non-null name of a mesh to check for
     * @return The string name of the SkeletonNode subgroup, or null if none is found.
     * @throws IllegalArgumentException If {@code meshName == null}
     */
    public static String getSubGroupNameForMesh(String meshName) {
        if (meshName == null)
            throw new IllegalArgumentException("Null meshname provided.");
        return MeshToSubGroupMap.get(meshName);
    }

    /**
     * Maps to game triggers from VK_ key IDs that are forwarded from the input
     * manager. This defines which triggers react to what keyboard input.
     * <KeyID, TriggerID>
     */
    protected final Hashtable<Integer, Integer>     m_keyBindings           = new Hashtable<Integer, Integer>();
    /** Context for the character's states**/
    protected GameContext                           m_context               = null;
    /** Mapping of different contexts **/
    protected final HashMap<String, GameContext>    m_registry              = new HashMap<String, GameContext>();
    /** The root of the PNode's for the character's scene graph **/
    protected PScene                                m_pscene                = null;
    /** The bridge from PScenes to jME **/
    protected JScene                                m_jscene                = null;
    /** The model instance for the avatar **/
    protected PPolygonModelInstance                 m_modelInst             = null;
    /** The model instance for the shadow quad **/
    protected PPolygonMeshInstance                  m_shadowMesh            = null; // Quad!
    /** Skeleton that our skin is attached to **/
    protected SkeletonNode                          m_skeleton              = null;
    /** Collection of objects that this character is associated with **/
    protected ObjectCollectionBase                  m_objectCollection      = null;
    /** Performs animation on the character **/
    protected CharacterAnimationProcessor           m_AnimationProcessor    = null;
    /** Provides update() calls to the character **/
    protected CharacterProcessor                    m_characterProcessor    = null;
    /** Animation queue for facial animations. Used to chain expressions. **/
    private   FacialAnimationController             m_facialAnimations      = null;
    /** The eyes! **/
    protected CharacterEyes                         m_eyes                  = null;
    /** The arms! **/
    protected VerletArm                             m_rightArm              = null;
    protected VerletArm                             m_leftArm               = null;
    /** True once initialization has completely finished (including asset loading) **/
    private   boolean                               m_initialized           = false;
    /** Expansion slot for updatable things **/
    private   Updatable                             m_updateExtension       = null;
    /** Expansion slot for initialization **/
    private   CharacterInitializationInterface      m_initialization        = null;
    /** index of the 'default' facial animation **/
    private   int                                   m_defaultFacePose       = 0; // No more 'playAll' cycle
    
    private   VerletSkeletonFlatteningManipulator   m_skeletonManipulator   = null;

    private static JAXBContext                      context;
    static {
        try {
            context = JAXBContext.newInstance("imi.serialization.xml.bindings", Character.class.getClassLoader());
        } catch (JAXBException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /** Construction specific variables **/
    protected CharacterParams                       characterParams;
    protected final WorldManager                    worldManager;

////////////////////////////////////////////////////////////////////////////////
// Builder
////////////////////////////////////////////////////////////////////////////////

    protected static abstract class CharacterBuilder {
        protected boolean                            addEntity          = true;
        protected xmlCharacter                       xmlCharDom          = null;
        protected String                             baseURL             = null;
        protected PMatrix                            transform           = new PMatrix();
        protected CharacterInitializationInterface   initializer         = null;
        protected CharacterParams                    attributeParams     = null;
        protected WorldManager                       worldManager        = null;
        protected URL                                configurationFile   = null;

        public CharacterBuilder(CharacterParams attributeParams, WorldManager worldManager) {
            this.attributeParams    = attributeParams;
            this.worldManager       = worldManager;
            this.initializer        = attributeParams.getInitializationObject();
            attributeParams.getOrigin(this.transform);
        }

        public CharacterBuilder(URL configurationFile, WorldManager worldManager) {
            this.configurationFile  = configurationFile;
            this.worldManager       = worldManager;
        }

        public CharacterBuilder addEntity(boolean addEntity) {
            this.addEntity = addEntity;
            return this;
        }

        public CharacterBuilder xmlCharDom(xmlCharacter xmlCharDom) {
            this.xmlCharDom = xmlCharDom;
            return this;
        }

        public CharacterBuilder baseURL(String baseURL) {
            this.baseURL    = baseURL;
            return this;
        }

        public CharacterBuilder transform(PMatrix transform) {
            this.transform  = transform;
            return this;
        }

        public CharacterBuilder initializer(CharacterInitializationInterface initializer) {
            this.initializer    = initializer;
            return this;
        }

        public CharacterBuilder attributeParam(CharacterParams params) {
            this.attributeParams    = params;
            return this;
        }

        public Character build() {
            return new Character(this) {

                @Override
                protected GameContext instantiateContext() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void initKeyBindings() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
    }

    ///////////////////////
    /// Public API
    //////////////////////

    public void saveConfiguration(File file) throws JAXBException, FileNotFoundException
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            saveConfiguration(fos);
        }
        finally
        {
            if (fos != null)
            {
                try {
                    fos.close();
                } catch (IOException ex) {
                    Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Save an XML version of the {@code CharacterParams} for this character along
     * with some other configuration information using the provided {@code OutputStream}.
     *
     * <p>See the CharacterXMLSchema in imi.serialization.xml.schemas for a more precise
     * definition of the format and contents of the XML file that is created.</p>
     * @param out A non-null ObjectOutputStream
     * @throws JAXBException If jaxb encounters an error will marshalling the file
     * @throws IllegalArgumentException If {@code out == null}
     */
    public void saveConfiguration(OutputStream out) throws JAXBException {
        if (out == null)
            throw new IllegalArgumentException("Null output stream provided.");
        final Marshaller m = context.createMarshaller();
        // Pretty files please
        m.setProperty("jaxb.formatted.output", Boolean.TRUE);
        // Starts a nested chain of DOM generation.
        xmlCharacter characterDom = generateCharacterDOM();
        m.marshal( characterDom, out);
    }




////////////////////////////////////////////////////////////////////////////////
// Accessors
////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////
    //////////// Public Part
    ////////////////////////////////////////////////

    /**
     * Retrieve the object collection this character is associated with.
     * @return Character's object collection, or null if none is set
     */
    public ObjectCollectionBase getObjectCollection() {
        return m_objectCollection;
    }

    /**
     * Retrieve the {@code SkeletonNode} that this character uses.
     * @return The character's skeleton
     * @see SkeletonNode
     */
    public SkeletonNode getSkeleton() {
        return m_skeleton;
    }

    /**
     * Retrieve the {@code CharacterParams} associated with this Character.
     *
     * <p>Note that this object is mutable, so the state of the object may not
     * necessarily reflect the state of the character when it is retrieved.</p>
     * @return The character's params, or null is none is present
     * @see CharacterParams
     */
    public CharacterParams getCharacterParams() {
        return characterParams;
    }

    /**
     * Retrieves the {@code GameContext} object associated with this Character.
     *
     * <p>If no context has been set, then null will be returned.</p>
     * @return The game context, or null if none is present
     * @see GameContext
     */
    public GameContext getContext() {
        return m_context;
    }

    /**
     * Retrieve the {@code JScene} that this character uses to interface with
     * jME.
     * @return The character's JScene, or null if none is present
     * @see JScene
     */
    @InternalAPI
    public JScene getJScene() {
        return m_jscene;
    }

    /**
     * Retrieve the {@code PScene} that this character uses to interface with
     * the mutable graph.
     * @return The character's PScene, or null if none is present
     * @see PScene
     */
    @InternalAPI
    public PScene getPScene() {
        return m_pscene;
    }

    /**
     * Retrieve the world manager associated with this character.
     * @return The worldmanager, or null if none is set
     * @see WorldManager
     */
    @InternalAPI
    public WorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * Retrieve the controller for the character's facial animations.
     * @return The controller, or null if none is present
     * @see FacialAnimationController
     */
    FacialAnimationController getFacialAnimationController() {
        if (m_facialAnimations == null && m_skeleton != null)
        {
            if (m_skeleton.getAnimationComponent().getGroupCount() > 1)
                m_facialAnimations = new FacialAnimationController(this, 1);
        }
        return m_facialAnimations;
    }

    /**
     * Retrieve the {@code VerletArm} associated with the Character's right arm.
     * @return The right arm, or null if none is present
     * @see VerletArm
     */
    public VerletArm getRightArm() {
        return m_rightArm;
    }

    /**
     * Retrieve the {@code VerletArm} associated with the Character's left arm.
     * @return The left arm, or null if none is present
     * @see VerletArm
     */
    public VerletArm getLeftArm() {
        return m_leftArm;
    }

    /**
     * Retrieve the {@code VerletSkeletonFlatteningManipulator} for this character.
     * @return The skeleton manipulator, or null if none is present
     * @see VerletSkeletonFlatteningManipulator
     */
    public VerletSkeletonFlatteningManipulator getSkeletonManipulator() {
        return m_skeletonManipulator;
    }

    public CharacterEyes getEyes() {
        return m_eyes;
    }
    /**
     * Get the update extension object (not used by default)
     * @return
     */
    public Updatable getUpdateExtension() {
        return m_updateExtension;
    }

    /**
     * Set an updatable extension to the character
     * @param up
     */
    public void setUpdateExtension(Updatable up) {
        m_updateExtension = up;
    }

    /**
     * Retrieve the values of the key bindings.
     * @param out A non-null storage object
     */
    public void getKeyBindings(Map<Integer,Integer> out) {
        out.clear(); // NPE if null
        out.putAll(m_keyBindings);
    }

////////////////////////////////////////////////////////////////////////////////
// Mutators
////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////
    //////// Public API
    ////////////////////////////////

    /**
     * Sets the camera on this character, NOT IMPLEMENTED YET
     */
    @ExperimentalAPI
    public void setCameraOnMe() {
    }

    /**
     * Sets the GameContext for this character to use.
     * @param context The non-null GameContext
     * @throws IllegalArgumentException If {@code context == null }
     */
    public void setContext(GameContext context) {
        if (context == null)
            throw new IllegalArgumentException("Null context provided");
        m_context = context;
    }


    /**
     * Returns true if currently transitioning between animation cycles with a
     * valid skeleton.
     * @return True if the character's (valid) skeleton is transitioning
     */
    @InternalAPI
    public boolean isTransitioning() {
        if (m_skeleton != null)
            return m_skeleton.getAnimationState().isTransitioning();
        return false;
    }

    /**
     * Will return true if the character is loaded and has a valid skeleton
     * and meshes
     * @return True if initialized
     */
    public boolean isInitialized() {
        return m_initialized;
    }

    /**
     * Tell the character to respond to the specified key press
     * @param swingVirtualKeyCode The VK_ enum for the pressed key
     */
    public void keyPressed(int swingVirtualKeyCode) {
        Integer trigger = m_keyBindings.get(swingVirtualKeyCode);

        if (trigger != null)
            m_context.triggerPressed(trigger);
    }

    /**
     * Tell the character to respond to the specified key released event
     * @param swingVirtualKeyCode The VK_ enum for the key
     */
    public void keyReleased(int key) {
        Integer trigger = m_keyBindings.get(key);

        if (trigger != null)
            m_context.triggerReleased(trigger);
    }


    ////////////////////////////////
    //////// Package API
    ////////////////////////////////


    void setJScene(JScene jscene) {
        if (jscene == null)
            throw new IllegalArgumentException("Null jscene provided");
        m_jscene = jscene;
    }

    void setPScene(PScene pscene) {
        if (pscene == null)
            throw new IllegalArgumentException("Null pscene provided");
        m_pscene = pscene;
    }

    void setModelInst(PPolygonModelInstance modelInst) {
        this.m_modelInst = modelInst;
    }

////////////////////////////////////////////////////////////////////////////////
// Helper Methods
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Finds every mesh associated with this avatar and applies its material.
     *
     * <p>This is a heavy weight operation, as it may cause texture loading,
     * shader compilation, or any number of other heavyweight material operations.
     * This method should not be invoked while the character does not have a valid
     * skeleton.
     * </p>
     */
    public void applyMaterials() {
        // Is there a reason to continue?
        if (m_skeleton == null)
        {
            logger.warning("applyMaterials called when no skeleton was present, dumping stack trace...");
            Thread.dumpStack();
            return; // Early out, cannot do anything without a skeleton
        }

        // Skinned meshes
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
            meshInst.applyMaterial();
        // Nonskinned; collect via tree traversal
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
     * This method applies the specified color to all of the meshes that meet our
     * 'flesh heuristic'. This heuristic is likely to change with time and the meshes
     * affected by this method should not be relied on to be consistent currently.
     * @param skinColor A non-null color value.
     * @throws IllegalArgumentException if {@code skinColor == null}
     */
    @ExperimentalAPI
    public void setSkinTone(ColorRGBA skinColor) {
        if (skinColor == null)
            throw new IllegalArgumentException("Null color specified");

        float[] skinColorFloats = new float[] { skinColor.r, skinColor.g, skinColor.b };
        // grab a reference to the head shader
        AbstractShaderProgram headShader = null;

        // grab a reference to the flesh shaders
        AbstractShaderProgram fleshShader = null;
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
        {
            String lowerCaseName = meshInst.getName().toLowerCase();
            // Hack: Hard coded heuristic
            if ( lowerCaseName.contains("nude") ||
                 lowerCaseName.contains("arms") ||
                 lowerCaseName.contains("hand"))// is it flesh?
            {
                fleshShader = meshInst.getMaterialRef().getShader();
                try {
                    fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColorFloats));
                } catch (NoSuchPropertyException ex)
                {
                    logger.warning("No skin tone property for this mesh: " + meshInst.getName());
                }
            }
        }
        // Order matters do the head last
        if (!characterParams.isApplySkinToneOnHead())
        {
            skinColorFloats[0] = 1.0f;
            skinColorFloats[1] = 1.0f;
            skinColorFloats[2] = 1.0f;
        }
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getMeshesBySubGroup("Head"))
        {
            String lowerCaseName = meshInst.getName().toLowerCase();
            if (lowerCaseName.contains("head"))
            {
                headShader = meshInst.getMaterialRef().getShader();
                try {
                    headShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColorFloats));
                } catch (NoSuchPropertyException ex)
                {
                    logger.warning("No skin tone property for this mesh's shader: " + meshInst.getName());
                }
            }
        }
        applyMaterials();
    }
    
    /**
     * Sets shaders on the parts according to the defaults.
     *
     * <p>There are heuristics used to determine what should have the clothing
     * shader applied, what meshes are fleshy and need the flesh shader, etc. This
     * behavior may change as the API matures. With that being said, do not make
     * any assumptions about which meshes will be affected and what shaders will
     * be applied to them.</p>
     */
    @ExperimentalAPI
    public void setDefaultShaders() {
        Repository repo = (Repository)worldManager.getUserData(Repository.class);

        AbstractShaderProgram accessoryShader   = null;
        AbstractShaderProgram eyeballShader     = null;
        // HACK : Used for the one hair we have that is a skinned mesh and hardcoded mesh matching heuristics
        AbstractShaderProgram specialHairShader = repo.newShader(VertDeformerWithSpecAndNormalMap.class);
        // HACK : Eye lashes
        AbstractShaderProgram specialEyeLashesShader = repo.newShader(VertexDeformer.class);

        float[] skinColor = new float[3];
        characterParams.getSkinTone(skinColor);
        AbstractShaderProgram fleshShader       = null;
        AbstractShaderProgram headShader        = null;
        
        if (characterParams.isUsingPhongLightingForHead())
            headShader = repo.newShader(PhongFleshShader.class);
        else
            headShader = repo.newShader(FleshShader.class);
        // Set the skin color
        float[] headSkinColor = new float[3];
        if (characterParams.isApplySkinToneOnHead())
        {
            headSkinColor[0] = skinColor[0];
            headSkinColor[1] = skinColor[1];
            headSkinColor[2] = skinColor[2];
        }
        else
        {
            headSkinColor[0] = 1.0f;
            headSkinColor[1] = 1.0f;
            headSkinColor[2] = 1.0f;
        }
        try {
            headShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, headSkinColor));
        } catch (NoSuchPropertyException ex) {
            logger.severe(ex.getMessage());
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
                if (meshMat.getTextureRef(0) != null)
                    meshMat.getTextureRef(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                eyeballShader     = repo.newShader(EyeballShader.class);
                meshMat.setDefaultShader(eyeballShader);
            }
            else if (tempName.contains("head")) // order matters must come before "nude" some heads have the name Head_NudeShape... yeah.. artists... I know...
                meshMat.setDefaultShader(headShader);
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
                meshMat.setDefaultShader(fleshShader);
            }
            else if(tempName.toLowerCase().startsWith("eyeao"))
                meshMat.setDefaultShader(specialEyeLashesShader);
            else if (tempName.equals("hairashape1")) // HACK
            {
                // Dahlgren: This method should not UNDER ANY CIRCUMSTANCES perform texture loading.
//                try {
//                    File normalMapFile = new File("assets/models/collada/Hair/FemaleHair/HairBrownHLBase_N.png");
//                    URL normalMapLocation = normalMapFile.toURI().toURL();
//                    meshMat.setTexture(meshMat.getTextureRef(0), 2);
//                    meshMat.setTexture(normalMapLocation, 1);
//                    meshMat.getTextureRef(1).loadTexture(m_pscene.getRepository());
                    // Change the textures, because we know they load incorrectly.
                    specialHairShader = repo.newShader(VertDeformerWithSpecAndNormalMap.class);
                    meshMat.setDefaultShader(specialHairShader);
                    meshMat.setCullFace(CullState.Face.None);
//                }
//                catch (MalformedURLException ex)
//                {
//                    // yeah yeah
//                }
            }
            else // assume to be clothing
            {
                AbstractShaderProgram clothingShader = repo.newShader(ClothingShaderSpecColor.class);
                if (meshInst.getParent().getName().equals("UpperBody"))
                {
                    try {
                        float[] color = new float[3];
                        characterParams.getShirtColor(color);
                        //System.out.println("shirt color: " + color[0] + " " + color[1] + " " + color[2]);
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, color));
                        color = new float[3];
                        characterParams.getShirtSpecColor(color);
                        //System.out.println("shirt specular color: " + color[0] + " " + color[1] + " " + color[2]);
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, color));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else if (meshInst.getParent().getName().equals("LowerBody"))
                {
                    try {
                        float[] color = new float[3];
                        characterParams.getPantsColor(color);
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, color));
                        color = new float[3];
                        characterParams.getPantsSpecColor(color);
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, color));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex); }
                }
                else if (meshInst.getParent().getName().equals("Feet"))
                {
                    try {
                        float[] color = new float[3];
                        characterParams.getShoesColor(color);
                        clothingShader.setProperty(new ShaderProperty("baseColor", GLSLDataType.GLSL_VEC3, color));
                        color = new float[3];
                        characterParams.getShoesSpecColor(color);
                        clothingShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, color));
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
                meshMat.setDefaultShader(clothingShader);
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
                    float[] color = new float[3];
                    AbstractShaderProgram hairShader = repo.newShader(HairShader.class);
                    try {
                        characterParams.getHairColor(color);
                        hairShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, color));
                        hairShader.setProperty(new ShaderProperty("specColor", GLSLDataType.GLSL_VEC3, color));
                    } catch (NoSuchPropertyException ex) {
                        Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    meshMat.setDefaultShader(hairShader);
                    meshMat.setCullFace(CullState.Face.None); // Double sided hair
                }
                else
                    meshMat.setDefaultShader(accessoryShader);
                meshMat.setCullFace(CullState.Face.None);
                meshInst.applyShader();
                // HACK HACK HACK: Cope with assets potentially having non-smooth normals
                meshInst.getGeometry().setSmoothNormals(true);
                meshInst.getGeometry().submit();
            }
            else if (current instanceof PPolygonMesh) // HACK HACK HACK
            {
                PPolygonMesh mesh = (PPolygonMesh)current;
                // HACK HACK HACK: Cope with assets potentially having non-smooth normals
                mesh.setSmoothNormals(true);
                mesh.submit();
            }
            // add all the kids
            if (current instanceof PJoint ||
                current instanceof PPolygonMeshInstance)
                queue.addAll(current.getChildren());
        }
    }
    
    /**
     * A subset of the functionality in setDefaultShaders that only affects
     * head meshes.
     *
     * <p>If no skin color is specified in the character parameters, a default
     * (non-white) skin tone is used. There are heuristics used to determine what
     * should have the clothing shader applied, what meshes are fleshy and need
     * the flesh shader, etc. This behavior may change as the API matures. With
     * that being said, do not make any assumptions about which meshes will be
     * affected and what shaders will be applied to them. </p>
     */
    @ExperimentalAPI
    public void setDefaultHeadShaders() {
        Repository repo = (Repository)worldManager.getUserData(Repository.class);

        AbstractShaderProgram eyeballShader = repo.newShader(EyeballShader.class);

        AbstractShaderProgram fleshShader = null;
        if (characterParams.isUsingPhongLightingForHead())
            fleshShader = repo.newShader(PhongFleshShader.class);
        else
            fleshShader = repo.newShader(FleshShader.class);
        float[] skinColor = new float[3];
        characterParams.getSkinTone(skinColor);
        if (skinColor == null)
        {
           skinColor = new float [3];
           skinColor[0] = (230.0f/255.0f);
           skinColor[1] = (197.0f/255.0f);
           skinColor[2] = (190.0f/255.0f);
        }
        if (!characterParams.isApplySkinToneOnHead())
        {
           skinColor[0] = (1.0f);
           skinColor[1] = (1.0f);
           skinColor[2] = (1.0f);
        }
        try {
            fleshShader.setProperty(new ShaderProperty("materialColor", GLSLDataType.GLSL_VEC3, skinColor));
        } catch (NoSuchPropertyException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }

        // first the skinned meshes
        Iterable<PPolygonSkinnedMeshInstance> smInstances = m_skeleton.retrieveSkinnedMeshes("Head");
        if (smInstances == null) // no subgroup found
            logger.severe("No \"Head\" meshes found during head installation!");
        else
        {
            AbstractShaderProgram specialEyeLashesShader = repo.newShader(VertexDeformer.class);
            for (PPolygonSkinnedMeshInstance meshInst : smInstances)
            {
                PMeshMaterial meshMat = meshInst.getMaterialRef();
                String tempName = meshInst.getName().toLowerCase();

                // is this an eyeball? (also used for tongue and teeth)
                if (tempName.contains("eyegeoshape"))
                {
                    meshMat.setDefaultShader(eyeballShader.duplicate());
                    if (meshMat.getTextureRef(0) != null)
                        meshMat.getTextureRef(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                }
                else if (tempName.contains("tongue") || tempName.contains("teeth"))
                {
                    if (meshMat.getTextureRef(0) != null)
                        meshMat.getTextureRef(0).setMinFilter(MinificationFilter.BilinearNoMipMaps);
                    meshMat.setDefaultShader(eyeballShader.duplicate());
                }
                else if(tempName.toLowerCase().startsWith("eyeao"))
                    meshMat.setDefaultShader(specialEyeLashesShader);
                else
                    meshMat.setDefaultShader(fleshShader.duplicate());
                // Apply it!
                meshInst.applyShader();
            }
        }
    }

    /**
     * This method can be used to disable rendering of the blob shadow during
     * run-time.
     * @param enable true to enable, false otherwise
     */
    public final void enableShadow(boolean enable) {
        if (m_shadowMesh != null)
            m_shadowMesh.setRenderStop(!enable);
    }

    /**
     * Create a shadow for this character if one is not already present
     */
    private void addShadow() {
        if (m_shadowMesh == null) // Not parameterized, no sense in remaking shadows
        {
            // make shadow, minor offset to avoid Z-fighting with the y=0 plane
            Vector3f pointOne =     new Vector3f( 0.45f, 0.003f,  0.5f);
            Vector3f pointTwo =     new Vector3f(-0.45f, 0.003f,  0.5f);
            Vector3f pointThree =   new Vector3f(-0.45f, 0.003f, -0.5f);
            Vector3f pointFour =    new Vector3f( 0.45f, 0.003f, -0.5f);
            // UV sets, standard texturing
            Vector2f uvSetOne =     new Vector2f(0, 0);
            Vector2f uvSetTwo =     new Vector2f(1, 0);
            Vector2f uvSetThree =   new Vector2f(1, 1);
            Vector2f uvSetFour =    new Vector2f(0, 1);

            // Use a transparent material with a blob shadow texture
            PMeshMaterial shadowMaterial = new PMeshMaterial("ShadowMaterial");
            URL path = getClass().getClassLoader().getResource(shadowPath);
            shadowMaterial.setTexture(0, path);
            shadowMaterial.setAlphaState(PMeshMaterial.AlphaTransparencyType.A_ONE);
            shadowMaterial.setColorMaterial(ColorMaterial.None);

            TextureMaterialProperties textureProp = shadowMaterial.getTextureRef(0);
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
     * Display the shadow quad or turn its rendering off
     */
    public void setEnableShadow(boolean enable) {
        if (m_shadowMesh != null)
            m_shadowMesh.setRenderStop(!enable);
    }

    /**
     * Called in the constructor, override this method to se407t your own
     * non-default render states.
     */
    protected void setRenderStates() {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.StateType.ZBuffer);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);

        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Material);
        matState.setColorMaterial(ColorMaterial.None);
        matState.setDiffuse(ColorRGBA.white);
        matState.setAmbient(ColorRGBA.white);

        // Light state
        LightState ls = (LightState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Light);
        ls.setEnabled(true);

        // Cull State
        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Cull);
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);

        // Wireframe State
        WireframeState ws = (WireframeState) worldManager.getRenderManager().createRendererState(RenderState.StateType.Wireframe);
        ws.setEnabled(false);

        // Push 'em down the pipe
        m_jscene.setRenderState(matState);
        m_jscene.setRenderState(buf);
        m_jscene.setRenderState(cs);
        m_jscene.setRenderState(ws);
        m_jscene.setRenderState(ls);
    }

    /**
     * This method makes this character the target of the {@code CharacterControls}.
     * @see CharacterControls
     */
    public final void selectForInput() {
        CharacterControls input = (CharacterControls)worldManager.getUserData(CharacterControls.class);
        input.setCharacter(this);
    }

    /**
     * Called each frame; used to drive the character's assorted time-based functionality.
     * @param deltaTime
     */
    void update(float deltaTime) {
        if (characterParams.isUseSimpleStaticModel())
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
     * Sets the characters head into Big Head mode.
     *
     * <p>This method's behavior is likely to change and be refined as the API
     * matures. Do not rely on this method using specific values in the future.</p>
     * @param fScale The new head scale, 1.0 for no weird scaling
     */
    @ExperimentalAPI
    public void setBigHeadMode(float fScale) {
        SkinnedMeshJoint joint = getSkeleton().getSkinnedMeshJoint("Head");
        getSkeleton().setJointPosition("Head", new Vector3f(0.0f, 0.11902f + 0.07f * (fScale - 1), 0.0f));

        joint.getBindPoseRef().setScale(fScale);
        SkinnedMeshJoint rhand = getSkeleton().getSkinnedMeshJoint("rightHand");
        rhand.getBindPoseRef().setScale(fScale);
        SkinnedMeshJoint lhand = getSkeleton().getSkinnedMeshJoint("leftHand");
        lhand.getBindPoseRef().setScale(fScale);
        SkinnedMeshJoint rfeet = getSkeleton().getSkinnedMeshJoint("rightFoot");
        rfeet.getBindPoseRef().setScale(fScale);
        SkinnedMeshJoint lfeet = getSkeleton().getSkinnedMeshJoint("leftFoot");
        lfeet.getBindPoseRef().setScale(fScale);

        getSkeleton().setJointPosition("Hips", new Vector3f(0.0f, 1.10598f + 0.09f * (fScale - 1), 0.0f));
    }

    /**
     * Make fists with the specified hands.
     *
     * <p>This method uses values that are likely to change over time. Do not
     * code with assumptions that this method will remain the same as the API
     * changes and matures.</p>
     * @param rightHand True to have a fist with the right hand
     * @param leftHand True to have a fist with the left hand
     */
    @ExperimentalAPI
    public void makeFist(boolean rightHand, boolean leftHand) {
        if (rightHand)
            throw new UnsupportedOperationException("Not yet implemented!");
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
                PMatrix bindPose = joint.getBindPoseRef();
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

            getSkeleton().getSkinnedMeshJoint("leftPalm").getBindPoseRef().set(matFloats);
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
    private void attachDebuggingJMECube() {
        Box newBox = new Box("box", Vector3f.UNIT_XYZ.negate().mult(0.1f), Vector3f.UNIT_XYZ.mult(0.1f));
        Node boxNode = new Node();
        boxNode.attachChild(newBox);
        boxNode.setName("BoxNode");
        boxNode.setLocalTranslation(0, 2.0f, 0);
        RenderManager rm = worldManager.getRenderManager();
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


    /**
     * This method parses through the SkeletonDetails list in the DOM and applies
     * the modifications.
     * @param characterDOM
     */
    private void applySkeletalModifications(xmlCharacter characterDOM) {
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
                        jMod.getBindPoseMatrix().getPMatrix(targetJoint.getBindPoseRef());
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
            PMeshMaterial meshMat = new PMeshMaterial(xmlMat, worldManager, characterParams.getBaseURL());
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
    private xmlCharacter generateCharacterDOM() {
        xmlCharacter result = new xmlCharacter();
        // Attributes
        if (characterParams != null)
            result.setAttributes(characterParams.generateAttributesDOM());
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


////////////////////////////////////////////////////////////////////////////////
// Construction Code
////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Construction code that is common to all construction paths.
     * @param addEntity True if the entity should be added to the WorldManager
     * @param characterDOM If non-null, use this to do additional setup
     */
    private void commonConstructionCode(boolean addEntity, xmlCharacter characterDOM) {
        // Initialize key bindings
        initKeyBindings();
        // The procedural scene graph
        m_pscene = new PScene(characterParams.getName(), worldManager);
        // The glue between JME and pscene
        m_jscene = new JScene(m_pscene);
        // Don't render yet
        m_pscene.setRenderStop(true);
        m_jscene.setRenderBool(false);
        // The collection of processors for this entity
        FastTable<ProcessorComponent> processors = new FastTable<ProcessorComponent>();
        // Apply the attributes file; this also initializes the skeleton
        loadSkeletonAndSetInitializerObject();
        // Initialize the scene, this adds the skeleton to the scene graph
        initScene(processors);
        // Use default render states (unless that method is overriden)
        setRenderStates();
        // Create a scene component and set the root to our jscene
        RenderComponent rc = worldManager.getRenderManager().createRenderComponent(m_jscene);
        // Add the scene component with our jscene to the entity
        addComponent(RenderComponent.class, rc);
        // Add our processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));
        // Add the processor collection component to the entity
        addComponent(ProcessorCollectionComponent.class, processorCollection);

        if (addEntity) // Add the entity to the world manager
            worldManager.addEntity(this);

        // Finish the initialization
        finalizeInitialization(characterDOM); // If not null, we are loading a configuration
    }
    
    /**
     * Instantiate the GameContext for this character
     *
     * @return The newly instantiated game context
     */
    protected abstract GameContext instantiateContext();

    /**
     * Override this method to initialize the game trigger actions mappings.
     *
     * <p>Overriding classes should use this method to map Swing Virtual Key Codes
     * to trigger names.</p>
     * @see AvatarContext
     */
    protected abstract void initKeyBindings();

    /**
     * Wrap up the initialization process.
     * <p>Overriding classes should ensure that the character is completely
     * initialized when this method exits. It is advised that this implementation
     * be called at some point in overriding methods. This implementation also
     * blocks until the character is loaded.</p>
     * @param characterDOM A character DOM if one was used; may be null otherwise
     */
    protected void finalizeInitialization(xmlCharacter characterDOM) {
        try {
            while (isLoaded() == false) // Bind up skeleton reference
                Thread.sleep(100);
        } catch (InterruptedException ex) {
            throw new RuntimeException("Interrupted while waiting on isLoaded()");
        }

        // Nothing below is relevant in the simple test sphere case
        if (characterParams.isUseSimpleStaticModel())
        {
            m_jscene.setRenderBool(true);
            return;
        }

        // Set animations and custom meshes
        executeAttributes(characterParams);

        // Set position
        characterParams.getOrigin(m_modelInst.getTransform().getLocalMatrix(true));

        // Facial animation state is designated to id (and index) 1
        m_AnimationProcessor.setAnimateFace(characterParams.isAnimatingFace());
        if (m_skeleton.getAnimationStateCount() > 1)
        {
            AnimationState facialAnimationState = m_skeleton.getAnimationState(1);
            facialAnimationState.setCurrentCycle(-1);
            facialAnimationState.setTransitionCycleMode(PlaybackMode.PlayOnce);
            facialAnimationState.setAnimationSpeed(0.1f);
        }
        if (m_skeleton.getAnimationComponent().getGroupCount() > 1)
        {
            m_facialAnimations = new FacialAnimationController(this, 1);
            // Go to default face pose
            AnimationState facialAnimationState = m_skeleton.getAnimationState(1);
            facialAnimationState.setAnimationSpeed(0.24f);
            facialAnimationState.setCurrentCycle(m_defaultFacePose);
            facialAnimationState.setCurrentCycleTime(0);
            facialAnimationState.setTransitionCycleMode(PlaybackMode.PlayOnce);
            // Smile
            if (m_skeleton.getAnimationGroup(1).getCycleCount() > 0)
                initiateFacialAnimation(1, 0.2f, 1.0f);
        }

        // Hook up eyeballs, if eyeballs exist
        if (!(characterParams instanceof UnimeshCharacterParams))
            if (characterParams.getHeadAttachment() != null)
                m_eyes = new CharacterEyes(characterParams.getEyeballTexture(), this, worldManager);

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
        else if (characterParams instanceof UnimeshCharacterParams)
        {
            Repository repo = (Repository)worldManager.getUserData(Repository.class);
            PPolygonSkinnedMeshInstance smi = m_skeleton.getMeshesBySubGroup("FullBody")[0];
            smi.getMaterialRef().setDefaultShader(repo.newShader(FleshShader.class));
        }
        else
            setDefaultShaders();


        // The verlet arm!
        SkinnedMeshJoint rightShoulderJoint = (SkinnedMeshJoint) m_skeleton.findChild("rightArm");
        SkinnedMeshJoint leftShoulderJoint  = (SkinnedMeshJoint) m_skeleton.findChild("leftArm");
        m_rightArm = new VerletArm(rightShoulderJoint, m_modelInst, true);
        m_leftArm  = new VerletArm(leftShoulderJoint,  m_modelInst, false);
        // Debugging visualization
//        VerletVisualManager visual = new VerletVisualManager("avatar arm visuals",worldManager);
//        visual.addVerletObject(m_leftArm);
//        visual.addVerletObject(m_rightArm);
//        visual.setWireframe(true);

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

        if (leftEye != null) {
            m_skeletonManipulator = new VerletSkeletonFlatteningManipulator(m_leftArm, m_rightArm,
                                                                            leftEye, rightEye,
                                                                            m_skeleton, m_modelInst);
            m_rightArm.setSkeletonManipulator(m_skeletonManipulator);
            m_leftArm.setSkeletonManipulator(m_skeletonManipulator);
        }
        //m_arm.setPointAtLocation(Vector3f.UNIT_Y.mult(2.0f)); // test pointing, set to null to stop pointing

        // Uncomment for verlet arm particle visualization
//        VisuManager vis = new VisuManager("Visualizations", m_wm);
//        vis.setWireframe(false);float handRadius    = 0.15f;
//        vis.addPositionObject(getLeftArm().getWristPosition(), ColorRGBA.magenta, handRadius);
//        vis.addPositionObject(getRightArm().getWristPosition(), ColorRGBA.magenta, handRadius);

        // Associate ourselves with our animation states
        for (AnimationState animState : m_skeleton.getAnimationStates())
            animState.addListener(this);
        
        // Initialization extension
        if (m_initialization != null)
            m_initialization.initialize(this);

        m_skeleton.setInstruments((Instrumentation)worldManager.getUserData(Instrumentation.class));
        // Turn on the animation
        if (characterParams.isAnimateBody())
            m_AnimationProcessor.setEnabled(true);
        // Turn on updates
        m_characterProcessor.setEnabled(true);
        m_pscene.setRenderStop(false);
        m_modelInst.setRenderStop(false);

        // blink if you can hear me
        if (m_eyes != null)
            m_eyes.blink();
        // Update bounds?
        m_jscene.setModelBound(new BoundingSphere());
        // Enable rendering for the render component
        m_jscene.setRenderBool(true);
        // This is required to inherit the renderstates (light specifically) from the render manager
        worldManager.addToUpdateList(m_jscene);
        m_initialized = true;
    }

    /**
     * This method performs the skeleton loading based on the attributes isMale
     * method. It also assigns the initialization object from the attributes if
     * one is present.
     */
    private void loadSkeletonAndSetInitializerObject() {
        if (characterParams == null)
        {
            logger.warning("No attributes, aborting applyAttributes.");
            return;
        }
        // Grab the initialization object if one is present
        m_initialization = characterParams.getInitializationObject();

        if (characterParams.isUseSimpleStaticModel() == true)
            return; // Nothing else to be done here

        // eat the skeleton ;)
        if (characterParams.isMale())
           m_skeleton = m_pscene.getRepository().getSkeleton("MaleSkeleton");
        else
           m_skeleton = m_pscene.getRepository().getSkeleton("FemaleSkeleton");
    }

    /**
     * This method applies all the commands of the CharacterParams object.
     * Things such as animation files to load, geometry to remove or add, etc.
     * @param attributes The attributes to process
     */
    private void executeAttributes(CharacterParams attributes) {
        String urlPrefix = attributes.getBaseURL();
        // If no base url was provided by the character attributes, then it is
        // assumed that the prefix should be the file protocol to the local machine
        // in the current folder.
        if (urlPrefix == null || urlPrefix.length() == 0)
            urlPrefix = "file:///" + System.getProperty("user.dir") + File.separatorChar;

        InstructionProcessor instructionProcessor = new InstructionProcessor(worldManager);
        Instruction attributeRoot = new Instruction();
        // Set the skeleton to our skeleton
        attributeRoot.addChildInstruction(InstructionType.setSkeleton, m_skeleton);

        // attach the appropriate head
        String headPath = attributes.getHeadAttachment();
        if (headPath != null) {
            try {
                URL headLocation = new URL(urlPrefix + headPath);
                if (FileUtils.doesPathReferToBinaryFile(headPath))
                    installBinaryHeadConfiguration(headLocation);
                else
                    installHeadConfiguration(headLocation);
            } catch (MalformedURLException ex) {
                    logger.severe("Error creating URL");
            }
        }

        // Load up any geometry requested by the provided attributes object
        for (String load : attributes.getLoadInstructions()) {
            URL url = null;//HeadAssets.class.getClassLoader().getResource(load);

            if (url != null)
                attributeRoot.addChildInstruction(InstructionType.loadGeometry, url.toString());
            else if (FileUtils.checkURLPath(urlPrefix + load))
                attributeRoot.addChildInstruction(InstructionType.loadGeometry, urlPrefix + load);
            else
                throw new RuntimeException("Failed to load " + urlPrefix + load);
        }

        // Skinned mesh attachments
        for (CharacterParams.SkinnedMeshParams add : attributes.getSkinnedMeshInstructions())
            attributeRoot.addSkinnedMeshInstruction(add.getMeshName(), add.getSubgroup());

        // Regular mesh attachments
        for (AttachmentParams param : attributes.getAttachmentsInstructions()) {
            PMatrix tempsolution = new PMatrix();
            param.getMatrix(tempsolution);

            if (param.getAttachmentJointName().toLowerCase().equals("hair"))
                attributeRoot.addAttachmentInstruction( param.getMeshName(), "HairAttach", tempsolution, param.getAttachmentJointName());
            else
                attributeRoot.addAttachmentInstruction( param.getMeshName(), param.getParentJointName(), tempsolution, param.getAttachmentJointName());

        }

        // Load up body animations
        for (String anim : attributes.getAnimations()) {
            if (FileUtils.checkURLPath(urlPrefix + anim))
                attributeRoot.addChildInstruction(InstructionType.loadAnimation, urlPrefix + anim);
            else
                throw new RuntimeException("Failed to load " + urlPrefix + anim);
        }

        // Load up facial animations
        for (String facialAnim : attributes.getFacialAnimations()) {
            if (FileUtils.checkURLPath(urlPrefix + facialAnim))
                attributeRoot.addChildInstruction(InstructionType.loadFacialAnimation, urlPrefix + facialAnim);
            else
                throw new RuntimeException("Failed to load " + urlPrefix + facialAnim);
        }

        // Execute the instruction tree
        instructionProcessor.execute(attributeRoot, false);
        if (attributes instanceof UnimeshCharacterParams)
        {
            UnimeshCharacterParams uniAttribs = (UnimeshCharacterParams)attributes;
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
            uniAttribs.setModifiedSkeleton(skelly);
        }
    }

    /**
     * Loads the model if using a simple static model and sets processors for the
     * character. This method also creates a <code>PPolygonModelInstance</code>
     * to place the skeleton and other child nodes under.
     * @param processors
     */
    protected void initScene(FastTable<ProcessorComponent> processors) {
        if (characterParams.isUseSimpleStaticModel())
        {
            PMatrix transform = new PMatrix();
            characterParams.getOrigin(transform);
            if (characterParams.getSimpleScene() == null)
            {
                m_modelInst = m_pscene.addModelInstance("Character", null, transform);
                m_modelInst.addChild(new PNode("not a place holder"));
            }
            else
            {
                SharedAsset modelAsset = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D_Mesh, ""));
                modelAsset.setAssetData(characterParams.getSimpleScene());
                m_modelInst = m_pscene.addModelInstance("Character", modelAsset, transform);
            }
        }
        else // Otherwise create the appropriate heirarchy
        {
            m_modelInst = new PPolygonModelInstance(characterParams.getName());
            m_modelInst.setRenderStop(true); // don't start rendering until we finish
            m_modelInst.addChild(m_skeleton); // Skeleton is for our model instance
            m_pscene.addInstanceNode(m_modelInst);

            m_AnimationProcessor = new CharacterAnimationProcessor(m_skeleton, worldManager);
            // Start the animation processor disabled until we finish loading
            m_AnimationProcessor.setEnabled(false);
            processors.add(m_AnimationProcessor);
        }
        // Used for updates, etc.
        m_characterProcessor = new CharacterProcessor(this);
        m_characterProcessor.setEnabled(false);
        processors.add(m_characterProcessor);
    }

    /**
     * This method will atempt a game context transition
     * @param transition A non-null transition object
     * @return true if the transition is succesfully validated
     * @throws IllegalArgumentException If {@code transition == null}
     * @see TransitionObject
     * @see GameContext
     */
    public boolean  executeContextTransition(TransitionObject transition) {
        if (transition == null)
            throw new IllegalArgumentException("Null transition object provided.");
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
     * @throws IllegalArgumentException If {@code context == null || methodName == null}
     */
    public void  registerContext(GameContext context, String methodName) {
        if (context == null || methodName == null)
            throw new IllegalArgumentException("Null parameter. context: " + context + ", methodName: " + methodName);
        m_registry.put(methodName, context);
    }

    /**
     * Traverses the tree and provides render state objects for any geometry that may
     * be missing it.
     * @throws IllegalStateException If no PScene is present
     */
    @InternalAPI
    public void initializeMeshInstanceMaterialStates() {
        if (m_pscene == null)
            throw new IllegalStateException("Cannot initialize material states with a null pscene!");

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
     * Use this method to animate a facial expression for a short
     * amount of time.
     * @param cycleName The name of the facial animation cycle to play, if the name is not found it will try to convert it into an int index
     * @param fTransitionTime How long should the transition take
     * @param fExpressionDuration How long the pose should be held
     */
    public void initiateFacialAnimation(String cycleName, float fTransitionTime, float fExpressionDuration) {
        if (m_skeleton == null) // Not ready to handle facial animations yet
            return;
        if (m_facialAnimations == null)
        {
            AnimationComponent ac = m_skeleton.getAnimationComponent();
            if (ac.getGroupCount() > 1)
                m_facialAnimations = new FacialAnimationController(this, 1);
            else
                logger.warning("No facial animation group present!");
        }
        int cycle = m_skeleton.getAnimationGroup(1).findAnimationCycleIndex(cycleName);
        if (cycle != -1 && m_skeleton.getAnimationGroup(1).isValidCycleIndex(cycle))
            initiateFacialAnimation(cycle, fTransitionTime, fExpressionDuration);
        else
        {
            // Try to convert to a cycleIndex before giving up
            try { 
                int cycleIndex = Integer.valueOf(cycleName);
                initiateFacialAnimation(cycleIndex, fTransitionTime, fExpressionDuration);
            }
            catch (NumberFormatException ex) {
                logger.info("failed to play facial animation cycle index: " + cycle + " name: " + cycleName); }
        }
    }

    /**
     * Convenience method for playing facial animations.
     * @param cycleIndex The index of the desired facial animation cycle
     * @param fTransitionTime How long should the transition take
     * @param fExpressionDuration How long the pose should be held
     */
    public void initiateFacialAnimation(int cycleIndex, float fTransitionTime, float fExpressionDuration) {
        if (m_skeleton == null) // No skeleton, do not animate
            return;
        if (m_facialAnimations == null)
        {
            if (m_skeleton.getAnimationComponent().getGroupCount() > 1)
            {
                m_facialAnimations = new FacialAnimationController(this, 1);
            }
            else
                 logger.warning("No facial animation group present!");
        }
        if (m_facialAnimations != null && m_skeleton.getAnimationGroup(1).isValidCycleIndex(cycleIndex))
            m_facialAnimations.queueFacialAnimation(fTransitionTime, // Time in
                                                    fTransitionTime, // Time out
                                                    fExpressionDuration, // Hold time
                                                    cycleIndex, // Cycle to play
                                                    PlaybackMode.PlayOnce); // Play mode
        else
            logger.info("failed to play facial animation cycle index: " + cycleIndex);
     }

    /**
     * If the model instance doesn't have kids then it is not loaded yet,
     * also checking if the first kid is a place holder.
     * If the first kid is not a SkeletonNode then this character
     * is using a simple scene for debugging visualization.
     */
    private boolean isLoaded() {
        // safety against place holders
        if (m_modelInst.getChildrenCount() <= 0 || m_modelInst.getChild(0) instanceof SharedAssetPlaceHolder)
            return false;

        // TODO : Move this logic somewhere sensible... i.e., NOT the isLoaded() method!
        if (!(m_modelInst.getChild(0) instanceof SkeletonNode))
        {
            m_skeleton   = null;
            m_pscene.getWorldManager().addToUpdateList(m_jscene);

            // Initialization extension, special spot for the simple model case
            if (m_initialization != null)
                m_initialization.initialize(this);

            m_characterProcessor.setEnabled(true);
            m_pscene.setRenderStop(false);
            m_modelInst.setRenderStop(false);

            m_initialized = true;
        }

        return true;
    }

    /**
     * Install a head configuration bundle (*.bhf file) or COLLADA (.dae) head
     * on this character.
     *
     * <p>This method also creates the eyeball objects and associates them with
     * the skeleton manipulator. If the skeleton manipulator is null</p>
     *
     * <p>All loaded facial animations are lost. </p>
     * @param headLocation A non-null location pointing to a head file.
     * @throws IllegalArgumentException If {@code headLocation == null}
     * @see CharacterEyes
     */
    public void installHead(URL headLocation) {
        if (headLocation == null)
            throw new IllegalArgumentException("Null URL provided.");

        boolean jsceneRender = m_jscene.getRenderBool();
        m_jscene.setRenderBool(false);

        if (FileUtils.doesURLReferToBinaryFile(headLocation))
            installBinaryHeadConfiguration(headLocation);
        else
            installHeadConfiguration(headLocation);

        // hook the eyeballs and such back up
        m_eyes = new CharacterEyes(characterParams.getEyeballTexture(), this, worldManager);
        m_skeletonManipulator.setLeftEyeBall(m_eyes.leftEyeBall);
        m_skeletonManipulator.setRightEyeBall(m_eyes.rightEyeBall);

        m_jscene.setRenderBool(jsceneRender);
    }

    /**
     * Installs a head from a specified collada file. This is performed by
     * @param headLocation
     */
    private void installHeadConfiguration(URL headLocation) {
        // Stop all of our processing.
        boolean jsceneRender = m_jscene.getRenderBool();
        m_jscene.setRenderBool(false);
        m_skeleton.setRenderStop(true);
        boolean animProcEnabled = m_AnimationProcessor.isEnabled();
        boolean charProcEnabled = m_characterProcessor.isEnabled();
        m_AnimationProcessor.setEnabled(false);
        m_characterProcessor.setEnabled(false);

        // Create parameters for the collada loader we will use
        ColladaLoaderParams params = new ColladaLoaderParams.Builder()
                                                .setLoadSkeleton(true)
                                                .setLoadGeometry(true)
                                                .setLoadAnimation(false)
                                                .setShowDebugInfo(false)
                                                .setMaxWeights(4)
                                                .setName("HeadSkeleton")
                                                .build();


        // Load the skeleton
        Collada loader = null;
        try {
            loader = new Collada(params);
            loader.load(new PScene(worldManager), headLocation);
        }
        catch (ColladaLoadingException ex)
        {
            logger.severe("Unabled to load collada file, " + ex.getMessage());
            throw new RuntimeException("Could not load the specified COLLADA file: " + headLocation, ex);
        }
        catch (IOException ex) {
            logger.severe("IOException while trying to load collada file, " + ex.getMessage());
            throw new RuntimeException("Could not load specified COLLADA file:", ex);
        }

        SkeletonNode newHeadSkeleton = loader.getSkeletonNode();
        newHeadSkeleton.setName(headLocation.getFile());

        // Chop of the head and put it on our body with no modifications
        m_skeleton.clearSubGroup("Head");

        // Kills any facial animations that may be baked into the binary body skel
        AnimationGroup facial = m_skeleton.getAnimationGroup(1);
        if (facial != null)
        {
            logger.info("Removing old facial animation group.");
            m_skeleton.getAnimationComponent().removeGroup(facial);
        }

        SkinnedMeshJoint copyJoint  = newHeadSkeleton.getSkinnedMeshJoint("Neck");
        SkinnedMeshJoint origJoint  = m_skeleton.getSkinnedMeshJoint("Neck");
        if (copyJoint == null || origJoint == null)
            throw new RuntimeException("Could not find \"Neck\" joint in skeleton!");

        origJoint.getParent().replaceChild(origJoint, copyJoint, false);
        // Rebuild the mappings
        m_skeleton.refresh();

        // Get all the SkinnedMeshInstances & place it in the head subgroup
        List<PPolygonSkinnedMeshInstance> skinnedMeshList = newHeadSkeleton.getSkinnedMeshInstances();
        if (skinnedMeshList.size() == 0) // No skinned mesh instances
        {
            logger.warning("No skinned mesh instances found in skeleton. Looking for skinned meshes.");
            List<PPolygonSkinnedMesh> ppsmList = newHeadSkeleton.getAllSkinnedMeshes();
            if (ppsmList != null || ppsmList.size() > 0) {
                for (PPolygonSkinnedMesh pPolygonSkinnedMesh : ppsmList) {
                    PPolygonSkinnedMeshInstance meshInst = (PPolygonSkinnedMeshInstance) m_pscene.addMeshInstance(pPolygonSkinnedMesh, new PMatrix());
                    meshInst.setAndLinkSkeletonNode(m_skeleton);
                    m_skeleton.addToSubGroup(meshInst, "Head");
                }
            }
        }
        else // found some skinned mesh instances
        {
            for (PPolygonSkinnedMeshInstance meshInst : skinnedMeshList) {
                meshInst.setAndLinkSkeletonNode(m_skeleton);
                m_skeleton.addToSubGroup(meshInst, "Head");
            }
        }

        // Finally, apply the default shaders
        setDefaultHeadShaders();

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnabled(animProcEnabled);
        m_characterProcessor.setEnabled(charProcEnabled);
        m_skeleton.setRenderStop(false);
        m_jscene.setRenderBool(jsceneRender);
    }

    /**
     * Installs a bhf file on this character.
     * @param headLocation
     */
    private void installBinaryHeadConfiguration(URL headLocation) {
        // Stop all of our processing.
        boolean jsceneRender = m_jscene.getRenderBool();
        m_jscene.setRenderBool(false);
        m_skeleton.setRenderStop(true);
        boolean animProcEnabled = m_AnimationProcessor.isEnabled();
        boolean charProcEnabled = m_characterProcessor.isEnabled();
        m_AnimationProcessor.setEnabled(false);
        m_characterProcessor.setEnabled(false);

        System.out.println("Installing head from " + headLocation.toString());
        try {
            SkeletonNode newHeadSkeleton = BinaryHeadFileImporter.loadHeadFile(headLocation.openStream());
            attachHeadSkeleton(newHeadSkeleton);
            initializeMeshInstanceMaterialStates();
            setDefaultHeadShaders();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "IO Exception while trying to install head!", ex);
        }

        // Re-enable all the processors that affect us.
        m_AnimationProcessor.setEnabled(animProcEnabled);
        m_characterProcessor.setEnabled(charProcEnabled);
        m_skeleton.setRenderStop(false);
        m_jscene.setRenderBool(jsceneRender);
    }

    private void attachHeadSkeleton(SkeletonNode headSkeleton)
    {
        List<PPolygonSkinnedMeshInstance> skinnedMeshList           = headSkeleton.getSkinnedMeshInstances();
        SkinnedMeshJoint copyJoint                                  = headSkeleton.getSkinnedMeshJoint("Neck");
        SkinnedMeshJoint originalJoint                              = m_skeleton.getSkinnedMeshJoint("Neck");

        m_skeleton.clearSubGroup("Head");

        if (m_skeleton.getAnimationGroup(1) != null)
            m_skeleton.getAnimationGroup(1).clear();

        originalJoint.getParent().replaceChild(originalJoint, copyJoint, false);
        m_skeleton.refresh();

        if (skinnedMeshList.size() == 0)
            logger.warning("No skinned mesh instances found in skeleton. Do you have meshes instead?");

        for (PPolygonSkinnedMeshInstance meshInst : skinnedMeshList)
            m_skeleton.addToSubGroup(meshInst, "Head");

        // Ensure the facial animation group exists
        if (m_skeleton.getAnimationComponent().getGroupCount() < 2)
            m_skeleton.getAnimationComponent().addGroup(new AnimationGroup("FacialAnimations"));

        // Add all the cycles to the original skeletons facial animation
        for (AnimationGroup group : headSkeleton.getAnimationComponent().getGroups())
            for (AnimationCycle cycle : group.getCycles())
                m_skeleton.getAnimationGroup(1).addCycle(cycle);
        

        // synch up animation states with groups
        while (m_skeleton.getAnimationComponent().getGroupCount() < m_skeleton.getAnimationStateCount())
            m_skeleton.addAnimationState(new AnimationState(m_skeleton.getAnimationStateCount()));
    }


////////////////////////////////////////////////////////////////////////////////
// Deprecated Methods
////////////////////////////////////////////////////////////////////////////////

    public void setClothesColors(ColorRGBA topColor, ColorRGBA topSpecColor, ColorRGBA bottomColor, 
                                 ColorRGBA bottomSpecColor, ColorRGBA shoesColor, ColorRGBA shoesSpecColor) {
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
//                meshMat.setDefaultShader(clothingShader);
//            }
//            // Apply it!
//            meshInst.applyShader();
//        }
    }

////////////////////////////////////////////////////////////////////////////////
// Override Methods
////////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc AnimationListener}
     */
    @Override
    @InternalAPI
    public void receiveAnimationMessage(AnimationMessageType message, int stateID) {
        if (m_context != null)
            m_context.notifyAnimationMessage(message, stateID);

    }

    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public void setObjectCollection(ObjectCollectionBase objs) {
        m_objectCollection = objs;
        objs.addObject(this);
    }


    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public Vector3f getPositionRef() {
        return m_context.getController().getPosition();
    }


    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public Vector3f getRightVector() {
        return m_context.getController().getRightVector();
    }


    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public Vector3f getForwardVector() {
        return m_context.getController().getForwardVector();
    }


    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public PSphere getNearestObstacleSphere(Vector3f myPosition) {
        return null;
    }

    /**
     * Sets the sphere data that is built around the model instances bounding sphere.
     * @param output
     */
    public void getBoundingSphere(PSphere output) {
        if (m_modelInst == null)
        {
            System.out.println("Character.getBoundingSphere() - m_modelInst is null");
            return;
        }
        if (m_modelInst.getBoundingSphere() == null)
            m_modelInst.calculateBoundingSphere();
        output.setRadius(m_modelInst.getBoundingSphere().getRadius());
        output.setCenter(m_modelInst.getTransform().getWorldMatrix(false).getTranslation().add(m_modelInst.getBoundingSphere().getCenterRef()));
    }

    /**
     * Get the CharacterController from the GameContext
     * @return
     */
    public CharacterController getController() {
        return m_context.getController();
    }

    /**
     * Return a new PSphere that is built around the model instances bounding sphere.
     * {@inheritDoc SpatialObject}
     */
    @Override
    public PSphere getBoundingSphere() {
        if (m_modelInst.getBoundingSphere() == null)
            m_modelInst.calculateBoundingSphere();
        PSphere result = new PSphere(m_modelInst.getBoundingSphere());
        result.setCenter(m_modelInst.getTransform().getWorldMatrix(false).getTranslation().add(result.getCenterRef()));
        return result;
    }


    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public PPolygonModelInstance getModelInst() {
        return m_modelInst;
    }

    /**
     * Shut the eyes or open them
     * @param shut
     */
    public void shutEyes(boolean shut) {
        if (m_eyes != null)
        {
            m_eyes.setKeepEyesClosed(shut);
            m_eyes.blink();
        }
    }

    /**
     * The D does not aprove
     * @return
     */
    public boolean isEyesShut() {
        return m_eyes.isEyesClosed();
    }

    /**
     * Get the name of this character (not the Entity name - that one is always "Character")
     * @return
     */
    @Override
    public String getName() {
        return characterParams.getName();
    }
    
    /**
     * {@inheritDoc SpatialObject}
     */
    @Override
    public void destroy() {
        if (worldManager == null)
            return;

        m_jscene.setRenderBool(false);

        ProcessorCollectionComponent pcc = (ProcessorCollectionComponent) getComponent(ProcessorCollectionComponent.class);
        for(ProcessorComponent p : pcc.getProcessors()) {
            p.setArmingCondition(null);                         // Workaround for mtgame bug
        }

        // Something needs to wait until this is finished
        m_characterProcessor.setEnabled(false);
        m_characterProcessor.setEnabled(false);
        m_characterProcessor.setArmingCondition(null);          // Workaround for mtgame bug
        if (m_AnimationProcessor!=null) {
            m_AnimationProcessor.setEnabled(false);
            m_AnimationProcessor.setArmingCondition(null);      // Workaround for mtgame bug
        }

        worldManager.removeEntity(this);

        m_context               = null;
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

        CharacterControls input = (CharacterControls)worldManager.getUserData(CharacterControls.class);
        if (input != null)
            input.removeCharacterFromTeam(this);

        // Clean components
        for (EntityComponent ec : getComponents())
            ec.setEntity(null); // Hack TODO
    }


    /**
     * Construct a new character using the provided builder
     * @param builder The builder to use.
     * @throws IllegalArgumentException If {@code builder == null}
     */
    protected Character(CharacterBuilder builder) {
        super("Character");
        if (builder == null)
            throw new IllegalArgumentException("Null builder provided");

        this.worldManager        = builder.worldManager;
        PMatrix origin = new PMatrix();
        
        if (builder.attributeParams != null) 
        {
            this.characterParams     = builder.attributeParams;
            // User the CharacterParams
            characterParams.setInitializationObject(builder.initializer);
            characterParams.getOrigin(origin);
            if (origin.equals(PMatrix.IDENTITY))
                characterParams.setOrigin(builder.transform);
            commonConstructionCode( builder.addEntity, builder.xmlCharDom);
        } 
        else if (builder.configurationFile != null) 
        {
            // Load and use a configuration file
            xmlCharacter characterDOM = null;

            try {
                final Unmarshaller m = context.createUnmarshaller();

                InputStream is = builder.configurationFile.openConnection().getInputStream();
                Object characterObj = m.unmarshal( is );

                if (characterObj instanceof xmlCharacter)
                {
                    characterDOM = (xmlCharacter)characterObj;
                    xmlCharacterAttributes xmlAttributes = characterDOM.getAttributes();
                    xmlAttributes.setBaseURL(builder.baseURL);
                    characterParams = new CharacterParams(xmlAttributes);
                    characterParams.setOrigin(builder.transform);
                }
                else
                    throw new ExceptionInInitializerError(
                            "JAXB somehow parsed the  configuration file " +
                            "and made some other object: " + characterObj.toString());
            }
            catch (JAXBException ex) {
                logger.log(Level.SEVERE, "Failed to parse the file! " + ex.getMessage());
                logger.log(Level.SEVERE, ex.getErrorCode() + " : " + ex.getLocalizedMessage() + " : " + ex.toString());
                throw new ExceptionInInitializerError("JAXB was unable to parse " + builder.configurationFile);

            }
            catch (IOException ex) {
                throw new RuntimeException("Failed to open InputStream to " +
                                        builder.configurationFile.toString() + "! " + ex.getMessage());
            }
            characterParams.setInitializationObject(builder.initializer);
            commonConstructionCode( builder.addEntity, characterDOM);
        }
        else
        {
            this.characterParams = null;
            throw new RuntimeException("No valid character params or configuration file!");
        }
    }
}
