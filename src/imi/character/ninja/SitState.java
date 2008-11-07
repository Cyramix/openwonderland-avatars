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

import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.objects.Chair;
import imi.character.statemachine.GameState;
import imi.character.statemachine.GameContext;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 *
 * @author Lou
 */
public class SitState extends GameState 
{
    NinjaContext ninjaContext = null;
    
    Chair chair = null;
    
    private float counter = 0.0f;
    private float sittingAnimationTime = 0.4f;
    
    private boolean bIdleSittingAnimationSet = false;
    private float idleSittingTransitionDuration = 0.2f;
    private float idleSittingAnimationSpeed = 1.0f;
    private String idleSittingAnimationName = "Block";
    
    private boolean bGettingUp = false;
    private boolean bGettingUpAnimationSet = false;
    private float gettingUpAnimationTime = 0.4f;
    private float gettingUpTransitionDuration = 0.2f;
    private float gettingUpAnimationSpeed = 0.5f;
    private String gettingUpAnimationName = "PickUp";
                    
    public SitState(NinjaContext master)
    {
        super(master);
        ninjaContext = master;
        
        setName("Sit");
        setAnimationName("PickUp");
        setTransitionDuration(0.2f);
        setAnimationSpeed(0.5f);
    }
    
    /**
     * Entry point method, validates the transition if the chair is not occupied
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toSit(Object data)
    {
//        // is the chair occupied?
//        if (ninjaContext.getSteering().getGoal() instanceof Chair)
//        {
//            chair = (Chair)ninjaContext.getSteering().getGoal();
//            if (chair.isOccupied())
//                return false;
//        }
        
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
        
        setReverseAnimation(false);
        
        // Stop the character
        ninjaContext.getController().stop();
        
        // Set the chair to occupied
        if (ninjaContext.getSteering().getGoal() instanceof Chair)
        {
            chair = (Chair)ninjaContext.getSteering().getGoal();
            chair.setOwner(ninjaContext.getNinja());
            chair.setOccupied(true);
        }
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
                    
        if (!ninjaContext.isTransitioning()) 
            counter += deltaTime;
        
        if (bIdleSittingAnimationSet && (ninjaContext.getTriggerState().isKeyPressed(TriggerNames.Punch.ordinal()) ||
            ninjaContext.getTriggerState().isKeyPressed(TriggerNames.Move_Forward.ordinal()) ||
            ninjaContext.getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal()) ||
            ninjaContext.getTriggerState().isKeyPressed(TriggerNames.Move_Left.ordinal()) ||
            ninjaContext.getTriggerState().isKeyPressed(TriggerNames.Move_Right.ordinal()) ))
        {
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
                if (!ninjaContext.isTransitioning() && bGettingUpAnimationSet)
                    transitionCheck();   
            }
        }
        else
        {
            if (counter > sittingAnimationTime && !bIdleSittingAnimationSet)
                setIdleAnimation();   
        }
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
            bGettingUpAnimationSet = skeleton.transitionTo(gettingUpAnimationName, true);
            // If sitting down and getting up is the same animation transitionTo will return false
            // when trying to get up immediatly after deciding to sit down... so
            if (skeleton.getAnimationState().getCurrentCycle() == skeleton.getAnimationGroup().findAnimationCycle(gettingUpAnimationName))
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
        if (ninjaContext.getSkeleton() != null)
        {
            int index = ninjaContext.getSkeleton().getAnimationGroup().findAnimationCycle(getAnimationName());
            float duration = ninjaContext.getSkeleton().getAnimationGroup().getCycle(index).getEndTime() - ninjaContext.getSkeleton().getAnimationGroup().getCycle(index).getStartTime();
            this.sittingAnimationTime = duration / getAnimationSpeed();   
            System.out.println("ddddddddddddddddddddddddddddddddddddddddddd       " +  this.sittingAnimationTime);
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
        
        if (message == AnimationMessageType.TransitionComplete)
        {
            if (bGettingUp || !bIdleSittingAnimationSet)
                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.PlayOnce);
            else
                gameContext.getSkeleton().getAnimationState().setCurrentCyclePlaybackMode(PlaybackMode.Loop);
        }
    }
    
}
