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
package imi.scene.processors;

import org.jdesktop.mtgame.*;
import com.jme.math.Vector3f;
import com.jme.math.Matrix3f;
import com.jme.math.Quaternion;
import com.jme.scene.Node;
import com.jme.scene.shape.Sphere;
import com.jme.intersection.TriangleCollisionResults;

import imi.tests.SkyBox;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.processor.AWTEventProcessorComponent;


/**
 * A camera processor. Based on code originally by Doug Twilleager
 * @author Ronald E Dahlgren
 */
public class CameraProcessor extends AWTEventProcessorComponent {
    /**
     * The arming conditions for this processor
     */
    private ProcessorArmingCollection collection = null;
    
    /**
     * First, some common variables
     */
    private int currentX = -1;
    private int currentY = -1;
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    
    /**
     * The cumulative rotation in Y and X
     */
    private float rotY = 0.0f;
    private float rotX = 0.0f;
    
    /**
     * This scales each change in X and Y
     */
    private float scaleX = 0.7f;
    private float scaleY = 0.7f;
    private float walkInc = 0.5f;
    
    /**
     * States for movement
     */
    private static final int STOPPED = 0;
    private static final int WALKING_FORWARD = 1;
    private static final int WALKING_BACK = 2;
    private static final int STRAFE_LEFT = 3;
    private static final int STRAFE_RIGHT = 4;
    
    /**
     * Our current state
     */
    private int state = STOPPED;
    
    /**
     * Our current position
     */
    private Vector3f position = new Vector3f(0.0f, 10.0f, -30.0f);
    
    /**
     * A sphere for initial avatar collisions
     */
    private Sphere bounds = new Sphere("Avatar", 10, 10, 1.5f);
    private Vector3f boundsPosition = new Vector3f();
    //private BoundingCollisionResults results = new BoundingCollisionResults();
    private TriangleCollisionResults results = new TriangleCollisionResults();
    
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
     * The Node to modify
     */
    private Node target = null;
    
    /**
     * The skybox component
     */
    private SkyBox skybox = null;
    
    /**
     * The WorldManager
     */
    private WorldManager worldManager = null;
    
    
    
    /**
     * The default constructor
     */
    public CameraProcessor(AWTInputComponent listener, Node cameraNode,
            WorldManager wm, Entity myEntity, SkyBox skyboxNode) {
        super(listener);
        target = cameraNode;
        worldManager = wm;
        setEntity(myEntity);
        skybox = skyboxNode;
        collection = new ProcessorArmingCollection(this);
        collection.addCondition(new AwtEventCondition(this));
        collection.addCondition(new NewFrameCondition(this));
    }
    
    public Vector3f getPosition()
    {
        return position;
    }
    
    public void setPosition(Vector3f newPosition)
    {
        position.set(newPosition);
    }
    
    public void setRotation(float rotationX, float rotationY)
    {
        rotX = rotationX;
        rotY = rotationY;
        
        directionRotation.fromAngleAxis(rotY * (float) Math.PI / 180.0f, yDir);
        directionRotation.mult(fwdDirection, rotatedFwdDirection);
        directionRotation.mult(sideDirection, rotatedSideDirection);
        quaternion.fromAngles(rotX * (float) Math.PI / 180.0f, rotY * (float) Math.PI / 180.0f, 0.0f);
    }
    
    @Override
    public void initialize() {
        setArmingCondition(collection);
    }
    
    @Override
    public void compute(ProcessorArmingCollection collection) {
        Object[] events = getEvents();
        boolean updateRotations = false;

        for (int i = 0; i < events.length; i++)
        {
            if (events[i] instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) events[i];
                if (me.getID() == MouseEvent.MOUSE_PRESSED)
                {
                    currentX = me.getX();
                    currentY = me.getY();
                    lastMouseX = currentX;
                    lastMouseY = currentY;
                }
                if (me.getID() == MouseEvent.MOUSE_DRAGGED) {
                    processRotations(me);
                    updateRotations = true;
                }
            } else if (events[i] instanceof KeyEvent)
            {
                KeyEvent ke = (KeyEvent) events[i];
                processKeyEvent(ke);
            }
        }
        
        if (updateRotations) {
            directionRotation.fromAngleAxis(rotY*(float)Math.PI/180.0f, yDir);
            directionRotation.mult(fwdDirection, rotatedFwdDirection);
            directionRotation.mult(sideDirection, rotatedSideDirection);
            //System.out.println("Forward: " + rotatedFwdDirection);
            quaternion.fromAngles(rotX*(float)Math.PI/180.0f, rotY*(float)Math.PI/180.0f, 0.0f);
        }
        
        updatePosition();
    }
    
    private void processRotations(MouseEvent me) {
        int deltaX = 0;
        int deltaY = 0;
        int currentX = 0;
        int currentY = 0;
        currentX = me.getX();
        currentY = me.getY();

        if (lastMouseX == -1) {
            // First time through, just initialize
            lastMouseX = currentX;
            lastMouseY = currentY;
        } else {
            deltaX = currentX - lastMouseX;
            deltaY = currentY - lastMouseY;
            deltaX = -deltaX;

            rotY += (deltaX * scaleX);
            rotX += (deltaY * scaleY);
            if (rotX > 60.0f) {
                rotX = 60.0f;
            } else if (rotX < -60.0f) {
                rotX = -60.0f;
            }
            lastMouseX = currentX;
            lastMouseY = currentY;
        }
    }
    
    
    private void processKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
            if (ke.getKeyCode() == KeyEvent.VK_W) {
                state = WALKING_FORWARD;
            }
            if (ke.getKeyCode() == KeyEvent.VK_S) {
                state = WALKING_BACK;
            }
            if (ke.getKeyCode() == KeyEvent.VK_A) {
                state = STRAFE_LEFT;
            }
            if (ke.getKeyCode() == KeyEvent.VK_D) {
                state = STRAFE_RIGHT;
            }
        }
        if (ke.getID() == KeyEvent.KEY_RELEASED) {
            if (ke.getKeyCode() == KeyEvent.VK_W ||
                ke.getKeyCode() == KeyEvent.VK_S ||
                ke.getKeyCode() == KeyEvent.VK_A ||
                ke.getKeyCode() == KeyEvent.VK_D) {
                state = STOPPED;
            }
        }
    }
    
    private void updatePosition() {
        switch (state) {
            case WALKING_FORWARD:
                position.x += (walkInc * rotatedFwdDirection.x);
                position.y += (walkInc * rotatedFwdDirection.y);
                position.z += (walkInc * rotatedFwdDirection.z);
                break;
            case WALKING_BACK:
                position.x -= (walkInc * rotatedFwdDirection.x);
                position.y -= (walkInc * rotatedFwdDirection.y);
                position.z -= (walkInc * rotatedFwdDirection.z);
                break;
            case STRAFE_LEFT:
                position.x += (walkInc * rotatedSideDirection.x);
                position.y += (walkInc * rotatedSideDirection.y);
                position.z += (walkInc * rotatedSideDirection.z);
                break;
            case STRAFE_RIGHT:
                position.x -= (walkInc * rotatedSideDirection.x);
                position.y -= (walkInc * rotatedSideDirection.y);
                position.z -= (walkInc * rotatedSideDirection.z);
                break;  
        }
    }
    /**
     * The commit methods
     */
    public void commit(ProcessorArmingCollection collection) {
        target.setLocalRotation(quaternion);
        target.setLocalTranslation(position);
        
        if (skybox != null)
        {
            skybox.setLocalTranslation(position);
            worldManager.addToUpdateList(skybox);
        }
        worldManager.addToUpdateList(target);
    }

}
