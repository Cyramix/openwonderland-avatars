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
package imi.character.statemachine.corestates.transitions;

import imi.character.statemachine.corestates.IdleState;
import imi.character.avatar.AvatarContext.ActionNames;
import imi.character.statemachine.GameState;
import imi.character.statemachine.TransitionObject;

/**
 * This class represents the transition from the Idle state to the Walk state.
 * @author Lou Hayt
 */
public class IdleToStrafe extends TransitionObject
{
    private float moveDelay = 0.075f; //  how long do we need to press forward\backward to exit idle
    
    @Override
    protected boolean testCondition(GameState state) 
    {
        if (!(state instanceof IdleState))
            return false;
        
        IdleState idle = (IdleState)state;

        if (idle.getMoveCounter() > moveDelay)
        {
            stateMessageName = "toSideStep";
            
            // If the walk action is active
            float x = state.getContext().getActions()[ActionNames.Movement_X.ordinal()];
            if (x > 0.0f || x < 0.0f)
                return state.getContext().excecuteTransition(this);
        }
        
        return false;
    }
}
