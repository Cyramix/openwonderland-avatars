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
package imi.objects;

import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.BlendState;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.MaterialState.ColorMaterial;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.repository.AssetDescriptor;
import imi.repository.SharedAsset;
import imi.repository.SharedAsset.SharedAssetType;
import imi.scene.JScene;
import imi.scene.PMatrix;
import imi.scene.PScene;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonMesh;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.PMeshMaterial;
import imi.scene.utils.PMeshUtils;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This class represents a "Goal". This may be interpreted however is appropriate,
 * the class handles visualizing the goal as well.
 * @author Lou Hayt
 */
@ExperimentalAPI
public class Goal 
{
    SpatialObject goal = null;
    
    PPolygonModelInstance modelInst = null;
        
    JScene jscene = null;

    /**
     * Construct a new goal using the provided WorldManager.
     * @param wm
     */
    public Goal(WorldManager wm)
    {
        float       radius  = 1.05f;
        ColorRGBA   color   = ColorRGBA.blue;
        PMatrix     origin  = new PMatrix(new Vector3f(0.0f, 0.0f, 0.0f));
        
        // The procedural scene graph
        PScene pscene = new PScene("Goal PScene", wm);
        
        // The collection of processors for this entity
        //FastTable<ProcessorComponent> processors = new FastTable<ProcessorComponent>();
        
        // Initialize the scene
        SharedAsset modelAsset = new SharedAsset(pscene.getRepository(), new AssetDescriptor(SharedAssetType.MS3D_Mesh, ""));
        PMeshMaterial geometryMaterial = new PMeshMaterial();
        geometryMaterial.setColorMaterial(ColorMaterial.Diffuse); // Make the vert colors affect diffuse coloring
        geometryMaterial.setDiffuse(ColorRGBA.white);
        PPolygonMesh sphereMesh = PMeshUtils.createSphere("Goal Sphere", Vector3f.ZERO, radius, 2, 2, color);
        sphereMesh.setMaterial(geometryMaterial);
        sphereMesh.submit();
        modelAsset.setAssetData(sphereMesh);
        modelInst = pscene.addModelInstance(modelAsset, origin);
                
        // The glue between JME and pscene
        jscene = new JScene(pscene);
        
        // Use default render states
        setDefaultRenderStates(jscene, wm);
        jscene.setWireframe(true);              // WTF?
               
        // Create entity
        Entity JSEntity = new Entity("Goal Entity");
        
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
        
    }

    public SpatialObject getGoal() {
        return goal;
    }

    public void setGoal(SpatialObject goal) {
        this.goal = goal;
        
//        Vector3f pos       = goal.getPosition();
//        Vector3f direction = goal.getForwardVector();
//        PMatrix goalMatrix = new PMatrix(pos); 
//        goalMatrix.lookAt(pos, pos.add(direction), Vector3f.UNIT_Y);
//        goalMatrix.invert();
//        modelInst.getTransform().setLocalMatrix(goalMatrix);
//        modelInst.getTransform().getLocalMatrix(true).setScale(1.0f);
//        //goalPoint.getTransform().getLocalMatrix(true).setTranslation(pos);
//        PScene GPScene = getPScene();
//        GPScene.setDirty(true, true);
//        GPScene.submitTransforms();
    }
    
    public PScene getPScene() 
    {
        return (PScene) modelInst.getParent().getParent();
    }

    public PTransform getTransform() 
    {
        return modelInst.getTransform();
    }
    
    public void setDefaultRenderStates(JScene jscene, WorldManager wm) 
    {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) wm.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        // Material State
        float opacityAmount = 0.5f;
        MaterialState matState  = null;
        matState = (MaterialState) wm.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        //matState.setDiffuse(ColorRGBA.white);
        matState.setAmbient(new ColorRGBA(0.0f, 0.0f, 0.0f, opacityAmount));
        matState.setDiffuse(new ColorRGBA(0.1f, 0.5f, 0.8f, opacityAmount));
        matState.setSpecular(new ColorRGBA(1.0f, 1.0f, 1.0f, opacityAmount));
        matState.setShininess(128.0f);
        matState.setEmissive(new ColorRGBA(0.0f, 0.0f, 0.0f, opacityAmount));
        matState.setEnabled(true);
        
        matState.setMaterialFace(MaterialState.MaterialFace.FrontAndBack);
        
        // Light state
//        Vector3f lightDir = new Vector3f(0.0f, -1.0f, 0.0f);
//        DirectionalLight dr = new DirectionalLight();
//        dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
//        dr.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
//        dr.setSpecular(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
//        dr.setDirection(lightDir);
//        dr.setEnabled(true);
//        LightState ls = (LightState) wm.createRendererState(RenderState.RS_LIGHT);
//        ls.setEnabled(true);
//        ls.attach(dr);
        // SET lighting
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAmbient(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setSpecular(ColorRGBA.white);
        light.setLocation(new Vector3f(100, 100, 100)); // not affecting anything ?
        light.setEnabled(true);
        LightState ls = (LightState) wm.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.detachAll();
        ls.setEnabled(true);
        ls.attach(light);
        
        // Cull State
        CullState cs = (CullState) wm.getRenderManager().createRendererState(RenderState.RS_CULL);      
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        
        // Wireframe State
        WireframeState ws = (WireframeState) wm.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(false);
        
        // Set transparancy
        BlendState alphaState = (BlendState) wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
        alphaState.setEnabled(true);
        alphaState.setBlendEnabled(true);
        alphaState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
        alphaState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
        alphaState.setTestEnabled(true);
        alphaState.setTestFunction(BlendState.TestFunction.GreaterThan);
                
        // Push 'em down the pipe
        //jscene.setRenderState(alphaState);
        jscene.setRenderState(matState);
        jscene.setRenderState(buf);
        jscene.setRenderState(cs);
        jscene.setRenderState(ws);
        jscene.setRenderState(ls);
        //jscene.setRenderQueueMode(Renderer.QUEUE_TRANSPARENT);
        jscene.updateRenderState();
    }

}
