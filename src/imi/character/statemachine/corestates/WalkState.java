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
import imi.scene.SkeletonNode;

/**
 * Walking state for a character
 * @author Lou Hayt
 */
public class WalkState extends GameState 
{
    GameContext context           = null;
        
    protected float impulse               = 15.0f;

    // RED: Not used anywhere. Commented out to reduce memory footprint
//    private float walkSpeedMax          = 2.5f;
//    private float walkSpeedFactor       = 1.3f; //  The walk state is using this value and OVERWRITES the super's animation speed...
    
    protected float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.05f; // still needed?
    private long enterTime;
    private float enterX;
    private float enterZ;
    
    public WalkState(AvatarContext master)
    {
        super(master);
        context = master;
        setName("Walk");
    }
    
    /**
     * Entry point method, validates a transition.
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toWalk(Object data)
    {   
        // Reverse animation if moving backwards
        SkeletonNode skeleton = context.getSkeleton();
        if (skeleton != null)   // avatar's skeleton might be null untill loaded
        {        
            CharacterController controller = context.getController();

            if (context.getTriggerState().isKeyPressed(TriggerNames.Move_Back.ordinal()))
                setTransitionReverseAnimation(true);
            else
                setTransitionReverseAnimation(false);
        }
                
        return true;
    }
     
    protected void takeAction(float deltaTime) 
    {
        float x = context.getActions()[AvatarContext.ActionNames.Movement_Rotate_Y.ordinal()];
        //float y = actions[ActionNames.Movement_Y.ordinal()];
        float z = context.getActions()[AvatarContext.ActionNames.Movement_Z.ordinal()];
        
        CharacterController controller = context.getController();

        // Turn
        if (x != 0.0f)
        {
            Vector3f direction = new Vector3f(x, 0.0f, z);
            controller.turnTo(direction);
        }
        
        // Move Forward
        if (z != 0.0f)
        {
            controller.accelerate(z * impulse);
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
        enterX = context.getActions()[AvatarContext.ActionNames.Movement_Rotate_Y.ordinal()];
        enterZ = context.getActions()[AvatarContext.ActionNames.Movement_Z.ordinal()];
        enterTime = System.nanoTime();
        
        owner.getController().setMaxAcceleration(8.0f);
        owner.getController().setMaxVelocity(3.0f);
        
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);

        // If we were only in this state briefly (because the user just
        // tapped a key) then nudge the avatar slightly
        if ((System.nanoTime()-enterTime)/1000000 < 100) {
            if (enterZ!=0)
                context.getController().accelerate(enterZ*30);
            if (enterX != 0.0f)
            {
                Vector3f direction = new Vector3f(enterX, 0.0f, enterZ);
                context.getController().turnTo(direction);
            }
        }
    }
        
    public void setImpulse(float amount)
    {
        impulse = amount;
    }

//    public void setWalkSpeedFactor(float walkSpeedFactor) {
//        this.walkSpeedFactor = walkSpeedFactor;
//    }
//
//    public void setWalkSpeedMax(float walkSpeedMax) {
//        this.walkSpeedMax = walkSpeedMax;
//    }

    public float getMinimumTimeBeforeTransition() {
        return minimumTimeBeforeTransition;
    }

    public void setMinimumTimeBeforeTransition(float minimumTimeBeforeTransition) {
        this.minimumTimeBeforeTransition = minimumTimeBeforeTransition;
    }
    
}
