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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.utils.graph.GraphNode;
import java.util.Hashtable;

/**
 * This class represents a node (used in avatar path following) at a given location.
 * @author Lou Hayt
 */
public class LocationNode extends GraphNode implements SpatialObject
{
    /** The name of this location **/
    private String              name     = null;
    /** The bounding volume for this location **/
    private PSphere             bv       = null;
    private Vector3f            forward  = Vector3f.UNIT_Z.mult(-1.0f);
    /** A collection of objects at this location (chairs in a classroom for instance) **/
    private ObjectCollectionBase objects  = null;
    private boolean             occupied = false;
    private Hashtable<String, LocationNode> bakedConnections = new Hashtable<String, LocationNode>();
   
    /**
     * Construct a new location node with the provided information.
     * @param name
     * @param position
     * @param radius
     * @param wm
     * @param objectCollection
     */
    public LocationNode(String name, Vector3f position, float radius, ObjectCollectionBase objectCollection)
    {
        this.name = name;
        bv        = new PSphere(position, radius);
        setObjectCollection(objectCollection);
    }
    
    /** Finds a location node with that name from the graph or the baked connections **/
    public LocationNode findConnection(String name)
    {
        return objects.findConnection(this, name, true);
    }

    public LocationNode getBakedConnection(String targetName) {
        return bakedConnections.get(targetName);
    }
    
    public void addBakedConnection(String destinationName, LocationNode node)
    {
        bakedConnections.put(destinationName, node);
    }
    
    public void removeBakedConnection(String name)
    {
        bakedConnections.remove(name);
    }
    
    public PPolygonModelInstance getModelInst() {
        return null;
    }

    public PSphere getBoundingSphere() {
        return bv;
    }

    public PSphere getNearestObstacleSphere(Vector3f myPosition) {
        return null;
    }

    public void setObjectCollection(ObjectCollectionBase objs)
    {
        objects = objs; 
        if (objects != null)
            objects.addLocation(this);
    }

    public ObjectCollectionBase getObjectCollection()
    {
        return objects;
    }

    /**
     * Generate the given number of chairs within this location.
     * @param numberOfChairs
     */
    public void generateChairs(int numberOfChairs) {
        if (objects != null && objects instanceof ObjectCollection)
            ((ObjectCollection)objects).generateChairs(bv.getCenter(), bv.getRadius(), numberOfChairs);
        else
            System.out.println("ERROR: LocationNode generateChairs() failed");
    }
    
    public Vector3f getPosition() {
        if (bv != null)
            return bv.getCenter();
        return null;
    }

    public Quaternion getQuaternion() {
        Quaternion result = new Quaternion();
        result.fromAxes(getRightVector(), Vector3f.UNIT_Y, forward);
        return result;
    }

    public Vector3f getRightVector() {
        return forward.cross(Vector3f.UNIT_Y);
    }

    public Vector3f getForwardVector() {
        return forward;
    }
    
    public void setForwardVector(Vector3f fwd)
    {
        forward = fwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
    
    public boolean isOccupied(boolean occupiedMatters) 
    {
        if (occupiedMatters)
            return occupied;
        return false;
    }
    
    public void destroy() 
    {
        if (objects != null)
            objects.removeLocation(this);
    }

    
}
