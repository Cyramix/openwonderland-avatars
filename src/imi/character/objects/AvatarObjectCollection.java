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

import imi.character.objects.console.ObjectCollectionGUI;
import com.jme.light.PointLight;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.state.CullState;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.RenderState;
import com.jme.scene.state.WireframeState;
import com.jme.scene.state.ZBufferState;
import imi.character.avatar.Avatar;
import imi.scene.JScene;
import imi.scene.PScene;
import imi.scene.utils.visualizations.VisuManager;
import imi.utils.graph.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import org.jdesktop.mtgame.ProcessorCollectionComponent;
import org.jdesktop.mtgame.ProcessorComponent;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class is a simple collection of objects, allowing for a very rudimentary
 * spatial object management system.
 * @author Lou Hayt
 */
public class AvatarObjectCollection extends ObjectCollectionBase
{
    /** Collection of objects :) **/
    protected ArrayList<SpatialObject> objects = new ArrayList<SpatialObject>();

    /** JGraph of locations :) **/
    //protected JGraph locations = new JGraph();
    /** Map of location names to location objects **/
    protected Hashtable<String, LocationNode> locationNames = new Hashtable<String, LocationNode>();

    protected WorldManager    worldManager   = null;
    protected PScene          pscene         = null;
    protected JScene          jscene         = null;

    /** Command line console :) **/
    protected ObjectCollectionGUI gui        = null;

    // Test :)
    Gadget lightSwitch = null;

    /**
     * Construct a new object collection with the given name using the world manager.
     * @param name
     * @param wm
     */
    public AvatarObjectCollection(String name, WorldManager wm)
    {
        super(name, wm);
        worldManager = wm;

        // The procedural scene graph
        pscene = new PScene(name, worldManager);

        // The collection of processors for this entity
        ArrayList<ProcessorComponent> processors = new ArrayList<ProcessorComponent>();
        processors.add(jmeGraphProc);
        
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

    public void boundingVolumeTest()
    {
       // for (S)
    }

    @Override
    public void hackRefresh()
    {
        getPScene().submitTransformsAndGeometry();
        getJScene().updateRenderState();
    }

    /////////////////////////  JGraph integration ///////////////////////////////////

    /**
     * For internal use
     */
//    public JGraph getLocationGraph() {
//        return locations;
//    }

    public Collection<LocationNode> getLocations(){
        return locationNames.values();
    }

    public void createLocation(String name, Vector3f position, float radius){
        new LocationNode(name, position, radius, this);
    }

    /** Called from LocationNode's constructor **/
    @Override
    public void addLocation(LocationNode location)
    {
        //if (!locations.containsVertex(location))
        {
            //locations.addVertex(location);
            locationNames.put(location.getName(), location);
            objects.add(location);
        }
    }

    public void removeLocation(String name) {
        removeLocation(getLocation(name));
    }
    @Override
    public void removeLocation(LocationNode location) {
        if (location == null)
            return;
        //locations.removeVertex(location);
        locationNames.remove(location.getName());
        objects.remove(location);
    }

    @Override
    public void removeObject(SpatialObject obj) {
        if (obj == null)
            return;
        getPScene().removeModelInstance(obj.getModelInst());
        objects.remove(obj);
    }

    public LocationNode getLocation(String name)
    {
        LocationNode location = locationNames.get(name);
        //if (locations.containsVertex(location))
            return location;
//        else
//            return null;
    }

    public Connection createConnection(LocationNode source, LocationNode destination){
        return null;//locations.addEdge(source, destination);
    }

    public Connection createConnection(String source, String destination){
        return createConnection(locationNames.get(source), locationNames.get(destination));
    }

    @Override
    public ArrayList<LocationNode> findPath(LocationNode source, String destination)
    {
        return null;
//        LocationNode destNode = locationNames.get(destination);
//        if (source.equals(destNode))
//            return null;
//        ArrayList<LocationNode> path = new ArrayList<LocationNode>();
//        LocationNode find = findConnection(source, destination, false);
//        if (find != null)
//        {
//            path.add(find);
//            return path;
//        }
//        try{
//            List<Connection> list = org.jgrapht.alg.BellmanFordShortestPath.findPathBetween(locations, source, destNode);
//            for (Connection con : list)
//                path.add((LocationNode) getConnectionDestination(con));
//        }
//        catch (Exception ex) {}
//        return path;
    }

    @Override
    public LocationNode findConnection(LocationNode source, String targetName, boolean allowBaked)
    {
//        Set<Connection> cons = locations.outgoingEdgesOf(source);
//        for (Connection con : cons)
//        {
//            GraphNode node = locations.getEdgeTarget(con);
//            if (node instanceof LocationNode)
//            {
//                // If we find a connected location node then great
//                LocationNode LNode = (LocationNode)node;
//                if (LNode.getName().equals(targetName))
//                    return LNode;
//            }
//        }
        // If we don't we try checking the baked connections
        if (allowBaked)
            return source.getBakedConnection(targetName);
        return null;
    }

//    public Connection getConnection(String sourceName, String destinationName) {
//        return locations.getEdge(locationNames.get(sourceName), locationNames.get(destinationName));
//    }
//
//    public Connection getConnection(LocationNode source, LocationNode destination) {
//        return locations.getEdge(source, destination);
//    }
//
//    public boolean removeConnection(Connection con) {
//        return locations.removeEdge(con);
//    }
//
//    public GraphNode getConnectionSource(Connection con){
//        return locations.getEdgeSource(con);
//    }
//    public GraphNode getConnectionDestination(Connection con){
//        return locations.getEdgeTarget(con);
//    }
//    public double getConnectionWeight(Connection con){
//        return locations.getEdgeWeight(con);
//    }

    public Avatar getAvatar(int objectID)
    {
        SpatialObject obj = objects.get(objectID);
        if (obj != null && obj instanceof Avatar)
            return (Avatar)obj;
        return null;

    }

    public void createTestPath()
    {
        float step = 3.0f;
        float radius = 1.0f;
        LocationNode loc1 = new LocationNode("loc1", Vector3f.ZERO, radius, this);
        LocationNode loc2 = new LocationNode("loc2", Vector3f.UNIT_X.mult(step),  radius, this);
        LocationNode loc3 = new LocationNode("loc3", new Vector3f(step, 0.0f, step),  radius, this);
        LocationNode loc4 = new LocationNode("loc4", Vector3f.UNIT_Z.mult(step),  radius, this);

        // quick ugly visu for test
        ArrayList<Vector3f> origin = new ArrayList<Vector3f>();
        ArrayList<Vector3f> point = new ArrayList<Vector3f>();

        // Create graph paths
        createConnection(loc1, loc2); origin.add(loc1.getPositionRef()); point.add(loc2.getPositionRef());
        createConnection(loc2, loc3); origin.add(loc2.getPositionRef()); point.add(loc3.getPositionRef());
        createConnection(loc3, loc4); origin.add(loc3.getPositionRef()); point.add(loc4.getPositionRef());
        createConnection(loc4, loc1); origin.add(loc4.getPositionRef()); point.add(loc1.getPositionRef());

        VisuManager vis = new VisuManager("path connections visu", worldManager);
        vis.addLineObject(origin, point, ColorRGBA.brown, 5.0f);
    }

    /////////////////////////////////////////////////////////////////////////

    /**
     * Add an object to the collection.
     * @param obj
     */
    @Override
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
            AvatarChair newChair = new AvatarChair(randomPosition, randomSittingDirection, "assets/models/collada/Objects/Chairs/ConfChair1.dae");
            newChair.setObjectCollection(this);
            newChair.setInScene(pscene);

            int attemptsCounter = 0;
            while(isCloseToOtherChairs(newChair) && attemptsCounter < 1000)
            {
                attemptsCounter++;

                randomX = (float) Math.random();
                if (Math.random() > 0.5)
                    randomX *= -1.0f;
                randomZ = (float) Math.random();
                if (Math.random() > 0.5)
                    randomZ *= -1.0f;
                randomDirection = new Vector3f(randomX, 0.0f, randomZ).normalize();

//                randomX = (float) Math.random();
//                if (Math.random() > 0.5)
//                    randomX *= -1.0f;
//                randomZ = (float) Math.random();
//                if (Math.random() > 0.5)
//                    randomZ *= -1.0f;
//                randomSittingDirection = new Vector3f(randomX, 0.0f, randomZ).normalize();

                randomDistance  = (float)Math.random() * maxRadius;
                randomPosition  = center.add(randomDirection.mult(randomDistance));

                newChair.setPosition(randomPosition);
                newChair.getModelInst().buildFlattenedHierarchy();

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

    public PScene getPScene() {
        return pscene;
    }

    // The chair's bounding volumes are not correct until finished loading, this method is still good on load time.
    private boolean isCloseToOtherChairs(TargetObject newChair)
    {
        for (SpatialObject check : objects)
        {
            if (!(check instanceof ChairObject))
                continue;
            TargetObject chair = (TargetObject)check;
            if (chair != newChair)
            {
                Vector3f chairPos    = chair.getPositionRef();
                Vector3f newChairPos = newChair.getPositionRef();
                float desiredDistance = chair.getDesiredDistanceFromOtherTargets() + newChair.getDesiredDistanceFromOtherTargets();
                if (chairPos.distanceSquared(newChairPos) < desiredDistance * desiredDistance)
                    return true;
            }
        }
        return false;
    }

    @Override
    public SpatialObject findNearestObjectOfType(Class type, SpatialObject obj, float consideredRange, float searchCone, boolean occupiedMatters)
    {
        // TODO - actual implemintation :D
        if (type.equals(LocationNode.class))
            return findNearestLocation(obj, consideredRange, searchCone, occupiedMatters);
        else if (type.equals(ChairObject.class))
            return findNearestChair(obj, consideredRange, searchCone, occupiedMatters);
        else if (type.equals(SpatialObject.class))
            return findNearestObject(obj, consideredRange, searchCone, occupiedMatters);
        return null;
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
                float range = obj.getPositionRef().distance(check.getPositionRef());
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
                    Vector3f directionToTarget = check.getPositionRef().subtract(obj.getPositionRef());
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
            if (check != obj && check instanceof ChairObject)
            {
                // Check if occupided
                boolean oc = ((TargetObject)check).isOccupied();
                if (!occupiedMatters)
                    oc = false;
                if (oc)
                    continue;

                // Check range
                float range = obj.getPositionRef().distance(check.getPositionRef());
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
                    Vector3f directionToTarget = ((TargetObject)check).getTargetPositionRef().subtract(obj.getPositionRef());
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
                float range = obj.getPositionRef().distance(check.getPositionRef());
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
                    Vector3f directionToTarget = check.getPositionRef().subtract(obj.getPositionRef());
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

    @Override
    public JScene getJScene() {
        return jscene;
    }

    /**
     * Removes a chair from the object collection.
     */
    public void removeAChair()
    {
        for (SpatialObject check : objects)
        {
            if (check instanceof ChairObject)
            {
                ((TargetObject)check).setOwner(null);
                ((TargetObject)check).setOccupied(true);
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
        //Vector3f center = new Vector3f(3.905138f, 0.0f, 18.265793f);
        Vector3f center = Vector3f.ZERO;

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

    public ArrayList<SpatialObject> getObjects() {
        return objects;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public ObjectCollectionGUI getGUI()
    {
        if (gui == null)
            gui = new ObjectCollectionGUI(this, worldManager);
        return gui;
    }

}
