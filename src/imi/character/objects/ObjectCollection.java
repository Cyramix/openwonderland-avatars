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
package imi.character.objects;

import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.scene.JScene;
import imi.scene.PScene;
import java.util.ArrayList;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class is a simple collection of objects, allowing for a very rudimentary
 * spatial object management system.
 * @author Lou Hayt
 */
public class ObjectCollection extends Entity
{
    /** Collection of objects :) **/
    protected ArrayList<SpatialObject> objects = new ArrayList<SpatialObject>();
    
    protected WorldManager    worldManager   = null;
    protected PScene          pscene         = null;
    protected JScene          jscene         = null;
    
    // Test
    Gadget lightSwitch = null;

    /**
     * Construct a new object collection with the given name using the world manager.
     * @param name
     * @param wm
     */
    public ObjectCollection(String name, WorldManager wm)
    {
        super(name);
        worldManager = wm;
        
        // The procedural scene graph
        pscene = new PScene(name, worldManager);
        
        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        
        // Initialize the scene
        //initScene(processors);
        
        // The glue between JME and pscene
        jscene = new JScene(pscene);
        
        // Use default render states (unless that method is overriden)
        setRenderStates();
        
        // Create a scene component and set the root to our jscene
        RenderComponent rc = worldManager.getRenderManager().createRenderComponent(jscene);
        
        // Add the scene component with our jscene to the entity
        addComponent(RenderComponent.class, rc);
        
        // Add our processors to a collection component
        ProcessorCollectionComponent processorCollection = new ProcessorCollectionComponent();
        for (int i = 0; i < processors.size(); i++)
            processorCollection.addProcessor(processors.get(i));
        
        // Add the processor collection component to the entity
        addComponent(ProcessorCollectionComponent.class, processorCollection);
        
        // Add the entity to the world manager
        worldManager.addEntity(this);  
    }

    /**
     * Add an object to the collection.
     * @param obj
     */
    public void addObject(SpatialObject obj) 
    {
        if (objects.contains(obj))
            return;
        objects.add(obj);
    }
    
    // TODO
    public void offsetPositionOfAllObjects(Vector3f offset)
    {
//        for(SpatialObject obj : objects)
//        {
//            
//        }
    }

    /**
     * Generate the specified number of chairs around the given center within
     * maxRadius of that center.
     * @param center
     * @param maxRadius
     * @param numberOfChairs
     */
    public void generateChairs(Vector3f center, float maxRadius, int numberOfChairs) 
    {
        if (numberOfChairs <= 0)
            return;
        
        for (int i = 0; i < numberOfChairs; i++)
        {
            float randomX = (float) Math.random();
            if (Math.random() > 0.5)
                randomX *= -1.0f;
            float randomZ = (float) Math.random();
            if (Math.random() > 0.5)
                randomZ *= -1.0f;
            Vector3f randomDirection = new Vector3f(randomX, 0.0f, randomZ).normalize();
            
            randomX = (float) Math.random();
            if (Math.random() > 0.5)
                randomX *= -1.0f;
            randomZ = (float) Math.random();
            if (Math.random() > 0.5)
                randomZ *= -1.0f;
            Vector3f randomSittingDirection = new Vector3f(randomX, 0.0f, randomZ).normalize();
            
            float randomDistance     = (float)Math.random() * maxRadius;
            Vector3f randomPosition  = center.add(randomDirection.mult(randomDistance));
            
            //Chair newChair = new Chair(randomPosition, randomSittingDirection, null); // renders as a sphere
            Chair newChair = new Chair(randomPosition, randomSittingDirection, "assets/models/collada/Objects/Chairs/ConfChair1.dae");
            newChair.setInScene(pscene);
            newChair.setObjectCollection(this);
            newChair.getModelInst().calculateBoundingSphere();
            
            int attemptsCounter = 0;
            while(isCloseToOtherChairs(newChair) && attemptsCounter < 100)
            {
                attemptsCounter++;
                
                randomX = (float) Math.random();
                if (Math.random() > 0.5)
                    randomX *= -1.0f;
                randomZ = (float) Math.random();
                if (Math.random() > 0.5)
                    randomZ *= -1.0f;
                randomDirection = new Vector3f(randomX, 0.0f, randomZ).normalize();

                randomX = (float) Math.random();
                if (Math.random() > 0.5)
                    randomX *= -1.0f;
                randomZ = (float) Math.random();
                if (Math.random() > 0.5)
                    randomZ *= -1.0f;
                randomSittingDirection = new Vector3f(randomX, 0.0f, randomZ).normalize();
                
                randomDistance  = (float)Math.random() * maxRadius;
                randomPosition  = center.add(randomDirection.mult(randomDistance));
                
                newChair.setPosition(randomPosition);
                newChair.getModelInst().buildFlattenedHierarchy();
                newChair.getModelInst().calculateBoundingSphere();
                
                if (attemptsCounter == 100)
                    System.out.println("ObjectCollection - generateChairs() - after 100 attempts was not able to find an empty space for this chair");
            }
        }
        
        // Make sure no funny stuff
        pscene.setDirty(true, true);
        pscene.buildFlattenedHierarchy();
        pscene.submitTransformsAndGeometry();
        
        // Dispaly PRenderer
        //jscene.setRenderInternallyBool(true);
        
        /////////  TEest   TEest   TEest   TEest   TEest   TEest   TEest   TEest 
//        
//        lightSwitch = new Gadget(new Vector3f(2.0f, 1.5f, 0.0f), Vector3f.UNIT_Z, "assets/models/collada/Objects/Interface/InterfaceSlider.dae");
//        lightSwitch.setInScene(pscene);
//        lightSwitch.setObjectCollection(this);
//        lightSwitch.getModelInst().calculateBoundingSphere();
//        
//        lightSwitch.translateSubMesh(Vector3f.UNIT_X, "Slider");
//        
//        pscene.setDirty(true, true);
//        pscene.buildFlattenedHierarchy();
//        pscene.submitTransformsAndGeometry();
//        
//        Gadget lighDimmer = new Gadget(new Vector3f(3.0f, 1.5f, 0.0f), Vector3f.UNIT_Z, "assets/models/collada/Objects/Interface/InterfaceKnobPlate.dae");
//        lighDimmer.setInScene(pscene);
//        lighDimmer.setObjectCollection(this);
//        lighDimmer.getModelInst().calculateBoundingSphere();
    }

    public void testLightToggle()
    {
        //lightSwitch.translateSubMesh(Vector3f.UNIT_X, "Slider");
        //lightSwitch.setRotationSubMesh(new Vector3f((float)Math.toRadians(0.0f),(float)Math.toRadians(0.0f), (float)Math.toRadians(0.0f)), "Slider");
    }
    
    /**
     * Check to see if the object is currently intersecting
     * another object in the collection according to their
     * overall sphere bounding volumes.
     * @param obj
     * @return
     */
    public boolean isColliding(SpatialObject obj) 
    {
        for (SpatialObject check : objects)
        {
            if (check != obj)
            {
                if (check.getBoundingSphere().isColliding(obj.getBoundingSphere()))
                    return true;
            }
        }
        return false;
    }
    
    // The chair's bounding volumes are not correct until finished loading, this method is still good on load time.
    private boolean isCloseToOtherChairs(Chair newChair) 
    {
        for (SpatialObject check : objects)
        {
            if (!(check instanceof Chair))
                continue;
            Chair chair = (Chair)check;
            if (chair != newChair)
            {
                Vector3f chairPos    = chair.getPosition();
                Vector3f newChairPos = newChair.getPosition();
                float desiredDistance = chair.getDesiredDistanceFromOtherChairs() + newChair.getDesiredDistanceFromOtherChairs();
                if (chairPos.distanceSquared(newChairPos) < desiredDistance * desiredDistance)
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Finds the nearest object within range, the searcCone should be between
     * 0.0f and 1.0f, if 1.0f it is not considered, otherwise it will be used
     * in a dot product with the right vector of the object to determind
     * if within area of interest (in front of the object).
     * @param obj   
     * @param consideredRange
     * @param searchCone 0.1f means only consider objects that are directly infront
     * @return
     */
    public SpatialObject findNearest(SpatialObject obj, float consideredRange, float searchCone)
    {
        if (obj == null)
            return null;
        
        SpatialObject nearest = null;
        float nearestObjDistance = 0.0f;
        for (SpatialObject check : objects)
        {
            if (check != obj)
            {
                // Check range
                float range = obj.getPosition().distance(check.getPosition());
                if(range > consideredRange)
                    continue;
        
                // First found element
                if (nearest == null)
                {
                    nearest  = check;
                    nearestObjDistance = range;
                    continue;
                }
                
                // Check if inside the search cone
                if (searchCone < 1.0f)
                {
                    Vector3f rightVec = obj.getRightVector();
                    Vector3f forwardVec = obj.getForwardVector();
                    Vector3f directionToTarget = check.getPosition().subtract(obj.getPosition());
                    directionToTarget.normalizeLocal();
                    
                    // Check if inside the front half of space
                    float dot = directionToTarget.dot(forwardVec);
                    if (dot > 0.0f)
                        continue;
                    
                    dot = directionToTarget.dot(rightVec);
                    if (dot < searchCone && dot > -searchCone)
                    {
                        if(range < nearestObjDistance)
                        {
                            nearest  = check;
                            nearestObjDistance = range;
                        }
                    }
                }
                else if(range < nearestObjDistance)
                {
                    nearest  = check;
                    nearestObjDistance = range;
                }
                 
            }
        }
        
        return nearest;
    }
    
    /**
     * Retrieve the nearest chair given the specified constraints
     * @param obj The object looking for a chair
     * @param consideredRange The maximum relavent range
     * @param searchCone 1.0 is 180 degree forward visibility, smaller values yield a smaller visibility wedge
     * @param occupiedMatters False to ignore occupancy (which is considered rude!)
     * @return The nearest chair, or null if none qualify
     */
    public SpatialObject findNearestChair(SpatialObject obj, float consideredRange, float searchCone, boolean occupiedMatters)
    {
        if (obj == null)
            return null;
        
        SpatialObject nearest = null;
        float nearestObjDistance = 0.0f;
        for (SpatialObject check : objects)
        {
            if (check != obj && check instanceof Chair)
            {
                // Check if occupided
                if (((Chair)check).isOccupied(occupiedMatters))
                    continue;
                
                // Check range
                float range = obj.getPosition().distance(check.getPosition());
                if(range > consideredRange)
                    continue;
        
                // First found element
                if (nearest == null)
                {
                    nearest  = check;
                    nearestObjDistance = range;
                    continue;
                }
                
                // Check if inside the search cone
                if (searchCone < 1.0f)
                {
                    Vector3f rightVec = obj.getRightVector();
                    Vector3f forwardVec = obj.getForwardVector();
                    Vector3f directionToTarget = ((Chair)check).getGoalPosition().subtract(obj.getPosition());
                    directionToTarget.normalizeLocal();
                    
                    // Check if inside the front half of space
                    float dot = directionToTarget.dot(forwardVec);
                    if (dot > 0.0f)
                        continue;
                    
                    dot = directionToTarget.dot(rightVec);
                    if (dot < searchCone && dot > -searchCone)
                    {
                        if(range < nearestObjDistance)
                        {
                            nearest  = check;
                            nearestObjDistance = range;
                        }
                    }
                }
                else if(range < nearestObjDistance)
                {
                    nearest  = check;
                    nearestObjDistance = range;
                }
                 
            }
        }
        
        return nearest;
    }

    /**
     * Find the location nearest to the provided SpatialObject.
     * @param obj The object that is looking
     * @param consideredRange Maximum relevant distance
     * @param searchCone 1.0 is 180 degree forward visibility, smaller values yield a smaller visibility wedge
     * @param occupiedMatters False to ignore occupancy
     * @return The nearest location, or null if none qualify
     */
    public LocationNode findNearestLocation(SpatialObject obj, float consideredRange, float searchCone, boolean occupiedMatters)
    {
        if (obj == null)
            return null;
        
        SpatialObject nearest = null;
        float nearestObjDistance = 0.0f;
        for (SpatialObject check : objects)
        {
            if (check != obj && check instanceof LocationNode)
            {
                // Check if occupided
                if (((LocationNode)check).isOccupied(occupiedMatters))
                    continue;
                
                // Check range
                float range = obj.getPosition().distance(check.getPosition());
                if(range > consideredRange)
                    continue;
        
                // First found element
                if (nearest == null)
                {
                    nearest  = check;
                    nearestObjDistance = range;
                    continue;
                }
                
                // Check if inside the search cone
                if (searchCone < 1.0f)
                {
                    Vector3f rightVec = obj.getRightVector();
                    Vector3f forwardVec = obj.getForwardVector();
                    Vector3f directionToTarget = check.getPosition().subtract(obj.getPosition());
                    directionToTarget.normalizeLocal();
                    
                    // Check if inside the front half of space
                    float dot = directionToTarget.dot(forwardVec);
                    if (dot > 0.0f)
                        continue;
                    
                    dot = directionToTarget.dot(rightVec);
                    if (dot < searchCone && dot > -searchCone)
                    {
                        if(range < nearestObjDistance)
                        {
                            nearest  = check;
                            nearestObjDistance = range;
                        }
                    }
                }
                else if(range < nearestObjDistance)
                {
                    nearest  = check;
                    nearestObjDistance = range;
                }
                 
            }
        }
        
        return (LocationNode)nearest;
    }
    
    public JScene getJScene() {
        return jscene;
    }

    /**
     * Removes a chair from the object collection.
     */
    public void removeChair()
    {    
        for (SpatialObject check : objects)
        {
            if (check instanceof Chair)
            {
                ((Chair)check).setOwner(null);
                ((Chair)check).setOccupied(true);
                objects.remove(check);
                pscene.removeModelInstance(check.getModelInst());
                
                return;
            }
        }
    }

    /**
     * Add another chair to the object collection
     */
    public void addRandomChair() 
    {
        // What are these magic numbers?
        Vector3f center = new Vector3f(3.905138f, 0.0f, 18.265793f);
       
        generateChairs(center, 7.0f, 1);
    }


    /**
     * Called in the constructor, override this method to set your own
     * non-default render states.
     */
    public void setRenderStates() 
    {
        // Z Buffer State
        ZBufferState buf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        
        // Material State
        MaterialState matState  = null;
        matState = (MaterialState) worldManager.getRenderManager().createRendererState(RenderState.RS_MATERIAL);
        matState.setDiffuse(ColorRGBA.white);
        
        // Light state
//        Vector3f lightDir = new Vector3f(0.0f, -1.0f, 0.0f);
//        DirectionalLight dr = new DirectionalLight();
//        dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
//        dr.setAmbient(new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f));
//        dr.setSpecular(new ColorRGBA(0.7f, 0.7f, 0.7f, 1.0f));
//        dr.setDirection(lightDir);
//        dr.setEnabled(true);
//        LightState ls = (LightState) worldManager.createRendererState(RenderState.RS_LIGHT);
//        ls.setEnabled(true);
//        ls.attach(dr);
        // SET lighting
        PointLight light = new PointLight();
        light.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setAmbient(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        light.setLocation(new Vector3f(-1000, 0, 0)); // not affecting anything
        light.setEnabled(true);
        LightState ls = (LightState) worldManager.getRenderManager().createRendererState(RenderState.RS_LIGHT);
        ls.setEnabled(true);
        ls.attach(light);
        
        // Cull State
        CullState cs = (CullState) worldManager.getRenderManager().createRendererState(RenderState.RS_CULL);      
        cs.setCullFace(CullState.Face.Back);
        cs.setEnabled(true);
        
        // Wireframe State
        WireframeState ws = (WireframeState) worldManager.getRenderManager().createRendererState(RenderState.RS_WIREFRAME);
        ws.setEnabled(false);
        
        // Push 'em down the pipe
        jscene.setRenderState(matState);
        jscene.setRenderState(buf);
        jscene.setRenderState(cs);
        jscene.setRenderState(ws);
        jscene.setRenderState(ls);
    }

}
