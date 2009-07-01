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

import imi.character.CharacterController;
import imi.character.avatar.AvatarContext;
import imi.character.avatar.AvatarContext.TriggerNames;
import imi.character.statemachine.GameContext;
import imi.character.statemachine.corestates.transitions.RunToWalk;

/**
 * Extends WalkState and alters the maximum acceleration and velocity of the
 * controler.
 * @author Lou Hayt
 */
public class RunState extends WalkState
{
    
    public RunState(AvatarContext master)
    {
        super(master);
        setName("Run");
    }
    
    @Override
    protected void stateEnter(GameContext owner)
    {
        super.stateEnter(owner);
        
        owner.getController().setMaxAcceleration(5.0f);
        owner.getController().setMaxVelocity(20.0f);
    }
    
    @Override
    public void update(float deltaTime)
    {
        super.update(deltaTime);
        
        CharacterController controller = context.getController();
        if (!controller.isMovingForward())
        {
            context.getTriggerState().keyReleased(TriggerNames.Movement_Modifier.ordinal());
            transition(RunToWalk.class);
        }
    }
    
    /**
     * Entry point method, validates a transition.
     * @param data - not used
     * @return true if the transition is validated
     */
    public boolean toRun(Object data)
    {   
        return true;
    }
}
