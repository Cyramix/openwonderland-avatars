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
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.Instruction;
import imi.loaders.collada.Instruction.InstructionNames;
import imi.loaders.collada.InstructionProcessor;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.loaders.repository.SharedAssetPlaceHolder;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import imi.scene.processors.CharacterProcessor;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PNode;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener;
import imi.scene.animation.AnimationState;
import imi.scene.animation.TransitionCommand;
import imi.scene.animation.TransitionQueue;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonMeshInstance;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.polygonmodel.parts.TextureMaterialProperties;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.processors.CharacterAnimationProcessor;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.shader.programs.NormalAndSpecularMapShader;
import imi.scene.shader.programs.SimpleTNLShader;
import imi.scene.shader.programs.VertDeformerWithNormalMapping;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import imi.scene.utils.PMeshUtils;
import imi.scene.utils.PModelUtils;
import imi.scene.utils.tree.JointModificationCollector;
import imi.scene.utils.tree.NodeProcessor;
import imi.scene.utils.tree.TreeTraverser;
import imi.serialization.xml.bindings.xmlCharacter;
import imi.serialization.xml.bindings.xmlCharacterAttributes;
import imi.serialization.xml.bindings.xmlJointModification;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * This class represents the high level avatar. It provides methods for performing
 * tasks that are character related.
 * @author Lou Hayt
 */
public abstract class Character extends Entity implements SpatialObject, AnimationListener
{   
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
    private boolean                         m_initialized           = false;
    protected SkeletonNode                  m_skeleton              = null;
    protected PPolygonMeshInstance          m_mesh                  = null;
    protected ObjectCollection              m_objectCollection      = null;
    protected CharacterAnimationProcessor   m_AnimationProcessor    = null;
    protected TransitionQueue               m_facialAnimationQ      = null;
    
    protected EyeBall                       m_leftEyeBall           = null;
    protected EyeBall                       m_rightEyeBall          = null;
    protected SkinnedMeshJoint              m_headJoint             = null;
    protected boolean                       m_bEyesWander           = true;
    protected Vector3f                      m_eyesWanderOffset      = new Vector3f();
    private float                           m_eyesWanderCounter     = 0.0f;
    private int                             m_eyesWanderIntCounter  = 0;
    protected VerletArm                     m_arm                   = null;
    private VerletSkeletonFlatteningManipulator m_skeletonManipulator = null;
    private float                           m_armTimer              = 0.0f;
    private float                           m_armTimeTick           = 1.0f / 60.0f;

    /**
     * Sets up the mtgame entity 
     * @param attributes
     * @param wm
     */
    public Character(CharacterAttributes attributes, WorldManager wm)
    {   
        super(attributes.getName());
        m_wm = wm;
                
        // Initialize key bindings
        initKeyBindings();
    
        // The procedural scene graph
        m_pscene = new PScene(attributes.getName(), m_wm);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
                
        // Initialize the SharedAsset of the attributes
        m_attributes = attributes;
        initAsset();
        
        // Initialize the scene
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
        
        // Add the entity to the world manager
        wm.addEntity(this);  
    }

    /**
     * Construct a new character using the specified configuration file and world
     * manager.
     * @param configurationFile
     * @param wm
     */
    public Character(URL configurationFile, WorldManager wm)
    {
        super("unsupported");
        throw new UnsupportedOperationException("Not implemented, check back later.");
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
     * Initialize the SharedAsset that will be used to load the character.
     */
    private void initAsset()
    {
        if (m_attributes.getBindPoseFile().endsWith(".dae"))
        {
            URL bindPoseURL = null;

            try {
                if (m_attributes.getBaseURL() != null)
                    bindPoseURL = new URL(m_attributes.getBaseURL() + m_attributes.getBindPoseFile());
            } catch (MalformedURLException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE,
                        "URL for the bind pose was malformed, it was: " +
                        m_attributes.getBaseURL().toString() +
                        m_attributes.getBindPoseFile().toString() +
                        "Exception says, \"" + ex.getMessage() + "\"");
                bindPoseURL = null;
            }

            SharedAsset character = null;

            if (bindPoseURL == null)
            {
                character = new SharedAsset(m_pscene.getRepository(),
                        new AssetDescriptor(SharedAssetType.COLLADA_Model,
                        m_attributes.getBindPoseFile()));
            }
            else
            {
                character = new SharedAsset(m_pscene.getRepository(),
                                new AssetDescriptor(SharedAssetType.COLLADA_Model,
                                        bindPoseURL));
            }

            // Debugging / Diagnostic information
            //System.err.println("GOT SHARED ASSET " + character);

            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, m_attributes.getName(), null));
            // Set up the asset initializer to apply the attributes on this character when it executes
            setAssetInitializer(character, m_attributes);
            m_attributes.setAsset(character);
        }
    }

    /**
     * Set up an asset initializer for the provided shared asset and using some
     * information from the provided CharacterAttributes object
     * @param character
     * @param attributes
     */
    private void setAssetInitializer(SharedAsset character, final CharacterAttributes attributes) 
    {
        AssetInitializer init = new AssetInitializer() {
            public boolean initialize(Object asset) 
            {
                PNode assetNode = (PNode)asset;
                if (assetNode.getChildrenCount() > 0 && assetNode.getChild(0) instanceof SkeletonNode)
                {
                    // Initialize references
                    m_modelInst = (PPolygonModelInstance) assetNode;
                    initializeCharacter();

                    // Set position
                    if (attributes.getOrigin() != null)
                        m_modelInst.getTransform().setLocalMatrix(attributes.getOrigin());

                    // sort the meshes
                    sortBindPoseMeshesIntoSubGroups();
                    
                    // Set animations and custom meshes
                    excecuteAttributes(m_attributes, false);
                    
                    // Gimme them eyeballs
                    PPolygonSkinnedMeshInstance leftEye = (PPolygonSkinnedMeshInstance) m_skeleton.findChild("leftEyeGeoShape");
                    m_leftEyeBall = new EyeBall(leftEye, m_modelInst, m_pscene);
                    leftEye.getParent().replaceChild(leftEye, m_leftEyeBall, true);
                    PPolygonSkinnedMeshInstance rightEye = (PPolygonSkinnedMeshInstance) m_skeleton.findChild("rightEyeGeoShape");
                    m_rightEyeBall = new EyeBall(rightEye, m_modelInst, m_pscene);
                    rightEye.getParent().replaceChild(rightEye, m_rightEyeBall, true);
                    m_leftEyeBall.setOtherEye(m_rightEyeBall);
                    m_rightEyeBall.setOtherEye(m_leftEyeBall);

                    // Set material
//                    m_skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(m_wm));
//                    m_skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(m_wm));
                    setSkinnedMeshShaders();
                    setMeshShaders();
                    m_leftEyeBall.applyShader(m_wm);
                    m_rightEyeBall.applyShader(m_wm);

                    // Facial animation state is designated to id (and index) 1
                    AnimationState facialAnimationState = new AnimationState(1);
                    facialAnimationState.setCurrentCycle(-1); 
                    facialAnimationState.setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
                    facialAnimationState.setAnimationSpeed(0.1f);
                    m_skeleton.addAnimationState(facialAnimationState);
                    if (m_skeleton.getAnimationComponent().getGroups().size() > 1)   
                    {
                        m_facialAnimationQ = new TransitionQueue(m_skeleton, 1);
                        initiateFacialAnimation(1, 0.75f, 0.75f); // 0 is "All Cycles"
                    }

                    // The verlet arm!
                    SkinnedMeshJoint shoulderJoint = (SkinnedMeshJoint) m_skeleton.findChild("rightArm");
                    m_arm = new VerletArm(shoulderJoint, m_modelInst);
                    // Debugging visualization
//                            VerletVisualManager visual = new VerletVisualManager("avatar arm visuals", m_wm);
//                            visual.addVerletObject(m_arm);
//                            visual.setWireframe(true);
                    // old prototype
//                    // Set the joint manipulator on every skinned mesh (remember to set it again when adding new meshes!)
//                    ArrayList<PPolygonSkinnedMeshInstance> skinnedMeshes = m_skeleton.getSkinnedMeshInstances();
//                    m_armJointManipulator = new VerletJointManipulator(m_arm, m_skeleton);
//                    m_arm.setJointManipulator(m_armJointManipulator);
//                    for(PPolygonSkinnedMeshInstance mesh : skinnedMeshes)
//                        mesh.setJointManipulator(m_armJointManipulator);
                    // New verlet skeleton manipulator
                    m_skeletonManipulator = new VerletSkeletonFlatteningManipulator(m_arm, m_leftEyeBall, m_rightEyeBall, m_skeleton);
                    m_arm.setSkeletonManipulator(m_skeletonManipulator);
                    //m_arm.setPointAtLocation(Vector3f.UNIT_Y.mult(2.0f)); // test pointing, set to null to stop pointing 
                }
                return true;

            }
        };
        /// Set it on the provided SharedAsset
        character.setInitializer(init);
    }

    /**
     * This method sorts the meshes into the appropriate mesh groups
     */
    private void sortBindPoseMeshesIntoSubGroups()
    {
        // sort the meshes!
        for (PPolygonSkinnedMeshInstance meshInst : m_skeleton.getSkinnedMeshInstances())
        {
            String subGroupName = PModelUtils.getSubGroupNameForMesh(meshInst.getName());
            if (subGroupName != null)
                m_skeleton.addToSubGroup(meshInst, subGroupName);
            else
                m_skeleton.addChild(meshInst);
        }
    }
    
    /**
     * This method applies all the commands of the CharacterAttributes object.
     * Things such as animation files to load, geometry to remove or add, etc.
     * @param attributes The attributes to process
     */
    private void excecuteAttributes(CharacterAttributes attributes, boolean bUpdate)
    {   
        String fileProtocol = attributes.getBaseURL();

        // If no base url was provided by the character attributes, then it is
        // assumed that the prefix should be the file protocol to the local machine
        // in the current folder.
        if (fileProtocol == null)
            fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");

        InstructionProcessor pProcessor = new InstructionProcessor(m_wm);
        Instruction pRootInstruction = new Instruction();
        // Set the skeleton to our skeleton
        pRootInstruction.addInstruction(InstructionNames.setSkeleton, m_skeleton);
        // Load up any geometry requested by the provided attributes object
        String [][] load = attributes.getLoadInstructions();
        if (load != null && load.length > 0) {
            for (int i = 0; i < load.length; i++) {
                pRootInstruction.addInstruction(InstructionNames.loadGeometry, fileProtocol + load[i][0]);
            }
        }

        // Skinned mesh removals
        String [] delete = attributes.getDeleteInstructions();
        if (delete != null && delete.length > 0) {
            for (int i = 0; i < delete.length; i++) {
                pRootInstruction.addInstruction(InstructionNames.deleteSkinnedMesh, delete[i]);
            }
        }

        // Skinned mesh attachments
        CharacterAttributes.SkinnedMeshParams [] add = attributes.getAddInstructions();
        if (add != null && add.length > 0) {
            for (int i = 0; i < add.length; i++) {
                pRootInstruction.addSkinnedMeshInstruction(add[i].meshName, add[i].subGroupName);
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
                pRootInstruction.addAttachmentInstruction( attachments[i].getMeshName(), attachments[i].getJointName(), tempsolution);
            }
        }

        // Load up body animations
        String [] anims = attributes.getAnimations();
        if (anims != null && anims.length > 0) {
            for (int i = 0; i < anims.length; i++) {
                pRootInstruction.addInstruction(InstructionNames.loadAnimation, fileProtocol + anims[i]);
            }
        }

        // Load up facial animations
        String [] facialAnims = attributes.getFacialAnimations();
        if (facialAnims != null && facialAnims.length > 0) {
            for (int i = 0; i < facialAnims.length; i++) {
                pRootInstruction.addInstruction(InstructionNames.loadFacialAnimation, fileProtocol + facialAnims[i]);
            }
        }

        // Execute the instruction tree
        pProcessor.execute(pRootInstruction);

        // Set shaders on all the meshes, as they may have changed during the above process
//        m_skeleton.setShaderOnSkinnedMeshes(new VertDeformerWithSpecAndNormalMap(m_wm));
//        m_skeleton.setShaderOnMeshes(new NormalAndSpecularMapShader(m_wm));
        setSkinnedMeshShaders();
        setMeshShaders();

        if (bUpdate)
            updateAttributes(attributes);
    }

    /**
     * This method handles the updating of the attributes of the character when
     * swapping out meshes during runtime
     * @param attributes the new attributes to add into they system
     */
    private void updateAttributes(CharacterAttributes attributes) {
        // Create avatar attribs
        CharacterAttributes.SkinnedMeshParams[] oldAdd      = m_attributes.getAddInstructions();
        CharacterAttributes.SkinnedMeshParams[] newAdd      = attributes.getAddInstructions();
        String[]                                oldDelete   = m_attributes.getDeleteInstructions();
        String[]                                newDelete   = attributes.getDeleteInstructions();
        String[][]                              oldLoad     = m_attributes.getLoadInstructions();
        String[][]                              newLoad     = attributes.getLoadInstructions();
        AttachmentParams[]                      oldAttatch  = m_attributes.getAttachmentsInstructions();
        AttachmentParams[]                      newAttatch  = attributes.getAttachmentsInstructions();
        String[]                                oldAnim     = m_attributes.getAnimations();
        String[]                                newAnim     = attributes.getAnimations();

        ArrayList<CharacterAttributes.SkinnedMeshParams> ADD = new ArrayList<CharacterAttributes.SkinnedMeshParams>();
        if (oldAdd == null || oldAdd.length <= 0) {
            for (int i = 0; i < newAdd.length; i++)
                ADD.add(newAdd[i]);
        } else {
            for (int i = 0; i < oldAdd.length; i ++) {
                for (int j = 0; j < newDelete.length; j++) {
                    if (oldAdd[i] == null)
                        continue;
                    if (oldAdd[i].meshName.equals(newDelete[j]))
                        oldAdd[i] = null;
                }
                if (oldAdd[i] != null)
                    ADD.add(oldAdd[i]);
            }

            for (int i = 0; i < newAdd.length; i++) {
                boolean bFound = false;
                for (int j = 0; j < ADD.size(); j++) {
                    if (newAdd[i].meshName.equals(ADD.get(j).meshName))
                        bFound = true;
                }
                if (!bFound)
                    ADD.add(newAdd[i]);
            }
        }

        ArrayList<String> DELETE = new ArrayList<String>();
        for (int i = 0; i < m_attributes.getMeshesAlteredArray().length; i++) {
            if(m_attributes.getFlagForAlteredRegion(i)) {
                if (m_attributes.getGender() == 1) {
                    for (int j = 1; j < m_attributes.getDefaultMaleMeshes().get(i).length; j++) {
                        DELETE.add(m_attributes.getDefaultMaleMeshes().get(i)[j]);
                    }
                } else if (m_attributes.getGender() == 2) {
                    for (int j = 1; j < m_attributes.getDefaultFemaleMeshes().get(i).length; j++) {
                        DELETE.add(m_attributes.getDefaultFemaleMeshes().get(i)[j]);
                    }
                }
            }
        }

        ArrayList<String[]> LOAD = new ArrayList<String[]>();
        if (oldLoad == null || oldLoad.length <= 0) {
            for (int i = 0; i < newLoad.length; i++)
                LOAD.add(newLoad[i]);
        } else {
            for (int i = 0; i < newLoad.length; i++) {
                boolean bFound = false;
                for (int j = 0; j < oldLoad.length; j++) {
                    if (oldLoad[j] == null)
                        continue;
                    if (newLoad[i][1].equals(oldLoad[j][1])) {
                        bFound = true;
                        oldLoad[j] = null;
                    }
                }
                if (bFound)
                    LOAD.add(newLoad[i]);
            }

            for (int i = 0; i < oldLoad.length; i++) {
                if (oldLoad[i] != null)
                    LOAD.add(oldLoad[i]);
            }
        }

        ArrayList<AttachmentParams> ATTACH = new ArrayList<AttachmentParams>();
        if (oldAttatch == null || oldAttatch.length <= 0) {
            for (int i = 0; i < newAttatch.length; i++)
                ATTACH.add(newAttatch[i]);
        } else {
            for (int i = 0; i < oldAttatch.length; i++)
                ATTACH.add(oldAttatch[i]);
            for (int i = 0; i < newAttatch.length; i++) {
                boolean bFound = false;
                for (int j = 0; j < oldAttatch.length; j++) {
                    if (newAttatch[i].getMeshName().equals(oldAttatch[j].getMeshName()))
                        bFound = true;
                }
                if (!bFound)
                    ATTACH.add(newAttatch[i]);
            }
        }

        ArrayList<String> ANIM = new ArrayList<String>();
        if (oldAnim == null || oldAnim.length <= 0) {
            if (newAnim != null && newAnim.length > 0) {
                for (int i = 0; i < newAnim.length; i++)
                    ANIM.add(newAnim[i]);
            }
        } else {
            for (int i = 0; i < oldAnim.length; i++)
                ANIM.add(oldAnim[i]);
            if (newAnim != null && newAnim.length > 0) {
                for (int i = 0; i < newAnim.length; i++) {
                    boolean bFound = false;
                    for (int j = 0; j < oldAnim.length; j++) {
                        if (newAnim[i].equals(oldAnim[j]))
                            bFound = true;
                    }
                    if (!bFound)
                        ANIM.add(newAnim[i]);
                }
            }
        }

        m_attributes.setAnimations(ANIM.toArray(new String[ANIM.size()]));
        m_attributes.setDeleteInstructions(DELETE.toArray(new String[DELETE.size()]));
        String[][] att = new String[LOAD.size()][2];
        for(int i = 0; i < LOAD.size(); i++)
            for(int j = 0; j < 2; j++) {
                att[i][j] = LOAD.get(i)[j];
            }
        m_attributes.setLoadInstructions(att);
        m_attributes.setAddInstructions(ADD.toArray(new CharacterAttributes.SkinnedMeshParams[ADD.size()]));
        m_attributes.setAttachmentsInstructions(ATTACH.toArray(new AttachmentParams[ATTACH.size()]));
    }

    /**
     * Interpret and apply the provided attributes and modify this character.
     * @param attributes
     */
    public void loadAttributes(CharacterAttributes attributes) {
        // if a URL prefix was specified, it needs to be glued to the front of the paths
        URL bindPoseURL = null;
        try {
            if (attributes.getBaseURL() != null)
                bindPoseURL = new URL(attributes.getBaseURL() + attributes.getBindPoseFile());
        } catch (MalformedURLException ex) {
            bindPoseURL = null;
        }

        SharedAsset character = null;

        // If a bind pose file was specified then we need to completely reinitialize
        // this avatar
        if (bindPoseURL != null) {
            this.numEntities();
            m_pscene = new PScene(attributes.getName(), m_wm);
            this.getJScene().setPScene(m_pscene);
            this.setPScene(m_pscene);
            m_attributes = attributes;
            ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
            this.removeComponent(ProcessorCollectionComponent.class);
            character = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, bindPoseURL));
            character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, m_attributes.getName(), null));
            setAssetInitializer(character, m_attributes);
            m_attributes.setAsset(character);
            initScene(processors);
            ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
            for (int i = 0; i < processors.size(); i++)
                processorCollection.addProcessor(processors.get(i));
            addComponent(ProcessorCollectionComponent.class, processorCollection);
        } else {
            excecuteAttributes(attributes, true);
        }
    }

    public void setSkinnedMeshShaders() {
        if (m_skeleton == null)
            return;

        VertDeformerWithSpecAndNormalMap        shiney      = new VertDeformerWithSpecAndNormalMap(m_wm);
        VertDeformerWithNormalMapping           noshiney    = new VertDeformerWithNormalMapping(m_wm);
        ArrayList<PPolygonSkinnedMeshInstance>  smInst      = m_skeleton.getSkinnedMeshInstances();
        String[] check = new String[] { "Head", "Eye", "Teeth", "Tongue", "Hand", "Nude", "Arms", "Legs" };

        for (int i = 0; i < smInst.size(); i++) {
            if (smInst.get(i).getName().contains(check[0]) || smInst.get(i).getName().contains(check[1]) || smInst.get(i).getName().contains(check[2]) ||
                smInst.get(i).getName().contains(check[3]) || smInst.get(i).getName().contains(check[4]) || smInst.get(i).getName().contains(check[5]) ||
                smInst.get(i).getName().contains(check[6]) || smInst.get(i).getName().contains(check[7])) {

                PMeshMaterial mat = smInst.get(i).getMaterialRef().getMaterial();
                mat.setShader(shiney);
                smInst.get(i).setMaterial(mat);
                smInst.get(i).setUseGeometryMaterial(false);
            } else {
                PMeshMaterial mat = smInst.get(i).getMaterialRef().getMaterial();
                mat.setShader(noshiney);
                smInst.get(i).setMaterial(mat);
                smInst.get(i).setUseGeometryMaterial(false);
            }
        }
    }

    public void setMeshShaders() {
        if (m_skeleton == null)
            return;

        NormalAndSpecularMapShader      shiney  = new NormalAndSpecularMapShader(m_wm);
        SimpleTNLShader                 regular = new SimpleTNLShader(m_wm);
        ArrayList<PPolygonMeshInstance> mInst   = null;
        MeshInstanceProcessor proc = new MeshInstanceProcessor();

        TreeTraverser.breadthFirst(m_pscene, proc);
        mInst = proc.getMeshInstances();

        for (int i = 0; i < mInst.size(); i++) {
            if (mInst.get(i).getName().equals(m_attributes.getGeomRef().get(5)[0])) {
                PMeshMaterial mat = mInst.get(i).getMaterialRef().getMaterial();
                mat.setShader(regular);
                mInst.get(i).setMaterial(mat);
                mInst.get(i).setUseGeometryMaterial(false);
            } else {
                PMeshMaterial mat = mInst.get(i).getMaterialRef().getMaterial();
                mat.setShader(shiney);
                mInst.get(i).setMaterial(mat);
                mInst.get(i).setUseGeometryMaterial(false);
            }
        }
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
                    Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
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
            m_modelInst = m_pscene.addModelInstance(m_attributes.getName(), m_attributes.getAsset(), new PMatrix());
            // Debugging / Diagnostic output
//            Logger.getLogger(Character.class.getName()).log(Level.INFO, "Model " + m_pscene + "  inst " + m_modelInst);
            m_AnimationProcessor = new CharacterAnimationProcessor(m_modelInst);
            processors.add(m_AnimationProcessor);
        }
        
        processors.add(new CharacterProcessor(this));
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
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (method != null)
        {
            Object bool = null;
            
            try {
                bool = method.invoke(context, transition.getContextMessageArgs());
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE, null, ex);
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
        
        // Light state
//        Vector3f lightDir = new Vector3f(0.0f, -1.0f, 0.0f);
//        DirectionalLight dr = new DirectionalLight();
//        dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
//        dr.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
//        dr.setSpecular(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
//        dr.setDirection(lightDir);
//        dr.setEnabled(true);
//        LightState ls = (LightState) m_wm.createRendererState(RenderState.RS_LIGHT);
//        ls.setEnabled(true);
//        ls.attach(dr);
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
            initializeCharacter();
        
        if (m_context != null)
            m_context.update(deltaTime);
        
        if (m_attributes.isUseSimpleStaticModel())
            m_modelInst.setDirty(true, true);
        
        updateEyes(deltaTime);
        
        if (m_arm != null && m_arm.isEnabled())
        {
            m_armTimer += deltaTime;
            if (m_armTimer >= m_armTimeTick)
            {
                m_armTimer = 0.0f;
                m_arm.update(m_armTimeTick);
            }
        }
    }

    /**
     * Perform eyeball behavior. This method is driven by the update method
     * @param deltaTime The timestep
     */
    private void updateEyes(float deltaTime)
    {
        if (!m_bEyesWander || m_leftEyeBall == null || m_rightEyeBall == null)
            return;

        if (m_headJoint == null)
            m_headJoint = (SkinnedMeshJoint)m_modelInst.findChild("Head");
                    
        // Position the target in fornt of the eyes
        Vector3f target = m_modelInst.getTransform().getWorldMatrix(false).getTranslation();
        target.addLocal(m_modelInst.getTransform().getWorldMatrix(false).getLocalZ().mult(5.0f));
        target.addLocal(m_headJoint.getTransform().getWorldMatrix(false).getTranslation().add(Vector3f.UNIT_Y.mult(-1.4f)));
        //Vector3f targetOrigin = new Vector3f(target);
        
        // Offset the target with a quick and dirty wander algorythim
        m_eyesWanderCounter += deltaTime;
        if (m_eyesWanderCounter > (float)Math.random() + 0.15f)
        {
            m_eyesWanderIntCounter += 1;
            m_eyesWanderCounter = 0.0f;
            
            float randomScale = 0.15f;
            
            if (Math.random() > 0.5)
                randomScale *= -1.0f;
            m_eyesWanderOffset.x += (float)Math.random() * randomScale;
            if (Math.random() > 0.5)
                randomScale *= -1.0f;
            m_eyesWanderOffset.y += (float)Math.random() * randomScale;
            if (Math.random() > 0.5)
                randomScale *= -1.0f;
            m_eyesWanderOffset.z += (float)Math.random() * randomScale;
        }
        if (m_eyesWanderIntCounter > 20)
        {
            m_eyesWanderIntCounter = 0;
            m_eyesWanderOffset.zero();
        }
        target.addLocal(m_eyesWanderOffset);
        
        m_leftEyeBall.setTarget(target);
        m_rightEyeBall.setTarget(target);
    }
    
    /**
     * Sets the mesh and skeleton references after load time
     */
    public void initializeCharacter()
    {
        // safety against place holders
        if (m_modelInst.getChildrenCount() <= 0 || m_modelInst.getChild(0) instanceof SharedAssetPlaceHolder)
            return;
        
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
        if (m_facialAnimationQ == null)
        {
            if (m_skeleton.getAnimationComponent().getGroups().size() > 1)   
                m_facialAnimationQ = new TransitionQueue(m_skeleton, 1);
            else
                return;   
        }
        int cycle = m_skeleton.getAnimationGroup(1).findAnimationCycle(cycleName);
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
        m_facialAnimationQ.addTransition(new TransitionCommand(cycleIndex, fTimeIn, PlaybackMode.PlayOnce, false));
        m_facialAnimationQ.addTransition(new TransitionCommand(cycleIndex, fTimeOut, PlaybackMode.PlayOnce, true));
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

    public EyeBall getLeftEyeBall() {
        return m_leftEyeBall;
    }

    public EyeBall getRightEyeBall() {
        return m_rightEyeBall;
    }

    public VerletArm getArm() {
        return m_arm;
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
     * Convenience method to switch heads on this character. The provided skeleton
     * should be the bind pose for the new head. The difference between the original
     * skeleton and the new head's bind pose is calculated and used to fit the mesh
     * more correctly to a different skeleton.
     * @param skeleton The skeleton of the new head mesh.
     */
    public void installHead(SkeletonNode skeleton) 
    {
        if (skeleton == null || m_skeleton == null)
        {
            System.out.println(getName() + " can not install head, got a null skeleton! current one: " + m_skeleton + " new one: " + skeleton);
            return;
        }
        
        // Gather the joints and set the new local modifiers
        SkinnedMeshJoint head = m_skeleton.findSkinnedMeshJoint("Head");
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
                SkinnedMeshJoint newHeadJoint     = skeleton.findSkinnedMeshJoint(currentHeadJoint.getName());
                
                PMatrix modifierDelta = new PMatrix();
                //modifierDelta.mul(newHeadJoint.getTransform().getLocalMatrix(false), currentHeadJoint.getTransform().getLocalMatrix(false).inverse());
                modifierDelta.mul( currentHeadJoint.getTransform().getLocalMatrix(false).inverse(), newHeadJoint.getTransform().getLocalMatrix(false));
                //modifierDelta.mulInverse(newHeadJoint.getTransform().getLocalMatrix(false), currentHeadJoint.getTransform().getLocalMatrix(false));
                //currentHeadJoint.setSkeletonModifier(modifierDelta);
            }
            else
                continue; // Prune (kids are not added to the list)
            // Add to the list all the kids
            for (PNode kid : current.getChildren())
                list.add(kid);
        }
        
        // Delete the old head mesh and add the new head mesh
        
        // Don't forget to remove the old teeth and tounge!
        
        // we should re-use the head that was already loaded... for quick testing....
        String fileProtocol = getAttributes().getBaseURL();
        if (fileProtocol == null)
            fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");
        InstructionProcessor pProcessor = new InstructionProcessor(m_wm);
        Instruction pRootInstruction = new Instruction();
        pRootInstruction.addInstruction(InstructionNames.setSkeleton, m_skeleton);
        
        pRootInstruction.addInstruction(InstructionNames.deleteSkinnedMesh, "HeadGeoShape");  // original head is HeadOneShape
        
        // African
        pRootInstruction.addInstruction(InstructionNames.loadGeometry, fileProtocol + "assets/models/collada/Heads/MaleAfricanHead/AfricanAmericanMaleHead1_Bind.dae");
        //pRootInstruction.addAttachmentInstruction(InstructionNames.loadGeometry, fileProtocol + "assets/models/collada/Avatars/Male/Male_Bind.dae");
        //pRootInstruction.addInstruction(InstructionNames.addSkinnedMesh, "headOneShape");
        pRootInstruction.addSkinnedMeshInstruction("headOneShape", "Head");
        // Asian
//        pRootInstruction.addAttachmentInstruction(InstructionNames.loadGeometry, fileProtocol + "assets/models/collada/Heads/MaleAsianHead/AsianMaleHead1_Bind.dae");
//        pRootInstruction.addAttachmentInstruction(InstructionNames.addSkinnedMesh, "head2Shape");
        
        pProcessor.execute(pRootInstruction);
    }

    public boolean isEyesWander() {
        return m_bEyesWander;
    }

    public void setEyesWander(boolean m_bEyesWander) {
        this.m_bEyesWander = m_bEyesWander;
    }
    
    /**
     * Will set m_bEyesWander to false and set the target
     * of both eyes.
     * @param target
     */
    public void setEyesTarget(Vector3f target)
    {
        m_bEyesWander = false;
        m_leftEyeBall.setTarget(target);
        m_rightEyeBall.setTarget(target);
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
//        try {
//            final JAXBContext context = JAXBContext.newInstance("imi.serialization.xml.bindings");
//            final Marshaller m = context.createMarshaller();
//            OutputStream os = targetSaveLocation.openConnection().getOutputStream();
//            m.marshal( generateCharacterDOM(), os);
//        }
//        catch (JAXBException ex) {
//            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, "Failed to write save file! " + ex.getMessage());
//        }
//        catch (IOException ex) {
//            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, "Failed to open OutputStream to " +
//                                    targetSaveLocation.toString() + "! " + ex.getMessage());
//        }
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
            m.setProperty("jaxb.formatted.output", Boolean.TRUE);
            if (location.exists() == true && location.canWrite() == false)
                throw new IOException("Request file (" + location.toString() + ") is not writeable.");
            else if (location.exists() == false)
                location.createNewFile();
            xmlCharacter characterDom = generateCharacterDOM();
            m.marshal( characterDom, location);
        }
        catch (JAXBException ex) {
            Logger log = Logger.getLogger(Character.class.getName());
            log.log(Level.SEVERE, "Failed to write save file! " + ex.getMessage());
            log.log(Level.SEVERE, ex.getErrorCode() + " : " + ex.getLocalizedMessage() + " : " + ex.toString());
            ex.printStackTrace();

        }
        catch (IOException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, "Failed to open OutputStream to " +
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
                Logger.getLogger(Character.class.getName()).log(Level.SEVERE,
                        "JAXB somehow parsed the file and made some other object: " + characterObj.toString());
            }

        }
        catch (JAXBException ex) {
            Logger log = Logger.getLogger(Character.class.getName());
            log.log(Level.SEVERE, "Failed to parse the file! " + ex.getMessage());
            log.log(Level.SEVERE, ex.getErrorCode() + " : " + ex.getLocalizedMessage() + " : " + ex.toString());
            ex.printStackTrace();

        }
        catch (IOException ex) {
            Logger.getLogger(Character.class.getName()).log(Level.SEVERE, "Failed to open InputStream to " +
                                    location.toString() + "! " + ex.getMessage());
        }
    }

    /**
     * Apply the data with to this character instance
     * @param characterDOM
     */
    private void applyCharacterDOM(xmlCharacter characterDOM)
    {
        // The Attribute loading section is not functional currently,
        // skeleton and local modifiers can be saved and loaded however
        // Attributes
        xmlCharacterAttributes xmlAttributes = characterDOM.getAttributes();

        CharacterAttributes attributes = new CharacterAttributes(xmlAttributes);

        // Apply the loaded attributes
        loadAttributes(attributes);

        // Skeletal modifications
        applySkeletalModifications(characterDOM);
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

                    if (jMod.getSkeletonModifierMatrix() != null)
                    {
                        mat = jMod.getSkeletonModifierMatrix().getPMatrix();
                        targetJoint.setSkeletonModifier(mat);
                    }
                }
                else
                {
                    Logger.getLogger(Character.class.getName()).log(Level.WARNING,
                            "Target joint not found for modifier: " + jMod.getTargetJointName());
                }
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
            Logger.getLogger(Character.class.getName()).log(Level.WARNING,
                    "Attemping to serialize a character with no attributes!");
        }
        // Store skeletal modifications
        JointModificationCollector collector = new JointModificationCollector();
        for (PNode kid : m_skeleton.getSkeletonRoot().getChildren())
        {
            collector.clearList();
            TreeTraverser.breadthFirst(kid, collector);
            for (xmlJointModification jMod : collector.getModifierList())
                result.addJointModification(jMod);
        }
        return result;
    }

    public class MeshInstanceProcessor implements NodeProcessor {

        ArrayList<PPolygonMeshInstance> mInst = new ArrayList<PPolygonMeshInstance>();

        public boolean processNode(PNode currentNode) {
            if (currentNode instanceof PPolygonMeshInstance && !(currentNode instanceof PPolygonSkinnedMeshInstance)) {
                mInst.add((PPolygonMeshInstance) currentNode);
            }
            return true;
        }

        public ArrayList<PPolygonMeshInstance> getMeshInstances() {
            return mInst;
        }
    };
}
