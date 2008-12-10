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

import com.jme.math.Vector3f;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.polygonmodel.parts.skinned.SkeletonNode;

/**
 *
 * @author Lou Hayt
 */
public class FlyState extends GameState 
{
    NinjaContext ninjaContext           = null;
        
    private float impulse               = 15.0f;
    
    private float walkSpeedMax          = 4.5f;
    private float walkSpeedFactor       = 1.0f;
    
    private float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.18f;
    
    public FlyState(NinjaContext master)
    {
        super(master);
        ninjaContext = master;
        
        setName("Fly");
        setAnimationName("Fly");
        setTransitionDuration(0.1f);
    }
    
    /**
     * Entry point method, validates a transition.
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toFly(Object data)
    {   
        return true;
    }
     
    private void takeAction(float deltaTime) 
    {
//        float x = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_X.ordinal()];
        //float y = actions[ActionNames.Movement_Y.ordinal()];
        float y = ninjaContext.getActions()[NinjaContext.ActionNames.Movement_Y.ordinal()];

        //System.err.println("TakeAction "+y);

        NinjaController controller = ninjaContext.getController();
        
        // Turn
//        if (x != 0.0f)
//        {
//            Vector3f direction = new Vector3f(x, 0.0f, z);
//            controller.turnTo(direction);
//        }
        
        // Move Up
        if (y != 0.0f)
        {
            controller.accelerate(Vector3f.UNIT_Y.mult(y * impulse));
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
        SkeletonNode skeleton = ninjaContext.getSkeleton();
        if (skeleton != null)   // Ninja's skeleton might be null untill loaded
        {        
            // Reverse animation if moving backwards
            NinjaController controller = ninjaContext.getController();
            
            if (controller.isMovingForward())
                skeleton.getAnimationState().setReverseAnimation(true);
            else
                skeleton.getAnimationState().setReverseAnimation(false);
            
            // Set animation speed
            float velocity = ninjaContext.getController().getVelocityScalar();
            float speed    = velocity * walkSpeedFactor;
            if (speed > walkSpeedMax)
                speed = walkSpeedMax;
            skeleton.getAnimationState().setAnimationSpeed(speed);
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
        
        // Ninja's skeleton might be null untill loaded
        SkeletonNode skeleton = ninjaContext.getSkeleton();
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

    public void setWalkSpeedFactor(float walkSpeedFactor) {
        this.walkSpeedFactor = walkSpeedFactor;
    }

    public void setWalkSpeedMax(float walkSpeedMax) {
        this.walkSpeedMax = walkSpeedMax;
    }
    
}
