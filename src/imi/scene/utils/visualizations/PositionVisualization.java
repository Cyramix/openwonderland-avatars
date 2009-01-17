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
package imi.scene.utils.visualizations;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;

/**
 *
 * @author Lou Hayt
 */
public class PositionVisualization 
{
    /** Root of the object **/
    Node objectRoot = null;
    /** Reference to the overall object's position **/
    Vector3f objectPosition = null;
    /** The visualization **/
    Sphere sphere = null;

    /**
     * Construct a new visualization object.
     * @param verletObject
     */
    public PositionVisualization(Vector3f position)
    {
        objectRoot = new Node(position.toString());

        // grab reference from the verlet object and map it
        objectPosition = position;
        objectRoot.setLocalTranslation(objectPosition);
        
        // clear out the old
        objectRoot.detachAllChildren();

        // add a new sphere
        sphere = new Sphere("Position sphere", Vector3f.ZERO, 10, 10, 0.05f);
        sphere.setDefaultColor(ColorRGBA.cyan);

        // Attach the sphere to the scene root
        objectRoot.attachChild(sphere);
    }

    /**
     * Update all the local translation components of the nodes representing the particles
     */
    public void updatePositions()
    {
          // Update the position
          sphere.setLocalTranslation(objectPosition);
    }


    
    
    ////////////////////////////////////////////////////////////////////////////
    /// Equality checking is agnostic of all state except the verlet object  ///
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PositionVisualization other = (PositionVisualization)obj;
        if (objectPosition.equals(other.getObjectPosition()) && sphere.equals(other.getSphere()))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.objectPosition != null ? this.objectPosition.hashCode() : 0);
        hash = 71 * hash + (this.sphere != null ? this.sphere.hashCode() : 0);
        return hash;
    }

    public Vector3f getObjectPosition() {
        return objectPosition;
    }

    public Sphere getSphere() {
        return sphere;
    }


}
