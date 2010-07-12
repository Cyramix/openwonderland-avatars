/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
import com.jme.math.Ray;
import com.jme.math.Vector3f;
import com.jme.scene.Spatial;
import imi.character.CharacterController;
import imi.collision.CollisionController;
import imi.collision.TransformUpdateManager;
import imi.scene.PMatrix;
import imi.scene.PTransform;
import imi.scene.polygonmodel.PPolygonModelInstance;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.jdesktop.mtgame.CollisionInfo;
import org.jdesktop.mtgame.PickDetails;
import org.jdesktop.mtgame.PickInfo;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 *  Concrete character controller.
 *  Controls the avatar according to internal and external actions.
 *  This is the only class that should move the avatar's model instance local
 *  transform - through a transform update manager if possible (there are
 *  threading issues that come up that may cause visible jitter).
 * 
 * @author Lou Hayt
 */
@ExperimentalAPI
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
    
    /** Collision Controller **/
    private CollisionController collisionController = null;

    private ArrayList<AvatarCollisionListener> collisionListeners = null;

    /**
     * If true, the TransformUpdateManager will be used to try and synch transform updates.
     * This is primarily needed when the collision system is operating on the avatar.
     **/
    private boolean bUseTransformUpdateManager = false;
    /** Used to enable / disabled ground clamping **/
    private boolean groundClampEnabled = true;

    /**
     * Construct a new avatar controller
     * @param theAvatar avatar to control
     */
    public AvatarController(Avatar theAvatar)
    {
        avatar = theAvatar;
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
    @Override
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
    @Override
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
    
    @Override
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
    @Override
    public void turnTo(Vector3f direction) 
    {
        desiredDirection = direction;
        bTurning         = true;
    }


    private static boolean nanReported = false;
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

        PMatrix previousRot = new PMatrix(currentRot);
        Vector3f previousPos = new Vector3f();
        body.getTransform().getLocalMatrix(false).getTranslation(previousPos);

        // Accelerate
        Vector3f currentDirection = body.getTransform().getWorldMatrix(false).getLocalZ();
        Vector3f tmpVelocity = new Vector3f(velocity);
        if (!bSlide)
        {
            velocity = currentDirection.normalize().mult(currentDirection.dot(velocity));
        } else {
            velocity.set(0,0,0);
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

        if (Float.isNaN(position.x)) {
            if (!nanReported) {
                Logger logger = Logger.getLogger(this.getClass().getName());
                logger.severe("POSTION IS NAN, added velocity "+(velocity.mult(deltaTime))+"   velVector "+velocity);
                logger.severe("tmpVelocity "+tmpVelocity);
                logger.severe("currentDir "+currentDirection+"  normalized "+currentDirection.normalize()+"  length "+currentDirection.length());
                logger.severe("Initial "+currentDirection.normalize().mult(currentDirection.dot(tmpVelocity)));
                logger.severe("addLocal "+currentDirection.mult(fwdAcceleration * (-deltaTime)));
                logger.severe("addLocal accel "+acceleration);
                logger.severe("addLocal grav "+gravityAcc);
                logger.severe("fwdAccel "+fwdAcceleration);

                if (!bSlide) {
                    logger.severe("STARTED WITH "+currentDirection.normalize().mult(fwdAcceleration*(-deltaTime)));
                    logger.severe("CURRENT DIR "+currentDirection);
                }
                nanReported = true;
            }
            position = body.getTransform().getLocalMatrix(false).getTranslation();
        }
        
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
            if (collisionController!=null && groundClampEnabled) {
                if (collisionController.isGravityEnabled())
                    checkGround(position);
                if (collisionCheck(position, currentRot)) {   // Check for collision and notify listeners
                    if (collisionController.isCollisionResponseEnabled()) {
                        position.set(previousPos);
                        currentRot.set(previousRot);
                    } else {
//                        System.err.println("CollisionResponse Disabled");
                    }
                }
            }

            if (fwdAcceleration < 1.0f && Math.max(Math.max(acceleration.x, acceleration.y), acceleration.z) < 1.0f)
                velocity.multLocal(velocityDamp);
        }

        TransformUpdateManager transformUpdateManager = (TransformUpdateManager) avatar.getWorldManager().getUserData(TransformUpdateManager.class);
        if(bUseTransformUpdateManager && transformUpdateManager != null)
        {
            transformUpdateManager.transformUpdate(avatar, body.getTransform().getLocalMatrix(true), position, currentRot);
        }
        else
        {
            currentRot.setTranslation(position);
            body.getTransform().getLocalMatrix(true).set(currentRot);
            notifyTransfromUpdate(position, currentRot);
        }
    }

    ////// Ground clamping code
    private Ray heightRay = new Ray();
    private Vector3f downVec = new Vector3f(0f,-1f,0f);
    private float fallInc = 0.5f;
    private void checkGround(Vector3f newPos) {
        float yDelta = 1.0f;
        float dy = 0;

        heightRay.origin.set(newPos);
        heightRay.origin.y+=yDelta;
        heightRay.direction = downVec;
        //System.out.println(newPos);
        PickInfo pi = collisionController.getCollisionSystem().pickAllWorldRay(heightRay, true, false);
        if (pi.size() != 0) {
            // Grab the first collidable
            PickDetails pd = null;

            for (int i = 0; i < pi.size(); i++) {
                PickDetails cur = pi.get(i);

                if (cur.getCollisionComponent().isCollidable()) {
                    pd = cur;
                    break;
                }
            }

            if (pd != null) {
                dy = pd.getDistance() - yDelta;

                newPos.y -= dy;
            }
        } else {
            //System.out.println("NO GROUND!!!");
        }
    }

    private boolean collisionCheck(Vector3f potentialPos, PMatrix potentialRot) {
        boolean collision = false;
        Spatial collisionGraph = collisionController.getCollisionGraph();
        collisionGraph.setLocalTranslation(potentialPos.x, potentialPos.y, potentialPos.z);
        collisionGraph.setLocalRotation(potentialRot.getRotationJME());
        collisionGraph.updateGeometricState(0, true);

        // For high quality avatars need to transform the boxes that enclose the body parts
        // int jointIndex = skeleton.getSkinnedMeshJointIndex(jointName)  gets the int index of a joint  (cache the jointIndex, recompute if avatar changes)
        // SkinnedMeshJoint specificJoint = skeleton.getSkinnedMeshJoint(jointIndex);
        // specificJoint.getTransform().getWorldMatrix(false)

        CollisionInfo collisionInfo = collisionController.getCollisionSystem().findAllCollisions(collisionGraph, true);
        if (collisionInfo.size() != 0) {
            collision = true;
//                System.err.println("OUCH ! "+tris.size());
//                TriMesh mesh = (TriMesh)tcr.getCollisionData(i).getSourceMesh();
            //computeCollisionResponse(position, potentialPos, rotatedFwdDirection, triData2, walkInc);
            notifyCollisionListeners(collisionInfo);
        }
        return collision;
    }

    public void addCollisionListener(AvatarCollisionListener listener) {
        if (collisionListeners==null)
            collisionListeners = new ArrayList();
        collisionListeners.add(listener);
    }

    public void removeCollisionListener(AvatarCollisionListener listener) {
        if (collisionListeners!=null) {
            collisionListeners.remove(listener);
        }
    }

    private void notifyCollisionListeners(CollisionInfo collision) {
        if (collisionListeners==null)
            return;

        for(AvatarCollisionListener listener : collisionListeners)
            listener.processCollision(collision);
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

    public void setCollisionController(CollisionController collisionController) {
        this.collisionController = collisionController;
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

    /**
     * Arm / Disarm the simple ground clamping
     * @param bUseIt
     */
    public void setSimpleGroundClamping(boolean enabled)
    {
        groundClampEnabled = enabled;
    }

    public boolean isUsingSimpleGroundClamping()
    {
        return groundClampEnabled;
    }
}
