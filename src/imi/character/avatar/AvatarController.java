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
package imi.character.avatar;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.CharacterController;
import imi.collision.TransformUpdateManager;
import imi.scene.PMatrix;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *  Concrete character controller.
 *  Controls the avatar according to internal and external actions
 * 
 * @author Lou Hayt
 */
public class AvatarController extends CharacterController
{
    /** The avatar being controller **/
    private Avatar    avatar               = null;
    /** True if initialization has completed **/
    protected boolean  initalized          = false;
    /** That which is controlled **/
    protected PPolygonModelInstance body   = null;
    /** Used to scale rotation **/
    private float    rotationSensitivity = -10.0f; // Why negative?
    /** matrix used for applying rotations **/
    private PMatrix  rotation            = new PMatrix();
    /** true if the avatar is currently turning **/
    private boolean  bTurning            = false;
    /** Direction the avatar would like to face **/
    private Vector3f desiredDirection    = new Vector3f();
    /** Movement vector **/
    private Vector3f velocity            = new Vector3f();
    /** Derivative of the above with respect to time **/
    private Vector3f acceleration        = new Vector3f();
    private float    fwdAcceleration     = 0.0f; // fwdAcceleration towards the current direction
    private float    maxAcceleration     = 10.0f;
    private float    maxVelocity         = 4.0f;
    private float    mass                = 1.0f;
    
    private float    velocityDamp        = 0.8f; // 1.0f - velocityDamp     = intuitive value
    private float    accelerationDamp    = 0.5f; // 1.0f - accelerationDamp = intuitive value
    private float    dampCounter         = 0.0f;
    private float    dampTick            = 1.0f / 60.0f;
    
    /** gravity **/
    private Vector3f gravity             = new Vector3f();//new Vector3f(0.0f, 0.098f, 0.0f);
    private Vector3f gravityAcc          = new Vector3f();

    private boolean  bSlide              = false; // if false velocity and heading will be alligned

    private PMatrix currentRot = new PMatrix();
    
    private JFrame window = null; // use this to set title name for debugging info

    /**
     * If true, the TransformUpdateManager will be used to try and synch transform updates.
     * This is primarily needed when the collision system is operating on the avatar.
     **/
    private boolean bUseTransformUpdateManager = true;

    /**
     * Construct a new avatar controller
     * @param theAvatar avatar to control
     */
    public AvatarController(Avatar theAvatar)
    {
        avatar = theAvatar;
        
        // Set the window to have access for the window title
        // used for displaying debugging info
        setWindow((JFrame) avatar.getWorldManager().getUserData(JFrame.class));
    }

    /**
     * try to grab the body
     */
    protected void initialize()
    {
        // avatar's mesh might be null until loaded
        PPolygonModelInstance model = avatar.getModelInst();
        if (model != null)
        {
            body       = model;
            initalized = true;
        }

        // Diagnostic / Debug output
        Logger.getLogger(AvatarController.class.getName()).log(Level.INFO, "GOT BODY " + body);
    }

    /**
     * Stop the avatar
     */
    @Override
    public void stop() 
    {
        fwdAcceleration = 0.0f;
        acceleration.set(Vector3f.ZERO);
        velocity.set(Vector3f.ZERO);
    }

    /**
     * Accelerate the avatar with the provided force
     * @param force
     */
    public void accelerate(float force) 
    {
        fwdAcceleration += force / mass;
        if (fwdAcceleration > maxAcceleration)
            fwdAcceleration = maxAcceleration;
    }

    /**
     * Accelerate the avatar with the provided force vector
     * @param force
     */
    public void accelerate(Vector3f force) 
    {
        acceleration.addLocal(force.divide(mass));
//        if (fwdAcceleration > maxAcceleration)
//            fwdAcceleration = maxAcceleration;
    }

    /**
     * Set the velocity to the provided scalar velocity
     * @param vel
     */
    public void setVelocity(float vel)
    {
        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
        velocity.set(currentDirection.mult(vel));
    }
    
    public float getVelocityScalar() 
    {
        float max = Math.max(Math.max(velocity.x, velocity.y), velocity.z);
        float min = Math.min(Math.min(velocity.x, velocity.y), velocity.z);
        return Math.max(max, Math.abs(min));
//        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
//        return currentDirection.dot(velocity);
    }

    /**
     * Turn to face the provided direction
     * @param direction
     */
    public void turnTo(Vector3f direction) 
    {
        desiredDirection = direction;
        bTurning         = true;
    }

    /**
     * Update the controller
     * @param deltaTime The timestep
     */
    public void update(float deltaTime) 
    {
        if (!initalized)
        {
            initialize();
            return;
        }
        
        // Turn
        if (bTurning)
        {
            rotation.buildRotationY(rotationSensitivity * desiredDirection.x * deltaTime);
            
            currentRot.set(body.getTransform().getLocalMatrix(true));
            currentRot.fastMul(rotation);
            
            bTurning = false;
        }
        else
            currentRot.set(body.getTransform().getLocalMatrix(true));
        
        // Accelerate
        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
        if (!bSlide)
        {
            velocity = currentDirection.normalize().mult(currentDirection.dot(velocity));
        }
        velocity.addLocal(currentDirection.mult(fwdAcceleration * (-deltaTime)));
        velocity.addLocal(acceleration);
        gravityAcc.addLocal(gravity);
        velocity.addLocal(gravityAcc);
        if (velocity.x > maxVelocity)
            velocity.x = maxVelocity;
        if (velocity.y > maxVelocity)
            velocity.y = maxVelocity;
        if (velocity.z > maxVelocity)
            velocity.z = maxVelocity;
        if (velocity.x < -maxVelocity)
            velocity.x = -maxVelocity;
        if (velocity.y < -maxVelocity)
            velocity.y = -maxVelocity;
        if (velocity.z < -maxVelocity)
            velocity.z = -maxVelocity;
         
        // Apply Velocity
        Vector3f position = body.getTransform().getLocalMatrix(false).getTranslation();
        if (bReverseHeading)
            position.addLocal(velocity.mult(-deltaTime));
        else
            position.addLocal(velocity.mult(deltaTime));
        
        // Dampen
        dampCounter += deltaTime;
        if (dampCounter > dampTick)
        {
            dampCounter = 0.0f;
            
            fwdAcceleration *= accelerationDamp;
            if (fwdAcceleration < 0.5f)
                fwdAcceleration = 0.0f;
            
            acceleration.multLocal(accelerationDamp);
            // TODO clamp down?

            if (fwdAcceleration < 1.0f && Math.max(Math.max(acceleration.x, acceleration.y), acceleration.z) < 1.0f)
                velocity.multLocal(velocityDamp);
        }

        TransformUpdateManager transformUpdateManager = (TransformUpdateManager) avatar.getWorldManager().getUserData(TransformUpdateManager.class);
        if(bUseTransformUpdateManager && transformUpdateManager != null)
        {
            transformUpdateManager.transformUpdate(this, body.getTransform().getLocalMatrix(true), position, currentRot);
        }
        else
        {
            currentRot.setTranslation(position);
            body.getTransform().getLocalMatrix(true).set(currentRot);
            notifyTransfromUpdate(position, currentRot);
        }
    }

    @Override
    public void notifyTransfromUpdate(Vector3f translation, PMatrix orientation)
    {
        avatar.getJScene().setExternalKidsRootPosition(translation, orientation.getRotationJME());
        super.notifyTransfromUpdate(translation, orientation);
    }

    @Override
    public void colliding(Vector3f projection) {
        gravityAcc.zero();
    }

    public Vector3f getGravity() {
        return gravity;
    }

    public void setGravity(Vector3f gravity) {
        this.gravity.set(gravity);
    }

    public Vector3f getGravityAcc() {
        return gravityAcc;
    }

    public void setGravityAcc(Vector3f gravityAcc) {
        this.gravityAcc.set(gravityAcc);
    }
    
    /**
     * true if the avatar being controlled is currently moving forward.
     * @return
     */
    @Override
    public boolean isMovingForward() {
        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
        float dot = currentDirection.dot(velocity);
        if (dot >= 0.0f) // Z is flipped!
            return false;
        else
            return true;
    }
    
    @Override
    public JFrame getWindow() {
        return window;
    }

    public void setWindow(JFrame window) {
        this.window = window;
    }

    public float getForwardAcceleration() {
        return fwdAcceleration;
    }

    public float getAccelerationDamp() {
        return 1.0f - accelerationDamp;
    }

    public void setAccelerationDamp(float accelerationDamp) {
        this.accelerationDamp = 1.0f - accelerationDamp;
    }

    public boolean isSlide() {
        return bSlide;
    }

    public void setSlide(boolean bSlide) {
        this.bSlide = bSlide;
    }

    public float getMaxAcceleration() {
        return maxAcceleration;
    }

    @Override
    public void setMaxAcceleration(float maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    @Override
    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public float getRotationSensetivity() {
        return rotationSensitivity;
    }

    public void setRotationSensetivity(float rotationSensetivity) {
        this.rotationSensitivity = rotationSensetivity;
    }

    public float getVelocityDamp() {
        return 1.0f - velocityDamp;
    }

    public void setVelocityDamp(float velocityDamp) {
        this.velocityDamp = 1.0f - velocityDamp;
    }
    
    public Vector3f getVelocity()
    {
        return velocity;
    }
    
    @Override
    public Vector3f getRightVector() {
        if (body != null)
        {
            if (bReverseHeading)
                return body.getTransform().getLocalMatrix(false).getLocalX().mult(-1.0f);
            else    
                return body.getTransform().getLocalMatrix(false).getLocalX();
        }
        return null;
    }
    
    @Override
    public Vector3f getForwardVector()
    {
        if (body != null)
        {
            if (bReverseHeading)
                return body.getTransform().getLocalMatrix(false).getLocalZ().mult(-1.0f);
            else    
                return body.getTransform().getLocalMatrix(false).getLocalZ();
        }
        return null; 
    }
    
    @Override
    public Vector3f getPosition()
    {
        // Debugging / Diagnostic information
//        Logger.getLogger(avatarController.class.getName()).log(Level.INFO,
//                "Bodyt "+body.getTransform().getWorldMatrix(false));
        
        if (body != null)
            return body.getTransform().getWorldMatrix(false).getTranslation();
        return null;
    }
     
    @Override
    public Quaternion getQuaternion() 
    {
        if (body != null)
        {
            if (bReverseHeading)
            {
                Vector3f position = body.getTransform().getWorldMatrix(false).getTranslation();
                PMatrix origin = new PMatrix();
                origin.lookAt(position, position.add(getForwardVector()), Vector3f.UNIT_Y);
                origin.invert();
                return origin.getRotationJME();
            }
            else    
                return body.getTransform().getWorldMatrix(false).getRotationJME();
        }
        return null; 
    }
    
    public PTransform getTransform()
    {
        if (body != null)
            return body.getTransform();
        return null;
    }

    @Override
    public PPolygonModelInstance getModelInstance() {
        return body;
    }

    @Override
    public void setModelInstance(PPolygonModelInstance newModelInstance) {
        body = newModelInstance;
    }

    /**
     * Arm / Disarm the transform update manager.
     * @param bUseIt
     */
    public void setUseTransformUpdateManager(boolean bUseIt)
    {
        bUseTransformUpdateManager = bUseIt;
    }

    public boolean isUsingTransformUpdateManager()
    {
        return bUseTransformUpdateManager;
    }
}
