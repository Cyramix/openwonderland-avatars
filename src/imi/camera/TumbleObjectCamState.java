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
package imi.camera;

import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 * This class encapsultes the required state for the TumbleObject camera model.
 * There is no complex logic in this class, just lots of accessors and mutators.
 * @author Ronald E Dahlgren
 */
public final class TumbleObjectCamState extends AbstractCameraState
{
    /**
     * Defines the states of movement that this state can be in
     */
    enum MovementStates {
        ZOOMING_IN,
        ZOOMING_OUT,
        STOPPED
    }

    /** Current movement state **/
    private MovementStates movementState = MovementStates.STOPPED;
    /** The camera's transformation matrix **/
    private final PMatrix camTransform = new PMatrix();

    /** If this is non-null, the camera is moving towards this position **/
    private Vector3f nextPosition = null;
    /** Cached for proper lerping **/
    private Vector3f originalPosition = null;

    /////////////////////////////////////////////////
    /////// User experience configuration below
    /////////////////////////////////////////////////

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

    /** Maintain a reference to a PPolygonModelInstance to follow. **/
    private PPolygonModelInstance   modelInstance   = null;
    /** This is the place that the camera looks at and zooms towards **/
    private final Vector3f          focalPoint      = new Vector3f();
    /** If this is non-null, the camera is transitioning to this focal point **/
    private Vector3f                nextFocalPoint  = null;
    /** Kept to lerp properly **/
    private Vector3f                originalFocalPoint  = null;
    /** Rotation about the Y axis **/
    private float                   rotationY       = 0.0f;
    

    /** Data for transitioning **/
    private float timeInPositionTransition = 0.0f;
    private float timeInFocusTransition = 0.0f;

    /** True anytime a new target is set, the camera must lookAt **/
    private boolean newTargetNeedsUpdate = false;
    /** This is the minimum distance squared that is allowed between the camera and its focal point **/
    private float minimumDistanceSquared = 1.5f;
    private float maximumDistanceSquared = 10.0f;

    /**
     * Construct a new state object with the given target. 
     * 
     * <p>The provided target may be null <b>but a focus point must be manually set
     * if that is the case.</b> Otherwise, the translation element of the
     * {@code PPolygonModelInstance}'s transform is used.</p>
     *
     * @param target The object to be tumbled and focused on.
     */
    public TumbleObjectCamState(PPolygonModelInstance target)
    {
        super(CameraStateType.TumbleObject);
        modelInstance = target;
        if (target != null) // Grab the focal point
            setTargetFocalPoint(modelInstance.getTransform().getWorldMatrix(false).getTranslation());
        newTargetNeedsUpdate = true;
    }

    /**
     * Construct a new state object with the given target with a specified initial
     * rotation.
     *
     * <p>If the target provided is null, a focal point must be manually set. </p>
     * @param target
     * @param rotationOnYAxis
     */
    public TumbleObjectCamState(PPolygonModelInstance target, float rotationOnYAxis)
    {
        super(CameraStateType.TumbleObject);
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

    //////////////////////////////
    //////// Public API
    //////////////////////////////


    /**
     * This method allows for smooth transitioning to a new focus point
     * @param newFocusPoint
     */
    public void turnTo(Vector3f newFocusPoint)
    {
        // out with the old
        setNextFocalPoint(null);
        setOriginalFocalPoint(null);
        setTimeInFocusTransition(0.0f);
        // in with the new
        setNextFocalPoint(new Vector3f(newFocusPoint)); // Defensive copy
        setOriginalFocalPoint(getTargetFocalPointRef());
    }

    /**
     * This method allows for smoothly transitioning to a new location, but it
     * will interrupt any pending transitions.
     * @param newPosition
     */
    public void moveTo(Vector3f newPosition)
    {
        // out with the old
        setNextPosition(null);
        setOriginalPosition(null);
        setTimeInPositionTransition(0.0f);
        // in with the new
        setNextPosition(new Vector3f(newPosition)); // defensive copy
        setOriginalPosition(getCameraPosition());
    }

    /**
     * Sets the point that the camera will try to look at.
     * @param focalPoint The non-null point that the camera should look at
     */
    public void setTargetFocalPoint(Vector3f focalPoint)
    {
        this.focalPoint.set(focalPoint);
        newTargetNeedsUpdate = true;
    }

    /**
     * Retrieve the point that the camera is currently trying to look at.
     * @param vOut The non-null vector to recieve the value
     */
    public void getTargetFocalPoint(Vector3f vOut)
    {
        vOut.set(focalPoint);
    }

    /**
     * Sets the {@code PPolygonModelInstance} that the camera will be tumbling.
     *
     * <p>Calling this method <b>WILL NOT</b> implicitely change the focal point
     * for the camera.
     * @param modelInstance The new target to manipulate
     */
    public void setTargetModelInstance(PPolygonModelInstance modelInstance)
    {
        this.modelInstance = modelInstance;
        newTargetNeedsUpdate = true;
    }

    /**
     * Retrieves the current rotation of the target model about the Y axis in
     * radians.
     * @return Rotation of model about the Y axis in radians.
     */
    public float getRotationY()
    {
        return rotationY;
    }

    /**
     * Sets the rotation of the target model about the Y axis in radians.
     *
     * <p>Calling this method will have no affect on the transform of the target
     * model until the next time an update occurs in the {@code CameraModel}
     * manipulating this state.</p>
     *
     * @param rotationYRadians The desired rotation of the target model
     */
    public void setRotationY(float rotationYRadians)
    {
        this.rotationY = rotationYRadians;
    }

    /**
     * Retrieve the value used for adjusting the mouse movement sensitivity on
     * the X axis (screen space).
     *
     * @return A float representing the mouse sensitivity on the X axis
     */
    public float getScaleRotationX()
    {
        return scaleRotationX;
    }

    /**
     * Set the value used for adjusting the mouse movement sensitivity on the
     * X axis (screen space).
     * @param scaleRotationX A float representing the mouse sensitivity on the X axis
     */
    public void setScaleRotationX(float scaleRotationX)
    {
        this.scaleRotationX = scaleRotationX;
    }

    /**
     * Retrieve the value used for adjusting the mouse movement sensitivity on
     * the Y axis (screen space).
     *
     * @return A float representing the mouse sensitivity on the Y axis
     */
    public float getScaleRotationY()
    {
        return scaleRotationY;
    }

    /**
     * Set the value used for adjusting the mouse movement sensitivity on the
     * Y axis (screen space).
     * @param scaleRotationY A float representing the mouse sensitivity on the Y axis
     */
    public void setScaleRotationY(float scaleRotationY)
    {
        this.scaleRotationY = scaleRotationY;
    }

    /**
     * Get the number of units that can be translated in a single frame.
     * @return The number of units that can be translated in a single frame
     */
    public float getMovementRate()
    {
        return movementRate;
    }

    /**
     * Sets the number of units that can be moved in a single frame.
     * @param movementRate Units moved per frame
     */
    public void setMovementRate(float movementRate)
    {
        this.movementRate = movementRate;
    }

    /**
     * Sets the translation of the camera.
     * 
     * <p>This method also triggers an update for the next time it is manipulated
     * by a {@code CameraModel}.
     * @param newPosition The new camera position
     */
    public void setCameraPosition(Vector3f newPosition)
    {
        setCameraPosition(newPosition, true);
    }
    
    /**
     * Retrieve the camera's current translation. 
     * @param vOut A non-null storage object.
     */
    public void getCameraPosition(Vector3f vOut)
    {
        camTransform.getTranslation(vOut);
    }
    
    /**
     * Retrieve a copy of the camera's current translation.
     * @return The camera's translation
     */
    public Vector3f getCameraPosition()
    {
        return camTransform.getTranslation();
    }

    /**
     * Get the smallest value acceptable for the square of the distance to the
     * target model.
     * @return minimum distance squared
     */
    public float getMinimumDistanceSquared() {
        return minimumDistanceSquared;
    }

    /**
     * Sets the square of the minimum distance allowed to separate the camera from
     * the target model it is tumbling.
     * @param minimumDistanceSquared Distance squared
     */
    public void setMinimumDistanceSquared(float minimumDistanceSquared) {
        this.minimumDistanceSquared = minimumDistanceSquared;
    }

    /**
     * Retrieve the square of the maximum distance that is allowed between the
     * camera and the target model.
     * @return max distance squared
     */
    public float getMaximumDistanceSquared() {
        return maximumDistanceSquared;
    }

    /**
     * Sets the square of the maximum distance allowed between the camera and
     * the target model.
     * @param maximumDistanceSquared The max distance squared
     */
    public void setMaximumDistanceSquared(float minimumDistanceSquared) {
        this.maximumDistanceSquared = minimumDistanceSquared;
    }

    //////////////////////////////
    ////////// Package API
    //////////////////////////////

    int getCurrentMouseX()
    {
        return currentMouseX;
    }

    void setCurrentMouseX(int currentMouseX)
    {
        this.currentMouseX = currentMouseX;
    }

    int getCurrentMouseY()
    {
        return currentMouseY;
    }

    void setCurrentMouseY(int currentMouseY)
    {
        this.currentMouseY = currentMouseY;
    }

    int getLastMouseX()
    {
        return lastMouseX;
    }

    void setLastMouseX(int lastMouseX)
    {
        this.lastMouseX = lastMouseX;
    }

    int getLastMouseY()
    {
        return lastMouseY;
    }

    void setLastMouseY(int lastMouseY)
    {
        this.lastMouseY = lastMouseY;
    }

    PPolygonModelInstance getTargetModelInstanceRef()
    {
        return modelInstance;
    }

    Vector3f getTargetFocalPointRef()
    {
        return focalPoint;
    }
    
    MovementStates getMovementState()
    {
        return movementState;
    }
    
    void setMovementState(MovementStates state)
    {
        movementState = state;
    }
    
    /**
     * {@inheritDoc AbstractCameraState}
     */
    PMatrix getCameraTransform()
    {
        return new PMatrix(camTransform);
    }

    /**
     * {@inheritDoc AbstractCameraState}
     */
    void setCameraTransform(PMatrix newTransform)
    {
        camTransform.set(newTransform);
        newTargetNeedsUpdate = true;
    }
    
    void setCameraPosition(Vector3f newPosition, boolean needsUpdate)
    {
        camTransform.setTranslation(newPosition);
        newTargetNeedsUpdate = needsUpdate;
    }
    
    boolean needsTargetUpdate()
    {
        return newTargetNeedsUpdate;
    }
    
    void setTargetNeedsUpdate(boolean needed)
    {
        newTargetNeedsUpdate = needed;
    }

    Vector3f getNextFocalPoint() {
        return nextFocalPoint;
    }

    void setNextFocalPoint(Vector3f nextFocalPoint) {
        this.nextFocalPoint = nextFocalPoint;
    }

    Vector3f getNextPosition() {
        return nextPosition;
    }

    void setNextPosition(Vector3f nextPosition) {
        this.nextPosition = nextPosition;
    }

    float getTimeInFocusTransition()
    {
        return timeInFocusTransition;
    }

    void setTimeInFocusTransition(float timeInFocusTransition)
    {
        this.timeInFocusTransition = timeInFocusTransition;
    }

    float getTimeInPositionTransition()
    {
        return timeInPositionTransition;
    }

    void setTimeInPositionTransition(float timeInPositionTransition)
    {
        this.timeInPositionTransition = timeInPositionTransition;
    }

    float getTransitionDuration()
    {
        return transitionDuration;
    }

    void setTransitionDuration(float transitionDuration)
    {
        this.transitionDuration = transitionDuration;
    }

    Vector3f getOriginalFocalPoint() {
        return originalFocalPoint;
    }

    void setOriginalFocalPoint(Vector3f originalFocalPoint) {
        this.originalFocalPoint = originalFocalPoint;
    }

    Vector3f getOriginalPosition() {
        return originalPosition;
    }

    void setOriginalPosition(Vector3f originalPosition) {
        this.originalPosition = originalPosition;
    }
}
