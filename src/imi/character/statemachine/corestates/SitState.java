/**
 * Copyright (c) 2016, Envisiture Consulting, LLC, All Rights Reserved
 */
/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2011, Open Wonderland Foundation, All Rights Reserved
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
package imi.character.statemachine.corestates;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.objects.ChairObject;
import imi.objects.TargetObject;
import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameStateChangeListener;
import imi.character.statemachine.GameStateChangeListenerRegisterar;
import imi.scene.PTransform;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.SkeletonNode;

/**
 * This state represents a character's behavior whilst sitting.
 * @author Lou
 * @author Abhishek Upadhyay <abhiit61@gmail.com>
 */
public class SitState extends GameState 
{
    /** The owning context **/
    GameContext context = null;
    /** The chair we will be sitting in **/
    TargetObject chair = null;

    private float   counter = 0.0f;
    private float   sittingAnimationTime = 0.7f;
    private float lieDownAnimationTime = 3.0f;

    private boolean bIdleSittingAnimationSet      = false;
    private float   idleSittingTransitionDuration = 0.3f;
    private float   idleSittingAnimationSpeed     = 1.0f;
    private String  idleSittingAnimationName      = null;
    private boolean bIdleLieDownAnimationSet = false;
    private float idleLieDownTransitionDuration = 0.3f;
    private float idleLieDownAnimationSpeed = 1.0f;
    private String idleLieDownAnimationName = null;
    private boolean bLieDownAnimationSet = false;
    private float lieDownTransitionDuration = 0.3f;
    private float lieDownAnimationSpeed = 1.0f;
    private String lieDownAnimationName = null;

    private boolean bGettingUp                  = false;
    private boolean bGettingUpAnimationSet      = false;
    private float   gettingUpAnimationTime      = 0.8f;
    private float   gettingUpTransitionDuration = 0.05f;
    private float   gettingUpAnimationSpeed     = 1.0f;
    private String  gettingUpAnimationName      = null;
    private boolean bGettingUpFromLieDown = false;
    private boolean bGettingUpFromLieDownAnimationSet = false;
    private float gettingUpFromLieDownAnimationTime = 3.0f;
    private float gettingUpFromLieDownTransitionDuration = 0.05f;
    private float gettingUpFromLieDownAnimationSpeed = 1.0f;
    private String gettingUpFromLieDownAnimationName = null;

    private boolean lieDownEnable = false;
    private boolean lieDownImmediately = false;
    public Vector3f liePos = null;
    public Quaternion lieDir = null;
    public Vector3f sittingPos = null;
    public Quaternion sittingDir = null;
    private boolean sleeping = false;
    private boolean sitting = false;
    private boolean processLieDown = false;
    /**
     * if we want to lock avatar in sleep
     */
    private boolean lock = false;


    /**
     * Construct a new sitting state with the provided context as its owner
     * @param master
     */
    public SitState(AvatarContext master)
    {
        super(master);
        context = master;
        setName("Sit");
        setCycleMode(PlaybackMode.PlayOnce);
    }

    /**
     * Entry point method, validates the transition if the chair is not occupied
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toSit(Object data) 
    {
        // check if gesture is playing then set the sitting animation
        if (context.isGesturePlayingInSitting()) {
            if (context.getCharacter().getCharacterParams().isMale()) {
                setAnimationName("Male_Sitting");
            } else {
                setAnimationName("Female_Sitting");
            }
        }

        // is the chair occupied?
        if (context.getBehaviorManager().getGoal() instanceof ChairObject) 
        {
            chair = (TargetObject)context.getBehaviorManager().getGoal();
            if (chair.isOccupied()) 
                return false;
        }

        return true;
    }

    public void setLieDownPosition(Vector3f liePos1, Quaternion lieDir1) {
        liePos = liePos1;
        lieDir = lieDir1;
    }

    public void setSittingPosition(Vector3f liePos1, Quaternion lieDir1) {
        sittingPos = liePos1;
        sittingDir = lieDir1;
    }

    public void setLieDownEnable(boolean liedownEnable, boolean liedownImgEnable) {
        lieDownEnable = liedownEnable;
        lieDownImmediately = liedownImgEnable;
    }
    
    public Vector3f getLieDownPosition() {
        return liePos;
    }
    
    public Vector3f getSittingDownPosition() {
        return sittingPos;
    }

    @Override
    protected void stateExit(GameContext owner) 
    {
        super.stateExit(owner);

        // Set the chair to not occupied
        if (chair != null) 
        {
            chair.setOwner(null);
            chair.setOccupied(false);
        }
    }

    @Override
    protected void stateEnter(GameContext owner) 
    {
        super.stateEnter(owner);

        counter = 0.0f;
        bGettingUp = false;
        bIdleSittingAnimationSet = false;
        bGettingUpAnimationSet = false;

        bGettingUpFromLieDown = false;
        bIdleLieDownAnimationSet = false;
        bGettingUpFromLieDownAnimationSet = false;
        bLieDownAnimationSet = false;

        // If any of the animations are not found or 
        // If using the simple sphere\scene model for the avatar the animation
        // these will never be set so this safry lets us get out of the state
        if( owner.getCharacter().getCharacterParams().isUseSimpleStaticModel() ||
                context.getSkeleton() != null && (
                context.getSkeleton().getAnimationComponent().findCycle(getAnimationName(), 0) == -1 ||
                context.getSkeleton().getAnimationComponent().findCycle(getIdleSittingAnimationName(), 0) == -1 ||
                context.getSkeleton().getAnimationComponent().findCycle(getGettingUpAnimationName(), 0) == -1 ))
        {
            bGettingUpAnimationSet   = true;
            bIdleSittingAnimationSet = true;
            bGettingUpFromLieDownAnimationSet = true;
            bIdleLieDownAnimationSet = true;
        }

        setTransitionReverseAnimation(false); // Play this animation forward

        // Stop the character
        context.getController().stop();

        // Set the chair to occupied
        if (context.getBehaviorManager().getGoal() instanceof ChairObject) 
        {
            chair = (TargetObject)context.getBehaviorManager().getGoal();
            chair.setOwner(context.getCharacter());
            chair.setOccupied(true);
        }

        if (context.getTriggerState().isKeyPressed(TriggerNames.GoSit.ordinal())) {
            lieDownEnable = false;
        } else if (context.getTriggerState().isKeyPressed(TriggerNames.GoSitLieDown.ordinal())) {
            lieDownEnable = true;
            lieDownImmediately = false;
        } else if (context.getTriggerState().isKeyPressed(TriggerNames.LieDown.ordinal())) {
            lieDownEnable = true;
            lieDownImmediately = true;
        }
        context.getCharacter().enableShadow(false);
    }

    private void triggerRelease(int trigger) 
    {
        if (context.getTriggerState().isKeyPressed(trigger)) 
            context.triggerReleased(trigger);
    }

    @Override
    public void update(float deltaTime) 
    {
        super.update(deltaTime);

        if ((ActionState.lieDownOnLeftClick(context))) {
            triggerRelease(TriggerNames.LieDownOnClick.ordinal());
            logger.info("-----processing the trigger...");
            processLieDown = true;
        }
        
        if (!context.isTransitioning()) 
            counter += deltaTime;

        if (lieDownEnable && bIdleSittingAnimationSet) {

            if (!lieDownImmediately) {
                if (processLieDown) {
                    processLieDown = false;
                    logger.info("-----lie down trigger pressed...");
                    if (!bIdleLieDownAnimationSet) {
                        try {
                            PTransform xform = new PTransform(lieDir, liePos,
                                    new Vector3f(1, 1, 1));
                            context.getCharacter().getModelInst().setTransform(xform);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        setLieDownAnimation();
                        counter = 0;
                    } else {
                        try {
                            PTransform xform = new PTransform(sittingDir, sittingPos,
                                    new Vector3f(1, 1, 1));
                            context.getCharacter().getModelInst().setTransform(xform);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bGettingUpFromLieDown = true;
                    }
                }
                if (!bLieDownAnimationSet && ActionState.isExitRepeat(context)) {
                    bGettingUp = true;
                }
            }

            if (lieDownEnable && !bLieDownAnimationSet && lieDownImmediately) {
                try {
                    PTransform xform = new PTransform(lieDir, liePos,
                            new Vector3f(1, 1, 1));
                    context.getCharacter().getModelInst().setTransform(xform);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setLieDownAnimation();
                counter = 0;
            }

            if (bLieDownAnimationSet && counter > lieDownAnimationTime
                    && !bIdleLieDownAnimationSet && !context.isTransitioning()) {
                setLieDownIdleAnimation();
                counter = 0;
            }

            if (bIdleLieDownAnimationSet && ActionState.isExitRepeat(context)
                    && !lock) {
                try {
                    PTransform xform = new PTransform(sittingDir, sittingPos,
                            new Vector3f(1, 1, 1));
                    context.getCharacter().getModelInst().setTransform(xform);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                bGettingUpFromLieDown = true;
            }

            //for lying down
            if (bGettingUpFromLieDown) {
                if (!bGettingUpFromLieDownAnimationSet) {
                    logger.info("-----Playing reverse lying down");
                    setLieDownToSitAnimation();
                    if (bGettingUpFromLieDownAnimationSet) {
                        counter = 0.0f;
                    }
                }
                if (counter > gettingUpFromLieDownAnimationTime && !context.isTransitioning()) {
                    logger.info("-----Playing reverse lying down finish");
                    if (lieDownImmediately) {
                        bGettingUp = true;
                        counter = 0;
                    } else {
                        bIdleSittingAnimationSet = false;
                        bGettingUpFromLieDown = false;
                        bIdleLieDownAnimationSet = false;
                        bGettingUpFromLieDownAnimationSet = false;
                        bLieDownAnimationSet = false;

                        counter = 0;
                    }
                }
            }
        } else {
            if (bIdleSittingAnimationSet && ActionState.isExitRepeat(context)) {
                bGettingUp = true;
            }
        }

        //for sitting
        if (bGettingUp) 
        {
            if (!bGettingUpAnimationSet) 
            {
                // do not get up if gesture is playing
                if (!context.isGesturePlayingInSitting() && !context.getTriggerState().isKeyPressed(TriggerNames.MiscActionInSitting.ordinal())) {
                    logger.info("-----Playing getting up...");
                    setGettingUpAnimation();
                }
                if (bGettingUpAnimationSet) 
                    counter = 0.0f;
            }

            if (counter > gettingUpAnimationTime) 
            {
                // Check for possible transitions
                if (context.isGesturePlayingInSitting() || context.getTriggerState().isKeyPressed(TriggerNames.MiscActionInSitting.ordinal())) 
                {
                    if (!context.isTransitioning()) {
                        transitionCheck();
                    }
                } else {
                    if ((!context.isTransitioning() && bGettingUpAnimationSet)) {
                        transitionCheck();
                    }
                }
            }
        } else {
            if (counter > sittingAnimationTime && !bIdleSittingAnimationSet) {
                logger.info("-----Playing Sitting Idel Animation...");
                setIdleAnimation();
                if (lieDownEnable) {
                    if (lieDownImmediately) {
                        triggerRelease(TriggerNames.LieDown.ordinal());
                    } else {
                        triggerRelease(TriggerNames.GoSitLieDown.ordinal());
                    }
                } else {
                    triggerRelease(TriggerNames.GoSit.ordinal());
                }
            }
        }
    }

    private boolean checkForFalling() 
    {
        if (chair.getOwner() != context.getCharacter()) 
        {
            FallFromSitState fall = (FallFromSitState) context.getStateMapping().get(FallFromSitState.class);
            if (fall != null && fall.toFallFromSit(null)) 
            {
                context.setCurrentState(fall);
                return true;
            }
        }
        return false;
    }

    /**
     * Transitions to the idle sitting animation and sets the speed and
     * transition duration.
     */
    private void setIdleAnimation() 
    {
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null) 
        {
            skeleton.getAnimationState().setTransitionDuration(idleSittingTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(idleSittingAnimationSpeed);
            skeleton.getAnimationState().setReverseAnimation(false);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.Loop);
            bIdleSittingAnimationSet = skeleton.transitionTo(idleSittingAnimationName, false);
            setAnimationSetBoolean(true);
            sitting = true;
            
            // call method of listener
            for(GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this,"sitting",true,"enter");
            }
        }
    }

    private void setLieDownIdleAnimation() {
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null) {
            skeleton.getAnimationState().setTransitionDuration(idleLieDownTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(idleLieDownAnimationSpeed);
            skeleton.getAnimationState().setReverseAnimation(false);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.Loop);
            bIdleLieDownAnimationSet = skeleton.transitionTo(idleLieDownAnimationName, false);
            setAnimationSetBoolean(true);
//            inLie = false;

            // call method of listener
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, "liedown", true, "enter");
            }
        }
    }

    private void setLieDownAnimation() {
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null) {
            skeleton.getAnimationState().setTransitionDuration(lieDownTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(lieDownAnimationSpeed);
            skeleton.getAnimationState().setReverseAnimation(false);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.PlayOnce);
            bLieDownAnimationSet = skeleton.transitionTo(lieDownAnimationName, false);
            setAnimationSetBoolean(true);
            sleeping = true;
            sitting = false;

            // call method of listener
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, "sitting", true, "exit");
            }
        }
    }

    /**
     * Transitions to the getting up animation and sets the speed and 
     * transition duration.
     */
    private void setGettingUpAnimation() 
    { 
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null)
        {
            skeleton.getAnimationState().setTransitionDuration(gettingUpTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(gettingUpAnimationSpeed);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.PlayOnce);
            bGettingUpAnimationSet = skeleton.transitionTo(gettingUpAnimationName, true);
            // If sitting down and getting up is the same animation transitionTo will return false
            // when trying to get up immediatly after deciding to sit down... so
            if (skeleton.getAnimationState().getCurrentCycle() == skeleton.getAnimationGroup().findAnimationCycleIndex(gettingUpAnimationName)) 
            {
                bGettingUpAnimationSet = true;
            }
            setAnimationSetBoolean(true);
            sitting = false;
            
            // call method of listener
            for(GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this,"sitting",true,"exit");
            }
        }
    }

    private void setLieDownToSitAnimation() {
        // Character's skeleton might be null untill loaded
        SkeletonNode skeleton = gameContext.getSkeleton();
        if (skeleton != null) {
            skeleton.getAnimationState().setTransitionDuration(gettingUpFromLieDownTransitionDuration);
            skeleton.getAnimationState().setAnimationSpeed(gettingUpFromLieDownAnimationSpeed);
            skeleton.getAnimationState().setTransitionCycleMode(PlaybackMode.PlayOnce);
            bGettingUpFromLieDownAnimationSet = skeleton.transitionTo(lieDownAnimationName, true);
            // If sitting down and getting up is the same animation transitionTo will return false
            // when trying to get up immediatly after deciding to sit down... so
            if (skeleton.getAnimationState().getCurrentCycle() == skeleton.getAnimationGroup().findAnimationCycleIndex("Male_LieDown")) {
                bGettingUpFromLieDownAnimationSet = true;
            }
            setAnimationSetBoolean(true);
            sleeping = false;
            sitting = true;

            // call method of listener
            for (GameStateChangeListener lis : GameStateChangeListenerRegisterar.getRegisteredListeners()) {
                lis.changeInState(this, "liedown", true, "exit");
            }
        }
    }

    public String getIdleSittingAnimationName() {
        return idleSittingAnimationName;
    }

    public void setIdleSittingAnimationName(String idleSittingAnimationName) {
        this.idleSittingAnimationName = idleSittingAnimationName;
    }

    public float getIdleSittingAnimationSpeed() {
        return idleSittingAnimationSpeed;
    }

    public void setIdleSittingAnimationSpeed(float idleSittingAnimationSpeed) {
        this.idleSittingAnimationSpeed = idleSittingAnimationSpeed;
    }

    public float getIdleSittingTransitionDuration() {
        return idleSittingTransitionDuration;
    }

    public void setIdleSittingTransitionDuration(float idleSittingTransitionDuration) {
        this.idleSittingTransitionDuration = idleSittingTransitionDuration;
    }

    public float getSittingAnimationTime() {
        return sittingAnimationTime;
    }

    public void setSittingAnimationTime(float sittingAnimationTime) {
        this.sittingAnimationTime = sittingAnimationTime;
    }

    public void setSittingAnimationTime() {
        if (context.getSkeleton() != null) 
        {
            int index = context.getSkeleton().getAnimationGroup().findAnimationCycleIndex(getAnimationName());
            float duration = context.getSkeleton().getAnimationGroup().getCycle(index).getDuration();
            this.sittingAnimationTime = duration / getAnimationSpeed();
        }
    }

    public String getGettingUpAnimationName() {
        return gettingUpAnimationName;
    }

    public void setGettingUpAnimationName(String gettingUpAnimationName) {
        this.gettingUpAnimationName = gettingUpAnimationName;
    }

    public float getGettingUpAnimationSpeed() {
        return gettingUpAnimationSpeed;
    }

    public void setGettingUpAnimationSpeed(float gettingUpAnimationSpeed) {
        this.gettingUpAnimationSpeed = gettingUpAnimationSpeed;
    }

    public float getGettingUpAnimationTime() {
        return gettingUpAnimationTime;
    }

    public void setGettingUpAnimationTime(float gettingUpAnimationTime) {
        this.gettingUpAnimationTime = gettingUpAnimationTime;
    }

    public float getGettingUpTransitionDuration() {
        return gettingUpTransitionDuration;
    }

    public void setGettingUpTransitionDuration(float gettingUpTransitionDuration) {
        this.gettingUpTransitionDuration = gettingUpTransitionDuration;
    }

    public String getIdleLieDownAnimationName() {
        return idleLieDownAnimationName;
    }

    public void setIdleLieDownAnimationName(String idleLieDownAnimationName) {
        this.idleLieDownAnimationName = idleLieDownAnimationName;
    }

    public String getLieDownAnimationName() {
        return lieDownAnimationName;
    }

    public void setLieDownAnimationName(String lieDownAnimationName) {
        this.lieDownAnimationName = lieDownAnimationName;
    }

    public String getGettingUpFromLieDownAnimationName() {
        return gettingUpFromLieDownAnimationName;
    }

    public void setGettingUpFromLieDownAnimationName(String gettingUpFromLieDownAnimationName) {
        this.gettingUpFromLieDownAnimationName = gettingUpFromLieDownAnimationName;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public boolean isSitting() {
        return sitting;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }
    
    @Override
    public void notifyAnimationMessage(AnimationMessageType message) {

//        if (message == AnimationMessageType.TransitionComplete)
//        {
//            if (bGettingUp || !bIdleSittingAnimationSet)
//                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
//            else
//                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Loop);
//        }
    }

}
