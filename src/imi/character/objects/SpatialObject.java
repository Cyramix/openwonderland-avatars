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

import com.jme.math.Vector3f;
import imi.scene.boundingvolumes.PSphere;
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 * The base class for an object occupying some location in space.
 * @author Lou Hayt
 */
public interface SpatialObject 
{
    /** remove and shut down **/
    public void destroy();
    
    /**
     * Returns the model instance of the object
     * @return
     */
    public PPolygonModelInstance getModelInst();
    
    /**
     * Returns the overall bounding sphere of the object
     * @return
     */
    public PSphere getBoundingSphere();
    
    /**
     * Returns the nearest obstacle center position
     * @return
     */
    public PSphere getNearestObstacleSphere(Vector3f myPosition);
    
    /**
     * Adds this object to an object collection
     * and sets the object collection for the object.
     * @param objs
     */
    public void setObjectCollection(ObjectCollectionBase objs);
    
    /**
     * Gets the position of the model instance
     * @return
     */
    public Vector3f getPosition();
    
    /**
     * Gets the right vector of the model instance
     * @return
     */
    public Vector3f getRightVector();
    
    /**
     * Gets the forward vector of the model instance
     * @return
     */
    public Vector3f getForwardVector();
}
