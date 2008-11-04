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
package imi.character.objects;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.utils.graph.Connection;
import imi.utils.graph.GraphNode;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class LocationNode extends GraphNode implements SpatialObject
{
    private String              name     = null;
    private PSphere             bv       = null;
    private Vector3f            forward  = null;
    private ObjectCollection    objects  = null;
    private boolean             occupied = false;

    public LocationNode(String name, Vector3f position, float radius, WorldManager wm)
    {
        this.name = name;
        bv        = new PSphere(position, radius);
        objects   = new ObjectCollection(name, wm);
        objects.addObject(this);
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

    public void setObjectCollection(ObjectCollection objs) {
        objects = objs; 
    }

    public ObjectCollection getObjectCollection()
    {
        return objects;
    }

    public void generateChairs(int numberOfChairs) {
        objects.generateChairs(bv.getCenter(), bv.getRadius(), numberOfChairs);
    }
    
    public Vector3f getPosition() {
        if (bv != null)
            return bv.getCenter();
        return null;
    }

    public Quaternion getQuaternion() {
        return null;
    }

    public Vector3f getRightVector() {
        return null;
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
    
    /**
     * The connection's name is the name of the path, there can be only one
     * connection for a single path.
     * @param con
     */
    @Override
    public void addConnection(Connection con)
    {
        for (Connection c : connections)
        {
            if (c == con || c.getName().equals(con.getName()))
                return;
        }
        
        connections.add(con);
    }
    
}
