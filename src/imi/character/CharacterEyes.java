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
import imi.scene.PScene;
import imi.scene.polygonmodel.PPolygonModelInstance;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;
import imi.scene.polygonmodel.parts.skinned.SkinnedMeshJoint;
import imi.scene.polygonmodel.skinned.PPolygonSkinnedMeshInstance;
import org.jdesktop.mtgame.WorldManager;

/**
 *
 * @author Lou Hayt
 */
public class CharacterEyes 
{
    private Character             character            = null;
    private SkeletonNode          skeleton             = null;
    private PPolygonModelInstance characterModelInst   = null;
    private SkinnedMeshJoint      headJoint            = null;
    
    protected EyeBall            leftEyeBall           = null;
    protected EyeBall            rightEyeBall          = null;
    
    private boolean              eyesWander            = true;
    private Vector3f             eyesWanderOffset      = new Vector3f();
    private float                eyesWanderCounter     = 0.0f;
    private int                  eyesWanderIntCounter  = 0;
    
    private final int leftEyeLid     = 24;
    private final int rightEyeLid    = 34;
    private float   closedEyeLid     = 0.0f;
    private final float   closedEyeLidMale    = -0.011f;
    private final float   closedEyeLidFemale  = -0.041f;
    
    private boolean blinkingOn      = true;
    private float   blinkingTimer   = 0.0f;
    private boolean blinking        = false;
    private boolean eyesClosed      = false;
    private boolean keepEyesClosed  = false;
    /** How long will it take to close the eyes **/
    private float blinkingCloseTime = 0.2f;
    /** How long will the eyes remain closed **/
    private float blinkingShutTime  = 0.05f;
    /** How long will it take to open the eyes **/
    private float blinkingOpenTime  = 0.4f;
    
    private boolean winkRight = true;
    private boolean winkLeft  = true;
    
    public CharacterEyes(Character character, WorldManager wm)
    {
        if (character == null)
        {
            System.out.println("CharacterEyes recieved null character in the constructor!");
            return;
        }
        
        if (character.getAttributes().isMale())
            closedEyeLid = closedEyeLidMale;
        else
            closedEyeLid = closedEyeLidFemale;
        
        this.character = character;
        skeleton = character.getSkeleton();
        characterModelInst = character.getModelInst();
        
        PPolygonSkinnedMeshInstance leftEyeMeshInst  = (PPolygonSkinnedMeshInstance) skeleton.findChild("leftEyeGeoShape");
        PPolygonSkinnedMeshInstance rightEyeMeshInst = (PPolygonSkinnedMeshInstance) skeleton.findChild("rightEyeGeoShape");
        
        leftEyeBall = new EyeBall(leftEyeMeshInst, character);
        leftEyeMeshInst.getParent().replaceChild(leftEyeMeshInst, leftEyeBall, true);
        rightEyeBall = new EyeBall(rightEyeMeshInst, character);
        rightEyeMeshInst.getParent().replaceChild(rightEyeMeshInst, rightEyeBall, true);
        
        leftEyeBall.setOtherEye(rightEyeBall);
        rightEyeBall.setOtherEye(leftEyeBall);

        leftEyeBall.applyShader(wm);
        rightEyeBall.applyShader(wm);
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
            headJoint = (SkinnedMeshJoint)characterModelInst.findChild("Head");
                    
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
            
            // open the eyed
            if (blinkingTimer > blinkingShutTime)
            {
                float overallTime = blinkingShutTime + blinkingOpenTime;
                // if done
                if (blinkingTimer > overallTime)
                {
                    // set the eyed to be fully open
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setTranslation(Vector3f.ZERO);
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setTranslation(Vector3f.ZERO);
                    blinking    = false;
                    eyesClosed  = false;
                    winkRight   = true;
                    winkLeft    = true;
                }
                else
                {
                    // transition to open eyes
                    Vector3f openingEyeLidTrans = new Vector3f(0.0f, closedEyeLid * (overallTime - blinkingTimer) / overallTime, 0.0f);
                    if (winkLeft)
                        skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setTranslation(openingEyeLidTrans);
                    if (winkRight)
                        skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setTranslation(openingEyeLidTrans);
                }
            }
        }
        else
        {
            // close the eyed
            if (blinkingTimer > blinkingCloseTime)
            {
                // set the eyed to be fully closed
                Vector3f closedEyeLidTrans = new Vector3f(0.0f, closedEyeLid, 0.0f);
                if (winkLeft)
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setTranslation(closedEyeLidTrans);
                if (winkRight)
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setTranslation(closedEyeLidTrans);
                eyesClosed     = true;
                blinkingTimer = 0.0f;
            }
            else
            {
                // transition to closed eyes
                Vector3f closingEyeLidTrans = new Vector3f(0.0f, closedEyeLid * blinkingTimer / blinkingCloseTime, 0.0f);
                if (winkLeft)
                    skeleton.getSkinnedMeshJoint(leftEyeLid).getLocalModifierMatrix().setTranslation(closingEyeLidTrans);
                if (winkRight)
                    skeleton.getSkinnedMeshJoint(rightEyeLid).getLocalModifierMatrix().setTranslation(closingEyeLidTrans);
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
