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
import imi.scene.polygonmodel.PPolygonModelInstance;

/**
 *
 * @author Lou Hayt
 */
public class StrafeState extends GameState
{
    GameContext context           = null;
        
    protected float impulse               = 15.0f;

    // RED: Not used anywhere currently, commented out to reduce memory footprint
//    private float walkSpeedMax          = 2.5f;
//    private float walkSpeedFactor       = 1.3f; //  The walk state is using this value and OVERWRITES the super's animation speed...
    
    protected float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.05f; // still needed?
    private float enterX;
    private long enterTime;

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
        PPolygonModelInstance modelInst = controller.getModelInstance();
        Vector3f localX = null;
        if (modelInst != null)
            localX = modelInst.getTransform().getLocalMatrix(false).getLocalXNormalized();
        // Side step
        if (x != 0.0f && localX != null) {
            controller.accelerate(localX.mult(x * impulse));
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
            
        }
        
        if (exitCounter > minimumTimeBeforeTransition)
            transitionCheck();
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {
        super.stateEnter(owner);

        exitCounter   = 0.0f;
        
        owner.getController().setMaxAcceleration(1.0f);
        owner.getController().setMaxVelocity(2.0f);
        ((AvatarController)owner.getController()).setSlide(true);
        
        enterX = context.getActions()[AvatarContext.ActionNames.Movement_X.ordinal()];
        enterTime = System.nanoTime();
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);

        // Nudging, like we do for other motion won't work because
        // once setSlide(false) is called the avatar no longer side steps.

        // If we were only in this state briefly (because the user just
        // tapped a key) then nudge the avatar slightly
//        if ((System.nanoTime()-enterTime)/1000000 < 100) {
//            PPolygonModelInstance modelInst = context.getController().getModelInstance();
//            Vector3f localX = null;
//            if (modelInst != null)
//                localX = modelInst.getTransform().getLocalMatrix(false).getLocalXNormalized();
//            // Side step
//            if (enterX != 0.0f && localX != null) {
//                context.getController().accelerate(localX.mult(enterX*60));
//            }
//        }
//        ((AvatarController)owner.getController()).setSlide(false);
    }
        
    public void setImpulse(float amount)
    {
        impulse = amount;
    }

    public float getMinimumTimeBeforeTransition() {
        return minimumTimeBeforeTransition;
    }

    public void setMinimumTimeBeforeTransition(float minimumTimeBeforeTransition) {
        this.minimumTimeBeforeTransition = minimumTimeBeforeTransition;
    }
    
}
