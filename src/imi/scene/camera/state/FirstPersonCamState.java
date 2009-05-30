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
public class FirstPersonCamState extends CameraState
{
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
    // Movement type state enumeration
    public static final int STOPPED = 0;
    public static final int WALKING_FORWARD = 1;
    public static final int WALKING_BACK = 2;
    public static final int STRAFE_LEFT = 3;
    public static final int STRAFE_RIGHT = 4;
    public static final int MOVE_UP = 5;
    public static final int MOVE_DOWN = 6;


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
    private int movementState = STOPPED;
    // Current position
    private Vector3f positionVec = new Vector3f();
    
    /**
     * The Y Axis
     */
    private Vector3f yDir = new Vector3f(0.0f, 1.0f, 0.0f);
    
    /**
     * Our current forward direction
     */
    private Vector3f fwdDirection = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f rotatedFwdDirection = new Vector3f();
    
    /**
     * Our current side direction
     */
    private Vector3f sideDirection = new Vector3f(1.0f, 0.0f, 0.0f);
    private Vector3f rotatedSideDirection = new Vector3f();
    
    /**
     * The quaternion for our rotations
     */
    private Quaternion quaternion = new Quaternion();
    
    /**
     * This is used to keep the direction rotated
     */
    private Matrix3f directionRotation = new Matrix3f();

    /**
     * Default construction
     */
    public FirstPersonCamState()
    {
        this(Vector3f.ZERO, 0.0f, 0.0f);
    }
    
    /**
     * Construct the state at the specified spatial postion
     * @param position
     */
    public FirstPersonCamState(Vector3f position)
    {
        this(position, 0.0f, 0.0f);
    }
    
    /**
     * Construct the state with the specified position and rotation
     * @param position
     * @param rotationX
     * @param rotationY
     */
    public FirstPersonCamState(Vector3f position, float rotationX, float rotationY)
    {
        // Set our type
        setType(CameraStateType.FirstPerson);
        // set position
        setPosition(position);
        // set rotation
        setRotation(rotationX, rotationY);
    }
    public Vector3f getPosition()
    {
        return positionVec;
    }
    
    public void setCameraPosition(Vector3f position)
    {
        setPosition(position);
    }
    
    public void setPosition(Vector3f position)
    {
        positionVec.set(position);
    }
    
    public void setRotation(float _rotationX, float _rotationY)
    {
        rotationX = _rotationX;
        rotationY = _rotationY;
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

    public boolean isControlDown() {
        return controlDown;
    }

    public void setControlDown(boolean controlDown) {
        this.controlDown = controlDown;
    }

    public boolean isShiftDown() {
        return shiftDown;
    }

    public void setShiftDown(boolean shiftDown) {
        this.shiftDown = shiftDown;
    }


    public Matrix3f getDirectionRotation()
    {
        return directionRotation;
    }

    public void setDirectionRotation(Matrix3f directionRotation)
    {
        this.directionRotation = directionRotation;
    }

    public Vector3f getFwdDirection()
    {
        return fwdDirection;
    }

    public void setFwdDirection(Vector3f fwdDirection)
    {
        this.fwdDirection = fwdDirection;
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

    public void setMovementState(int movementState)
    {
        this.movementState = movementState;
    }

    public Quaternion getQuaternion()
    {
        return quaternion;
    }

    public void setQuaternion(Quaternion quaternion)
    {
        this.quaternion = quaternion;
    }

    public Vector3f getRotatedFwdDirection()
    {
        return rotatedFwdDirection;
    }

    public void setRotatedFwdDirection(Vector3f rotatedFwdDirection)
    {
        this.rotatedFwdDirection = rotatedFwdDirection;
    }

    public Vector3f getRotatedSideDirection()
    {
        return rotatedSideDirection;
    }

    public void setRotatedSideDirection(Vector3f rotatedSideDirection)
    {
        this.rotatedSideDirection = rotatedSideDirection;
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

    public Vector3f getSideDirection()
    {
        return sideDirection;
    }

    public void setSideDirection(Vector3f sideDirection)
    {
        this.sideDirection = sideDirection;
    }

    public Vector3f getYDir()
    {
        return yDir;
    }

    public void setYDir(Vector3f yDir)
    {
        this.yDir = yDir;
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
    
    ////////////////////////////////////////////////////////
    //////// Behavior hooks for FirstPersonCamModel ////////
    ////////////////////////////////////////////////////////
    public boolean shouldMoveForward(KeyEvent ke) {
        return (ke.getKeyCode() == forwardKeyCode);
    }
    
    public boolean shouldMoveBackward(KeyEvent ke) {
        return (ke.getKeyCode() == backwardKeyCode);
    }
    
    public boolean shouldMoveLeft(KeyEvent ke) {
        return (ke.getKeyCode() == leftKeyCode);
    }
    
    public boolean shouldMoveRight(KeyEvent ke) {
        return (ke.getKeyCode() == rightKeyCode);
    }
    
    public boolean shouldAscend(KeyEvent ke) {
        return (ke.getKeyCode() == ascendKeyCode);
    }
    
    public boolean shouldDescend(KeyEvent ke) {
        return (ke.getKeyCode() == descendKeyCode);
    }

    public boolean isMovementKey(KeyEvent ke) {
        return (ke.getKeyCode() == forwardKeyCode ||
                ke.getKeyCode() == backwardKeyCode ||
                ke.getKeyCode() == leftKeyCode ||
                ke.getKeyCode() == rightKeyCode ||
                ke.getKeyCode() == ascendKeyCode ||
                ke.getKeyCode() == descendKeyCode);
    }

    @Override
    public void setCameraTransform(PMatrix transform)
    {
        setCameraPosition(transform.getTranslation());
        Quaternion rot = transform.getRotationJME();
        setQuaternion(rot);
        float[] angles = rot.toAngles(null);
        System.out.println("Rotation angles are " + angles[0] + ", " + angles[1] + ", " + angles[2]);
        setRotationX((float)Math.toDegrees(angles[0]));
        setRotationY((float)Math.toDegrees(angles[1]));

    }

    @Override
    public PMatrix getCameraTransform() {
        PMatrix trans = new PMatrix(positionVec);
        trans.setRotation(quaternion);
        return trans;
    }
    
    
    
}
