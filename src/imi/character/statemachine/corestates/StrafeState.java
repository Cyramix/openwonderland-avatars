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

import imi.character.avatar.*;
import com.jme.math.Vector3f;
import imi.character.CharacterController;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 *
 * @author Lou Hayt
 */
public class StrafeState extends GameState
{
    GameContext context           = null;
        
    protected float impulse               = 15.0f;
    
    private float walkSpeedMax          = 2.5f;
    private float walkSpeedFactor       = 1.3f; //  The walk state is using this value and OVERWRITES the super's animation speed...
    
    protected float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.05f; // still needed?
    
    public StrafeState(AvatarContext master)
    {
        super(master);
        context = master;
        setName("SideStep");
    }
    
    /**
     * Entry point method, validates a transition.
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toSideStep(Object data)
    {   
        // Reverse animation if moving backwards
        SkeletonNode skeleton = context.getSkeleton();
        if (skeleton != null)   // avatar's skeleton might be null untill loaded
        {        
            CharacterController controller = context.getController();

            if (context.getTriggerState().isKeyPressed(TriggerNames.Move_Strafe_Left.ordinal()))
                setTransitionReverseAnimation(true);
            else
                setTransitionReverseAnimation(false);
        }
                
        return true;
    }
     
    protected void takeAction(float deltaTime) 
    {
        float x = context.getActions()[AvatarContext.ActionNames.Movement_X.ordinal()];
        
        CharacterController controller = context.getController();

        // Side step
        if (x != 0.0f)
        {
            controller.accelerate(new Vector3f(x * impulse, 0f, 0));
        }
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        
        // Behavior due to actions
        takeAction(deltaTime);
        
        exitCounter += deltaTime;
        
        // Set animation state
        SkeletonNode skeleton = context.getSkeleton();
        if (skeleton != null)   // avatar's skeleton might be null until loaded
        {        
            // Reverse animation if moving backwards
            CharacterController controller = context.getController();
            
            if (!controller.isMovingForward())
                skeleton.getAnimationState().setReverseAnimation(true);
            else
                skeleton.getAnimationState().setReverseAnimation(false);
            
            // Set animation speed
//            float velocity = context.getController().getVelocityScalar();
//            float speed    = velocity * walkSpeedFactor;
//            if (speed > walkSpeedMax)
//                speed = walkSpeedMax;
//            skeleton.getAnimationState().setAnimationSpeed(speed);
        }
        
        if (exitCounter > minimumTimeBeforeTransition)
            transitionCheck();
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {
        super.stateEnter(owner);
        
        exitCounter   = 0.0f;
        
        owner.getController().setMaxAcceleration(8.0f);
        owner.getController().setMaxVelocity(3.0f);
        
//        if (bHack)
//        {
//            // Debug
//            if (avatarContext != null && avatarContext.getSkeleton() != null)
//            {
//                int cycleIndex = avatarContext.getSkeleton().getAnimationGroup().findAnimationCycle(animationName);
//                float startTime = avatarContext.getSkeleton().getAnimationGroup().getCycle(cycleIndex).getStartTime();
//                float endTime = avatarContext.getSkeleton().getAnimationGroup().getCycle(cycleIndex).getEndTime();
//                avatarContext.getSkeleton().getAnimationGroup().getCycle(cycleIndex).setEndTime(endTime - magic);
//                avatarContext.getSkeleton().getAnimationGroup().getCycle(cycleIndex).setEndTime(startTime - magic);
//                
//                bHack = false;
//            }
//        }
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);
    }
        
    public void setImpulse(float amount)
    {
        impulse = amount;
    }

    public void setWalkSpeedFactor(float walkSpeedFactor) {
        this.walkSpeedFactor = walkSpeedFactor;
    }

    public void setWalkSpeedMax(float walkSpeedMax) {
        this.walkSpeedMax = walkSpeedMax;
    }

    public float getMinimumTimeBeforeTransition() {
        return minimumTimeBeforeTransition;
    }

    public void setMinimumTimeBeforeTransition(float minimumTimeBeforeTransition) {
        this.minimumTimeBeforeTransition = minimumTimeBeforeTransition;
    }
    
}
