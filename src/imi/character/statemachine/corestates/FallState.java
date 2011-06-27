/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
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

package imi.character.statemachine.corestates;

import imi.character.avatar.*;
import com.jme.math.Vector3f;
import imi.character.CharacterController;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.SkeletonNode;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This state accounts for flying behavior with the character, this is a test 
 * state made by Paul.
 * @author Lou Hayt
 * @author PaulBy
 */
@ExperimentalAPI
public class FallState extends GameState 
{
    GameContext context           = null;
        
    private float impulse               = 15.0f;
    
    private float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.18f;

    private Vector3f gravity = new Vector3f();
    
    public FallState(AvatarContext master)
    {
        super(master);
        context = master;
        
        setName("Fall");
        setAnimationName("Fall");
        setTransitionDuration(0.1f);
    }
    
    /**
     * Entry point method, validates a transition.
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toFall(Object data)
    {   
        return true;
    }
     
    private void takeAction(float deltaTime) 
    {
        float x = context.getActions()[AvatarContext.ActionNames.Movement_Rotate_Y.ordinal()];
        float y = context.getActions()[AvatarContext.ActionNames.Movement_Y.ordinal()];
        float z = context.getActions()[AvatarContext.ActionNames.Movement_Z.ordinal()];

        // Debugging / Diagnostic output
//        Logger.getLogger(FlyState.class.getName()).log(Level.INFO, "TakeAction " + y);

        AvatarController controller = (AvatarController)context.getController();
        
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
        if (skeleton != null)   // avatar's skeleton might be null untill loaded
        {        
            // Reverse animation if moving backwards
            CharacterController controller = context.getController();
            
            if (!controller.isMovingForward())
                skeleton.getAnimationState().setReverseAnimation(true);
            else
                skeleton.getAnimationState().setReverseAnimation(false);
        }
        
        if (exitCounter > minimumTimeBeforeTransition)
            transitionCheck();
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {
        super.stateEnter(owner);
        
        exitCounter   = 0.0f;
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);

        // avatar's skeleton might be null untill loaded
        SkeletonNode skeleton = context.getSkeleton();
        if (skeleton != null)   
        {
            // Clean up... make sure the animation is not on reverse
            skeleton.getAnimationState().setReverseAnimation(false);
        }
    }
        
    public void setImpulse(float amount)
    {
        impulse = amount;
    }
}
