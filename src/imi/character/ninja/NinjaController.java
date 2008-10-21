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
package imi.character.ninja;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.CharacterController;
import imi.scene.PMatrix;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonModelInstance;
import javax.swing.JFrame;

/**
 *  Controls the ninja according to internal and external actions
 * 
 * @author Lou Hayt
 */
public class NinjaController extends CharacterController
{
    private Ninja    ninja               = null;
    private boolean  initalized          = false;
    private PPolygonModelInstance body   = null;
    
    private float    rotationSensetivity = -7.5f;
    private PMatrix  rotation            = new PMatrix();
    private boolean  bTurning            = false;
    private Vector3f desiredDirection    = new Vector3f();
    private Vector3f velocity            = new Vector3f();
    private float    acceleration        = 0.0f; // acceleration towards the current direction
    private float    maxAcceleration     = 10.0f;
    private float    maxVelocity         = 5.0f;
    private float    mass                = 1.0f;
    
    private float    velocityDamp        = 0.8f; // 1.0f - velocityDamp     = intuitive value
    private float    accelerationDamp    = 0.5f; // 1.0f - accelerationDamp = intuitive value
    private float    dampCounter         = 0.0f;
    private float    dampTick            = 1.0f / 60.0f;

    private boolean  bSlide              = false; // if false velocity and heading will be alligned

    private PMatrix currentRot = new PMatrix();
    
    private JFrame window = null; // use this to set title name for debugging info
    
    public NinjaController(Ninja master)
    {
        ninja = master;
        
        // Set the window to have access for the window title
        // used for displaying debugging info
        // TODO : This is no longer exposed (or even stored explicitely) by the wm
        //setWindow(ninja.getWorldManager().getSwingFrame());
    }
    
    private void initialize() 
    {
        // Ninja's mesh might be null untill loaded
        PPolygonModelInstance model = ninja.getModelInst();
        if (model != null)
        {
            body       = model;
            initalized = true;
        }
    }

    @Override
    public void stop() 
    {
        acceleration = 0.0f;
        velocity.set(Vector3f.ZERO);
    }
    
    public void accelerate(float force) 
    {
        acceleration += force / mass;
        if (acceleration > maxAcceleration)
            acceleration = maxAcceleration;
    }

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

    public void turnTo(Vector3f direction) 
    {
        desiredDirection = direction;
        bTurning         = true;
    }

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
            rotation.buildRotationY(rotationSensetivity * desiredDirection.x * deltaTime);
            
            currentRot = body.getTransform().getLocalMatrix(true);
            currentRot.mul(rotation);
            
            bTurning = false;
        }
        else
            currentRot = body.getTransform().getLocalMatrix(true);
        
        // Accelerate
        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
        if (!bSlide)
        {
            velocity = currentDirection.normalize().mult(currentDirection.dot(velocity));
        }
        velocity.addLocal(currentDirection.mult(acceleration * (-deltaTime)));
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
        body.getTransform().getLocalMatrix(true).setTranslation(position);
        
        // Dampen
        dampCounter += deltaTime;
        if (dampCounter > dampTick)
        {
            dampCounter = 0.0f;
            
            acceleration *= accelerationDamp;
            if (acceleration < 0.5f)
                acceleration = 0.0f;

            if (acceleration < 1.0f)
                velocity.multLocal(velocityDamp);
        }

        notifyTransfromUpdate(position, currentRot);
        
//        if (getVelocityScalar() > 1.0f)
//            window.setTitle("yes");
//        else
//            window.setTitle("no");
       
        //window.setTitle("acc: " + (int)acceleration + " vel: " + (int)velocity.x + " " + (int)velocity.y + " " + (int)velocity.z);
    }
    
    public boolean isMovingForward() {
        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
        float dot = currentDirection.dot(velocity);
        if (dot >= 0.0f)
            return true;
        else
            return false;
    }
    
    @Override
    public JFrame getWindow() {
        return window;
    }

    public void setWindow(JFrame window) {
        this.window = window;
    }

    public float getAcceleration() {
        return acceleration;
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

    public void setMaxAcceleration(float maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }

    public float getRotationSensetivity() {
        return rotationSensetivity;
    }

    public void setRotationSensetivity(float rotationSensetivity) {
        this.rotationSensetivity = rotationSensetivity;
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
        
}
