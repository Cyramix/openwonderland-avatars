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

import imi.character.ninja.NinjaContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.animation.AnimationListener.AnimationMessageType;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 * This state represents a character's behavior while sitting on the ground.
 * @author Lou Hayt
 */
public class SitOnGroundState extends GameState  
{ 
    GameContext     context = null;

    private float   counter = 0.0f;
    
    private float sittingAnimationTime = 0.7f;
    
    private boolean bIdleSittingAnimationSet        = false;
    private float   idleSittingTransitionDuration   = 0.5f;
    private float   idleSittingAnimationSpeed       = 1.0f;
    private String  idleSittingAnimationName        = null;
    
    private boolean bGettingUp                  = false;
    private boolean bGettingUpAnimationSet      = false;
    private float   gettingUpAnimationTime      = 1.0f;
    private float   gettingUpTransitionDuration = 0.1f;
    private float   gettingUpAnimationSpeed     = 1.0f;
    private String  gettingUpAnimationName      = null;
    
    /**
     * Construct a new instance with the given context.
     * @param master
     */
    public SitOnGroundState(GameContext master)
    {
        super(master);
        context = master;
        setName("Sit on the ground");
    }

    /**
     * Entry point method, validates the transition 
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toSitOnGround(Object data)
    {
        return true;
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {       
        super.stateEnter(owner);
        
        counter                  = 0.0f;
        bGettingUp               = false;
        bIdleSittingAnimationSet = false;
        bGettingUpAnimationSet   = false;
        
        // If any of the animations are not found or 
        // If using the simple sphere\scene model for the avatar the animation
        // these will never be set so this safry lets us get out of the state
        if( owner.getCharacter().getAttributes().isUseSimpleStaticModel() || context.getSkeleton() != null && (
                context.getSkeleton().getAnimationComponent().findCycle(getAnimationName(), 0) == -1 ||
                context.getSkeleton().getAnimationComponent().findCycle(getIdleSittingAnimationName(), 0) == -1 ||
                context.getSkeleton().getAnimationComponent().findCycle(getGettingUpAnimationName(), 0) == -1 ))
        {
            bGettingUpAnimationSet   = true;
            bIdleSittingAnimationSet = true;
        }
        
        // Stop the character
        context.getController().stop();
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
                            
        if (!context.isTransitioning()) 
            counter += deltaTime;
        
        if (bIdleSittingAnimationSet && (context.getTriggerState().isKeyPressed(TriggerNames.MiscAction.ordinal()) ||
            context.getTriggerState().isKeyPressed(TriggerNames.Move_Forward.ordinal()) ||
            context.getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal()) ||
            context.getTriggerState().isKeyPressed(TriggerNames.Move_Left.ordinal()) ||
            context.getTriggerState().isKeyPressed(TriggerNames.Move_Right.ordinal()) ))
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
                if (!context.isTransitioning() && bGettingUpAnimationSet)
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
            //skeleton.getAnimationState().setReverseAnimation(false);
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
            bGettingUpAnimationSet = skeleton.transitionTo(gettingUpAnimationName, false);
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
    
    public float getSittingAnimationTime() {
        return sittingAnimationTime;
    }

    public void setSittingAnimationTime(float sittingAnimationTime) {
        this.sittingAnimationTime = sittingAnimationTime;
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
