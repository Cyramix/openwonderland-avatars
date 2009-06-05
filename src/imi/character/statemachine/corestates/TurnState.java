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
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 * This class represents the turning behavior of a avatarContext owner.
 * @author Lou
 */
public class TurnState extends GameState
{
    /** The owning context. **/
    GameContext context = null;

    private boolean bTurning = true;
    
    private float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.18f;
    
    private boolean bMoveInput = false;
    private float moveCounter  = 0.0f;

    /**
     * Construct a new turn state instance with the provided context
     * @param master
     */
    public TurnState(AvatarContext master)
    {
        super(master);
        context = master;
        setName("Turn");
    }
    
    /**
     * Entry point method, validates the transition
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toTurn(Object data)
    {
        // The character doesn't have a skeleton if it's represented as a sphere
        if (context.getCharacter().getAttributes().isUseSimpleStaticModel())
            return true;
        
        // avatar's skeleton might be null untill loaded
        SkeletonNode skeleton = context.getSkeleton();
        if (skeleton == null)   
            return false;
        
        // Validate only if fully transitioned to the current animation
        if (!context.isTransitioning())
            return true;
        
        return false;
    }
         
    private void takeAction(float deltaTime) 
    {
        float x = context.getActions()[AvatarContext.ActionNames.Movement_Rotate_Y.ordinal()];
        //float y = actions[ActionNames.Movement_Y.ordinal()];
        float z = context.getActions()[AvatarContext.ActionNames.Movement_Z.ordinal()];
        
        CharacterController controller = context.getController();
        
        // avatar's skeleton might be null untill loaded
        SkeletonNode skeleton = context.getSkeleton();
            
        // Turn
        if (x != 0.0f)
        {
            bTurning = true;
                
            // Set animation direction
            if (skeleton != null)  
            {
                if ( x > 0.0f)
                    skeleton.getAnimationState().setReverseAnimation(true);
                else 
                    skeleton.getAnimationState().setReverseAnimation(false);
            }
            
            // Turn only if transitioned to the turning animation
            if (!context.isTransitioning())
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
        super.stateExit(owner);
        // avatar's skeleton might be null untill loaded
        SkeletonNode skeleton = context.getSkeleton();
        
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
                !context.isTransitioning() )
            transitionCheck();
    }

    public boolean isTurning() {
        return bTurning;
    }
    
    public float getMoveCounter() {
        return moveCounter;
    }

    public float getMinimumTimeBeforeTransition() {
        return minimumTimeBeforeTransition;
    }

    public void setMinimumTimeBeforeTransition(float minimumTimeBeforeTransition) {
        this.minimumTimeBeforeTransition = minimumTimeBeforeTransition;
    }
}
