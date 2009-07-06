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

import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.scene.PMatrix;
import java.awt.event.KeyEvent;

/**
 * This class encapsulates all the state needed for first person camera
 * behavior.
 * @author Ronald E Dahlgren
 */
public final class FirstPersonCamState extends AbstractCameraState
{
    /**
     * This enum represents the different states of movement that the first
     * person camera behavior can be in.
     */
    enum MovementStates {
        STOPPED,
        WALKING_FORWARD,
        WALKING_BACK,
        STRAFE_LEFT,
        STRAFE_RIGHT,
        MOVE_UP,
        MOVE_DOWN,
    }

    // User experience configuration
    private float movementRate = 0.075f; 
    private float scaleRotationX = 0.56f;
    private float scaleRotationY = 0.56f;
    // Cached cursor info
    private int currentMouseX   = -1;
    private int currentMouseY   = -1;
    private int lastMouseX      = -1;
    private int lastMouseY      = -1;
    // Rotation information
    private float rotationX = 0.0f;
    private float rotationY = 0.0f;

    /** Keyboard state indicators **/
    private boolean shiftDown = false;
    private boolean controlDown = false;

    /** Virtual Key codes for movement **/
    private int forwardKeyCode = KeyEvent.VK_UP;
    private int leftKeyCode = KeyEvent.VK_LEFT;
    private int rightKeyCode = KeyEvent.VK_RIGHT;
    private int backwardKeyCode = KeyEvent.VK_DOWN;
    private int ascendKeyCode = KeyEvent.VK_OPEN_BRACKET;
    private int descendKeyCode = KeyEvent.VK_CLOSE_BRACKET;

    // The current movementState
    private MovementStates movementState = MovementStates.STOPPED;
    // Current position
    private final Vector3f positionVec = new Vector3f();
    
    /**
     * The Y Axis
     */
    private final Vector3f yDir = new Vector3f(0.0f, 1.0f, 0.0f);
    
    /**
     * Our current forward direction
     */
    private final Vector3f fwdDirection = new Vector3f(0.0f, 0.0f, 1.0f);
    private final Vector3f rotatedFwdDirection = new Vector3f();
    
    /**
     * Our current side direction
     */
    private final Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
    private final Vector3f rotatedSideDirection = new Vector3f();
    
    /**
     * The quaternion for our rotations
     */
    private final Quaternion quaternion = new Quaternion();
    
    /**
     * This is used to keep the direction rotated
     */
    private final Matrix3f directionRotation = new Matrix3f();

    /**
     * Default construction provides a position of (0,0,0) and 0 rotation
     * on the X and Y axes.
     */
    public FirstPersonCamState()
    {
        this(Vector3f.ZERO, 0.0f, 0.0f);
    }
    
    /**
     * Construct the state at the specified spatial position with 0 rotation
     * on the X and Y axes.
     * @param position a non-null position vector
     * @throws IllegalArgumentException If {@code position} == null
     */
    public FirstPersonCamState(Vector3f position)
    {
        this(position, 0.0f, 0.0f);
    }
    
    /**
     * Construct the state with the specified position and rotation values.
     * 
     * @param position A non-null translation vector
     * @param rotationX Rotation about the X axis in degrees
     * @param rotationY Rotation about the Y axis in degrees
     * @throws IllegalArgumentException If position == null
     */
    public FirstPersonCamState(Vector3f position, float rotationX, float rotationY)
    {
        // Set our type
        super(CameraStateType.FirstPerson);
        if (position == null)
            throw new IllegalArgumentException("position == null");
        // set position
        setCameraPosition(position);
        // set rotation
        setRotation(rotationX, rotationY);
    }
    
    ///////////////////////////////////
    /////// Public API
    ///////////////////////////////////
    
    /**
     * Sets the camera position to the specified position.
     * @param position a non-null position vector
     * @throws IllegalArgumentException If position == null
     */
    public void setCameraPosition(Vector3f position)
    {
        if (position == null)
            throw new IllegalArgumentException("position == null");
        positionVec.set(position);
    }
    
    /**
     * Sets the rotation to the specified values.
     * @param rotationX Rotation about the X axis in degrees
     * @param rotationY Rotation about the Y axis in degrees
     */
    public void setRotation(float rotationX, float rotationY)
    {
        this.rotationX = rotationX;
        this.rotationY = rotationY;
    }

    public float getMovementRate()
    {
        return movementRate;
    }

    public void setMovementRate(float movementRate)
    {
        this.movementRate = movementRate;
    }

    public float getRotationX()
    {
        return rotationX;
    }

    public void setRotationX(float rotationX)
    {
        this.rotationX = rotationX;
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

    public int getAscendKeyCode() {
        return ascendKeyCode;
    }

    public void setAscendKeyCode(int ascendKeyCode) {
        this.ascendKeyCode = ascendKeyCode;
    }

    public int getBackwardKeyCode() {
        return backwardKeyCode;
    }

    public void setBackwardKeyCode(int backwardKeyCode) {
        this.backwardKeyCode = backwardKeyCode;
    }

    public int getDescendKeyCode() {
        return descendKeyCode;
    }

    public void setDescendKeyCode(int descendKeyCode) {
        this.descendKeyCode = descendKeyCode;
    }

    public int getForwardKeyCode() {
        return forwardKeyCode;
    }

    public void setForwardKeyCode(int forwardKeyCode) {
        this.forwardKeyCode = forwardKeyCode;
    }

    public int getLeftKeyCode() {
        return leftKeyCode;
    }

    public void setLeftKeyCode(int leftKeyCode) {
        this.leftKeyCode = leftKeyCode;
    }

    public int getRightKeyCode() {
        return rightKeyCode;
    }

    public void setRightKeyCode(int rightKeyCode) {
        this.rightKeyCode = rightKeyCode;
    }
    
    ///////////////////////////////////
    ///// Package API
    ///////////////////////////////////

    Vector3f getPositionRef()
    {
        return positionVec;
    }

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

    boolean isControlDown() {
        return controlDown;
    }

    void setControlDown(boolean controlDown) {
        this.controlDown = controlDown;
    }

    boolean isShiftDown() {
        return shiftDown;
    }

    void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }

    Matrix3f getDirectionRotation()
    {
        return directionRotation;
    }

    void setDirectionRotation(Matrix3f directionRotation)
    {
        // HACK : jME needs to implement a Matrix3f.set(Matrix3f)
        // Logic tested and approved
        for (int i = 0; i < 9; ++i)
            directionRotation.set(i % 3, i / 3, directionRotation.get(i % 3, i / 3));
    }

    Vector3f getFwdDirectionRef()
    {
        return fwdDirection;
    }

    void setFwdDirection(Vector3f fwdDirection)
    {
        this.fwdDirection.set(fwdDirection);
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

    MovementStates getMovementState()
    {
        return movementState;
    }

    void setMovementState(MovementStates movementState)
    {
        this.movementState = movementState;
    }

    Quaternion getQuaternion()
    {
        return quaternion;
    }

    void setQuaternion(Quaternion quaternion)
    {
        this.quaternion.set(quaternion);
    }

    Vector3f getRotatedFwdDirection()
    {
        return rotatedFwdDirection;
    }

    void setRotatedFwdDirection(Vector3f rotatedFwdDirection)
    {
        this.rotatedFwdDirection.set(rotatedFwdDirection);
    }

    Vector3f getRotatedSideDirection()
    {
        return rotatedSideDirection;
    }

    void setRotatedSideDirection(Vector3f rotatedSideDirection)
    {
        this.rotatedSideDirection.set(rotatedSideDirection);
    }

    Vector3f getSideDirection()
    {
        return sideDirection;
    }

    void setSideDirection(Vector3f sideDirection)
    {
        this.sideDirection.set(sideDirection);
    }

    Vector3f getYDir()
    {
        return yDir;
    }

    void setYDir(Vector3f yDir)
    {
        this.yDir.set(yDir);
    }

    ////////////////////////////////////////////////////////
    //////// Behavior hooks for FirstPersonCamModel ////////
    ////////////////////////////////////////////////////////
    boolean shouldMoveForward(KeyEvent ke) {
        return (ke.getKeyCode() == forwardKeyCode);
    }
    
    boolean shouldMoveBackward(KeyEvent ke) {
        return (ke.getKeyCode() == backwardKeyCode);
    }
    
    boolean shouldMoveLeft(KeyEvent ke) {
        return (ke.getKeyCode() == leftKeyCode);
    }
    
    boolean shouldMoveRight(KeyEvent ke) {
        return (ke.getKeyCode() == rightKeyCode);
    }
    
    boolean shouldAscend(KeyEvent ke) {
        return (ke.getKeyCode() == ascendKeyCode);
    }
    
    boolean shouldDescend(KeyEvent ke) {
        return (ke.getKeyCode() == descendKeyCode);
    }

    boolean isMovementKey(KeyEvent ke) {
        return (ke.getKeyCode() == forwardKeyCode ||
                ke.getKeyCode() == backwardKeyCode ||
                ke.getKeyCode() == leftKeyCode ||
                ke.getKeyCode() == rightKeyCode ||
                ke.getKeyCode() == ascendKeyCode ||
                ke.getKeyCode() == descendKeyCode);
    }

  //  @Override
    public void setCameraTransform(PMatrix transform)
    {
        setCameraPosition(transform.getTranslation());
        Quaternion rot = transform.getRotationJME();
        setQuaternion(rot);
        float[] angles = rot.toAngles(null);
        setRotationX((float)Math.toDegrees(angles[0]));
        setRotationY((float)Math.toDegrees(angles[1]));

    }

   // @Override
    public PMatrix getCameraTransform() {
        PMatrix trans = new PMatrix(positionVec);
        trans.setRotation(quaternion);
        return trans;
    }
    
    
    
}
