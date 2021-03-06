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

import com.jme.math.Quaternion;
import imi.character.avatar.*;
import com.jme.math.Vector3f;
import imi.character.CharacterController;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.GameState;
import imi.scene.SkeletonNode;
import imi.scene.polygonmodel.PPolygonModelInstance;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * This state accounts for flying behavior with the character, this is a test 
 * state made by Paul.
 * @author Lou Hayt
 * @author PaulBy
 */
@ExperimentalAPI
public class FlyState extends GameState 
{
    GameContext context           = null;
        
    private float impulse               = 15.0f;
        
    private float exitCounter           = 0.0f;
    private float minimumTimeBeforeTransition = 0.18f;

    private boolean restoreGravity       = false;
    private boolean restoreGravityToggle = false;

    private Vector3f gravity = new Vector3f();
    
    public FlyState(AvatarContext master)
    {
        super(master);
        context = master;
        
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
        // the new velocity
        Vector3f velocity = new Vector3f();
        velocity.x = context.getActions()[AvatarContext.ActionNames.Movement_X.ordinal()];
        velocity.y = context.getActions()[AvatarContext.ActionNames.Movement_Y.ordinal()];
        velocity.z = context.getActions()[AvatarContext.ActionNames.Movement_Z.ordinal()];
        float turn = context.getActions()[AvatarContext.ActionNames.Movement_Rotate_Y.ordinal()];

        boolean fast = context.getTriggerState().isKeyPressed(TriggerNames.Movement_Modifier.ordinal());
        
        // Debugging / Diagnostic output
//        Logger.getLogger(FlyState.class.getName()).log(Level.INFO, "TakeAction " + y);

        AvatarController controller = (AvatarController) context.getController();
        
        // are we going fast, or regular speed?
        if (fast) {
            controller.setMaxAcceleration(5.0f);
            controller.setMaxVelocity(20.0f);
        } else {
            controller.setMaxAcceleration(8.0f);
            controller.setMaxVelocity(3.0f);
        }
        
        // Turn
        if (turn != 0.0f)
        {
            Vector3f direction = new Vector3f(turn, 0.0f, velocity.z);
            controller.turnTo(direction);
        }

        
        // z direction is inverse...
        velocity.z *= -1;
        
        // scale velocity by the impulse
        velocity.multLocal(impulse); 
        controller.accelerate(velocity); 
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

        // Kill gravity while flying
        gravity.set(((AvatarController)context.getController()).getGravity());
        ((AvatarController)context.getController()).setGravity(Vector3f.ZERO);
        ((AvatarController)context.getController()).setGravityAcc(Vector3f.ZERO);
    }
    
    @Override
    protected void stateExit(GameContext owner)
    {
        super.stateExit(owner);

        if (restoreGravity)
        {
            // Restore gravity after flying
            ((AvatarController)context.getController()).setGravity(gravity);
        }
        
        if (restoreGravityToggle)
            restoreGravity = !restoreGravity;

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

    public boolean isRestoreGravity() {
        return restoreGravity;
    }

    public void setRestoreGravity(boolean restoreGravity) {
        this.restoreGravity = restoreGravity;
    }

    public boolean isRestoreGravityToggle() {
        return restoreGravityToggle;
    }

    public void setRestoreGravityToggle(boolean restoreGravityToggle) {
        this.restoreGravityToggle = restoreGravityToggle;
    }
}
