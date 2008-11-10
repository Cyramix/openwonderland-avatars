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
package imi.scene.camera.state;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 * This class encapsultes the required state for the TumbleObject camera model.
 * @author Ronald E Dahlgren
 */
public class TumbleObjectCamState extends CameraState
{
    // Camera spatial data
    private PMatrix camTransform = new PMatrix();
    // User experience configuration
    private float movementRate = 0.03f; 
    private float scaleRotationX = 0.56f;
    private float scaleRotationY = 0.56f;
    // Cached cursor info
    private int currentMouseX   = -1;
    private int currentMouseY   = -1;
    private int lastMouseX      = -1;
    private int lastMouseY      = -1;
    /** Maintain a reference to a PPolygonModelInstance to manipulate. **/
    private PPolygonModelInstance   modelInstance   = null;
    private Vector3f                focalPoint      = null;
    /** Rotation about the Y axis **/
    private float                   rotationY       = 0.0f; 
    private Vector3f                worldYVec       = new Vector3f(0,1,0);
    
    /** State indicators **/
    public final static int ZOOMING_IN  = 1;
    public final static int ZOOMING_OUT = 2;
    public final static int STOPPED     = 0;
    
    private int movementState = STOPPED;
    /** True anytime a new target is set, the camera must lookAt **/
    private boolean newTargetNeedsUpdate = false;
    private boolean bDirty = false;
    
    public TumbleObjectCamState(PPolygonModelInstance target)
    {
        setType(CameraStateType.TumbleObject);
        modelInstance = target;
        setTargetFocalPoint(modelInstance.getTransform().getWorldMatrix(false).getTranslation());
        newTargetNeedsUpdate = true;
    }
    
    public TumbleObjectCamState(PPolygonModelInstance target, float rotationOnYAxis)
    {
        setType(CameraStateType.TumbleObject);
        modelInstance = target;
        setTargetFocalPoint(modelInstance.getTransform().getWorldMatrix(false).getTranslation());
        newTargetNeedsUpdate = true;
        rotationY = rotationOnYAxis;
    }

    public int getCurrentMouseX()
    {
        return currentMouseX;
    }

    public void setCurrentMouseX(int currentMouseX)
    {
        this.currentMouseX = currentMouseX;
    }

    public int getCurrentMouseY()
    {
        return currentMouseY;
    }

    public void setCurrentMouseY(int currentMouseY)
    {
        this.currentMouseY = currentMouseY;
    }

    public int getLastMouseX()
    {
        return lastMouseX;
    }

    public void setLastMouseX(int lastMouseX)
    {
        this.lastMouseX = lastMouseX;
    }

    public int getLastMouseY()
    {
        return lastMouseY;
    }

    public void setLastMouseY(int lastMouseY)
    {
        this.lastMouseY = lastMouseY;
    }

    public PPolygonModelInstance getTargetModelInstance()
    {
        return modelInstance;
    }

    public void setTargetModelInstance(PPolygonModelInstance modelInstance)
    {
        this.modelInstance = modelInstance;
        newTargetNeedsUpdate = true;
    }

    public Vector3f getTargetFocalPoint()
    {
        return focalPoint;
    }

    public void setTargetFocalPoint(Vector3f focalPoint)
    {
        this.focalPoint = focalPoint;
        newTargetNeedsUpdate = true;
    }

    public float getMovementRate()
    {
        return movementRate;
    }

    public void setMovementRate(float movementRate)
    {
        this.movementRate = movementRate;
    }
    
    public int getMovementState()
    {
        return movementState;
    }
    
    public void setMovementState(int state)
    {
        movementState = state;
    }

    public float getRotationY()
    {
        return rotationY;
    }

    public void setRotationY(float rotationY)
    {
        this.rotationY = rotationY;
    }

    public float getScaleRotationX()
    {
        return scaleRotationX;
    }

    public void setScaleRotationX(float scaleRotationX)
    {
        this.scaleRotationX = scaleRotationX;
    }

    public float getScaleRotationY()
    {
        return scaleRotationY;
    }

    public void setScaleRotationY(float scaleRotationY)
    {
        this.scaleRotationY = scaleRotationY;
    }
    
    /**
     * Retrieve the transform
     * @return A *copy* of the current transform
     */
    public PMatrix getCameraTransform()
    {
        return new PMatrix(camTransform);
    }
    
    public void setCameraTransform(PMatrix newTransform)
    {
        camTransform.set(newTransform);
        newTargetNeedsUpdate = true;
    }
    
    public Vector3f getCameraPosition()
    {
        return camTransform.getTranslation();
    }
    
    public void setCameraPosition(Vector3f newPosition)
    {
        camTransform.setTranslation(newPosition);
        newTargetNeedsUpdate = true;
    }
    
    public boolean needsTargetUpdate()
    {
        return newTargetNeedsUpdate;
    }
    
    public void setTargetNeedsUpdate(boolean needed)
    {
        newTargetNeedsUpdate = needed;
    }
    
    public Vector3f getWorldYVector()
    {
        return worldYVec;
    }
    
    public boolean isDirty()
    {
        return bDirty;
    }
    
    public void setDirty(boolean dirty)
    {
        bDirty = dirty;
    }
}
