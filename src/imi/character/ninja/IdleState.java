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
package imi.character.ninja;

import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.animation.AnimationComponent.PlaybackMode;

/**
 * Represents the character's idling behavior
 * @author Lou Hayt
 */
public class IdleState extends GameState
{
    NinjaContext ninjaContext = null;
        
    private float velocityThreshhold = 0.5f;
        
    private boolean bMoveInput = false;
    private float moveCounter  = 0.0f;
    
    private boolean bTurning = false;
        
    public IdleState(NinjaContext master)
    {
        super(master);
        ninjaContext = master;
        
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
        if (ninjaContext.getController().getVelocityScalar() < velocityThreshhold)
            return true;
        return false;
    }

    private void takeAction(float deltaTime) 
    {
        float x = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_X.ordinal()];
        float y = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_Y.ordinal()];
        float z = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_Z.ordinal()];
        
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
        
        moveCounter   = 0.0f;
                
        // Stop moving
        ninjaContext.getController().stop();
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
        if (!ninjaContext.isTransitioning())
            transitionCheck();
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
