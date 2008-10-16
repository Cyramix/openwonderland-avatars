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
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.character.objects.ObjectCollection;
import imi.character.objects.SpatialObject;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.TransitionObject;
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
import imi.scene.boundingvolumes.PSphere;
import imi.scene.shader.programs.VertexDeformer;
import imi.scene.polygonmodel.parts.PMeshMaterial;
import imi.scene.processors.JSceneAWTEventProcessor;
import java.io.File;

/**
 *
 * @author Lou Hayt
 */
public abstract class Character extends Entity implements SpatialObject
{   
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
    
    public Character(String name, WorldManager wm, String configuration) 
    {
        this(name, null, null, wm);
        if (configuration.equals("Ninja"))
        {
            // Defaults are set for the Ninja
        }
        else if (configuration.equals("Adam"))
        {
            // TODO
            // load "adam.ms3d", "AdamDiffuse.png" and set scale to 0.06f
            // TODO
            
//            this.getController().setReverseHeading(true);
//            this.getContext().getStates().get(PunchState.class).setAnimationSpeed(3.0f);
//            ((PunchState)this.getContext().getStates().get(PunchState.class)).setMinimumTimeBeforeTransition(1.25f);
//            this.getContext().getStates().get(TurnState.class).setAnimationName("StrafeRight");
//            this.getContext().getStates().get(TurnState.class).setAnimationSpeed(5.0f);
//            ((WalkState)this.getContext().getStates().get(WalkState.class)).setWalkSpeedFactor(5.0f);
//            ((WalkState)this.getContext().getStates().get(WalkState.class)).setWalkSpeedMax(5.0f);
//            this.getContext().getStates().get(SitState.class).setAnimationName("CrouchWalk");
//            ((SitState)this.getContext().getStates().get(SitState.class)).setIdleSittingAnimationName("Crawl");
//            ((SitState)this.getContext().getStates().get(SitState.class)).setSittingAnimationTime(2.0f);
//            ((SitState)this.getContext().getStates().get(SitState.class)).setGettingUpAnimationName("CrouchWalk");
//            ((SitState)this.getContext().getStates().get(SitState.class)).setGettingUpAnimationTime(2.0f);
        }
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
            SharedAsset character = new SharedAsset(m_pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D, "assets/models/ms3d/" + m_attributes.getModelFile()));
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
                        material.setTexture(new File("assets/textures/" + m_attributes.getTextureFile()), 0);
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
        else
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
        ((JSceneAWTEventProcessor)m_wm.getUserData(JSceneAWTEventProcessor.class)).setJScene(m_jscene); 
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
        return m_modelInst.getBoundingSphere();
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
    
    public Vector3f getNearestObstaclePosition(Vector3f myPosition)
    {
        return null;
    }
    
}
