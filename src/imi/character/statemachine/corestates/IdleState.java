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
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.animation.AnimationComponent.PlaybackMode;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 * Represents the character's idling behavior
 * @author Lou Hayt
 */
public class IdleState extends GameState
{
    GameContext context = null;
        
    private float velocityThreshhold = 0.5f;
        
    private boolean bMoveInput = false;
    private float moveCounter  = 0.0f;
    
    private boolean bTurning = false;

    private boolean resetBodyParts = false; // Just feet currently
        
    public IdleState(AvatarContext master)
    {
        super(master);
        context = master;
        
        setName("Idle");
        setAnimationName("Idle");
        setTransitionCycleMode(PlaybackMode.PlayOnce);
        setTransitionDuration(0.1f);
    }
    
    /**
     * Entry point method, validates a transition if
     * the velocity is low enough.
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toIdle(Object data)
    {
        if (context.getController().getVelocityScalar() < velocityThreshhold)
            return true;
        return false;
    }

    private void takeAction(float deltaTime) 
    {
        float x = context.getActions()[AvatarContext.ActionNames.Movement_X.ordinal()];
        float y = context.getActions()[AvatarContext.ActionNames.Movement_Y.ordinal()];
        float z = context.getActions()[AvatarContext.ActionNames.Movement_Z.ordinal()];
        
        // Turn
        if (x == 0.0f)
            bTurning = false;
        else
            bTurning = true;
        
        // If the input is forwards or backwards we should 
        // count for a move
        if (z != 0.0f || y !=0f)
            bMoveInput = true;
        else
            moveCounter = 0.0f;
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
        resetBodyParts = true;
        moveCounter   = 0.0f;
                
        // Stop moving
        context.getController().stop();
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        
        // Behavior due to actions
        takeAction(deltaTime);
                        
        // Count time for a possible transition
        if (bMoveInput)
        {
            moveCounter += deltaTime;
            bMoveInput = false;
        }
        
        // Check for possible transitions
        if (!context.isTransitioning())
        {
            if (resetBodyParts == true)
            {
                // do the resetting
                SkeletonNode skeleton = context.getSkeleton();
                if (skeleton != null)
                {
//                    skeleton.resetJointToBindPose("leftFoot");
//                    skeleton.resetJointToBindPose("leftFootBall");
//                    skeleton.resetJointToBindPose("rightFoot");
//                    skeleton.resetJointToBindPose("rightFootBall");
                    resetBodyParts = false;
                }
            }
            transitionCheck();
        }
    }

    public float getVelocityThreshhold() {
        return velocityThreshhold;
    }

    public void setVelocityThreshhold(float velocityThreshhold) {
        this.velocityThreshhold = velocityThreshhold;
    }
    
    public float getMoveCounter() {
        return moveCounter;
    }
        
    public boolean isTurning() {
        return bTurning;
    }

}
