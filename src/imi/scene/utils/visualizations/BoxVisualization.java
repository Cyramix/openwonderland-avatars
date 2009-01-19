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
import com.jme.scene.shape.Box;

/**
 * quick visu for testing
 * @author Lou Hayt
 */
public class BoxVisualization 
{
    /** Root of the object **/
    Node objectRoot = null;
    /** Reference to the overall object's position **/
    Vector3f origin = null;
    Vector3f min = null;
    Vector3f max = null;
    /** The visualization **/
    Box box = null;

    /**
     * Construct a new visualization object.
     * @param verletObject
     */
    public BoxVisualization(Vector3f origin, Vector3f min, Vector3f max, ColorRGBA color) 
    {
        objectRoot = new Node("Box visu");
        this.origin = origin;
        this.min = min;
        this.max = max;
        objectRoot.setLocalTranslation(origin);
        
        // clear out the old
        objectRoot.detachAllChildren();

        // add a new sphere
        box = new Box("Box visu", min, max);
        box.setDefaultColor(color);

        // Attach the sphere to the scene root
        objectRoot.attachChild(box);
    }

    public void updatePositions()
    {
          // Update the position
          box.setLocalTranslation(origin);
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
        BoxVisualization other = (BoxVisualization)obj;
        if (min.equals(other.getMin()) && max.equals(other.getMax()))
            return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.min != null ? this.min.hashCode() : 0);
        hash = 47 * hash + (this.max != null ? this.max.hashCode() : 0);
        return hash;
    }

    public Vector3f getMax() {
        return max;
    }

    public Vector3f getMin() {
        return min;
    }

    
    
}
