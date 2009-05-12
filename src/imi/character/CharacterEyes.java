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
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import java.util.logging.Logger;
import org.jdesktop.mtgame.WorldManager;

/**
 * This class performs eyeball related behavior and handles general eyeball
 * management.
 * @author Lou Hayt
 */
public class CharacterEyes 
{
    /** Logger ref **/
    private static final Logger logger = Logger.getLogger(CharacterEyes.class.getName());

    /** The owner of these eyes **/
    private Character             character            = null;
    /** Skeleton of the above **/
    private SkeletonNode          skeleton             = null;
    /** Model instance of the above above **/
    private PPolygonModelInstance characterModelInst   = null;
    /** Reference to the head joint **/
    private SkinnedMeshJoint      headJoint            = null;
    /** The eyes! **/
    protected EyeBall            leftEyeBall           = null;
    protected EyeBall            rightEyeBall          = null;
    /** True if eyes should randomly wander (looks more lifelike) **/
    private boolean              eyesWander            = true;
    /** Used as a buffer for wandering calculations **/
    private Vector3f             eyesWanderOffset      = new Vector3f();
    /** Used to track how long it has been since the last wandering target **/
    private float                eyesWanderCounter     = 0.0f;
    /** Count the number of wander targets for blinking algo **/
    private int                  eyesWanderIntCounter  = 0;

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

    /**
     * Construct a new set of peepers with the specified characteristics.
     * @param eyeballTexture Relative path of the eyeball texture to use
     * @param character The owner of the eyeballs
     * @param wm The world manager!
     */
    public CharacterEyes(String eyeballTexture, Character character, WorldManager wm)
    {
        if (character == null)
        {
            logger.severe("CharacterEyes recieved null character in the constructor!");
            return;
        }
        
        if (character.getAttributes().isMale())
        {
            closedEyeLid = closedEyeLidMale;
            openEyeLid = openEyeLidMale;
        }
        else
        {
            closedEyeLid = closedEyeLidFemale;
            openEyeLid = openEyeLidFemale;
        }
        
        this.character = character;
        skeleton = character.getSkeleton();
        characterModelInst = character.getModelInst();
        
        rightEyeLid = skeleton.getSkinnedMeshJointIndex("rightEyeLid");
        leftEyeLid  = skeleton.getSkinnedMeshJointIndex("leftEyeLid");
//        System.out.println(rightEyeLidCheck + " and the left one is " + leftEyeLidCheck);
        
        PPolygonSkinnedMeshInstance leftEyeMeshInst  = (PPolygonSkinnedMeshInstance) skeleton.findChild("leftEyeGeoShape");
        PPolygonSkinnedMeshInstance rightEyeMeshInst = (PPolygonSkinnedMeshInstance) skeleton.findChild("rightEyeGeoShape");
        if (leftEyeMeshInst == null || rightEyeMeshInst == null)
        {
            logger.severe("Eyeball meshes not located, aborting EyeBall construction!");
            return;
        }

        leftEyeBall = new EyeBall(leftEyeMeshInst, character);
        leftEyeMeshInst.getParent().replaceChild(leftEyeMeshInst, leftEyeBall, true);
        rightEyeBall = new EyeBall(rightEyeMeshInst, character);
        rightEyeMeshInst.getParent().replaceChild(rightEyeMeshInst, rightEyeBall, true);
        
        leftEyeBall.setOtherEye(rightEyeBall);
        rightEyeBall.setOtherEye(leftEyeBall);

        leftEyeBall.applyEyeBallMaterial(eyeballTexture, wm);
        rightEyeBall.applyEyeBallMaterial(eyeballTexture, wm);
    }

    /**
     * Perform eyeball behavior.
     * @param deltaTime The timestep
     */
    public void update(float deltaTime)
    {
        if (!eyesWander || leftEyeBall == null || rightEyeBall == null)
            return;

        if (headJoint == null)
            headJoint = (SkinnedMeshJoint) skeleton.getJoint("Head");
                    
        // Position the target in fornt of the eyes
        Vector3f target = characterModelInst.getTransform().getWorldMatrix(false).getTranslation();
        target.addLocal(characterModelInst.getTransform().getWorldMatrix(false).getLocalZ().mult(10.0f));
        target.addLocal(headJoint.getTransform().getLocalMatrix(false).getTranslation());
        
        // Offset the target with a quick and dirty wander algorythim
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
    
    private void blinkingUpdate(float deltaTime)
    {
        if (!blinking)
            return;
        
        blinkingTimer += deltaTime;
        
        if (eyesClosed)
        {
             if (keepEyesClosed)
                 return;
            
            // open the eye
            if (blinkingTimer > blinkingShutTime)
            {
                float overallTime = blinkingShutTime + blinkingOpenTime;
                // if done
                if (blinkingTimer > overallTime)
                {
                    // set the eyed to be fully open
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(new Vector3f(openEyeLid, 0, 0));
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(new Vector3f(openEyeLid, 0, 0));
                    blinking    = false;
                    eyesClosed  = false;
                    winkRight   = true;
                    winkLeft    = true;
                }
                else
                {
                    // transition to open eyes
                    Vector3f openingEyeLidRot = new Vector3f(closedEyeLid * ((overallTime - blinkingTimer) / overallTime) + openEyeLid, 0.0f, 0.0f);
                    if (winkLeft)
                        skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(openingEyeLidRot);
                    if (winkRight)
                        skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(openingEyeLidRot);
                }
            }
        }
        else
        {
            // close the eyed
            if (blinkingTimer > blinkingCloseTime)
            {
                // set the eyed to be fully closed
                Vector3f closedEyeLidRot = new Vector3f(closedEyeLid, 0.0f, 0.0f);
                if (winkLeft)
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(closedEyeLidRot);
                if (winkRight)
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(closedEyeLidRot);
                eyesClosed     = true;
                blinkingTimer = 0.0f;
            }
            else
            {
                // transition to closed eyes
                Vector3f closingEyeLidRot = new Vector3f(closedEyeLid * blinkingTimer / blinkingCloseTime, 0.0f, 0.0f);
                if (winkLeft)
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setRotation(closingEyeLidRot);
                if (winkRight)
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setRotation(closingEyeLidRot);
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
     */
    public void blinkSettings(float closeTime, float shutTime, float openTime) 
    {
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
    
    public boolean isEyesWander() {
        return eyesWander;
    }

    public void setEyesWander(boolean bEyesWander) {
        this.eyesWander = bEyesWander;
    }
    
    /**
     * Will set eyesWander to false and set the target
     * of both eyes.
     * @param target
     */
    public void setEyesTarget(Vector3f target)
    {
        eyesWander = false;
        leftEyeBall.setTarget(target);
        rightEyeBall.setTarget(target);
    }
    
    public EyeBall getLeftEyeBall() {
        return leftEyeBall;
    }

    public EyeBall getRightEyeBall() {
        return rightEyeBall;
    }

    public boolean isEyesClosed() {
        return eyesClosed;
    }

    public boolean isKeepEyesClosed() {
        return keepEyesClosed;
    }

    public void setKeepEyesClosed(boolean keepEyesClosed) {
        this.keepEyesClosed = keepEyesClosed;
    }

    public boolean isBlinkingOn() {
        return blinkingOn;
    }

    public void setBlinkingOn(boolean blinkingOn) {
        this.blinkingOn = blinkingOn;
    }
}
