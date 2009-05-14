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
package imi.scene.camera.state;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 * This class encapsultes the required state for the TumbleObject camera model.
 * There is no complex logic in this class, just lots of accessors and mutators.
 * @author Ronald E Dahlgren
 */
public class ThirdPersonCamState extends CameraState
{
    /** The camera's transformation matrix **/
    private PMatrix camTransform = new PMatrix();
    /** If this is non-null, the camera is moving towards this position **/
    private Vector3f nextPosition = null;
    /** Cached for proper lerping **/
    private Vector3f originalPosition = null;
    /** An offset for the focal point from the character **/
    private final Vector3f offsetFromCharacterTransform = new Vector3f();

    // User experience configuration below
    /** The amount of movement per second **/
    private float movementRate = 0.0386f;
    /** Scale rotation from screen delta by this amount **/
    private float scaleRotationX = 0.56f;
    private float scaleRotationY = 0.56f;
    /** Length of transition in seconds **/
    private float transitionDuration = 1.5f;

    // Cached cursor info
    private int currentMouseX   = -1;
    private int currentMouseY   = -1;
    private int lastMouseX      = -1;
    private int lastMouseY      = -1;

    /** Maintain a reference to a PPolygonModelInstance to manipulate. **/
    private PPolygonModelInstance   modelInstance   = null;
    /** This is the place that the camera looks at and zooms towards **/
    private Vector3f                focalPoint      = new Vector3f();
    /** If this is non-null, the camera is transitioning to this focal point **/
    private Vector3f                nextFocalPoint  = null;
    /** Kept to lerp properly **/
    private Vector3f                originalFocalPoint  = null;
    /** Rotation about the Y axis **/
    private float                   rotationY       = 0.0f;

    /** State indicators **/
    public final static int ZOOMING_IN  = 1;
    public final static int ZOOMING_OUT = 2;
    public final static int MOVING_LEFT = 3;
    public final static int MOVING_RIGHT = 4;
    public final static int STOPPED     = 0;

    /** Current movement state **/
    private int movementState = STOPPED;

    /** Data for transitioning **/
    private float timeInPositionTransition = 0.0f;
    private float timeInFocusTransition = 0.0f;

    /** True anytime a new target is set, the camera must lookAt **/
    private boolean newTargetNeedsUpdate = false;
    /** This is the minimum distance squared that is allowed between the camera and its focal point **/
    private float minimumDistanceSquared = 2.5f;
    /** Outer boundary **/
    private float maximumDistanceSquared = 36;
    /** Offset from target after move **/
    private final Vector3f vectorToCamera = new Vector3f(0,3,-2);

    public void getCameraPosition(Vector3f vOut)
    {
        camTransform.getTranslation(vOut);
    }

    public float getDefaultCameraOffset() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public float getMaximumDistanceSquared()
    {
        return maximumDistanceSquared;
    }

    public void getOriginalFocalPoint(Vector3f vOut) {
        vOut.set(originalFocalPoint);
    }

    public void getOriginalPosition(Vector3f vOut) {
        vOut.set(originalPosition);
    }

    public void getTargetFocalPoint(Vector3f vOut)
    {
        vOut.set(focalPoint);
    }

    public Vector3f getToCamera()
    {
        return vectorToCamera;
    }

    public void getToCamera(Vector3f vOut)
    {
        vOut.set(vectorToCamera);
    }

    public void setToCamera(Vector3f toCam)
    {
        vectorToCamera.set(toCam);
    }

    public void setMaximumDistanceSquared(float maximumDistanceSquared)
    {
        this.maximumDistanceSquared = maximumDistanceSquared;
    }

    /**
     * Construct a new state object with the given target. The provided target
     * may be null but a focus point must be manually set if that is the case.
     * @param target
     */
    public ThirdPersonCamState(PPolygonModelInstance target)
    {
        setType(CameraStateType.ThirdPerson);
        modelInstance = target;
        if (target != null) // Grab the focal point
            setTargetFocalPoint(modelInstance.getTransform().getWorldMatrix(false).getTranslation());
        newTargetNeedsUpdate = true;
    }

    /**
     * Construct a new state object with the given target. If the target provided
     * is null, a focal point must be manually set.
     * @param target
     * @param rotationOnYAxis
     */
    public ThirdPersonCamState(PPolygonModelInstance target, float rotationOnYAxis)
    {
        setType(CameraStateType.ThirdPerson);
        modelInstance = target;
        if (modelInstance != null)
            setTargetFocalPoint(modelInstance.getTransform().getWorldMatrix(false).getTranslation());
        newTargetNeedsUpdate = true;
        rotationY = rotationOnYAxis;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////    Accessors & Mutators, aka The Dungeon ////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
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

    public void setNeedsRefresh()
    {
        newTargetNeedsUpdate = true;
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
        this.focalPoint.set(focalPoint);
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

    public void getOffsetFromCharacter(Vector3f vOut)
    {
        vOut.set(offsetFromCharacterTransform);
    }

    public void setOffsetFromCharacter(Vector3f offset)
    {
        offsetFromCharacterTransform.set(offset);
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
     * @return A reference to the current transform
     */
    public PMatrix getCameraTransform()
    {
        return camTransform;
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
        setCameraPosition(newPosition, true);
    }

    public void setCameraPosition(Vector3f newPosition, boolean needsUpdate)
    {
        camTransform.setTranslation(newPosition);
        newTargetNeedsUpdate = needsUpdate;
    }

    public boolean needsTargetUpdate()
    {
        return newTargetNeedsUpdate;
    }

    public void setTargetNeedsUpdate(boolean needed)
    {
        newTargetNeedsUpdate = needed;
    }

    public Vector3f getNextFocalPoint() {
        return nextFocalPoint;
    }

    public void setNextFocalPoint(Vector3f nextFocalPoint) {
        this.nextFocalPoint = nextFocalPoint;
    }

    public Vector3f getNextPosition() {
        return nextPosition;
    }

    public void setNextPosition(Vector3f nextPosition) {
        if (nextPosition != null)
            this.nextPosition = new Vector3f(nextPosition);
        else
            this.nextPosition = null;
    }

    public float getTimeInFocusTransition()
    {
        return timeInFocusTransition;
    }

    public void setTimeInFocusTransition(float timeInFocusTransition)
    {
        this.timeInFocusTransition = timeInFocusTransition;
    }

    public float getTimeInPositionTransition()
    {
        return timeInPositionTransition;
    }

    public void setTimeInPositionTransition(float timeInPositionTransition)
    {
        this.timeInPositionTransition = timeInPositionTransition;
    }

    public float getTransitionDuration()
    {
        return transitionDuration;
    }

    public void setTransitionDuration(float transitionDuration)
    {
        this.transitionDuration = transitionDuration;
    }

    public Vector3f getOriginalFocalPoint() {
        return originalFocalPoint;
    }

    public void setOriginalFocalPoint(Vector3f originalFocalPoint) {
        this.originalFocalPoint = originalFocalPoint;
    }

    public Vector3f getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(Vector3f originalPosition) {
        this.originalPosition = originalPosition;
    }

    public float getMinimumDistanceSquared() {
        return minimumDistanceSquared;
    }

    public void setMinimumDistanceSquared(float minimumDistanceSquared) {
        this.minimumDistanceSquared = minimumDistanceSquared;
    }
}
