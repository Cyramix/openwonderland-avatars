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
package imi.character;

import com.jme.math.Vector3f;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.SkeletonNode;
import imi.scene.SkinnedMeshJoint;
import imi.scene.polygonmodel.PPolygonSkinnedMeshInstance;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class performs character-eyeball related behavior and handles general eyeball
 * management.
 * @author Lou Hayt
 */
public class CharacterEyes 
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(CharacterEyes.class.getName());

    /** Skeleton of the above **/
    private final SkeletonNode          skeleton;
    /** Model instance of the above above **/
    private final PPolygonModelInstance characterModelInst;
    /** Reference to the head joint **/
    private SkinnedMeshJoint            headJoint;

    /** The eyes! **/
    protected EyeBall leftEyeBall   = null;
    protected EyeBall rightEyeBall  = null;
    /** True if eyes should randomly wander (looks more lifelike) **/
    private boolean     eyesWander = true;
    /** Used as a buffer for wandering calculations **/
    private final Vector3f eyesWanderOffset = new Vector3f();
    /** Used to track how long it has been since the last wandering target **/
    private float   eyesWanderCounter     = 0.0f;
    /** Count the number of wander targets for blinking algo **/
    private int     eyesWanderIntCounter  = 0;

    /** These are Breadth-First indices to the left and right eyelid joints **/
    private int leftEyeLid     = 37;//24;
    private int rightEyeLid    = 45;//34;
    /** Variable for caching the male or female versions **/
    private float   closedEyeLid     = 0.0f;
    private float   openEyeLid     = 0.0f;
    /** Rotational angles for closing the eyes **/
    // RED: hack added to fix WIDE OPEN EYE thing
    private final float   openEyeLidMale = ((float)Math.PI) * -0.07f;
    private final float   openEyeLidFemale = ((float)Math.PI) * -0.07f;
    private final float   closedEyeLidMale    = ((float)Math.PI) * -0.5f;//-0.011f;
    private final float   closedEyeLidFemale  = ((float)Math.PI) * -0.5f;//-0.041f;
    /** True to enable blinking **/
    private boolean blinkingOn      = true;
    /** How long has blinking been occuring? **/
    private float   blinkingTimer   = 0.0f;
    /** true during the act of blinking **/
    private boolean blinking        = false;
    /** True if the eyes are closed **/
    private boolean eyesClosed      = false;
    /** True to keep them closed **/
    private boolean keepEyesClosed  = false;
    /** How long will it take to close the eyes **/
    private float blinkingCloseTime = 0.2f;
    /** How long will the eyes remain closed **/
    private float blinkingShutTime  = 0.05f;
    /** How long will it take to open the eyes **/
    private float blinkingOpenTime  = 0.4f;
    /** Individual eye closing controls **/
    private boolean winkRight = true;
    private boolean winkLeft  = true;

    /** Rotational vectors **/
    private final Vector3f rotationBuffer = new Vector3f();

    /**
     * Construct a new set of peepers with the specified characteristics.
     * @param eyeballTexture A non-null relative path of the eyeball texture to use
     * @param character A non-null owner of the eyeballs
     * @param wm The (non-null) world manager!
     */
    public CharacterEyes(String eyeballTexture, Character character, WorldManager wm)
    {
        if (eyeballTexture == null || character == null || wm == null)
            throw new IllegalArgumentException("Null param, eyeballTexture: " +
                    eyeballTexture + ", character: " + character + ", wm:" + wm);
            
        
        if (character.getCharacterParams().isMale())
        {
            closedEyeLid = closedEyeLidMale;
            openEyeLid = openEyeLidMale;
        }
        else
        {
            closedEyeLid = closedEyeLidFemale;
            openEyeLid = openEyeLidFemale;
        }

        skeleton = character.getSkeleton();
        characterModelInst = character.getModelInst();
        
        rightEyeLid = skeleton.getSkinnedMeshJointIndex("rightEyeLid");
        leftEyeLid  = skeleton.getSkinnedMeshJointIndex("leftEyeLid");
        
        PPolygonSkinnedMeshInstance leftEyeMeshInst  = (PPolygonSkinnedMeshInstance) skeleton.findChild("leftEyeGeoShape");
        PPolygonSkinnedMeshInstance rightEyeMeshInst = (PPolygonSkinnedMeshInstance) skeleton.findChild("rightEyeGeoShape");

        if (leftEyeMeshInst == null || rightEyeMeshInst == null)
            throw new RuntimeException("Eyeball meshes not found!");

        leftEyeBall = new EyeBall(leftEyeMeshInst, character);
        leftEyeMeshInst.getParent().replaceChild(leftEyeMeshInst, leftEyeBall, true);
        rightEyeBall = new EyeBall(rightEyeMeshInst, character);
        rightEyeMeshInst.getParent().replaceChild(rightEyeMeshInst, rightEyeBall, true);
        
        leftEyeBall.setOtherEye(rightEyeBall);
        rightEyeBall.setOtherEye(leftEyeBall);

        leftEyeBall.applyEyeBallMaterial(eyeballTexture, wm);
        rightEyeBall.applyEyeBallMaterial(eyeballTexture, wm);

        headJoint = (SkinnedMeshJoint) skeleton.getJoint("Head");
        if (headJoint == null)
            throw new RuntimeException("Unable to find head joint within skeleton!");
    }

    /**
     * Perform eyeball behavior.
     * @param deltaTime The timestep
     */
    public void update(float deltaTime)
    {
        if (!eyesWander) // Nothing to do!
            return;
                    
        // Position the target in fornt of the eyes
        Vector3f target = characterModelInst.getTransform().getWorldMatrix(false).getTranslation();
        target.addLocal(characterModelInst.getTransform().getWorldMatrix(false).getLocalZ().mult(10.0f));
        target.addLocal(headJoint.getTransform().getLocalMatrix(false).getTranslation());
        
        // Offset the target with a quick and dirty wander algorithm
        eyesWanderCounter += deltaTime;
        if (eyesWanderCounter > (float)Math.random() + 0.15f)
        {
            eyesWanderIntCounter += 1;
            eyesWanderCounter = 0.0f;
            
            float randomScale = 0.15f;
            
            if (Math.random() > 0.5)
                randomScale *= -1.0f;
            eyesWanderOffset.x += (float)Math.random() * randomScale;
            if (Math.random() > 0.5)
                randomScale *= -1.0f;
            eyesWanderOffset.y += (float)Math.random() * randomScale;
            if (Math.random() > 0.5)
                randomScale *= -1.0f;
            eyesWanderOffset.z += (float)Math.random() * randomScale;
        }

        if (eyesWanderIntCounter > 20)
        {
            eyesWanderIntCounter = 0;
            eyesWanderOffset.zero();
            if (blinkingOn && Math.random() > 0.35)
                blink();
        }
        target.addLocal(eyesWanderOffset);
        
        leftEyeBall.setTarget(target);
        rightEyeBall.setTarget(target);
        
        // Take care of blinking
        blinkingUpdate(deltaTime);
    }

    /**
     * Handle update for blinking
     * @param deltaTime
     */
    private void blinkingUpdate(float deltaTime)
    {
        if (!blinking)
            return;
        
        blinkingTimer += deltaTime;
        
        if (eyesClosed)
        {
             if (keepEyesClosed) // stay that way!
                 return;
            // open the eye
            if (blinkingTimer > blinkingShutTime)
            {
                float overallTime = blinkingShutTime + blinkingOpenTime;
                // if done
                if (blinkingTimer > overallTime)
                {
                    // set the eyed to be fully open
                    rotationBuffer.set(openEyeLid, 0, 0);
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                    blinking    = false;
                    eyesClosed  = false;
                    winkRight   = true;
                    winkLeft    = true;
                }
                else
                {
                    // transition to open eyes
                    rotationBuffer.set(closedEyeLid * ((overallTime - blinkingTimer) / overallTime) + openEyeLid, 0.0f, 0.0f);
                    if (winkLeft)
                        skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                    if (winkRight)
                        skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                }
            }
        }
        else
        {
            // close the eye
            if (blinkingTimer > blinkingCloseTime)
            {
                // set the eyed to be fully closed
                rotationBuffer.set(closedEyeLid, 0.0f, 0.0f);
                if (winkLeft)
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                if (winkRight)
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                eyesClosed     = true;
                blinkingTimer = 0.0f;
            }
            else
            {
                // transition to closed eyes
                rotationBuffer.set(closedEyeLid * blinkingTimer / blinkingCloseTime, 0.0f, 0.0f);
                if (winkLeft)
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
                if (winkRight)
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(rotationBuffer);
            }
        }
    }
    
    /**
     * Sets behavior variables.
     * Warning : after the blink setting are changed it will affect
     * any subsequent blink() calls (until blinkSettings is called again).
     * @param closeTime - how long will it take the eye lids to close
     * @param shutTime  - how long will the eye lids remain closed
     * @param openTime  - how long will it take the eye lids to open
     * @throws IllegalArgumentException If any value is less than zero
     */
    public void blinkSettings(float closeTime, float shutTime, float openTime) 
    {
        if (closeTime < 0 || shutTime < 0 || openTime < 0)
            throw new IllegalArgumentException("Negative value encountered!");
        blinkingCloseTime = closeTime;
        blinkingShutTime  = shutTime;
        blinkingOpenTime  = openTime;
    }
    
    /**
     * Initiate a blink if none is in progress.
     */
    public void blink() 
    {
        if (blinking)
            return;
        blinking      = true;
        eyesClosed    = false;
        blinkingTimer = 0.0f;
    }
    
    /**
     * This behavior is using the current blink settings for close time,
     * shut time (delay before opening) and open time of the eye lids.
     * (call blinkSettings() to set these, see warning bellow)
     * Warning : after the blink setting are changed it will affect
     * any subsequent blink() calls (until blinkSettings is called again).
     * @param rightEye - true if you wish to wink with the right eye
     * and false if you wish to wink with the left eye.
     */
    public void wink(boolean rightEye)
    {
        if (rightEye)
            winkLeft = false;
        else
            winkRight = false;
        blink();
    }
    
    /**
     * This behavior is using the current blink settings for close time,
     * shut time (delay before opening) and open time of the eye lids.
     * (call blinkSettings() to set these)
     * To open the eyes call setSkeepEyesClosed(true).
     * Note : if you want to close the eyes for a specific period of 
     * time use blink() with a long shut time setting.
     * Warning : after the blink setting are changed it will affect
     * any subsequent blink() calls (until blinkSettings is called again).
     */
    public void closeEyes()
    {
        keepEyesClosed = true;
        blink();
    }

    /**
     * Determine if the eye wandering behavior is armed.
     * @return
     */
    public boolean isEyesWander() {
        return eyesWander;
    }

    /**
     * Enable or disable th eeye wandering behavior
     * @param bEyesWander
     */
    public void setEyesWander(boolean bEyesWander) {
        this.eyesWander = bEyesWander;
    }
    
    /**
     * Will set eyesWander to false and set the target
     * of both eyes.
     * @param target A non-null target vector
     * @throws IllegalArgumentException If {@code target == null}
     */
    public void setEyesTarget(Vector3f target)
    {
        if (target == null)
            throw new IllegalArgumentException("NUll target provided!");
        eyesWander = false;
        leftEyeBall.setTarget(target);
        rightEyeBall.setTarget(target);
    }

    /**
     * Retrieve a reference to the left eyeball of the character's eyes.
     * @return The left eyeball
     */
    public EyeBall getLeftEyeBall() {
        return leftEyeBall;
    }


    /**
     * Retrieve a reference to the right eyeball of the character's eyes.
     * @return The right eyeball
     */
    public EyeBall getRightEyeBall() {
        return rightEyeBall;
    }

    /**
     * True if the eyes are closed.
     * @return True if the eyes are closed.
     */
    public boolean isEyesClosed() {
        return eyesClosed;
    }

    /**
     * True if the eyes are set to stay closed.
     * @return true if set to stay closed
     */
    public boolean isKeepEyesClosed() {
        return keepEyesClosed;
    }

    /**
     * Enable / Disable the eyes closed
     * @param keepEyesClosed True to close them
     */
    public void setKeepEyesClosed(boolean keepEyesClosed) {
        this.keepEyesClosed = keepEyesClosed;
    }

    /**
     * True if the eyes will be blinking.
     * @return True if blinking is enabled
     */
    public boolean isBlinkingOn() {
        return blinkingOn;
    }

    /**
     * Disable / Enable the blinking behavior
     * @param blinkingOn
     */
    public void setBlinkingOn(boolean blinkingOn) {
        this.blinkingOn = blinkingOn;
    }
}
