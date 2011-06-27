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

import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.objects.ChairObject;
import imi.objects.TargetObject;
import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.SkeletonNode;

/**
 * This state represents a character's behavior whilst sitting.
 * @author Lou
 */
public class SitState extends GameState 
{
    /** The owning context **/
    GameContext context = null;
    /** The chair we will be sitting in **/
    TargetObject chair = null;
    
    private float   counter = 0.0f;
    private float   sittingAnimationTime = 0.7f;
    
    private boolean bIdleSittingAnimationSet      = false;
    private float   idleSittingTransitionDuration = 0.3f;
    private float   idleSittingAnimationSpeed     = 1.0f;
    private String  idleSittingAnimationName      = null;
    
    private boolean bGettingUp                  = false;
    private boolean bGettingUpAnimationSet      = false;
    private float   gettingUpAnimationTime      = 0.8f;
    private float   gettingUpTransitionDuration = 0.05f;
    private float   gettingUpAnimationSpeed     = 1.0f;
    private String  gettingUpAnimationName      = null;

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
        // System.out.println("Enter SitState Constructor");
    }
    
    /**
     * Entry point method, validates the transition if the chair is not occupied
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toSit(Object data)
    {
        // is the chair occupied?
        if (context.getBehaviorManager().getGoal() instanceof ChairObject)
        {
            chair = (TargetObject)context.getBehaviorManager().getGoal();
            if (chair.isOccupied())
                return false;
        }
        
        return true;
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
        
        // If we no longer own the chair; set the FallFromSitState
//        if (checkForFalling())
//            return;
                    
        if (!context.isTransitioning()) 
            counter += deltaTime;
        
        if (bIdleSittingAnimationSet && ActionState.isExitRepeat(context)) {
            bGettingUp = true;
        }
        
        if (bGettingUp)
        {
            if (!bGettingUpAnimationSet)
            {
                setGettingUpAnimation();   
                if (bGettingUpAnimationSet)
                    counter = 0.0f;
            }
            
            if (counter > gettingUpAnimationTime)
            {
                // Check for possible transitions
                if (!context.isTransitioning() && bGettingUpAnimationSet)
                    transitionCheck();   
            }
        }
        else
        {
            if (counter > sittingAnimationTime && !bIdleSittingAnimationSet)
            {
                setIdleAnimation();
                triggerRelease(TriggerNames.GoSit.ordinal());
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
//            System.out.println("ddddddddddddddddddddddddddddddddddddddddddd       " +  this.sittingAnimationTime);
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
