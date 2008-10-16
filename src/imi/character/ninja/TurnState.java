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

import com.jme.math.Vector3f;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 *
 * @author Lou
 */
public class TurnState extends GameState
{
    NinjaContext ninjaContext = null;
                
    private boolean bTurning = true;
    
    private float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.18f;
    
    private boolean bMoveInput = false;
    private float moveCounter  = 0.0f;
    
    public TurnState(NinjaContext master)
    {
        super(master);
        ninjaContext = master;
                
        setName("Turn");
        setAnimationName("Stalk");
        setAnimationSpeed(1.5f);
        setTransitionDuration(0.2f);
    }
    
    /**
     * Entry point method, validates the transition
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toTurn(Object data)
    {
        // Ninja's skeleton might be null untill loaded
        SkeletonNode skeleton = ninjaContext.getSkeleton();
        if (skeleton == null)   
            return false;
        
        // Validate only if fully transitioned to the current animation
        if (!ninjaContext.isTransitioning())
            return true;
        
        return false;
    }
         
    private void takeAction(float deltaTime) 
    {
        float x = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_X.ordinal()];
        //float y = actions[ActionNames.Movement_Y.ordinal()];
        float z = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_Z.ordinal()];
        
        NinjaController controller = ninjaContext.getController();
        
        // Ninja's skeleton might be null untill loaded
        SkeletonNode skeleton = ninjaContext.getSkeleton();
        if (skeleton == null)  
            return;
            
        // Turn
        if (x != 0.0f)
        {
            bTurning = true;
                
            // Set animation direction
            if ( x > 0.0f)
                skeleton.getAnimationState().setReverseAnimation(true);
            else 
                skeleton.getAnimationState().setReverseAnimation(false);
            
            // Turn only if transitioned to the turning animation
            if (!ninjaContext.isTransitioning())
            {
                Vector3f direction = new Vector3f(x, 0.0f, z);
                controller.turnTo(direction);
            }
        }
        else
            bTurning = false;
        
        // If the input is forwards or backwards we should 
        // count for a move
        if (z != 0.0f)
            bMoveInput = true;
        else
            moveCounter = 0.0f;
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        // Ninja's skeleton might be null untill loaded
        SkeletonNode skeleton = ninjaContext.getSkeleton();
        
        // Make sure the animation is not set to reverse
        if (skeleton != null)
            skeleton.getAnimationState().setReverseAnimation(false);
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    { 
        super.stateEnter(owner);
                
        exitCounter   = 0.0f;
        
        moveCounter   = 0.0f;
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        
        // Behavior due to actions
        takeAction(deltaTime);
        
        exitCounter += deltaTime;
                    
        // Count time for a possible transition (to walk)
        if (bMoveInput)
        {
            moveCounter += deltaTime;
            bMoveInput = false;
        }
                                
        // Check for possible transitions
        if ( exitCounter > minimumTimeBeforeTransition &&
                !ninjaContext.isTransitioning() )
            transitionCheck();
    }

    public boolean isTurning() {
        return bTurning;
    }
    
    public float getMoveCounter() {
        return moveCounter;
    }
    
}
