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
package imi.character;

import com.jme.light.PointLight;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.character.ninja.NinjaAvatar.NinjaAvatarAttributes;
import imi.character.objects.ObjectCollection;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.TransitionObject;
import imi.loaders.collada.ColladaLoaderParams;
import imi.loaders.collada.Instruction;
import imi.loaders.collada.InstructionProcessor;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import imi.scene.processors.SkinnedAnimationProcessor;
import imi.loaders.repository.AssetDescriptor;
import imi.loaders.repository.AssetInitializer;
import imi.loaders.repository.SharedAsset;
import imi.loaders.repository.SharedAsset.SharedAssetType;
import imi.scene.PNode;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.shader.programs.VertexDeformer;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.processors.JSceneEventProcessor;
import imi.scene.shader.programs.VertDeformerWithSpecAndNormalMap;
import java.io.File;

/**
 *
 * @author Lou Hayt
 */
public abstract class Character extends Entity implements SpatialObject
{   
    public static int hack = 0;
    /**
     * Maps to game triggers from VK_ key IDs that are forwarded from the input
     * manager. This defines which triggers react to what keyboard input.
     * <KeyID, TriggerID>
     */
    protected Hashtable<Integer, Integer> m_keyBindings = new Hashtable<Integer, Integer>();
        
    protected GameContext     m_context        = null;
    protected Attributes      m_attributes     = null;
    
    protected HashMap<String, GameContext> m_registry = new HashMap<String, GameContext>();
    
    protected WorldManager    m_wm             = null;
    protected PScene          m_pscene         = null;
    protected JScene          m_jscene         = null;
    
    protected PPolygonModelInstance m_modelInst  = null;
    
    private boolean           m_initalized     = false;
    protected SkeletonNode    m_skeleton       = null;
    protected PPolygonSkinnedMeshInstance m_mesh = null;
    
    protected ObjectCollection m_objectCollection = null;
        
    public class Attributes
    {
        String m_name ="nameless";

        SharedAsset m_asset = null;
        
        String m_ModelFile = null;
        
        String m_TextureFile = null;
        
        public Attributes(String name) {
            m_name = name;
        }
        
        public String getName() {
            return m_name;
        }

        public void setName(String name) {
            m_name = name;
        }

        public SharedAsset getAsset() {
            return m_asset;
        }

        public void setAsset(SharedAsset asset) {
            m_asset = asset;
        }

        public String getModelFile() {
            return m_ModelFile;
        }

        public void setModelFile(String ModelFile) {
            m_ModelFile = ModelFile;
        }

        public String getTextureFile() {
            return m_TextureFile;
        }

        public void setTextureFile(String TextureFile) {
            m_TextureFile = TextureFile;
        }
        
        
    }
        
    /**
     * This constructor calls initScne(), setRenderStates() and adds this
     * Entity to the world manager.
     * @param wm
     * @param name
     */
    public Character(String name, WorldManager wm)
    {   
        this(name, null, null, wm);
    }
        
    /***
     * This constructor calls initScne(), setRenderStates() and adds this
     * Entity to the world manager.
     * @param name
     * @param modelIMI  -   the IMI xml file for loading of complex scenes 
     * @param wm
     */
    public Character(String name, PMatrix origin, String modelIMI, WorldManager wm)
    {
        super(name);
        m_wm = wm;
                
        // Initialize key bindings
        initKeyBindings();
    
        // The procedural scene graph
        m_pscene = new PScene(name, m_wm);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // Initialize character attributes
        initAttributes(name, null, null, origin, 1.0f, modelIMI);
        
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
    
    /***
     * This constructor calls initScne(), setRenderStates() and adds this
     * Entity to the world manager.
     * @param name
     * @param origin
     * @param modelFile
     * @param textureFile
     * @param wm
     */
    
    public Character(String name, PMatrix origin, String modelFile, String textureFile, final float visualScale, WorldManager wm)
    {
        super(name);
        m_wm = wm;
        
        // Initialize key bindings
        initKeyBindings();
    
        // The procedural scene graph
        m_pscene = new PScene(name, m_wm);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // Initialize character attributes
        initAttributes(name, modelFile, textureFile, origin, visualScale, null);
        
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
     * Override this method to initialize the game trigger actions mappings
     */
    protected abstract void initKeyBindings();
    
    /**
     * Override this method to allocate derrived attributes
     */
    protected Attributes createAttributes(String name)
    {
        return new Attributes(name);
    }
    
    /***
     * Override this method to initialize the character's attributes diffrently
     */
    protected void initAttributes(String name, String modelFile, String textureFile, final PMatrix origin, final float visualScale, String modelIMI)
    {
        m_attributes = createAttributes(name);
        if (modelFile != null)
            m_attributes.setModelFile(modelFile);
        if (textureFile != null)
            m_attributes.setTextureFile(textureFile);
        
        if (modelIMI == null)
        {
            if (m_attributes.getModelFile().endsWith(".ms3d"))
            {       
                SharedAsset character = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D, m_attributes.getModelFile()));
                AssetInitializer init = new AssetInitializer() {
                    public boolean initialize(Object asset) {

                        if (asset instanceof SkeletonNode)
                        {
                            // Grab the skinned mesh instance from the skelton node that was returned to us.
                            PPolygonSkinnedMeshInstance skinned = (PPolygonSkinnedMeshInstance)((SkeletonNode)asset).findChild("MS3DSkinnedMesh");

                            // Set position
                            if (origin != null)
                                m_modelInst.getTransform().setLocalMatrix(origin);

                            // Visual scale affects the skinned mesh and not the model instance
                            skinned.getTransform().getLocalMatrix(true).setScale(visualScale);

                            PMeshMaterial material = new PMeshMaterial("Character Material");
                            material.setTexture(new File(m_attributes.getTextureFile()), 0);
                            material.setShader(new VertexDeformer(m_wm));
                            skinned.getGeometry().setMaterial(material);
                            skinned.setUseGeometryMaterial(true);
                        }
                        return true;

                    }
                };
                character.setInitializer(init);
                m_attributes.setAsset(character);
            }
            else if (m_attributes.getModelFile().endsWith(".dae"))
            {
                SharedAsset character = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.COLLADA_Model, m_attributes.getModelFile()));
                character.setUserData(new ColladaLoaderParams(true, true, false, false, 4, "name", null));
                AssetInitializer init = new AssetInitializer() {
                    public boolean initialize(Object asset) {

                        if (((PNode)asset).getChild(0) instanceof SkeletonNode)
                        {
                            SkeletonNode skeleton = (SkeletonNode)((PNode)asset).getChild(0);
                            
                            // Visual Scale
                            ArrayList<PPolygonSkinnedMeshInstance> meshes = skeleton.getSkinnedMeshInstances();
                            for (PPolygonSkinnedMeshInstance mesh : meshes)
                                mesh.getTransform().getLocalMatrix(true).setScale(visualScale);
                            
                            // Set position
                            if (origin != null)
                                m_modelInst.getTransform().setLocalMatrix(origin);
  
                            // Set material
                            skeleton.setShader(new VertexDeformer(m_wm));
                            
                            // Set animations
                            if (m_attributes instanceof NinjaAvatarAttributes && ((NinjaAvatarAttributes)m_attributes).getAnimations() != null)
                            {
                                String [] anims = ((NinjaAvatarAttributes)m_attributes).getAnimations();
                                
                                String fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");

                                InstructionProcessor pProcessor = new InstructionProcessor();
                                Instruction pRootInstruction = new Instruction("setCharacterStuff");
                                pRootInstruction.addInstruction("setSkeleton", skeleton);
                                
                                
                                for (int i = 0; i < anims.length; i++)
                                    pRootInstruction.addInstruction("loadAnimation", fileProtocol + anims[i]);
                                
                                
                                Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
                                pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
                                pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressPants1.dae");
                                pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "Legs_LegsNudeShape");
                                pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
                                pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
                                pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/FlipFlopsFeet.dae");
                                //pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurfaceShape3");
                                pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
                                pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
                                pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
                                pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
                                pProcessor.execute(m_pscene, pRootInstruction);
                                    
                            }
                        }
                        return true;

                    }
                };
                character.setInitializer(init);
                m_attributes.setAsset(character);
            }
        }
        else // IMI xml configuration file (out of date)
        {
            SharedAsset character = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.Model, modelIMI));
            
            if (origin != null)
            {
                AssetInitializer init = new AssetInitializer() {
                    public boolean initialize(Object asset) {

                        if (asset instanceof SkeletonNode)
                        {
                            // Grab the skinned mesh instance from the skelton node that was returned to us.
                            PPolygonSkinnedMeshInstance skinned = (PPolygonSkinnedMeshInstance)((SkeletonNode)asset).findChild("MS3DSkinnedMesh");

                            // Visual scale affects the skinned mesh and not the model instance
                            // (might be defined in the xml... if so take this out)
                            skinned.getTransform().getLocalMatrix(true).setScale(visualScale);
                            
                            // Set position
                            if (origin != null)
                                m_modelInst.getTransform().setLocalMatrix(origin);
                        }
                        return true;
                    }
                };
                character.setInitializer(init);
            }
            
            m_attributes.setAsset(character);
        }
    }
    
    /**
     * Loads the model and sets processors 
     * @param processors
     */
    protected void initScene(ArrayList<ProcessorComponent> processors)
    {
        if (m_attributes.getModelFile().endsWith(".dae"))
        {
            m_pscene.setUseRepository(false);
            
            // Hack code - instead of loading it the same way
            // and using the initializer for run-time customizations
            
            InstructionProcessor pProcessor = new InstructionProcessor();
        
            String fileProtocol = new String("file://localhost/" + System.getProperty("user.dir") + "/");
        
            Instruction pRootInstruction = new Instruction("loadCharacter");
            pRootInstruction.addInstruction("loadBindPose", fileProtocol + m_attributes.getModelFile());
            
//            //pRootInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
//            pRootInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
//            pRootInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
//            //pRootInstruction.addInstruction("loadGeometry", fileProtocol + "assets/models/collada/Clothing/MaleDressPants1.dae");
//            pRootInstruction.addInstruction("loadGeometry", fileProtocol + "assets/models/collada/Clothing/FlipFlopsFeet.dae");
//            //pRootInstruction.addInstruction("addSkinnedMesh", "Legs_LegsNudeShape");
//            pRootInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
//            pRootInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
//            pRootInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
//            pRootInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
            
            // customize!
            switch (hack)
            {
                case 0: // flip flops, blue polo shirt, shorts!
                {
                    Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Pants/Shorts_M/Shorts.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "MaleShortsShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/FlipFlopsFeet.dae");
                    //pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurfaceShape3");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
                    pProcessor.execute(m_pscene, pRootInstruction);
                }
                break;
                case 1: // dress shoes, dress pants, sweater
                {
                    Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressPants1.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "Legs_LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressShoes.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurfaceShape3");
                    pReplaceGeometryInstruction.addInstruction("deleteGeometry", "Torso_TorsoNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteGeometry", "TorsoNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Accessories/MaleSweater.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "SweaterMaleShape");
//                                        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
//                                        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
//                                        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
//                                        pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
                    pProcessor.execute(m_pscene, pRootInstruction);
                }
                break;
                case 287: // hat sunglasses sneakers jeans (polySurface3Shape) etc
                {
                    Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Pants/Jeans_M/Jeans.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurface3Shape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Shoes/TennisShoes_M/MaleTennisShoes.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "TennisShoesShape");
                   
                    pProcessor.execute(m_pscene, pRootInstruction);
                }
                break;
                case 2:
                {
                    Instruction pReplaceGeometryInstruction = pRootInstruction.addInstruction("replaceGeometry");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "Legs_LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/MaleDressPants1.dae");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "Legs_LegsNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "LFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("deleteSkinnedMesh", "RFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("loadGeometry", "file://localhost/work/avatars/assets/models/collada/Clothing/FlipFlopsFeet.dae");
                    //pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "polySurfaceShape3");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFootNudeShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "LFlipFlopShape");
                    pReplaceGeometryInstruction.addInstruction("addSkinnedMesh", "RFlipFlopShape");
                    pProcessor.execute(m_pscene, pRootInstruction);
                }
                break;
                default:
                    break;
            }
            hack++;
            
            String [] anims = ((NinjaAvatarAttributes)m_attributes).getAnimations();
            for (int i = 0; i < anims.length; i++)
                pRootInstruction.addInstruction("loadAnimation", fileProtocol + anims[i]);

            pProcessor.execute(new PScene(m_wm), pRootInstruction);
            SkeletonNode skeleton = pProcessor.getSkeleton();
            m_modelInst = m_pscene.addModelInstance(skeleton, new PMatrix());

            if (m_modelInst.getChild(0) instanceof SkeletonNode)
            {
                SkeletonNode pSkeletonNode = (SkeletonNode) m_modelInst.getChild(0);
                //  Assign the specified shader to all SkinnedMeshes.
                pSkeletonNode.setShader(new VertDeformerWithSpecAndNormalMap(m_wm));
            }
        }
        else
            m_modelInst = m_pscene.addModelInstance(m_attributes.getName(), m_attributes.getAsset(), new PMatrix());
        
        processors.add(new SkinnedAnimationProcessor(m_modelInst));
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
                bool = method.invoke(context, transition.getContextMessgeArgs());
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
     * Called each frame
     * @param deltaTime
     */
    public void update(float deltaTime)
    {
        if (!m_initalized)
            initialize();
        
        if (m_context != null)
            m_context.update(deltaTime);
        
        
                                m_pscene.setDirty(true, true);
                                m_pscene.buildFlattenedHierarchy();
                                m_pscene.submitTransformsAndGeometry();
    }
    
    /**
     * Sets the mesh and skeleton references after load time
     */
    private void initialize()
    {
        // safty against place holders
        if (m_modelInst.getChild(0) instanceof SharedAssetPlaceHolder)
            return;
     
        if (m_modelInst.getChild(0).getChild(1) instanceof PPolygonSkinnedMeshInstance
                && m_modelInst.getChild(0) instanceof SkeletonNode) 
        {
            m_mesh       = (PPolygonSkinnedMeshInstance)m_modelInst.getChild(0).getChild(1);
            m_skeleton   = (SkeletonNode)m_modelInst.getChild(0);
            m_initalized = true;
        }
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
    
    public PPolygonSkinnedMeshInstance getMesh() {
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
    
    public Attributes getAttributes() {
        return m_attributes;
    }

    public void setAttributes(Attributes attributes) {
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
    
    
}
